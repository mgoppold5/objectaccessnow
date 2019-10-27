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

import unnamed.common.*;

public class ExtendedIntTokenHelperData extends TokenHelperData {
	// private variables
	//
	
	static final int STATE_INTEGER = 10;
	static final int STATE_INITIAL_ZERO = 12;
	static final int STATE_INTEGER_START = 13;
	static final int STATE_FLAG_UNSIGNED = 14;
	static final int STATE_FLAG_LONG = 15;
	static final int STATE_INTEGER_UNDERSCORE = 20;

	TextIndex integerStartIndex;
	TextIndex integerPastIndex;
	
	long currentIndex;
	
	short radix;
	
	boolean flagUnsigned;
	int flagLongCount;
	
	boolean unsure;
	long unsureLength;

	
	public void init() {
		super.init();
		
		integerStartIndex = new TextIndex();
		integerPastIndex = new TextIndex();
	}
}
