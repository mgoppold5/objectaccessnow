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

import unnamed.common.*;
import unnamed.file.system.*;
import unnamed.family.compiler.*;

public class TranslateOldTest1LogicReadData {
	FileNode2 testDirNode;
	FileNode2 testFileNode;
	FileRef2 fileDat;
	
	CharReaderAccess charRead;
	StringReaderAccess strRead;
	//CharReaderParamsFile charReadParams;
	
	TokenChooser tokChoose;
	CFamilySimpleTokenReader2 tokenRead;
	KeywordTokenFilter keywordRead;
	CommentRemoveTokenFilter commentRemoveRead;
	LrGramReader gramRead;
	TokenIntegerEval intEval;
	
	SymbolAllocHelper allocHelp;
	CommonInt32Array trimPositions;
	CommonInt32Array currentTrimPositions;
	SymbolAllocHelper allocHelp2;
	CommonInt32Array trimPositions2;
	
	Symbol mostRecentParseSym;
	CFamilyNode2 currentCFamilyNode;
	
	public void init(
		TranslateOldTest1Data commonDat,
		TranslateOldTest1GrammarData grmrDat) {
		
		tokChoose = new TokenChooser();
		tokChoose.dat = new TokenChooserData();
		tokChoose.dat.init();
		//tokChoose.dat.charReadParams = 
		tokChoose.charRead = charRead;
		tokChoose.utils = commonDat.utils;

		tokenRead = new CFamilySimpleTokenReader2();
		tokenRead.dat = new CFamilySimpleTokenReader2Data();
		tokenRead.dat.init();
		//tokenRead.dat.charReadParams = 
		tokenRead.utils = commonDat.utils;
		tokenRead.charRead = charRead;
		tokenRead.tokChoose = tokChoose;
		tokenRead.initHelpers(commonDat.utils, commonDat.tokUtils);
		
		keywordRead = new KeywordTokenFilter();
		keywordRead.dat = new KeywordTokenFilterData();
		keywordRead.dat.init();
		//keywordRead.dat.charReadParams = 
		keywordRead.utils = commonDat.utils;
		keywordRead.charRead = charRead;
		keywordRead.tokRead = tokenRead;
		keywordRead.tokChoose = tokChoose;
		keywordRead.keywordHelp = new MatchingTokenHelper();
		keywordRead.keywordHelp.dat = new MatchingTokenHelperData();
		keywordRead.keywordHelp.dat.init();
		keywordRead.keywordHelp.dat.matchMap
			= commonDat.tokUtils.cFamilyKeyword2TokenMap;
		keywordRead.keywordHelp.utils = commonDat.utils;
		
		commentRemoveRead = new CommentRemoveTokenFilter();
		commentRemoveRead.dat = new CommentRemoveTokenFilterData();
		commentRemoveRead.dat.init();
		commentRemoveRead.utils = commonDat.utils;
		commentRemoveRead.tokRead = keywordRead;
		
		gramRead = new LrGramReader();
		gramRead.dat = new LrGramReaderData();
		gramRead.dat.init();
		//gramRead.dat.charReadParams = 
		gramRead.dat.lrkDat = grmrDat.lrkDat;
		gramRead.utils = commonDat.utils;
		gramRead.lrUtils = commonDat.lrUtils;
		gramRead.grmrUtils = commonDat.grmrUtils;
		gramRead.tokRead = commentRemoveRead;
		
		intEval = new TokenIntegerEval();
		intEval.dat = new TokenIntegerEvalData();
		intEval.dat.init();
		//intEval.dat.charReadParams = 
		intEval.utils = commonDat.utils;
		//intEval.charRead = charRead;
	}
}
