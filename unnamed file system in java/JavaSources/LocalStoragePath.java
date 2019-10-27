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

public class LocalStoragePath
	implements StoragePathAccess {
	
	private boolean haveInit;
	private boolean haveHandleEraInit;

	private short accessRights;
	private int separatorChar;
	private CommonInt32Array basePath;
	private short clientUnitSize;
	private short memoryUnitSize;
	private short strictAlignSize;
	
	private short maxStrictAlignSize;
	
	private Object fsLock;
	
	private HandleSet hs;
	private int handleEra;

	private BufferNode nativeBuf;

	private BufferNode patchBufMem16Var1;
	private BufferNode zeroBufMem16;
	private BufferNode patchBufMem64Var1;
	private BufferNode zeroBufMem64;
	
	private static short NATIVE_UNIT_SIZE = 3;
	private static int INTERNAL_BUF_LEN = 1024 * 1024 * 16;
	
	private CommonInt32Array int32Masks;
	private CommonInt64Array int64Masks;
	
	private int calcLeastCommonMultiple(int a1, int a2) {
		int i;
		int m;
		
		if(a1 <= 0 || a2 <= 0) 
			throw makeOutOfBounds("integer not positive");
		
		i = a1;
		while(true) {
			m = i % a2;
			
			if(m != 0) {
				i += a1;
				continue;
			}
			
			break;
		}
		
		return i;
	}

	public void initRights(short rights)
	{
		if(haveInit) throw new IllegalStateException("already initialized");
		accessRights = rights;
	}
	
	public void initSeparatorChar(int sepChar)
	{
		if(haveInit) throw new IllegalStateException("already initialized");
		separatorChar = sepChar;
	}
	
	public void initBasePath(CommonInt32Array path)
	{
		if(haveInit) throw new IllegalStateException("already initialized");
		//basePath = ArrayUtils.copyInt32Array(path);
		basePath = CommonIntArrayUtils.copy32(path);
	}
	
	public void initClientUnitSize(short size) {
		boolean ok;
		
		ok = false;
		if(size == 0) ok = true;
		if(size >= 2 && size <= 6) ok = true;

		if(ok) clientUnitSize = size;
	}

	public void initMemoryUnitSize(short size) {
		boolean ok;
		
		ok = false;
		if(size == 0) ok = true;
		if(size == 4) ok = true;
		if(size == 6) ok = true;

		if(ok) memoryUnitSize = size;
	}
	
	public void initStrictAlignSize(short size) {
		boolean ok;
		
		ok = false;
		if(size == 0) ok = true;
		if(size >= 4 && size <= 16) ok = true;
		
		if(ok) strictAlignSize = size;
	}

	private BufferNode wrapBuf(Object obj) {
		BufferNode buf = new BufferNode();
		buf.theObject = obj;
		return buf;
	}
	
	private BufferNode createInt8Buf(int len) {
		return wrapBuf(makeInt8Array(len));
	}

	private BufferNode createInt16Buf(int len) {
		return wrapBuf(makeInt16Array(len));
	}

	private BufferNode createInt64Buf(int len) {
		return wrapBuf(makeInt64Array(len));
	}
	
	private void initNativeBuffers() {
		nativeBuf = createInt8Buf(INTERNAL_BUF_LEN / (1 << NATIVE_UNIT_SIZE));
	}
	
	private void initJunk() {
		int32Masks = BitMaskUtils.lowerBitsInit32((short) 32);
		int64Masks = BitMaskUtils.lowerBitsInit64((short) 64);
	}
	
	public void init() {
		fsLock = new Object();
		maxStrictAlignSize = 0;
		
		initNativeBuffers();
		initJunk();
		
		haveInit = true;
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
	
	public void close()
	{
		deinitFileNumberingEra();
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

	// properties
	public boolean getIsThreadSafe() {return false;}
	public short getRights() {return accessRights;}
	public int getSeparatorChar() {return separatorChar;}
	public int getFileNumberingEra() {return handleEra;}

	// functions which deal with a path
	//

	// utilities
	
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
	
	private FileRecord2 getFileRecordFromHandle(int fileHandle) {
		return (FileRecord2) hs.getObject(fileHandle);
	}

	// fs info
	
	public CommonArrayList listDirectory(CommonInt32Array path) {
		CommonInt32Array pth;
		String[] files;
		boolean found;
		int fileType;
		
		found = false;
		files = null;

		pth = basePath;
		fileType = getLocalFileType(pth);
		
		if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE) {
			if(path == null) {
				found = true;
				files = new String[0];
			}
		}
		
		if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
			pth = combinePaths(pth, path);
			fileType = getLocalFileType(pth);
			
			if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
				found = true;
				files = nativeListDirectory(pth);
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

	// adding/removing
	
	public void createDirectory(CommonInt32Array parentDir, CommonInt32Array name) {
		CommonInt32Array pth;
		int fileType;
		
		if(name == null)
			throw makeNullPointerException(null);

		pth = combinePaths(basePath, parentDir);
		fileType = getLocalFileType(pth);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeDirectoryNotFound(
				"Could not create directory, since parent does not exist");
		
		pth = combinePaths(pth, name);
		fileType = getLocalFileType(pth);
		if(fileType != FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeAlreadyExistError(
				"Could not create directory");
		
		if(!nativeCreateDirectory(pth))
			throw makeIoError("Could not create directory", null);
		
		return;
	}
	
	public void deleteEmptyDirectory(CommonInt32Array path) {
		CommonInt32Array pth;
		int fileType;

		pth = combinePaths(basePath, path);
		fileType = getLocalFileType(pth);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeDirectoryNotFound(
				"Could not remove directory, because it does not exist");
		
		if(!nativeDeleteDirectory(pth))
			throw makeIoError("Could not remove directory", null);

		return;
	}
	
	public void createFile(CommonInt32Array parentDir, CommonInt32Array name) {
		CommonInt32Array pth;
		int fileType;

		if(name == null)
			throw makeNullPointerException(null);
		
		pth = combinePaths(basePath, parentDir);
		fileType = getLocalFileType(pth);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeDirectoryNotFound(
				"Could not create file,"
				+ " because the parent directory does not exist");
		
		pth = combinePaths(pth, name);
		fileType = getLocalFileType(pth);
		if(fileType != FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeAlreadyExistError(
				"Could not create file");
		
		nativeCreateEmptyFile(pth);
		return;
	}
	
	public void deleteFile(CommonInt32Array path) {
		CommonInt32Array pth;
		int fileType;
		
		if(path == null)
			throw makeNullPointerException(null);
		
		pth = combinePaths(basePath, path);
		fileType = getLocalFileType(pth);
		if(fileType != FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeFileNotFound("File to delete does not exist");
		
		if(!nativeDeleteFile(pth))
			throw makeIoError("Could not delete file", null);

		return;
	}
	
	// file access
	
	public int openFile(CommonInt32Array path, short desiredRights) {
		CommonInt32Array pth;
		File f;
		int fileType;
		boolean writeAccess;
		boolean readAccess;
		String permissions;
		boolean found;
		short rights;
		
		if(!haveHandleEraInit)
			throw makeIllegalState("not initialized");
		
		found = false;
		
		pth = basePath;
		fileType = getLocalFileType(pth);
		if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE) {
			if(path == null) found = true;
		}
		
		if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
			pth = combinePaths(pth, path);
			fileType = getLocalFileType(pth);
			if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE)
				found = true;
		}

		if(!found)
			throw makeFileNotFound("File to open does not exist");
		
		rights = (short) (desiredRights & accessRights);
		
		writeAccess = ((rights & AccessRights.ACCESS_WRITE) != 0);
		readAccess = ((rights & AccessRights.ACCESS_READ) != 0);

		permissions = "";

		if(readAccess) {
			if(writeAccess) permissions = "rw";
			if(!writeAccess) permissions = "r";
		}
		
		if(!readAccess) {
			if(writeAccess) permissions = "w";
			if(!writeAccess) permissions = "";
		}

		FileRecord2 fh = new FileRecord2();
		fh.desiredAccessRights = desiredRights;
		fh.accessRights = rights;
		fh.fLock = new Object();
		fh.path = pth;

		
		try {
			fh.raFile = new RandomAccessFile(
				StringUtils.javaStringFromInt32String(pth),
				permissions);
		} catch(FileNotFoundException e2) {
			throw makeFileNotFound("Could not open file");
		}

		fh.handle = hs.alloc(fh);

		return fh.handle;
	}
		
	// functions which operate on a file handle
	//
	
	// file init/deinit
	public void initFileClientUnitSize(int fileHandle, short size)
	{
		boolean ok;
		
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);

		if(fh.haveInit)
			throw makeIllegalState("already initialized");
		
		ok = false;
		if(size == 0) ok = true;
		if(size >= 2 && size <= 6) ok = true;
		
		if(!ok) throw makeIllegalState(null);
		
		fh.clientUnitSize = size;
	}
	
	public void initFileMemoryUnitSize(int fileHandle, short size)
	{
		boolean ok;
		
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);

		if(fh.haveInit)
			throw makeIllegalState("already initialized");
		
		ok = false;
		if(size == 0) ok = true;
		if(size == 4) ok = true;
		if(size == 6) ok = true;

		if(!ok) throw makeIllegalState(null);
		
		fh.memoryUnitSize = size;
	}

	public void initFileStrictAlignSize(int fileHandle, short size)
	{
		boolean ok;
		
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);

		if(fh.haveInit)
			throw makeIllegalState("already initialized");
		
		ok = false;
		
		ok = false;
		if(size == 0) ok = true;
		if(size >= 4 && size <= 16) ok = true;

		if(!ok) throw makeIllegalState(null);

		fh.strictAlignSize = size;
	}
	
	private void initPatchBuffers(short newStrictAlignSize) {
		int mult;
		
		mult = 1;
		if(newStrictAlignSize > 4)
			mult = 1 << (newStrictAlignSize - 4);

		patchBufMem16Var1 = createInt16Buf(mult);
		zeroBufMem16 = createInt16Buf(mult);
		myZeroBuf(zeroBufMem16, (short) 4);

		mult = 1;
		if(newStrictAlignSize > 6)
			mult = 1 << (newStrictAlignSize - 6);

		patchBufMem64Var1 = createInt64Buf(mult);
		zeroBufMem64 = createInt64Buf(mult);
		myZeroBuf(zeroBufMem64, (short) 6);
	}
	
	public void initFile(int fileHandle)
	{
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);

		if(fh.haveInit)
			throw makeIllegalState("already initialized");
		
		if(fh.clientUnitSize == 0) fh.clientUnitSize = clientUnitSize;
		if(fh.memoryUnitSize == 0) fh.memoryUnitSize = memoryUnitSize;
		if(fh.strictAlignSize == 0) fh.strictAlignSize = strictAlignSize;

		if(fh.clientUnitSize == 0) fh.clientUnitSize = 4;
		if(fh.memoryUnitSize == 0) fh.memoryUnitSize = 4;
		if(fh.strictAlignSize < fh.clientUnitSize) fh.strictAlignSize = fh.clientUnitSize;
		if(fh.strictAlignSize < fh.memoryUnitSize) fh.strictAlignSize = fh.memoryUnitSize;
		
		if(fh.strictAlignSize > maxStrictAlignSize) {
			initPatchBuffers(fh.strictAlignSize);
			maxStrictAlignSize = fh.strictAlignSize;
		}
		
		// TESTING
		//fh.clientUnitSize = 0;
		// TESTING
		
		fh.haveInit = true;
	}

	public void closeFile(int fileHandle) {
		int h;
		FileRecord2 fh = null;
		
		fh = getFileRecordFromHandle(fileHandle);

		nativeCloseFile(fh.raFile);
		fh.raFile = null;

		h = fh.handle;
		hs.free(h);
	}

	// file properties
	public boolean getFileHaveInit(int fileHandle)
	{
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);

		return fh.haveInit;
	}
	
	public short getFileClientUnitSize(int fileHandle) {
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);
		
		return fh.clientUnitSize;
	}
	
	public short getFileMemoryUnitSize(int fileHandle) {
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);
		
		return fh.memoryUnitSize;
	}

	public short getFileStrictAlignSize(int fileHandle) {
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);
		
		return fh.strictAlignSize;
	}
	
	// file io

	private long nativeGetFileLength(RandomAccessFile raFile) {
		long len = 0;
		
		try {
			len = raFile.length();
		} catch(IOException e2) {
			throw makeIoError("Could not get file length", e2);
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
	
	public long getFileSuggestLength(int fileHandle) {return 0;}
	public void setFileSuggestLength(int fileHandle, long len) {}
	
	private long nativeGetFilePointer(RandomAccessFile raFile) {
		long pos = 0;
		
		try {
			pos = raFile.getFilePointer();
		} catch(IOException e2) {
			throw makeIoError("Could not get file pointer", e2);
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
	
	/*
	public void readFile(int fileHandle,
		CommonInt8Array buf, int start, int length) {
		
		FileRecord fh = getFileRecordFromHandle(fileHandle);		
		nativeReadFile(fh.raFile, buf, start, length);
	}
	*/
	
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
	
	
	//
	// Helper functions for
	// accessFile/getFileLength/setFileLength
	//
	
	private void readFromFileToByteBuf(
		RandomAccessFile raFile, long raFilePointer,
		CommonInt8Array buf, int bufPointer,
		int len) {
		
		nativeSetFilePointer(raFile, raFilePointer);
		
		nativeReadFile(raFile,
			buf, bufPointer, len);

		if(nativeGetFilePointer(raFile)
			!= (raFilePointer + len)) {

			throw makeIllegalState(null);
		}
		
		return;
	}

	private void readFromFileToByteBufWithPadding(
		RandomAccessFile raFile, long raFilePointer,
		CommonInt8Array buf, int bufPointer,
		int len) {
		
		long actualFileLen = nativeGetFileLength(raFile);
		
		if(raFilePointer > actualFileLen)
			throw makeFileIndexOutOfBounds(null);
		
		nativeSetFilePointer(raFile, raFilePointer);
		
		long possibleLen = actualFileLen - raFilePointer;

		int len2 = len;
		if(len2 > possibleLen) len2 = (int) possibleLen;
		
		nativeReadFile(raFile,
			buf, bufPointer, len2);

		if(nativeGetFilePointer(raFile)
			!= (raFilePointer + len2)) {

			throw makeIllegalState(null);
		}
		
		int i = bufPointer + len2;
		int max = bufPointer + len;
		while(i < max) {
			buf.aryPtr[i] = 0;
			i += 1;
		}
		
		return;
	}

	private void writeToFileFromByteBuf(
		RandomAccessFile raFile, long raFilePointer,
		CommonInt8Array buf, int bufPointer,
		int len) {
		
		nativeSetFilePointer(raFile, raFilePointer);
		
		nativeWriteFile(raFile,
			buf, bufPointer, len);

		if(nativeGetFilePointer(raFile)
			!= (raFilePointer + len)) {

			throw makeIllegalState(null);
		}

		return;
	}
	
	private long myGetFileLength(FileRecord2 fh) {
		long len = nativeGetFileLength(fh.raFile);
		
		len = PowerUnitUtils.convertInt64(
			len,
			NATIVE_UNIT_SIZE, fh.clientUnitSize,
			true);
		
		return len;
	}
	
	private boolean myCheckBuf(BufferNode buf, short memoryUnitSize) {
		boolean bad = true;
		
		if(memoryUnitSize == 3)
			bad = false;

		if(memoryUnitSize == 4)
			bad = false;

		if(memoryUnitSize == 5)
			bad = false;

		if(memoryUnitSize == 6)
			bad = false;
			
		return bad;
	}
	
	private int myGetBufCapacity(BufferNode buf,
		short memoryUnitSize, short clientUnitSize) {
		
		int cap;
		Object obj = buf.theObject;
		
		cap = 0;
		
		if(memoryUnitSize == 3) {
			CommonInt8Array arr = (CommonInt8Array) obj;
			cap = arr.length;
		}
		
		if(memoryUnitSize == 4) {
			CommonInt16Array arr = (CommonInt16Array) obj;
			cap = arr.length;
		}

		if(memoryUnitSize == 5) {
			CommonInt32Array arr = (CommonInt32Array) obj;
			cap = arr.length;
		}

		if(memoryUnitSize == 6) {
			CommonInt64Array arr = (CommonInt64Array) obj;
			cap = arr.length;
		}
		
		cap = PowerUnitUtils.convertInt32(cap,
			memoryUnitSize, clientUnitSize,
			false);
		
		return cap;
	}

	private void myZeroBuf(BufferNode buf,
		short memoryUnitSize) {
		
		Object obj = buf.theObject;

		boolean ok;
		int i;
		int len;
		
		ok = false;
		
		if(memoryUnitSize == 3) {
			CommonIntArrayUtils.zero8((CommonInt8Array) obj);
			ok = true;
		}
		
		if(memoryUnitSize == 4) {
			CommonIntArrayUtils.zero16((CommonInt16Array) obj);
			ok = true;
		}

		if(memoryUnitSize == 5) {
			CommonIntArrayUtils.zero32((CommonInt32Array) obj);
			ok = true;
		}

		if(memoryUnitSize == 6) {
			CommonIntArrayUtils.zero64((CommonInt64Array) obj);
			ok = true;
		}
		
		if(!ok) throw makeIllegalState(null);
		return;
	}
	
	private void myRead(FileRecord2 fh, long storagePointer,
		BufferNode buf, int bufPointer,
		int len,
		boolean allowPadding) {
		
		CommonInt8Array nativeBufData = (CommonInt8Array) nativeBuf.theObject;
		int alignToNativeMult = 1 << (fh.strictAlignSize - NATIVE_UNIT_SIZE);
		
		if(allowPadding)
			readFromFileToByteBufWithPadding(
				fh.raFile, storagePointer * alignToNativeMult,
				nativeBufData, 0,
				len * alignToNativeMult);
		
		if(!allowPadding)
			readFromFileToByteBuf(
				fh.raFile, storagePointer * alignToNativeMult,
				nativeBufData, 0,
				len * alignToNativeMult);
		
		BufferFastCopyUtils.copyClassic(
			nativeBuf, NATIVE_UNIT_SIZE, 0,
			buf, fh.memoryUnitSize, bufPointer,
			fh.strictAlignSize, len,
			int32Masks);
	}

	private void myWrite(FileRecord2 fh, long storagePointer,
		BufferNode buf, int bufPointer,
		int len) {
		
		CommonInt8Array nativeBufData = (CommonInt8Array) nativeBuf.theObject;
		int alignToNativeMult = 1 << (fh.strictAlignSize - NATIVE_UNIT_SIZE);
		
		BufferFastCopyUtils.copyClassic(
			buf, fh.memoryUnitSize, bufPointer,
			nativeBuf, NATIVE_UNIT_SIZE, 0,
			fh.strictAlignSize, len,
			int32Masks);
		
		writeToFileFromByteBuf(
			fh.raFile, storagePointer * alignToNativeMult,
			nativeBufData, 0,
			len * alignToNativeMult);
	}
	
	private void myFileLenTruncate(FileRecord2 fh, long len) {
		long len2;
		
		len2 = len;
		
		len2 = PowerUnitUtils.convertInt64(len2,
			fh.clientUnitSize, NATIVE_UNIT_SIZE,
			true);
		
		nativeSetFileLength(fh.raFile, len2);
		return;
	}
	
	private BufferNode myGetPatchBufVar1(FileRecord2 fh) {
		BufferNode b;
		
		b = null;
		
		if(fh.memoryUnitSize == 4) b = patchBufMem16Var1;
		if(fh.memoryUnitSize == 6) b = patchBufMem64Var1;
		
		if(b == null) throw makeIllegalState(null);
		return b;
	}

	private BufferNode myGetZeroBuf(FileRecord2 fh) {
		BufferNode b;
		
		b = null;
		
		if(fh.memoryUnitSize == 4) b = zeroBufMem16;
		if(fh.memoryUnitSize == 6) b = zeroBufMem64;
		
		if(b == null) throw makeIllegalState(null);
		return b;
	}

	private void myZeroAlignUnits(FileRecord2 fh,
		BufferNode buf, int start, int len) {
		
		int alignToMemMult = 1 << (fh.strictAlignSize - fh.memoryUnitSize);
		
		int i;
		int i64;
		int i16;
		
		if(fh.memoryUnitSize == 4) {
			short zero = 0;
			CommonInt16Array arr = (CommonInt16Array) buf.theObject;
			
			i = 0;
			while(i < len) {
				int start2 = (start + i) * alignToMemMult;
				
				i16 = 0;
				while(i16 < alignToMemMult) {
					arr.aryPtr[start2 + i16] = zero;
					
					i16 += 1;
				}
				
				i += 1;
			}
		}

		if(fh.memoryUnitSize == 6) {
			long zero = 0;
			CommonInt64Array arr = (CommonInt64Array) buf.theObject;
			
			i = 0;
			while(i < len) {
				int start2 = (start + i) * alignToMemMult;
				
				i64 = 0;
				while(i64 < alignToMemMult) {
					arr.aryPtr[start2 + i64] = zero;
					
					i64 += 1;
				}
				
				i += 1;
			}
		}
		
		throw makeIllegalState(null);
	}

	private void myZeroClientUnits(FileRecord2 fh,
		BufferNode buf, int start, int len) {
		
		
		int i;
		int sectionLen;
		
		int leadFract;
		int trailFract;
		
		int len2;

		int patchPos1;
		int patchPos2;
		int patchPos3;

		
		int alignToClientMult = 1 << (fh.strictAlignSize - fh.clientUnitSize);
		int clientToBitMult = 1 << (fh.clientUnitSize - 0);
		
		leadFract = start % alignToClientMult;

		// length to end from aligned position
		len2 = leadFract + len;
		
		// position in client units, inside align unit
		trailFract = len2 % alignToClientMult;

		int start2 = start;
		
		start2 = PowerUnitUtils.convertInt32(
			start2,
			fh.clientUnitSize, fh.strictAlignSize,
			false);

		len2 = PowerUnitUtils.convertInt32(
			len2,
			fh.clientUnitSize, fh.strictAlignSize,
			false);

		boolean enablePatch1;
		boolean enablePatch2;
		boolean enableBlockLoadAndStore;

		BufferNode zeroBuf = myGetZeroBuf(fh);
		
		patchPos1 = 0;
		patchPos2 = 0;
		patchPos3 = 0;
		
		i = 0;
		while(true) {
			enablePatch1 = (i == 0 && leadFract > 0);
			enablePatch2 = false;
			enableBlockLoadAndStore = false;
			
			sectionLen = 0;
			
			if(i < len2) {
				enableBlockLoadAndStore = true;
				sectionLen = len2 - i;

				if(enablePatch1) {
					enableBlockLoadAndStore = false;
					
					if(sectionLen > 1)
						sectionLen = 1;
				}
			}
			
			if(i >= len2) {
				if(i > len2 || trailFract == 0) break;
				
				sectionLen = 1;

				enablePatch2 = true;
			}

			if(enablePatch1 || enablePatch2) {
				patchPos3 = alignToClientMult * clientToBitMult;
				
				if(enablePatch1 && enablePatch2) {
					patchPos1 = leadFract * clientToBitMult;
					patchPos2 = trailFract * clientToBitMult;
				}

				if(enablePatch1 && !enablePatch2) {
					patchPos1 = leadFract * clientToBitMult;
					patchPos2 = alignToClientMult * clientToBitMult;
				}

				if(!enablePatch1 && enablePatch2) {
					patchPos1 = 0;
					patchPos2 = trailFract * clientToBitMult;
				}
			}

			if(sectionLen == 0)
				throw makeIllegalState(null);

			if(enableBlockLoadAndStore) {
				myZeroAlignUnits(fh, buf, start2 + i, sectionLen);
			}

			if(enablePatch1 || enablePatch2) {
				BufferFastCopyUtils.copyPartial(
					zeroBuf, fh.memoryUnitSize, 0,
					buf, fh.memoryUnitSize, start2 + i,
					fh.strictAlignSize,
					patchPos1, patchPos2 - patchPos1,
					int32Masks, int64Masks);
			}
			
			i += sectionLen;
		}
		
		return;
	}
	
	//
	// Input/Output functions
	// accessFile/getFileLength/setFileLength
	//

	public void accessFile(int fileHandle,
		short accessType, long storagePointer,
		BufferNode buf, int bufStart, int len)
	{
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);
		
		int i;
		int len2;
		int sectionLen;

		int leadFract;
		int trailFract;
		
		long fileLen1;
		long fileLen2;
		
		int nativeBufLen;
		
		int patchPos1;
		int patchPos2;
		int patchPos3;

		boolean ok;

		int alignToClientMult = 1 << (fh.strictAlignSize - fh.clientUnitSize);
		int clientToBitMult = 1 << (fh.clientUnitSize - 0);
		
		
		// BASIC ALIGNMENT CHECK
		//
		
		if((storagePointer % alignToClientMult)
			!= (bufStart % alignToClientMult))
			throw makeIllegalState(null);

		
		// FILE CHECKS
		//
		
		fileLen1 = myGetFileLength(fh);

		if(accessType == StorageAccessTypes.TYPE_READ
			|| accessType == StorageAccessTypes.TYPE_WRITE_REPLACE) {

			if(fileLen1 < storagePointer + len)
				throw makeFileIndexOutOfBounds(null);
		}

		if(accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {
			ok = true;

			long minLen = storagePointer;

			minLen = PowerUnitUtils.convertInt64(
				minLen,
				fh.clientUnitSize, fh.strictAlignSize,
				false);
			minLen = PowerUnitUtils.convertInt64(
				minLen,
				fh.strictAlignSize, fh.clientUnitSize,
				false);

			if(fileLen1 < minLen) ok = false;

			long maxLen = storagePointer;

			maxLen = PowerUnitUtils.convertInt64(
				maxLen,
				fh.clientUnitSize, fh.strictAlignSize,
				true);
			maxLen = PowerUnitUtils.convertInt64(
				maxLen,
				fh.strictAlignSize, fh.clientUnitSize,
				true);

			if(fileLen1 > maxLen) ok = false;

			if(!ok) {
				throw makeFileIndexOutOfBounds(null);
			}
		}

		
		// BUFFER CHECKS
		//
		
		{
			if(myCheckBuf(buf, fh.memoryUnitSize))
				makeIllegalState(null);

			int maxLen = bufStart + len;
			int cap = myGetBufCapacity(
				buf, fh.memoryUnitSize, fh.clientUnitSize);
			
			if(cap < maxLen)
				throw makeOutOfBounds(null);
		}
		
		
		// There are 4 types of sections
		// leading, middle, trailing, small

		// leading and trailing are each within a align unit
		// middle is made of whole align units
		
		// small is strictly inside an align unit,
		// not hitting an end,
		// and has a length greater than zero

		// These sequences are possible
		//   1. leading maybe, middle maybe, trailing maybe
		//   2. small
		
		// "BLOCK" means that the step uses whole align units
		
		// STYLE "A":
		// The cases translate into using,
		// a sequence of these states/steps
		//int STATE_SECTION_START = 1;
		//int STATE_SECTION_STOP = 2;
		//int STATE_BLOCK_LOAD = 3;
		//int STATE_PATCH_BLOCK_LOAD = 4;
		//int STATE_PATCH_APPLY = 5;
		//int STATE_BLOCK_STORE = 6;
		//int STATE_TRUNCATE = 7;

		// STYLE "B":
		// The cases translate into sections,
		// from beginning to end of the access,
		// each with some of these actions enabled
		boolean enableBlockLoadAndStore;
		boolean enablePatch1;
		boolean enablePatch2;
		boolean enableFileLenMinimumCheck;
		boolean enableFileLenExactCheck;
		boolean enableFileLenTrim;
		boolean enableFileLenMaintain;
		// other flags
		boolean allowBlockLoadPadding;

		
		// USEFUL CONSTANTS
		//

		// position in client units, inside align unit
		leadFract = bufStart % alignToClientMult;

		// length to end from aligned position
		len2 = leadFract + len;
		
		// position in client units, inside align unit
		trailFract = len2 % alignToClientMult;
		
		// convert to align units (ignoring trailing stuff)
		len2 = PowerUnitUtils.convertInt32(
			len2,
			fh.clientUnitSize, fh.strictAlignSize,
			false);
		
		int bufStart2 = bufStart;
		bufStart2 = PowerUnitUtils.convertInt32(
			bufStart2,
			fh.clientUnitSize, fh.strictAlignSize,
			false);

		long storagePointer2 = storagePointer;
		storagePointer2 = PowerUnitUtils.convertInt64(
			storagePointer2,
			fh.clientUnitSize, fh.strictAlignSize,
			false);
		
		// nativeBuf length in align units
		nativeBufLen = myGetBufCapacity(nativeBuf,
			NATIVE_UNIT_SIZE, fh.clientUnitSize);
		nativeBufLen = PowerUnitUtils.convertInt32(nativeBufLen,
			fh.clientUnitSize, fh.strictAlignSize,
			false);
		
		
		// PATCH BUFFERS
		//
		BufferNode patchBuf1 = myGetPatchBufVar1(fh);
		BufferNode zeroBuf = myGetZeroBuf(fh);

		
		// READY THE LOOP
		//
		
		enableFileLenMinimumCheck =
			(accessType == StorageAccessTypes.TYPE_READ
			|| accessType == StorageAccessTypes.TYPE_WRITE_REPLACE);

		enableFileLenExactCheck =
			accessType == StorageAccessTypes.TYPE_WRITE_APPEND;

		enableFileLenMaintain =
			accessType == StorageAccessTypes.TYPE_WRITE_REPLACE;
		
		enableFileLenTrim = false;

		patchPos1 = 0;
		patchPos2 = 0;
		patchPos3 = 0;
		
		i = 0;
		while(true) {
			enablePatch1 = (i == 0 && leadFract > 0);
			enablePatch2 = false;
			allowBlockLoadPadding = false;
			enableBlockLoadAndStore = false;
			
			sectionLen = 0;
			
			if(i < len2) {
				enableBlockLoadAndStore = true;
				sectionLen = len2 - i;

				if(sectionLen >= nativeBufLen)
					sectionLen = nativeBufLen;

				if(enablePatch1) {
					enableBlockLoadAndStore = false;
					
					if(sectionLen > 1)
						sectionLen = 1;
				}
			}
			
			if(i >= len2) {
				if(i > len2 || trailFract == 0) break;
				
				sectionLen = 1;

				enablePatch2 = true;
				allowBlockLoadPadding = true;

				if(accessType == StorageAccessTypes.TYPE_WRITE_APPEND)
					enableFileLenTrim = true;
			}
			
			if(enablePatch1 || enablePatch2) {
				patchPos3 = alignToClientMult * clientToBitMult;
				
				if(enablePatch1 && enablePatch2) {
					patchPos1 = leadFract * clientToBitMult;
					patchPos2 = trailFract * clientToBitMult;
				}

				if(enablePatch1 && !enablePatch2) {
					patchPos1 = leadFract * clientToBitMult;
					patchPos2 = alignToClientMult * clientToBitMult;
				}

				if(!enablePatch1 && enablePatch2) {
					patchPos1 = 0;
					patchPos2 = trailFract * clientToBitMult;
				}
			}
			
			if(sectionLen == 0)
				throw makeIllegalState(null);
			
			if(enableBlockLoadAndStore) {
				if(accessType == StorageAccessTypes.TYPE_READ) {
					myRead(
						fh, storagePointer2 + i,
						buf, bufStart2 + i,
						sectionLen,
						allowBlockLoadPadding);
				}

				if(accessType == StorageAccessTypes.TYPE_WRITE_REPLACE
					|| accessType == StorageAccessTypes.TYPE_WRITE_APPEND)
				{
					myWrite(
						fh, storagePointer2 + i,
						buf, bufStart2 + i,
						sectionLen);
				}
			}
			
			if(enablePatch1 || enablePatch2) {
				if(accessType == StorageAccessTypes.TYPE_READ) {
					myRead(
						fh, storagePointer2 + i,
						patchBuf1, 0,
						sectionLen,
						true);
					
					BufferFastCopyUtils.copyPartial(
						patchBuf1, fh.memoryUnitSize, 0,
						buf, fh.memoryUnitSize, bufStart2 + i,
						fh.strictAlignSize,
						patchPos1, patchPos2 - patchPos1,
						int32Masks, int64Masks);
				}
				
				if(accessType == StorageAccessTypes.TYPE_WRITE_REPLACE
					|| accessType == StorageAccessTypes.TYPE_WRITE_APPEND)
				{
					myRead(
						fh, storagePointer2 + i,
						patchBuf1, 0,
						sectionLen,
						true);
					
					/*
					CommonInt16Array patchBufData = (CommonInt16Array) patchBuf1.theObject;
					
					System.out.println("i=" + i);
					System.out.println("storagePointer2=" + storagePointer2);

					System.out.println("1(");
					myPrintThis(patchBufData[0]);
					myPrintThis(patchBufData[1]);
					System.out.println(")");
					*/
					
					BufferFastCopyUtils.copyPartial(
						buf, fh.memoryUnitSize, bufStart2 + i,
						patchBuf1, fh.memoryUnitSize, 0,
						fh.strictAlignSize,
						patchPos1, patchPos2 - patchPos1,
						int32Masks, int64Masks);

					/*
					System.out.println("2(");
					myPrintThis(patchBufData[0]);
					myPrintThis(patchBufData[1]);
					System.out.println(")");
					*/
					
					if(accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {
						// myZeroBuf(zeroBuf, fh.memoryUnitSize);

						BufferFastCopyUtils.copyPartial(zeroBuf, fh.memoryUnitSize, 0,
							patchBuf1, fh.memoryUnitSize, 0,
							fh.strictAlignSize,
							patchPos2, patchPos3 - patchPos2,
							int32Masks, int64Masks);
					}
					
					myWrite(
						fh, storagePointer2 + i,
						patchBuf1, 0,
						sectionLen);
				}
			}
			
			if(enableFileLenMinimumCheck) {
				long minLen = storagePointer + len;

				fileLen2 = myGetFileLength(fh);

				if(fileLen2 < minLen) {
					throw makeFileIndexOutOfBounds(null);
				}
			}

			if(enableFileLenExactCheck) {
				long exactLen = (storagePointer2 + i + sectionLen) * alignToClientMult;

				fileLen2 = myGetFileLength(fh);
				
				if(exactLen != fileLen2) {
					//System.out.println("ExactCheck," + fileLen2 + "," + exactLen);
					
					throw makeIllegalState(null);
				}
			}

			/*
			System.out.println("sectionLen " + sectionLen);
			System.out.println("---");
			*/
			
			i += sectionLen;
		}
		
		if(enableFileLenTrim) {
			fileLen2 = myGetFileLength(fh);

			if(fileLen2 > storagePointer + len)
				myFileLenTruncate(fh, storagePointer + len);
		}
		
		if(enableFileLenMaintain) {
			fileLen2 = myGetFileLength(fh);
			
			if(fileLen2 > fileLen1)
				myFileLenTruncate(fh, fileLen1);
		}
		
		return;
	}
	
	/*
	private void myPrintThis(short a) {
		int b;
		
		b = a & 0xFF;
		
		if(b >= 'a' && b <= 'z') System.out.println((char) b);
		else System.out.println(b);
		
		b = (a >> 8) & 0xFF;
		
		if(b >= 'a' && b <= 'z') System.out.println((char) b);
		else System.out.println(b);
	}
	*/
	
	public long getFileLength(int fileHandle) {
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);
		return myGetFileLength(fh);
	}

	public void setFileLength(int fileHandle, long len) {
		FileRecord2 fh = getFileRecordFromHandle(fileHandle);
		
		long len1;
		long len2;
		long len3;
		
		len1 = nativeGetFileLength(fh.raFile);
		
		len2 = len;
		
		len2 = PowerUnitUtils.convertInt64(len2,
			fh.clientUnitSize, NATIVE_UNIT_SIZE,
			true);

		if(len2 > len1)
			throw makeFileIndexOutOfBounds(null);
		
		len3 = len;
		
		len3 = PowerUnitUtils.convertInt64(len3,
			fh.clientUnitSize, NATIVE_UNIT_SIZE,
			false);
		
		if(len1 > len2)
			nativeSetFileLength(fh.raFile, len2);
		
		if(len3 == len2) {
			// client and native lengths align together,
			// the first truncate did fine
			return;
		}
		
		
		// WHAT NEEDS TO BE DONE
		//
		// Read last align unit in,
		// clear ending bits,
		// write it out,
		// and truncate
		//

		
		// USEFUL CONSTANTS
		//

		int alignToClientMult = 1 << (fh.strictAlignSize - fh.clientUnitSize);
		int clientToBitMult = 1 << (fh.clientUnitSize - 0);

		// position in client units, inside align unit
		int trailFract = (int) (len % alignToClientMult);
		if(trailFract == 0) throw makeIllegalState(null);
		
		len2 = len;
		
		// convert to align units (ignoring trailing stuff)
		len2 = PowerUnitUtils.convertInt64(len2,
			fh.clientUnitSize, fh.strictAlignSize,
			false);
		
		
		// PATCH BUFFERS
		//
		
		BufferNode patchBuf1 = myGetPatchBufVar1(fh);
		BufferNode zeroBuf = myGetZeroBuf(fh);


		// READY PATCH
		//
		
		int sectionLen = 1;
		
		int patchPos2 = trailFract * clientToBitMult;
		int patchPos3 = alignToClientMult * clientToBitMult;
		
		
		// DO PATCH
		//

		myRead(
			fh, len2,
			patchBuf1, 0,
			sectionLen,
			true);

		//myZeroBuf(zeroBuf, fh.memoryUnitSize);

		BufferFastCopyUtils.copyPartial(zeroBuf, fh.memoryUnitSize, 0,
			patchBuf1, fh.memoryUnitSize, 0,
			fh.strictAlignSize,
			patchPos2, patchPos3 - patchPos2,
			int32Masks, int64Masks);

		myWrite(
			fh, len2,
			patchBuf1, 0,
			sectionLen);

		
		// FINAL TRUNCATE
		//

		len1 = myGetFileLength(fh);

		if(len1 > len)
			myFileLenTruncate(fh, len);
		
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
	
	private CommonError makeOutOfBounds(String msg) {
		CommonError e4;
		
		e4 = new CommonError();
		e4.id = unnamed.common.CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e4.msg = msg;
		return e4;
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
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}
	
	private CommonInt8Array makeInt8Array(int len) {
		return CommonUtils.makeInt8Array(len);
	}

	private CommonInt16Array makeInt16Array(int len) {
		return CommonUtils.makeInt16Array(len);
	}

	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}

	private CommonInt64Array makeInt64Array(int len) {
		return CommonUtils.makeInt64Array(len);
	}
}
