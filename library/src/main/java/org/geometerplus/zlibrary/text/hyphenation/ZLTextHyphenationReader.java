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

package org.geometerplus.zlibrary.text.hyphenation;

import android.content.Context;
import android.text.TextUtils;

import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

final class ZLTextHyphenationReader {

  private static final String PATTERN = "pattern";

  private final ZLTextTeXHyphenator myHyphenator;

  ZLTextHyphenationReader(ZLTextTeXHyphenator hyphenator) {
    myHyphenator = hyphenator;
  }

  public boolean readQuietly(Context pContext, String filePath) {
    try {
      XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
      parser.setInput(pContext.getAssets().open(filePath), "utf-8");

      int eventCode = parser.getEventType();//获取事件类型
      // 直到文档的结尾处
      while (eventCode != XmlPullParser.END_DOCUMENT) {
        switch (eventCode) {
          case XmlPullParser.START_DOCUMENT: //开始读取XML文档
            //实例化集合类
            break;
          case XmlPullParser.START_TAG://开始读取某个标签
            if (PATTERN.equals(parser.getName())) {
              //通过getName判断读到哪个标签，然后通过nextText()获取文本节点值，或通过getAttributeValue(i)获取属性节点值
              String text = parser.nextText();
              if (!TextUtils.isEmpty(text)) {
                myHyphenator.addPattern(
                    new ZLTextTeXHyphenationPattern(text.toCharArray(), 0, text.length(), true));
              }
            }
            break;
        }
        eventCode = parser.next();
      }
      return true;
    } catch (Exception pE) {
      pE.printStackTrace();
      return false;
    }
  }
}
