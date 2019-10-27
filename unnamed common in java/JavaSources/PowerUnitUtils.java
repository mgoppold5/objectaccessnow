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

/*
 * This Java class contains functions,
 * which deal with 2^POWER based units
 */

package unnamed.common;

public class PowerUnitUtils {
	public static int convertInt32(int i,
		short fromUnit, short toUnit, boolean roundUp) {
		
		int mult;
		int roundAdd;
		
		if(toUnit < fromUnit) {
			mult = 1 << (fromUnit - toUnit);
			return i * mult;
		}

		if(toUnit > fromUnit) {
			mult = 1 << (toUnit - fromUnit);

			roundAdd = 0;
			if(roundUp) if((i % mult) != 0) roundAdd = 1;
			
			return i / mult + roundAdd;
		}
		
		return i;
	}

	public static long convertInt64(long i,
		short fromUnit, short toUnit, boolean roundUp) {
		
		int mult;
		int roundAdd;
		
		if(toUnit < fromUnit) {
			mult = 1 << (fromUnit - toUnit);
			return i * mult;
		}

		if(toUnit > fromUnit) {
			mult = 1 << (toUnit - fromUnit);

			roundAdd = 0;
			if(roundUp) if((i % mult) != 0) roundAdd = 1;
			
			return i / mult + roundAdd;
		}
		
		return i;
	}

	public static boolean alignOkInt32(int i,
		short fromUnit, short toUnit) {
		
		int mult;
		
		if(toUnit <= fromUnit) return true;

		mult = 1 << (toUnit - fromUnit);

		return (i % mult) == 0;
	}

	public static boolean alignOkInt64(long i,
		short fromUnit, short toUnit) {
		
		int mult;
		
		if(toUnit <= fromUnit) return true;

		mult = 1 << (toUnit - fromUnit);

		return (i % mult) == 0;
	}
}
