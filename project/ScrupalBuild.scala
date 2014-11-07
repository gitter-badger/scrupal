/**********************************************************************************************************************
 * This file is part of Scrupal a Web Application Framework.                                                          *
 *                                                                                                                    *
 * Copyright (c) 2014, Reid Spencer and viritude llc. All Rights Reserved.                                            *
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

import com.typesafe.sbt.web.SbtWeb
import play.twirl.sbt.SbtTwirl

// import play.PlayScala
import sbt._
import sbt.Keys._

import com.typesafe.sbt.web.Import.pipelineStages
import com.typesafe.sbt.rjs.Import.rjs
import com.typesafe.sbt.gzip.Import.gzip
import com.typesafe.sbt.digest.Import.digest

object ScrupalBuild extends Build with BuildSettings with AssetsSettings with Dependencies {

  import sbtunidoc.{ Plugin => UnidocPlugin }

  val base_name = BuildInfo.projectName

  lazy val utils = Project(base_name + "-utils", file("./scrupal-utils"))
    .settings(buildSettings ++ Seq(
    libraryDependencies ++= utils_dependencies
  ):_*)
  lazy val utils_deps = utils % "compile->compile;test->test"

  lazy val db = Project(base_name + "-db", file("./scrupal-db"))
    // .enablePlugins(PlayScala)
    .settings(buildSettings ++ Seq(
      resolvers ++= all_resolvers,
      libraryDependencies ++= db_dependencies
    ):_*
  ).dependsOn(utils_deps)
  lazy val db_deps = db % "compile->compile;test->test"

  lazy val core = Project(base_name + "-core", file("./scrupal-core"))
    .enablePlugins(SbtTwirl)
    .settings(buildSettings ++ twirlSettings ++ Seq(
      resolvers ++= all_resolvers,
      libraryDependencies ++= core_dependencies
    ):_*)
    .dependsOn(utils_deps, db_deps)
  lazy val core_deps = core % "compile->compile;test->test"

  lazy val http = Project(base_name + "-http", file("./scrupal-http"))
    .settings(
      buildSettings ++ Seq(
        resolvers ++= all_resolvers,
        libraryDependencies ++= http_dependencies
    ):_*)
    .dependsOn(utils_deps, db_deps, core_deps)
  lazy val http_deps = http % "compile->compile;test->test"

  lazy val config_proj = Project(base_name + "-config", file("./scrupal-config"))
    .settings(buildSettings ++ Seq(
      resolvers ++= all_resolvers,
      libraryDependencies ++= http_dependencies
    ):_*)
    .dependsOn(utils_deps, db_deps, core_deps, http_deps)
  lazy val config_deps = config_proj % "compile->compile;test->test"

  /* TODO: This isn't building yet
  lazy val web = Project(BuildInfo.appName + "-web", file("./scrupal-web"))
    .enablePlugins(SbtWeb)
    .settings(
      buildSettings ++ Seq(
        pipelineStages := Seq(rjs, digest, gzip),
        //requireJs += "scrupal.js",
        //requireJsShim += "scrupal.js",
        resolvers ++= all_resolvers,
        libraryDependencies ++= web_dependencies
      ):_*
    )
    .dependsOn(utils_deps, db_deps, core_deps, http_deps)
  val web_deps = web % "compile->compile;test->test"
  */

  lazy val root = Project(base_name, file("."))
    .settings(buildSettings ++ docSettings ++ Seq(
      libraryDependencies ++= root_dependencies
    ):_*)
    .settings(UnidocPlugin.unidocSettings: _*)
    .dependsOn(config_deps)

  override def rootProject = Some(root)
}