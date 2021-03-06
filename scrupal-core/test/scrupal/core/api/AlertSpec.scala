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
package scrupal.core.api

import java.util.concurrent.TimeUnit

import org.joda.time.DateTime
import scrupal.test.ScrupalSpecification
import scrupal.utils.Icons
import scrupal.utils.AlertKind

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * One line sentence description here.
 * Further description here.
 */
class AlertSpec extends ScrupalSpecification("AlertSpec") {


	"Alert" should {

		lazy val t :DateTime =  DateTime.now().plusDays(1)

		"construct with one argument and give sane results" in {
			val alert =  new Alert( 'Alert, "Alert", "Description", "<span>Note Message</span>", AlertKind.Note )
			alert.message.toString must beEqualTo("<span>Note Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Note)
			alert.iconKind must beEqualTo(Icons.info)
			alert.cssClass must beEqualTo("alert-info")
			alert.prefix must beEqualTo("Note:")
		}

		"construct with two arguments and give sane results" in {
			val alert = new Alert('Alert, "Alert", "Description", "<span>Danger Message</span>", AlertKind.Danger )
			alert.message.toString must beEqualTo("<span>Danger Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Danger)
			alert.iconKind must beEqualTo(Icons.warning_sign)
			alert.cssClass must beEqualTo("alert-danger")
			alert.prefix must beEqualTo("Danger!")
		}

		"construct with three arguments and give sane results" in {
			val alert = Alert('Alert, "Alert", "Description", "<span>Warning Message</span>", AlertKind.Warning,
        AlertKind.toIcon(AlertKind.Warning), AlertKind.toPrefix(AlertKind.Warning),
        AlertKind.toCss(AlertKind.Warning), Some(t) )
			alert.message.toString must beEqualTo("<span>Warning Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Warning)
			alert.iconKind must beEqualTo(Icons.exclamation)
			alert.prefix must beEqualTo("Warning!")
			alert.cssClass must beEqualTo("")
			alert.expiry.get must beEqualTo( t)
		}

		"construct with four arguments and give sane results" in {
			val alert = Alert('Alert, "Alert", "Description", "<span>Caution Message</span>", AlertKind.Caution,
        Icons.align_center, "Alignment!", "", Some(new DateTime(0)))
			alert.message.toString must beEqualTo("<span>Caution Message</span>")
			alert.alertKind must beEqualTo(AlertKind.Caution)
			alert.iconKind must beEqualTo(Icons.align_center)
			alert.cssClass must beEqualTo("")
			alert.prefix must beEqualTo("Alignment!")
		}

		"produce correct icon html" in {
			val alert = new Alert('A, "A", "Description", "<span>Html Message</span>", AlertKind.Note)
			alert.iconHtml.toString must beEqualTo("<i class=\"icon-info\"></i>")
		}

    "save to and fetch from the DB" in  {
      withSchema { schema: Schema =>
				withEmptyDB(schema.nodes.db.name) { db =>
					val future = db.dropCollection("alerts") flatMap { result =>
						val a1 = new Alert('foo, "Alert", "Description", "Message", AlertKind.Warning )
						schema.alerts.insert(a1) flatMap { wr =>
							schema.alerts.fetch('foo) map { optAlert =>
								val a2 = optAlert.get
								val saved_time = a2.expires
								a2._id must beEqualTo('foo)
								a1._id must beEqualTo('foo)
							}
						}
					}
					Await.result(future,Duration(5,TimeUnit.SECONDS))
				}
      }
    }
	}
}
