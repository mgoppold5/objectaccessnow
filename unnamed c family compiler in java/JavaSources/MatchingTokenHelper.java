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

public class MatchingTokenHelper
	implements TokenHelper {
	
	public GeneralUtils utils;
	public MatchingTokenHelperData dat;
	private SymbolAllocHelper allocHelp;

	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}

	public Token getToken() {
		// this function only supports simple tokens which
		// class is Token

		Token tok;
		
		if(!dat.match)
			throw new IllegalStateException();
		
		if(dat.tokenId == null) throw new NullPointerException(null);
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(utils.getIdLenFromSymbolId(dat.tokenId));
		if(allocHelp != null)
			tok = makeTokenWithHelper(utils.getIdLenFromSymbolId(dat.tokenId));
		
		utils.copySymbolIdToSymbolFromSymbolId(tok, dat.tokenId);
		
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
		dat.unsureLength = 0;
		dat.strMatchIndex = 0;
		dat.tokenId = makeSymbolId(4);
		
		dat.state = BaseModuleData.STATE_START;
	}
	
	public void advanceOneChar(CharReaderContext cc) {
		int c;
		int len;
		int state;
		
		TypeAndObject[] matchMap;

		CommonError e1;
		
		matchMap = dat.matchMap;
		
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
			
			// simple to update state, state stays the same,
			// but indexes are incremented
			dat.unsureLength += len;
			dat.currentIndex += len;
			return;
		}
		
		if(cc.state != CharReaderData.STATE_HAVE_CHAR) {
			throw new IllegalStateException();
		}
		
		c = cc.resultChar;
		len = cc.resultCharLength;
		
		if(dat.state == BaseModuleData.STATE_START) {
			utils.copyTextIndex(dat.startIndex, ti);

			// garbage in
			//
			
			if(matchMap == null) {
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			if(matchMap.length == 0) {
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			// we're good
			//
			
			dat.selectionStartIndex = 0;
			dat.selectionPastIndex = matchMap.length;
			dat.currentIndex = ti.index;
			narrowWithChar(cc);
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
		
		narrowWithChar(cc);
		return;
	}
	
	private void narrowWithChar(CharReaderContext cc) {
		int c;
		int len;
		int c2;
		int minIndexTemp; // inclusive end
		int maxIndexTemp; // enclusive end
		int guessIndex;
		int minIndex2; // inclusive end
		int maxIndex2; // exclusive end
		
		TextIndex ti = cc.ti;
		
		SymbolId idRec;
		
		TypeAndObject entry;
		CommonInt32Array entryStr;
		
		TypeAndObject[] matchMap;

		matchMap = dat.matchMap;
		
		c = cc.resultChar;
		len = cc.resultCharLength;
		
		// check for a match before we narrow the selection
		entry = matchMap[dat.selectionStartIndex];
		entryStr = (CommonInt32Array) entry.sortObject;
		if(entryStr.length == dat.strMatchIndex) {
			// yes, checking for a match is simple
			// found match
						
			utils.copyTextIndex(dat.pastIndex, ti);
			
			idRec = (SymbolId) entry.theObject;
			//dat.tokenId = ArrayUtils.copyInt32Array(idRec.id);
			utils.copySymbolId(dat.tokenId, idRec);
			dat.match = true;
			
			dat.resultLength += dat.unsureLength;
			dat.unsureLength = 0;
		}		
		
		// find the new maxIndex2, by narrowing between
		// strings which are greater, and strings which
		// are equal or less

		minIndexTemp = dat.selectionStartIndex;
		maxIndexTemp = dat.selectionPastIndex;
		
		while(true) {
			if(minIndexTemp == maxIndexTemp) {
				maxIndex2 = minIndexTemp;
				break;
			}
			
			guessIndex = (minIndexTemp + maxIndexTemp) / 2;
			
			entry = matchMap[guessIndex];
			entryStr = (CommonInt32Array) entry.sortObject;
			
			if(entryStr.length <= dat.strMatchIndex) {
				// string in map is less
				minIndexTemp = guessIndex + 1;
				continue;
			}
			
			c2 = entryStr.aryPtr[dat.strMatchIndex];
			
			if(c2 <= c) {
				// string in map is less or equal
				minIndexTemp = guessIndex + 1;
				continue;
			}
			
			// string in map is greater
			maxIndexTemp = guessIndex;
			continue;
		}
		
		// find minIndex2, by narrowing between
		// strings which are less, and strings which
		// are equal or greater
		
		minIndexTemp = dat.selectionStartIndex;
		maxIndexTemp = maxIndex2;
		
		while(true) {
			if(minIndexTemp == maxIndexTemp) {
				minIndex2 = minIndexTemp;
				break;
			}
			
			guessIndex = (minIndexTemp + maxIndexTemp) / 2;
			
			entry = matchMap[guessIndex];
			entryStr = (CommonInt32Array) entry.sortObject;
			
			if(entryStr.length <= dat.strMatchIndex) {
				// string in map is less
				minIndexTemp = guessIndex + 1;
				continue;
			}
			
			c2 = entryStr.aryPtr[dat.strMatchIndex];
			
			if(c2 < c) {
				// string in map is less
				minIndexTemp = guessIndex + 1;
				continue;
			}
			
			// string in map is equal or greater
			maxIndexTemp = guessIndex;
			continue;
		}
		
		// we have minIndex2 and maxIndex2
				
		// if they are equal, we are done, and the most
		// recent match, if any, is valid
		if(minIndex2 == maxIndex2) {
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		// update state
		dat.state = TokenHelperData.STATE_CONTINUE;
		dat.strMatchIndex += 1;
		dat.selectionStartIndex = minIndex2;
		dat.selectionPastIndex = maxIndex2;
		dat.unsureLength += len;
		dat.currentIndex += len;
		return;
	}
	
	public void processEndOfStream(CharReaderContext cc) {
		SymbolId idRec;
		
		TypeAndObject entry;
		CommonInt32Array entryStr;
		
		TypeAndObject[] matchMap;
		
		CommonError e1;
		
		matchMap = dat.matchMap;
		
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
			e1.msg = "int token helper is in a bad state";
			throw e1;
		}
		
		// check for a match with previous state
		entry = matchMap[dat.selectionStartIndex];
		entryStr = (CommonInt32Array) entry.sortObject;
		
		if(entryStr.length == dat.strMatchIndex) {
			// yes, checking for a match is simple
			// found match
			
			utils.copyTextIndex(dat.pastIndex, ti);
			
			idRec = (SymbolId) entry.theObject;
			//dat.tokenId = ArrayUtils.copyInt32Array(idRec.id);
			utils.copySymbolId(dat.tokenId, idRec);
			dat.match = true;
			
			dat.resultLength += dat.unsureLength;
			dat.unsureLength = 0;
		}
		
		// we're done
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

	public SymbolId makeSymbolId(int idLen) {
		SymbolId symId = utils.makeSymbolId(idLen);
		return symId;
	}
}
