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

public class ExtendedFloatTokenHelperData extends TokenHelperData {
	// private variables
	//
	
	static final int STATE_INITIAL_ZERO = 10;
	static final int STATE_NUMBER_START = 11;
	static final int STATE_INTEGER = 12;
	static final int STATE_RADIX_POINT = 13;
	static final int STATE_FRACTION = 14;
	static final int STATE_EXPONENT_MARK = 15;
	static final int STATE_EXPONENT_SIGN = 16;
	static final int STATE_EXPONENT = 17;
	static final int STATE_FLAG = 18;
	static final int STATE_INTEGER_UNDERSCORE = 20;
	static final int STATE_FRACTION_UNDERSCORE = 21;
	static final int STATE_EXPONENT_UNDERSCORE = 22;

	TextIndex integerStartIndex;
	TextIndex integerPastIndex;
	TextIndex fractionStartIndex;
	TextIndex fractionPastIndex;
	TextIndex exponentStartIndex;
	TextIndex exponentPastIndex;
	
	long currentIndex;
		
	short radix;
	
	boolean hasInteger;
	boolean hasFraction;
	boolean hasExponent;
	boolean flagExponentNeg;
	short flagSize;
	
	boolean unsure;
	long unsureLength;

	
	public void init() {
		super.init();
		
		integerStartIndex = new TextIndex();
		integerPastIndex = new TextIndex();
		
		fractionStartIndex = new TextIndex();
		fractionPastIndex = new TextIndex();
		
		exponentStartIndex = new TextIndex();
		exponentPastIndex = new TextIndex();
	}
}
