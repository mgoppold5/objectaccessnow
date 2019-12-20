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

//import unnamed.common.*;
import unnamed.file.system.*;
import unnamed.family.compiler.*;

public class TranslateOldTest1DocData {
	FileNode2 testDirNode;
	FileNode2 testFileNode;
	FileRef2 fileDat;
	
	CharReaderAccess charRead;
	StringReaderAccess strRead;
	//CharReaderParamsFile charReadParams;
	
	TokenChooser tokChoose;
	XmlBaseReader xmlRead;
	TokenIntegerEval intEval;
	
	public void init(
		TranslateOldTest1Data commonDat) {
		
		tokChoose = new TokenChooser();
		tokChoose.dat = new TokenChooserData();
		tokChoose.dat.init();
		//tokChoose.dat.charReadParams = 
		tokChoose.charRead = charRead;
		tokChoose.utils = commonDat.utils;

		xmlRead = new XmlBaseReader();
		xmlRead.dat = new XmlBaseReaderData();
		xmlRead.dat.init();
		xmlRead.utils = commonDat.utils;
		//xmlRead.dat.charReadParams = 
		xmlRead.charRead = charRead;
		xmlRead.tokChoose = tokChoose;
		xmlRead.initHelpers(commonDat.utils);
		
		intEval = new TokenIntegerEval();
		intEval.dat = new TokenIntegerEvalData();
		intEval.dat.init();
		//intEval.dat.charReadParams = 
		intEval.utils = commonDat.utils;
		//intEval.charRead = charRead;
	}
}
