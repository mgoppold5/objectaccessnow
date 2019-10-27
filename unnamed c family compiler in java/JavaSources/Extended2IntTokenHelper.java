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

public class Extended2IntTokenHelper 
	implements TokenHelper {
	
	public GeneralUtils utils;
	public Extended2IntTokenHelperData dat;
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
		
		tok.radix = dat.radix;
		tok.flagUnsigned = false;
		tok.flagNumberNeg = dat.flagNumberNeg;
		tok.size = 0;
		
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
		dat.radix = 0;
		dat.flagNumberNeg = false;
	}

	private boolean isRadixMark(int c) {
		return (c == 'r');
	}	
	
	private boolean isNumberMark(int c) {
		return (c == 'n');
	}

	public void advanceOneChar(CharReaderContext cc) {
		int state;
		
		int c;
		int len;
		short digit;
		
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
				e1.msg = "extended int token helper in a bad state";
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
				dat.miniState = Extended2IntTokenHelperData.STATE_INITIAL_ZERO;
				dat.state = TokenHelperData.STATE_CONTINUE;
				//dat.resultLength += len;
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
			e1.msg = "extended int token helper in a bad state";
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
			
			if(state == Extended2IntTokenHelperData.STATE_INTEGER) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix){
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2IntTokenHelperData.STATE_INTEGER_UNDERSCORE;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				// we matched a number

				utils.copyTextIndex(dat.integerPastIndex, ti);
				utils.copyTextIndex(dat.pastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
		}
		
		if(dat.unsure) {
			// unsure states

			if(state == Extended2IntTokenHelperData.STATE_INTEGER_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix){
					dat.miniState = Extended2IntTokenHelperData.STATE_INTEGER;
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					dat.unsure = false;
					return;
				}

				// the underscores were not terminated,
				// so the result is the previous number
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			if(state == Extended2IntTokenHelperData.STATE_RADIX) {
				if(isNumberMark(c))
				if(dat.radixAccum >= 2 && dat.radixAccum <= 16) {
					dat.radix = dat.radixAccum;

					dat.miniState =
						Extended2IntTokenHelperData.STATE_NUMBER_MARK;
					
					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}
				
				if(dat.radixAccum <= 16)
				if(utils.isHexChar(c)) {
					digit = utils.getHexCharValue(c);
					
					if(digit
						< Extended2IntTokenHelperData.RADIX_FOR_RADIX) {

						dat.radixAccum = (short)
							(dat.radixAccum
								* Extended2IntTokenHelperData.RADIX_FOR_RADIX
								+ digit);

						//dat.resultLength += len;
						dat.unsureLength += len;
						dat.currentIndex += len;
						return;
					}
				}
				
				// we've failed

				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			if(state == Extended2IntTokenHelperData.STATE_RADIX_MARK) {
				if(utils.isHexChar(c)) {
					digit = utils.getHexCharValue(c);

					if(digit
						< Extended2IntTokenHelperData.RADIX_FOR_RADIX) {

						dat.radixAccum = digit;

						dat.miniState =
							Extended2IntTokenHelperData.STATE_RADIX;

						//dat.resultLength += len;
						dat.unsureLength += len;
						dat.currentIndex += len;
						return;
					}
				}

				// we've failed

				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2IntTokenHelperData.STATE_NUMBER_MARK) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.integerStartIndex, ti);
					dat.miniState = Extended2IntTokenHelperData.STATE_INTEGER;
					dat.match = true;
					dat.unsure = false;
					
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}
				
				if(c == '+') {
					dat.miniState =
						Extended2IntTokenHelperData.STATE_NUMBER_SIGN;

					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '-') {
					dat.miniState =
						Extended2IntTokenHelperData.STATE_NUMBER_SIGN;
					dat.flagNumberNeg = true;
					
					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}
				
				// failed
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2IntTokenHelperData.STATE_NUMBER_SIGN) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.integerStartIndex, ti);
					dat.miniState = Extended2IntTokenHelperData.STATE_INTEGER;
					dat.match = true;
					dat.unsure = false;
					
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}
				
				// failed
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2IntTokenHelperData.STATE_INITIAL_ZERO) {
				if(isRadixMark(c)) {
					dat.miniState =
						Extended2IntTokenHelperData.STATE_RADIX_MARK;
					
					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				// we've failed

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
			e1.msg = "extended int token helper is in a bad state";
			throw e1;
		}
		
		if(dat.currentIndex != ti.index) {
			throw new IllegalStateException();
		}
		
		state = dat.miniState;
		
		if(state == Extended2IntTokenHelperData.STATE_INTEGER) {
			// we matched a number
			
			utils.copyTextIndex(dat.integerPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2IntTokenHelperData.STATE_INTEGER_UNDERSCORE) {
			// the underscores were not terminated,
			// so the result is the previous number
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2IntTokenHelperData.STATE_RADIX) {
			// no match

			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2IntTokenHelperData.STATE_NUMBER_MARK) {
			// no match
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2IntTokenHelperData.STATE_NUMBER_SIGN) {
			// no match
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == Extended2IntTokenHelperData.STATE_INITIAL_ZERO) {
			// no match
			
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
