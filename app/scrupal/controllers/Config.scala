/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2013, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

package scrupal.controllers

import scrupal.api.{ConfigKey, Entity}
import play.api.libs.json.{Json, JsObject}
import play.api.mvc.{AnyContent, RequestHeader, Action}
import scrupal.db.SiteBootstrap
import play.api.Logger

/** The Entity definition for the Configuration workflow/wizard.
  * This controller handles first-time configuration and subsequent reconfiguration of the essentials of Scrupal. It
  * makes very few assumptions about the running state of Scrupal and has to operate from initial conditions where
  * not even a database is configured.
  * Further description here.
  */
object Config extends Entity('Config, "Scrupal System Configuration Entity", 'EmptyBundle ) {

  type SiteMap = Map[Symbol,String]

  object Step extends Enumeration {
    type Kind = Value
    val One_Specify_Databases = Value
    val Two_DBS_Validated = Value
    val Three_DBS_Connected = Value
    val Four_DB_Schema = Value
    val Five_Site_Created = Value
    val Six_Entity_Created = Value

    /** Determine which step we are at based on the Context provided */
    def apply(context: Context) : Step.Kind = {
      if (context.site.isDefined) {
        Five_Site_Created
      } else {
        val sites : SiteBootstrap.Site2Jdbc = SiteBootstrap.get(context)
        if (sites.isEmpty)
          One_Specify_Databases
        else {
          val valid = for (
            (site: String, (url: String, error: Option[String])) <- sites if !error.isDefined
          ) yield (site, url)
          if (valid.isEmpty)
            One_Specify_Databases
          else {
            Logger.debug("SiteBootstrap has returned: " + valid )
            Three_DBS_Connected
          }
        }
      }
    }
  }

  def get(id: String, what: String) : Action[AnyContent] = Action { implicit request : RequestHeader =>

    val step = Step(context)
    Ok(Json.obj( "state" -> step.toString) )
  }

}