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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;

import org.geometerplus.zlibrary.core.fonts.FontEntry;
import org.geometerplus.zlibrary.core.image.ZLImageData;

import java.util.List;

final class DummyPaintContext extends ZLPaintContext {
	DummyPaintContext() {
	}

	@Override
	public void clear(Bitmap wallpaperFile, FillMode mode) {
	}

	@Override
	public void clear(@ColorInt Integer color) {
	}

	@ColorInt
	@Override
	public Integer getBackgroundColor() {
		return Color.rgb(0, 0, 0);
	}

	@Override
	protected void setFontInternal(Context pContext, List<FontEntry> entries, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrought) {
	}

	@Override
	public void setTextColor(@ColorInt Integer color) {
	}

	@Override
	public void setLineColor(@ColorInt Integer color) {
	}
	@Override
	public void setLineWidth(int width) {
	}

	@Override
	public void setFillColor(@ColorInt Integer color, int alpha) {
	}

	@Override
	public int getWidth() {
		return 1;
	}
	@Override
	public int getHeight() {
		return 1;
	}
	@Override
	protected int getCharHeightInternal(char chr) {
		return 1;
	}
	@Override
	public int getStringWidth(char[] string, int offset, int length) {
		return 1;
	}

	@Override
	protected int getSpaceWidthInternal() {
		return 1;
	}

	@Override
	protected int getStringHeightInternal() {
		return 1;
	}

	@Override
	protected int getDescentInternal() {
		return 1;
	}

	@Override
	public void drawString(int x, int y, char[] string, int offset, int length) {
	}

	@Override
	public Size imageSize(ZLImageData image, Size maxSize, ScalingType scaling) {
		return null;
	}
	@Override
	public void drawImage(int x, int y, ZLImageData image, Size maxSize, ScalingType scaling, ColorAdjustingMode adjustingMode) {
	}

	@Override
	public void drawLine(int x0, int y0, int x1, int y1) {
	}
	@Override
	public void fillRectangle(int x0, int y0, int x1, int y1) {
	}

	@Override
	public void fillPolygon(int[] xs, int[] ys) {
	}
	@Override
	public void drawPolygonalLine(int[] xs, int[] ys) {
	}
	@Override
	public void drawOutline(int[] xs, int[] ys) {
	}

	@Override
	public void fillCircle(int x, int y, int radius) {
	}
}
