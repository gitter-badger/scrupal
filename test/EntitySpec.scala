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

package scrupal.models.test

import scrupal.models.Entity

import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.test._
import play.api.test.Helpers.running

import scala.concurrent.Await
import org.specs2.mutable.Specification

import reactivemongo.core.commands.LastError
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration

/**
 * One line sentence description here.
 * Further description here.
 */
object TestEntity { implicit val teFormatter = Json.format[TestEntity] }

case class TestEntity(one : Int = 1, two: String = "2") extends Entity
{
  override lazy val collectionName = "test_entities"
  override def toJson = {
    super.toJson.deepMerge( Json.toJson(this).asInstanceOf[JsObject] )
  }
}

class EntitySpec extends Specification
{
	val te = new TestEntity()

	"Entity" should {
		"generate plural collection name" in {
			te.collectionName must equalTo("test_entities")
		}
		"fail to compare against a non-entity" in {
			val other = "not-matchable"
			te.equals(other) must beFalse
			te.equals(te) must beTrue
		}
		"allow reincarnation of Entity Subclass" in {
			val js = Json.toJson[TestEntity](te)
      Logger.debug("TestEntity.toJson -> " + Json.prettyPrint(js))
			val cm2 : TestEntity = Json.fromJson[TestEntity](js).get
      val js2 = Json.toJson[TestEntity](cm2)
      Logger.debug("TestEntity(2).toJson -> " + Json.prettyPrint(js2))
			te.equals(cm2) must beTrue
		}
    "save to and delete from reactivemongo" in {
      running(FakeApplication()) {
        val future = Entity.save(te)
        val x : LastError = Await.result(future, Duration(2,TimeUnit.SECONDS) )
        x.ok must beTrue
        val remove_future = Entity.remove(te)
        val y : LastError = Await.result(remove_future, Duration(2,TimeUnit.SECONDS) )
        y.ok must beTrue
      }
    }
	}
}
