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

package scrupal.core

import java.util.concurrent.atomic.AtomicReference

import akka.actor.ActorRef
import akka.pattern.ask
import reactivemongo.bson.BSONDocument
import scrupal.core.actors.EntityProcessor

import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

import com.typesafe.config.{ConfigRenderOptions, ConfigValue}

import scrupal.core.api._
import scrupal.db.{ScrupalDB, DBContext}
import scrupal.utils.{ScrupalComponent, Configuration}

class Scrupal(ec: ExecutionContext = null, config: Configuration = null, dbc: DBContext = null) extends ScrupalComponent
{
  val Copyright = "2013, 2014 viritude llc"

  val _dbContext : AtomicReference[DBContext] = new AtomicReference[DBContext](dbc)
  val _configuration : AtomicReference[Configuration] = new AtomicReference[Configuration](config)
  val _executionContext : AtomicReference[ExecutionContext] = new AtomicReference[ExecutionContext](ec)

  def withConfiguration[T](f: (Configuration) ⇒ T) : T = {
    val config = _configuration.get
    require(config != null)
    f(config)
  }

  def withDBContext[T](f: (DBContext) ⇒ T) : T = {
    val dbc = _dbContext.get()
    require(dbc != null)
    f(dbc)
  }

  def withCoreSchema[T](f: (DBContext, ScrupalDB, CoreSchema) => T) : T = {
    withDBContext { dbc =>
      dbc.withDatabase(CoreModule.dbName) { db =>
        f(dbc, db, new CoreSchema(dbc))
      }
    }
  }

  def withExecutionContext[T](f: (ExecutionContext) => T) : T = {
    implicit val ec = _executionContext.get()
    require(ec != null)
    f(ec)
  }

  /** Simple utility to determine if we are considered "ready" or not. Basically, if we have a non empty Site
    * Registry then we have had to found a database and loaded the sites. So that is our indicator of whether we
    * are configured yet or not.
    * @return True iff there are sites loaded
    */
  def isReady : Boolean = _configuration.get() != null && Site.nonEmpty

  /**
	 * Called before the application starts.
	 *
	 * Resources managed by plugins, such as database connections, are likely not available at this point.
	 *
	 */
	def beforeStart() = {


    val config = onLoadConfig(Configuration.default)

    _configuration.set(config)

    // FIXME: This should be obtained from configuration instead
    _executionContext.set(scala.concurrent.ExecutionContext.Implicits.global)

    // Get the database started up
    DBContext.startup()

    val dbc = DBContext.fromConfiguration(Some(config))
    _dbContext.set(dbc)

    // We do a lot of stuff in API objects and they need to be instantiated in the right order,
    // so "touch" them now because they are otherwise initialized randomly as used
    require(Type.registryName == "Types")
    require(Module.registryName == "Modules")
    require(Site.registryName == "Sites")
    require(Entity.registryName == "Entities")

    // Make sure that we registered the CoreModule as 'Core just to make sure it is instantiated at this point
    require(CoreModule.id == 'Core)

    // TODO: scan classpath for additional modules
    val configured_modules = Seq.empty[String]

    // We are now ready to process the registered modules
    Module.bootstrap(configured_modules)

    // Load the configuratoin
    load(config, dbc)

    config -> dbc
  }

  def afterStop(): Unit = {
    DBContext.shutdown()
  }

  type FlatConfig =  TreeMap[String,ConfigValue]

  def interestingConfig(config: Configuration) : FlatConfig = {
    val elide : Regex = "^(akka|java|sun|user|awt|os|path|line).*".r
    val entries = config.entrySet.toSeq
    val filtered = entries filter { case (x,y) =>  !elide.findPrefixOf(x).isDefined }
    TreeMap[String,ConfigValue](filtered.toSeq:_*)
  }

  /** Load the Sites from configuration
    * Site loading is based on the database configuration. Whatever databases are loaded, they are scanned and any
    * sites in them are fetched and instantiated into the memory registry. Note that multiple sites may utilize the
    * same database information. We utilize this to open the database and load the site objects they contain
    * @param config The Scrupal Configuration to use to determine the initial loading
    * @param context The database context from which to load the
    */
  def load(config: Configuration, context: DBContext) : Map[String, Site] = {
    Try {
      val result: mutable.Map[String, Site] = mutable.Map() // FIXME: shouldn't need this, have the code below construct it
      withCoreSchema { (dbc, db, schema) =>
        schema.validate match {
          case Success(true) =>
            val sites = schema.sites.fetchAllSync(5.seconds)
            for (s <- sites) {
              log.debug("Loading site '" + s._id.name + "' for host " + s.host + ", index=" + s.siteRoot.toString
                + ", " +"enabled: " + s.isEnabled)
              result.put(s.host, s)
            }
          case Success(false) =>
            log.warn("Attempt to validate schema for '" + db.name + "' failed.")
          case Failure(x) =>
            log.warn("Attempt to validate schema for '" + db.name + "' failed.", x)

        }
      }
      Map(result.toSeq:_*)
    } match {
      case Success(x) => x
      case Failure(e) => log.warn("Error while loading sites: ", e); Map[String,Site]()
    }
  }

  /**
   * Called once the application is started.
   */
  def onStart() {
  }

  /** Mapping of entity paths to Entities */
  type EntityMap = Map[String,(String,Entity)]

  /** Mapping of application paths to Applications and their corresponding EntityMap  */
  type AppEntityMap = Map[String,(Application,EntityMap)]

  /** Mapping of site names to Sites and their corresponding AppEntityMap
    * This type just gives a name to a nested set of tuples that defines the top level structure in Scrupal.
    * At the root we have various Sites which contain Applications which contain Entities (via Modules). Note
    * that Modules provide entities generally, possibly to more than one site or application. This is how entity paths
    * are mapped. The outer map maps the name of a site to a pair of the Site object and the middle map. The middle
    * map, similarly, maps the path of the application to a pair of the Application object and the inner map. The
    * inner map maps the path(s) of an entity (type) to the Entity object.
    */
  type SiteAppEntityMap = Map[String,(Site,AppEntityMap)]

  /** Construct Top Level Structure For Current Situation
    * This method constructs a TopLevelStructure instance that reflects the current enablement status of sites,
    * applications, modules and entities. The intention here is to allow dynamic routing in an efficient way so that
    * a request can be matched against this structure efficiently and the corresponding objects involved accessed.
    * Note that the three object types (Site, Application, Entity) are the same three as in the Context. This isn't
    * a coincidence. Also note that the inner map of entities will contain both the singular and plural forms of the
    * entity's path as keys in the map. This makes it simple to invoke collection versus instance operations.
    * The intent of all this is to match paths like {{{http://site/application/entity}}} dynamically and quickly
    * locate the correct entity to which the request should be forwarded.
    * @return The TopLevelStructure mapping sites to applications to entities
    */
  def getAppEntities : SiteAppEntityMap = {
    Site.forEachEnabled { theSite ⇒
      theSite.name → {
        theSite → {
          for (app ← theSite.applications if app.isEnabled) yield {
            app.path -> {
              app → {
                for (
                  mod ← app.modules if mod.isEnabled;
                  entity ← mod.entities if entity.isEnabled;
                  name ← Seq(entity.path, entity.plural_path)
                ) yield {
                  name → (name → entity)
                }
              }.toMap
            }
          }
        }.toMap
      }
    }
  }.toMap

  // TODO: Instantiate the dispatching EntityProcessor (which requires configuration to work)
  val THE_DISPATCHER : ActorRef = EntityProcessor.makeSingletonRef

  /** Handle An Action
    * This is the main entry point into Scrupal for processing actions. It very simply forwards the action to
    * the dispatcher for processing and (quickly) returns a future that will be completed when the dispatcher gets
    * around to it. The point of this is to hide the use of actors within Scrupal and have a nice, simple, quickly
    * responding synchronous call in order to obtain the Future to the eventual result of the action.
    * @param action The action to act upon (a Request => Result[P] function).
    * @return A Future to the eventual Result[P]
    */
  def handle(action: Action) : Future[Result[_]] = {
    withExecutionContext { implicit ec: ExecutionContext ⇒
      THE_DISPATCHER.ask(action)(scrupal.core.actors.timeout) map { any ⇒ any.asInstanceOf[Result[_]] }
    }
  }

  /**
   * Called on application stop.
   */
  def onStop() {
  }


  /** Provide handling of configuration loading
    *
    * This method can be overridden by subclasses to refine the configuration read from default sources or do anything
    * else that might be interesting. This default version just prints the configuration to the log at TRACE level.
	  *
	  * @param config the loaded configuration
	  * @return The configuration that Scrupal should use
    */
	def onLoadConfig(config: Configuration): Configuration = {
    // Trace log the configuration
    log.trace("STARTUP CONFIGURATION VALUES")
    interestingConfig(config) foreach { case (key: String, value: ConfigValue) =>
      log.trace ( "    " + key + " = " + value.render(ConfigRenderOptions.defaults))
    }

    // Make things from the configuration override defaults and database read settings
    // Features
    config.getBoolean("scrupal.developer.mode") map   { value => CoreFeatures.DevMode.enabled(value) }
    config.getBoolean("scrupal.developer.footer") map { value => CoreFeatures.DebugFooter.enabled(value) }
    config.getBoolean("scrupal.config.wizard") map    { value => CoreFeatures.ConfigWizard.enabled(value) }

    // return the configuration
    config
 	}

	/**
	 * Called Just before the action is used.
	 *
	 */
	def doFilter(a: Action): Action = {
		a
	}


  /**
	 * Called when an exception occurred.
	 *
	 * The default is to send the default error page.
	 *
	 * @param request The HTTP request header
	 * @param ex The exception
	 * @return The result to send to the client
	 */
	def onError(request: Request, ex: Throwable) = {

	/*
		try {
			InternalServerError(Play.maybeApplication.map {
				case app if app.mode != Mode.Prod => views.html.defaultpages.devError.f
				case app => views.html.defaultpages.error.f
			}.getOrElse(views.html.defaultpages.devError.f) {
				ex match {
					case e: UsefulException => e
					case NonFatal(e) => UnexpectedException(unexpected = Some(e))
				}
			})
		} catch {
			case e: Throwable => {
				Logger.error("Error while rendering default error page", e)
				InternalServerError
			}
		}
		*/
	}

	/**
	 * Called when no action was found to serve a request.
	 *
	 * The default is to send the framework default 404 page.
	 *
	 * @param request the HTTP request header
	 * @return the result to send to the client
	 */
  def onHandlerNotFound(request: Request)  = {
		/*
		NotFound(Play.maybeApplication.map {
			case app if app.mode != Mode.Prod => views.html.defaultpages.devNotFound.f
			case app => views.html.defaultpages.notFound.f
		}.getOrElse(views.html.defaultpages.devNotFound.f)(request, Play.maybeApplication.flatMap(_.routes)))
		*/
	}

	/**
	 * Called when an action has been found, but the request parsing has failed.
	 *
	 * The default is to send the framework default 400 page.
	 *
	 * @param request the HTTP request header
	 * @return the result to send to the client
	 */
	def onBadRequest(request: Request, error: String)  = {
		/*
		BadRequest(views.html.defaultpages.badRequest(request, error))
		*/
	}

	def onRequestCompletion(request: Request) {
	}
}

object Scrupal {


}
