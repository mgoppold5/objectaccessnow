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

package unnamed.common;

public interface StorageCursorAccess {
	public boolean getHaveInit();
	
	public short getClientUnitSize();
	public short getStrictAlignSize();
	
	public long getLength();
	public void setLength(long len);

	public long getSuggestLength();
	public void setSuggestLength(long len);
	
	public int getAccessType();
	public void setAccessType(short pAccessType);
	
	public boolean getIsCursorRunning();
	public void setIsCursorRunning(boolean running);
	
	public long getStoragePointer();
	public void setStoragePointer(long pointer);

	public int getAlloc();
	public void setAlloc(int pAlloc);
	public void moveForward();

	public byte getValue8(int index);
	public void setValue8(int index, byte value);
	public short getValue16(int index);
	public void setValue16(int index, short value);
	public int getValue32(int index);
	public void setValue32(int index, int value);
	public long getValue64(int index);
	public void setValue64(int index, long value);
}
