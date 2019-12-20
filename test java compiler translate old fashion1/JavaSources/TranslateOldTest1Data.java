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

import unnamed.common.*;
import unnamed.file.system.*;
import unnamed.family.compiler.*;

public class TranslateOldTest1Data {
	// given
	//

	public String[] args;
	

	// general
	//

	GeneralUtils utils;
	TokenUtils tokUtils;
	PublicLinkUtils linkUtils;
	LrUtils lrUtils;
	GrammarUtils grmrUtils;
	SymbolReAllocUtils symReAllocUtils;
	CFamilyNodeUtils nameNodeUtils;
	
	
	// argument stuff
	//
	
	static final int FLAG_TARGET = 1;
	static final int FLAG_OUTSIDE = 2;
	static final int FLAG_CACHE = 3;
	static final int FLAG_OVERRIDE = 4;
	
	static final short SELECT_MAIN_SOURCE = (short) 0;
	static final short SELECT_MAIN_TARGET = (short) (1 << FLAG_TARGET);
	
	short testFlags;

	
	// translation data
	// 

	static final int TRIM_WAIT_MAX = 1;
	
	static final int PASS_1_COLLECT_NAMES = 1;
	static final int PASS_2_LINK_TYPES = 2;
	static final int PASS_3_WRITE_LOGIC = 3;
	
	CFamilyNode2 cfamilyChunkStack;
	SymbolAllocHelper loadingAllocHelp;
	SymbolAllocHelper loadingAllocHelp2;
	CommonArrayList trimStatStack;
	int trimWaitNum;
	int majorPassNum;
	
	// important chunks
	CFamilyNode2 rootChunk;
	CFamilyNode2 mainChunk;
	

	// directory stuff
	//
	
	CommonArrayList directory;

	FileNode2 globalGrammarDirNode;
	CommonInt32Array globalGrammarFilePath;
	
	FileNode2 globalDefDirNode;
	CommonInt32Array globalDefFilePath;

	FileNode2 globalTestDirNode;
	FileNode2 globalTestOutputDirNode;

	CommonInt32Array nameType;

	CommonArrayList testDirList;

	
	
	// testing stuff
	//
	
	int okTestFileCount;
	boolean shouldPrintTestFileName;
	long traceOldAllocCount;
	long traceNewAllocCount;
	long traceStrangeAllocCount;
	long traceSpecificAllocCount;

	
	// console output stuff
	//
	
	static final String INDENT_STRING = "  ";
	
	int indentCount;
	
	
	// file output stuff
	//
	
	CommonInt32Array lineReturnStr;
	CommonInt32Array spaceStr;
}
