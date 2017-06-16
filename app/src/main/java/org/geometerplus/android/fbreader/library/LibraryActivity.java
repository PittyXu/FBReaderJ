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

package org.geometerplus.android.fbreader.library;

import java.util.*;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;

import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import org.geometerplus.zlibrary.ui.android.R;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.library.*;
import org.geometerplus.fbreader.tree.FBTree;

import org.geometerplus.android.util.*;
import org.geometerplus.android.fbreader.*;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.tree.TreeActivity;

public class LibraryActivity extends TreeActivity<LibraryTree> implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, IBookCollection.Listener<Book> {

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile RootTree myRootTree;
	private Book mySelectedBook;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		mySelectedBook = FBReaderIntents.getBookExtra(getIntent(), myCollection);

		new LibraryTreeAdapter(this);

		getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);

		deleteRootTree();

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(!myCollection.status().IsComplete);
				myRootTree = new RootTree(myCollection, PluginCollection.Instance(Paths.systemInfo(LibraryActivity.this)));
				myCollection.addListener(LibraryActivity.this);
				init(getIntent());
			}
		});
	}

	@Override
	protected LibraryTree getTreeByKey(FBTree.Key key) {
		return key != null ? myRootTree.getLibraryTree(key) : myRootTree;
	}

	private synchronized void deleteRootTree() {
		if (myRootTree != null) {
			myCollection.removeListener(this);
			myCollection.unbind();
			myRootTree = null;
		}
	}

	@Override
	protected void onDestroy() {
		deleteRootTree();
		super.onDestroy();
	}

	@Override
	public boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree)tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}

	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		final LibraryTree tree = (LibraryTree)getTreeAdapter().getItem(position);
		final Book book = tree.getBook();
		if (book == null) {
			openTree(tree);
		}
	}

	//
	// Search
	//
	private final ZLStringOption BookSearchPatternOption =
		new ZLStringOption("BookSearch", "Pattern", "");

	@Override
	public boolean onSearchRequested() {
		if (DeviceType.Instance().hasStandardSearchDialog()) {
			startSearch(BookSearchPatternOption.getValue(), true, null, false);
		} else {
		}
		return true;
	}

	private interface ContextItemId {
		int OpenBook              = 0;
		int AddToFavorites        = 1;
		int RemoveFromFavorites   = 2;
		int MarkAsRead            = 3;
		int MarkAsUnread          = 4;
		int DeleteBook            = 5;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final Book book = ((LibraryTree)getTreeAdapter().getItem(position)).getBook();
		if (book != null) {
			createBookContextMenu(menu, book);
		}
	}

	private void createBookContextMenu(ContextMenu menu, Book book) {
		final ZLResource resource = LibraryTree.resource();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, ContextItemId.OpenBook, 0, resource.getResource("openBook").getValue());
		if (book.hasLabel(Book.FAVORITE_LABEL)) {
			menu.add(0, ContextItemId.RemoveFromFavorites, 0, resource.getResource("removeFromFavorites").getValue());
		} else {
			menu.add(0, ContextItemId.AddToFavorites, 0, resource.getResource("addToFavorites").getValue());
		}
		if (book.hasLabel(Book.READ_LABEL)) {
			menu.add(0, ContextItemId.MarkAsUnread, 0, resource.getResource("markAsUnread").getValue());
		} else {
			menu.add(0, ContextItemId.MarkAsRead, 0, resource.getResource("markAsRead").getValue());
		}
		if (myCollection.canRemoveBook(book, true)) {
			menu.add(0, ContextItemId.DeleteBook, 0, resource.getResource("deleteBook").getValue());
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final Book book = ((LibraryTree)getTreeAdapter().getItem(position)).getBook();
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book);
		}
		return super.onContextItemSelected(item);
	}

	private boolean onContextItemSelected(int itemId, Book book) {
		switch (itemId) {
			case ContextItemId.OpenBook:
				FBReader.openBookActivity(this, book, null);
				return true;
			case ContextItemId.AddToFavorites:
				book.addNewLabel(Book.FAVORITE_LABEL);
				myCollection.saveBook(book);
				return true;
			case ContextItemId.RemoveFromFavorites:
				book.removeLabel(Book.FAVORITE_LABEL);
				myCollection.saveBook(book);
				if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
					getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
				return true;
			case ContextItemId.MarkAsRead:
				book.addNewLabel(Book.READ_LABEL);
				myCollection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ContextItemId.MarkAsUnread:
				book.removeLabel(Book.READ_LABEL);
				myCollection.saveBook(book);
				getListView().invalidateViews();
				return true;
			case ContextItemId.DeleteBook:
				tryToDeleteBook(book);
				return true;
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		return false;
	}

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final List<Book> myBooks;

		BookDeleter(List<Book> books) {
			myBooks = new ArrayList<Book>(books);
		}

		public void onClick(DialogInterface dialog, int which) {
			if (getCurrentTree() instanceof FileTree) {
				for (Book book : myBooks) {
					getTreeAdapter().remove(new FileTree(
						(FileTree)getCurrentTree(),
						BookUtil.fileByBook(book)
					));
					myCollection.removeBook(book, true);
				}
				getListView().invalidateViews();
			} else {
				boolean doReplace = false;
				for (Book book : myBooks) {
					doReplace |= getCurrentTree().onBookEvent(BookEvent.Removed, book);
					myCollection.removeBook(book, true);
				}
				if (doReplace) {
					getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
				}
			}
		}
	}

	private void tryToDeleteBooks(List<Book> books) {
		final int size = books.size();
		if (size == 0) {
			return;
		}
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource(
			size == 1 ? "deleteBookBox" : "deleteMultipleBookBox"
		);
		final String title = size == 1
			? books.get(0).getTitle()
			: boxResource.getResource("title").getValue();
		final String message =
			boxResource.getResource("message").getValue(size).replaceAll("%s", String.valueOf(size));
		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(books))
			.setNegativeButton(buttonResource.getResource("no").getValue(), null)
			.create().show();
	}

	private void tryToDeleteBook(Book book) {
		tryToDeleteBooks(Collections.singletonList(book));
	}

	public void onBookEvent(BookEvent event, Book book) {
		if (getCurrentTree().onBookEvent(event, book)) {
			getTreeAdapter().replaceAll(getCurrentTree().subtrees(), true);
		}
	}

	public void onBuildEvent(IBookCollection.Status status) {
		setProgressBarIndeterminateVisibility(!status.IsComplete);
	}
}
