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
 * This Java class has character encoding utilities.
 */

package unnamed.common;

public class CharacterEncodingUtils {
	// returns a size in bytes
	public static int getCodeUnitSize(int encoding) {
		switch(encoding) {
		case CharacterEncoding.UTF_8:
			return 1;
		case CharacterEncoding.UTF_16_LE:
			return 2;
		case CharacterEncoding.UTF_32_LE:
			return 4;
		}
		
		throw makeUnknownCharEncoding();
	}
	
	private static CommonError makeUnknownCharEncoding() {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNKNOWN_CHARACTER_ENCODING;
		return e1;
	}
}
