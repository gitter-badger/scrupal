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

package scrupal.core

import org.joda.time.DateTime
import play.twirl.api.Html
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import scrupal.api._
import shapeless.HList
import spray.routing.PathMatchers.PathEnd

import scala.concurrent.{ExecutionContext, Future}

object AdminApp extends Application {
  def id: Symbol = 'admin
  def description: String = "The Scrupal Administrative Application"
  def name: String = "AdminApp"
  val timestamp = Some(new DateTime(2014,12,5,12,20,6))
  def created: Option[DateTime] = timestamp
  def modified: Option[DateTime] = timestamp
  val kind: Symbol = 'Admin

  object StatusBarNode extends AbstractHtmlNode {
    def description: String = "Lists the Sites"
    def created: Option[DateTime] = timestamp
    def modified: Option[DateTime] = timestamp
    def _id: BSONObjectID = BSONObjectID.generate
    def kind: Symbol = 'StatusBar
    def content(context: Context)(implicit ec: ExecutionContext): Future[Html] = {
      context.withSchema { (dbc, schema) ⇒
        schema.sites.fetchAll.map { sites =>
          scrupal.core.views.html.admin.statusBar()(context)
        }
      }
    }
  }

  object SitesNode extends AbstractHtmlNode {
    def description: String = "Lists the Sites"
    def created: Option[DateTime] = timestamp
    def modified: Option[DateTime] = timestamp
    def _id: BSONObjectID = BSONObjectID.generate
    def kind: Symbol = 'SitesNode

    def content(context: Context)(implicit ec: ExecutionContext): Future[Html] = {
      context.withSchema { (dbc, schema) ⇒
        schema.sites.fetchAll.map { sites =>
          scrupal.core.views.html.admin.siteList(sites)(context)
        }
      }
    }
  }

  object adminLayout extends LayoutNode(
    description = "Layout for Admin application",
    subordinates = Map[String, Either[NodeRef,Node]](
      "StatusBar" → Right(StatusBarNode),
      "SitesList" → Right(SitesNode)
    ),
    layout = new TwirlHtmlLayout('adminLayout, "Administration Pages Layout", scrupal.core.views.html.admin.adminLayout)
  )

  override val pathsToActions : Seq[PathMatcherToAction[_ <: HList]] = Seq(
    PathToNodeAction(PathEnd, adminLayout)
  )
}

object SiteAdminEntity extends Entity {
  def id: Symbol = 'SiteAdmin

  def kind: Symbol = 'SiteAdmin

  val key: String = "Site"

  def description: String = "An entity that handles administration of Scrupal sites."

  def instanceType: BundleType = BundleType.Empty

  override def create(context: Context, id: String, instance: BSONDocument) : Create = {
    new Create(context, id, instance) {
      override def apply() : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.create(id, instance)(context)) )
      }
    }
  }

  override def retrieve(context: Context, id: String) : Retrieve = {
    new Retrieve(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieve(id)(context)) )
      }
    }
  }

  override def update(context: Context, id: String, fields: BSONDocument) : Update = {
    new Update(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.update(id, fields)(context)) )
      }
    }
  }

  override  def delete(context: Context, id: String) : Delete = {
    new Delete(context, id) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.delete(id)(context)) )
      }
    }
  }


  override def query(context: Context, id: String, fields: BSONDocument) : Query = {
    new Query(context, id, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.query(id, fields)(context)) )
      }
    }
  }

  override def createFacet(context: Context, id: String,
    what: Seq[String], instance: BSONDocument) : CreateFacet = {
    new CreateFacet(context, id, what, instance) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.createFacet(id, what, instance)(context)) )
      }
    }
  }

  override def retrieveFacet(context: Context, id: String, what: Seq[String]) : RetrieveFacet = {
    new RetrieveFacet(context, id, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.retrieveFacet(id, what)(context)) )
      }
    }
  }

  override def updateFacet(context: Context, id: String,
    what: Seq[String], fields: BSONDocument) : UpdateFacet = {
    new UpdateFacet(context, id, what, fields) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.updateFacet(id, what, fields)(context)) )
      }
    }
  }

  override def deleteFacet(context: Context, id: String, what: Seq[String]) : DeleteFacet = {
    new DeleteFacet(context, id, what) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.deleteFacet(id, what)(context)) )
      }
    }
  }

  override def queryFacet(context: Context, id: String,
    what: Seq[String], args: BSONDocument) : QueryFacet = {
    new QueryFacet(context, id, what, args) {
      override def apply : Future[Result[_]] = {
        Future.successful( HtmlResult(scrupal.core.views.html.echo.queryFacet(id, what, args)(context)) )
      }
    }
  }
}