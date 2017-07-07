package org.geometerplus.android.fbreader.config;

import android.content.Context;

import org.geometerplus.android.util.DefaultPreferences;

import java.util.List;
import java.util.Set;

public class MiscPreferences {
  public enum WordTappingActionEnum {
    doNothing, selectSingleWord, startSelecting, openDictionary
  }

  public enum FootnoteToastEnum {
    never, footnotesOnly, footnotesAndSuperscripts, allInternalLinks
  }

  private static final String BOOKMARK_CURRENT_TAB = "misc_bookmark_current_tab";
  private static final String ENABLE_DOUBLE_TAP = "misc_enable_double_tap";
  private static final String NAVIGATE_ALL_WORDS = "misc_navigate_all_words";
  private static final String WORD_TAPPING_ACTION = "misc_word_tapping_action";
  private static final String SHOW_FOOTNOTE_TOAST = "misc_show_footnote_toast";
  private static final String TAP_ZONE_ACTION = "misc_tap_zone_action_";
  private static final String TAP_ZONE_WIDTH = "misc_tap_zone_width_";
  private static final String TAP_ZONE_HEIGHT = "misc_tap_zone_height_";
  private static final String TAP_ZONE_MAP_LIST = "misc_tap_zone_map_list";
  private static final String KEYS = "misc_keys_";
  private static final String KEY_LIST = "misc_key_list_";

  public static void setBookmarkCurrentTab(Context pContext, String tab) {
    DefaultPreferences.getInstance(pContext).setString(BOOKMARK_CURRENT_TAB, tab);
  }

  public static String getBookmarkCurrentTab(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(BOOKMARK_CURRENT_TAB, "");
  }

  public static void setEnableDoubleTap(Context pContext, boolean enable) {
    DefaultPreferences.getInstance(pContext).setBoolean(ENABLE_DOUBLE_TAP, enable);
  }

  public static boolean getEnableDoubleTap(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(ENABLE_DOUBLE_TAP);
  }

  public static void setNavigateAllWords(Context pContext, boolean nav) {
    DefaultPreferences.getInstance(pContext).setBoolean(NAVIGATE_ALL_WORDS, nav);
  }

  public static boolean getNavigateAllWords(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(NAVIGATE_ALL_WORDS);
  }

  public static void setWordTappingAction(Context pContext, int action) {
    DefaultPreferences.getInstance(pContext).setInt(WORD_TAPPING_ACTION, action);
  }

  public static int getWordTappingAction(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(WORD_TAPPING_ACTION, WordTappingActionEnum.startSelecting.ordinal());
  }

  public static void setShowFootnoteToast(Context pContext, int show) {
    DefaultPreferences.getInstance(pContext).setInt(SHOW_FOOTNOTE_TOAST, show);
  }

  public static int getShowFootnoteToast(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(SHOW_FOOTNOTE_TOAST, FootnoteToastEnum.footnotesAndSuperscripts.ordinal());
  }

  public static void setTapZoneAction(Context pContext, String ac, int x, int y, String action) {
    DefaultPreferences.getInstance(pContext)
        .setString(TAP_ZONE_ACTION + ac + "_" + x + "_" + y, action);
  }

  public static String getTapZoneAction(Context pContext, String action, int x, int y, String def) {
    return DefaultPreferences.getInstance(pContext)
        .getString(TAP_ZONE_ACTION + action + "_" + x + "_" + y, def);
  }

  public static void setTapZoneWidth(Context pContext, String name, int width) {
    DefaultPreferences.getInstance(pContext).setInt(TAP_ZONE_WIDTH + name, width);
  }

  public static int getTapZoneWidth(Context pContext, String name, int def) {
    return DefaultPreferences.getInstance(pContext).getInt(TAP_ZONE_WIDTH + name, def);
  }

  public static void setTapZoneHeight(Context pContext, String name, int height) {
    DefaultPreferences.getInstance(pContext).setInt(TAP_ZONE_HEIGHT + name, height);
  }

  public static int getTapZoneHeight(Context pContext, String name, int height) {
    return DefaultPreferences.getInstance(pContext).getInt(TAP_ZONE_HEIGHT + name, height);
  }

  public static void setTapZoneMapList(Context pContext, Set<String> list) {
    DefaultPreferences.getInstance(pContext).setStringSet(TAP_ZONE_MAP_LIST, list);
  }

  public static Set<String> getTapZoneMapList(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getStringSet(TAP_ZONE_MAP_LIST);
  }

  public static void setKeyBindings(Context pContext, String action, String key, String actionId) {
    DefaultPreferences.getInstance(pContext).setString(KEYS + action + "_" + key, actionId);
  }

  public static String getKeyBindings(Context pContext, String action, String key, String def) {
    return DefaultPreferences.getInstance(pContext)
        .getString(KEYS + action + "_" + key, def);
  }

  public static void setKeyList(Context pContext, String name, Set<String> list) {
    DefaultPreferences.getInstance(pContext).setStringSet(KEY_LIST + name, list);
  }

  public static Set<String> getKeyList(Context pContext, String name) {
    return DefaultPreferences.getInstance(pContext).getStringSet(KEY_LIST + name);
  }
}
