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

public class SymbolOftenElementAllocHelper implements ElementAllocHelper {
	public static final int ELEMENT_TOKEN = 1;
	public static final int ELEMENT_GRAM_CONTAINER = 2;
	
	public Object makeElement(int elementId) {
		Symbol sym;
		
		switch(elementId) {
		case ELEMENT_TOKEN:
			sym = new Token();
			sym.symbolType = SymbolTypes.TYPE_TOKEN;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN;
			return sym;
		case ELEMENT_GRAM_CONTAINER:
			sym = new GramContainer();
			sym.symbolType = SymbolTypes.TYPE_GRAM;
			sym.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER;
			return sym;
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
