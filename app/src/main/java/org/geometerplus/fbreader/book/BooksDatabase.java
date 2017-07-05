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

package org.geometerplus.fbreader.book;

import android.support.annotation.ColorInt;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import java.util.Collection;
import java.util.List;

public abstract class BooksDatabase {
	// returns map fileId -> book
	public abstract boolean hasVisibleBookmark(long bookId);

	protected Bookmark createBookmark(
		long id, String uid, String versionUid,
		long bookId, String bookTitle, String text, String originalText,
		long creationTimestamp, Long modificationTimestamp, Long accessTimestamp,
		String modelId,
		int start_paragraphIndex, int start_wordIndex, int start_charIndex,
		int end_paragraphIndex, int end_wordIndex, int end_charIndex,
		boolean isVisible,
		int styleId
	) {
		return new Bookmark(null,
			id, uid, versionUid,
			bookId, bookTitle, text, originalText,
			creationTimestamp, modificationTimestamp, accessTimestamp,
			modelId,
			start_paragraphIndex, start_wordIndex, start_charIndex,
			end_paragraphIndex, end_wordIndex, end_charIndex,
			isVisible,
			styleId
		);
	}

	public abstract List<Bookmark> loadBookmarks(BookmarkQuery query);
	public abstract long saveBookmark(Bookmark bookmark);
	public abstract void deleteBookmark(Bookmark bookmark);

	protected HighlightingStyle createStyle(int id, long timestamp, String name, @ColorInt Integer bgColor, @ColorInt Integer fgColor) {
		return new HighlightingStyle(id, timestamp, name, bgColor, fgColor);
	}
	public abstract List<HighlightingStyle> loadStyles();
	public abstract void saveStyle(HighlightingStyle style);

	public abstract ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId);
	public abstract void storePosition(long bookId, ZLTextPosition position);

	protected abstract Collection<String> loadVisitedHyperlinks(long bookId);
	protected abstract void addVisitedHyperlink(long bookId, String hyperlinkId);
}
