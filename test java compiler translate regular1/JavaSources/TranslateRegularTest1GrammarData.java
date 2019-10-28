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

public class TranslateRegularTest1GrammarData {
	FileNode2 grammarDirNode;
	CommonInt32Array grammarFilePath;

	int k;

	GrammarReaderData gdef;
	GrammarReader gdefRead;
	LrkCanonData lrkDat;
	LrkCanon lrkCalc;
	CommonArrayList spectrumStack;
	
	SymbolAllocHelper allocHelp;
	CommonInt32Array trimPositions;

	
	public void init(TranslateRegularTest1Data commonDat) {
		k = 1;
		
		gdef = new GrammarReaderData();
		gdef.init();
		
		gdefRead = new GrammarReader();
		gdefRead.dat = gdef;
		gdefRead.utils = commonDat.utils;
		gdefRead.tokUtils = commonDat.tokUtils;
		gdefRead.linkUtils = commonDat.linkUtils;
		
		lrkDat = new LrkCanonData();
		lrkDat.init();
		lrkDat.grmrDef = gdef;
		lrkDat.k = k;
		lrkDat.keepClosures = true;
		
		lrkCalc = new LrkCanon();
		lrkCalc.dat = lrkDat;
		lrkCalc.utils = commonDat.utils;
		lrkCalc.lrUtils = commonDat.lrUtils;
	}
}
