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

package unnamed.family.compiler;

import unnamed.common.*;

public class XmlBaseReader implements BaseModule {
	public XmlBaseReaderData dat;
	public GeneralUtils utils;
	public CharReaderAccess charRead;
	public TokenChooser tokChoose;
	
	public XmlCommentTokenHelper commentHelp;
	public XmlCDataTokenHelper cdataHelp;
	public XmlNameTokenHelper nameHelp;
	public XmlReferenceTokenHelper refHelp;
	
	public void initHelpers(GeneralUtils utils) {
		commentHelp = new XmlCommentTokenHelper();
		commentHelp.dat = new XmlCommentTokenHelperData();
		commentHelp.dat.init();
		commentHelp.utils = utils;
		
		cdataHelp = new XmlCDataTokenHelper();
		cdataHelp.dat = new XmlCDataTokenHelperData();
		cdataHelp.dat.init();
		cdataHelp.utils = utils;
		
		nameHelp = new XmlNameTokenHelper();
		nameHelp.dat = new XmlNameTokenHelperData();
		nameHelp.dat.init();
		nameHelp.utils = utils;
		
		refHelp = new XmlReferenceTokenHelper();
		refHelp.dat = new XmlReferenceTokenHelperData();
		refHelp.dat.init();
		refHelp.utils = utils;
	}
	
	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, charRead);
		addExistingModule(o, tokChoose);
		addExistingModule(o, commentHelp);
		addExistingModule(o, cdataHelp);
		addExistingModule(o, nameHelp);
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
		if(commentHelp == null) return true;
		if(cdataHelp == null) return true;
		if(nameHelp == null) return true;
		if(refHelp == null) return true;
		
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.probBag.reset();
		
		if(charRead != null) charRead.reset();
		
		dat.nodeStack.clear();
		dat.nodeStackIndex = 0;
		
		dat.nextState = null;
		
		dat.state = BaseModuleData.STATE_START;
	}
	
	public Gram getGram() {
		SimpleReaderNode n;
		
		if(dat.state != SimpleReaderData.STATE_HAVE_SYMBOL)
			throw new IllegalStateException();
		
		n = getNormalNodeRequireGram(dat.nodeStackIndex);
		
		return (Gram) n.sym;
	}
	
	public Token getToken() {
		SimpleReaderNode n;
		
		if(dat.state != SimpleReaderData.STATE_HAVE_SYMBOL)
			throw new IllegalStateException();
		
		n = getNormalNodeRequireToken(dat.nodeStackIndex);

		return (Token) n.sym;
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
				//System.out.println("move TOK dir=" + direction);
				
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
				if(cat == Symbols.TOKEN_CATEGORY_XML) {
					switch(id) {
					case Symbols.TOKEN_XML_NAME:
						moveResult = moveFromName(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					case Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY:
					case Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC:
						moveResult = moveFromContentToken(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					case Symbols.TOKEN_XML_COMMENT:
					case Symbols.TOKEN_XML_CDATA:
						moveResult = moveFromTextToken(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					case Symbols.TOKEN_XML_TAG_FINISH_WITH_END_MARK:
					case Symbols.TOKEN_XML_TAG_FINISH:
						moveResult = moveFromTextToken(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					}
				}
				
				if(!handled)
				if(id == Symbols.TOKEN_ASSIGN) {
					moveResult = moveFromEqual(
						direction, dat.nodeStackIndex);
					handled = true;
				}

				if(!handled)
				if(id == Symbols.TOKEN_STRING_SPAN) {
					//System.out.println("place1");
					moveResult = moveFromContentToken(
						direction, dat.nodeStackIndex);
					handled = true;
				}
				
				if(!handled)
				if(id == Symbols.TOKEN_BAD_SPAN) {
					moveResult = moveFromBadSpan(
						direction, dat.nodeStackIndex);
					handled = true;
				}
			
				if(!handled)
				if(cat == Symbols.GRAM_CATEGORY_XML) {
					switch(id) {
					case Symbols.GRAM_XML_CONTENT:
						moveResult = moveFromContent(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					case Symbols.GRAM_XML_TAG:
					case Symbols.GRAM_XML_TAG_WITH_END_MARK:
						moveResult = moveFromTag(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					case Symbols.GRAM_XML_ATTRIBUTE:
						moveResult = moveFromAttrib(
							direction, dat.nodeStackIndex);
						handled = true;
						break;
					}
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
				throwInvalidEnum("Invalid XML state");
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
		
	private int moveToNextText(int nodeStackIndex, int ccIndex,
		int ccUnusedIndex) {
		
		Symbol sym;
		int id;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		boolean inQuote;
		
		LangError e2;
		
		inQuote = false;
		
		readContent(inQuote, ccIndex, ccUnusedIndex);
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n = getNextStateRequireSymbol();
		sym = n.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.TOKEN_XML_COMMENT
			|| id == Symbols.TOKEN_XML_CDATA
			|| id == Symbols.TOKEN_END_OF_STREAM
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM
			|| id == Symbols.GRAM_XML_TAG
			|| id == Symbols.GRAM_XML_TAG_WITH_END_MARK) {
			
			n.lastKnownDirection = ModuleMoveDirection.TO_HERE;
			setNode(nodeStackIndex, n);
			if(dat.nodeStackIndex == nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}
		
		if(id == Symbols.TOKEN_STRING_SPAN
			|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC
			|| id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY
			|| id == Symbols.TOKEN_BAD_SPAN) {
			
			n2 = makeNodeFromGram(makeContentGram(
				sym.startIndex, sym.startIndex));
			n2.lastKnownDirection = ModuleMoveDirection.TO_HERE;
			setNode(nodeStackIndex, n2);
			if(dat.nodeStackIndex == nodeStackIndex)
				updateState();
			
			setNode(nodeStackIndex + 1, n);
			
			n2.state = SimpleReaderNode.STATE_FIRST_STEP;
			n2.miniState = SimpleReaderNode.STATE_AT_END;
			return ModuleMoveResult.SUCCESS;
		}

		e2 = makeSymbolUnexpectedError(
			sym, makeExpectedSymbolsForText());
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
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
		
		return moveToNextText(nodeStackIndex, 0, 1);
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
		
		inQuote = false;
		
		if(nodeStackIndex >= 1) {
			n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
			
			id = utils.getSymbolIdPrimary(n2.sym);
			if(id == Symbols.GRAM_XML_ATTRIBUTE)
				inQuote = true;
		}
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD) {
			if(n.state != SimpleReaderNode.STATE_FIRST_STEP) {
				n.state = SimpleReaderNode.STATE_FIRST_STEP;
				n.miniState = SimpleReaderNode.STATE_FIRST_STEP;
			}
		}
	
		if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			if(n.miniState == SimpleReaderNode.STATE_FIRST_STEP) {
				setCcToTextIndex(0, cont.elementsStartIndex);
				readContent(inQuote, 0, 1);

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
			
			setNode(nodeStackIndex + 1, decor);

			utils.copyTextIndex(cont.elementsPastIndex, n2.sym.pastIndex);
			utils.copyTextIndex(cont.pastIndex, n2.sym.pastIndex);
			
			id = utils.getSymbolIdPrimary(decor.sym);
			
			if(inQuote)
			if(id == Symbols.TOKEN_QUOTE) {
				utils.copyTextIndex(cont.pastIndex, decor.sym.pastIndex);
			}
			
			n.state = SimpleReaderNode.STATE_AT_END;
		}
		
		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT
		
		decor = getDecorationRequireSymbol();

		sym = decor.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(inQuote)
		if(id == Symbols.TOKEN_QUOTE)
			return ModuleMoveResult.AT_END;
		
		decor.decoration = false;
		setNode(nodeStackIndex, decor);
		decor.lastKnownDirection = ModuleMoveDirection.TO_HERE;
		if(dat.nodeStackIndex == nodeStackIndex)
			updateState();
		
		return ModuleMoveResult.SUCCESS;
	}
	
	private int moveFromContentToken(int direction, int nodeStackIndex) {
		Token tok;
		Symbol sym;
		int id;
		boolean inQuote;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		CharReaderContext cc;
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
		
		if(nodeStackIndex < 1)
			throw new IllegalStateException();

		n = getNormalNodeRequireGram(nodeStackIndex - 1);
		n.lastKnownDirection = direction;
		sym = (Gram) n.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id != Symbols.GRAM_XML_CONTENT)
			throw new IllegalStateException();
		
		inQuote = false;
		
		if(nodeStackIndex >= 2) {
			n = getNormalNodeRequireGram(nodeStackIndex - 2);
			sym = (Gram) n.sym;
			id = utils.getSymbolIdPrimary(sym);
			
			if(id == Symbols.GRAM_XML_ATTRIBUTE)
				inQuote = true;
		}
		
		n = getNormalNodeRequireToken(nodeStackIndex);
		tok = (Token) n.sym;
		
		setCcToTextIndex(0, tok.pastIndex);
		readContent(inQuote, 0, 1);
		
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
		
		if(!inQuote)
		if(id == Symbols.TOKEN_XML_COMMENT
			|| id == Symbols.TOKEN_XML_CDATA
			|| id == Symbols.GRAM_XML_TAG
			|| id == Symbols.GRAM_XML_TAG_WITH_END_MARK) {

			n2.decoration = true;
			setNode(nodeStackIndex + 1, n2);
			return ModuleMoveResult.AT_END;
		}
		
		expectedSymbols = null;
		if(inQuote) expectedSymbols = makeExpectedSymbolsForContent(inQuote);
		if(!inQuote) expectedSymbols = makeExpectedSymbolsForText();
		
		e2 = makeSymbolUnexpectedError(sym, expectedSymbols);
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}

	private int moveFromTextToken(int direction, int nodeStackIndex) {
		Symbol sym;
		SimpleReaderNode n;
		CharReaderContext cc;

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
		sym = n.sym;
		
		setCcToTextIndex(0, sym.pastIndex);
		
		return moveToNextText(nodeStackIndex, 0, 1);
	}
	
	int moveFromTag(int direction, int nodeStackIndex) {
		GramContainer tag;
		Token tok;
		Symbol sym;
		int id;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode decor;
		boolean handled;
		int moveResult;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		tag = (GramContainer) n.sym;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD
			|| n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			
			// get the Name for this tag
			tok = (Token) tag.sym[0];
			
			n2 = makeNodeFromToken(tok);
			
			setNode(nodeStackIndex + 1, n2);

			n.state = SimpleReaderNode.STATE_WALKING_CHILDREN;

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
			
			if(!handled)
			if(id == Symbols.TOKEN_XML_NAME) {
				moveResult = moveFromName(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
			
			if(!handled)
			if(id == Symbols.GRAM_XML_ATTRIBUTE) {
				moveResult = moveFromAttrib(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
			
			if(!handled)
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
			// move the decoration to the tag, and
			// set the tag's pastIndex to the
			// docoration's pastIndex.  The decoration
			// will be a TAG_WAS_START_TAG, or
			// TAG_WAS_EMPTY_TAG, or END_OF_STREAM, or
			// UNEXPECTED_END_OF_STREAM, so the
			// end of each of these is the end of the tag.

			decor = getDecorationRequireSymbol();

			sym = (Token) decor.sym;
			id = utils.getSymbolIdPrimary(sym);

			if(id != Symbols.TOKEN_XML_TAG_FINISH
				&& id != Symbols.TOKEN_XML_TAG_FINISH_WITH_END_MARK
				&& id != Symbols.TOKEN_END_OF_STREAM
				&& id != Symbols.TOKEN_UNEXPECTED_END_OF_STREAM)
				throw new IllegalStateException();
			
			setNode(nodeStackIndex + 1, decor);

			utils.copyTextIndex(tag.pastIndex, sym.pastIndex);
			n.state = SimpleReaderNode.STATE_AT_END;
		}
		
		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT
		
		decor = getDecorationRequireSymbol();
		
		// make the decoration the next state
		
		decor.decoration = false;
		setNode(nodeStackIndex, decor);
		decor.lastKnownDirection = ModuleMoveDirection.TO_HERE;
		if(dat.nodeStackIndex == nodeStackIndex)
			updateState();
		return ModuleMoveResult.SUCCESS;
	}
	
	private int attachNextRegularTagThing(
		int nodeStackIndex, TextIndex ti) {
		
		Symbol sym;
		int id;
		CharReaderContext cc;
		SimpleReaderNode n;
		boolean stopTag;

		LangError e2;
		
		setCcToTextIndex(0, ti);
		stopTag = false;
		readTag(stopTag, 0, 1);

		if(dat.state == BaseModuleData.STATE_STUCK)
			return ModuleMoveResult.STUCK;

		n = getNextStateRequireSymbol();
		sym = n.sym;
		id = utils.getSymbolIdPrimary(sym);

		if(id == Symbols.TOKEN_XML_TAG_FINISH
			|| id == Symbols.TOKEN_XML_TAG_FINISH_WITH_END_MARK
			|| id == Symbols.TOKEN_END_OF_STREAM
			|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM
			|| id == Symbols.TOKEN_BAD_SPAN
			|| id == Symbols.GRAM_XML_ATTRIBUTE) {

			n.decoration = true;
			setNode(nodeStackIndex + 1, n);
			return ModuleMoveResult.SUCCESS;
		}

		//System.out.println("id=" + id);
		//System.out.println("class" + sym.getClass().getName());
		
		e2 = makeSymbolUnexpectedError(
			sym, makeExpectedSymbolsForRegularTag());
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, e2);
		dat.stuckState = StuckStates.STATE_PERMANENT;
		dat.state = BaseModuleData.STATE_STUCK;
		return ModuleMoveResult.STUCK;
	}
	
	private int moveToNextTagThing(int nodeStackIndex) {
		SimpleReaderNode decor;
		Symbol sym;
		int id;
		
		decor = getDecorationRequireSymbol();

		sym = decor.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.TOKEN_BAD_SPAN
			|| id == Symbols.GRAM_XML_ATTRIBUTE) {

			decor.decoration = false;
			setNode(nodeStackIndex, decor);
			decor.lastKnownDirection = ModuleMoveDirection.TO_HERE;
			if(dat.nodeStackIndex == nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}
		
		return ModuleMoveResult.AT_END;
	}

	private int moveFromAttrib(int direction, int nodeStackIndex) {
		GramContainer att;
		Token nameTok;
		Symbol sym;
		int id;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		SimpleReaderNode decor;
		boolean handled;
		int moveResult;
		CharReaderContext cc;
		
		if(direction != ModuleMoveDirection.TO_FIRST_CHILD
			&& direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END) {
			
			return ModuleMoveResult.INVALID;
		}
		
		n = getNormalNodeRequireGram(nodeStackIndex);
		n.lastKnownDirection = direction;
		att = (GramContainer) n.sym;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD
			|| n.state == SimpleReaderNode.STATE_FIRST_STEP) {
			
			// get the Name for this tag
			
			// get the Name
			nameTok = (Token) att.sym[0];
			
			n2 = makeNodeFromToken(nameTok);
			
			setNode(nodeStackIndex + 1, n2);

			n.state = SimpleReaderNode.STATE_WALKING_CHILDREN;

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
			
			if(!handled)
			if(id == Symbols.TOKEN_XML_NAME) {
				moveResult = moveFromName(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}
			
			if(!handled)
			if(id == Symbols.TOKEN_ASSIGN) {
				moveResult = moveFromEqual(
					ModuleMoveDirection.TO_NEXT, nodeStackIndex + 1);
				handled = true;
			}

			if(!handled)
			if(id == Symbols.GRAM_XML_CONTENT) {
				moveResult = moveFromContent(
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
			// move the decoration to the tag, and
			// set the tag's pastIndex to the
			// docoration's pastIndex.  The decoration
			// will be a TAG_ENTERING_CONTENT, or
			// TAG_END_EMPTY, or END_OF_STREAM, or
			// UNEXPECTED_END_OF_STREAM, so the
			// end of each of these is the end of the tag.

			decor = getDecorationRequireSymbol();

			sym = decor.sym;
			id = utils.getSymbolIdPrimary(sym);

			if(id != Symbols.TOKEN_QUOTE)
				throw new IllegalStateException();

			utils.copyTextIndex(att.pastIndex, sym.pastIndex);

			moveResult = attachNextRegularTagThing(
				nodeStackIndex, att.pastIndex);
			if(moveResult != ModuleMoveResult.SUCCESS)
				return moveResult;

			n.state = SimpleReaderNode.STATE_AT_END;				
		}
		
		if(n.state != SimpleReaderNode.STATE_AT_END)
			throw new IllegalStateException();
		
		if(direction == ModuleMoveDirection.TO_END)
			return ModuleMoveResult.SUCCESS;
		
		// direction is TO_NEXT
		
		return moveToNextTagThing(nodeStackIndex);
	}
	
	int moveFromName(int direction, int nodeStackIndex) {
		GramContainer att;
		Token equalTok;
		Symbol sym;
		int id;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END)
			return ModuleMoveResult.INVALID;
		
		// direction is TO_NEXT or TO_END
		
		n = getNormalNodeRequireToken(nodeStackIndex);
		n.lastKnownDirection = direction;

		if(nodeStackIndex < 1)
			throw new IllegalStateException();

		n = getNormalNodeRequireGram(nodeStackIndex - 1);
		sym = n.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_XML_TAG
			|| id == Symbols.GRAM_XML_TAG_WITH_END_MARK)
			return moveFromTagToken(direction, nodeStackIndex);

		if(id == Symbols.GRAM_XML_ATTRIBUTE) {
			if(direction == ModuleMoveDirection.TO_END)
				return ModuleMoveResult.SUCCESS;

			// direction is TO_NEXT
			
			att = (GramContainer) sym;
			
			// get Equal token
			equalTok = (Token) att.sym[1];
			
			n2 = makeNodeFromToken(equalTok);
			
			setNode(nodeStackIndex, n2);
			if(dat.nodeStackIndex == nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}

		// unexpected stack state which contains Name
		throw new IllegalStateException();
	}

	int moveFromEqual(int direction, int nodeStackIndex) {
		GramContainer att;
		Gram contGrm;
		Symbol sym;
		int id;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;
		
		if(direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END)
			return ModuleMoveResult.INVALID;
		
		// direction is TO_NEXT or TO_END
		
		n = getNormalNodeRequireToken(nodeStackIndex);
		n.lastKnownDirection = direction;

		if(nodeStackIndex < 1)
			throw new IllegalStateException();

		n = getNormalNodeRequireGram(nodeStackIndex - 1);
		sym = n.sym;
		id = utils.getSymbolIdPrimary(sym);
		
		if(id == Symbols.GRAM_XML_ATTRIBUTE) {
			if(direction == ModuleMoveDirection.TO_END)
				return ModuleMoveResult.SUCCESS;

			// direction is TO_NEXT
			
			att = (GramContainer) sym;
			
			// get Content Gram
			contGrm = (Gram) att.sym[2];
			
			n2 = makeNodeFromGram(contGrm);
			
			setNode(nodeStackIndex, n2);
			if(dat.nodeStackIndex == nodeStackIndex)
				updateState();
			return ModuleMoveResult.SUCCESS;
		}

		// unexpected stack state which contains Name
		throw new IllegalStateException();
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
		
		if(id == Symbols.GRAM_XML_TAG
			|| id == Symbols.GRAM_XML_TAG_WITH_END_MARK)
			return moveFromTagToken(direction, nodeStackIndex);
		
		// unexpected stack state
		throw new IllegalStateException();
	}
	
	private int moveFromTagToken(int direction, int nodeStackIndex) {
		Token tok;
		Gram grm;
		int id;
		SimpleReaderNode n;
		SimpleReaderNode n2;
		int moveResult;
		
		if(direction == ModuleMoveDirection.TO_FIRST_CHILD)
			return ModuleMoveResult.AT_END;

		if(direction != ModuleMoveDirection.TO_NEXT
			&& direction != ModuleMoveDirection.TO_END)
			return ModuleMoveResult.INVALID;

		// direction is TO_NEXT or TO_END
				
		n = getNormalNodeRequireToken(nodeStackIndex);
		n.lastKnownDirection = direction;
		tok = (Token) n.sym;
		
		if(nodeStackIndex < 1)
			throw new IllegalStateException();
		
		n2 = getNormalNodeRequireGram(nodeStackIndex - 1);
		grm = (Gram) n2.sym;
		id = utils.getSymbolIdPrimary(grm);
		
		// handles both tag and stop tags
		if(id == Symbols.GRAM_XML_TAG
			|| id == Symbols.GRAM_XML_TAG_WITH_END_MARK) {
			
			if(n.state == SimpleReaderNode.STATE_FIRST_STEP) {
				moveResult = attachNextRegularTagThing(
					nodeStackIndex, tok.pastIndex);
				if(moveResult != ModuleMoveResult.SUCCESS)
					return moveResult;
				
				n.state = SimpleReaderNode.STATE_AT_END;				
			} 

			if(n.state != SimpleReaderNode.STATE_AT_END) {
				throwInvalidEnum("SimpleReaderNode state is bad");
			}
			
			if(direction == ModuleMoveDirection.TO_END)
				return ModuleMoveResult.SUCCESS;
			
			// direction is TO_NEXT
			
			return moveToNextTagThing(nodeStackIndex);
		}
				
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
	
	private CommonArrayList makeExpectedSymbolsForText() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForEndOfStream(es);
		addExpectedSymbolsForContentElements(es);
		addExpectedSymbolsForBadSpan(es);
		
		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_XML, Symbols.TOKEN_XML_COMMENT);
		es.add(is);

		is = makeIntegerSpecifier(
			Symbols.TOKEN_CATEGORY_XML, Symbols.TOKEN_XML_CDATA);
		es.add(is);

		is = makeIntegerSpecifier(
			Symbols.GRAM_CATEGORY_XML, Symbols.GRAM_XML_TAG);
		es.add(is);

		is = makeIntegerSpecifier(Symbols.GRAM_CATEGORY_XML, Symbols.GRAM_XML_TAG_WITH_END_MARK);
		es.add(is);

		return es;
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
	
	private CommonArrayList makeExpectedSymbolsForRegularTag() {
		CommonArrayList es;
		SymbolId is;
		
		es = makeArrayList();
		
		addExpectedSymbolsForEndOfStream(es);
		addExpectedSymbolsForBadSpan(es);
		
		is = makeIntegerSpecifier(
			Symbols.GRAM_CATEGORY_XML, Symbols.GRAM_XML_ATTRIBUTE);
		es.add(is);

		is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_XML, Symbols.TOKEN_XML_TAG_FINISH);
		es.add(is);

		is = makeIntegerSpecifier(Symbols.TOKEN_CATEGORY_XML, Symbols.TOKEN_XML_TAG_FINISH_WITH_END_MARK);
		es.add(is);
		
		return es;
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
	
	private void readTextLessThan(int ccStartIndex, int ccCurrentIndex,
		int ccUnusedIndex) {
		
		Token tok;
		TokenChooserData chooseDat;
	
		CharReader2Data charDat = (CharReader2Data) charRead.getData();
		
		CharReaderContext cc1;
		CharReaderContext cc2;
		CharReaderContext cc3;
		int cc3Index;
		CharReaderContext cc4;
		int cc4Index;
		CharReaderContext cc5;
		int cc5Index;
		
		cc1 = utils.getCharReaderContext(dat.ccStack, ccStartIndex);
		
		cc2 = utils.getCharReaderContext(dat.ccStack, ccCurrentIndex);

		cc3Index = ccUnusedIndex;
		cc3 = utils.getCharReaderContext(dat.ccStack, cc3Index);
		
		cc4Index = cc3Index + 1;
		cc4 = utils.getCharReaderContext(dat.ccStack, cc4Index);
		
		cc5Index = cc4Index + 1;
		cc5 = utils.getCharReaderContext(dat.ccStack, cc5Index);
		
		chooseDat = tokChoose.dat;
		
		if(cc2.state != CharReaderData.STATE_HAVE_CHAR)
			throw new IllegalStateException();
		
		if(cc2.resultChar != '<')
			throw new IllegalStateException();
		
		// first char, '<'
		
		tokChoose.reset();
		
		chooseDat.possibleHelpers.add(commentHelp);
		chooseDat.possibleHelpers.add(cdataHelp);
		
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
			tok = chooseDat.chosenHelper.getToken();
			
			dat.nextState = makeNodeFromToken(tok);
			return;
		}
		
		// resultCount = 0,
		// and so this is not a comment or cdata
		
		utils.copyCharReaderContext(cc3, cc2);
		
		// skip past the '<'
		utils.charReaderContextSkip(cc3);
		
		readPastBadChars(cc3);

		if(cc3.state == BaseModuleData.STATE_STUCK) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
			return;
		}

		if(cc3.state == CharReaderData.STATE_END_OF_STREAM) {
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeBadSpanError(cc1.ti, cc3.ti));
			
			dat.nextState = makeNodeFromToken(
				makeBadSpanToken(cc1.ti, cc3.ti));
			return;
		}
		
		if(cc3.state != CharReaderData.STATE_HAVE_CHAR) {
			throwInvalidEnum("CharReader has an invalid state");
		}
		
		cc3.resultChar = charDat.resultChar;
		
		if(cc3.resultChar == '/') {
			utils.copyCharReaderContext(cc4, cc3);
			
			// skip past the '/'
			utils.charReaderContextSkip(cc4);
			
			readPastBadChars(cc4);

			if(cc4.state == BaseModuleData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc4.state == CharReaderData.STATE_END_OF_STREAM) {
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeBadSpanError(cc1.ti, cc4.ti));
				
				dat.nextState = makeNodeFromToken(
					makeBadSpanToken(cc1.ti, cc4.ti));
				return;
			}

			if(cc4.state != CharReaderData.STATE_HAVE_CHAR) {
				throwInvalidEnum("CharReader has an invalid state");
			}

			cc4.resultChar = charDat.resultChar;

			// 3nd char, after '</'
			
			if(utils.isXmlNameStartChar(cc4.resultChar, true)) {
				tokChoose.reset();
				
				chooseDat.possibleHelpers.add(nameHelp);

				utils.copyCharReaderContext(cc5, cc4);

				tokChoose.chooseToken(cc5);

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
					
					// make a stop tag
					dat.nextState = makeNodeFromGram(
						makeTagGram(true, tok, cc1.ti));
					return;
				}
			}
			
			// have "</", but 
			// could not figure out what it was
			
			// make a bad span with the "<"

			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeBadSpanError(cc1.ti, cc3.ti));
			
			dat.nextState = makeNodeFromToken(
				makeBadSpanToken(cc1.ti, cc3.ti));
			return;
		}
		
		// have "<", and we are at the next char
		
		if(utils.isXmlNameStartChar(cc3.resultChar, true)) {
			tokChoose.reset();
			
			chooseDat.possibleHelpers.add(nameHelp);
			
			utils.copyCharReaderContext(cc4, cc3);
		
			tokChoose.chooseToken(cc4);
			
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

				// make a start/empty tag
				dat.nextState = makeNodeFromGram(
					makeTagGram(false, tok, cc1.ti));
				return;
			}
		}
		
		// have "<", and
		// could not figure out what it was
		// make a bad span with the "<"
		
		dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
			makeBadSpanError(cc1.ti, cc3.ti));

		dat.nextState = makeNodeFromToken(
			makeBadSpanToken(cc1.ti, cc3.ti));
		return;
	}
	
	private void readContent(boolean inQuote, int ccStartIndex,
		int ccUnusedIndex) {
	
		Token tok;
		boolean prevCharWas13;
		TokenChooserData chooseDat;

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
			
			readTextLessThan(ccStartIndex, cc2Index, ccUnusedNewIndex);
			return;
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

	private void readTag(boolean stopTag,
		int ccStartIndex, int ccUnusedIndex) {
		
		CharReader2Data charDat = (CharReader2Data) charRead.getData();
				
		// this function is a little complicated, because it
		// handles a lot of conditions
		
		// it handles stuff withing a stop tag.  If there's
		// anything unexpected, it accumulates in a bad span.
		// Attribute stuff in a stop tag would count as bad span stuff.
		
		// For an attribute in start/empty tags, if things go well,
		// an attribute is read until after the beginning quote,
		// and an attribute is returned.

		// If unexpected stuff is encountered, the bad boolean
		// is set, and the function gathers as much of
		// a bad span as possible until a terminator.
		
		// terminators of a bad span include, a NameStartChar
		// in a regular tag (which could be for a good
		// attribute), a '/' in a regular tag
		// (which could be for an empty tag terminator),
		// or a '>' in any tag
		
		TokenChooserData chooseDat;
		boolean prevCharWas13;
		boolean prevCharWasWhitespace;
		boolean terminate;
		
		boolean bad; // stuff has become a bad span
		boolean badInQuote; // reading bad span stuff within 2 quotes

		boolean attrib; // true after creating/reading a Name token
		Token nameTok;
		Token equalTok;
		boolean attribPastEqual;
		
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
		
		// initial conditions
		bad = false;
		badInQuote = false;
		attrib = false;
		attribPastEqual = false;
		prevCharWas13 = false;
		prevCharWasWhitespace = false;
		nameTok = null;
		equalTok = null;
		
		// I chose to handle stop tags just like other tags
		stopTag = false;
		
		utils.copyCharReaderContext(cc2, cc1);
		
		while(true) {
			readPastBadChars(cc2);

			if(cc2.state == BaseModuleData.STATE_STUCK) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc2.state == CharReaderData.STATE_END_OF_STREAM) {
				if(prevCharWas13) {
					// previous char was a lone 0xD
					utils.textIndexSkipReturn(cc2.ti);
					prevCharWas13 = false;
				}
				
				if(attrib)
					// an unfinished attribute is a bad span
					bad = true;
				
				if(bad) {
					// it was a bad span
					
					dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
						makeBadSpanError(cc1.ti, cc2.ti));

					dat.nextState = makeNodeFromToken(
						makeBadSpanToken(cc1.ti, cc2.ti));
					return;
				}
				
				// make unexpected end of stream
				dat.nextState = makeNodeFromToken(
					makeEndOfStreamToken(true, cc2.ti));
				return;
			}

			if(cc2.state != CharReaderData.STATE_HAVE_CHAR) {
				throwInvalidEnum("CharReader has an invalid state");
			}

			cc2.resultChar = charDat.resultChar;

			if(!stopTag) {
				if(!bad && !attrib) {
					if(cc2.resultChar == '/') {
						// this could be a "/>" to terminate the tag

						if(prevCharWas13) {
							// previous char was a lone 0xD
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						utils.copyCharReaderContext(cc3, cc2);

						// skip past the '/'
						utils.charReaderContextSkip(cc3);

						readPastBadChars(cc3);

						if(cc3.state == BaseModuleData.STATE_STUCK) {
							dat.state = BaseModuleData.STATE_STUCK;
							dat.stuckState =
								StuckStates.STATE_DUE_TO_DESCENDANTS;
							return;
						}

						if(cc3.state == CharReaderData.STATE_END_OF_STREAM) {
							dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
								makeBadSpanError(cc1.ti, cc3.ti));

							dat.nextState = makeNodeFromToken(
								makeBadSpanToken(cc1.ti, cc3.ti));
							return;
						}

						if(cc3.state != CharReaderData.STATE_HAVE_CHAR) {
							throwInvalidEnum(
								"CharReader has an invalid state");
						}

						cc3.resultChar = charDat.resultChar;

						if(cc3.resultChar == '>') {
							utils.charReaderContextSkip(cc3);

							// make an empty tag token
							dat.nextState = makeNodeFromToken(
								makeTagKindToken(
									false, cc2.ti, cc3.ti));
							return;
						}
						
						// was a lone '/', start a bad span with the '/'
						bad = true;
						
						// resume after the '/'
						utils.copyCharReaderContext(cc2, cc3);
						prevCharWasWhitespace = false;
						continue;
					}
					
					if(cc2.resultChar == '>') {
						// this ends the tag, which is a start tag
						
						if(prevCharWas13) {
							// previous char was a lone 0xD
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						utils.copyCharReaderContext(cc3, cc2);

						// skip past the '>'
						utils.charReaderContextSkip(cc3);

						// make a start tag token
						dat.nextState = makeNodeFromToken(
							makeTagKindToken(
								true, cc2.ti, cc3.ti));
						return;
					}

					if(utils.isXmlNameStartChar(cc2.resultChar, true)) {
						// attribute start

						if(prevCharWas13) {
							// previous char was a lone 0xD
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						tokChoose.reset();

						chooseDat.possibleHelpers.add(nameHelp);

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
							nameTok = chooseDat.chosenHelper.getToken();

							utils.copyTextIndex(cc2.ti, nameTok.pastIndex);
							cc2.bi.versionNumber =
								ReadBuffer.VERSION_NUMBER_INVALID;

							attrib = true;
							prevCharWasWhitespace = false;
							continue;
						}

						// resultCount is zero, which should not
						// happen here
						throw new IllegalStateException();
					}
				} // !bad && !attrib
				
				if(attrib) {
					if(!attribPastEqual)
					if(cc2.resultChar == '=') {
						if(prevCharWas13) {
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						utils.copyCharReaderContext(cc3, cc2);

						// skip past the "="
						utils.charReaderContextSkip(cc3);

						equalTok = makeAssignToken(cc2.ti, cc3.ti);
						
						utils.copyCharReaderContext(cc2, cc3);

						attribPastEqual = true;
						prevCharWasWhitespace = false;
						continue;
					}

					if(attribPastEqual)
					if(cc2.resultChar == '"') {
						// this completes our attribute
						
						if(prevCharWas13) {
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						utils.copyCharReaderContext(cc3, cc2);

						// skip past the '"', to the start of the content
						utils.charReaderContextSkip(cc3);
						
						dat.nextState = makeNodeFromGram(
							makeAttribGram(
								nameTok, equalTok, cc2.ti, cc3.ti));
						return;
					}
				} // attrib
				
				if(bad) {
					terminate = false;
					
					if(!terminate && !badInQuote)
					if(cc2.resultChar == '/' || cc2.resultChar == '>')
						terminate = true;
					
					if(!terminate && !badInQuote && prevCharWasWhitespace)
					if(utils.isXmlNameStartChar(cc2.resultChar, true))
						// this char could be the start of a good attribute
						terminate = true;
					
					if(terminate) {
						// this terminates the bad span

						if(prevCharWas13) {
							// previous char was a lone 0xD
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
							makeBadSpanError(cc1.ti, cc2.ti));

						dat.nextState = makeNodeFromToken(
							makeBadSpanToken(cc1.ti, cc2.ti));
						return;
					}
				} // bad
			} // !stopTag
				
			if(stopTag) {
				if(!bad) {
					if(cc2.resultChar == '>') {
						// this concludes the stop tag
						
						if(prevCharWas13) {
							// previous char was a lone 0xD
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						utils.copyCharReaderContext(cc3, cc2);

						// skip past the '>'
						utils.charReaderContextSkip(cc3);

						dat.nextState = makeNodeFromToken(
							makeTagKindToken(
								true, cc2.ti, cc3.ti));
						return;
					}
				} // !bad
				
				if(bad) {
					if(!badInQuote)
					if(cc2.resultChar == '>') {
						// this concludes the bad span

						if(prevCharWas13) {
							// previous char was a lone 0xD
							utils.textIndexSkipReturn(cc2.ti);
							prevCharWas13 = false;
						}

						dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
							makeBadSpanError(cc1.ti, cc2.ti));

						dat.nextState = makeNodeFromToken(
							makeBadSpanToken(cc1.ti, cc2.ti));
						return;
					}
				} // bad
			} // stopTag
			
			
			if(!bad)
			if(!utils.isWhitespaceChar(cc2.resultChar)) {
				// not whitespace, and not handled, so it
				// is an unexpected char

				// transition to bad, re-read char
				
				bad = true;
				attrib = false;
				continue;
			}
			
			// whitespace handling
			
			if(prevCharWas13) {
				if(cc2.resultChar == 0xA) {
					// skip a the 0xA in an 0xD,0xA combo
					
					utils.charReaderContextSkip(cc2);
					utils.textIndexSkipReturn(cc2.ti);
					prevCharWas13 = false;
					prevCharWasWhitespace = true;
					continue;
				}
				
				// previous char was a lone 0xD
				utils.textIndexSkipReturn(cc2.ti);
				prevCharWas13 = false;
			}
			
			if(cc2.resultChar == 0xA) {
				utils.charReaderContextSkip(cc2);
				utils.textIndexSkipReturn(cc2.ti);
				prevCharWasWhitespace = true;
				continue;
			}
			
			if(cc2.resultChar == 0xD) {
				prevCharWas13 = true;
				utils.charReaderContextSkip(cc2);
				prevCharWasWhitespace = true;
				continue;
			}
			
			if(cc2.resultChar == ' ' || cc2.resultChar == 0x9) {
				utils.charReaderContextSkip(cc2);
				prevCharWasWhitespace = true;
				continue;
			}			
			
			//if(cc2.resultChar == '"') badInQuote = !badInQuote;

			// skip past the char
			utils.charReaderContextSkip(cc2);
			prevCharWasWhitespace = false;
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
	
	private Token makeAssignToken(TextIndex startIndex, TextIndex pastIndex) {
		Token tok;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_MATH_AND_LOGIC,
			Symbols.TOKEN_ASSIGN);

		utils.copyTextIndex(tok.startIndex, startIndex);
		utils.copyTextIndex(tok.pastIndex, pastIndex);

		return tok;
	}
	
	private Gram makeAttribGram(Token nameTok, Token equalTok,
		TextIndex quoteStartIndex, TextIndex contentStartIndex) {
		
		Gram strGrm;
		GramContainer grmCon;
		
		strGrm = makeContentGram(quoteStartIndex, contentStartIndex);

		grmCon = makeGramContainer(2, 3);
		//grmCon.sym = new Symbol[3];
		
		grmCon.symbolType = SymbolTypes.TYPE_GRAM;
		utils.setSymbolIdLen2(grmCon, Symbols.GRAM_CATEGORY_XML,
			Symbols.GRAM_XML_ATTRIBUTE);

		utils.copyTextIndex(
			grmCon.startIndex, nameTok.startIndex);
		utils.copyTextIndex(
			grmCon.pastIndex, nameTok.startIndex);

		grmCon.sym[0] = nameTok;
		grmCon.sym[1] = equalTok;
		grmCon.sym[2] = strGrm;
		
		return grmCon;
	}
	
	private Token makeTagKindToken(boolean containsContent,
		TextIndex startIndex, TextIndex pastIndex) {
		
		Token tok;
		int id;
		
		tok = makeToken(2);
		tok.symbolType = SymbolTypes.TYPE_TOKEN;
		
		id = Symbols.TOKEN_XML_TAG_FINISH_WITH_END_MARK;
		if(containsContent)
			id = Symbols.TOKEN_XML_TAG_FINISH;
		
		utils.setSymbolIdLen2(tok, Symbols.TOKEN_CATEGORY_XML, id);

		utils.copyTextIndex(tok.startIndex, startIndex);
		utils.copyTextIndex(tok.pastIndex, pastIndex);

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
	
	private Gram makeTagGram(
		boolean stopTag, Token nameTok, TextIndex startIndex) {

		GramContainer grmCon;
		int id;
		
		grmCon = makeGramContainer(2, 1);
		//grmCon.sym = new Symbol[1];
		
		grmCon.symbolType = SymbolTypes.TYPE_GRAM;
	
		id = Symbols.GRAM_XML_TAG;
		if(stopTag)
			id = Symbols.GRAM_XML_TAG_WITH_END_MARK;
		
		utils.setSymbolIdLen2(grmCon, Symbols.GRAM_CATEGORY_XML, id);

		utils.copyTextIndex(grmCon.startIndex, startIndex);
		utils.copyTextIndex(grmCon.pastIndex, startIndex);

		grmCon.sym[0] = nameTok;

		return grmCon;
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
