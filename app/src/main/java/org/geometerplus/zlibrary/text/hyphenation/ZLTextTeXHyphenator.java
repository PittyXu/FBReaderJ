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

import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;

import java.util.HashMap;

final class ZLTextTeXHyphenator extends ZLTextHyphenator {
	private final HashMap<ZLTextTeXHyphenationPattern,ZLTextTeXHyphenationPattern> myPatternTable =
		new HashMap<ZLTextTeXHyphenationPattern,ZLTextTeXHyphenationPattern>();
	private int myMaxPatternLength;
	private String myLanguage;

	void addPattern(ZLTextTeXHyphenationPattern pattern) {
		myPatternTable.put(pattern, pattern);
		if (myMaxPatternLength < pattern.length()) {
			myMaxPatternLength = pattern.length();
		}
	}

	public void load(Context pContext, String language) {
		if (language == null || Language.OTHER_CODE.equals(language)) {
			language = ZLLanguageUtil.defaultLanguageCode();
		}
		if (language == null || language.equals(myLanguage)) {
			return;
		}
		myLanguage = language;
		unload();

		new ZLTextHyphenationReader(this).readQuietly(pContext, "hyphenationPatterns/" + language + ".pattern");
	}

	public void unload() {
		myPatternTable.clear();
		myMaxPatternLength = 0;
	}

	public void hyphenate(char[] stringToHyphenate, boolean[] mask, int length) {
		if (myPatternTable.isEmpty()) {
			for (int i = 0; i < length - 1; i++) {
				mask[i] = false;
			}
			return;
		}

		byte[] values = new byte[length + 1];

		final HashMap<ZLTextTeXHyphenationPattern,ZLTextTeXHyphenationPattern> table = myPatternTable;
		ZLTextTeXHyphenationPattern pattern =
			new ZLTextTeXHyphenationPattern(stringToHyphenate, 0, length, false);
		for (int offset = 0; offset < length - 1; offset++) {
			int len = Math.min(length - offset, myMaxPatternLength) + 1;
			pattern.update(stringToHyphenate, offset, len - 1);
			while (--len > 0) {
				pattern.reset(len);
				ZLTextTeXHyphenationPattern toApply =
            table.get(pattern);
				if (toApply != null) {
					toApply.apply(values, offset);
				}
			}
		}

		for (int i = 0; i < length - 1; i++) {
			mask[i] = (values[i + 1] % 2) == 1;
		}
	}
}
