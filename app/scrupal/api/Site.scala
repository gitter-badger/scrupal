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

package scrupal.api

import org.joda.time.DateTime
import scrupal.utils.{ConfigHelper, Registry, Registrable}
import play.api.{Logger, Configuration}
import scala.util.{Failure, Success, Try}
import scala.slick.session.Session
import scrupal.db.{CoreSchema,Sketch}
import scala.collection.mutable


/** Information about one site that Scrupal is serving.
  * Sites are associated with a port number that Play! is configured to listen on. We configure play's ports by
  * scanning this table and collecting all the port numbers that are configured for active sites.
  * @param id The name of the `Thing`
  * @param description A brief description of the `Thing`
  * @param listenPort The port number that Play! should listen on for this site
  * @param urlDomain The domain name to use in generated urls
  * @param urlPort The port number to use in generated urls
  * @param urlHttps The HTTP method to use in generated urls (e.g. https or http)
  * @param enabled Whether the site is enabled for serving or not
  * @param modified Modification time, optional
  * @param created Creation time, optional
  */
case class EssentialSite(
  override val id: Symbol,
  override val description: String,
  listenPort: Short,
  urlDomain: String,
  urlPort: Short,
  urlHttps: Boolean = false,
  override val enabled: Boolean = false,
  override val modified: Option[DateTime] = None,
  override val created: Option[DateTime] = None
) extends SymbolicEnablableThing(id, description, enabled, modified, created)

class Site (
  id: Symbol,
  description: String,
  listenPort: Short,
  urlDomain: String,
  urlPort: Short,
  urlHttps: Boolean = false,
  enabled: Boolean = false,
  modified: Option[DateTime] = None,
  created: Option[DateTime] = None
) extends EssentialSite(id, description, listenPort, urlDomain, urlPort, urlHttps, enabled,
  modified, created) with Registrable {
  def this(e: EssentialSite) = this(e.id, e.description, e.listenPort, e.urlDomain, e.urlPort, e.urlHttps, e.enabled, e.modified,
    e.created)
}


object Site extends Registry[Site]{

  val registrantsName: String = "site"
  val registryName: String = "Sites"

  def apply(esite: EssentialSite) = new Site(esite)

  /** Load the Sites from configuration
    * Site loading is based on the Play Database configuration. There should be a one-to-one correspondence between a
    * site name and its db url/driver pair per usual Play configuration. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    */
  def load(config: Configuration) : Map[Short, Site] = {
    Try {
      val result: mutable.Map[Short, Site] = mutable.Map()
      ConfigHelper(config).forEachDB {
        case (db: String, dbConfig: Configuration) => {
          val sketch = Sketch(dbConfig)
          sketch.withSession { implicit session: Session =>
            val url = dbConfig.getString("url").getOrElse("")
            Logger.debug("Found valid DB Config named '" + db + "' with  URL '" + url + "': attempting load." )
            val schema = new CoreSchema(sketch)
            schema.validate match {
              case Success(true) =>
                for (s <- schema.Sites.findAll if s.enabled) {
                  Logger.debug("Loading site '" + s.id.name + "' for port " + s.listenPort)
                  result.put(s.listenPort, Site(s) )
                }
              case Success(false) =>
                Logger.warn("Attempt to validate schema for '" + url + "' failed.")
              case Failure(x) =>
                Logger.warn("Attempt to validate schema for '" + url + "' failed.", x)
            }
            false
          }
        }
      }
      Map(result.toSeq:_*)
    } match {
      case Success(x) => x
      case Failure(e) => Logger.warn("Error while loading sites: ", e); Map[Short,Site]()
    }
  }
}
