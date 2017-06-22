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

package org.geometerplus.zlibrary.core.language;

public class Language {

  public static final String ANY_CODE = "any";
  public static final String OTHER_CODE = "other";
  public static final String MULTI_CODE = "multi";
  public static final String SYSTEM_CODE = "system";

  public final String Code;

  public Language(String code) {
    Code = code;
  }

  @Override
  public boolean equals(Object lang) {
    if (this == lang) {
      return true;
    }
    if (!(lang instanceof Language)) {
      return false;
    }
    return Code.equals(((Language) lang).Code);
  }

  @Override
  public int hashCode() {
    return Code.hashCode();
  }
}
