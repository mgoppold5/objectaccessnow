/*
 * Copyright (c) 2016-2017 Mike Goppold von Lobsdorf
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
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package unnamed.file.system;

import unnamed.common.*;

public class FileNode2Utils {
	public static StoragePathAccess getInnerFileSystem(
		FileNode2 fpNode) {
		
		int i;
		
		if(fpNode == null) return null;
		
		if(fpNode.theType != FileNodeTypes.TYPE_FILE_PATH)
			throw makeObjectUnexpected(null);
		
		i = 0;
		if(fpNode.openFileSystemStack != null)
			i = fpNode.openFileSystemStack.size();
		
		if(i < 1) return getInnerFileSystem(
			(FileNode2) fpNode.parent);
		
		return (StoragePathAccess) fpNode.openFileSystemStack.get(i - 1);
	}
	
	public static void collectFilePathStrings(
		CommonArrayList dstList,
		FileNode2 fpNode) {
		
		int i;
		
		if(fpNode == null) return;
		
		if(fpNode.theType != FileNodeTypes.TYPE_FILE_PATH)
			throw makeObjectUnexpected(null);
		
		i = 0;
		if(fpNode.openFileSystemStack != null)
			i = fpNode.openFileSystemStack.size();
		
		if(i > 0) return;
		
		collectFilePathStrings(
			dstList,
			(FileNode2) fpNode.parent);
		
		dstList.add(fpNode.sortObject);
		return;
	}
	
	public static FileNode2 createFilePathNode(
		FileNode2 parentNode, CommonInt32Array subPath) {
		
		FileNode2 targetNode;
		SortParams sortRec;
		
		sortRec = null;

		if(subPath == null)
			throw makeNullPointerException(null);
		
		if(parentNode != null) {
			sortRec = makeSortParams();

			if(parentNode.children == null)
				parentNode.children = makeArrayList();

			SortUtils.int32StringBinaryLookupSimple(parentNode.children, subPath, sortRec);

			if(sortRec.foundExisting) {
				targetNode = (FileNode2) parentNode.children.get(sortRec.index);
				if(targetNode.theType != FileNodeTypes.TYPE_FILE_PATH)
					throw makeObjectUnexpected(null);

				return (FileNode2) targetNode;
			}
		}
		
		targetNode = new FileNode2();
		targetNode.theType = FileNodeTypes.TYPE_FILE_PATH;
		targetNode.sortObject = CommonIntArrayUtils.copy32(subPath);
		targetNode.parent = parentNode;
		
		if(parentNode != null)
			parentNode.children.addAt(sortRec.index, targetNode);

		return (FileNode2) targetNode;
	}

	public static boolean hasOpenNormalFiles(
		FileNode2 fpNode) {
		
		if(fpNode.openNormalFileList != null)
		if(fpNode.openNormalFileList.size() > 0)
			return true;

		return false;
	}

	public static boolean hasOpenFileSystems(
		FileNode2 fpNode) {
		
		if(fpNode.openFileSystemStack != null)
		if(fpNode.openFileSystemStack.size() > 0)
			return true;

		return false;
	}
	
	public static boolean hasOpenThingsInSubTree(
		FileNode2 fpNode, boolean ignoreRoot) {
		
		int i;
		int count;
		CommonArrayList children;
		FileNode2 child;
		
		if(!ignoreRoot) {
			if(hasOpenNormalFiles(fpNode)) return true;
			if(hasOpenFileSystems(fpNode)) return true;
		}
		
		children = fpNode.children;
		if(children == null) return false;

		i = 0;
		count = children.size();
		while(i < count) {
			child = (FileNode2) children.get(i);
			
			if(child.theType == FileNodeTypes.TYPE_FILE_PATH)
			if(hasOpenThingsInSubTree(
				(FileNode2) child, false))
					return true;
			
			i += 1;
		}
		
		return false;
	}
	
	public static int getFileType(
		FileNode2 fpNode, CommonInt32Array subPath,
		int internalSepChar) {
		
		StoragePathAccess fs;
		CommonArrayList pathList;
		CommonInt32Array path2;
		
		fs = getInnerFileSystem(fpNode);
		if(fs == null)
			throw makeObjectNotFound(null);
		
		pathList = makeArrayList();
		collectFilePathStrings(pathList, fpNode);
		if(subPath != null) pathList.add(subPath);
		path2 = PathUtils.combineManyPaths(
			pathList, internalSepChar);
		if(path2 != null)
			StringUtils.int32StringReplaceChar(
				path2,
				internalSepChar,
				fs.getSeparatorChar());
		
		return fs.getFileType(path2);
	}
	
	public static void createFile(
		FileNode2 fpNode, CommonInt32Array subPath,
		int internalSepChar) {
		
		boolean ok;
		StoragePathAccess fs;
		CommonArrayList pathList1;
		CommonArrayList pathList2;
		CommonInt32Array path2;
		CommonInt32Array path3;
		CommonInt32Array path4;
		int fileType;
		int fileType2;

		int i;
		int count;
		boolean didAddPart;
		boolean isLeafPart;
		
		fs = getInnerFileSystem(fpNode);
		if(fs == null)
			throw makeObjectNotFound(null);

		pathList1 = makeArrayList();
		collectFilePathStrings(pathList1, fpNode);

		pathList2 = PathUtils.splitPath(subPath, internalSepChar);

		i = 0;
		count = pathList2.size();
		
		if(count == 0)
		while(true) {
			path2 = PathUtils.combineManyPaths(
				pathList1, internalSepChar);
			if(path2 != null)
				StringUtils.int32StringReplaceChar(
					path2,
					internalSepChar,
					fs.getSeparatorChar());
			fileType = fs.getFileType(path2);
			
			if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE)
				break;
			
			throw makeObjectUnexpected(null);
		}
		
		didAddPart = true;
		if(count > 0)
		while(i < count) {
			isLeafPart = (i + 1 >= count);
			
			if(!didAddPart) {
				pathList1.add(pathList2.get(i));
				i += 1;
				didAddPart = true;
				continue;
			}
			
			path2 = PathUtils.combineManyPaths(
				pathList1, internalSepChar);
			if(path2 != null)
				StringUtils.int32StringReplaceChar(
					path2,
					internalSepChar,
					fs.getSeparatorChar());
			fileType = fs.getFileType(path2);
			
			path3 = (CommonInt32Array) pathList2.get(i);
			path4 = PathUtils.combine2OptionalPaths(
				path2, path3, fs.getSeparatorChar());
			
			if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
				throw makeObjectUnexpected(null);
			
			fileType2 = fs.getFileType(path4);

			if(isLeafPart) {
				if(fileType2 == FileTypes.FILE_TYPE_NORMAL_FILE)
					break;

				if(fileType2 == FileTypes.FILE_TYPE_NOT_EXIST) {
					fs.createFile(path2, path3);
					break;
				}

				throw makeObjectUnexpected(null);
			}

			if(fileType2 == FileTypes.FILE_TYPE_DIRECTORY) {
				didAddPart = false;
				continue;
			}

			if(fileType2 == FileTypes.FILE_TYPE_NOT_EXIST) {
				fs.createDirectory(path2, path3);
				didAddPart = false;
				continue;
			}

			throw makeObjectUnexpected(null);
		}
		
		return;
	}

	public static FileRef2 openNormalFile(
		FileNode2 fpNode, short accessRights,
		int internalSepChar) {
		
		boolean ok;
		StoragePathAccess fs;
		CommonArrayList pathList;
		CommonInt32Array path2;
		
		ok = true;
		
		if(ok)
		if(hasOpenFileSystems(fpNode))
			ok = false;
		
		if(ok)
		if(hasOpenThingsInSubTree(fpNode, true))
			ok = false;
		
		if(!ok)
			throw makeObjectUnexpected(null);
		
		fs = getInnerFileSystem(fpNode);
		if(fs == null)
			throw makeObjectNotFound(null);
		
		pathList = makeArrayList();
		collectFilePathStrings(pathList, fpNode);
		path2 = PathUtils.combineManyPaths(
			pathList, internalSepChar);
		if(path2 != null)
			StringUtils.int32StringReplaceChar(
				path2,
				internalSepChar,
				fs.getSeparatorChar());
		
		FileRef2 fileDat = new FileRef2();
		fileDat.fileHandle = fs.openFile(path2, accessRights);
		fileDat.fs = fs;
		fileDat.filePath = path2;
		
		if(fpNode.openNormalFileList == null)
			fpNode.openNormalFileList = makeArrayList();
		fpNode.openNormalFileList.add(fileDat);
		
		return fileDat;
	}
	
	public static void closeNormalFile(
		FileNode2 fpNode, FileRef2 fileDat) {
		
		CommonArrayList openFiles;

		if(fileDat.fileHandle != HandleSet.INVALID_HANDLE)
			fileDat.fs.closeFile(fileDat.fileHandle);
		fileDat.fileHandle = HandleSet.INVALID_HANDLE;

		openFiles = fpNode.openNormalFileList;
		if(openFiles != null)
			ListUtils.removeObjectFromList(openFiles, fileDat);
		
		return;
	}

	private static CommonError makeNullPointerException(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_NULL_POINTER;
		e1.msg = msg;
		return e1;
	}

	private static CommonError makeObjectUnexpected(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNEXPECTED_OBJECT;
		e1.msg = msg;
		return e1;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null) return new IllegalStateException(msg);
		return new IllegalStateException();
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private static CommonError makeObjectNotFound(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e1.msg = msg;
		return e1;
	}
	
	private CommonError makeIndexOutOfBounds(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e1.msg = msg;
		return e1;
	}
	
	private static CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private TextRange makeTextRange() {
		TextRange tr;
		
		tr = new TextRange();
		tr.init();
		return tr;
	}
	
	private static SortParams makeSortParams() {
		SortParams sortRec;

		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}
}
