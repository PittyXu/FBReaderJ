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

package org.geometerplus.android.fbreader.bookmark;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TabHost;
import android.widget.TextView;

import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.api.FBReaderIntents.Key;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkUtil;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.ArrayList;
import java.util.List;

public class EditBookmarkActivity extends Activity implements IBookCollection.Listener<Book> {
	private Bookmark myBookmark;

	private void addTab(TabHost host, String id, int content) {
		final TabHost.TabSpec spec = host.newTabSpec(id);
		spec.setIndicator(id);
		spec.setContent(content);
        host.addTab(spec);
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.edit_bookmark);

		myBookmark = getIntent().getParcelableExtra(Key.BOOKMARK);
		if (myBookmark == null) {
			finish();
			return;
		}

		final DisplayMetrics dm = getResources().getDisplayMetrics();
		final int width = Math.min(
			(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, dm),
			dm.widthPixels * 9 / 10
		);
		final int height = Math.min(
			(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350, dm),
			dm.heightPixels * 9 / 10
		);

        final TabHost tabHost = (TabHost)findViewById(R.id.edit_bookmark_tabhost);
		tabHost.setLayoutParams(new FrameLayout.LayoutParams(
			new ViewGroup.LayoutParams(width, height)
		));
		tabHost.setup();

		addTab(tabHost, getString(R.string.bookmarks_text), R.id.edit_bookmark_content_text);
		addTab(tabHost, getString(R.string.bookmarks_style), R.id.edit_bookmark_content_style);
		addTab(tabHost, getString(R.string.delete), R.id.edit_bookmark_content_delete);

		final ZLStringOption currentTabOption =
			new ZLStringOption("LookNFeel", "EditBookmark", "text");
		tabHost.setCurrentTabByTag(currentTabOption.getValue());
		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tag) {
				if (!"delete".equals(tag)) {
					currentTabOption.setValue(tag);
				}
			}
		});

		final EditText editor = (EditText)findViewById(R.id.edit_bookmark_text);
		editor.setText(myBookmark.getText());
		final int len = editor.getText().length();
		editor.setSelection(len, len);

		final Button saveTextButton = (Button)findViewById(R.id.edit_bookmark_save_text_button);
		saveTextButton.setEnabled(false);
		saveTextButton.setText(R.string.bookmarks_save);
		editor.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence sequence, int start, int before, int count) {
				final String originalText = myBookmark.getText();
				saveTextButton.setEnabled(!originalText.equals(editor.getText().toString()));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		final Button deleteButton = (Button)findViewById(R.id.edit_bookmark_delete_button);
		deleteButton.setText(R.string.bookmarks_delete);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		if (event == BookEvent.BookmarkStyleChanged) {
		}
	}

	// method from IBookCollection.Listener
	public void onBuildEvent(IBookCollection.Status status) {
	}
}
