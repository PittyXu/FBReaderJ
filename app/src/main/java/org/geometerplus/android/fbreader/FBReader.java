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

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RelativeLayout;

import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.FBReaderIntents;
import org.geometerplus.android.fbreader.api.MenuNode;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIMessageUtil;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.fbreader.fbreader.options.ColorProfile;
import org.geometerplus.zlibrary.core.application.ZLApplicationWindow;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.Config;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FBReader extends FBReaderMainActivity implements ZLApplicationWindow {
	public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
	public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;

	public static Intent defaultIntent(Context context) {
		return new Intent(context, FBReader.class)
			.setAction(FBReaderIntents.Action.VIEW)
			.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	}

	public static void openBookActivity(Context context, Book book, Bookmark bookmark) {
		final Intent intent = defaultIntent(context);
		FBReaderIntents.putBookExtra(intent, book);
		FBReaderIntents.putBookmarkExtra(intent, bookmark);
		context.startActivity(intent);
	}

	private FBReaderApp myFBReaderApp;
	private volatile Book myBook;

	private RelativeLayout myRootView;
	private ZLAndroidWidget myMainView;

	private String myMenuLanguage;

	volatile boolean IsPaused = false;
	volatile Runnable OnResumeAction = null;

	private Intent myOpenBookIntent = null;

	private synchronized void openBook(Intent intent, final Runnable action, boolean force) {
		if (!force && myBook != null) {
			return;
		}

		myBook = FBReaderIntents.getBookExtra(intent, myFBReaderApp.Collection);
		final Bookmark bookmark = FBReaderIntents.getBookmarkExtra(intent);
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
				UIMessageUtil.showErrorMessage(this, "fileNotFound", file.getPath());
				myBook = null;
			} else {
				NotificationUtil.drop(this, myBook);
			}
		}
		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				myFBReaderApp.openBook(myBook, bookmark, action);
				AndroidFontUtil.clearFontCache();
			}
		});
	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myFBReaderApp.Collection.getBookByFile(file.getPath());
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myFBReaderApp.Collection.getBookByFile(child.getPath());
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		final Config config = Config.Instance();
		config.runOnConnect(new Runnable() {
			public void run() {
				config.requestAllValuesForGroup("Options");
				config.requestAllValuesForGroup("Style");
				config.requestAllValuesForGroup("LookNFeel");
				config.requestAllValuesForGroup("Fonts");
				config.requestAllValuesForGroup("Colors");
				config.requestAllValuesForGroup("Files");
			}
		});

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		myRootView = (RelativeLayout)findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget)findViewById(R.id.main_view);

		myFBReaderApp = (FBReaderApp)FBReaderApp.Instance();
		if (myFBReaderApp == null) {
			myFBReaderApp = new FBReaderApp(Paths.systemInfo(this), new BookCollectionShadow());
		}
		getCollection().bindToService(this, null);
		myBook = null;

		myFBReaderApp.setWindow(this);
		myFBReaderApp.initWindow();

		if (myFBReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(myFBReaderApp);
		}
		if (myFBReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myFBReaderApp);
		}

		myFBReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionBookmarkAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.DISPLAY_BOOK_POPUP, new DisplayBookPopupAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.OPEN_VIDEO, new OpenVideoAction(this, myFBReaderApp));
		myFBReaderApp.addAction(ActionCode.HIDE_TOAST, new HideToastAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.OPEN_START_SCREEN, new StartScreenAction(this, myFBReaderApp));

		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_SENSOR));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (getZLibrary().supportsAllOrientations()) {
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			myFBReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, myFBReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}

		myFBReaderApp.addAction(ActionCode.SWITCH_TO_DAY_PROFILE, new SwitchProfileAction(this, myFBReaderApp, ColorProfile.DAY));
		myFBReaderApp.addAction(ActionCode.SWITCH_TO_NIGHT_PROFILE, new SwitchProfileAction(this, myFBReaderApp, ColorProfile.NIGHT));

		myOpenBookIntent = getIntent();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		final String action = intent.getAction();
		final Uri data = intent.getData();

		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action)
				   && data != null && "fbreader-action".equals(data.getScheme())) {
			myFBReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_VIEW.equals(action) || FBReaderIntents.Action.VIEW.equals(action)) {
			myOpenBookIntent = intent;
		} else if (Intent.ACTION_SEARCH.equals(action)) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup)myFBReaderApp.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					myFBReaderApp.MiscOptions.TextSearchPattern.setValue(pattern);
					if (myFBReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								myFBReaderApp.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIMessageUtil.showErrorMessage(FBReader.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		final ZLAndroidLibrary zlibrary = getZLibrary();

		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				myFBReaderApp.ViewOptions.ColorProfileName.saveSpecialValue();
				SetScreenOrientationAction.setOrientation(FBReader.this, zlibrary.getOrientationOption().getValue());
			}
		});

		((PopupPanel)myFBReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
		((NavigationPopup)myFBReaderApp.getPopupById(NavigationPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel)myFBReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Config.Instance().runOnConnect(new Runnable() {
			public void run() {
				getCollection().bindToService(FBReader.this, new Runnable() {
					public void run() {
						final BookModel model = myFBReaderApp.Model;
						if (model == null || model.Book == null) {
							return;
						}
						onPreferencesUpdate(myFBReaderApp.Collection.getBookById(model.Book.getId()));
					}
				});
			}
		});

		IsPaused = false;
		if (OnResumeAction != null) {
			final Runnable action = OnResumeAction;
			OnResumeAction = null;
			action.run();
		}

		SetScreenOrientationAction.setOrientation(this, getZLibrary().getOrientationOption().getValue());
		if (myOpenBookIntent != null) {
			final Intent intent = myOpenBookIntent;
			myOpenBookIntent = null;
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					openBook(intent, null, true);
				}
			});
		} else if (myFBReaderApp.Model == null && myFBReaderApp.ExternalBook != null) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					myFBReaderApp.openBook(myFBReaderApp.ExternalBook, null, null);
				}
			});
		} else {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
				}
			});
		}

		PopupPanel.restoreVisibilities(myFBReaderApp);
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);
	}

	@Override
	protected void onPause() {

		IsPaused = true;

		myFBReaderApp.stopTimer();
		myFBReaderApp.onWindowClosing();

		super.onPause();
	}

	@Override
	protected void onStop() {
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(myFBReaderApp, this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getCollection().unbind();
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		myFBReaderApp.onWindowClosing();
		super.onLowMemory();
	}

	@Override
	public boolean onSearchRequested() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		myFBReaderApp.hideActivePopup();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					myFBReaderApp.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(myFBReaderApp.MiscOptions.TextSearchPattern.getValue(), true, null, false);
		return true;
	}

	public void showSelectionPanel() {
		final ZLTextView view = myFBReaderApp.getTextView();
		((SelectionPopup)myFBReaderApp.getPopupById(SelectionPopup.ID))
			.move(view.getSelectionStartY(), view.getSelectionEndY());
		myFBReaderApp.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		final FBReaderApp.PopupPanel popup = myFBReaderApp.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			myFBReaderApp.hideActivePopup();
		}
	}

	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		myFBReaderApp.onBookUpdated(book);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
			case REQUEST_PREFERENCES:
				if (resultCode != RESULT_DO_NOTHING && data != null) {
					final Book book = FBReaderIntents.getBookExtra(data, myFBReaderApp.Collection);
					if (book != null) {
						getCollection().bindToService(this, new Runnable() {
							public void run() {
								onPreferencesUpdate(book);
							}
						});
					}
				}
				break;
		}
	}

	public void navigate() {
		((NavigationPopup)myFBReaderApp.getPopupById(NavigationPopup.ID)).runNavigation();
	}

	private Menu addSubmenu(Menu menu, String id) {
		return menu.addSubMenu(ZLResource.resource("menu").getResource(id).getValue());
	}

	private void addMenuItem(Menu menu, String actionId, Integer iconId, String name) {
		if (name == null) {
			name = ZLResource.resource("menu").getResource(actionId).getValue();
		}
		final MenuItem menuItem = menu.add(name);
		if (iconId != null) {
			menuItem.setIcon(iconId);
		}
		menuItem.setOnMenuItemClickListener(myMenuListener);
		myMenuItemMap.put(menuItem, actionId);
	}

	private void addMenuItem(Menu menu, String actionId, int iconId) {
		addMenuItem(menu, actionId, iconId, null);
	}

	private void addMenuItem(Menu menu, String actionId) {
		addMenuItem(menu, actionId, null, null);
	}

	private void fillMenu(Menu menu, List<MenuNode> nodes) {
		for (MenuNode n : nodes) {
			if (n instanceof MenuNode.Item) {
				final Integer iconId = ((MenuNode.Item)n).IconId;
				if (iconId != null) {
					addMenuItem(menu, n.Code, iconId);
				} else {
					addMenuItem(menu, n.Code);
				}
			} else /* if (n instanceof MenuNode.Submenu) */ {
				final Menu submenu = addSubmenu(menu, n.Code);
				fillMenu(submenu, ((MenuNode.Submenu)n).Children);
			}
		}
	}

	private void setupMenu(Menu menu) {
		final String menuLanguage = ZLResource.getLanguageOption().getValue();
		if (menuLanguage.equals(myMenuLanguage)) {
			return;
		}
		myMenuLanguage = menuLanguage;

		menu.clear();
		fillMenu(menu, MenuData.topLevelNodes());
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		setupMenu(menu);

		return true;
	}

	protected void onPluginNotFound(final Book book) {
		final BookCollectionShadow collection = getCollection();
		collection.bindToService(this, new Runnable() {
			public void run() {
				final Book recent = collection.getRecentBook(0);
				if (recent != null && !collection.sameBook(recent, book)) {
					myFBReaderApp.openBook(recent, null, null);
				} else {
					myFBReaderApp.openHelpBook();
				}
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}

	private BookCollectionShadow getCollection() {
		return (BookCollectionShadow)myFBReaderApp.Collection;
	}

	// methods from ZLApplicationWindow interface
	@Override
	public void showErrorMessage(String key) {
		UIMessageUtil.showErrorMessage(this, key);
	}

	@Override
	public void showErrorMessage(String key, String parameter) {
		UIMessageUtil.showErrorMessage(this, key, parameter);
	}

	@Override
	public FBReaderApp.SynchronousExecutor createExecutor(String key) {
		return UIUtil.createExecutor(this, key);
	}

	@Override
	public void close() {
		finish();
	}

	@Override
	public ZLViewWidget getViewWidget() {
		return myMainView;
	}

	private final HashMap<MenuItem,String> myMenuItemMap = new HashMap<MenuItem,String>();

	private final MenuItem.OnMenuItemClickListener myMenuListener =
		new MenuItem.OnMenuItemClickListener() {
			public boolean onMenuItemClick(MenuItem item) {
				myFBReaderApp.runAction(myMenuItemMap.get(item));
				return true;
			}
		};

	@Override
	public void refresh() {
		runOnUiThread(new Runnable() {
			public void run() {
				for (Map.Entry<MenuItem,String> entry : myMenuItemMap.entrySet()) {
					final String actionId = entry.getValue();
					final MenuItem menuItem = entry.getKey();
					menuItem.setVisible(myFBReaderApp.isActionVisible(actionId) && myFBReaderApp.isActionEnabled(actionId));
					switch (myFBReaderApp.isActionChecked(actionId)) {
						case TRUE:
							menuItem.setCheckable(true);
							menuItem.setChecked(true);
							break;
						case FALSE:
							menuItem.setCheckable(true);
							menuItem.setChecked(false);
							break;
						case UNDEFINED:
							menuItem.setCheckable(false);
							break;
					}
				}
			}
		});
	}

	@Override
	public void processException(Exception exception) {
		exception.printStackTrace();
	}

	@Override
	public void setWindowTitle(final String title) {
		runOnUiThread(new Runnable() {
			public void run() {
				setTitle(title);
			}
		});
	}
}
