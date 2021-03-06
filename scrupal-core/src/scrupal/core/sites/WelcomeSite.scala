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

package scrupal.core.sites

import org.joda.time.DateTime
import scrupal.core.CoreModule
import scrupal.core.api._
import scrupal.core.entities.EchoEntity
import scrupal.core.html.PlainPage
import scrupal.core.nodes.{HtmlNode, MarkedDocNode}
import shapeless.{::, HNil}
import spray.routing.PathMatcher
import spray.routing.PathMatchers._

import scalatags.Text.all._

case class WelcomeSite(sym: Identifier) extends Site(sym) {
  val name: String = "Welcome To Scrupal"
  val description: String = "The default 'Welcome To Scrupal' site that is built in to Scrupal"
  val modified: Option[DateTime] = Some(DateTime.now)
  val created: Option[DateTime] = Some(new DateTime(2014,11,18,17,40))
  override val themeName = "cyborg"
  def host: String = ".*"
  val siteRoot: Node =
    new HtmlNode (
      "Main index page for Welcome To Scrupal Site",
      WelcomeSite.WelcomePageTemplate,
      modified=Some(DateTime.now),
      created=Some(new DateTime(2014, 11, 18, 18, 0))
    )

  object DocPathToDocs extends FunctionalNodeActionProducer(PathMatcher("doc")/Segments, {
    (list: ::[List[String],HNil], ctxt) ⇒ new MarkedDocNode("doc","docs", list.head)
  }) {}


  override def delegates : Seq[ActionExtractor] = super.delegates ++ Seq(
    DocPathToDocs,
    NodeActionProducer(PathMatcher("index.html") | Slash | spray.routing.PathMatchers.PathEnd, siteRoot)
  )

  CoreModule.enable(this)
  EchoEntity.enable(this)
  CoreModule.enable(EchoEntity)
}

object WelcomeSite {

  object WelcomePageTemplate
    extends PlainPage('WelcomePage, "Welcome To Scrupal!", "An introductory page for Scrupal", Seq(
    div(cls:="panel panel-primary",
      div(cls:="panel-heading",
        h1(cls:="panel-title", "Welcome To Scrupal!")
      ),
      div(cls:="panel-body",
        p("""You are seeing this page because Scrupal has not found an enabled site in its database. There could
            |be lots of reasons why that happened but it is likely that this is a new installation. So, you have a
            |variety of choices you can make from here:""".stripMargin),
        ul(
          li("You can read the ", a(href:="/doc/index.md", "Scrupal Documentation")),
          li("You can ", a(href:="/config", em("Configure Scrupal")),
            """. If you've just installed Scrupal, this is what you want. The Scrupal ConfigWizard will walk you
              |through the steps to having your first, minimal, site constructed. It should take less than 2 minutes.
              |""".stripMargin
          ),
          li("You can access the echo entity. Try ", a(href:="/echoes/foo", em("this link")),
            """and others like it to access the echo entity. This is a very  simple entity that is bundled with
              |Scrupal. It serves as a reference for building Scrupal entity objects. All it does is turn each
              |request into an HTML page that displays the request in a readable format. You can use this to learn
              |Scrupal at the code level or you can use it for benchmarking Scrupal's internal machinery without
              |latency introduced by modules, nodes, databases, etc.""".stripMargin
          )
        ),
        p("""There's lots more you can do with Scrupal, of course, but you need to get configured first or solve the
            |reason why this page came up. Some of the potential reasons are:""".stripMargin),
        ul(
          li("Your MongoDB database that scrupal was using got damaged somehow."),
          li("There is a network failure between your Scrupal machine and your MongoDB server machine."),
          li("A coding problem has made the procedure of finding sites yield an empty list")
        )
      )
    )
  ))
}
