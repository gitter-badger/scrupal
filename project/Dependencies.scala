/**********************************************************************************************************************
 * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
 *                                                                                                                    *
 * Copyright © 2015 Reactific Software LLC                                                                            *
 *                                                                                                                    *
 * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
 * except in compliance with the License. You may obtain a copy of the License at                                     *
 *                                                                                                                    *
 *        http://www.apache.org/licenses/LICENSE-2.0                                                                  *
 *                                                                                                                    *
 * Unless required by applicable law or agreed to in writing, software distributed under the                          *
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
 * either express or implied. See the License for the specific language governing permissions                         *
 * and limitations under the License.                                                                                 *
 **********************************************************************************************************************/


import sbt.Keys._
import sbt._

/** Build Dependencies
  * This trait can be mixed in to get all of Scrupals repository resolvers and dependent libraries.
  */
trait Dependencies
{
  // val scrupal_org_releases    = "Scrupal.org Releases" at "http://scrupal.github.org/mvn/releases"
  val google_sedis            = "Google Sedis" at "http://pk11-scratch.googlecode.com/svn/trunk/"
  val jcenter_repo            = "JCenter" at "http://jcenter.bintray.com/"

//val scala_lang              = "Scala Language" at "http://mvnrepository.com/artifact/org.scala-lang/"
//val geolocation             = "geolocation repository" at "http://blabluble.github.com/modules/releases/"

  val all_resolvers : Seq[MavenRepository] = Seq (
    google_sedis, jcenter_repo
  )

  // Compiler Features
  val parser_combinators      = "org.scala-lang.modules"    %% "scala-parser-combinators" % "1.0.4"
  val scala_xml               = "org.scala-lang.modules"    %% "scala-xml"                % "1.0.4"

  // Things we borrow from Play Framework
  val playV = "2.4.0"
  val play_anorm              = "com.typesafe.play"         %% "anorm"                    % playV
  val play_cache              = "com.typesafe.play"         %% "play-cache"               % playV
  val play_docs               = "com.typesafe.play"         %% "play-docs"                % playV
  val play_filters            = "com.typesafe.play"         %% "filters-helpers"          % playV
  val play_iteratees          = "com.typesafe.play"         %% "play-iteratees"           % playV
  val play_jdbc               = "com.typesafe.play"         %% "play-jdbc"                % playV
  val play_json               = "com.typesafe.play"         %% "play-json"                % playV
  val play_ws                 = "com.typesafe.play"         %% "play-ws"                  % playV

  // Play Plugins
  val mail_plugin             = "com.typesafe.play.plugins" %% "play-plugins-mailer"      % "3.0.1"
  val silhouette              = "com.mohiva"                %% "play-silhouette"          % "3.0.0-RC1"
//val play_plugins_redis      = "com.typesafe.play.plugins" %% "play-plugins-redis"       % "2.3.1"

  // Spray Stuff
  val akkaV = "2.3.9"
  val sprayV = "1.3.2"
  val spray_can               = "io.spray"            %%  "spray-can"             % sprayV
  val spray_routing           = "io.spray"            %%  "spray-routing"         % sprayV
  val spray_http              = "io.spray"            %%  "spray-http"            % sprayV
  val spray_httpx             = "io.spray"            %%  "spray-httpx"           % sprayV
  val spray_caching           = "io.spray"            %%  "spray-caching"         % sprayV

  // Akka Stuff
  val akka_actor              = "com.typesafe.akka"   %% "akka-actor"             % akkaV
  val akka_slf4j              = "com.typesafe.akka"   %% "akka-slf4j"             % akkaV
  val akka_http               = "com.typesafe.akka"   %% "akka-http-experimental" % "1.0-RC3"

  // Fundamental Libraries
  val shapeless               = "com.chuusai"               %% "shapeless"                % "2.2.1"
  val scala_arm               = "com.jsuereth"              %% "scala-arm"                % "1.4"

  // Databass, Caches, Serialization, Data Storage stuff
  val rxmongo                 = "com.reactific"             %% "rxmongo"                  % "0.1.0-SNAPSHOT"
  val reactivemongo           = "org.reactivemongo"         %% "reactivemongo"            % "0.11.0-SNAPSHOT"
  val livestream_scredis      = "com.livestream"            %% "scredis"                  % "2.0.6"
  val akka_kryo_serialization = "com.github.romix.akka"     %% "akka-kryo-serialization"  % "0.3.3"
  val scala_pickling          = "org.scala-lang.modules"    %% "scala-pickling"           % "0.10.1"

  // UI Based Stuff
  val marked                  = "org.webjars"               %  "marked"                   % "0.3.2-1"
  val fontawesome             = "org.webjars"               %  "font-awesome"             % "4.2.0"
  val scalatags               = "com.scalatags"             %% "scalatags"                % "0.5.0-SNAPSHOT"

  // Hashing Algorithms
  val pbkdf2                  = "io.github.nremond"         %% "pbkdf2-scala"             % "0.4"
  val bcrypt                  = "org.mindrot"               % "jbcrypt"                   % "0.3m"
  val scrypt                  = "com.lambdaworks"           % "scrypt"                    % "1.4.0"

  // Logging
  val grizzled_slf4j          = "org.clapper"               %% "grizzled-slf4j"           % "1.0.2"
  val logback_classic         = "ch.qos.logback"            %  "logback-classic"          % "1.1.3"

  // Miscellaneous
  val osgi_core               = "org.osgi"                  % "org.osgi.core"             % "6.0.0"
  val guava                   = "com.google.guava"          % "guava"                     % "16.0.1"
  val joda_time               = "joda-time"                 %  "joda-time"                % "2.5"
  val joda_convert            = "org.joda"                  % "joda-convert"              % "1.2"
  val config                  =  "com.typesafe"             %  "config"                   % "1.2.1"
  val commons_lang3           = "org.apache.commons"        % "commons-lang3"             % "3.3.2"
  val hsp                     = "com.reactific"             %% "hotspot-profiler"         % "0.1.0-SNAPSHOT"
  // Test Libraries

//val icu4j                   = "com.ibm.icu"          % "icu4j"                  % "51.1"
//val geolocation             =  "com.edulify"        %% "geolocation"            % "1.1.0"

/*  val all_dependencies : Seq[ModuleID] = Seq(
    play_cache, play_filters, play_test, play_docs, play_ws,
    mailer_plugin,
    reactivemongo,
    pbkdf2, bcrypt, scrypt,
    osgi_core, slf4j,
    webjars_play,
    requirejs, requirejs_domready,
    angularjs, angular_drag_drop, angular_multi_select,
    angular_ui, angular_ui_bootstrap, angular_ui_router, angular_ui_utils, angular_ui_calendar,
    marked, fontawesome
  )
  */

  object Test {
    val akka_testkit     = "com.typesafe.akka"      %% "akka-testkit"       % akkaV       % "test"
    val specs2           = "org.specs2"             %% "specs2-core"        % "3.6.1"     % "test"
    val commons_io       = "commons-io"              %  "commons-io"        % "2.4"       % "test"
    val nu_validator     = "nu.validator.htmlparser" % "htmlparser"         % "1.4"       % "test"
    val play_specs2      = "com.typesafe.play"      %% "play-specs2"        % playV       % "test"
    val play_test        = "com.typesafe.play"      %% "play-test"          % playV       % "test"
    val silhouette_test  = "com.mohiva"        %% "play-silhouette-testkit" % "3.0.0-RC1" % "test"
  }

  val root_dependencies : Seq[ModuleID] = Seq(
  )

  val common_dependencies : Seq[ModuleID] = Seq(
    guava, parser_combinators, scala_xml, scala_pickling,
    grizzled_slf4j, akka_slf4j, logback_classic,
    Test.specs2, Test.play_test, Test.play_specs2
  )

  val utils_dependencies : Seq[ModuleID] = Seq(
    scalatags, pbkdf2, bcrypt, scrypt, joda_time, joda_convert, config, shapeless
  ) ++ common_dependencies

  val storage_dependencies : Seq[ModuleID] = Seq(
    play_json, scala_pickling, akka_kryo_serialization
  ) ++ common_dependencies

  val filesys_dependencies : Seq[ModuleID] = Seq( )

  val types_dependencies : Seq[ModuleID] = Seq(
    play_json, shapeless
  ) ++ common_dependencies

  val api_dependencies : Seq[ModuleID] = Seq(
    play_json, scala_pickling, shapeless, akka_http
  ) ++ common_dependencies

  val db_dependencies : Seq[ModuleID] = Seq(
    reactivemongo, rxmongo, play_iteratees, play_json
  ) ++ common_dependencies

  val ui_dependencies : Seq[ModuleID] = Seq (
    scalatags, spray_http, spray_httpx, spray_caching, spray_routing, spray_can, marked, fontawesome,
    commons_lang3, scala_arm, livestream_scredis, akka_actor, play_iteratees,
    Test.akka_testkit, Test.commons_io, Test.nu_validator
  ) ++ common_dependencies

  val core_dependencies : Seq[ModuleID] = Seq(
    commons_lang3, scala_arm, scala_pickling, livestream_scredis, akka_actor, play_iteratees,
    Test.akka_testkit, Test.commons_io, Test.nu_validator
  ) ++ common_dependencies

}
