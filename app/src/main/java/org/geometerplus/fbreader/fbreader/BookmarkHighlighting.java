/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.fbreader.fbreader;

import android.support.annotation.ColorInt;

import org.geometerplus.android.fbreader.dao.Bookmark;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextSimpleHighlighting;
import org.geometerplus.zlibrary.text.view.ZLTextView;

public final class BookmarkHighlighting extends ZLTextSimpleHighlighting {
	final Bookmark bookmark;

	private static ZLTextPosition startPosition(Bookmark bookmark) {
		return new ZLTextFixedPosition(bookmark.startPosition.getParagraphIndex(), bookmark.startPosition.getElementIndex(), 0);
	}

	private static ZLTextPosition endPosition(Bookmark bookmark) {
		final ZLTextPosition end = bookmark.endPosition;
		if (end != null) {
			return end;
		}
		// TODO: compute end and save bookmark
		return bookmark.startPosition;
	}

	BookmarkHighlighting(ZLTextView view, Bookmark bookmark) {
		super(view, startPosition(bookmark), endPosition(bookmark));
		this.bookmark = bookmark;
	}

	@ColorInt
	@Override
	public Integer getBackgroundColor() {
		return bookmark.backgroundColor;
	}

	@ColorInt
	@Override
	public Integer getForegroundColor() {
		return bookmark.foregroundColor;
	}

	@ColorInt
	@Override
	public Integer getOutlineColor() {
		return null;
	}
}
