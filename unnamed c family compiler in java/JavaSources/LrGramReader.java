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

package unnamed.family.compiler;

import unnamed.common.*;

public class LrGramReader implements GramReader {
	public LrGramReaderData dat;
	public GeneralUtils utils;
	public LrUtils lrUtils;
	public GrammarUtils grmrUtils;
	public TokenReader tokRead;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}
	
	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, tokRead);

		return o;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		if(lrUtils == null) return true;
		if(tokRead == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.recentReducedRule = 0;
		
		dat.lrAhead.len = 0;
		dat.lrAhead.pos = 0;
		dat.lrAhead.tok = null;
		dat.lrAhead.tokNum = null;
		
		dat.lrStack.len = 0;
		dat.lrStack.clientVisitedLen = 0;
		
		dat.aheadTokNumStr = null;

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
			if(dat.state == LrGramReaderData.STATE_HAVE_SHIFTED_TOKEN
				|| dat.state == LrGramReaderData.STATE_HAVE_REDUCED_GRAM) {
				
				moveResult = moveFromLrState();
				handled = true;
			}

			if(!handled)
			if(dat.state == BaseModuleData.STATE_START) {
				initK();
				moveResult = moveFromStartLrState();
				handled = true;
			}

			if(!handled)
			if(dat.state == LrGramReaderData.STATE_HAVE_CONFLICT) {
				moveResult = ModuleMoveResult.AT_END;
				handled = true;
			}
			
			if(!handled)
			if(dat.state == LrGramReaderData.STATE_DONE) {
				moveResult = ModuleMoveResult.AT_END;
				handled = true;
			}
			
			if(!handled)
				throw makeInvalidEnum("LrGramReader has a bad state");
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			fireRuntimeError(ex);
			moveResult = ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
	
	public Symbol getCurrentSymbol() {
		LrStack theStack = dat.lrStack;
		LrStackEntry ent;
		
		if(theStack.len == 0) return null;

		ent = getStackEntry(theStack.stack, theStack.len - 1);
		return ent.sym;
	}

	public void trim(SymbolReAllocUtils reAllocUtils) {
		LrStack theLrStack = dat.lrStack;
		LrAheadCircleQueue theLrAhead = dat.lrAhead;
		
		int i;
		int stackLen;
		int queueLen;
		
		stackLen = theLrStack.len;
		i = 0;
		while(i < stackLen) {
			LrStackEntry ent = (LrStackEntry) theLrStack.stack.get(i);

			Symbol oldSym = ent.sym;
			if(oldSym != null) {
				reAllocUtils.doTrim(oldSym, utils);
				//reAllocUtils.breakupSymbolTree(oldSym);
			}
			
			i += 1;
		}

		queueLen = theLrAhead.tok.length;
		i = 0;
		while(i < queueLen) {
			Token oldSym = theLrAhead.tok[i];
			if(oldSym != null) {
				reAllocUtils.doTrim(oldSym, utils);
				//reAllocUtils.breakupSymbolTree(oldSym);
			}
			
			i += 1;
		}
	}
	
	public void reAlloc(
		SymbolAllocHelper nextAllocHelp, SymbolReAllocUtils reAllocUtils) {

		LrStack theLrStack = dat.lrStack;
		LrAheadCircleQueue theLrAhead = dat.lrAhead;
		
		int i;
		int stackLen;
		int queueLen;
		
		stackLen = theLrStack.len;
		i = 0;
		while(i < stackLen) {
			LrStackEntry ent = (LrStackEntry) theLrStack.stack.get(i);

			Symbol oldSym = ent.sym;
			if(oldSym != null) {
				ent.sym = reAllocUtils.doReAlloc(
					oldSym, nextAllocHelp, utils);
				//reAllocUtils.breakupSymbolTree(oldSym);
			}
			
			i += 1;
		}

		queueLen = theLrAhead.tok.length;
		i = 0;
		while(i < queueLen) {
			Token oldSym = theLrAhead.tok[i];
			if(oldSym != null) {
				theLrAhead.tok[i] = (Token)
					reAllocUtils.doReAlloc(
						oldSym, nextAllocHelp, utils);
				//reAllocUtils.breakupSymbolTree(oldSym);
			}
			
			i += 1;
		}
	}
	
	private void initK() {
		dat.k = dat.lrkDat.k;
		
		dat.kMinOne = dat.k;
		if(dat.kMinOne < 1) dat.kMinOne = 1;
	}
	
	private void copySymbolIdToInt32ArrayFromSymbolId(
		CommonInt32Array dst, SymbolId src) {
		
		int i;
		int len;
		
		i = 0;
		len = src.length;
		while(i < len) {
			dst.aryPtr[i] = src.aryPtr[i];
			i += 1;
		}
		
		dst.length = src.length;
		return;
	}
	
	private int acquireAheadTokens() {
		LrAheadCircleQueue theAhead = dat.lrAhead;
		
		CommonArrayList tokIdToNameMap;
		TypeAndObject[] nameStore;
		CommonInt32 intRec;
		TypeAndObject nameRec;

		int moveResult;
		TokenReaderData tokReadDat;

		SortParams sortP;
		int sortIndex;
		
		Token nextTok;
		short nextTokNum;
		boolean gotTok;
		
		
		nameStore = dat.lrkDat.names;
		tokIdToNameMap = dat.lrkDat.tokIdToNameMap;
		tokReadDat = (TokenReaderData) tokRead.getData();
		sortP = utils.getSortParamsFromStack(dat.sortRecords, 0);
		
		if(theAhead == null)
			throw makeNullPointer(null);
		
		if(theAhead.tok == null)
			theAhead.tok = makeTokenArray(dat.kMinOne);
		if(dat.kMinOne != theAhead.tok.length)
			throw makeIllegalState(null);

		if(theAhead.tokNum == null)
			theAhead.tokNum = makeInt16Array(dat.kMinOne);
		if(dat.kMinOne != theAhead.tokNum.length)
			throw makeIllegalState(null);
		
		gotTok = false;
		nextTok = null;
		nextTokNum = (short) 0;
		
		while(true) {
			if(theAhead.len >= dat.kMinOne)
				break;
			
			if(gotTok) {
				insertOneToAheadCircleQueue(nextTok, nextTokNum);
				
				nextTok = null;
				nextTokNum = (short) 0;
				gotTok = false;
				continue;
			}
			
			moveResult = tokRead.move(ModuleMoveDirection.TO_NEXT);
			
			if(moveResult == ModuleMoveResult.AT_END)
				break;
				
			if(moveResult == ModuleMoveResult.STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return ModuleMoveResult.STUCK;
			}
			
			if(moveResult != ModuleMoveResult.SUCCESS)
				throw makeInvalidEnum("TokenReader bad moveResult");
			
			if(tokReadDat.state != TokenReaderData.STATE_HAVE_TOKEN)
				throw makeInvalidEnum("TokenReader bad state");
			
			nextTok = tokReadDat.resultToken;
			nextTokNum = (short) 0;
			
			//CommonInt32Array idStr = utils.getRightLengthInt32StringFromStack(
			//	dat.rightLenStrings,
			//	utils.getIdLenFromSymbol(nextTok));
			//copySymbolIdToInt32ArrayFromSymbolId(idStr, nextTok.id);
			
			SortUtils.int32StringBinaryLookupSimple(
				tokIdToNameMap, nextTok.id, sortP);
			
			if(sortP.foundExisting) {
				TypeAndObject name = (TypeAndObject) tokIdToNameMap.get(sortP.index);
				
				intRec = (CommonInt32) name.theObject;
				nextTokNum = (short) intRec.value;
				gotTok = true;
				continue;
			}
			
			sortIndex = sortP.index;
			if(sortIndex > 0) {
				sortIndex -= 1;
				
				TypeAndObject name = (TypeAndObject) tokIdToNameMap.get(sortIndex);

				intRec = (CommonInt32) name.theObject;
				nameRec = nameStore[intRec.value];
				
				GrammarSymbol gsym = (GrammarSymbol) nameRec.theObject;
				
				if(isTokenIdSimiliar(nextTok.id, gsym.symId)) {
					nextTokNum = (short) intRec.value;
					gotTok = true;
					continue;
				}
			}
			
			System.out.println("tok not in grammar, id="
				+ utils.getSymbolIdPrimary(nextTok));
			
			dat.probBag.addProblem(
				ProblemLevels.LEVEL_LANG_ERROR,
				makeTokenNotInGrammar(null));
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_PERMANENT;
			return ModuleMoveResult.STUCK;
		}
		
		// got here, so there are no errors

		if(dat.k > 0) {
			if(dat.aheadTokNumStr == null)
				dat.aheadTokNumStr = makeInt16Array(dat.kMinOne);
			if(dat.kMinOne != dat.aheadTokNumStr.length)
				throw makeIllegalState(null);

			CommonIntArrayUtils.zero16(dat.aheadTokNumStr);
			myCopyInt16ArrayWithWrap(dat.aheadTokNumStr, 0,
				theAhead.tokNum, theAhead.pos,
				theAhead.len, dat.kMinOne);
		}

		return ModuleMoveResult.SUCCESS;
	}
	
	private int moveFromStartLrState() {
		dat.lrStack.len = 0;
		dat.lrStack.clientVisitedLen = 0;
		dat.lrStack.stack.clear();
		
		if(dat.lrAhead.tok != null) {
			int i;
			int len;
			len = dat.lrAhead.tok.length;
			i = 0;
			while(i < len) {
				dat.lrAhead.tok[i] = null;
				i += 1;
			}
		}
		
		dat.lrAhead.len = 0;
		
		return moveFromLrState();
	}

	private int moveFromLrState() {
		LrAheadCircleQueue theAhead = dat.lrAhead;
		
		int moveResult;
		int ruleNum;
		short rulePrecTok;

		CompareParams compRec;

		LrState st;
		LrEdge edg;
		BufferNode reduceRules;
		
		CommonArrayList spectrumStack;
		GrammarRule[] rules;
		TypeAndObject[] names;
		
		compRec = utils.getCompareParamsFromStack(dat.compRecords, 0);

		reduceRules = dat.lrReduceRules;

		spectrumStack = dat.precedenceSpectrumStack;
		rules = dat.lrkDat.rules;
		names = dat.lrkDat.names;
		
		moveResult = acquireAheadTokens();
		if(moveResult != ModuleMoveResult.SUCCESS)
			return moveResult;
		
		st = getTopLrStateFromStack();

		edg = null;
		if(theAhead.len > 0)
		if(st.nextEdges != null)
			edg = lrUtils.getEdge(st.nextEdges, getTokNum());

		reduceRules.used = 0;
		lrUtils.addReduceRules(
			reduceRules, st.nucleusFinalCfgs, dat.k, dat.aheadTokNumStr);
		lrUtils.addReduceRules(
			reduceRules, st.closureFinalCfgs, dat.k, dat.aheadTokNumStr);
		
		if(reduceRules.used > 1) {
			System.out.println("reduce/reduce conflict"
				+ " count=" + reduceRules.used);

			/*
			GrammarTokenHolder tokHold =
				(GrammarTokenHolder) dat.aheadTokQueue.get(0);
			System.out.println("index="
				+ "(" + tokHold.tok.startIndex.index
				+ "," + tokHold.tok.startIndex.line
				+ "," + tokHold.tok.startIndex.indexWithinLine
				+ ")");
			*/

			dat.state = LrGramReaderData.STATE_HAVE_CONFLICT;
			return ModuleMoveResult.SUCCESS;
		}
		
		if(reduceRules.used == 1 && edg != null) {
			// try precedence
			
			CommonInt32 intHldr = (CommonInt32)
				utils.getInt32FromStack(reduceRules, 0);
			
			ruleNum = intHldr.value;
			
			rulePrecTok = lrUtils.getRulePrecedenceToken(
				rules[ruleNum], names);
			
			compareReduceTokenAndShiftToken(
				compRec, spectrumStack,
				rulePrecTok, edg.transitionSym);
			
			if(compRec.greater)
				return moveWithReduce(ruleNum);
			
			if(compRec.less)
				return moveWithShift(edg);
			
			System.out.println("shift/reduce conflict");
			System.out.println("rule: "
				+ grmrUtils.dumpGrammarRule(ruleNum, dat.lrkDat.names, dat.lrkDat.rules));
			System.out.println("rulePrecTok (val)=" + rulePrecTok);
			TypeAndObject nm = names[rulePrecTok];
			System.out.println("rulePrecTok="
				+ StringUtils.javaStringFromInt32String(
					(CommonInt32Array) nm.sortObject));
			nm = names[edg.transitionSym];
			System.out.println("edg.transitionSym="
				+ StringUtils.javaStringFromInt32String(
					(CommonInt32Array) nm.sortObject));
			
			//GrammarTokenHolder tokHold =
			//	(GrammarTokenHolder) dat.aheadTokQueue.get(0);
			/*
			System.out.println("index="
				+ "(" + tokHold.tok.startIndex.index
				+ "," + tokHold.tok.startIndex.line
				+ "," + tokHold.tok.startIndex.indexWithinLine
				+ ")");
			*/

			dat.state = LrGramReaderData.STATE_HAVE_CONFLICT;
			return ModuleMoveResult.SUCCESS;
		}
		
		if(edg != null)
			return moveWithShift(edg);
		
		if(reduceRules.used == 1) {
			CommonInt32 intHldr = (CommonInt32)
				utils.getInt32FromStack(reduceRules, 0);
			
			ruleNum = intHldr.value;
			return moveWithReduce(ruleNum);
		}
		
		if(theAhead.len == 0)
			throw makeNullPointer(null);
		
		// make unexpected token error?
		fireRuntimeError(makeTokenUnexpected(
			"token unexpected", getTok()));
		return ModuleMoveResult.STUCK;
	}
	
	private int moveWithShift(LrEdge edg) {
		LrStack theStack = dat.lrStack;
		
		LrStackEntry ent;

		ent = getStackEntry(theStack.stack, theStack.len);

		ent.st = edg.next;
		ent.sym = getTok();

		// commit
		eatTokFromAheadCircleQueue();
		theStack.len += 1;
		dat.state = LrGramReaderData.STATE_HAVE_SHIFTED_TOKEN;
		return ModuleMoveResult.SUCCESS;
	}
	
	private int moveWithReduce(int ruleNum) {
		LrStack theStack = dat.lrStack;
		
		GrammarRule ruleRec;
		int len;
		Gram reduceGrm;
		LrStackEntry ent;
		TypeAndObject nameRec;
		TypeAndObject[] names;
		LrState st2;
		LrEdge edg;
		
		ruleRec = dat.lrkDat.rules[ruleNum];
		names = dat.lrkDat.names;

		len = ruleRec.rightArray.length;

		// reduce

		if(len > theStack.len) {
			throw makeIndexOutOfBounds(
				"cannot reduce more than availble on stack");
		}

		reduceGrm = null;
		
		{
			GrammarSymbol gsym;
			int idLen;

			gsym = null;
			idLen = 0;

			//if(ruleRec.reduceGrm == 0) reduceGrm.id = new int[0];

			if(ruleRec.reduceGrm != 0) {
				nameRec = names[ruleRec.reduceGrm];

				if(!nameIsGram(nameRec))
					throw makeIllegalState(
						"expected GRAM_DEF name");

				gsym = (GrammarSymbol) nameRec.theObject;
				//idLen = gsym.symId.id.length;
				idLen = utils.getIdLenFromSymbolId(gsym.symId);
			}

			reduceGrm = makeReduceGram(theStack.len - len, len, idLen);
			if(gsym != null) {
				//ArrayUtils.copyCompatibleInt32Array(reduceGrm.id, gsym.symId.id);
				utils.copySymbolIdToSymbolFromSymbolId(reduceGrm, gsym.symId);
			}
		}

		// reduceGrm is good

		st2 = getInnerLrStateFromStack(len);

		edg = null;
		if(st2.nextEdges != null)
			edg = lrUtils.getEdge(st2.nextEdges, ruleRec.leftVar);

		if(edg == null) {
			// cannot move from final lr state;
			dat.state = LrGramReaderData.STATE_DONE;
			return ModuleMoveResult.SUCCESS;
		}

		theStack.len -= len;

		ent = getStackEntry(dat.lrStack.stack, theStack.len);

		ent.st = edg.next;
		ent.sym = reduceGrm;
		
		
		// This updates the variable lrClientVisitedLength
		if(theStack.clientVisitedLen > theStack.len)
			theStack.clientVisitedLen = theStack.len;
		

		theStack.len += 1;
		dat.recentReducedRule = (short) ruleNum;
		dat.state = LrGramReaderData.STATE_HAVE_REDUCED_GRAM;

		//System.out.println("Reduced with rule=" + ruleNum);
		//System.out.println("  stacksize=" + dat.lrStackSize);

		return ModuleMoveResult.SUCCESS;
	}
	
	private boolean isTokenIdSimiliar(CommonInt32Array id1, CommonInt32Array id2) {
		int i;
		int len3;
		
		len3 = id1.length;
		if(id2.length < len3) len3 = id2.length;
		
		i = 0;
		while(i < len3) {
			if(id1.aryPtr[i] > id2.aryPtr[i]) return false;
			if(id1.aryPtr[i] < id2.aryPtr[i]) return false;
			i += 1;
		}
		
		return true;
	}
	
	private Token getTok() {
		LrAheadCircleQueue theAhead = dat.lrAhead;
		
		if(theAhead.len < 1)
			throw makeIndexOutOfBounds(null);

		int index3 = myWrapIndex(dat.kMinOne, theAhead.pos);
		return theAhead.tok[index3];
	}
	
	private short getTokNum() {
		LrAheadCircleQueue theAhead = dat.lrAhead;

		if(theAhead.len < 1)
			throw makeIndexOutOfBounds(null);

		int index3 = myWrapIndex(dat.kMinOne, theAhead.pos);
		return theAhead.tokNum.aryPtr[index3];
	}

	private void insertOneToAheadCircleQueue(
		Token tok, short tokNum) {
		
		LrAheadCircleQueue theAhead = dat.lrAhead;

		if(theAhead.len >= dat.kMinOne)
			throw makeIndexOutOfBounds(null);
		
		int index2 = myWrapIndex(dat.kMinOne, theAhead.pos + theAhead.len);
		
		theAhead.tok[index2] = tok;
		theAhead.tokNum.aryPtr[index2] = tokNum;
		
		theAhead.len += 1;
	}

	private void eatTokFromAheadCircleQueue() {
		LrAheadCircleQueue theAhead = dat.lrAhead;

		if(theAhead.len < 1)
			throw makeIndexOutOfBounds(null);
		
		theAhead.len -= 1;
		
		int removePos = theAhead.pos;
		
		theAhead.pos = myWrapIndex(dat.kMinOne, theAhead.pos + 1);
		
		theAhead.tok[removePos] = null;
		theAhead.tokNum.aryPtr[removePos] = (short) 0;
		
		return;
	}
	
	private int myWrapIndex(int k, int i) {
		int i2 = i;
		while(i2 >= k) i2 -= k;
		return i2;
	}
	
	private void myCopyInt16ArrayWithWrap(
		CommonInt16Array dst, int dstStart,
		CommonInt16Array src, int srcStart,
		int len, int wrap) {
		
		int i;
		int dstIndex;
		int srcIndex;
		
		i = 0;
		while(i < len) {
			dstIndex = myWrapIndex(wrap, dstStart + i);
			srcIndex = myWrapIndex(wrap, srcStart + i);
			dst.aryPtr[dstIndex] = src.aryPtr[srcIndex];
			i += 1;
		}
		
		return;
	}
	
	private void compareReduceTokenAndShiftToken(
		CompareParams compRec,
		CommonArrayList specStack,
		short reduceTok, short shiftTok) {
		
		TypeAndObject nameRec;
		GrammarPrecedenceSpectrum specRec;
		GrammarPrecedenceLine lineRec;

		boolean foundLineA;
		int lineA;
		boolean foundLineB;
		int lineB;
		
		int specNum;
		int specCount;
		int lineNum;
		int lineCount;
		int lineIndex;
		int lineLength;
		int storedTok;
		
		compRec.greater = false;
		compRec.less = false;
		
		if(specStack == null) return;
		
		specNum = 0;
		specCount = specStack.size();
		while(specNum < specCount) {
			nameRec = (TypeAndObject) specStack.get(specNum);

			if(!nameIsSpectrum(nameRec))
				throw makeUnhandledType(
					"name is not a precedence spectrum");
			
			specRec = (GrammarPrecedenceSpectrum) nameRec.theObject;
			foundLineA = false;
			foundLineB = false;
			lineA = 0;
			lineB = 0;
			
			lineNum = 0;
			lineCount = specRec.lines.size();
			while(lineNum < lineCount) {
				lineRec = (GrammarPrecedenceLine)
					specRec.lines.get(lineNum);
				
				lineIndex = 0;
				lineLength = lineRec.tokens.length;
				while(lineIndex < lineLength) {
					storedTok = lineRec.tokens.aryPtr[lineIndex];
					
					if(!foundLineA)
					if(reduceTok == storedTok) {
						lineA = lineNum;
						foundLineA = true;
					}

					if(!foundLineB)
					if(shiftTok == storedTok) {
						lineB = lineNum;
						foundLineB = true;
					}
					
					lineIndex += 1;
				}
				
				if(foundLineA && foundLineB) {
					if(lineA > lineB) {
						compRec.greater = true;
						return;
					}
					
					if(lineA < lineB) {
						compRec.less = true;
						return;
					}
					
					// lineA/lineB are equal, so the tokens have
					// equal precedence
					
					if(lineRec.assocType
						== GrammarAssociativityTypes.TYPE_LEFT) {
						// reduce
						
						compRec.greater = true;
						return;
					}

					if(lineRec.assocType
						== GrammarAssociativityTypes.TYPE_RIGHT) {
						// shift
						
						compRec.less = true;
						return;
					}
					
					// NON associativity
					// try next spectrum
					break;
				}
				
				lineNum += 1;
			}
			
			specNum += 1;
		}
		
		// there was not an appropriate spectrum
		return;
	}
	
	private boolean nameIsVariable(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_VARIABLE)
			return false;
		
		return true;
	}

	private boolean nameIsToken(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_TOKEN_DEF)
			return false;
		
		return true;
	}

	private boolean nameIsGram(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_GRAM_DEF)
			return false;
		
		return true;
	}

	private boolean nameIsSpectrum(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_PRECEDENCE_SPECTRUM)
			return false;
		
		return true;
	}
	
	private GramContainer makeReduceGram(int startIndex, int len, int idLen) {
		LrStack theStack = dat.lrStack;
		
		GramContainer grmCon;
		int i;
		LrStackEntry ent;
		
		TextIndex symbolStartIndex;
		TextIndex symbolPastIndex;
		
		grmCon = null;
		if(allocHelp == null)
			grmCon = makeGramContainer(idLen, len);
		if(allocHelp != null)
			grmCon = makeGramContainerWithHelper(idLen, len);
		//grmCon.sym = new Symbol[len];
		
		grmCon.symbolType = SymbolTypes.TYPE_GRAM;
				
		i = 0;
		while(i < len) {
			ent = (LrStackEntry) theStack.stack.get(startIndex + i);
			
			grmCon.sym[i] = ent.sym;
			i += 1;
		}

		symbolStartIndex = null;
		i = 0;
		while(i < len) {
			Symbol sym5 = (Symbol) grmCon.sym[i];
			symbolStartIndex = sym5.startIndex;
			if(symbolStartIndex != null) break;
			
			i += 1;
		}

		symbolPastIndex = null;
		i = len;
		while(i > 0) {
			i -= 1;
			
			Symbol sym5 = (Symbol) grmCon.sym[i];
			symbolPastIndex = sym5.pastIndex;
			if(symbolPastIndex != null) break;
		}
		
		boolean enableTextIndex;
		
		enableTextIndex = true;
		
		if(symbolStartIndex == null || symbolPastIndex == null)
			enableTextIndex = false;
		
		if(enableTextIndex) {
			utils.copyTextIndex(grmCon.startIndex, symbolStartIndex);
			utils.copyTextIndex(grmCon.pastIndex, symbolPastIndex);
		}
		
		grmCon.disableAllTextIndex = !enableTextIndex;
		
		return grmCon;
	}
	
	private LrState getTopLrStateFromStack() {
		LrStack theStack = dat.lrStack;

		LrState st;
		int i;
		
		i = theStack.len;
		
		st = null;
		
		if(i == 0)
			st = dat.lrkDat.startLrState;
		
		if(i > 0) {
			LrStackEntry ent = getStackEntry(theStack.stack, i - 1);
			st = ent.st;
		}
		
		if(st == null)
			throw makeNullPointer(null);
		
		return st;
	}
	
	private LrState getInnerLrStateFromStack(int lenFromTop) {
		LrStack theStack = dat.lrStack;

		LrState st;
		int i;
		int stackSize;
		
		stackSize = theStack.len;
		
		if(lenFromTop > stackSize)
			throw makeIndexOutOfBounds(null);
		
		i = stackSize - lenFromTop;

		st = null;
		
		if(i == 0)
			st = dat.lrkDat.startLrState;
		
		if(i > 0) {
			LrStackEntry ent = getStackEntry(theStack.stack, i - 1);

			st = ent.st;
		}
		
		if(st == null)
			throw makeNullPointer(null);
		
		return st;
	}
	
	private LrStackEntry getStackEntry(CommonArrayList store, int i) {
		int count;
		
		while(true) {
			count = store.size();
			
			if(i >= count) {
				store.add(makeStackEntry());
				continue;
			}
			
			break;
		}
		
		return (LrStackEntry) store.get(i);
	}
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}
	
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private CommonError makeUnhandledType(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_UNHANDLED_TYPE;
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

	private CommonError makeObjectUnexpected(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNEXPECTED_OBJECT;
		e1.msg = msg;
		return e1;
	}

	private LangError makeTokenNotInGrammar(String msg) {
		LangError e3;
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_TOKEN_NOT_IN_GRAMMAR;
		e3.msg = msg;
		return e3;
	}
	
	private SymbolUnexpected makeTokenUnexpected(String msg, Token tok) {
		SymbolUnexpected e3;
		
		e3 = new SymbolUnexpected();
		e3.id = LangErrors.ERROR_TOKEN_UNEXPECTED;
		e3.msg = msg;
		e3.givenSymbol = tok;
		return e3;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}
	
	private RuntimeException makeNullPointer(String msg) {
		if(msg == null) return new NullPointerException();
		
		return new NullPointerException(msg);
	}
	
	private RuntimeException makeIndexOutOfBounds(String msg) {
		if(msg == null) return new IndexOutOfBoundsException();
		
		return new IndexOutOfBoundsException(msg);
	}
		
	private void fireRuntimeError(Throwable ex) {
		if(ex == null) return;
		
		dat.probBag.addProblem(ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
		dat.state = BaseModuleData.STATE_STUCK;
		dat.stuckState = StuckStates.STATE_PERMANENT;
	}
	
	private LrStackEntry makeStackEntry() {
		LrStackEntry ent;
		
		ent = new LrStackEntry();
		ent.st = null;
		ent.sym = null;
		return ent;
	}

	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
	
	private CommonInt16Array makeInt16Array(int len) {
		return CommonUtils.makeInt16Array(len);
	}
	
	private Token[] makeTokenArray(int len) {
		return new Token[len];
	}
	
	private GramContainer makeGramContainer(
		int idLen, int childCount) {
		
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

	private GramContainer makeGramContainerWithHelper(
		int idLen, int childCount) {
		
		dat.traceNewAllocCount += 1;
		return allocHelp.makeGramContainer(idLen, childCount);
	}
}
