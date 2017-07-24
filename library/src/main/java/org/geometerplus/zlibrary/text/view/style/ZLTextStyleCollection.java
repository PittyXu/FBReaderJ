/*
 * Copyright (C) 2007-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.zlibrary.text.view.style;

import android.content.Context;

import org.geometerplus.android.fbreader.config.StylePreferences;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.util.XmlUtil;
import org.geometerplus.zlibrary.text.model.ZLTextAlignmentType;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ZLTextStyleCollection {

  private static final String BASE = "base";
  private static final String SCREEN = "screen";
  private static final String FONT_FAMILY = "family";
  private static final String FONT_SIZE = "fontSize";

  public final String screen;
  private ZLTextBaseStyle myBaseStyle;
  private final List<ZLTextNGStyleDescription> myDescriptionList;
  private final ZLTextNGStyleDescription[] myDescriptionMap = new ZLTextNGStyleDescription[256];
  private Context mContext;
  private FBReaderApp mApp;

  public ZLTextStyleCollection(FBReaderApp pApp, Context pContext, String screen) {
    mApp = pApp;
    mContext = pContext;
    this.screen = screen;
    final Map<Integer, ZLTextNGStyleDescription> descriptions = new SimpleCSSReader().read(pContext, "default/styles.css");
    myDescriptionList = Collections.unmodifiableList(new ArrayList<>(descriptions.values()));
    for (Map.Entry<Integer, ZLTextNGStyleDescription> entry : descriptions.entrySet()) {
      myDescriptionMap[entry.getKey() & 0xFF] = entry.getValue();
    }
    XmlUtil.parseQuietly(pContext, "default/styles.xml", new TextStyleReader());
  }

  public ZLTextBaseStyle getBaseStyle() {
    return myBaseStyle;
  }

  public List<ZLTextNGStyleDescription> getDescriptionList() {
    return myDescriptionList;
  }

  public ZLTextNGStyleDescription getDescription(byte kind) {
    return myDescriptionMap[kind & 0xFF];
  }

  private class TextStyleReader extends DefaultHandler {

    private int intValue(Attributes attributes, String name, int defaultValue) {
      final String value = attributes.getValue(name);
      if (value != null) {
        try {
          return Integer.parseInt(value);
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }
      return defaultValue;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if (BASE.equals(localName) && screen.equals(attributes.getValue(SCREEN))) {
        myBaseStyle = StylePreferences.getStyle(mContext, screen);
        String fontFamily = attributes.getValue(FONT_FAMILY);
        int fontSize = intValue(attributes, FONT_SIZE, 0);
        if (null == myBaseStyle.fontFamily) {
          myBaseStyle.fontFamily = fontFamily;
        }
        if (null == myBaseStyle.fontSize) {
          fontSize = fontSize * mApp.getDisplayDPI() / 160;
          myBaseStyle.fontSize = fontSize;
        }
        if (null == myBaseStyle.lineSpacing) {
          myBaseStyle.lineSpacing = 12;
        }
        if (myBaseStyle.alignment <= 0) {
          myBaseStyle.alignment = (int) ZLTextAlignmentType.ALIGN_JUSTIFY;
        }
      }
    }
  }
}
