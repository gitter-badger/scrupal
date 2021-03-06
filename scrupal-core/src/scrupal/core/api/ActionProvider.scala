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

package scrupal.core.api

import scrupal.core.actions.{NodeAliasAction, NodeIdAction}
import scrupal.utils._
import spray.routing.PathMatcher.{Unmatched, Matched}
import spray.routing.PathMatchers._

/** An Object That Recursively Matches Paths And Provides And Action
  *
  * This trait is mixed in to classes that provide actions in conjunction with a singular or plural path segment by
  * extending PluralActionExtractor. This trait provides a final implementation of
  * [[scrupal.core.api.PluralActionExtractor.actionFor()]] that defers to `provideAction` method or, if that method
  * returns None, defers to subordinate ActionProviders.
  *
  * Subclasses must provide the set of subordinates, if any, and implement the `provideAction` method to convert
  * a context and the segment keyword that matched this object into an Action. If no action is provided then it is
  * treated as if this ActionProvider was not a match for the context and alternatives are attempted.
  */
trait ActionProvider extends SingularActionExtractor with DelegatingActionExtractor {

  /** Indicates whether this ActionProvider is at the bottom of the hierarchy.
    *
    * Action providers form a hierarchy that correspond to the initial segments of the path they match. This method
    * indicates whether this ActionProvider is at the leaf of the hierarchy (i.e. it has no subordinates).
    * @return
    */
  def isTerminal : Boolean = delegates.isEmpty

  /** Resolve an Action
    *
    * Given a path and a context, find the matching PathToAction and then invoke it to yield the corresponding Action.
    * A subclass must implement this method.
    *
    * @param keyUsed The key used to select this ActionProvider
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  final def actionFor(matchingSegment: String, context: Context) : Option[Action] = {
    provideAction(matchingSegment, context) match {
      case Some(action) ⇒ Some(action)
      case None ⇒ delegateAction(context)
    }
  }

  def provideAction(matchingSegment: String, context: Context) : Option[Action] = None
}

trait PluralActionProvider extends ActionProvider with PluralActionExtractor

/** ActionProvider with no subordinates
  *
  * Classes mixing this trait in are leaf nodes in the hierarchy of action providers. They have no subordinates and
  * return true for the isTerminal method.
  */
trait TerminalActionProvider extends ActionProvider {
  final override val delegates = Seq.empty[ActionExtractor]
  override val isTerminal = true
}

trait PluralTerminalActionProvider extends TerminalActionProvider with PluralActionExtractor

trait EnablementActionExtractor[T <: EnablementActionExtractor[T]]
  extends DelegatingActionExtractor with Enablement[T] with Enablee
{
  def delegates : Seq[ActionExtractor] = forEach[ActionExtractor] { e: Enablee ⇒
    e.isInstanceOf[ActionExtractor] && isEnabled(e, this)
  } { e: Enablee ⇒
    e.asInstanceOf[ActionExtractor]
  }

  def extractAction(context: Context) : Option[Action] = { delegateAction(context) }
}

trait EnablementActionProvider[T <: EnablementActionProvider[T]] extends ActionProvider with Enablement[T] with Enablee
{
  def delegates : Seq[ActionExtractor] = forEach[ActionExtractor] { e: Enablee ⇒
    e.isInstanceOf[ActionExtractor] && isEnabled(e, this)
  } { e: Enablee ⇒
    e.asInstanceOf[ActionExtractor]
  }
}

/** An ActionProvider that uses PathMatcherToAction instances for its matching actions
  *
  * PathMatcherToActionProviders convert an path into an action by searching a list of PathMatcherToAction instances.
  * PathMatchersToAction use a PathMatcher to implement a matches method that matches a Path against a PathMatcher. If
  * the match succeeds, the corresponding Action is returned. Because the PathMatcherToAction instances are searched
  * sequentially, this is not highly performant and the order of the PathMatcherToAction instances matters.

trait ActionExtractorToActionProvider extends ActionProvider {

  /** The Acceptable Matches
    *
    * This method should return a set of PathMatcherToAction instances that translate the matched path to an Action.
    * Be sure to list the longest patterns first as the first one that matches any prefix will win. So if you want
    * to match `/path/to/42` and `/path` then put the longer one first or else /path will get recognized first.
    * @return A Seq of PathMatcherToAction
    */
  def extractors: Seq[ActionExtractor] = Seq.empty[ActionExtractor]

  /** Resolve An Action
    *
    * Give a key, a path and a context, find the matching PathToAction and then invoke it to yield the corresponding
    * Action.
    *
    * @param key The key used to select this ActionProvider
    * @param path The path to use to match the PathToAction function
    * @param context The context to use to match the PathToAction function
    * @return
    */
  override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    for (p2a ← extractors ; action = p2a.matches(path, context) if action != None) { return action }
    None
  }
}

trait EnablementActionExtractorToActionProvider[T <: EnablementActionExtractorToActionProvider[T]]
  extends EnablementActionProvider[T] with ActionExtractorToActionProvider {
  override def actionFor(key: String, path: Uri.Path, context: Context) : Option[Action] = {
    for (p2a ← extractors ; action = p2a.matches(path, context) if action != None) { return action }
    super.actionFor(key, path, context)
  }
}
*/

object NodeProvider extends { val id: Symbol = 'Node ; val segment = id.name } with TerminalActionProvider {
  override def provideAction(matchingSegment: String, context: Context) : Option[Action] = {
    if (matchingSegment == singularKey) {
      val path = context.request.unmatchedPath

      {
        ScrupalPathMatchers.BSONObjectIdentifier(path) match {
          case Matched(pathRest, extractions) ⇒
            if (pathRest.isEmpty) {
              val id = extractions.head
              Some(NodeIdAction(id, context))
            } else {
              None
            }
          case Unmatched ⇒ None
        }
      } match {
        case Some(action) ⇒ Some(action)
        case None ⇒
          Segments(path) match {
            case Matched(pathRest, extractions) ⇒
              val path = extractions.head.mkString("/")
              Some(NodeAliasAction(path, context))
            case Unmatched ⇒
              None
          }
      }
    } else {
      None
    }
  }
}
