package org.geometerplus.android.fbreader.config;

import android.content.Context;

import org.geometerplus.android.util.DefaultPreferences;
import org.geometerplus.zlibrary.core.view.ZLView;

public class PageTurningPreferences {

  public enum FingerScrollingType {
    byTap, byFlick, byTapAndFlick
  }

  private static final String FINGER_SCROLLING = "page_turning_finger_scrolling";
  private static final String ANIMATION = "page_turning_animation";
  private static final String ANIMATION_SPEED = "page_turning_animation_speed";
  private static final String HORIZONTAL = "page_turning_horizontal";
  private static final String TAP_ZONE_MAP = "page_turning_tap_zone_map";

  public static void setFingerScrolling(Context pContext, int finger) {
    DefaultPreferences.getInstance(pContext).setInt(FINGER_SCROLLING, finger);
  }

  public static int getFingerScrolling(Context pContext) {
    return DefaultPreferences.getInstance(pContext)
        .getInt(FINGER_SCROLLING, FingerScrollingType.byTapAndFlick.ordinal());
  }

  public static void setAnimation(Context pContext, int animation) {
    DefaultPreferences.getInstance(pContext).setInt(ANIMATION, animation);
  }

  public static int getAnimation(Context pContext) {
    return DefaultPreferences.getInstance(pContext)
        .getInt(ANIMATION, ZLView.Animation.slide.ordinal());
  }

  public static void setAnimationSpeed(Context pContext, int speed) {
    DefaultPreferences.getInstance(pContext).setInt(ANIMATION_SPEED, speed);
  }

  public static int getAnimationSpeed(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(ANIMATION_SPEED, 7);
  }

  public static void setHorizontal(Context pContext, boolean horizontal) {
    DefaultPreferences.getInstance(pContext).setBoolean(HORIZONTAL, horizontal);
  }

  public static boolean getHorizontal(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(HORIZONTAL, true);
  }

  public static void setTapZoneMap(Context pContext, String map) {
    DefaultPreferences.getInstance(pContext).setString(TAP_ZONE_MAP, map);
  }

  public static String getTapZoneMap(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(TAP_ZONE_MAP, "");
  }
}
