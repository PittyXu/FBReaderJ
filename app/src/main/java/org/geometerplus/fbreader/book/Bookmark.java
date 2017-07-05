/*
 * Copyright (C) 2009-2015 FBReader.ORG Limited <contact@fbreader.org>
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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable.Creator;

import org.geometerplus.android.fbreader.config.StylePreferences;
import org.geometerplus.fbreader.util.ComparisonUtil;
import org.geometerplus.fbreader.util.TextSnippet;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import java.util.Comparator;
import java.util.UUID;

public final class Bookmark extends ZLTextFixedPosition {
	public enum DateType {
		Creation,
		Modification,
		Access,
		Latest
	}

	private long myId;
	public final String Uid;
	private String myVersionUid;

	public final AbstractBook book;
	public final long BookId;
	public final String BookTitle;
	private String myText;
	private String myOriginalText;

	public final long CreationTimestamp;
	private Long myModificationTimestamp;
	private Long myAccessTimestamp;
	private ZLTextFixedPosition myEnd;
	private int myLength;
	private int myStyleId;

	public final String ModelId;
	public final boolean IsVisible;

	// used for migration only
	private Bookmark(AbstractBook pBook, Bookmark original) {
		super(original);
		myId = -1;
		Uid = newUUID();
		book = pBook;
		BookId = pBook.getId();
		BookTitle = original.BookTitle;
		myText = original.myText;
		myOriginalText = original.myOriginalText;
		CreationTimestamp = original.CreationTimestamp;
		myModificationTimestamp = original.myModificationTimestamp;
		myAccessTimestamp = original.myAccessTimestamp;
		myEnd = original.myEnd;
		myLength = original.myLength;
		myStyleId = original.myStyleId;
		ModelId = original.ModelId;
		IsVisible = original.IsVisible;
	}

	// create java object for existing bookmark
	// uid parameter can be null when comes from old format plugin!
	public Bookmark(Book pBook,
		long id, String uid, String versionUid,
		long bookId, String bookTitle, String text, String originalText,
		long creationTimestamp, Long modificationTimestamp, Long accessTimestamp,
		String modelId,
		int start_paragraphIndex, int start_elementIndex, int start_charIndex,
		int end_paragraphIndex, int end_elementIndex, int end_charIndex,
		boolean isVisible,
		int styleId
	) {
		super(start_paragraphIndex, start_elementIndex, start_charIndex);

		myId = id;
		Uid = verifiedUUID(uid);
		myVersionUid = verifiedUUID(versionUid);

		book = pBook;
		BookId = bookId;
		BookTitle = bookTitle;
		myText = text;
		myOriginalText = originalText;
		CreationTimestamp = creationTimestamp;
		myModificationTimestamp = modificationTimestamp;
		myAccessTimestamp = accessTimestamp;
		ModelId = modelId;
		IsVisible = isVisible;

		if (end_charIndex >= 0) {
			myEnd = new ZLTextFixedPosition(end_paragraphIndex, end_elementIndex, end_charIndex);
		} else {
			myLength = end_paragraphIndex;
		}

		myStyleId = styleId;
	}

	// creates new bookmark
	public Bookmark(Context pContext, Book pBook, String modelId, TextSnippet snippet, boolean visible) {
		super(snippet.getStart());

		myId = -1;
		Uid = newUUID();
		book = pBook;
		BookId = pBook.getId();
		BookTitle = pBook.getTitle();
		myText = snippet.getText();
		myOriginalText = null;
		CreationTimestamp = System.currentTimeMillis();
		ModelId = modelId;
		IsVisible = visible;
		myEnd = new ZLTextFixedPosition(snippet.getEnd());
		myStyleId = StylePreferences.getDefaultHighlightingStyle(pContext);
	}

	public long getId() {
		return myId;
	}

	public String getVersionUid() {
		return myVersionUid;
	}

	private void onModification() {
		myVersionUid = newUUID();
		myModificationTimestamp = System.currentTimeMillis();
	}

	public int getStyleId() {
		return myStyleId;
	}

	public void setStyleId(int styleId) {
		if (styleId != myStyleId) {
			myStyleId = styleId;
			onModification();
		}
	}

	public String getText() {
		return myText;
	}

	public String getOriginalText() {
		return myOriginalText;
	}

	public void setText(String text) {
		if (!text.equals(myText)) {
			if (myOriginalText == null) {
				myOriginalText = myText;
			} else if (myOriginalText.equals(text)) {
				myOriginalText = null;
			}
			myText = text;
			onModification();
		}
	}

	public Long getTimestamp(DateType type) {
		switch (type) {
			case Creation:
				return CreationTimestamp;
			case Modification:
				return myModificationTimestamp;
			case Access:
				return myAccessTimestamp;
			default:
			case Latest:
			{
				Long latest = myModificationTimestamp;
				if (latest == null) {
					latest = CreationTimestamp;
				}
				if (myAccessTimestamp != null && latest < myAccessTimestamp) {
					return myAccessTimestamp;
				} else {
					return latest;
				}
			}
		}
	}

	public ZLTextPosition getEnd() {
		return myEnd;
	}

	void setEnd(int paragraphsIndex, int elementIndex, int charIndex) {
		myEnd = new ZLTextFixedPosition(paragraphsIndex, elementIndex, charIndex);
	}

	public int getLength() {
		return myLength;
	}

	public void markAsAccessed() {
		myVersionUid = newUUID();
		myAccessTimestamp = System.currentTimeMillis();
	}

	public static class ByTimeComparator implements Comparator<Bookmark> {
		public int compare(Bookmark bm0, Bookmark bm1) {
			final Long ts0 = bm0.getTimestamp(DateType.Latest);
			final Long ts1 = bm1.getTimestamp(DateType.Latest);
			// yes, reverse order; yes, latest ts is not null
			return ts1.compareTo(ts0);
		}
	}

	public void setId(long id) {
		myId = id;
	}

	public void update(Bookmark other) {
		// TODO: copy other fields (?)
		if (other != null) {
			myId = other.myId;
		}
	}

	Bookmark transferToBook(AbstractBook book) {
		final long bookId = book.getId();
		return bookId != -1 ? new Bookmark(book, this) : null;
	}

	// not equals, we do not compare ids
	boolean sameAs(Bookmark other) {
		return
			ParagraphIndex == other.ParagraphIndex &&
			ElementIndex == other.ElementIndex &&
			CharIndex == other.CharIndex &&
			ComparisonUtil.equal(myText, other.myText);
	}

	private static String newUUID() {
		return UUID.randomUUID().toString();
	}

	private static String verifiedUUID(String uid) {
		if (uid == null || uid.length() == 36) {
			return uid;
		}
		throw new RuntimeException("INVALID UUID: " + uid);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(this.myId);
		dest.writeString(this.Uid);
		dest.writeString(this.myVersionUid);
		dest.writeParcelable(book, flags);
		dest.writeLong(this.BookId);
		dest.writeString(this.BookTitle);
		dest.writeString(this.myText);
		dest.writeString(this.myOriginalText);
		dest.writeLong(this.CreationTimestamp);
		dest.writeValue(this.myModificationTimestamp);
		dest.writeValue(this.myAccessTimestamp);
		dest.writeParcelable(this.myEnd, flags);
		dest.writeInt(this.myLength);
		dest.writeInt(this.myStyleId);
		dest.writeString(this.ModelId);
		dest.writeByte(this.IsVisible ? (byte) 1 : (byte) 0);
	}

	protected Bookmark(Parcel in) {
		super(in);
		this.myId = in.readLong();
		this.Uid = in.readString();
		this.myVersionUid = in.readString();
		this.book = in.readParcelable(Book.class.getClassLoader());
		this.BookId = in.readLong();
		this.BookTitle = in.readString();
		this.myText = in.readString();
		this.myOriginalText = in.readString();
		this.CreationTimestamp = in.readLong();
		this.myModificationTimestamp = (Long) in.readValue(Long.class.getClassLoader());
		this.myAccessTimestamp = (Long) in.readValue(Long.class.getClassLoader());
		this.myEnd = in.readParcelable(ZLTextFixedPosition.class.getClassLoader());
		this.myLength = in.readInt();
		this.myStyleId = in.readInt();
		this.ModelId = in.readString();
		this.IsVisible = in.readByte() != 0;
	}

	public static final Creator<Bookmark> CREATOR = new Creator<Bookmark>() {
		@Override
		public Bookmark createFromParcel(Parcel source) {
			return new Bookmark(source);
		}

		@Override
		public Bookmark[] newArray(int size) {
			return new Bookmark[size];
		}
	};
}
