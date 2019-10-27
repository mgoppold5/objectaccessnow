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

public class CFamilyCommentTokenHelper
	implements TokenHelper {
	
	public GeneralUtils utils;
	public CFamilyCommentTokenHelperData dat;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}
	
	public Token getToken() {
		Token tok;
		TokenContainer t2;
		
		tok = null;
		if(allocHelp == null)
			tok = makeToken(2);
		if(allocHelp != null)
			tok = makeTokenWithHelper(2);
		
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING,
			Symbols.TOKEN_STRING_SPAN);

		utils.copyTextIndex(tok.startIndex, dat.contentStartIndex);
		utils.copyTextIndex(tok.pastIndex, dat.contentPastIndex);
		
		t2 = null;
		if(allocHelp == null)
			t2 = makeTokenContainer(2, 1);
		if(allocHelp != null)
			t2 = makeTokenContainerWithHelper(2, 1);		
		
		if(dat.commentType ==
			CFamilyCommentTokenHelperData.COMMENT_TYPE_SINGLE_LINE)
			
			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_COMMENT,
				Symbols.TOKEN_COMMENT_SINGLE_LINE);
		else if(dat.commentType == 
			CFamilyCommentTokenHelperData.COMMENT_TYPE_MULTI_LINE)
			
			utils.setSymbolIdLen2(t2, Symbols.TOKEN_CATEGORY_COMMENT,
				Symbols.TOKEN_COMMENT_MULTI_LINE);
		else
			throw new IllegalStateException();
		
		utils.copyTextIndex(t2.startIndex, dat.startIndex);
		utils.copyTextIndex(t2.pastIndex, dat.pastIndex);
		
		t2.tok[0] = tok;
		
		return t2;
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
		dat.commentType = CFamilyCommentTokenHelperData.COMMENT_TYPE_UNKNOWN;
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
						
			if(c == '/') {
				utils.copyTextIndex(dat.startIndex, ti);

				dat.miniState =
					CFamilyCommentTokenHelperData.STATE_BEGIN_SLASH;
				dat.state = TokenHelperData.STATE_CONTINUE;
				dat.resultLength += len;
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
		
		if(state == CFamilyCommentTokenHelperData.STATE_MIDDLE_MULTI_LINE) {
			if(c == '*') {
				utils.copyTextIndex(dat.contentPastIndex, ti);

				dat.miniState =
					CFamilyCommentTokenHelperData.STATE_END_ASTERISK;
				dat.resultLength += len;
				dat.currentIndex += len;
				return;
			}

			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}

		if(state == CFamilyCommentTokenHelperData.STATE_MIDDLE_SINGLE_LINE) {
			if(c == 0xD || c == 0xA) {
				utils.copyTextIndex(dat.pastIndex, ti);
				utils.copyTextIndex(dat.contentPastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}

			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}
		
		if(state == CFamilyCommentTokenHelperData.STATE_BEGIN_SLASH) {
			if(c == '/') {
				dat.miniState =
					CFamilyCommentTokenHelperData.STATE_BEGIN_SLASH2;
				dat.commentType =
					CFamilyCommentTokenHelperData.COMMENT_TYPE_SINGLE_LINE;
				dat.resultLength += len;
				dat.currentIndex += len;
				dat.match = true;
				return;
			}
			
			if(c == '*') {
				dat.miniState =
					CFamilyCommentTokenHelperData.STATE_BEGIN_ASTERISK;
				dat.commentType =
					CFamilyCommentTokenHelperData.COMMENT_TYPE_MULTI_LINE;
				dat.resultLength += len;
				dat.currentIndex += len;
				dat.match = true;
				return;
			}
			
			// we failed matching a comment
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyCommentTokenHelperData.STATE_BEGIN_SLASH2) {
			utils.copyTextIndex(dat.contentStartIndex, ti);
			
			if(c == 0xD || c == 0xA) {
				utils.copyTextIndex(dat.pastIndex, ti);
				utils.copyTextIndex(dat.contentPastIndex, ti);
				dat.state = TokenHelperData.STATE_DONE;
				return;
			}
			
			dat.miniState =
				CFamilyCommentTokenHelperData.STATE_MIDDLE_SINGLE_LINE;
			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}
		
		if(state == CFamilyCommentTokenHelperData.STATE_BEGIN_ASTERISK) {
			utils.copyTextIndex(dat.contentStartIndex, ti);
			
			if(c == '*') {
				utils.copyTextIndex(dat.contentPastIndex, ti);
				
				dat.miniState =
					CFamilyCommentTokenHelperData.STATE_END_ASTERISK;
				dat.resultLength += len;
				dat.currentIndex += len;
				return;
			}
			
			dat.miniState =
				CFamilyCommentTokenHelperData.STATE_MIDDLE_MULTI_LINE;
			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}

		if(state == CFamilyCommentTokenHelperData.STATE_END_ASTERISK) {
			if(c == '*') {
				utils.copyTextIndex(dat.contentPastIndex, ti);
				
				dat.resultLength += len;
				dat.currentIndex += len;
				return;
			}
			
			if(c == '/') {
				dat.miniState =
					CFamilyCommentTokenHelperData.STATE_END_SLASH;
				dat.resultLength += len;
				dat.currentIndex += len;
				return;
			}
			
			// cancel the end of the comment, back to content
			
			dat.miniState =
				CFamilyCommentTokenHelperData.STATE_MIDDLE_MULTI_LINE;
			dat.resultLength += len;
			dat.currentIndex += len;
			return;
		}
		
		if(state == CFamilyCommentTokenHelperData.STATE_END_SLASH) {
			utils.copyTextIndex(dat.pastIndex, ti);
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
		LangError e2;
		TextIndex context;
		
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
		
		if(state == CFamilyCommentTokenHelperData.STATE_MIDDLE_MULTI_LINE
			|| state == CFamilyCommentTokenHelperData.STATE_BEGIN_ASTERISK
			|| state == CFamilyCommentTokenHelperData.STATE_END_ASTERISK) {
			
			context = new TextIndex();
			utils.copyTextIndex(context, ti);

			e2 = new LangError();
			e2.id = LangErrors.ERROR_COMMENT_UNTERMINATED;
			e2.context = context;

			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);

			utils.copyTextIndex(dat.pastIndex, ti);
			utils.copyTextIndex(dat.contentPastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}

		if(state == CFamilyCommentTokenHelperData.STATE_MIDDLE_SINGLE_LINE
			|| state == CFamilyCommentTokenHelperData.STATE_BEGIN_SLASH2) {

			utils.copyTextIndex(dat.pastIndex, ti);
			utils.copyTextIndex(dat.contentPastIndex, ti);
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyCommentTokenHelperData.STATE_BEGIN_SLASH) {
			// we've failed to match a comment
			
			dat.state = TokenHelperData.STATE_DONE;
			return;
		}
		
		if(state == CFamilyCommentTokenHelperData.STATE_END_SLASH) {
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

	public Token makeTokenWithHelper(int idLen) {
		dat.traceNewAllocCount += 1;
		return allocHelp.makeToken(idLen);
	}

	public TokenContainer makeTokenContainerWithHelper(
		int idLen, int childCount) {

		dat.traceNewAllocCount += 1;
		
		return allocHelp.makeTokenContainer(idLen, childCount);
	}
}
