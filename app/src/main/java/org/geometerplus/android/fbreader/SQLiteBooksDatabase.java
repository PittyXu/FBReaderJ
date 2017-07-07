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

package org.geometerplus.android.fbreader;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.android.util.SQLiteUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.BooksDatabase;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;

final class SQLiteBooksDatabase extends BooksDatabase {
	private final SQLiteDatabase myDatabase;
	private final HashMap<String,SQLiteStatement> myStatements = new HashMap<>();

	SQLiteBooksDatabase(Context context) {
		myDatabase = context.openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	@Override
	public void finalize() {
		myDatabase.close();
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		final int currentVersion = 40;
		if (version >= currentVersion) {
			return;
		}

		myDatabase.beginTransaction();

		createTables();
		myDatabase.setTransactionSuccessful();
		myDatabase.setVersion(currentVersion);
		myDatabase.endTransaction();

		myDatabase.execSQL("VACUUM");
	}

	@Override
	public boolean hasVisibleBookmark(long bookId) {
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT bookmark_id FROM Bookmarks WHERE book_id = " + bookId +
			" AND visible = 1 LIMIT 1", null
		);
		final boolean result = cursor.moveToNext();
		cursor.close();
		return result;
	}

	@Override
	public List<Bookmark> loadBookmarks(BookmarkQuery query) {
		final LinkedList<Bookmark> list = new LinkedList<>();
		final StringBuilder sql = new StringBuilder("SELECT")
			.append(" bm.bookmark_id,bm.uid,bm.version_uid,")
			.append(" bm.bookmark_text,bm.original_text,")
			.append("bm.creation_time,bm.modification_time,bm.access_time,")
			.append("bm.model_id,bm.paragraph,bm.word,bm.char,")
			.append("bm.end_paragraph,bm.end_word,bm.end_character,")
			.append("bm.style_id")
			.append(" FROM Bookmarks AS bm")
			.append(" WHERE")
			.append(" bm.visible = " + (query.Visible ? 1 : 0))
			.append(" ORDER BY bm.bookmark_id")
			.append(" LIMIT " + query.Limit * query.Page + "," + query.Limit);
		Cursor cursor = myDatabase.rawQuery(sql.toString(), null);
		while (cursor.moveToNext()) {
			list.add(createBookmark(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				-1, null,
				cursor.getString(3),
				cursor.isNull(4) ? null : cursor.getString(4),
				cursor.getLong(5),
				cursor.isNull(6) ? null : cursor.getLong(6),
				cursor.isNull(7) ? null : cursor.getLong(7),
				cursor.getString(8),
				(int)cursor.getLong(9),
				(int)cursor.getLong(10),
				(int)cursor.getLong(11),
				(int)cursor.getLong(12),
				cursor.isNull(13) ? -1 : (int)cursor.getLong(13),
				cursor.isNull(14) ? -1 : (int)cursor.getLong(14),
				query.Visible,
				(int)cursor.getLong(15)
			));
		}
		cursor.close();
		return list;
	}

	@Override
	public List<HighlightingStyle> loadStyles() {
		final LinkedList<HighlightingStyle> list = new LinkedList<>();
		final String sql = "SELECT style_id,timestamp,name,bg_color,fg_color FROM HighlightingStyle";
		final Cursor cursor = myDatabase.rawQuery(sql, null);
		while (cursor.moveToNext()) {
			final String name = cursor.getString(2);
			final int bgColor = (int)cursor.getLong(3);
			final int fgColor = (int)cursor.getLong(4);
			list.add(createStyle(
				(int)cursor.getLong(0),
				cursor.getLong(1),
				name.length() > 0 ? name : null,
				bgColor != -1 ? bgColor : null,
				fgColor != -1 ? fgColor : null
			));
		}
		cursor.close();
		return list;
	}

	public void saveStyle(HighlightingStyle style) {
		final SQLiteStatement statement = get(
			"INSERT OR REPLACE INTO HighlightingStyle (style_id,name,bg_color,fg_color,timestamp) VALUES (?,?,?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, style.Id);
			final String name = style.getNameOrNull();
			statement.bindString(2, name != null ? name : "");
			final Integer bgColor = style.getBackgroundColor();
			statement.bindLong(3, bgColor != null ? bgColor : -1);
			final Integer fgColor = style.getForegroundColor();
			statement.bindLong(4, fgColor != null ? fgColor : -1);
			statement.bindLong(5, System.currentTimeMillis());
			statement.execute();
		}
	}

	// this is workaround for working with old format plugins;
	// it should never go via the third way with new versions
	private String uid(Bookmark bookmark) {
		if (bookmark.Uid != null) {
			return bookmark.Uid;
		}
		if (bookmark.getId() == -1) {
			return UUID.randomUUID().toString();
		}

		final Cursor cursor = myDatabase.rawQuery(
			"SELECT uid FROM Bookmarks WHERE bookmark_id = " + bookmark.getId(), null
		);
		try {
			if (cursor.moveToNext()) {
				return cursor.getString(0);
			}
		} finally {
			cursor.close();
		}

		return UUID.randomUUID().toString();
	}

	@Override
	public long saveBookmark(Bookmark bookmark) {
		final SQLiteStatement statement;
		final long bookmarkId = bookmark.getId();

		if (bookmarkId == -1) {
			statement = get(
				"INSERT INTO Bookmarks (uid,version_uid,book_id,bookmark_text,original_text,creation_time,modification_time,access_time,model_id,paragraph,word,char,end_paragraph,end_word,end_character,visible,style_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
			);
		} else {
			statement = get(
				"UPDATE Bookmarks SET uid=?,version_uid=?,book_id=?,bookmark_text=?,original_text=?,creation_time=?,modification_time=?,access_time=?,model_id=?,paragraph=?,word=?,char=?,end_paragraph=?,end_word=?,end_character=?,visible=?,style_id=? WHERE bookmark_id=?"
			);
		}

		synchronized (statement) {
			int fieldCount = 0;
			SQLiteUtil.bindString(statement, ++fieldCount, uid(bookmark));
			SQLiteUtil.bindString(statement, ++fieldCount, bookmark.getVersionUid());
			statement.bindLong(++fieldCount, bookmark.BookId);
			statement.bindString(++fieldCount, bookmark.getText());
			SQLiteUtil.bindString(statement, ++fieldCount, bookmark.getOriginalText());
			SQLiteUtil.bindLong(statement, ++fieldCount, bookmark.getTimestamp(Bookmark.DateType.Creation));
			SQLiteUtil.bindLong(statement, ++fieldCount, bookmark.getTimestamp(Bookmark.DateType.Modification));
			SQLiteUtil.bindLong(statement, ++fieldCount, bookmark.getTimestamp(Bookmark.DateType.Access));
			SQLiteUtil.bindString(statement, ++fieldCount, bookmark.ModelId);
			statement.bindLong(++fieldCount, bookmark.ParagraphIndex);
			statement.bindLong(++fieldCount, bookmark.ElementIndex);
			statement.bindLong(++fieldCount, bookmark.CharIndex);
			final ZLTextPosition end = bookmark.getEnd();
			if (end != null) {
				statement.bindLong(++fieldCount, end.getParagraphIndex());
				statement.bindLong(++fieldCount, end.getElementIndex());
				statement.bindLong(++fieldCount, end.getCharIndex());
			} else {
				statement.bindLong(++fieldCount, bookmark.getLength());
				statement.bindNull(++fieldCount);
				statement.bindNull(++fieldCount);
			}
			statement.bindLong(++fieldCount, bookmark.IsVisible ? 1 : 0);
			statement.bindLong(++fieldCount, bookmark.getStyleId());

			if (bookmarkId == -1) {
				return statement.executeInsert();
			} else {
				statement.bindLong(++fieldCount, bookmarkId);
				statement.execute();
				return bookmarkId;
			}
		}
	}

	@Override
	public void deleteBookmark(Bookmark bookmark) {
		final String uuid = uid(bookmark);
		SQLiteStatement statement = get("DELETE FROM Bookmarks WHERE uid=?");
		synchronized (statement) {
			statement.bindString(1, uuid);
			statement.execute();
		}
	}

	public ZLTextFixedPosition.WithTimestamp getStoredPosition(long bookId) {
		ZLTextFixedPosition.WithTimestamp position = null;
		final Cursor cursor = myDatabase.rawQuery(
			"SELECT paragraph,word,char,timestamp FROM BookState WHERE book_id = " + bookId, null
		);
		if (cursor.moveToNext()) {
			position = new ZLTextFixedPosition.WithTimestamp(
				(int)cursor.getLong(0),
				(int)cursor.getLong(1),
				(int)cursor.getLong(2),
				cursor.getLong(3)
			);
		}
		cursor.close();
		return position;
	}

	public void storePosition(long bookId, ZLTextPosition position) {
		final SQLiteStatement statement = get(
			"INSERT OR REPLACE INTO BookState (book_id,paragraph,word,char,timestamp) VALUES (?,?,?,?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindLong(2, position.getParagraphIndex());
			statement.bindLong(3, position.getElementIndex());
			statement.bindLong(4, position.getCharIndex());

			long timestamp = -1;
			if (position instanceof ZLTextFixedPosition.WithTimestamp) {
				timestamp = ((ZLTextFixedPosition.WithTimestamp)position).Timestamp;
			}
			if (timestamp == -1) {
				timestamp = System.currentTimeMillis();
			}
			statement.bindLong(5, timestamp);

			statement.execute();
		}
	}

	private void deleteVisitedHyperlinks(long bookId) {
		final SQLiteStatement statement = get("DELETE FROM VisitedHyperlinks WHERE book_id=?");
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.execute();
		}
	}

	protected void addVisitedHyperlink(long bookId, String hyperlinkId) {
		final SQLiteStatement statement = get(
			"INSERT OR IGNORE INTO VisitedHyperlinks(book_id,hyperlink_id) VALUES (?,?)"
		);
		synchronized (statement) {
			statement.bindLong(1, bookId);
			statement.bindString(2, hyperlinkId);
			statement.execute();
		}
	}

	protected Collection<String> loadVisitedHyperlinks(long bookId) {
		final TreeSet<String> links = new TreeSet<String>();
		final Cursor cursor = myDatabase.rawQuery("SELECT hyperlink_id FROM VisitedHyperlinks WHERE book_id = ?", new String[] { String.valueOf(bookId) });
		while (cursor.moveToNext()) {
			links.add(cursor.getString(0));
		}
		cursor.close();
		return links;
	}

	private void createTables() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookState(" +
				"book_id INTEGER UNIQUE NOT NULL REFERENCES Books(book_id)," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL," +
				"timestamp INTEGER DEFAULT 0)");

		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS VisitedHyperlinks(" +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"hyperlink_id TEXT NOT NULL," +
				"CONSTRAINT VisitedHyperlinks_Unique UNIQUE (book_id, hyperlink_id))");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS HighlightingStyle(" +
				"style_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"fg_color INTEGER NOT NULL DEFAULT -1," +
				"bg_color INTEGER NOT NULL," +
				"timestamp INTEGER DEFAULT 0)");

		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (1, '', 136*256*256 + 138*256 + 133)"); // #888a85
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (2, '', 245*256*256 + 121*256 + 0)"); // #f57900
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (3, '', 114*256*256 + 159*256 + 207)"); // #729fcf

		myDatabase.execSQL(
				"CREATE TABLE IF NOT EXISTS Bookmarks(" +
						"bookmark_id INTEGER PRIMARY KEY," +
						"uid TEXT(36) NOT NULL UNIQUE," +
						"version_uid TEXT(36)," +
						"visible INTEGER DEFAULT 1," +
						"style_id INTEGER NOT NULL REFERENCES HighlightingStyle(style_id) DEFAULT 1," +
						"bookmark_text TEXT NOT NULL," +
						"creation_time INTEGER NOT NULL," +
						"modification_time INTEGER," +
						"access_time INTEGER," +
						"model_id TEXT," +
						"paragraph INTEGER NOT NULL," +
						"word INTEGER NOT NULL," +
						"char INTEGER NOT NULL," +
						"end_paragraph INTEGER," +
						"end_word INTEGER," +
						"end_character INTEGER," +
						"original_text TEXT DEFAULT NULL)"
		);
	}

	private SQLiteStatement get(String sql) {
		SQLiteStatement statement = myStatements.get(sql);
		if (statement == null) {
			statement = myDatabase.compileStatement(sql);
			myStatements.put(sql, statement);
		}
		return statement;
	}
}
