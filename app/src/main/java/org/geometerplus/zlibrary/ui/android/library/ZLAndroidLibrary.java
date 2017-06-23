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

package org.geometerplus.zlibrary.ui.android.library;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

public final class ZLAndroidLibrary extends ZLibrary {
	public final ZLBooleanOption EnableFullscreenModeOption = new ZLBooleanOption("LookNFeel", "FullscreenMode", true);
	{
		EnableFullscreenModeOption.setSpecialName("enableFullscreen");
	}

	private final Application myApplication;

	ZLAndroidLibrary(Application application) {
		myApplication = application;
	}

	public AssetManager getAssets() {
		return myApplication.getAssets();
	}

	@Override
	public String getVersionName() {
		try {
			final PackageInfo info =
				myApplication.getPackageManager().getPackageInfo(myApplication.getPackageName(), 0);
			return info.versionName;
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public String getFullVersionName() {
		try {
			final PackageInfo info =
				myApplication.getPackageManager().getPackageInfo(myApplication.getPackageName(), 0);
			return info.versionName + " (" + info.versionCode + ")";
		} catch (Exception e) {
			return "";
		}
	}

	@Override
	public String getCurrentTimeString() {
		return DateFormat.getTimeFormat(myApplication.getApplicationContext()).format(new Date());
	}

	private DisplayMetrics myMetrics;
	private DisplayMetrics getMetrics() {
		if (myMetrics == null) {
			myMetrics = myApplication.getApplicationContext().getResources().getDisplayMetrics();
		}
		return myMetrics;
	}

	@Override
	public int getDisplayDPI() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : (int)(160 * metrics.density);
	}

	@Override
	public int getWidthInPixels() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : metrics.widthPixels;
	}

	@Override
	public int getHeightInPixels() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : metrics.heightPixels;
	}

	@Override
	public List<String> defaultLanguageCodes() {
		final TreeSet<String> set = new TreeSet<String>();
		set.add(Locale.getDefault().getLanguage());
		final TelephonyManager manager = (TelephonyManager)myApplication.getSystemService(Context.TELEPHONY_SERVICE);
		if (manager != null) {
			String country0 = manager.getSimCountryIso();
			if (country0 != null) {
				country0 = country0.toLowerCase();
			}
			String country1 = manager.getNetworkCountryIso();
			if (country1 != null) {
				country1 = country1.toLowerCase();
			}
			for (Locale locale : Locale.getAvailableLocales()) {
				final String country = locale.getCountry().toLowerCase();
				if (country != null && country.length() > 0 &&
					(country.equals(country0) || country.equals(country1))) {
					set.add(locale.getLanguage());
				}
			}
			if ("ru".equals(country0) || "ru".equals(country1)) {
				set.add("ru");
			} else if ("by".equals(country0) || "by".equals(country1)) {
				set.add("ru");
			} else if ("ua".equals(country0) || "ua".equals(country1)) {
				set.add("ru");
			}
		}
		set.add("multi");
		return new ArrayList<String>(set);
	}

	@Override
	public boolean supportsAllOrientations() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	}
}
