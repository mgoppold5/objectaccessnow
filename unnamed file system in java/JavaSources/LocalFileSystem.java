/*
 * Copyright (c) 2013-2017 Mike Goppold von Lobsdorf
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

import java.io.*;
import unnamed.common.*;

public class LocalFileSystem implements FileSystem {
	public boolean enableIoLocking;
	
	private short accessRights;
	private int separatorChar;
	private CommonInt32Array basePath;

	private Object fsLock;
	private HandleSet hs;
	private int handleEra;
	
	private boolean haveHandleEraInit;
	
	public void initRights(short rights)
	{
		accessRights = rights;
	}

	public void initSeparatorChar(int sepChar)
	{
		separatorChar = sepChar;
	}

	public void initBasePath(CommonInt32Array path)
	{
		basePath = CommonIntArrayUtils.copy32(path);
	}

	public void init() {
		fsLock = new Object();
		hs = null;
		handleEra = 0;
		return;
	}
	
	// filesystem init/deinit
	public void initFileNumberingEra(int eraNum)
	{
		if(haveHandleEraInit)
			throw new IllegalStateException("already initialized");
		
		hs = new HandleSet();
		hs.init();
		handleEra = eraNum;
		haveHandleEraInit = true;
	}

	public void deinitFileNumberingEra()
	{
		closeAllFiles();
		hs = null;
		haveHandleEraInit = false;
	}
		
	public short getRights() {
		return accessRights;
	}

	public int getSeparatorChar() {
		return separatorChar;
	}

	public int getFileNumberingEra() {
		int i = 0;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			i = handleEra;
		}
		
		if(!doLock) {
			i = handleEra;
		}
		
		return i;
	}
	
	public CommonInt32Array getBasePath() {
		return basePath;
	}
	
	private void closeAllFiles() {
		int h;
		int count;
		
		if(hs == null) return;
		
		while(true) {
			count = hs.count();
			if(count == 0) break;

			h = hs.getHandleAtIndex(0);
			closeFile(h);
		}
		
		return;
	}
	
	public void setFileNumbering(HandleSet hs, int era) {
		boolean doLock = enableIoLocking;

		if(doLock) synchronized(fsLock) {
			closeAllFiles();
			this.hs = hs;
			this.handleEra = era;
		}

		if(!doLock) {
			closeAllFiles();
			this.hs = hs;
			this.handleEra = era;
		}
		
		return;
	}
	
	public void close() {
		boolean doLock = enableIoLocking;

		if(doLock) synchronized(fsLock) {
			closeAllFiles();
		}

		if(!doLock) {
			closeAllFiles();
		}
		
		return;
	}
	
	private int getLocalFileType(CommonInt32Array path) {
		if(path == null) return FileTypes.FILE_TYPE_NOT_EXIST;

		String path2 = StringUtils.javaStringFromInt32String(path);
		File f = new File(path2);
		
		if(!f.exists()) return FileTypes.FILE_TYPE_NOT_EXIST;
		if(f.isDirectory()) return FileTypes.FILE_TYPE_DIRECTORY;
		if(f.isFile()) return FileTypes.FILE_TYPE_NORMAL_FILE;
		return FileTypes.FILE_TYPE_OTHER;
	}
	
	private CommonInt32Array combinePaths(CommonInt32Array path, CommonInt32Array extend) {
		return PathUtils.combine2OptionalPaths(path, extend, separatorChar);
	}
	
	private CommonArrayList listFromJavaStringArray(
		String[] a) {
		
		int i;
		int count;
		CommonArrayList lst;
		
		if(a == null) return null;
		
		lst = makeArrayList();
		
		count = a.length;
		i = 0;
		while(i < count) {
			lst.add(
				StringUtils.int32StringFromJavaString(
					a[i]));
			i += 1;
		}
		
		return lst;
	}
	
	private String[] nativeListDirectory(CommonInt32Array path) {
		String path2 = StringUtils.javaStringFromInt32String(path);
		File f = new File(path2);
		return f.list();
	}
	
	private boolean nativeCreateDirectory(CommonInt32Array path) {
		String path2 = StringUtils.javaStringFromInt32String(path);
		File f = new File(path2);
		return f.mkdir();
	}
	
	private boolean nativeDeleteDirectory(CommonInt32Array path) {
		String path2 = StringUtils.javaStringFromInt32String(path);
		File f = new File(path2);
		return f.delete();
	}
	
	private boolean nativeDeleteFile(CommonInt32Array path) {
		String path2 = StringUtils.javaStringFromInt32String(path);
		File f = new File(path2);
		return f.delete();
	}
	
	public CommonArrayList listDirectory(CommonInt32Array path) {
		CommonInt32Array p;
		String[] files;
		boolean found;
		int fileType;
		
		found = false;
		files = null;

		p = basePath;
		fileType = getLocalFileType(p);
		
		if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE) {
			if(path == null) {
				found = true;
				files = new String[0];
			}
		}
		
		if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
			p = combinePaths(p, path);
			fileType = getLocalFileType(p);
			
			if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
				found = true;
				files = nativeListDirectory(p);
			}
		}

		if(!found)
			throw makeDirectoryNotFound("Could not list directory");
		
		if(files == null)
			throw makeNullPointerException(null);
		
		return listFromJavaStringArray(files);
	}
	
	public int getFileType(CommonInt32Array path) {
		CommonInt32Array path2 = combinePaths(basePath, path);
		return getLocalFileType(path2);
	}
	
	public void createDirectory(CommonInt32Array parentDir, CommonInt32Array name) {
		CommonInt32Array p;
		int fileType;
		
		if(name == null)
			throw makeNullPointerException(null);

		p = combinePaths(basePath, parentDir);
		fileType = getLocalFileType(p);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeDirectoryNotFound(
				"Could not create directory, since parent does not exist");
		
		p = combinePaths(p, name);
		fileType = getLocalFileType(p);
		if(fileType != FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeAlreadyExistError(
				"Could not create directory");
		
		if(!nativeCreateDirectory(p))
			throw makeIoError("Could not create directory", null);
		
		return;
	}
	
	public void deleteEmptyDirectory(CommonInt32Array path) {
		CommonInt32Array p;
		int fileType;

		p = combinePaths(basePath, path);
		fileType = getLocalFileType(p);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeDirectoryNotFound(
				"Could not remove directory, because it does not exist");
		
		if(!nativeDeleteDirectory(p))
			throw makeIoError("Could not remove directory", null);

		return;
	}
	
	private void nativeCreateEmptyFile(CommonInt32Array path) {
		String path2 = StringUtils.javaStringFromInt32String(path);
		RandomAccessFile rf = null;
		
		try {
			rf = new RandomAccessFile(path2, "rw");
		} catch(FileNotFoundException e2) {
			throw makeFileNotFound("Could not create file");
		}

		try {
			if(rf != null) rf.close();
		} catch(IOException e2) {
			throw makeIoError("Could not close a new file", e2);
		}
		
		return;
	}
	
	public void createFile(CommonInt32Array parentDir, CommonInt32Array name) {
		CommonInt32Array p;
		int fileType;

		if(name == null)
			throw makeNullPointerException(null);
		
		p = combinePaths(basePath, parentDir);
		fileType = getLocalFileType(p);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeDirectoryNotFound(
				"Could not create file,"
				+ " because the parent directory does not exist");
		
		p = combinePaths(p, name);
		fileType = getLocalFileType(p);
		if(fileType != FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeAlreadyExistError(
				"Could not create file");
		
		nativeCreateEmptyFile(p);
		return;
	}
	
	public void deleteFile(CommonInt32Array path) {
		CommonInt32Array p;
		int fileType;
		
		if(path == null)
			throw makeNullPointerException(null);
		
		p = combinePaths(basePath, path);
		fileType = getLocalFileType(p);
		if(fileType != FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeFileNotFound("File to delete does not exist");
		
		if(!nativeDeleteFile(p))
			throw makeIoError("Could not delete file", null);

		return;
	}
		
	public int openFile(CommonInt32Array path, short desiredRights) {
		CommonInt32Array p;
		File f;
		int fileType;
		boolean writeAccess;
		boolean readAccess;
		String permissions;
		boolean found;
		boolean doLock = enableIoLocking;
		short rights;
		
		found = false;
		
		p = basePath;
		fileType = getLocalFileType(p);
		if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE) {
			if(path == null) found = true;
		}
		
		if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
			p = combinePaths(p, path);
			fileType = getLocalFileType(p);
			if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE)
				found = true;
		}

		if(!found)
			throw makeFileNotFound("File to open does not exist");
			
		rights = (short) (desiredRights & accessRights);
		
		writeAccess = ((rights & AccessRights.ACCESS_WRITE) != 0);
		readAccess = ((rights & AccessRights.ACCESS_READ) != 0);

		permissions = null;

		if(readAccess) {
			if(writeAccess) permissions = "rw";
			if(!writeAccess) permissions = "r";
		}
		
		if(!readAccess) {
			if(writeAccess) permissions = "w";
			if(!writeAccess) permissions = "";
		}

		FileRecord fh = new FileRecord();
		fh.desiredAccessRights = desiredRights;
		fh.accessRights = rights;
		fh.fLock = new Object();
		fh.path = p;
		
		try {
			fh.raFile = new RandomAccessFile(
				StringUtils.javaStringFromInt32String(p),
				permissions);
		} catch(FileNotFoundException e2) {
			throw makeFileNotFound("Could not open file");
		}

		if(doLock) synchronized(fsLock) {
			fh.handle = hs.alloc(fh);
		}
		
		if(!doLock) {
			fh.handle = hs.alloc(fh);
		}
		
		return fh.handle;
	}
	
	private FileRecord getFileRecordFromHandle(int fileHandle) {
		return (FileRecord) hs.getObject(fileHandle);
	}
	
	private void nativeCloseFile(RandomAccessFile raFile) {
		try {
			if(raFile != null) {
				raFile.close();
			}
		} catch(IOException e2) {
			throw makeIoError("Could not close file", e2);
		}
		
		return;
	}
	
	public void closeFile(int fileHandle) {
		int h;
		FileRecord fh = null;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}

		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}
		
		if(doLock) synchronized(fh.fLock) {
			nativeCloseFile(fh.raFile);
			fh.raFile = null;
		}

		if(!doLock) {
			nativeCloseFile(fh.raFile);
			fh.raFile = null;
		}
		
		if(doLock) synchronized(fsLock) {
			h = fh.handle;
			hs.free(h);
		}

		if(!doLock) {
			h = fh.handle;
			hs.free(h);
		}
		
		return;
	}
	
	private long nativeGetFileLength(RandomAccessFile raFile) {
		long len = 0;
		
		try {
			len = raFile.length();
		} catch(IOException e2) {
			throw makeIoError("Could not get file length", e2);
		}
		
		return len;
	}
	
	public long getFileLength(int fileHandle) {
		FileRecord fh = null;
		long len = 0;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}
		
		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}
		
		if(doLock) synchronized(fh.fLock) {
			len = nativeGetFileLength(fh.raFile);
		}

		if(!doLock) {
			len = nativeGetFileLength(fh.raFile);
		}
		
		return len;
	}

	private void nativeSetFileLength(
		RandomAccessFile raFile, long len) {
		
		try {
			raFile.setLength(len);
		} catch(IOException e2) {
			throw makeIoError("Could not set file length", e2);
		}
		
		return;
	}
	
	public void setFileLength(int fileHandle, long len) {
		FileRecord fh = null;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}

		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}
		
		if(doLock) synchronized(fh.fLock) {
			nativeSetFileLength(fh.raFile, len);
		}

		if(!doLock) {
			nativeSetFileLength(fh.raFile, len);
		}
		
		return;
	}	

	private long nativeGetFilePointer(RandomAccessFile raFile) {
		long pos = 0;
		
		try {
			pos = raFile.getFilePointer();
		} catch(IOException e2) {
			throw makeIoError("Could not get file pointer", e2);
		}
		
		return pos;
	}

	public long getFilePointer(int fileHandle) {
		FileRecord fh = null;
		long pos = 0;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);		
		}

		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);		
		}
		
		if(doLock) synchronized(fh.fLock) {
			pos = nativeGetFilePointer(fh.raFile);
		}

		if(!doLock) {
			pos = nativeGetFilePointer(fh.raFile);
		}
		
		return pos;
	}

	private void nativeSetFilePointer(
		RandomAccessFile raFile, long pos) {
		
		try {
			raFile.seek(pos);
		} catch(IOException e2) {
			throw makeIoError("Could not set file pointer", e2);
		}
		
		return;
	}
	
	public void setFilePointer(int fileHandle, long pos) {
		FileRecord fh = null;
		boolean doLock = enableIoLocking;

		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);		
		}
		
		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);		
		}
		
		if(doLock) synchronized(fh.fLock) {
			nativeSetFilePointer(fh.raFile, pos);
		}

		if(!doLock) {
			nativeSetFilePointer(fh.raFile, pos);
		}
		
		return;
	}
	
	private void nativeReadFile(
		RandomAccessFile raFile,
		CommonInt8Array buf, int start, int length) {

		int bytesRead;
		int r;

		bytesRead = 0;
		while(bytesRead < length) {
			try {
				r = raFile.read(buf.aryPtr,
					start + bytesRead,
					length - bytesRead);
			} catch(IOException e2) {
				throw makeIoError("Could not read from file", e2);
			}

			if(r < 0)
				throw makeFileIndexOutOfBounds(
					"Cannot read past end of file");

			bytesRead += r;
		}
		
		return;
	}
	
	public void readFile(int fileHandle,
		CommonInt8Array buf, int start, int length) {
		
		FileRecord fh = null;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);		
		}
		
		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);		
		}
		
		if(doLock) synchronized(fh.fLock) {
			nativeReadFile(fh.raFile, buf, start, length);
		}

		if(!doLock) {
			nativeReadFile(fh.raFile, buf, start, length);
		}
		
		return;
	}
	
	private void nativeWriteFile(
		RandomAccessFile raFile,
		CommonInt8Array buf, int start, int length) {
		
		try {
			raFile.write(buf.aryPtr, start, length);
		} catch(IOException e2) {
			throw makeIoError("Cannot write to file", e2);
		}
		
		return;
	}

	public void writeFile(int fileHandle,
		CommonInt8Array buf, int start, int length) {
		
		FileRecord fh = null;
		boolean doLock = enableIoLocking;
		
		if(doLock) synchronized(fsLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}
		
		if(!doLock) {
			fh = getFileRecordFromHandle(fileHandle);
		}
		
		if(doLock) synchronized(fh.fLock) {
			nativeWriteFile(fh.raFile, buf, start, length);
		}

		if(!doLock) {
			nativeWriteFile(fh.raFile, buf, start, length);
		}
		
		return;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private RuntimeException makeNullPointerException(String msg) {
		if(msg == null) return new NullPointerException();
		return new NullPointerException();
	}
	
	private FileSystemError makeFileIndexOutOfBounds(String msg) {
		FileSystemError e2;
		
		e2 = new FileSystemError();
		e2.id = unnamed.common.CommonErrorTypes.ERROR_FILE_INDEX_OUT_OF_BOUNDS;
		e2.msg = msg;
		return e2;
	}
	
	private FileSystemIoError makeIoError(String msg, Throwable cause) {
		FileSystemIoError e3;
		
		e3 = new FileSystemIoError();
		e3.id = unnamed.common.CommonErrorTypes.ERROR_FS_IO;
		e3.msg = msg;
		//e3.cause = cause;
		return e3;
	}
	
	private FileSystemError makeFileNotFound(String msg) {
		FileSystemError e2;
		
		e2 = new FileSystemError();
		e2.id = unnamed.common.CommonErrorTypes.ERROR_FILE_NOT_FOUND;
		e2.msg = msg;
		return e2;
	}

	private FileSystemError makeDirectoryNotFound(String msg) {
		FileSystemError e2;
		
		e2 = new FileSystemError();
		e2.id = unnamed.common.CommonErrorTypes.ERROR_DIRECTORY_NOT_FOUND;
		e2.msg = msg;
		return e2;
	}
	
	private FileSystemError makeAlreadyExistError(String msg) {
		int id;
		
		FileSystemError e2;
		
		id = unnamed.common.CommonErrorTypes.ERROR_FILE_ALREADY_EXISTS;

		e2 = new FileSystemError();
		e2.id = id;
		e2.msg = msg;
		return e2;
	}
}
