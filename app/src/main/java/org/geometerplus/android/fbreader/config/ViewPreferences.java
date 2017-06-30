package org.geometerplus.android.fbreader.config;

import android.content.Context;

import org.geometerplus.android.util.DefaultPreferences;

public class ViewPreferences {
  enum ProgressDisplayType {
    dontDisplay,
    asPages,
    asPercentage,
    asPagesAndPercentage
  }

  private static final String TWO_COLUMN_VIEW = "view_two_column_view";
  private static final String LEFT_MARGIN = "view_left_margin";
  private static final String RIGHT_MARGIN = "view_right_margin";
  private static final String TOP_MARGIN = "view_top_margin";
  private static final String BOTTOM_MARGIN = "view_bottom_margin";
  private static final String SPACE_BETWEEN_COLUMNS = "view_space_between_columns";
  private static final String COLOR_PROFILE_NAME = "view_color_profile";
  private static final String COLOR_PROFILE = "view_color_profile_";
  private static final String FOOTER_HEIGHT = "view_footer_height";
  private static final String FOOTER_SHOW_TOC_MARKS = "view_footer_show_toc_marks";
  private static final String FOOTER_MAX_TOC_MARKS = "view_footer_max_toc_marks";
  private static final String FOOTER_DISPLAY_PROGRESS = "view_footer_display_progress";
  private static final String FOOTER_FONT = "view_footer_font";
  private static final String FONT_ANTI_ALIAS = "font_anti_alias";
  private static final String FONT_DEVICE_KERNING = "font_device_kerning";
  private static final String FONT_DITHERING = "font_dithering";
  private static final String FONT_SUBPIXEL = "font_subpixel";

  public static void setTwoColumnView(Context pContext, boolean column) {
    DefaultPreferences.getInstance(pContext).setBoolean(TWO_COLUMN_VIEW, column);
  }

  public static boolean getTwoColumnView(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(TWO_COLUMN_VIEW);
  }

  public static void setLeftMargin(Context pContext, int margin) {
    DefaultPreferences.getInstance(pContext).setInt(LEFT_MARGIN, margin);
  }

  public static int getLeftMargin(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(LEFT_MARGIN, 10);
  }

  public static void setRightMargin(Context pContext, int margin) {
    DefaultPreferences.getInstance(pContext).setInt(RIGHT_MARGIN, margin);
  }

  public static int getRightMargin(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(RIGHT_MARGIN, 10);
  }

  public static void setTopMargin(Context pContext, int margin) {
    DefaultPreferences.getInstance(pContext).setInt(TOP_MARGIN, margin);
  }

  public static int getTopMargin(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(TOP_MARGIN, 10);
  }

  public static void setBottomMargin(Context pContext, int margin) {
    DefaultPreferences.getInstance(pContext).setInt(BOTTOM_MARGIN, margin);
  }

  public static int getBottomMargin(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(BOTTOM_MARGIN, 10);
  }

  public static void setSpaceBetweenColumns(Context pContext, int margin) {
    DefaultPreferences.getInstance(pContext).setInt(SPACE_BETWEEN_COLUMNS, margin);
  }

  public static int getSpaceBetweenColumns(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(SPACE_BETWEEN_COLUMNS, 30);
  }

  public static void setColorProfileName(Context pContext, String profile) {
    DefaultPreferences.getInstance(pContext).setString(COLOR_PROFILE_NAME, profile);
  }

  public static String getColorProfileName(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(COLOR_PROFILE_NAME, ColorProfile.DAY);
  }

  public static void setColorProfile(Context pContext, ColorProfile profile) {
    DefaultPreferences.getInstance(pContext).setObject(COLOR_PROFILE + profile.name, profile);
  }

  public static ColorProfile getColorProfile(Context pContext, String name) {
    return DefaultPreferences.getInstance(pContext).getObject(COLOR_PROFILE + name, ColorProfile.class);
  }

  public static ColorProfile getColorProfile(Context pContext) {
    String name = getColorProfileName(pContext);
    ColorProfile colorProfile = getColorProfile(pContext, name);
    if (null == colorProfile) {
      colorProfile = new ColorProfile(name);
    }
    return colorProfile;
  }

  public static int getFooterHeight(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(FOOTER_HEIGHT, 30);
  }

  public static void setFooterHeight(Context pContext, int height) {
    DefaultPreferences.getInstance(pContext).setInt(FOOTER_HEIGHT, height);
  }

  public static boolean getFooterShowTOCMarks(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(FOOTER_SHOW_TOC_MARKS, true);
  }

  public static void setFooterShowTOCMarks(Context pContext, boolean show) {
    DefaultPreferences.getInstance(pContext).setBoolean(FOOTER_SHOW_TOC_MARKS, show);
  }

  public static Integer getFooterMaxTOCMarks(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(FOOTER_MAX_TOC_MARKS, 100);
  }

  public static void setFooterMaxTOCMarks(Context pContext, Integer max) {
    DefaultPreferences.getInstance(pContext).setInt(FOOTER_MAX_TOC_MARKS, max);
  }

  public static Integer getFooterDisplayProgress(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getInt(FOOTER_DISPLAY_PROGRESS, ProgressDisplayType.asPages.ordinal());
  }

  public static void setFooterDisplayProgress(Context pContext, Integer progress) {
    DefaultPreferences.getInstance(pContext).setInt(FOOTER_DISPLAY_PROGRESS, progress);
  }

	public static boolean showFooterProgressAsPercentage(Context pContext) {
    int show = getFooterDisplayProgress(pContext);
    return show == ProgressDisplayType.asPercentage.ordinal()
        || show == ProgressDisplayType.asPagesAndPercentage.ordinal();
  }

	public static boolean showFooterProgressAsPages(Context pContext) {
    int show = getFooterDisplayProgress(pContext);
    return show == ProgressDisplayType.asPages.ordinal()
        || show == ProgressDisplayType.asPagesAndPercentage.ordinal();
	}

  public static void setFooterFont(Context pContext, String font) {
    DefaultPreferences.getInstance(pContext).setString(FOOTER_FONT, font);
  }

  public static String getFooterFont(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(FOOTER_FONT, "Droid Sans");
  }

  public static void setFontAntiAlias(Context pContext, boolean antiAlias) {
    DefaultPreferences.getInstance(pContext).setBoolean(FONT_ANTI_ALIAS, antiAlias);
  }

  public static boolean getFontAntiAlias(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(FONT_ANTI_ALIAS, true);
  }

  public static void setFontDeviceKerning(Context pContext, boolean b) {
    DefaultPreferences.getInstance(pContext).setBoolean(FONT_DEVICE_KERNING, b);
  }

  public static boolean getFontDeviceKerning(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(FONT_DEVICE_KERNING);
  }

  public static void setFontDithering(Context pContext, boolean dithering) {
    DefaultPreferences.getInstance(pContext).setBoolean(FONT_DITHERING, dithering);
  }

  public static boolean getFontDithering(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(FONT_DITHERING);
  }

  public static void setFontSubpixel(Context pContext, boolean subpixel) {
    DefaultPreferences.getInstance(pContext).setBoolean(FONT_SUBPIXEL, subpixel);
  }

  public static boolean getFontSubpixel(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getBoolean(FONT_SUBPIXEL);
  }

}
