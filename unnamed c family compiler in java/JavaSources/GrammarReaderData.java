/*
 * Copyright (c) 2015-2016 Mike Goppold von Lobsdorf
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
import unnamed.file.system.*;

public class GrammarReaderData extends BaseModuleData {
	// given parameters
	//
	
	public CommonArrayList searchableDirContextList;
	public CommonInt32Array grammarFilePath;
	
	
	// states
	//
	
	public static final int STATE_HAVE_GRAMMAR = 11;
	public static final int STATE_HAVE_SYMBOL = 12;
	
	// public variables
	//
	
	// sorted list of tokens/grams/variables/unknowns
	public CommonArrayList nameList;
	public TypeAndObject rootVariableName;
	public TypeAndObject endMarkerName;
	public TypeAndObject precSpectrumName;
	
	public CommonArrayList ruleList;
	
	
	// private variables
	//
	
	public CommonArrayList sourceStack;
	int sourceStackIndex;
	
	static final int INTERNAL_SEP_CHAR = '/';
	
	CommonInt32Array REGULAR_FILE_PATH_SEPARATOR_SET;
	CommonInt32Array FILE_PATH_SPECIAL_CHAR_SET;

	
	public static long traceOldAllocCount;
	public static long traceNewAllocCount;
	public static long traceStrangeAllocCount;
	

	public void init() {
		super.init();
		
		REGULAR_FILE_PATH_SEPARATOR_SET =
			StringUtils.int32StringSortChars(
				StringUtils.int32StringFromJavaString(
					"\\/ ."));
		
		// ref: https://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words
		FILE_PATH_SPECIAL_CHAR_SET =
			StringUtils.int32StringSortChars(
				StringUtils.int32StringFromJavaString(
					"?%*:|\"<>;"));
		
		nameList = makeArrayList();
		ruleList = makeArrayList();
		sourceStack = makeArrayList();
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
