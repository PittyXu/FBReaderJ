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
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import org.geometerplus.android.fbreader.BookCollectionShadow;
import org.geometerplus.android.fbreader.FBReader;
import org.geometerplus.android.fbreader.api.FBReaderIntents.Key;
import org.geometerplus.android.fbreader.config.MiscPreferences;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.BookmarkQuery;
import org.geometerplus.fbreader.book.HighlightingStyle;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.ui.android.R;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BookmarksActivity extends Activity implements IBookCollection.Listener<Book> {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;

	private TabHost myTabHost;

	private final Map<Integer,HighlightingStyle> myStyles =
		Collections.synchronizedMap(new HashMap<Integer, HighlightingStyle>());

	private BookCollectionShadow myCollection;
	private volatile Book myBook;
	private volatile Bookmark myBookmark;

	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();

	private volatile BookmarksAdapter myThisBookAdapter;
	private volatile BookmarksAdapter myAllBooksAdapter;
	private volatile BookmarksAdapter mySearchResultsAdapter;

	private void createTab(String tag, int id) {
		final String label = getString(R.string.tag);
		myTabHost.addTab(myTabHost.newTabSpec(tag).setIndicator(label).setContent(id));
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.bookmarks);

		myCollection = new BookCollectionShadow(this);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

        myTabHost = (TabHost)findViewById(R.id.bookmarks_tabhost);
		myTabHost.setup();

		createTab("thisBook", R.id.bookmarks_this_book);
		createTab("allBooks", R.id.bookmarks_all_books);
		createTab("search", R.id.bookmarks_search);

		myTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if ("search".equals(tabId)) {
					findViewById(R.id.bookmarks_search_results).setVisibility(View.GONE);
					onSearchRequested();
				}
			}
		});

		myBook = getIntent().getParcelableExtra(Key.BOOK);
		if (myBook == null) {
			finish();
		}
		myBookmark = getIntent().getParcelableExtra(Key.BOOKMARK);
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (myAllBooksAdapter == null) {
			myThisBookAdapter = new BookmarksAdapter((ListView) findViewById(R.id.bookmarks_this_book), myBookmark != null);
			myAllBooksAdapter = new BookmarksAdapter((ListView) findViewById(R.id.bookmarks_all_books), false);
			myCollection.addListener(BookmarksActivity.this);

			updateStyles();
			loadBookmarks();
		}
	}

	private void updateStyles() {
		synchronized (myStyles) {
			myStyles.clear();
			for (HighlightingStyle style : myCollection.highlightingStyles()) {
				myStyles.put(style.Id, style);
			}
		}
	}

	private final Object myBookmarksLock = new Object();

	private void loadBookmarks() {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					for (BookmarkQuery query = new BookmarkQuery(myBook, 50); ; query = query.next()) {
						final List<Bookmark> thisBookBookmarks = myCollection.bookmarks(query);
						if (thisBookBookmarks.isEmpty()) {
							break;
						}
						myThisBookAdapter.addAll(thisBookBookmarks);
						myAllBooksAdapter.addAll(thisBookBookmarks);
					}
					for (BookmarkQuery query = new BookmarkQuery(50); ; query = query.next()) {
						final List<Bookmark> allBookmarks = myCollection.bookmarks(query);
						if (allBookmarks.isEmpty()) {
							break;
						}
						myAllBooksAdapter.addAll(allBookmarks);
					}
				}
			}
		}).start();
	}

	private void updateBookmarks(final Book book) {
		new Thread(new Runnable() {
			public void run() {
				synchronized (myBookmarksLock) {
					final boolean flagThisBookTab = book.getId() == myBook.getId();
					final boolean flagSearchTab = mySearchResultsAdapter != null;

					final Map<String,Bookmark> oldBookmarks = new HashMap<String,Bookmark>();
					if (flagThisBookTab) {
						for (Bookmark b : myThisBookAdapter.bookmarks()) {
							oldBookmarks.put(b.Uid, b);
						}
					} else {
						for (Bookmark b : myAllBooksAdapter.bookmarks()) {
							if (b.BookId == book.getId()) {
								oldBookmarks.put(b.Uid, b);
							}
						}
					}
					final String pattern = MiscPreferences.getBookmarkSearchPattern(BookmarksActivity.this).toLowerCase();

					for (BookmarkQuery query = new BookmarkQuery(book, 50); ; query = query.next()) {
						final List<Bookmark> loaded = myCollection.bookmarks(query);
						if (loaded.isEmpty()) {
							break;
						}
						for (Bookmark b : loaded) {
							final Bookmark old = oldBookmarks.remove(b.Uid);
							myAllBooksAdapter.replace(old, b);
							if (flagThisBookTab) {
								myThisBookAdapter.replace(old, b);
							}
							if (flagSearchTab && MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
								mySearchResultsAdapter.replace(old, b);
							}
						}
					}
					myAllBooksAdapter.removeAll(oldBookmarks.values());
					if (flagThisBookTab) {
						myThisBookAdapter.removeAll(oldBookmarks.values());
					}
					if (flagSearchTab) {
						mySearchResultsAdapter.removeAll(oldBookmarks.values());
					}
				}
			}
		}).start();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		MiscPreferences.setBookmarkSearchPattern(this, pattern);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
		for (Bookmark b : myAllBooksAdapter.bookmarks()) {
			if (MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				bookmarks.add(b);
			}
		}
		if (!bookmarks.isEmpty()) {
			final ListView resultsView = (ListView)findViewById(R.id.bookmarks_search_results);
			resultsView.setVisibility(View.VISIBLE);
			if (mySearchResultsAdapter == null) {
				mySearchResultsAdapter = new BookmarksAdapter(resultsView, false);
			} else {
				mySearchResultsAdapter.clear();
			}
			mySearchResultsAdapter.addAll(bookmarks);
		} else {
			UIMessageUtil.showErrorMessage(this, getString(R.string.bookmarks_not_found));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		startSearch(MiscPreferences.getBookmarkSearchPattern(this), true, null, false);
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final String tag = myTabHost.getCurrentTabTag();
		final BookmarksAdapter adapter;
		if ("thisBook".equals(tag)) {
			adapter = myThisBookAdapter;
		} else if ("allBooks".equals(tag)) {
			adapter = myAllBooksAdapter;
		} else if ("search".equals(tag)) {
			adapter = mySearchResultsAdapter;
		} else {
			throw new RuntimeException("Unknown tab tag: " + tag);
		}

		final Bookmark bookmark = adapter.getItem(position);
		switch (item.getItemId()) {
			case OPEN_ITEM_ID:
				gotoBookmark(bookmark);
				return true;
			case EDIT_ITEM_ID:
				final Intent intent = new Intent(this, EditBookmarkActivity.class);
				intent.putExtra(Key.BOOKMARK, bookmark);
				startActivity(intent);
				return true;
			case DELETE_ITEM_ID:
				myCollection.deleteBookmark(bookmark);
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.BookId);
		if (book != null) {
			FBReader.openBookActivity(this, book, bookmark);
		} else {
			UIMessageUtil.showErrorMessage(this, getString(R.string.cannot_open_book));
		}
	}

	private final class BookmarksAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, View.OnCreateContextMenuListener {
		private final List<Bookmark> myBookmarksList =
			Collections.synchronizedList(new LinkedList<Bookmark>());
		private volatile boolean myShowAddBookmarkItem;

		BookmarksAdapter(ListView listView, boolean showAddBookmarkItem) {
			myShowAddBookmarkItem = showAddBookmarkItem;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
		}

		public List<Bookmark> bookmarks() {
			return Collections.unmodifiableList(myBookmarksList);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarksList) {
						for (Bookmark b : bookmarks) {
							final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
							if (position < 0) {
								myBookmarksList.add(- position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		private boolean areEqualsForView(Bookmark b0, Bookmark b1) {
			return
				b0.getStyleId() == b1.getStyleId() &&
				b0.getText().equals(b1.getText()) &&
				b0.getTimestamp(Bookmark.DateType.Latest).equals(b1.getTimestamp(Bookmark.DateType.Latest));
		}

		public void replace(final Bookmark old, final Bookmark b) {
			if (old != null && areEqualsForView(old, b)) {
				return;
			}
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarksList) {
						if (old != null) {
							myBookmarksList.remove(old);
						}
						final int position = Collections.binarySearch(myBookmarksList, b, myComparator);
						if (position < 0) {
							myBookmarksList.add(- position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void removeAll(final Collection<Bookmark> bookmarks) {
			if (bookmarks.isEmpty()) {
				return;
			}
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarksList.removeAll(bookmarks);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarksList.clear();
					notifyDataSetChanged();
				}
			});
		}

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if (getItem(position) != null) {
				menu.add(0, OPEN_ITEM_ID, 0, R.string.bookmarks_open);
				menu.add(0, EDIT_ITEM_ID, 0, R.string.bookmarks_edit);
				menu.add(0, DELETE_ITEM_ID, 0, R.string.bookmarks_delete);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
			final ImageView imageView = ViewUtil.findImageView(view, R.id.bookmark_item_icon);
			final View colorContainer = ViewUtil.findView(view, R.id.bookmark_item_color_container);
			final TextView colorView = (TextView) ViewUtil.findView(view, R.id.bookmark_item_color);
			final TextView textView = ViewUtil.findTextView(view, R.id.bookmark_item_text);
			final TextView bookTitleView = ViewUtil.findTextView(view, R.id.bookmark_item_booktitle);

			final Bookmark bookmark = getItem(position);
			if (bookmark == null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.ic_list_plus);
				colorContainer.setVisibility(View.GONE);
				textView.setText(R.string.bookmarks_new);
				bookTitleView.setVisibility(View.GONE);
			} else {
				imageView.setVisibility(View.GONE);
				colorContainer.setVisibility(View.VISIBLE);
				BookmarksUtil.setupColorView(colorView, myStyles.get(bookmark.getStyleId()));
				textView.setText(bookmark.getText());
				if (myShowAddBookmarkItem) {
					bookTitleView.setVisibility(View.GONE);
				} else {
					bookTitleView.setVisibility(View.VISIBLE);
					bookTitleView.setText(bookmark.BookTitle);
				}
			}
			return view;
		}

		@Override
		public final boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public final boolean isEnabled(int position) {
			return true;
		}

		@Override
		public final long getItemId(int position) {
			final Bookmark item = getItem(position);
			return item != null ? item.getId() : -1;
		}

		@Override
		public final Bookmark getItem(int position) {
			if (myShowAddBookmarkItem) {
				--position;
			}
			return position >= 0 ? myBookmarksList.get(position) : null;
		}

		@Override
		public final int getCount() {
			return myShowAddBookmarkItem ? myBookmarksList.size() + 1 : myBookmarksList.size();
		}

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final Bookmark bookmark = getItem(position);
			if (bookmark != null) {
				gotoBookmark(bookmark);
			} else if (myShowAddBookmarkItem) {
				myShowAddBookmarkItem = false;
				myCollection.saveBookmark(myBookmark);
			}
		}
	}

	// method from IBookCollection.Listener
	public void onBookEvent(BookEvent event, Book book) {
		switch (event) {
			default:
				break;
			case BookmarkStyleChanged:
				runOnUiThread(new Runnable() {
					public void run() {
						updateStyles();
						myAllBooksAdapter.notifyDataSetChanged();
						myThisBookAdapter.notifyDataSetChanged();
						if (mySearchResultsAdapter != null) {
							mySearchResultsAdapter.notifyDataSetChanged();
						}
					}
				});
				break;
			case BookmarksUpdated:
				updateBookmarks(book);
				break;
		}
	}

	// method from IBookCollection.Listener
	public void onBuildEvent(IBookCollection.Status status) {
	}
}
