/*
 * Copyright (c) 2015-2017 Mike Goppold von Lobsdorf
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

public class CommonIntArrayUtils {
	public static CommonInt16Array copy16(CommonInt16Array a1) {
		CommonInt16Array a2 = CommonUtils.makeInt16Array(a1.length);
		int i;
		int len;
		
		i = 0;
		len = a1.length;
		while(i < len) {
			a2.aryPtr[i] = a1.aryPtr[i];
			i += 1;
		}
		
		return a2;
	}

	public static CommonInt32Array copy32(CommonInt32Array a1) {
		CommonInt32Array a2 = CommonUtils.makeInt32Array(a1.length);
		int i;
		int len;
		
		i = 0;
		len = a1.length;
		while(i < len) {
			a2.aryPtr[i] = a1.aryPtr[i];
			i += 1;
		}
		
		return a2;
	}

	public static void zero8(CommonInt8Array a1) {
		int i;
		int len;
		
		byte zero = (byte) 0;
		
		i = 0;
		len = a1.length;
		while(i < len) {
			a1.aryPtr[i] = zero;
			i += 1;
		}
		
		return;
	}

	public static void zero16(CommonInt16Array a1) {
		int i;
		int len;
		
		short zero = (short) 0;
		
		i = 0;
		len = a1.length;
		while(i < len) {
			a1.aryPtr[i] = zero;
			i += 1;
		}
		
		return;
	}

	public static void zero32(CommonInt32Array a1) {
		int i;
		int len;
		
		int zero = (int) 0;
		
		i = 0;
		len = a1.length;
		while(i < len) {
			a1.aryPtr[i] = zero;
			i += 1;
		}
		
		return;
	}

	public static void zero64(CommonInt64Array a1) {
		int i;
		int len;
		
		long zero = (long) 0;
		
		i = 0;
		len = a1.length;
		while(i < len) {
			a1.aryPtr[i] = zero;
			i += 1;
		}
		
		return;
	}
}
