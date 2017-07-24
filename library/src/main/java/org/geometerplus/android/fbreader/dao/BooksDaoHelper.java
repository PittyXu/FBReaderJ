package org.geometerplus.android.fbreader.dao;


import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import org.geometerplus.android.fbreader.Constant;

import de.greenrobot.dao.query.QueryBuilder;

public class BooksDaoHelper {

  private static final boolean DEBUG = Constant.DEBUG;

  private BooksDaoSession mSession;

  private static class SingletonHolder {

    private static final BooksDaoHelper INSTANCE = new BooksDaoHelper();
  }

  private BooksDaoHelper() {
    if (DEBUG) {
      QueryBuilder.LOG_SQL = true;
      QueryBuilder.LOG_VALUES = true;
    }
  }

  public static BooksDaoHelper getInstance(Context pContext) {
    if (null == SingletonHolder.INSTANCE.mSession) {
      synchronized (BooksDaoHelper.class) {
        SQLiteOpenHelper helper = BooksDaoMaster.getHelper(pContext);
        BooksDaoMaster master = new BooksDaoMaster(helper.getWritableDatabase());
        SingletonHolder.INSTANCE.mSession = master.newSession();
      }
    }

    return SingletonHolder.INSTANCE;
  }

  public BookmarksDao getBookmarksDao() {
    return mSession.getBookmarksDao();
  }

  public BookStateDao getBookStateDao() {
    return mSession.getBookStateDao();
  }
}
