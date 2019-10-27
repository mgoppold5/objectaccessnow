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

package unnamed.family.compiler;

public class WhitespaceHelperData extends TokenHelperData {	
	// public variables
	//

	public int contentType;
	
	public static final int CONTENT_TYPE_UNKNOWN = 1;
	public static final int CONTENT_TYPE_TABS_AND_SPACES = 2;
	public static final int CONTENT_TYPE_RETURN = 3;
	public static final int CONTENT_TYPE_NON_WHITESPACE = 4;
	
	public long tabCount;
	public long spaceCount;
	

	// private variables
	//
	
	long currentIndex;
	boolean prevCharWas13;

	
	public void reset() {
		state = STATE_START;
		contentType = CONTENT_TYPE_UNKNOWN;
		
		resultLength = 0;
		match = false;
		
		tabCount = 0;
		spaceCount = 0;
		
		prevCharWas13 = false;
	}	
}
