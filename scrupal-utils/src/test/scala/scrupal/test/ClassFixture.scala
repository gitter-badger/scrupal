/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                                   *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                                   *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                                   *
  *       http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                                   *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.test

import org.specs2.execute.{ Result, AsResult }
import org.specs2.specification.Fixture

/** Title Of Thing.
  *
  * Description of thing
  */
class ClassFixture[CLASS <: AutoCloseable](create : ⇒ CLASS) extends Fixture[CLASS] {
  def apply[R : AsResult](f : CLASS ⇒ R) = {
    val fixture = create
    try {
      AsResult(f (fixture))
    } finally {
      fixture.close()
    }
  }
}

class CaseClassFixture[T <: CaseClassFixture[T]] extends Fixture[T] {
  def apply[R : AsResult](f : T ⇒ R) = {
    AsResult(f (this.asInstanceOf[T]))
  }

}
