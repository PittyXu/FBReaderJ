# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
# 动态方式加载 speex是libxxxx.so的xxxx部分
#add_library(LineBreak-v2 SHARED src/main/cpp/LineBreak/LineBreaker.cpp)

#include_directories(src/main/cpp/LineBreak/)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

#find_library( # Sets the name of the path variable.
#              log-lib

#              # Specifies the name of the NDK library that
#              # you want CMake to locate.
#              zlib )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# target_link_libraries( # Specifies the target library.
#                       DeflatingDecompressor-v3

#                       # Links the target library to the log library
#                       # included in the NDK.
#                       ${log-lib} )

#include_directories(${CMAKE_CURRENT_BINARY_DIR} ${CMAKE_CURRENT_SOURCE_DIR})

add_library(LineBreak-v2 SHARED
  src/main/cpp/LineBreak/LineBreaker.cpp
  src/main/cpp/LineBreak/liblinebreak-2.0/linebreak.c
  src/main/cpp/LineBreak/liblinebreak-2.0/linebreakdata.c
  src/main/cpp/LineBreak/liblinebreak-2.0/linebreakdef.c)

add_library(expat STATIC
  src/main/cpp/expat-2.0.1/lib/xmlparse.c
  src/main/cpp/expat-2.0.1/lib/xmlrole.c
  src/main/cpp/expat-2.0.1/lib/xmltok.c)
include_directories(src/main/cpp/expat-2.0.1/
  src/main/cpp/expat-2.0.1/lib/)

add_library(NativeFormats-v4 SHARED
  src/main/cpp/NativeFormats/main.cpp
  src/main/cpp/NativeFormats/JavaNativeFormatPlugin.cpp
  src/main/cpp/NativeFormats/JavaPluginCollection.cpp
  src/main/cpp/NativeFormats/util/AndroidUtil.cpp
  src/main/cpp/NativeFormats/util/JniEnvelope.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/constants/ZLXMLNamespace.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/drm/FileEncryptionInfo.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/DummyEncodingConverter.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/Utf16EncodingConverters.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/Utf8EncodingConverter.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/JavaEncodingConverter.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/ZLEncodingCollection.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/ZLEncodingConverter.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/ZLDir.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/ZLFSManager.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/ZLFile.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/ZLInputStreamDecorator.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/zip/ZLGzipInputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/zip/ZLZDecompressor.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipDir.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipEntryCache.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipHeader.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipInputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLCharSequence.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLLanguageDetector.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLLanguageList.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLLanguageMatcher.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLStatistics.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLStatisticsGenerator.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLStatisticsItem.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/language/ZLStatisticsXMLReader.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/library/ZLibrary.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/logger/ZLLogger.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/util/ZLFileUtil.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/util/ZLStringUtil.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/util/ZLUnicodeUtil.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/xml/ZLAsynchronousInputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/xml/ZLPlainAsynchronousInputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/xml/ZLXMLReader.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/xml/expat/ZLXMLReaderInternal.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSDir.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSManager.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileInputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileOutputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/core/src/unix/library/ZLUnixLibrary.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/model/ZLCachedMemoryAllocator.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/model/ZLTextModel.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/model/ZLTextParagraph.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/model/ZLTextStyleEntry.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/model/ZLVideoEntry.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/fonts/FontManager.cpp
  src/main/cpp/NativeFormats/zlibrary/text/src/fonts/FontMap.cpp
  src/main/cpp/NativeFormats/zlibrary/ui/src/android/filesystem/JavaFSDir.cpp
  src/main/cpp/NativeFormats/zlibrary/ui/src/android/filesystem/JavaInputStream.cpp
  src/main/cpp/NativeFormats/zlibrary/ui/src/android/filesystem/ZLAndroidFSManager.cpp
  src/main/cpp/NativeFormats/zlibrary/ui/src/android/library/ZLAndroidLibraryImplementation.cpp
  src/main/cpp/NativeFormats/fbreader/src/bookmodel/BookModel.cpp
  src/main/cpp/NativeFormats/fbreader/src/bookmodel/BookReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/FormatPlugin.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/PluginCollection.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/css/CSSInputStream.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/css/CSSSelector.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/css/StringInputStream.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/css/StyleSheetParser.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/css/StyleSheetTable.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/css/StyleSheetUtil.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/NCXReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBBookReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBCoverReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBEncryptionReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBMetaInfoReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBPlugin.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBSimpleIdReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBTextStream.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OEBUidReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/OPFReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/oeb/XHTMLImageFinder.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/txt/PlainTextFormat.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/util/EntityFilesCollector.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/util/MergedStream.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/util/MiscUtil.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/util/XMLTextStream.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/xhtml/XHTMLReader.cpp
  src/main/cpp/NativeFormats/fbreader/src/formats/xhtml/XHTMLTagInfo.cpp
  src/main/cpp/NativeFormats/fbreader/src/library/Author.cpp
  src/main/cpp/NativeFormats/fbreader/src/library/Book.cpp
  src/main/cpp/NativeFormats/fbreader/src/library/Comparators.cpp
  src/main/cpp/NativeFormats/fbreader/src/library/UID.cpp)
include_directories(src/main/cpp/NativeFormats/util/
  src/main/cpp/NativeFormats/zlibrary/core/src/constants/
  src/main/cpp/NativeFormats/zlibrary/core/src/drm/
  src/main/cpp/NativeFormats/zlibrary/core/src/encoding/
  src/main/cpp/NativeFormats/zlibrary/core/src/filesystem/
  src/main/cpp/NativeFormats/zlibrary/core/src/image/
  src/main/cpp/NativeFormats/zlibrary/core/src/language/
  src/main/cpp/NativeFormats/zlibrary/core/src/library/
  src/main/cpp/NativeFormats/zlibrary/core/src/logger/
  src/main/cpp/NativeFormats/zlibrary/core/src/util/
  src/main/cpp/NativeFormats/zlibrary/core/src/xml/
  src/main/cpp/NativeFormats/zlibrary/text/src/model/
  src/main/cpp/NativeFormats/zlibrary/text/src/fonts/)

find_library(z-lib z)
find_library(log-lib log)
target_link_libraries(NativeFormats-v4 expat ${z-lib} ${log-lib})
