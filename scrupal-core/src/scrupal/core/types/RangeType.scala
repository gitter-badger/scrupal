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

package scrupal.core.types

import reactivemongo.bson.{BSONString, BSONInteger, BSONLong, BSONValue}
import scrupal.core.api._

/** A Range type constrains Long Integers between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class RangeType (
  id : Identifier,
  description : String,
  min : Long = Long.MinValue,
  max : Long = Long.MaxValue
  ) extends Type {
  override type ScalaValueType = Long
  require(min <= max)
  def validate(ref: ValidationLocation, value: BSONValue) : VR = {
    simplify(ref, value, "Integer or Long") {
      case BSONLong(l) if l < min => Some(s"Value $l is out of range, below minimum of $min")
      case BSONLong(l) if l > max => Some(s"Value $l is out of range, above maximum of $max")
      case BSONLong(l) => None
      case BSONInteger(i) if i < min => Some(s"Value $i is out of range, below minimum of $min")
      case BSONInteger(i) if i > max => Some(s"Value $i is out of range, above maximum of $max")
      case BSONInteger(i) => None
      case BSONString(is) if { try { is.toInt ; false } catch { case x: Throwable ⇒ true } } ⇒
        Some(s"Value $is is not convertible to a numeric")
      case BSONString(is) if is.toInt > max ⇒ Some(s"Value $is is out of range, above maximum of $max")
      case BSONString(is) if is.toInt < min ⇒ Some(s"Value $is is out of range, below minimum of $min")
      case BSONString(is) ⇒ None
      case _ => Some("")
    }
  }
  override def kind = 'Range
}

object AnyInteger_t
  extends RangeType('AnyInteger, "A type that accepts any integer value", Int.MinValue, Int.MaxValue)

/** The Scrupal Type for TCP port numbers */
object TcpPort_t
  extends RangeType('TcpPort, "A type for TCP port numbers", 1, 65535) {
}

