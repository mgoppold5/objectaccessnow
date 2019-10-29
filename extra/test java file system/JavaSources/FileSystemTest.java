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

/*
 * This Java class tests the interface FileSystem.
 */

import unnamed.common.*;
import unnamed.file.system.*;

public class FileSystemTest {
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

	public void createTestDir() {
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
	
	public void createFile() {
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

	public void writeToFile() {
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
		createTestDir();
		createFile();
		writeToFile();
	}
	
	public static void main(String[] args) {
		(new FileSystemTest()).run();
	}
}
