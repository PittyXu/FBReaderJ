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

package org.geometerplus.zlibrary.core.options;

import android.support.annotation.ColorInt;

public final class ZLColorOption extends ZLOption {
	private Integer myValue;
	private String myStringValue;

	private static String stringColorValue(@ColorInt Integer color) {
		return String.valueOf(color != null ? color : -1);
	}

	public ZLColorOption(String group, String optionName, @ColorInt Integer defaultValue) {
		super(group, optionName, stringColorValue(defaultValue));
	}

	public Integer getValue() {
		final String stringValue = getConfigValue();
		if (!stringValue.equals(myStringValue)) {
			myStringValue = stringValue;
			try {
				final int intValue = Integer.parseInt(stringValue);
				myValue = intValue != -1 ? intValue : null;
			} catch (NumberFormatException e) {
			}
		}
		return myValue;
	}

	public void setValue(@ColorInt Integer value) {
		if (value == null) {
			return;
		}
		myValue = value;
		myStringValue = stringColorValue(value);
		setConfigValue(myStringValue);
	}
}
