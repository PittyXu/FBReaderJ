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

package org.geometerplus.zlibrary.core.util;

import org.geometerplus.fbreader.util.ComparisonUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class MimeType {
	private static Map<String,MimeType> ourSimpleTypesMap = new HashMap<String,MimeType>();

	public static MimeType get(String text) {
		if (text == null) {
			return NULL;
		}

		final String[] items = text.split(";");
		if (items.length == 0) {
			return NULL;
		}

		final String name = items[0].intern();
		Map<String,String> parameters = null;
		for (int i = 1; i < items.length; ++i) {
			final String[] pair = items[i].split("=");
			if (pair.length == 2) {
				if (parameters == null) {
					parameters = new TreeMap<String,String>();
				}
				parameters.put(pair[0].trim(), pair[1].trim());
			}
		}

		if (parameters == null) {
			MimeType type = ourSimpleTypesMap.get(name);
			if (type == null) {
				type = new MimeType(name, null);
				ourSimpleTypesMap.put(name, type);
			}
			return type;
		}

		return new MimeType(name, parameters);
	}

	// MIME types / application
	// ???
	public static final MimeType APP_ZIP = get("application/zip");
	public static final MimeType APP_RAR = get("application/x-rar-compressed");
	// unofficial, http://en.wikipedia.org/wiki/EPUB
	public static final MimeType APP_EPUB_ZIP = get("application/epub+zip");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_EPUB = get("application/epub");
	// ???
	// unofficial, used by Calibre server
	// unofficial, used by FBReder book network
	// http://www.iana.org/assignments/media-types/application/index.html
	// http://www.iana.org/assignments/media-types/application/index.html
	public static final MimeType APP_RTF = get("application/rtf");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_TXT = get("application/txt");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_HTML = get("application/html");
	// unofficial, used by flibusta catalog
	public static final MimeType APP_HTMLHTM = get("application/html+htm");
	// unofficial, used by flibusta catalog
	// http://www.iana.org/assignments/media-types/application/index.html
	// MIME types / text
	// ???
	// http://www.iana.org/assignments/media-types/text/index.html
	public static final MimeType TEXT_HTML = get("text/html");
	// ???
	// http://www.iana.org/assignments/media-types/text/index.html
	public static final MimeType TEXT_PLAIN = get("text/plain");
	// http://www.iana.org/assignments/media-types/text/index.html
	public static final MimeType TEXT_RTF = get("text/rtf");
	// unofficial, used by Calibre OPDS server

	// MIME images
	// http://www.iana.org/assignments/media-types/image/index.html
	public static final MimeType IMAGE_PNG = get("image/png");
	// http://www.iana.org/assignments/media-types/image/index.html
	public static final MimeType IMAGE_JPEG = get("image/jpeg");
	// http://www.iana.org/assignments/media-types/image/index.html
	// ???
	// video
	public static final MimeType VIDEO_MP4 = get("video/mp4");
	public static final MimeType VIDEO_WEBM = get("video/webm");
	public static final MimeType VIDEO_OGG = get("video/ogg");

	public static final MimeType UNKNOWN = get("*/*");
	public static final MimeType NULL = new MimeType(null, null);

	public static final List<MimeType> TYPES_VIDEO
		 = Arrays.asList(VIDEO_WEBM, VIDEO_OGG, VIDEO_MP4);

	public static final List<MimeType> TYPES_EPUB
		 = Arrays.asList(APP_EPUB_ZIP, APP_EPUB);
	public static final List<MimeType> TYPES_TXT
		 = Arrays.asList(TEXT_PLAIN, APP_TXT);
	public static final List<MimeType> TYPES_RTF
		 = Arrays.asList(APP_RTF, TEXT_RTF);
	public static final List<MimeType> TYPES_HTML
		 = Arrays.asList(TEXT_HTML, APP_HTML, APP_HTMLHTM);

	public final String Name;

	private final Map<String,String> myParameters;

	private MimeType(String name, Map<String,String> parameters) {
		Name = name;
		myParameters = parameters;
	}

	public MimeType clean() {
		if (myParameters == null) {
			return this;
		}
		return get(Name);
	}

	public String getParameter(String key) {
		return myParameters != null ? myParameters.get(key) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof MimeType)) {
			return false;
		}
		final MimeType type = (MimeType)o;
		return
			ComparisonUtil.equal(Name, type.Name) &&
			MiscUtil.mapsEquals(myParameters, type.myParameters);
	}

	public boolean weakEquals(MimeType type) {
		return ComparisonUtil.equal(Name, type.Name);
	}

	@Override
	public int hashCode() {
		return ComparisonUtil.hashCode(Name);
	}

	@Override
	public String toString() {
		if (myParameters == null) {
			return Name;
		}

		final StringBuilder buffer = new StringBuilder(Name);
		for (Map.Entry<String,String> entry : myParameters.entrySet()) {
			buffer.append(';');
			buffer.append(entry.getKey());
			buffer.append('=');
			buffer.append(entry.getValue());
		}
		return buffer.toString();
	}
}
