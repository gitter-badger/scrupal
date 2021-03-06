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

import reactivemongo.bson._
import scala.util.{Success, Failure, Try}

import scrupal.utils.{Registry, Pluralizer, Registrable}

/** A generic Type used as a placeholder for subclasses that compose types.
  * Note that the name of the Type is a Symbol. Symbols are interned so there is only ever one copy of the name of
  * the type. This is important because type linkage is done by indirectly referencing the name, not the actual type
  * object and name reference equality is the same as type equality.  Scrupal also interns the Type objects that
  * modules provide so they can be looked up by name quickly. This allows inter-module integration without sharing
  * code and provides rapid determination of type equality.
  *
  * Types are interned by the Registry[Type] utility. This means that types share a single global name space.
  * Modules must cooperate on defining types in such a way that their names do not conflict.
  */
trait Type extends Registrable[Type] with Describable with BSONValidator with Bootstrappable {
  def registry = Type

  type ScalaValueType

  /** The plural of the name of the type.
    * The name given to the type should be the singular form (Color not Colors) but things associated with this type
    * may wish to use the plural form. To ensure that everyone uses the same rules for pluralization,
    * we use our utility [[scrupal.utils.Pluralizer]] to make it consistent. This is lazy constructed so there's no
    * cost for it unless it gets used.
    */
  lazy val plural = Pluralizer.pluralize(label)

  def moduleOf = { Module.values.find(mod ⇒ mod.types.contains(this)) }

  /** The kind of this class is simply its simple class name. Each "kind" has a different information structure */
  def kind : Symbol = Symbol(super.getClass.getSimpleName.replace("$","_"))

  def trivial = false
  def nonTrivial = !trivial

  override protected def simplify(ref: ValidationLocation, value: BSONValue, classes: String)
  (validator: (BSONValue) => Option[String]) : VR = {
    validator(value) match {
      case Some("") ⇒ wrongClass(ref, value, classes)
      case Some(msg: String) ⇒ TypeValidationError(ref, value, this, msg)
      case None ⇒ ValidationSucceeded(ref,value)
    }
  }


  /** Convert the BSON value B into the Scala value of type S
    *
    * This validates the BSONValue first and then attempts to apply a BSONReader in order to extract a value of type S
    * @param value
    * @return
    */
  def convert[S <: ScalaValueType](value: BSONValue)(implicit reader: BSONReader[BSONValue,S]) : Try[S] = {
    val result = validate(SomeValidationLocation, value)
    if (result.isError)
      Failure[S](new Exception(result.asInstanceOf[ValidationErrorResults[BSONValue]].message.toString()))
    else
      reader.readTry(value)
  }

  /** Convert the Scala value of type S into the BSON value of type B
    * After applying the BSONWriter this validates that what was written can be converted back to an S and
    * reports validation errors if anything is wrong.
    * @param value
    * @param writer
    * @return
    */
  def convert[S <: ScalaValueType](value: S)(implicit writer: BSONWriter[S,BSONValue]) : Try[BSONValue] = {
    writer.writeTry(value).flatMap { bson =>
      val result = validate(SomeValidationLocation, bson)
      if (result.isError)
        Failure[BSONValue](new Exception(result.asInstanceOf[ValidationErrorResults[BSONValue]].message.toString()))
      else
        Success[BSONValue](bson)
    }
  }
}

case class Not_A_Type() extends Type {
  lazy val id = 'NotAType
  override val kind = 'NotAKind
  override val trivial = true
  val description = "Not A Type"
  val module = 'NotAModule
  def validate(ref: ValidationLocation, value: BSONValue) =
    TypeValidationError(ref, value, this, "NotAType is not valid")
}

case class UnfoundType(id: Symbol) extends Type {
  override val kind = 'Unfound
  override val trivial = true
  val description = "A type that was not loaded in memory"
  val module = 'NotAModule
  def validate(ref: ValidationLocation, value: BSONValue) =
    TypeValidationError(ref, value, this,
      s"Unfound type '${id.name}' cannot be used for validation. The module defining the type is not loaded.")
}


/** Abstract base class of Types that refer to another Type that is the element type of the compound type
  */
trait CompoundType extends Type {
  def elemType: Type
}

trait IndexableType extends CompoundType

trait DocumentType extends Type {
  def validatorFor(id: String) : Option[Type]
  def fieldNames: Iterable[String]
  def allowMissingFields : Boolean = false
  def allowExtraFields : Boolean = false

  def validate(ref: ValidationLocation, value: BSONValue) : VR = {
    value match {
      case d: BSONDocument =>
        def doValidate(name: String, value: BSONValue) : VR = {
          validatorFor(name) match {
            case Some(validator) ⇒
              validator.validate(ref.get(name).getOrElse(ref), value)
            case None ⇒ {
              if (!allowExtraFields)
                ValidationError(ref, value, s"Field '$name' is spurious.")
              else
              // Don't validate or anything, spurious field
                ValidationSucceeded(ref, value)
            }
          }
        }
        // Note: Stream this to a map up front because we're going to access everything so we might as well stream it once
        val elements = d.elements.toMap
        val errors : Iterable[ValidationErrorResults[BSONValue]] = {
          val field_results : Iterable[ValidationErrorResults[BSONValue]] = for (
            field ← elements ;
            result = doValidate(field._1, field._2) if result.isError
          ) yield {
            result.asInstanceOf[ValidationErrorResults[BSONValue]]
          }

          val missing_results : Iterable[ValidationErrorResults[BSONValue]] = if (!allowMissingFields) {
            for (
              fieldName ← fieldNames if !elements.contains(fieldName)
            ) yield {
              ValidationError(ref, value, s"Field '$fieldName' is missing")
            }
          } else {
            Iterable.empty[ValidationError[BSONValue]]
          }
          field_results ++ missing_results
        }
        if (errors.isEmpty)
          ValidationSucceeded(ref, value)
        else
          ValidationFailed(ref, value, errors.toSeq)
      case x: BSONValue => wrongClass(ref, x, "BSONDocument")
    }
  }
}


trait StructuredType extends DocumentType {
  def fields : Map[String, Type]
  def validatorFor(id:String) : Option[Type] = fields.get(id)
  def fieldNames : Iterable[String] = fields.keys
  def size = fields.size
}


/** Type Registry and companion */
object Type extends Registry[Type] {

  val registryName = "Types"
  val registrantsName = "type"

  /** Determine if a type is a certain kind
    * While Scrupal defines a useful set of types that will suffice for many needs,
    * Modules are free to create new kinds of types (i.e. subclass from Type itself,
    * not create a new instance of Type). Types have a "kind" field that allows them to be categorized roughly by the
    * nature of the subclass from Type. This method checks to see if a given type is a member of that category.
    * @param name The symbol for the type to check
    * @param kind The symbol for the kind that ```name``` should be
    * @return true iff ```name``` is of kind ```kind```
    */
  def isKindOf(name: Symbol, kind: Symbol) : Boolean = lookupOrElse(name,NotAType).kind == kind

  lazy val NotAType = Not_A_Type()

  def of(id: Identifier) : Type = {
    Type(id) match {
      case Some(typ) => typ
      case None => new UnfoundType(id)
    }
  }

  /** Handle reading/writing Type instances to and from BSON.
    * Note that types are a little special. We write them as strings and restore them via lookup. Types are intended
    * to only ever live in memory but they can be references in the database. So when a Type is a field of some
    * class that is stored in the database, what actually gets stored is just the name of the type.
    */
  class BSONHandlerForType[T <: Type]  extends BSONHandler[BSONString,T] {
    override def write(t: T): BSONString = BSONString(t.id.name)
    override def read(bson: BSONString): T = Type.as(Symbol(bson.value))
  }
}

