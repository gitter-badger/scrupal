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

package scrupal.controllers

import play.api.mvc.{Action,AnyContent}

/**
 * Asset controller for core assets. This one gets used by the templates
 */
object Assets extends controllers.AssetsBuilder
{
  override def at(path : String, file : String) : Action[AnyContent] = {
    super.at(path, file)
  }

	/**
	 * A way to obtain a theme css file just by the name of the theme
	 * @param name Name of the theme
	 * @return path to the theme's .css file
	 */
	 def theme(name: String) : Action[AnyContent] = {
		// TODO: This should validate that the theme exists
		super.at("/public/themes", name + ".css")
	}

}