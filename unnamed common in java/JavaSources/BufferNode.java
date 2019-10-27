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
 * This Java class contains text/binary buffer data.
 */

package unnamed.common;

public class BufferNode extends CommonNode {
	// If this node directly stores data,
	// it is in an array,
	// in TypeAndObject.theObject

	// The array uses memory units.
	
	// These quantities are in client units.
	public int used;
	public int start;
	public int capacity;
	
	
	// growNum deals with a special way,
	// of remembering buffer sizes
	//
	// see buffer metrics in StorageBlockCursor
	public int growNum;
}
