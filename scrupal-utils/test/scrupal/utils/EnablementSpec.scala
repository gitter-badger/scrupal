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

package scrupal.utils

import org.specs2.mutable.Specification
import scrupal.test.ClassFixture

class Scenario extends AutoCloseable {
  object TSRegistry extends Registry[TestScope] { val registryName = "TestScopes"; val registrantsName = "test scopes" }
  case class TestScope(id: Symbol, children : Seq[Enablement[_]] = Seq())
    extends Enablement[TestScope] with Registrable[TestScope] {
    def registry : Registry[TestScope] = TSRegistry
    def asT : TestScope = this
    def isChildScope(x: Enablement[_]) : Boolean = children.contains(x)
  }
  object TERegistry extends Registry[TestEnablee] {
    val registryName = "TestEnablees"; val registrantsName = "test enablees"
  }

  case class TestEnablee(id: Symbol, override val parent: Option[Enablee] = None) extends Enablee with
                                                                                          Registrable[TestEnablee]{
    def asT : TestEnablee = this
    def registry : Registry[TestEnablee] = TERegistry
  }

  val root_1_a = TestScope('root_1_a )
  val root_1_b = TestScope('root_1_b)
  val root_1 = TestScope('root_1, Seq(root_1_a, root_1_b))
  val root_2_a = TestScope('root_2_a)
  val root_2 = TestScope('root_2, Seq(root_2_a))
  val root = TestScope('root, Seq(root_1, root_2))

  val e_root = TestEnablee('e_root)
  val e_root_1 = TestEnablee('e_root_1, Some(e_root))
  val e_root_2 = TestEnablee('e_root_2, Some(e_root))

  def close() = {

  }
}
/** Test Suite For Enablement */
class EnablementSpec extends Specification {

  val scenario = new ClassFixture(new Scenario)

  "Enablee" should {
    "allow enable on multiple scopes" in scenario { s ⇒
      s.e_root.enable(s.root)
      s.e_root.isEnabled(s.root) must beTrue
      s.e_root.enable(s.root_1)
      s.e_root.isEnabled(s.root_1) must beTrue
    }
    "allow disable on multiple scopes" in scenario { s ⇒
      s.e_root.disable(s.root)
      s.e_root.isEnabled(s.root) must beFalse
      s.e_root.disable(s.root_1)
      s.e_root.isEnabled(s.root_1) must beFalse
    }
    "allow query on arbitrary scopes" in scenario { s ⇒
      s.root.enable(s.e_root, s.root_1)
      s.e_root.isEnabled(s.root) must beFalse
      s.e_root.isEnabled(s.root_1) must beFalse
      s.root.isEnabled(s.e_root, s.root_1) must beTrue
      s.root_1.isEnabled(s.e_root) must beFalse
      s.root_1_a.isEnabled(s.e_root) must beFalse
    }
    "should not be enabled if parent is disabled" in scenario { s ⇒
      s.root.disable(s.e_root)
      s.root.enable(s.e_root_1)
      s.root.isEnabled(s.e_root_1) must beFalse
      s.e_root_1.isEnabled(s.root) must beFalse
    }

  }
}