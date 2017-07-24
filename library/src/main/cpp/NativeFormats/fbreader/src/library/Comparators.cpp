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

#include "Book.h"
#include "Author.h"

bool BookComparator::operator() (
	const shared_ptr<Book> book0,
	const shared_ptr<Book> book1
) const {
    return book0->title() < book1->title();
}

bool BookByFileNameComparator::operator() (
	const shared_ptr<Book> book0,
	const shared_ptr<Book> book1
) const {
	return book0->file() < book1->file();
}

bool AuthorComparator::operator() (
	const shared_ptr<Author> author0,
	const shared_ptr<Author> author1
) const {
	if (author0.isNull()) {
		return !author1.isNull();
	}
	if (author1.isNull()) {
		return false;
	}

	const int comp = author0->sortKey().compare(author1->sortKey());
	return comp != 0 ? comp < 0 : author0->name() < author1->name();
}
