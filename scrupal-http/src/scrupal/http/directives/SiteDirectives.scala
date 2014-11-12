/**********************************************************************************************************************
 * Copyright © 2014 Reactific Software, Inc.                                                                          *
 *                                                                                                                    *
 * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
 *                                                                                                                    *
 * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
 * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
 * or (at your option) any later version.                                                                             *
 *                                                                                                                    *
 * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
 * details.                                                                                                           *
 *                                                                                                                    *
 * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
 * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
 **********************************************************************************************************************/

package scrupal.http.directives

import scrupal.core.api.Site
import scrupal.core.Scrupal
import spray.routing._
import spray.routing.Directives._


/** Spray Routing Directives For Scrupal Sites
  * This provides a few routing directives that deal with sites being enabled and requiring a certain scheme
  */
trait SiteDirectives {

  def siteScheme(site: Site) = {
    scheme("http").hrequire { hnil => !site.requireHttps } |
      scheme("https").hrequire { hnil => site.requireHttps }
  }

  def siteEnabled(site: Site) = {
    validate(site.isEnabled,s"Site '${site.name}' is disabled.")
  }

  def scrupalIsReady(scrupal: Scrupal) = {
    validate(scrupal.isReady, s"Scrupal is not configured!")
  }

  /*

  schemeName { scheme =>
    reject(ValidationRejection(s"Site '${site._id.name}' does not support scheme'$scheme'"))
  }
}

    require
    validate(!site.requireHttps, s"Site '${site._id.name}' does not permit https.") {
      extract (ctx => provide(site) )
    }
  } ~
    scheme("https") {
      validate(site.requireHttps, s"Site '${site._id.name}' requires https.") { hnil =>
        extract(ctx => site)
      }
    } ~
}
*/

  def site: Directive1[Site] = {
    hostName.flatMap { host:String ⇒
      Site.forHost(host) match {
        case Some(site) ⇒
          siteScheme(site) & siteEnabled(site) & extract(ctx => site)
        case None ⇒
          reject(ValidationRejection(s"No site defined for host '$host'."))
      }
    }
  }
}



