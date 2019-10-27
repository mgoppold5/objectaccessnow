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

public class SymbolReAllocUtils {
	public void init() {}
	
	private boolean isFunctionGram(Symbol sym, GeneralUtils utils) {
		int id = utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_FUNCTION_DEF_FULL
			|| id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS
			|| id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_INIT
			|| id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT)
			return true;
		
		return false;
	}
	
	private void breakupSymbolIdInSymbol(Symbol sym) {
		int i;
		int len;
		
		//if(sym.id == null) return;
		
		i = 0;
		len = sym.id.length;
		while(i < len) {
			sym.id.aryPtr[i] = 0;
			i += 1;
		}
		
		sym.id.length = 0;
		
		sym.id = null;
	}
	
	private void breakupAllTextIndex(Symbol srcSym) {
		srcSym.startIndex = null;
		srcSym.pastIndex = null;
	}
	
	public void breakupSymbolTree(Symbol srcSym) {
		int i;
		int len;
		
		if(true) {
			//if(srcSym == null) return;
			
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
				
				GramContainer srcSym2 = (GramContainer) srcSym;
				
				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				i = 0;
				len = srcSym2.childCount;
				while(i < len) {
					breakupSymbolTree(srcSym2.sym[i]);
					srcSym2.sym[i] = null;
					i += 1;
				}
				
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN) {
				
				Token srcSym2 = (Token) srcSym;
				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_CONTAINER) {
				
				TokenContainer srcSym2 = (TokenContainer) srcSym;
				
				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				i = 0;
				len = srcSym2.childCount;
				while(i < len) {
					breakupSymbolTree(srcSym2.tok[i]);
					srcSym2.tok[i] = null;
					i += 1;
				}
				
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_FLOAT_FULL) {
				
				TokenFloatFull srcSym2 = (TokenFloatFull) srcSym;

				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				breakupSymbolTree(srcSym2.integer);
				srcSym2.integer = null;
				breakupSymbolTree(srcSym2.fraction);
				srcSym2.fraction = null;
				breakupSymbolTree(srcSym2.exponent);
				srcSym2.exponent = null;

				srcSym2.radix = 0;
				srcSym2.size = 0;
				srcSym2.flagNumberNeg = false;

				srcSym2.flagExponentNeg = false;
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_INTEGER_FULL) {
				
				TokenIntegerFull srcSym2 = (TokenIntegerFull) srcSym;

				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				breakupSymbolTree(srcSym2.integer);
				srcSym2.integer = null;

				srcSym2.radix = 0;
				srcSym2.size = 0;
				srcSym2.flagNumberNeg = false;
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_INTEGER_SIMPLE) {
				
				TokenIntegerSimple srcSym2 = (TokenIntegerSimple) srcSym;

				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				breakupSymbolTree(srcSym2.integer);
				srcSym2.integer = null;

				srcSym2.radix = 0;
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_TABS_AND_SPACES) {
				
				TokenTabsAndSpaces srcSym2 = (TokenTabsAndSpaces) srcSym;

				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				srcSym2.tabCount = 0;
				srcSym2.spaceCount = 0;
				return;
			}
			
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_STRING) {
				
				TokenString srcSym2 = (TokenString) srcSym;

				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				i = 0;
				len = srcSym2.elements.size();
				while(i < len) {
					breakupSymbolTree((Symbol) srcSym2.elements.get(i));
					i += 1;
				}
				
				srcSym2.elements.clear();
				srcSym2.elements = null;
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST) {
				
				GramContainerList srcSym2 = (GramContainerList) srcSym;

				breakupAllTextIndex(srcSym);
				breakupSymbolIdInSymbol(srcSym);

				i = 0;
				len = srcSym2.symList.size();
				while(i < len) {
					breakupSymbolTree((Symbol) srcSym2.symList.get(i));
					i += 1;
				}
				
				srcSym2.symList.clear();
				srcSym2.symList = null;
				return;
			}

			throw makeIllegalState("unhandled symbol");
		}
		
		return;
	}

	private void doTrimForFunction(Symbol sym, GeneralUtils utils) {
		int id;
		GramContainer grmCon;
		
		id = utils.getSymbolIdPrimary(sym);
		grmCon = (GramContainer) sym;
		
		if(id == Symbols.GRAM_FUNCTION_DEF_FULL) {
			grmCon.sym[1] = null;
			return;
		}

		if(id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS) {
			grmCon.sym[2] = null;
			return;
		}

		if(id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_INIT) {
			grmCon.sym[2] = null;
			return;
		}

		if(id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT) {
			grmCon.sym[3] = null;
			return;
		}
		
		throw makeIllegalState("unexpected symbol");
	}
	
	public void doTrim(Symbol srcSym, GeneralUtils utils) {
		int i;
		int len;

		if(true) {
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
				
				GramContainer srcSym2 = (GramContainer) srcSym;
				
				boolean isFunc = isFunctionGram(srcSym, utils);
				
				if(isFunc) {
					doTrimForFunction(srcSym, utils);
				}
				
				if(!isFunc) {
					i = 0;
					len = srcSym2.childCount;
					while(i < len) {
						doTrim(srcSym2.sym[i], utils);
						i += 1;
					}
				}
				
				return;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN) return;
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_CONTAINER) return;
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_FLOAT_FULL) return;
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_INTEGER_FULL) return;
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_INTEGER_SIMPLE) return;
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_TABS_AND_SPACES) return;
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_STRING) return;

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST) {
				
				GramContainerList srcSym2 = (GramContainerList) srcSym;

				i = 0;
				len = srcSym2.symList.size();
				while(i < len) {
					doTrim((Symbol) srcSym2.symList.get(i), utils);
					i += 1;
				}

				return;
			}
			
			throw makeIllegalState("unhandled symbol");
		}
	}

	public Symbol doReAlloc(Symbol srcSym, SymbolAllocHelper allocHelp,
		GeneralUtils utils) {
		
		int i;
		int len;
		
		if(true) {
			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
				
				GramContainer srcSym2 = (GramContainer) srcSym;
				GramContainer dstSym2 = allocHelp.makeGramContainer(
					utils.getIdLenFromSymbol(srcSym2), srcSym2.childCount);
				
				copySymbolStuff(dstSym2, srcSym2, utils);
				
				i = 0;
				len = dstSym2.childCount;
				while(i < len) {
					if(srcSym2.sym[i] == null) {
						dstSym2.sym[i] = null;
						i += 1;
						continue;
					}
					
					dstSym2.sym[i] = doReAlloc(
						srcSym2.sym[i], allocHelp, utils);
					i += 1;
				}
				
				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN) {
				
				Token srcSym2 = (Token) srcSym;
				Token dstSym2 = allocHelp.makeToken(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);
				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_CONTAINER) {
				
				TokenContainer srcSym2 = (TokenContainer) srcSym;
				TokenContainer dstSym2 = allocHelp.makeTokenContainer(
					utils.getIdLenFromSymbol(srcSym2), srcSym2.childCount);

				copySymbolStuff(dstSym2, srcSym2, utils);

				i = 0;
				len = dstSym2.childCount;
				while(i < len) {
					dstSym2.tok[i] = (Token) doReAlloc(
						srcSym2.tok[i], allocHelp, utils);
					i += 1;
				}

				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_FLOAT_FULL) {
				
				TokenFloatFull srcSym2 = (TokenFloatFull) srcSym;
				TokenFloatFull dstSym2 = allocHelp.makeTokenFloatFull(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);

				dstSym2.integer = (Token) doReAlloc(
					srcSym2.integer, allocHelp, utils);
				dstSym2.fraction = (Token) doReAlloc(
					srcSym2.fraction, allocHelp, utils);
				dstSym2.exponent = (Token) doReAlloc(
					srcSym2.exponent, allocHelp, utils);

				dstSym2.radix = srcSym2.radix;
				dstSym2.size = srcSym2.size;
				dstSym2.flagNumberNeg = srcSym2.flagNumberNeg;

				dstSym2.flagExponentNeg = srcSym2.flagExponentNeg;
				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_INTEGER_FULL) {
				
				TokenIntegerFull srcSym2 = (TokenIntegerFull) srcSym;
				TokenIntegerFull dstSym2 = allocHelp.makeTokenIntegerFull(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);

				dstSym2.integer = (Token) doReAlloc(
					srcSym2.integer, allocHelp, utils);

				dstSym2.radix = srcSym2.radix;
				dstSym2.size = srcSym2.size;
				dstSym2.flagNumberNeg = srcSym2.flagNumberNeg;

				dstSym2.flagUnsigned = srcSym2.flagUnsigned;
				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_INTEGER_SIMPLE) {
				
				TokenIntegerSimple srcSym2 = (TokenIntegerSimple) srcSym;
				TokenIntegerSimple dstSym2 = allocHelp.makeTokenIntegerSimple(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);

				dstSym2.integer = (Token) doReAlloc(
					srcSym2.integer, allocHelp, utils);

				dstSym2.radix = srcSym2.radix;
				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_TABS_AND_SPACES) {
				
				TokenTabsAndSpaces srcSym2 = (TokenTabsAndSpaces) srcSym;
				TokenTabsAndSpaces dstSym2 = allocHelp.makeTokenTabsAndSpaces(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);
				
				dstSym2.tabCount = srcSym2.tabCount;
				dstSym2.spaceCount = srcSym2.spaceCount;

				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_TOKEN_STRING) {
				
				TokenString srcSym2 = (TokenString) srcSym;
				TokenString dstSym2 = allocHelp.makeTokenString(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);
				
				dstSym2.elements = allocHelp.makeArrayList();
				
				i = 0;
				len = srcSym2.elements.size();
				while(i < len) {
					dstSym2.elements.add((Token) doReAlloc(
						(Symbol) srcSym2.elements.get(i), allocHelp, utils));
					i += 1;
				}

				return dstSym2;
			}

			if(srcSym.symbolStorageType
				== SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST) {
				
				GramContainerList srcSym2 = (GramContainerList) srcSym;
				GramContainerList dstSym2 = allocHelp.makeGramContainerList(
					utils.getIdLenFromSymbol(srcSym2));

				copySymbolStuff(dstSym2, srcSym2, utils);
				
				dstSym2.symList = allocHelp.makeArrayList();
				
				i = 0;
				len = srcSym2.symList.size();
				while(i < len) {
					dstSym2.symList.add((Token) doReAlloc(
						(Symbol) srcSym2.symList.get(i), allocHelp, utils));
					i += 1;
				}

				return dstSym2;
			}
			
			throw makeIllegalState("unhandled symbol");
		}
		
		return srcSym;
	}
	
	private void copySymbolStuff(Symbol sym, Symbol sym2,
		GeneralUtils utils) {
		
		sym.symbolType = sym2.symbolType;
		utils.copySymbolIdToSymbolFromSymbol(sym, sym2);

		sym.disableAllTextIndex = sym2.disableAllTextIndex;

		if(!sym.disableAllTextIndex) {
			utils.copyTextIndex(sym.startIndex, sym2.startIndex);
			utils.copyTextIndex(sym.pastIndex, sym2.pastIndex);
		}
	}

	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}
}
