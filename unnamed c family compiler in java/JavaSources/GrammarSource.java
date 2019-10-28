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

public class GrammarSource implements BaseModule {
	public GrammarSourceData dat;
	public CharReaderAccess charRead;
	public StringReaderAccess strRead;
	
	public CFamilySimpleTokenReader2 tokenRead;
	public TokenChooser tokChoose;
	public MatchingTokenHelper keywordHelp;
	public TokenIntegerEval intEval;
	public TokenStringEval strEval;
	
	public BaseModuleData getData() {return dat;}
	public CommonArrayList getChildModules() {return null;}
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		
		if(charRead == null) return true;
		if(strRead == null) return true;
		if(tokenRead == null) return true;
		if(tokChoose == null) return true;
		if(keywordHelp == null) return true;
		if(intEval == null) return true;
		if(strEval == null) return true;
		
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
}