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

import java.io.PrintStream;

import unnamed.common.*;
import unnamed.file.system.*;
import unnamed.family.compiler.*;

public class ArithParserTest1 {
	public ArithParserTest1Data dat;
	public PrintStream out;
	
	public void initOnce() {
		// general
		//
		
		dat.directory = makeArrayList();
		
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
		
		
		// for grammar file
		//
		
		dat.k = 1;
				
		dat.gdef = new GrammarReaderData();
		dat.gdef.init();
		
		dat.gdefRead = new GrammarReader();
		dat.gdefRead.dat = dat.gdef;
		dat.gdefRead.utils = dat.utils;
		dat.gdefRead.linkUtils = dat.linkUtils;
		dat.gdefRead.tokUtils = dat.tokUtils;
		
		dat.lrkDat = new LrkCanonData();
		dat.lrkDat.init();
		dat.lrkDat.grmrDef = dat.gdef;
		dat.lrkDat.k = dat.k;
		dat.lrkDat.keepClosures = true;
		
		dat.lrkCalc = new LrkCanon();
		dat.lrkCalc.dat = dat.lrkDat;
		dat.lrkCalc.utils = dat.utils;
		dat.lrkCalc.lrUtils = dat.lrUtils;

		
		// for data file
		//
		
		dat.tokChoose = new TokenChooser();
		dat.tokChoose.dat = new TokenChooserData();
		dat.tokChoose.dat.init();
		//dat.tokChoose.charRead = dat.charRead;
		dat.tokChoose.utils = dat.utils;

		dat.tokenRead = new CFamilySimpleTokenReader2();
		dat.tokenRead.dat = new CFamilySimpleTokenReader2Data();
		dat.tokenRead.dat.init();
		dat.tokenRead.utils = dat.utils;
		//dat.tokenRead.charRead = dat.charRead;
		dat.tokenRead.tokChoose = dat.tokChoose;
		dat.tokenRead.initHelpers(dat.utils, dat.tokUtils);
		
		dat.gramRead = new LrGramReader();
		dat.gramRead.dat = new LrGramReaderData();
		dat.gramRead.dat.init();
		dat.gramRead.dat.lrkDat = dat.lrkDat;
		dat.gramRead.utils = dat.utils;
		dat.gramRead.lrUtils = dat.lrUtils;
		dat.gramRead.grmrUtils = dat.grmrUtils;
		dat.gramRead.tokRead = dat.tokenRead;
		
		dat.intEval = new TokenIntegerEval();
		dat.intEval.dat = new TokenIntegerEvalData();
		dat.intEval.dat.init();
		dat.intEval.utils = dat.utils;
		//dat.intEval.charRead = dat.charRead;
	}
	
	public void setGrammarDir(CommonInt32Array path) {
		FileNode2 fpNode;
		
		fpNode = getFilePathNode(path);
		if(fpNode == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(fpNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(fpNode, null, '/')
			!= FileTypes.FILE_TYPE_DIRECTORY)
			throw makeObjectUnexpected(null);
		
		dat.grammarDirNode = fpNode;
		return;
	}
	
	public void initGrammarFile(CommonInt32Array grammarFilePath) {		
		FileNode2 dirNode;
		FileNode2 fileNode;

		dirNode = (FileNode2) dat.grammarDirNode;
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(dirNode, grammarFilePath, '/')
			!= FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectUnexpected(null);
		
		fileNode = FileNode2Utils.createFilePathNode(
			dirNode, grammarFilePath);
		
		dat.grammarFileNode = fileNode;
		
		dat.gdef.searchableDirContextList =
			makeListWithSingleObject(dat.grammarDirNode);
		dat.gdef.grammarFilePath = CommonIntArrayUtils.copy32(
			(CommonInt32Array) fileNode.sortObject);
		
		dat.gdefRead.reset();
		dat.lrkCalc.reset();
	}
	
	public boolean loadGrammarFile() {
		int moveResult;
		boolean isError;
		
		out.println(indentString() + "Grammar File: " +
			StringUtils.javaStringFromInt32String(
				(CommonInt32Array) dat.grammarFileNode.sortObject));
		
		dat.indentCount += 1; // now in File
		
		isError = false;
		
		if(!isError)
		while(true) {
			if(dat.gdef.state
				== GrammarReaderData.STATE_HAVE_GRAMMAR)
				break;
			
			moveResult = dat.gdefRead.move(ModuleMoveDirection.TO_NEXT);

			if(moveResult != ModuleMoveResult.SUCCESS) {
				out.println(indentString()
					+ "Bad move result: moveResult="
					+ moveResult);
				isError = true;
				break;
			}
		}
				
		if(!isError)
		while(true) {
			if(dat.lrkDat.state
				== LrkData.STATE_HAVE_MACHINE)
				break;
			
			moveResult = dat.lrkCalc.move(ModuleMoveDirection.TO_NEXT);

			if(moveResult != ModuleMoveResult.SUCCESS) {
				out.println(indentString()
					+ "Bad move result: moveResult="
					+ moveResult);
				isError = true;
				break;
			}
		}

		dat.spectrumStack = null;
		
		if(!isError) {
			dat.spectrumStack = dat.lrUtils
				.makePrecedenceSpectrumStackSimple(
					dat.lrkCalc.dat.names);
		}
		
		// check modules

		if(isError) {
			out.println(indentString() + "Outputing module info");

			dat.indentCount += 1; // now in module info

			checkModule(dat.gdefRead.getData(), "GrammarReader");

			int srcNum;
			int srcCount;
			
			srcNum = 0;
			srcCount = dat.gdef.sourceStack.size();
			// for debugging
			while(srcNum < srcCount) {
				GrammarSource grmr = (GrammarSource)
					dat.gdef.sourceStack.get(srcNum);
				GrammarSourceData grmrDat = (GrammarSourceData) grmr.getData();

				checkModule(grmrDat, "GrammarDefGrammarSource");
				checkModule(grmr.charRead.getData(), "CharReader");
				checkModule(grmr.intEval.dat, "intEval");
				checkModule(grmr.keywordHelp.dat, "keywordHelp");
				checkModule(grmr.strEval.dat, "strEval");
				checkModule(grmr.tokChoose.dat, "tokChoose");
				checkModule(grmr.tokenRead.dat, "tokenRead");
				
				srcNum += 1;
			}				

			checkModule(dat.lrkCalc.getData(), "LrkCanon");

			dat.indentCount -= 1; // now in file
		}
		
		dat.indentCount -= 1; // now outside
		
		return isError;
	}
	
	public void setTestDir(CommonInt32Array path) {
		FileNode2 fpNode;
		
		fpNode = getFilePathNode(path);
		if(fpNode == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getInnerFileSystem(fpNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(fpNode, null, '/')
			!= FileTypes.FILE_TYPE_DIRECTORY)
			throw makeObjectUnexpected(null);
		
		dat.testDirNode = fpNode;
		return;
	}

	public void initTestFile(CommonInt32Array filePath) {
		FileNode2 dirNode;
		FileNode2 fileNode;

		dirNode = (FileNode2) dat.testDirNode;
		if(dirNode == null)
			throw makeObjectNotFound(null);

		if(FileNode2Utils.getInnerFileSystem(dirNode) == null)
			throw makeObjectNotFound(null);
		
		if(FileNode2Utils.getFileType(dirNode, filePath, '/')
			!= FileTypes.FILE_TYPE_NORMAL_FILE)
			throw makeObjectUnexpected(null);
		
		fileNode = FileNode2Utils.createFilePathNode(dirNode, filePath);
		
		dat.testFileNode = fileNode;
		
		dat.fileDat = FileNode2Utils.openNormalFile(
			fileNode, (short) AccessRights.ACCESS_READ, '/');
		
		dat.linkUtils.initFileContext(fileNode, dat.fileDat);
		dat.charRead = dat.linkUtils.createCharReader3FromFileContext(
			fileNode, dat.fileDat, dat.utils);
		dat.strRead = dat.linkUtils.createStringReader3FromFileContext(
			fileNode, dat.fileDat, dat.utils);

		dat.strRead.reset();
		dat.charRead.reset();
		
		dat.tokenRead.charRead = dat.charRead;
		dat.tokenRead.reset();
				
		dat.tokChoose.charRead = dat.charRead;

		dat.intEval.strRead = dat.strRead;
		
		dat.gramRead.dat.precedenceSpectrumStack = dat.spectrumStack;
		dat.gramRead.reset();
	}
	
	public void cleanupTestFile() {
		dat.charRead = null;
		dat.strRead = null;
		
		dat.tokChoose.charRead = null;
		dat.tokChoose.reset();
		
		dat.tokenRead.charRead = null;
		dat.tokenRead.reset();
		
		dat.intEval.strRead = null;
				
		FileNode2Utils.closeNormalFile(
			(FileNode2) dat.testFileNode, dat.fileDat);
		dat.testFileNode = null;
		dat.fileDat = null;
		return;
	}
	
	private boolean runTestFile() {
		boolean isError;
		int moveResult;
		int state;
		Symbol sym;
		GramContainer grmCon;
		LrGramReaderData grmDat;
		
		out.println(indentString()
			+ "File: " + StringUtils.javaStringFromInt32String(
				(CommonInt32Array) dat.testFileNode.sortObject)
			+ " with Grammar File: "
				+ StringUtils.javaStringFromInt32String(
					(CommonInt32Array) dat.grammarFileNode.sortObject));

		isError = false;
		dat.indentCount += 1; // now in File
		
		grmDat = (LrGramReaderData) dat.gramRead.getData();
		
		if(!isError)
		while(true) {
			state = grmDat.state;
			
			if(state == LrGramReaderData.STATE_DONE)
				break;
			
			if(state == LrGramReaderData.STATE_HAVE_CONFLICT) {
				out.println(indentString()
					+ "HAVE CONFLICT");
				isError = true;
			}
			
			if(state == LrGramReaderData.STATE_HAVE_REDUCED_GRAM) {
				sym = dat.gramRead.getCurrentSymbol();
				
				if(dat.utils.getIdLenFromSymbol(sym) >= 2)
				if(dat.utils.getSymbolIdPrimary(sym)
					== Symbols.GRAM_STATEMENT_EXPRESSION) {
					
					grmCon = (GramContainer) sym;
					
					evalExpression((Gram) grmCon.sym[0]);
				}
			}
			
			moveResult = dat.gramRead.move(ModuleMoveDirection.TO_NEXT);
			
			if(moveResult == ModuleMoveResult.AT_END)
				break;
			
			if(moveResult != ModuleMoveResult.SUCCESS) {
				out.println(indentString()
					+ "Bad move result: moveResult="
					+ moveResult);
				isError = true;
				break;
			}
		}
		
		if(isError) {
			out.println(indentString() + "Outputing module info");

			dat.indentCount += 1; // now in module info

			checkModule(dat.gramRead.getData(), "GramReader");
			checkModule(dat.tokenRead.getData(), "TokenReader");

			dat.indentCount -= 1; // now in file
		}
		
		dat.indentCount -= 1; // now outside
		
		return isError;
	}
	
	private void evalExpression(Gram grm) {
		out.println(indentString()
			+ "expression " + getSymbolString(grm));

		dat.indentCount += 1;

		out.println(indentString()
			+ "arithmetic result = " + expressionToInteger(grm));

		out.println(indentString() + "object tree");
		dat.indentCount += 1;
		dumpGram(grm);
		dat.indentCount -= 1;
		
		dat.indentCount -= 1;

		return;
	}
	
	private int expressionToInteger(Symbol sym) {
		int id;
		int id2;
		GramContainer grmCon;
		Symbol sym2;
		
		id = dat.utils.getSymbolIdPrimary(sym);
		
		
		if(id == Symbols.TOKEN_INTEGER_FULL) {
			dat.intEval.dat.fullIntTok = (TokenIntegerFull) sym;
			
			dat.intEval.readInteger32FromFull();
			
			if(dat.intEval.dat.state
				!= TokenIntegerEvalData.STATE_HAVE_INT_32)
				throw makeInvalidEnum(null);
			
			return dat.intEval.dat.resultInt32;
		}
		
		if(id == Symbols.GRAM_EXPRESSION_SIMPLE) {
			grmCon = (GramContainer) sym;
			
			return expressionToInteger(grmCon.sym[0]);
		}

		if(id == Symbols.GRAM_EXPRESSION_PAREN) {
			grmCon = (GramContainer) sym;
			
			return expressionToInteger(grmCon.sym[1]);
		}
		
		if(id == Symbols.GRAM_EXPRESSION_BINARY) {
			grmCon = (GramContainer) sym;
			
			sym2 = grmCon.sym[1];
			if(sym == null) throw makeNullException(null);
			id2 = dat.utils.getSymbolIdPrimary(sym2);
			
			if(id2 == Symbols.TOKEN_PLUS)
				return expressionToInteger(grmCon.sym[0])
					+ expressionToInteger(grmCon.sym[2]);
			
			if(id2 == Symbols.TOKEN_MINUS)
				return expressionToInteger(grmCon.sym[0])
					- expressionToInteger(grmCon.sym[2]);
			
			if(id2 == Symbols.TOKEN_TIMES)
				return expressionToInteger(grmCon.sym[0])
					* expressionToInteger(grmCon.sym[2]);
			
			if(id2 == Symbols.TOKEN_DIVIDE)
				return expressionToInteger(grmCon.sym[0])
					/ expressionToInteger(grmCon.sym[2]);
		}

		if(id == Symbols.GRAM_EXPRESSION_UNARY_PREFIX) {
			grmCon = (GramContainer) sym;
			
			sym2 = grmCon.sym[0];
			if(sym == null) throw makeNullException(null);
			id2 = dat.utils.getSymbolIdPrimary(sym2);
			
			if(id2 == Symbols.TOKEN_MINUS)
				return 0 - expressionToInteger(grmCon.sym[1]);
		}
		
		throw makeObjectUnexpected(null);
	} 
	
	private String getSymbolString(Symbol sym) {
		long stringLen = 0;
		String s = null;
		
		CommonError e1;
		
		StringReader2Data strDat = (StringReader2Data) dat.strRead.getData();
		
		if(sym.pastIndex.index > sym.startIndex.index)
			stringLen = sym.pastIndex.index - sym.startIndex.index;
		
		if(stringLen > 1000) return "STRING_TOO_LONG";

		dat.strRead.readJavaStringThrow(sym.startIndex, stringLen);

		if(strDat.state != 
			CharReaderData.STATE_HAVE_JAVA_STRING_BUFFER) {
			
			e1 = new CommonError();
			e1.id = CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}
		
		s = strDat.resultJavaStringBuilder.toString();
		
		if(false) {
			s = getIndexString(sym.startIndex)
				+ " " + s + " "
				+ getIndexString(sym.pastIndex);
		}
		
		return "(" + s + ")";
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
		String indentStr;
		String tokenName;
		int id;
		int cat;
		
		indentStr = indentString();
		
		id = dat.utils.getSymbolIdPrimary(tok);
		cat = dat.utils.getSymbolIdCategory(tok);
		
		if(id == Symbols.TOKEN_END_OF_STREAM) {
			out.println(indentStr + "TOKEN_END_OF_STREAM");
			return;
		}
		
		if(cat == Symbols.TOKEN_CATEGORY_NUMBER) {
			if(id == Symbols.TOKEN_INTEGER_SIMPLE) {
				tokIntSimple = (TokenIntegerSimple) tok;

				out.println(indentStr
					+ "TOKEN_INTEGER_SIMPLE "
					+ getSymbolString(tokIntSimple));

				dat.indentCount += 1;

				out.println(indentString()
					+ "RADIX (" + tokIntSimple.radix + ")");

				dat.indentCount -= 1;
				return;
			}

			if(id == Symbols.TOKEN_INTEGER_FULL) {
				tInt = (TokenIntegerFull) tok;

				out.println(indentStr + "TOKEN_INTEGER_FULL "
					+ getSymbolString(tok));

				dat.indentCount += 1;
				indentStr = indentString();

				if(tInt.integer != null) {
					out.println(indentStr + "INTEGER "
						+ getSymbolString(tInt.integer));
				}

				out.println(indentStr
					+ "RADIX (" + tInt.radix + ")");

				if(tInt.size != 0) {
					out.println(indentStr + "SIZE ("
						+ tInt.size + ")");
				}

				if(tInt.flagUnsigned) {
					out.println(indentStr + "UNSIGNED");
				}

				dat.indentCount -= 1;
				indentStr = indentString();
				return;
			}

			if(id == Symbols.TOKEN_FLOAT_FULL) {
				tFloat = (TokenFloatFull) tok;

				out.println(indentString() + "TOKEN_FLOAT_FULL "
					+ getSymbolString(tok));

				dat.indentCount += 1;
				indentStr = indentString();

				if(tFloat.integer != null) {
					out.println(indentStr + "INTEGER "
						+ getSymbolString(tFloat.integer));
				}

				if(tFloat.fraction != null) {
					out.println(indentStr + "FRACTION "
						+ getSymbolString(tFloat.fraction));
				}

				if(tFloat.exponent != null) {
					out.println(indentStr + "EXPONENT "
						+ getSymbolString(tFloat.exponent));
				}

				if(tFloat.flagExponentNeg) {
					out.println(indentStr + "EXPONENT_NEGATIVE");
				}

				out.println(indentStr + "RADIX ("
					+ tFloat.radix + ")");

				if(tFloat.size != 0) {
					out.println(indentStr + "SIZE ("
						+ tFloat.size + ")");
				}

				dat.indentCount -= 1;
				indentStr = indentString();
				return;			
			}
		}
		
		if(id == Symbols.TOKEN_COMMENT_MULTI_LINE) {
			out.println(indentStr + "TOKEN_COMMENT_MULTI_LINE "
				+ getSymbolString(tok));
			
			dat.indentCount += 1;

			tCont = (TokenContainer) tok;
			dumpToken(tCont.tok[0]);

			dat.indentCount -= 1;
			return;
		}

		if(id == Symbols.TOKEN_COMMENT_SINGLE_LINE) {
			out.println(indentStr + "TOKEN_COMMENT_SINGLE_LINE "
				+ getSymbolString(tok));
			
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

			out.println(indentString() + tokenName + " "
				+ getSymbolString(tok));

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
		case Symbols.TOKEN_LPAREN:
			tokenName = "TOKEN_LPAREN";
			break;
		case Symbols.TOKEN_RPAREN:
			tokenName = "TOKEN_RPAREN";
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
		}

		if(tokenName != null) {
			out.println(indentStr + tokenName + " "
				+ getSymbolString(tok));
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
		}
		
		if(gramName != null) {
			out.println(indentString()
				+ gramName + " "
				+ getSymbolString(grm));
			
			dat.indentCount += 1;
			
			grmCon = (GramContainer) grm;
			len = dat.utils.getGramChildCount(grmCon);
			i = 0;
			while(i < len) {
				dumpSymbol(grmCon.sym[i]);
				i += 1;
			}

			dat.indentCount -= 1;
			
			return;
		}
				
		dumpGramGeneral(grm);
		return;
	}
	
	private void dumpSymbolGeneral(Symbol sym) {
		out.println(indentString() + "SYMBOL " + getSymbolString(sym));
		
		dat.indentCount += 1;
		
		out.println(indentString()
			+ "id=(" + dat.utils.getSymbolIdCategory(sym)
			+ "," + dat.utils.getSymbolIdPrimary(sym) + ")");
		
		dat.indentCount -= 1;

		throw makeObjectUnexpected(null);
	}
		
	private void dumpTokenGeneral(Token tok) {
		out.println(indentString() + "TOKEN " + getSymbolString(tok));
		
		dat.indentCount += 1;
		
		out.println(indentString()
			+ "id=(" + dat.utils.getSymbolIdCategory(tok)
			+ "," + dat.utils.getSymbolIdPrimary(tok) + ")");
		
		dat.indentCount -= 1;
		
		throw makeObjectUnexpected(null);
	}
		
	private void dumpGramGeneral(Gram grm) {
		out.println(indentString() + "GRAM " + getSymbolString(grm));
		
		dat.indentCount += 1;
		
		out.println(indentString()
			+ "id=(" + dat.utils.getSymbolIdCategory(grm)
			+ "," + dat.utils.getSymbolIdPrimary(grm) + ")");
		
		dat.indentCount -= 1;

		throw makeObjectUnexpected(null);
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
		
	public void checkModule(BaseModuleData modDat, String moduleName) {
		out.println(indentString() + "module " + moduleName);
		
		if(modDat.state == BaseModuleData.STATE_STUCK) {
			dat.indentCount += 1;
			
			out.println(indentString() + "STUCK with stuckState="
				+ modDat.stuckState);
			
			dat.indentCount -= 1;
		}
		
		dat.indentCount += 1;
		dumpProblemContainer(modDat.probBag);
		dat.indentCount -= 1;
	}
	
	private void dumpContext(Object obj) {
		if(obj == null) return;
		
		if(obj instanceof TextIndex) {
			TextIndex ti = (TextIndex) obj;

			out.println(indentString() + getIndexString(ti));
		}
		
		if(obj instanceof CommonInt32Array) {
			CommonInt32Array str = (CommonInt32Array) obj;
			
			out.println(indentString()
				+ StringUtils.javaStringFromInt32String(str));
		}
		
		Class c = obj.getClass();
		out.println(c.getName());
		return;
	}
			
	public void dumpProblemContainer(ProblemContainer probBag) {
		int probNum;
		int probCount;
		
		probNum = 0;
		probCount = probBag.problems.size();
		while(probNum < probCount) {
			Problem prob = (Problem) probBag.problems.get(probNum);
			
			out.println(indentString() + "Problem with "
				+ "problemLevel=" + prob.problemLevel);
			
			dat.indentCount += 1;
			
			Class c = prob.errorObject.getClass();
			String className = c.getName();
			out.println(indentString() + className);

			dat.indentCount += 1;
			
			if(prob.errorObject instanceof BaseError) {
				BaseError be = (BaseError) prob.errorObject;
				
				out.println(indentString() + "id=" + be.id);

				if(be.msg != null)
					out.println(indentString() + "msg=(" + be.msg + ")");
				
				dumpContext(be.context);

				if(be instanceof SymbolUnexpected) {
					SymbolUnexpected su = (SymbolUnexpected) be;
					
					out.println(indentString() + "sym id="
						+ dat.utils.getSymbolIdPrimary(su.givenSymbol));
					
					out.println(indentString() + "index "
						+ getIndexString(su.givenSymbol.startIndex));
				}
			}
			
			prob.errorObject.printStackTrace(out);

			dat.indentCount -= 2;
			
			probNum += 1;
		}
	}
	
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

	public void openLocalFileSystem(CommonInt32Array path, boolean writable) {
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
		fs.initRights((short) AccessRights.ACCESS_READ);
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
	
	private String stringEmptyToNull(String s) {
		if(s == null) return null;
		if(s.equals("")) return null;
		return s;
	}
	
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
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}
	
	private RuntimeException makeNullException(String msg) {
		if(msg == null) return new NullPointerException();
		
		return new NullPointerException(msg);
	}

	private CommonArrayList makeListWithSingleObject(Object o) {
		CommonArrayList lst;
		
		if(o == null) return null;
		
		lst = makeArrayList();
		lst.add(o);
		return lst;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}

	private HandleSet makeHandleSet() {
		HandleSet hs;

		hs = new HandleSet();
		hs.init();
		return hs;
	}

	private SortParams makeSortParams() {
		SortParams sortRec;
		
		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}

	private CharReaderParamsFile makeCharReaderParamsFile() {
		CharReaderParamsFile cp;

		cp = new CharReaderParamsFile();
		cp.init();
		return cp;
	}

	private ReadBufferPartial makeReadBufferPartial() {
		ReadBufferPartial rb;

		rb = new ReadBufferPartial();
		rb.init();
		return rb;
	}
	
	public void run() {
		int i;
		int count;
		String[] args;
		String arg;
		boolean isError;
		CommonInt32Array path;
		
		args = dat.args;
		
		if(args == null) return;

		initOnce();
				
		count = args.length;
		i = 0;
		while(i < count) {
			arg = args[i];
			
			if(arg == null) {
				i += 1;
				continue;
			}
			
			if(!arg.startsWith("--")) {
				out.println("Argument is not in the form --command");
				return;
			}
			
			if(arg.equals("--create-file-path-node")) {
				if(i + 1 >= count) {
					out.println("No argument for --create-file-path-node command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(args[i + 1]);
				createFilePathNode(path);

				i += 2;
				continue;
			}

			if(arg.equals("--open-local-file-system")) {
				if(i + 1 >= count) {
					out.println("No argument for --open-local-file-system command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(args[i + 1]);
				openLocalFileSystem(path, false);

				i += 2;
				continue;
			}

			if(arg.equals("--grammar-dir")) {
				if(i + 1 >= count) {
					out.println("No argument for --grammar-dir option");
					return;
				}

				path = StringUtils.int32StringFromJavaString(args[i + 1]);
				setGrammarDir(path);

				i += 2;
				continue;
			}

			if(arg.equals("--test-dir")) {
				if(i + 1 >= count) {
					out.println("No argument for --test-dir option");
					return;
				}

				path = StringUtils.int32StringFromJavaString(args[i + 1]);
				setTestDir(path);

				i += 2;
				continue;
			}
			
			if(arg.equals("--grammar-file")) {
				if(i + 1 >= count) {
					out.println("No argument for --grammar-file command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(args[i + 1]);
				initGrammarFile(path);
				isError = loadGrammarFile();
				
				if(isError) return;

				i += 2;
				continue;
			}

			if(arg.equals("--test-file")) {
				if(i + 1 >= count) {
					out.println("No argument for --test-file command");
					return;
				}

				path = StringUtils.int32StringFromJavaString(args[i + 1]);
				initTestFile(path);
				isError = runTestFile();
				cleanupTestFile();
				
				if(isError) return;

				i += 2;
				continue;
			}

			out.println("Invalid command " + arg);
			return;
		}
						
		return;
	}

	public static void main(String[] args) {
		ArithParserTest1 o = new ArithParserTest1();
		o.dat = new ArithParserTest1Data();
		o.out = System.out;

		o.dat.args = args;

		o.run();
	}
}
