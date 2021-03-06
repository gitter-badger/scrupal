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

import reactivemongo.bson.Subtype.GenericBinarySubtype
import reactivemongo.bson._
import scrupal.core.types._
import scrupal.test.{ScrupalSpecification, FakeContext}
import scrupal.utils.Patterns._



/** Test specifications for the abstract Type system portion of the API.  */
class TypeSpec extends ScrupalSpecification("TypeSpec") {

  case class TestTypes() extends FakeContext[TestTypes] {
    /** The Scrupal Type for Uniform Resource Identifiers per http://tools.ietf.org/html/rfc3986 */
    object MiddlePeriod extends AnyType(sym("MiddlePeriod"), "A type for validating URI strings.") {
      override def validate(ref: ValidationLocation, value: BSONValue) = simplify(ref, value, "BSONString") {
        case v: BSONString => {
          val a = v.value.split('.')
          if (a.size > 2)
            Some("Too many periods")
          else if (a.size < 2)
            Some("Must have at least one period")
          else if (a(0).length != a(1).length)
            Some("Strings on each side of . must have same length")
          else
            None
        }
        case x: BSONValue => Some("")
      }
    }

    val vLoc = SomeValidationLocation

    object rangeTy extends RangeType(sym("aRange"), "Ten from 10", 10, 20)
    object realTy extends RealType(sym("aReal"), "Ten from 10", 10.1, 20.9)
    object enumTy extends EnumType(sym("enumTy"), "Enum example", Map(
      'one -> 1, 'two -> 2, 'three -> 3, 'four -> 5, 'five -> 8, 'six -> 13
    ))

    object blobTy extends BLOBType(sym("blobTy"), "Blob example", "application/binary", 4)
    object listTy extends ListType(sym("listTy"), "List example", enumTy)

    object setTy extends SetType(sym("setTy"), "Set example", rangeTy)

    object mapTy extends MapType(sym("mapTy"), "Map example", realTy)

    object emailTy extends StringType(sym("EmailAddress"), "An email address", anchored(EmailAddress), 253)

    object trait1 extends BundleType(sym("trait1"), "Trait example 1",
      fields = Map (
        "even" -> MiddlePeriod,
        "email" -> emailTy,
        "range" -> rangeTy,
        "real" -> realTy,
        "enum" -> enumTy
      )
    )

    object trait2 extends BundleType(sym("trait2"), "Trait example 2",
      fields = Map(
        "list" -> listTy,
        "set" -> setTy,
        "map" -> mapTy
      )
    )

    object AnEntity extends BundleType(sym("AnEntity"), "Entity example",
      fields = Map("trait1" -> trait1, "trait2" -> trait2)
    )

    val js1 = BSONDocument(
      "even" -> "foo.bar",
      "email" -> "somebody@example.com",
      "range" -> 17,
      "real" -> 17.0,
      "enum" -> "three"
    )

    val js2 = BSONDocument(
      "list" -> BSONArray("one", "three", "five"),
      "set" -> BSONArray(17, 18),
      "map" -> BSONDocument("foo" -> 17.0)
    )
  }



  "MiddlePeriod" should {
    "accept 'foo.bar'" in TestTypes() { t : TestTypes ⇒
      val result = t.MiddlePeriod.validate(t.vLoc, BSONString("foo.bar"))
      result.isError must beFalse
    }
    "reject 'foo'" in TestTypes() { t: TestTypes ⇒
      t.MiddlePeriod.validate(t.vLoc, BSONString("foo")).isError must beTrue
    }
    "reject 'foo.barbaz'" in TestTypes() { t: TestTypes ⇒
      t.MiddlePeriod.validate(t.vLoc, BSONString("foo.barbaz")).isError must beTrue
    }
  }


  "RangeType(10,20)" should {
    "accept 17" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, BSONInteger(17)).isError must beFalse
    }
    "accept 10" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, BSONInteger(10)).isError must beFalse
    }
    "accept 20" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, BSONInteger(20)).isError must beFalse
    }
    "reject 9" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, BSONInteger(9)).isError must beTrue
    }
    "reject 21" in TestTypes() { t: TestTypes ⇒
      t.rangeTy.validate(t.vLoc, BSONInteger(21)).isError must beTrue
    }
  }


  "RangeType(10.1,20.9)" should {
    "accept 17.0" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, BSONDouble(17.0)).isError must beFalse
    }
    "accept 10.2" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, BSONDouble(10.2)).isError must beFalse
    }
    "accept 20.8" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, BSONDouble(20.8)).isError must beFalse
    }
    "reject 10.01" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, BSONDouble(10.01)).isError must beTrue
    }
    "reject 20.99" in TestTypes() { t: TestTypes ⇒
      t.realTy.validate(t.vLoc, BSONDouble(20.99)).isError must beTrue
    }
  }

  "EnumType(fibonacci)" should {
    "accept 'five'" in TestTypes() { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, BSONString("five")).isError must beFalse
    }
    "reject 'seven'" in TestTypes() { t: TestTypes ⇒
      t.enumTy.validate(t.vLoc, BSONString("seven")).isError must beTrue
    }
    "provide 13 for 'six' " in TestTypes() { t: TestTypes ⇒
      t.enumTy.valueOf("six").get must beEqualTo(13)
    }
  }

  "BLOBType(4)" should {
    "reject a string that is too long" in TestTypes() { t: TestTypes ⇒
      val url = BSONString("http://foo.com/bar/baz.bin")
      t.blobTy.validate(t.vLoc, url).isError must beTrue
    }
    "accept a string that is short enough" in TestTypes() { t: TestTypes ⇒
      val url = BSONString("http")
      t.blobTy.validate(t.vLoc, url).isError must beFalse
    }
    "accept BSONBinary" in TestTypes() { t: TestTypes ⇒
      val url = BSONBinary(Array[Byte](0,3,2,1), GenericBinarySubtype)
      t.blobTy.validate(t.vLoc, url).isError must beFalse
    }
  }

  "ListType(enumTy)" should {
    "reject BSONArray(6,7)" in TestTypes() { t: TestTypes ⇒
      val js :BSONValue = BSONArray( 6, 7 )
      t.listTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept BSONArray('six')" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("six")
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept BSONArray()" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray()
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept BSONArray(\"one\", \"three\", \"five\")" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("one", "three", "five")
      t.listTy.validate(t.vLoc, js).isError must beFalse
    }
    "reject BSONArray('nine')" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("nine")
      t.listTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "SetType(t.rangeTy)" should {
    "reject BSONArray(\"foo\")" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(BSONString("foo"))
      t.setTy.validate(t.vLoc, js).isError must beTrue
    }
    "accept BSONArray(17)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(BSONInteger(17))
      t.setTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept BSONArray(17,18)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(17, 18)
      t.setTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept BSONArray(17,17)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(17, 17)
      val result = t.setTy.validate(t.vLoc, js)
      result.isError must beTrue
      result.message.toString must contain("non-distinct")
    }
    "reject BSONArray(21)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray(21)
      t.setTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "MapType(realTy)" should {
    "reject JsObject('foo' -> 17)" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument("foo" -> 17L)
      t.mapTy.validate(t.vLoc, js).isError must beFalse
    }
    "accept JsObject('foo' -> 17.0)" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument("foo" -> 17.0)
      t.mapTy.validate(t.vLoc, js).isError must beFalse
    }
    "reject BSONArray('foo', 17.0)" in TestTypes() { t: TestTypes ⇒
      val js = BSONArray("foo", 17.0)
      t.mapTy.validate(t.vLoc, js).isError must beTrue
    }
  }

  "Complex Entity With Traits" should {
    "accept matching input" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument( "trait1" -> t.js1, "trait2" -> t.js2)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beFalse
    }
    "reject mismatched input" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument( "trait1" -> t.js2, "trait2" -> t.js1)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beTrue
    }
    "accept reversed input" in TestTypes() { t: TestTypes ⇒
      val js = BSONDocument( "trait2" -> t.js2, "trait1" -> t.js1)
      val result = t.AnEntity.validate(t.vLoc, js)
      result.isError must beFalse
    }
  }

  "Identifier_t" should {
    "accept ***My-Funky.1d3nt1f13r###" in TestTypes() { t: TestTypes ⇒
      Identifier_t.validate(t.vLoc, BSONString("***My-Funky.1d3nt1f13r###")).isError must beFalse
    }
    "reject 'Not An Identifier'" in TestTypes() { t: TestTypes ⇒
      Identifier_t.validate(t.vLoc, BSONString("Not An Identifier ")).isError must beTrue
    }
  }

  "AnyType_t" should {
    "have some test cases" in { pending }
  }
  "AnyString_t" should {
    "have some test cases" in { pending }
  }
  "AnyInteger_t" should {
    "have some test cases" in { pending }
  }
  "AnyReal_t" should {
    "have some test cases" in { pending }
  }
  "AnyTimestamp_t" should {
    "have some test cases" in { pending }
  }
  "Boolean_t" should {
    "have some test cases" in { pending }
  }
  "NonEmptyString_t" should {
    "have some test cases" in { pending }
  }
  "Password_t" should {
    "have some test cases" in { pending }
  }
  "Description_t" should {
    "have some test cases" in { pending }
  }
  "Markdown_t" should {
    "have some test cases" in { pending }
  }


  "DomainName_t" should {
    "accept scrupal.org" in TestTypes() { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, BSONString("scrupal.org")).isError must beFalse
    }
    "reject ###.999" in TestTypes() { t: TestTypes ⇒
      DomainName_t.validate(t.vLoc, BSONString("###.999")).isError must beTrue
    }
  }

  "URI_t" should {
    "accept http://user:pw@scrupal.org/path?q=where#extra" in TestTypes() { t: TestTypes ⇒
      URL_t.validate(t.vLoc, BSONString("http://user:pw@scrupal.org/path?q=where#extra")).isError must beFalse
    }
    "reject Not\\A@URI" in TestTypes() { t: TestTypes ⇒
      URL_t.validate(t.vLoc, BSONString("Not\\A@URI")).isError must beTrue
    }
  }

  "IPv4Address_t" should {
    "accept 1.2.3.4" in TestTypes() { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, BSONString("1.2.3.4")).isError must beFalse
    }
    "reject 1.2.3.400" in TestTypes() { t: TestTypes ⇒
      IPv4Address_t.validate(t.vLoc, BSONString("1.2.3.400")).isError must beTrue
    }
  }

  "TcpPort_t" should {
    "accept 8088" in TestTypes() { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, BSONInteger(8088)).isError must beFalse
    }
    "reject 65537" in TestTypes() { t: TestTypes ⇒
      TcpPort_t.validate(t.vLoc, BSONString("65537")).isError must beTrue
    }
  }

  "EmailAddress_t" should {
    "accept someone@scrupal.org" in TestTypes() { t: TestTypes ⇒
      // println("Email Regex: " + EmailAddress_t.regex.pattern.pattern)
      EmailAddress_t.validate(t.vLoc, BSONString("someone@scrupal.org")).isError must beFalse
    }
    "reject white space" in TestTypes() { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, BSONString(" \t\r\n")).isError must beTrue
    }
    "reject nobody@ scrupal dot org" in TestTypes() { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, BSONString("nobody@ 24 dot com")).isError must beTrue
    }
    "reject no body@scrupal.org" in TestTypes() { t: TestTypes ⇒
      EmailAddress_t.validate(t.vLoc, BSONString("no body@scrupal.org")).isError must beTrue
    }
  }

  "LegalName_t" should {
    "accept 'My Legal Name'" in TestTypes() { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, BSONString("My Legal Name")).isError must beFalse
    }
    "reject tab char" in TestTypes() { t: TestTypes ⇒
      LegalName_t.validate(t.vLoc, BSONString("\t")).isError must beTrue
    }
  }

  "Types" should {
    "not spoof registration in a module via Type.moduleof" in TestTypes() { t: TestTypes ⇒
      val mod = t.AnEntity.moduleOf // Make sure AnEntity is referenced her lest it be garbage collected!
      mod.isDefined must beFalse
    }
  }
}
