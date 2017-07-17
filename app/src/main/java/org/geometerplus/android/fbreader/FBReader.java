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

package org.geometerplus.android.fbreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog.FileCallback;

import org.geometerplus.android.fbreader.FBReaderIntents.Key;
import org.geometerplus.android.fbreader.dao.Bookmark;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.formats.BookReadingException;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;

import java.io.File;

public final class FBReader extends AppCompatActivity implements FileCallback {

	private FBReaderApp myFBReaderApp;
	private volatile Book myBook;

	private ReaderView myMainView;

	private Intent myOpenBookIntent = null;

	private synchronized void openBook(Intent intent, boolean force) {
		if (!force && myBook != null) {
			return;
		}

		myBook = intent.getParcelableExtra(Key.BOOK);
		final Bookmark bookmark = intent.getParcelableExtra(Key.BOOKMARK);
		if (myBook == null) {
			final Uri data = intent.getData();
			if (data != null) {
				myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));
			}
		}
		if (myBook != null) {
			ZLFile file = BookUtil.fileByBook(myBook);
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}
				showErrorMessage("文件未找到: " + file.getPath());
				myBook = null;
			}
		}
		myFBReaderApp.openBook(myBook, bookmark);
		AndroidFontUtil.clearFontCache();
	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}

		String path = file.getPath();
		Book book = new Book(-1, path, null, null, null);
		try {
			BookUtil.readMetainfo(book, PluginCollection.Instance().getPlugin(ZLFile.createFileByPath(path)));
		} catch (BookReadingException pE) {
			pE.printStackTrace();
		}

		return book;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.main);
		myMainView = (ReaderView)findViewById(R.id.main_view);

		myFBReaderApp = (FBReaderApp)FBReaderApp.Instance();
		if (myFBReaderApp == null) {
			myFBReaderApp = new FBReaderApp(this);
		}
		myBook = null;

		myFBReaderApp.setViewWidget(myMainView);
		myFBReaderApp.initWindow();

		myOpenBookIntent = getIntent();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		final String action = intent.getAction();

		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
			myOpenBookIntent = intent;
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		final BookModel model = myFBReaderApp.Model;
		if (model != null && model.Book != null) {
			onPreferencesUpdate(model.Book);
		}

		if (myOpenBookIntent != null) {
			final Intent intent = myOpenBookIntent;
			myOpenBookIntent = null;
			openBook(intent, true);
		} else if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
			myFBReaderApp.openBook(myFBReaderApp.ExternalBook, null);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		myFBReaderApp.stopTimer();
		myFBReaderApp.onWindowClosing();
	}

	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.fbreader, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_file:
				FileChooserDialog.Builder builder = new FileChooserDialog.Builder(this);
				builder.mimeType("application/epub+zip")
						.show();
				break;
			case R.id.action_increase_font:
				myFBReaderApp.actionChangeFontSize(2);
				break;
			case R.id.action_decrease_font:
				myFBReaderApp.actionChangeFontSize(-2);
				break;
			case R.id.action_add_bookmark:
				myFBReaderApp.addSelectionBookmark();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	public void showErrorMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onFileSelection(@NonNull final FileChooserDialog pFileChooserDialog,
			@NonNull final File pFile) {
		myBook = createBookForFile(ZLFile.createFileByPath(pFile.getAbsolutePath()));
		if (myBook != null) {
			ZLFile file = BookUtil.fileByBook(myBook);
			if (!file.exists()) {
				if (file.getPhysicalFile() != null) {
					file = file.getPhysicalFile();
				}
				showErrorMessage("文件未找到: " + file.getPath());
				myBook = null;
			}
		}
		myFBReaderApp.openBook(myBook, null);
		AndroidFontUtil.clearFontCache();
	}

	@Override
	public void onFileChooserDismissed(@NonNull final FileChooserDialog pFileChooserDialog) {
	}
}
