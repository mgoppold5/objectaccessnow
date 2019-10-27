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

public class Extended2FloatTokenHelperData extends TokenHelperData {
	// private variables
	//
	
	static final int STATE_INITIAL_ZERO = 11;
	static final int STATE_RADIX = 12;
	static final int STATE_INTEGER = 13;
	static final int STATE_FRACTION = 14;
	static final int STATE_EXPONENT = 15;
	
	static final int STATE_RADIX_MARK = 21;
	static final int STATE_NUMBER_MARK = 22;
	static final int STATE_NUMBER_SIGN = 23;
	static final int STATE_RADIX_POINT = 24;
	static final int STATE_EXPONENT_MARK = 25;
	static final int STATE_EXPONENT_SIGN = 26;
	
	static final int STATE_INTEGER_UNDERSCORE = 31;
	static final int STATE_FRACTION_UNDERSCORE = 32;
	static final int STATE_EXPONENT_UNDERSCORE = 33;

	static final short
		RADIX_FOR_RADIX = (short) 10;  // must be less or equal 16

	TextIndex integerStartIndex;
	TextIndex integerPastIndex;
	TextIndex fractionStartIndex;
	TextIndex fractionPastIndex;
	TextIndex exponentStartIndex;
	TextIndex exponentPastIndex;
	
	long currentIndex;
	
	short radixAccum;
	short radix;
	
	boolean hasInteger;
	boolean hasFraction;
	boolean hasExponent;
	boolean flagNumberNeg;
	boolean flagExponentNeg;
	
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
