/** ********************************************************************************************************************
  * This file is part of Scrupal, a Scalable Reactive Content Management System.                                       *
  *                                                                                                       *
  * Copyright © 2015 Reactific Software LLC                                                                            *
  *                                                                                                       *
  * Licensed under the Apache License, Version 2.0 (the "License");  you may not use this file                         *
  * except in compliance with the License. You may obtain a copy of the License at                                     *
  *                                                                                                       *
  * http://www.apache.org/licenses/LICENSE-2.0                                                                  *
  *                                                                                                       *
  * Unless required by applicable law or agreed to in writing, software distributed under the                          *
  * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,                          *
  * either express or implied. See the License for the specific language governing permissions                         *
  * and limitations under the License.                                                                                 *
  * ********************************************************************************************************************
  */

package scrupal.storage.api

import scrupal.utils.ScrupalComponent

import scala.concurrent.Future

trait Collection[S <: Storable] extends AutoCloseable with ScrupalComponent {
  def name : String
  def schema : Schema
  override def toString = { s"Collection $name in $schema" }
  def count : Long
  def indexOf(field : Seq[Indexable]) : Option[Index]
  def addIndex(index : Index) : Future[WriteResult]
  def removeIndex(index : Index) : Future[WriteResult]
  def indices : Seq[Index]
  def fetch(id : ID) : Future[Option[S]]
  def fetchAll() : Future[Iterable[S]]
  def find(query : Query) : Future[Seq[S]]
  def insert(obj : S, update : Boolean = false) : Future[WriteResult]
  def update(obj : S, update : Modification[S]) : Future[WriteResult]
  def update(id : ID, update : Modification[S]) : Future[WriteResult]
  def updateWhere(query : Query, update : Modification[S]) : Future[Seq[WriteResult]]
  def delete(obj : S) : Future[WriteResult]
  def delete(id : ID) : Future[WriteResult]
  def delete(ids : Seq[ID]) : Future[Seq[WriteResult]]
}

sealed trait WriteResult { def isSuccess : Boolean; def isFailure : Boolean = !isSuccess }
case class WriteSuccess() extends WriteResult { val isSuccess = true }
case class WriteFailure(failure : Throwable) extends WriteResult { val isSuccess = false }
case class WriteError(error : String) extends WriteResult { val isSuccess = false }

object WriteResult {
  def failure(x : Throwable) : WriteResult = { WriteFailure(x) }
  def error(x : String) : WriteResult = { WriteError(x) }
  def success() : WriteResult = WriteSuccess()
}

trait IndexKind
trait Modification[S <: Storable] { def apply(s : S) : S = s }
trait Query
