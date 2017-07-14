package org.geometerplus.android.fbreader.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;

import java.util.Date;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "reader_highlights".
 */
public class BookmarksDao extends AbstractDao<Bookmark, Long> {

  public static final String TABLENAME = "bookmarks";

  /**
   * Properties of entity ReaderHighlight.<br/>
   * Can be used for QueryBuilder and for referencing column names.
   */
  public static class Properties {

    public final static Property Id = new Property(0, Long.class, "id", true, "id");
    public final static Property BookCode = new Property(1, Integer.class, "bookCode", false,
        "book_code");
    public final static Property Type = new Property(2, Integer.class, "type", false, "type");
    public final static Property Uid = new Property(3, String.class, "uid", false, "uid");
    public final static Property Visible = new Property(4, Integer.class, "visible", false,
        "visible");
    public final static Property ForegroundColor = new Property(5, Integer.class, "foregroundColor",
        false, "fg_color");
    public final static Property BackgroundColor = new Property(6, Integer.class, "backgroundColor",
        false, "bg_color");
    public final static Property Title = new Property(7, String.class, "title", false, "title");
    public final static Property Text = new Property(8, String.class, "text", false, "text");
    public final static Property CreationTime = new Property(9, Date.class, "creationTime", false,
        "creation_time");
    public final static Property ModificationTime = new Property(10, Date.class, "modificationTime",
        false, "modification_time");
    public final static Property Paragraph = new Property(11, Integer.class, "paragraph", false,
        "paragraph");
    public final static Property Word = new Property(12, Integer.class, "word", false, "word");
    public final static Property Character = new Property(13, Integer.class, "character", false,
        "char");
    public final static Property EndParagraph = new Property(14, Integer.class, "endParagraph",
        false, "end_paragraph");
    public final static Property EndWord = new Property(15, Integer.class, "endWord", false,
        "end_word");
    public final static Property EndCharacter = new Property(16, Integer.class, "endCharacter",
        false, "end_char");
  }

  private BooksDaoSession daoSession;

  public BookmarksDao(DaoConfig config) {
    super(config);
  }

  public BookmarksDao(DaoConfig config, BooksDaoSession daoSession) {
    super(config, daoSession);
    this.daoSession = daoSession;
  }

  /** Creates the underlying database table. */
  public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
    String constraint = ifNotExists ? "IF NOT EXISTS " : "";
    db.execSQL("CREATE TABLE " + constraint + "\"" + TABLENAME + "\" (" + //
        "\"id\" INTEGER PRIMARY KEY AUTOINCREMENT , " +                   // 0: id
        "\"book_code\" INTEGER, " +                                       // 1: bookCode
        "\"type\" INTEGER DEFAULT 1, " +                                  // 2: type
        "\"uid\" TEXT(36) NOT NULL UNIQUE, " +                            // 3: uid
        "\"visible\" INTEGER DEFAULT 1, " +                               // 4: visible
        "\"fg_color\" INTEGER DEFAULT -1, " +                             // 5: fg_color
        "\"bg_color\" INTEGER DEFAULT 8948357, " +                        // 6: bg_color
        "\"title\" TEXT, " +                                              // 7: title
        "\"text\" TEXT NOT NULL, " +                                      // 8: text
        "\"creation_time\" INTEGER NOT NULL, " +                          // 9: creation_time
        "\"modification_time\" INTEGER, " +                               // 10: modification_time
        "\"paragraph\" INTEGER NOT NULL, " +                              // 11: paragraph
        "\"word\" INTEGER NOT NULL, " +                                   // 12: word
        "\"char\" INTEGER NOT NULL, " +                                   // 13: char
        "\"end_paragraph\" INTEGER, " +                                   // 14: end_paragraph
        "\"end_word\" INTEGER, " +                                        // 15: end_word
        "\"end_char\" INTEGER);");                                        // 16: end_char
    // Add Indexes
    db.execSQL("CREATE INDEX " + constraint
        + TABLENAME + "_book_code_paragraph_idx ON " + TABLENAME +
        " (\"book_code\",\"paragraph\");");
  }

  /** Drops the underlying database table. */
  public static void dropTable(SQLiteDatabase db, boolean ifExists) {
    String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + TABLENAME + "\"";
    db.execSQL(sql);
  }

  /** @inheritdoc */
  @Override
  protected void bindValues(SQLiteStatement stmt, Bookmark entity) {
    stmt.clearBindings();

    if (entity.id != null) {
      stmt.bindLong(1, entity.id);
    }

    if (entity.bookCode != null) {
      stmt.bindLong(2, entity.bookCode);
    }

    stmt.bindLong(3, entity.type);

    if (entity.uid != null) {
      stmt.bindString(4, entity.uid);
    }

    stmt.bindLong(5, entity.visible ? 1 : 0);
    stmt.bindLong(6, entity.foregroundColor);
    stmt.bindLong(7, entity.backgroundColor);
    if (entity.title != null) {
      stmt.bindString(8, entity.title);
    }
    if (entity.text != null) {
      stmt.bindString(9, entity.text);
    }
    if (entity.creationTime != null) {
      stmt.bindLong(10, entity.creationTime.getTime());
    }
    if (entity.modificationTime != null) {
      stmt.bindLong(11, entity.modificationTime.getTime());
    }
    if (entity.startPosition != null) {
      stmt.bindLong(12, entity.startPosition.ParagraphIndex);
      stmt.bindLong(13, entity.startPosition.ElementIndex);
      stmt.bindLong(14, entity.startPosition.CharIndex);
    }
    if (entity.endPosition != null) {
      stmt.bindLong(15, entity.endPosition.ParagraphIndex);
      stmt.bindLong(16, entity.endPosition.ElementIndex);
      stmt.bindLong(17, entity.endPosition.CharIndex);
    }
  }

  @Override
  protected void attachEntity(Bookmark entity) {
    super.attachEntity(entity);
    entity.__setDaoSession(daoSession);
  }

  /** @inheritdoc */
  @Override
  public Long readKey(Cursor cursor, int offset) {
    return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
  }

  /** @inheritdoc */
  @Override
  public Bookmark readEntity(Cursor cursor, int offset) {
    return new Bookmark( //
        cursor.isNull(offset) ? null : cursor.getLong(offset),                     // id
        cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1),             // bookCode
        cursor.getInt(offset + 2),                                                 // type
        cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3),           // uid
        cursor.getShort(offset + 4) == 1,                                          // visible
        cursor.getInt(offset + 5),
        // foregroundColor
        cursor.getInt(offset + 6),
        // backgroundColor
        cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7),           // title
        cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8),           // text
        cursor.isNull(offset + 9) ? null : new Date(cursor.getLong(offset + 9)),   // creationTime
        cursor.isNull(offset + 10) ? null : new Date(cursor.getLong(offset + 10)), // modificationTime
        cursor.getInt(offset + 11),                                                // paragraph
        cursor.getInt(offset + 12),                                                // word
        cursor.getInt(offset + 13),                                                // character
        cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14),            // endParagraph
        cursor.isNull(offset + 15) ? null : cursor.getInt(offset + 15),            // endWord
        cursor.isNull(offset + 16) ? null : cursor.getInt(offset + 16)             // endCharacter
    );
  }

  /** @inheritdoc */
  @Override
  public void readEntity(Cursor cursor, Bookmark entity, int offset) {
    entity.id = cursor.isNull(offset) ? null : cursor.getLong(offset);
    entity.bookCode = cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1);
    entity.type = cursor.getInt(offset + 2);
    entity.uid = cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3);
    entity.visible = cursor.getShort(4) == 1;
    entity.foregroundColor = cursor.getInt(5);
    entity.backgroundColor = cursor.getInt(6);
    entity.title = cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7);
    entity.text = cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8);
    entity.creationTime = cursor.isNull(offset + 9) ? null : new Date(cursor.getLong(offset + 9));
    entity.modificationTime = cursor.isNull(offset + 10) ? null : new Date(cursor.getLong(offset + 10));
    int paragraph = cursor.getInt(offset + 11);
    int word = cursor.getInt(offset + 12);
    int character = cursor.getInt(offset + 13);
    entity.startPosition = new ZLTextFixedPosition(paragraph, word, character);
    Integer endParagraph = cursor.isNull(offset + 14) ? 0 : cursor.getInt(offset + 14);
    Integer endWord = cursor.isNull(offset + 15) ? 0 : cursor.getInt(offset + 15);
    Integer endCharacter = cursor.isNull(offset + 16) ? null : cursor.getInt(offset + 16);
    if (null != endCharacter) {
      entity.endPosition = new ZLTextFixedPosition(endParagraph, endWord, endCharacter);
    }
  }

  /** @inheritdoc */
  @Override
  protected Long updateKeyAfterInsert(Bookmark entity, long rowId) {
    entity.id = rowId;
    return rowId;
  }

  /** @inheritdoc */
  @Override
  public Long getKey(Bookmark entity) {
    if (entity != null) {
      return entity.id;
    } else {
      return null;
    }
  }

  /** @inheritdoc */
  @Override
  protected boolean isEntityUpdateable() {
    return true;
  }
}
