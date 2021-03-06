/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation,                                    *
 * either version 3 of the License, or (at your option) any later version.                                            *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                               *
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                          *
 * See the GNU General Public License for more details.                                                               *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal.                              *
 * If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                         *
 **********************************************************************************************************************/

package scrupal.core.http

import akka.event.Logging._
import scrupal.core.api._
import scrupal.utils.{Registrable, Registry}
import spray.http.StatusCodes.{NotFound, _}
import spray.http.{HttpRequest, HttpResponse, _}
import spray.routing._
import spray.routing.directives.LogEntry

/** Abstract Controller
  *
  * A controller in scrupal is a route provider and action generator. It provides a route object which
  * should separate valid from invalid routes and generate Actions for the valid ones.
  *
  * Created by reidspencer on 10/29/14.
  */
trait Controller extends Registrable[Controller]
                         with Directives with ScrupalMarshallers with RequestLoggers {

  /** The priority of this controller for routing
    * This affects the controller's placement in the list of controllers that form the route processing.
    * Lower numbers increase the priority of the controller and move it towards the start of the list.
    * Scrupal's own controllers work within a -100 to 100 range and it is recommended that other
    * controllers operate within that same range. Note that this just helps optimize the processing
    * of routes.
    */
  def priority: Int

  /** The routes that this controller provides
    * Note that this does not include checking of the context path. That will have already done before the
    * routes here are invoked. So, this should only check within that 'context"
    */
  def routes(implicit scrupal: Scrupal): Route

  /** Required method for registration */
  def registry: Registry[Controller] = Controller

  def request_context = extract( rc ⇒ rc )

  def site(scrupal: Scrupal): Directive1[Site] = {
    hostName.flatMap { host:String ⇒
      val sites = Site.forHost(host)
      if (sites.isEmpty)
        reject(ValidationRejection(s"No site defined for host '$host'."))
      else {
        val site = sites.head
        if (site.isEnabled(scrupal)) {
          schemeName.flatMap { scheme: String ⇒
            validate((scheme == "https") == site.requireHttps,
                      s"Site '${site.name} requires ${if (site.requireHttps) "https" else "http"}.").hflatMap { hNil ⇒
              extract { ctxt ⇒ site }
            }
          }
        } else {
          reject(ValidationRejection(s"Site '${site.name} is disabled."))
        }
      }
    }
  }
}

abstract class BasicController(val id : Identifier, val priority: Int = 0) extends Controller

/** A Controller For ActionProviders
  *
  * Scrupal API module provides the ActionProvider that can convert a path
  */
class ActionProviderController extends BasicController('ActionProviderController, 0) with PathHelpers {

  def routes(implicit scrupal: Scrupal): Route = {
    site(scrupal) { site: Site ⇒
      rawPathPrefix(Slash) {
/*        val x = FormDataMarshaller
        entity(as[FormData]){ emailData=>

        } */
        request_context { ctxt: RequestContext ⇒
          implicit val context = Context(scrupal, ctxt, site)
          site.extractAction(context) match {
            case Some(action) ⇒ complete {
              makeMarshallable {
                val future_result = action.dispatch
                future_result
              }
            }
            case None ⇒ reject
          }
        }
      }
    }
  }
}

trait RequestLoggers {
  def showRequest(request: HttpRequest) = LogEntry(request.uri, InfoLevel)

  def showAllResponses(request: HttpRequest) : Any ⇒ Option[LogEntry] = {
    case x: HttpResponse => {
      println (s"Normal: $request")
      createLogEntry(request,   x.status + " " + x.toString)
    }
    case Rejected(rejections) => {
      println (s"Rejection: $request")
      createLogEntry(request,   " Rejection " + rejections.toString())
    }
    case x => {
      println (s"other: $request")
      createLogEntry(request,   x.toString)
    }
  }

  def createLogEntry(request: HttpRequest, text: String): Some[LogEntry] = {
    Some(LogEntry("#### Request " + request + " => " + text, DebugLevel))
  }

  def showErrorResponses(request: HttpRequest): Any ⇒ Option[LogEntry] = {
    case HttpResponse(OK | NotModified | PartialContent, _, _, _) ⇒ None
    case HttpResponse(NotFound, _, _, _)                          ⇒ Some(LogEntry("404: " + request.uri, WarningLevel))
    case r @ HttpResponse(Found | MovedPermanently, _, _, _) ⇒
      Some(LogEntry(s"${r.status.intValue}: ${request.uri} -> ${r.header[HttpHeaders.Location].map(_.uri.toString).getOrElse("")}", WarningLevel))
    case response ⇒ Some(
      LogEntry("Non-200 response for\n  Request : " + request + "\n  Response: " + response, WarningLevel))
  }

  def showRepoResponses(repo: String)(request: HttpRequest): HttpResponsePart ⇒ Option[LogEntry] = {
    case HttpResponse(s @ (OK | NotModified), _, _, _) ⇒ Some(LogEntry(s"$repo  ${s.intValue}: ${request.uri}", InfoLevel))
    case ChunkedResponseStart(HttpResponse(OK, _, _, _)) ⇒ Some(LogEntry(repo + " 200 (chunked): " + request.uri, InfoLevel))
    case HttpResponse(NotFound, _, _, _) ⇒ Some(LogEntry(repo + " 404: " + request.uri))
    case _ ⇒ None
  }
}


object Controller extends Registry[Controller] {
  override val registryName: String = "Controllers"
  override val registrantsName: String = "Controller"

}
