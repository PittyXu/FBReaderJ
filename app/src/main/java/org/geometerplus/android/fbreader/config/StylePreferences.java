package org.geometerplus.android.fbreader.config;

import android.content.Context;

import org.geometerplus.android.util.DefaultPreferences;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextNGStyleDescription;

public class StylePreferences {

  private static final String STYLE_NAME = "style_name";
  private static final String STYLE = "style_";
  private static final String STYLE_DESCRIPTION = "style_description_";
  private static final String TEXT_FONT_FAMILY = "style_text_font_family";
  private final static String DEFAULT_STYLE_ID_KEY = "style_default_highlighting_id";


  public static void setFontFamily(Context pContext, String font) {
    DefaultPreferences.getInstance(pContext).setString(TEXT_FONT_FAMILY, font);
  }

  public static String getFontFamily(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(TEXT_FONT_FAMILY, "Droid Sans");
  }

  public static void setStyleName(Context pContext, String name) {
    DefaultPreferences.getInstance(pContext).setString(STYLE_NAME, name);
  }

  public static String getStyleName(Context pContext) {
    return DefaultPreferences.getInstance(pContext).getString(STYLE_NAME, "Base");
  }

  public static void setStyle(Context pContext, ZLTextBaseStyle style) {
    DefaultPreferences.getInstance(pContext).setObject(STYLE + style.name, style);
  }

  public static ZLTextBaseStyle getStyle(Context pContext, String name) {
    ZLTextBaseStyle style = DefaultPreferences.getInstance(pContext).getObject(STYLE + name, ZLTextBaseStyle.class);
    if (null == style) {
      style = new ZLTextBaseStyle();
    }
    return style;
  }

  public static void setDefaultHighlightingStyle(Context pContext, int style) {
    DefaultPreferences.getInstance(pContext).setInt(DEFAULT_STYLE_ID_KEY, style);
  }

  public static int getDefaultHighlightingStyle(Context pContext) {
     return DefaultPreferences.getInstance(pContext).getInt(DEFAULT_STYLE_ID_KEY, 1);
  }
}
