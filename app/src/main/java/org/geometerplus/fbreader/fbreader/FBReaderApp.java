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
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public final class FBReaderApp extends ZLApplication {
	public final ZLTextStyleCollection myTextStyleCollection;

	private final ZLKeyBindings myBindings;

	public final FBView BookTextView;
	public final FBView FootnoteView;
	private String myFootnoteModelId;

	public volatile BookModel Model;
	public volatile Book ExternalBook;

	private Context mContext;

	public FBReaderApp(Context pContext) {
		mContext = pContext;
		myBindings = new ZLKeyBindings(pContext);
		myTextStyleCollection = new ZLTextStyleCollection(pContext, "Base");

		addAction(ActionCode.INCREASE_FONT, new ChangeFontSizeAction(this, +2));
		addAction(ActionCode.DECREASE_FONT, new ChangeFontSizeAction(this, -2));

		addAction(ActionCode.FIND_NEXT, new FindNextAction(this));
		addAction(ActionCode.FIND_PREVIOUS, new FindPreviousAction(this));
		addAction(ActionCode.CLEAR_FIND_RESULTS, new ClearFindResultsAction(this));

		addAction(ActionCode.SELECTION_CLEAR, new SelectionClearAction(this));

		addAction(ActionCode.TURN_PAGE_FORWARD, new TurnPageAction(this, true));
		addAction(ActionCode.TURN_PAGE_BACK, new TurnPageAction(this, false));

		addAction(ActionCode.MOVE_CURSOR_UP, new MoveCursorAction(this, FBView.Direction.up));
		addAction(ActionCode.MOVE_CURSOR_DOWN, new MoveCursorAction(this, FBView.Direction.down));
		addAction(ActionCode.MOVE_CURSOR_LEFT, new MoveCursorAction(this, FBView.Direction.rightToLeft));
		addAction(ActionCode.MOVE_CURSOR_RIGHT, new MoveCursorAction(this, FBView.Direction.leftToRight));

		addAction(ActionCode.VOLUME_KEY_SCROLL_FORWARD, new VolumeKeyTurnPageAction(this, true));
		addAction(ActionCode.VOLUME_KEY_SCROLL_BACK, new VolumeKeyTurnPageAction(this, false));

		BookTextView = new FBView(this);
		FootnoteView = new FBView(this);

		setView(BookTextView);
	}

	public Book getCurrentBook() {
		final BookModel m = Model;
		return m != null ? m.Book : ExternalBook;
	}

	public void openBook(Book book, final Bookmark bookmark) {
		if (Model != null) {
			if (book == null || bookmark == null) {
				return;
			}
		}

		if (book == null) {
			return;
		}

		openBookInternal(book, bookmark, false);
	}

	private void reloadBook() {
		final Book book = getCurrentBook();
		if (book != null) {
			openBookInternal(book, null, true);
		}
	}

	public ZLKeyBindings keyBindings() {
		return myBindings;
	}

	public FBView getTextView() {
		return (FBView)getCurrentView();
	}

	public AutoTextSnippet getFootnoteData(String id) {
		if (Model == null) {
			return null;
		}
		final BookModel.Label label = Model.getLabel(id);
		if (label == null) {
			return null;
		}
		final ZLTextModel model;
		if (label.ModelId != null) {
			model = Model.Book.getFootnoteModel(label.ModelId);
		} else {
			model = Model.Book.getTextModel();
		}
		if (model == null) {
			return null;
		}
		final ZLTextWordCursor cursor =
			new ZLTextWordCursor(new ZLTextParagraphCursor(mContext, model, label.ParagraphIndex));
		final AutoTextSnippet longSnippet = new AutoTextSnippet(cursor, 140);
		if (longSnippet.IsEndOfText) {
			return longSnippet;
		} else {
			return new AutoTextSnippet(cursor, 100);
		}
	}

	public void tryOpenFootnote(String id) {
		if (Model != null) {
			final BookModel.Label label = Model.getLabel(id);
			if (label != null) {
				if (label.ModelId == null) {
					BookTextView.gotoPosition(label.ParagraphIndex, 0, 0);
					setView(BookTextView);
				} else {
					setFootnoteModel(label.ModelId);
					setView(FootnoteView);
					FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
				}
				getViewWidget().repaint();
				storePosition();
			}
		}
	}

	public void clearTextCaches() {
		BookTextView.clearCaches();
		FootnoteView.clearCaches();
	}

	public Bookmark addSelectionBookmark() {
		final FBView fbView = getTextView();
		final TextSnippet snippet = fbView.getSelectedSnippet();
		if (snippet == null) {
			return null;
		}

		final Bookmark bookmark = new Bookmark((long) Model.Book.getPath().hashCode(), snippet, true);
		BooksDaoHelper.getInstance(mContext).getBookmarksDao().insertOrReplace(bookmark);
		fbView.clearSelection();

		return bookmark;
	}

	private void setBookmarkHighlightings(ZLTextView view, String modelId) {
		view.removeHighlightings(BookmarkHighlighting.class);
		List<Bookmark> bookmarks = BooksDaoHelper.getInstance(mContext).getBookmarksDao().loadAll();
		for (Bookmark b : bookmarks) {
			if (b.endPosition == null) {
				b.findEnd(view);
			}
			view.addHighlighting(new BookmarkHighlighting(view, b));
		}
	}

	private void setFootnoteModel(String modelId) {
		final ZLTextModel model = Model.Book.getFootnoteModel(modelId);
		FootnoteView.setModel(model);
		if (model != null) {
			myFootnoteModelId = modelId;
			setBookmarkHighlightings(FootnoteView, modelId);
		}
	}

	private synchronized void openBookInternal(final Book book, Bookmark bookmark, boolean force) {
		if (!force && Model != null) {
			if (bookmark != null) {
				gotoBookmark(bookmark, false);
			}
			return;
		}

		hideActivePopup();
		storePosition();

		BookTextView.setModel(null);
		FootnoteView.setModel(null);
		clearTextCaches();
		Model = null;
		ExternalBook = null;
		System.gc();
		System.gc();

		final PluginCollection pluginCollection = PluginCollection.Instance();
		final FormatPlugin plugin;
		try {
			plugin = BookUtil.getPlugin(pluginCollection, book);
		} catch (BookReadingException e) {
			e.printStackTrace();
			return;
		}

		try {
			Model = BookModel.createModel(mContext, book, plugin);
			ZLTextHyphenator.Instance().load(mContext, book.getLanguage());
			BookTextView.setModel(Model.Book.getTextModel());
			setBookmarkHighlightings(BookTextView, null);
			gotoStoredPosition();
			if (bookmark == null) {
				setView(BookTextView);
			} else {
				gotoBookmark(bookmark, false);
			}
		} catch (BookReadingException e) {
			e.printStackTrace();
		}

		getViewWidget().reset();
		getViewWidget().repaint();

		for (FileEncryptionInfo info : plugin.readEncryptionInfos(book)) {
			if (info != null && !EncryptionMethod.isSupported(info.Method)) {
				Toast.makeText(mContext, "FBReader不支持文件 "+ book.getPath() +" 加密方法. 某些页面不可读", Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	private void gotoBookmark(Bookmark bookmark, boolean exactly) {
		if (exactly) {
			BookTextView.gotoPosition(bookmark.startPosition);
		} else {
			BookTextView.gotoHighlighting(
				new BookmarkHighlighting(BookTextView, bookmark)
			);
		}
		setView(BookTextView);
		getViewWidget().repaint();
		storePosition();
	}

	public void showBookTextView() {
		setView(BookTextView);
	}

	public void onWindowClosing() {
		storePosition();
	}

	private class PositionSaver implements Runnable {
		private final Book myBook;
		private final ZLTextPosition myPosition;
		private final RationalNumber myProgress;

		PositionSaver(Book book, ZLTextPosition position, RationalNumber progress) {
			myBook = book;
			myPosition = position;
			myProgress = progress;
		}

		public void run() {
			if (null != myPosition) {
				BookState state = new BookState((long) myBook.getPath().hashCode(), myPosition, new Date());
				BooksDaoHelper.getInstance(mContext).getBookStateDao()
						.insertOrReplace(state);
			}
			myBook.setProgress(myProgress);
		}
	}

	private class SaverThread extends Thread {
		private final List<Runnable> myTasks =
			Collections.synchronizedList(new LinkedList<Runnable>());

		SaverThread() {
			setPriority(MIN_PRIORITY);
		}

		void add(Runnable task) {
			myTasks.add(task);
		}

		public void run() {
			while (true) {
				synchronized (myTasks) {
					while (!myTasks.isEmpty()) {
						myTasks.remove(0).run();
					}
				}
				try {
					sleep(500);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private final SaverThread mySaverThread = new SaverThread();
	private volatile ZLTextPosition myStoredPosition;
	private volatile Book myStoredPositionBook;

	private ZLTextPosition getStoredPosition(Book book) {
		BookState state = BooksDaoHelper.getInstance(mContext).getBookStateDao()
				.load((long) book.getPath().hashCode());
		if (null != state) {
			return state.position;
		}
		return null;
	}

	private void gotoStoredPosition() {
		myStoredPositionBook = Model != null ? Model.Book : null;
		if (myStoredPositionBook == null) {
			return;
		}
		myStoredPosition = getStoredPosition(myStoredPositionBook);
		BookTextView.gotoPosition(myStoredPosition);
		savePosition();
	}

	public void storePosition() {
		final Book bk = Model != null ? Model.Book : null;
		if (bk != null && bk == myStoredPositionBook && myStoredPosition != null && BookTextView != null) {
			final ZLTextPosition position = new ZLTextFixedPosition(BookTextView.getStartCursor());
			if (!myStoredPosition.equals(position)) {
				myStoredPosition = position;
				savePosition();
			}
		}
	}

	private void savePosition() {
		final RationalNumber progress = BookTextView.getProgress();
		synchronized (mySaverThread) {
			if (!mySaverThread.isAlive()) {
				mySaverThread.start();
			}
			mySaverThread.add(new PositionSaver(myStoredPositionBook, myStoredPosition, progress));
		}
	}

	public Bookmark createBookmark(int maxChars, boolean visible) {
		final FBView view = getTextView();
		final ZLTextWordCursor cursor = view.getStartCursor();

		if (cursor.isNull()) {
			return null;
		}

		return new Bookmark((long) Model.Book.getPath().hashCode(), new AutoTextSnippet(cursor, maxChars), visible);
	}

	public TOCTree getCurrentTOCElement() {
		final ZLTextWordCursor cursor = BookTextView.getStartCursor();
		if (Model == null || cursor == null) {
			return null;
		}

		int index = cursor.getParagraphIndex();
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = null;
		for (TOCTree tree : Model.Book.TOCTree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference == null) {
				continue;
			}
			if (reference.ParagraphIndex > index) {
				break;
			}
			treeToSelect = tree;
		}
		return treeToSelect;
	}

	public void onBookUpdated(Book book) {
		if (Model == null || Model.Book == null || book == null) {
			return;
		}

		final String newEncoding = book.getEncodingNoDetection();
		final String oldEncoding = Model.Book.getEncodingNoDetection();

		Model.Book.updateFrom(book);

		if (newEncoding != null && !newEncoding.equals(oldEncoding)) {
			reloadBook();
		} else {
			ZLTextHyphenator.Instance().load(mContext, Model.Book.getLanguage());
			clearTextCaches();
			getViewWidget().repaint();
		}
	}

	@Override
	public void runAction(final String actionId, final Object... params) {
		if (ActionCode.INCREASE_FONT.equals(actionId)) {
			actionChangeFontSize(2);
		} else if (ActionCode.DECREASE_FONT.equals(actionId)) {
			actionChangeFontSize(-2);
		} else {
			super.runAction(actionId, params);
		}
	}

	public Context getContext() {
		return mContext;
	}

	/**
	 * 改变字体大小
	 *
	 * @param delta
	 *     改变范围
	 */
	public void actionChangeFontSize(int delta) {
		myTextStyleCollection.getBaseStyle().changeFontSize(getContext(), delta);

		clearTextCaches();
		getViewWidget().repaint();
	}

	/**
	 * 获取分页信息
	 * @return
	 */
	public PagePosition pagePosition() {
		return getTextView().pagePosition();
	}

	/**
	 * 前往某页, 第一页为 1
	 * @param page
	 */
	public void gotoPage(int page) {
		final ZLTextView view = getTextView();
		if (page == 1) {
			view.gotoHome();
		} else {
			view.gotoPage(page);
		}
		getViewWidget().reset();
		getViewWidget().repaint();
	}

	/**
	 * 前往某位置
	 * @param position
	 */
	public void gotoPosition(ZLTextWordCursor position) {
		getTextView().gotoPosition(position);
		getViewWidget().reset();
		getViewWidget().repaint();
	}

	/**
	 * 获取当前位置
	 * @return
	 */
	public ZLTextWordCursor getCurrentPosition() {
		return getTextView().getStartCursor();
	}
}
