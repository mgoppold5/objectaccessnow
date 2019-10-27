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

public class XmlReferenceTokenHelper
	implements TokenHelper {
	
	public XmlReferenceTokenHelperData dat;
	public GeneralUtils utils;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}
	
	public Token getToken() {
		Token tok;
		TokenContainer t2;
		TokenIntegerSimple t3;

		if(dat.integerValid) {
			t3 = null;
			if(allocHelp == null)
				t3 = makeTokenIntegerSimple(2);
			if(allocHelp != null)
				t3 = allocHelp.makeTokenIntegerSimple(2);
			
			utils.setSymbolIdLen2(t3, Symbols.TOKEN_CATEGORY_NUMBER,
				Symbols.TOKEN_INTEGER_SIMPLE);
			
			t3.radix = dat.radix;
			
			utils.copyTextIndex(t3.startIndex, dat.integerStartIndex);
			utils.copyTextIndex(t3.pastIndex, dat.integerPastIndex);
			
			t2 = null;
			if(allocHelp == null)
				t2 = makeTokenContainer(2, 1);
			if(allocHelp != null)
				t2 = allocHelp.makeTokenContainer(2, 1);

			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_XML,
				Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC);
			
			utils.copyTextIndex(t2.startIndex, dat.refStartIndex);
			utils.copyTextIndex(t2.pastIndex, dat.refPastIndex);
			
			t2.tok[0] = t3;
			
			return t2;
		}
		
		if(dat.nameValid) {
			tok = null;
			if(allocHelp == null)
				tok = makeToken(2);
			if(allocHelp != null)
				tok = allocHelp.makeToken(2);

			utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_XML,
				Symbols.TOKEN_XML_NAME);
			
			utils.copyTextIndex(tok.startIndex, dat.nameStartIndex);
			utils.copyTextIndex(tok.pastIndex, dat.namePastIndex);
			
			t2 = null;
			if(allocHelp == null)
				t2 = makeTokenContainer(2, 1);
			if(allocHelp != null)
				t2 = allocHelp.makeTokenContainer(2, 1);
			
			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_XML,
				Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY);
			
			utils.copyTextIndex(t2.startIndex, dat.refStartIndex);
			utils.copyTextIndex(t2.pastIndex, dat.refPastIndex);
			
			t2.tok[0] = tok;
			
			return t2;
		}
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = allocHelp.makeToken(2);
		
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_BAD_SPAN);

		utils.copyTextIndex(tok.startIndex, dat.badSpanStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.badSpanPastIndex);

		return tok;
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

		dat.unsure = false;
		dat.radix = 0;
		dat.match = false;
		dat.integerValid = false;
		dat.nameValid = false;
		dat.resultLength = 0;
		
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
			
			if(dat.unsure) {
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
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
			utils.copyTextIndex(dat.pastIndex, ti);
			utils.copyTextIndex(dat.refStartIndex, ti);
			utils.copyTextIndex(dat.refPastIndex, ti);
			utils.copyTextIndex(dat.badSpanStartIndex, ti);
			utils.copyTextIndex(dat.badSpanPastIndex, ti);
			
			if(c == '&') {
				dat.match = true;
				dat.currentIndex = ti.index + len;
				dat.resultLength += len;
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE;
				dat.state = TokenHelperData.STATE_CONTINUE;
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
				
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER) {
			if(c == ';') {
				utils.copyTextIndex(dat.integerPastIndex, ti);
				
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER_END;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}
			
			if(dat.radix == 10 && utils.isDecimalChar(c)) {
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}

			if(dat.radix == 16 && utils.isHexChar(c)) {
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}

			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_NAME) {
			if(c == ';') {
				utils.copyTextIndex(dat.namePastIndex, ti);
				
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_NAME_END;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}
			
			if(utils.isXmlNameChar(c, true)) {
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}

			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE) {
			utils.copyTextIndex(dat.badSpanPastIndex, ti);

			dat.unsure = true;
			dat.unsureLength = 0;
			
			if(c == '#') {
				dat.radix = 10;
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_NUMBER;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}
			
			if(utils.isXmlNameStartChar(c, true)) {
				utils.copyTextIndex(dat.nameStartIndex, ti);
				
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_NAME;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}
			
			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_NUMBER) {
			if(c == 'x') {
				dat.radix = 16;
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_LETTER_X;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}
			
			if(dat.radix == 10 && utils.isDecimalChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}

			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_LETTER_X) {
			if(dat.radix == 16 && utils.isHexChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				
				dat.miniState =
					XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER;
				dat.currentIndex += len;
				dat.unsureLength += len;
				return;
			}
			
			// bad reference
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			
			dat.unsure = false;
			dat.resultLength += dat.unsureLength;

			dat.integerValid = true;
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_NAME_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);

			dat.unsure = false;
			dat.resultLength += dat.unsureLength;
			
			dat.nameValid = true;
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = "token helper in invalid state";
		throw e1;
	}

	public void processEndOfStream(CharReaderContext cc) {
		int state;
		
		CommonError e1;
		
		if(dat.state == TokenHelperData.STATE_DONE)
			return;
		
		TextIndex ti = cc.ti;

		if(dat.state == BaseModuleData.STATE_START) {
			utils.copyTextIndex(dat.startIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			utils.copyTextIndex(dat.refStartIndex, ti);
			utils.copyTextIndex(dat.refPastIndex, ti);
			utils.copyTextIndex(dat.badSpanStartIndex, ti);
			utils.copyTextIndex(dat.badSpanPastIndex, ti);

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
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE
			|| state == XmlReferenceTokenHelperData.STATE_REFERENCE_NAME
			|| state == XmlReferenceTokenHelperData.STATE_REFERENCE_NUMBER
			|| state == XmlReferenceTokenHelperData.STATE_REFERENCE_LETTER_X
			|| state == XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER) {
			
			//badReference(dat.refStartIndex);
			addBadSpanError(dat.badSpanStartIndex, dat.badSpanPastIndex);
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_INTEGER_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			
			dat.unsure = false;
			dat.resultLength += dat.unsureLength;
			
			dat.integerValid = true;
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == XmlReferenceTokenHelperData.STATE_REFERENCE_NAME_END) {
			utils.copyTextIndex(dat.refPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);

			dat.unsure = false;
			dat.resultLength += dat.unsureLength;
			
			dat.nameValid = true;
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = "token helper in invalid state";
		throw e1;
	}	
	
	/*
	private void badReference(TextIndex ti) {
		LangError e2;
		TextIndex context;

		context = new TextIndex();
		utils.copyTextIndex(context, ti);

		e2 = new LangError();
		e2.id = LangErrors.ERROR_XML_REFERENCE_BAD;
		e2.context = context;

		dat.probBag.addProblem(
			ProblemLevels.PROBLEM_LANG_ERROR, e2);
	}
	*/
	
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

	public Token makeTokenWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeToken(idLen);
	}

	public TokenContainer makeTokenContainerWithHelper(
		int idLen, int childCount) {

		dat.traceNewAllocCount += 1;
		
		return allocHelp.makeTokenContainer(idLen, childCount);
	}
	
	public Token makeTokenIntegerSimpleWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeTokenIntegerSimple(idLen);
	}
}
