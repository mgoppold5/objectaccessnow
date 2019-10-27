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

public class LrkData extends BaseModuleData {
	// given variables
	//
	
	public GrammarReaderData grmrDef;
	public int k;
	public boolean keepClosures;
	
	
	// public variables
	//
	
	public static final int NAME_COUNT_LIMIT = 32768;
	public static final int RULE_COUNT_LIMIT = 32768;
	
	public static final int FLAG_FRESH = 0;
	public static final int FLAG_UNWALKED = 1;
	
	// states
	public static final int STATE_HAVE_IMPORTED_DATA = 11;
	public static final int STATE_HAVE_POSSIBLE_FIRST_SETS = 12;
	public static final int STATE_BUSY_BUILDING_MACHINE = 13;
	public static final int STATE_HAVE_MACHINE = 14;
	
	public TypeAndObject[] names;
	public GrammarRule[] rules;
	
	public short rootRuleNum;
	public short endMarkerNameNum;
	
	public CommonArrayList tokIdToNameMap;
	public GrammarPossibleSet[] possibleFirstSets;
	
	public CommonArrayList machineStates; // of LrStateGroup
	public LrState startLrState;
	
	public void init() {
		super.init();
		
		machineStates = makeArrayList();
	}

	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
