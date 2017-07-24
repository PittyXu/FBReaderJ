/*
 * Copyright (C) 2004-2015 FBReader.ORG Limited <contact@fbreader.org>
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

#include <cstring>

#include <algorithm>

#include <ZLUnicodeUtil.h>
#include <ZLImage.h>

#include "ZLCachedMemoryAllocator.h"
#include "ZLTextParagraph.h"
#include "ZLTextStyleEntry.h"

const shared_ptr<ZLTextParagraphEntry> ResetBidiEntry::Instance = new ResetBidiEntry();

ZLTextControlEntryPool ZLTextControlEntryPool::Pool;

shared_ptr<ZLTextParagraphEntry> ZLTextControlEntryPool::controlEntry(ZLTextKind kind, bool isStart) {
	std::map<ZLTextKind, shared_ptr<ZLTextParagraphEntry> > &entries = isStart ? myStartEntries : myEndEntries;
	std::map<ZLTextKind, shared_ptr<ZLTextParagraphEntry> >::iterator it = entries.find(kind);
	if (it != entries.end()) {
		return it->second;
	}
	shared_ptr<ZLTextParagraphEntry> entry = new ZLTextControlEntry(kind, isStart);
	entries[kind] = entry;
	return entry;
}
