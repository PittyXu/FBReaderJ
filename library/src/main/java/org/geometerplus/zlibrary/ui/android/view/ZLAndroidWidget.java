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

package org.geometerplus.zlibrary.ui.android.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

import org.geometerplus.android.fbreader.dao.BookState;
import org.geometerplus.android.fbreader.dao.Bookmark;
import org.geometerplus.android.fbreader.dao.BooksDaoHelper;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.BookmarkHighlighting;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.util.AutoTextSnippet;
import org.geometerplus.fbreader.util.TextSnippet;
import org.geometerplus.zlibrary.core.drm.EncryptionMethod;
import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextParagraphCursor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextView.PagePosition;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.ui.android.view.animation.AnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.CurlAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.NoneAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.ShiftAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.SlideAnimationProvider;
import org.geometerplus.zlibrary.ui.android.view.animation.SlideOldStyleAnimationProvider;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ZLAndroidWidget extends MainView implements ZLViewWidget, View.OnLongClickListener {
	private final Paint myPaint = new Paint();

	private final BitmapManagerImpl myBitmapManager = new BitmapManagerImpl(this);
	private Bitmap myFooterBitmap;
	private FBReaderApp mApp;
	public volatile BookModel Model;

	private volatile ZLView mCurrentView;
	public FBView BookTextView;
	public FBView FootnoteView;
	private String myFootnoteModelId;

	public ZLTextStyleCollection myTextStyleCollection;

	private volatile ZLTextPosition myStoredPosition;
	private volatile Book myStoredPositionBook;

	public ZLAndroidWidget(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ZLAndroidWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ZLAndroidWidget(Context context) {
		super(context);
		init();
	}

	private void init() {
		// next line prevent ignoring first onKeyDown DPad event
		// after any dialog was closed
		setFocusableInTouchMode(true);
		setDrawingCacheEnabled(false);
		setOnLongClickListener(this);

		mApp = new FBReaderApp(getContext(), this);
		BookTextView = new FBView(mApp);
		FootnoteView = new FBView(mApp);
		setCurrentView(BookTextView);

		myTextStyleCollection = new ZLTextStyleCollection(mApp, getContext(), "Base");
	}

	public FBReaderApp getApp() {
		return mApp;
	}

	public void clearTextCaches() {
		BookTextView.clearCaches();
		FootnoteView.clearCaches();
	}

	public final ZLView getCurrentView() {
		return mCurrentView;
	}

	public void setCurrentView(ZLView pView) {
		mCurrentView = pView;
	}

	private Book createBookForFile(ZLFile file, int bookCode) {
		if (file == null) {
			return null;
		}

		String path = file.getPath();
		Book book = new Book(bookCode, path, null, null, null);
		try {
			BookUtil.readMetainfo(book, PluginCollection.Instance().getPlugin(ZLFile.createFileByPath(path)));
		} catch (BookReadingException pE) {
			pE.printStackTrace();
		}

		return book;
	}

	public Book getCurrentBook() {
		return Model != null ? Model.Book : null;
	}

	public void reloadBook() {
		final Book book = getCurrentBook();
		if (book != null) {
			openBookInternal(book, null, true);
		}
	}

	public void openBook(String path) {
		openBook(path, -1);
	}

	public void openBook(String path, int bookCode) {
		Book book = createBookForFile(ZLFile.createFileByPath(path), bookCode);
		if (book != null) {
			ZLFile file = BookUtil.fileByBook(book);
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}
				Toast.makeText(getContext(), "文件未找到: " + file.getPath(), Toast.LENGTH_LONG).show();
				book = null;
			}
		}
		openBook(book, null);
		AndroidFontUtil.clearFontCache();
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

	private synchronized void openBookInternal(final Book book, Bookmark bookmark, boolean force) {
		if (!force && Model != null) {
			if (bookmark != null) {
				gotoBookmark(bookmark, false);
			}
			return;
		}

		storePosition();

		BookTextView.setModel(null);
		BookTextView.setModel(null);
		clearTextCaches();
		Model = null;
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
			Model = BookModel.createModel(getContext(), book, plugin);
			ZLTextHyphenator.Instance().load(getContext(), book.getLanguage());
			BookTextView.setModel(Model.Book.getTextModel());
			setBookmarkHighlightings(BookTextView);
			gotoStoredPosition();
			if (bookmark == null) {
				setCurrentView(BookTextView);
			} else {
				gotoBookmark(bookmark, false);
			}
		} catch (BookReadingException e) {
			e.printStackTrace();
		}

		reset();
		repaint();

		for (FileEncryptionInfo info : plugin.readEncryptionInfos(book)) {
			if (info != null && !EncryptionMethod.isSupported(info.Method)) {
				Toast.makeText(getContext(), "FBReader不支持文件 "+ book.getPath() +" 加密方法. 某些页面不可读", Toast.LENGTH_LONG).show();
				break;
			}
		}
	}

	private void gotoBookmark(Bookmark bookmark, boolean exactly) {
		if (exactly) {
			BookTextView.gotoPosition(bookmark.startPosition);
		} else {
			BookTextView.gotoHighlighting(new BookmarkHighlighting(BookTextView, bookmark)
			);
		}

		setCurrentView(BookTextView);
		repaint();
		storePosition();
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
		if (null != myStoredPosition) {
			BookState state = new BookState((long) myStoredPositionBook.getPath().hashCode(),
					myStoredPosition, new Date());
			BooksDaoHelper.getInstance(getContext()).getBookStateDao()
					.insertOrReplace(state);
		}
		myStoredPositionBook.setProgress(progress);
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

	private ZLTextPosition getStoredPosition(Book book) {
		BookState state = BooksDaoHelper.getInstance(getContext()).getBookStateDao()
				.load((long) book.getPath().hashCode());
		if (null != state) {
			return state.position;
		}
		return null;
	}

	public Bookmark createBookmark(int maxChars, boolean visible) {
		final FBView view = (FBView) mCurrentView;
		final ZLTextWordCursor cursor = view.getStartCursor();

		if (cursor.isNull()) {
			return null;
		}

		return new Bookmark((long) Model.Book.getPath().hashCode(), new AutoTextSnippet(cursor, maxChars), visible);
	}

	public Bookmark addSelectionBookmark() {
		final FBView fbView = (FBView) mCurrentView;
		final TextSnippet snippet = fbView.getSelectedSnippet();
		if (snippet == null) {
			return null;
		}

		final Bookmark bookmark = new Bookmark((long) Model.Book.getPath().hashCode(), snippet, true);
		BooksDaoHelper.getInstance(getContext()).getBookmarksDao().insertOrReplace(bookmark);
		fbView.clearSelection();

		if (Model != null) {
			if (BookTextView.getModel() != null) {
				setBookmarkHighlightings(BookTextView);
			}
			if (FootnoteView.getModel() != null && myFootnoteModelId != null) {
				setBookmarkHighlightings(FootnoteView);
			}
		}

		return bookmark;
	}

	private void setBookmarkHighlightings(ZLTextView view) {
		view.removeHighlightings(BookmarkHighlighting.class);
		List<Bookmark> bookmarks = BooksDaoHelper.getInstance(getContext()).getBookmarksDao().loadAll();
		for (Bookmark b : bookmarks) {
			if (b.endPosition == null) {
				b.findEnd(view);
			}
			view.addHighlighting(new BookmarkHighlighting(view, b));
		}
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
				new ZLTextWordCursor(new ZLTextParagraphCursor(getContext(), model, label.ParagraphIndex));
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
					setCurrentView(BookTextView);
				} else {
					setFootnoteModel(label.ModelId);
					setCurrentView(FootnoteView);
					FootnoteView.gotoPosition(label.ParagraphIndex, 0, 0);
				}
				repaint();
				storePosition();
			}
		}
	}

	private void setFootnoteModel(String modelId) {
		final ZLTextModel model = Model.Book.getFootnoteModel(modelId);
		FootnoteView.setModel(model);
		if (model != null) {
			myFootnoteModelId = modelId;
			setBookmarkHighlightings(FootnoteView);
		}
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

	/**
	 * 获取当前位置
	 * @return
	 */
	public ZLTextWordCursor getCurrentPosition() {
		return ((FBView) mCurrentView).getStartCursor();
	}

	/**
	 * 前往某页, 第一页为 1
	 * @param page
	 */
	public void gotoPage(int page) {
		final ZLTextView view = (ZLTextView) mCurrentView;
		if (page == 1) {
			view.gotoHome();
		} else {
			view.gotoPage(page);
		}
		reset();
		repaint();
	}

	/**
	 * 获取分页信息
	 * @return
	 */
	public PagePosition pagePosition() {
		return ((FBView) mCurrentView).pagePosition();
	}

	/**
	 * 前往某位置
	 * @param position
	 */
	public void gotoPosition(ZLTextWordCursor position) {
		((FBView) mCurrentView).gotoPosition(position);
		reset();
		repaint();
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
		repaint();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mApp.stopTimer();
		storePosition();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		getAnimationProvider().terminate();
		if (myScreenIsTouched) {
			myScreenIsTouched = false;
			mCurrentView.onScrollingFinished(ZLView.PageIndex.current);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

//		final int w = getWidth();
//		final int h = getMainAreaHeight();

		myBitmapManager.setSize(getWidth(), getMainAreaHeight());
		if (getAnimationProvider().inProgress()) {
			onDrawInScrolling(canvas);
		} else {
			onDrawStatic(canvas);
		}
	}

	private AnimationProvider myAnimationProvider;
	private ZLView.Animation myAnimationType;
	private AnimationProvider getAnimationProvider() {
		final ZLView.Animation type = mCurrentView.getAnimationType();
		if (myAnimationProvider == null || myAnimationType != type) {
			myAnimationType = type;
			switch (type) {
				case none:
					myAnimationProvider = new NoneAnimationProvider(mApp, myBitmapManager);
					break;
				case curl:
					myAnimationProvider = new CurlAnimationProvider(mApp, myBitmapManager);
					break;
				case slide:
					myAnimationProvider = new SlideAnimationProvider(mApp, myBitmapManager);
					break;
				case slideOldStyle:
					myAnimationProvider = new SlideOldStyleAnimationProvider(mApp, myBitmapManager);
					break;
				case shift:
					myAnimationProvider = new ShiftAnimationProvider(mApp, myBitmapManager);
					break;
			}
		}
		return myAnimationProvider;
	}

	private void onDrawInScrolling(Canvas canvas) {
		final AnimationProvider animator = getAnimationProvider();
		final AnimationProvider.Mode oldMode = animator.getMode();
		animator.doStep();
		if (animator.inProgress()) {
			animator.draw(canvas);
			if (animator.getMode().Auto) {
				postInvalidate();
			}
			drawFooter(canvas, animator);
		} else {
			switch (oldMode) {
				case AnimatedScrollingForward:
				{
					final ZLView.PageIndex index = animator.getPageToScrollTo();
					myBitmapManager.shift(index == ZLView.PageIndex.next);
					mCurrentView.onScrollingFinished(index);
					break;
				}
				case AnimatedScrollingBackward:
					mCurrentView.onScrollingFinished(ZLView.PageIndex.current);
					break;
			}
			onDrawStatic(canvas);
		}
	}

	@Override
	public void reset() {
		myBitmapManager.reset();
	}

	@Override
	public void repaint() {
		postInvalidate();
	}

	@Override
	public void startManualScrolling(int x, int y, ZLView.Direction direction) {
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
		animator.startManualScrolling(x, y);
	}

	@Override
	public void scrollManuallyTo(int x, int y) {
		final AnimationProvider animator = getAnimationProvider();
		if (mCurrentView.canScroll(animator.getPageToScrollTo(x, y))) {
			animator.scrollTo(x, y);
			postInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, int x, int y, ZLView.Direction direction, int speed) {
		if (pageIndex == ZLView.PageIndex.current || !mCurrentView.canScroll(pageIndex)) {
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
		animator.startAnimatedScrolling(pageIndex, x, y, speed);
		if (animator.getMode().Auto) {
			postInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(ZLView.PageIndex pageIndex, ZLView.Direction direction, int speed) {
		if (pageIndex == ZLView.PageIndex.current || !mCurrentView.canScroll(pageIndex)) {
			return;
		}
		final AnimationProvider animator = getAnimationProvider();
		animator.setup(direction, getWidth(), getMainAreaHeight(), myColorLevel);
		animator.startAnimatedScrolling(pageIndex, null, null, speed);
		if (animator.getMode().Auto) {
			postInvalidate();
		}
	}

	@Override
	public void startAnimatedScrolling(int x, int y, int speed) {
		final AnimationProvider animator = getAnimationProvider();
		if (!mCurrentView.canScroll(animator.getPageToScrollTo(x, y))) {
			animator.terminate();
			return;
		}
		animator.startAnimatedScrolling(x, y, speed);
		postInvalidate();
	}

	void drawOnBitmap(Bitmap bitmap, ZLView.PageIndex index) {
		if (mCurrentView == null) {
			return;
		}

		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(getContext(),
			new Canvas(bitmap),
			new ZLAndroidPaintContext.Geometry(
				getWidth(),
				getHeight(),
				getWidth(),
				getMainAreaHeight(),
				0,
				0
			), mCurrentView.isScrollbarShown() ? getVerticalScrollbarWidth() : 0);
		mCurrentView.paint(context, index);
	}

	private void drawFooter(Canvas canvas, AnimationProvider animator) {
		final ZLView.FooterArea footer = mCurrentView.getFooterArea();

		if (footer == null) {
			myFooterBitmap = null;
			return;
		}

		if (myFooterBitmap != null &&
			(myFooterBitmap.getWidth() != getWidth() ||
			 myFooterBitmap.getHeight() != footer.getHeight())) {
			myFooterBitmap = null;
		}
		if (myFooterBitmap == null) {
			myFooterBitmap = Bitmap.createBitmap(getWidth(), footer.getHeight(), Bitmap.Config.RGB_565);
		}
		final ZLAndroidPaintContext context = new ZLAndroidPaintContext(getContext(),
			new Canvas(myFooterBitmap),
			new ZLAndroidPaintContext.Geometry(
				getWidth(),
				getHeight(),
				getWidth(),
				footer.getHeight(),
				0,
				getMainAreaHeight()
			),
			mCurrentView.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
		);
		footer.paint(context);
		final int voffset = getHeight() - footer.getHeight();
		if (animator != null) {
			animator.drawFooterBitmap(canvas, myFooterBitmap, voffset);
		} else {
			canvas.drawBitmap(myFooterBitmap, 0, voffset, myPaint);
		}
	}

	private void onDrawStatic(final Canvas canvas) {
		canvas.drawBitmap(myBitmapManager.getBitmap(ZLView.PageIndex.current), 0, 0, myPaint);
		drawFooter(canvas, null);

		Observable.just(canvas)
		.observeOn(AndroidSchedulers.mainThread())
		.subscribe(new Action1<Canvas>() {
			@Override
			public void call(final Canvas pCanvas) {
				final ZLAndroidPaintContext context = new ZLAndroidPaintContext(getContext(),
						canvas, new ZLAndroidPaintContext.Geometry(getWidth(), getHeight(), getWidth(),
								getMainAreaHeight(), 0, 0),
						mCurrentView.isScrollbarShown() ? getVerticalScrollbarWidth() : 0
				);
				mCurrentView.preparePage(context, ZLView.PageIndex.next);
			}
		});
	}

	private class LongClickRunnable implements Runnable {
		@Override
		public void run() {
			if (performLongClick()) {
				myLongClickPerformed = true;
			}
		}
	}
	private volatile LongClickRunnable myPendingLongClickRunnable;
	private volatile boolean myLongClickPerformed;

	private void postLongClickRunnable() {
		myLongClickPerformed = false;
		myPendingPress = false;
		if (myPendingLongClickRunnable == null) {
			myPendingLongClickRunnable = new LongClickRunnable();
		}
		postDelayed(myPendingLongClickRunnable, 2 * ViewConfiguration.getLongPressTimeout());
	}

	private class ShortClickRunnable implements Runnable {
		@Override
		public void run() {
			mCurrentView.onFingerSingleTap(myPressedX, myPressedY);
			myPendingPress = false;
			myPendingShortClickRunnable = null;
		}
	}
	private volatile ShortClickRunnable myPendingShortClickRunnable;

	private volatile boolean myPendingPress;
	private volatile boolean myPendingDoubleTap;
	private int myPressedX, myPressedY;
	private boolean myScreenIsTouched;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int)event.getX();
		int y = (int)event.getY();

		switch (event.getAction()) {
			case MotionEvent.ACTION_CANCEL:
				myPendingDoubleTap = false;
				myPendingPress = false;
				myScreenIsTouched = false;
				myLongClickPerformed = false;
				if (myPendingShortClickRunnable != null) {
					removeCallbacks(myPendingShortClickRunnable);
					myPendingShortClickRunnable = null;
				}
				if (myPendingLongClickRunnable != null) {
					removeCallbacks(myPendingLongClickRunnable);
					myPendingLongClickRunnable = null;
				}
				mCurrentView.onFingerEventCancelled();
				break;
			case MotionEvent.ACTION_UP:
				if (myPendingDoubleTap) {
					mCurrentView.onFingerDoubleTap(x, y);
				} else if (myLongClickPerformed) {
					mCurrentView.onFingerReleaseAfterLongPress(x, y);
				} else {
					if (myPendingLongClickRunnable != null) {
						removeCallbacks(myPendingLongClickRunnable);
						myPendingLongClickRunnable = null;
					}
					if (myPendingPress) {
						if (mCurrentView.isDoubleTapSupported()) {
							if (myPendingShortClickRunnable == null) {
								myPendingShortClickRunnable = new ShortClickRunnable();
							}
							postDelayed(myPendingShortClickRunnable, ViewConfiguration.getDoubleTapTimeout());
						} else {
							mCurrentView.onFingerSingleTap(x, y);
						}
					} else {
						mCurrentView.onFingerRelease(x, y);
					}
				}
				myPendingDoubleTap = false;
				myPendingPress = false;
				myScreenIsTouched = false;
				break;
			case MotionEvent.ACTION_DOWN:
				if (myPendingShortClickRunnable != null) {
					removeCallbacks(myPendingShortClickRunnable);
					myPendingShortClickRunnable = null;
					myPendingDoubleTap = true;
				} else {
					postLongClickRunnable();
					myPendingPress = true;
				}
				myScreenIsTouched = true;
				myPressedX = x;
				myPressedY = y;
				break;
			case MotionEvent.ACTION_MOVE:
			{
				final int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
				final boolean isAMove =
					Math.abs(myPressedX - x) > slop || Math.abs(myPressedY - y) > slop;
				if (isAMove) {
					myPendingDoubleTap = false;
				}
				if (myLongClickPerformed) {
					mCurrentView.onFingerMoveAfterLongPress(x, y);
				} else {
					if (myPendingPress) {
						if (isAMove) {
							if (myPendingShortClickRunnable != null) {
								removeCallbacks(myPendingShortClickRunnable);
								myPendingShortClickRunnable = null;
							}
							if (myPendingLongClickRunnable != null) {
								removeCallbacks(myPendingLongClickRunnable);
							}
							mCurrentView.onFingerPress(myPressedX, myPressedY);
							myPendingPress = false;
						}
					}
					if (!myPendingPress) {
						mCurrentView.onFingerMove(x, y);
					}
				}
				break;
			}
		}

		return true;
	}

	@Override
	public boolean onLongClick(View v) {
		return mCurrentView.onFingerLongPress(myPressedX, myPressedY);
	}

	@Override
	protected int computeVerticalScrollExtent() {
		if (!mCurrentView.isScrollbarShown()) {
			return 0;
		}
		final AnimationProvider animator = getAnimationProvider();
		if (animator.inProgress()) {
			final int from = mCurrentView.getScrollbarThumbLength(ZLView.PageIndex.current);
			final int to = mCurrentView.getScrollbarThumbLength(animator.getPageToScrollTo());
			final int percent = animator.getScrolledPercent();
			return (from * (100 - percent) + to * percent) / 100;
		} else {
			return mCurrentView.getScrollbarThumbLength(ZLView.PageIndex.current);
		}
	}

	@Override
	protected int computeVerticalScrollOffset() {
		if (!mCurrentView.isScrollbarShown()) {
			return 0;
		}
		final AnimationProvider animator = getAnimationProvider();
		if (animator.inProgress()) {
			final int from = mCurrentView.getScrollbarThumbPosition(ZLView.PageIndex.current);
			final int to = mCurrentView.getScrollbarThumbPosition(animator.getPageToScrollTo());
			final int percent = animator.getScrolledPercent();
			return (from * (100 - percent) + to * percent) / 100;
		} else {
			return mCurrentView.getScrollbarThumbPosition(ZLView.PageIndex.current);
		}
	}

	@Override
	protected int computeVerticalScrollRange() {
		if (!mCurrentView.isScrollbarShown()) {
			return 0;
		}
		return mCurrentView.getScrollbarFullSize();
	}

	private int getMainAreaHeight() {
		final ZLView.FooterArea footer = mCurrentView.getFooterArea();
		return footer != null ? getHeight() - footer.getHeight() : getHeight();
	}
}
