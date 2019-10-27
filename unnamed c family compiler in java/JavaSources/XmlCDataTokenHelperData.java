/*
 * Copyright (c) 2013-2015 Mike Goppold von Lobsdorf
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

public class XmlCDataTokenHelperData extends TokenHelperData {
	// private variables
	// 
	
	// mini states
	static final int STATE_BEGIN_STRING = 21;
	static final int STATE_MIDDLE_SPAN = 23;
	
	static final int STATE_END_GREATER_THAN = 24;
	
	static final int STATE_END_MARK1 = 41; // ]
	static final int STATE_END_MARK2 = 42; // ]
	
	long currentIndex;
	
	CommonInt32Array beginString;
	CommonInt32 stringMatchIndex;
	CommonBoolean stringMatchDidAdvance;
	CommonBoolean stringMatchFinished;
	
	TextIndex contentStartIndex;
	TextIndex contentPastIndex;
	
	
	public void init() {
		super.init();
		
		beginString = StringUtils.int32StringFromJavaString(
			Punctuation.PUNCT_XML_CDATA_START);
		
		stringMatchIndex = new CommonInt32();
		stringMatchDidAdvance = new CommonBoolean();
		stringMatchFinished = new CommonBoolean();
		
		contentStartIndex = new TextIndex();
		contentPastIndex = new TextIndex();
	}
}
