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

package org.geometerplus.zlibrary.core.view;

import android.content.Context;

import org.geometerplus.fbreader.fbreader.FBReaderApp;

abstract public class ZLView implements ZLViewEnums {
	public final FBReaderApp Application;
	private ZLPaintContext myViewContext = new DummyPaintContext();
	public final Context context;

	protected ZLView(Context pContext, FBReaderApp application) {
		Application = application;
		context = pContext;
	}

	protected final void setContext(ZLPaintContext context) {
		myViewContext = context;
	}

	public final ZLPaintContext getContext() {
		return myViewContext;
	}

	public final int getContextWidth() {
		return myViewContext.getWidth();
	}

	public final int getContextHeight() {
		return myViewContext.getHeight();
	}

	public interface FooterArea {
		int getHeight();
		void paint(ZLPaintContext context);
	}

	abstract public FooterArea getFooterArea();

	public abstract Animation getAnimationType();

	abstract public void preparePage(ZLPaintContext context, PageIndex pageIndex);
	abstract public void paint(ZLPaintContext context, PageIndex pageIndex);
	abstract public void onScrollingFinished(PageIndex pageIndex);

	public abstract void onFingerPress(int x, int y);
	public abstract void onFingerRelease(int x, int y);
	public abstract void onFingerMove(int x, int y);
	public abstract boolean onFingerLongPress(int x, int y);
	public abstract void onFingerReleaseAfterLongPress(int x, int y);
	public abstract void onFingerMoveAfterLongPress(int x, int y);
	public abstract void onFingerSingleTap(int x, int y);
	public abstract void onFingerDoubleTap(int x, int y);
	public abstract void onFingerEventCancelled();

	public boolean isDoubleTapSupported() {
		return false;
	}

	public abstract boolean isScrollbarShown();
	public abstract int getScrollbarFullSize();
	public abstract int getScrollbarThumbPosition(PageIndex pageIndex);
	public abstract int getScrollbarThumbLength(PageIndex pageIndex);

	public abstract boolean canScroll(PageIndex index);
}
