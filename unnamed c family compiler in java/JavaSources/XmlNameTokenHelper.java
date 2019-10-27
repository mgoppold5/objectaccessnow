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

public class XmlNameTokenHelper
	implements TokenHelper {
	
	public GeneralUtils utils;
	public XmlNameTokenHelperData dat;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}

	public Token getToken() {
		Token tok;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);

		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_XML, 
			Symbols.TOKEN_XML_NAME);
		
		utils.copyTextIndex(tok.startIndex, dat.startIndex);
		utils.copyTextIndex(tok.pastIndex, dat.pastIndex);
		
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
		
		dat.match = false;
		dat.resultLength = 0;
		
		dat.state = BaseModuleData.STATE_START;
	}
		
	public void advanceOneChar(CharReaderContext cc) {
		int c;
		int len;
		
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
			
			if(utils.isXmlNameStartChar(c, true)) {
				dat.resultLength += len;
				dat.currentIndex = ti.index + len;
				dat.state = TokenHelperData.STATE_CONTINUE;
				dat.match = true;
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
		
		if(utils.isXmlNameChar(c, true)) {
			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}
		
		utils.copyTextIndex(dat.pastIndex, ti);
		dat.state = TokenHelperData.STATE_DONE;
		return;
	}

	public void processEndOfStream(CharReaderContext cc) {
		CommonError e1;
		
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

		utils.copyTextIndex(dat.pastIndex, ti);
		dat.state = TokenHelperData.STATE_DONE;
		return;
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

	public Token makeTokenWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeToken(idLen);
	}
}
