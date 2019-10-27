/*
 * Copyright (c) 2015 Mike Goppold von Lobsdorf
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

public class LimbUtils {
	public static void adjustLimbs(
		LimbElement elem, long lowerHalfMask) {
		
		int carry;
		int i;
		int count;
		CommonInt64Array a;
		
		a = elem.a;
		count = elem.used;
		i = 0;
		
		while(i < count) {
			carry = (int) (a.aryPtr[i] >>> 32);
			
			if(carry == 0) {
				i += 1;
				continue;
			}

			if(i + 1 >= count) {
				if(count >= elem.capacity)
					throw makeIndexOutOfBoundsException(null);

				elem.used += 1;
				count = elem.used;
			}

			a.aryPtr[i] &= lowerHalfMask;
			a.aryPtr[i + 1] += carry;
			
			i += 1;
			continue;
		}
		
		return;
	}
	
	public static void multLimbs(LimbElement elem, int b) {
		int count;
		int i;
		CommonInt64Array a;
		
		a = elem.a;
		count = elem.used;
		i = 0;
		while(i < count) {
			a.aryPtr[i] *= b;
			
			i += 1;
		}
		
		return;
	}


	public static void allocLimbs(LimbElement elem) {
		int count1;
		int count2;
		int i;
		CommonInt64Array b;
		CommonInt64Array a;
		
		a = elem.a;
		count1 = 0;
		if(a != null) count1 = a.length;
		
		count2 = 1;
		while(count2 <= count1 + 1)
			count2 *= 2;
		
		//b = new long[count2];
		b = CommonUtils.makeInt64Array(count2);

		i = 0;
		while(i < count1) {
			b.aryPtr[i] = a.aryPtr[i];
			
			i += 1;
		}
		
		while(i < count2) {
			b.aryPtr[i] = 0;
			
			i += 1;
		}
		
		elem.a = b;
		elem.capacity = count2;
		return;
	}
	
	public static void touchLimb(LimbElement elem, int limb) {
		if(limb < elem.used) return;
		
		if(limb >= elem.capacity)
			throw makeIndexOutOfBoundsException(null);
		
		if(elem.a.aryPtr[limb] == 0) return;
		
		elem.used = limb + 1;
		return;
	}
	
	private static RuntimeException makeIndexOutOfBoundsException(
		String msg) {
		
		if(msg == null)
			return new IndexOutOfBoundsException();
		
		return new IndexOutOfBoundsException(msg);
	}
}
