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

import android.support.annotation.ColorInt;

public class HighlightingStyle {

  public final int Id;
  public final long LastUpdateTimestamp;

  private String myName;
  @ColorInt
  private Integer myBackgroundColor;
  @ColorInt
  private Integer myForegroundColor;

  HighlightingStyle(int id, long timestamp, String name, @ColorInt Integer bgColor,
      @ColorInt Integer fgColor) {
    Id = id;
    LastUpdateTimestamp = timestamp;
    myName = name;
    myBackgroundColor = bgColor;
    myForegroundColor = fgColor;
  }

  public String getNameOrNull() {
    return "".equals(myName) ? null : myName;
  }

  void setName(String name) {
    myName = name;
  }

  @ColorInt
  public Integer getBackgroundColor() {
    return myBackgroundColor;
  }

  public void setBackgroundColor(@ColorInt Integer bgColor) {
    myBackgroundColor = bgColor;
  }

  @ColorInt
  public Integer getForegroundColor() {
    return myForegroundColor;
  }

  public void setForegroundColor(@ColorInt int fgColor) {
    myForegroundColor = fgColor;
  }
}
