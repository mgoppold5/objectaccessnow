/*
 * Copyright (c) 2016-2017 Mike Goppold von Lobsdorf
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

public class KeywordTokenFilter
	implements TokenReader {

	public KeywordTokenFilterData dat;
	public GeneralUtils utils;
	public CharReaderAccess charRead;
	public TokenReader tokRead;
	public TokenChooser tokChoose;
	public MatchingTokenHelper keywordHelp;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}
	
	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList c;
		
		c = makeArrayList();
		
		addExistingModule(c, charRead);
		addExistingModule(c, tokRead);
		
		return c;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		if(utils == null) return true;
		if(charRead == null) return true;
		if(tokRead == null) return true;
		if(keywordHelp == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.probBag.reset();
		
		dat.state = BaseModuleData.STATE_START;
	}
	
	public int move(int moveDirection) {
		int moveResult;
		TokenReaderData tokDat;
		Token keywordTok;
		TokenChooserData chooseDat;

		Throwable ex;
		
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		
		try {

		if(moveDirection != ModuleMoveDirection.TO_NEXT)
			return moveResult;
		
		moveResult = tokRead.move(moveDirection);
		
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		tokDat = (TokenReaderData) tokRead.getData();
		if(tokDat.state != TokenReaderData.STATE_HAVE_TOKEN)
			throw makeInvalidEnum(null);
				
		if(utils.getSymbolIdPrimary(tokDat.resultToken)
			!= Symbols.TOKEN_IDENTIFIER) {
			
			dat.state = tokDat.state;
			dat.resultToken = tokDat.resultToken;
			return moveResult;
		}
		
		chooseDat = tokChoose.dat;
		
		tokChoose.reset();
		
		chooseDat.possibleHelpers.add(keywordHelp);
		
		utils.copyTextIndex(dat.cc1.ti, tokDat.resultToken.startIndex);
		dat.cc1.bi.versionNumber =
			ReadBuffer.VERSION_NUMBER_INVALID;
		
		transferStrangeAllocCounts2(chooseDat);

		//if(allocHelp == null) throw new NullPointerException();
		tokChoose.setAllocHelper(allocHelp);
		tokChoose.chooseToken(dat.cc1);
		tokChoose.setAllocHelper(null);

		transferAllocCounts2(chooseDat);
		
		if(chooseDat.state == TokenChooserData.STATE_STUCK) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState =
				StuckStates.STATE_DUE_TO_DESCENDANTS;
			return ModuleMoveResult.STUCK;
		}

		if(chooseDat.state != TokenChooserData.STATE_CHOSEN) {
			throw makeInvalidEnum("TokenChooser has a bad state");
		}

		if(chooseDat.resultCount > 1)
			throw new IllegalStateException();

		if(chooseDat.resultCount == 1) {
			TokenHelperData helpDat = (TokenHelperData) chooseDat.chosenHelper.getData();

			transferStrangeAllocCounts(helpDat);
			
			//if(allocHelp == null) throw new NullPointerException();
			chooseDat.chosenHelper.setAllocHelper(allocHelp);
			keywordTok = chooseDat.chosenHelper.getToken();
			chooseDat.chosenHelper.setAllocHelper(null);

			transferAllocCounts(helpDat);

			if(keywordTok.pastIndex.index
				== tokDat.resultToken.pastIndex.index) {
				
				dat.resultToken = keywordTok;
				dat.state = TokenReaderData.STATE_HAVE_TOKEN;
				return ModuleMoveResult.SUCCESS;
			}
		}
		
		dat.resultToken = tokDat.resultToken;
		dat.state = TokenReaderData.STATE_HAVE_TOKEN;
		return ModuleMoveResult.SUCCESS;
		
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			dat.probBag.addProblem(
				ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_PERMANENT;
			return ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
	
	public void trim(SymbolReAllocUtils reAllocUtils) {}
	
	public void reAlloc(
		SymbolAllocHelper nextAllocHelp, SymbolReAllocUtils reAllocUtils) {

		Symbol oldSym = dat.resultToken;
		if(oldSym != null) {
			dat.resultToken = (Token) reAllocUtils.doReAlloc(
				oldSym, nextAllocHelp, utils);
			//reAllocUtils.breakupSymbolTree(oldSym);
		}
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}

	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}

	private void transferAllocCounts(TokenHelperData helpDat) {
		long aCount;

		aCount = helpDat.traceOldAllocCount;
		helpDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;

		aCount = helpDat.traceNewAllocCount;
		helpDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferAllocCounts2(TokenChooserData chooseDat) {
		long aCount;

		aCount = chooseDat.traceOldAllocCount;
		chooseDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;
		
		aCount = chooseDat.traceNewAllocCount;
		chooseDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferStrangeAllocCounts(TokenHelperData helpDat) {
		long aCount;

		aCount = helpDat.traceOldAllocCount;
		helpDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		aCount = helpDat.traceNewAllocCount;
		helpDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
	}

	private void transferStrangeAllocCounts2(TokenChooserData chooseDat) {
		long aCount;

		aCount = chooseDat.traceOldAllocCount;
		chooseDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
		
		aCount = chooseDat.traceNewAllocCount;
		chooseDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
	}
}
