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

public class LrkCanonData extends LrkData {	
	// public variables
	//
	
	// mini states
	public static final int STATE_EXISTS_UNWALKED_LR_STATES = 21;
	public static final int STATE_BUSY_BUILDING_CLOSURES = 22;
	public static final int STATE_BUSY_WALKING_LR_STATES = 23;
	public static final int STATE_BUSY_UNBUILDING_CLOSURES = 24;
	public static final int STATE_BUSY_COMMITING_NEW_LR_STATES = 25;
	public static final int STATE_BUSY_COMMITING_NEW_EDGES = 26;
	public static final int STATE_BUSY_UPDATING_UNWALKED_LR_STATES = 27;
	
	// private variables
	//
	
	// at first new states are fresh,
	// then they are unwalked machineStates,
	// then they are regular machineStates
	CommonArrayList unwalkedStates; // of LrStateGroup
	CommonArrayList freshStates; // of LrStateGroup
}
