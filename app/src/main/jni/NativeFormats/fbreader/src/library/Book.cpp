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

#include <algorithm>
#include <set>

#include <AndroidUtil.h>
#include <JniEnvelope.h>

#include <ZLStringUtil.h>
#include <ZLFile.h>
#include <ZLLanguageList.h>

#include "Book.h"
#include "Author.h"
#include "UID.h"

#include "../formats/FormatPlugin.h"
//#include "../migration/BookInfo.h"

const std::string Book::AutoEncoding = "auto";

Book::Book(const ZLFile &file, int id) : myBookId(id), myFile(file) {
}

Book::~Book() {
}

shared_ptr<Book> Book::createBook(
	const ZLFile &file,
	int id,
	const std::string &encoding,
	const std::string &language,
	const std::string &title
) {
	Book *book = new Book(file, id);
	book->setEncoding(encoding);
	book->setLanguage(language);
	book->setTitle(title);
	return book;
}

/*
shared_ptr<Book> Book::loadFromFile(const ZLFile &file) {
	shared_ptr<FormatPlugin> plugin = PluginCollection::Instance().plugin(file, false);
	if (plugin.isNull()) {
		return 0;
	}

	shared_ptr<Book> book = new Book(file, 0);
	if (!plugin->readMetainfo(*book)) {
		return 0;
	}

	if (book->title().empty()) {
		book->setTitle(ZLFile::fileNameToUtf8(file.name(true)));
	}

	if (book->encoding().empty()) {
		book->setEncoding(AutoEncoding);
	}

	if (book->language().empty()) {
		book->setLanguage(PluginCollection::Instance().defaultLanguage());
	}

	return book;
}
*/

shared_ptr<Book> Book::loadFromJavaBook(JNIEnv *env, jobject javaBook) {
	const std::string path = AndroidUtil::Method_Book_getPath->callForCppString(javaBook);
	const std::string title = AndroidUtil::Method_Book_getTitle->callForCppString(javaBook);
	const std::string language = AndroidUtil::Method_Book_getLanguage->callForCppString(javaBook);
	const std::string encoding = AndroidUtil::Method_Book_getEncodingNoDetection->callForCppString(javaBook);

	return createBook(ZLFile(path), 0, encoding, language, title);
}

bool Book::replaceAuthor(shared_ptr<Author> from, shared_ptr<Author> to) {
	AuthorList::iterator it = std::find(myAuthors.begin(), myAuthors.end(), from);
	if (it == myAuthors.end()) {
		return false;
	}
	if (to.isNull()) {
		myAuthors.erase(it);
	} else {
		*it = to;
	}
	return true;
}

void Book::setTitle(const std::string &title) {
	myTitle = title;
}

void Book::setLanguage(const std::string &language) {
	if (!myLanguage.empty()) {
		const std::vector<std::string> &codes = ZLLanguageList::languageCodes();
		std::vector<std::string>::const_iterator it =
			std::find(codes.begin(), codes.end(), myLanguage);
		std::vector<std::string>::const_iterator jt =
			std::find(codes.begin(), codes.end(), language);
		if (it != codes.end() && jt == codes.end()) {
			return;
		}
	}
	myLanguage = language;
}

void Book::setEncoding(const std::string &encoding) {
	myEncoding = encoding;
}

void Book::addAuthor(const std::string &displayName, const std::string &sortKey) {
	addAuthor(Author::getAuthor(displayName, sortKey));
}

void Book::addAuthor(shared_ptr<Author> author) {
	if (!author.isNull()) {
		myAuthors.push_back(author);
	}
}

void Book::removeAllAuthors() {
	myAuthors.clear();
}

void Book::addUid(shared_ptr<UID> uid) {
	if (uid.isNull()) {
		return;
	}
	UIDList::const_iterator it = std::find(myUIDs.begin(), myUIDs.end(), uid);
	if (it == myUIDs.end()) {
		myUIDs.push_back(uid);
	}
}

void Book::addUid(const std::string &type, const std::string &id) {
	if (type == "" || id == "") {
		return;
	}
	addUid(new UID(type, id));
}

void Book::removeAllUids() {
	myUIDs.clear();
}
