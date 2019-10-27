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

public class CFamilyOctalIntTokenHelper 
	implements TokenHelper {
	
	public GeneralUtils utils;
	public CFamilyOctalIntTokenHelperData dat;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}

	public Token getToken() {
		TokenIntegerFull tok;
		
		tok = null;
		if(allocHelp == null)
			tok = makeTokenIntegerFull(2);
		if(allocHelp != null)
			tok = makeTokenIntegerFullWithHelper(2);
		
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_NUMBER,
			Symbols.TOKEN_INTEGER_FULL);
		
		utils.copyTextIndex(tok.startIndex, dat.startIndex);
		utils.copyTextIndex(tok.pastIndex, dat.pastIndex);

		tok.integer = null;
		if(allocHelp == null)
			tok.integer = makeToken(2);
		if(allocHelp != null)
			tok.integer = makeTokenWithHelper(2);
		
		utils.setSymbolIdLen2(tok.integer, Symbols.TOKEN_CATEGORY_NUMBER,
			Symbols.TOKEN_INTEGER_DATA);
		
		utils.copyTextIndex(tok.integer.startIndex, dat.integerStartIndex);
		utils.copyTextIndex(tok.integer.pastIndex, dat.integerPastIndex);
		
		tok.radix = 8;
		tok.flagUnsigned = dat.flagUnsigned;
		tok.size = 0;
		if(dat.flagLongCount == 1) tok.size = 32;
		if(dat.flagLongCount == 2) tok.size = 64;
		
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
		
		dat.state = BaseModuleData.STATE_START;
		dat.match = false;
		dat.resultLength = 0;
		
		dat.flagUnsigned = false;
		dat.flagLongCount = 0;
	}
	
	private boolean isFlagUnsigned(int c) {
		return (c == 'u' || c == 'U');
	}

	private boolean isFlagLong(int c) {
		return (c == 'l' || c == 'L');
	}
	
	public void advanceOneChar(CharReaderContext cc) {
		int c;
		int len;
		int state;
		
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
			
			state = dat.miniState;
			
			if(!dat.unsure) {

				dat.resultLength += len;
				dat.currentIndex += len;
				return;
			}
			
			if(dat.unsure) {
				// dat.resultLength += len;
				dat.unsureLength += len;
				dat.currentIndex += len;
				return;
			}

			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			e1.msg = "token helper in invalid state";
			throw e1;
		}
		
		if(cc.state != CharReaderData.STATE_HAVE_CHAR) {
			throw new IllegalStateException();
		}
		
		c = cc.resultChar;
		len = cc.resultCharLength;
		
		if(dat.state == BaseModuleData.STATE_START) {
			utils.copyTextIndex(dat.startIndex, ti);

			if(c == '0') {
				dat.miniState = CFamilyOctalIntTokenHelperData.STATE_INITIAL_ZERO;
				dat.state = TokenHelperData.STATE_CONTINUE;
				//report.resultLength += len;
				dat.unsureLength = len;
				dat.currentIndex = ti.index + len;
				dat.unsure = true;
				return;
			}
						
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(dat.state != TokenHelperData.STATE_CONTINUE) {
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			e1.msg = "int token helper is in a bad state";
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

		if(!dat.unsure) {
			// sure states

			if(state == CFamilyOctalIntTokenHelperData.STATE_INTEGER) {
				if(utils.isOctalChar(c)) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(isFlagUnsigned(c)) {
					utils.copyTextIndex(dat.integerPastIndex, ti);
					dat.miniState =
						CFamilyOctalIntTokenHelperData.STATE_FLAG_UNSIGNED;
					dat.flagUnsigned = true;
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(isFlagLong(c)) {
					utils.copyTextIndex(dat.integerPastIndex, ti);
					dat.miniState =
						CFamilyOctalIntTokenHelperData.STATE_FLAG_LONG;
					dat.flagLongCount = 1;
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				// we matched a number

				utils.copyTextIndex(dat.integerPastIndex, ti);
				utils.copyTextIndex(dat.pastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyOctalIntTokenHelperData.STATE_FLAG_UNSIGNED) {
				if(dat.flagLongCount == 0)
				if(isFlagLong(c)) {
					dat.miniState = CFamilyOctalIntTokenHelperData.STATE_FLAG_LONG;
					dat.flagLongCount = 1;
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				// we're after the u flag, and we're done
				utils.copyTextIndex(dat.pastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyOctalIntTokenHelperData.STATE_FLAG_LONG) {
				if(dat.flagLongCount < 2)
				if(isFlagLong(c)) {
					dat.flagLongCount += 1;
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(!dat.flagUnsigned)
				if(isFlagUnsigned(c)) {
					dat.miniState =
						CFamilyOctalIntTokenHelperData.STATE_FLAG_UNSIGNED;
					dat.flagUnsigned = true;
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				// we're after the L flag, and we're done
				utils.copyTextIndex(dat.pastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
		}
		
		if(dat.unsure) {
			// unsure states
			
			if(state == CFamilyOctalIntTokenHelperData.STATE_INITIAL_ZERO) {
				if(utils.isOctalChar(c)) {
					dat.miniState = CFamilyOctalIntTokenHelperData.STATE_INTEGER;
					utils.copyTextIndex(dat.integerStartIndex, ti);
					dat.match = true;
					dat.resultLength = len + dat.unsureLength;
					dat.currentIndex += len;
					dat.unsure = false;
					return;
				}

				// we've only got "0", fail

				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
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
						
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(dat.state != TokenHelperData.STATE_CONTINUE) {
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			e1.msg = "token helper in invalid state";
			throw e1;
		}

		if(dat.currentIndex != ti.index) {
			throw new IllegalStateException();
		}
		
		state = dat.miniState;
		
		if(state == CFamilyOctalIntTokenHelperData.STATE_INTEGER) {			
			// we matched a number
			
			utils.copyTextIndex(dat.integerPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
				
		if(state == CFamilyOctalIntTokenHelperData.STATE_INITIAL_ZERO) {			
			// we've only got "0", fail
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyOctalIntTokenHelperData.STATE_FLAG_UNSIGNED) {
			// we're after the u flag, and we're done
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyOctalIntTokenHelperData.STATE_FLAG_LONG) {
			// we're after the L flag, and we're done
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = "token helper in invalid state";
		throw e1;
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
	
	public TokenIntegerFull makeTokenIntegerFull(int idLen) {
		dat.traceOldAllocCount += 1;

		TokenIntegerFull tok;
	
		tok = new TokenIntegerFull();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_INTEGER_FULL;

		tok.initAllTextIndex();

		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}
		
	public Token makeTokenWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeToken(idLen);
	}

	public TokenIntegerFull makeTokenIntegerFullWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeTokenIntegerFull(idLen);
	}
}
