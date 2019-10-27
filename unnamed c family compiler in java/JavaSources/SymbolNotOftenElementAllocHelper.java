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

public class SymbolNotOftenElementAllocHelper implements ElementAllocHelper {
	public static final int ELEMENT_TOKEN_CONTAINER = 1;
	public static final int ELEMENT_TOKEN_FLOAT_FULL = 2;
	public static final int ELEMENT_TOKEN_INTEGER_FULL = 3;
	public static final int ELEMENT_TOKEN_INTEGER_SIMPLE = 4;
	public static final int ELEMENT_TOKEN_TABS_AND_SPACES = 5;
	public static final int ELEMENT_TOKEN_STRING = 6;
	public static final int ELEMENT_GRAM_CONTAINER_LIST = 7;
	public static final int ELEMENT_ARRAY_LIST = 8;
	
	public Object makeElement(int elementId) {
		Symbol sym;
		
		switch(elementId) {
		case ELEMENT_TOKEN_CONTAINER:
			sym = new TokenContainer();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_CONTAINER;
			return sym;
		case ELEMENT_TOKEN_FLOAT_FULL:
			sym = new TokenFloatFull();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_FLOAT_FULL;
			return sym;
		case ELEMENT_TOKEN_INTEGER_FULL:
			sym = new TokenIntegerFull();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_INTEGER_FULL;
			return sym;
		case ELEMENT_TOKEN_INTEGER_SIMPLE:
			sym = new TokenIntegerSimple();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_INTEGER_SIMPLE;
			return sym;
		case ELEMENT_TOKEN_TABS_AND_SPACES:
			sym = new TokenTabsAndSpaces();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_TABS_AND_SPACES;
			return sym;
		case ELEMENT_TOKEN_STRING:
			sym = new TokenString();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_STRING;
			return sym;
		case ELEMENT_GRAM_CONTAINER_LIST:
			sym = new GramContainerList();
			sym.symbolType = SymbolTypes.TYPE_GRAM;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST;
			return sym;
		case ELEMENT_ARRAY_LIST:
			return CommonUtils.makeArrayList();
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
}
