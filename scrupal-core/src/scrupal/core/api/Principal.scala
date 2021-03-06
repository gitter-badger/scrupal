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

import org.joda.time.DateTime
import reactivemongo.bson.Macros
import scrupal.db.{ScrupalDB, IdentifierDAO, Storable}
import scrupal.utils.Hash

/**
 * Information about a Principal, the essential identify of a user of the system. Authentication of Principals requires
 * either one or more authentication factors. The first factor (something the Principal knows) is embodied in this object
 * via the password, hasher algorithm, salt and complexity fields. Subsequent authentication factors are dealt with in
 * separate objects. Each Principal is associated with an email address and a unique identifier.
 * @param email The principal's Email address
 * @param password The Principal's hashed password
 * @param hasher The Hasher algorithm used
 * @param salt The salt used in generation of the principal's hashed password
 * @param complexity The complexity factor for the Hasher algorithm
 */
case class Principal (
  _id : Identifier,
  email: String,
  aliases: List[String],
  password: String,
  hasher: String,
  salt: String = Hash.salt,
  complexity: Long = 0,
  override val created: Option[DateTime] = None
) extends Storable[Identifier] with Creatable

object Principal {
  import BSONHandlers._

  case class PrincipalDAO(db: ScrupalDB) extends IdentifierDAO[Principal] {
    final def collectionName = "principals"
    implicit val reader : IdentifierDAO[Principal]#Reader = Macros.reader[Principal]
    implicit val writer : IdentifierDAO[Principal]#Writer = Macros.writer[Principal]
  }
}
