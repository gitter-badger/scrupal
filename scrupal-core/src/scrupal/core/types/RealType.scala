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

import reactivemongo.bson.{BSONDouble, BSONInteger, BSONLong, BSONValue}
import scrupal.core.api._

/** A Real type constrains Double values between a minimum and maximum value
  *
  * @param id
  * @param description
  * @param min
  * @param max
  */
case class RealType (
  id : Identifier,
  description : String,
  min : Double = Double.MinValue,
  max : Double = Double.MaxValue
  ) extends Type {
  override type ScalaValueType = Double
  require(min <= max)
  def validate(ref: ValidationLocation, value: BSONValue) : VR =  {
    simplify(ref, value, "Double, Long or Integer") {
      case BSONDouble(d) if d < min => Some(s"Value $d is out of range, below minimum of $min")
      case BSONDouble(d) if d > max => Some(s"Value $d is out of range, above maximum of $max")
      case BSONDouble(d) => None
      case BSONLong(l) if l < min => Some(s"Value $l is out of range, below minimum of $min")
      case BSONLong(l) if l > max => Some(s"Value $l is out of range, above maximum of $max")
      case BSONLong(l) => None
      case BSONInteger(i) if i < min => Some(s"Value $i is out of range, below minimum of $min")
      case BSONInteger(i) if i > max => Some(s"Value $i is out of range, above maximum of $max")
      case BSONInteger(i) => None
      case _ ⇒ Some("")
    }
  }
  override def kind = 'Real
}

object AnyReal_t
  extends RealType('AnyReal, "A type that accepts any double floating point value", Double.MinValue, Double.MaxValue)

