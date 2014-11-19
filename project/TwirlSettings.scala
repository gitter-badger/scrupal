
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

import sbt._
import sbt.Keys._
import play.twirl.sbt.Import.TwirlKeys

/**
 * Settings for building Scrupal. These are common settings for each sub-project.
 * Only put things in here that must be identical for each sub-project. Otherwise,
 * Specialize below in the definition of each Project object.
 */
trait TwirlSettings {
  lazy val twirlSettings = Seq[Setting[_]](
    sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value
  )

  lazy val core_imports = Seq( TwirlKeys.templateImports += "scrupal.core.views.%format%._"  )
  lazy val http_imports = core_imports ++ Seq( TwirlKeys.templateImports += "scrupal.http.views.%format%._" )
  lazy val web_imports  = http_imports ++ Seq( TwirlKeys.templateImports += "scrupal.web.views.%format%._" )
  lazy val top_imports = web_imports ++ Seq( TwirlKeys.templateImports += "scrupal.views.%format%._" )

  lazy val twirlSettings_core = twirlSettings ++ core_imports
  lazy val twirlSettings_http = twirlSettings ++ http_imports
  lazy val twirlSettings_web = twirlSettings ++ web_imports
  lazy val twirlSettings_top = twirlSettings ++ top_imports
}
