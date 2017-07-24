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

import android.os.Parcel;

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.text.model.ZLTextModel;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public final class Book extends AbstractBook {
	public final ZLFile File;
	private final String myPath;
	private Set<String> mVisitedHyperlinks;
	public final TOCTree TOCTree = new TOCTree();
	private TOCTree myCurrentTree = TOCTree;
	protected ZLTextModel myBookTextModel;
	protected final HashMap<String, ZLTextModel> myFootnotes = new HashMap<>();

	public Book(long id, String path, String title, String encoding, String language) {
		super(id, title, encoding, language);
		if (path == null) {
			throw new IllegalArgumentException("Creating book with no file");
		}
		File = ZLFile.createFileByPath(path);
		myPath = path;
	}

	public Book(ZLFile file, FormatPlugin plugin) throws BookReadingException {
		super(-1, null, null, null);
		BookUtil.readMetainfo(this, plugin);
		File = file;
		myPath = file.getPath();
	}

	public boolean isHyperlinkVisited(String linkId) {
		return mVisitedHyperlinks.contains(linkId);
	}

	public void markHyperlinkAsVisited(String linkId) {
		mVisitedHyperlinks.add(linkId);
	}

	public void setFootnoteModel(ZLTextModel model) {
		myFootnotes.put(model.getId(), model);
	}

	public ZLTextModel getFootnoteModel(String id) {
		return myFootnotes.get(id);
	}

	public void setBookTextModel(ZLTextModel model) {
		myBookTextModel = model;
	}

	public ZLTextModel getTextModel() {
		return myBookTextModel;
	}

	public void addTOCItem(String text, int reference) {
		myCurrentTree = new TOCTree(myCurrentTree);
		myCurrentTree.setText(text);
		myCurrentTree.setReference(myBookTextModel, reference);
	}

	public void leaveTOCItem() {
		myCurrentTree = myCurrentTree.Parent;
		if (myCurrentTree == null) {
			myCurrentTree = TOCTree;
		}
	}

	@Override
	public String getPath() {
		return File.getPath();
	}

	@Override
	public int hashCode() {
		return myPath.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Book)) {
			return false;
		}
		return myPath.equals(((Book)o).myPath);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(this.myPath);
		dest.writeParcelable(File, flags);
	}

	protected Book(Parcel in) {
		super(in);
		this.myPath = in.readString();
		this.File = in.readParcelable(ZLFile.class.getClassLoader());
	}

	public static final Creator<Book> CREATOR = new Creator<Book>() {
		@Override
		public Book createFromParcel(Parcel source) {
			return new Book(source);
		}

		@Override
		public Book[] newArray(int size) {
			return new Book[size];
		}
	};
}
