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

public class CFamilyFloatTokenHelper
	implements TokenHelper {
	
	public GeneralUtils utils;
	public CFamilyFloatTokenHelperData dat;
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
		
		tok.radix = 10;
		tok.flagExponentNeg = dat.flagExponentNeg;
		tok.size = dat.flagSize;
		
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

		dat.hasInteger = false;
		dat.hasFraction = false;
		dat.hasExponent = false;
		
		dat.flagSize = 0;
		dat.flagExponentNeg = false;
	}
	
	private boolean isFlagSingle(int c) {
		return (c == 'f' || c == 'F');
	}

	private boolean isFlagDouble(int c) {
		return (c == 'd' || c == 'D');
	}
	
	private boolean isFlagLong(int c) {
		return (c == 'l' || c == 'L');
	}

	private boolean isExponentMark(int c) {
		return (c == 'e' || c == 'E');
	}
	
	public void advanceOneChar(CharReaderContext cc) {
		int state;
		
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
			
			if(utils.isDecimalChar(c)) {
				utils.copyTextIndex(dat.integerStartIndex, ti);
				dat.hasInteger = true;
				dat.resultLength += len;
				dat.currentIndex = ti.index + len;
				dat.miniState = CFamilyFloatTokenHelperData.STATE_INTEGER;
				dat.state = TokenHelperData.STATE_CONTINUE;
				dat.unsure = false;
				return;
			}
			
			if(c == '.') {
				dat.miniState = CFamilyFloatTokenHelperData.STATE_RADIX_POINT;
				dat.state = TokenHelperData.STATE_CONTINUE;
				// dat.resultLength += len;
				dat.unsureLength = len;
				dat.currentIndex = ti.index + len;
				dat.unsure = true;
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
			// for sure states
		
			if(state == CFamilyFloatTokenHelperData.STATE_INTEGER) {
				if(utils.isDecimalChar(c)) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_INTEGER_UNDERSCORE;
					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					
					// set these variables in case we're at the end of
					// the token
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					return;
				}

				if(c == '.') {
					dat.miniState = CFamilyFloatTokenHelperData.STATE_RADIX_POINT;
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				if(isExponentMark(c)) {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_EXPONENT_MARK;
					utils.copyTextIndex(dat.integerPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				// we failed matching a float number

				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyFloatTokenHelperData.STATE_FRACTION) {
				if(utils.isDecimalChar(c)) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_FRACTION_UNDERSCORE;
					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;

					// set these variables in case we're at the end of
					// the token
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					return;
				}

				if(isExponentMark(c)) {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_EXPONENT_MARK;
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;
					return;
				}

				if(isFlagSingle(c)) {
					dat.flagSize = 32;
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_FLAG;
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(isFlagDouble(c)) {
					dat.flagSize = 64;
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FLAG;
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(isFlagLong(c)) {
					dat.flagSize = 128;
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FLAG;
					utils.copyTextIndex(dat.fractionPastIndex, ti);
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				// we've found a float number with a fraction

				utils.copyTextIndex(dat.pastIndex, ti);
				utils.copyTextIndex(dat.fractionPastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT) {
				if(utils.isDecimalChar(c)) {
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '_') {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_EXPONENT_UNDERSCORE;
					// dat.resultLength += len;
					dat.unsureLength = len;
					dat.currentIndex += len;
					dat.unsure = true;

					// set these variables in case we're at the end of
					// the token
					utils.copyTextIndex(dat.exponentPastIndex, ti);
					utils.copyTextIndex(dat.pastIndex, ti);
					return;
				}

				if(isFlagSingle(c)) {
					dat.flagSize = 32;
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FLAG;
					utils.copyTextIndex(dat.exponentPastIndex, ti);
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(isFlagDouble(c)) {
					dat.flagSize = 64;
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FLAG;
					utils.copyTextIndex(dat.exponentPastIndex, ti);
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				if(isFlagLong(c)) {
					dat.flagSize = 128;
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FLAG;
					utils.copyTextIndex(dat.exponentPastIndex, ti);
					dat.resultLength += len;
					dat.currentIndex += len;
					return;
				}

				// we've found a float with an exponent

				utils.copyTextIndex(dat.exponentPastIndex, ti);
				utils.copyTextIndex(dat.pastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyFloatTokenHelperData.STATE_FLAG) {
				dat.state = TokenHelperData.STATE_DONE;
				utils.copyTextIndex(dat.pastIndex, ti);
				return;
			}		
		}
		
		if(dat.unsure) {
			// unsure states
			
			if(state == CFamilyFloatTokenHelperData.STATE_INTEGER_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isDecimalChar(c)) {
					dat.miniState = CFamilyFloatTokenHelperData.STATE_INTEGER;
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

			if(state == CFamilyFloatTokenHelperData.STATE_FRACTION_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isDecimalChar(c)) {
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FRACTION;
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

			if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT_UNDERSCORE) {
				if(c == '_') {
					// dat.resultLength += len;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(utils.isDecimalChar(c)) {
					dat.miniState = CFamilyFloatTokenHelperData.STATE_EXPONENT;
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

			if(state == CFamilyFloatTokenHelperData.STATE_RADIX_POINT) {
				if(utils.isDecimalChar(c)) {
					utils.copyTextIndex(dat.fractionStartIndex, ti);
					dat.miniState = CFamilyFloatTokenHelperData.STATE_FRACTION;
					dat.hasFraction = true;
					dat.match = true;
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					dat.unsure = false;
					return;
				}

				// we've failed to get a float number
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT_MARK) {
				if(utils.isDecimalChar(c)) {
					utils.copyTextIndex(dat.exponentStartIndex, ti);
					dat.miniState = CFamilyFloatTokenHelperData.STATE_EXPONENT;
					dat.hasExponent = true;
					dat.match = true;
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					dat.unsure = false;
					return;
				}

				if(c == '+') {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_EXPONENT_SIGN;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				if(c == '-') {
					dat.miniState =
						CFamilyFloatTokenHelperData.STATE_EXPONENT_SIGN;
					dat.flagExponentNeg = true;
					dat.unsureLength += len;
					dat.currentIndex += len;
					return;
				}

				// return previous result
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT_SIGN) {
				if(utils.isDecimalChar(c)) {
					utils.copyTextIndex(dat.exponentStartIndex, ti);
					dat.miniState = CFamilyFloatTokenHelperData.STATE_EXPONENT;
					dat.hasExponent = true;
					dat.match = true;
					dat.resultLength += len + dat.unsureLength;
					dat.currentIndex += len;
					dat.unsure = false;
					return;
				}

				// return previous result
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
		
		if(state == CFamilyFloatTokenHelperData.STATE_INTEGER) {
			// we failed matching a float number
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == CFamilyFloatTokenHelperData.STATE_FRACTION) {
			// we've found a float number with a fraction
			
			utils.copyTextIndex(dat.pastIndex, ti);
			utils.copyTextIndex(dat.fractionPastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT) {
			// we've found a float with an exponent
			
			utils.copyTextIndex(dat.exponentPastIndex, ti);
			utils.copyTextIndex(dat.pastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyFloatTokenHelperData.STATE_RADIX_POINT) {
			// we've failed to get a float number
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
				
		if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT_MARK) {
			// return previous result
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == CFamilyFloatTokenHelperData.STATE_EXPONENT_SIGN) {
			// return previous result
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyFloatTokenHelperData.STATE_FLAG) {
			dat.state = TokenHelperData.STATE_DONE;
			utils.copyTextIndex(dat.pastIndex, ti);
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
