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

public class CFamilyCommentTokenHelperData extends TokenHelperData {
	// private variables
	//
	
	// mini states
	static final int STATE_BEGIN_SLASH = 21;
	static final int STATE_BEGIN_SLASH2 = 22;
	static final int STATE_BEGIN_ASTERISK = 23;
	static final int STATE_MIDDLE_SINGLE_LINE = 24;
	static final int STATE_MIDDLE_MULTI_LINE = 25;
	static final int STATE_END_ASTERISK = 26;
	static final int STATE_END_SLASH = 27;
	
	long currentIndex;
	
	int commentType;
	
	static final int COMMENT_TYPE_UNKNOWN = 0;
	static final int COMMENT_TYPE_SINGLE_LINE = 1;
	static final int COMMENT_TYPE_MULTI_LINE = 2;
	
	TextIndex contentStartIndex;
	TextIndex contentPastIndex;

	
	public void init() {
		super.init();
		
		contentStartIndex = new TextIndex();
		contentPastIndex = new TextIndex();
	}
}
