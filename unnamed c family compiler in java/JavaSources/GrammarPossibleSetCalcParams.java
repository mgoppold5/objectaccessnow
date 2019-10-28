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

public class GrammarPossibleSetCalcParams {
	public int k;
	
	public GrammarPossibleSet accumSet;
	
	public CommonInt16Array symbolString;
	public int symbolStringLen;
	
	public CommonInt16Array tokenString;
	public int tokenStringLen;
	public boolean tokenStringNotSync;
	
	public CommonArrayList stringNumberStack;
	
	public GrammarPossibleSet[] possibleFirstSets;
	
	public CommonBoolean change;
	public CommonBoolean done;
	
	public SortParams sortRec;
	
	public CommonArrayList tempStrStore;
}