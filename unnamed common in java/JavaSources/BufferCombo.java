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
 * This Java class contains a text/binary buffer.
 */

package unnamed.common;

public class BufferCombo {
	public CommonArrayList bufferList; // list of segments making a whole
	public int length; // length in units

	public void init() {
		bufferList = makeArrayList();
		length = 0;
	}
	
	public void clear() {
		bufferList.clear();
		length = 0;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
