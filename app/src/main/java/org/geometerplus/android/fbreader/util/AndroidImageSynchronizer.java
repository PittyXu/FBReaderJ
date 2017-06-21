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

package org.geometerplus.android.fbreader.util;

import org.geometerplus.zlibrary.core.image.ZLImageProxy;
import org.geometerplus.zlibrary.core.image.ZLImageSimpleProxy;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

public class AndroidImageSynchronizer implements ZLImageProxy.Synchronizer {
	public AndroidImageSynchronizer() {
	}

	@Override
	public void startImageLoading(ZLImageProxy image, Runnable postAction) {
		final ZLAndroidImageManager manager = (ZLAndroidImageManager)ZLAndroidImageManager.Instance();
		manager.startImageLoading(this, image, postAction);
	}

	@Override
	public void synchronize(ZLImageProxy image, final Runnable postAction) {
		if (image.isSynchronized()) {
			// TODO: also check if image is under synchronization
			if (postAction != null) {
				postAction.run();
			}
		} else if (image instanceof ZLImageSimpleProxy) {
			((ZLImageSimpleProxy)image).synchronize();
			if (postAction != null) {
				postAction.run();
			}
		} else {
			throw new RuntimeException("Cannot synchronize " + image.getClass());
		}
	}
}
