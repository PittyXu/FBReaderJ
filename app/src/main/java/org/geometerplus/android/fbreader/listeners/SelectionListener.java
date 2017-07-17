package org.geometerplus.android.fbreader.listeners;

import org.geometerplus.fbreader.util.TextSnippet;

public interface SelectionListener {

  void onStarted(int x, int y);

  void onChanged(TextSnippet pSnippet, int startY, int endY);

  void onEnded(TextSnippet pSnippet, int startY, int endY);

  void onCancelled();
}
