/*
 * Copyright (C) 2011-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.content.Context;

import org.geometerplus.fbreader.book.AbstractBook;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.formats.oeb.OEBNativePlugin;
import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLFileImage;
import org.geometerplus.zlibrary.core.image.ZLFileImageProxy;
import org.geometerplus.zlibrary.text.model.CachedCharStorageException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NativeFormatPlugin extends BuiltinFormatPlugin {
	private static final Object ourNativeLock = new Object();

	public static NativeFormatPlugin create(String fileType) {
		if ("ePub".equals(fileType)) {
			return new OEBNativePlugin();
		} else {
			return new NativeFormatPlugin(fileType);
		}
	}

	protected NativeFormatPlugin(String fileType) {
		super(fileType);
	}

	@Override
	synchronized public void readMetainfo(Book book) throws BookReadingException {
		final int code;
		synchronized (ourNativeLock) {
			code = readMetainfoNative(book);
		}
		if (code != 0) {
			throw new BookReadingException(
					"原生代码错误 " + String.valueOf(code) + ", 阅读 '" + book.getPath() + "' 时",
					BookUtil.fileByBook(book));
		}
	}

	private native int readMetainfoNative(AbstractBook book);

	@Override
	public List<FileEncryptionInfo> readEncryptionInfos(Book book) {
		final FileEncryptionInfo[] infos;
		synchronized (ourNativeLock) {
			infos = readEncryptionInfosNative(book);
		}
		return infos != null
			? Arrays.asList(infos)
			: Collections.<FileEncryptionInfo>emptyList();
	}

	private native FileEncryptionInfo[] readEncryptionInfosNative(AbstractBook book);

	@Override
	public void detectLanguageAndEncoding(Book book) {
		synchronized (ourNativeLock) {
			detectLanguageAndEncodingNative(book);
		}
	}

	private native void detectLanguageAndEncodingNative(AbstractBook book);

	@Override
	synchronized public void readModel(Context pContext, BookModel model) throws BookReadingException {
		final int code;
		final String tempDirectory = pContext.getExternalCacheDir().getAbsolutePath();
		synchronized (ourNativeLock) {
			code = readModelNative(model, tempDirectory);
		}
		switch (code) {
			case 0:
				return;
			case 3:
				throw new CachedCharStorageException(
					"Cannot write file from native code to " + tempDirectory
				);
			default:
				throw new BookReadingException(
						"原生代码错误 " + String.valueOf(code) + ", 阅读 '" + model.Book.getPath() + "' 时",
						BookUtil.fileByBook(model.Book));
		}
	}

	private native int readModelNative(BookModel model, String cacheDir);

	@Override
	public final ZLFileImageProxy readCover(ZLFile file) {
		return new ZLFileImageProxy(file) {
			@Override
			protected ZLFileImage retrieveRealImage() {
				final ZLFileImage[] box = new ZLFileImage[1];
				synchronized (ourNativeLock) {
					readCoverNative(File, box);
				}
				return box[0];
			}
		};
	}

	private native void readCoverNative(ZLFile file, ZLFileImage[] box);

	@Override
	public int priority() {
		return 5;
	}

	@Override
	public String toString() {
		return "NativeFormatPlugin [" + supportedFileType() + "]";
	}
}
