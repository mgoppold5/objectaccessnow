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

public class StringTokenHelperData
	extends TokenHelperData {
	
	public boolean enableInitialLetterL;
	public boolean enableCEscapes;
	public boolean enableCEscapeIgnoredReturn;
	public boolean enableXmlReferences;
	
	public int quoteBeginChar;
	public int quoteEndChar;
	
	public int tokenCategoryId;
	public int tokenId;

	
	// private variables
	//
	
	// mini states
	static final int STATE_INITIAL_LETTER_L = 21;
	static final int STATE_QUOTE = 22;
	static final int STATE_QUOTE_END = 23;
	static final int STATE_SPAN = 24;
	static final int STATE_ESCAPE = 25;
	static final int STATE_ESCAPE_LETTER_X = 26;
	static final int STATE_ESCAPE_INTEGER = 27;
	static final int STATE_ESCAPE_NAME = 28;
	static final int STATE_ESCAPE_VERBATIM = 29;
	static final int STATE_ESCAPE_IGNORED_RETURN = 31;

	static final int STATE_REFERENCE = 41;
	static final int STATE_REFERENCE_NAME = 42;
	static final int STATE_REFERENCE_NAME_END = 43;
	static final int STATE_REFERENCE_NUMBER = 44;
	static final int STATE_REFERENCE_LETTER_X = 45;
	static final int STATE_REFERENCE_INTEGER = 46;
	static final int STATE_REFERENCE_INTEGER_END = 47;
	

	public CommonArrayList elementList;
	
	long currentIndex;
	
	//boolean haveSpanGoing;
	
	TextIndex badSpanStartIndex;
	TextIndex badSpanPastIndex;
	
	TextIndex spanStartIndex;
	TextIndex spanPastIndex;
	
	TextIndex escapeStartIndex;
	TextIndex escapePastIndex;
	
	TextIndex returnStartIndex;
	TextIndex returnPastIndex;

	TextIndex refStartIndex;
	TextIndex refPastIndex;
	
	short radix;
	TextIndex integerStartIndex;
	TextIndex integerPastIndex;

	TextIndex verbatimStartIndex;
	TextIndex verbatimPastIndex;
	
	TextIndex nameStartIndex;
	TextIndex namePastIndex;
	
	boolean prevCharWas13;
	
	int escapeTokenId;
	
	public void init() {
		super.init();
		
		badSpanStartIndex = new TextIndex();
		badSpanPastIndex = new TextIndex();
		
		spanStartIndex = new TextIndex();
		spanPastIndex = new TextIndex();
		
		escapeStartIndex = new TextIndex();
		escapePastIndex = new TextIndex();
		
		returnStartIndex = new TextIndex();
		returnPastIndex = new TextIndex();

		refStartIndex = new TextIndex();
		refPastIndex = new TextIndex();
		
		integerStartIndex = new TextIndex();
		integerPastIndex = new TextIndex();

		verbatimStartIndex = new TextIndex();
		verbatimPastIndex = new TextIndex();

		nameStartIndex = new TextIndex();
		namePastIndex = new TextIndex();
		
		quoteBeginChar = '"';
		quoteEndChar = '"';

		enableInitialLetterL = false;
		enableCEscapes = false;
		enableXmlReferences = false;
	}
}
