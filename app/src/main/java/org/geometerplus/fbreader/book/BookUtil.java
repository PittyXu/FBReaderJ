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

package org.geometerplus.fbreader.book;

import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

public abstract class BookUtil {

	public static FormatPlugin getPlugin(PluginCollection pluginCollection, Book book) throws BookReadingException {
		final ZLFile file = fileByBook(book);
		final FormatPlugin plugin = pluginCollection.getPlugin(file);
		if (plugin == null) {
			throw new BookReadingException("pluginNotFound", file);
		}
		return plugin;
	}

	public static void readMetainfo(Book book, PluginCollection pluginCollection) throws BookReadingException {
		readMetainfo(book, getPlugin(pluginCollection, book));
	}

	public static void readMetainfo(Book book, FormatPlugin plugin) throws BookReadingException {
		book.myEncoding = null;
		book.myLanguage = null;
		book.setTitle(null);

		book.mySaveState = AbstractBook.SaveState.NotSaved;

		plugin.readMetainfo(book);

		if (book.isTitleEmpty()) {
			final String fileName = fileByBook(book).getShortName();
			final int index = fileName.lastIndexOf('.');
			book.setTitle(index > 0 ? fileName.substring(0, index) : fileName);
		}
	}

	public static ZLFile fileByBook(Book book) {
		return book.File;
	}
}
