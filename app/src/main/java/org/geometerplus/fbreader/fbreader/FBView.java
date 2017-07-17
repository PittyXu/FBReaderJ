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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.ColorInt;
import android.text.TextUtils;

import org.geometerplus.android.fbreader.config.ColorProfile;
import org.geometerplus.android.fbreader.config.ImagePreferences;
import org.geometerplus.android.fbreader.config.ImagePreferences.TapActionEnum;
import org.geometerplus.android.fbreader.config.MiscPreferences;
import org.geometerplus.android.fbreader.config.MiscPreferences.WordTappingActionEnum;
import org.geometerplus.android.fbreader.config.PageTurningPreferences;
import org.geometerplus.android.fbreader.config.PageTurningPreferences.FingerScrollingType;
import org.geometerplus.android.fbreader.config.ViewPreferences;
import org.geometerplus.android.fbreader.dao.Bookmark;
import org.geometerplus.android.fbreader.listeners.SelectionListener;
import org.geometerplus.android.fbreader.listeners.onElementClickListener;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.TapZoneMap.Tap;
import org.geometerplus.fbreader.util.FixedTextSnippet;
import org.geometerplus.fbreader.util.TextSnippet;
import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.view.SelectionCursor;
import org.geometerplus.zlibrary.core.view.SelectionCursor.Which;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.view.ZLTextHighlighting;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlinkRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextImageElement;
import org.geometerplus.zlibrary.text.view.ZLTextImageRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextVideoRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public final class FBView extends ZLTextView {
	private final FBReaderApp myReader;
	private SelectionListener mSelectionListener;
	private onElementClickListener mHyperlinkListener;

	public FBView(FBReaderApp reader) {
		super(reader.getContext(), reader);
		myReader = reader;
	}

	public void setModel(ZLTextModel model) {
		super.setModel(model);
		if (myFooter != null) {
			myFooter.resetTOCMarks();
		}
	}

	private TapZoneMap myZoneMap;

	private TapZoneMap getZoneMap() {
		String id = PageTurningPreferences.getTapZoneMap(myReader.getContext());
		if (TextUtils.isEmpty(id)) {
			id = PageTurningPreferences.getHorizontal(myReader.getContext()) ? "right_to_left" : "up";
		}
		if (myZoneMap == null || !id.equals(myZoneMap.Name)) {
			myZoneMap = TapZoneMap.zoneMap(myReader.getContext(), id);
		}
		return myZoneMap;
	}

	private void onFingerSingleTapLastResort(int x, int y) {
		Tap tap = isDoubleTapSupported() ? TapZoneMap.Tap.singleNotDoubleTap : TapZoneMap.Tap.singleTap;
		applyAction(x, y, tap);
	}

	@Override
	public void onFingerSingleTap(int x, int y) {
		if (!isSelectionEmpty()) {
			clearSelection();
			if (null != mSelectionListener) {
				mSelectionListener.onCancelled();
			}
			return;
		}
		final ZLTextRegion hyperlinkRegion = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.HyperlinkFilter);
		if (hyperlinkRegion != null) {
			outlineRegion(hyperlinkRegion);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			processHyperlink();
			return;
		}

		final ZLTextRegion videoRegion = findRegion(x, y, 0, ZLTextRegion.VideoFilter);
		if (videoRegion != null) {
			outlineRegion(videoRegion);
			myReader.getViewWidget().reset();
			myReader.getViewWidget().repaint();
			if (null != mHyperlinkListener) {
				mHyperlinkListener.onVideoClick(((ZLTextVideoRegionSoul)videoRegion.getSoul()).VideoElement);
			}
			return;
		}

		final ZLTextHighlighting highlighting = findHighlighting(x, y, maxSelectionDistance());
		if (highlighting instanceof BookmarkHighlighting) {
			Bookmark bookmark = ((BookmarkHighlighting)highlighting).bookmark;
			markBookmarkSelection(bookmark);
			return;
		}

		onFingerSingleTapLastResort(x, y);
	}

	@Override
	public boolean isDoubleTapSupported() {
		return MiscPreferences.getEnableDoubleTap(myReader.getContext());
	}

	@Override
	public void onFingerDoubleTap(int x, int y) {
		applyAction(x, y, Tap.doubleTap);
	}

	public void applyAction(int x, int y, Tap tap) {
		TapZoneMap map = getZoneMap();
		String actionId = map.getActionByCoordinates(x, y, getContextWidth(), getContextHeight(), tap);
		boolean horizontal = PageTurningPreferences.getHorizontal(myReader.getContext());
		Direction dir = horizontal ? Direction.rightToLeft : Direction.up;
		int speed = PageTurningPreferences.getAnimationSpeed(myReader.getContext());
		if (TapZoneMap.TURN_PAGE_FORWARD.equals(actionId)) {
			myReader.getViewWidget().startAnimatedScrolling(PageIndex.next, x, y, dir, speed);
		} else if (TapZoneMap.TURN_PAGE_BACK.equals(actionId)) {
			myReader.getViewWidget().startAnimatedScrolling(PageIndex.previous, dir, speed);
		}
	}

	@Override
	public void onFingerPress(int x, int y) {
		final float maxDist = myReader.getDisplayDPI() / 4;
		final SelectionCursor.Which cursor = findSelectionCursor(x, y, maxDist * maxDist);
		if (cursor != null) {
			moveSelectionCursorTo(cursor, x, y);
			return;
		}

		startManualScrolling(x, y);
	}

	private boolean isFlickScrollingEnabled() {
		final FingerScrollingType fingerScrolling = FingerScrollingType.values()[PageTurningPreferences.getFingerScrolling(myReader.getContext())];
		return fingerScrolling == FingerScrollingType.byFlick
						|| fingerScrolling == FingerScrollingType.byTapAndFlick;
	}

	private void startManualScrolling(int x, int y) {
		if (!isFlickScrollingEnabled()) {
			return;
		}

		final boolean horizontal = PageTurningPreferences.getHorizontal(myReader.getContext());
		final Direction direction = horizontal ? Direction.rightToLeft : Direction.up;
		myReader.getViewWidget().startManualScrolling(x, y, direction);
	}

	@Override
	public void onFingerMove(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			moveSelectionCursorTo(cursor, x, y);
			return;
		}

		synchronized (this) {
			if (isFlickScrollingEnabled()) {
				myReader.getViewWidget().scrollManuallyTo(x, y);
			}
		}
	}

	@Override
	public void onFingerRelease(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			releaseSelectionCursor();
		} else if (isFlickScrollingEnabled()) {
			myReader.getViewWidget().startAnimatedScrolling(
				x, y, PageTurningPreferences.getAnimationSpeed(myReader.getContext()));
		}
	}

	@Override
	public boolean onFingerLongPress(int x, int y) {
		final ZLTextRegion region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();
			boolean doSelectRegion = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				int action = MiscPreferences.getWordTappingAction(myReader.getContext());
				if (WordTappingActionEnum.startSelecting.ordinal() == action) {
					if (null != mSelectionListener) {
						mSelectionListener.onStarted(x, y);
					}
					initSelection(x, y);
					final SelectionCursor.Which cursor = findSelectionCursor(x, y);
					if (cursor != null) {
						moveSelectionCursorTo(cursor, x, y);
					}
					return true;
				} else if (action == WordTappingActionEnum.selectSingleWord.ordinal() ||
						action == WordTappingActionEnum.openDictionary.ordinal()) {
					doSelectRegion = true;
				}
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doSelectRegion = ImagePreferences.TapActionEnum.values()[ImagePreferences.getTappingAction(myReader.getContext())] != TapActionEnum.doNothing;
			} else if (soul instanceof ZLTextHyperlinkRegionSoul) {
				doSelectRegion = true;
			}

			if (doSelectRegion) {
				outlineRegion(region);
				myReader.getViewWidget().reset();
				myReader.getViewWidget().repaint();
				return true;
			}
		}
		return false;
	}

	@Override
	public void onFingerMoveAfterLongPress(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			moveSelectionCursorTo(cursor, x, y);
			return;
		}

		ZLTextRegion region = getOutlinedRegion();
		if (region != null) {
			ZLTextRegion.Soul soul = region.getSoul();
			if (soul instanceof ZLTextHyperlinkRegionSoul ||
				soul instanceof ZLTextWordRegionSoul) {
				int action = MiscPreferences.getWordTappingAction(myReader.getContext());

				if (action != WordTappingActionEnum.doNothing.ordinal()) {
					region = findRegion(x, y, maxSelectionDistance(), ZLTextRegion.AnyRegionFilter);
					if (region != null) {
						soul = region.getSoul();
						if (soul instanceof ZLTextHyperlinkRegionSoul
							 || soul instanceof ZLTextWordRegionSoul) {
							outlineRegion(region);
							myReader.getViewWidget().reset();
							myReader.getViewWidget().repaint();
						}
					}
				}
			}
		}
	}

	@Override
	public void onFingerReleaseAfterLongPress(int x, int y) {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			releaseSelectionCursor();
			return;
		}

		final ZLTextRegion region = getOutlinedRegion();
		if (region != null) {
			final ZLTextRegion.Soul soul = region.getSoul();

			boolean doRunAction = false;
			if (soul instanceof ZLTextWordRegionSoul) {
				doRunAction = MiscPreferences.getWordTappingAction(myReader.getContext()) ==
					WordTappingActionEnum.openDictionary.ordinal();
			} else if (soul instanceof ZLTextImageRegionSoul) {
				doRunAction = ImagePreferences.TapActionEnum.values()[ImagePreferences.getTappingAction(myReader.getContext())] == TapActionEnum.openImageView;
			}

			if (doRunAction) {
				processHyperlink();
			}
		}
	}

	@Override
	public void onFingerEventCancelled() {
		final SelectionCursor.Which cursor = getSelectionCursorInMovement();
		if (cursor != null) {
			releaseSelectionCursor();
		}
	}

	@Override
	public ZLTextStyleCollection getTextStyleCollection() {
		return myReader.myTextStyleCollection;
	}

	@Override
	public ImageFitting getImageFitting() {
		int fit = ImagePreferences.getFitToScreen(myReader.getContext());
		return ImageFitting.values()[fit];
	}

	@Override
	public int getLeftMargin() {
		return ViewPreferences.getLeftMargin(myReader.getContext());
	}

	@Override
	public int getRightMargin() {
		return ViewPreferences.getRightMargin(myReader.getContext());
	}

	@Override
	public int getTopMargin() {
		return ViewPreferences.getTopMargin(myReader.getContext());
	}

	@Override
	public int getBottomMargin() {
		return ViewPreferences.getBottomMargin(myReader.getContext());
	}

	@Override
	public int getSpaceBetweenColumns() {
		return ViewPreferences.getSpaceBetweenColumns(myReader.getContext());
	}

	@Override
	public boolean twoColumnView() {
		return getContextHeight() <= getContextWidth() && ViewPreferences.getTwoColumnView(myReader.getContext());
	}

	@Override
	public Bitmap getWallpaperFile() {
		final String filePath = ViewPreferences.getColorProfile(myReader.getContext()).wallpaper;
		if ("".equals(filePath)) {
			return null;
		}

		try {
			return BitmapFactory.decodeStream(myReader.getContext().getAssets().open(filePath));
		} catch (IOException pE) {
			pE.printStackTrace();
		}
		return null;
	}

	@Override
	public ZLPaintContext.FillMode getFillMode() {
		return ZLPaintContext.FillMode.tileMirror;
	}

	@ColorInt
	@Override
	public Integer getBackgroundColor() {
		return ViewPreferences.getColorProfile(myReader.getContext()).backgroundColor;
	}

	@ColorInt
	@Override
	public Integer getSelectionBackgroundColor() {
		return ViewPreferences.getColorProfile(myReader.getContext()).selectionBackgroundColor;
	}

	@ColorInt
	@Override
	public Integer getSelectionForegroundColor() {
		return ViewPreferences.getColorProfile(myReader.getContext()).selectionForegroundColor;
	}

	@ColorInt
	@Override
	public Integer getTextColor(ZLTextHyperlink hyperlink) {
		final ColorProfile profile = ViewPreferences.getColorProfile(myReader.getContext());
		switch (hyperlink.Type) {
			default:
			case FBHyperlinkType.NONE:
				return profile.regularTextColor;
			case FBHyperlinkType.INTERNAL:
			case FBHyperlinkType.FOOTNOTE:
				return myReader.getCurrentBook().isHyperlinkVisited(hyperlink.Id)
					? profile.visitedHyperlinkTextColor
					: profile.hyperlinkTextColor;
			case FBHyperlinkType.EXTERNAL:
				return profile.hyperlinkTextColor;
		}
	}

	@ColorInt
	@Override
	public Integer getHighlightingBackgroundColor() {
		return ViewPreferences.getColorProfile(myReader.getContext()).highlightingBackgroundColor;
	}

	@ColorInt
	@Override
	public Integer getHighlightingForegroundColor() {
		return ViewPreferences.getColorProfile(myReader.getContext()).highlightingForegroundColor;
	}

	public void processHyperlink() {
		final ZLTextRegion region = getOutlinedRegion();
		if (region == null) {
			return;
		}

		final ZLTextRegion.Soul soul = region.getSoul();
		if (soul instanceof ZLTextHyperlinkRegionSoul) {
			hideOutline();
			myReader.getViewWidget().repaint();
			final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul) soul).Hyperlink;
			switch (hyperlink.Type) {
				case FBHyperlinkType.EXTERNAL:
					if (null != mHyperlinkListener) {
						mHyperlinkListener.onHyperlinkExternalClick(hyperlink);
					}
					break;
				case FBHyperlinkType.INTERNAL:
					if (null != mHyperlinkListener) {
						mHyperlinkListener.onHyperlinkInternalClick(hyperlink);
					}
					break;
				case FBHyperlinkType.FOOTNOTE: {
					if (null != mHyperlinkListener) {
						mHyperlinkListener.onHyperlinkFootnoteClick(hyperlink);
					}
					break;
				}
			}
		} else if (soul instanceof ZLTextImageRegionSoul) {
			hideOutline();
			myReader.getViewWidget().repaint();
			final ZLTextImageElement element = ((ZLTextImageRegionSoul) soul).ImageElement;
			if (null != mHyperlinkListener) {
				mHyperlinkListener.onImageClick(element);
			}
		} else if (soul instanceof ZLTextWordRegionSoul) {
      if (null != mHyperlinkListener) {
        mHyperlinkListener.onWordClick(((ZLTextWordRegionSoul)soul).Word);
      }
		}
	}

	private abstract class Footer implements FooterArea {
		private Runnable UpdateTask = new Runnable() {
			public void run() {
				myReader.getViewWidget().repaint();
			}
		};

		protected ArrayList<TOCTree> myTOCMarks;
		private int myMaxTOCMarksNumber = -1;

		public int getHeight() {
			return ViewPreferences.getFooterHeight(myReader.getContext());
		}

		public synchronized void resetTOCMarks() {
			myTOCMarks = null;
		}

		protected synchronized void updateTOCMarks(BookModel model, int maxNumber) {
			if (myTOCMarks != null && myMaxTOCMarksNumber == maxNumber) {
				return;
			}

			myTOCMarks = new ArrayList<>();
			myMaxTOCMarksNumber = maxNumber;

			TOCTree toc = model.Book.TOCTree;
			if (toc == null) {
				return;
			}
			int maxLevel = Integer.MAX_VALUE;
			if (toc.getSize() >= maxNumber) {
				final int[] sizes = new int[10];
				for (TOCTree tocItem : toc) {
					if (tocItem.Level < 10) {
						++sizes[tocItem.Level];
					}
				}
				for (int i = 1; i < sizes.length; ++i) {
					sizes[i] += sizes[i - 1];
				}
				for (maxLevel = sizes.length - 1; maxLevel >= 0; --maxLevel) {
					if (sizes[maxLevel] < maxNumber) {
						break;
					}
				}
			}
			for (TOCTree tocItem : toc.allSubtrees(maxLevel)) {
				myTOCMarks.add(tocItem);
			}
		}

		protected String buildInfoString(PagePosition pagePosition, String separator) {
			final StringBuilder info = new StringBuilder();

			if (ViewPreferences.showFooterProgressAsPages(myReader.getContext())) {
				maybeAddSeparator(info, separator);
				info.append(pagePosition.Current);
				info.append("/");
				info.append(pagePosition.Total);
			}
			if (ViewPreferences.showFooterProgressAsPercentage(myReader.getContext()) && pagePosition.Total != 0) {
				maybeAddSeparator(info, separator);
				info.append(String.valueOf(100 * pagePosition.Current / pagePosition.Total));
				info.append("%");
			}

			return info.toString();
		}

		private void maybeAddSeparator(StringBuilder info, String separator) {
			if (info.length() > 0) {
				info.append(separator);
			}
		}

		private List<FontEntry> myFontEntry;
		private Map<String,Integer> myHeightMap = new HashMap<>();
		private Map<String,Integer> myCharHeightMap = new HashMap<>();
		protected synchronized int setFont(ZLPaintContext context, int height, boolean bold) {
			final String family = ViewPreferences.getFooterFont(myReader.getContext());
			if (myFontEntry == null || !family.equals(myFontEntry.get(0).Family)) {
				myFontEntry = Collections.singletonList(FontEntry.systemEntry(family));
			}
			final String key = family + (bold ? "N" : "B") + height;
			final Integer cached = myHeightMap.get(key);
			if (cached != null) {
				context.setFont(myReader.getContext(), myFontEntry, cached, bold, false, false, false);
				final Integer charHeight = myCharHeightMap.get(key);
				return charHeight != null ? charHeight : height;
			} else {
				int h = height + 2;
				int charHeight = height;
				final int max = height < 9 ? height - 1 : height - 2;
				for (; h > 5; --h) {
					context.setFont(myReader.getContext(), myFontEntry, h, bold, false, false, false);
					charHeight = context.getCharHeight('H');
					if (charHeight <= max) {
						break;
					}
				}
				myHeightMap.put(key, h);
				myCharHeightMap.put(key, charHeight);
				return charHeight;
			}
		}
	}

	private class FooterNewStyle extends Footer {
		public synchronized void paint(ZLPaintContext context) {
			final ColorProfile cProfile = ViewPreferences.getColorProfile(myReader.getContext());
			context.clear(cProfile.footerNGBackgroundColor);

			final BookModel model = myReader.Model;
			if (model == null) {
				return;
			}

			final Integer textColor = cProfile.footerNGForegroundColor;
			final Integer readColor = cProfile.footerNGForegroundColor;
			final Integer unreadColor = cProfile.footerNGForegroundUnreadColor;

			final int left = getLeftMargin();
			final int right = context.getWidth() - getRightMargin();
			final int height = getHeight();
			final int lineWidth = height <= 12 ? 1 : 2;
			final int charHeight = setFont(context, height, height > 12);

			final PagePosition pagePosition = FBView.this.pagePosition();

			// draw info text
			final String infoString = buildInfoString(pagePosition, "  ");
			final int infoWidth = context.getStringWidth(infoString);
			context.setTextColor(textColor);
			context.drawString(right - infoWidth, (height + charHeight + 1) / 2, infoString);

			// draw gauge
			final int gaugeRight = right - (infoWidth == 0 ? 0 : infoWidth + 10);
			final int gaugeInternalRight =
				left + (int)(1.0 * (gaugeRight - left) * pagePosition.Current / pagePosition.Total + 0.5);
			final int v = height / 2;

			context.setLineWidth(lineWidth);
			context.setLineColor(readColor);
			context.drawLine(left, v, gaugeInternalRight, v);
			if (gaugeInternalRight < gaugeRight) {
				context.setLineColor(unreadColor);
				context.drawLine(gaugeInternalRight + 1, v, gaugeRight, v);
			}

			// draw labels
			if (ViewPreferences.getFooterShowTOCMarks(myReader.getContext())) {
				final TreeSet<Integer> labels = new TreeSet<>();
				labels.add(left);
				labels.add(gaugeRight);
				updateTOCMarks(model, ViewPreferences.getFooterMaxTOCMarks(myReader.getContext()));
				final int fullLength = sizeOfFullText();
				for (TOCTree tocItem : myTOCMarks) {
					TOCTree.Reference reference = tocItem.getReference();
					if (reference != null) {
						final int refCoord = sizeOfTextBeforeParagraph(reference.ParagraphIndex);
						labels.add(left + (int)(1.0 * (gaugeRight - left) * refCoord / fullLength + 0.5));
					}
				}
				for (int l : labels) {
					context.setLineColor(l <= gaugeInternalRight ? readColor : unreadColor);
					context.drawLine(l, v + 3, l, v - lineWidth - 2);
				}
			}
		}
	}

	private Footer myFooter;

	@Override
	public Footer getFooterArea() {
		if (!(myFooter instanceof FooterNewStyle)) {
			if (myFooter != null) {
				myReader.removeTimerTask(myFooter.UpdateTask);
			}
			myFooter = new FooterNewStyle();
			myReader.addTimerTask(myFooter.UpdateTask, 15000);
		}
		return myFooter;
	}

	@Override
	protected void moveSelectionCursorTo(final Which which, final int x, final int y) {
		super.moveSelectionCursorTo(which, x, y);
		if (null != mSelectionListener) {
			mSelectionListener.onChanged(getSelectedSnippet(), getSelectionStartY(), getSelectionEndY());
		}
	}

	@Override
	protected void releaseSelectionCursor() {
		super.releaseSelectionCursor();
		if (getCountOfSelectedWords() > 0) {
			if (null != mSelectionListener) {
				mSelectionListener.onEnded(getSelectedSnippet(), getSelectionStartY(), getSelectionEndY());
			}
		}
	}

	public TextSnippet getSelectedSnippet() {
		final ZLTextPosition start = getSelectionStartPosition();
		final ZLTextPosition end = getSelectionEndPosition();
		if (start == null || end == null) {
			return null;
		}
		final TextBuildTraverser traverser = new TextBuildTraverser(this);
		traverser.traverse(start, end);
		return new FixedTextSnippet(start, end, traverser.getText());
	}

	public int getCountOfSelectedWords() {
		final WordCountTraverser traverser = new WordCountTraverser(this);
		if (!isSelectionEmpty()) {
			traverser.traverse(getSelectionStartPosition(), getSelectionEndPosition());
		}
		return traverser.getCount();
	}

	public static final int SCROLLBAR_SHOW_AS_FOOTER = 3;

	@Override
	public int scrollbarType() {
		return SCROLLBAR_SHOW_AS_FOOTER;
	}

	@Override
	public Animation getAnimationType() {
		return Animation.values()[PageTurningPreferences.getAnimation(myReader.getContext())];
	}

	@Override
	protected ZLPaintContext.ColorAdjustingMode getAdjustingModeForImages() {
		if (ImagePreferences.getMatchBackground(myReader.getContext())) {
			if (ColorProfile.DAY.equals(ViewPreferences.getColorProfileName(myReader.getContext()))) {
				return ZLPaintContext.ColorAdjustingMode.DARKEN_TO_BACKGROUND;
			} else {
				return ZLPaintContext.ColorAdjustingMode.LIGHTEN_TO_BACKGROUND;
			}
		} else {
			return ZLPaintContext.ColorAdjustingMode.NONE;
		}
	}

	@Override
	public synchronized void onScrollingFinished(PageIndex pageIndex) {
		super.onScrollingFinished(pageIndex);
		myReader.storePosition();
	}

	public void setSelectionListener(SelectionListener pListener) {
		mSelectionListener = pListener;
	}

	public void setProcessHyperlinkListener(onElementClickListener pListener) {
		mHyperlinkListener = pListener;
	}
}
