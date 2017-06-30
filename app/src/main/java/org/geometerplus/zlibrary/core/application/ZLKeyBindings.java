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

package org.geometerplus.zlibrary.core.application;

import android.content.Context;

import org.geometerplus.android.fbreader.config.MiscPreferences;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.util.XmlUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class ZLKeyBindings {
	private static final String ACTION = "Action";
	private static final String LONG_PRESS_ACTION = "LongPressAction";

	private final String myName;
	private List<String> myKeysOption;
	private final TreeMap<Integer, String> myActionMap = new TreeMap<>();
	private final TreeMap<Integer, String> myLongPressActionMap = new TreeMap<>();
	private Context mContext;

	public ZLKeyBindings(Context pContext) {
		this(pContext, "Keys");
	}

	private ZLKeyBindings(Context pContext, String name) {
		mContext = pContext;
		myName = name;
		new Initializer();
	}

	private class Initializer implements Runnable {
		public void run() {
			final Set<String> keys = new TreeSet<>();

			final String keymapFilename = "keymap.xml";
			new Reader(keys).readQuietly(mContext, "default/" + keymapFilename);
			new Reader(keys).readQuietly(mContext, Paths.systemShareDirectory() + "/keymap.xml");
			new Reader(keys).readQuietly(mContext, Paths.bookPath(mContext).get(0) + "/keymap.xml");

			Set<String> list = MiscPreferences.getKeyList(mContext, myName);
			if (null == list) {
				myKeysOption = new ArrayList<>(keys);
			} else {
				myKeysOption = new ArrayList<>(list);
			}
		}
	}

	private String createOption(int key, boolean longPress, String defaultValue) {
		return MiscPreferences.getKeyBindings(mContext, (longPress ? LONG_PRESS_ACTION : ACTION), String.valueOf(key), defaultValue);
	}

	public String getOption(int key, boolean longPress) {
		final TreeMap<Integer, String> map = longPress ? myLongPressActionMap : myActionMap;
		String option = map.get(key);
		if (option == null) {
			option = createOption(key, longPress, ZLApplication.NoAction);
			map.put(key, option);
		}
		return option;
	}

	public void bindKey(int key, boolean longPress, String actionId) {
		if (myKeysOption == null) {
			return;
		}
		final String stringKey = String.valueOf(key);
		List<String> keys = myKeysOption;
		if (!keys.contains(stringKey)) {
			keys = new ArrayList<>(keys);
			keys.add(stringKey);
			Collections.sort(keys);
			MiscPreferences.setKeyList(mContext, myName, new HashSet<>(keys));
		}
		MiscPreferences.setKeyBindings(mContext, (longPress ? LONG_PRESS_ACTION : ACTION), String.valueOf(key), actionId);
	}

	public String getBinding(int key, boolean longPress) {
		return getOption(key, longPress);
	}

	public boolean hasBinding(int key, boolean longPress) {
		return !ZLApplication.NoAction.equals(getBinding(key, longPress));
	}

	private class Reader extends DefaultHandler {
		private final Set<String> myKeySet;

		Reader(Set<String> keySet) {
			myKeySet = keySet;
		}

		public void readQuietly(Context pContext, String path) {
			XmlUtil.parseQuietly(pContext, path, this);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if ("binding".equals(localName)) {
				final String stringKey = attributes.getValue("key");
				final String actionId = attributes.getValue("action");
				if (stringKey != null && actionId != null) {
					try {
						final int key = Integer.parseInt(stringKey);
						myKeySet.add(stringKey);
						myActionMap.put(key, createOption(key, false, actionId));
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}
}
