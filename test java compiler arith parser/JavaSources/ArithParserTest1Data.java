/*
 * Copyright (c) 2015-2016 Mike Goppold von Lobsdorf
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

public class ArithParserTest1Data {
	public String[] args;
	
	// private variables
	//
	
	static final String INDENT_STRING = "  ";
	
	int indentCount;

	// file and directory stuff
	
	CommonArrayList directory;

	FileNode2 grammarDirNode;
	FileNode2 grammarFileNode;
	
	FileNode2 testDirNode;
	FileNode2 testFileNode;
	FileRef2 fileDat;

	
	// General
	//
	

	GeneralUtils utils;
	TokenUtils tokUtils;
	PublicLinkUtils linkUtils;
	LrUtils lrUtils;
	
	
	// for grammar file
	//
	
	int k;

	GrammarReaderData gdef;
	GrammarReader gdefRead;
	LrkCanonData lrkDat;
	LrkCanon lrkCalc;
	GrammarUtils grmrUtils;
	CommonArrayList spectrumStack;
	
	
	// for data file
	//
	
	CharReaderAccess charRead;
	StringReaderAccess strRead;
	//CharReaderParamsFile charReadParams;
	
	TokenChooser tokChoose;
	CFamilySimpleTokenReader2 tokenRead;
	LrGramReader gramRead;
	TokenIntegerEval intEval;
}
