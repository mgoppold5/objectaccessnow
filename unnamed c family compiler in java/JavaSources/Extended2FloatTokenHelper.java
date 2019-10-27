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

public class Extended2FloatTokenHelper 
	implements TokenHelper {
	
	public GeneralUtils utils;
	public Extended2FloatTokenHelperData dat;
	private SymbolAllocHelper allocHelp;

	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}

	public Token getToken() {
		TokenFloatFull tok;
		Token t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeTokenFloatFull(2);
		if(allocHelp != null)
			tok = makeTokenFloatFullWithHelper(2);
		
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_NUMBER,
			Symbols.TOKEN_FLOAT_FULL);
		
		utils.copyTextIndex(tok.startIndex, dat.startIndex);
		utils.copyTextIndex(tok.pastIndex, dat.pastIndex);

		tok.integer = null;

		if(dat.hasInteger) {
			t2 = null;
			if(allocHelp == null)
				t2 = makeToken(2);
			if(allocHelp != null)
				t2 = makeTokenWithHelper(2);
			
			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_NUMBER,
				Symbols.TOKEN_INTEGER_DATA);
		
			utils.copyTextIndex(t2.startIndex, dat.integerStartIndex);
			utils.copyTextIndex(t2.pastIndex, dat.integerPastIndex);
			
			tok.integer = t2;
		}

		tok.fraction = null;

		if(dat.hasFraction) {
			t2 = null;
			if(allocHelp == null)
				t2 = makeToken(2);
			if(allocHelp != null)
				t2 = makeTokenWithHelper(2);

			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_NUMBER,
				Symbols.TOKEN_INTEGER_DATA);
		
			utils.copyTextIndex(t2.startIndex, dat.fractionStartIndex);
			utils.copyTextIndex(t2.pastIndex, dat.fractionPastIndex);
			
			tok.fraction = t2;
		}

		tok.exponent = null;
		
		if(dat.hasExponent) {
			t2 = null;
			if(allocHelp == null)
				t2 = makeToken(2);
			if(allocHelp != null)
				t2 = makeTokenWithHelper(2);

			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_NUMBER,
				Symbols.TOKEN_INTEGER_DATA);
		
			utils.copyTextIndex(t2.startIndex, dat.exponentStartIndex);
			utils.copyTextIndex(t2.pastIndex, dat.exponentPastIndex);
			
			tok.exponent = t2;
		}
		
		tok.radix = dat.radix;
		
		tok.flagNumberNeg = dat.flagNumberNeg;
		
		tok.flagExponentNeg = false;
		if(dat.hasExponent) tok.flagExponentNeg = dat.flagExponentNeg;
		
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

		dat.hasInteger = false;
		dat.hasFraction = false;
		dat.hasExponent = false;
		
		dat.flagNumberNeg = false;
		dat.flagExponentNeg = false;
	}
	
	private boolean isRadixMark(int c) {
		return (c == 'r');
	}	
	
	private boolean isNumberMark(int c) {
		return (c == 'n');
	}

	private boolean isExponentMark(int c) {
		return (c == 'p');
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
				// report.resultLength += len;
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
				dat.miniState =
					Extended2FloatTokenHelperData.STATE_INITIAL_ZERO;
				dat.state = TokenHelperData.STATE_CONTINUE;
				dat.unsure = true;
				
				// dat.resultLength += len;
				dat.unsureLength = len;
				dat.currentIndex = ti.index + len;
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
		
		if(!dat.unsure) {
			// sure states
			
			if(state == Extended2FloatTokenHelperData.STATE_INTEGER) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_INTEGER_UNDERSCORE;
					dat.unsure = true;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					return;
				}

				if(c == '.') {
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_RADIX_POINT;
					dat.unsure = true;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					return;
				}

				if(isExponentMark(c)) {
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_EXPONENT_MARK;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				// we failed matching a float number

				utils.copyTextIndex(dat.pastIndex, ti);
				utils.copyTextIndex(dat.integerPastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_FRACTION) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_FRACTION_UNDERSCORE;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				if(isExponentMark(c)) {
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_EXPONENT_MARK;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				// we've found a float number with a fraction

				utils.copyTextIndex(dat.pastIndex, ti);
				utils.copyTextIndex(dat.fractionPastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_EXPONENT) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					utils.copyTextIndex(dat.exponentPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_EXPONENT_UNDERSCORE;

					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				// we've found a float with an exponent

				utils.copyTextIndex(dat.exponentPastIndex, ti);
				utils.copyTextIndex(dat.pastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
		}
		
		if(dat.unsure) {
			// unsure states

			if(state == Extended2FloatTokenHelperData.STATE_INTEGER_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					dat.miniState = Extended2FloatTokenHelperData.STATE_INTEGER;
					dat.unsure = false;
					
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				// the underscores were not terminated,
				// so the result is the previous number
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_FRACTION_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					dat.miniState = Extended2FloatTokenHelperData.STATE_FRACTION;
					dat.unsure = false;
					
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				// the underscores were not terminated,
				// so the result is the previous number
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_EXPONENT_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					dat.miniState = Extended2FloatTokenHelperData.STATE_EXPONENT;
					dat.unsure = false;
					
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				// the underscores were not terminated,
				// so the result is the previous number
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_RADIX) {
				if(isNumberMark(c))
				if(dat.radixAccum >= 2 && dat.radixAccum <= 16) {
					dat.radix = dat.radixAccum;

					dat.miniState =
						Extended2FloatTokenHelperData.STATE_NUMBER_MARK;

					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				
				if(isExponentMark(c))
				if(dat.radixAccum >= 2 && dat.radixAccum <= 16) {
					dat.radix = dat.radixAccum;

					dat.miniState =
						Extended2FloatTokenHelperData.STATE_EXPONENT_MARK;
					
					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(dat.radixAccum <= 16)
				if(utils.isHexChar(c)) {
					digit = utils.getHexCharValue(c);
					
					if(digit
						< Extended2FloatTokenHelperData.RADIX_FOR_RADIX){

						dat.radixAccum = (short)
							(dat.radixAccum
								* Extended2FloatTokenHelperData.RADIX_FOR_RADIX
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

			if(state == Extended2FloatTokenHelperData.STATE_RADIX_MARK) {
				if(utils.isHexChar(c)) {
					digit = utils.getHexCharValue(c);
					
					if(digit
						< Extended2FloatTokenHelperData.RADIX_FOR_RADIX) {

						dat.radixAccum = digit;

						dat.miniState =
							Extended2FloatTokenHelperData.STATE_RADIX;

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
			
			if(state == Extended2FloatTokenHelperData.STATE_NUMBER_MARK) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.integerStartIndex, ti);
					dat.miniState = Extended2FloatTokenHelperData.STATE_INTEGER;
					dat.hasInteger = true;
					dat.unsure = false;

					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				if(c == '+') {
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_NUMBER_SIGN;

					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '-') {
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_NUMBER_SIGN;
					dat.flagNumberNeg = true;
					
					//dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				// we failed

				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_NUMBER_SIGN) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.integerStartIndex, ti);
					dat.miniState = Extended2FloatTokenHelperData.STATE_INTEGER;
					dat.hasInteger = true;
					dat.unsure = false;

					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				// we failed
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			if(state == Extended2FloatTokenHelperData.STATE_RADIX_POINT) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.fractionStartIndex, ti);
					dat.miniState = Extended2FloatTokenHelperData.STATE_FRACTION;
					dat.hasFraction = true;
					dat.match = true;
					dat.unsure = false;

					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				// we've failed to get a float number
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_EXPONENT_MARK) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.exponentStartIndex, ti);
					dat.miniState = Extended2FloatTokenHelperData.STATE_EXPONENT;
					dat.hasExponent = true;
					dat.match = true;
					dat.unsure = false;

					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				if(c == '+') {
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_EXPONENT_SIGN;
					
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '-') {
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_EXPONENT_SIGN;
					dat.flagExponentNeg = true;
					
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				// return previous result
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == Extended2FloatTokenHelperData.STATE_EXPONENT_SIGN) {
				if(utils.isHexChar(c))
				if(utils.getHexCharValue(c) < dat.radix) {
					utils.copyTextIndex(dat.exponentStartIndex, ti);
					dat.miniState = Extended2FloatTokenHelperData.STATE_EXPONENT;
					dat.hasExponent = true;
					dat.match = true;
					dat.unsure = false;

					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					return;
				}

				// return previous result
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			if(state == Extended2FloatTokenHelperData.STATE_INITIAL_ZERO) {
				if(isRadixMark(c)) {
					dat.miniState =
						Extended2FloatTokenHelperData.STATE_RADIX_MARK;
					
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				// we failed
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
			throw e1;
		}
		
		if(dat.currentIndex != ti.index) {
			throw new IllegalStateException();
		}
		
		state = dat.miniState;
		
		if(state == Extended2FloatTokenHelperData.STATE_INTEGER) {
			// we failed matching a float number
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2FloatTokenHelperData.STATE_FRACTION) {
			// we've found a float number with a fraction
			
			utils.copyTextIndex(dat.pastIndex, ti);
			utils.copyTextIndex(dat.fractionPastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == Extended2FloatTokenHelperData.STATE_EXPONENT) {
			// we've found a float with an exponent
			
			utils.copyTextIndex(dat.exponentPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == Extended2FloatTokenHelperData.STATE_INITIAL_ZERO) {
			// failed
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2FloatTokenHelperData.STATE_RADIX_MARK) {
			// failed
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2FloatTokenHelperData.STATE_NUMBER_MARK) {
			// failed
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2FloatTokenHelperData.STATE_NUMBER_SIGN) {
			// failed
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == Extended2FloatTokenHelperData.STATE_RADIX_POINT) {
			// we've failed to get a float number
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
				
		if(state == Extended2FloatTokenHelperData.STATE_EXPONENT_MARK) {
			// return previous result
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == Extended2FloatTokenHelperData.STATE_EXPONENT_SIGN) {
			// return previous result
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
	
	public TokenFloatFull makeTokenFloatFull(int idLen) {
		dat.traceOldAllocCount += 1;

		TokenFloatFull tok;
	
		tok = new TokenFloatFull();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN_FLOAT_FULL;
		
		tok.initAllTextIndex();

		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}

	public Token makeTokenWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeToken(idLen);
	}

	public TokenFloatFull makeTokenFloatFullWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeTokenFloatFull(idLen);
	}
}
