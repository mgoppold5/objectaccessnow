#
# Copyright (c) 2017 Mike Goppold von Lobsdorf
# 
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#

import os


def appendPath(path1, path2, sep):
	if(path1 is None):
		return path2
	if(path2 is None):
		return path1
	return path1 + sep + path2

def join(path1, path2):
	return appendPath(path1, path2, "/")

def getClassPath(jarNames):
	jarExt = ".jar"
	
	classPath = None

	i = 0
	count = len(jarNames)
	
	while(i < count):
		pth1 = jarNames[i] + jarExt
		
		if(os.path.isfile(pth1)):
			classPath = appendPath(classPath, pth1, ":")
			i += 1
			continue

		pth2 = join("../extra libraries", jarNames[i] + jarExt)

		if(os.path.isfile(pth2)):
			classPath = appendPath(classPath, pth2, ":")
			i += 1
			continue
		
		return None
	
	return classPath


def main():
	QUOTE = "\""

	env = os.environ
	path1 = env["JRE_HOME"]
	cmd1 = path1 + "/bin/java"

	useTest = False

	classPath = None

	if(classPath is None):
		jarNames = ["test"]
		classPath = getClassPath(jarNames)

	if(classPath is None):
		jarNames = ["common", "file-system", "compiler-proper", "compiler-driver"]
		classPath = getClassPath(jarNames)
		
	if(classPath is None):
		print("program not found")
		return
	
	cmdModule = "TranslateOldTest1"
	
	cmd1 = cmd1 + " -cp " + QUOTE + classPath + QUOTE
	cmd1 = cmd1 + " " + cmdModule


	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		testDir1 = "../unnamed common in java/JavaSources"
		#testDir1 = "../unnamed file system in java/JavaSources"
		#testDir1 = "../unnamed c family compiler in java/JavaSources"
		testDir2 = QUOTE + testDir1 + QUOTE

		testOutDir1 = "out"
		testOutDir2 = QUOTE + testOutDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2

			+ " --create-file-path-node " + testOutDir2
			+ " --open-local-file-system " + testOutDir2

			+ " --grammar-dir " + grammarDir2
			+ " --grammar-file c-sugar-simple-1-0.grm"
			#+ " --grammar-file c-sugar-prec-mess-1-0.grm"

			+ " --test-dir " + testDir2
			+ " --is-target false --is-outside false --is-cache false"
			+ " --file-name-type java"

			+ " --logic-file BaseError"
			+ " --logic-file BitMaskUtils"
			+ " --logic-file BufferCombo"
			+ " --logic-file BufferElement"
			+ " --logic-file BufferFastCopyUtils"
			+ " --logic-file BufferNode"
			+ " --logic-file BufferUtils"
			+ " --logic-file CharacterEncoding"
			+ " --logic-file CharacterEncodingUtils"
			+ " --logic-file CommonArrayList"
			#+ " --logic-file CommonArrayList2"
			+ " --logic-file CommonArrayList3"
			+ " --logic-file CommonBoolean"
			+ " --logic-file CommonError"
			+ " --logic-file CommonErrorTypes"
			+ " --logic-file CommonInt32"
			+ " --logic-file CommonInt64"
			+ " --logic-file CommonInt8Array"
			+ " --logic-file CommonInt16Array"
			+ " --logic-file CommonInt32Array"
			+ " --logic-file CommonInt64Array"
			+ " --logic-file CommonNode"
			+ " --logic-file CompareParams"
			+ " --logic-file ContextNode"
			+ " --logic-file FileSystemError"
			+ " --logic-file FileSystemIoError"
			+ " --logic-file FlagUtils"
			+ " --logic-file LimbElement"
			+ " --logic-file LimbUtils"
			+ " --logic-file ListUtils"
			#+ " --logic-file JavaListUtils"
			+ " --logic-file ObjectRef"
			+ " --logic-file PathUtils"
			+ " --logic-file PowerUnitUtils"
			+ " --logic-file SortParams"
			+ " --logic-file SortUtils"
			+ " --logic-file StorageAccessTypes"
			+ " --logic-file StorageBlockCursor"
			+ " --logic-file StorageCursorAccess"
			+ " --logic-file StorageRangeAccess"
			+ " --logic-file StorageRangeMetrics"
			+ " --logic-file StreamIndex"
			+ " --logic-file StringUtils"
			+ " --logic-file TextIndex"
			+ " --logic-file TextRange"
			+ " --logic-file TypeAndObject"

			+ " --test-dir " + testOutDir2
			+ " --is-target true --is-outside false --is-cache false"
			+ " --file-name-type java"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system("mkdir -p " + testOutDir2)
		os.system(cmd2)


	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		#testDir1 = "../unnamed common in java/JavaSources"
		testDir1 = "../unnamed file system in java/JavaSources"
		#testDir1 = "../unnamed c family compiler in java/JavaSources"
		testDir2 = QUOTE + testDir1 + QUOTE

		testOutDir1 = "out"
		testOutDir2 = QUOTE + testOutDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2
			
			+ " --create-file-path-node " + testOutDir2
			+ " --open-local-file-system " + testOutDir2

			+ " --grammar-dir " + grammarDir2

			+ " --grammar-file c-sugar-simple-1-0.grm"
			#+ " --grammar-file c-sugar-prec-mess-1-0.grm"

			+ " --test-dir " + testDir2
			+ " --is-target false --is-outside false --is-cache false"
			+ " --file-name-type java"

			+ " --logic-file AccessRights"
			+ " --logic-file FileNode2"
			+ " --logic-file FileNode2Utils"
			+ " --logic-file FileNode"
			+ " --logic-file FileNodeTypes"
			+ " --logic-file FileNodeUtils"
			+ " --logic-file FileRecord2"
			+ " --logic-file FileRecord"
			+ " --logic-file FileRef2"
			+ " --logic-file FileRef"
			+ " --logic-file FileSystem"
			+ " --logic-file FileTypes"
			+ " --logic-file HandleRecord"
			+ " --logic-file HandleSet"
			#+ " --logic-file LocalFileSystem"
			+ " --logic-file LocalStoragePath"
			+ " --logic-file StoragePathAccess"
			+ " --logic-file StorageRangeFromPath"

			+ " --test-dir " + testOutDir2
			+ " --is-target true --is-outside false --is-cache false"
			+ " --file-name-type java"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system("mkdir -p " + testOutDir2)
		os.system(cmd2)


	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		#testDir1 = "../unnamed common in java/JavaSources"
		#testDir1 = "../unnamed file system in java/JavaSources"
		testDir1 = "../unnamed c family compiler in java/JavaSources"
		testDir2 = QUOTE + testDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2
			+ " --grammar-dir " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2
			+ " --test-dir " + testDir2

			+ " --grammar-file c-sugar-simple-1-0.grm"
			#+ " --grammar-file c-sugar-prec-mess-1-0.grm"

			+ " --is-target false --is-outside false --is-cache false"
			+ " --file-name-type java"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system(cmd2)


	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		#testDir1 = "../unnamed common in java/JavaSources"
		#testDir1 = "../unnamed file system in java/JavaSources"
		testDir1 = "../unnamed c family compiler in java/JavaSources"
		testDir2 = QUOTE + testDir1 + QUOTE

		testOutDir1 = "out"
		testOutDir2 = QUOTE + testOutDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2

			+ " --create-file-path-node " + testOutDir2
			+ " --open-local-file-system " + testOutDir2

			+ " --grammar-dir " + grammarDir2
			+ " --grammar-file c-sugar-simple-1-0.grm"
			#+ " --grammar-file c-sugar-prec-mess-1-0.grm"

			+ " --test-dir " + testDir2
			+ " --is-target false --is-outside false --is-cache false"
			+ " --file-name-type java"
			+ " --logic-files-all"

			+ " --test-dir " + testOutDir2
			+ " --is-target true --is-outside false --is-cache false"
			+ " --file-name-type java"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system("mkdir -p " + testOutDir2)
		os.system(cmd2)


	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		#testDir1 = "../unnamed common in java/JavaSources"
		#testDir1 = "../unnamed file system in java/JavaSources"
		testDir1 = "../test java compiler translate simple1/tests/csugar/simple"
		testDir2 = QUOTE + testDir1 + QUOTE

		testOutDir1 = "out"
		testOutDir2 = QUOTE + testOutDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2

			+ " --create-file-path-node " + testOutDir2
			+ " --open-local-file-system " + testOutDir2

			+ " --grammar-dir " + grammarDir2
			+ " --grammar-file c-sugar-simple-1-0.grm"
			#+ " --grammar-file c-sugar-prec-mess-1-0.grm"
			#+ " --grammar-file c-sugar-list-1-0.grm"

			+ " --test-dir " + testDir2
			+ " --is-target false --is-outside false --is-cache false"
			+ " --file-name-type c"
			+ " --logic-files-all"

			+ " --test-dir " + testOutDir2
			+ " --is-target true --is-outside false --is-cache false"
			+ " --file-name-type c"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system("mkdir -p " + testOutDir2)
		os.system(cmd2)


	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		testDir1 = "../test java compiler translate regular1/tests/multiple"
		testDir2 = QUOTE + testDir1 + QUOTE

		testOutDir1 = "out"
		testOutDir2 = QUOTE + testOutDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2

			+ " --create-file-path-node " + testOutDir2
			+ " --open-local-file-system " + testOutDir2

			+ " --grammar-dir " + grammarDir2
			+ " --grammar-file c-sugar-list-1-0.grm"

			+ " --is-target false --is-outside false --is-cache false"
			+ " --test-dir " + testDir2
			+ " --file-name-type c"
			+ " --logic-files-all"

			+ " --test-dir " + testOutDir2
			+ " --is-target true --is-outside false --is-cache false"
			+ " --file-name-type c"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system("mkdir -p " + testOutDir2)
		os.system(cmd2)

	if(False):
		grammarDir1 = "../test java compiler translate simple1/grammars"
		grammarDir2 = QUOTE + grammarDir1 + QUOTE

		#testDir1 = "../unnamed common in java/JavaSources"
		#testDir1 = "../unnamed file system in java/JavaSources"
		testDir1 = "../test java compiler translate regular1/tests/java"
		testDir2 = QUOTE + testDir1 + QUOTE

		testOutDir1 = "out"
		testOutDir2 = QUOTE + testOutDir1 + QUOTE

		cmd2 = (
			cmd1

			+ " --create-file-path-node " + grammarDir2
			+ " --open-local-file-system " + grammarDir2

			+ " --create-file-path-node " + testDir2
			+ " --open-local-file-system " + testDir2

			+ " --create-file-path-node " + testOutDir2
			+ " --open-local-file-system " + testOutDir2

			+ " --grammar-dir " + grammarDir2
			+ " --grammar-file c-sugar-simple-1-0.grm"
			#+ " --grammar-file c-sugar-prec-mess-1-0.grm"
			#+ " --grammar-file c-sugar-list-1-0.grm"

			+ " --test-dir " + testDir2
			+ " --is-target false --is-outside false --is-cache false"
			+ " --file-name-type c"
			+ " --logic-files-all"

			+ " --test-dir " + testOutDir2
			+ " --is-target true --is-outside false --is-cache false"
			+ " --file-name-type c"
			+ " --logic-files-all"

			+ " --translate"
			)

		os.system("mkdir -p " + testOutDir2)
		os.system(cmd2)


main()
