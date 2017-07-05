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

package org.geometerplus.fbreader.formats;

import org.geometerplus.fbreader.book.AbstractBook;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

import java.util.Collections;
import java.util.List;

public abstract class FormatPlugin {
	private final String myFileType;

	protected FormatPlugin(String fileType) {
		myFileType = fileType;
	}

	public final String supportedFileType() {
		return myFileType;
	}

	public final String name() {
		return myFileType;
	}

	public List<FileEncryptionInfo> readEncryptionInfos(Book book) {
		return Collections.emptyList();
	}
	public abstract void readMetainfo(Book book) throws BookReadingException;
	public abstract void detectLanguageAndEncoding(Book book) throws BookReadingException;
	public abstract ZLImage readCover(ZLFile file);

	/* lesser is higher: 0 for ePub, 5 for other native, 10 for external */
	public abstract int priority();
}
