/*
 * Copyright (c) 2016 Mike Goppold von Lobsdorf
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

public class CommentRemoveTokenFilter
	implements TokenReader {

	public CommentRemoveTokenFilterData dat;
	public GeneralUtils utils;
	public TokenReader tokRead;
	
	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList c;
		
		c = makeArrayList();
		
		addExistingModule(c, tokRead);
		
		return c;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		if(tokRead == null) return true;
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

		Throwable ex;
		
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		
		try {
			
		if(moveDirection != ModuleMoveDirection.TO_NEXT)
			return moveResult;
		
		while(true) {
			moveResult = tokRead.move(moveDirection);

			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;

			tokDat = (TokenReaderData) tokRead.getData();
			if(tokDat.state != TokenReaderData.STATE_HAVE_TOKEN)
				throw makeInvalidEnum(null);

			if(utils.getSymbolIdCategory(tokDat.resultToken)
				!= Symbols.TOKEN_CATEGORY_COMMENT) {

				break;
			}
		
			continue;
		}
		
		dat.resultToken = tokDat.resultToken;
		dat.state = TokenReaderData.STATE_HAVE_TOKEN;
		
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
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
