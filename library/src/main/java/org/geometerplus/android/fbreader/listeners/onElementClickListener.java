package org.geometerplus.android.fbreader.listeners;

import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextImageElement;
import org.geometerplus.zlibrary.text.view.ZLTextImageRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextVideoElement;
import org.geometerplus.zlibrary.text.view.ZLTextWord;

public interface onElementClickListener {

  void onHyperlinkExternalClick(ZLTextHyperlink hyperlink);

  void onHyperlinkInternalClick(ZLTextHyperlink hyperlink);

  void onHyperlinkFootnoteClick(ZLTextHyperlink hyperlink);

  void onImageClick(ZLTextImageElement pElement);

  void onWordClick(ZLTextWord pElement);

  void onVideoClick(ZLTextVideoElement pElement);
}
