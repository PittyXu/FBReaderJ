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

package org.geometerplus.zlibrary.core.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

final class ZLZipEntryFile extends ZLArchiveEntryFile {
	static List<ZLFile> archiveEntries(ZLFile archive) {
		try {
			final ZipFile zf = ZLZipEntryFile.getZipFile(archive);
			Enumeration<? extends ZipEntry> e = zf.entries();
			ArrayList<ZLFile> entries = new ArrayList<ZLFile>(zf.size());
			while (e.hasMoreElements()) {
				ZipEntry entry = e.nextElement();
				entries.add(new ZLZipEntryFile(archive, entry.getName()));
			}
			return entries;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}

	private static HashMap<ZLFile,ZipFile> ourZipFileMap = new HashMap<ZLFile,ZipFile>();

	private static ZipFile getZipFile(final ZLFile file) throws IOException {
		synchronized (ourZipFileMap) {
			ZipFile zf = file.isCached() ? ourZipFileMap.get(file) : null;
			if (zf == null) {
				zf = new ZipFile(file.getPhysicalFile().javaFile());
				if (file.isCached()) {
					ourZipFileMap.put(file, zf);
				}
			}
			return zf;
		}
	}

	static void removeFromCache(ZLFile file) {
		ourZipFileMap.remove(file);
	}

	ZLZipEntryFile(ZLFile parent, String name) {
		super(parent, name);
	}

	@Override
	public boolean exists() {
		try {
			return myParent.exists() && getZipFile(myParent).getEntry(myName) != null;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public long size() {
		try {
			return getZipFile(myParent).getEntry(myName).getSize();
		} catch (IOException e) {
			return 0;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		ZipFile zipFile = getZipFile(myParent);
		ZipEntry zipEntry = zipFile.getEntry(myName);
		return zipFile.getInputStream(zipEntry);
	}
}
