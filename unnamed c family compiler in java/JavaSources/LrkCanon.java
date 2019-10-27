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

package unnamed.family.compiler;

import unnamed.common.*;

public class LrkCanon implements BaseModule {
	public LrkCanonData dat;
	public GeneralUtils utils;
	public LrUtils lrUtils;
	
	public BaseModuleData getData() {return dat;}
	public CommonArrayList getChildModules() {return null;}
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		if(lrUtils == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.names = null;
		dat.rules = null;
		dat.rootRuleNum = 0;
		dat.endMarkerNameNum = 0;
		dat.tokIdToNameMap = null;
		dat.possibleFirstSets = null;
		
		dat.machineStates.clear();
		dat.startLrState = null;

		dat.unwalkedStates = null;
		dat.freshStates = null;
		
		dat.state = BaseModuleData.STATE_START;
		return;
	}
	
	public int move(int direction) {
		int moveResult;
		boolean handled;
		
		Throwable ex;
		
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		handled = false;
		
		try {
			if(direction != ModuleMoveDirection.TO_NEXT)
				return moveResult;

			if(!handled)
			if(dat.state == BaseModuleData.STATE_START) {
				moveResult = importStuff();
				handled = true;
			}
			
			if(!handled)
			if(dat.state == LrkData.STATE_HAVE_IMPORTED_DATA) {
				moveResult = calcPossibleFirstSets();
				handled = true;
			}
			
			if(!handled)
			if(dat.state == LrkData.STATE_HAVE_POSSIBLE_FIRST_SETS) {
				dat.state = LrkData.STATE_BUSY_BUILDING_MACHINE;
				dat.miniState = BaseModuleData.STATE_START;
				
				moveResult = buildMachine();
				handled = true;
			}

			if(!handled)
			if(dat.state == LrkData.STATE_HAVE_MACHINE) {
				moveResult = ModuleMoveResult.AT_END;
				handled = true;
			}
			
			if(!handled)
				throw makeInvalidEnum("LrkCanon has a bad state");
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			fireRuntimeError(ex);
			moveResult = ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
	
	private int importStuff() {
		int moveResult;
		
		moveResult = makeNamesSnapshot();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		moveResult = makeTokenIdMap();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		moveResult = importPrecSpectrums();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		moveResult = makeRulesSnapshot();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		moveResult = makeVariableToRuleMap();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;

		moveResult = setEndMarker();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		moveResult = setRootRule();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		moveResult = checkNamesAndRulesHealth();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;		
		
		dat.state = LrkData.STATE_HAVE_IMPORTED_DATA;
		return ModuleMoveResult.SUCCESS;
	}
	
	private SymbolId copySymbolId(SymbolId srcSymId) {
		SymbolId symId;
		
		if(srcSymId == null) return null;
		
		symId = makeSymbolId(utils.getIdLenFromSymbolId(srcSymId));
		utils.copySymbolId(symId, srcSymId);
		return symId;
	}
	
	private TypeAndObject importName(
		TypeAndObject srcThing) {
		
		TypeAndObject thing;
		GrammarSymbol lrSym;
		GrammarVariable lrVar;
		GrammarPrecedenceSpectrum spectrum;
		
		thing = null;
		if(srcThing == null) return thing;
		
		thing = new TypeAndObject();
		thing.sortObject = CommonIntArrayUtils.copy32(
			(CommonInt32Array) srcThing.sortObject);
		thing.theType = srcThing.theType;
		thing.theObject = null;
		
		if(thing.theType == GrammarNameTypes.TYPE_UNKNOWN) {
			if(srcThing.theObject != null)
				throw makeIllegalState(
					"name of type unknown should have object set to null");
			return thing;
		}
		
		if(thing.theType == GrammarNameTypes.TYPE_VARIABLE) {
			if(srcThing.theObject != null)
				throw makeIllegalState(
					"name of type variable should have object set to null");
			
			lrVar = new GrammarVariable();
			lrVar.flags = 0;
			lrVar.symId = null;
			lrVar.rules = null;
			
			thing.theObject = lrVar;
			return thing;
		}
		
		if(thing.theType == GrammarNameTypes.TYPE_TOKEN_DEF) {
			if(srcThing.theObject == null)
				throw makeIllegalState(
					"token missing symbol id");
			
			lrSym = new GrammarSymbol();
			lrSym.flags = 0;
			lrSym.symId = copySymbolId((SymbolId) srcThing.theObject);
			
			thing.theObject = lrSym;
			return thing;
		}
		
		if(thing.theType == GrammarNameTypes.TYPE_GRAM_DEF) {
			if(srcThing.theObject == null)
				throw makeIllegalState(
					"gram missing symbol id");

			lrSym = new GrammarSymbol();
			lrSym.flags = 0;
			lrSym.symId = copySymbolId((SymbolId) srcThing.theObject);
			
			thing.theObject = lrSym;
			return thing;
		}
		
		if(thing.theType == GrammarNameTypes.TYPE_PRECEDENCE_SPECTRUM) {
			if(srcThing.theObject == null)
				throw makeNullPointer(null);
			
			spectrum = new GrammarPrecedenceSpectrum();
			spectrum.lines = makeArrayList();
			
			thing.theObject = spectrum;
			return thing;
		}

		throw makeInvalidEnum(
			"name does not have known type");
	}
	
	private int makeNamesSnapshot() {
		int i;
		int len;
		TypeAndObject[] store;
		TypeAndObject thing1;
		CommonArrayList srcList;
		
		srcList = dat.grmrDef.nameList;
		
		len = srcList.size();
		if(len >= LrkData.NAME_COUNT_LIMIT)
			throw makeIntegerOverflow(null);
		
		store = new TypeAndObject[len];
		
		i = 0;
		while(i < len) {
			thing1 = (TypeAndObject) srcList.get(i);
			store[i] = importName(thing1);

			i += 1;
		}
		
		dat.names = store;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int makeTokenIdMap() {
		CommonArrayList idMap;
		TypeAndObject[] nameStore;
		int nameNum;
		int nameCount;
		SortParams sortP;
		SymbolId symId;
		CommonInt32 nameNumRec;
		TypeAndObject entry;
		
		
		nameStore = dat.names;
		
		idMap = makeArrayList();
		sortP = makeSortParams();
		
		nameCount = nameStore.length;
		nameNum = 0;
		while(nameNum < nameCount) {
			if(!nameIsToken(nameStore[nameNum])) {
				nameNum += 1;
				continue;
			}
			
			TypeAndObject nm = (TypeAndObject) nameStore[nameNum];
			GrammarSymbol gsym = (GrammarSymbol) nm.theObject;
			
			//symId = gsym.symId.id;
			//symId = makeNativeSymbolId(
			//	utils.getIdLenFromSymbolId(gsym.symId));
			//utils.copySymbolIdToNativeSymbolIdFromSymbolId(symId, gsym.symId);
			
			symId = gsym.symId;
			
			SortUtils.int32StringBinaryLookupSimple(
				idMap, symId, sortP);
			
			if(sortP.foundExisting) {
				dat.probBag.addProblem(
					ProblemLevels.LEVEL_LANG_ERROR,
					makeDupName());
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_PERMANENT;
				return ModuleMoveResult.STUCK;
			}
			
			nameNumRec = new CommonInt32();
			nameNumRec.value = nameNum;
			
			SymbolId symId2 = makeSymbolId(symId.length);
			utils.copySymbolId(symId2, symId);
			
			entry = new TypeAndObject();
			//entry.sortObject = ArrayUtils.copyInt32Array(symId);
			entry.sortObject = symId2;
			entry.theObject = nameNumRec;
			
			idMap.addAt(sortP.index, entry);
					
			nameNum += 1;
		}
		
		dat.tokIdToNameMap = idMap;
		return ModuleMoveResult.SUCCESS;
	}
	
	private TypeAndObject getInt32(
		CommonArrayList store, int num) {
		
		SortParams sortP;
		TypeAndObject existRec;
		
		sortP = makeSortParams();
		
		SortUtils.int32BinaryLookupSimple(store, num, sortP);
		
		if(!sortP.foundExisting) return null;
		
		existRec = (TypeAndObject) store.get(sortP.index);
		return existRec;
	}
	
	private void addInt32(
		CommonArrayList store, int num) {
		
		SortParams sortP;
		TypeAndObject newRec;
		CommonInt32 newRecNum;
		
		sortP = makeSortParams();
		
		SortUtils.int32BinaryLookupSimple(store, num, sortP);
		
		if(sortP.foundExisting) {
			throw makeIllegalState(
				"int32 not unique");
		}
		
		newRecNum = new CommonInt32();
		newRecNum.value = num;
		
		newRec = new TypeAndObject();
		newRec.sortObject = newRecNum;
		
		store.addAt(sortP.index, newRec);
		return;
	}
	
	private CommonInt16Array int16StringFromIntList(
		CommonArrayList intList) {
		
		CommonInt32 intRec;
		short val2;
		int i;
		int len;
		CommonInt16Array shortArray;
		
		i = 0;
		len = intList.size();
		shortArray = makeInt16Array(len);
		
		while(i < len) {
			intRec = (CommonInt32) intList.get(i);
			
			val2 = (short) intRec.value;
			
			if(val2 != intRec.value)
				throw makeIntegerOverflow(null);
			
			shortArray.aryPtr[i] = val2;

			i += 1;
		}
		
		return shortArray;
	}
	
	private int importPrecSpectrums() {
		int nameNum;
		int nameCount;
		int lineNum;
		int lineCount;
		int tokNum;
		int tokCount;
		int tok;
		CommonInt32 intRec;
		CommonArrayList nameList;
		TypeAndObject[] nameStore;
		GrammarPrecedenceSpectrumInterim spectrumRec1;
		GrammarPrecedenceSpectrum spectrumRec2;
		GrammarPrecedenceLineInterim lineRec1;
		GrammarPrecedenceLine lineRec2;
		TypeAndObject thing;
		CommonArrayList uniqueToken;
		CommonArrayList lineTokens;
		
		nameList = dat.grmrDef.nameList;
		nameStore = dat.names;
		
		uniqueToken = makeArrayList();
		lineTokens = makeArrayList();
		
		nameNum = 0;
		nameCount = nameStore.length;
		while(nameNum < nameCount) {
			TypeAndObject nm = (TypeAndObject) nameStore[nameNum];
			
			if(nm.theType != GrammarNameTypes.TYPE_PRECEDENCE_SPECTRUM) {
				nameNum += 1;
				continue;
			}
			
			thing = (TypeAndObject) nameList.get(nameNum);
			if(thing.theType != GrammarNameTypes.TYPE_PRECEDENCE_SPECTRUM)
				throw makeIllegalState(
					"name list not stable");
			
			spectrumRec1 = (GrammarPrecedenceSpectrumInterim)
				thing.theObject;
			spectrumRec2 = (GrammarPrecedenceSpectrum)
				nm.theObject;
			
			uniqueToken.clear();
			
			lineCount = spectrumRec1.lines.size();
			lineNum = 0;
			while(lineNum < lineCount) {
				lineRec1 = (GrammarPrecedenceLineInterim) 
					spectrumRec1.lines.get(lineNum);
				
				lineTokens.clear();
				
				tokCount = lineRec1.tokList.size();
				tokNum = 0;
				while(tokNum < tokCount) {
					tok = utils.getObjectNumberFromList(
						nameList, lineRec1.tokList.get(tokNum));
					
					if(getInt32(uniqueToken, tok) != null)
						throw makeIllegalState(
							"precedence spectrum token not unique");
					
					addInt32(uniqueToken, tok);
					
					intRec = new CommonInt32();
					intRec.value = tok;
					
					lineTokens.add(intRec);
					
					tokNum += 1;
				}
				
				lineRec2 = new GrammarPrecedenceLine();
				lineRec2.assocType = lineRec1.assocType;
				lineRec2.tokens = int16StringFromIntList(lineTokens);
				
				spectrumRec2.lines.add(lineRec2);
				
				lineNum += 1;
			}
			
			nameNum += 1;
		}
		
		return ModuleMoveResult.SUCCESS;
	}
	
	private int makeRulesSnapshot() {
		CommonArrayList ruleList;
		CommonArrayList nameList;
		int len1;
		int i;
		GrammarRule[] ruleStore;
		GrammarRuleInterim ruleRec1;
		GrammarRule ruleRec2;
		int j;
		int len2;
		
		nameList = dat.grmrDef.nameList;
		ruleList = dat.grmrDef.ruleList;
		
		len1 = ruleList.size();
		if(len1 >= LrkData.RULE_COUNT_LIMIT)
			throw makeIntegerOverflow(null);
		
		ruleStore = new GrammarRule[len1];
		i = 0;
		while(i < len1) {
			ruleRec1 = (GrammarRuleInterim) ruleList.get(i);

			ruleRec2 = new GrammarRule();
			ruleRec2.flags = 0;

			ruleRec2.leftVar = (short)
				utils.getObjectNumberFromList(
					nameList, ruleRec1.leftVar);
			
			len2 = ruleRec1.rightList.size();
			ruleRec2.rightArray = makeInt16Array(len2);
			j = 0;
			while(j < len2) {
				ruleRec2.rightArray.aryPtr[j] = (short)
					utils.getObjectNumberFromList(
						nameList, ruleRec1.rightList.get(j));
				
				j += 1;
			}
			
			ruleRec2.precedenceTok = 0;
			
			if(ruleRec1.precedenceTok != null)
				ruleRec2.precedenceTok = (short)
					utils.getObjectNumberFromList(
						nameList, ruleRec1.precedenceTok);
			
			ruleRec2.reduceGrm = 0;
			
			if(ruleRec1.reduceGrm != null)
				ruleRec2.reduceGrm = (short)
					utils.getObjectNumberFromList(
						nameList, ruleRec1.reduceGrm);
			
			ruleStore[i] = ruleRec2;
			
			i += 1;
		}
		
		dat.rules = ruleStore;
		return ModuleMoveResult.SUCCESS;
	}
	
	private TypeAndObject getInt16String(
		CommonArrayList store, CommonInt16Array str) {
		
		SortParams sortP;
		TypeAndObject existRec;
		
		sortP = makeSortParams();
		
		SortUtils.int16StringBinaryLookupSimple(store, str, sortP);
		
		if(!sortP.foundExisting) return null;
		
		existRec = (TypeAndObject) store.get(sortP.index);
		return existRec;
	}

	private void addInt16String(
		CommonArrayList store, CommonInt16Array str) {
		
		SortParams sortP;
		TypeAndObject newRec;
		
		sortP = makeSortParams();
		
		SortUtils.int16StringBinaryLookupSimple(store, str, sortP);
		
		if(sortP.foundExisting) {
			throw makeIllegalState(
				"int16string not unique");
		}
		
		newRec = new TypeAndObject();
		newRec.sortObject = str;
		
		store.addAt(sortP.index, newRec);
		return;
	}
	
	private int makeVariableToRuleMap() {
		TypeAndObject[] names;
		GrammarVariable varRec;
		GrammarRule[] rules;
		GrammarRule ruleRec;
		int nameNum;
		int nameCount;
		int ruleNum;
		int ruleCount;
		int ruleForVarCount;
		int ruleForVarNum;
		CommonInt16Array ruleForVarMap;
		CommonArrayList uniqueRight;
		
		names = dat.names;
		rules = dat.rules;
		nameCount = names.length;
		ruleCount = rules.length;
		
		uniqueRight = makeArrayList();
		
		nameNum = 0;
		while(nameNum < nameCount) {
			TypeAndObject nm = (TypeAndObject) names[nameNum];
			
			if(!nameIsVariable(nm)) {
				nameNum += 1;
				continue;
			}
			
			varRec = (GrammarVariable) nm.theObject;
			
			uniqueRight.clear();
			
			ruleForVarCount = 0;
			ruleNum = 0;
			while(ruleNum < ruleCount) {
				ruleRec = rules[ruleNum];
				
				if(ruleRec.leftVar != nameNum) {
					ruleNum += 1;
					continue;
				}
				
				if(getInt16String(uniqueRight, ruleRec.rightArray) != null) {
					throw makeIllegalState(
						"rule not unique");
				}
				
				addInt16String(uniqueRight, ruleRec.rightArray);
				
				ruleForVarCount += 1;
				ruleNum += 1;
			}
			
			ruleForVarMap = makeInt16Array(ruleForVarCount);
			
			ruleNum = 0;
			ruleForVarNum = 0;
			while(ruleNum < ruleCount) {
				ruleRec = rules[ruleNum];
				
				if(ruleRec.leftVar != nameNum) {
					ruleNum += 1;
					continue;
				}
				
				if(ruleForVarNum > ruleForVarCount)
					throw makeUnknownError(
						"rule store not stable");
				
				ruleForVarMap.aryPtr[ruleForVarNum] = (short) ruleNum;
				ruleForVarNum += 1;
				ruleNum += 1;
			}
			
			if(ruleForVarNum != ruleForVarCount)
				throw makeUnknownError(
					"rule store not stable");
			
			varRec.rules = ruleForVarMap;
			
			nameNum += 1;
		}
		
		return ModuleMoveResult.SUCCESS;
	}
	
	private int setEndMarker() {
		CommonArrayList nameList;
		TypeAndObject endMarkerName;
		
		nameList = dat.grmrDef.nameList;
		endMarkerName = dat.grmrDef.endMarkerName;
		
		if(endMarkerName == null)
			throw makeNullPointer(null);
		
		if(endMarkerName.theType != GrammarNameTypes.TYPE_TOKEN_DEF)
			throw makeInvalidEnum(
				"end marker is not a token");
		
		dat.endMarkerNameNum = (short)
			utils.getObjectNumberFromList(
				nameList, endMarkerName);
		return ModuleMoveResult.SUCCESS;
	}
	
	private int setRootRule() {
		CommonArrayList nameList;
		TypeAndObject rootVarName;
		int rootVarNum;
		GrammarVariable varRec;
		
		TypeAndObject[] nameArray;
		
		nameList = dat.grmrDef.nameList;
		rootVarName = dat.grmrDef.rootVariableName;
		
		nameArray = dat.names;
		
		if(rootVarName == null)
			throw makeNullPointer(null);
		
		if(rootVarName.theType != GrammarNameTypes.TYPE_VARIABLE)
			throw makeInvalidEnum(
				"root variable name type is not variable");
		
		rootVarNum = utils.getObjectNumberFromList(
			nameList, rootVarName);
		
		TypeAndObject nm = (TypeAndObject) nameArray[rootVarNum];
		varRec = (GrammarVariable) nm.theObject;
		
		if(varRec.rules.length != 1) {
			dat.probBag.addProblem(
				ProblemLevels.LEVEL_LANG_ERROR,
				makeRootVariableNotCompatible(
					"root varible does not have exactly one rule"));
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_PERMANENT;
			return ModuleMoveResult.STUCK;
		}
		
		dat.rootRuleNum = varRec.rules.aryPtr[0];
		return ModuleMoveResult.SUCCESS;
	}

	private int checkNamesAndRulesHealth() {
		if(dat.names.length != dat.grmrDef.nameList.size())
			throw makeUnknownError(
				"GrammarDef.nameList not stable");
		
		if(dat.rules.length != dat.grmrDef.ruleList.size())
			throw makeUnknownError(
				"GrammarDef.ruleList not stable");
		
		return ModuleMoveResult.SUCCESS;
	}
	
	private CommonInt16Array makeStartContextData() {
		CommonInt16Array ctx;
		int count;
		
		count = dat.k;
		if(count == 0) return null;
		
		ctx = makeInt16Array(count);
		//ArrayUtils.zeroInt16Array(ctx);
		return ctx;
	}
	
	private boolean nameIsToken(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_TOKEN_DEF)
			return false;
		
		return true;
	}
	
	private boolean nameIsVariable(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_VARIABLE)
			return false;
		
		return true;
	}
		
	private void addPossibleString(
		GrammarPossibleSet possibleRec, CommonInt16Array str,
		CommonBoolean change, SortParams sortRec) {
		
		TypeAndObject sortThing;
		CommonArrayList store;
		
		store = possibleRec.strings;
		
		if(store != null) {
			SortUtils.int16StringBinaryLookupSimple(store, str, sortRec);
			if(sortRec.foundExisting) return;
		}

		if(possibleRec.update == null) {
			possibleRec.update = makeArrayList();
		}
		
		store = (CommonArrayList) possibleRec.update;
		
		SortUtils.int16StringBinaryLookupSimple(store, str, sortRec);
		if(sortRec.foundExisting) return;
		
		// add token string
		
		if(change != null) change.value = true;
		
		sortThing = new TypeAndObject();
		//sortThing.sortObject = ArrayUtils.copyInt16Array(str);
		sortThing.sortObject = CommonIntArrayUtils.copy16(str);
		
		store.addAt(sortRec.index, sortThing);
		return;
	}
	
	private void possibleSetAddAllFromOtherSet(
		GrammarPossibleSet psetAccum, GrammarPossibleSet pset2,
		CommonBoolean change, SortParams sortRec) {
		
		int i;
		int count;
		TypeAndObject thing;
		
		if(possibleSetIsEmpty(pset2)) return;
		
		i = 0;
		count = pset2.strings.size();
		while(i < count) {
			thing = (TypeAndObject) pset2.strings.get(i);
			
			addPossibleString(
				psetAccum,
				(CommonInt16Array) thing.sortObject,
				change, sortRec);
			
			i += 1;
		}
		
		return;
	}
	
	private boolean possibleSetIsEmpty(GrammarPossibleSet pset) {
		if(pset == null) return true;
		if(pset.strings == null) return true;
		if(pset.strings.size() == 0) return true;
		
		return false;
	}

	private boolean possibleSetIsComplete(
		GrammarPossibleSet pset, int k) {
		
		int i;
		int count;
		int len;
		CommonInt16Array str;
		
		if(possibleSetIsEmpty(pset)) return false;
		
		i = 0;
		count = pset.strings.size();
		while(i < count) {
			TypeAndObject str5 = (TypeAndObject) pset.strings.get(i);
			str = (CommonInt16Array) str5.sortObject;
			len = str.length;
			
			if(len < k) return false;
			
			i += 1;
		}
		
		return true;
	}
	
	private void possibleSetApplyUpdate(
		GrammarPossibleSet pset, SortParams sortRec) {
		
		CommonArrayList updateStrings;
		int i;
		int len;
		TypeAndObject str;
		
		if(pset == null) return;

		updateStrings = (CommonArrayList) pset.update;
		
		if(updateStrings == null) return;
		
		i = 0;
		len = updateStrings.size();
		while(i < len) {
			str = (TypeAndObject) updateStrings.get(i);
			
			if(pset.strings == null) {
				pset.strings = makeArrayList();
			}
			
			SortUtils.int16StringBinaryLookupSimple(
				pset.strings,
				(CommonInt16Array) str.sortObject,
				sortRec);
			
			if(!sortRec.foundExisting) {
				pset.strings.addAt(sortRec.index, str);
			}
			
			i += 1;
		}
		
		updateStrings.clear();
		return;
	}
	
	private void possibleSetClean(
		GrammarPossibleSet pset) {
		
		CommonArrayList updateStrings;
		
		if(pset == null) return;
		
		if(pset.strings != null)
		if(pset.strings.size() == 0)
			pset.strings = null;
		
		if(pset.update != null) {
			updateStrings = (CommonArrayList) pset.update;
			
			if(updateStrings.size() == 0)
				pset.update = null;
		}
		
		return;
	}
		
	private CommonInt16Array strExtractRange(
		CommonInt16Array chars1, int start, int length,
		CommonArrayList tempStrStore) {
		
		int i;
		int len2;
		CommonInt16Array chars2;
		
		len2 = chars1.length;
		
		if(start > len2)
			throw makeIndexOutOfBounds(
				"start index for string is out of bounds");
		
		if(start + length > len2)
			throw makeIndexOutOfBounds(
				"span for string is out of bounds");
		
		chars2 = getStringWithLength(tempStrStore, length);
		i = 0;
		while(i < length) {
			chars2.aryPtr[i] = chars1.aryPtr[i + start];
			i += 1;
		}
		
		return chars2;
	}
	
	private GrammarPossibleSet makePossibleSetWithEmptyString(
		GrammarPossibleSetCalcParams calcRec) {
		
		GrammarPossibleSet pset;
		CommonInt16Array str;
		SortParams sortRec;
		
		pset = new GrammarPossibleSet();
		str = makeInt16Array(0);
		sortRec = calcRec.sortRec;
		
		addPossibleString(
			pset, str,
			null, sortRec);
		possibleSetApplyUpdate(
			pset, sortRec);
		
		return pset;
	}
	
	private void possibleStringsCalcParamsInitOnce(
		GrammarPossibleSetCalcParams calcRec,
		int k, GrammarPossibleSet[] possibleFirstSets) {
		
		calcRec.k = k;
		calcRec.sortRec = makeSortParams();
		calcRec.tempStrStore = makeArrayList();
		calcRec.change = new CommonBoolean();
		calcRec.possibleFirstSets = possibleFirstSets;
		
		calcRec.stringNumberStack = makeArrayList();
		calcRec.tokenString = makeInt16Array(k);
		calcRec.done = new CommonBoolean();
		return;
	}
	
	private void possibleStringsCalcParamsInit(
		GrammarPossibleSetCalcParams calcRec,
		GrammarPossibleSet accumSet, CommonInt16Array symbolString) {
		
		calcRec.done.value = false;
		calcRec.accumSet = accumSet;
		calcRec.symbolString = symbolString;
		calcRec.symbolStringLen = 0;
		calcRec.tokenStringLen = 0;
		calcRec.tokenStringNotSync = false;
		return;
	}
	
	private int calcPossibleFirstSets() {
		GrammarPossibleSet[] psets;
		TypeAndObject[] names;
		GrammarRule[] rules;
		GrammarRule ruleRec;
		int k;
		int nameNum;
		int nameCount;
		int ruleForVarNum;
		int ruleForVarCount;
		CommonInt16Array ruleForVarMap;
		CommonInt16Array tokenString2;

		boolean found;

		int stringLen;
		int i;
		
		CommonBoolean existsBadVariables;
		GrammarPossibleSetCalcParams calcRec;
		
		names = dat.names;
		nameCount = names.length;
		rules = dat.rules;
		k = dat.k;
		
		if(k < 1)  {
			dat.possibleFirstSets = null;
			
			dat.state = LrkData.STATE_HAVE_POSSIBLE_FIRST_SETS;
			return ModuleMoveResult.SUCCESS;
		}
		
		psets = new GrammarPossibleSet[nameCount];
		
		calcRec = new GrammarPossibleSetCalcParams();
		possibleStringsCalcParamsInitOnce(calcRec, k, psets);
		
		nameNum = 0;
		while(nameNum < nameCount) {
			if(nameIsToken(names[nameNum])) {
				// Tokens have one possible string,
				// a string with just that token
				
				if(psets[nameNum] == null)
					psets[nameNum] = new GrammarPossibleSet();
				
				tokenString2 = getStringWithLength(
					calcRec.tempStrStore, 1);
				tokenString2.aryPtr[0] = (short) nameNum;
				addPossibleString(psets[nameNum], tokenString2,
					calcRec.change, calcRec.sortRec);
				possibleSetApplyUpdate(psets[nameNum], calcRec.sortRec);
				
				nameNum += 1;
				continue;
			}
			
			if(nameIsVariable(names[nameNum])) {
				TypeAndObject nm = (TypeAndObject) names[nameNum];
				GrammarVariable gv = (GrammarVariable) nm.theObject;
				ruleForVarMap = gv.rules;
				
				ruleForVarCount = ruleForVarMap.length;
				ruleForVarNum = 0;
				while(ruleForVarNum < ruleForVarCount) {
					ruleRec = rules[ruleForVarMap.aryPtr[ruleForVarNum]];
					
					calcRec.symbolString = ruleRec.rightArray;
					
					// Here is where, for rules with right hand side,
					// which begins with k tokens,
					// or has less than k symbols, which are all tokens,
					// we add that beginning as a first possible string.
					
					// This is called the zeroth approximation for first sets.
					
					found = true;
					i = 0;
					stringLen = calcRec.symbolString.length;
					if(stringLen > k) stringLen = k;
					
					while(i < stringLen) {
						if(!nameIsToken(names[calcRec.symbolString.aryPtr[i]])) {
							found = false;
							break;
						}
						
						i += 1;
					}
					
					if(found) {
						if(psets[nameNum] == null)
							psets[nameNum] = new GrammarPossibleSet();
						
						addPossibleString(psets[nameNum],
							strExtractRange(
								calcRec.symbolString, 0, stringLen,
								calcRec.tempStrStore),
							calcRec.change, calcRec.sortRec);
					}
					
					ruleForVarNum += 1;
				}
				
				possibleSetApplyUpdate(
					psets[nameNum], calcRec.sortRec);
				
				nameNum += 1;
				continue;
			}
			
			nameNum += 1;
			continue;
		}
		
		existsBadVariables = new CommonBoolean();
		
		while(true) {
			calcRec.change.value = false;
			existsBadVariables.value = false;

			nameNum = 0;
			while(nameNum < nameCount) {
				if(!nameIsVariable(names[nameNum])) {
					nameNum += 1;
					continue;
				}

				TypeAndObject nm = (TypeAndObject) names[nameNum];
				GrammarVariable gv = (GrammarVariable) nm.theObject;
				ruleForVarMap = gv.rules;
				
				ruleForVarCount = ruleForVarMap.length;
				ruleForVarNum = 0;
				while(ruleForVarNum < ruleForVarCount) {
					ruleRec = rules[ruleForVarMap.aryPtr[ruleForVarNum]];
					
					if(psets[nameNum] == null)
						psets[nameNum] = new GrammarPossibleSet();
					
					possibleStringsCalcParamsInit(
						calcRec, psets[nameNum], ruleRec.rightArray);
					calcPossibleFirstSet1(calcRec);
										
					ruleForVarNum += 1;
				}
				
				possibleSetApplyUpdate(
					psets[nameNum], calcRec.sortRec);
				
				if(possibleSetIsEmpty(psets[nameNum]))
					existsBadVariables.value = true;

				nameNum += 1;
				continue;		
			}
			
			if(calcRec.change.value)
				continue;
			
			if(existsBadVariables.value) {
				// fail
				
				dat.probBag.addProblem(
					ProblemLevels.LEVEL_LANG_ERROR,
					makeFirstSetsBadVariables(
						"bad variables exist"
						+ " at end of first sets calculation"));
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_PERMANENT;
				return ModuleMoveResult.STUCK;
			}
			
			// success
			break;
		}
		
		// clean possible sets
		
		nameNum = 0;
		while(nameNum < nameCount) {
			if(nameIsVariable(names[nameNum])
				|| nameIsToken(names[nameNum])) {
				
				possibleSetClean(psets[nameNum]);
			}
			
			nameNum += 1;
		}
		
		dat.possibleFirstSets = psets;
		
		dat.state = LrkData.STATE_HAVE_POSSIBLE_FIRST_SETS;
		return ModuleMoveResult.SUCCESS;
	}
	
	private void calcPossibleFirstSet2(
		GrammarPossibleSetCalcParams calcRec) {
		
		int i;
		int len;
		GrammarPossibleSet[] psets;
		GrammarPossibleSet psetAccum;
		GrammarPossibleSet psetB;
		int k;
		
		psetAccum = makePossibleSetWithEmptyString(calcRec);
		psets = calcRec.possibleFirstSets;
		k = calcRec.tokenString.length;
		
		len = calcRec.symbolString.length;
		i = 0;
		while(i < len) {
			psetB = psets[calcRec.symbolString.aryPtr[i]];			
			psetAccum = catPossibleSet(psetAccum, psetB, k, calcRec);
			possibleSetApplyUpdate(psetAccum, calcRec.sortRec);
			
			if(possibleSetIsEmpty(psetAccum)) break;
			if(possibleSetIsComplete(psetAccum, k)) break;
			
			i += 1;
		}
		
		possibleSetAddAllFromOtherSet(
			calcRec.accumSet, psetAccum,
			calcRec.change, calcRec.sortRec);
		
		calcRec.done.value = true;
		return;
	}
	
	private GrammarPossibleSet catPossibleSet(
		GrammarPossibleSet pset1, GrammarPossibleSet pset2,
		int strLimit, GrammarPossibleSetCalcParams calcRec) {
		
		GrammarPossibleSet pset3;
		int pset1Index;
		int pset2Index;
		int pset1Len;
		int pset2Len;
		int i1;
		int i2;
		int len1;
		int len2;
		CommonInt16Array str1;
		CommonInt16Array str2;
		CommonInt16Array str3;
		
		pset3 = new GrammarPossibleSet();
		
		pset1Len = 0;
		if(pset1 != null)
		if(pset1.strings != null)
			pset1Len = pset1.strings.size();
		
		pset2Len = 0;
		if(pset2 != null)
		if(pset2.strings != null)
			pset2Len = pset2.strings.size();

		if(pset1Len == 0) return pset3;
		if(pset2Len == 0) return pset3;
		
		pset1Index = 0;
		while(pset1Index < pset1Len) {
			TypeAndObject str5;
			str5 = (TypeAndObject) pset1.strings.get(pset1Index);
			str1 = (CommonInt16Array) str5.sortObject;
			len1 = str1.length;
			if(len1 > strLimit) len1 = strLimit;
						
			pset2Index = 0;
			while(pset2Index < pset2Len) {
				str5 = (TypeAndObject) pset2.strings.get(pset2Index);
				str2 = (CommonInt16Array) str5.sortObject;
				len2 = str2.length;
				if(len1 + len2 > strLimit) len2 = strLimit - len1;
				
				str3 = getStringWithLength(
					calcRec.tempStrStore, len1 + len2);
				
				i1 = 0;
				while(i1 < len1) {
					str3.aryPtr[i1] = str1.aryPtr[i1];
					i1 += 1;
				}
				
				i2 = 0;
				while(i2 < len2) {
					str3.aryPtr[len1 + i2] = str2.aryPtr[i2];
					i2 += 1;
				}
				
				addPossibleString(pset3, str3,
					null, calcRec.sortRec);
				
				pset2Index += 1;
			}
			
			pset1Index += 1;
		}
		
		return pset3;
	}
	
	private void calcPossibleFirstSet1(
		GrammarPossibleSetCalcParams calcRec) {
		
		while(!calcRec.done.value)
			calcPossibleFirstSetStep(calcRec);
	}
	
	private void calcPossibleFirstSetStep(
		GrammarPossibleSetCalcParams calcRec) {
		
		GrammarPossibleSet[] psets;
		int tokenStringLimit;
		int symbolStringLimit;
		int strLen;
		int i;
		CommonInt32 strNumInt;
		short nameNum;
		CommonInt16Array str;
		
		if(calcRec.tokenStringNotSync) throw makeIllegalState(null);
		
		tokenStringLimit = calcRec.tokenString.length;
		symbolStringLimit = calcRec.symbolString.length;
		psets = calcRec.possibleFirstSets;
		
		if(calcRec.tokenStringLen >= tokenStringLimit
			|| calcRec.symbolStringLen >= symbolStringLimit) {
			
			// the possible string is complete
			
			strLen = calcRec.tokenStringLen;
			if(strLen > tokenStringLimit) strLen = tokenStringLimit;
			
			addPossibleString(calcRec.accumSet,
				strExtractRange(
					calcRec.tokenString, 0, strLen,
					calcRec.tempStrStore),
				calcRec.change,
				calcRec.sortRec);
			
			calcPossibleFirstSetHitEnd(calcRec);
			return;
		}
		
		i = calcRec.symbolStringLen;
		nameNum = calcRec.symbolString.aryPtr[i];

		if(possibleSetIsEmpty(psets[nameNum])) {
			calcPossibleFirstSetHitEnd(calcRec);
			return;
		}

		strNumInt = getIntegerInStack(calcRec.stringNumberStack, i);
		strNumInt.value = 0;

		GrammarPossibleSet pset5 = psets[nameNum];
		TypeAndObject str5 = (TypeAndObject) pset5.strings.get(strNumInt.value);
		str = (CommonInt16Array) str5.sortObject;
		
		calcRec.symbolStringLen += 1;
		calcRec.tokenStringNotSync = true;
		calcPossibleFirstSetAdvance(calcRec, str);
		return;
	}
	
	
	private void calcPossibleFirstSetHitEnd(
		GrammarPossibleSetCalcParams calcRec) {
		
		GrammarPossibleSet[] psets;
		GrammarPossibleSet possibleSet;
		int i;
		int strNum2;
		CommonInt32 strNumInt;
		short nameNum;
		CommonInt16Array str1;
		CommonInt16Array str2;
		CommonArrayList strList;
		
		if(calcRec.tokenStringNotSync) throw makeIllegalState(null);
		
		psets = calcRec.possibleFirstSets;

		i = calcRec.symbolStringLen;
			
		if(i == 0) {
			calcRec.done.value = true;
			return;
		}

		i -= 1;

		while(true) {
			strNumInt = getIntegerInStack(calcRec.stringNumberStack, i);
			nameNum = calcRec.symbolString.aryPtr[i];

			if(possibleSetIsEmpty(psets[nameNum]))
				throw makeIllegalState(null);

			GrammarPossibleSet pset5 = (GrammarPossibleSet) psets[nameNum];
			strList = pset5.strings;

			TypeAndObject str5;
			str5 = (TypeAndObject) strList.get(strNumInt.value);
			str1 = (CommonInt16Array) str5.sortObject;
			calcPossibleFirstSetRetract(calcRec, str1);

			strNum2 = strNumInt.value + 1;

			if(strNum2 < strList.size()) {
				str5 = (TypeAndObject) strList.get(strNum2);
				str2 = (CommonInt16Array) str5.sortObject;
				
				strNumInt.value = strNum2;
				calcPossibleFirstSetAdvance(calcRec, str2);
				return;
			}

			// strNumInt.value >= strList.size()
			// done with this symbol,
			// need to back out

			calcRec.symbolStringLen = i;
			calcRec.tokenStringNotSync = false;
			
			if(i == 0) {
				calcRec.done.value = true;
				return;
			}

			i -= 1;
			continue;
		}
		
		// UNREACHABLE
	}

	private void calcPossibleFirstSetAdvance(
		GrammarPossibleSetCalcParams calcRec, CommonInt16Array str) {
		
		int tokenStringLimit;
		int strLen;
		int strLen2;
		int j;
		int start;
		
		if(!calcRec.tokenStringNotSync) throw makeIllegalState(null);
		
		tokenStringLimit = calcRec.tokenString.length;
		
		strLen = str.length;

		strLen2 = tokenStringLimit - calcRec.tokenStringLen;
		if(strLen < strLen2) strLen2 = strLen;

		// load into tokenString
		j = 0;
		start = calcRec.tokenStringLen;
		while(j < strLen2) {
			calcRec.tokenString.aryPtr[start + j] = str.aryPtr[j];
			j += 1;
		}
		
		calcRec.tokenStringLen += strLen;
		calcRec.tokenStringNotSync = false;
		return;
	}

	private void calcPossibleFirstSetRetract(
		GrammarPossibleSetCalcParams calcRec, CommonInt16Array str) {

		int strLen;
		
		if(calcRec.tokenStringNotSync) throw makeIllegalState(null);

		strLen = str.length;
		calcRec.tokenStringLen -= strLen;
		calcRec.tokenStringNotSync = true;
		return;
	}
	
	private boolean stateStoreIsEmpty(
		CommonArrayList stateGrpStore) {
		
		if(stateGrpStore == null) return true;
		if(stateGrpStore.size() == 0) return true;
		
		return false;
	}
	
	private void configUnpack(
		LrConfigGroup cfg, int k,
		CommonInt16Array tempStr, SortParams sortRec) {
		
		CommonInt16Array ctxDat;
		GrammarPossibleSet pset;
		int i;
		int len;
		int j;
		
		if(k < 1) return;
		
		ctxDat = cfg.contextData;
		len = ctxDat.length;
		
		if(tempStr.length != k)
			throw makeIllegalState(
				"temp string does not have k length");
		
		if((len % k) != 0)
			throw makeIllegalState(
				"packed context array has bad length");
		
		pset = new GrammarPossibleSet();

		i = 0;
		while(i < len) {
			j = 0;
			while(j < k) {
				tempStr.aryPtr[j] = ctxDat.aryPtr[i + j];
				j += 1;
			}
			
			addPossibleString(
				pset, tempStr,
				null, sortRec);
			
			i += k;
		}
		
		possibleSetApplyUpdate(pset, sortRec);
		possibleSetClean(pset);
		
		cfg.update = pset;
		return;
	}
	
	private void configPack(LrConfigGroup cfg, int k) {
		CommonInt16Array ctxDat;
		GrammarPossibleSet pset;
		int i;
		int len;
		int j;
		int packPos;
		CommonInt16Array str;
		
		if(k < 1) return;
		
		pset = (GrammarPossibleSet) cfg.update;
		if(pset == null) return;
		
		if(possibleSetIsEmpty(pset))
			throw makeIllegalState(
				"config group has no contexts");
		
		len = pset.strings.size();
		
		ctxDat = makeInt16Array(k * len);
		i = 0;
		while(i < len) {
			TypeAndObject str5 = (TypeAndObject) pset.strings.get(i);
			str = (CommonInt16Array) str5.sortObject;
			
			if(str.length != k)
				throw makeIllegalState(
					"context string length is not equal to k");
			
			j = 0;
			packPos = i * k;
			while(j < k) {
				ctxDat.aryPtr[packPos + j] = str.aryPtr[j];
				j += 1;
			}
			
			i += 1;
		}
		
		cfg.contextData = ctxDat;
		cfg.update = null;
		return;
	}
	
	private void configStoreUnpackAll(
		CommonArrayList configStore) {
		
		int i;
		int len;
		LrConfigGroup cfg;
		
		int k;
		CommonInt16Array tempStr;
		SortParams sortRec;
		
		k = dat.k;
		tempStr = makeInt16Array(k);
		sortRec = makeSortParams();

		i = 0;
		len = configStore.size();
		while(i < len) {
			cfg = (LrConfigGroup) configStore.get(i);
			
			configUnpack(cfg, k, tempStr, sortRec);

			i += 1;
		}
		
		return;
	}

	private void configStorePackAll(
		CommonArrayList configStore) {
		
		int i;
		int len;
		LrConfigGroup cfg;
		
		int k;
		k = dat.k;

		i = 0;
		len = configStore.size();
		while(i < len) {
			cfg = (LrConfigGroup) configStore.get(i);
			
			configPack(cfg, k);
			
			if(k > 0)
			if(cfg.contextData == null)
				throw makeNullPointer(null);

			i += 1;
		}
		
		return;
	}
	
	private void configStoreCleanAll(
		CommonArrayList configStore) {
		
		int i;
		int len;
		LrConfigGroup cfg;
		
		int k;
		k = dat.k;

		i = 0;
		len = configStore.size();
		while(i < len) {
			cfg = (LrConfigGroup) configStore.get(i);
			
			cfg.update = null;

			i += 1;
		}
		
		return;
	}

	private void setConfigUnwalked(LrConfigGroup cfg, boolean value) {
		cfg.flags = FlagUtils.setFlagInt16(
			cfg.flags, LrkData.FLAG_UNWALKED, value);
	}
	
	private boolean isConfigUnwalked(LrConfigGroup cfg) {
		return FlagUtils.getFlagInt16(
			cfg.flags, LrkData.FLAG_UNWALKED);
	}
		
	private void configStoreMarkAllUnwalked(
		CommonArrayList configStore) {
		
		int i;
		int len;
		LrConfigGroup cfg;
		
		i = 0;
		len = configStore.size();
		while(i < len) {
			cfg = (LrConfigGroup) configStore.get(i);
			setConfigUnwalked(cfg, true);
			
			i += 1;
		}
		
		return;
	}
	
	private void buildClosureWalkConfig(
		CommonArrayList closureConfigStore, LrConfigGroup cfg) {
		
		TypeAndObject[] names;
		GrammarRule[] rules;
		GrammarPossibleSet[] possibleFirstSets;
		GrammarRule ruleRec;
		CommonInt16Array ruleForVarMap;
		int i1;
		int i2;
		int len1;
		int len2;
		int ruleForVarNum;
		int ruleForVarCount;
		int k;
		short nameNum;
		GrammarPossibleSet pset;
		GrammarPossibleSetCalcParams calcRec;
		LrConfigGroup destCfg;
		LrConfigGroup existCfg;
		GrammarPossibleSet accumSet;
		boolean addedToSelf;
		
		names = dat.names;
		rules = dat.rules;
		possibleFirstSets = dat.possibleFirstSets;
		k = dat.k;
		
		i1 = cfg.markerPosition;
		ruleRec = rules[cfg.ruleNumber];
		len1 = ruleRec.rightArray.length;
		
		if(i1 > len1)
			throw makeIndexOutOfBounds(
				"marker position in config group is out of bounds");
		
		if(i1 == len1) {
			// This is the final item for the rule
			setConfigUnwalked(cfg, false);
			return;
		}
		
		nameNum = ruleRec.rightArray.aryPtr[i1];
		
		if(!nameIsVariable(names[nameNum])) {
			// closure only done if next symbol is a variable
			setConfigUnwalked(cfg, false);
			return;
		}
		
		TypeAndObject nm = (TypeAndObject) names[nameNum];
		GrammarVariable gv = (GrammarVariable) nm.theObject;
		ruleForVarMap = gv.rules;
		
		ruleForVarCount = ruleForVarMap.length;
		
		if(ruleForVarCount < 1) {
			// variable symbol doesnt have any rules
			setConfigUnwalked(cfg, false);
			return;
		}
		
		i2 = i1 + 1; // marker position after current variable
		len2 = len1 - i2;
		
		// now need to calculate new possible first set
		calcRec = new GrammarPossibleSetCalcParams();
		possibleStringsCalcParamsInitOnce(calcRec, k, possibleFirstSets);
		pset = null;
		
		if(k > 0) {
			pset = new GrammarPossibleSet();
			possibleStringsCalcParamsInit(
				calcRec, pset, 
				StringUtils.int16StringExtractRange(
					ruleRec.rightArray, i2, len2));
			calcPossibleFirstSet1(calcRec);
			possibleSetApplyUpdate(pset, calcRec.sortRec);

			pset = catPossibleSet(
				pset, (GrammarPossibleSet) cfg.update,
				k, calcRec);
			possibleSetApplyUpdate(pset, calcRec.sortRec);

			if(!possibleSetIsComplete(pset, k))
				throw makeIllegalState(
					"calculated first set should have"
					+ " all strings with length k");
		}
	
		// pset is ready
		
		addedToSelf = false;
		
		ruleForVarNum = 0;
		while(ruleForVarNum < ruleForVarCount) {
			destCfg = makeConfig(ruleForVarMap.aryPtr[ruleForVarNum], (short) 0);
			existCfg = lrUtils.getConfig(closureConfigStore, destCfg);
			
			calcRec.change.value = false;
			
			if(existCfg == null) {
				lrUtils.addConfig(closureConfigStore, destCfg);
				calcRec.change.value = true;
			} else {
				destCfg = existCfg;
			}
			
			if(k > 0) {
				if(destCfg.update == null)
					destCfg.update = new GrammarPossibleSet();

				accumSet = (GrammarPossibleSet) destCfg.update;

				possibleSetAddAllFromOtherSet(accumSet, pset,
					calcRec.change, calcRec.sortRec);
				possibleSetApplyUpdate(accumSet, calcRec.sortRec);
			}
			
			if(calcRec.change.value) {
				if(destCfg == cfg) addedToSelf = true;
				else {
					setConfigUnwalked(destCfg, true);
				}
			}
			
			ruleForVarNum += 1;
		}
		
		if(!addedToSelf) {
			// we walked cfg, and there werent any updates on it,
			// so cfg is done
			setConfigUnwalked(cfg, false);
		}
			
		return;
	}
	
	private void buildClosureWalkConfigStore(
		CommonArrayList closureConfigStore,
		CommonArrayList configStore, CommonBoolean change) {
		
		Object[] cfgs;
		LrConfigGroup cfg;
		int i;
		int count;
		
		cfgs = configStore.toArray();
		i = 0;
		count = cfgs.length;
		while(i < count) {
			cfg = (LrConfigGroup) cfgs[i];

			if(isConfigUnwalked(cfg)) {
				buildClosureWalkConfig(closureConfigStore, cfg);
				change.value = true;
			}

			i += 1;
		}
		
		return;
	}
	
	private LrConfigGroup[] buildClosureCreateFinalConfigArrayFromConfigArray(
		LrConfigGroup[] cfgArray) {
		
		CommonArrayList cfgStore1;
		CommonArrayList cfgStore2;
		int i;
		int len;
		LrConfigGroup cfg;
		GrammarRule[] rules;
		int ruleRightLen;
		LrConfigGroup[] cfgArray2;
		
		rules = dat.rules;
		
		cfgStore1 = makeArrayList();
		utils.copyToListFromArray(cfgStore1, cfgArray);
		
		cfgStore2 = makeArrayList();
		
		i = 0;
		len = cfgStore1.size();
		while(i < len) {
			cfg = (LrConfigGroup) cfgStore1.get(i);
			GrammarRule rule5 = rules[cfg.ruleNumber];
			ruleRightLen = rule5.rightArray.length;
			
			if(cfg.markerPosition > ruleRightLen)
				throw makeIllegalState(
					"marker position is greater than rule right length");
			
			if(cfg.markerPosition == ruleRightLen)
				cfgStore2.add(cfg);
			
			i += 1;
		}
		
		cfgArray2 = new LrConfigGroup[cfgStore2.size()];
		utils.copyToArrayFromList(cfgArray2, cfgStore2);
		return cfgArray2;
	}
	
	private void buildClosure(LrState st) {
		CommonArrayList nucleusCfgs;
		CommonArrayList closureCfgs;
		CommonBoolean change;
		LrConfigGroup[] closureCfgArray;
		
		change = new CommonBoolean();
		
		nucleusCfgs = makeArrayList();
		utils.copyToListFromArray(nucleusCfgs, st.nucleusCfgs);
		
		configStoreUnpackAll(nucleusCfgs);
		configStoreMarkAllUnwalked(nucleusCfgs);

		closureCfgs = makeArrayList();
		
		change.value = true;
		while(change.value){
			change.value = false;
			buildClosureWalkConfigStore(
				closureCfgs, nucleusCfgs, change);
		}
		
		change.value = true;
		while(change.value){
			change.value = false;
			buildClosureWalkConfigStore(
				closureCfgs, closureCfgs, change);
		}
		
		configStorePackAll(closureCfgs);
		
		closureCfgArray = new LrConfigGroup[closureCfgs.size()];
		utils.copyToArrayFromList(closureCfgArray, closureCfgs);
		st.closureCfgs = closureCfgArray;
		
		configStoreCleanAll(closureCfgs);
		configStoreCleanAll(nucleusCfgs);
		
		
		st.nucleusFinalCfgs =
			buildClosureCreateFinalConfigArrayFromConfigArray(st.nucleusCfgs);
		st.closureFinalCfgs =
			buildClosureCreateFinalConfigArrayFromConfigArray(st.closureCfgs);

		return;
	}

	private void unbuildClosure(LrState st) {
		st.closureCfgs = null;
		return;
	}
	
	private void buildGotosWalkConfigCreateEdges(
		CommonArrayList edgStore, LrConfigGroup cfg, LrState fromSt) {
		
		GrammarRule[] rules;
		GrammarRule ruleRec;
		LrEdge edg;
		LrEdge existEdg;

		int i;
		int len;
		
		rules = dat.rules;
		ruleRec = rules[cfg.ruleNumber];
		
		len = ruleRec.rightArray.length;
		i = cfg.markerPosition;
		
		if(i > len)
			throw makeIndexOutOfBounds(
				"marker position in config group is out of bounds");
		
		if(i == len) return;
		
		edg = new LrEdge();
		edg.prev = fromSt;
		edg.next = null;
		edg.transitionSym = ruleRec.rightArray.aryPtr[i];

		existEdg = lrUtils.getEdge(edgStore, edg.transitionSym);
		if(existEdg == null)
			lrUtils.addEdge(edgStore, edg);
		return;
	}
	
	private void buildGotosWalkConfigStoreCreateEdges(
		CommonArrayList edgStore, CommonArrayList cfgStore, LrState fromSt) {
		
		int i;
		int len;
		
		len = cfgStore.size();
		i = 0;
		while(i < len) {
			buildGotosWalkConfigCreateEdges(
				edgStore, (LrConfigGroup) cfgStore.get(i), fromSt);
			i += 1;
		}
		
		return;
	}
	
	private void buildGotosWalkConfigCreateConfig(
		CommonArrayList toCfgStore,
		short transitionSym,
		LrConfigGroup fromCfg) {

		int i;
		int len;
		CommonInt16Array rightArray;
		LrConfigGroup toCfg;
		LrConfigGroup existCfg;
		
		GrammarRule rule5 = (GrammarRule) dat.rules[fromCfg.ruleNumber];
		rightArray = rule5.rightArray;
		i = fromCfg.markerPosition;
		len = rightArray.length;
		
		if(i > len)
			throw makeIndexOutOfBounds(
				"marker position for rule is out of bounds");
		
		if(i == len) return;
		if(transitionSym != rightArray.aryPtr[i]) return;
		
		toCfg = new LrConfigGroup();
		toCfg.markerPosition = (short) (i + 1);
		toCfg.ruleNumber = fromCfg.ruleNumber;
		
		existCfg = lrUtils.getConfig(toCfgStore, toCfg);
		if(existCfg != null)
			throw makeIllegalState(
				"unexpected duplicate config group");
		
		lrUtils.addConfig(toCfgStore, toCfg);
	
		toCfg.contextData = null;
		if(fromCfg.contextData != null)
			//toCfg.contextData = ArrayUtils.copyInt16Array(fromCfg.contextData);
			toCfg.contextData = CommonIntArrayUtils.copy16(fromCfg.contextData);
		
		return;
	}

	private void buildGotosWalkConfigStoreCreateConfigs(
		CommonArrayList toCfgStore,
		short transitionSym,
		CommonArrayList fromCfgStore) {
		
		int i;
		int len;
		
		i = 0;
		len = fromCfgStore.size();
		while(i < len) {
			buildGotosWalkConfigCreateConfig(
				toCfgStore,
				transitionSym, (LrConfigGroup) fromCfgStore.get(i));
			i += 1;
		}
		
		return;
	}

	private void buildGotosWalkEdgeStoreCreateStates(
		CommonArrayList machineStates,
		CommonArrayList freshStates,
		CommonArrayList edgStore, LrState fromSt) {
		
		LrState toSt;
		LrState toExistSt;
		CommonArrayList toCfgStore;
		LrEdge edg;
		//TypeAndObjectSortedByInt32 thing;
		TypeAndObject thing;
		
		int i;
		int len;
		
		CommonArrayList fromNucleusCfgStore;
		CommonArrayList fromClosureCfgStore;
		
		fromNucleusCfgStore = makeArrayList();
		utils.copyToListFromArray(fromNucleusCfgStore, fromSt.nucleusCfgs);
		
		fromClosureCfgStore = makeArrayList();
		utils.copyToListFromArray(fromClosureCfgStore, fromSt.closureCfgs);
		
		len = edgStore.size();
		i = 0;
		while(i < len) {
			thing = (TypeAndObject) edgStore.get(i);
			edg = (LrEdge) thing.theObject;
			toCfgStore = makeArrayList();
			
			buildGotosWalkConfigStoreCreateConfigs(
				toCfgStore,
				edg.transitionSym, fromNucleusCfgStore);
			buildGotosWalkConfigStoreCreateConfigs(
				toCfgStore,
				edg.transitionSym, fromClosureCfgStore);
			
			toSt = new LrState();
			toSt.nucleusCfgs = new LrConfigGroup[toCfgStore.size()];
			utils.copyToArrayFromList(toSt.nucleusCfgs, toCfgStore);
			
			toExistSt = lrUtils.getState(machineStates, toSt, dat.k);
			if(toExistSt != null) {
				edg.next = toExistSt;
				
				i += 1;
				continue;
			}

			toExistSt = lrUtils.getState(freshStates, toSt, dat.k);
			if(toExistSt != null) {
				edg.next = toExistSt;
				
				i += 1;
				continue;
			}
			
			lrUtils.addState(freshStates, toSt, dat.k);
			edg.next = toSt;
			
			i += 1;
		}
		
		return;
	}
	
	private void buildGotosWalkState(
		CommonArrayList machineStates,
		CommonArrayList freshStates,
		LrState fromSt){
		
		// aka buildGotos, doGoto, createNewStatesAndEdges,
		// aka doTransitions
		
		CommonArrayList nucleusCfgStore;
		CommonArrayList closureCfgStore;
		
		CommonArrayList freshEdgs;
		
		nucleusCfgStore = makeArrayList();
		utils.copyToListFromArray(nucleusCfgStore, fromSt.nucleusCfgs);
		
		closureCfgStore = makeArrayList();
		utils.copyToListFromArray(nucleusCfgStore, fromSt.closureCfgs);
		
		freshEdgs = makeArrayList();
		buildGotosWalkConfigStoreCreateEdges(
			freshEdgs, nucleusCfgStore, fromSt);
		buildGotosWalkConfigStoreCreateEdges(
			freshEdgs, closureCfgStore, fromSt);
		buildGotosWalkEdgeStoreCreateStates(
			machineStates, freshStates, freshEdgs, fromSt);
		
		fromSt.update = freshEdgs;
		
		dat.miniState = LrkCanonData.STATE_BUSY_UNBUILDING_CLOSURES;
		return;
	}
		
	private void addStartLrState() {
		LrState startState1;
		LrConfigGroup cfg;
		LrConfigGroup[] nucleus;
		
		startState1 = new LrState();

		cfg = new LrConfigGroup();
		cfg.ruleNumber = dat.rootRuleNum;
		cfg.markerPosition = 0;
		cfg.contextData = makeStartContextData();
		
		nucleus = new LrConfigGroup[1];
		nucleus[0] = cfg;
		
		startState1.nucleusCfgs = nucleus;
		
		dat.machineStates.clear();
		dat.unwalkedStates = null;
		dat.freshStates = makeArrayList();
		
		lrUtils.addState(dat.freshStates, startState1, dat.k);
		dat.startLrState = startState1;
		
		dat.miniState = LrkCanonData.STATE_BUSY_COMMITING_NEW_LR_STATES;
		return;
	}

	private void buildClosures(
		CommonArrayList states) {
		
		CommonArrayList grpStore;
		LrStateGroup grp;
		LrState st;
		int grpNum;
		int grpCount;
		int stNum;
		int stCount;
		
		grpStore = states;
		if(grpStore == null) {
			dat.miniState = LrkCanonData.STATE_BUSY_WALKING_LR_STATES;
			return;
		}
		
		grpCount = grpStore.size();
		grpNum = 0;
		while(grpNum < grpCount) {
			grp = (LrStateGroup) grpStore.get(grpNum);
			
			stCount = grp.states.size();
			stNum = 0;
			while(stNum < stCount) {
				st = (LrState) grp.states.get(stNum);
				buildClosure(st);
				
				stNum += 1;
			}
			
			grpNum += 1;
		}
		
		dat.miniState = LrkCanonData.STATE_BUSY_WALKING_LR_STATES;
		return;
	}
	
	private void buildGotos(
		CommonArrayList states) {
		
		CommonArrayList machineStates;
		CommonArrayList freshStates;
		
		CommonArrayList grpStore;
		LrStateGroup grp;
		LrState st;
		int grpNum;
		int grpCount;
		int stNum;
		int stCount;
		
		machineStates = dat.machineStates;
		freshStates = dat.freshStates;
		
		grpStore = states;
		if(grpStore == null) {
			dat.miniState = LrkCanonData.STATE_BUSY_UNBUILDING_CLOSURES;
			return;
		}
		
		grpCount = grpStore.size();
		grpNum = 0;
		while(grpNum < grpCount) {
			grp = (LrStateGroup) grpStore.get(grpNum);
			
			stCount = grp.states.size();
			stNum = 0;
			while(stNum < stCount) {
				st = (LrState) grp.states.get(stNum);
				
				buildGotosWalkState(machineStates, freshStates, st);
				
				stNum += 1;
			}
			
			grpNum += 1;
		}
		
		dat.miniState = LrkCanonData.STATE_BUSY_UNBUILDING_CLOSURES;
		return;

	}
	
	private void unbuildClosures(
		CommonArrayList states) {
		CommonArrayList grpStore;
		LrStateGroup grp;
		LrState st;
		int grpNum;
		int grpCount;
		int stNum;
		int stCount;
		
		grpStore = states;
		if(grpStore == null) {
			dat.miniState = LrkCanonData.STATE_BUSY_COMMITING_NEW_LR_STATES;
			return;
		}
		
		if(dat.keepClosures) {
			dat.miniState = LrkCanonData.STATE_BUSY_COMMITING_NEW_LR_STATES;
			return;
		}
		
		grpCount = grpStore.size();
		grpNum = 0;
		while(grpNum < grpCount) {
			grp = (LrStateGroup) grpStore.get(grpNum);
			
			stCount = grp.states.size();
			stNum = 0;
			while(stNum < stCount) {
				st = (LrState) grp.states.get(stNum);
				
				unbuildClosure(st);
				
				stNum += 1;
			}
			
			grpNum += 1;
		}
		
		dat.miniState = LrkCanonData.STATE_BUSY_COMMITING_NEW_LR_STATES;
		return;
	}
	
	private void commitNewStates(int k) {
		CommonArrayList theStates;
		CommonArrayList machineStates;
		LrStateGroup grp;
		LrState st;
		LrState existSt;
		
		int grpNum;
		int grpCount;
		int stateNum;
		int stateCount;
		
		theStates = dat.freshStates;
		machineStates = dat.machineStates;
		
		if(theStates == null) {
			dat.miniState = LrkCanonData.STATE_BUSY_COMMITING_NEW_EDGES;
			return;
		}
		
		grpCount = theStates.size();
		grpNum = 0;
		while(grpNum < grpCount) {
			grp = (LrStateGroup) theStates.get(grpNum);
			
			stateCount = grp.states.size();
			stateNum = 0;
			while(stateNum < stateCount) {
				st = (LrState) grp.states.get(stateNum);
				existSt = lrUtils.getState(machineStates, st, k);
				
				if(existSt != null)
					throw makeIllegalState("existing duplicate state");
				
				lrUtils.addState(machineStates, st, k);
				
				stateNum += 1;
			}
			
			grpNum += 1;
		}
		
		dat.miniState = LrkCanonData.STATE_BUSY_COMMITING_NEW_EDGES;
		return;
	}

	private void commitNewEdges(int k) {
		CommonArrayList theStates;
		CommonArrayList machineStates;
		LrStateGroup grp;
		LrState st;
		LrState nextSt;
		CommonArrayList theEdges;
		//TypeAndObjectSortedByInt32 sortEntry;
		TypeAndObject sortEntry;
		LrEdge theEdge;
		LrEdge existEdg;
		LrState existSt;
		
		int grpNum;
		int grpCount;
		int stateNum;
		int stateCount;
		int edgeNum;
		int edgeCount;
		
		theStates = dat.unwalkedStates;
		machineStates = dat.machineStates;
		
		if(theStates == null) {
			dat.miniState =
				LrkCanonData.STATE_BUSY_UPDATING_UNWALKED_LR_STATES;
			return;
		}
		
		grpCount = theStates.size();
		grpNum = 0;
		while(grpNum < grpCount) {
			grp = (LrStateGroup) theStates.get(grpNum);
			
			stateCount = grp.states.size();
			stateNum = 0;
			while(stateNum < stateCount) {
				st = (LrState) grp.states.get(stateNum);
				theEdges = (CommonArrayList) st.update;
				
				if(theEdges == null) {
					stateNum += 1;
					continue;
				}
				
				edgeCount = theEdges.size();
				edgeNum = 0;
				while(edgeNum < edgeCount) {
					sortEntry = (TypeAndObject)
						theEdges.get(edgeNum);
					theEdge = (LrEdge) sortEntry.theObject;
					
					if(st.nextEdges == null) {
						st.nextEdges = makeArrayList();
					}
					existEdg = lrUtils.getEdge(
						st.nextEdges, theEdge.transitionSym);
					if(existEdg != null) {
						if(existEdg.next != theEdge.next)
							throw makeIllegalState(
								"existing edge conflicts with new one");
					} else lrUtils.addEdge(st.nextEdges, theEdge);
					
					nextSt = theEdge.next;
					
					if(nextSt.prevStates == null) {
						nextSt.prevStates = makeArrayList();
					}
					existSt = lrUtils.getState(nextSt.prevStates, st, k);
					if(existSt == null) {
						lrUtils.addState(nextSt.prevStates, st, k);
					}
					
					edgeNum += 1;
				}
				
				// got here, so update has been handled
				st.update = null;
				
				stateNum += 1;
			}
			
			grpNum += 1;
		}
		
		dat.miniState =
			LrkCanonData.STATE_BUSY_UPDATING_UNWALKED_LR_STATES;
		return;
	}
	
	private void updateUnwalkedStates() {
		// dealloc and unref
		dat.unwalkedStates = null;
		
		// lr state and edge info has been commited,
		// so what were fresh states,
		// become unwalked states
		dat.unwalkedStates = dat.freshStates;
		
		dat.freshStates = makeArrayList();
		
		if(!stateStoreIsEmpty(dat.unwalkedStates)) {
			dat.miniState =
				LrkCanonData.STATE_EXISTS_UNWALKED_LR_STATES;
			return;
		}
		
		dat.state = LrkData.STATE_HAVE_MACHINE;
		return;
	}
	
	private int buildMachine() {
		if(dat.state != LrkData.STATE_BUSY_BUILDING_MACHINE)
			throw makeIllegalState("LrkCanon bad state");
		
		while(true) {
			if(dat.state == BaseModuleData.STATE_STUCK)
				return ModuleMoveResult.STUCK;
			
			if(dat.state == LrkData.STATE_HAVE_MACHINE)
				return ModuleMoveResult.SUCCESS;
			
			if(dat.miniState == BaseModuleData.STATE_START) {
				addStartLrState();
				continue;
			}
			
			if(dat.miniState == LrkCanonData.STATE_BUSY_BUILDING_CLOSURES) {
				buildClosures(dat.unwalkedStates);
				continue;
			}
			
			if(dat.miniState == LrkCanonData.STATE_BUSY_WALKING_LR_STATES) {
				buildGotos(dat.unwalkedStates);
				continue;
			}

			if(dat.miniState == LrkCanonData.STATE_BUSY_UNBUILDING_CLOSURES) {
				unbuildClosures(dat.unwalkedStates);
				continue;
			}
			
			if(dat.miniState == LrkCanonData.STATE_BUSY_COMMITING_NEW_LR_STATES) {
				commitNewStates(dat.k);
				continue;
			}
			
			if(dat.miniState == LrkCanonData.STATE_BUSY_COMMITING_NEW_EDGES) {
				commitNewEdges(dat.k);
				continue;
			}
			
			if(dat.miniState == LrkCanonData.STATE_EXISTS_UNWALKED_LR_STATES) {
				dat.miniState = LrkCanonData.STATE_BUSY_BUILDING_CLOSURES;
				continue;
			}
			
			if(dat.miniState == LrkCanonData.STATE_BUSY_UPDATING_UNWALKED_LR_STATES) {
				updateUnwalkedStates();
				continue;
			}
			
			throw makeIllegalState("LrkCanon bad state");
		}
		
		// UNREACHABLE
	}
	
	private CommonInt32 getIntegerInStack(CommonArrayList store, int index) {
		int len;
		
		while(true) {
			len = store.size();
			if(index < len) break;
			store.addAt(len, new CommonInt32());
		}
		
		return (CommonInt32) store.get(index);
	}
	
	private CommonInt16Array getStringWithLength(
		CommonArrayList store, int len) {
		
		int count;
		CommonInt16Array str;
		
		while(true) {
			count = store.size();
			if(count > len) break;
			store.addAt(count, makeInt16Array(count));
		}
		
		str = (CommonInt16Array) store.get(len);
		if(str.length != len)
			throw makeIllegalState(
				"StringWithLength store is bad");
		
		return str;
	}
	
	private LrConfigGroup makeConfig(
		short ruleNum, short markerPosition) {
		
		LrConfigGroup cfg;
		
		cfg = new LrConfigGroup();
		cfg.ruleNumber = ruleNum;
		cfg.markerPosition = markerPosition;
		cfg.flags = 0;
		cfg.contextData = null;
		cfg.update = null;
		return cfg;
	}
	
	private CommonError makeUnknownError(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_UNKNOWN;
		e1.msg = msg;
		return e1;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}

	private RuntimeException makeNullPointer(String msg) {
		if(msg == null) return new NullPointerException();
		
		return new NullPointerException(msg);
	}
	
	private CommonError makeIntegerOverflow(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INTEGER_OVERFLOW;
		return e1;
	}
	
	private RuntimeException makeIndexOutOfBounds(String msg) {
		if(msg == null) return new IndexOutOfBoundsException();
		
		return new IndexOutOfBoundsException(msg);
	}
	
	private LangError makeUnspecifiedName() {
		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_UNSPECIFIED_NAME;
		return e3;
	}

	private LangError makeDupName() {
		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_DUPLICATE_NAME;
		return e3;
	}
		
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private LangError makeFirstSetsBadVariables(String msg) {
		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_LR_FIRST_SETS_BAD_VARIABLES;
		e3.msg = msg;
		return e3;
	}
	
	private LangError makeRootVariableNotCompatible(String msg) {
		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_GRAMMAR_LR_ROOT_VARIABLE_NOT_COMPATIBLE;
		e3.msg = msg;
		return e3;
	}
	
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
	
	public SymbolId makeSymbolId(int idLen) {
		SymbolId symId = utils.makeSymbolId(idLen);
		return symId;
	}
	
	public CommonInt16Array makeInt16Array(int len) {
		return CommonUtils.makeInt16Array(len);
	}
}
