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

public class SymbolAllocHelper2 implements SymbolAllocHelper {
	public static long traceBufferAllocCount;
	public static long traceNewBufferAllocCount;
	
	
	private static final int CURSOR_COUNT = 15;
	
	private static final int CURSOR_TEXT_INDEX = 0;
	private static final int CURSOR_SYMBOL_ID_4 = 1;
	private static final int CURSOR_SYMBOL_ARRAY_4 = 2;
	private static final int CURSOR_SYMBOL_ARRAY_16 = 3;
	private static final int CURSOR_TOKEN_ARRAY_4 = 4;
	
	private static final int CURSOR_TOKEN = 5;
	private static final int CURSOR_GRAM_CONTAINER = 6;
	
	private static final int CURSOR_TOKEN_CONTAINER = 7;
	private static final int CURSOR_TOKEN_FLOAT_FULL = 8;
	private static final int CURSOR_TOKEN_INTEGER_FULL = 9;
	private static final int CURSOR_TOKEN_INTEGER_SIMPLE = 10;
	private static final int CURSOR_TOKEN_TABS_AND_SPACES = 11;
	private static final int CURSOR_TOKEN_STRING = 12;
	private static final int CURSOR_GRAM_CONTAINER_LIST = 13;

	private static final int CURSOR_ARRAY_LIST = 14;
	
	private static final short TYPICAL_GROW_STEP_SIZE = (short) 1;
	
	private SymbolOftenElementAllocHelper oftenHelp;
	private SymbolInnerElementAllocHelper innerHelp;
	private SymbolNotOftenElementAllocHelper notOftenHelp;
	
	private CursorAllocHelper[] cursors;
	
	public void init() {
		CursorAllocHelper cur;
		
		oftenHelp = new SymbolOftenElementAllocHelper();
		innerHelp = new SymbolInnerElementAllocHelper();
		notOftenHelp = new SymbolNotOftenElementAllocHelper();
		
		cursors = new CursorAllocHelper[CURSOR_COUNT];
		
		// Often elements
		//
		
		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(oftenHelp, SymbolOftenElementAllocHelper.ELEMENT_TOKEN);
		cur.init();
		cursors[CURSOR_TOKEN] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(oftenHelp, SymbolOftenElementAllocHelper.ELEMENT_GRAM_CONTAINER);
		cur.init();
		cursors[CURSOR_GRAM_CONTAINER] = cur;

		// Inner elements
		//

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(innerHelp, SymbolInnerElementAllocHelper.ELEMENT_TEXT_INDEX);
		cur.init();
		cursors[CURSOR_TEXT_INDEX] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(innerHelp, SymbolInnerElementAllocHelper.ELEMENT_SYMBOL_ID_4);
		cur.init();
		cursors[CURSOR_SYMBOL_ID_4] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(innerHelp, SymbolInnerElementAllocHelper.ELEMENT_SYMBOL_ARRAY_4);
		cur.init();
		cursors[CURSOR_SYMBOL_ARRAY_4] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(innerHelp, SymbolInnerElementAllocHelper.ELEMENT_SYMBOL_ARRAY_16);
		cur.init();
		cursors[CURSOR_SYMBOL_ARRAY_16] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(innerHelp, SymbolInnerElementAllocHelper.ELEMENT_TOKEN_ARRAY_4);
		cur.init();
		cursors[CURSOR_TOKEN_ARRAY_4] = cur;

		// Not often elements
		//
		
		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_TOKEN_CONTAINER);
		cur.init();
		cursors[CURSOR_TOKEN_CONTAINER] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_TOKEN_FLOAT_FULL);
		cur.init();
		cursors[CURSOR_TOKEN_FLOAT_FULL] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_TOKEN_INTEGER_FULL);
		cur.init();
		cursors[CURSOR_TOKEN_INTEGER_FULL] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_TOKEN_INTEGER_SIMPLE);
		cur.init();
		cursors[CURSOR_TOKEN_INTEGER_SIMPLE] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_TOKEN_TABS_AND_SPACES);
		cur.init();
		cursors[CURSOR_TOKEN_TABS_AND_SPACES] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_TOKEN_STRING);
		cur.init();
		cursors[CURSOR_TOKEN_STRING] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_GRAM_CONTAINER_LIST);
		cur.init();
		cursors[CURSOR_GRAM_CONTAINER_LIST] = cur;

		cur = new CursorAllocHelper();
		cur.initGrowStepSize((short) TYPICAL_GROW_STEP_SIZE);
		cur.initElementAllocHelper(notOftenHelp, SymbolNotOftenElementAllocHelper.ELEMENT_ARRAY_LIST);
		cur.init();
		cursors[CURSOR_ARRAY_LIST] = cur;
	}

	public int getCursorCount() {return CURSOR_COUNT;}
	
	public void getCursorPositions(CommonInt32Array cursorStore) {
		if(cursorStore == null)
			throw new NullPointerException();
		if(cursorStore.length != CURSOR_COUNT)
			throw new IndexOutOfBoundsException();
		
		int i;
		int count;
		int pos;
		
		count = CURSOR_COUNT;
		i = 0;
		while(i < count) {
			CursorAllocHelper cur = cursors[i];
			
			pos = 0;
			if(cur != null)
				pos = cur.getCursorPosition();
			
			cursorStore.aryPtr[i] = pos;
			
			i += 1;
		}
		
		return;
	}

	public void setCursorPositions(CommonInt32Array cursorStore) {
		if(cursorStore == null)
			throw new NullPointerException();
		if(cursorStore.length != CURSOR_COUNT)
			throw new IndexOutOfBoundsException();
		
		int i;
		int count;
		int pos;
		
		count = CURSOR_COUNT;
		i = 0;
		while(i < count) {
			CursorAllocHelper cur = cursors[i];
			
			pos = cursorStore.aryPtr[i];
			
			if(cur != null)
				cur.setCursorPosition(pos);
			
			i += 1;
		}
		
		return;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null) return new IllegalStateException(msg);
		return new IllegalStateException();
	}
	
	private void allocSymbolId(Symbol sym, int idLen) {
		CursorAllocHelper cur;

		if(idLen <= 4) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_SYMBOL_ID_4];
			sym.id = (SymbolId) cur.makeElement();
			sym.id.length = idLen;
		}

		if(idLen > 4) {
			traceBufferAllocCount += 1;

			sym.id = makeSymbolIdConventional(idLen);
			sym.id.length = idLen;
		}
	}

	private void allocAllTextIndex(Symbol sym) {
		CursorAllocHelper cur;

		if(true) {
			traceNewBufferAllocCount += 2;

			cur = cursors[CURSOR_TEXT_INDEX];
			sym.startIndex = (TextIndex) cur.makeElement();
			sym.pastIndex = (TextIndex) cur.makeElement();
			return;
		}
		
		traceBufferAllocCount += 2;
		
		sym.startIndex = new TextIndex();
		sym.pastIndex = new TextIndex();
	}
	
	public void allocSymbolArray(GramContainer grmCon, int childCount) {
		CursorAllocHelper cur;
		
		if(childCount <= 4) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_SYMBOL_ARRAY_4];
			grmCon.sym = (Symbol[]) cur.makeElement();
			grmCon.childCount = childCount;
		}

		if(childCount > 4 && childCount <= 16) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_SYMBOL_ARRAY_16];
			grmCon.sym = (Symbol[]) cur.makeElement();
			grmCon.childCount = childCount;
		}

		if(childCount > 16) {
			traceBufferAllocCount += 1;

			grmCon.sym = (Symbol[]) new Symbol[childCount];
			grmCon.childCount = childCount;
		}
	}

	public void allocTokenArray(TokenContainer tokCon, int childCount) {
		CursorAllocHelper cur;
		
		if(childCount <= 4) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_TOKEN_ARRAY_4];
			tokCon.tok = (Token[]) cur.makeElement();
			tokCon.childCount = childCount;
		}

		if(childCount > 4) {
			traceBufferAllocCount += 1;

			tokCon.tok = (Token[]) new Token[childCount];
			tokCon.childCount = childCount;
		}
	}
	
	private void clearSymbol(Symbol sym) {
		sym.disableAllTextIndex = false;
		sym.startIndex = null;
		sym.pastIndex = null;
		sym.id = null;
		//sym.symbolType = 0;
		//sym.symbolStorageType = 0;
		return;
	}

	private void clearGramContainer(GramContainer grmCon) {
		clearSymbol(grmCon);
		grmCon.sym = null;
		grmCon.childCount = 0;
	}

	private void clearTokenContainer(TokenContainer tokCon) {
		clearSymbol(tokCon);
		tokCon.tok = null;
		tokCon.childCount = 0;
	}

	private void clearGramContainerList(GramContainerList grmList) {
		clearSymbol(grmList);
		grmList.symList = null;
	}
	
	public Token makeToken(int idLen) {
		Token tok;
		CursorAllocHelper cur;

		if(true) {
			traceNewBufferAllocCount += 1;
			
			cur = cursors[CURSOR_TOKEN];
			tok = (Token) cur.makeElement();
			clearSymbol(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			return tok;
		}
		
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 1;
		
		tok = new Token();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN;
		
		tok.initAllTextIndex();
		
		tok.id = makeSymbolIdConventional(idLen);
		return tok;
	}
	
	public GramContainer makeGramContainer(int idLen, int childCount) {
		GramContainer g;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;
			
			cur = cursors[CURSOR_GRAM_CONTAINER];
			g = (GramContainer) cur.makeElement();
			clearGramContainer(g);
			
			allocAllTextIndex(g);
			allocSymbolId(g, idLen);
			allocSymbolArray(g, childCount);
			return g;
		}
		
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 2;

		g = new GramContainer();
		g.symbolType = SymbolTypes.TYPE_GRAM;
		g.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER;

		g.initAllTextIndex();
		
		g.id = makeSymbolIdConventional(idLen);
		g.sym = new Symbol[childCount];
		g.childCount = childCount;
		return g;
	}
	
	public TokenContainer makeTokenContainer(int idLen, int childCount) {
		TokenContainer tok;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;
			
			cur = cursors[CURSOR_TOKEN_CONTAINER];
			tok = (TokenContainer) cur.makeElement();
			clearTokenContainer(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			allocTokenArray(tok, childCount);
			return tok;
		}
		
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 2;

		tok = new TokenContainer();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_CONTAINER;
		
		tok.initAllTextIndex();
		
		tok.id = makeSymbolIdConventional(idLen);
		tok.tok = new Token[childCount];
		tok.childCount = childCount;
		return tok;
	}

	public TokenFloatFull makeTokenFloatFull(int idLen) {
		TokenFloatFull tok;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_TOKEN_FLOAT_FULL];
			tok = (TokenFloatFull) cur.makeElement();
			clearSymbol(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			return tok;
		}
	
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 1;

		tok = new TokenFloatFull();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_FLOAT_FULL;
		
		tok.initAllTextIndex();

		tok.id = makeSymbolIdConventional(idLen);
		return tok;
	}

	public TokenIntegerFull makeTokenIntegerFull(int idLen) {
		TokenIntegerFull tok;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;
			
			cur = cursors[CURSOR_TOKEN_INTEGER_FULL];
			tok = (TokenIntegerFull) cur.makeElement();
			clearSymbol(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			return tok;
		}
	
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 1;

		tok = new TokenIntegerFull();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_INTEGER_FULL;

		tok.initAllTextIndex();

		tok.id = makeSymbolIdConventional(idLen);
		return tok;
	}
	
	public TokenIntegerSimple makeTokenIntegerSimple(int idLen) {
		TokenIntegerSimple tok;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_TOKEN_INTEGER_SIMPLE];
			tok = (TokenIntegerSimple) cur.makeElement();
			clearSymbol(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			return tok;
		}
	
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 1;

		tok = new TokenIntegerSimple();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_INTEGER_SIMPLE;

		tok.initAllTextIndex();

		tok.id = makeSymbolIdConventional(idLen);
		return tok;
	}

	public TokenTabsAndSpaces makeTokenTabsAndSpaces(int idLen) {
		TokenTabsAndSpaces tok;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_TOKEN_TABS_AND_SPACES];
			tok = (TokenTabsAndSpaces) cur.makeElement();
			clearSymbol(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			return tok;
		}
		
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 1;

		tok = new TokenTabsAndSpaces();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_TABS_AND_SPACES;

		tok.initAllTextIndex();
		
		tok.id = makeSymbolIdConventional(idLen);
		return tok;
	}
	
	public TokenString makeTokenString(int idLen) {
		TokenString tok;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_TOKEN_STRING];
			tok = (TokenString) cur.makeElement();
			clearSymbol(tok);
			
			allocAllTextIndex(tok);
			allocSymbolId(tok, idLen);
			return tok;
		}
	
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 1;

		tok = new TokenString();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_STRING;
		
		tok.initAllTextIndex();

		tok.id = makeSymbolIdConventional(idLen);
		return tok;
	}
	
	public GramContainerList makeGramContainerList(int idLen) {
		GramContainerList g;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 1;

			cur = cursors[CURSOR_GRAM_CONTAINER_LIST];
			g = (GramContainerList) cur.makeElement();
			clearGramContainerList(g);
			
			allocAllTextIndex(g);
			allocSymbolId(g, idLen);
			g.symList = makeArrayList();
			return g;
		}
		
		traceBufferAllocCount += 3;
		traceBufferAllocCount += 4;

		g = new GramContainerList();
		g.symbolType = SymbolTypes.TYPE_GRAM;
		g.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST;

		g.initAllTextIndex();
		
		g.id = makeSymbolIdConventional(idLen);
		g.symList = makeArrayListConventional();
		return g;
	}

	public CommonArrayList makeArrayList() {
		CommonArrayList aList;
		CursorAllocHelper cur;
		
		if(true) {
			traceNewBufferAllocCount += 3;
			
			cur = cursors[CURSOR_ARRAY_LIST];
			aList = (CommonArrayList) cur.makeElement();
			aList.clear();
			
			return aList;
		}

		traceBufferAllocCount += 3;
		return CommonUtils.makeArrayList();
	}
	
	public SymbolId makeSymbolIdConventional(int idLen) {
		SymbolId symId;

		symId = new SymbolId();
		symId.aryPtr = new int[idLen];
		symId.capacity = idLen;
		symId.length = idLen;
		return symId;
	}
	
	public CommonArrayList makeArrayListConventional() {
		traceBufferAllocCount += 3;
		return CommonUtils.makeArrayList();
	}
}
