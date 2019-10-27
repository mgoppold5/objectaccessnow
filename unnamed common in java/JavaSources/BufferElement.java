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
 * This Java class contains text/binary buffer data.
 */

package unnamed.common;

public class BufferElement {
	// a buffer element is self sufficient, and can represent a whole
	// buffer

	// for character data, the code unit size depends on the character type
	// for binary data, the unit size is a byte
	
	public CommonInt8Array buf; // where data is stored
	public int start; // number of units until data begins
	public int used; // how many units the data is
	public int capacity; // how many units this buffer can hold

	public void init() {
		buf = null;
		start = 0;
		used = 0;
		capacity = 0;
	}	
}
