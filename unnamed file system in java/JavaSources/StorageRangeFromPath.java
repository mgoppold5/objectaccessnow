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

package unnamed.file.system;

import unnamed.common.*;

public class StorageRangeFromPath implements StorageRangeAccess {
	private boolean haveInit;
	private StoragePathAccess fs;
	private int fileHandle;
	private StorageRangeMetrics m;
	
	public void initFileRef(StoragePathAccess pFs, int pFileHandle) {
		if(haveInit) throw makeIllegalState("already initialized");
		
		if(pFs == null) {
			fs = null;
			fileHandle = 0;
			m = null;
			return;
		}
		
		if(!pFs.getFileHaveInit(pFileHandle))
			throw makeIllegalState("not initialized");
		
		fs = pFs;
		fileHandle = pFileHandle;
		initMetrics();
	}
	
	private void initMetrics() {
		m = new StorageRangeMetrics();
		
		m.clientUnitSize = fs.getFileClientUnitSize(fileHandle);
		m.memoryUnitSize = fs.getFileMemoryUnitSize(fileHandle);
		m.strictAlignSize = fs.getFileStrictAlignSize(fileHandle);
	}

	public void init() {
		if(haveInit) throw makeIllegalState("already initialized");
		if(fs == null) throw new NullPointerException();
		
		haveInit = true;
	}
	
	public boolean getIsThreadSafe() {
		if(!haveInit) throw makeIllegalState("not initialized");
		
		return fs.getIsThreadSafe();
	}
	
	public boolean getHaveInit() {return haveInit;}
	
	public short getClientUnitSize() {return m.clientUnitSize;}
	public short getMemoryUnitSize() {return m.memoryUnitSize;}
	public short getStrictAlignSize() {return m.strictAlignSize;}

	public long getLength() {return fs.getFileLength(fileHandle);}
	public void setLength(long len) {fs.setFileLength(fileHandle, len);}
	
	public long getSuggestLength() {return fs.getFileSuggestLength(fileHandle);}
	public void setSuggestLength(long len) {fs.setFileSuggestLength(fileHandle, len);}
	
	public void access(short accessType, long storagePointer,
		BufferNode buf, int bufStart, int len) {
		
		fs.accessFile(fileHandle, accessType,
			storagePointer,
			buf, bufStart,
			len);
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}
}
