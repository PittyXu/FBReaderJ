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

import org.geometerplus.fbreader.sort.TitledEntity;
import org.geometerplus.fbreader.util.ComparisonUtil;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.RationalNumber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class AbstractBook extends TitledEntity<AbstractBook> {

	protected volatile long myId;

	protected volatile String myEncoding;
	protected volatile String myLanguage;
	protected volatile List<Author> myAuthors;
	protected volatile List<Label> myLabels;
//	protected volatile SeriesInfo mySeriesInfo;
	protected volatile List<UID> myUids;
	protected volatile RationalNumber myProgress;

	public volatile boolean HasBookmark;

	protected enum SaveState {
		Saved,
		ProgressNotSaved,
		NotSaved
  }

  protected volatile SaveState mySaveState = SaveState.NotSaved;

	AbstractBook(long id, String title, String encoding, String language) {
		super(title);
		myId = id;
		myEncoding = encoding;
		myLanguage = language;
		mySaveState = SaveState.Saved;
	}

	public abstract String getPath();

	public void updateFrom(AbstractBook book) {
		if (book == null || myId != book.myId) {
			return;
		}
		setTitle(book.getTitle());
		setEncoding(book.myEncoding);
		setLanguage(book.myLanguage);
		if (!ComparisonUtil.equal(myAuthors, book.myAuthors)) {
			myAuthors = book.myAuthors != null ? new ArrayList<Author>(book.myAuthors) : null;
			mySaveState = SaveState.NotSaved;
		}
		if (!MiscUtil.listsEquals(myLabels, book.myLabels)) {
			myLabels = book.myLabels != null ? new ArrayList<Label>(book.myLabels) : null;
			mySaveState = SaveState.NotSaved;
		}
		if (!MiscUtil.listsEquals(myUids, book.myUids)) {
			myUids = book.myUids != null ? new ArrayList<UID>(book.myUids) : null;
			mySaveState = SaveState.NotSaved;
		}
		setProgress(book.myProgress);
		if (HasBookmark != book.HasBookmark) {
			HasBookmark = book.HasBookmark;
			mySaveState = SaveState.NotSaved;
		}
	}

	public final List<Author> authors() {
		return myAuthors != null
			? Collections.unmodifiableList(myAuthors)
			: Collections.<Author>emptyList();
	}

	public final String authorsString(String separator) {
		final List<Author> authors = myAuthors;
		if (authors == null || authors.isEmpty()) {
			return null;
		}

		final StringBuilder buffer = new StringBuilder();
		boolean first = true;
		for (Author a : authors) {
			if (!first) {
				buffer.append(separator);
			}
			buffer.append(a.DisplayName);
			first = false;
		}
		return buffer.toString();
	}

	void addAuthorWithNoCheck(Author author) {
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
		}
		myAuthors.add(author);
	}

	public void removeAllAuthors() {
		if (myAuthors != null) {
			myAuthors = null;
			mySaveState = SaveState.NotSaved;
		}
	}

	public void addAuthor(Author author) {
		if (author == null) {
			return;
		}
		if (myAuthors == null) {
			myAuthors = new ArrayList<Author>();
			myAuthors.add(author);
			mySaveState = SaveState.NotSaved;
		} else if (!myAuthors.contains(author)) {
			myAuthors.add(author);
			mySaveState = SaveState.NotSaved;
		}
	}

	public void addAuthor(String name) {
		addAuthor(name, null);
	}

	public void addAuthor(String name, String sortKey) {
		addAuthor(Author.create(name, sortKey));
	}

	public long getId() {
		return myId;
	}

	@Override
	public void setTitle(String title) {
		if (title == null) {
			return;
		}
		title = title.trim();
		if (title.length() == 0) {
			return;
		}
		if (!getTitle().equals(title)) {
			super.setTitle(title);
			mySaveState = SaveState.NotSaved;
		}
	}

	@Override
	public String getLanguage() {
		return myLanguage;
	}

	public void setLanguage(String language) {
		if (!ComparisonUtil.equal(myLanguage, language)) {
			myLanguage = language;
			resetSortKey();
			mySaveState = SaveState.NotSaved;
		}
	}

	public String getEncodingNoDetection() {
		return myEncoding;
	}

	public void setEncoding(String encoding) {
		if (!ComparisonUtil.equal(myEncoding, encoding)) {
			myEncoding = encoding;
			mySaveState = SaveState.NotSaved;
		}
	}

	public boolean hasLabel(String name) {
		for (Label l : labels()) {
			if (name.equals(l.Name)) {
				return true;
			}
		}
		return false;
	}

	public List<Label> labels() {
		return myLabels != null ? Collections.unmodifiableList(myLabels) : Collections.<Label>emptyList();
	}

	void addLabelWithNoCheck(Label label) {
		if (myLabels == null) {
			myLabels = new ArrayList<Label>();
		}
		myLabels.add(label);
	}

	public void addNewLabel(String label) {
		addLabel(new Label(label));
	}

	public void addLabel(Label label) {
		if (myLabels == null) {
			myLabels = new ArrayList<Label>();
		}
		if (!myLabels.contains(label)) {
			myLabels.add(label);
			mySaveState = SaveState.NotSaved;
		}
	}

	public List<UID> uids() {
		return myUids != null ? Collections.unmodifiableList(myUids) : Collections.<UID>emptyList();
	}

	public void addUid(String type, String id) {
		addUid(new UID(type, id));
	}

	void addUidWithNoCheck(UID uid) {
		if (uid == null) {
			return;
		}
		if (myUids == null) {
			myUids = new ArrayList<UID>();
		}
		myUids.add(uid);
	}

	public void addUid(UID uid) {
		if (uid == null) {
			return;
		}
		if (myUids == null) {
			myUids = new ArrayList<UID>();
		}
		if (!myUids.contains(uid)) {
			myUids.add(uid);
			mySaveState = SaveState.NotSaved;
		}
	}

	public boolean matchesUid(UID uid) {
		return myUids.contains(uid);
	}

	public RationalNumber getProgress() {
		return myProgress;
	}

	public void setProgress(RationalNumber progress) {
		if (!ComparisonUtil.equal(myProgress, progress)) {
			myProgress = progress;
			if (mySaveState == SaveState.Saved) {
				mySaveState = SaveState.ProgressNotSaved;
			}
		}
	}

	public void setProgressWithNoCheck(RationalNumber progress) {
		myProgress = progress;
	}

	public boolean matches(String pattern) {
		if (MiscUtil.matchesIgnoreCase(getTitle(), pattern)) {
			return true;
		}
		if (myAuthors != null) {
			for (Author author : myAuthors) {
				if (MiscUtil.matchesIgnoreCase(author.DisplayName, pattern)) {
					return true;
				}
			}
		}

		String fileName = getPath();
		// first archive delimiter
		int index = fileName.indexOf(":");
		// last path delimiter before first archive delimiter
		if (index == -1) {
			index = fileName.lastIndexOf("/");
		} else {
			index = fileName.lastIndexOf("/", index);
		}
		fileName = fileName.substring(index + 1);
    return MiscUtil.matchesIgnoreCase(fileName, pattern);
  }

	@Override
	public String toString() {
		return getClass().getName() + "[" + getPath() + ", " + myId + ", " + getTitle() + "]";
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(this.myId);
		dest.writeString(this.myEncoding);
		dest.writeString(this.myLanguage);
		dest.writeList(this.myAuthors);
		dest.writeList(this.myLabels);
		dest.writeList(this.myUids);
		dest.writeParcelable(this.myProgress, flags);
		dest.writeByte(this.HasBookmark ? (byte) 1 : (byte) 0);
		dest.writeInt(this.mySaveState == null ? -1 : this.mySaveState.ordinal());
	}

	protected AbstractBook(Parcel in) {
		super(in);
		this.myId = in.readLong();
		this.myEncoding = in.readString();
		this.myLanguage = in.readString();
		this.myAuthors = new ArrayList<Author>();
		in.readList(this.myAuthors, Author.class.getClassLoader());
		this.myLabels = new ArrayList<Label>();
		in.readList(this.myLabels, Label.class.getClassLoader());
		this.myUids = new ArrayList<UID>();
		in.readList(this.myUids, UID.class.getClassLoader());
		this.myProgress = in.readParcelable(RationalNumber.class.getClassLoader());
		this.HasBookmark = in.readByte() != 0;
		int tmpMySaveState = in.readInt();
		this.mySaveState = tmpMySaveState == -1 ? null : SaveState.values()[tmpMySaveState];
	}
}
