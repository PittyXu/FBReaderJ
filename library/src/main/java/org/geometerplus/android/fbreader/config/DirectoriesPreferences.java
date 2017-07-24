package org.geometerplus.android.fbreader.config;

import android.content.Context;

import org.geometerplus.android.util.DefaultPreferences;

import java.util.List;
import java.util.Set;

public class DirectoriesPreferences {
  private static final String BOOK_PATH = "directories_book_path";
  private static final String FONT_PATH = "directories_font_path";
  private static final String TEMP_DIR = "directories_temp_dir";

  public static void setBookPath(Context pContext, Set<String> path) {
    DefaultPreferences.getInstance(pContext).setStringSet(BOOK_PATH, path);
  }

  public static Set<String> getBookPath(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getStringSet(BOOK_PATH);
  }

  public static void setFontPath(Context pContext, Set<String> path) {
    DefaultPreferences.getInstance(pContext).setStringSet(FONT_PATH, path);
  }

  public static Set<String> getFontPath(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getStringSet(FONT_PATH);
  }

  public static void setTempDir(Context pContext, String path) {
    DefaultPreferences.getInstance(pContext).setString(TEMP_DIR, path);
  }

  public static String getTempDir(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(TEMP_DIR);
  }
}
