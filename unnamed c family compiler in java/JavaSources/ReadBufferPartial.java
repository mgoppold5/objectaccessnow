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

package unnamed.family.compiler;

import unnamed.common.*;

public class ReadBufferPartial extends ReadBuffer {	
	public boolean isAtEnd;
	public boolean isAtBeginning;
	
	// reusable buffer stuff
	public CommonInt8Array extraBufData1;
	public CommonInt8Array extraBufData2;
	
	public void init() {
		super.init();
		
		extraBufData1 = null;
		extraBufData2 = null;
		
		isAtEnd = false;
		isAtBeginning = false;
	}

	public void clear() {
		bufferCombo.clear();
		isAtEnd = false;
		isAtBeginning = false;
		
		incrementVersion();
	}
}
