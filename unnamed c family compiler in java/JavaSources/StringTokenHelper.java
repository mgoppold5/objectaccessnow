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

public class StringTokenHelper
	implements TokenHelper {
	
	public GeneralUtils utils;
	public StringTokenHelperData dat;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}

	public Token getToken() {
		TokenString tokStr;
		
		if(dat.elementList == null)
			throw new IllegalStateException();
		
		tokStr = null;
		if(allocHelp == null)
			tokStr = makeTokenString(2);
		if(allocHelp != null)
			tokStr = makeTokenStringWithHelper(2);
		
		utils.setSymbolIdLen2(tokStr, dat.tokenCategoryId, dat.tokenId);		
		
		tokStr.elements = null;
		if(allocHelp == null)
			tokStr.elements = makeArrayList();
		if(allocHelp != null)
			tokStr.elements = makeArrayListWithHelper();
		
		
		int i;
		int count;
		int id;

		Token srcTok;
		TokenContainer srcTok2;
		Token srcTok1;

		TokenContainer outTok2;
		Token outTok1;
		
		count = dat.elementList.size();
		i = 0;
		while(i < count) {
			srcTok = (Token) dat.elementList.get(i);
			id = utils.getSymbolIdPrimary(srcTok);
			
			srcTok2 = null;
			switch(id) {
			case Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY:
			case Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC:
			case Symbols.TOKEN_STRING_ESCAPE_IGNORED:
			case Symbols.TOKEN_STRING_ESCAPE_NAME:
			case Symbols.TOKEN_STRING_ESCAPE_INTEGER:
			case Symbols.TOKEN_STRING_ESCAPE_VERBATIM:
				srcTok2 = (TokenContainer) srcTok;
			}
			
			if(srcTok2 != null) {
				if(utils.getTokenChildCount(srcTok2) != 1) {
					throw new IllegalStateException();
				}
				
				outTok2 = null;
				if(allocHelp == null)
					outTok2 = makeTokenContainer(utils.getIdLenFromSymbol(srcTok2), utils.getTokenChildCount(srcTok2));
				if(allocHelp != null)
					outTok2 = makeTokenContainerWithHelper(utils.getIdLenFromSymbol(srcTok2), utils.getTokenChildCount(srcTok2));
				
				utils.copySymbolIdToSymbolFromSymbol(outTok2, srcTok2);
				utils.copyTextIndex(outTok2.startIndex, srcTok2.startIndex);
				utils.copyTextIndex(outTok2.pastIndex, srcTok2.pastIndex);
				
				srcTok1 = (Token) srcTok2.tok[0];
				
				outTok1 = null;
				if(allocHelp == null)
					outTok1 = makeToken(utils.getIdLenFromSymbol(srcTok1));
				if(allocHelp != null)
					outTok1 = makeTokenWithHelper(utils.getIdLenFromSymbol(srcTok1));

				utils.copySymbolIdToSymbolFromSymbol(outTok1, srcTok1);
				utils.copyTextIndex(outTok1.startIndex, srcTok1.startIndex);
				utils.copyTextIndex(outTok1.pastIndex, srcTok1.pastIndex);
				
				// commit
				outTok2.tok[0] = outTok1;
				tokStr.elements.add(outTok2);
				
				i += 1;
				continue;
			}

			srcTok1 = null;
			switch(id) {
			case Symbols.TOKEN_STRING_SPAN:
			case Symbols.TOKEN_BAD_SPAN:
				srcTok1 = srcTok;
			}

			if(srcTok1 != null) {
				outTok1 = null;
				if(allocHelp == null)
					outTok1 = makeToken(utils.getIdLenFromSymbol(srcTok1));
				if(allocHelp != null)
					outTok1 = makeTokenWithHelper(utils.getIdLenFromSymbol(srcTok1));

				utils.copySymbolIdToSymbolFromSymbol(outTok1, srcTok1);
				utils.copyTextIndex(outTok1.startIndex, srcTok1.startIndex);
				utils.copyTextIndex(outTok1.pastIndex, srcTok1.pastIndex);

				// commit
				tokStr.elements.add(outTok1);
				
				i += 1;
				continue;
			}
			
			throw new IllegalStateException();
		}

		utils.copyTextIndex(tokStr.startIndex, dat.startIndex);
		utils.copyTextIndex(tokStr.pastIndex, dat.pastIndex);
		
		return tokStr;
	}
	
	public BaseModuleData getData() {return dat;}
	public CommonArrayList getChildModules() {return null;}
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.probBag.reset();

		dat.match = false;
		dat.resultLength = 0;
		dat.elementList = null;
		
		dat.state = BaseModuleData.STATE_START;
	}
		
	public void advanceOneChar(CharReaderContext cc) {
		int state;
		int c;
		int len;
		int id;
		boolean verbatim;
		
		CommonError e1;
				
		if(dat.state == TokenHelperData.STATE_DONE)
			return;
		
		TextIndex ti = cc.ti;
		
		if(cc.state == CharReaderData.STATE_HAVE_BAD_CHAR) {
			len = cc.resultCharLength;
			
			// ignore bad char like it wasnt there,
			// and maintain state
			
			if(dat.state == BaseModuleData.STATE_START)
				return;
			
			if(dat.state != TokenHelperData.STATE_CONTINUE) {
				e1 = new CommonError();
				e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
				throw e1;
			}
			
			if(dat.currentIndex > ti.index) {
				// do nothing, it seems the character is being advanced
				// over again
				return;
			}

			if(dat.currentIndex != ti.index) {
				throw new IllegalStateException();
			}
			
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}
		
		if(cc.state != CharReaderData.STATE_HAVE_CHAR) {
			throw new IllegalStateException();
		}
		
		c = cc.resultChar;
		len = cc.resultCharLength;
		
		if(dat.state == BaseModuleData.STATE_START) {
			utils.copyTextIndex(dat.startIndex, ti);
			
			if(dat.enableInitialLetterL)
			if(c == 'L') {
				dat.currentIndex = ti.index + len;
				dat.resultLength += len;
				dat.miniState =
					StringTokenHelperData.STATE_INITIAL_LETTER_L;
				dat.state = TokenHelperData.STATE_CONTINUE;
				return;
			}
			
			if(c == dat.quoteBeginChar) {
				initList();

				dat.resultLength += len;
				dat.currentIndex = ti.index + len;
				dat.miniState = StringTokenHelperData.STATE_QUOTE;
				dat.state = TokenHelperData.STATE_CONTINUE;
				dat.match = true;
				//dat.haveSpanGoing = false;
				return;
			}
			
			// we failed
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(dat.state != TokenHelperData.STATE_CONTINUE) {
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}
		
		if(dat.currentIndex > ti.index) {
			// do nothing, it seems the character is being advanced
			// over again
			return;
		}

		if(dat.currentIndex != ti.index) {
			throw new IllegalStateException();
		}
		
		state = dat.miniState;
		
		if(state == StringTokenHelperData.STATE_SPAN) {
			advanceSpan(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE) {
			utils.copyTextIndex(dat.badSpanPastIndex, ti);
			utils.copyTextIndex(dat.spanStartIndex, ti);
			
			if(c == 'x') {
				dat.radix = 16;
				dat.miniState = StringTokenHelperData.STATE_ESCAPE_LETTER_X;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(utils.isOctalChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				
				dat.radix = 8;
				dat.miniState = StringTokenHelperData.STATE_ESCAPE_INTEGER;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}

			id = Symbols.SYMBOL_UNSPECIFIED;
			switch(c) {
			case 'a':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_ALERT;
				break;
			case 'b':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_BACKSPACE;
				break;
			case 'f':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_FORM_FEED;
				break;
			case 'n':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_NEW_LINE;
				break;
			case 'r':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_CARRIAGE_RETURN;
				break;
			case 't':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_TAB;
				break;
			case 'v':
				id = Symbols.TOKEN_STRING_ESCAPE_NAME_VERTICAL_TAB;
				break;
			}
			
			if(id != Symbols.SYMBOL_UNSPECIFIED) {
				utils.copyTextIndex(dat.nameStartIndex, ti);

				dat.escapeTokenId = id;
				dat.miniState = StringTokenHelperData.STATE_ESCAPE_NAME;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			verbatim = false;
			
			switch(c) {
			case '\'':
			case '\"':
			case '?':
			case '\\':
				verbatim = true;
			}
			
			if(c == dat.quoteBeginChar || c == dat.quoteEndChar)
				verbatim = true;
			
			if(verbatim) {
				utils.copyTextIndex(dat.verbatimStartIndex, ti);
				
				dat.miniState = StringTokenHelperData.STATE_ESCAPE_VERBATIM;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(dat.enableCEscapeIgnoredReturn)
			if(c == 0xA || c == 0xD) {
				dat.prevCharWas13 = false;
				if(c == 0xD)
					dat.prevCharWas13 = true;
				
				utils.copyTextIndex(dat.returnStartIndex, ti);
				
				dat.miniState =
					StringTokenHelperData.STATE_ESCAPE_IGNORED_RETURN;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			//badEscape(dat.escapeStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_INTEGER) {
			if(dat.radix == 16 && utils.isHexChar(c)) {
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(dat.radix == 8 && utils.isOctalChar(c)) {
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
						
			utils.copyTextIndex(dat.integerPastIndex, ti);
			utils.copyTextIndex(dat.escapePastIndex, ti);
			addEscapeInteger();
			
			// resume
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_VERBATIM) {
			utils.copyTextIndex(dat.verbatimPastIndex, ti);
			utils.copyTextIndex(dat.escapePastIndex, ti);
			addEscapeVerbatim();
			
			// resume
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_NAME) {
			utils.copyTextIndex(dat.namePastIndex, ti);
			utils.copyTextIndex(dat.escapePastIndex, ti);
			addEscapeName();
			
			// resume
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_IGNORED_RETURN) {
			if(dat.prevCharWas13)
			if(c == 0xA) {
				dat.prevCharWas13 = false;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			utils.copyTextIndex(dat.returnPastIndex, ti);
			utils.copyTextIndex(dat.escapePastIndex, ti);
			addEscapeIgnoredReturn();
			
			// resume
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_LETTER_X) {
			if(utils.isHexChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				
				dat.miniState = StringTokenHelperData.STATE_ESCAPE_INTEGER;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			// bad escape
			
			//badEscape(dat.escapeStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			dat.miniState = StringTokenHelperData.STATE_SPAN;
			advanceSpan(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE_INTEGER) {
			if(c == ';') {
				utils.copyTextIndex(dat.integerPastIndex, ti);
				
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_INTEGER_END;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(dat.radix == 10 && utils.isDecimalChar(c)) {
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}

			if(dat.radix == 16 && utils.isHexChar(c)) {
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}

			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			dat.miniState = StringTokenHelperData.STATE_SPAN;
			advanceSpan(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE_NAME) {
			if(c == ';') {
				utils.copyTextIndex(dat.namePastIndex, ti);
				
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_NAME_END;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(utils.isXmlNameChar(c, true)) {
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}

			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			dat.miniState = StringTokenHelperData.STATE_SPAN;
			advanceSpan(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE) {
			utils.copyTextIndex(dat.badSpanPastIndex, ti);
			utils.copyTextIndex(dat.spanStartIndex, ti);
			
			if(c == '#') {
				dat.radix = 10;
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_NUMBER;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(utils.isXmlNameStartChar(c, true)) {
				utils.copyTextIndex(dat.nameStartIndex, ti);
				
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_NAME;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			// bad reference
			
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			startNewThing(cc);
			return;
		}
				
		if(state == StringTokenHelperData.STATE_REFERENCE_NUMBER) {
			if(c == 'x') {
				dat.radix = 16;
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_LETTER_X;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			if(dat.radix == 10 && utils.isDecimalChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_INTEGER;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}

			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			dat.miniState = StringTokenHelperData.STATE_SPAN;
			advanceSpan(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE_LETTER_X) {
			if(dat.radix == 16 && utils.isHexChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				
				dat.miniState =
					StringTokenHelperData.STATE_REFERENCE_INTEGER;
				dat.currentIndex += len;
				dat.resultLength += len;
				return;
			}
			
			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			dat.miniState = StringTokenHelperData.STATE_SPAN;
			advanceSpan(cc);
			return;
		}

		if(state == StringTokenHelperData.STATE_REFERENCE_INTEGER_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			addReferenceInteger();
			
			// resume
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE_NAME_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			addReferenceName();
			
			// resume
			startNewThing(cc);
			return;
		}

		if(state == StringTokenHelperData.STATE_QUOTE) {
			startNewThing(cc);
			return;
		}
		
		if(state == StringTokenHelperData.STATE_QUOTE_END) {
			utils.copyTextIndex(dat.pastIndex, ti);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_INITIAL_LETTER_L) {
			if(c == dat.quoteBeginChar) {
				initList();
				
				dat.miniState = StringTokenHelperData.STATE_QUOTE;
				dat.match = true;
				dat.currentIndex += len;
				dat.resultLength += len;
				//dat.haveSpanGoing = false;
				return;
			}

			// we failed
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = "token helper in invalid state";
		throw e1;
	}
	
	private void advanceSpan(CharReaderContext cc) {
		int c;
		int len;
		
		TextIndex ti = cc.ti;
		
		c = cc.resultChar;
		len = cc.resultCharLength;

		if(dat.enableCEscapes)
		if(c == '\\') {
			// finish span
			utils.copyTextIndex(dat.spanPastIndex, ti);
			addSpan();

			utils.copyTextIndex(dat.escapeStartIndex, ti);
			utils.copyTextIndex(dat.badSpanStartIndex, ti);

			dat.miniState = StringTokenHelperData.STATE_ESCAPE;
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}

		if(dat.enableXmlReferences)
		if(c == '&') {
			// finish span
			utils.copyTextIndex(dat.spanPastIndex, ti);
			addSpan();

			utils.copyTextIndex(dat.refStartIndex, ti);
			utils.copyTextIndex(dat.badSpanStartIndex, ti);

			dat.miniState = StringTokenHelperData.STATE_REFERENCE;
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}

		if(c == dat.quoteEndChar) {
			utils.copyTextIndex(dat.spanPastIndex, ti);
			addSpan();
			//dat.haveSpanGoing = false;

			dat.miniState = StringTokenHelperData.STATE_QUOTE_END;
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}

		if(c == 0xA || c == 0xD) {
			// unterminated string

			utils.copyTextIndex(dat.spanPastIndex, ti);
			addSpan();
			//dat.haveSpanGoing = false;

			unterminatedString(ti);

			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		// extend span
		dat.currentIndex += len;
		dat.resultLength += len;
		return;
	}
	
	private void startNewThing(CharReaderContext cc) {
		int c;
		int len;

		CommonError e1;
		LangError e2;
		TextIndex context;
		
		TextIndex ti = cc.ti;
		
		c = cc.resultChar;
		len = cc.resultCharLength;
		
		if(c == dat.quoteEndChar) {
			dat.miniState = StringTokenHelperData.STATE_QUOTE_END;
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}

		if(dat.enableCEscapes)
		if(c == '\\') {
			utils.copyTextIndex(dat.escapeStartIndex, ti);
			utils.copyTextIndex(dat.badSpanStartIndex, ti);

			dat.miniState = StringTokenHelperData.STATE_ESCAPE;
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}

		if(dat.enableXmlReferences)
		if(c == '&') {
			utils.copyTextIndex(dat.refStartIndex, ti);
			utils.copyTextIndex(dat.badSpanStartIndex, ti);

			dat.miniState = StringTokenHelperData.STATE_REFERENCE;
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}

		if(c == 0xA || c == 0xD) {
			// unterminated string

			unterminatedString(ti);

			utils.copyTextIndex(dat.pastIndex, ti);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		// start a span
		
		utils.copyTextIndex(dat.spanStartIndex, ti);

		dat.miniState = StringTokenHelperData.STATE_SPAN;
		dat.currentIndex += len;
		dat.resultLength += len;
		//dat.haveSpanGoing = true;
		return;
	}
	
	public void processEndOfStream(CharReaderContext cc) {
		int state;
		
		CommonError e1;
		LangError e2;
		TextIndex context;
		
		if(dat.state == TokenHelperData.STATE_DONE)
			return;
		
		TextIndex ti = cc.ti;

		if(dat.state == BaseModuleData.STATE_START) {
			utils.copyTextIndex(dat.startIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);

			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(dat.state != TokenHelperData.STATE_CONTINUE) {
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}
		
		if(dat.currentIndex != ti.index) {
			throw new IllegalStateException();
		}
		
		state = dat.miniState;
		
		if(state == StringTokenHelperData.STATE_QUOTE_END) {
			utils.copyTextIndex(dat.pastIndex, ti);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_QUOTE) {
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_SPAN) {
			utils.copyTextIndex(dat.spanPastIndex, ti);
			addSpan();
			//dat.haveSpanGoing = false;
			
			unterminatedString(ti);

			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE) {
			//badEscape(dat.escapeStartIndex);
			utils.copyTextIndex(dat.badSpanPastIndex, ti);
			
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			
			//utils.copyTextIndex(dat.escapePastIndex, ti);
			//addBadEscape();
				
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_NAME) {
			utils.copyTextIndex(dat.escapePastIndex, ti);
			utils.copyTextIndex(dat.namePastIndex, ti);
			addEscapeName();
			
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
				
		if(state == StringTokenHelperData.STATE_ESCAPE_VERBATIM) {
			utils.copyTextIndex(dat.escapePastIndex, ti);
			utils.copyTextIndex(dat.verbatimPastIndex, ti);
			addEscapeVerbatim();
			
			unterminatedString(ti);

			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_IGNORED_RETURN) {
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_LETTER_X) {
			utils.copyTextIndex(dat.spanPastIndex, ti);
			
			//badEscape(dat.escapeStartIndex);
			
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			addSpan();
			
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_ESCAPE_INTEGER) {
			utils.copyTextIndex(dat.escapePastIndex, ti);
			utils.copyTextIndex(dat.integerPastIndex, ti);
			addEscapeInteger();
			
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE
			|| state == StringTokenHelperData.STATE_REFERENCE_NAME
			|| state == StringTokenHelperData.STATE_REFERENCE_NUMBER
			|| state == StringTokenHelperData.STATE_REFERENCE_LETTER_X
			|| state == StringTokenHelperData.STATE_REFERENCE_INTEGER) {
			
			utils.copyTextIndex(dat.spanPastIndex, ti);
			
			//badReference(dat.refStartIndex);
			
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			addBadSpanToken();
			addSpan();
			
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE_INTEGER_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			addReferenceInteger();
			
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == StringTokenHelperData.STATE_REFERENCE_NAME_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			addReferenceName();
			
			unterminatedString(ti);
			
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
				
		if(state == StringTokenHelperData.STATE_INITIAL_LETTER_L) {
			// failed
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = "token helper in invalid state";
		throw e1;
	}	

	private void initList() {
		dat.elementList = null;
		if(allocHelp == null)
			dat.elementList = makeArrayList();
		if(allocHelp != null)
			dat.elementList = makeArrayListWithHelper();
	}
	
	private void addSpan() {
		Token tok;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);
		
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING, 
			Symbols.TOKEN_STRING_SPAN);

		utils.copyTextIndex(tok.startIndex, dat.spanStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.spanPastIndex);
		
		dat.elementList.add(tok);
	}
	
	private void addBadSpanToken() {
		Token tok;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);

		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_BASIC, 
			Symbols.TOKEN_BAD_SPAN);
		
		utils.copyTextIndex(tok.startIndex, dat.badSpanStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.badSpanPastIndex);
		
		dat.elementList.add(tok);
	}
	
	private void addEscapeVerbatim() {
		Token tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);

		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING, 
			Symbols.TOKEN_STRING_SPAN);
		
		utils.copyTextIndex(tok.startIndex, dat.verbatimStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.verbatimPastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);
		//t2.tok = new Token[1];
		
		t2.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_STRING, 
			Symbols.TOKEN_STRING_ESCAPE_VERBATIM);
		
		utils.copyTextIndex(t2.startIndex, dat.escapeStartIndex);
		utils.copyTextIndex(t2.pastIndex, dat.escapePastIndex);
		
		t2.tok[0] = tok;
		
		dat.elementList.add(t2);
	}
	
	private void addEscapeInteger() {
		TokenIntegerSimple tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeTokenIntegerSimple(2);
		if(allocHelp != null)
			tok = makeTokenIntegerSimpleWithHelper(2);
		
		tok.radix = dat.radix;
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_NUMBER,
			Symbols.TOKEN_INTEGER_SIMPLE);
		
		utils.copyTextIndex(tok.startIndex, dat.integerStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.integerPastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);
		//t2.tok = new Token[1];
		
		t2.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_STRING,
			Symbols.TOKEN_STRING_ESCAPE_INTEGER);
		
		utils.copyTextIndex(t2.startIndex, dat.escapeStartIndex);
		utils.copyTextIndex(t2.pastIndex, dat.escapePastIndex);
		
		t2.tok[0] = tok;
		
		dat.elementList.add(t2);
	}
	
	private void addEscapeName() {
		Token tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);

		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING,
			dat.escapeTokenId);
		
		utils.copyTextIndex(tok.startIndex, dat.nameStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.namePastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);
		//t2.tok = new Token[1];
		
		t2.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_STRING,
			Symbols.TOKEN_STRING_ESCAPE_NAME);
		
		utils.copyTextIndex(t2.startIndex, dat.escapeStartIndex);
		utils.copyTextIndex(t2.pastIndex, dat.escapePastIndex);
		
		t2.tok[0] = tok;
		
		dat.elementList.add(t2);
	}
	
	private void addEscapeIgnoredReturn() {
		Token tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);

		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING,
			Symbols.TOKEN_STRING_ESCAPE_IGNORED_RETURN);
		
		utils.copyTextIndex(tok.startIndex, dat.returnStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.returnPastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);
		//t2.tok = new Token[1];
		
		t2.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_STRING,
			Symbols.TOKEN_STRING_ESCAPE_IGNORED);
		
		utils.copyTextIndex(t2.startIndex, dat.escapeStartIndex);
		utils.copyTextIndex(t2.pastIndex, dat.escapePastIndex);
		
		t2.tok[0] = tok;
		
		dat.elementList.add(t2);
	}
	
	/*
	private void addBadEscape() {
		Token tok;
		
		tok = makeToken();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING, 
			Symbols.TOKEN_STRING_ESCAPE_BAD);
		
		utils.copyTextIndex(tok.startIndex, dat.escapeStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.escapePastIndex);
		
		dat.strTok.elements.add(tok);
	}
	*/
	
	private void addReferenceInteger() {
		TokenIntegerSimple tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeTokenIntegerSimple(2);
		if(allocHelp != null)
			tok = makeTokenIntegerSimpleWithHelper(2);
		
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_NUMBER, 
			Symbols.TOKEN_INTEGER_SIMPLE);
		tok.radix = dat.radix;
		
		utils.copyTextIndex(tok.startIndex, dat.integerStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.integerPastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);
		//t2.tok = new Token[1];
		
		t2.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_XML,
			Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC);
		
		utils.copyTextIndex(t2.startIndex, dat.refStartIndex);
		utils.copyTextIndex(t2.pastIndex, dat.refPastIndex);
		
		t2.tok[0] = tok;
		
		dat.elementList.add(t2);
	}

	private void addReferenceName() {
		Token tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);

		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_XML,
			Symbols.TOKEN_XML_NAME);
		
		utils.copyTextIndex(tok.startIndex, dat.nameStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.namePastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);
		//t2.tok = new Token[1];
		
		t2.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_XML,
			Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY);
		
		utils.copyTextIndex(t2.startIndex, dat.refStartIndex);
		utils.copyTextIndex(t2.pastIndex, dat.refPastIndex);
		
		t2.tok[0] = tok;
		
		dat.elementList.add(t2);
	}
	
	/*
	private void addBadReference() {
		Token tok;
		
		tok = makeToken();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_XML,
			Symbols.TOKEN_XML_REFERENCE_BAD);
		
		utils.copyTextIndex(tok.startIndex, dat.refStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.refPastIndex);
		
		dat.strTok.elements.add(tok);
	}
	*/
	
	private void unterminatedString(TextIndex ti) {
		LangError e2;
		TextIndex context;

		context = new TextIndex();
		utils.copyTextIndex(context, ti);

		e2 = new LangError();
		e2.id = LangErrors.ERROR_STRING_UNTERMINATED;
		e2.context = context;

		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
	}	

	private void addBadSpanError(TextIndex startIndex, TextIndex pastIndex) {
		LangError e2;
		TextRange context;

		context = makeTextRange();
		utils.copyTextIndex(context.startIndex, startIndex);
		utils.copyTextIndex(context.pastIndex, pastIndex);

		e2 = new LangError();
		e2.id = LangErrors.ERROR_BAD_SPAN;
		e2.context = context;

		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
	}
	
	private TextRange makeTextRange() {
		TextRange tr;
		
		tr = new TextRange();
		tr.init();
		return tr;
	}
	
	public Token makeToken(int idLen) {
		dat.traceOldAllocCount += 1;
		
		Token tok;
		
		tok = new Token();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN;
		
		tok.initAllTextIndex();
		
		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}

	public TokenContainer makeTokenContainer(int idLen, int childCount) {
		dat.traceOldAllocCount += 1;

		TokenContainer tok;
		
		tok = new TokenContainer();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_CONTAINER;

		tok.initAllTextIndex();
		
		utils.allocNewSymbolId(tok, idLen);
		utils.allocNewTokenArrayForTokenContainer(tok, childCount);
		return tok;
	}

	public TokenIntegerSimple makeTokenIntegerSimple(int idLen) {
		dat.traceOldAllocCount += 1;

		TokenIntegerSimple tok;
	
		tok = new TokenIntegerSimple();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_INTEGER_SIMPLE;

		tok.initAllTextIndex();

		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}

	public TokenString makeTokenString(int idLen) {
		dat.traceOldAllocCount += 1;

		TokenString tok;

		tok = new TokenString();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_STRING;

		tok.initAllTextIndex();

		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}

	public CommonArrayList makeArrayList() {
		dat.traceOldAllocCount += 1;

		return CommonUtils.makeArrayList();
	}

	public Token makeTokenWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeToken(idLen);
	}

	public TokenContainer makeTokenContainerWithHelper(
		int idLen, int childCount) {

		dat.traceNewAllocCount += 1;
		
		return allocHelp.makeTokenContainer(idLen, childCount);
	}
	
	public TokenIntegerSimple makeTokenIntegerSimpleWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeTokenIntegerSimple(idLen);
	}

	public TokenString makeTokenStringWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeTokenString(idLen);
	}

	public CommonArrayList makeArrayListWithHelper() {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeArrayList();
	}
}
