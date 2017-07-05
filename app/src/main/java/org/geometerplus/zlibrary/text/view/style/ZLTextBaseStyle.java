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

import com.alibaba.fastjson.annotation.JSONField;

import org.geometerplus.android.fbreader.config.StylePreferences;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.text.model.ZLTextMetrics;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextStyle;

import java.util.Collections;
import java.util.List;

public class ZLTextBaseStyle extends ZLTextStyle {
	@JSONField(name = "useCssTextAlignment")
	public boolean useCssTextAlignment = true;
	@JSONField(name = "useCssMargins")
	public boolean useCssMargins = true;
	@JSONField(name = "useCssFontSize")
	public boolean useCssFontSize = true;
	@JSONField(name = "useCssFontFamily")
	public boolean useCssFontFamily = true;
	@JSONField(name = "autoHyphenation")
	public boolean autoHyphenation = true;

	@JSONField(name = "name")
	public String name;
	@JSONField(name = "bold")
	public boolean bold = false;
	@JSONField(name = "italic")
	public boolean italic = false;
	@JSONField(name = "underline")
	public boolean underline = false;
	@JSONField(name = "strikeThrough")
	public boolean strikeThrough = false;
	@JSONField(name = "alignment")
	public int alignment;
	@JSONField(name = "lineSpacing")
	public Integer lineSpacing;

	@JSONField(name = "fontFamily")
	public String fontFamily;
	@JSONField(name = "fontSize")
	public Integer fontSize;

	@JSONField(serialize = false, deserialize = false)
	private List<FontEntry> myFontEntries;

	public ZLTextBaseStyle(){
		this("Base");
	}

	public ZLTextBaseStyle(String prefix) {
		super(null, ZLTextHyperlink.NO_LINK);
		name = prefix;
	}

	@JSONField(serialize = false, deserialize = false)
	@Override
	public List<FontEntry> getFontEntries() {
		final String family = fontFamily;
		if (myFontEntries == null) {
			myFontEntries = Collections.singletonList(FontEntry.systemEntry(family));
		}
		return myFontEntries;
	}

	public void changeFontSize(Context pContext, int delta) {
		fontSize = fontSize + delta;
		StylePreferences.setStyle(pContext, this);
	}

	public int getFontSize() {
		return fontSize;
	}

	@Override
	public int getFontSize(ZLTextMetrics metrics) {
		return getFontSize();
	}

	@Override
	public boolean isBold() {
		return bold;
	}

	@Override
	public boolean isItalic() {
		return italic;
	}

	@Override
	public boolean isUnderline() {
		return underline;
	}

	@Override
	public boolean isStrikeThrough() {
		return strikeThrough;
	}

	@Override
	public int getLeftMargin(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getRightMargin(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getLeftPadding(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getRightPadding(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getFirstLineIndent(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getLineSpacePercent() {
		return lineSpacing * 10;
	}

	@Override
	public int getVerticalAlign(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public boolean isVerticallyAligned() {
		return false;
	}

	@Override
	public int getSpaceBefore(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public int getSpaceAfter(ZLTextMetrics metrics) {
		return 0;
	}

	@Override
	public byte getAlignment() {
		return (byte)alignment;
	}

	@Override
	public boolean allowHyphenations() {
		return true;
	}
}
