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

public class WhitespaceHelper
	implements TokenHelper {
	
	public WhitespaceHelperData dat;
	public GeneralUtils utils;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}
	
	public Token getToken() {
		Token tok;
		TokenTabsAndSpaces t2;
		
		if(dat.contentType ==
			WhitespaceHelperData.CONTENT_TYPE_TABS_AND_SPACES) {

			t2 = null;
			if(allocHelp == null)
				t2 = makeTokenTabsAndSpaces(2);
			if(allocHelp != null)
				t2 = allocHelp.makeTokenTabsAndSpaces(2);

			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_WHITESPACE,
				Symbols.TOKEN_TABS_AND_SPACES);

			utils.copyTextIndex(t2.startIndex, dat.startIndex);
			utils.copyTextIndex(t2.pastIndex, dat.pastIndex);
			
			t2.tabCount = dat.tabCount;
			t2.spaceCount = dat.spaceCount;
			return t2;
		}
		
		if(dat.contentType ==
			WhitespaceHelperData.CONTENT_TYPE_RETURN) {
			
			tok = null;
			if(allocHelp == null)
				tok = makeToken(2);
			if(allocHelp != null)
				tok = allocHelp.makeToken(2);

			utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_WHITESPACE,
				Symbols.TOKEN_LINE_RETURN);

			utils.copyTextIndex(tok.startIndex, dat.startIndex);
			utils.copyTextIndex(tok.pastIndex, dat.pastIndex);

			return tok;
		} 

		throw new IllegalStateException();
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
		dat.reset();
	}
	
	public void advanceOneChar(CharReaderContext cc) {
		int c;
		int len;

		CommonError e1;
		
		if(dat.state == WhitespaceHelperData.STATE_PAST)
			return;
		
		TextIndex ti = cc.ti;
		
		if(cc.state == CharReaderData.STATE_HAVE_BAD_CHAR) {
			len = cc.resultCharLength;
			
			if(dat.state == WhitespaceHelperData.STATE_START) {
				// different behaivor for this helper.  Having
				// a CONTENT_TYPE_UNKNOWN state is necessary
				// so that there arent gaps in the stream,
				// between whitespace tokens and non-whitespace
				// spans
				
				utils.copyTextIndex(dat.startIndex, ti);
				dat.currentIndex = ti.index;
				
				dat.contentType = WhitespaceHelperData.CONTENT_TYPE_UNKNOWN;

				dat.resultLength += len;
				dat.currentIndex += len;
				dat.state = WhitespaceHelperData.STATE_CONTINUE;
				return;
			}
			
			if(dat.currentIndex > ti.index) {
				// seems we are reading the same thing over again
				return;
			}
			
			if(ti.index != dat.currentIndex) {
				throw new IllegalStateException();
			}
			
			// we are keeping state the same, and ignoring the
			// bad character
			
			dat.currentIndex += len;
			dat.resultLength += len;
			return;
		}
		
		if(cc.state != CharReaderData.STATE_HAVE_CHAR) {
			throw new IllegalStateException();
		}
		
		c = cc.resultChar;
		len = cc.resultCharLength;
		
		if(dat.state == WhitespaceHelperData.STATE_START) {
			dat.currentIndex = ti.index;
			utils.copyTextIndex(dat.startIndex, ti);
			
			evalFirstChar(cc);
			return;
		}
		
		if(dat.state != WhitespaceHelperData.STATE_CONTINUE) {
			throw new IllegalStateException();
		}
		
		if(dat.currentIndex > ti.index) {
			// seems we are reading the same thing over again
			return;
		}

		if(ti.index != dat.currentIndex) {
			throw new IllegalStateException();
		}
		
		// state is STATE_CONTINUE
		
		if(dat.contentType ==
			WhitespaceHelperData.CONTENT_TYPE_UNKNOWN) {
			
			evalFirstChar(cc);
			return;
		}
		
		
		if(dat.contentType ==
			WhitespaceHelperData.CONTENT_TYPE_TABS_AND_SPACES) {
			
			if(dat.spaceCount > 0) {
				// only spaces are allowed now
				
				if(c == ' ') {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}
				
				// encountered something different
				utils.copyTextIndex(dat.pastIndex, ti);				
				dat.state = WhitespaceHelperData.STATE_PAST;
				return;
			}
			
			if(c == 0x9) {
				dat.tabCount += 1;
			}
			
			if(c == ' ') {
				dat.spaceCount += 1;
			}
			
			if((c == 0x9) || (c == ' ')) {
				dat.resultLength += len;
				dat.currentIndex += len;
				return;
			}

			// encountered something different
			utils.copyTextIndex(dat.pastIndex, ti);				
			dat.state = WhitespaceHelperData.STATE_PAST;
			return;
		}

		if(dat.contentType ==
			WhitespaceHelperData.CONTENT_TYPE_RETURN) {
			
			// handle special case with 0xD then 0xA
			if(dat.prevCharWas13)
			if(c == 0xA) {
				// advance
				dat.resultLength += len;
				dat.currentIndex += len;
				dat.prevCharWas13 = false;
				return;
			}
			
			// previous stuff was a return
			utils.copyTextIndex(dat.pastIndex, ti);				
			dat.state = WhitespaceHelperData.STATE_PAST;
			return;
		}
		
		if(dat.contentType ==
			WhitespaceHelperData.CONTENT_TYPE_NON_WHITESPACE) {
			
			if((c == 0x9) || (c == ' ')
				|| (c == 0xD) || (c == 0xA)) {
				
				utils.copyTextIndex(dat.pastIndex, ti);				
				dat.state = WhitespaceHelperData.STATE_PAST;
				return;
			}
			
			// still more non whitespace
			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = "token helper in invalid state";
		throw e1;
	}

	private void evalFirstChar(CharReaderContext cc) {
		int c;
		int len;
		
		TextIndex ti = cc.ti;
		
		c = cc.resultChar;
		len = cc.resultCharLength;

		if((c == 0x9) || (c == ' ')) {
			if(c == 0x9)
				dat.tabCount += 1;

			if(c == ' ')
				dat.spaceCount += 1;
			
			dat.contentType =
				WhitespaceHelperData.CONTENT_TYPE_TABS_AND_SPACES;
			//utils.copyTextIndex(dat.startIndex, ti);
			dat.resultLength += len;
			dat.match = true;
			dat.currentIndex += len;
			dat.state = WhitespaceHelperData.STATE_CONTINUE;
			return;
		}

		if((c == 0xA) || (c == 0xD)) {
			if(c == 0xD)
				dat.prevCharWas13 = true;

			dat.contentType = WhitespaceHelperData.CONTENT_TYPE_RETURN;
			//utils.copyTextIndex(dat.startIndex, ti);
			dat.resultLength += len;
			dat.match = true;
			dat.currentIndex += len;
			dat.state = WhitespaceHelperData.STATE_CONTINUE;
			return;
		}

		dat.contentType =
			WhitespaceHelperData.CONTENT_TYPE_NON_WHITESPACE;
		//utils.copyTextIndex(dat.startIndex, ti);
		dat.resultLength += len;
		dat.currentIndex += len;
		dat.state = WhitespaceHelperData.STATE_CONTINUE;
		return;
	}
	
	public void processEndOfStream(CharReaderContext cc) {
		CommonError e1;
		
		TextIndex ti = cc.ti;
		
		if(dat.state == WhitespaceHelperData.STATE_PAST)
			return;

		if(dat.state == WhitespaceHelperData.STATE_START)
			return;
		
		if(dat.state != WhitespaceHelperData.STATE_CONTINUE) {
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}

		utils.copyTextIndex(dat.pastIndex, ti);
		dat.state = WhitespaceHelperData.STATE_PAST;
		return;
	}
	
	public Token makeToken(int idLen) {
		Token tok;
		
		tok = new Token();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN;

		tok.initAllTextIndex();
		
		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}

	public TokenTabsAndSpaces makeTokenTabsAndSpaces(int idLen) {
		TokenTabsAndSpaces tok;
		
		tok = new TokenTabsAndSpaces();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_TABS_AND_SPACES;
		
		tok.initAllTextIndex();
		
		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}
}
