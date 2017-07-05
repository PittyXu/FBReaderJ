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

package org.geometerplus.fbreader;

import android.content.Context;
import android.os.Environment;

import org.geometerplus.android.fbreader.config.DirectoriesPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class Paths {

  public static String cardDirectory() {
    if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
      return Environment.getExternalStorageDirectory().getPath();
    }

    final List<String> dirNames = new LinkedList<String>();
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader("/proc/self/mounts"));
      String line;
      while ((line = reader.readLine()) != null) {
        final String[] parts = line.split("\\s+");
        if (parts.length >= 4 &&
            parts[2].toLowerCase().indexOf("fat") >= 0 &&
            parts[3].indexOf("rw") >= 0) {
          final File fsDir = new File(parts[1]);
          if (fsDir.isDirectory() && fsDir.canWrite()) {
            dirNames.add(fsDir.getPath());
          }
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    } finally {
      try {
        reader.close();
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }

    for (String dir : dirNames) {
      if (dir.toLowerCase().indexOf("media") > 0) {
        return dir;
      }
    }
    if (dirNames.size() > 0) {
      return dirNames.get(0);
    }

    return Environment.getExternalStorageDirectory().getPath();
  }

  private static String defaultBookDirectory() {
    return cardDirectory() + "/Books";
  }

  public static List<String> bookPath(Context pContext) {
    Set<String> path = DirectoriesPreferences.getBookPath(pContext);
    if (null == path || path.isEmpty()) {
      return Collections.singletonList(defaultBookDirectory());
    }
    return new ArrayList<>(path);
  }

  public static List<String> fontPath(Context pContext) {
    Set<String> path = DirectoriesPreferences.getFontPath(pContext);
    if (null == path || path.isEmpty()) {
      return Collections.singletonList(cardDirectory() + "/Fonts");
    }
    return new ArrayList<>(path);
  }

  public static String systemShareDirectory() {
    return "/system/usr/share/FBReader";
  }
}
