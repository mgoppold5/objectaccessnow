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

public class BitMaskUtils {
	// lowerBitsInit
	// bitmask[0] = 0b0000_0000;
	// bitmask[1] = 0b0000_0001;
	// bitmask[2] = 0b0000_0011;
	// bitmask[3] = 0b0000_0111; 
	// ...

	public static CommonInt32Array lowerBitsInit32(short limit) {
		CommonInt32Array bitmask;
		int i;
		int accumulator;
		int limit2;
		int one = 1;
		
		limit2 = limit;
		bitmask = CommonUtils.makeInt32Array(limit2 + 1);
		
		bitmask.aryPtr[0] = 0;
		
		i = 1;
		accumulator = 0;
		while(i <= limit2) {
			accumulator |= (one << (i - 1));
			bitmask.aryPtr[i] = accumulator;
			
			i += 1;
		}
		
		return bitmask;
	}

	public static CommonInt64Array lowerBitsInit64(short limit) {
		CommonInt64Array bitmask;
		int i;
		long accumulator;
		int limit2;
		long one = 1;
		
		limit2 = limit;
		bitmask = CommonUtils.makeInt64Array(limit2 + 1);
		
		bitmask.aryPtr[0] = 0;
		
		i = 1;
		accumulator = 0;
		while(i <= limit2) {
			accumulator |= (one << (i - 1));
			bitmask.aryPtr[i] = accumulator;
			
			i += 1;
		}
		
		return bitmask;
	}
}
