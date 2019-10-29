/*
 * Copyright (c) 2017 Mike Goppold von Lobsdorf
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

/*
 * This Java class tests the interface StoragePathAccess.
 */

import unnamed.common.*;
import unnamed.file.system.*;

public class StoragePathTest {
	private CommonInt32Array currentDirRoot;
	private CommonInt32Array testDirName;
	private CommonInt32Array testFile1Name;
	private short accessRights1;
	
	public void initGlobals() {
		currentDirRoot = StringUtils.int32StringFromJavaString(".");
		testDirName = StringUtils.int32StringFromJavaString("test-dir");
		testFile1Name = StringUtils.int32StringFromJavaString("hello.txt");
		accessRights1 = AccessRights.ACCESS_READ + AccessRights.ACCESS_WRITE;
	}

	public void createTestDir1() {
		HandleSet hs = new HandleSet();
		hs.init();
		LocalFileSystem localFS = new LocalFileSystem();
		localFS.initBasePath(currentDirRoot);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();
		
		localFS.setFileNumbering(hs, 1);
		localFS.createDirectory(null, testDirName);
	}

	public void createTestDir2() {
		LocalStoragePath localFS = new LocalStoragePath();
		localFS.initBasePath(currentDirRoot);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();
		localFS.createDirectory(null, testDirName);
	}
	
	public void deleteFileIfExists() {
		LocalStoragePath localFS = new LocalStoragePath();
		localFS.initBasePath(testDirName);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();
		
		int fileType = localFS.getFileType(testFile1Name);
		if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE)
			localFS.deleteFile(testFile1Name);
	}
	
	public void createFile1() {
		HandleSet hs = new HandleSet();
		hs.init();
		LocalFileSystem localFS = new LocalFileSystem();
		localFS.initBasePath(testDirName);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();

		localFS.setFileNumbering(hs, 1);
		localFS.createFile(null, testFile1Name);
	}

	public void createFile2() {
		LocalStoragePath localFS = new LocalStoragePath();
		localFS.initBasePath(testDirName);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();
		localFS.createFile(null, testFile1Name);
	}

	public void writeToFile1() {
		HandleSet hs = new HandleSet();
		hs.init();
		LocalFileSystem localFS = new LocalFileSystem();
		localFS.initBasePath(testDirName);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();

		localFS.setFileNumbering(hs, 1);
		
		String s = "hip hip\n hooray!!";
		byte[] buf1 = s.getBytes();
		CommonInt8Array buf2 = makeGoodArray8(buf1);
		
		int h = localFS.openFile(testFile1Name, accessRights1);
		localFS.writeFile(h, buf2, 0, buf2.length);
		localFS.closeFile(h);
	}

	public void writeToFile2() {
		LocalStoragePath localFS = new LocalStoragePath();
		localFS.initBasePath(testDirName);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();

		localFS.initFileNumberingEra(1);
		int h = localFS.openFile(testFile1Name, accessRights1);

		
		if(true) {
			localFS.initFileClientUnitSize(h, (short) 3);
			localFS.initFileMemoryUnitSize(h, (short) 4);
			localFS.initFileStrictAlignSize(h, (short) 5);
			localFS.initFile(h);

			String s1 = "aaa aaa aaa aaa a";
			byte[] buf1 = s1.getBytes();
			BufferNode niceBuf1 = wrapBuf(copyFromByteBufToNormalBuf16(buf1, buf1.length));

			localFS.accessFile(
				h, (short) StorageAccessTypes.TYPE_WRITE_APPEND, 0, 
				niceBuf1, 0, buf1.length);

			if(false) {
				String s2 = "bbb-bbb-bbb-bbb-b";
				byte[] buf2 = s2.getBytes();
				BufferNode niceBuf2 = wrapBuf(copyFromByteBufToNormalBuf16(buf2, buf2.length));

				int p = 3;
				int q = 2;

				localFS.accessFile(
					h, (short) StorageAccessTypes.TYPE_WRITE_REPLACE, p, 
					niceBuf2, p, buf2.length - p - q);
			}
		}
		
		if(false) {
			// these are tests with the clientUnitSize is zero
			// (units are bits)
			
			// zero is currently used to mean null/default,
			// so to get this test working,
			// function LocalStoragePath.fileInit,
			// needs to be hacked manually
			localFS.initFileClientUnitSize(h, (short) 0);
			
			localFS.initFileMemoryUnitSize(h, (short) 4);
			localFS.initFileStrictAlignSize(h, (short) 5);
			localFS.initFile(h);

			int i;

			int len1 = 4;
			short[] buf1 = new short[len1];

			i = 0;
			while(i < len1) {
				buf1[i] = (short) 0xFFFF;
				i += 1;
			}

			short[] buf2 = new short[len1];

			i = 0;
			while(i < len1) {
				buf2[i] = (short) 0;
				i += 1;
			}
			
			BufferNode niceBuf1 = wrapBuf(buf1);
			BufferNode niceBuf2 = wrapBuf(buf2);

			if(false) {
				localFS.accessFile(
					h, (short) StorageAccessTypes.TYPE_WRITE_APPEND, 0, 
					niceBuf1, 0, 17);

				if(false) {
					localFS.accessFile(
						h, (short) StorageAccessTypes.TYPE_WRITE_REPLACE, 12, 
						niceBuf2, 12, 1);
				}
			}

			if(false) {
				localFS.accessFile(
					h, (short) StorageAccessTypes.TYPE_WRITE_APPEND, 0, 
					niceBuf1, 0, 15);

				if(false) {
					localFS.accessFile(
						h, (short) StorageAccessTypes.TYPE_WRITE_APPEND, 12, 
						niceBuf1, 12, 1);
				}
			}
		}
		
		localFS.closeFile(h);
	}
	
	private short[] copyFromByteBufToNormalBuf16(
		byte[] buf1, int len) {
		
		int i1;
		int i2;
		int mask = 0xFF;
		short b1;
		short accum;
		int innerUnitCount = 2;
		int byteBits = 8;

		short[] buf2 = new short[1024];
		
		i1 = 0;
		while(i1 < len) {
			accum = 0;
			
			i2 = 0;
			while(i2 < innerUnitCount) {
				b1 = 0;
				if(i1 + i2 < len)
					b1 = buf1[i1 + i2];
				b1 &= mask;
				
				accum |= b1 << (i2 * byteBits);
				
				i2 += 1;
			}
			
			buf2[i1 / innerUnitCount] = accum;
			i1 += innerUnitCount;
		}
		
		return buf2;
	}

	private long[] copyFromByteBufToNormalBuf64(
		byte[] buf1, int len) {
		
		int i1;
		int i2;
		int mask = 0xFF;
		long b1;
		long accum;
		int innerUnitCount = 8;
		int byteBits = 8;

		long[] buf2 = new long[1024];
		
		i1 = 0;
		while(i1 < len) {
			accum = 0;
			
			i2 = 0;
			while(i2 < innerUnitCount) {
				b1 = 0;
				if(i1 + i2 < len)
					b1 = buf1[i1 + i2];
				b1 &= mask;
				
				accum |= b1 << (i2 * byteBits);
				
				i2 += 1;
			}
			
			buf2[i1 / innerUnitCount] = accum;
			i1 += innerUnitCount;
		}
		
		return buf2;
	}
	
	public void writeToFile3() {
		LocalStoragePath localFS = new LocalStoragePath();
		localFS.initBasePath(testDirName);
		localFS.initRights(accessRights1);
		localFS.initSeparatorChar('/');
		localFS.init();

		localFS.initFileNumberingEra(1);
		int h = localFS.openFile(testFile1Name, accessRights1);

		
		if(true) {
			localFS.initFileClientUnitSize(h, (short) 3);
			localFS.initFileMemoryUnitSize(h, (short) 4);
			localFS.initFileStrictAlignSize(h, (short) 5);
			localFS.initFile(h);
			
			StorageRangeFromPath r = new StorageRangeFromPath();
			r.initFileRef(localFS, h);
			r.init();
			
			StorageBlockCursor c = new StorageBlockCursor();
			c.initRangeRef(r);
			c.initMinBlockSize((short) 24);
			c.init();
			
			c.setAccessType((short) StorageAccessTypes.TYPE_WRITE_APPEND);
			c.setStoragePointer(0);
			c.setIsCursorRunning(true);
			
			long base = '0';
			
			c.setAlloc(3);
			c.setValue64(0, base + 1);
			c.setValue64(1, base + 2);
			c.setValue64(2, base + 3);
			c.moveForward();
			
			c.setAlloc(3);
			c.setValue64(0, base + 4);
			c.setValue64(1, base + 5);
			c.setValue64(2, base + 6);
			c.moveForward();
			
			c.setIsCursorRunning(false);
			
			c.setAccessType((short) StorageAccessTypes.TYPE_WRITE_REPLACE);
			c.setStoragePointer(2);
			c.setIsCursorRunning(true);

			c.setAlloc(3);
			c.setValue64(0, base + 7);
			c.setValue64(1, base + 8);
			c.setValue64(2, base + 9);
			c.moveForward();

			c.setIsCursorRunning(false);
		}
		
		localFS.closeFile(h);
	}

	private BufferNode wrapBuf(Object o) {
		BufferNode buf2 = new BufferNode();
		buf2.theObject = o;
		return buf2;
	}

	private CommonInt8Array makeGoodArray8(byte[] a1) {
		CommonInt8Array a2 = CommonUtils.makeInt8Array(a1.length);
		int i;
		int len;
		i = 0;
		len = a1.length;
		while(i < len) {
			a2.aryPtr[i] = a1[i];
			i += 1;
		}
		
		return a2;
	}

	public void run() {
		initGlobals();
		//createTestDir2();
		deleteFileIfExists();
		createFile2();
		//writeToFile1();
		//writeToFile2();
		writeToFile3();
	}
	
	public static void main(String[] args) {
		(new StoragePathTest()).run();
	}
}
