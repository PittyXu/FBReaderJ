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

#include <ZLUnicodeUtil.h>
#include <ZLLogger.h>

#include "ZLibrary.h"
#include "../filesystem/ZLFSManager.h"

std::string ZLibrary::ourZLibraryDirectory;

std::string ZLibrary::ourApplicationDirectory;

void ZLibrary::parseArguments(int &argc, char **&argv) {
	static const std::string LOGGER_OPTION = "-log";
	while ((argc > 2) && (argv[1] != 0) && (argv[2] != 0)) {
		const std::string argument = argv[1];
		if (LOGGER_OPTION == argument) {
			std::string loggerClasses = argv[2];
			while (std::size_t index = loggerClasses.find(':') != std::string::npos) {
				ZLLogger::Instance().registerClass(loggerClasses.substr(0, index));
				loggerClasses.erase(0, index + 1);
			}
			ZLLogger::Instance().registerClass(loggerClasses);
		} else {
			ZLLogger::Instance().println(ZLLogger::DEFAULT_CLASS, "unknown argument: " + argument);
		}
		argc -= 2;
		argv += 2;
	}
	ourZLibraryDirectory = ".";//BaseDirectory + FileNameDelimiter + "zlibrary";
}

void ZLibrary::shutdown() {
	ZLFSManager::deleteInstance();
}

void ZLibrary::initApplication() {
	ourApplicationDirectory = ".";//BaseDirectory + FileNameDelimiter + ourApplicationName;
}
