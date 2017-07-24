package org.geometerplus.android.fbreader.config;

import android.graphics.Color;

import com.alibaba.fastjson.annotation.JSONField;

import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public class ColorProfile {

  public static final String DAY = "defaultLight";
  public static final String NIGHT = "defaultDark";

  @JSONField(name = "name")
  public String name;
  @JSONField(name = "wallpaper")
  public String wallpaper;
  @JSONField(name = "fillMode")
  public int fillMode;
  @JSONField(name = "backgroundColor")
  public Integer backgroundColor;
  @JSONField(name = "selectionBackgroundColor")
  public Integer selectionBackgroundColor;
  @JSONField(name = "selectionForegroundColor")
  public Integer selectionForegroundColor;
  @JSONField(name = "highlightingBackgroundColor")
  public Integer highlightingBackgroundColor;
  @JSONField(name = "highlightingForegroundColor")
  public Integer highlightingForegroundColor;
  @JSONField(name = "regularTextColor")
  public Integer regularTextColor;
  @JSONField(name = "hyperlinkTextColor")
  public Integer hyperlinkTextColor;
  @JSONField(name = "visitedHyperlinkTextColor")
  public Integer visitedHyperlinkTextColor;
  @JSONField(name = "footerFillColor")
  public Integer footerFillColor;
  @JSONField(name = "footerNGBackgroundColor")
  public Integer footerNGBackgroundColor;
  @JSONField(name = "footerNGForegroundColor")
  public Integer footerNGForegroundColor;
  @JSONField(name = "footerNGForegroundUnreadColor")
  public Integer footerNGForegroundUnreadColor;

  public ColorProfile() {
    this(DAY);
  }

  public ColorProfile(String name) {
    this.name = name;
    if (NIGHT.equals(name)) {
      wallpaper = "";
      fillMode = ZLPaintContext.FillMode.tile.ordinal();
      backgroundColor = Color.rgb(0, 0, 0);
      selectionBackgroundColor = Color.rgb(82, 131, 194);
      selectionForegroundColor = null;
      highlightingBackgroundColor = Color.rgb(96, 96, 128);
      highlightingForegroundColor = null;
      regularTextColor = Color.rgb(192, 192, 192);
      hyperlinkTextColor = Color.rgb(60, 142, 224);
      visitedHyperlinkTextColor = Color.rgb(200, 139, 255);
      footerFillColor = Color.rgb(85, 85, 85);
      footerNGBackgroundColor = Color.rgb(68, 68, 68);
      footerNGForegroundColor = Color.rgb(187, 187, 187);
      footerNGForegroundUnreadColor = Color.rgb(119, 119, 119);
    } else {
      wallpaper = "wallpapers/sepia.jpg";
      fillMode = ZLPaintContext.FillMode.tile.ordinal();
      backgroundColor = Color.rgb(255, 255, 255);
      selectionBackgroundColor = Color.rgb(82, 131, 194);
      selectionForegroundColor = null;
      highlightingBackgroundColor = Color.rgb(255, 192, 128);
      highlightingForegroundColor = null;
      regularTextColor = Color.rgb(0, 0, 0);
      hyperlinkTextColor = Color.rgb(60, 139, 255);
      visitedHyperlinkTextColor = Color.rgb(200, 139, 255);
      footerFillColor = Color.rgb(170, 170, 170);
      footerNGBackgroundColor = Color.rgb(68, 68, 68);
      footerNGForegroundColor = Color.rgb(187, 187, 187);
      footerNGForegroundUnreadColor = Color.rgb(119, 119, 119);
    }
  }
}
