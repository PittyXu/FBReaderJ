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
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.geometerplus.android.fbreader.dao.BookState;
import org.geometerplus.android.fbreader.dao.Bookmark;
import org.geometerplus.android.fbreader.dao.BooksDaoHelper;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.util.AutoTextSnippet;
import org.geometerplus.fbreader.util.TextSnippet;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.drm.EncryptionMethod;
import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextView.PagePosition;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import java.util.Date;
import java.util.List;

public final class FBReaderApp extends ZLApplication {

	private Context mContext;
	private DisplayMetrics myMetrics;

	public FBReaderApp(Context pContext, ZLAndroidWidget pWidget) {
		mContext = pContext;

		setViewWidget(pWidget);
	}

	private DisplayMetrics getMetrics() {
		if (myMetrics == null) {
			myMetrics = mContext.getApplicationContext().getResources().getDisplayMetrics();
		}
		return myMetrics;
	}

	public int getDisplayDPI() {
		final DisplayMetrics metrics = getMetrics();
		return metrics == null ? 0 : (int)(160 * metrics.density);
	}

	public Context getContext() {
		return mContext;
	}
}
