/*
 * Copyright (c) 2016 Mike Goppold von Lobsdorf
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

public class FileNodeUtils {
	public static FileSystem getInnerFileSystem(
		FileNode fpNode) {
		
		int i;
		
		if(fpNode == null) return null;
		
		if(fpNode.theType != FileNodeTypes.TYPE_FILE_PATH)
			throw makeObjectUnexpected(null);
		
		i = 0;
		if(fpNode.openFileSystemStack != null)
			i = fpNode.openFileSystemStack.size();
		
		if(i < 1) return getInnerFileSystem(
			(FileNode) fpNode.parent);
		
		return (FileSystem) fpNode.openFileSystemStack.get(i - 1);
	}
	
	public static void collectFilePathStrings(
		CommonArrayList dstList,
		FileNode fpNode) {
		
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
			(FileNode) fpNode.parent);
		
		dstList.add(fpNode.sortObject);
		return;
	}
	
	public static FileNode createFilePathNode(
		FileNode parentNode, CommonInt32Array subPath) {
		
		FileNode targetNode;
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
				targetNode = (FileNode) parentNode.children.get(sortRec.index);
				if(targetNode.theType != FileNodeTypes.TYPE_FILE_PATH)
					throw makeObjectUnexpected(null);

				return (FileNode) targetNode;
			}
		}
		
		targetNode = new FileNode();
		targetNode.theType = FileNodeTypes.TYPE_FILE_PATH;
		targetNode.sortObject = CommonIntArrayUtils.copy32(subPath);
		targetNode.parent = parentNode;
		
		if(parentNode != null)
			parentNode.children.addAt(sortRec.index, targetNode);

		return (FileNode) targetNode;
	}

	public static boolean hasOpenNormalFiles(
		FileNode fpNode) {
		
		if(fpNode.openNormalFileList != null)
		if(fpNode.openNormalFileList.size() > 0)
			return true;

		return false;
	}

	public static boolean hasOpenFileSystems(
		FileNode fpNode) {
		
		if(fpNode.openFileSystemStack != null)
		if(fpNode.openFileSystemStack.size() > 0)
			return true;

		return false;
	}
	
	public static boolean hasOpenThingsInSubTree(
		FileNode fpNode, boolean ignoreRoot) {
		
		int i;
		int count;
		CommonArrayList children;
		FileNode child;
		
		if(!ignoreRoot) {
			if(hasOpenNormalFiles(fpNode)) return true;
			if(hasOpenFileSystems(fpNode)) return true;
		}
		
		children = fpNode.children;
		if(children == null) return false;

		i = 0;
		count = children.size();
		while(i < count) {
			child = (FileNode) children.get(i);
			
			if(child.theType == FileNodeTypes.TYPE_FILE_PATH)
			if(hasOpenThingsInSubTree(
				(FileNode) child, false))
					return true;
			
			i += 1;
		}
		
		return false;
	}
	
	public static int getFileType(
		FileNode fpNode, CommonInt32Array subPath,
		int internalSepChar) {
		
		FileSystem fs;
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
	
	public static FileRef openNormalFile(
		FileNode fpNode, short accessRights,
		int internalSepChar) {
		
		boolean ok;
		FileSystem fs;
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
		
		FileRef fileDat = new FileRef();
		fileDat.fileHandle = fs.openFile(path2, accessRights);
		fileDat.fs = fs;
		fileDat.filePath = path2;
		
		if(fpNode.openNormalFileList == null)
			fpNode.openNormalFileList = makeArrayList();
		fpNode.openNormalFileList.add(fileDat);
		
		return fileDat;
	}
	
	public static void closeNormalFile(
		FileNode fpNode, FileRef fileDat) {
		
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
