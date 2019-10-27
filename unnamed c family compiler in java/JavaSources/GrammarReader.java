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
import unnamed.file.system.*;

public class GrammarReader implements BaseModule {
	public GrammarReaderData dat;
	public GeneralUtils utils;
	public TokenUtils tokUtils;
	public PublicLinkUtils linkUtils;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}

	public BaseModuleData getData() {return dat;}

	public CommonArrayList getChildModules() {
		CommonArrayList o;
		CommonArrayList src;
		GrammarSource grmrMod;
		
		int i;
		int len;
		
		o = makeArrayList();
		src = dat.sourceStack;
		
		i = 0;
		len = src.size();
		while(i < len) {
			grmrMod = (GrammarSource) src.get(i);
			addExistingModule(o, grmrMod);
			
			i += 1;
		}

		return o;
	}
		
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		if(tokUtils == null) return true;
		if(linkUtils == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}

	public void reset() {
		dat.nameList.clear();
		dat.ruleList.clear();
		dat.rootVariableName = null;
		dat.endMarkerName = null;
		dat.precSpectrumName = null;
		
		dat.state = BaseModuleData.STATE_START;
		return;
	}
	
	public boolean isRegularFilePath(CommonInt32Array path) {
		CommonInt32Array sepSet = dat.REGULAR_FILE_PATH_SEPARATOR_SET;
		CommonInt32Array badSet = dat.FILE_PATH_SPECIAL_CHAR_SET;
		
		if(!PathUtils.isPurePath(path, sepSet))
			return false;
		if(PathUtils.pathHasBadChars(path, badSet))
			return false;
		
		return true;
	}
	
	// walking move functions
	//
	
	public int move(int direction) {
		int moveResult;
		
		Throwable ex;
		
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		
		try {
			if(direction != ModuleMoveDirection.TO_NEXT)
				return moveResult;
			
			if(dat.state == BaseModuleData.STATE_STUCK)
				return ModuleMoveResult.STUCK;

			if(dat.state == BaseModuleData.STATE_START) {
				moveResult = moveSourceStack();
				return moveResult;
			}
			
			if(dat.state == GrammarReaderData.STATE_HAVE_GRAMMAR)
				return ModuleMoveResult.AT_END;
						
			throw makeInvalidEnum("GrammarDefReader has a bad state");
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			fireRuntimeError(ex);
			moveResult = ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
	
	private int addZeroToken() {
		SortParams sortP;
		CommonInt32Array name;
		TypeAndObject newRec;
		SymbolId symIdRec;

		name = makeInt32Array(0);
		
		sortP = makeSortParams();

		SortUtils.int32StringBinaryLookupSimple(dat.nameList, 
			name,
			sortP);
		
		if(sortP.index != 0) {
			fireRuntimeError(makeUnknownError(
				"index from sort should be 0"));
			return ModuleMoveResult.STUCK;
		}

		if(sortP.foundExisting)
			return ModuleMoveResult.SUCCESS;
		
		newRec = new TypeAndObject();
		newRec.sortObject = name;
		newRec.theType = GrammarNameTypes.TYPE_TOKEN_DEF;
		
		symIdRec = makeSymbolId(0);
		//symIdRec.id = symbolId;
		
		newRec.theObject = symIdRec;
		
		dat.nameList.addAt(sortP.index, newRec);
		
		return ModuleMoveResult.SUCCESS;
	}

	private int finalizeGrammar() {
		int moveResult;
		
		moveResult = addZeroToken();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;

		moveResult = checkUnknownNames();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		// done
		dat.state = GrammarReaderData.STATE_HAVE_GRAMMAR;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int checkUnknownNames() {
		int i;
		int count;
		TypeAndObject nameRec;
		boolean bad;
		
		bad = false;
		i = 0;
		count = dat.nameList.size();
		while(i < count) {
			nameRec = (TypeAndObject) dat.nameList.get(i);
			
			if(nameRec.theType == GrammarNameTypes.TYPE_UNKNOWN) {
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeGrammarUnknownName((CommonInt32Array) nameRec.sortObject));
				
				bad = true;
			}
				
			i += 1;
		}

		if(bad) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_PERMANENT;
			return ModuleMoveResult.STUCK;
		}
		
		return ModuleMoveResult.SUCCESS;
	}
	
	public int moveSourceStack() {
		int moveResult;
		
		Throwable ex;
				
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		
		try {
			moveResult = moveSourceStackThrow();
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			fireRuntimeError(ex);
			moveResult = ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
			
	public int moveSourceStackThrow() {
		GrammarSource grmr;
		GrammarSource grmr2;
		GrammarSourceData grmrDat;
		int moveResult;
		Symbol sym;
		Symbol sym2;
		int id;
		CommonInt32Array filePath;
		FileNode2 fileContext;
		FileRef2 f;
		
		dat.sourceStack.clear();

		if(!isRegularFilePath(dat.grammarFilePath)) {
			fireRuntimeError(
				makeInvalidFilePath2(
					"grammarFilePath is not a pure file path"));
			return ModuleMoveResult.STUCK;
		}
		
		fileContext = searchAndGetFileContext(dat.grammarFilePath);
		if(fileContext == null)
			throw makeObjectNotFound(null);
		
		f = FileNode2Utils.openNormalFile(
			fileContext, (short) AccessRights.ACCESS_READ,
			GrammarReaderData.INTERNAL_SEP_CHAR);
		
		grmr2 = makeGrammarSource(fileContext, f, dat.grammarFilePath);

		fileContext = null;
		f = null;
		
		dat.sourceStack.clear();
		dat.sourceStack.add(grmr2);
		dat.sourceStackIndex = 0;

		grmr = (GrammarSource)
			dat.sourceStack.get(dat.sourceStackIndex);
		grmrDat = (GrammarSourceData) grmr.getData();
		
		while(true) {
			if(dat.sourceStack.size() == 0)
				return finalizeGrammar();
			
			transferStrangeAllocCounts3(grmr.tokenRead.dat);
			
			moveResult = moveGrammarToNext(grmr);

			transferAllocCounts3(grmr.tokenRead.dat);
			
			if(moveResult == ModuleMoveResult.AT_END) {
				removeStack(dat.sourceStackIndex);
				// todo: move stored problems from done
				// grammar files to dat.probBag
				
				if(dat.sourceStackIndex == 0) {
					grmr = null;
					continue;
				}
					
				dat.sourceStackIndex -= 1;
				grmr = (GrammarSource)
					dat.sourceStack.get(dat.sourceStackIndex);
				grmrDat = (GrammarSourceData) grmr.getData();
				continue;
			}
			
			if(moveResult == ModuleMoveResult.STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return moveResult;
			}
			
			if(moveResult != ModuleMoveResult.SUCCESS)
				throw makeInvalidEnum(
					"moveGrammarToNext returned a bad move result");
			
			if(grmrDat.state != GrammarReaderData.STATE_HAVE_SYMBOL)
				throw makeInvalidEnum(
					"moveGrammarToNext returned a bad state");
			
			sym = grmrDat.completeSym;
			id = utils.getSymbolIdPrimary(sym);
			
			if(id == Symbols.GRAM_GRAMMAR_INCLUDE_GRAMMAR) {
				GramContainer gc5 = (GramContainer) sym;
				sym2 = gc5.sym[2];
				
				grmr.strEval.dat.strTok = (TokenString) sym2;
				grmr.strEval.readInt32String();
				
				if(grmr.strEval.dat.state == BaseModuleData.STATE_STUCK) {
					grmrDat.state = BaseModuleData.STATE_STUCK;
					grmrDat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
					return ModuleMoveResult.STUCK;
				}
				
				if(grmr.strEval.dat.state !=
					TokenStringEvalData.STATE_HAVE_INT32_STRING)
					throw makeInvalidEnum("TokenStringEval bad state");
				
				filePath = grmr.strEval.dat.resultInt32String;
				
				if(filePath == null)
					throw makeNullPointerException(null);
				
				if(!isRegularFilePath(filePath)) {
					// note failure
					grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
						makeInvalidFilePath(sym2.startIndex));

					continue;
				}
					
				if(checkIncludeLoop(filePath)) {
					// note failure
					grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
						makeIncludeLoopError(sym.startIndex));
					
					continue;
				}
				
				fileContext = searchAndGetFileContext(filePath);
				if(fileContext == null) {
					// todo, add text range of file path
					
					grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
						makeObjectNotFound(null));
					
					continue;
				}
				
				f = FileNode2Utils.openNormalFile(
					fileContext, (short) AccessRights.ACCESS_READ,
					GrammarReaderData.INTERNAL_SEP_CHAR);

				grmr2 = makeGrammarSource(fileContext, f, filePath);
				
				f = null;
				fileContext = null;

				dat.sourceStack.add(grmr2);
				dat.sourceStackIndex += 1;
				
				// grmr set to grmr2
				grmr = (GrammarSource)
					dat.sourceStack.get(dat.sourceStackIndex);
				grmrDat = (GrammarSourceData) grmr.getData();
				continue;
			}
			
			if(id == Symbols.TOKEN_END_OF_STREAM
				|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM) {
				
				continue;
			}
			
			moveResult = commitGrammarStatement(grmr, grmrDat.completeSym);
			
			if(moveResult == ModuleMoveResult.STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return moveResult;
			}
			
			if(moveResult != ModuleMoveResult.SUCCESS)
				throw makeInvalidEnum(
					"moveGrammarToNext returned a bad move result");
			
			continue;
		}
		
		// unreachable
	}
	
	private int commitGrammarStatement(
		GrammarSource grmr, Symbol sym) {
		
		GrammarSourceData grmrDat;
		int id;
		int moveResult;
		boolean done;
		
		Throwable ex;
		
		grmrDat = (GrammarSourceData) grmr.getData();
		ex = null;
		id = utils.getSymbolIdPrimary(sym);
		done = false;
		moveResult = ModuleMoveResult.INVALID;
		
		try {
			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_RULE_LEFT) {
				moveResult = commitRuleLeft(grmr, (GramContainer) sym);
				done = true;
			}

			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_TOKEN) {
				moveResult = commitTokenDef(grmr, (GramContainer) sym);
				done = true;
			}

			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_GRAM) {
				moveResult = commitGramDef(grmr, (GramContainer) sym);
				done = true;
			}
			
			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_ASSOCIATIVITY) {
				moveResult = commitAssoc(grmr, (GramContainer) sym);
				done = true;
			}
			
			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_ROOT_VARIABLE) {
				moveResult = commitRootVariable(grmr, (GramContainer) sym);
				done = true;
			}
			
			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_END_MARKER) {
				moveResult = commitEndMarker(grmr, (GramContainer) sym);
				done = true;
			}
			
			if(!done)
			if(id == Symbols.GRAM_GRAMMAR_PRECEDENCE_SPECTRUM) {
				moveResult = commitPrecSpectrum(grmr, (GramContainer) sym);
				done = true;
			}

			if(!done) {
				System.out.println("statement with id="
					+ utils.getSymbolIdPrimary(sym)
					+ " and sourceStackIndex="
					+ dat.sourceStackIndex);
				moveResult = ModuleMoveResult.SUCCESS;
			}
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, ex);
						
			grmrDat.state = BaseModuleData.STATE_STUCK;
			grmrDat.stuckState = StuckStates.STATE_PERMANENT;
			return ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}

	private int commitRootVariable(
		GrammarSource grmrMod, GramContainer rootVarGrm) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		long len;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		TypeAndObject newRec;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		sym = rootVarGrm.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		len = sym.pastIndex.index - sym.startIndex.index;
		grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
		name = StringUtils.int32StringFromUtf32(
			strDat.resultBufferString);
		
		existRec = null;
		
		sortP = makeSortParams();
		SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
		
		if(sortP.foundExisting) {
			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);
		}
		
		if(!sortP.foundExisting) {
			newRec = new TypeAndObject();
			newRec.sortObject = name;
			newRec.theType = GrammarNameTypes.TYPE_UNKNOWN;
			newRec.theObject = null;

			dat.nameList.addAt(sortP.index, newRec);
			
			existRec = newRec;
		}
		
		dat.rootVariableName = existRec;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int commitEndMarker(
		GrammarSource grmrMod, GramContainer endMarkGrm) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		long len;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		TypeAndObject newRec;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		sym = endMarkGrm.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		len = sym.pastIndex.index - sym.startIndex.index;
		grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
		name = StringUtils.int32StringFromUtf32(
			strDat.resultBufferString);
		
		existRec = null;
		
		sortP = makeSortParams();
		SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
		
		if(sortP.foundExisting) {
			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);
		}
		
		if(!sortP.foundExisting) {
			newRec = new TypeAndObject();
			newRec.sortObject = name;
			newRec.theType = GrammarNameTypes.TYPE_UNKNOWN;
			newRec.theObject = null;

			dat.nameList.addAt(sortP.index, newRec);
			
			existRec = newRec;
		}
		
		dat.endMarkerName = existRec;
		return ModuleMoveResult.SUCCESS;
	}

	private int commitPrecSpectrum(
		GrammarSource grmrMod, GramContainer spectrumGrm) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		long len;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		TypeAndObject newRec;
		GrammarPrecedenceSpectrumInterim spectrumRec;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();

		sym = spectrumGrm.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		len = sym.pastIndex.index - sym.startIndex.index;
		grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
		name = StringUtils.int32StringFromUtf32(
			strDat.resultBufferString);
		
		existRec = null;
		
		sortP = makeSortParams();
		SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
		
		if(sortP.foundExisting) {
			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);

			// fire existing name

			grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeNameAlreadyExists("name already exists"));
			
			return ModuleMoveResult.SUCCESS;
		}
		
		if(!sortP.foundExisting) {
			newRec = new TypeAndObject();
			newRec.sortObject = name;
			newRec.theType = GrammarNameTypes.TYPE_PRECEDENCE_SPECTRUM;
			
			spectrumRec = new GrammarPrecedenceSpectrumInterim();
			spectrumRec.lines = makeArrayList();
			
			newRec.theObject = spectrumRec;

			dat.nameList.addAt(sortP.index, newRec);
			
			existRec = newRec;
		}
		
		dat.precSpectrumName = existRec;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int commitGetSymbolId(
		GrammarSource grmrMod, GramContainer idDefGrm,
		ObjectRef result) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		GramContainerList grmList;
		SymbolId symId;
		int i;
		int len;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		sym = idDefGrm.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.GRAM_CATEGORY_DATA,
			Symbols.GRAM_SEQUENCE_INTEGER))
			throw makeInvalidEnum("unexpected type");
		
		grmList = (GramContainerList) sym;
		
		len = grmList.symList.size();
		symId = makeSymbolId(len);
		i = 0;		
		while(i < len) {
			sym = (Symbol) grmList.symList.get(i);
			
			if(checkMatchSymbolId(sym,
				Symbols.TOKEN_CATEGORY_NUMBER,
				Symbols.TOKEN_INTEGER_FULL))
				throw makeInvalidEnum("unexpected type");
			
			grmrMod.intEval.dat.fullIntTok = (TokenIntegerFull) sym;
			grmrMod.intEval.readInteger32FromFull();
			
			if(grmrMod.intEval.dat.state == BaseModuleData.STATE_STUCK) {
				grmrDat.state = BaseModuleData.STATE_STUCK;
				grmrDat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return ModuleMoveResult.STUCK;
			}
			
			if(grmrMod.intEval.dat.state
				!= TokenIntegerEvalData.STATE_HAVE_INT_32)
				throw makeInvalidEnum("TokenIntegerEval bad state");
			
			symId.aryPtr[i] = grmrMod.intEval.dat.resultInt32;
			
			i += 1;
		}
		
		result.value = symId;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int commitGetTokenList(
		GrammarSource grmrMod, Symbol sym,
		ObjectRef result) {
		
		GrammarSourceData grmrDat;
		GramContainerList grmList;
		int i;
		int len;
		long len2;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		CommonArrayList tokList;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		tokList = makeArrayList();
		sortP = makeSortParams();
		
		if(checkMatchSymbolId(sym,
			Symbols.GRAM_CATEGORY_DATA,
			Symbols.GRAM_SEQUENCE_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		grmList = (GramContainerList) sym;
		
		len = grmList.symList.size();
		i = 0;		
		while(i < len) {
			sym = (Symbol) grmList.symList.get(i);
			
			if(checkMatchSymbolId(sym,
				Symbols.TOKEN_CATEGORY_BASIC,
				Symbols.TOKEN_IDENTIFIER))
				throw makeInvalidEnum("unexpected type");

			len2 = sym.pastIndex.index - sym.startIndex.index;
			grmrMod.strRead.readUtf32Throw(sym.startIndex, len2);
			name = StringUtils.int32StringFromUtf32(
				strDat.resultBufferString);

			sortP = makeSortParams();
			SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
			if(!sortP.foundExisting) {
				grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeGrammarUnknownName(name));
				grmrDat.state = BaseModuleData.STATE_STUCK;
				grmrDat.stuckState = StuckStates.STATE_PERMANENT;
				return ModuleMoveResult.STUCK;
			}

			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);

			if(existRec.theType != GrammarNameTypes.TYPE_TOKEN_DEF) {
				grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeGrammarUnexpectedNameType(
						"expected token name",
						name));
				grmrDat.state = BaseModuleData.STATE_STUCK;
				grmrDat.stuckState = StuckStates.STATE_PERMANENT;
				return ModuleMoveResult.STUCK;
			}
			
			tokList.add(existRec);
			
			i += 1;
		}
		
		result.value = tokList;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int commitTokenDef(
		GrammarSource grmrMod, GramContainer tokenDefGrm) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		long len;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		ObjectRef symIdResult;
		int moveResult;
		TypeAndObject newRec;
		SymbolId symId2;
		SymbolId symId1;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		sym = tokenDefGrm.sym[3];
		
		if(checkMatchSymbolId(sym,
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_ID))
			throw makeInvalidEnum("unexpected type");
		
		symIdResult = new ObjectRef();
		
		moveResult = commitGetSymbolId(
			grmrMod, (GramContainer) sym, symIdResult);
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		sym = tokenDefGrm.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		len = sym.pastIndex.index - sym.startIndex.index;
		grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
		name = StringUtils.int32StringFromUtf32(
			strDat.resultBufferString);
		
		sortP = makeSortParams();
		SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
		if(sortP.foundExisting) {
			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);

			// fire existing name

			grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeNameAlreadyExists("name already exists"));
			
			return ModuleMoveResult.SUCCESS;
		}
		
		newRec = new TypeAndObject();
		newRec.sortObject = name;
		newRec.theType = GrammarNameTypes.TYPE_TOKEN_DEF;
		
		symId1 = (SymbolId) symIdResult.value;
		
		symId2 = makeSymbolId(symId1.length);
		utils.copySymbolId(symId2, symId1);
		
		newRec.theObject = symId2;
		
		dat.nameList.addAt(sortP.index, newRec);
		return ModuleMoveResult.SUCCESS;
	}

	private int commitAssoc(
		GrammarSource grmrMod, GramContainer assocGrm) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		Symbol sym2;
		long len;
		SortParams sortP;
		TypeAndObject spectrumNameRec;
		GrammarPrecedenceSpectrumInterim spectrumRec;
		ObjectRef tokListResult;
		int moveResult;
		GrammarPrecedenceLineInterim newRec;
		SymbolId symIdRec;
		int id;
		int id2;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		sym = assocGrm.sym[2];
		
		if(sym == null) {
			// statement has zero tokens specified
			// ignore
			return ModuleMoveResult.SUCCESS;
		}
		
		tokListResult = new ObjectRef();
		
		moveResult = commitGetTokenList(
			grmrMod, sym, tokListResult);
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		spectrumNameRec = dat.precSpectrumName;
		
		if(spectrumNameRec == null) {
			grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeUnspecifiedName());
			grmrDat.state = BaseModuleData.STATE_STUCK;
			grmrDat.stuckState = StuckStates.STATE_PERMANENT;
			return ModuleMoveResult.STUCK;
		}
		
		spectrumRec = (GrammarPrecedenceSpectrumInterim)
			spectrumNameRec.theObject;
		
		newRec = new GrammarPrecedenceLineInterim();
		
		sym2 = assocGrm.sym[1];
		if(sym2 == null)
			throw makeNullPointerException(null);
		
		id2 = utils.getSymbolIdPrimary(sym2);
		
		newRec.assocType = GrammarAssociativityTypes.TYPE_UNKNOWN;		
		if(id2 == Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_NON)
			newRec.assocType = GrammarAssociativityTypes.TYPE_NON;
		if(id2 == Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_LEFT)
			newRec.assocType = GrammarAssociativityTypes.TYPE_LEFT;
		if(id2 == Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_RIGHT)
			newRec.assocType = GrammarAssociativityTypes.TYPE_RIGHT;
		if(newRec.assocType == GrammarAssociativityTypes.TYPE_UNKNOWN)
			throw makeInvalidEnum(
				"associativity has unknown type");
		
		newRec.tokList = (CommonArrayList) tokListResult.value;
		
		spectrumRec.lines.add(newRec);
		return ModuleMoveResult.SUCCESS;
	}
	
	private int commitGramDef(
		GrammarSource grmrMod, GramContainer gramDefGrm) {
		
		GrammarSourceData grmrDat;
		Symbol sym;
		long len;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		ObjectRef symIdResult;
		int moveResult;
		TypeAndObject newRec;
		SymbolId symId2;
		SymbolId symId1;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		sym = gramDefGrm.sym[3];
		
		if(checkMatchSymbolId(sym,
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_ID))
			throw makeInvalidEnum("unexpected type");
		
		symIdResult = new ObjectRef();
		
		moveResult = commitGetSymbolId(
			grmrMod, (GramContainer) sym, symIdResult);
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		sym = gramDefGrm.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		len = sym.pastIndex.index - sym.startIndex.index;
		grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
		name = StringUtils.int32StringFromUtf32(
			strDat.resultBufferString);
		
		sortP = makeSortParams();
		SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
		if(sortP.foundExisting) {
			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);

			// fire existing name

			grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeNameAlreadyExists("name already exists"));
			
			return ModuleMoveResult.SUCCESS;
		}
		
		newRec = new TypeAndObject();
		newRec.sortObject = name;
		newRec.theType = GrammarNameTypes.TYPE_GRAM_DEF;
		
		symId1 = (SymbolId) symIdResult.value;
		
		symId2 = makeSymbolId(symId1.length);
		utils.copySymbolId(symId2, symId1);
		
		newRec.theObject = symId2;
		
		dat.nameList.addAt(sortP.index, newRec);
		return ModuleMoveResult.SUCCESS;
	}
	
	private int commitRuleLeft(
		GrammarSource grmrMod, GramContainer ruleLeftGrm) {

		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Symbol sym;
		long len;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		TypeAndObject newRec;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		int i;
		int count;
		GramContainerList grmList;
		int moveResult;
		
		grmCon = ruleLeftGrm;
		
		sym = grmCon.sym[2];
		
		if(checkMatchSymbolId(sym,
			Symbols.TOKEN_CATEGORY_BASIC,
			Symbols.TOKEN_IDENTIFIER))
			throw makeInvalidEnum("unexpected type");
		
		len = sym.pastIndex.index - sym.startIndex.index;
		grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
		name = StringUtils.int32StringFromUtf32(
			strDat.resultBufferString);
		
		sortP = makeSortParams();
		SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
		existRec = null;
		if(sortP.foundExisting) {
			existRec = (TypeAndObject)
				dat.nameList.get(sortP.index);

			if(existRec.theType == GrammarNameTypes.TYPE_UNKNOWN) {
				existRec.theType = GrammarNameTypes.TYPE_VARIABLE;
			}
			
			if(existRec.theType != GrammarNameTypes.TYPE_VARIABLE) {
				// fire existing name

				grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeNameAlreadyExists("name already exists"));

				return ModuleMoveResult.SUCCESS;
			}
		} else {
			newRec = new TypeAndObject();
			newRec.sortObject = name;

			newRec.theObject = null;
			newRec.theType = GrammarNameTypes.TYPE_VARIABLE;

			dat.nameList.addAt(sortP.index, newRec);
			
			existRec = newRec;
		}
		
		sym = grmCon.sym[3];

		if(checkMatchSymbolId(sym,
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_RULE_RIGHT_SEQUENCE))
			throw makeInvalidEnum("unexpected type");
		
		grmList = (GramContainerList) sym;
		i = 0;
		count = grmList.symList.size();
		while(i < count) {
			sym = (Symbol) grmList.symList.get(i);
			
			if(checkMatchSymbolId(sym,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_RULE_RIGHT))
				throw makeInvalidEnum("unexpected type");
			
			moveResult = commitRuleRight(
				grmrMod, (GramContainer) sym, existRec);
			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;

			i += 1;
		}
		
		return ModuleMoveResult.SUCCESS;
	}
		
	private int commitRuleRight(
		GrammarSource grmrMod, GramContainer ruleRightGrm,
		TypeAndObject leftVar) {

		GrammarSourceData grmrDat;
		GrammarRuleInterim rule;
		Symbol sym;
		GramContainer grmCon;
		GramContainer grmCon2;
		GramContainerList grmList;
		int i;
		int count;
		long len;
		long len2;
		CommonInt32Array name;
		SortParams sortP;
		TypeAndObject existRec;
		TypeAndObject newRec;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		StringReader2Data strDat = (StringReader2Data) grmrMod.strRead.getData();
		
		grmCon = ruleRightGrm;
		
		rule = new GrammarRuleInterim();
		rule.leftVar = leftVar;
		rule.rightList = makeArrayList();

		rule.precedenceTok = null;
		rule.reduceGrm = null;
		
		sym = grmCon.sym[2];
		
		if(sym != null) {

			if(checkMatchSymbolId(sym,
				Symbols.GRAM_CATEGORY_DATA,
				Symbols.GRAM_SEQUENCE_IDENTIFIER))
				throw makeInvalidEnum("unexpected type");

			sortP = makeSortParams();

			i = 0;
			grmList = (GramContainerList) sym;
			count = grmList.symList.size();
			while(i < count) {
				sym = (Symbol) grmList.symList.get(i);

				if(checkMatchSymbolId(sym,
					Symbols.TOKEN_CATEGORY_BASIC,
					Symbols.TOKEN_IDENTIFIER))
					throw makeInvalidEnum("unexpected type");

				len = sym.pastIndex.index - sym.startIndex.index;
				grmrMod.strRead.readUtf32Throw(sym.startIndex, len);
				name = StringUtils.int32StringFromUtf32(
					strDat.resultBufferString);

				SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
				existRec = null;

				if(sortP.foundExisting) {
					existRec = (TypeAndObject)
						dat.nameList.get(sortP.index);
				} else {
					newRec = new TypeAndObject();
					newRec.sortObject = name;
					newRec.theObject = null;
					newRec.theType = GrammarNameTypes.TYPE_UNKNOWN;

					dat.nameList.addAt(sortP.index, newRec);

					existRec = newRec;
				}

				rule.rightList.add(existRec);

				i += 1;
			}
		}

		sym = grmCon.sym[3]; // modifier sequence
		
		if(sym != null) {
			if(checkMatchSymbolId(sym,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_RULE_MODIFIER_SEQUENCE))
				throw makeInvalidEnum("unexpected type");
			
			i = 0;
			grmList = (GramContainerList) sym;
			count = grmList.symList.size();
			while(i < count) {
				sym = (Symbol) grmList.symList.get(i);
				
				if(!checkMatchSymbolId(sym,
					Symbols.GRAM_CATEGORY_GRAMMAR,
					Symbols.GRAM_GRAMMAR_PRECEDENCE)) {
					
					grmCon2 = (GramContainer) sym;
					
					sym = grmCon2.sym[2];
					
					if(checkMatchSymbolId(sym,
						Symbols.TOKEN_CATEGORY_BASIC,
						Symbols.TOKEN_IDENTIFIER))
						throw makeInvalidEnum("unexpected type");

					len2 = sym.pastIndex.index - sym.startIndex.index;
					grmrMod.strRead.readUtf32Throw(sym.startIndex, len2);
					name = StringUtils.int32StringFromUtf32(
						strDat.resultBufferString);

					sortP = makeSortParams();
					SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
					if(!sortP.foundExisting) {
						grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
							makeGrammarUnknownName(name));
						grmrDat.state = BaseModuleData.STATE_STUCK;
						grmrDat.stuckState = StuckStates.STATE_PERMANENT;
						return ModuleMoveResult.STUCK;
					}

					existRec = (TypeAndObject)
						dat.nameList.get(sortP.index);

					if(existRec.theType != GrammarNameTypes.TYPE_TOKEN_DEF) {
						grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
							makeGrammarUnexpectedNameType(
								"expected token name",
								name));
						grmrDat.state = BaseModuleData.STATE_STUCK;
						grmrDat.stuckState = StuckStates.STATE_PERMANENT;
						return ModuleMoveResult.STUCK;
					}

					rule.precedenceTok = existRec;
					
					i += 1;
					continue;
				}

				if(!checkMatchSymbolId(sym,
					Symbols.GRAM_CATEGORY_GRAMMAR,
					Symbols.GRAM_GRAMMAR_REDUCE_GRAM)) {
					
					grmCon2 = (GramContainer) sym;
					
					sym = grmCon2.sym[2];
					
					if(checkMatchSymbolId(sym,
						Symbols.TOKEN_CATEGORY_BASIC,
						Symbols.TOKEN_IDENTIFIER))
						throw makeInvalidEnum("unexpected type");

					len2 = sym.pastIndex.index - sym.startIndex.index;
					grmrMod.strRead.readUtf32Throw(sym.startIndex, len2);
					name = StringUtils.int32StringFromUtf32(
						strDat.resultBufferString);

					sortP = makeSortParams();
					SortUtils.int32StringBinaryLookupSimple(dat.nameList, name, sortP);
					if(!sortP.foundExisting) {
						System.out.println("unknown name:" + StringUtils.javaStringFromInt32String(name));
						grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
							makeGrammarUnknownName(name));
						grmrDat.state = BaseModuleData.STATE_STUCK;
						grmrDat.stuckState = StuckStates.STATE_PERMANENT;
						return ModuleMoveResult.STUCK;
					}

					existRec = (TypeAndObject)
						dat.nameList.get(sortP.index);

					if(existRec.theType != GrammarNameTypes.TYPE_GRAM_DEF) {
						System.out.println("name not gram:" + StringUtils.javaStringFromInt32String(name));
						grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
							makeGrammarUnexpectedNameType(
								"expected gram name",
								name));
						grmrDat.state = BaseModuleData.STATE_STUCK;
						grmrDat.stuckState = StuckStates.STATE_PERMANENT;
						return ModuleMoveResult.STUCK;
					}

					rule.reduceGrm = existRec;
					
					i += 1;
					continue;
				}
				
				i += 1;
			}
		}
		
		count = dat.ruleList.size();
		dat.ruleList.addAt(count, rule);
		return ModuleMoveResult.SUCCESS;
	}
	
	public int moveGrammarToNext(
		GrammarSource grmr) {
		
		GrammarSourceData grmrDat;

		int moveResult;
		
		Throwable ex;
		
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		grmrDat = (GrammarSourceData) grmr.getData();
		
		try {
			moveResult = moveGrammarToNextThrow(grmr);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			grmrDat.probBag.addProblem(ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
			grmrDat.stuckState = StuckStates.STATE_PERMANENT;
			grmrDat.state = BaseModuleData.STATE_STUCK;
			
			moveResult = ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
	
	private int moveGrammarToNextThrow(GrammarSource grmr) {
		GrammarSourceData grmrDat;
		TokenReader tokRead;
		TokenReaderData tokReadDat;
		int moveResult;
		Symbol sym;
		Token tok;
		int id;
		int id2;
		boolean didRead;
		ObjectRef rootSymRef;
		
		LangError e2;
		
		grmrDat = (GrammarSourceData) grmr.getData();
		
		if(grmrDat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;
		
		tokRead = grmr.tokenRead;
		tokReadDat = (TokenReaderData) tokRead.getData();
		
		rootSymRef = new ObjectRef();
		
		sym = grmrDat.completeSym;
		if(sym != null) {
			id = utils.getSymbolIdPrimary(sym);
			
			if(id == Symbols.TOKEN_END_OF_STREAM
				|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM)
				return ModuleMoveResult.AT_END;
		}
		
		grmrDat.completeSym = null;
		grmrDat.currentSym = null;
		grmrDat.nextTok = null;
		
		clearDirectiveVariables(grmr);
		
		didRead = false;
		moveResult = ModuleMoveResult.INVALID;
		
		while(true) {
			if(grmrDat.nextTok != null
				|| grmrDat.declSym != null
				|| didRead) {
				
				// we have unsettled variables
				
				tok = grmrDat.nextTok;
				if(tok == null)
					tok = getMostRecentDirectiveToken(grmr);
				
				e2 = makeTokenUnexpectedError(tok, null);
				grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
				grmrDat.stuckState = StuckStates.STATE_PERMANENT;
				grmrDat.state = BaseModuleData.STATE_STUCK;
				return ModuleMoveResult.STUCK;
			}
			
			if(grmrDat.currentSym != null)
			if(checkTerminatedSymbol(grmrDat.currentSym)) {
				grmrDat.completeSym = grmrDat.currentSym;
				grmrDat.currentSym = null;

				grmrDat.state = GrammarReaderData.STATE_HAVE_SYMBOL;
				return ModuleMoveResult.SUCCESS;
			}
			
			if(!didRead) {
				moveResult = tokRead.move(ModuleMoveDirection.TO_NEXT);
				didRead = true;
			}
						
			if(moveResult == ModuleMoveResult.STUCK) {
				grmrDat.state = BaseModuleData.STATE_STUCK;
				grmrDat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return ModuleMoveResult.STUCK;
			}
			
			if(moveResult != ModuleMoveResult.SUCCESS) {
				throw makeInvalidEnum(
					"TokenReader returned bad ModuleMoveResult");
			}
			
			if(tokReadDat.state != TokenReaderData.STATE_HAVE_TOKEN) {
				throw makeInvalidEnum("TokenReader has a bad state");
			}
			
			tok = tokReadDat.resultToken;
			grmrDat.nextTok = tok;
			
			id = Symbols.SYMBOL_UNSPECIFIED;
			sym = grmrDat.currentSym;
			if(sym != null)
				id = utils.getSymbolIdPrimary(sym);
			
			// simply eat comments
			id2 = utils.getSymbolIdCategory(tok);
			if(id2 == Symbols.TOKEN_CATEGORY_COMMENT) {
				grmrDat.nextTok = null;
				didRead = false;
				continue;
			}
			
			if(grmrDat.modTok != null) {
				// we are processing a directive
				
				moveResult = collectDirective(grmr);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;
				
				if(grmrDat.declSym == null) {
					// we are not done processing a directive
					
					didRead = false;
					continue;
				}
				
				// We are done processing the directive,
				// and grmr.declSym is set
			}
			
			if(sym == null) {
				rootSymRef.value = null;
				
				moveResult = insertSymbolIntoFullStatement(
					grmr, rootSymRef);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;
				
				if(rootSymRef.value != null)
					grmrDat.currentSym = (Symbol) rootSymRef.value;
				
				didRead = false;
				continue;
			}
			
			if(id == Symbols.GRAM_GRAMMAR_RULE_LEFT) {
				moveResult = insertSymbolIntoRuleLeft(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;
				
				didRead = false;
				continue;
			}
			
			if(id == Symbols.GRAM_GRAMMAR_ASSOCIATIVITY) {
				moveResult = insertSymbolIntoAssociativity(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;

				didRead = false;
				continue;
			}
			
			if(id == Symbols.GRAM_GRAMMAR_TOKEN) {
				moveResult = insertSymbolIntoTokenDef(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;
				
				didRead = false;
				continue;
			}

			if(id == Symbols.GRAM_GRAMMAR_GRAM) {
				moveResult = insertSymbolIntoGramDef(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;
				
				didRead = false;
				continue;
			}

			if(id == Symbols.GRAM_GRAMMAR_INCLUDE_GRAMMAR) {
				moveResult = insertSymbolIntoIncludeGrammar(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;

				didRead = false;
				continue;
			}
			
			if(id == Symbols.GRAM_GRAMMAR_ROOT_VARIABLE) {
				moveResult = insertSymbolIntoRootVariable(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;

				didRead = false;
				continue;
			}

			if(id == Symbols.GRAM_GRAMMAR_END_MARKER) {
				moveResult = insertSymbolIntoEndMarker(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;

				didRead = false;
				continue;
			}

			if(id == Symbols.GRAM_GRAMMAR_PRECEDENCE_SPECTRUM) {
				moveResult = insertSymbolIntoPrecSpectrum(
					grmr, (GramContainer) sym);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;

				didRead = false;
				continue;
			}
			
			// fail
			continue;
		}
		
		// unreachable
	}
	
	// Routines which add symbols to currentSym
	//
	
	private int insertSymbolIntoFullStatement(
		GrammarSource grmr,
		ObjectRef rootSymRef) {
		
		GrammarSourceData grmrDat;
		int id;
		int id2;
		Token tok;

		grmrDat = (GrammarSourceData) grmr.getData();
		
		if(grmrDat.declSym != null) {
			// we had a directive, now commit it
			
			id2 = utils.getSymbolIdPrimary(grmrDat.declSym);
			
			if(id2 == Symbols.GRAM_GRAMMAR_INCLUDE_GRAMMAR
				|| id2 == Symbols.GRAM_GRAMMAR_ASSOCIATIVITY
				|| id2 == Symbols.GRAM_GRAMMAR_RULE_LEFT
				|| id2 == Symbols.GRAM_GRAMMAR_TOKEN
				|| id2 == Symbols.GRAM_GRAMMAR_GRAM
				|| id2 == Symbols.GRAM_GRAMMAR_ROOT_VARIABLE
				|| id2 == Symbols.GRAM_GRAMMAR_END_MARKER
				|| id2 == Symbols.GRAM_GRAMMAR_PRECEDENCE_SPECTRUM) {

				rootSymRef.value = grmrDat.declSym;

				clearDirectiveVariables(grmr);

				return ModuleMoveResult.SUCCESS;
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);
				
		if(id == Symbols.TOKEN_MOD) {
			grmrDat.modTok = tok;
			
			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}

		if(id == Symbols.TOKEN_END_OF_STREAM) {
			rootSymRef.value = tok;

			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}

		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoIncludeGrammar(
		GrammarSource grmrMod, GramContainer incSym) {

		GrammarSourceData grmrDat;
		Token tok;
		GramContainer grmCon;
		int id;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		tok = grmrDat.nextTok;
		grmCon = incSym;
		id = utils.getSymbolIdPrimary(tok);
		
		if(grmCon.sym[2] != null) {
			if(id == Symbols.TOKEN_SEMICOLON) {
				grmCon.sym[3] = tok;
				
				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		if(id == Symbols.TOKEN_STRING) {
			grmCon.sym[2] = tok;
			
			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}
		
		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoAssociativity(
		GrammarSource grmrMod, GramContainer assocSym) {
		
		GrammarSourceData grmrDat;
		Token tok;
		GramContainer grmCon;
		int id;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = assocSym;
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);
		
		if(id == Symbols.TOKEN_SEMICOLON) {
			grmCon.sym[3] = tok;

			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}

		if(id == Symbols.TOKEN_IDENTIFIER) {
			if(grmCon.sym[2] == null) {
				grmCon.sym[2] = (GramContainerList) makeSequenceGramTypical(
					Symbols.GRAM_CATEGORY_DATA,
					Symbols.GRAM_SEQUENCE_IDENTIFIER);
			}

			if(checkMatchSymbolId(grmCon.sym[2],
				Symbols.GRAM_CATEGORY_DATA,
				Symbols.GRAM_SEQUENCE_IDENTIFIER))
				throw makeInvalidEnum("unexpected type");
			
			appendToSequence(
				(GramContainerList) grmCon.sym[2], tok);
			
			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}
		
		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoRootVariable(
		GrammarSource grmrMod, GramContainer rootVarSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		int id2;
		int moveResult;
		Symbol sym;
		GramContainer idDefSym;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = rootVarSym;
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);

		if(grmCon.sym[2] == null) {
			if(id == Symbols.TOKEN_IDENTIFIER) {
				grmCon.sym[2] = tok;

				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}

		if(grmCon.sym[3] == null) {
			if(id == Symbols.TOKEN_SEMICOLON) {
				grmCon.sym[3] = tok;

				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}
			
			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoEndMarker(
		GrammarSource grmrMod, GramContainer endMarkSym) {

		return insertSymbolIntoRootVariable(grmrMod, endMarkSym);
	}

	private int insertSymbolIntoPrecSpectrum(
		GrammarSource grmrMod, GramContainer spectrumSym) {

		return insertSymbolIntoRootVariable(grmrMod, spectrumSym);
	}

	private int insertSymbolIntoTokenDef(
		GrammarSource grmrMod, GramContainer tokenDefSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		int id2;
		int moveResult;
		Symbol sym;
		GramContainer idDefSym;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = tokenDefSym;

		if(grmrDat.declSym != null) {
			// we had a directive, now commit it

			id2 = utils.getSymbolIdPrimary(grmrDat.declSym);

			if(id2 == Symbols.GRAM_GRAMMAR_ID) {
				grmCon.sym[3] = grmrDat.declSym;
				
				clearDirectiveVariables(grmrMod);
				
				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}

		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);

		if(!checkCompleteTokenDef(grmCon)) {
			if(grmCon.sym[2] == null) {
				if(id == Symbols.TOKEN_IDENTIFIER) {
					grmCon.sym[2] = tok;

					grmrDat.nextTok = null;
					return ModuleMoveResult.SUCCESS;
				}
				
				// fail
				return ModuleMoveResult.SUCCESS;
			}

			if(grmCon.sym[3] == null) {
				if(id == Symbols.TOKEN_MOD) {
					grmrDat.modTok = tok;

					grmrDat.nextTok = null;
					return ModuleMoveResult.SUCCESS;
				}

				// fail
				return ModuleMoveResult.SUCCESS;
			}
			
			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		sym = grmCon.sym[3];
		
		if(sym != null) {
			if(checkMatchSymbolId(sym,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_ID))
				throw makeInvalidEnum("unexpected type");
			idDefSym = (GramContainer) sym;

			moveResult = insertSymbolIntoIdDef(
				grmrMod, idDefSym);
			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;

			if(grmrDat.nextTok == null)
				return ModuleMoveResult.SUCCESS;

		}
		
		if(id == Symbols.TOKEN_SEMICOLON) {
			grmCon.sym[4] = tok;

			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}
		
		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoGramDef(
		GrammarSource grmrMod, GramContainer gramDefSym) {
		
		return insertSymbolIntoTokenDef(grmrMod, gramDefSym);
	}
	
	private int insertSymbolIntoIdDef(
		GrammarSource grmrMod, GramContainer idDefSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = idDefSym;
		
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);
		
		if(id == Symbols.TOKEN_INTEGER_FULL) {
			if(grmCon.sym[2] == null) {
				grmCon.sym[2] = (GramContainerList) makeSequenceGramTypical(
					Symbols.GRAM_CATEGORY_DATA,
					Symbols.GRAM_SEQUENCE_INTEGER);
			}

			if(checkMatchSymbolId(grmCon.sym[2],
				Symbols.GRAM_CATEGORY_DATA,
				Symbols.GRAM_SEQUENCE_INTEGER))
				throw makeInvalidEnum("unexpected type");
			
			appendToSequence(
				(GramContainerList) grmCon.sym[2], tok);
			
			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}
		
		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoRuleLeft(
		GrammarSource grmrMod, GramContainer ruleLeftSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		int id2;
		int moveResult;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		grmCon = ruleLeftSym;
		
		if(grmrDat.declSym != null) {
			// we had a directive, now commit it

			moveResult = insertSymbolIntoRuleLeftRuleRightSequence(
				grmrMod, grmCon);
			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;
			
			if(grmrDat.declSym == null)
				return ModuleMoveResult.SUCCESS;
			
			// did not commit directive yet
			
			id2 = utils.getSymbolIdPrimary(grmrDat.declSym);

			if(id2 == Symbols.GRAM_GRAMMAR_RULE_RIGHT) {
				if(grmCon.sym[3] == null) {
					grmCon.sym[3] =
						makeSequenceGramTypical(
							Symbols.GRAM_CATEGORY_GRAMMAR,
							Symbols.GRAM_GRAMMAR_RULE_RIGHT_SEQUENCE);
				}

				if(checkMatchSymbolId(grmCon.sym[3],
					Symbols.GRAM_CATEGORY_GRAMMAR,
					Symbols.GRAM_GRAMMAR_RULE_RIGHT_SEQUENCE))
					throw makeInvalidEnum("unexpected type");

				appendToSequence(
					(GramContainerList) grmCon.sym[3], grmrDat.declSym);
				clearDirectiveVariables(grmrMod);
				
				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}
			
			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);

		if(!checkCompleteRuleLeft(grmCon)) {
			if(grmCon.sym[2] == null) {
				if(id == Symbols.TOKEN_IDENTIFIER) {
					grmCon.sym[2] = tok;

					grmrDat.nextTok = null;
					return ModuleMoveResult.SUCCESS;
				}
				
				// fail
				return ModuleMoveResult.SUCCESS;
			}
			
			// grmCon.sym[2], the LHS identifier, has been set
			
			return insertSymbolIntoRuleLeftRuleRightSequence(
				grmrMod, grmCon);
		}
		
		moveResult = insertSymbolIntoRuleLeftRuleRightSequence(
			grmrMod, grmCon);
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;

		if(grmrDat.nextTok == null)
			return ModuleMoveResult.SUCCESS;

		// LHS identifer was specified,
		// and any RHS rules are complete as is,
		// but the stuff can be added
		
		if(id == Symbols.TOKEN_SEMICOLON) {
			grmCon.sym[4] = tok;

			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}

		if(id == Symbols.TOKEN_MOD) {
			grmrDat.modTok = tok;
			
			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}

		// fail
		return ModuleMoveResult.SUCCESS;
	}

	private int insertSymbolIntoRuleLeftRuleRightSequence(
		GrammarSource grmrMod, GramContainer ruleLeftSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Symbol ruleRightSym1;
		GramContainer ruleRightSym2;

		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = ruleLeftSym;
		
		ruleRightSym1 = getLastSymbolInSequence(
			grmCon.sym[3],
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_RULE_RIGHT_SEQUENCE);

		if(ruleRightSym1 != null) {
			if(checkMatchSymbolId(ruleRightSym1,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_RULE_RIGHT))
				throw makeInvalidEnum("unexpected type");
			ruleRightSym2 = (GramContainer) ruleRightSym1;

			return insertSymbolIntoRuleRight(
				grmrMod, ruleRightSym2);
		}
		
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoRuleRight(
		GrammarSource grmrMod, GramContainer ruleRightSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		int id2;
		int moveResult;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = ruleRightSym;

		if(grmrDat.declSym != null) {
			// we had a directive, now commit it

			moveResult = insertSymbolIntoRuleRightModifierSequence(
				grmrMod, grmCon);
			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;
			
			if(grmrDat.declSym == null)
				return ModuleMoveResult.SUCCESS;
			
			// did not commit directive yet
			
			id2 = utils.getSymbolIdPrimary(grmrDat.declSym);

			if(id2 == Symbols.GRAM_GRAMMAR_PRECEDENCE
				|| id2 == Symbols.GRAM_GRAMMAR_REDUCE_GRAM) {
				
				if(grmCon.sym[3] == null) {
					grmCon.sym[3] =
						makeSequenceGramTypical(
							Symbols.GRAM_CATEGORY_GRAMMAR,
							Symbols.GRAM_GRAMMAR_RULE_MODIFIER_SEQUENCE);
				}

				if(checkMatchSymbolId(grmCon.sym[3],
					Symbols.GRAM_CATEGORY_GRAMMAR,
					Symbols.GRAM_GRAMMAR_RULE_MODIFIER_SEQUENCE))
					throw makeInvalidEnum("unexpected type");

				appendToSequence(
					(GramContainerList) grmCon.sym[3], grmrDat.declSym);
				clearDirectiveVariables(grmrMod);
				
				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}
			
			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);

		if(!checkCompleteRuleRight(grmCon)) {
			if(grmCon.sym[3] != null) {
				return insertSymbolIntoRuleRightModifierSequence(
					grmrMod, grmCon);
			}
			
			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		if(grmCon.sym[3] == null) {
			if(id == Symbols.TOKEN_IDENTIFIER) {
				if(grmCon.sym[2] == null) {
					grmCon.sym[2] = (GramContainerList) makeSequenceGramTypical(
						Symbols.GRAM_CATEGORY_DATA,
						Symbols.GRAM_SEQUENCE_IDENTIFIER);
				}

				if(checkMatchSymbolId(grmCon.sym[2],
					Symbols.GRAM_CATEGORY_DATA,
					Symbols.GRAM_SEQUENCE_IDENTIFIER))
					throw makeInvalidEnum("unexpected type");
				appendToSequence(
					(GramContainerList) grmCon.sym[2], tok);

				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}
		}
		
		if(id == Symbols.TOKEN_MOD) {
			grmrDat.modTok = tok;
			
			grmrDat.nextTok = null;
			return ModuleMoveResult.SUCCESS;
		}
		
		return insertSymbolIntoRuleRightModifierSequence(
			grmrMod, grmCon);
	}
	
	private int insertSymbolIntoRuleRightModifierSequence(
		GrammarSource grmrMod, GramContainer ruleRightSym) {
		
		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Symbol modifierSym1;
		GramContainer modifierSym2;
		int id;

		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = ruleRightSym;
		
		modifierSym1 = getLastSymbolInSequence(
			grmCon.sym[3],
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_RULE_MODIFIER_SEQUENCE);

		if(modifierSym1 != null) {
			id = utils.getSymbolIdPrimary(modifierSym1);
			
			if(id == Symbols.GRAM_GRAMMAR_REDUCE_GRAM) {
				modifierSym2 = (GramContainer) modifierSym1;

				return insertSymbolIntoReduce(
					grmrMod, modifierSym2);
			}

			if(id == Symbols.GRAM_GRAMMAR_PRECEDENCE) {
				modifierSym2 = (GramContainer) modifierSym1;

				return insertSymbolIntoPrecedence(
					grmrMod, modifierSym2);
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		// fail
		return ModuleMoveResult.SUCCESS;
	}
	
	private int insertSymbolIntoReduce(
		GrammarSource grmrMod, GramContainer reduSym) {

		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = reduSym;
		
		if(grmrDat.declSym != null) {
			// fail
			return ModuleMoveResult.SUCCESS;
		}
		
		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);
		
		if(grmCon.sym[2] == null) {
			if(id == Symbols.TOKEN_IDENTIFIER) {
				grmCon.sym[2] = tok;
				
				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}
				
		// fail
		return ModuleMoveResult.SUCCESS;
	}

	private int insertSymbolIntoPrecedence(
		GrammarSource grmrMod, GramContainer precSym) {

		GrammarSourceData grmrDat;
		GramContainer grmCon;
		Token tok;
		int id;
		
		grmrDat = (GrammarSourceData) grmrMod.getData();
		
		grmCon = precSym;

		if(grmrDat.declSym != null) {
			// fail
			return ModuleMoveResult.SUCCESS;
		}

		tok = grmrDat.nextTok;
		id = utils.getSymbolIdPrimary(tok);
		
		if(grmCon.sym[2] == null) {
			if(id == Symbols.TOKEN_IDENTIFIER) {
				grmCon.sym[2] = tok;
				
				grmrDat.nextTok = null;
				return ModuleMoveResult.SUCCESS;
			}

			// fail
			return ModuleMoveResult.SUCCESS;
		}
				
		// fail
		return ModuleMoveResult.SUCCESS;
	}

	// Routines which evaluate directives
	// and create partial declarations
	//
	
	private int collectDirective(
		GrammarSource grmr) {

		GrammarSourceData grmrDat;
		ObjectRef keywordResult;
		int moveResult;
		Token tok;
		int id;
		
		grmrDat = (GrammarSourceData) grmr.getData();
		
		LangError e2;
		
		tok = grmrDat.nextTok;
		
		if(tok == null)
			return ModuleMoveResult.SUCCESS;
		
		if(grmrDat.modTok == null)
			return ModuleMoveResult.SUCCESS;

		id = utils.getSymbolIdPrimary(tok);
		
		// modTok is set
		
		if(grmrDat.identifierTok != null) {
			// we have already processed the directive
			return ModuleMoveResult.SUCCESS;
		}
		
		// we should expect an identifier,
		// and we should have a directive

		if(id == Symbols.TOKEN_IDENTIFIER) {
			grmrDat.identifierTok = tok;

			grmrDat.nextTok = null;
			
			keywordResult = new ObjectRef();
			keywordResult.value = null;

			moveResult = collectKeyword(
				grmr, grmrDat.identifierTok, keywordResult);
			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;

			if(keywordResult.value != null) {
				grmrDat.keywordTok = (Token) keywordResult.value;

				grmrDat.declSym = collectDeclaration(
					grmrDat.modTok, grmrDat.keywordTok);

				if(grmrDat.declSym != null) {
					return ModuleMoveResult.SUCCESS;
				}
			}
		}

		e2 = makeTokenUnexpectedError(tok, null);
		grmrDat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		grmrDat.stuckState = StuckStates.STATE_PERMANENT;
		grmrDat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int collectKeyword(
		GrammarSource grmr,
		Token identifierTok,
		ObjectRef keywordResult) {
		
		GrammarSourceData grmrDat;
		Token keywordTok;
		TokenChooser tokChoose;
		TokenChooserData chooseDat;
		CharReaderContext cc1;

		grmrDat = (GrammarSourceData) grmr.getData();
		
		tokChoose = grmr.tokChoose;
		chooseDat = tokChoose.dat;

		tokChoose.reset();

		chooseDat.possibleHelpers.add(grmr.keywordHelp);

		cc1 = utils.getCharReaderContext(grmrDat.ccStack, 0);
		utils.copyTextIndex(cc1.ti, identifierTok.startIndex);
		cc1.bi.versionNumber =
			ReadBuffer.VERSION_NUMBER_INVALID;
		
		transferStrangeAllocCounts2(chooseDat);

		//if(allocHelp == null) throw new NullPointerException();
		tokChoose.setAllocHelper(allocHelp);
		tokChoose.chooseToken(cc1);
		tokChoose.setAllocHelper(null);

		transferAllocCounts2(chooseDat);

		if(chooseDat.state == TokenChooserData.STATE_STUCK) {
			grmrDat.state = BaseModuleData.STATE_STUCK;
			grmrDat.stuckState =
				StuckStates.STATE_DUE_TO_DESCENDANTS;
			return ModuleMoveResult.STUCK;
		}

		if(chooseDat.state != TokenChooserData.STATE_CHOSEN) {
			throw makeInvalidEnum("TokenChooser has a bad state");
		}

		if(chooseDat.resultCount > 1)
			throw new IllegalStateException();

		if(chooseDat.resultCount == 1) {
			TokenHelperData helpDat = (TokenHelperData) chooseDat.chosenHelper.getData();
			
			transferStrangeAllocCounts(helpDat);
			
			//if(allocHelp == null) throw new NullPointerException();
			chooseDat.chosenHelper.setAllocHelper(allocHelp);
			keywordTok = chooseDat.chosenHelper.getToken();
			chooseDat.chosenHelper.setAllocHelper(null);

			transferAllocCounts(helpDat);

			if(keywordTok.pastIndex.index
				== identifierTok.pastIndex.index) {
				
				keywordResult.value = keywordTok;
				return ModuleMoveResult.SUCCESS;
			}
		}

		keywordResult.value = null;
		return ModuleMoveResult.SUCCESS;
	}

	private Gram collectDeclaration(
		Token modTok, Token keywordTok) {
				
		int id;
		
		id = utils.getSymbolIdPrimary(keywordTok);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_RULE_LEFT)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_RULE_LEFT,
				5);

		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_RULE_RIGHT)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_RULE_RIGHT,
				4);

		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_TOKEN)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_TOKEN,
				5);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_GRAM)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_GRAM,
				5);

		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_ID)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_ID,
				3);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_LEFT)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_ASSOCIATIVITY,
				4);

		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_RIGHT)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_ASSOCIATIVITY,
				4);

		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_NON)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_ASSOCIATIVITY,
				4);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_REDUCE_GRAM)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_REDUCE_GRAM,
				3);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_PRECEDENCE)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_PRECEDENCE,
				3);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_INCLUDE_GRAMMAR)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_INCLUDE_GRAMMAR,
				4);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_ROOT_VARIABLE)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_ROOT_VARIABLE,
				4);

		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_END_MARKER)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_END_MARKER,
				4);
		
		if(id == Symbols.TOKEN_GRAMMAR_KEYWORD_PRECEDENCE_SPECTRUM)
			return makeDeclarationGramTypical(modTok, keywordTok,
				Symbols.GRAM_CATEGORY_GRAMMAR,
				Symbols.GRAM_GRAMMAR_PRECEDENCE_SPECTRUM,
				4);

		return null;
	}
	
	// Helper routines for processing directives
	//

	private Token getMostRecentDirectiveToken(
		GrammarSource grmr) {
		
		GrammarSourceData grmrDat;
		Token tok;
		
		grmrDat = (GrammarSourceData) grmr.getData();
		
		tok = grmrDat.identifierTok;
		if(tok != null) return tok;

		tok = grmrDat.modTok;
		if(tok != null) return tok;
		
		return null;
	}

	private void clearDirectiveVariables(
		GrammarSource grmr) {
		
		GrammarSourceData grmrDat;
		
		grmrDat = (GrammarSourceData) grmr.getData();
		
		grmrDat.modTok = null;
		grmrDat.identifierTok = null;
		grmrDat.keywordTok = null;
		grmrDat.declSym = null;
		return;
	}
	
	// routines which check if a Symbol has been terminated
	//
	
	private boolean checkTerminatedAssociativity(GramContainer assocGrm) {
		return assocGrm.sym[3] != null;
	}
	
	private boolean checkTerminatedRuleLeft(GramContainer ruleLeftGrm) {
		return ruleLeftGrm.sym[4] != null;
	}
	
	private boolean checkTerminatedRootVariable(GramContainer rootVarGrm) {
		return rootVarGrm.sym[3] != null;
	}

	private boolean checkTerminatedEndMarker(GramContainer endMarkGrm) {
		return endMarkGrm.sym[3] != null;
	}
	
	private boolean checkTerminatedPrecSpectrum(GramContainer spectrumGrm) {
		return spectrumGrm.sym[3] != null;
	}
	
	private boolean checkTerminatedTokenDef(GramContainer tokenDefGrm) {
		return tokenDefGrm.sym[4] != null;
	}

	private boolean checkTerminatedGramDef(GramContainer gramDefGrm) {
		return checkTerminatedTokenDef(gramDefGrm);
	}
	
	private boolean checkTerminatedIncludeGrammar(GramContainer gramIncGrm) {
		return gramIncGrm.sym[3] != null;
	}
	
	private boolean checkTerminatedSymbol(Symbol sym) {
		int id;
		
		if(sym == null) return false;
		
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_GRAMMAR_RULE_LEFT) {
			return checkTerminatedRuleLeft((GramContainer) sym);
		}
		
		if(id == Symbols.GRAM_GRAMMAR_TOKEN) {
			return checkTerminatedTokenDef((GramContainer) sym);
		}

		if(id == Symbols.GRAM_GRAMMAR_GRAM) {
			return checkTerminatedGramDef((GramContainer) sym);
		}
		
		if(id == Symbols.GRAM_GRAMMAR_ASSOCIATIVITY) {
			return checkTerminatedAssociativity((GramContainer) sym);
		}
		
		if(id == Symbols.GRAM_GRAMMAR_INCLUDE_GRAMMAR) {
			return checkTerminatedIncludeGrammar((GramContainer) sym);
		}
		
		if(id == Symbols.GRAM_GRAMMAR_ROOT_VARIABLE) {
			return checkTerminatedRootVariable((GramContainer) sym);
		}

		if(id == Symbols.GRAM_GRAMMAR_END_MARKER) {
			return checkTerminatedEndMarker((GramContainer) sym);
		}
		
		if(id == Symbols.GRAM_GRAMMAR_PRECEDENCE_SPECTRUM) {
			return checkTerminatedPrecSpectrum((GramContainer) sym);
		}
		
		if(id == Symbols.TOKEN_END_OF_STREAM
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM)
			return true;
		
		return false;
	}
	
	// routines which check for the completeness of an accumulated
	// Symbol.
	// Can depend on contained Symbols.
	// For Symbols that end with a SEMICOLON, if they are complete,
	// they are ready to be terminated.
	//
	
	private boolean checkCompleteRuleLeft(GramContainer ruleLeftGrm) {
		
		Symbol sym;
		GramContainerList rules;
		int id;
		int len;
		GramContainer ruleRightGrm;
		
		if(ruleLeftGrm.sym[2] == null) return false;
		
		sym = getLastSymbolInSequence(
			ruleLeftGrm.sym[3],
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_RULE_RIGHT_SEQUENCE);
		
		if(sym == null) return true;
		
		id = utils.getSymbolIdPrimary(sym);
		if(id != Symbols.GRAM_GRAMMAR_RULE_RIGHT)
			throw makeInvalidEnum("type is not RULE_RIGHT");
		
		ruleRightGrm = (GramContainer) sym;
		
		return checkCompleteRuleRight(ruleRightGrm);
	}

	private boolean checkCompleteRuleRight(GramContainer ruleRightGrm) {
		Symbol sym;
		int id;
		GramContainer grmCon;
		
		sym = getLastSymbolInSequence(
			ruleRightGrm.sym[3],
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_RULE_MODIFIER_SEQUENCE);

		if(sym == null) return true;
		
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_GRAMMAR_PRECEDENCE) {
			grmCon = (GramContainer) sym;
			
			return checkCompletePrecedence(grmCon);
		}

		if(id == Symbols.GRAM_GRAMMAR_REDUCE_GRAM) {
			grmCon = (GramContainer) sym;
			
			return checkCompleteReduce(grmCon);
		}
		
		throw makeInvalidEnum("type unexpected");
	}

	private boolean checkCompletePrecedence(GramContainer precGrm) {
		Symbol sym;
		
		sym = precGrm.sym[2];
		if(sym != null) return true;
				
		return false;
	}

	private boolean checkCompleteReduce(GramContainer reduceGrm) {
		Symbol sym;
		
		sym = reduceGrm.sym[2];
		if(sym != null) return true;
				
		return false;
	}
	
	private boolean checkCompleteRootVariable(GramContainer rootVarGrm) {
		GramContainer grmCon;
		
		grmCon = rootVarGrm;
		
		if(grmCon.sym[2] == null) return false;
		
		return true;
	}

	private boolean checkCompleteEndMarker(GramContainer endMarkGrm) {
		GramContainer grmCon;
		
		grmCon = endMarkGrm;
		
		if(grmCon.sym[2] == null) return false;
		
		return true;
	}

	private boolean checkCompleteIdDef(GramContainer idDefGrm) {
		return true;
	}
	
	private boolean checkCompleteTokenDef(GramContainer tokenDefGrm) {
		Symbol sym;
		GramContainer grmCon;
		
		grmCon = tokenDefGrm;
		
		if(grmCon.sym[2] == null) return false;
		
		sym = (Symbol) grmCon.sym[3];
		
		if(sym == null) return false;
		
		if(checkMatchSymbolId(sym,
			Symbols.GRAM_CATEGORY_GRAMMAR,
			Symbols.GRAM_GRAMMAR_ID))
			throw makeInvalidEnum("type unexpected");
		
		return checkCompleteIdDef((GramContainer) sym);
	}

	private boolean checkCompleteGramDef(GramContainer gramDefGrm) {
		return checkCompleteTokenDef(gramDefGrm);
	}

	// currentSym utilities,
	// meant for making currentSym more complete,
	// and checking things about currentSym
	//
	
	private void appendToSequence(
		GramContainerList grmList, Symbol sym) {

		if(grmList.symList.size() == 0)
			utils.copyTextIndex(
				grmList.startIndex, sym.startIndex);

		grmList.symList.add(sym);

		utils.copyTextIndex(
			grmList.pastIndex, sym.pastIndex);
		
		return;
	}

	private Symbol getLastSymbolInSequence(
		Symbol grmList1,
		int seqIdCategory, int seqIdPrimary) {
		
		int id;
		int len;
		GramContainerList grmList2;
		
		if(grmList1 == null) return null;
		
		id = utils.getSymbolIdCategory(grmList1);
		if(id != seqIdCategory) throw makeInvalidEnum("type unexpected");

		id = utils.getSymbolIdPrimary(grmList1);
		if(id != seqIdPrimary) throw makeInvalidEnum("type unexpected");

		grmList2 = (GramContainerList) grmList1;
		
		len = grmList2.symList.size();
		if(len == 0) return null;
		
		return (Symbol) grmList2.symList.get(len - 1);
	}
	
	private boolean checkMatchSymbolId(
		Symbol sym,
		int idCategory, int idPrimary) {

		// returns true iff there's an id mismatch
		
		int id;

		id = utils.getSymbolIdCategory(sym);
		if(id != idCategory) return true;
		
		id = utils.getSymbolIdPrimary(sym);
		if(id != idPrimary) return true;
		
		return false;
	}
	
	// Source stack utilities
	//
	
	private boolean checkIncludeLoop(CommonInt32Array filePath) {
		GrammarSource src;
		GrammarSourceData srcDat;
		
		int srcNum;
		int srcCount;
		
		srcNum = 0;
		srcCount = dat.sourceStack.size();
		while(srcNum < srcCount) {
			src = (GrammarSource) dat.sourceStack.get(srcNum);
			srcDat = (GrammarSourceData) src.getData();
			
			if(int32StringEqual(srcDat.filePath, filePath))
				return true;
			
			srcNum += 1;
		}
		
		return false;
	}
	
	private boolean int32StringEqual(CommonInt32Array a, CommonInt32Array b) {
		CompareParams compRec = new CompareParams();
		StringUtils.int32StringCompareSimple(a, b, compRec);
		if(compRec.greater || compRec.less) return false;
		return true;
	}

	void removeStack(int stackIndex) {
		int size;
		int lastIndex;
		GrammarSource src;
		
		size = dat.sourceStack.size();
		if(size == 0) return;
		lastIndex = size - 1;
		
		while(lastIndex >= stackIndex) {
			src = (GrammarSource) dat.sourceStack.get(lastIndex);
			closeSourceFile(src);
			
			dat.sourceStack.removeAt(lastIndex);
			
			size = dat.sourceStack.size();
			if(size == 0) return;
			lastIndex = size - 1;
		}
		
		return;
	}
	
	private void closeSourceFile(GrammarSource grmr) {
		GrammarSourceData grmrDat;
		
		grmrDat = (GrammarSourceData) grmr.getData();
		
		FileNode2Utils.closeNormalFile(
			grmrDat.fileContext, grmrDat.fileDat);
		return;
	}
	
	// routines which build things
	//
	
	private FileNode2 searchAndGetFileContext(CommonInt32Array filePath) {
		int i;
		int count;
		FileNode2 dNode;
		FileNode2 fpNode;
		
		CommonArrayList searchDir = dat.searchableDirContextList;
		
		if(filePath == null)
			throw new NullPointerException();
		
		i = 0;
		count = searchDir.size();
		while(i < count) {
			dNode = (FileNode2) searchDir.get(i);
			if(dNode.theType != FileNodeTypes.TYPE_FILE_PATH)
				throw makeObjectUnexpected(null);
			fpNode = (FileNode2) dNode;
			
			if(FileNode2Utils.getFileType(fpNode, filePath,
					GrammarReaderData.INTERNAL_SEP_CHAR)
				== FileTypes.FILE_TYPE_NORMAL_FILE) {
				
				return FileNode2Utils.createFilePathNode(
					fpNode, filePath);
			}
			
			i += 1;
		}
		
		return null;
	}
	
	private GrammarSource makeGrammarSource(
		FileNode2 fileContext, FileRef2 fileDat, CommonInt32Array filePath) {
		
		GrammarSource grmrMod;
		GrammarSourceData grmrDat;

		TokenChooser tokChoose;
		
		grmrMod = new GrammarSource();
		grmrDat = new GrammarSourceData();
		grmrDat.init();
		
		grmrMod.dat = grmrDat;
		
		grmrDat.filePath = filePath;
		
		grmrDat.fileContext = fileContext;
		grmrDat.fileDat = fileDat;
		
		linkUtils.initFileContext(fileContext, fileDat);
		grmrMod.charRead = linkUtils.createCharReader3FromFileContext(
			fileContext, fileDat, utils);
		grmrMod.strRead = linkUtils.createStringReader3FromFileContext(
			fileContext, fileDat, utils);

		tokChoose = new TokenChooser();
		tokChoose.dat = new TokenChooserData();
		tokChoose.dat.init();
		//tokChoose.dat.charReadParams = grmrDat.charReadParams;
		tokChoose.charRead = grmrMod.charRead;
		tokChoose.utils = utils;
		
		grmrMod.tokenRead = new CFamilySimpleTokenReader2();
		grmrMod.tokenRead.dat = new CFamilySimpleTokenReader2Data();
		grmrMod.tokenRead.dat.init();
		//grmrMod.tokenRead.dat.charReadParams = grmrDat.charReadParams;
		grmrMod.tokenRead.utils = utils;
		grmrMod.tokenRead.charRead = grmrMod.charRead;
		grmrMod.tokenRead.tokChoose = tokChoose;
		grmrMod.tokenRead.initHelpers(utils, tokUtils);

		// SET ALLOC HELPER
		//if(allocHelp == null) throw new NullPointerException();
		grmrMod.tokenRead.setAllocHelper(allocHelp);
		
		tokChoose = new TokenChooser();
		tokChoose.dat = new TokenChooserData();
		tokChoose.dat.init();
		//tokChoose.dat.charReadParams = grmrDat.charReadParams;
		tokChoose.charRead = grmrMod.charRead;
		tokChoose.utils = utils;

		grmrMod.tokChoose = tokChoose;
		
		grmrMod.keywordHelp = new MatchingTokenHelper();
		grmrMod.keywordHelp.dat = new MatchingTokenHelperData();
		grmrMod.keywordHelp.dat.init();
		grmrMod.keywordHelp.dat.matchMap = tokUtils.grammarKeyword2TokenMap;
		grmrMod.keywordHelp.utils = utils;
		
		grmrMod.intEval = new TokenIntegerEval();
		grmrMod.intEval.dat = new TokenIntegerEvalData();
		grmrMod.intEval.dat.init();
		//grmrMod.intEval.dat.charReadParams = grmrDat.charReadParams;
		grmrMod.intEval.strRead = grmrMod.strRead;
		grmrMod.intEval.utils = utils;

		grmrMod.strEval = new TokenStringEval();
		grmrMod.strEval.dat = new TokenStringEvalData();
		grmrMod.strEval.dat.init();
		//grmrMod.strEval.dat.charReadParams = grmrDat.charReadParams;
		grmrMod.strEval.strRead = grmrMod.strRead;
		grmrMod.strEval.utils = utils;
		
		grmrDat.completeSym = null;
		
		return grmrMod;
	}
	
	private Gram makeDeclarationGramTypical(
		Token modTok, Token keywordTok,
		int idCategory, int idPrimary,
		int length) {
		
		GramContainer grm;

		grm = null;
		if(allocHelp == null)
			grm = makeGramContainer(2, length);
		if(allocHelp != null)
			grm = makeGramContainerWithHelper(2, length);
		//grm.sym = new Symbol[length];
		
		grm.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(grm, idCategory, idPrimary);
		
		utils.copyTextIndex(grm.startIndex, modTok.startIndex);
		utils.copyTextIndex(grm.pastIndex, modTok.startIndex);
		
		grm.sym[0] = modTok;
		grm.sym[1] = keywordTok;
		
		return grm;
	}
	
	private Gram makeSequenceGramTypical(
		int idCategory, int idPrimary) {
		
		GramContainerList grm;
		
		grm = null;
		if(allocHelp == null)
			grm = makeGramContainerList(2);
		if(allocHelp != null)
			grm = makeGramContainerListWithHelper(2);
		//grm.symList = makeArrayList();
		
		grm.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(grm, idCategory, idPrimary);
		
		return grm;
	}
	
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}

	private RuntimeException makeIllegalStateException(String msg) {
		if(msg == null)
			return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}

	private RuntimeException makeNullPointerException(String msg) {
		if(msg == null)
			return new NullPointerException();
		
		return new NullPointerException(msg);
	}
	
	private LangError makeIncludeLoopError(TextIndex ti) {
		TextIndex context;
		
		LangError e2;

		context = new TextIndex();
		utils.copyTextIndex(context, ti);
		
		e2 = new LangError();
		e2.id = LangErrors.ERROR_INCLUDE_LOOP;
		e2.context = context;
		return e2;
	}
	
	private CommonError makeObjectUnexpected(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNEXPECTED_OBJECT;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeObjectNotFound(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e1.msg = msg;
		return e1;
	}
	
	private FileSystemError makeInvalidFilePath(TextIndex ti) {
		TextIndex context;

		FileSystemError e1;

		context = new TextIndex();
		utils.copyTextIndex(context, ti);
		
		e1 = new FileSystemError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_MALFORMED_PATH;
		e1.context = context;
		return e1;
	}

	private FileSystemError makeInvalidFilePath2(String msg) {
		FileSystemError e1;
		
		e1 = new FileSystemError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_MALFORMED_PATH;
		e1.msg = msg;
		return e1;
	}
	
	private CommonError makeUnknownError(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_UNKNOWN;
		e1.msg = msg;
		return e1;
	}
	
	private LangError makeNameAlreadyExists(String msg) {
		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_NAME_ALREADY_EXISTS;
		e3.msg = msg;
		return e3;
	}

	private LangError makeGrammarUnknownName(CommonInt32Array name) {
		// fires when a name is used, but not defined
		
		//int[] context;
		LangError e3;
		
		//context = ArrayUtils.copyInt32Array(name);
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_UNKNOWN_NAME;
		//e3.context = context;
		return e3;
	}

	private LangError makeGrammarUnexpectedNameType(String msg, CommonInt32Array name) {
		// fires when a name is used, which has the wrong type
		
		//int[] context;
		LangError e3;
		
		//context = ArrayUtils.copyInt32Array(name);
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_UNEXPECTED_NAME_TYPE;
		//e3.context = context;
		e3.msg = msg;
		return e3;
	}
	
	private LangError makeUnspecifiedName() {
		// fires when a special name has not been set

		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_UNSPECIFIED_NAME;
		return e3;
	}
	
	private LangError makeTokenUnexpectedError(
		Symbol givenSymbol, CommonArrayList expectedSymbols) {
		
		SymbolUnexpected e3;
		
		e3 = new SymbolUnexpected();
		e3.id = LangErrors.ERROR_TOKEN_UNEXPECTED;
		e3.givenSymbol = givenSymbol;
		e3.expectedSymbols = expectedSymbols;
		return e3;
	}
	
	// module helper routines
	//
	
	private void fireRuntimeError(Throwable ex) {
		if(ex == null) return;
		
		dat.probBag.addProblem(ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
		dat.state = BaseModuleData.STATE_STUCK;
		dat.stuckState = StuckStates.STATE_PERMANENT;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;

		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}
	
	private GramContainer makeGramContainer(int idLen, int childCount) {
		dat.traceOldAllocCount += 1;
		
		GramContainer g;
		
		g = new GramContainer();
		g.symbolType = SymbolTypes.TYPE_GRAM;
		g.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER;

		g.initAllTextIndex();
		
		utils.allocNewSymbolId(g, idLen);
		utils.allocNewSymbolArrayForGramContainer(g, childCount);
		return g;
	}
	
	private GramContainerList makeGramContainerList(int idLen) {
		dat.traceOldAllocCount += 1;

		GramContainerList g;
		
		g = new GramContainerList();
		g.symbolType = SymbolTypes.TYPE_GRAM;
		g.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER_LIST;
		
		g.initAllTextIndex();

		utils.allocNewSymbolId(g, idLen);
		g.symList = makeArrayList();
		return g;
	}

	private GramContainer makeGramContainerWithHelper(
		int idLen, int childCount) {

		dat.traceNewAllocCount += 1;
		return allocHelp.makeGramContainer(idLen, childCount);
	}

	private GramContainerList makeGramContainerListWithHelper(
		int idLen) {

		dat.traceNewAllocCount += 1;
		return allocHelp.makeGramContainerList(idLen);
	}

	private void transferAllocCounts(TokenHelperData helpDat) {
		long aCount;

		aCount = helpDat.traceOldAllocCount;
		helpDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;
		
		aCount = helpDat.traceNewAllocCount;
		helpDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}
	
	private void transferAllocCounts2(TokenChooserData chooseDat) {
		long aCount;

		aCount = chooseDat.traceOldAllocCount;
		chooseDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;
		
		aCount = chooseDat.traceNewAllocCount;
		chooseDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}
	
	private void transferAllocCounts3(CFamilySimpleTokenReader2Data rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;
		
		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}

	private void transferStrangeAllocCounts(TokenHelperData helpDat) {
		long aCount;

		aCount = helpDat.traceOldAllocCount;
		helpDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;
		
		aCount = helpDat.traceNewAllocCount;
		helpDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;
	}
	
	private void transferStrangeAllocCounts2(TokenChooserData chooseDat) {
		long aCount;

		aCount = chooseDat.traceOldAllocCount;
		chooseDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
		
		aCount = chooseDat.traceNewAllocCount;
		chooseDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
	}

	private void transferStrangeAllocCounts3(CFamilySimpleTokenReader2Data rdrDat) {
		long aCount;

		aCount = rdrDat.traceOldAllocCount;
		rdrDat.traceOldAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
		
		aCount = rdrDat.traceNewAllocCount;
		rdrDat.traceNewAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;

		aCount = rdrDat.traceStrangeAllocCount;
		rdrDat.traceStrangeAllocCount = 0;
		dat.traceStrangeAllocCount += aCount;
	}

	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}
	
	public SymbolId makeSymbolId(int idLen) {
		SymbolId symId = utils.makeSymbolId(idLen);
		return symId;
	}

	public CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
}
