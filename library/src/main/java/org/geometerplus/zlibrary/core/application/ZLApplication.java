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

import org.geometerplus.zlibrary.core.view.ZLViewWidget;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public abstract class ZLApplication {
	public volatile ZLAndroidWidget mWidget;

	protected ZLApplication() {
	}

	public final void setViewWidget(ZLAndroidWidget pWidget) {
		mWidget = pWidget;
	}

	public final ZLViewWidget getViewWidget() {
		return mWidget;
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
