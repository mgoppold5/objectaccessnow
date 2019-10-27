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

public class CommonUtils {
	public static CommonArrayList makeArrayList() {
		CommonArrayList4 aList;
		
		aList = new CommonArrayList4();
		aList.init();
		return aList;
	}

	public static CommonInt8Array makeInt8Array(int len) {
		CommonInt8Array ary;
		
		ary = new CommonInt8Array();
		ary.aryPtr = new byte[len];
		ary.length = len;
		ary.capacity = len;
		return ary;
	}

	public static CommonInt16Array makeInt16Array(int len) {
		CommonInt16Array ary;
		
		ary = new CommonInt16Array();
		ary.aryPtr = new short[len];
		ary.length = len;
		ary.capacity = len;
		return ary;
	}

	public static CommonInt32Array makeInt32Array(int len) {
		CommonInt32Array ary;
		
		ary = new CommonInt32Array();
		ary.aryPtr = new int[len];
		ary.length = len;
		ary.capacity = len;
		return ary;
	}

	public static CommonInt64Array makeInt64Array(int len) {
		CommonInt64Array ary;
		
		ary = new CommonInt64Array();
		ary.aryPtr = new long[len];
		ary.length = len;
		ary.capacity = len;
		return ary;
	}
}
