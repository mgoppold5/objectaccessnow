/*
 * Copyright (c) 2015-2017 Mike Goppold von Lobsdorf
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

public class TokenStringEval implements BaseModule {
	public TokenStringEvalData dat;
	public GeneralUtils utils;
	public StringReaderAccess strRead;
	
	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, strRead);

		return o;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		
		if(utils == null) return true;
		if(strRead == null) return true;
		
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void readInt32String() {
		readJavaString();
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;
		
		dat.resultInt32String = StringUtils.int32StringFromJavaString(
			dat.resultJavaString);
		dat.state = TokenStringEvalData.STATE_HAVE_INT32_STRING;
		return;
	}
	
	public void readJavaString() {
		StringBuilder sb = new StringBuilder();
		String s;
		Token tok;
		int id;
		long len;
		int state;
		
		StringReader2Data strDat = (StringReader2Data) strRead.getData();

		int elemNum;
		int elemCount;
		
		elemNum = 0;
		elemCount = dat.strTok.elements.size();
		
		while(elemNum < elemCount) {
			tok = (Token) dat.strTok.elements.get(elemNum);
			id = utils.getSymbolIdPrimary(tok);

			if(id == Symbols.TOKEN_STRING_SPAN) {
				len = tok.pastIndex.index - tok.startIndex.index;
				
				strRead.readJavaString(tok.startIndex, len);
				
				state = strDat.state;

				if(state == BaseModuleData.STATE_STUCK) {
					dat.state = BaseModuleData.STATE_STUCK;
					dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
					return;
				}
				
				if(state != CharReaderData.STATE_HAVE_JAVA_STRING_BUFFER)
					throw makeInvalidEnum("CharReader bad state");
				
				s = strDat.resultJavaStringBuilder.toString();
				
				sb.append(s);
				elemNum += 1;
				continue;
			}

			if(id == Symbols.TOKEN_STRING_ESCAPE_VERBATIM) {
				TokenContainer tok5 = (TokenContainer) tok;
				tok = tok5.tok[0];

				len = tok.pastIndex.index - tok.startIndex.index;

				strRead.readJavaString(tok.startIndex, len);
				
				state = strDat.state;

				if(state == BaseModuleData.STATE_STUCK) {
					dat.state = BaseModuleData.STATE_STUCK;
					dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
					return;
				}
				
				if(state != CharReaderData.STATE_HAVE_JAVA_STRING_BUFFER)
					throw makeInvalidEnum("CharReader bad state");
				
				s = strDat.resultJavaStringBuilder.toString();
				
				sb.append(s);
				elemNum += 1;
				continue;
			}

			if(id == Symbols.TOKEN_STRING_ESCAPE_NAME) {
				TokenContainer tok5 = (TokenContainer) tok;
				tok = tok5.tok[0];
				id = utils.getSymbolIdPrimary(tok);

				if(id == Symbols.TOKEN_STRING_ESCAPE_NAME_NEW_LINE) {
					sb.append("\n");
					elemNum += 1;
					continue;
				}

				if(id == Symbols.TOKEN_STRING_ESCAPE_NAME_TAB) {
					sb.append("\t");
					elemNum += 1;
					continue;
				}
			}
			
			if(id == Symbols.TOKEN_STRING_ESCAPE_IGNORED) {
				elemNum += 1;
				continue;
			}
			
			// note unhandled string element
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeStringUnimplemented(tok.startIndex));

			elemNum += 1;
			continue;
		}
		
		dat.resultJavaString = sb.toString();
		dat.state = TokenStringEvalData.STATE_HAVE_JAVA_STRING;
		return;
	}
	
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private LangError makeStringUnimplemented(TextIndex ti) {
		TextIndex context;
		LangError e3;
		
		context = new TextIndex();
		utils.copyTextIndex(context, ti);
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_STRING_UNIMPLEMENTED;
		e3.context = context;
		return e3;
	}
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
