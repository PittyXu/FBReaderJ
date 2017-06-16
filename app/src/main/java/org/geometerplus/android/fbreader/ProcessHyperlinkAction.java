/*
 * Copyright (C) 2010-2015 FBReader.ORG Limited <contact@fbreader.org>
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

package org.geometerplus.android.fbreader;

import android.content.ActivityNotFoundException;
import android.content.Intent;

import org.geometerplus.android.fbreader.image.ImageViewActivity;
import org.geometerplus.android.util.OrientationUtil;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.util.AutoTextSnippet;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlinkRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextImageRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;

class ProcessHyperlinkAction extends FBAndroidAction {

  ProcessHyperlinkAction(FBReader baseActivity, FBReaderApp fbreader) {
    super(baseActivity, fbreader);
  }

  @Override
  public boolean isEnabled() {
    return Reader.getTextView().getOutlinedRegion() != null;
  }

  @Override
  protected void run(Object... params) {
    final ZLTextRegion region = Reader.getTextView().getOutlinedRegion();
    if (region == null) {
      return;
    }

    final ZLTextRegion.Soul soul = region.getSoul();
    if (soul instanceof ZLTextHyperlinkRegionSoul) {
      Reader.getTextView().hideOutline();
      Reader.getViewWidget().repaint();
      final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul) soul).Hyperlink;
      switch (hyperlink.Type) {
        case FBHyperlinkType.EXTERNAL:
          openInBrowser(hyperlink.Id);
          break;
        case FBHyperlinkType.INTERNAL:
        case FBHyperlinkType.FOOTNOTE: {
          final AutoTextSnippet snippet = Reader.getFootnoteData(hyperlink.Id);
          if (snippet == null) {
            break;
          }

          Reader.Collection.markHyperlinkAsVisited(Reader.getCurrentBook(), hyperlink.Id);
          switch (Reader.MiscOptions.ShowFootnoteToast.getValue()) {
            default:
            case never:
              break;
            case footnotesOnly:
              break;
            case footnotesAndSuperscripts:
              break;
            case allInternalLinks:
              break;
          }
          Reader.tryOpenFootnote(hyperlink.Id);
          break;
        }
      }
    } else if (soul instanceof ZLTextImageRegionSoul) {
      Reader.getTextView().hideOutline();
      Reader.getViewWidget().repaint();
      final String url = ((ZLTextImageRegionSoul) soul).ImageElement.URL;
      if (url != null) {
        try {
          final Intent intent = new Intent();
          intent.setClass(BaseActivity, ImageViewActivity.class);
          intent.putExtra(ImageViewActivity.URL_KEY, url);
          intent.putExtra(
              ImageViewActivity.BACKGROUND_COLOR_KEY,
              Reader.ImageOptions.ImageViewBackground.getValue().intValue()
          );
          OrientationUtil.startActivity(BaseActivity, intent);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } else if (soul instanceof ZLTextWordRegionSoul) {
    }
  }

  private void openInBrowser(final String url) {
    final Intent intent = new Intent(Intent.ACTION_VIEW);
    new Thread(new Runnable() {
      public void run() {
        BaseActivity.runOnUiThread(new Runnable() {
          public void run() {
            try {
              OrientationUtil.startActivity(BaseActivity, intent);
            } catch (ActivityNotFoundException e) {
              e.printStackTrace();
            }
          }
        });
      }
    }).start();
  }
}
