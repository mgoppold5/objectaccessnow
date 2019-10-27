/*
 * Copyright (c) 2017 Mike Goppold von Lobsdorf
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

public class SymbolInnerElementAllocHelper implements ElementAllocHelper {
	public static final int ELEMENT_TEXT_INDEX = 1;
	public static final int ELEMENT_SYMBOL_ID_4 = 2;
	public static final int ELEMENT_SYMBOL_ARRAY_4 = 3;
	public static final int ELEMENT_SYMBOL_ARRAY_16 = 4;
	public static final int ELEMENT_TOKEN_ARRAY_4 = 5;
	
	public Object makeElement(int elementId) {
		switch(elementId) {
		case ELEMENT_TEXT_INDEX:
			return new TextIndex();
		case ELEMENT_SYMBOL_ID_4:
			return makeSymbolId(4);
		case ELEMENT_SYMBOL_ARRAY_4:
			return new Symbol[4];
		case ELEMENT_SYMBOL_ARRAY_16:
			return new Symbol[16];
		case ELEMENT_TOKEN_ARRAY_4:
			return new Token[4];
		}
		
		throw makeInvalidEnum("elementId is bad");
	}
	
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private SymbolId makeSymbolId(int len) {
		SymbolId symId = new SymbolId();
		symId.aryPtr = new int[len];
		symId.capacity = len;
		symId.length = len;
		return symId;
	}
}
