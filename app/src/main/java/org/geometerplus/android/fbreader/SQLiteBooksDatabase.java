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
	private final HashMap<String,SQLiteStatement> myStatements =
		new HashMap<String,SQLiteStatement>();

	SQLiteBooksDatabase(Context context) {
		myDatabase = context.openOrCreateDatabase("books.db", Context.MODE_PRIVATE, null);
		migrate();
	}

	@Override
	public void finalize() {
		myDatabase.close();
	}

	protected void executeAsTransaction(Runnable actions) {
		boolean transactionStarted = false;
		try {
			myDatabase.beginTransaction();
			transactionStarted = true;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			actions.run();
			if (transactionStarted) {
				myDatabase.setTransactionSuccessful();
			}
		} finally {
			if (transactionStarted) {
				myDatabase.endTransaction();
			}
		}
	}

	private void migrate() {
		final int version = myDatabase.getVersion();
		final int currentVersion = 40;
		if (version >= currentVersion) {
			return;
		}

		myDatabase.beginTransaction();

		switch (version) {
			case 0:
				createTables();
			case 3:
				updateTables3();
			case 4:
				updateTables4();
			case 5:
				updateTables5();
			case 6:
				updateTables6();
			case 8:
				updateTables8();
			case 9:
				updateTables9();
			case 11:
				updateTables11();
			case 12:
				updateTables12();
			case 13:
				updateTables13();
			case 15:
				updateTables15();
			case 16:
				updateTables16();
			case 17:
				updateTables17();
			case 19:
				updateTables19();
			case 22:
				updateTables22();
			case 23:
				updateTables23();
			case 24:
				updateTables24();
			case 27:
				updateTables27();
			case 28:
				updateTables28();
			case 30:
				updateTables30();
			case 33:
				updateTables33();
			case 35:
				updateTables35();
			case 36:
				updateTables36();
			case 37:
				updateTables37();
			case 38:
				updateTables38();
		}
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
			.append("bm.book_id,b.title,bm.bookmark_text,bm.original_text,")
			.append("bm.creation_time,bm.modification_time,bm.access_time,")
			.append("bm.model_id,bm.paragraph,bm.word,bm.char,")
			.append("bm.end_paragraph,bm.end_word,bm.end_character,")
			.append("bm.style_id")
			.append(" FROM Bookmarks AS bm")
			.append(" INNER JOIN Books AS b ON b.book_id = bm.book_id")
			.append(" WHERE");
		if (query.Book != null) {
			sql.append(" b.book_id = " + query.Book.getId() +" AND");
		}
		sql
			.append(" bm.visible = " + (query.Visible ? 1 : 0))
			.append(" ORDER BY bm.bookmark_id")
			.append(" LIMIT " + query.Limit * query.Page + "," + query.Limit);
		Cursor cursor = myDatabase.rawQuery(sql.toString(), null);
		while (cursor.moveToNext()) {
			list.add(createBookmark(
				cursor.getLong(0),
				cursor.getString(1),
				cursor.getString(2),
				cursor.getLong(3),
				cursor.getString(4),
				cursor.getString(5),
				cursor.isNull(6) ? null : cursor.getString(6),
				cursor.getLong(7),
				cursor.isNull(8) ? null : cursor.getLong(8),
				cursor.isNull(9) ? null : cursor.getLong(9),
				cursor.getString(10),
				(int)cursor.getLong(11),
				(int)cursor.getLong(12),
				(int)cursor.getLong(13),
				(int)cursor.getLong(14),
				cursor.isNull(15) ? -1 : (int)cursor.getLong(15),
				cursor.isNull(16) ? -1 : (int)cursor.getLong(16),
				query.Visible,
				(int)cursor.getLong(17)
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
			"CREATE TABLE IF NOT EXISTS Books(" +
				"book_id INTEGER PRIMARY KEY," +
				"encoding TEXT," +
				"language TEXT," +
				"title TEXT NOT NULL," +
				"file_name TEXT UNIQUE NOT NULL)");
	}

	private void updateTables3() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Files(" +
				"file_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"parent_id INTEGER REFERENCES Files(file_id)," +
				"size INTEGER," +
				"CONSTRAINT Files_Unique UNIQUE (name, parent_id))");
	}

	private void updateTables4() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS RecentBooks(" +
				"book_index INTEGER PRIMARY KEY," +
				"book_id INTEGER REFERENCES Books(book_id))");
	}

	private void updateTables5() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Bookmarks(" +
				"bookmark_id INTEGER PRIMARY KEY," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"bookmark_text TEXT NOT NULL," +
				"creation_time INTEGER NOT NULL," +
				"modification_time INTEGER," +
				"access_time INTEGER," +
				"access_counter INTEGER NOT NULL," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL)");

		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookState(" +
				"book_id INTEGER UNIQUE NOT NULL REFERENCES Books(book_id)," +
				"paragraph INTEGER NOT NULL," +
				"word INTEGER NOT NULL," +
				"char INTEGER NOT NULL)");
		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id,file_name FROM Books", null
		);
		final SQLiteStatement statement = myDatabase.compileStatement("INSERT INTO BookState (book_id,paragraph,word,char) VALUES (?,?,?,?)");
		while (cursor.moveToNext()) {
			final long bookId = cursor.getLong(0);
			final String fileName = cursor.getString(1);
			final int position = 0;
			final int paragraph = 0;
			final int word = 0;
			final int chr = 0;
			if ((paragraph != 0) || (word != 0) || (chr != 0)) {
				statement.bindLong(1, bookId);
				statement.bindLong(2, paragraph);
				statement.bindLong(3, word);
				statement.bindLong(4, chr);
				statement.execute();
			}
		}
		cursor.close();
	}

	private void updateTables6() {
		myDatabase.execSQL(
			"ALTER TABLE Bookmarks ADD COLUMN model_id TEXT"
		);

		myDatabase.execSQL(
			"ALTER TABLE Books ADD COLUMN file_id INTEGER"
		);

		Cursor cursor = myDatabase.rawQuery(
			"SELECT book_id,file_name FROM Books", null
		);
		final SQLiteStatement deleteStatement = myDatabase.compileStatement("DELETE FROM Books WHERE book_id=?");
		final SQLiteStatement updateStatement = myDatabase.compileStatement("UPDATE OR IGNORE Books SET file_id=? WHERE book_id=?");
		while (cursor.moveToNext()) {
			final long bookId = cursor.getLong(0);

			updateStatement.bindLong(2, bookId);
			updateStatement.execute();
		}
		cursor.close();

		myDatabase.execSQL("ALTER TABLE Books RENAME TO Books_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Books(" +
				"book_id INTEGER PRIMARY KEY," +
				"encoding TEXT," +
				"language TEXT," +
				"title TEXT NOT NULL," +
				"file_id INTEGER UNIQUE NOT NULL REFERENCES Files(file_id))");
		myDatabase.execSQL("INSERT INTO Books (book_id,encoding,language,title,file_id) SELECT book_id,encoding,language,title,file_id FROM Books_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Books_Obsolete");
	}

	private void updateTables8() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookList ( " +
				"book_id INTEGER UNIQUE NOT NULL REFERENCES Books (book_id))");
	}

	private void updateTables9() {
		myDatabase.execSQL("CREATE INDEX BookList_BookIndex ON BookList (book_id)");
	}

	private void updateTables11() {
		myDatabase.execSQL("UPDATE Files SET size = size + 1");
	}

	private void updateTables12() {
		myDatabase.execSQL("DELETE FROM Files WHERE parent_id IN (SELECT file_id FROM Files WHERE name LIKE '%.epub')");
	}

	private void updateTables13() {
		myDatabase.execSQL(
			"ALTER TABLE Bookmarks ADD COLUMN visible INTEGER DEFAULT 1"
		);
	}

	private void updateTables15() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS VisitedHyperlinks(" +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
				"hyperlink_id TEXT NOT NULL," +
				"CONSTRAINT VisitedHyperlinks_Unique UNIQUE (book_id, hyperlink_id))");
	}

	private void updateTables16() {
		myDatabase.execSQL(
			"ALTER TABLE Books ADD COLUMN `exists` INTEGER DEFAULT 1"
		);
	}

	private void updateTables17() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS BookStatus(" +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id) PRIMARY KEY," +
				"access_time INTEGER NOT NULL," +
				"pages_full INTEGER NOT NULL," +
				"page_current INTEGER NOT NULL)");
	}

	private void updateTables19() {
		myDatabase.execSQL("DROP TABLE IF EXISTS BookList");
	}

	private void updateTables22() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN end_paragraph INTEGER");
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN end_word INTEGER");
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN end_character INTEGER");
	}

	private void updateTables23() {
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS HighlightingStyle(" +
				"style_id INTEGER PRIMARY KEY," +
				"name TEXT NOT NULL," +
				"bg_color INTEGER NOT NULL)");
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN style_id INTEGER NOT NULL REFERENCES HighlightingStyle(style_id) DEFAULT 1");
		myDatabase.execSQL("UPDATE Bookmarks SET end_paragraph = LENGTH(bookmark_text)");
	}

	private void updateTables24() {
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (1, '', 136*256*256 + 138*256 + 133)"); // #888a85
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (2, '', 245*256*256 + 121*256 + 0)"); // #f57900
		myDatabase.execSQL("INSERT OR REPLACE INTO HighlightingStyle (style_id, name, bg_color) VALUES (3, '', 114*256*256 + 159*256 + 207)"); // #729fcf
	}

	private void updateTables27() {
		myDatabase.execSQL("ALTER TABLE BookState ADD COLUMN timestamp INTEGER");
	}

	private void updateTables28() {
		myDatabase.execSQL("ALTER TABLE HighlightingStyle ADD COLUMN fg_color INTEGER NOT NULL DEFAULT -1");
	}

	private void updateTables30() {
		myDatabase.execSQL("DROP TABLE IF EXISTS RecentBooks");
	}

	private void updateTables33() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN uid TEXT(36)");
		final Cursor cursor = myDatabase.rawQuery("SELECT bookmark_id FROM Bookmarks", null);
		final SQLiteStatement statement = get("UPDATE Bookmarks SET uid=? WHERE bookmark_id=?");
		while (cursor.moveToNext()) {
			statement.bindString(1, UUID.randomUUID().toString());
			statement.bindLong(2, cursor.getLong(0));
			statement.execute();
		}
		cursor.close();

		myDatabase.execSQL("ALTER TABLE Bookmarks RENAME TO Bookmarks_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Bookmarks(" +
				"bookmark_id INTEGER PRIMARY KEY," +
				"uid TEXT(36) NOT NULL UNIQUE," +
				"version_uid TEXT(36)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
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
				"end_character INTEGER)"
		);
		final String fields = "bookmark_id,uid,book_id,visible,style_id,bookmark_text,creation_time,modification_time,access_time,model_id,paragraph,word,char,end_paragraph,end_word,end_character";
		myDatabase.execSQL("INSERT INTO Bookmarks (" + fields + ") SELECT " + fields + " FROM Bookmarks_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Bookmarks_Obsolete");
	}

	private void updateTables35() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN original_text TEXT DEFAULT NULL");
	}

	private int styleBg(int styleId) {
		switch (styleId) {
			case 1:
				return 0x888a85;
			case 2:
				return 0xf57900;
			case 3:
				return 0x729fcf;
			default:
				return 0;
		}
	}

	private void updateTables36() {
		myDatabase.execSQL("ALTER TABLE HighlightingStyle ADD COLUMN timestamp INTEGER DEFAULT 0");

		final String sql = "SELECT style_id,name,bg_color FROM HighlightingStyle";
		final Cursor cursor = myDatabase.rawQuery(sql, null);
		final SQLiteStatement statement =
			get("UPDATE HighlightingStyle SET timestamp=? WHERE style_id=?");
		while (cursor.moveToNext()) {
			final int styleId = (int)cursor.getLong(0);
			if ((!cursor.isNull(1) && !"".equals(cursor.getString(1))) ||
					styleBg(styleId) != (int)cursor.getLong(2)) {
				statement.bindLong(1, System.currentTimeMillis());
				statement.bindLong(2, styleId);
				statement.execute();
			}
		}
		cursor.close();
	}

	private void updateTables37() {
		myDatabase.execSQL("ALTER TABLE Bookmarks RENAME TO Bookmarks_Obsolete");
		myDatabase.execSQL(
			"CREATE TABLE IF NOT EXISTS Bookmarks(" +
				"bookmark_id INTEGER PRIMARY KEY," +
				"uid TEXT(36) NOT NULL UNIQUE," +
				"version_uid TEXT(36)," +
				"book_id INTEGER NOT NULL REFERENCES Books(book_id)," +
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
				"end_character INTEGER)"
		);
		final String fields = "bookmark_id,uid,version_uid,book_id,visible,style_id,bookmark_text,creation_time,modification_time,access_time,model_id,paragraph,word,char,end_paragraph,end_word,end_character";
		myDatabase.execSQL("INSERT INTO Bookmarks (" + fields + ") SELECT " + fields + " FROM Bookmarks_Obsolete");
		myDatabase.execSQL("DROP TABLE IF EXISTS Bookmarks_Obsolete");
	}

	private void updateTables38() {
		myDatabase.execSQL("ALTER TABLE Bookmarks ADD COLUMN original_text TEXT DEFAULT NULL");
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
