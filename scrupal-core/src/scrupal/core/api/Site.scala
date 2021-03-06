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
package scrupal.core.api

import reactivemongo.api.DefaultDB
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.bson._

import scrupal.db._
import scrupal.utils._
import scala.util.matching.Regex

/** Site Top Level Object
  * Scrupal manages sites.
 * Created by reidspencer on 11/3/14.
 */
abstract class Site(sym: Identifier) extends { val id: Identifier = sym ; val _id : Identifier = sym }
  with Settingsable with EnablementActionExtractor[Site] with VariantStorable[Identifier] with Registrable[Site]
          with Nameable with Describable with Modifiable {
  val kind = 'Site
  def registry = Site

  def requireHttps: Boolean = false // = getBoolean("requireHttps").get

  def host: String // = getString("host").get

  def themeProvider : String = "bootswatch"

  def themeName : String = "default" // = getString("theme").get

  def applications = forEach[Application] { e ⇒
    e.isInstanceOf[Application] && isEnabled(e, this)
  } { e ⇒
    e.asInstanceOf[Application]
  }

  def isChildScope(e: Enablement[_]) : Boolean = applications.contains(e)
}

object Site extends Registry[Site] {

  val registrantsName: String = "site"
  val registryName: String = "Sites"

  object variants extends VariantRegistry[Site]("Site")

  def kinds: Seq[String] = {  variants.kinds }

//  implicit val dtHandler = DateTimeBSONHandler
  private[this] val _byhost = new AbstractRegistry[String, Site] {
    def reg(site:Site) = _register(site.host,site)
    def unreg(site:Site) = _unregister(site.host)
    def registry = _registry
  }

  override def register(site: Site) : Unit = {
    _byhost.reg(site)
    super.register(site)
  }

  override def unregister(site: Site) : Unit = {
    _byhost.unreg(site)
    super.unregister(site)
  }

  def forHost(hostName: String) : Iterable[Site] = {
    for (
      (host, site) <- _byhost.registry ;
      regex = new Regex(host) if regex.pattern.matcher(hostName).matches()
    ) yield {
      site
    }
  }

  import BSONHandlers._

  implicit lazy val SiteReader : VariantBSONDocumentReader[Site] = new VariantBSONDocumentReader[Site] {
    def read(doc: BSONDocument) : Site = variants.read(doc)
  }

  implicit val SiteWriter : VariantBSONDocumentWriter[Site] = new VariantBSONDocumentWriter[Site] {
    def write(site: Site) : BSONDocument = variants.write(site)
  }

  /** Data Access Object For Sites
    * This DataAccessObject sublcass represents the "sites" collection in the database and permits management of
    * that collection as well as conversion to and from BSON format.
    * @param db A [[reactivemongo.api.DefaultDB]] instance in which to find the collection
    */
  case class SiteDAO(db: DefaultDB) extends VariantIdentifierDAO[Site] {
    final def collectionName: String = "sites"
    implicit val writer = new Writer(variants)
    implicit val reader = new Reader(variants)

    override def indices : Traversable[Index] = super.indices ++ Seq(
      Index(key = Seq("path" -> IndexType.Ascending), name = Some("path")),
      Index(key = Seq("kind" -> IndexType.Ascending), name = Some("kind"))
    )
  }
}
