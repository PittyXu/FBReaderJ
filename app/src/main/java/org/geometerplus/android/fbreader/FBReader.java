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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.folderselector.FileChooserDialog;
import com.afollestad.materialdialogs.folderselector.FileChooserDialog.FileCallback;


import org.geometerplus.android.R;

import java.io.File;

public final class FBReader extends AppCompatActivity implements FileCallback {

	private ReaderView myMainView;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.main);
		myMainView = (ReaderView)findViewById(R.id.main_view);
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else {
			super.onNewIntent(intent);
		}
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
				myMainView.actionChangeFontSize(2);
				break;
			case R.id.action_decrease_font:
				myMainView.actionChangeFontSize(-2);
				break;
			case R.id.action_add_bookmark:
				myMainView.addSelectionBookmark();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onFileSelection(@NonNull final FileChooserDialog pFileChooserDialog,
			@NonNull final File pFile) {
		myMainView.openBook(pFile.getAbsolutePath());
	}

	@Override
	public void onFileChooserDismissed(@NonNull final FileChooserDialog pFileChooserDialog) {
	}
}
