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

package org.geometerplus.zlibrary.core.application;

import org.geometerplus.zlibrary.core.view.ZLView;
import org.geometerplus.zlibrary.core.view.ZLViewWidget;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public abstract class ZLApplication {
	public static ZLApplication Instance() {
		return ourInstance;
	}

	private static ZLApplication ourInstance;

	private volatile ZLViewWidget mWidget;
	private volatile ZLView myView;

	protected ZLApplication() {
		ourInstance = this;
	}

	protected final void setView(ZLView view) {
		if (view != null) {
			myView = view;
			final ZLViewWidget widget = getViewWidget();
			if (widget != null) {
				widget.reset();
				widget.repaint();
			}
		}
	}

	public final ZLView getCurrentView() {
		return myView;
	}

	public final void setViewWidget(ZLViewWidget pWidget) {
		mWidget = pWidget;
	}

	public final void initWindow() {
		setView(myView);
	}

	public final ZLViewWidget getViewWidget() {
		return mWidget;
	}

	public final void onRepaintFinished() {
	}

	public void onWindowClosing() {
	}

	private volatile Timer myTimer;
	private final HashMap<Runnable, Long> myTimerTaskPeriods = new HashMap<>();
	private final HashMap<Runnable, TimerTask> myTimerTasks = new HashMap<>();
	private static class MyTimerTask extends TimerTask {
		private final Runnable myRunnable;

		MyTimerTask(Runnable runnable) {
			myRunnable = runnable;
		}

		@Override
		public void run() {
			myRunnable.run();
		}
	}

	private void addTimerTaskInternal(Runnable runnable, long periodMilliseconds) {
		final TimerTask task = new MyTimerTask(runnable);
		myTimer.schedule(task, periodMilliseconds / 2, periodMilliseconds);
		myTimerTasks.put(runnable, task);
	}

	private final Object myTimerLock = new Object();

	public final void stopTimer() {
		synchronized (myTimerLock) {
			if (myTimer != null) {
				myTimer.cancel();
				myTimer = null;
				myTimerTasks.clear();
			}
		}
	}

	public final void addTimerTask(Runnable runnable, long periodMilliseconds) {
		synchronized (myTimerLock) {
			removeTimerTask(runnable);
			myTimerTaskPeriods.put(runnable, periodMilliseconds);
			if (myTimer != null) {
				addTimerTaskInternal(runnable, periodMilliseconds);
			}
		}
	}

	public final void removeTimerTask(Runnable runnable) {
		synchronized (myTimerLock) {
			TimerTask task = myTimerTasks.get(runnable);
			if (task != null) {
				task.cancel();
				myTimerTasks.remove(runnable);
			}
			myTimerTaskPeriods.remove(runnable);
		}
	}
}
