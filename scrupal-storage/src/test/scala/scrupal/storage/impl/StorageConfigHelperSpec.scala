/** ********************************************************************************************************************
  * Copyright © 2014 Reactific Software, Inc.                                                                          *
  *                                                                                                            *
  * This file is part of Scrupal, an Opinionated Web Application Framework.                                            *
  *                                                                                                            *
  * Scrupal is free software: you can redistribute it and/or modify it under the terms                                 *
  * of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License,   *
  * or (at your option) any later version.                                                                             *
  *                                                                                                            *
  * Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied      *
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more      *
  * details.                                                                                                           *
  *                                                                                                            *
  * You should have received a copy of the GNU General Public License along with Scrupal. If not, see either:          *
  * http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                                             *
  * ********************************************************************************************************************
  */

package scrupal.storage.impl

import java.io.File

import org.specs2.mutable.Specification
import play.api.Configuration
import scrupal.utils.ConfigHelpers

/** Test Suite for the ConfigHelper class  */
class StorageConfigHelperSpec extends Specification {

  def makeTestConfig(file : String) = Configuration.from(
    Map(StorageConfigHelper.scrupal_storage_config_file_key -> ("scrupal-storage/src/test/resources/storage/config/" + file))
  )

  "ConfigHelper" should {
    "Correctly extract database configuration from default.conf file" in {
      val helper = StorageConfigHelper(makeTestConfig("default.conf"))
      val conf = helper.getStorageConfig
      conf.getConfig("storage").isDefined must beTrue
      conf.getString("storage.default.uri").isDefined must beTrue
      conf.getString("storage.default.uri").get must beEqualTo("scrupal-mem://localhost/scrupal")
    }

    "Correctly extract empty config from empty_conf.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("empty_conf.conf"))
      helper.getStorageConfig.getConfig("storage").isDefined must beFalse
    }

    "Reflect get/set/get for valid.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("valid.conf"))
      val tmpFile = File.createTempFile("scrupal", ".conf")
      tmpFile.deleteOnExit()
      val get1 = helper.getStorageConfig
      helper.setStorageConfig(get1, Some(tmpFile))
      val helper2 = StorageConfigHelper(Configuration.from(
        Map(StorageConfigHelper.scrupal_storage_config_file_key -> tmpFile.getCanonicalPath))
      )
      val get2 = helper2.getStorageConfig
      get1 must beEqualTo(get2)
    }
  }

  "ConfigHelper.forEachDB" should {
    "Not iterate on empty_conf.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("empty_conf.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          println("You should never see this!")
          failure
          false
      }
      result.size must beEqualTo(0)
    }

    "Find only one value in valid.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("valid.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          println("You should see this only once!")
          true
      }
      result.size must beEqualTo(1)
    }

    "Find multiple values in multiple.conf" in {
      val helper = StorageConfigHelper(makeTestConfig("multiple.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          true
      }
      result.size must beGreaterThan(1)
    }

    "Not return configurations when function returns false" in {
      val helper = StorageConfigHelper(makeTestConfig("multiple.conf"))
      val result = helper.forEachStorage {
        case (name : String, config : Configuration) ⇒
          false
      }
      result.size must beEqualTo(2)
      result.count { case (name : String, config : Option[Configuration]) ⇒ config.isDefined } must beEqualTo(0)
    }
  }

  "ConfigHelper.validateDBConfiguration" should {
    "Return Failure(x) for empty configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("empty_conf.conf"))
      val result = helper.validateStorageConfig
      result must beAFailedTry
    }

    "Return Failure(x) for default configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("default.conf"))
      val result = helper.validateStorageConfig
      result must beAFailedTry
    }

    "Return Failure(x) for bad configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("bad.conf"))
      val result = helper.validateStorageConfig
      result must beAFailedTry
    }

    "Return Success(x) for valid configuration" in {
      val helper = StorageConfigHelper(makeTestConfig("valid.conf"))
      val result = helper.validateStorageConfig
      result must beASuccessfulTry
    }
  }
}
