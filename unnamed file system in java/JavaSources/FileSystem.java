/*
 * Copyright (c) 2013-2016 Mike Goppold von Lobsdorf
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
 * This Java interface is for file systems.
 */

package unnamed.file.system;

import unnamed.common.*;

public interface FileSystem {
	// filesystem init/deinit
	public void initFileNumberingEra(int era);
	public void deinitFileNumberingEra();
	public void close();

	// properties
	public short getRights();
	public int getSeparatorChar();
	public int getFileNumberingEra();

	// functions which deal with a path
	//
	
	// fs info
	public CommonArrayList listDirectory(CommonInt32Array path);
	public int getFileType(CommonInt32Array path);

	// adding/removing
	public void createDirectory(CommonInt32Array parentDir, CommonInt32Array name);
	public void deleteEmptyDirectory(CommonInt32Array path);
	public void createFile(CommonInt32Array parentDir, CommonInt32Array name);
	public void deleteFile(CommonInt32Array path);

	// file access
	public int openFile(CommonInt32Array path, short desiredRights);
	
	// functions which operate on a file handle
	//
	public void closeFile(int fileHandle);
	public long getFileLength(int fileHandle);
	public void setFileLength(int fileHandle, long len);
	public void setFilePointer(int fileHandle, long pos);
	public long getFilePointer(int fileHandle);
	public void readFile(int fileHandle,
		CommonInt8Array buf, int start, int length);
	public void writeFile(int fileHandle,
		CommonInt8Array buf, int start, int length);
}
