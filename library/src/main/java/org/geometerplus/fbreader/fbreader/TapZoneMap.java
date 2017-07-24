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

package org.geometerplus.fbreader.fbreader;

import android.content.Context;

import org.geometerplus.android.fbreader.config.MiscPreferences;
import org.geometerplus.zlibrary.core.util.XmlUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.Map;

public class TapZoneMap {
	public static final String TURN_PAGE_BACK = "previousPage";
	public static final String TURN_PAGE_FORWARD = "nextPage";

	private static final Map<String,TapZoneMap> ourMaps = new HashMap<>();

	public static TapZoneMap zoneMap(Context pContext, String name) {
		TapZoneMap map = ourMaps.get(name);
		if (map == null) {
			map = new TapZoneMap(pContext, name);
			ourMaps.put(name, map);
		}
		return map;
	}

	public enum Tap {
		singleTap,
		singleNotDoubleTap,
		doubleTap
	}

	private Context mContext;
  public final String Name;
	private int myHeight;
	private int myWidth;
	private final HashMap<Zone, String> myZoneMap = new HashMap<>();
	private final HashMap<Zone, String> myZoneMap2 = new HashMap<>();

	private TapZoneMap(Context pContext, String name) {
		mContext = pContext;
		Name = name;
		myHeight = MiscPreferences.getTapZoneHeight(pContext, name, 3);
		myWidth = MiscPreferences.getTapZoneWidth(pContext, name, 3);
		XmlUtil.parseQuietly(pContext, "default/" + name.toLowerCase() + ".xml", new Reader());
	}

	public int getHeight() {
		return myHeight;
	}

	public int getWidth() {
		return myWidth;
	}

	public String getActionByCoordinates(int x, int y, int width, int height, Tap tap) {
		if (width == 0 || height == 0) {
			return null;
		}
		x = Math.max(0, Math.min(width - 1, x));
		y = Math.max(0, Math.min(height - 1, y));
		return getActionByZone(myWidth * x / width, myHeight * y / height, tap);
	}

	public String getActionByZone(int h, int v, Tap tap) {
		return getOptionByZone(new Zone(h, v), tap);
	}

	private String getOptionByZone(Zone zone, Tap tap) {
		switch (tap) {
			default:
				return null;
			case singleTap:
				final String option = myZoneMap.get(zone);
				return option != null ? option : myZoneMap2.get(zone);
			case singleNotDoubleTap:
				return myZoneMap.get(zone);
			case doubleTap:
				return myZoneMap2.get(zone);
		}
	}

	private String createOptionForZone(Zone zone, boolean singleTap, String action) {
		return MiscPreferences.getTapZoneAction(mContext, (singleTap ? "Action" : "Action2"),
				zone.HIndex, zone.VIndex, action);
	}

	private static class Zone {
		int HIndex;
		int VIndex;

		Zone(int h, int v) {
			HIndex = h;
			VIndex = v;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (!(o instanceof Zone)) {
				return false;
			}

			final Zone tz = (Zone)o;
			return HIndex == tz.HIndex && VIndex == tz.VIndex;
		}

		@Override
		public int hashCode() {
			return (HIndex << 5) + VIndex;
		}
	}

	private class Reader extends DefaultHandler {
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			try {
				if ("zone".equals(localName)) {
					final Zone zone = new Zone(
						Integer.parseInt(attributes.getValue("x")),
						Integer.parseInt(attributes.getValue("y"))
					);
					final String action = attributes.getValue("action");
					final String action2 = attributes.getValue("action2");
					if (action != null) {
						myZoneMap.put(zone, createOptionForZone(zone, true, action));
					}
					if (action2 != null) {
						myZoneMap2.put(zone, createOptionForZone(zone, false, action2));
					}
				} else if ("tapZones".equals(localName)) {
					final String v = attributes.getValue("v");
					if (v != null) {
						myHeight = Integer.parseInt(v);
						MiscPreferences.setTapZoneHeight(mContext, Name, myHeight);
					}
					final String h = attributes.getValue("h");
					if (h != null) {
						myWidth = Integer.parseInt(h);
						MiscPreferences.setTapZoneWidth(mContext, Name, myWidth);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}
