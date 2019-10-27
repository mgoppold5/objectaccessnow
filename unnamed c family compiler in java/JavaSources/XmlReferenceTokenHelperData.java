/*
 * Copyright (c) 2013-2014 Mike Goppold von Lobsdorf
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

public class XmlReferenceTokenHelperData extends TokenHelperData {
	// private variables
	//

	// mini states
	static final int STATE_REFERENCE = 41;
	static final int STATE_REFERENCE_NAME = 42;
	static final int STATE_REFERENCE_NAME_END = 43;
	static final int STATE_REFERENCE_NUMBER = 44;
	static final int STATE_REFERENCE_LETTER_X = 45;
	static final int STATE_REFERENCE_INTEGER = 46;
	static final int STATE_REFERENCE_INTEGER_END = 47;
	
	long currentIndex;
	boolean unsure;
	long unsureLength;
	
	TextIndex badSpanStartIndex;
	TextIndex badSpanPastIndex;

	TextIndex refStartIndex;
	TextIndex refPastIndex;
	
	boolean integerValid;
	short radix;
	TextIndex integerStartIndex;
	TextIndex integerPastIndex;
	
	boolean nameValid;
	TextIndex nameStartIndex;
	TextIndex namePastIndex;
	
	
	public void init() {
		super.init();
		
		badSpanStartIndex = new TextIndex();
		badSpanPastIndex = new TextIndex();
		
		refStartIndex = new TextIndex();
		refPastIndex = new TextIndex();
		
		integerStartIndex = new TextIndex();
		integerPastIndex = new TextIndex();

		nameStartIndex = new TextIndex();
		namePastIndex = new TextIndex();
	}
}
