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

package org.geometerplus.android.fbreader;

import android.content.Context;

import org.geometerplus.fbreader.book.AbstractBook;
import org.geometerplus.fbreader.book.AbstractBookCollection;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.BooksDatabase;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class BookCollectionShadow extends AbstractBookCollection<Book> {
	private final BooksDatabase myDatabase;
	private final PluginCollection mPluginCollection;
	private final Map<Integer, HighlightingStyle> myStyles =
			Collections.synchronizedMap(new TreeMap<Integer, HighlightingStyle>());

	public BookCollectionShadow(Context pContext) {
		myDatabase = new SQLiteBooksDatabase(pContext);
		mPluginCollection = PluginCollection.Instance();
	}

	public synchronized Book getBookByFile(String path) {
		Book book = new Book(-1, path, null, null, null);
		try {
			BookUtil.readMetainfo(book, mPluginCollection.getPlugin(ZLFile.createFileByPath(path)));
		} catch (BookReadingException pE) {
			pE.printStackTrace();
		}

		return book;
	}

	public synchronized ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		final ZLTextPosition position =  myDatabase.getStoredPosition(bookId);
		PositionWithTimestamp pos = position != null ? new PositionWithTimestamp(position) : null;
		if (pos == null) {
			return null;
		}

		return new ZLTextFixedPosition.WithTimestamp(
				pos.ParagraphIndex, pos.ElementIndex, pos.CharIndex, pos.Timestamp
		);
	}

	public synchronized void storePosition(long bookId, ZLTextPosition position) {
		if (position != null) {
			PositionWithTimestamp pos = new PositionWithTimestamp(position);
			if (bookId != -1) {
				myDatabase.storePosition(bookId, new ZLTextFixedPosition.WithTimestamp(
						pos.ParagraphIndex, pos.ElementIndex, pos.CharIndex, pos.Timestamp));
			}
		}
	}

	@Override
	public List<Bookmark> bookmarks(final BookmarkQuery query) {
		return myDatabase.loadBookmarks(query);
	}

	public synchronized void saveBookmark(Bookmark bookmark) {
		if (bookmark != null) {
			bookmark.setId(myDatabase.saveBookmark(bookmark));
			if (bookmark.IsVisible) {
				final AbstractBook book = bookmark.book;
				if (book != null) {
					book.HasBookmark = true;
					fireBookEvent(BookEvent.BookmarksUpdated, book);
				}
			}
		}
	}

	public synchronized void deleteBookmark(Bookmark bookmark) {
		if (bookmark != null && bookmark.getId() != -1) {
			myDatabase.deleteBookmark(bookmark);
			if (bookmark.IsVisible) {
				final AbstractBook book = bookmark.book;
				if (book != null) {
					book.HasBookmark = myDatabase.hasVisibleBookmark(bookmark.BookId);
					fireBookEvent(BookEvent.BookmarksUpdated, book);
				}
			}
		}
	}

	private synchronized void initStylesTable() {
		if (myStyles.isEmpty()) {
			for (HighlightingStyle style : myDatabase.loadStyles()) {
				myStyles.put(style.Id, style);
			}
		}
	}

	public synchronized HighlightingStyle getHighlightingStyle(int styleId) {
		initStylesTable();
		return myStyles.get(styleId);
	}

	public List<HighlightingStyle> highlightingStyles() {
		initStylesTable();
		return new ArrayList<>(myStyles.values());
	}

	public synchronized void saveHighlightingStyle(HighlightingStyle style) {
		myDatabase.saveStyle(style);
		myStyles.clear();
		fireBookEvent(BookEvent.BookmarkStyleChanged, null);
	}
}
