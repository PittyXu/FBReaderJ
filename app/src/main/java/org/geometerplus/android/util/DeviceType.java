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

package org.geometerplus.android.util;

import android.os.Build;

public enum DeviceType {
	GENERIC,
	YOTA_PHONE,
	EKEN_M001,
	PAN_DIGITAL,
	SAMSUNG_GT_S5830;

	private static DeviceType ourInstance;
	public static DeviceType Instance() {
		if (ourInstance == null) {
			if ("YotaPhone".equals(Build.BRAND)) {
				ourInstance = YOTA_PHONE;
			} else if ("GT-S5830".equals(Build.MODEL)) {
				ourInstance = SAMSUNG_GT_S5830;
			} else if (Build.DISPLAY != null && Build.DISPLAY.contains("simenxie")) {
				ourInstance = EKEN_M001;
			} else if ("PD_Novel".equals(Build.MODEL)) {
				ourInstance = PAN_DIGITAL;
			} else {
				ourInstance = GENERIC;
			}
		}
		return ourInstance;
	}

	public boolean hasNoHardwareMenuButton() {
		return this == EKEN_M001 || this == PAN_DIGITAL;
	}

	public boolean hasButtonLightsBug() {
		return this == SAMSUNG_GT_S5830;
	}
}
