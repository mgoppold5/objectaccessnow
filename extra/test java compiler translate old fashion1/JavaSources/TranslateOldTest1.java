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

/*
table of contents

app specific init
core logic test
core logic run test file
core logic utility functions
core logic trim stuff
core logic file output
core logic run test file collect cfamily node tree
core logic grammar
core logic doc
directory walk logic
directory config stuff
file system access
console output stuff
small utility functions
testing access functions
testing counting functions
error allocators
stack allocators
general allocators
native string small utility functions
native string console output stuff
command loop
general program init
*/

import java.io.PrintStream;

import unnamed.common.*;
import unnamed.file.system.*;
import unnamed.family.compiler.*;

public class TranslateOldTest1 {
	public TranslateOldTest1Data dat;
	public PrintStream out;
	
	TranslateOldTest1GrammarData grmrDat;
	TranslateOldTest1DocData docDat;
	TranslateOldTest1LogicReadData srcDat;
	TranslateOldTest1LogicWriteData trgDat;
	
	//CommonArrayList grmrDatList;

	CommonInt32Array KEYWORD_JAVA_BYTE;
	CommonInt32Array KEYWORD_JAVA_SHORT;
	CommonInt32Array KEYWORD_JAVA_INT;
	CommonInt32Array KEYWORD_JAVA_LONG;
	
	CommonInt32Array KEYWORD_C_INT8;
	CommonInt32Array KEYWORD_C_INT16;
	CommonInt32Array KEYWORD_C_INT32;
	CommonInt32Array KEYWORD_C_INT64;
	
	
	// app specific init
	//
	
	public void initOnce() {
		initKeywords();
		
		// init general
		//
		
		dat.utils = new GeneralUtils();
		dat.utils.init();
		dat.tokUtils = new TokenUtils();
		dat.tokUtils.init();
		dat.linkUtils = new PublicLinkUtils();
		dat.linkUtils.init();
		dat.lrUtils = new LrUtils();
		dat.lrUtils.init();
		dat.grmrUtils = new GrammarUtils();
		dat.grmrUtils.init();
		dat.symReAllocUtils = new SymbolReAllocUtils();
		dat.symReAllocUtils.init();
		dat.nameNodeUtils = new CFamilyNodeUtils();
		dat.nameNodeUtils.init();
		
		// init translation data
		//
		
		dat.cfamilyChunkStack = null;
		dat.loadingAllocHelp = makeSymbolAllocHelper();
		dat.loadingAllocHelp2 = makeSymbolAllocHelper();
		dat.trimStatStack = makeArrayList();

		
		// init directory stuff
		//
		
		dat.directory = makeArrayList();
		dat.testDirList = makeArrayList();


		
		
		// init file output stuff
		//
		
		dat.lineReturnStr = makeLineReturnStr();
		dat.spaceStr = makeSpaceStr();
	}
	
	public void initKeywords() {
		KEYWORD_JAVA_BYTE = makeStandardString("byte");
		KEYWORD_JAVA_SHORT = makeStandardString("short");
		KEYWORD_JAVA_INT = makeStandardString("int");
		KEYWORD_JAVA_LONG = makeStandardString("long");

		KEYWORD_C_INT8 = makeStandardString("int8");
		KEYWORD_C_INT16 = makeStandardString("int16");
		KEYWORD_C_INT32 = makeStandardString("int32");
		KEYWORD_C_INT64 = makeStandardString("int64");
	}


	// core logic test
	//
	
	public void initTestIncludeFileStack(CommonInt32Array filePath) {
		FileNode2 dirNode;
		FileNode2 fileNode;
		
		TranslateOldTest1IncludeFile incF = new TranslateOldTest1IncludeFile();
		incF.init(dat, grmrDat);
		
		incF.testDirNode = dat.globalTestDirNode;
		
		dirNode = (FileNode2) incF.testDirNode;
		if(dirNode == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(dirNode, filePath, '/')
			!= FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectUnexpected(null);
		
		fileNode = FileNode2Utils.createFilePathNode(dirNode, filePath);
		incF.testFileNode = fileNode;
		incF.fileDat = FileNode2Utils.openNormalFile(
			fileNode, (short) AccessRights.ACCESS_READ, '/');
		
		dat.linkUtils.initFileContext(fileNode, incF.fileDat);
		incF.charRead = dat.linkUtils.createCharReader3FromFileContext(
			fileNode, incF.fileDat, dat.utils);
		incF.strRead = dat.linkUtils.createStringReader3FromFileContext(
			fileNode, incF.fileDat, dat.utils);

		incF.strRead.reset();
		incF.charRead.reset();

		incF.tokChoose.charRead = incF.charRead;

		incF.tokenRead.charRead = incF.charRead;
		//incF.tokenRead.setAllocHelper(incF.allocHelp);
		incF.tokenRead.reset();
		
		incF.keywordRead.charRead = incF.charRead;
		//incF.keywordRead.setAllocHelper(incF.allocHelp);
		incF.keywordRead.reset();

		incF.intEval.strRead = incF.strRead;

		srcDat.includeFileStack = makeArrayList();
		srcDat.includeFileStack.add(incF);
	}
	
	
	public void initTestFile(CommonInt32Array filePath) {
		FileNode2 dirNode;
		FileNode2 fileNode;

		srcDat = new TranslateOldTest1LogicReadData();
		srcDat.init(dat, grmrDat);
		
		srcDat.testDirNode = dat.globalTestDirNode;
		
		dirNode = (FileNode2) srcDat.testDirNode;
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(dirNode, filePath, '/')
			!= FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectUnexpected(null);
		
		fileNode = FileNode2Utils.createFilePathNode(dirNode, filePath);
		
		srcDat.testFileNode = fileNode;
		
		srcDat.fileDat = FileNode2Utils.openNormalFile(
			fileNode, (short) AccessRights.ACCESS_READ, '/');
		
		dat.linkUtils.initFileContext(fileNode, srcDat.fileDat);
		srcDat.charRead = dat.linkUtils.createCharReader3FromFileContext(
			fileNode, srcDat.fileDat, dat.utils);
		srcDat.strRead = dat.linkUtils.createStringReader3FromFileContext(
			fileNode, srcDat.fileDat, dat.utils);
		
		srcDat.strRead.reset();
		srcDat.charRead.reset();
		
		srcDat.tokChoose.charRead = srcDat.charRead;
		
		srcDat.allocHelp = dat.loadingAllocHelp;
		srcDat.allocHelp2 = dat.loadingAllocHelp2;
		
		srcDat.trimPositions = makeInt32Array(srcDat.allocHelp.getCursorCount());
		srcDat.allocHelp.getCursorPositions(srcDat.trimPositions);

		srcDat.currentTrimPositions = makeInt32Array(srcDat.allocHelp.getCursorCount());

		srcDat.trimPositions2 = makeInt32Array(srcDat.allocHelp2.getCursorCount());
		srcDat.allocHelp2.getCursorPositions(srcDat.trimPositions2);

		srcDat.tokenRead.charRead = srcDat.charRead;
		srcDat.tokenRead.setAllocHelper(srcDat.allocHelp);
		srcDat.tokenRead.reset();
		
		srcDat.keywordRead.charRead = srcDat.charRead;
		srcDat.keywordRead.setAllocHelper(srcDat.allocHelp);
		srcDat.keywordRead.reset();

		srcDat.gramRead.dat.precedenceSpectrumStack = grmrDat.spectrumStack;
		srcDat.gramRead.setAllocHelper(srcDat.allocHelp);
		srcDat.gramRead.reset();

		srcDat.intEval.strRead = srcDat.strRead;
	}

	public void cleanupTestFile() {
		srcDat.allocHelp.setCursorPositions(srcDat.trimPositions);
		srcDat.allocHelp2.setCursorPositions(srcDat.trimPositions2);
		
		srcDat.charRead = null;
		srcDat.strRead = null;
		
		srcDat.tokChoose.charRead = null;

		srcDat.tokenRead.charRead = null;
		srcDat.tokenRead.setAllocHelper(null);
		srcDat.tokenRead.reset();
		
		srcDat.keywordRead.charRead = null;
		srcDat.keywordRead.setAllocHelper(null);
		srcDat.keywordRead.reset();

		srcDat.gramRead.setAllocHelper(null);
		
		srcDat.intEval.strRead = null;
		
		FileNode2Utils.closeNormalFile(
			(FileNode2) srcDat.testFileNode, srcDat.fileDat);
		srcDat.testFileNode = null;
		srcDat.fileDat = null;
		return;
	}
	
	public void initTestOutputFile(CommonInt32Array filePath) {
		FileNode2 dirNode;
		FileNode2 fileNode;

		trgDat = new TranslateOldTest1LogicWriteData();
		trgDat.init(dat, grmrDat);
		
		trgDat.testDirNode = dat.globalTestOutputDirNode;
		
		dirNode = (FileNode2) trgDat.testDirNode;
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(dirNode, filePath, '/')
			== FileTypes.FILE_TYPE_NOT_EXIST) {
			
			FileNode2Utils.createFile(dirNode, filePath, '/');
		}

		if(FileNode2Utils.getFileType(dirNode, filePath, '/')
			!= FileTypes.FILE_TYPE_NORMAL_FILE) {
			
			throw makeObjectUnexpected(null);
		}
		
		fileNode = FileNode2Utils.createFilePathNode(dirNode, filePath);
		
		trgDat.testFileNode = fileNode;
		
		trgDat.fileDat = FileNode2Utils.openNormalFile(
			fileNode,
			(short) (AccessRights.ACCESS_READ | AccessRights.ACCESS_WRITE),
			'/');
		
		dat.linkUtils.initFileContext(fileNode, trgDat.fileDat);
		//trgDat.fileDat.fs.setFileLength(trgDat.fileDat.fileHandle, 0);
		
		trgDat.curWrite = dat.linkUtils.createCursorStyle2FromFileContext(
			fileNode, trgDat.fileDat);
		trgDat.strWrite = dat.linkUtils.createStringWriter3FromFileContextAndCursor(
			fileNode, trgDat.fileDat, trgDat.curWrite, dat.utils);
		
		trgDat.curWrite.setLength(0);
		trgDat.curWrite.setStoragePointer(0);
		trgDat.curWrite.setAccessType(
			(short) StorageAccessTypes.TYPE_WRITE_APPEND);
		trgDat.curWrite.setIsCursorRunning(true);
		
		trgDat.strWrite.reset();
	}

	public void cleanupTestOutputFile() {
		trgDat.curWrite.setIsCursorRunning(false);
		trgDat.curWrite = null;

		trgDat.strWrite.reset();
		trgDat.strWrite = null;
		
		FileNode2Utils.closeNormalFile(
			(FileNode2) trgDat.testFileNode, trgDat.fileDat);
		trgDat.testFileNode = null;
		trgDat.fileDat = null;
		return;
	}
	
	
	// core logic run test file
	//
	
	public boolean runTestFile() {
		boolean isError;
		int moveResult;
		int state;
		Symbol sym;
		int id;
		LrGramReader grmModule;
		LrGramReaderData grmDat;
		boolean didMove;
		Symbol mostRecentSym;
		
		dat.shouldPrintTestFileName = false;
		
		if(false) {
			printLine(
				"File: " + StringUtils.javaStringFromInt32String(
					(CommonInt32Array) srcDat.testFileNode.sortObject)
				+ " with Grammar File: "
					+ StringUtils.javaStringFromInt32String(
						(CommonInt32Array) grmrDat.grammarFilePath));
		}
		
		if(false) {
			printLine(
				"Test File: " + StringUtils.javaStringFromInt32String(
					(CommonInt32Array) srcDat.testFileNode.sortObject));
		}
		
		if(true) {
			transferStrangeAllocCounts(srcDat.tokenRead.dat);
			if(dat.traceStrangeAllocCount > 0)
				printLine("runTestFile,TokenReader,strange," + dat.traceStrangeAllocCount);

			transferStrangeAllocCounts3(srcDat.keywordRead.dat);
			if(dat.traceStrangeAllocCount > 0)
				printLine("runTestFile,KeywordTokenFilter,strange," + dat.traceStrangeAllocCount);

			transferStrangeAllocCounts4(srcDat.gramRead.dat);
			if(dat.traceStrangeAllocCount > 0)
				printLine("runTestFile,LrGramReader,strange," + dat.traceStrangeAllocCount);
		}
		
		if(dat.majorPassNum == TranslateOldTest1Data.PASS_3_WRITE_LOGIC) {
			trgDat.whitespaceState
				= TranslateOldTest1LogicWriteData.STATE_WAS_NOTHING;
		}
		
		isError = false;
		dat.indentCount += 1; // now in File

		grmModule = srcDat.gramRead;
		grmDat = (LrGramReaderData) grmModule.getData();
		
		mostRecentSym = null;
		
		didMove = true;
		
		if(!isError)
		while(true) {
			if(!didMove) {
				moveResult = grmModule.move(ModuleMoveDirection.TO_NEXT);

				if(moveResult == ModuleMoveResult.AT_END) {
					printLine(
						"Bad move result: moveResult="
						+ moveResult);
					isError = true;
					break;
				}

				if(moveResult != ModuleMoveResult.SUCCESS) {
					printLine(
						"Bad move result: moveResult="
						+ moveResult);
					isError = true;
					break;
				}
				
				didMove = true;
			}
			
			state = grmDat.state;
			
			if(state == LrGramReaderData.STATE_HAVE_CONFLICT) {
				printLine("HAVE CONFLICT");
				isError = true;
				break;
			}
			
			if(state == LrGramReaderData.STATE_HAVE_REDUCED_GRAM) {
				sym = grmModule.getCurrentSymbol();
				id = dat.utils.getSymbolIdPrimary(sym);
								
				if(dat.majorPassNum == TranslateOldTest1Data.PASS_3_WRITE_LOGIC)

				//if(false
					//|| isVariableGram(sym)  
				//	|| isFunctionGram(sym)
				//	|| isHeaderGram(sym)) {
					
				if(isSymbolMatchForCurrentCFamilyNode(sym)) {
					isError = writeSymbol2(sym);
					if(isError) break;
				}

				if(isFunctionGram(sym) || isModuleGram(sym)) {
					boolean didTrim = false;
					if(true) {
						sym = null;
						id = 0;
						mostRecentSym = null;

						didTrim = maybeDoTrim(srcDat.gramRead, false);
					}
				}
			}

			if(state == LrGramReaderData.STATE_HAVE_REDUCED_GRAM) {
				sym = grmModule.getCurrentSymbol();
				id = dat.utils.getSymbolIdPrimary(sym);
				
				mostRecentSym = sym;
				
				// continue until an important symbol comes
				didMove = false;
				continue;
			}

			if(state == LrGramReaderData.STATE_HAVE_SHIFTED_TOKEN) {
				//sym = grmModule.getCurrentSymbol();
				//id = dat.utils.getSymbolIdPrimary(sym);
				
				didMove = false;
				continue;
			}

			if(state == LrGramReaderData.STATE_DONE) {
				break;
			}
			
			if(state == BaseModuleData.STATE_START) {
				didMove = false;
				continue;
			}
			
			
			printLine("PARSER BAD STATE,state=" + state);
			isError = true;
			break;
		}
		
		if(isError) {
			printLine("Outputing module info");

			dat.indentCount += 1; // now in module info

			dumpModuleStatus(srcDat.gramRead);
			dumpModuleStatus(srcDat.tokenRead);
			
			if(dat.majorPassNum == TranslateOldTest1Data.PASS_3_WRITE_LOGIC) {
				dumpModuleStatus(trgDat.strWrite);
			}

			dat.indentCount -= 1; // now in file
		}

		if(false) {
			if(dat.shouldPrintTestFileName)
			printLine(
				StringUtils.javaStringFromInt32String(
					(CommonInt32Array) srcDat.testFileNode.sortObject));
		}
		
		if(true)
		if(dat.majorPassNum == TranslateOldTest1Data.PASS_1_COLLECT_NAMES)
		if(!isError) {
			if(mostRecentSym == null) {
				throw makeObjectNotFound(null);
			}
			
			srcDat.mostRecentParseSym = mostRecentSym;
			isError = runTestFileCollectBasics();
			srcDat.mostRecentParseSym = null;

			if(isError) return isError;
		}

		if(!isError) {
			if(false) {
				printLine("OK");
			}
			
			if(false) {
				printLine("curReadCharTrace1="
					+ getCursorTrace1(getCharReaderCursor(srcDat.charRead)));
				printLine("curReadCharTrace2="
					+ getCursorTrace2(getCharReaderCursor(srcDat.charRead)));

				printLine("curReadStringTrace1="
					+ getCursorTrace1(getStringReaderCursor(srcDat.strRead)));
				printLine("curReadStringTrace2="
					+ getCursorTrace2(getStringReaderCursor(srcDat.strRead)));
				
				if(dat.majorPassNum == TranslateOldTest1Data.PASS_3_WRITE_LOGIC) {
					printLine("curWriteStringTrace1="
						+ getCursorTrace1(getStringWriterCursor(trgDat.strWrite)));
					printLine("curWriteStringTrace2="
						+ getCursorTrace2(getStringWriterCursor(trgDat.strWrite)));
				}

				printLine("readChar3Trace1="
					+ getCharReadTrace1(srcDat.charRead));
				printLine("readChar3Trace2="
					+ getCharReadTrace2(srcDat.charRead));
				printLine("readChar3Trace3="
					+ getCharReadTrace3(srcDat.charRead));
				printLine("readChar3Trace4="
					+ getCharReadTrace4(srcDat.charRead));
				printLine("readChar3Trace5="
					+ getCharReadTrace5(srcDat.charRead));
			}

			if(false) {
				printLine("traceOldAllocCount="
					+ srcDat.tokenRead.dat.traceOldAllocCount);
				printLine("traceNewAllocCount="
					+ srcDat.tokenRead.dat.traceNewAllocCount);
			}
				
			if(false) {
				srcDat.allocHelp.getCursorPositions(
					srcDat.currentTrimPositions);
				printLine("count," + getCursorPositionsSum(
						srcDat.currentTrimPositions));
			}
		}
		
		dat.indentCount -= 1; // now outside
		
		transferAllocCounts(srcDat.tokenRead.dat);
		transferAllocCounts3(srcDat.keywordRead.dat);
		transferAllocCounts4(srcDat.gramRead.dat);

		return isError;
	}
	
	
	// core logic utility functions
	//
	
	private boolean isHeaderGram(Symbol sym) {
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_IMPORT_DEF) return true;
		if(id == Symbols.GRAM_PACKAGE_DEF) return true;
		return false;
	}
	
	private boolean isModuleGram(Symbol sym) {
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_CLASS_DEF) return true;
		if(id == Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS) return true;
		//if(id == Symbols.GRAM_PACKAGE_DEF) return true;
		//if(1==1) return true;
		return false;
	}

	private boolean isClassGram(Symbol sym) {
		GramContainer grmCon;
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_CLASS_DEF) {
			grmCon = (GramContainer) sym;
			id = dat.utils.getSymbolIdPrimary(grmCon.sym[0]);
		}
		
		if(id == Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS) {
			grmCon = (GramContainer) sym;
			id = dat.utils.getSymbolIdPrimary(grmCon.sym[1]);
		}
		
		if(id == Symbols.TOKEN_KEYWORD_CLASS)
			return true;
		
		return false;
	}

	private boolean isInterfaceGram(Symbol sym) {
		GramContainer grmCon;
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_CLASS_DEF) {
			grmCon = (GramContainer) sym;
			id = dat.utils.getSymbolIdPrimary(grmCon.sym[0]);
		}
		
		if(id == Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS) {
			grmCon = (GramContainer) sym;
			id = dat.utils.getSymbolIdPrimary(grmCon.sym[1]);
		}
		
		if(id == Symbols.TOKEN_KEYWORD_INTERFACE)
			return true;
		
		return false;
	}

	private boolean isFunctionGram(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_FUNCTION_DEF_FULL
			|| id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS
			|| id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_INIT
			|| id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT)
			return true;
		
		return false;
	}
	
	private boolean isVariableGram(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_VARIABLE_DEF_FULL
			|| id == Symbols.GRAM_VARIABLE_DEF_FULL_WITH_INIT
			|| id == Symbols.GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS
			|| id == Symbols.GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS_WITH_INIT)
			return true;
		
		return false;
	}
	
	private boolean isIdentifierToken(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.TOKEN_IDENTIFIER) return true;

		return false;
	}
	
	private CommonInt32Array getSymbolString2(Symbol sym) {
		long stringLen = 0;
		CommonInt32Array str = null;
		
		CommonError e1;
		
		StringReader2Data strDat = (StringReader2Data) srcDat.strRead.getData();
		
		if(sym.startIndex == null || sym.pastIndex == null)
			throw makeIllegalState(null);

		if(sym.disableAllTextIndex)
			throw makeIllegalState(null);
		
		if(sym.pastIndex.index > sym.startIndex.index)
			stringLen = sym.pastIndex.index - sym.startIndex.index;
		
		if(stringLen > 1000) {
			throw makeIllegalState(null);
		}

		srcDat.strRead.readUtf32Throw(sym.startIndex, stringLen);

		if(strDat.state != 
			CharReaderData.STATE_HAVE_BUFFER_STRING) {
			
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}
		
		str = StringUtils.int32StringFromUtf32(strDat.resultBufferString);
		
		if(true) {
			return str;
		}
		
		return makeInt32Array(0);
	}

	private long getSymbolStartPos(Symbol sym) {
		if(sym.disableAllTextIndex) throw makeObjectNotFound(null);
		return sym.startIndex.index;
	}
	

	// core logic trim stuff
	//

	private boolean maybeDoTrim(GramReader gramRead, boolean forceTrim) {
		LrGramReaderData grmDat = (LrGramReaderData) gramRead.getData();
		LrStack theLrStack = grmDat.lrStack;
		LrAheadCircleQueue theLrAhead = grmDat.lrAhead;

		SymbolAllocHelper allocHelp1 = srcDat.allocHelp;
		CommonInt32Array trimPositions1 = srcDat.trimPositions;
		SymbolAllocHelper allocHelp2 = srcDat.allocHelp2;
		CommonInt32Array trimPositions2 = srcDat.trimPositions2;
		
		CommonInt32Array currentTrimPositions = srcDat.currentTrimPositions;

		
		CFamilyTrimStat stat;
		boolean ok;
		int visitedLen;
		int i;
		int stackLen;
		float ratio;
		
		ratio = 0.0f;
		
		ok = true;
				
		if(false) {
			// enable this block, to disables trim-ing completely
			
			ok = false;
		}

		if(ok)
		if(!forceTrim)
		if(true) {
			ok = false;
			dat.trimWaitNum += 1;
			
			if(dat.trimWaitNum >= dat.TRIM_WAIT_MAX) {
				dat.trimWaitNum = 0;
				ok = true;
			}
		}
		
		stat = null;
		
		if(!forceTrim)
		if(ok) {
			visitedLen = theLrStack.clientVisitedLen;
			i = visitedLen;
			stackLen = theLrStack.len;
			while(i < stackLen) {
				stat = getTrimStatObject(dat.trimStatStack, i);
				stat.canTrim = 0;
				stat.shouldNotTrim = 0;

				LrStackEntry ent = (LrStackEntry) theLrStack.stack.get(i);
				calcTrimStats(stat, ent.sym, false);

				i += 1;
			}

			addTrimStats(dat.trimStatStack, stackLen);
			stat = getTrimStatObject(dat.trimStatStack, stackLen);

			theLrStack.clientVisitedLen = stackLen;

			ratio = ratioTwoInt32(stat.canTrim, stat.shouldNotTrim);
			if(ratio < 0.66f) ok = false;
		}
		
		if(ok) {
			// copy the data to a temporary place (allocHelp2),
			// then reset the cursor,
			// then copy the data back
			
			allocHelp1.getCursorPositions(currentTrimPositions);
			int count1 = getCursorPositionsSum(currentTrimPositions);
			
			
			// do trim
			//
			
			gramRead.trim(dat.symReAllocUtils);
			srcDat.tokenRead.trim(dat.symReAllocUtils);
			srcDat.keywordRead.trim(dat.symReAllocUtils);
			srcDat.commentRemoveRead.trim(dat.symReAllocUtils);
			
			
			// do re-alloc
			//
			
			gramRead.reAlloc(allocHelp2, dat.symReAllocUtils);
			srcDat.tokenRead.reAlloc(allocHelp2, dat.symReAllocUtils);
			srcDat.keywordRead.reAlloc(allocHelp2, dat.symReAllocUtils);
			srcDat.commentRemoveRead.reAlloc(allocHelp2, dat.symReAllocUtils);

			allocHelp1.setCursorPositions(trimPositions1);
			
			gramRead.reAlloc(allocHelp1, dat.symReAllocUtils);
			srcDat.tokenRead.reAlloc(allocHelp1, dat.symReAllocUtils);
			srcDat.keywordRead.reAlloc(allocHelp1, dat.symReAllocUtils);
			srcDat.commentRemoveRead.reAlloc(allocHelp1, dat.symReAllocUtils);

			allocHelp2.setCursorPositions(trimPositions2);
			
			
			
			allocHelp1.getCursorPositions(currentTrimPositions);
			int count2 = getCursorPositionsSum(currentTrimPositions);

			if(false) {
				printLine("" + true + "," + count1 + "," + count2);
			}
		}

		if(!ok) {
			allocHelp1.getCursorPositions(currentTrimPositions);
			int count2 = getCursorPositionsSum(currentTrimPositions);

			if(false) {
				printLine("" + false + "," + count2 + "," + ratio);
			}
		}
		
		return ok;
	}
	
	private void calcTrimStats(
		CFamilyTrimStat stat, Symbol sym, boolean shouldTrim) {
		
		Symbol childSym;
		int childCount;
		int i;

		boolean trimThisFunction = false;
		if(isFunctionGram(sym)) trimThisFunction = true;
		
		if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
			GramContainer grmCon = (GramContainer) sym;
			
			childCount = dat.utils.getGramChildCount(grmCon);
			i = 0;
			
			if(!trimThisFunction)
			while(i < childCount) {
				childSym = grmCon.sym[i];
				if(childSym != null) {
					calcTrimStats(stat, childSym, shouldTrim);
				}
				
				i += 1;
			}

			if(trimThisFunction)
			while(i < childCount) {
				childSym = grmCon.sym[i];
				if(childSym != null) {
					int id2 = dat.utils.getSymbolIdPrimary(childSym);
					
					boolean trimFunctionStatement = 
						id2 == Symbols.GRAM_STATEMENT_BLOCK;
					if(trimFunctionStatement)
						calcTrimStats(stat, childSym, true);
					if(!trimFunctionStatement)
						calcTrimStats(stat, childSym, shouldTrim);
				}
				
				i += 1;
			}
		}

		if(shouldTrim) stat.canTrim += 1;
		if(!shouldTrim) stat.shouldNotTrim += 1;
		return;
	}

	private void addTrimStats(CommonArrayList store, int len) {
		int i;
		
		CFamilyTrimStat totalStat = getTrimStatObject(store, len);
		totalStat.canTrim = 0;
		totalStat.shouldNotTrim = 0;
		
		i = 0;
		while(i < len) {
			CFamilyTrimStat stat = getTrimStatObject(store, i);
			
			totalStat.canTrim += stat.canTrim;
			totalStat.shouldNotTrim += stat.shouldNotTrim;
			
			i += 1;
		}
		
		return;
	}
	
	
	// core logic file output
	//

	private boolean writeStr(CommonInt32Array str) {
		boolean isError = false;
		int state;

		StringWriter3Data trgStrDat = (StringWriter3Data) trgDat.strWrite.getData();

		if(!isError) {
			if(str == null) return isError;
			if(str.length == 0) return isError;
		}
		
		if(!isError) {
			trgStrDat.resultString = str;

			trgDat.strWrite.writeInt32String(null);
			state = trgStrDat.state;

			if(state != StringWriter3Data.STATE_WROTE_STRING) {
				printLine("Bad string write result: state="
					+ state);
				isError = true;
			}
		}

		return isError;
	}
	
	private boolean symbolNeedsLineReturnAfter(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);
		
		if(isFunctionGram(sym)) return true;
		//if(isVariableGram(sym)) return true;
		if(isHeaderGram(sym)) return true;
		if(isModuleGram(sym)) return true;
		
		return false;
	}

	private boolean symbolNeedsSpaceBefore(Symbol sym) {
		return true;
	}
	
	private Symbol getGramChild(Symbol sym, int index) {
		GramContainer grmCon = (GramContainer) sym;
		return grmCon.sym[index];
	}
	
	private boolean hasIdentifierJavaInt8(Symbol sym) {
		int i;
		int len;
		
		int id = dat.utils.getSymbolIdPrimary(sym);

		CompareParams compRec = makeCompareParams();
		
		if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
			GramContainer grmCon = (GramContainer) sym;
			i = 0;
			len = dat.utils.getGramChildCount(grmCon);
			while(i < len) {
				if(hasIdentifierJavaInt8(grmCon.sym[i]))
					return true;
				i += 1;
			}
		}
		
		if(id == Symbols.TOKEN_IDENTIFIER) {
			CommonInt32Array str = getSymbolString2(sym);
			
			StringUtils.int32StringCompareSimple(str, KEYWORD_JAVA_BYTE, compRec);
			if(compareIsEqual(compRec)) return true;
		}
		
		return false;
	}
	
	private boolean hasIdentifierJavaInt16(Symbol sym) {
		int i;
		int len;
		
		int id = dat.utils.getSymbolIdPrimary(sym);

		CompareParams compRec = makeCompareParams();
		
		if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
			GramContainer grmCon = (GramContainer) sym;
			i = 0;
			len = dat.utils.getGramChildCount(grmCon);
			while(i < len) {
				if(hasIdentifierJavaInt16(grmCon.sym[i]))
					return true;
				i += 1;
			}
		}
		
		if(id == Symbols.TOKEN_IDENTIFIER) {
			CommonInt32Array str = getSymbolString2(sym);
			
			StringUtils.int32StringCompareSimple(str, KEYWORD_JAVA_SHORT, compRec);
			if(compareIsEqual(compRec)) return true;
		}
		
		return false;
	}
	
	private boolean hasIdentifierJavaInt32(Symbol sym) {
		int i;
		int len;
		
		int id = dat.utils.getSymbolIdPrimary(sym);

		CompareParams compRec = makeCompareParams();
		
		if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
			GramContainer grmCon = (GramContainer) sym;
			i = 0;
			len = dat.utils.getGramChildCount(grmCon);
			while(i < len) {
				if(hasIdentifierJavaInt32(grmCon.sym[i]))
					return true;
				i += 1;
			}
		}
		
		if(id == Symbols.TOKEN_IDENTIFIER) {
			CommonInt32Array str = getSymbolString2(sym);
			
			StringUtils.int32StringCompareSimple(str, KEYWORD_JAVA_INT, compRec);
			if(compareIsEqual(compRec)) return true;
		}
		
		return false;
	}
	
	private boolean hasIdentifierJavaInt64(Symbol sym) {
		int i;
		int len;
		
		int id = dat.utils.getSymbolIdPrimary(sym);

		CompareParams compRec = makeCompareParams();
		
		if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
			GramContainer grmCon = (GramContainer) sym;
			i = 0;
			len = dat.utils.getGramChildCount(grmCon);
			while(i < len) {
				if(hasIdentifierJavaInt64(grmCon.sym[i]))
					return true;
				i += 1;
			}
		}
		
		if(id == Symbols.TOKEN_IDENTIFIER) {
			CommonInt32Array str = getSymbolString2(sym);
			
			StringUtils.int32StringCompareSimple(str, KEYWORD_JAVA_LONG, compRec);
			if(compareIsEqual(compRec)) return true;
		}
		
		return false;
	}
	
	private boolean writeSymbolInParen(Symbol sym) {
		if(writeStr1("(")) return true;
		if(writeSymbol2(sym)) return true;
		if(writeStr1(")")) return true;
		return false;
	}
	
	private boolean writeSymbol2(Symbol sym) {
		boolean ok;
		Symbol child;

		if(false) {
			return writeSymbol(sym);
		}
		
		CompareParams compRec = makeCompareParams();
		
		int id = dat.utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.TOKEN_KEYWORD_STATIC) return false;
		
		if(false) {
			if(id == Symbols.GRAM_EXPRESSION_ALLOCATION_ARRAY) {
				if(writeSpace()) return true;

				ok = false;
				child = getGramChild(sym, 1);

				if(!ok)
				if(hasIdentifierJavaInt8(child)) {
					writeStr1(" makeInt8Array");
					writeSymbolInParen(getGramChild(getGramChild(sym, 2), 1));
					ok = true;
				}

				if(!ok)
				if(hasIdentifierJavaInt16(child)) {
					writeStr1(" makeInt16Array");
					writeSymbolInParen(getGramChild(getGramChild(sym, 2), 1));
					ok = true;
				}

				if(!ok)
				if(hasIdentifierJavaInt32(child)) {
					writeStr1(" makeInt32Array");
					writeSymbolInParen(getGramChild(getGramChild(sym, 2), 1));
					ok = true;
				}

				if(!ok)
				if(hasIdentifierJavaInt64(child)) {
					writeStr1(" makeInt64Array");
					writeSymbolInParen(getGramChild(getGramChild(sym, 2), 1));
					ok = true;
				}

				//if(!ok) {
				//	if(writeSymbol2(getGramChild(sym, 0))) return true;
				//	if(writeSymbol2(child)) return true;
				//	if(writeSymbol2(getGramChild(sym, 2))) return true;
				//	return false;
				//}

				if(ok) return false;
			}
		}
		
		if(false) {
			if(id == Symbols.GRAM_EXPRESSION_ARRAY_ACCESS) {
				//printLine("array access," + getSymbolString(sym));
				if(writeSpace()) return true;
				if(writeSymbol2(getGramChild(sym, 0))) return true;
				if(writeStr(StringUtils.int32StringFromJavaString(" . arrPtr")))
					return true;
				if(writeSymbol2(getGramChild(sym, 1)))
					return true;
				return false;
			}
		}
		

		if(false) {
			if(id == Symbols.GRAM_VARIABLE_DEF_SIMPLE) {
				Symbol sym2 = getGramChild(sym, 0);
				int id2 = dat.utils.getSymbolIdPrimary(sym2);
				if(id2 == Symbols.GRAM_EXPRESSION_ARRAY_TYPE) {
					if(hasIdentifierJavaInt8(sym2)) {
						dat.shouldPrintTestFileName = true;
					}

					if(hasIdentifierJavaInt16(sym2)) {
						dat.shouldPrintTestFileName = true;
					}

					if(hasIdentifierJavaInt32(sym2)) {
						dat.shouldPrintTestFileName = true;
					}

					if(hasIdentifierJavaInt64(sym2)) {
						dat.shouldPrintTestFileName = true;
					}
				}
			}
		}
		
		if(false) {
			if(id == Symbols.GRAM_VARIABLE_DEF_SIMPLE) {
				Symbol sym2 = getGramChild(sym, 0);
				int id2 = dat.utils.getSymbolIdPrimary(sym2);
				if(id2 == Symbols.GRAM_EXPRESSION_ARRAY_TYPE) {
					if(hasIdentifierJavaInt8(sym2)) {
						if(writeStr1(" c_ArrayOf_p2n_Int8")) return true;
						if(writeSymbol2(getGramChild(sym, 1))) return true;
						return false;
					}

					if(hasIdentifierJavaInt16(sym2)) {
						if(writeStr1(" c_ArrayOf_p2n_Int16")) return true;
						if(writeSymbol2(getGramChild(sym, 1))) return true;
						return false;
					}

					if(hasIdentifierJavaInt32(sym2)) {
						if(writeStr1(" c_ArrayOf_p2n_Int32")) return true;
						if(writeSymbol2(getGramChild(sym, 1))) return true;
						return false;
					}

					if(hasIdentifierJavaInt64(sym2)) {
						if(writeStr1(" c_ArrayOf_p2n_Int64")) return true;
						if(writeSymbol2(getGramChild(sym, 1))) return true;
						return false;
					}
				}
				//dumpSymbol();
				//return true;
			}
		}

		if(id == Symbols.TOKEN_IDENTIFIER) {
			if(hasIdentifierJavaInt8(sym)) {
				if(writeSpace()) return true;
				return writeStr(KEYWORD_C_INT8);
			}

			if(hasIdentifierJavaInt16(sym)) {
				if(writeSpace()) return true;
				return writeStr(KEYWORD_C_INT16);
			}

			if(hasIdentifierJavaInt32(sym)) {
				if(writeSpace()) return true;
				return writeStr(KEYWORD_C_INT32);
			}

			if(hasIdentifierJavaInt64(sym)) {
				if(writeSpace()) return true;
				return writeStr(KEYWORD_C_INT64);
			}
		}
		
		return writeSymbol(sym);
	}
	
	private boolean writeToken2(Symbol sym) {
		boolean isError = false;
		
		int id;
		
		boolean needsSpace;
		boolean needsLineReturnAfter;
		
		if(sym == null) return isError;
		StringReader2Data srcStrDat = (StringReader2Data) srcDat.strRead.getData();
		
		id = dat.utils.getSymbolIdPrimary(sym);
		//printLine("Writing token,id," + id + "," + getSymbolString(sym));
		
		if(sym.symbolType != SymbolTypes.TYPE_TOKEN)
			throw makeIllegalState(null);
		
		needsLineReturnAfter = symbolNeedsLineReturnAfter(sym);
		needsSpace = symbolNeedsSpaceBefore(sym);

		if(needsSpace)
		if(trgDat.whitespaceState
			== TranslateOldTest1LogicWriteData.STATE_WAS_SOMETHING_IN_LINE)
			writeSpace();

		isError = writeStr(getSymbolString2(sym));
		trgDat.whitespaceState
			= TranslateOldTest1LogicWriteData.STATE_WAS_SOMETHING_IN_LINE;

		if(needsLineReturnAfter)
		if(trgDat.whitespaceState
			== TranslateOldTest1LogicWriteData.STATE_WAS_SOMETHING_IN_LINE) {

			writeLineReturn(id);
			trgDat.whitespaceState
				= TranslateOldTest1LogicWriteData.STATE_WAS_NOTHING;
		}
		
		return isError;
	}

	private boolean writeGramContainer2(Symbol sym) {
		boolean isError = false;
		int i;
		int count;
		
		boolean needsSpace;
		boolean needsLineReturnAfter;
		
		GramContainer grmCon;
		int id;
		
		if(sym == null) return isError;
		
		StringReader2Data srcStrDat = (StringReader2Data) srcDat.strRead.getData();
		
		if(sym.symbolType != SymbolTypes.TYPE_GRAM)
			throw makeIllegalState(null);

		if(sym.symbolStorageType != SymbolStorageTypes.TYPE_GRAM_CONTAINER)
			throw makeIllegalState(null);
		
		grmCon = (GramContainer) sym;
		
		needsLineReturnAfter = symbolNeedsLineReturnAfter(sym);

		id = dat.utils.getSymbolIdPrimary(sym);
		//printLine("Writing GramContainer,id," + id);

		if(grmCon.sym == null) return isError;

		i = 0;
		count = dat.utils.getGramChildCount(grmCon);
		while(i < count) {
			isError = writeSymbol2(grmCon.sym[i]);
			if(isError) return isError;

			i += 1;
		}

		if(needsLineReturnAfter)
		if(trgDat.whitespaceState
			== TranslateOldTest1LogicWriteData.STATE_WAS_SOMETHING_IN_LINE) {

			writeLineReturn(id);
			trgDat.whitespaceState
				= TranslateOldTest1LogicWriteData.STATE_WAS_NOTHING;
		}

		return isError;
	}
		
	private boolean writeGramContainerList2(Symbol sym) {
		boolean isError = false;
		int i;
		int count;
		
		boolean needsSpace;
		boolean needsLineReturnAfter;
		
		GramContainerList grmConList;
		int id;

		needsLineReturnAfter = symbolNeedsLineReturnAfter(sym);
		needsSpace = symbolNeedsSpaceBefore(sym);
		
		if(sym.symbolType != SymbolTypes.TYPE_GRAM)
			throw makeIllegalState(null);
		
		if(sym.symbolStorageType != SymbolStorageTypes.TYPE_GRAM_CONTAINER)
			throw makeIllegalState(null);
		
		grmConList = (GramContainerList) sym;
		id = dat.utils.getSymbolIdPrimary(sym);
		
		if(grmConList.symList == null) return isError;

		i = 0;
		count = 0;
		if(grmConList.symList != null) count = grmConList.symList.size();
		while(i < count) {
			isError = writeSymbol2((Symbol) grmConList.symList.get(i));
			if(isError) return isError;

			i += 1;
		}

		if(needsLineReturnAfter)
		if(trgDat.whitespaceState
			== TranslateOldTest1LogicWriteData.STATE_WAS_SOMETHING_IN_LINE) {

			writeLineReturn(id);
			trgDat.whitespaceState
				= TranslateOldTest1LogicWriteData.STATE_WAS_NOTHING;
		}

		return isError;
	}
	
	private boolean writeSymbol(Symbol sym) {
		boolean isError = false;

		if(sym == null) return isError;
		
		if(sym.symbolType == SymbolTypes.TYPE_TOKEN) {
			return writeToken2(sym);
		}
		
		if(sym.symbolType == SymbolTypes.TYPE_GRAM) {
			if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER)
				return writeGramContainer2(sym);

			if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST)
				return writeGramContainerList2(sym);

			throw makeObjectUnexpected(null);
		}

		throw makeObjectUnexpected(null);
	}
	
	private boolean writeLineReturn(int id) {
		if(writeStr(dat.lineReturnStr)) return true;
		return false;
	}

	private boolean writeSpace() {
		return writeStr(dat.spaceStr);
	}
	
	
	// core logic run test file collect cfamily node tree
	//
	
	private boolean runTestFileCollectBasics() {
		boolean isError;
		
		isError = false;
		
		if(true) {
			CFamilyNodeWalkParams p = new CFamilyNodeWalkParams();

			p.majorAction = CFamilyNodeWalkParams.ACTION_COLLECT_NAMES;
			collectNames(p, dat.mainChunk, null, srcDat.mostRecentParseSym);
			collectClearWalkParams(p);

			p.majorAction = CFamilyNodeWalkParams.ACTION_COLLECT_TYPES;
			collectNames(p, dat.mainChunk, null, srcDat.mostRecentParseSym);
			collectClearWalkParams(p);
		}
		
		return isError;
	}
	
	private void initCurrentCFamilyNode() {
		CFamilyNode2 n;
		
		n = null;
	}
	
	private CFamilyNode2 getCurrentCFamilyNode() {
		return srcDat.currentCFamilyNode;
	}
	
	private boolean isSymbolMatchForCurrentCFamilyNode(Symbol sym) {
		CFamilyNode2 n = srcDat.currentCFamilyNode;
		
		if(n == null) return false;
				
		if(sym.disableAllTextIndex)
			throw makeIllegalState(null);
		
		// check for exact position
		
		long parseSymIndex = sym.startIndex.index;
		long cfamilyNodeIndex = srcDat.currentCFamilyNode.startPos;
		
		if(parseSymIndex > cfamilyNodeIndex)
			throw makeIllegalState(null);

		if(parseSymIndex < cfamilyNodeIndex) return false;
		
		// position is correct
		
		if(n.theType == CFamilyNodeTypes.TYPE_DEFINITION) {
			if(n.theSubType == CFamilyNodeTypes.TYPE_FUNCTION) {
				if(isFunctionGram(sym)) {
					// found FUNCTION match
					return true;
				}
			}

			if(n.theSubType == CFamilyNodeTypes.TYPE_VARIABLE) {
				if(isVariableGram(sym)) {
					// found VARIABLE match
					return true;
				}
			}
		}
		
		return false;
	}

	private void collectClearWalkParams(CFamilyNodeWalkParams p) {
		p.existsContent = false;
		p.packageForFile = null;
		p.majorAction = 0;
		p.extensionVariety = 0;
	}
	
	private void collectNames(
		CFamilyNodeWalkParams p,
		CFamilyNode2 namespaceNode,
		CommonInt32Array packageStr,
		Symbol sym) {

		int id;
		int catId;
		CommonInt32Array name;
		CommonInt32Array packageStr2;

		CFamilyNodeUtils nameNodeUtils = dat.nameNodeUtils;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		catId = dat.utils.getSymbolIdCategory(sym);

		if(id == Symbols.SYMBOL_UNSPECIFIED) {
			if(sym.symbolStorageType == SymbolStorageTypes.TYPE_GRAM_CONTAINER) {
				collectNamesFromGramContainer(
					p, namespaceNode, packageStr,
					sym);
				return;
			}
		}
		
		if(id == Symbols.GRAM_SEQUENCE) {
			collectNamesFromGramContainer(
				p, namespaceNode, packageStr,
				sym);
			return;
		}
		
		if(catId == Symbols.GRAM_CATEGORY_BLOCKS) {
			collectNamesFromBlock(
				p, namespaceNode, packageStr,
				sym);
			return;
		}

		if(id == Symbols.GRAM_PACKAGE_DEF) {
			Symbol sym2 = getGramChild(sym,
				fromPackageSymbolGetBodyIndex(sym));
			int id2 = dat.utils.getSymbolIdPrimary(sym2);
			
			if(id2 == Symbols.TOKEN_SEMICOLON) {
				if(!getIsTypeMatchLen2(namespaceNode,
					CFamilyNodeTypes.TYPE_DEFINITION,
					CFamilyNodeTypes.TYPE_CHUNK_NAMESPACE))
					throw makeObjectUnexpected(null);
				
				if(packageStr != null
					|| p.packageForFile != null
					|| p.existsContent)
					throw makeObjectUnexpected(null);
				
				packageStr2 = fromPackageSymbolGetPath(sym);

				if(false) {
					printLine("package,"
						+ StringUtils.javaStringFromInt32String(
							packageStr2));
				}

				CFamilyNode2 packageNode = nameNodeUtils.getPackage(
					namespaceNode, packageStr2);

				if(packageNode != null) {
					p.packageForFile = packageNode;
					return;
				}
				
				packageNode = nameNodeUtils.makePackage(
					namespaceNode, packageStr2);
				
				p.packageForFile = packageNode;
				return;
			}

			if(id2 == Symbols.GRAM_PACKAGE_BLOCK
				|| id2 == Symbols.GRAM_PACKAGE_BLOCK_EMPTY) {
				
				if(p.packageForFile != null)
					throw makeObjectUnexpected(null);
				
				packageStr2 = fromPackageSymbolGetPath(sym);
				
				CommonInt32Array packageStr3
					= PathUtils.combine2OptionalPaths(
						packageStr, packageStr2, ',');

				if(false) {
					printLine("package,"
						+ StringUtils.javaStringFromInt32String(
							packageStr3));
				}

				CFamilyNode2 packageNode = nameNodeUtils.getPackage(
					namespaceNode, packageStr3);

				if(packageNode == null) {
					packageNode = nameNodeUtils.makePackage(
						namespaceNode, packageStr3);
				}
				
				dat.indentCount += 1;
				collectNamesFromBlock(p,
					namespaceNode,
					packageStr3,
					sym2);
				dat.indentCount -= 1;
				return;
			}

			throw makeObjectUnexpected(null);
		}
			
		if(isClassGram(sym)) {
			// create/get the package
			
			CFamilyNode2 packageNode;
			
			packageNode = null;
			
			if(packageNode == null) {
				if(p.packageForFile != null)
					packageNode = p.packageForFile;
			}

			if(packageNode == null) {
				packageStr2 = packageStr;
				if(packageStr2 == null)
					packageStr2 = nameNodeUtils.getDefaultPackagePath();

				packageNode = nameNodeUtils.getPackage(
					namespaceNode, packageStr2);

				if(packageNode == null) {
					packageNode = nameNodeUtils.makePackage(
						namespaceNode, packageStr2);
				}
			}
			
			
			// add the class

			name = fromClassSymbolGetName(sym);

			if(false) {
				printLine("class,"
					+ StringUtils.javaStringFromInt32String(
						name));
			}

			CFamilyNode2 classNode = nameNodeUtils.getClass(
				packageNode, name);
			
			if(p.majorAction == CFamilyNodeWalkParams.ACTION_COLLECT_NAMES) {
				if(classNode != null) 
					throw makeObjectUnexpected(null);

				classNode = nameNodeUtils.makeClass(
					packageNode, name);
				classNode.startPos = getSymbolStartPos(sym);
				p.existsContent = true;


				// stuff within a class
				//

				dat.indentCount += 1;
				collectNames(
					p, classNode, packageStr,
					getGramChild(sym,
						fromClassSymbolGetBodyIndex(sym)));
				dat.indentCount -= 1;
			}

			if(p.majorAction == CFamilyNodeWalkParams.ACTION_COLLECT_TYPES) {
				if(classNode == null) 
					throw makeObjectNotFound(null);
				p.existsContent = true;
				
				dat.indentCount += 1;
				collectRelationsNamesAndTypes(
					p, classNode, packageStr,
					getGramChild(sym,
						fromClassSymbolGetRelationsIndex(sym)));
				dat.indentCount -= 1;
			}

			return;
		}

		if(isInterfaceGram(sym)) {
			// create/get the package
			
			CFamilyNode2 packageNode;
			
			packageNode = null;
			
			if(packageNode == null) {
				if(p.packageForFile != null)
					packageNode = p.packageForFile;
			}

			if(packageNode == null) {
				packageStr2 = packageStr;
				if(packageStr2 == null)
					packageStr2 = nameNodeUtils.getDefaultPackagePath();

				packageNode = nameNodeUtils.getPackage(
					namespaceNode, packageStr2);

				if(packageNode == null) {
					packageNode = nameNodeUtils.makePackage(
						namespaceNode, packageStr2);
				}
			}
			
			
			// add the interface

			name = fromClassSymbolGetName(sym);

			if(false) {
				printLine("interface,"
					+ StringUtils.javaStringFromInt32String(
						name));
			}

			CFamilyNode2 intrNode = nameNodeUtils.getInterface(
				packageNode, name);
			
			if(p.majorAction == CFamilyNodeWalkParams.ACTION_COLLECT_NAMES) {
				if(intrNode != null) 
					throw makeObjectUnexpected(null);

				intrNode = nameNodeUtils.makeInterface(
					packageNode, name);
				intrNode.startPos = getSymbolStartPos(sym);
				p.existsContent = true;


				// stuff within a class
				//

				dat.indentCount += 1;
				collectNames(
					p, intrNode, packageStr,
					getGramChild(sym,
						fromClassSymbolGetBodyIndex(sym)));
				dat.indentCount -= 1;
			}

			if(p.majorAction == CFamilyNodeWalkParams.ACTION_COLLECT_TYPES) {
				if(intrNode == null) 
					throw makeObjectNotFound(null);
				p.existsContent = true;
				
				dat.indentCount += 1;
				collectRelationsNamesAndTypes(
					p, intrNode, packageStr,
					getGramChild(sym,
						fromClassSymbolGetRelationsIndex(sym)));
				dat.indentCount -= 1;
			}

			return;
		}

		if(isFunctionGram(sym)) {
			name = fromFunctionSymbolGetName(sym);

			if(false) {
				printLine("function,"
					+ StringUtils.javaStringFromInt32String(
						name));
			}

			if(p.majorAction == CFamilyNodeWalkParams.ACTION_COLLECT_TYPES) {
				p.existsContent = true;
				return;
			}

			CFamilyNode2 funcNode = nameNodeUtils.getFunction(
				namespaceNode, name);

			if(funcNode != null) 
				throw makeObjectUnexpected(null);

			funcNode = nameNodeUtils.makeFunction(
				namespaceNode, name);
			funcNode.startPos = getSymbolStartPos(sym);
			funcNode.typeName = fromFunctionSymbolGetPrimaryTypeName(sym);
			p.existsContent = true;
			return;
		}

		if(isVariableGram(sym)) {
			name = fromVariableSymbolGetName(sym);

			if(false) {
				printLine("variable,"
					+ StringUtils.javaStringFromInt32String(
						name));
			}

			if(p.majorAction == CFamilyNodeWalkParams.ACTION_COLLECT_TYPES) {
				p.existsContent = true;
				return;
			}

			CFamilyNode2 varNode = nameNodeUtils.getVariable(
				namespaceNode, name);

			if(varNode != null) 
				throw makeObjectUnexpected(null);

			varNode = nameNodeUtils.makeVariable(
				namespaceNode, name);
			varNode.startPos = getSymbolStartPos(sym);
			varNode.typeName = fromVariableSymbolGetPrimaryTypeName(sym);
			p.existsContent = true;
			return;
		}

		if(id == Symbols.GRAM_IMPORT_DEF) {
			return;
		}
		
		printLine("symUnexpected," + id);
		printLine("index," + getIndexString(sym.startIndex));
		
		throw makeObjectUnexpected(null);
	}

	private void collectNamesFromGramContainer(
		CFamilyNodeWalkParams p,
		CFamilyNode2 namespaceNode,
		CommonInt32Array packageStr,
		Symbol sym) {
		
		int i;
		int len;

		GramContainer grmCon = (GramContainer) sym;

		i = 0;
		len = dat.utils.getGramChildCount(grmCon);
		while(i < len) {
			collectNames(p, namespaceNode, packageStr, grmCon.sym[i]);
			i += 1;
		}
		
		return;
	}

	private void collectNamesFromBlock(
		CFamilyNodeWalkParams p,
		CFamilyNode2 namespaceNode,
		CommonInt32Array packageStr,
		Symbol sym) {

		GramContainer grmCon;
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		
		grmCon = null;
		
		if(id == Symbols.GRAM_STATEMENT_BLOCK
			|| id == Symbols.GRAM_VARIABLE_BLOCK
			|| id == Symbols.GRAM_CLASS_BLOCK
			|| id == Symbols.GRAM_PACKAGE_BLOCK) {
			grmCon = (GramContainer) sym;
		}
		
		if(grmCon != null) {
			collectNames(p, namespaceNode, packageStr, grmCon.sym[1]);
			return;
		}

		if(id == Symbols.GRAM_STATEMENT_BLOCK_EMPTY
			|| id == Symbols.GRAM_VARIABLE_BLOCK_EMPTY
			|| id == Symbols.GRAM_CLASS_BLOCK_EMPTY
			|| id == Symbols.GRAM_PACKAGE_BLOCK_EMPTY) {
			grmCon = (GramContainer) sym;
		}
		
		if(grmCon != null) {
			return;
		}
		
		throw makeObjectUnexpected(null);
	}

	private void collectRelationsNamesAndTypes(
		CFamilyNodeWalkParams p,
		CFamilyNode2 namespaceNode,
		CommonInt32Array packageStr,
		Symbol sym) {
		
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_SEQUENCE) {
			collectRelationsNamesFromGramContainer(
				p, namespaceNode, packageStr, sym);
			return;
		}
		
		if(id == Symbols.GRAM_MODULE_EXTENDS) {
			p.extensionVariety = CFamilyNodeWalkParams.EXTENSION_VARIETY_EXTENDS;
			collectExtensionVarietyNamesAndTypes(
				p, namespaceNode, packageStr, getGramChild(sym, 1));
			return;
		}

		if(id == Symbols.GRAM_MODULE_IMPLEMENTS) {
			p.extensionVariety = CFamilyNodeWalkParams.EXTENSION_VARIETY_IMPLEMENTS;
			collectExtensionVarietyNamesAndTypes(
				p, namespaceNode, packageStr, getGramChild(sym, 1));
			return;
		}

		if(id == Symbols.GRAM_MODULE_EXTENDS_AND_IMPLEMENTS) {
			p.extensionVariety = CFamilyNodeWalkParams.EXTENSION_VARIETY_EXTENDS_AND_IMPLEMENTS;
			collectExtensionVarietyNamesAndTypes(
				p, namespaceNode, packageStr, getGramChild(sym, 1));
			return;
		}

		throw makeObjectUnexpected(null);
	}

	private void collectRelationsNamesFromGramContainer(
		CFamilyNodeWalkParams p,
		CFamilyNode2 namespaceNode,
		CommonInt32Array packageStr,
		Symbol sym) {
		
		int i;
		int len;

		GramContainer grmCon = (GramContainer) sym;

		i = 0;
		len = dat.utils.getGramChildCount(grmCon);
		while(i < len) {
			collectRelationsNamesAndTypes(
				p, namespaceNode, packageStr, grmCon.sym[i]);
			i += 1;
		}
		
		return;
	}

	private void collectExtensionVarietyNamesAndTypes(
		CFamilyNodeWalkParams p,
		CFamilyNode2 namespaceNode,
		CommonInt32Array packageStr,
		Symbol sym) {
		
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		
		boolean isRegularExtends;
		
		if(id == Symbols.TOKEN_IDENTIFIER) {
			if(false) {
				if(p.extensionVariety == CFamilyNodeWalkParams.EXTENSION_VARIETY_EXTENDS)
				printLine("extends,"
					+ StringUtils.javaStringFromInt32String(
						getSymbolString2(sym)));

				if(p.extensionVariety == CFamilyNodeWalkParams.EXTENSION_VARIETY_IMPLEMENTS)
				printLine("implements,"
					+ StringUtils.javaStringFromInt32String(
						getSymbolString2(sym)));

				if(p.extensionVariety == CFamilyNodeWalkParams.EXTENSION_VARIETY_EXTENDS_AND_IMPLEMENTS)
				printLine("extendsAndImplements,"
					+ StringUtils.javaStringFromInt32String(
						getSymbolString2(sym)));
			}
			
			isRegularExtends = false;
			if(p.extensionVariety == CFamilyNodeWalkParams.EXTENSION_VARIETY_EXTENDS)
				isRegularExtends = true;
			if(p.extensionVariety == CFamilyNodeWalkParams.EXTENSION_VARIETY_EXTENDS_AND_IMPLEMENTS)
				isRegularExtends = true;
			
			if(!isRegularExtends)
			if(p.extensionVariety != CFamilyNodeWalkParams.EXTENSION_VARIETY_IMPLEMENTS)
				throw makeInvalidEnum(null);

			CFamilyNode2 n;
			
			n = null;
			
			if(isRegularExtends)
			n = dat.nameNodeUtils.makeRelationExtends(
				namespaceNode);

			if(!isRegularExtends)
			n = dat.nameNodeUtils.makeRelationImplements(
				namespaceNode);

			n.typeName = getSymbolString2(sym);
			return;
		}

		if(id == Symbols.GRAM_EXPRESSION_SIMPLE) {
			collectExtensionVarietyNamesAndTypes(
				p, namespaceNode, packageStr, getGramChild(sym, 0));
			return;
		}

		if(id == Symbols.GRAM_EXPRESSION_BINARY) {
			int id2;
			
			id2 = dat.utils.getSymbolIdPrimary(getGramChild(sym, 1));
			if(id2 != Symbols.TOKEN_COMMA)
				throw makeObjectUnexpected(null);

			collectExtensionVarietyNamesAndTypes(
				p, namespaceNode, packageStr, getGramChild(sym, 0));
			collectExtensionVarietyNamesAndTypes(
				p, namespaceNode, packageStr, getGramChild(sym, 2));
			return;
		}

		printLine("symInExtensionVariety," + id);
		throw makeObjectUnexpected(null);
	}
	
	
	private int fromPackageSymbolGetIdentifierWalkIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_PACKAGE_DEF) return 1;
		
		throw makeObjectUnexpected(null);
	}
	
	private int fromPackageSymbolGetBodyIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_PACKAGE_DEF) return 2;
		
		throw makeObjectUnexpected(null);
	}
	
	private int fromClassSymbolGetNameIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_CLASS_DEF) return 1;
		if(id == Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS) return 2;
		
		throw makeObjectUnexpected(null);
	}
	
	private int fromClassSymbolGetRelationsIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_CLASS_DEF) return 2;
		if(id == Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS) return 3;
		
		throw makeObjectUnexpected(null);
	}
	
	private int fromClassSymbolGetBodyIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_CLASS_DEF) return 3;
		if(id == Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS) return 4;
		
		throw makeObjectUnexpected(null);
	}
	
	private int fromFunctionSymbolGetFunctionDefSimpleIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_FUNCTION_DEF_FULL) return 0;
		if(id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_INIT) return 0;
		if(id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS) return 1;
		if(id == Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT) return 1;

		throw makeObjectUnexpected(null);
	}

	private int fromFunctionSimpleSymbolGetVariableDefSimpleIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_FUNCTION_DEF_SIMPLE) return 0;

		throw makeObjectUnexpected(null);
	}

	private int fromVariableSymbolGetVariableSimpleIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_VARIABLE_DEF_FULL) return 0;
		if(id == Symbols.GRAM_VARIABLE_DEF_FULL_WITH_INIT) return 0;
		if(id == Symbols.GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS) return 1;
		if(id == Symbols.GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS_WITH_INIT) return 1;

		throw makeObjectUnexpected(null);
	}

	private int fromVariableSimpleSymbolGetNameIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_VARIABLE_DEF_SIMPLE) return 1;

		throw makeObjectUnexpected(null);
	}

	private int fromVariableSimpleSymbolGetTypeIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_VARIABLE_DEF_SIMPLE) return 0;

		throw makeObjectUnexpected(null);
	}

	private int fromArrayTypeSymbolGetInnerTypeIndex(Symbol sym) {
		int id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_EXPRESSION_ARRAY_TYPE) return 0;

		throw makeObjectUnexpected(null);
	}

	private CommonInt32Array fromClassSymbolGetName(Symbol sym) {
		Symbol sym2;
		
		sym2 = sym;
		
		sym2 = getGramChild(sym2,
			fromClassSymbolGetNameIndex(sym2));
		if(isIdentifierToken(sym2))
			return getSymbolString2(sym2);
		
		printLine("symInClass," + dat.utils.getSymbolIdPrimary(sym2));
		throw makeObjectUnexpected(null);
	}

	private CommonInt32Array fromFunctionSymbolGetName(Symbol sym) {
		Symbol sym2;
		
		sym2 = sym;
		
		sym2 = getGramChild(sym2,
			fromFunctionSymbolGetFunctionDefSimpleIndex(sym2));
		sym2 = getGramChild(sym2,
			fromFunctionSimpleSymbolGetVariableDefSimpleIndex(sym2));
		sym2 = getGramChild(sym2,
			fromVariableSimpleSymbolGetNameIndex(sym2));
		if(isIdentifierToken(sym2))
			return getSymbolString2(sym2);
		
		printLine("symInFunction," + dat.utils.getSymbolIdPrimary(sym2));
		throw makeObjectUnexpected(null);
	}

	private CommonInt32Array fromFunctionSymbolGetPrimaryTypeName(Symbol sym) {
		Symbol sym2;
		
		sym2 = sym;
		
		sym2 = getGramChild(sym2,
			fromFunctionSymbolGetFunctionDefSimpleIndex(sym2));
		sym2 = getGramChild(sym2,
			fromFunctionSimpleSymbolGetVariableDefSimpleIndex(sym2));
		sym2 = getGramChild(sym2,
			fromVariableSimpleSymbolGetTypeIndex(sym2));

		while(true) {
			if(dat.utils.getSymbolIdPrimary(sym2)
				== Symbols.GRAM_EXPRESSION_SIMPLE) {
				
				sym2 = getGramChild(sym2, 0);
				continue;
			}

			if(dat.utils.getSymbolIdPrimary(sym2)
				== Symbols.GRAM_EXPRESSION_ARRAY_TYPE) {
				
				sym2 = getGramChild(sym2, 
					fromArrayTypeSymbolGetInnerTypeIndex(sym2));
				continue;
			}
			
			break;
		}
		
		if(isIdentifierToken(sym2))
			return getSymbolString2(sym2);
		
		printLine("symInFunction," + dat.utils.getSymbolIdPrimary(sym2));
		throw makeObjectUnexpected(null);
	}

	private CommonInt32Array fromVariableSymbolGetName(Symbol sym) {
		Symbol sym2;
		
		sym2 = sym;

		sym2 = getGramChild(sym2,
			fromVariableSymbolGetVariableSimpleIndex(sym2));
		sym2 = getGramChild(sym2,
			fromVariableSimpleSymbolGetNameIndex(sym2));
		if(isIdentifierToken(sym2))
			return getSymbolString2(sym2);
		
		printLine("symInVariable," + dat.utils.getSymbolIdPrimary(sym2));
		throw makeObjectUnexpected(null);
	}

	private CommonInt32Array fromVariableSymbolGetPrimaryTypeName(Symbol sym) {
		Symbol sym2;
		
		sym2 = sym;
		
		sym2 = getGramChild(sym2,
			fromVariableSymbolGetVariableSimpleIndex(sym2));
		sym2 = getGramChild(sym2,
			fromVariableSimpleSymbolGetTypeIndex(sym2));

		while(true) {
			if(dat.utils.getSymbolIdPrimary(sym2)
				== Symbols.GRAM_EXPRESSION_SIMPLE) {
				
				sym2 = getGramChild(sym2, 0);
				continue;
			}

			if(dat.utils.getSymbolIdPrimary(sym2)
				== Symbols.GRAM_EXPRESSION_ARRAY_TYPE) {
				
				sym2 = getGramChild(sym2, 
					fromArrayTypeSymbolGetInnerTypeIndex(sym2));
				continue;
			}
			
			break;
		}

		if(isIdentifierToken(sym2))
			return getSymbolString2(sym2);
		
		printLine("symInVariable," + dat.utils.getSymbolIdPrimary(sym2));
		throw makeObjectUnexpected(null);
	}
	
	private CommonInt32Array fromPackageSymbolGetPath(Symbol sym) {
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);

		if(id == Symbols.GRAM_PACKAGE_DEF) {
			CommonArrayList list = CommonUtils.makeArrayList();
			fromIdentifierWalkLoadList(list,
				getGramChild(sym,
					fromPackageSymbolGetIdentifierWalkIndex(sym)));
			return PathUtils.combineManyPaths(list, ',');
		}

		printLine("symInPackage," + id);
		throw makeObjectUnexpected(null);
	}

	private void fromIdentifierWalkLoadList(
		CommonArrayList list, Symbol sym) {
		
		int id;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.TOKEN_IDENTIFIER) {
			list.add(getSymbolString2(sym));
			return;
		}

		if(id == Symbols.GRAM_EXPRESSION_SIMPLE) {
			fromIdentifierWalkLoadList(list, getGramChild(sym, 0));
			return;
		}

		if(id == Symbols.GRAM_EXPRESSION_BINARY) {
			int id2;
			
			id2 = dat.utils.getSymbolIdPrimary(getGramChild(sym, 1));
			if(id2 != Symbols.TOKEN_DOT)
				throw makeObjectUnexpected(null);
			
			fromIdentifierWalkLoadList(list, getGramChild(sym, 0));
			fromIdentifierWalkLoadList(list, getGramChild(sym, 2));
			return;
		}

		printLine("symInIdentiferWalk," + id);
		throw makeObjectUnexpected(null);
	}
	
	
	// core logic grammar
	//
	
	public void initGrammarFileWithPath(CommonInt32Array grammarFilePath) {
		FileNode2 dirNode;
		FileNode2 fileNode;
		int fileType;
		
		grmrDat = new TranslateOldTest1GrammarData();
		grmrDat.init(dat);
		grmrDat.grammarDirNode = dat.globalGrammarDirNode;
		grmrDat.grammarFilePath = CommonIntArrayUtils.copy32(
			grammarFilePath);

		dirNode = (FileNode2) grmrDat.grammarDirNode;
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		fileType = FileNode2Utils.getFileType(dirNode, grammarFilePath, '/');
		
		if(fileType == FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeObjectNotFound(null);

		if(fileType != FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectUnexpected(null);
		
		grmrDat.gdef.searchableDirContextList =
			makeListWithSingleObject(grmrDat.grammarDirNode);
		grmrDat.gdef.grammarFilePath = CommonIntArrayUtils.copy32(
			grammarFilePath);
		
		grmrDat.gdefRead.reset();
		grmrDat.lrkCalc.reset();
		
		grmrDat.allocHelp = dat.loadingAllocHelp;
		grmrDat.trimPositions = makeInt32Array(grmrDat.allocHelp.getCursorCount());
		grmrDat.allocHelp.getCursorPositions(grmrDat.trimPositions);
		

		//if(dat.allocHelp == null) throw new NullPointerException();
		grmrDat.gdefRead.setAllocHelper(grmrDat.allocHelp);
	}

	public void cleanupGrammarFile() {
		grmrDat.gdefRead.setAllocHelper(null);
		grmrDat.allocHelp.setCursorPositions(grmrDat.trimPositions);
	}

	public boolean loadGrammarFile() {
		int moveResult;
		boolean isError;

		printLine("Grammar File," +
			StringUtils.javaStringFromInt32String(
				(CommonInt32Array) grmrDat.grammarFilePath));
		
		transferStrangeAllocCounts2(grmrDat.gdefRead.dat);
		if(dat.traceStrangeAllocCount > 0)
			printLine("loadGrammarFile,strange," + dat.traceStrangeAllocCount);
		
		dat.indentCount += 1; // now in File
		
		isError = false;
		
		if(!isError)
		while(true) {
			if(grmrDat.gdef.state
				== GrammarReaderData.STATE_HAVE_GRAMMAR)
				break;
			
			moveResult = grmrDat.gdefRead.move(ModuleMoveDirection.TO_NEXT);

			if(moveResult != ModuleMoveResult.SUCCESS) {
				printLine("Bad move result: moveResult="
					+ moveResult);
				isError = true;
				break;
			}
		}
				
		if(!isError)
		while(true) {
			if(grmrDat.lrkDat.state
				== LrkData.STATE_HAVE_MACHINE)
				break;
			
			moveResult = grmrDat.lrkCalc.move(ModuleMoveDirection.TO_NEXT);

			if(moveResult != ModuleMoveResult.SUCCESS) {
				printLine("Bad move result: moveResult="
					+ moveResult);
				isError = true;
				break;
			}
		}

		grmrDat.spectrumStack = null;
		
		if(!isError) {
			grmrDat.spectrumStack = dat.lrUtils
				.makePrecedenceSpectrumStackSimple(
					grmrDat.lrkCalc.dat.names);
		}
		
		// check modules

		if(isError) {
			printLine("Outputing module info");

			dat.indentCount += 1; // now in module info

			dumpModuleStatus(grmrDat.gdefRead);
			
			int srcNum;
			int srcCount;
			
			srcNum = 0;
			srcCount = grmrDat.gdef.sourceStack.size();
			// for debugging
			while(srcNum < srcCount) {
				GrammarSource grmr = (GrammarSource)
					grmrDat.gdef.sourceStack.get(srcNum);
				GrammarSourceData grmrDat = (GrammarSourceData) grmr.getData();

				dumpModuleStatus(grmr);
				dumpModuleStatus(grmr.charRead);
				dumpModuleStatus(grmr.intEval);
				dumpModuleStatus(grmr.keywordHelp);
				dumpModuleStatus(grmr.strEval);
				dumpModuleStatus(grmr.tokChoose);
				dumpModuleStatus(grmr.tokenRead);
				
				srcNum += 1;
			}				
			
			dumpModuleStatus(grmrDat.lrkCalc);

			dat.indentCount -= 1; // now in file
		}
		
		dat.indentCount -= 1; // now outside

		transferAllocCounts2(grmrDat.gdefRead.dat);

		return isError;
	}

	
	// core logic doc
	//

	public void initDocFile(CommonInt32Array filePath) {
		initDocFileWithDirNode(
			(FileNode2) dat.globalTestDirNode, filePath);
	}

	public void initDocFileWithDirNode(
		FileNode2 dirNode, CommonInt32Array filePath) {

		FileNode2 fileNode;

		docDat = new TranslateOldTest1DocData();
		docDat.init(dat);
		
		docDat.testDirNode = dirNode;
		
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(dirNode, filePath, '/')
			!= FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectUnexpected(null);
		
		fileNode = FileNode2Utils.createFilePathNode(dirNode, filePath);

		docDat.testFileNode = fileNode;
		
		docDat.fileDat = FileNode2Utils.openNormalFile(
			fileNode, (short) AccessRights.ACCESS_READ, '/');
		
		dat.linkUtils.initFileContext(fileNode, docDat.fileDat);
		docDat.charRead = dat.linkUtils.createCharReader3FromFileContext(
			fileNode, docDat.fileDat, dat.utils);
		docDat.strRead = dat.linkUtils.createStringReader3FromFileContext(
			fileNode, docDat.fileDat, dat.utils);

		docDat.strRead.reset();
		docDat.charRead.reset();
				
		docDat.tokChoose.charRead = docDat.charRead;
		docDat.tokChoose.reset();
		
		docDat.xmlRead.charRead = docDat.charRead;
		docDat.xmlRead.reset();

		docDat.intEval.strRead = docDat.strRead;
	}
	
	public void cleanupDocFile() {
		docDat.charRead = null;
		docDat.strRead = null;
		
		docDat.tokChoose.charRead = null;
		docDat.tokChoose.reset();
				
		docDat.xmlRead.charRead = null;
		docDat.xmlRead.reset();
		
		docDat.intEval.strRead = null;
		
		FileNode2Utils.closeNormalFile(
			(FileNode2) docDat.testFileNode, docDat.fileDat);
		docDat.testFileNode = null;
		docDat.fileDat = null;
		return;
	}

	public boolean runDocFile() {
		boolean isError;
		int moveResult;
		int state;
		Symbol sym;
		int id;
		GramContainer grmCon;
		boolean didMove;
		XmlBaseReader xmlRead;
		BaseModuleData xmlDat;
		CFamilyForm logicForm;
		
		printLine("Doc File: " + StringUtils.javaStringFromInt32String(
				(CommonInt32Array) docDat.testFileNode.sortObject));
		
		isError = false;
		dat.indentCount += 1; // now in File
		
		xmlRead = docDat.xmlRead;
		xmlDat = docDat.xmlRead.dat;
		
		didMove = true;
		
		if(!isError)
		while(true) {
			if(!didMove) {
				moveResult = xmlRead.move(ModuleMoveDirection.TO_NEXT);

				if(moveResult == ModuleMoveResult.AT_END)
					break;

				if(moveResult != ModuleMoveResult.SUCCESS) {
					printLine("Bad move result: moveResult="
						+ moveResult);
					isError = true;
					break;
				}
				
				didMove = true;
			}
			
			state = xmlDat.state;
			
			if(state == SimpleReaderData.STATE_HAVE_SYMBOL) {
				didMove = false;
				continue;
			}
			
			if(state == BaseModuleData.STATE_START) {
				didMove = false;
				continue;
			}
			
			printLine("PARSER BAD STATE");
			isError = true;
			break;
		}
		
		if(isError) {
			printLine("Outputing module info");

			dat.indentCount += 1; // now in module info

			dumpModuleStatus(xmlRead);

			dat.indentCount -= 1; // now in file
		}
		
		dat.indentCount -= 1; // now outside
		
		return isError;
	}

	
	// directory walk logic
	//

	private CommonArrayList listDirectory(
		FileNode2 dirNode, CommonInt32Array subPath) {
		
		StoragePathAccess fs = FileNode2Utils.getInnerFileSystem(dirNode);
		
		CommonArrayList pathList = makeArrayList();
		FileNode2Utils.collectFilePathStrings(
			pathList, dirNode);
		if(subPath != null) pathList.add(subPath);
		
		CommonInt32Array fsSubPath = PathUtils.combineManyPaths(pathList, '/');
		
		return fs.listDirectory(fsSubPath);
	}
	
	private boolean isNameTypeCompatible(CommonInt32Array path) {
		CommonArrayList extParts = PathUtils.splitPath(dat.nameType, ',');
		CommonInt32Array ext2 = PathUtils.combineManyPaths(extParts, '.');
		String ext3 = "." + StringUtils.javaStringFromInt32String(ext2);
		String path2 = StringUtils.javaStringFromInt32String(path);
		return path2.endsWith(ext3);
	}
	
	private boolean walkSource() {
		TranslateOldTest1TestDirInfo srcDirInfo
			= getTestDirInfoEntry(TranslateOldTest1Data.SELECT_MAIN_SOURCE);

		TranslateOldTest1TestDirInfo trgDirInfo
			= getTestDirInfoEntry(TranslateOldTest1Data.SELECT_MAIN_TARGET);
		
		boolean isError = false;
		int i;
		int count;
		
		if(srcDirInfo != null) {
			TranslateOldTest1FileSelectInfo
				selectInfo = srcDirInfo.logicInfo;

			if(srcDirInfo.grammarDirNode != dat.globalGrammarDirNode) {
				setGrammarDir(srcDirInfo.grammarDirNode);
			}

			if(srcDirInfo.grammarDirNode != dat.globalGrammarDirNode) {
				initGrammarFileWithPath(srcDirInfo.grammarFilePath);
			}

			if(!refCompareBasic(selectInfo.fileNameType, dat.nameType))
				dat.nameType = CommonIntArrayUtils.copy32(selectInfo.fileNameType);
			if(!slowInt32StringEqual(selectInfo.fileNameType, dat.nameType))
				dat.nameType = CommonIntArrayUtils.copy32(selectInfo.fileNameType);


			CommonArrayList dirList = listDirectory(srcDirInfo.testDirNode, null);
			int fileType;

			setTestDir(srcDirInfo.testDirNode);
			FileNode2 outDir;
			outDir = null;
			if(trgDirInfo != null) outDir = trgDirInfo.testDirNode;
			setTestOutputDir(outDir);

			if(!selectInfo.filesAll) {
				if(selectInfo.manualFileList == null) return isError;

				dat.majorPassNum = TranslateOldTest1Data.PASS_1_COLLECT_NAMES;
				printLine("Pass,1CollectNames");

				i = 0;
				count = selectInfo.manualFileList.size();
				while(i < count) {
					FileNode2 fpNode = (FileNode2) selectInfo.manualFileList.get(i);
					CommonInt32Array path = (CommonInt32Array) fpNode.sortObject;

					initTestFile(path);
					if(trgDirInfo != null) initTestOutputFile(path);

					isError = runTestFile();

					if(trgDirInfo != null) cleanupTestOutputFile();
					cleanupTestFile();

					if(!isError) dat.okTestFileCount += 1;

					if(isError) return isError;

					i += 1;
				}

				return isError;
			}
			
			// whole translate
			//
			
			dat.cfamilyChunkStack = dat.nameNodeUtils.makeChunkStack();
			dat.rootChunk = dat.nameNodeUtils.makeChunkNamespace(dat.cfamilyChunkStack);
			dat.mainChunk = dat.nameNodeUtils.makeChunkNamespace(dat.cfamilyChunkStack);
			
			
			dat.majorPassNum = TranslateOldTest1Data.PASS_1_COLLECT_NAMES;
			dat.okTestFileCount = 0;
			printLine("Pass,1CollectNames");
			
			i = 0;
			count = dirList.size();
			while(i < count) {
				CommonInt32Array name = (CommonInt32Array) dirList.get(i);
				fileType = FileNode2Utils.getFileType(srcDirInfo.testDirNode, name, '/');

				if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE)
				if(isNameTypeCompatible(name)) {
					//FileNode2Utils.createFilePathNode(selectInfo.testDirNode, name);


					initTestFile(name);
					//if(trgDirInfo != null) initTestOutputFile(name);

					isError = runTestFile();

					//if(trgDirInfo != null) cleanupTestOutputFile();
					cleanupTestFile();

					if(!isError) dat.okTestFileCount += 1;

					if(isError) return isError;
				}

				i += 1;
			}

			if(trgDirInfo != null) {
				dat.majorPassNum = TranslateOldTest1Data.PASS_3_WRITE_LOGIC;
				dat.okTestFileCount = 0;
				printLine("Pass,3WriteLogic");

				i = 0;
				count = dirList.size();
				while(i < count) {
					CommonInt32Array name = (CommonInt32Array) dirList.get(i);
					fileType = FileNode2Utils.getFileType(srcDirInfo.testDirNode, name, '/');

					if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE)
					if(isNameTypeCompatible(name)) {
						//FileNode2Utils.createFilePathNode(selectInfo.testDirNode, name);


						initTestFile(name);
						if(trgDirInfo != null) initTestOutputFile(name);

						isError = runTestFile();

						if(trgDirInfo != null) cleanupTestOutputFile();
						cleanupTestFile();

						if(!isError) dat.okTestFileCount += 1;

						if(isError) return isError;
					}

					i += 1;
				}

			}
		}
		
		return isError;
	}
	
	/*
	private boolean walkLogic(
		int action,
		FileNode2 pathNode,
		CommonInt32Array subPath,
		CommonArrayList logicForms) {
		
		CommonInt32Array docXmlExt = StringUtils.int32StringFromJavaString("doc.xml");
		int fileType;
		boolean isError;
		CommonArrayList logicForms2 = null;
		
		if(pathNode == null)
			throw makeObjectNotFound(null);

		if(subPath == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(pathNode) == null)
			throw makeObjectNotFound(null);
				
		CommonInt32Array docSubPath = PathUtils.combine2OptionalPaths(
			subPath, docXmlExt, '.');

		fileType = FileNode2Utils.getFileType(pathNode, docSubPath, '/');
		if(fileType == FileTypes.FILE_TYPE_NORMAL_FILE) {
			initDocFileWithDirNode(pathNode, docSubPath);
			isError = runDocFile();
			cleanupDocFile();
			
			if(isError) return isError;
			
			if(dat.logicForms != null) {
				logicForms2 = makeArrayList();
				
				ListUtils.listExtend(logicForms2, logicForms);
				ListUtils.listExtend(logicForms2, dat.logicForms);
			}
			
			dat.logicForms = null;
		}
		
		if(logicForms2 == null) logicForms2 = logicForms;
		
		fileType = FileNode2Utils.getFileType(pathNode, subPath, '/');
		if(fileType == FileTypes.FILE_TYPE_DIRECTORY) {
			CommonArrayList dirList = listDirectory(pathNode, subPath);
			
			int i;
			int count;
			
			i = 0;
			count = dirList.size();
			while(i < count) {
				CommonInt32Array name = (CommonInt32Array) dirList.get(i);
				CommonInt32Array name2 = removeDocXml(name);
				CommonInt32Array subPath2 = PathUtils.combine2OptionalPaths(
					subPath, name2, '/');
				
				if(walkLogic(action, pathNode, subPath2, logicForms2))
					return true;
				
				i += 1;
			}
		}
		
		return false;
	}
	*/
		
	/*
	private CommonInt32Array removeDocXml(CommonInt32Array path) {
		int i;
		CommonInt32Array xmlExt = StringUtils.int32StringFromJavaString("xml");
		CommonInt32Array docExt = StringUtils.int32StringFromJavaString("doc");
		//CommonInt32Array docXmlExt = StringUtils.int32StringFromJavaString("doc.xml");
		CompareParams cmpRec = new CompareParams();
		CommonInt32Array testChunk;
		
		CommonArrayList pathChunks = PathUtils.splitPath(
			path, '.');
		
		// remove .doc and .xml from the end
		while(true) {
			i = pathChunks.size();
			
			if(i == 0) break;
			
			i -= 1;
			
			testChunk = (CommonInt32Array) pathChunks.get(i);
			
			StringUtils.int32StringCompareSimple(testChunk, xmlExt, cmpRec);
			if(compareIsEqual(cmpRec)) {
				pathChunks.removeAt(i);
				continue;
			}

			StringUtils.int32StringCompareSimple(testChunk, docExt, cmpRec);
			if(compareIsEqual(cmpRec)) {
				pathChunks.removeAt(i);
				continue;
			}
			
			// done
			break;
		}
		
		CommonInt32Array path2 = PathUtils.combineManyPaths(pathChunks, '.');
		if(path2 == null)
			throw makeObjectNotFound(null);
		
		return path2;
	}
	*/
	
	
	// directory config stuff
	//
	
	private void setTestDir(FileNode2 dirNode) {
		if(dirNode == dat.globalTestDirNode) return;
		
		
		if(dirNode != null) {
			printLine("setTestDir,set,"
				+ makeNativeString(
					(CommonInt32Array) dirNode.sortObject));
		}

		if(dirNode == null && dat.globalTestDirNode != null)
			printLine("setTestDir,set-to-null");
		
		dat.globalTestDirNode = dirNode;
	}

	public void setTestDirWithFullPath(CommonInt32Array path) {
		FileNode2 fpNode;
		int fileType;
		
		fpNode = getFilePathNode(path);
		if(fpNode == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(fpNode) == null)
			throw makeObjectNotFound(null);
		
		fileType = FileNode2Utils.getFileType(fpNode, null, '/');
		if(fileType == FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeObjectNotFound(null);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeObjectUnexpected(null);
		
		setTestDir(fpNode);
		return;
	}

	private void setTestOutputDir(FileNode2 dirNode) {
		if(dirNode == dat.globalTestOutputDirNode) return;

		if(dirNode != null) {
			printLine("setTestOutputDir,set,"
				+ makeNativeString(
					(CommonInt32Array) dirNode.sortObject));
		}
		
		if(dirNode == null && dat.globalTestOutputDirNode != null)
			printLine("setTestOutputDir,set-to-null");
		
		dat.globalTestOutputDirNode = dirNode;
	}
		
	private void setGrammarDir(FileNode2 dirNode) {
		dat.globalGrammarDirNode = dirNode;
	}
	
	public void setGrammarDirWithFullPath(CommonInt32Array path) {
		FileNode2 fpNode;
		int fileType;
		
		fpNode = getFilePathNode(path);
		if(fpNode == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(fpNode) == null)
			throw makeObjectNotFound(null);
		
		fileType = FileNode2Utils.getFileType(fpNode, null, '/');
		if(fileType == FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeObjectNotFound(null);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeObjectUnexpected(null);
		
		setGrammarDir(fpNode);
		return;
	}
	
	private void setDefDir(FileNode2 dirNode) {
		dat.globalDefDirNode = dirNode;
	}
	
	public void setDefDirWithFullPath(CommonInt32Array path) {
		FileNode2 fpNode;
		int fileType;
		
		fpNode = getFilePathNode(path);
		if(fpNode == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(fpNode) == null)
			throw makeObjectNotFound(null);
		
		fileType = FileNode2Utils.getFileType(fpNode, null, '/');
		if(fileType == FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeObjectNotFound(null);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY)
			throw makeObjectUnexpected(null);
		
		setDefDir(fpNode);
		return;
	}

	public void setNameType(CommonInt32Array ext) {
		dat.nameType = CommonIntArrayUtils.copy32(ext);
	}
	
	public boolean checkTestSelectionOverlap() {
		return false;
	}

	public void addLogicFile(CommonInt32Array path) {
		TranslateOldTest1TestDirInfo entry;
		TranslateOldTest1FileSelectInfo selectInfo;
		
		entry = getTestDirInfoEntry(dat.testFlags);
		
		if(entry == null) {
			selectInfo = addFile(null, path);
			entry = createTestDirInfoEntry(dat.testFlags);
			entry.logicInfo = selectInfo;
			setTestDirSettings(entry);
			dat.testDirList.add(entry);
			return;
		}
		
		checkTestDirCompatibile(entry);
		entry.logicInfo = addFile(entry.logicInfo, path);
		return;
	}

	public void addLogicFilesAll() {
		TranslateOldTest1TestDirInfo entry;
		TranslateOldTest1FileSelectInfo selectInfo;
		
		entry = getTestDirInfoEntry(dat.testFlags);
		
		if(entry == null) {
			selectInfo = addFilesAll(null);
			entry = createTestDirInfoEntry(dat.testFlags);
			entry.logicInfo = selectInfo;
			setTestDirSettings(entry);
			dat.testDirList.add(entry);
			return;
		}

		checkTestDirCompatibile(entry);
		entry.logicInfo = addFilesAll(entry.logicInfo);
		return;
	}

	public void addDocFile(CommonInt32Array path) {
		TranslateOldTest1TestDirInfo entry;
		TranslateOldTest1FileSelectInfo selectInfo;
		
		entry = getTestDirInfoEntry(dat.testFlags);
		
		if(entry == null) {
			selectInfo = addFile(null, path);
			entry = createTestDirInfoEntry(dat.testFlags);
			entry.docInfo = selectInfo;
			setTestDirSettings(entry);
			dat.testDirList.add(entry);
			return;
		}

		checkTestDirCompatibile(entry);
		entry.docInfo = addFile(entry.logicInfo, path);
		return;
	}

	public void addDocFilesAll() {
		TranslateOldTest1TestDirInfo entry;
		TranslateOldTest1FileSelectInfo selectInfo;
		
		entry = getTestDirInfoEntry(dat.testFlags);
		
		if(entry == null) {
			selectInfo = addFilesAll(null);
			entry = createTestDirInfoEntry(dat.testFlags);
			entry.docInfo = selectInfo;
			setTestDirSettings(entry);
			dat.testDirList.add(entry);
			return;
		}

		checkTestDirCompatibile(entry);
		entry.docInfo = addFilesAll(entry.docInfo);
		return;
	}
	
	public TranslateOldTest1TestDirInfo getTestDirInfoEntry(short flags) {
		CommonArrayList testSelectList;
		TranslateOldTest1TestDirInfo entry;

		int i;
		int count;

		testSelectList = dat.testDirList;

		i = 0;
		count = testSelectList.size();
		while(i < count) {
			entry = (TranslateOldTest1TestDirInfo) testSelectList.get(i);
			
			if(FlagUtils.getFlagInt16(flags, TranslateOldTest1Data.FLAG_TARGET)
				!= FlagUtils.getFlagInt16(entry.theFlags, TranslateOldTest1Data.FLAG_TARGET))
			{
				i += 1;
				continue;
			}
			
			if(FlagUtils.getFlagInt16(flags, TranslateOldTest1Data.FLAG_OUTSIDE)
				!= FlagUtils.getFlagInt16(entry.theFlags, TranslateOldTest1Data.FLAG_OUTSIDE))
			{
				i += 1;
				continue;
			}

			if(FlagUtils.getFlagInt16(flags, TranslateOldTest1Data.FLAG_CACHE)
				!= FlagUtils.getFlagInt16(entry.theFlags, TranslateOldTest1Data.FLAG_CACHE))
			{
				i += 1;
				continue;
			}
			
			//printLine("theTest," + flags + "," + entry.theFlags);
			return entry;
		}
		
		return null;
	}
	
	public TranslateOldTest1TestDirInfo createTestDirInfoEntry(short flags) {
		TranslateOldTest1TestDirInfo entry;

		entry = new TranslateOldTest1TestDirInfo();
		entry.theFlags = flags;
		return entry;
	}
	
	private CommonInt32Array addNameTypeToPath(CommonInt32Array path, CommonInt32Array nameType) {
		CommonArrayList parts;
		CommonArrayList extParts;
		
		int i;
		int count;
		
		parts = makeArrayList();
		if(path != null) parts.add(path);
		
		extParts = PathUtils.splitPath(nameType, ',');
		
		i = 0;
		count = extParts.size();
		while(i < count) {
			parts.add(extParts.get(i));
			i += 1;
		}
		
		return PathUtils.combineManyPaths(parts, '.');
	}
	
	private TranslateOldTest1FileSelectInfo addFile(
		TranslateOldTest1FileSelectInfo selectInfo, CommonInt32Array path) {
		
		TranslateOldTest1FileSelectInfo selectInfo2;
		FileNode2 dirNode;
		FileNode2 fpNode;
		int fileType;
		CommonInt32Array path2;
		
		dirNode = dat.globalTestDirNode;


		// various checking

		if(dat.nameType == null)
			throw makeObjectNotFound(null);
		
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(selectInfo != null) {
			if(selectInfo.filesAll)
				throw makeObjectUnexpected(null);

			if(selectInfo.fileNameType != null)
			if(!slowInt32StringEqual(dat.nameType, selectInfo.fileNameType))
				throw makeObjectUnexpected(null);
		}


		// check if file exists

		path2 = addNameTypeToPath(path, dat.nameType);
		
		fileType = FileNode2Utils.getFileType(dirNode, path2, '/');
		if(fileType == FileTypes.FILE_TYPE_NOT_EXIST)
			throw makeObjectNotFound(null);

		
		// checking done, time to commit
		
		selectInfo2 = selectInfo;
		if(selectInfo2 == null) selectInfo2
			= new TranslateOldTest1FileSelectInfo();
		
		fpNode = FileNode2Utils.createFilePathNode(dirNode, path2);

		if(selectInfo2.fileNameType == null)
			selectInfo2.fileNameType = CommonIntArrayUtils.copy32(dat.nameType);

		if(selectInfo2.manualFileList == null) selectInfo2.manualFileList = makeArrayList();
		selectInfo2.manualFileList.add(fpNode);
		
		return selectInfo2;
	}
	
	private TranslateOldTest1FileSelectInfo addFilesAll(
		TranslateOldTest1FileSelectInfo selectInfo) {
		
		TranslateOldTest1FileSelectInfo selectInfo2;

		if(dat.nameType == null)
			throw makeObjectNotFound(null);
		
		if(selectInfo != null) {
			if(selectInfo.manualFileList != null)
				throw makeObjectUnexpected(null);
			
			if(selectInfo.fileNameType != null)
			if(!slowInt32StringEqual(dat.nameType, selectInfo.fileNameType))
				throw makeObjectUnexpected(null);
		}
		

		// checking done, time to commit
		
		selectInfo2 = selectInfo;
		if(selectInfo2 == null) selectInfo2
			= new TranslateOldTest1FileSelectInfo();

		if(selectInfo2.fileNameType == null)
			selectInfo2.fileNameType = CommonIntArrayUtils.copy32(dat.nameType);

		selectInfo2.filesAll = true;

		return selectInfo2;
	}
	
	private void checkTestDirCompatibile(
		TranslateOldTest1TestDirInfo entry) {
		
		if(entry.grammarDirNode != dat.globalGrammarDirNode)
			throw makeObjectUnexpected(null);
		if(!refCompareBasic(entry.grammarFilePath, dat.globalGrammarFilePath))
			throw makeObjectUnexpected(null);
		if(entry.grammarFilePath != null && dat.globalGrammarFilePath != null)
			if(!slowInt32StringEqual(entry.grammarFilePath, dat.globalGrammarFilePath))
				throw makeObjectUnexpected(null);
		if(entry.testDirNode != dat.globalTestDirNode)
			throw makeObjectUnexpected(null);
	}

	private void setTestDirSettings(
		TranslateOldTest1TestDirInfo entry) {
		
		entry.grammarDirNode = dat.globalGrammarDirNode;
		entry.grammarFilePath = dat.globalGrammarFilePath;
		entry.testDirNode = dat.globalTestDirNode;
	}
	
	public boolean checkSelectInfoExist(short flags) {
		return getTestDirInfoEntry(flags) != null;
	}

	
	// file system access
	//	
	
	public void createFilePathNode(CommonInt32Array path) {
		CommonArrayList pathChunks = PathUtils.splitPath(path, ',');
		SortParams sortRec = makeSortParams();
		int i;
		int count;
		CommonArrayList dir;
		CommonInt32Array chunk;
		FileNode2 dNode;
		FileNode2 dNodePrev;
		
		count = pathChunks.size();
		i = 0;
		dir = dat.directory;
		dNodePrev = null;
		dNode = null;
		while(i < count) {
			chunk = (CommonInt32Array) pathChunks.get(i);
			
			SortUtils.int32StringBinaryLookupSimple(dir, chunk, sortRec);
			
			if(sortRec.foundExisting) {
				dNode = (FileNode2) dir.get(sortRec.index);
				
				if(dNode.theType != FileNodeTypes.TYPE_FILE_PATH)
					throw makeObjectUnexpected(null);
				
				if(i + 1 >= count)
					// last chunk points to an existing node
					// we dont have do to anything
					break;
				
				if(dNode.children == null)
					dNode.children = makeArrayList();
				
				dir = dNode.children;
				dNodePrev = dNode;
				dNode = null;
				i += 1;
				continue;
			}
			
			if(i + 1 >= count) {
				// last chunk
				// lets create the node
				dNode = new FileNode2();
				dNode.parent = dNodePrev;
				dNode.theType = FileNodeTypes.TYPE_FILE_PATH;
				dNode.sortObject = chunk;
				
				// commit
				dir.addAt(sortRec.index, dNode);
				
				// we are done
				break;
			}
			
			// something other than the final chunk,
			// points to nothing
			throw makeObjectNotFound(null);
		}
		
		return;
	}

	public void openLocalFileSystem(CommonInt32Array path, short rights) {
		CommonArrayList pathChunks = PathUtils.splitPath(path, ',');
		SortParams sortRec = makeSortParams();
		int i;
		int count;
		CommonArrayList dir;
		CommonInt32Array chunk;
		FileNode2 dNode;
		FileNode2 dNodePrev;
		FileNode2 fpNode;
		
		count = pathChunks.size();
		i = 0;
		dir = dat.directory;
		dNodePrev = null;
		dNode = null;
		fpNode = null;
		while(i < count) {
			chunk = (CommonInt32Array) pathChunks.get(i);
			
			SortUtils.int32StringBinaryLookupSimple(dir, chunk, sortRec);
			
			if(sortRec.foundExisting) {
				dNode = (FileNode2) dir.get(sortRec.index);
				
				if(dNode.theType != FileNodeTypes.TYPE_FILE_PATH)
					throw makeObjectUnexpected(null);
				
				fpNode = (FileNode2) dNode;
				
				// this part is special
				if(FileNode2Utils.hasOpenFileSystems(fpNode)
					|| FileNode2Utils.hasOpenNormalFiles(fpNode))
					throw makeObjectUnexpected(null);
				
				if(i + 1 >= count)
					// fpNode is the target node
					break;
				
				if(dNode.children == null)
					dNode.children = makeArrayList();
				
				dir = dNode.children;
				dNodePrev = dNode;
				dNode = null;
				i += 1;
				continue;
			}
			
			throw makeObjectNotFound(null);
		}
		
		CommonArrayList pathList = makeArrayList();
		FileNode2Utils.collectFilePathStrings(pathList, fpNode);
		CommonInt32Array nativePath = PathUtils.combineManyPaths(pathList, '/');
		
		LocalStoragePath fs = new LocalStoragePath();
		
		fs.initBasePath(nativePath);
		fs.initSeparatorChar('/');
		fs.initRights(rights);
		fs.init();
		
		fs.initFileNumberingEra(1);
		
		int fileType = fs.getFileType(null);
		if(fileType != FileTypes.FILE_TYPE_DIRECTORY
			&& fileType != FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectNotFound(null);
		
		// commit
		//
		
		if(fpNode.openFileSystemStack == null)
			fpNode.openFileSystemStack = makeArrayList();
		
		// we are done
		fpNode.openFileSystemStack.add(fs);
		return;
	}

	private FileNode2 getFilePathNode(CommonInt32Array path) {
		CommonArrayList pathChunks = PathUtils.splitPath(path, ',');
		SortParams sortRec = makeSortParams();
		int i;
		int count;
		CommonArrayList dir;
		CommonInt32Array chunk;
		FileNode2 dNode;
		FileNode2 dNodePrev;
		FileNode2 fpNode;
		
		count = pathChunks.size();
		i = 0;
		dir = dat.directory;
		dNodePrev = null;
		dNode = null;
		fpNode = null;
		while(i < count) {
			chunk = (CommonInt32Array) pathChunks.get(i);
			
			SortUtils.int32StringBinaryLookupSimple(dir, chunk, sortRec);
			
			if(sortRec.foundExisting) {
				dNode = (FileNode2) dir.get(sortRec.index);
				
				if(dNode.theType != FileNodeTypes.TYPE_FILE_PATH)
					throw makeObjectUnexpected(null);
				
				fpNode = (FileNode2) dNode;
				
				if(i + 1 >= count)
					// we found the target node
					break;
				
				if(dNode.children == null)
					dNode.children = makeArrayList();
				
				dir = dNode.children;
				dNodePrev = dNode;
				dNode = null;
				i += 1;
				continue;
			}
			
			return null;
		}
		
		return fpNode;
	}
	
	
	// console output stuff
	//
	
	public void dumpModuleStatus(BaseModule mod) {
		/*
		BaseModuleData modDat = mod.getData();
		Class c = mod.getClass();
		String moduleName = c.getName();

		printLine("module " + moduleName);
		
		if(modDat.state == BaseModuleData.STATE_STUCK) {
			dat.indentCount += 1;
			printLine("STUCK with stuckState=" + modDat.stuckState);
			dat.indentCount -= 1;
		}
		
		dat.indentCount += 1;
		dumpProblemContainer(modDat.probBag);
		dat.indentCount -= 1;
		*/
	}

	private void dumpContext(Object obj) {
		/*
		if(obj == null) return;
		
		if(obj instanceof TextIndex) {
			TextIndex ti = (TextIndex) obj;

			printLine(getIndexString(ti));
		}
		
		if(obj instanceof CommonInt32Array) {
			CommonInt32Array str = (CommonInt32Array) obj;
			
			printLine(StringUtils.javaStringFromInt32String(str));
		}
		
		Class c = obj.getClass();
		printLine(c.getName());
		return;
		*/
	}
		
	public void dumpProblemContainer(ProblemContainer probBag) {
		/*
		int probNum;
		int probCount;
		Class c;
		
		probNum = 0;
		probCount = probBag.problems.size();
		while(probNum < probCount) {
			Problem prob = (Problem) probBag.problems.get(probNum);
			
			printLine("Problem with "
				+ "problemLevel=" + prob.problemLevel);
			
			dat.indentCount += 1;
			
			c = prob.errorObject.getClass();
			String className = c.getName();
			printLine(className);

			dat.indentCount += 1;
			
			if(prob.errorObject instanceof BaseError) {
				BaseError be = (BaseError) prob.errorObject;
				
				printLine("id=" + be.id);

				if(be.msg != null)
					printLine("msg=(" + be.msg + ")");
				
				dumpContext(be.context);

				if(be instanceof SymbolUnexpected) {
					SymbolUnexpected su = (SymbolUnexpected) be;
					
					printLine("sym id="
						+ dat.utils.getSymbolIdPrimary(su.givenSymbol));
					
					printLine("index "
						+ getIndexString(su.givenSymbol.startIndex));
				}
			}
			
			prob.errorObject.printStackTrace(out);

			dat.indentCount -= 2;
			
			probNum += 1;
		}
		*/
	}

	private void dumpSymbol(Symbol sym) {
		if(sym == null) return;
		
		if(sym.symbolType == SymbolTypes.TYPE_TOKEN) {
			dumpToken((Token) sym);
			return;
		}
		
		if(sym.symbolType == SymbolTypes.TYPE_GRAM) {
			dumpGram((Gram) sym);
			return;
		}
		
		dumpSymbolGeneral(sym);
		return;
	}
	
	private void dumpToken(Token tok) {
		TokenIntegerFull tInt;
		TokenFloatFull tFloat;
		TokenIntegerSimple tokIntSimple;
		TokenContainer tCont;
		String tokenName;
		int id;
		int cat;
		
		id = dat.utils.getSymbolIdPrimary(tok);
		cat = dat.utils.getSymbolIdCategory(tok);
		
		if(id == Symbols.TOKEN_END_OF_STREAM) {
			printLine("TOKEN_END_OF_STREAM");
			return;
		}
		
		if(cat == Symbols.TOKEN_CATEGORY_NUMBER) {
			if(id == Symbols.TOKEN_INTEGER_SIMPLE) {
				tokIntSimple = (TokenIntegerSimple) tok;

				printLine("TOKEN_INTEGER_SIMPLE "
					+ getSymbolString1(tokIntSimple));

				dat.indentCount += 1;

				printLine("RADIX (" + tokIntSimple.radix + ")");

				dat.indentCount -= 1;
				return;
			}

			if(id == Symbols.TOKEN_INTEGER_FULL) {
				tInt = (TokenIntegerFull) tok;

				printLine("TOKEN_INTEGER_FULL "
					+ getSymbolString1(tok));

				dat.indentCount += 1;

				if(tInt.integer != null) {
					printLine("INTEGER "
						+ getSymbolString1(tInt.integer));
				}

				printLine("RADIX (" + tInt.radix + ")");

				if(tInt.size != 0) {
					printLine("SIZE ("
						+ tInt.size + ")");
				}

				if(tInt.flagUnsigned) {
					printLine("UNSIGNED");
				}

				dat.indentCount -= 1;
				return;
			}

			if(id == Symbols.TOKEN_FLOAT_FULL) {
				tFloat = (TokenFloatFull) tok;

				printLine("TOKEN_FLOAT_FULL "
					+ getSymbolString1(tok));

				dat.indentCount += 1;

				if(tFloat.integer != null) {
					printLine("INTEGER "
						+ getSymbolString1(tFloat.integer));
				}

				if(tFloat.fraction != null) {
					printLine("FRACTION "
						+ getSymbolString1(tFloat.fraction));
				}

				if(tFloat.exponent != null) {
					printLine("EXPONENT "
						+ getSymbolString1(tFloat.exponent));
				}

				if(tFloat.flagExponentNeg) {
					printLine("EXPONENT_NEGATIVE");
				}

				printLine("RADIX ("
					+ tFloat.radix + ")");

				if(tFloat.size != 0) {
					printLine("SIZE ("
						+ tFloat.size + ")");
				}

				dat.indentCount -= 1;
				return;			
			}
		}
		
		if(id == Symbols.TOKEN_COMMENT_MULTI_LINE) {
			printLine("TOKEN_COMMENT_MULTI_LINE "
				+ getSymbolString1(tok));
			
			dat.indentCount += 1;

			tCont = (TokenContainer) tok;
			dumpToken(tCont.tok[0]);

			dat.indentCount -= 1;
			return;
		}

		if(id == Symbols.TOKEN_COMMENT_SINGLE_LINE) {
			printLine("TOKEN_COMMENT_SINGLE_LINE "
				+ getSymbolString1(tok));
			
			dat.indentCount += 1;

			tCont = (TokenContainer) tok;
			dumpToken(tCont.tok[0]);

			dat.indentCount -= 1;
			return;
		}
				
		tokenName = null;
		
		switch(id) {
		case Symbols.TOKEN_STRING_ESCAPE_NAME:
			tokenName = "TOKEN_STRING_ESCAPE_NAME";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_VERBATIM:
			tokenName = "TOKEN_STRING_ESCAPE_VERBATIM";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_INTEGER:
			tokenName = "TOKEN_STRING_ESCAPE_INTEGER";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_IGNORED:
			tokenName = "TOKEN_STRING_ESCAPE_IGNORED";
			break;
		}
		
		if(tokenName != null) {
			tCont = (TokenContainer) tok;

			printLine(tokenName + " "
				+ getSymbolString1(tok));

			dat.indentCount += 1;
			dumpToken(tCont.tok[0]);
			dat.indentCount -= 1;
			return;
		}
		
		switch(id) {
		case Symbols.TOKEN_IDENTIFIER:
			tokenName = "TOKEN_IDENTIFIER";
			break;
		case Symbols.TOKEN_BAD_SPAN:
			tokenName = "TOKEN_BAD_SPAN";
			break;
		case Symbols.TOKEN_STRING:
			tokenName = "TOKEN_STRING";
			break;
		case Symbols.TOKEN_STRING_SPAN:
			tokenName = "TOKEN_STRING_SPAN";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_IGNORED_RETURN:
			tokenName = "TOKEN_STRING_ESCAPE_IGNORED_RETURN";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_ALERT:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_ALERT";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_BACKSPACE:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_BACKSPACE";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_FORM_FEED:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_FORM_FEED";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_NEW_LINE:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_NEW_LINE";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_CARRIAGE_RETURN:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_CARRIAGE_RETURN";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_TAB:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_TAB";
			break;
		case Symbols.TOKEN_STRING_ESCAPE_NAME_VERTICAL_TAB:
			tokenName = "TOKEN_STRING_ESCAPE_NAME_VERTICAL_TAB";
			break;
		case Symbols.TOKEN_CHARACTER:
			tokenName = "TOKEN_CHARACTER";
			break;
		case Symbols.TOKEN_KEYWORD_NULL:
			tokenName = "TOKEN_KEYWORD_NULL";
			break;
		case Symbols.TOKEN_KEYWORD_FALSE:
			tokenName = "TOKEN_KEYWORD_FALSE";
			break;
		case Symbols.TOKEN_KEYWORD_TRUE:
			tokenName = "TOKEN_KEYWORD_TRUE";
			break;
		case Symbols.TOKEN_COMMA:
			tokenName = "TOKEN_COMMA";
			break;
		case Symbols.TOKEN_COLON:
			tokenName = "TOKEN_COLON";
			break;
		case Symbols.TOKEN_SEMICOLON:
			tokenName = "TOKEN_SEMICOLON";
			break;
		case Symbols.TOKEN_DOT:
			tokenName = "TOKEN_DOT";
			break;
		case Symbols.TOKEN_LPAREN:
			tokenName = "TOKEN_LPAREN";
			break;
		case Symbols.TOKEN_RPAREN:
			tokenName = "TOKEN_RPAREN";
			break;
		case Symbols.TOKEN_LBRACK:
			tokenName = "TOKEN_LBRACK";
			break;
		case Symbols.TOKEN_RBRACK:
			tokenName = "TOKEN_RBRACK";
			break;
		case Symbols.TOKEN_LBRACE:
			tokenName = "TOKEN_LBRACE";
			break;
		case Symbols.TOKEN_RBRACE:
			tokenName = "TOKEN_RBRACE";
			break;
		case Symbols.TOKEN_ASSIGN:
			tokenName = "TOKEN_ASSIGN";
			break;
		case Symbols.TOKEN_PLUS:
			tokenName = "TOKEN_PLUS";
			break;
		case Symbols.TOKEN_MINUS:
			tokenName = "TOKEN_MINUS";
			break;
		case Symbols.TOKEN_TIMES:
			tokenName = "TOKEN_TIMES";
			break;
		case Symbols.TOKEN_DIVIDE:
			tokenName = "TOKEN_DIVIDE";
			break;
		case Symbols.TOKEN_MOD:
			tokenName = "TOKEN_MOD";
			break;
		case Symbols.TOKEN_PLUS_ASSIGN:
			tokenName = "TOKEN_PLUS_ASSIGN";
			break;
		case Symbols.TOKEN_MINUS_ASSIGN:
			tokenName = "TOKEN_MINUS_ASSIGN";
			break;
		case Symbols.TOKEN_TIMES_ASSIGN:
			tokenName = "TOKEN_TIMES_ASSIGN";
			break;
		case Symbols.TOKEN_DIVIDE_ASSIGN:
			tokenName = "TOKEN_DIVIDE_ASSIGN";
			break;
		case Symbols.TOKEN_MOD_ASSIGN:
			tokenName = "TOKEN_MOD_ASSIGN";
			break;
		case Symbols.TOKEN_AND_BITWISE:
			tokenName = "TOKEN_AND_BITWISE";
			break;
		case Symbols.TOKEN_OR_BITWISE:
			tokenName = "TOKEN_OR_BITWISE";
			break;
		case Symbols.TOKEN_XOR_BITWISE:
			tokenName = "TOKEN_XOR_BITWISE";
			break;
		case Symbols.TOKEN_AND_BITWISE_ASSIGN:
			tokenName = "TOKEN_AND_BITWISE_ASSIGN";
			break;
		case Symbols.TOKEN_OR_BITWISE_ASSIGN:
			tokenName = "TOKEN_OR_BITWISE_ASSIGN";
			break;
		case Symbols.TOKEN_XOR_BITWISE_ASSIGN:
			tokenName = "TOKEN_XOR_BITWISE_ASSIGN";
			break;
		case Symbols.TOKEN_NOT_BITWISE:
			tokenName = "TOKEN_NOT_BITWISE";
			break;
		case Symbols.TOKEN_SHIFT_LEFT:
			tokenName = "TOKEN_SHIFT_LEFT";
			break;
		case Symbols.TOKEN_SHIFT_RIGHT:
			tokenName = "TOKEN_SHIFT_RIGHT";
			break;
		case Symbols.TOKEN_SHIFT_RIGHT_LOGICAL:
			tokenName = "TOKEN_SHIFT_RIGHT_LOGICAL";
			break;
		case Symbols.TOKEN_SHIFT_LEFT_ASSIGN:
			tokenName = "TOKEN_SHIFT_LEFT_ASSIGN";
			break;
		case Symbols.TOKEN_SHIFT_RIGHT_ASSIGN:
			tokenName = "TOKEN_SHIFT_RIGHT_ASSIGN";
			break;
		case Symbols.TOKEN_SHIFT_RIGHT_LOGICAL_ASSIGN:
			tokenName = "TOKEN_SHIFT_RIGHT_LOGICAL_ASSIGN";
			break;
		case Symbols.TOKEN_AND_LOGICAL:
			tokenName = "TOKEN_AND_LOGICAL";
			break;
		case Symbols.TOKEN_OR_LOGICAL:
			tokenName = "TOKEN_OR_LOGICAL";
			break;
		case Symbols.TOKEN_NOT_LOGICAL:
			tokenName = "TOKEN_NOT_LOGICAL";
			break;
		case Symbols.TOKEN_EQUAL:
			tokenName = "TOKEN_EQUAL";
			break;
		case Symbols.TOKEN_NOT_EQUAL:
			tokenName = "TOKEN_NOT_EQUAL";
			break;
		case Symbols.TOKEN_LESS:
			tokenName = "TOKEN_LESS";
			break;
		case Symbols.TOKEN_LESS_EQUAL:
			tokenName = "TOKEN_LESS_EQUAL";
			break;
		case Symbols.TOKEN_GREATER:
			tokenName = "TOKEN_GREATER";
			break;
		case Symbols.TOKEN_GREATER_EQUAL:
			tokenName = "TOKEN_GREATER_EQUAL";
			break;
		case Symbols.TOKEN_KEYWORD_NEW:
			tokenName = "TOKEN_KEYWORD_NEW";
			break;
		case Symbols.TOKEN_KEYWORD_CAST:
			tokenName = "TOKEN_KEYWORD_CAST";
			break;
		case Symbols.TOKEN_KEYWORD_INSTANCE_OF:
			tokenName = "TOKEN_KEYWORD_INSTANCE_OF";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_PARAMETERS:
			tokenName = "TOKEN_KEYWORD_PAREN_PARAMETERS";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_PARAMETERS_DEFINITION:
			tokenName = "TOKEN_KEYWORD_PAREN_PARAMETERS_DEFINITION";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_EXPRESSION:
			tokenName = "TOKEN_KEYWORD_PAREN_EXPRESSION";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_TYPE:
			tokenName = "TOKEN_KEYWORD_PAREN_TYPE";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS:
			tokenName = "TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS_DEFINITION:
			tokenName = "TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS_DEFINITION";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_ARRAY_ACCESS:
			tokenName = "TOKEN_KEYWORD_PAREN_ARRAY_ACCESS";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_ARRAY_INIT:
			tokenName = "TOKEN_KEYWORD_PAREN_ARRAY_INIT";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_LIST_ACCESS:
			tokenName = "TOKEN_KEYWORD_PAREN_LIST_ACCESS";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_LIST_INIT:
			tokenName = "TOKEN_KEYWORD_PAREN_LIST_INIT";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_DICT_ACCESS:
			tokenName = "TOKEN_KEYWORD_PAREN_DICT_ACCESS";
			break;
		case Symbols.TOKEN_KEYWORD_PAREN_DICT_INIT:
			tokenName = "TOKEN_KEYWORD_PAREN_DICT_INIT";
			break;
		case Symbols.TOKEN_KEYWORD_IF:
			tokenName = "TOKEN_KEYWORD_IF";
			break;
		case Symbols.TOKEN_KEYWORD_ELSE:
			tokenName = "TOKEN_KEYWORD_ELSE";
			break;
		case Symbols.TOKEN_KEYWORD_WHILE:
			tokenName = "TOKEN_KEYWORD_WHILE";
			break;
		case Symbols.TOKEN_KEYWORD_TRY:
			tokenName = "TOKEN_KEYWORD_TRY";
			break;
		case Symbols.TOKEN_KEYWORD_CATCH:
			tokenName = "TOKEN_KEYWORD_CATCH";
			break;
		case Symbols.TOKEN_KEYWORD_SWITCH:
			tokenName = "TOKEN_KEYWORD_SWITCH";
			break;
		case Symbols.TOKEN_KEYWORD_BREAK:
			tokenName = "TOKEN_KEYWORD_BREAK";
			break;
		case Symbols.TOKEN_KEYWORD_CONTINUE:
			tokenName = "TOKEN_KEYWORD_CONTINUE";
			break;
		case Symbols.TOKEN_KEYWORD_RETURN:
			tokenName = "TOKEN_KEYWORD_RETURN";
			break;
		case Symbols.TOKEN_KEYWORD_THROW:
			tokenName = "TOKEN_KEYWORD_THROW";
			break;
		case Symbols.TOKEN_KEYWORD_CASE:
			tokenName = "TOKEN_KEYWORD_CASE";
			break;
		case Symbols.TOKEN_KEYWORD_DEFAULT:
			tokenName = "TOKEN_KEYWORD_DEFAULT";
			break;
		case Symbols.TOKEN_KEYWORD_CLASS:
			tokenName = "TOKEN_KEYWORD_CLASS";
			break;
		case Symbols.TOKEN_KEYWORD_INTERFACE:
			tokenName = "TOKEN_KEYWORD_INTERFACE";
			break;
		case Symbols.TOKEN_KEYWORD_EXTENDS:
			tokenName = "TOKEN_KEYWORD_EXTENDS";
			break;
		case Symbols.TOKEN_KEYWORD_IMPLEMENTS:
			tokenName = "TOKEN_KEYWORD_IMPLEMENTS";
			break;
		case Symbols.TOKEN_KEYWORD_IMPORT:
			tokenName = "TOKEN_KEYWORD_IMPORT";
			break;
		case Symbols.TOKEN_KEYWORD_USING:
			tokenName = "TOKEN_KEYWORD_USING";
			break;
		case Symbols.TOKEN_KEYWORD_PACKAGE:
			tokenName = "TOKEN_KEYWORD_PACKAGE";
			break;
		case Symbols.TOKEN_KEYWORD_NAMESPACE:
			tokenName = "TOKEN_KEYWORD_NAMESPACE";
			break;
		case Symbols.TOKEN_KEYWORD_PUBLIC:
			tokenName = "TOKEN_KEYWORD_PUBLIC";
			break;
		case Symbols.TOKEN_KEYWORD_INTERNAL:
			tokenName = "TOKEN_KEYWORD_INTERNAL";
			break;
		case Symbols.TOKEN_KEYWORD_PROTECTED:
			tokenName = "TOKEN_KEYWORD_PROTECTED";
			break;
		case Symbols.TOKEN_KEYWORD_PRIVATE:
			tokenName = "TOKEN_KEYWORD_PRIVATE";
			break;
		case Symbols.TOKEN_KEYWORD_CONSTANT:
			tokenName = "TOKEN_KEYWORD_CONSTANT";
			break;
		case Symbols.TOKEN_KEYWORD_FINAL:
			tokenName = "TOKEN_KEYWORD_FINAL";
			break;
		case Symbols.TOKEN_KEYWORD_STATIC:
			tokenName = "TOKEN_KEYWORD_STATIC";
			break;
		case Symbols.TOKEN_KEYWORD_VIRTUAL:
			tokenName = "TOKEN_KEYWORD_VIRTUAL";
			break;
		case Symbols.TOKEN_KEYWORD_ABSTRACT:
			tokenName = "TOKEN_KEYWORD_ABSTRACT";
			break;
		case Symbols.TOKEN_KEYWORD_OVERRIDE:
			tokenName = "TOKEN_KEYWORD_OVERRIDE";
			break;
		}

		if(tokenName != null) {
			printLine(tokenName + " "
				+ getSymbolString1(tok));
			return;
		}
		
		dumpTokenGeneral(tok);
		return;
	}
	
	private void dumpGram(Gram grm) {
		String gramName;
		int id;
		Symbol sym;
		int i;
		int len;
		GramContainer grmCon;
		
		gramName = null;
		id = dat.utils.getSymbolIdPrimary(grm);
		
		switch(id) {
		case Symbols.GRAM_SEQUENCE:
			gramName = "GRAM_SEQUENCE";
			break;
		case Symbols.GRAM_EXPRESSION_UNARY_PREFIX:
			gramName = "GRAM_EXPRESSION_UNARY_PREFIX";
			break;
		case Symbols.GRAM_EXPRESSION_UNARY_POSTFIX:
			gramName = "GRAM_EXPRESSION_UNARY_POSTFIX";
			break;
		case Symbols.GRAM_EXPRESSION_BINARY:
			gramName = "GRAM_EXPRESSION_BINARY";
			break;
		case Symbols.GRAM_EXPRESSION_SIMPLE:
			gramName = "GRAM_EXPRESSION_SIMPLE";
			break;
		case Symbols.GRAM_EXPRESSION_PAREN:
			gramName = "GRAM_EXPRESSION_PAREN";
			break;
		case Symbols.GRAM_EXPRESSION_PAREN_EMPTY:
			gramName = "GRAM_EXPRESSION_PAREN_EMPTY";
			break;
		case Symbols.GRAM_EXPRESSION_BRACK:
			gramName = "GRAM_EXPRESSION_BRACK";
			break;
		case Symbols.GRAM_EXPRESSION_BRACK_EMPTY:
			gramName = "GRAM_EXPRESSION_BRACK_EMPTY";
			break;
		case Symbols.GRAM_EXPRESSION_ARRAY_ACCESS:
			gramName = "GRAM_EXPRESSION_ARRAY_ACCESS";
			break;
		case Symbols.GRAM_EXPRESSION_ARRAY_TYPE:
			gramName = "GRAM_EXPRESSION_ARRAY_TYPE";
			break;
		case Symbols.GRAM_EXPRESSION_FUNCTION_CALL:
			gramName = "GRAM_EXPRESSION_FUNCTION_CALL";
			break;
		case Symbols.GRAM_EXPRESSION_ALLOCATION_CALL:
			gramName = "GRAM_EXPRESSION_ALLOCATION_CALL";
			break;
		case Symbols.GRAM_EXPRESSION_ALLOCATION_ARRAY:
			gramName = "GRAM_EXPRESSION_ALLOCATION_ARRAY";
			break;
		case Symbols.GRAM_EXPRESSION_CAST:
			gramName = "GRAM_EXPRESSION_CAST";
			break;
		case Symbols.GRAM_EXPRESSION_INSTANCE_OF:
			gramName = "GRAM_EXPRESSION_INSTANCE_OF";
			break;
		case Symbols.GRAM_EXPRESSION_TYPE_WITH_PARAMETERS:
			gramName = "GRAM_EXPRESSION_TYPE_WITH_PARAMETERS";
			break;
		case Symbols.GRAM_STATEMENT_EXPRESSION:
			gramName = "GRAM_STATEMENT_EXPRESSION";
			break;
		case Symbols.GRAM_STATEMENT_VARIABLE_DEF:
			gramName = "GRAM_STATEMENT_VARIABLE_DEF";
			break;
		case Symbols.GRAM_STATEMENT_IF_THEN:
			gramName = "GRAM_STATEMENT_IF_THEN";
			break;
		case Symbols.GRAM_STATEMENT_IF_THEN_ELSE:
			gramName = "GRAM_STATEMENT_IF_THEN_ELSE";
			break;
		case Symbols.GRAM_STATEMENT_WHILE:
			gramName = "GRAM_STATEMENT_WHILE";
			break;
		case Symbols.GRAM_STATEMENT_TRY:
			gramName = "GRAM_STATEMENT_TRY";
			break;
		case Symbols.GRAM_STATEMENT_CATCH:
			gramName = "GRAM_STATEMENT_CATCH";
			break;
		case Symbols.GRAM_STATEMENT_SWITCH:
			gramName = "GRAM_STATEMENT_SWITCH";
			break;
		case Symbols.GRAM_STATEMENT_BREAK:
			gramName = "GRAM_STATEMENT_BREAK";
			break;
		case Symbols.GRAM_STATEMENT_BREAK_WITH_LABEL:
			gramName = "GRAM_STATEMENT_BREAK_WITH_LABEL";
			break;
		case Symbols.GRAM_STATEMENT_CONTINUE:
			gramName = "GRAM_STATEMENT_CONTINUE";
			break;
		case Symbols.GRAM_STATEMENT_CONTINUE_WITH_LABEL:
			gramName = "GRAM_STATEMENT_CONTINUE_WITH_LABEL";
			break;
		case Symbols.GRAM_STATEMENT_RETURN:
			gramName = "GRAM_STATEMENT_RETURN";
			break;
		case Symbols.GRAM_STATEMENT_RETURN_WITH_EXPRESSION:
			gramName = "GRAM_STATEMENT_RETURN_WITH_EXPRESSION";
			break;
		case Symbols.GRAM_STATEMENT_THROW:
			gramName = "GRAM_STATEMENT_THROW";
			break;
		case Symbols.GRAM_STATEMENT_LABEL:
			gramName = "GRAM_STATEMENT_LABEL";
			break;
		case Symbols.GRAM_STATEMENT_CASE:
			gramName = "GRAM_STATEMENT_CASE";
			break;
		case Symbols.GRAM_STATEMENT_DEFAULT:
			gramName = "GRAM_STATEMENT_DEFAULT";
			break;
		case Symbols.GRAM_FUNCTION_INIT:
			gramName = "GRAM_FUNCTION_INIT";
			break;
		case Symbols.GRAM_FUNCTION_DEF_SIMPLE:
			gramName = "GRAM_FUNCTION_DEF_SIMPLE";
			break;
		case Symbols.GRAM_FUNCTION_DEF_FULL:
			gramName = "GRAM_FUNCTION_DEF_FULL";
			break;
		case Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS:
			gramName = "GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS";
			break;
		case Symbols.GRAM_FUNCTION_DEF_FULL_WITH_INIT:
			gramName = "GRAM_FUNCTION_DEF_FULL_WITH_INIT";
			break;
		case Symbols.GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT:
			gramName = "GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT";
			break;
		case Symbols.GRAM_VARIABLE_INIT:
			gramName = "GRAM_VARIABLE_INIT";
			break;
		case Symbols.GRAM_VARIABLE_DEF_SIMPLE:
			gramName = "GRAM_VARIABLE_DEF_SIMPLE";
			break;
		case Symbols.GRAM_VARIABLE_DEF_FULL:
			gramName = "GRAM_VARIABLE_DEF_FULL";
			break;
		case Symbols.GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS:
			gramName = "GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS";
			break;
		case Symbols.GRAM_VARIABLE_DEF_FULL_WITH_INIT:
			gramName = "GRAM_VARIABLE_DEF_FULL_WITH_INIT";
			break;
		case Symbols.GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS_WITH_INIT:
			gramName = "GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS_WITH_INIT";
			break;
		case Symbols.GRAM_CLASS_DEF:
			gramName = "GRAM_CLASS_DEF";
			break;
		case Symbols.GRAM_CLASS_DEF_WITH_MODIFIERS:
			gramName = "GRAM_CLASS_DEF_WITH_MODIFIERS";
			break;
		case Symbols.GRAM_MODULE_EXTENDS:
			gramName = "GRAM_MODULE_EXTENDS";
			break;
		case Symbols.GRAM_MODULE_IMPLEMENTS:
			gramName = "GRAM_MODULE_IMPLEMENTS";
			break;
		case Symbols.GRAM_MODULE_EXTENDS_AND_IMPLEMENTS:
			gramName = "GRAM_MODULE_EXTENDS_AND_IMPLEMENTS";
			break;
		case Symbols.GRAM_PACKAGE_DEF:
			gramName = "GRAM_PACKAGE_DEF";
			break;
		case Symbols.GRAM_IMPORT_DEF:
			gramName = "GRAM_IMPORT_REF";
			break;
		case Symbols.GRAM_STATEMENT_BLOCK:
			gramName = "GRAM_STATEMENT_BLOCK";
			break;
		case Symbols.GRAM_STATEMENT_BLOCK_EMPTY:
			gramName = "GRAM_STATEMENT_BLOCK_EMPTY";
			break;
		case Symbols.GRAM_VARIABLE_BLOCK:
			gramName = "GRAM_VARIABLE_BLOCK";
			break;
		case Symbols.GRAM_VARIABLE_BLOCK_EMPTY:
			gramName = "GRAM_VARIABLE_BLOCK_EMPTY";
			break;
		case Symbols.GRAM_CLASS_BLOCK:
			gramName = "GRAM_CLASS_BLOCK";
			break;
		case Symbols.GRAM_CLASS_BLOCK_EMPTY:
			gramName = "GRAM_CLASS_BLOCK_EMPTY";
			break;
		case Symbols.GRAM_PACKAGE_BLOCK:
			gramName = "GRAM_PACKAGE_BLOCK";
			break;
		case Symbols.GRAM_PACKAGE_BLOCK_EMPTY:
			gramName = "GRAM_NAMESPACE_BLOCK_EMPTY";
			break;
		}
		
		if(gramName != null) {
			printLine(gramName + " "
				+ getSymbolString1(grm));
			
			dat.indentCount += 1;
			
			grmCon = (GramContainer) grm;
			Symbol[] symArray = grmCon.sym;
			
			if(symArray != null) {
				len = dat.utils.getGramChildCount(grmCon);
				i = 0;
				while(i < len) {
					dumpSymbol(symArray[i]);
					i += 1;
				}
			}

			dat.indentCount -= 1;
			
			return;
		}
				
		dumpGramGeneral(grm);
		return;
	}
	
	private void dumpSymbolGeneral(Symbol sym) {
		printLine("SYMBOL " + getSymbolString1(sym));
		
		dat.indentCount += 1;
		
		printLine("id=(" + dat.utils.getSymbolIdCategory(sym)
			+ "," + dat.utils.getSymbolIdPrimary(sym) + ")");
		
		dat.indentCount -= 1;

		throw makeObjectUnexpected(null);
	}
		
	private void dumpTokenGeneral(Token tok) {
		printLine("TOKEN " + getSymbolString1(tok));
		
		dat.indentCount += 1;
		
		printLine("id=(" + dat.utils.getSymbolIdCategory(tok)
			+ "," + dat.utils.getSymbolIdPrimary(tok) + ")");
		
		dat.indentCount -= 1;
	}
		
	private void dumpGramGeneral(Gram grm) {
		printLine("GRAM " + getSymbolString1(grm));
		
		dat.indentCount += 1;
		
		printLine("id=(" + dat.utils.getSymbolIdCategory(grm)
			+ "," + dat.utils.getSymbolIdPrimary(grm) + ")");
		
		dat.indentCount -= 1;
		return;
	}
	
	
	// small utility functions
	//
	
	private boolean compareIsEqual(CompareParams cmpRec) {
		return !cmpRec.greater && !cmpRec.less;
	}

	private boolean refCompareBasic(Object a, Object b) {
		if(a == null && b == null) return true;
		if(a != null && b != null) return true;
		return false;
	}
	
	private boolean slowInt32StringEqual(CommonInt32Array a, CommonInt32Array b) {
		CompareParams compRes = new CompareParams();
		
		StringUtils.int32StringCompareSimple(a, b, compRes);
		return !compRes.greater && !compRes.less;
	}
		
	private float ratioTwoInt32(int a1, int a2) {
		int total = a1 + a2;
		if(total == 0) return 0.0f;
		
		return ((float) a1) / total;
	}
		
	private int getCursorPositionsSum(CommonInt32Array arr) {
		int sum2;
		int i;
		int len;
		
		sum2 = 0;
		
		i = 0;
		len = arr.length;
		while(i < len) {
			sum2 += arr.aryPtr[i];
			i += 1;
		}
		
		return sum2;
	}
	
	private boolean getIsTypeMatchLen2(TypeAndObject n, int type1, int type2) {
		if(n.theType != type1) return false;
		if(n.theSubType != type2) return false;
		return true;
	}


	// testing access functions
	//
	
	private StorageBlockCursor getCharReaderCursor(CharReaderAccess charRead) {
		CharReader3 charReadB = (CharReader3) charRead;
		StorageBlockCursor cur = (StorageBlockCursor) charReadB.dat.curRead;
		return cur;
	}

	private StorageBlockCursor getStringReaderCursor(StringReaderAccess strRead) {
		StringReader3 strReadB = (StringReader3) strRead;
		return getCharReaderCursor(strReadB.charRead);
	}
	
	private StorageBlockCursor getCharWriterCursor(CharWriter3 charWrite) {
		StorageBlockCursor cur = (StorageBlockCursor) charWrite.dat.curWrite;
		return cur;
	}

	private StorageBlockCursor getStringWriterCursor(StringWriter3 strWrite) {
		return getCharWriterCursor(strWrite.charWrite);
	}
	
	private long getCursorTrace1(StorageBlockCursor cur) {
		return cur.trace1Count;
	}

	private long getCursorTrace2(StorageBlockCursor cur) {
		return cur.trace2Count;
	}

	private long getCharReadTrace1(CharReaderAccess charRead) {
		CharReader3 charReadB = (CharReader3) charRead;
		return charReadB.trace1Count;
	}
	
	private long getCharReadTrace2(CharReaderAccess charRead) {
		CharReader3 charReadB = (CharReader3) charRead;
		return charReadB.trace2Count;
	}
	
	private long getCharReadTrace3(CharReaderAccess charRead) {
		CharReader3 charReadB = (CharReader3) charRead;
		return charReadB.trace3Count;
	}
	
	private long getCharReadTrace4(CharReaderAccess charRead) {
		CharReader3 charReadB = (CharReader3) charRead;
		return charReadB.trace4Count;
	}
	
	private long getCharReadTrace5(CharReaderAccess charRead) {
		CharReader3 charReadB = (CharReader3) charRead;
		return charReadB.trace5Count;
	}


	// testing counting functions
	//

	private void transferAllocCounts(CFamilySimpleTokenReader2Data rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferAllocCounts2(GrammarReaderData rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferAllocCounts3(KeywordTokenFilterData rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferAllocCounts4(LrGramReaderData rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferStrangeAllocCounts(CFamilySimpleTokenReader2Data rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
		
		if(aCount > 0) {
			printLine("transferStrange,TokenReader,old," + aCount);
		}

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,TokenReader,new," + aCount);
		}

		aCount = rdrDat.traceStrangeAllocCount;
		rdrDat.traceStrangeAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,TokenReader,strange," + aCount);
		}
	}

	private void transferStrangeAllocCounts2(GrammarReaderData rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,GrammarReader,old," + aCount);
		}

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,GrammarReader,new," + aCount);
		}

		aCount = rdrDat.traceStrangeAllocCount;
		rdrDat.traceStrangeAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,GrammarReader,strange," + aCount);
		}
	}

	private void transferStrangeAllocCounts3(KeywordTokenFilterData rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,KeywordTokenFilter,old," + aCount);
		}

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,KeywordTokenFilter,new," + aCount);
		}

		aCount = rdrDat.traceStrangeAllocCount;
		rdrDat.traceStrangeAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,KeywordTokenFilter,strange," + aCount);
		}
	}
	
	private void transferStrangeAllocCounts4(LrGramReaderData rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,LrGramReader,old," + aCount);
		}

		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		if(aCount > 0) {
			printLine("transferStrange,LrGramReader,new," + aCount);
		}
	}
	
	
	// error allocators
	//
	
	private CommonError makeObjectNotFound(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeObjectUnexpected(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNEXPECTED_OBJECT;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private CommonError makeOutOfBounds(String msg) {
		CommonError e4;
		
		e4 = new CommonError();
		e4.id = unnamed.common.CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e4.msg = msg;
		return e4;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}


	// stack allocators
	//
	
	private CFamilyTrimStat getTrimStatObject(
		CommonArrayList store, int index) {
		
		int count;
		CFamilyTrimStat stat;
		
		count = store.size();
		while(count <= index) {
			store.add(makeTrimStatObject());
			count = store.size();
		}
		
		stat = (CFamilyTrimStat) store.get(index);
		return stat;
	}


	// general allocators
	//
	
	private CommonArrayList makeListWithSingleObject(Object o) {
		CommonArrayList lst;
		
		if(o == null) return null;
		
		lst = makeArrayList();
		lst.add(o);
		return lst;
	}

	private CommonInt32Array makeLineReturnStr() {
		CommonInt32Array str = makeInt32Array(2);
		str.aryPtr[0] = 13;
		str.aryPtr[1] = 10;
		return str;
	}

	private CommonInt32Array makeSpaceStr() {
		CommonInt32Array str = makeInt32Array(1);
		str.aryPtr[0] = ' ';
		return str;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
	
	private SymbolAllocHelper makeSymbolAllocHelper() {
		SymbolAllocHelper2 allocHelp;
		
		allocHelp = new SymbolAllocHelper2();
		allocHelp.init();
		return allocHelp;
	}
	
	private CFamilyTrimStat makeTrimStatObject() {
		return new CFamilyTrimStat();
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;
		
		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}

	private CompareParams makeCompareParams() {
		CompareParams compRec;
		
		compRec = new CompareParams();
		return compRec;
	}

	
	// native string small utility functions
	//
	
	private CommonInt32Array makeStandardString(String str) {
		return StringUtils.int32StringFromJavaString(str);
	}

	private String makeNativeString(CommonInt32Array str) {
		return StringUtils.javaStringFromInt32String(str);
	}
	
	private String stringEmptyToNull(String s) {
		if(s == null) return null;
		if(s.equals("")) return null;
		return s;
	}

	private boolean testStringStartsWith(String a, String startPart) {
		if(a == null) return false;
		if(startPart == null) return false;
		return a.startsWith(startPart);
	}
	
	private boolean testStringEquals(String a, String b) {
		if(a == null) return false;
		if(b == null) return false;
		return a.equals(b);
	}
	
	private String getSymbolString1(Symbol sym) {
		return StringUtils.javaStringFromInt32String(
			getSymbolString2(sym));
	}

	private boolean writeStr1(String str) {
		return writeStr(
			StringUtils.int32StringFromJavaString(str));
	}
	
	
	// native string console output stuff
	//
	
	private void printLine(String str) {
		out.println(indentString() + str);
		return;
	}

	private String indentString() {
		int i;
		int len;
		StringBuilder sb = new StringBuilder();
		
		i = 0;
		len = dat.indentCount;
		while(i < len) {
			sb.append(dat.INDENT_STRING);
			
			i += 1;
		}
		
		return sb.toString();
	}
	
	private String getIndexString(TextIndex ti) {
		return "(" + ti.index
			+ "," + ti.line
			+ "," + ti.indexWithinLine
			+ ")";
	}
	
	
	// command loop
	//
	
	public void run() {
		int i;
		int count;
		String[] args;
		String arg;
		String nextArg;
		boolean isError;
		CommonInt32Array path;
		boolean ok;
		
		long t1;
		long t2;
		
		t1 = System.currentTimeMillis();
		
		args = dat.args;
		
		if(args == null) return;

		initOnce();
				
		count = args.length;
		i = 0;
		while(i < count) {
			arg = args[i];
			nextArg = null;
			if(i + 1 < count) nextArg = args[i + 1];
			
			if(arg == null) {
				i += 1;
				continue;
			}
			
			if(!testStringStartsWith(arg, "--")) {
				printLine("Argument is not in the form --command");
				return;
			}
			
			if(testStringEquals(arg, "--create-file-path-node")) {
				if(nextArg == null) {
					printLine("No argument for --create-file-path-node command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				createFilePathNode(path);

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--open-local-file-system")) {
				if(nextArg == null) {
					printLine("No argument for --open-local-file-system command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				openLocalFileSystem(path,
					(short) (AccessRights.ACCESS_READ | AccessRights.ACCESS_WRITE));

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--open-local-file-system-read-only")) {
				if(nextArg == null) {
					printLine("No argument for --open-local-file-system command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				openLocalFileSystem(path, (short) AccessRights.ACCESS_READ);

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--grammar-dir")) {
				if(nextArg == null) {
					printLine("No argument for --grammar-dir option");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				setGrammarDirWithFullPath(path);

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--def-dir")) {
				if(nextArg == null) {
					printLine("No argument for --def-dir option");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				setDefDirWithFullPath(path);

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--test-dir")) {
				if(nextArg == null) {
					printLine("No argument for --test-dir option");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				setTestDirWithFullPath(path);

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--is-target")) {
				if(nextArg == null) {
					printLine("No argument for --is-target option");
					return;
				}
				
				ok = false;
				
				if(testStringEquals(nextArg, "true")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_TARGET,
						true);
					ok = true;
				}

				if(testStringEquals(nextArg, "false")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_TARGET,
						false);
					ok = true;
				}
				
				if(!ok) {
					printLine("Bad argument for --is-target");
					return;
				}
				
				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--is-outside")) {
				if(nextArg == null) {
					printLine("No argument for --is-outside option");
					return;
				}
				
				ok = false;
				
				if(testStringEquals(nextArg, "true")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_OUTSIDE,
						true);
					ok = true;
				}

				if(testStringEquals(nextArg, "false")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_OUTSIDE,
						false);
					ok = true;
				}
				
				if(!ok) {
					printLine("Bad argument for --is-outside");
					return;
				}
				
				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--is-cache")) {
				if(nextArg == null) {
					printLine("No argument for --is-cache option");
					return;
				}
				
				ok = false;
				
				if(testStringEquals(nextArg, "true")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_CACHE,
						true);
					ok = true;
				}

				if(testStringEquals(nextArg, "false")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_CACHE,
						false);
					ok = true;
				}
				
				if(!ok) {
					printLine("Bad argument for --is-cache");
					return;
				}
				
				i += 2;
				continue;
			}
			
			if(testStringEquals(arg, "--is-override")) {
				if(nextArg == null) {
					printLine("No argument for --is-override option");
					return;
				}
				
				ok = false;
				
				if(testStringEquals(nextArg, "true")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_OVERRIDE,
						true);
					ok = true;
				}

				if(testStringEquals(nextArg, "false")) {
					dat.testFlags = FlagUtils.setFlagInt16(
						dat.testFlags,
						TranslateOldTest1Data.FLAG_OVERRIDE,
						false);
					ok = true;
				}
				
				if(!ok) {
					printLine("Bad argument for --is-cache");
					return;
				}
				
				i += 2;
				continue;
			}
			
			if(testStringEquals(arg, "--file-name-type")) {
				if(nextArg == null) {
					printLine("No argument for --file-name-type option");
					return;
				}
				
				path = StringUtils.int32StringFromJavaString(nextArg);
				setNameType(path);
				
				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--grammar-file")) {
				if(nextArg == null) {
					printLine("No argument for --grammar-file command");
					return;
				}
				
				t1 = System.currentTimeMillis();

				path = StringUtils.int32StringFromJavaString(nextArg);
				initGrammarFileWithPath(path);
				isError = loadGrammarFile();
				cleanupGrammarFile();
				
				if(isError) return;
				
				t2 = System.currentTimeMillis();
				
				printLine("Grammar Load Time," + (t2 - t1) / 1000.0);

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--test-file")) {
				if(nextArg == null) {
					printLine("No argument for --test-file command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				initTestFile(path);
				isError = runTestFile();
				cleanupTestFile();
				
				if(isError) return;

				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--logic-file")) {
				if(nextArg == null) {
					printLine("No argument for --logic-file command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				addLogicFile(path);
				
				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--doc-file")) {
				if(nextArg == null) {
					printLine("No argument for --doc-file command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(nextArg);
				addDocFile(path);
				
				i += 2;
				continue;
			}

			if(testStringEquals(arg, "--logic-files-all")) {
				addLogicFilesAll();
				
				i += 1;
				continue;
			}

			if(testStringEquals(arg, "--doc-files-all")) {
				addDocFilesAll();
				
				i += 1;
				continue;
			}

			if(testStringEquals(arg, "--translate")) {
				if(checkTestSelectionOverlap()) {
					printLine("Error.  Selected files overlap.");
					return;
				}
				/*
				if(!checkSelectInfoExist(
					TranslateRegularTest1Data.SELECT_MAIN_SOURCE)) {
					
					printLine("Error.  Zero source files selected.");
					return;
				}
				*/
				
				dat.okTestFileCount = 0;
				
				t1 = System.currentTimeMillis();

				walkSource();

				t2 = System.currentTimeMillis();
				
				printLine("Translate Time," + (t2 - t1) / 1000.0);
				printLine("okTestFileCount,value," + dat.okTestFileCount);
				
				if(true) {
					printLine("traceOldAllocCount,"
						+ dat.traceOldAllocCount);
					printLine("traceNewAllocCount,"
						+ dat.traceNewAllocCount);
					printLine("traceStrangeAllocCount,"
						+ dat.traceStrangeAllocCount);
					printLine("traceSpecificAllocCount,"
						+ dat.traceSpecificAllocCount);
					
					SymbolAllocHelper2 allocHelp = (SymbolAllocHelper2) dat.loadingAllocHelp;
					printLine("traceBufferAllocCount,"
						+ allocHelp.traceBufferAllocCount);
					printLine("traceNewBufferAllocCount,"
						+ allocHelp.traceNewBufferAllocCount);
					
					if(false) {
						printLine("HELLO!");
					}
				}

				dat.okTestFileCount = 0;

				i += 1;
				continue;
			}
			
			printLine("Invalid command " + arg);
			return;
		}
		
		return;
	}
	
	
	// general program init
	//

	public static void main(String[] args) {
		TranslateOldTest1 o = new TranslateOldTest1();
		o.dat = new TranslateOldTest1Data();
		o.out = System.out;

		o.dat.args = args;

		o.run();
	}
}
