/*
 * Copyright (c) 2013-2017 Mike Goppold von Lobsdorf
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

public class GrammarUtils {
	public void init() {}
	
	public String dumpNameListFromSorted(CommonArrayList names) {
		StringBuilder sb;
		int i;
		int count;
		TypeAndObject tok;
		CommonInt32Array tStr;
		
		sb = new StringBuilder();
		count = names.size();
		i = 0;
		sb.append("[");
		while(i < count) {
			tok = (TypeAndObject) names.get(i);
			tStr = (CommonInt32Array) tok.sortObject;
			
			if(i != 0) sb.append(",");
			sb.append(StringUtils.javaStringFromInt32String(tStr));

			i += 1;
		}

		sb.append("]");
			
		return sb.toString();
	}

	public String dumpGrammarName(int nameNum, TypeAndObject[] names) {
		TypeAndObject nm = (TypeAndObject) names[nameNum];

		return StringUtils.javaStringFromInt32String(
			(CommonInt32Array) nm.sortObject);
	}
	
	public String dumpGrammarRule(
		int ruleNum, TypeAndObject[] names, GrammarRule[] rules) {

		GrammarRule r = rules[ruleNum];

		StringBuilder sb;
		int i;
		int count;

		sb = new StringBuilder();
		
		sb.append(dumpGrammarName(r.leftVar, names));
		sb.append("->");
		
		i = 0;
		count = r.rightArray.length;
		while(i < count) {
			if(i == 0) sb.append("[");
			if(i > 0) sb.append(",");
			
			sb.append(dumpGrammarName(r.rightArray.aryPtr[i], names));

			if(i == count - 1) sb.append("]");
			
			i += 1;
		}
		
		return sb.toString();
	}
	
	private CommonError makeNullPointer(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_NULL_POINTER;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeObjectNotFound(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e1.msg = msg;
		return e1;
	}
	
	private CommonError makeIndexOutOfBounds(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e1.msg = msg;
		return e1;
	}
	
	private CommonInt32 makeInt32() {
		return new CommonInt32();
	}
	
	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
	
	private CompareParams makeCompareParams() {
		return new CompareParams();
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;

		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}

	private CharReaderContext makeCharReaderContext() {
		CharReaderContext cc;

		cc = new CharReaderContext();
		cc.init();
		return cc;
	}
	
	public SymbolId makeSymbolId(int idLen) {
		SymbolId id = new SymbolId();
		id.aryPtr = new int[idLen];
		id.length = idLen;
		id.capacity = idLen;
		return id;
	}
}
