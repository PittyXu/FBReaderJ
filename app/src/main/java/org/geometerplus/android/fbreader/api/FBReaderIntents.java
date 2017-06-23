/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.api;

import android.content.Intent;

public abstract class FBReaderIntents {
	public static final String DEFAULT_PACKAGE = "org.geometerplus.zlibrary.ui.android";

	public interface Action {
		String VIEW                             = "android.fbreader.action.VIEW";
		String CONFIG_SERVICE                   = "android.fbreader.action.CONFIG_SERVICE";
		String LIBRARY                          = "android.fbreader.action.LIBRARY";
		String EXTERNAL_BOOKMARKS               = "android.fbreader.action.EXTERNAL_BOOKMARKS";
	}

	public interface Event {
		String CONFIG_OPTION_CHANGE             = "fbreader.config_service.option_change_event";
	}

	public interface Key {
		String BOOK                             = "fbreader.book";
		String BOOKMARK                         = "fbreader.bookmark";
	}

	public static Intent internalIntent(String action) {
		return new Intent(action).setPackage(DEFAULT_PACKAGE);
	}
}
