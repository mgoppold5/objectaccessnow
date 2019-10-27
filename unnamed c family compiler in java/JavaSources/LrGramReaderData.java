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

public class LrGramReaderData extends BaseModuleData {	
	// given variables
	//

	public LrkData lrkDat;
	public CommonArrayList precedenceSpectrumStack;
	
	
	// public variables
	//
	
	public short recentReducedRule;
	
	// states
	public static final int STATE_HAVE_SHIFTED_TOKEN = 11;
	public static final int STATE_HAVE_REDUCED_GRAM = 12;
	public static final int STATE_HAVE_CONFLICT = 13;
	public static final int STATE_DONE = 14;
	
	
	// protected variables
	//
	
	public LrAheadCircleQueue lrAhead;
	public LrStack lrStack;
	

	// private variables
	//
	

	short aheadTokNum;
	CommonInt16Array aheadTokNumStr;
	
	
	BufferNode lrReduceRules;
	BufferNode compRecords;
	BufferNode sortRecords;
	BufferNode rightLenStrings;
	
	int k;
	int kMinOne;
	
	
	public static long traceOldAllocCount;
	public static long traceNewAllocCount;
	
	
	public void init() {
		super.init();
		
		lrStack = new LrStack();
		lrStack.stack = makeArrayList();
		
		lrAhead = new LrAheadCircleQueue();
		
		lrReduceRules = new BufferNode();
		lrReduceRules.theObject = makeArrayList();
		
		compRecords = new BufferNode();
		compRecords.theObject = makeArrayList();

		sortRecords = new BufferNode();
		sortRecords.theObject = makeArrayList();
		
		rightLenStrings = new BufferNode();
		rightLenStrings.theObject = makeArrayList();
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
