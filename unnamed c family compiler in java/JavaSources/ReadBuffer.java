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

package unnamed.family.compiler;

import unnamed.common.*;

public class ReadBuffer {
	public BufferCombo bufferCombo;
	public long startIndex;

	public static final int VERSION_NUMBER_INVALID = 0;
	public static final int VERSION_NUMBER_START = 1;
	public static final int VERSION_NUMBER_LIMIT = 1024 * 1024;

	public int versionNumber;

	public void init() {
		startIndex = 0;
		bufferCombo = makeBufferCombo();
		versionNumber = VERSION_NUMBER_START;
	}
	
	public void incrementVersion() {
		if(versionNumber == VERSION_NUMBER_INVALID)
			return;
	
		versionNumber += 1;
		if(versionNumber >= VERSION_NUMBER_LIMIT)
			versionNumber = VERSION_NUMBER_START;
	}
	
	public void resetForMemory() {
		incrementVersion();
	}
	
	private BufferCombo makeBufferCombo() {
		BufferCombo bc;
		
		bc = new BufferCombo();
		bc.init();
		return bc;
	}
}
