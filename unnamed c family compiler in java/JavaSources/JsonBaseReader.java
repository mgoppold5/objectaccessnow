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

public class JsonBaseReader
	implements BaseModule {
	
	public JsonBaseReaderData dat;
	public GeneralUtils utils;
	public CharReaderAccess charRead;
	public TokenChooser tokChoose;
	
	public CFamilyCommentTokenHelper commentHelp;
	public XmlReferenceTokenHelper refHelp;
	public CFamilyIdentifierTokenHelper varHelp;

	// for json punct
	public MatchingTokenHelper punctHelp;

	// for json keywords
	public MatchingTokenHelper keywordsHelp;

	public void initHelpers(GeneralUtils utils, TokenUtils tokUtils) {
		commentHelp = new CFamilyCommentTokenHelper();
		commentHelp.dat = new CFamilyCommentTokenHelperData();
		commentHelp.dat.init();
		commentHelp.utils = utils;
				
		refHelp = new XmlReferenceTokenHelper();
		refHelp.dat = new XmlReferenceTokenHelperData();
		refHelp.dat.init();
		refHelp.utils = utils;
		
		varHelp = new CFamilyIdentifierTokenHelper();
		varHelp.dat = new CFamilyIdentifierTokenHelperData();
		varHelp.dat.init();
		varHelp.utils = utils;
		
		punctHelp = new MatchingTokenHelper();
		punctHelp.dat = new MatchingTokenHelperData();
		punctHelp.dat.init();
		punctHelp.dat.matchMap = tokUtils.jsonPunct2TokenMap;
		punctHelp.utils = utils;
		
		keywordsHelp = new MatchingTokenHelper();
		keywordsHelp.dat = new MatchingTokenHelperData();
		keywordsHelp.dat.init();
		keywordsHelp.dat.matchMap = tokUtils.jsonKeyword2TokenMap;
		keywordsHelp.utils = utils;
		
		return;
	}

	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, charRead);
		addExistingModule(o, tokChoose);
		addExistingModule(o, commentHelp);
		addExistingModule(o, refHelp);
		addExistingModule(o, varHelp);
		addExistingModule(o, punctHelp);
		addExistingModule(o, keywordsHelp);

		return o;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		
		if(utils == null) return true;
		if(charRead == null) return true;
		if(tokChoose == null) return true;
		if(commentHelp == null) return true;
		if(refHelp == null) return true;
		if(varHelp == null) return true;
		if(punctHelp == null) return true;
		if(keywordsHelp == null) return true;
		
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.probBag.reset();
		
		if(charRead != null) charRead.reset();
		
		dat.nodeStack.clear();
		dat.nodeStackIndex = 0;
		
		dat.state = BaseModuleData.STATE_START;
	}

	public Symbol getSymbol() {
		SimpleReaderNode n;
		
		if(dat.state != SimpleReaderData.STATE_HAVE_SYMBOL)
			throw new IllegalStateException();
		
		n = getNormalNodeRequireSymbol(dat.nodeStackIndex);

		return n.sym;
	}

	public boolean isSymbolComplete() {
		SimpleReaderNode n;
		
		if(dat.state != SimpleReaderData.STATE_HAVE_SYMBOL)
			throw new IllegalStateException();
		
		n = getNormalNodeRequireSymbol(dat.nodeStackIndex);
		
		if(n.id == SimpleReaderNodeTypes.TYPE_TOKEN)
			return true;
		
		if(n.id == SimpleReaderNodeTypes.TYPE_GRAM)
		if(n.state == SimpleReaderNode.STATE_AT_END)
			return true;
		
		return false;
	}

	public int getStackIndex() {
		return dat.nodeStackIndex;
	}
		
	public int move(int direction) {
		int moveResult;
		boolean handled;
		Symbol sym;
		int id;
		int cat;
		SimpleReaderNode n;
		boolean isToken;
		boolean isGram;
		
		Throwable ex;

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;
		
		ex = null;
		moveResult = ModuleMoveResult.INVALID;
		handled = false;
		isToken = false;
		isGram = false;
		
		try {
			if(direction == ModuleMoveDirection.TO_PARENT)
				return moveToParent();
					
			if(!handled)
			if(dat.state == SimpleReaderData.STATE_HAVE_SYMBOL) {
				
				n = getNormalNodeRequireSymbol(dat.nodeStackIndex);
				sym = n.sym;
				
				
				if(n.id == SimpleReaderNodeTypes.TYPE_TOKEN)
					isToken = true;
				
				if(n.id == SimpleReaderNodeTypes.TYPE_GRAM)
					isGram = true;
				
				if(!handled && isToken)
				if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
					return ModuleMoveResult.AT_END;
				
				id = utils.getSymbolIdPrimary(sym);
				cat = utils.getSymbolIdCategory(sym);
				
				if(!handled)
				if(isToken)
				switch(id) {
				case Symbols.TOKEN_STRING_SPAN:
				case Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY:
				case Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC:
					moveResult = moveFromContentToken(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.TOKEN_KEYWORD_NULL:
					moveResult = moveFromTokenThing(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.TOKEN_COLON:
					moveResult = moveFromColon(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.TOKEN_COMMA:
					moveResult = moveFromComma(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.TOKEN_BAD_SPAN:
					moveResult = moveFromBadSpan(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				}
							
				if(!handled)
				if(isGram)
				switch(id) {
				case Symbols.GRAM_XML_CONTENT:
					moveResult = moveFromContent(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.GRAM_LIST:
					moveResult = moveFromList(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.GRAM_DICT:
					moveResult = moveFromDict(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.GRAM_DICT_ENTRY:
					moveResult = moveFromDictEntry(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				}
				
				if(handled && isGram)
				if(moveResult == ModuleMoveResult.SUCCESS)
				if(direction == ModuleMoveDirection.TO_FIRST_CHILD) {
					return moveToChild();
				}
			}
			
			if(!handled)
			if(dat.state == BaseModuleData.STATE_START) {
				moveResult = moveFromStart(
					direction, dat.nodeStackIndex);
				handled = true;
			}
			
			if(!handled) {
				throwInvalidEnum("Invalid Json state");
			}
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) {
			dat.probBag.addProblem(ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.state = BaseModuleData.STATE_STUCK;
			moveResult = ModuleMoveResult.STUCK;
		}
		
		return moveResult;
	}
	
	public int moveToAncestor(int stackIndex) {
		SimpleReaderNode n;
		
		if(stackIndex > dat.nodeStackIndex) {
			throw new IndexOutOfBoundsException();
		}
		
		if(stackIndex == dat.nodeStackIndex) {
			return ModuleMoveResult.SUCCESS;
		}
		
		if(dat.nodeStack.size() == 0) {
			throw new IndexOutOfBoundsException();
		}
		
		n = (SimpleReaderNode) dat.nodeStack.get(
			dat.nodeStackIndex);
		n.lastKnownDirection = ModuleMoveDirection.TO_ANCESTOR;
		
		n = (SimpleReaderNode) dat.nodeStack.get(stackIndex);
		n.lastKnownDirection = ModuleMoveDirection.TO_HERE;

		dat.nodeStackIndex = stackIndex;

		updateState();
		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;
				
		return ModuleMoveResult.SUCCESS;
	}
	
	private int moveToParent() {
		SimpleReaderNode n;
		int stackIndex;

		if(dat.nodeStackIndex <= 0)
			return ModuleMoveResult.AT_END;

		if(dat.nodeStack.size() == 0) {
			return ModuleMoveResult.AT_END;
		}
		
		n = (SimpleReaderNode) dat.nodeStack.get(
			dat.nodeStackIndex);
		n.lastKnownDirection = ModuleMoveDirection.TO_PARENT;

		stackIndex = dat.nodeStackIndex - 1;

		n = (SimpleReaderNode) dat.nodeStack.get(
			stackIndex);
		n.lastKnownDirection = ModuleMoveDirection.TO_HERE;

		dat.nodeStackIndex = stackIndex;
		updateState();

		return ModuleMoveResult.SUCCESS;
	}
	
	private int moveToChild() {
		int stackIndex;
		SimpleReaderNode n;
		
		n = (SimpleReaderNode) dat.nodeStack.get(
			dat.nodeStackIndex);
		n.lastKnownDirection = ModuleMoveDirection.TO_FIRST_CHILD;

		stackIndex = dat.nodeStackIndex + 1;
		
		n = getNormalNodeRequireSymbol(stackIndex);
		n.lastKnownDirection = ModuleMoveDirection.TO_HERE;
		
		dat.nodeStackIndex = stackIndex;
		updateState();

		return ModuleMoveResult.SUCCESS;
	}
	
	private boolean isSymbolAThing(Symbol sym) {
		int id;
		
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_LIST
			|| id == Symbols.GRAM_DICT
			|| id == Symbols.GRAM_XML_CONTENT
			|| id == Symbols.TOKEN_KEYWORD_NULL) {
			
			return true;
		}
		
		return false;
	}
	
	private int moveFromStart(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		CharReaderContext cc;
		
		LangError e2;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		if(direction != ModuleMoveDirection.TO_NEXT)
			return ModuleMoveResult.INVALID;

		cc = utils.getCharReaderContext(dat.ccStack, 0);
		getCharReader2StartIndex(cc.ti, charRead);
		cc.bi.versionNumber = ReadBuffer.VERSION_NUMBER_INVALID;
		
		readRegularHighLevel(0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n3 = getNextStateRequireSymbol();

		if(isSymbolAThing(n3.sym)) {
			setNode(nodeStackIndex, n3);
			if(nodeStackIndex == dat.nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}
		
		e2 = makeSymbolUnexpectedError(
			n3.sym, makeExpectedSymbolsForThings());
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int moveFromList(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode decor;
		GramContainer listGrm;
		Token listBeginToken;
		CharReaderContext cc;
		Symbol sym;
		int id;
		boolean handled;
		int moveResult;

		LangError e2;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		listGrm = (GramContainer) n.sym;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD) {
			if(n.state != SimpleReaderNode.STATE_FIRST_STEP) {
				removeStack(nodeStackIndex + 1);
				n.state = SimpleReaderNode.STATE_FIRST_STEP;
			}
		}
		
		if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			listBeginToken = (Token) listGrm.sym[0];
			
			setCcToTextIndex(0, listBeginToken.pastIndex);
			readRegularHighLevel(0, 1);
			
			if(dat.state == BaseModuleData.STATE_STUCK)
				return ModuleMoveResult.STUCK;

			n2 = getNextStateRequireSymbol();
			sym = n2.sym;
			id = utils.getSymbolIdPrimary(sym);
			
			handled = false;
			
			if(!handled)
			if(id == Symbols.TOKEN_RBRACK) {
				n2.decoration = true;

				setNode(nodeStackIndex + 1, n2);

				utils.copyTextIndex(listGrm.pastIndex, sym.pastIndex);
				n.state = SimpleReaderNode.STATE_AT_END;

				if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
					return ModuleMoveResult.AT_END;

				handled = true;
			}
			
			if(!handled)
			if(isSymbolAThing(sym)) {
				setNode(nodeStackIndex + 1, n2);
				n.state = SimpleReaderNode.STATE_WALKING_CHILDREN;
				
				if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
					return ModuleMoveResult.SUCCESS;
				
				handled = true;
			}
			
			if(!handled) {
				e2 = makeSymbolUnexpectedError(
					sym, makeExpectedSymbolsForListStuff());
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
				dat.stuckState = StuckStates.STATE_PERMANENT;
				dat.state = BaseModuleData.STATE_STUCK;
				return ModuleMoveResult.STUCK;
			}
		}
		
		// direction is TO_END or TO_NEXT
		
		if(n.state == SimpleReaderNode.STATE_WALKING_CHILDREN)
		while(true) {
			moveResult = ModuleMoveResult.INVALID;
			handled = false;
			
			n2 = (SimpleReaderNode) dat.nodeStack.get(nodeStackIndex + 1);

			if(n2.id != SimpleReaderNodeTypes.TYPE_TOKEN
				&& n2.id != SimpleReaderNodeTypes.TYPE_GRAM)
				throw new IllegalStateException();
			
			sym = n2.sym;
			id = utils.getSymbolIdPrimary(sym);

			if(isSymbolAThing(sym)) {
				moveResult = moveFromThing(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}

			if(id == Symbols.TOKEN_COMMA) {
				moveResult = moveFromComma(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
						
			if(!handled)
				throw new IllegalStateException();
			
			if(moveResult == ModuleMoveResult.SUCCESS)
				continue;
			
			if(moveResult == ModuleMoveResult.STUCK
				|| moveResult == ModuleMoveResult.INVALID) {

				return moveResult;
			}
			
			if(moveResult == ModuleMoveResult.AT_END) {
				n.state = SimpleReaderNode.STATE_FINALIZE;
				break;
			}

			throwInvalidEnum("ModuleMoveResult is bad");
		}

		if(n.state == SimpleReaderNode.STATE_FINALIZE) {
			decor = getDecorationRequireSymbol();
			utils.copyTextIndex(listGrm.pastIndex, decor.sym.pastIndex);

			removeStack(nodeStackIndex + 1);
			n.state = SimpleReaderNode.STATE_AT_END;
		}

		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT		
		
		return moveToNextAfterThing(nodeStackIndex);
	}

	private int moveFromDict(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		SimpleReaderNode decor;
		GramContainer dictGrm;
		Token dictBeginToken;
		CharReaderContext cc;
		Symbol sym;
		int id;
		boolean handled;
		int moveResult;

		LangError e2;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		dictGrm = (GramContainer) n.sym;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD) {
			if(n.state != SimpleReaderNode.STATE_FIRST_STEP) {
				removeStack(nodeStackIndex + 1);
				n.state = SimpleReaderNode.STATE_FIRST_STEP;
			}
		}
		
		if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			dictBeginToken = (Token) dictGrm.sym[0];
			
			setCcToTextIndex(0, dictBeginToken.pastIndex);
			readRegularHighLevel(0, 1);
			
			if(dat.state == BaseModuleData.STATE_STUCK)
				return ModuleMoveResult.STUCK;

			n2 = getNextStateRequireSymbol();
			sym = n2.sym;
			id = utils.getSymbolIdPrimary(sym);
			
			handled = false;
			
			if(!handled)
			if(id == Symbols.TOKEN_RBRACE) {
				removeStack(nodeStackIndex + 1);
				
				utils.copyTextIndex(dictGrm.pastIndex, sym.pastIndex);
				n.state = SimpleReaderNode.STATE_AT_END;

				if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
					return ModuleMoveResult.AT_END;

				handled = true;
			}
			
			if(!handled)
			if(isSymbolAThing(sym)) {
				n3 = makeNodeFromGram(makeDictEntryGram(n2.sym));
				
				setNode(nodeStackIndex + 1, n3);
				setNode(nodeStackIndex + 2, n2);
				
				n3.state = SimpleReaderNode.STATE_FIRST_STEP;
				n3.miniState = SimpleReaderNode.STATE_AT_END;
				
				n.state = SimpleReaderNode.STATE_WALKING_CHILDREN;
				
				if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
					return ModuleMoveResult.SUCCESS;
				
				handled = true;
			}
			
			if(!handled) {
				e2 = makeSymbolUnexpectedError(
					sym, makeExpectedSymbolsForDictStuff());
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
				dat.stuckState = StuckStates.STATE_PERMANENT;
				dat.state = BaseModuleData.STATE_STUCK;
				return ModuleMoveResult.STUCK;
			}
		}
		
		// direction is TO_END or TO_NEXT
		
		if(n.state == SimpleReaderNode.STATE_WALKING_CHILDREN)
		while(true) {
			moveResult = ModuleMoveResult.INVALID;
			handled = false;
			
			n2 = (SimpleReaderNode) dat.nodeStack.get(nodeStackIndex + 1);

			if(n2.id != SimpleReaderNodeTypes.TYPE_TOKEN
				&& n2.id != SimpleReaderNodeTypes.TYPE_GRAM)
				throw new IllegalStateException();
			
			sym = n2.sym;
			id = utils.getSymbolIdPrimary(sym);

			if(id == Symbols.GRAM_DICT_ENTRY) {
				moveResult = moveFromDictEntry(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}

			if(id == Symbols.TOKEN_COMMA) {
				moveResult = moveFromComma(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}

			if(!handled)
				throw new IllegalStateException();
			
			if(moveResult == ModuleMoveResult.SUCCESS)
				continue;
			
			if(moveResult == ModuleMoveResult.STUCK
				|| moveResult == ModuleMoveResult.INVALID) {

				return moveResult;
			}
			
			if(moveResult == ModuleMoveResult.AT_END) {
				n.state = SimpleReaderNode.STATE_FINALIZE;
				break;
			}

			throwInvalidEnum("ModuleMoveResult is bad");
		}

		if(n.state == SimpleReaderNode.STATE_FINALIZE) {
			decor = getDecorationRequireSymbol();
			utils.copyTextIndex(dictGrm.pastIndex, decor.sym.pastIndex);

			removeStack(nodeStackIndex + 1);
			n.state = SimpleReaderNode.STATE_AT_END;
		}

		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT		
		
		return moveToNextAfterThing(nodeStackIndex);
	}
	
	private int moveFromDictEntry(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode decor;
		GramContainer dictEntGrm;
		CharReaderContext cc;
		Symbol sym;
		int id;
		boolean handled;
		int moveResult;
		
		LangError e2;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		dictEntGrm = (GramContainer) n.sym;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD) {
			if(n.state != SimpleReaderNode.STATE_FIRST_STEP) {
				removeStack(nodeStackIndex + 1);
				n.state = SimpleReaderNode.STATE_FIRST_STEP;
				n.miniState = SimpleReaderNode.STATE_FIRST_STEP;
			}
		}
		
		if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			if(n.miniState == SimpleReaderNode.STATE_FIRST_STEP) {
				setCcToTextIndex(0, dictEntGrm.startIndex);
				readRegularHighLevel(0, 1);

				if(dat.state == BaseModuleData.STATE_STUCK)
					return ModuleMoveResult.STUCK;

				n2 = getNextStateRequireSymbol();
				sym = n2.sym;

				handled = false;

				if(!handled)
				if(isSymbolAThing(sym)) {
					setNode(nodeStackIndex + 1, n2);
					n.miniState = SimpleReaderNode.STATE_AT_END;

					handled = true;
				}

				if(!handled) {
					e2 = makeSymbolUnexpectedError(
						sym, makeExpectedSymbolsForThings());
					dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
					dat.stuckState = StuckStates.STATE_PERMANENT;
					dat.state = BaseModuleData.STATE_STUCK;
					return ModuleMoveResult.STUCK;
				}
			}
			
			if(n.miniState != SimpleReaderNode.STATE_AT_END)
				throw new IllegalStateException();

			if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
				return ModuleMoveResult.SUCCESS;
			
			n.state = SimpleReaderNode.STATE_WALKING_CHILDREN;
			n.miniState = SimpleReaderNode.STATE_FIRST_STEP;
		}
		
		// direction is TO_END or TO_NEXT
		
		if(n.state == SimpleReaderNode.STATE_WALKING_CHILDREN)
		while(true) {
			moveResult = ModuleMoveResult.INVALID;
			handled = false;
			
			n2 = (SimpleReaderNode) dat.nodeStack.get(nodeStackIndex + 1);

			if(n2.id != SimpleReaderNodeTypes.TYPE_TOKEN
				&& n2.id != SimpleReaderNodeTypes.TYPE_GRAM)
				throw new IllegalStateException();
			
			sym = n2.sym;
			id = utils.getSymbolIdPrimary(sym);

			if(isSymbolAThing(sym)) {
				moveResult = moveFromThing(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}

			if(id == Symbols.TOKEN_COLON) {
				moveResult = moveFromColon(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
						
			if(!handled)
				throw new IllegalStateException();
			
			if(moveResult == ModuleMoveResult.SUCCESS)
				continue;
			
			if(moveResult == ModuleMoveResult.STUCK
				|| moveResult == ModuleMoveResult.INVALID) {

				return moveResult;
			}
			
			if(moveResult == ModuleMoveResult.AT_END) {
				n.state = SimpleReaderNode.STATE_FINALIZE;
				break;
			}

			throwInvalidEnum("ModuleMoveResult is bad");
		}

		if(n.state == SimpleReaderNode.STATE_FINALIZE) {
			n2 = getNormalNodeRequireSymbol(nodeStackIndex + 1);
			
			utils.copyTextIndex(dictEntGrm.pastIndex, n2.sym.pastIndex);

			removeStack(nodeStackIndex + 1);
			n.state = SimpleReaderNode.STATE_AT_END;
		}

		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT		
		
		return moveToNextAfterDictEntry(nodeStackIndex);
	}
	
	private int moveFromComma(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		SimpleReaderNode n4;
		SimpleReaderNode decor;
		GramContainer listGrm;
		Token listBeginToken;
		CharReaderContext cc;
		Symbol sym;
		boolean handled;
		int moveResult;
		boolean isList;
		boolean isDict;
		int id;
		
		LangError e2;

		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		if(direction != ModuleMoveDirection.TO_NEXT)
			return ModuleMoveResult.INVALID;

		n = getNormalNodeRequireSymbol(nodeStackIndex);
		
		n2 = null;
		if(nodeStackIndex > 0)
			n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
		
		id = Symbols.SYMBOL_UNSPECIFIED;
		if(n2 != null) id = utils.getSymbolIdPrimary(n2.sym);

		isList = false;
		if(id == Symbols.GRAM_LIST) isList = true;
		
		isDict = false;
		if(id == Symbols.GRAM_DICT) isDict = true;
		
		n3 = null;
		
		if(n2 != null && !isList && !isDict)
			throw new IllegalStateException();
		
		setCcToTextIndex(0, n.sym.pastIndex);
		readRegularHighLevel(0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n3 = getNextStateRequireSymbol();

		if(isList || n2 == null) {
			if(isSymbolAThing(n3.sym)) {
				setNode(nodeStackIndex, n3);
				if(nodeStackIndex == dat.nodeStackIndex)
					updateState();
				return ModuleMoveResult.SUCCESS;
			}
			
			e2 = makeSymbolUnexpectedError(
				n3.sym, makeExpectedSymbolsForThings());
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.state = BaseModuleData.STATE_STUCK;
			return ModuleMoveResult.STUCK;
		}
		
		if(isDict) {
			if(isSymbolAThing(n3.sym)) {
				n4 = makeNodeFromGram(makeDictEntryGram(n3.sym));
				
				setNode(nodeStackIndex, n4);
				if(nodeStackIndex == dat.nodeStackIndex)
					updateState();
				
				setNode(nodeStackIndex + 1, n3);
				n4.state = SimpleReaderNode.STATE_FIRST_STEP;
				n4.miniState = SimpleReaderNode.STATE_AT_END;
				
				return ModuleMoveResult.SUCCESS;
			}
			
			e2 = makeSymbolUnexpectedError(
				n3.sym, makeExpectedSymbolsForThings());
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.state = BaseModuleData.STATE_STUCK;
			return ModuleMoveResult.STUCK;
		}
				
		throw new IllegalStateException();
	}

	private int moveFromColon(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		SimpleReaderNode n4;
		CharReaderContext cc;
		Symbol sym;
		GramContainer dictEntGrm;
		boolean handled;
		int moveResult;
		boolean isDictEntry;
		int id;

		LangError e2;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		if(direction != ModuleMoveDirection.TO_NEXT)
			return ModuleMoveResult.INVALID;

		n = getNormalNodeRequireSymbol(nodeStackIndex);
		
		n2 = null;
		if(nodeStackIndex > 0)
			n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
		
		id = Symbols.SYMBOL_UNSPECIFIED;
		if(n2 != null) id = utils.getSymbolIdPrimary(n2.sym);
		
		isDictEntry = false;
		if(id == Symbols.GRAM_DICT_ENTRY) isDictEntry = true;
		
		if(!isDictEntry)
			throw new IllegalStateException();
		
		dictEntGrm = (GramContainer) n2.sym;
		
		if(dictEntGrm.sym[2] != null
			|| dictEntGrm.sym[1] == null)
			
			throw new IllegalStateException();
		
		n3 = null;
		
		setCcToTextIndex(0, n.sym.pastIndex);
		readRegularHighLevel(0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n3 = getNextStateRequireSymbol();

		if(isSymbolAThing(n3.sym)) {
			setNode(nodeStackIndex, n3);
			if(nodeStackIndex == dat.nodeStackIndex)
				updateState();
			
			dictEntGrm.sym[2] = n3.sym;
			return ModuleMoveResult.SUCCESS;
		}
			
		e2 = makeSymbolUnexpectedError(
			n3.sym, makeExpectedSymbolsForThings());
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int moveToNextAfterThing(int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		SimpleReaderNode decor;
		GramContainer listGrm;
		GramContainer dictEntGrm;
		Token listBeginToken;
		CharReaderContext cc;
		Symbol sym;
		boolean handled;
		int moveResult;
		boolean isList;
		boolean isDictEntry;
		int id;
		
		LangError e2;
		
		n = getNormalNodeRequireSymbol(nodeStackIndex);
		
		n2 = null;
		if(nodeStackIndex > 0)
			n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
	
		id = Symbols.SYMBOL_UNSPECIFIED;
		if(n2 != null) id = utils.getSymbolIdPrimary(n2.sym);

		isList = false;
		if(id == Symbols.GRAM_LIST) isList = true;
		
		isDictEntry = false;
		if(id == Symbols.GRAM_DICT_ENTRY) isDictEntry = true;
		
		// before read
		
		dictEntGrm = null;
		
		if(isDictEntry) {
			dictEntGrm = (GramContainer) n2.sym;
						
			if(dictEntGrm.sym[2] != null)
				return ModuleMoveResult.AT_END;
		}
		
		n3 = null;
		
		setCcToTextIndex(0, n.sym.pastIndex);
		readRegularHighLevel(0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n3 = getNextStateRequireSymbol();
		
		// after read
		
		if(isList) {
			id = utils.getSymbolIdPrimary(n3.sym);
			
			if(id == Symbols.TOKEN_COMMA) {
				setNode(nodeStackIndex, n3);
				if(nodeStackIndex == dat.nodeStackIndex)
					updateState();
				return ModuleMoveResult.SUCCESS;
			}
			
			if(id == Symbols.TOKEN_RBRACK) {
				n3.decoration = true;
				setNode(nodeStackIndex + 1, n3);
				return ModuleMoveResult.AT_END;
			}
			
			e2 = makeSymbolUnexpectedError(
				n3.sym, makeExpectedSymbolsForListStuffAfterThing());
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.state = BaseModuleData.STATE_STUCK;
			return ModuleMoveResult.STUCK;
		}
		
		if(isDictEntry) {
			id = utils.getSymbolIdPrimary(n3.sym);
			
			if(dictEntGrm.sym[1] == null) {
				// we can fill in the COLON seperator
				
				if(id == Symbols.TOKEN_COLON) {
					setNode(nodeStackIndex, n3);
					if(nodeStackIndex == dat.nodeStackIndex)
						updateState();
					
					dictEntGrm.sym[1] = n3.sym;
					return ModuleMoveResult.SUCCESS;
				}
				
				e2 = makeSymbolUnexpectedError(
					n3.sym, makeExpectedSymbolsForNameValueSeparator());
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
				dat.stuckState = StuckStates.STATE_PERMANENT;
				dat.state = BaseModuleData.STATE_STUCK;
				return ModuleMoveResult.STUCK;
			}
			
			throw new IllegalStateException();
		}
		
		if(n2 == null) {
			id = utils.getSymbolIdPrimary(n3.sym);

			if(id == Symbols.TOKEN_END_OF_STREAM) {
				// this is the normal conclusion
				setNode(nodeStackIndex, n3);
				if(nodeStackIndex == dat.nodeStackIndex)
					updateState();
				return ModuleMoveResult.SUCCESS;
			}
			
			if(id == Symbols.TOKEN_COMMA) {
				// for a implicit root list
				setNode(nodeStackIndex, n3);
				if(nodeStackIndex == dat.nodeStackIndex)
					updateState();
				return ModuleMoveResult.SUCCESS;
			}
			
			e2 = makeSymbolUnexpectedError(
				n3.sym, makeExpectedSymbolsForAfterRootThing());
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.state = BaseModuleData.STATE_STUCK;
			return ModuleMoveResult.STUCK;
		}
		
		throw new IllegalStateException();
	}

	private int moveToNextAfterDictEntry(int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		CharReaderContext cc;
		boolean isDict;
		int id;
		
		LangError e2;
		
		n = getNormalNodeRequireSymbol(nodeStackIndex);
		
		n2 = null;
		if(nodeStackIndex > 0)
			n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
	
		id = Symbols.SYMBOL_UNSPECIFIED;
		if(n2 != null) id = utils.getSymbolIdPrimary(n2.sym);
		
		isDict = false;
		if(id == Symbols.GRAM_DICT) isDict = true;
		
		if(!isDict)
			throw new IllegalStateException();
		
		n3 = null;
		
		setCcToTextIndex(0, n.sym.pastIndex);
		readRegularHighLevel(0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n3 = getNextStateRequireSymbol();
		id = utils.getSymbolIdPrimary(n3.sym);

		if(id == Symbols.TOKEN_COMMA) {
			setNode(nodeStackIndex, n3);
			if(nodeStackIndex == dat.nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}

		if(id == Symbols.TOKEN_RBRACE) {
			n3.decoration = true;
			setNode(nodeStackIndex + 1, n3);
			return ModuleMoveResult.AT_END;
		}
		
		e2 = makeSymbolUnexpectedError(
			n3.sym, makeExpectedSymbolsForDictStuffAfterDictEntry());
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int moveFromContent(int direction, int nodeStackIndex) {
		SimpleReaderString cont;
		Symbol sym;
		int id;
		boolean inQuote;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode decor;
		boolean handled;
		int moveResult;
		CharReaderContext cc;
		
		LangError e2;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		cont = (SimpleReaderString) n.sym;
		
		inQuote = true;
				
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD) {
			if(n.state != SimpleReaderNode.STATE_FIRST_STEP) {
				n.state = SimpleReaderNode.STATE_FIRST_STEP;
				n.miniState = SimpleReaderNode.STATE_FIRST_STEP;
			}
		}
	
		if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			if(n.miniState == SimpleReaderNode.STATE_FIRST_STEP) {
				setCcToTextIndex(0, cont.elementsStartIndex);
				readContent(0, 1);

				if(dat.state == BaseModuleData.STATE_STUCK)
					return ModuleMoveResult.STUCK;

				n2 = getNextStateRequireSymbol();
				sym = n2.sym;
				id = utils.getSymbolIdPrimary(sym);

				handled = false;

				if(!handled)
				if(id == Symbols.TOKEN_STRING_SPAN
					|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC
					|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY
					|| id == Symbols.TOKEN_BAD_SPAN) {

					setNode(nodeStackIndex + 1, n2);

					n.miniState = SimpleReaderNode.STATE_AT_END;
					handled = true;
				}

				if(!handled)
				if(id == Symbols.TOKEN_QUOTE
					|| id == Symbols.TOKEN_END_OF_STREAM
					|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM) {

					n2.decoration = true;

					setNode(nodeStackIndex + 1, n2);

					utils.copyTextIndex(
						cont.elementsPastIndex, sym.startIndex);
					utils.copyTextIndex(
						cont.pastIndex, sym.pastIndex);
					
					n.state = SimpleReaderNode.STATE_AT_END;
					n.miniState = SimpleReaderNode.STATE_FIRST_STEP;

					if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
						return ModuleMoveResult.AT_END;

					handled = true;
				}

				if(!handled) {
					e2 = makeSymbolUnexpectedError(
						sym, makeExpectedSymbolsForContent(inQuote));
					dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
					dat.stuckState = StuckStates.STATE_PERMANENT;
					dat.state = BaseModuleData.STATE_STUCK;
					return ModuleMoveResult.STUCK;
				}
			}
		}
		
		if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			if(n.miniState != SimpleReaderNode.STATE_AT_END)
				throw new IllegalStateException();
			
			n.state = SimpleReaderNode.STATE_WALKING_CHILDREN;
			n.miniState = SimpleReaderNode.STATE_FIRST_STEP;

			if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
				return ModuleMoveResult.SUCCESS;
		}
		
		// direction is TO_END or TO_NEXT
		
		if(n.state == SimpleReaderNode.STATE_WALKING_CHILDREN)
		while(true) {
			moveResult = ModuleMoveResult.INVALID;
			handled = false;
			
			n2 = (SimpleReaderNode) dat.nodeStack.get(nodeStackIndex + 1);
			
			if(n2.id != SimpleReaderNodeTypes.TYPE_TOKEN
				&& n2.id != SimpleReaderNodeTypes.TYPE_GRAM)
				throw new IllegalStateException();
			
			sym = n2.sym;
			id = utils.getSymbolIdPrimary(sym);

			if(id == Symbols.TOKEN_STRING_SPAN
				|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC
				|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY) {

				moveResult = moveFromContentToken(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
			
			if(id == Symbols.TOKEN_BAD_SPAN) {
				moveResult = moveFromBadSpan(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
			
			if(!handled)
				throw new IllegalStateException();
			
			if(moveResult == ModuleMoveResult.SUCCESS)
				continue;
			
			if(moveResult == ModuleMoveResult.STUCK
				|| moveResult == ModuleMoveResult.INVALID) {

				return moveResult;
			}
			
			if(moveResult == ModuleMoveResult.AT_END) {
				n.state = SimpleReaderNode.STATE_FINALIZE;
				break;
			}

			throwInvalidEnum("ModuleMoveResult is bad");
		}
		
		if(n.state == SimpleReaderNode.STATE_FINALIZE) {
			decor = getDecorationRequireSymbol();
			n2 = getNormalNodeRequireSymbol(nodeStackIndex + 1);

			utils.copyTextIndex(
				cont.elementsPastIndex, decor.sym.startIndex);
			utils.copyTextIndex(
				cont.pastIndex, decor.sym.pastIndex);
			
			removeStack(nodeStackIndex + 1);

			n.state = SimpleReaderNode.STATE_AT_END;
		}
		
		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT
		
		return moveToNextAfterThing(nodeStackIndex);
	}
	
	private int moveFromTokenThing(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
			
		if(direction != ModuleMoveDirection.TO_NEXT)
			return ModuleMoveResult.INVALID;
		
		n = getNormalNodeRequireToken(nodeStackIndex);
		n.lastKnownDirection = direction;

		return moveToNextAfterThing(nodeStackIndex);
	}
	
	private int moveFromThing(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		Symbol sym;
		int id;
		
		n = getNormalNodeRequireSymbol(nodeStackIndex);
		sym = n.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_LIST)
			return moveFromList(direction, nodeStackIndex);

		if(id == Symbols.GRAM_DICT)
			return moveFromDict(direction, nodeStackIndex);
		
		if(id == Symbols.GRAM_XML_CONTENT)
			return moveFromContent(direction, nodeStackIndex);
		
		if(id == Symbols.TOKEN_KEYWORD_NULL)
			return moveFromTokenThing(direction, nodeStackIndex);
		
		throw new IllegalStateException();
	}

	private int moveFromContentToken(int direction, int nodeStackIndex) {
		Token tok;
		Symbol sym;
		int id;
		boolean inQuote;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		CharReaderContext cc;
		
		LangError e2;

		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction != ModuleMoveDirection.TO_NEXT) {
			return ModuleMoveResult.INVALID;
		}
		
		// direction is TO_NEXT
		
		n = getNormalNodeRequireToken(nodeStackIndex);
		n.lastKnownDirection = direction;
		tok = (Token) n.sym;

		if(nodeStackIndex < 1)
			throw new IllegalStateException();
		
		n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
		sym = (Gram) n2.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id != Symbols.GRAM_XML_CONTENT)
			throw new IllegalStateException();
		
		inQuote = true;
		
		setCcToTextIndex(0, tok.pastIndex);
		readContent(0, 1);
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n2 = getNextStateRequireSymbol();
		sym = n2.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.TOKEN_STRING_SPAN
			|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC
			|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY
			|| id == Symbols.TOKEN_BAD_SPAN) {

			setNode(nodeStackIndex, n2);
			n2.lastKnownDirection = ModuleMoveDirection.TO_HERE;
			if(dat.nodeStackIndex == nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}

		if(id == Symbols.TOKEN_END_OF_STREAM
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM) {

			n2.decoration = true;
			setNode(nodeStackIndex + 1, n2);
			return ModuleMoveResult.AT_END;
		}
		
		if(inQuote)
		if(id == Symbols.TOKEN_QUOTE) {
			n2.decoration = true;
			setNode(nodeStackIndex + 1, n2);
			return ModuleMoveResult.AT_END;
		}
				
		e2 = makeSymbolUnexpectedError(sym, 
			makeExpectedSymbolsForContent(inQuote));
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int moveFromBadSpan(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		Symbol sym;
		int id;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;

		if(direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END)
			return ModuleMoveResult.INVALID;

		n = getNormalNodeRequireToken(nodeStackIndex);
		n.lastKnownDirection = direction;
		sym = n.sym;
		
		// direction is TO_NEXT or TO_END
						
		if(nodeStackIndex < 1)
			throw new IllegalStateException();

		n = getNormalNodeRequireGram(nodeStackIndex - 1);
		sym = n.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_XML_CONTENT)
			return moveFromContentToken(direction, nodeStackIndex);
				
		// unexpected stack state
		throw new IllegalStateException();
	}

	private SymbolId makeIntegerSpecifier(
		int categoryId, int primaryId) {
		
		SymbolId is;
		
		is = makeSymbolId(2);
		is.aryPtr[0] = categoryId;
		is.aryPtr[1] = primaryId;
		return is;
	}
	
	private CommonArrayList makeExpectedSymbolsForContent(boolean inQuote) {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForEndOfStream(es);
		addExpectedSymbolsForContentElements(es);
		addExpectedSymbolsForBadSpan(es);
		
		if(inQuote) {
			is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_QUOTE);
			es.add(is);
		}
		
		return es;
	}
	
	private CommonArrayList makeExpectedSymbolsForThings() {
		CommonArrayList es;
		
		es = makeArrayList();
		
		addExpectedSymbolsForThings(es);
		
		return es;
	}
	
	private CommonArrayList makeExpectedSymbolsForListStuff() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForThings(es);
				
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_RBRACK);
		es.add(is);
		
		return es;
	}

	private CommonArrayList makeExpectedSymbolsForDictStuff() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForThings(es);
				
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_RBRACE);
		es.add(is);
		
		return es;
	}

	private CommonArrayList makeExpectedSymbolsForAfterRootThing() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForComma(es);
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_BASIC, Symbols.TOKEN_END_OF_STREAM);
		es.add(is);
		
		return es;
	}

	private CommonArrayList makeExpectedSymbolsForListStuffAfterThing() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForComma(es);
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_RBRACK);
		es.add(is);
		
		return es;
	}
	
	private CommonArrayList makeExpectedSymbolsForDictStuffAfterDictEntry() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForComma(es);
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_RBRACE);
		es.add(is);
		
		return es;
	}

	private CommonArrayList makeExpectedSymbolsForNameValueSeparator() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForThings(es);
				
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_COLON);
		es.add(is);
		
		return es;
	}

	private void addExpectedSymbolsForThings(CommonArrayList es) {
		SymbolId is;
		
		addExpectedSymbolsForEndOfStream(es);
		addExpectedSymbolsForContainers(es);
		addExpectedSymbolsForNameThings(es);

		is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_QUOTE);
		es.add(is);
	}
	
	private void addExpectedSymbolsForContainers(CommonArrayList es) {
		SymbolId is;
		
		is = makeIntegerSpecifier(
			Symbols.GRAM_CATEGORY_DATA, Symbols.GRAM_LIST);
		es.add(is);

		is = makeIntegerSpecifier(
			Symbols.GRAM_CATEGORY_DATA, Symbols.GRAM_DICT);
		es.add(is);
	}
	
	private void addExpectedSymbolsForNameThings(CommonArrayList es) {
		SymbolId is;
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_KEYWORDS_DATA, Symbols.TOKEN_KEYWORD_NULL);
		es.add(is);
	}
	
	private void addExpectedSymbolsForContentElements(CommonArrayList es) {
		SymbolId is;
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_STRING, Symbols.TOKEN_STRING_SPAN);
		es.add(is);
		
		is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_XML, Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC);
		es.add(is);

		is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_XML, Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY);
		es.add(is);
	}

	private void addExpectedSymbolsForComma(CommonArrayList es) {
		SymbolId is;
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_COMMA);
		es.add(is);
	}
	
	private void addExpectedSymbolsForBadSpan(CommonArrayList es) {
		SymbolId is;
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_BASIC, Symbols.TOKEN_BAD_SPAN);
		es.add(is);
	}
	
	private void addExpectedSymbolsForEndOfStream(CommonArrayList es) {
		SymbolId is;
		int cat;
		
		cat = Symbols.TOKEN_CATEGORY_BASIC;
		
		is = makeIntegerSpecifier(
			cat, Symbols.TOKEN_END_OF_STREAM);
		es.add(is);
		
		is = makeIntegerSpecifier(
			cat, Symbols.TOKEN_UNEXPECTED_END_OF_STREAM);
		es.add(is);
	}	
	
	private void readRegularHighLevel(
		int ccStartIndex, int ccUnusedIndex) {
		
		Symbol sym;
		Token tok;
		int id;
		int cat;
		TokenChooserData chooseDat;
		Token keywordTok;

		CharReaderContext cc1;
		CharReaderContext cc2;
		int cc2Index;
		CharReaderContext cc3;
		int cc3Index;
		int ccUnusedNewIndex;

		cc1 = utils.getCharReaderContext(dat.ccStack, ccStartIndex);
		
		cc2Index = ccUnusedIndex;
		cc2 = utils.getCharReaderContext(dat.ccStack, cc2Index);

		cc3Index = cc2Index + 1;
		cc3 = utils.getCharReaderContext(dat.ccStack, cc3Index);

		ccUnusedNewIndex = cc3Index + 1;

		chooseDat = tokChoose.dat;
		
		utils.copyCharReaderContext(cc2, cc1);
		
		while(true) {
			readRegularLowLevel(cc2Index, ccUnusedNewIndex);
			
			id = dat.nextState.id;
			
			if(id != SimpleReaderNodeTypes.TYPE_TOKEN)
				throw new IllegalStateException();
			
			tok = (Token) dat.nextState.sym;
			id = utils.getSymbolIdPrimary(tok);
			cat = utils.getSymbolIdCategory(tok);
			
			if(cat == Symbols.TOKEN_CATEGORY_COMMENT) {
				utils.copyTextIndex(cc2.ti, tok.pastIndex);
				cc2.bi.versionNumber = ReadBuffer.VERSION_NUMBER_INVALID;
				
				dat.nextState = null;
				
				continue;
			}
			
			if(id == Symbols.TOKEN_LBRACK) {
				dat.nextState = makeNodeFromGram(makeListGram(tok));
				return;
			}
			
			if(id == Symbols.TOKEN_LBRACE) {
				dat.nextState = makeNodeFromGram(makeDictGram(tok));
				return;
			}
			
			if(id == Symbols.TOKEN_QUOTE) {
				dat.nextState = makeNodeFromGram(makeContentGram(
					tok.startIndex, tok.pastIndex));
				return;
			}
			
			if(id == Symbols.TOKEN_IDENTIFIER) {
				tokChoose.reset();

				chooseDat.possibleHelpers.add(keywordsHelp);

				utils.copyTextIndex(cc3.ti, tok.startIndex);
				cc3.bi.versionNumber = ReadBuffer.VERSION_NUMBER_INVALID;

				tokChoose.chooseToken(cc3);

				if(chooseDat.state == TokenChooserData.STATE_STUCK) {
					dat.state = BaseModuleData.STATE_STUCK;
					dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
					return;
				}

				if(chooseDat.state != TokenChooserData.STATE_CHOSEN) {
					throwInvalidEnum("TokenChooser has a bad state");
				}

				if(chooseDat.resultCount > 1)
					throw new IllegalStateException();

				if(chooseDat.resultCount == 1) {
					keywordTok = chooseDat.chosenHelper.getToken();
					
					if(keywordTok.pastIndex.index
						== tok.pastIndex.index) {

						// is a keyword
						dat.nextState = makeNodeFromToken(keywordTok);
						return;
					}
				}
			}
			
			// accept symbol as is
			return;
		}
		
		// unreachable
	}
	
	private void readRegularLowLevel(
		int ccStartIndex, int ccUnusedIndex) {
		
		Token tok;
		boolean prevCharWas13;
		TokenChooserData chooseDat;
		boolean bad;
		
		CharReader2Data charDat = (CharReader2Data) charRead.getData();

		CharReaderContext cc1;
		CharReaderContext cc2;
		int cc2Index;
		CharReaderContext cc3;
		int cc3Index;
		int ccUnusedNewIndex;

		cc1 = utils.getCharReaderContext(dat.ccStack, ccStartIndex);
		
		cc2Index = ccUnusedIndex;
		cc2 = utils.getCharReaderContext(dat.ccStack, cc2Index);
		
		cc3Index = cc2Index + 1;
		cc3 = utils.getCharReaderContext(dat.ccStack, cc3Index);
		
		ccUnusedNewIndex = cc3Index + 1;

		chooseDat = tokChoose.dat;
		
		utils.copyCharReaderContext(cc2, cc1);

		prevCharWas13 = false;

		while(true) {
			readPastBadChars(cc2);

			if(cc2.state == BaseModuleData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc2.state == CharReaderData.STATE_END_OF_STREAM) {
				// make end of stream
				dat.nextState = makeNodeFromToken(
					makeEndOfStreamToken(false, cc2.ti));
				return;
			}

			if(cc2.state != CharReaderData.STATE_HAVE_CHAR) {
				throwInvalidEnum("CharReader has an invalid state");
			}

			cc2.resultChar = charDat.resultChar;

			if(prevCharWas13) {
				if(cc2.resultChar == 0xA) {
					// skip a the 0xA in an (0xD,0xA) combo
					
					utils.charReaderContextSkip(cc2);
					utils.textIndexSkipReturn(cc2.ti);
					prevCharWas13 = false;
					continue;
				}
				
				// previous char was a lone 0xD
				utils.textIndexSkipReturn(cc2.ti);
				prevCharWas13 = false;
			}
			
			if(cc2.resultChar == 0xA) {
				utils.charReaderContextSkip(cc2);
				utils.textIndexSkipReturn(cc2.ti);
				continue;
			}
			
			if(cc2.resultChar == 0xD) {
				prevCharWas13 = true;
				utils.charReaderContextSkip(cc2);
				continue;
			}

			if(cc2.resultChar == 0x9 || cc2.resultChar == ' ') {
				utils.charReaderContextSkip(cc2);
				continue;
			}
			
			// test for punct/keywords/comments
			
			tokChoose.reset();
			
			chooseDat.possibleHelpers.add(punctHelp);
			chooseDat.possibleHelpers.add(commentHelp);
			chooseDat.possibleHelpers.add(varHelp);
			
			utils.copyCharReaderContext(cc3, cc2);
		
			tokChoose.chooseToken(cc3);
			
			if(chooseDat.state == TokenChooserData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}
			
			if(chooseDat.state != TokenChooserData.STATE_CHOSEN) {
				throwInvalidEnum("TokenChooser has a bad state");
			}

			if(chooseDat.resultCount > 1)
				throw new IllegalStateException();
			
			if(chooseDat.resultCount == 1) {
				// xml name token
				tok = chooseDat.chosenHelper.getToken();
				
				dat.nextState = makeNodeFromToken(tok);
				return;
			}

			// not punct, is bad char
			
			utils.copyCharReaderContext(cc3, cc2);
			
			utils.charReaderContextSkip(cc3);
			
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeBadSpanError(cc2.ti, cc3.ti));

			dat.nextState = makeNodeFromToken(
				makeBadSpanToken(cc2.ti, cc3.ti));
			return;
		}
		
		// unreachable
	}
		
	private void readContent(int ccStartIndex,
		int ccUnusedIndex) {
	
		Token tok;
		boolean prevCharWas13;
		TokenChooserData chooseDat;
		boolean inQuote;
		
		CharReader2Data charDat = (CharReader2Data) charRead.getData();

		CharReaderContext cc1;
		CharReaderContext cc2;
		int cc2Index;
		CharReaderContext cc3;
		int cc3Index;
		int ccUnusedNewIndex;
		
		cc1 = utils.getCharReaderContext(dat.ccStack, ccStartIndex);
		
		cc2Index = ccUnusedIndex;
		cc2 = utils.getCharReaderContext(dat.ccStack, cc2Index);
		
		cc3Index = cc2Index + 1;
		cc3 = utils.getCharReaderContext(dat.ccStack, cc3Index);
		
		ccUnusedNewIndex = cc3Index + 1;
		
		chooseDat = tokChoose.dat;
		inQuote = true;
		
		utils.copyCharReaderContext(cc2, cc1);
		
		readPastBadChars(cc2);
		
		if(cc2.state == BaseModuleData.STATE_STUCK) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
			return;
		}

		if(cc2.state == CharReaderData.STATE_END_OF_STREAM) {
			if(inQuote) {
				// make unexpected end of stream
				dat.nextState = makeNodeFromToken(
					makeEndOfStreamToken(true, cc2.ti));
				return;
			}
			
			// make end of stream
			dat.nextState = makeNodeFromToken(
				makeEndOfStreamToken(false, cc2.ti));
			return;
		}
		
		if(cc2.state != CharReaderData.STATE_HAVE_CHAR) {
			throwInvalidEnum("CharReader has an invalid state");
		}
		
		cc2.resultChar = charDat.resultChar;
		
		// first char

		if(cc2.resultChar == '<') {
			if(inQuote) {
				utils.copyCharReaderContext(cc3, cc2);
				
				// skip past the '<'
				utils.charReaderContextSkip(cc3);
				
				// make a bad span with the "<"
				
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeBadSpanError(cc1.ti, cc3.ti));
				
				dat.nextState = makeNodeFromToken(
					makeBadSpanToken(cc1.ti, cc3.ti));
				return;
			}
			
			//readTextLessThan(ccStartIndex, cc2Index, ccUnusedNewIndex);
			//return;
			throw new IllegalStateException();
		}
		
		if(cc2.resultChar == '&') {
			tokChoose.reset();
			
			chooseDat.possibleHelpers.add(refHelp);
			
			utils.copyCharReaderContext(cc3, cc2);
		
			tokChoose.chooseToken(cc3);
			
			if(chooseDat.state == TokenChooserData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}
			
			if(chooseDat.state != TokenChooserData.STATE_CHOSEN) {
				throwInvalidEnum("TokenChooser has a bad state");
			}

			if(chooseDat.resultCount > 1)
				throw new IllegalStateException();

			if(chooseDat.resultCount == 1) {
				// xml name token
				tok = chooseDat.chosenHelper.getToken();
				
				dat.nextState = makeNodeFromToken(tok);
				return;
			}
		}
		
		if(inQuote)
		if(cc2.resultChar == '"') {
			utils.copyCharReaderContext(cc3, cc2);
			
			// skip past the '"'
			utils.charReaderContextSkip(cc3);
			
			dat.nextState = makeNodeFromToken(
				makeQuoteEndToken(cc2.ti, cc3.ti));
			return;
		}

		// we read our first regular string span char
		// now we collect as much of them as possible
		// read the span
		
		utils.copyCharReaderContext(cc3, cc2);
				
		prevCharWas13 = false;
		
		// the loop reads the first char over again, but
		// it simplifies everything, so I dont care
		
		while(true) {
			readPastBadChars(cc3);

			if(cc3.state == BaseModuleData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc3.state == CharReaderData.STATE_END_OF_STREAM) {
				if(prevCharWas13) {
					// previous char was a lone 0xD
					utils.textIndexSkipReturn(cc3.ti);
					prevCharWas13 = false;
				}
				
				dat.nextState = makeNodeFromToken(
					makeStringSpanToken(cc1.ti, cc3.ti));
				return;
			}

			if(cc3.state != CharReaderData.STATE_HAVE_CHAR) {
				throwInvalidEnum("CharReader has an invalid state");
			}

			cc3.resultChar = charDat.resultChar;

			// possible span char
			
			if(cc3.resultChar == '<' || cc3.resultChar == '&') {
				// our span is terminated
				
				if(prevCharWas13) {
					// previous char was a lone 0xD
					utils.textIndexSkipReturn(cc3.ti);
					prevCharWas13 = false;
				}
				
				dat.nextState = makeNodeFromToken(
					makeStringSpanToken(cc1.ti, cc3.ti));
				return;
			}
			
			if(inQuote)
			if(cc3.resultChar == '"') {
				// our span is terminated

				if(prevCharWas13) {
					// previous char was a lone 0xD
					utils.textIndexSkipReturn(cc3.ti);
					prevCharWas13 = false;
				}

				dat.nextState = makeNodeFromToken(
					makeStringSpanToken(cc1.ti, cc3.ti));
				return;
			}
			
			// span char, skip it
			
			if(prevCharWas13) {
				if(cc3.resultChar == 0xA) {
					// skip a the 0xA in an (0xD,0xA) combo
					
					utils.charReaderContextSkip(cc3);
					utils.textIndexSkipReturn(cc3.ti);
					prevCharWas13 = false;
					continue;
				}
				
				// previous char was a lone 0xD
				utils.textIndexSkipReturn(cc3.ti);
				prevCharWas13 = false;
			}
			
			if(cc3.resultChar == 0xA) {
				utils.charReaderContextSkip(cc3);
				utils.textIndexSkipReturn(cc3.ti);
				continue;
			}
			
			if(cc3.resultChar == 0xD) {
				prevCharWas13 = true;
				utils.charReaderContextSkip(cc3);
				continue;
			}
			
			// skip normal span char
			utils.charReaderContextSkip(cc3);
			continue;
		}
		
		// unreachable
	}
	
	private void readPastBadChars(CharReaderContext cc) {
		charRead.readChar(cc.ti, cc.bi);

		CharReader2Data charDat = (CharReader2Data) charRead.getData();
		
		cc.state = charDat.state;
		cc.resultCharLength = charDat.resultCharLength;
		
		while(cc.state == CharReaderData.STATE_HAVE_BAD_CHAR) {
			utils.textIndexSkip(cc.ti, cc.resultCharLength);
			utils.bufferIndexSkip(cc.bi, cc.resultCharLength);
			
			charRead.readChar(cc.ti, cc.bi);
			
			cc.state = charDat.state;
			cc.resultCharLength = charDat.resultCharLength;
		}
	}

	private SimpleReaderNode makeNodeFromToken(Token tok) {
		SimpleReaderNode r;
		
		r = new SimpleReaderNode();
		r.id = SimpleReaderNodeTypes.TYPE_TOKEN;
		r.state = SimpleReaderNode.STATE_FIRST_STEP;
		r.miniState = SimpleReaderNode.STATE_FIRST_STEP;
		r.lastKnownDirection = 0;
		r.sym = tok;
		
		return r;
	}
	
	private SimpleReaderNode makeNodeFromGram(Gram grm) {
		SimpleReaderNode r;
		
		r = new SimpleReaderNode();
		r.id = SimpleReaderNodeTypes.TYPE_GRAM;
		r.state = SimpleReaderNode.STATE_FIRST_STEP;
		r.miniState = SimpleReaderNode.STATE_FIRST_STEP;
		r.lastKnownDirection = 0;
		r.sym = grm;
		
		return r;
	}
	
	private Token makeEndOfStreamToken(
		boolean unexpected, TextIndex ti) {
		Token tok;
		int id;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
	
		id = Symbols.TOKEN_END_OF_STREAM;
		if(unexpected)
			id = Symbols.TOKEN_UNEXPECTED_END_OF_STREAM;
		
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_BASIC, id);

		utils.copyTextIndex(tok.startIndex, ti);
		utils.copyTextIndex(tok.pastIndex, ti);
		
		return tok;
	}

	private Gram makeListGram(Token tok) {
		GramContainer listGrm;
		
		listGrm = makeGramContainer(2, 1);
		//listGrm.sym = new Symbol[1];

		listGrm.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(listGrm, Symbols.GRAM_CATEGORY_DATA,
			Symbols.GRAM_LIST);
		listGrm.sym[0] = tok;

		utils.copyTextIndex(listGrm.startIndex, tok.startIndex);
		utils.copyTextIndex(listGrm.pastIndex, tok.startIndex);
		
		return listGrm;
	}
	
	private Gram makeDictGram(Token tok) {
		GramContainer dictGrm;
		
		dictGrm = makeGramContainer(2, 1);
		//dictGrm.sym = new Symbol[1];

		dictGrm.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(dictGrm, Symbols.GRAM_CATEGORY_DATA,
			Symbols.GRAM_DICT);
		dictGrm.sym[0] = tok;

		utils.copyTextIndex(dictGrm.startIndex, tok.startIndex);
		utils.copyTextIndex(dictGrm.pastIndex, tok.startIndex);
		
		return dictGrm;
	}

	private Gram makeDictEntryGram(Symbol key) {
		GramContainer dictEntGrm;
		
		dictEntGrm = makeGramContainer(2, 3);
		//dictEntGrm.sym = new Symbol[3];
		
		dictEntGrm.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(dictEntGrm, Symbols.GRAM_CATEGORY_DATA,
			Symbols.GRAM_DICT_ENTRY);

		utils.copyTextIndex(
			dictEntGrm.startIndex, key.startIndex);
		utils.copyTextIndex(
			dictEntGrm.pastIndex, key.startIndex);

		dictEntGrm.sym[0] = key;

		return dictEntGrm;
	}

	private Gram makeContentGram(
		TextIndex quoteStartIndex,
		TextIndex contentStartIndex) {

		SimpleReaderString strGrm;
		
		strGrm = makeSimpleReaderString(2);
		
		strGrm.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(strGrm, Symbols.GRAM_CATEGORY_XML, 
			Symbols.GRAM_XML_CONTENT);
		
		utils.copyTextIndex(strGrm.startIndex, quoteStartIndex);
		utils.copyTextIndex(strGrm.pastIndex, quoteStartIndex);

		utils.copyTextIndex(strGrm.elementsStartIndex, contentStartIndex);
		utils.copyTextIndex(strGrm.elementsPastIndex, contentStartIndex);

		return strGrm;
	}

	private Token makeQuoteEndToken(
		TextIndex startIndex, TextIndex pastIndex) {
		
		Token tok;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_PUNCTUATION, 
			Symbols.TOKEN_QUOTE);

		// make the token cover the "\""

		utils.copyTextIndex(tok.startIndex, startIndex);
		utils.copyTextIndex(tok.pastIndex, pastIndex);

		return tok;
	}

	private Token makeStringSpanToken(
		TextIndex startIndex, TextIndex pastIndex) {
		
		Token tok;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_STRING, 
			Symbols.TOKEN_STRING_SPAN);

		utils.copyTextIndex(tok.startIndex, startIndex);
		utils.copyTextIndex(tok.pastIndex, pastIndex);
		
		return tok;
	}
	
	private Token makeBadSpanToken(
		TextIndex startIndex, TextIndex pastIndex) {
		
		Token tok;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_BASIC, 
			Symbols.TOKEN_BAD_SPAN);
		
		utils.copyTextIndex(tok.startIndex, startIndex);
		utils.copyTextIndex(tok.pastIndex, pastIndex);
		
		return tok;
	}
	
	private LangError makeBadSpanError(
		TextIndex startIndex, TextIndex pastIndex) {
		
		LangError e2;
		TextRange context;

		context = makeTextRange();
		utils.copyTextIndex(context.startIndex, startIndex);
		utils.copyTextIndex(context.pastIndex, pastIndex);

		e2 = new LangError();
		e2.id = LangErrors.ERROR_BAD_SPAN;
		e2.context = context;

		return e2;
	}
	
	private LangError makeSymbolUnexpectedError(
		Symbol givenSymbol, CommonArrayList expectedSymbols) {
		
		SymbolUnexpected e3;
		
		e3 = new SymbolUnexpected();
		e3.id = LangErrors.ERROR_SYMBOL_UNEXPECTED;
		e3.givenSymbol = givenSymbol;
		e3.expectedSymbols = expectedSymbols;
		return e3;
	}
		
	private void throwInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		throw e1;
	}
	
	private void updateState() {
		SimpleReaderNode xs;
		int size;
		
		size = dat.nodeStack.size();
		if(size == 0)
			throw new IllegalStateException();
		
		if(dat.nodeStackIndex >= size)
			throw new IllegalStateException();
		
		xs = (SimpleReaderNode) dat.nodeStack.get(dat.nodeStackIndex);
		
		if(xs.id == SimpleReaderNodeTypes.TYPE_TOKEN) {
			dat.state = SimpleReaderData.STATE_HAVE_SYMBOL;
			return;
		}
		
		if(xs.id == SimpleReaderNodeTypes.TYPE_GRAM) {
			dat.state = SimpleReaderData.STATE_HAVE_SYMBOL;
			return;
		}
		
		if(xs.id == SimpleReaderNodeTypes.TYPE_ERROR) {
			dat.state = BaseModuleData.STATE_STUCK;
			return;
		}
		
		throwInvalidEnum("ParserNodeSimple has a bad type");
	}

	private void setCcToTextIndex(int ccIndex, TextIndex ti) {
		CharReaderContext cc;
		
		cc = utils.getCharReaderContext(dat.ccStack, 0);
		utils.copyTextIndex(cc.ti, ti);
		cc.bi.versionNumber = ReadBuffer.VERSION_NUMBER_INVALID;
		
		return;
	}

	private void getCharReader2StartIndex(TextIndex ti, CharReaderAccess charRead) {
		CharReader2Data charDat = (CharReader2Data) charRead.getData();
		CharReaderParams prms = charDat.charReadParams;

		utils.getCharReaderStartIndex(ti, prms);
		return;
	}
		
	private SimpleReaderNode getNextStateRequireSymbol() {
		SimpleReaderNode n;
		
		n = dat.nextState;
		
		if(n == null) throw new NullPointerException();
		
		if(n.id != SimpleReaderNodeTypes.TYPE_GRAM
			&& n.id != SimpleReaderNodeTypes.TYPE_TOKEN)
			
			throw new IllegalStateException();

		dat.nextState = null;
		return n;
	}

	private SimpleReaderNode getNormalNodeRequireSymbol(int stackIndex) {
		SimpleReaderNode n;
		
		n = (SimpleReaderNode) dat.nodeStack.get(stackIndex);
		
		if(n.id != SimpleReaderNodeTypes.TYPE_TOKEN
			&& n.id != SimpleReaderNodeTypes.TYPE_GRAM)
			throw new IllegalStateException();
		
		if(n.decoration)
			throw new IllegalStateException();
		
		return n;
	}

	private SimpleReaderNode getNormalNodeRequireToken(int stackIndex) {
		SimpleReaderNode n;
		
		n = (SimpleReaderNode) dat.nodeStack.get(stackIndex);
		
		if(n.id != SimpleReaderNodeTypes.TYPE_TOKEN)
			throw new IllegalStateException();
		
		if(n.decoration)
			throw new IllegalStateException();
		
		return n;
	}
	
	private SimpleReaderNode getNormalNodeRequireGram(int stackIndex) {
		SimpleReaderNode n;
		
		n = (SimpleReaderNode) dat.nodeStack.get(stackIndex);
		
		if(n.id != SimpleReaderNodeTypes.TYPE_GRAM)
			throw new IllegalStateException();
		
		if(n.decoration)
			throw new IllegalStateException();

		return n;
	}
	
	private SimpleReaderNode getDecoration() {
		int stackIndex;
		SimpleReaderNode xs;
		
		stackIndex = dat.nodeStack.size();
		if(stackIndex == 0) return null;
		
		stackIndex -= 1;
		xs = (SimpleReaderNode) dat.nodeStack.get(stackIndex);
		
		if(xs.decoration) return xs;
		
		return null;
	}
	
	private SimpleReaderNode getDecorationRequireSymbol() {
		SimpleReaderNode n;
		
		n = getDecoration();
		if(n == null)
			throw new IllegalStateException();
		
		if(n.id != SimpleReaderNodeTypes.TYPE_TOKEN
			&& n.id != SimpleReaderNodeTypes.TYPE_GRAM)
			throw new IllegalStateException();
		
		return n;
	}

	private void setNode(int nodeStackIndex, SimpleReaderNode n) {
		removeStack(nodeStackIndex);
		dat.nodeStack.add(n);
	}
		
	void removeStack(int stackIndex) {
		int size;
		int lastIndex;
		
		size = dat.nodeStack.size();
		if(size == 0) return;
		lastIndex = size - 1;
		
		while(lastIndex >= stackIndex) {
			dat.nodeStack.removeAt(lastIndex);
			
			size = dat.nodeStack.size();
			if(size == 0) return;
			lastIndex = size - 1;
		}
		
		return;
	}
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private TextRange makeTextRange() {
		TextRange tr;
		
		tr = new TextRange();
		tr.init();
		return tr;
	}
	
	public SimpleReaderString makeSimpleReaderString(int idLen) {
		SimpleReaderString ss;
		
		ss = new SimpleReaderString();
		ss.symbolType = SymbolTypes.TYPE_GRAM;
		ss.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_SIMPLE_READER_STRING;

		ss.initAllTextIndex();
		
		utils.allocNewSymbolId(ss, idLen);
		return ss;
	}
	
	public Token makeToken(int idLen) {
		Token tok;
		
		tok = new Token();
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		tok.symbolStorageType = SymbolStorageTypes.TYPE_TOKEN;

		tok.initAllTextIndex();
		
		utils.allocNewSymbolId(tok, idLen);
		return tok;
	}
	
	private GramContainer makeGramContainer(int idLen, int childCount) {
		GramContainer g;
		
		g = new GramContainer();
		g.symbolType = SymbolTypes.TYPE_GRAM;
		g.symbolStorageType = SymbolStorageTypes.TYPE_GRAM_CONTAINER;
		
		g.initAllTextIndex();
		
		utils.allocNewSymbolId(g, idLen);
		utils.allocNewSymbolArrayForGramContainer(g, childCount);
		return g;
	}

	public SymbolId makeSymbolId(int idLen) {
		SymbolId symId = utils.makeSymbolId(idLen);
		return symId;
	}
}
