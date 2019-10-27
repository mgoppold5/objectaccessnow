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

public class CsvBaseReader
	implements BaseModule {
	
	public CsvBaseReaderData dat;
	public GeneralUtils utils;
	public CharReaderAccess charRead;
	public TokenChooser tokChoose;
	
	public XmlReferenceTokenHelper refHelp;

	public void initHelpers(GeneralUtils utils) {
		refHelp = new XmlReferenceTokenHelper();
		refHelp.dat = new XmlReferenceTokenHelperData();
		refHelp.dat.init();
		refHelp.utils = utils;
				
		return;
	}

	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, charRead);
		addExistingModule(o, tokChoose);
		addExistingModule(o, refHelp);

		return o;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		
		if(utils == null) return true;
		if(charRead == null) return true;
		if(tokChoose == null) return true;
		if(refHelp == null) return true;

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
		int cat;
		int id;
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
				
				cat = utils.getSymbolIdCategory(sym);
				id = utils.getSymbolIdPrimary(sym);

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
				case Symbols.TOKEN_COMMA:
					moveResult = moveFromComma(
						direction, dat.nodeStackIndex);
					handled = true;
					break;
				case Symbols.TOKEN_LINE_RETURN:
					moveResult = moveFromLineReturn(
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
	
	private int moveFromStart(int direction, int nodeStackIndex) {
		CharReaderContext cc;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		if(direction != ModuleMoveDirection.TO_NEXT)
			return ModuleMoveResult.INVALID;

		cc = utils.getCharReaderContext(dat.ccStack, 0);
		getCharReader2StartIndex(cc.ti, charRead);
		cc.bi.versionNumber = ReadBuffer.VERSION_NUMBER_INVALID;
		
		return moveToNextThing(nodeStackIndex);
	}
	
	private int moveFromSeparatorToken(int direction, int nodeStackIndex) {
		SimpleReaderNode n;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		if(direction != ModuleMoveDirection.TO_NEXT)
			return ModuleMoveResult.INVALID;
		
		n = getNormalNodeRequireSymbol(nodeStackIndex);
		
		setCcToTextIndex(0, n.sym.pastIndex);

		return moveToNextThing(nodeStackIndex);
	}
	
	private int moveFromComma(int direction, int nodeStackIndex) {
		return moveFromSeparatorToken(direction, nodeStackIndex);
	}

	private int moveFromLineReturn(int direction, int nodeStackIndex) {
		return moveFromSeparatorToken(direction, nodeStackIndex);
	}
	
	private int moveToNextThing(int nodeStackIndex) {
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode n3;
		SimpleReaderNode n4;
		TextIndex startIndex;
		int id;
		boolean inQuote;
		
		LangError e2;
		
		inQuote = false;
		
		readContent(0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n3 = getNextStateRequireSymbol();
		
		id = utils.getSymbolIdPrimary(n3.sym);

		if(id == Symbols.TOKEN_END_OF_STREAM
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM
			|| id == Symbols.TOKEN_LINE_RETURN
			|| id == Symbols.TOKEN_COMMA) {
			
			startIndex = n3.sym.startIndex;
			
			n4 = makeNodeFromGram(makeContentGram(
				startIndex, startIndex));
			
			setNode(nodeStackIndex, n4);
			n3.decoration = true;
			setNode(nodeStackIndex + 1, n3);
			
			n4.state = SimpleReaderNode.STATE_AT_END;
			
			if(nodeStackIndex == dat.nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}
		
		if(id == Symbols.TOKEN_STRING_SPAN
			|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC
			|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY
			|| id == Symbols.TOKEN_BAD_SPAN) {
			
			startIndex = n3.sym.startIndex;

			n4 = makeNodeFromGram(makeContentGram(
				startIndex, startIndex));
			
			setNode(nodeStackIndex, n4);
			setNode(nodeStackIndex + 1, n3);
			
			n4.state = SimpleReaderNode.STATE_FIRST_STEP;
			n4.miniState = SimpleReaderNode.STATE_AT_END;
			
			if(nodeStackIndex == dat.nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}

		e2 = makeSymbolUnexpectedError(
			n3.sym, makeExpectedSymbolsForContent(inQuote));
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}

	private int moveFromContent(int direction, int nodeStackIndex) {
		SimpleReaderString cont;
		Symbol sym;
		boolean inQuote;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode decor;
		boolean handled;
		int moveResult;
		int id;
		
		LangError e2;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		cont = (SimpleReaderString) n.sym;
		
		inQuote = false;
				
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

				handled = false;

				id = utils.getSymbolIdPrimary(sym);
				
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
				if(id == Symbols.TOKEN_COMMA
					|| id == Symbols.TOKEN_LINE_RETURN
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

			utils.copyTextIndex(
				cont.elementsPastIndex, decor.sym.startIndex);
			utils.copyTextIndex(
				cont.pastIndex, decor.sym.startIndex);
			
			setNode(nodeStackIndex + 1, decor);

			n.state = SimpleReaderNode.STATE_AT_END;
		}
		
		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT
		
		decor = getDecorationRequireSymbol();
		id = utils.getSymbolIdPrimary(decor.sym);
		
		if(id == Symbols.TOKEN_COMMA
			|| id == Symbols.TOKEN_LINE_RETURN
			|| id == Symbols.TOKEN_END_OF_STREAM
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM) {
			
			decor.decoration = false;
			setNode(nodeStackIndex, decor);
			if(nodeStackIndex == dat.nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}
		
		throw new IllegalStateException();
	}
	
	private int moveFromContentToken(int direction, int nodeStackIndex) {
		Token tok;
		Symbol sym;
		int id;
		boolean inQuote;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		CommonArrayList expectedSymbols;
		
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
		
		inQuote = false;
		
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
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM
			|| id == Symbols.TOKEN_LINE_RETURN
			|| id == Symbols.TOKEN_COMMA) {

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
				
		expectedSymbols = makeExpectedSymbolsForContent(inQuote);
		
		e2 = makeSymbolUnexpectedError(sym, expectedSymbols);
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int moveFromBadSpan(int direction, int nodeStackIndex) {
		return moveFromContentToken(direction, nodeStackIndex);
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
		addExpectedSymbolsForSeparators(es);
		
		if(inQuote) {
			is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_QUOTE);
			es.add(is);
		}
		
		return es;
	}
	
	private void addExpectedSymbolsForSeparators(CommonArrayList es) {
		SymbolId is;
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_PUNCTUATION, Symbols.TOKEN_COMMA);
		es.add(is);
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_WHITESPACE, Symbols.TOKEN_LINE_RETURN);
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
		
	private void readContent(int ccStartIndex,
		int ccUnusedIndex) {
	
		Token tok;
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
		inQuote = false;
		
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
		
		if(cc2.resultChar == 0xD
			|| cc2.resultChar == 0xA) {

			readNewLine(ccStartIndex, ccUnusedNewIndex);
			return;
		}
		
		if(cc2.resultChar == ',') {
			utils.copyCharReaderContext(cc3, cc2);
			utils.charReaderContextSkip(cc3);
			
			dat.nextState = makeNodeFromToken(makeCommaToken(
				cc2.ti, cc3.ti));
			return;
		}

		readSpan(ccStartIndex, ccUnusedNewIndex);
		return;
	}

	private void readSpan(int ccStartIndex,
		int ccUnusedIndex) {
		
		int c;
		
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
				
		utils.copyCharReaderContext(cc2, cc1);
		
		while(true) {
			readPastBadChars(cc2);

			if(cc2.state == BaseModuleData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc2.state == CharReaderData.STATE_END_OF_STREAM) {
				dat.nextState = makeNodeFromToken(
					makeStringSpanToken(cc1.ti, cc2.ti));
				return;
			}

			if(cc2.state != CharReaderData.STATE_HAVE_CHAR) {
				throwInvalidEnum("CharReader has an invalid state");
			}

			cc2.resultChar = charDat.resultChar;
			
			c = cc2.resultChar;
			
			// possible span char
						
			if(c == '&'
				|| c == 0xA || c == 0xD
				|| c == ',') {
				
				// our span is terminated
				
				dat.nextState = makeNodeFromToken(
					makeStringSpanToken(cc1.ti, cc2.ti));
				return;
			}
			
			// span char, skip it
			
			utils.charReaderContextSkip(cc2);
			continue;
		}
		
		// unreachable
	}
	
	private void readNewLine(int ccStartIndex,
		int ccUnusedIndex) {
	
		Token tok;
		boolean prevCharWas13;
		boolean isFirstChar;
		
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
				
		utils.copyCharReaderContext(cc3, cc2);
		
		isFirstChar = true;
		prevCharWas13 = false;
		
		// first char
		
		utils.copyCharReaderContext(cc2, cc1);
				
		while(true) {
			readPastBadChars(cc2);

			if(cc2.state == BaseModuleData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc2.state == CharReaderData.STATE_END_OF_STREAM) {
				if(isFirstChar)
					throw new IllegalStateException();
				
				// previous char was the newline char
				utils.textIndexSkipReturn(cc2.ti);

				dat.nextState = makeNodeFromToken(makeNewLineToken(
					cc1.ti, cc2.ti));
				return;
			}

			if(cc2.state != CharReaderData.STATE_HAVE_CHAR) {
				throwInvalidEnum("CharReader has an invalid state");
			}

			cc2.resultChar = charDat.resultChar;

			if(isFirstChar) {
				if(cc2.resultChar == 0xD) {
					isFirstChar = false;
					prevCharWas13 = true;
					
					utils.charReaderContextSkip(cc2);
					continue;
				}
				
				if(cc2.resultChar == 0xA) {
					utils.charReaderContextSkip(cc2);
					utils.textIndexSkipReturn(cc2.ti);
					
					dat.nextState = makeNodeFromToken(makeNewLineToken(
						cc1.ti, cc2.ti));
					return;
				}
				
				throw new IllegalStateException();
			}
			
			if(prevCharWas13) {
				if(cc2.resultChar == 0xA) {
					utils.charReaderContextSkip(cc2);
					utils.textIndexSkipReturn(cc2.ti);

					dat.nextState = makeNodeFromToken(makeNewLineToken(
						cc1.ti, cc2.ti));
					return;
				}

				// prev char was lone 13
				
				utils.textIndexSkipReturn(cc2.ti);

				dat.nextState = makeNodeFromToken(makeNewLineToken(
					cc1.ti, cc2.ti));
				return;
			}
			
			throw new IllegalStateException();
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

	private Token makeNewLineToken(
		TextIndex startIndex, TextIndex pastIndex) {
		
		Token tok;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_WHITESPACE,
			Symbols.TOKEN_LINE_RETURN);

		utils.copyTextIndex(tok.startIndex, startIndex);
		utils.copyTextIndex(tok.pastIndex, pastIndex);
		
		return tok;
	}
	
	private Token makeCommaToken(
		TextIndex startIndex, TextIndex pastIndex) {
		
		Token tok;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_PUNCTUATION,
			Symbols.TOKEN_COMMA);

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
	
	public SymbolId makeSymbolId(int idLen) {
		SymbolId symId = utils.makeSymbolId(idLen);
		return symId;
	}
}
