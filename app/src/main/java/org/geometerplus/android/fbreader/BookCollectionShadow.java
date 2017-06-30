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

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.AbstractBookCollection;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookCollection;
import org.geometerplus.fbreader.book.BookQuery;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.BooksDatabase;
import org.geometerplus.fbreader.book.DbBook;
import org.geometerplus.fbreader.book.Filter;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.UID;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import java.util.ArrayList;
import java.util.List;

public class BookCollectionShadow extends AbstractBookCollection<Book> {
	private volatile Context myContext;
//	private volatile LibraryInterface myInterface;
	private final BooksDatabase myDatabase;
	private BookCollection myCollection;

	public BookCollectionShadow(Context pContext) {
		myContext = pContext;
		myDatabase = new SQLiteBooksDatabase(pContext);
		myCollection = new BookCollection(
				Paths.systemInfo(pContext), myDatabase, Paths.bookPath(pContext)
		);
		reset(true);
	}

	public synchronized void reset(final boolean force) {
		resetInternal(force);
	}

	private void resetInternal(boolean force) {
		final List<String> bookDirectories = Paths.bookPath(myContext);
		if (!force &&
				myCollection.status() != BookCollection.Status.NotStarted &&
				bookDirectories.equals(myCollection.BookDirectories)) {
			return;
		}

		myCollection = new BookCollection(
				Paths.systemInfo(myContext), myDatabase, bookDirectories
		);

		myCollection.startBuild();
	}

	public synchronized Status status() {
		return Status.valueOf(myCollection.status().toString());
	}

	public synchronized boolean hasBooks(Filter filter) {
		return myCollection.hasBooks(new BookQuery(filter, 1).Filter);
	}

	public List<Book> recentlyAddedBooks(final int count) {
		List<DbBook> books = myCollection.recentlyAddedBooks(count);
		List<Book> result = new ArrayList<>();
		for (DbBook b : books) {
			result.add(createBook(b));
		}
		return result;
	}

	public List<Book> recentlyOpenedBooks(final int count) {
		List<DbBook> books = myCollection.recentlyOpenedBooks(count);
		List<Book> result = new ArrayList<>();
		for (DbBook b : books) {
			result.add(createBook(b));
		}
		return result;
	}

	public synchronized Book getRecentBook(int index) {
		return createBook(myCollection.getRecentBook(index));
	}

	public synchronized Book getBookByFile(String path) {
		return createBook(myCollection.getBookByFile(path));
	}

	public synchronized Book getBookById(long id) {
		return createBook(myCollection.getBookById(id));
	}

	public synchronized Book getBookByUid(UID uid) {
		return createBook(myCollection.getBookByUid(new UID(uid.Type, uid.Id)));
	}

	public synchronized Book getBookByHash(String hash) {
		return createBook(myCollection.getBookByHash(hash));
	}

	public synchronized boolean saveBook(Book book) {
		return myCollection.saveBook(myCollection.createBook(book));
	}

	public synchronized boolean canRemoveBook(Book book, boolean deleteFromDisk) {
		return myCollection.canRemoveBook(myCollection.createBook(book), deleteFromDisk);
	}

	public synchronized void removeBook(Book book, boolean deleteFromDisk) {
		myCollection.removeBook(myCollection.createBook(book), deleteFromDisk);
	}

	public synchronized void addToRecentlyOpened(Book book) {
		myCollection.addToRecentlyOpened(myCollection.createBook(book));
	}

	public synchronized void removeFromRecentlyOpened(Book book) {
		myCollection.removeFromRecentlyOpened(myCollection.createBook(book));
	}

	public List<String> labels() {
		return myCollection.labels();
	}

	public String getHash(Book book, boolean force) {
		return myCollection.getHash(myCollection.createBook(book), force);
	}

	public void setHash(Book book, String hash) {
		myCollection.setHash(myCollection.createBook(book), hash);
	}

	public synchronized ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		final ZLTextPosition position = myCollection.getStoredPosition(bookId);
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
			myCollection.storePosition(bookId, new ZLTextFixedPosition.WithTimestamp(
					pos.ParagraphIndex, pos.ElementIndex, pos.CharIndex, pos.Timestamp
			));
		}
	}

	public synchronized boolean isHyperlinkVisited(Book book, String linkId) {
		return myCollection.isHyperlinkVisited(myCollection.createBook(book), linkId);
	}

	public synchronized void markHyperlinkAsVisited(Book book, String linkId) {
		myCollection.markHyperlinkAsVisited(myCollection.createBook(book), linkId);
	}

	@Override
	public List<Bookmark> bookmarks(final BookmarkQuery query) {
		return myCollection.bookmarks(query);
	}

	public synchronized void saveBookmark(Bookmark bookmark) {
		myCollection.saveBookmark(bookmark);
	}

	public synchronized void deleteBookmark(Bookmark bookmark) {
		myCollection.deleteBookmark(bookmark);
	}

	public synchronized List<String> deletedBookmarkUids() {
		return myCollection.deletedBookmarkUids();
	}

	public void purgeBookmarks(List<String> uids) {
		myCollection.purgeBookmarks(uids);
	}

	public synchronized HighlightingStyle getHighlightingStyle(int styleId) {
		return myCollection.getHighlightingStyle(styleId);
	}

	public List<HighlightingStyle> highlightingStyles() {
		return myCollection.highlightingStyles();
	}

	public synchronized void saveHighlightingStyle(HighlightingStyle style) {
		myCollection.saveHighlightingStyle(style);
	}

	public int getDefaultHighlightingStyleId() {
		return myCollection.getDefaultHighlightingStyleId();
	}

	public void setDefaultHighlightingStyleId(int styleId) {
		myCollection.setDefaultHighlightingStyleId(styleId);
	}

	public synchronized void rescan(String path) {
		myCollection.rescan(path);
	}

	public List<FormatDescriptor> formats() {
		return myCollection.formats();
	}

	public synchronized boolean setActiveFormats(List<String> formatIds) {
		if (myCollection.setActiveFormats(formatIds)) {
			reset(true);
			return true;
		} else {
			return false;
		}
	}

	public Book createBook(long id, String url, String title, String encoding, String language) {
		return new Book(id, url.substring("file://".length()), title, encoding, language);
	}

	public Book createBook(DbBook pBook) {
		return createBook(pBook.getId(), "file://" + pBook.getPath(), pBook.getTitle(), pBook.getEncodingNoDetection(), pBook.getLanguage());
	}
}
