/*
 * Copyright (c) 2013-2016 Mike Goppold von Lobsdorf
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

public class TokenTest {
	public TokenTestData dat;
	public PrintStream out;
	
	public void initOnce() {
		dat.directory = makeArrayList();
		
		dat.utils = new GeneralUtils();
		dat.utils.init();
		dat.tokUtils = new TokenUtils();
		dat.tokUtils.init();
		dat.linkUtils = new PublicLinkUtils();
		dat.linkUtils.init();

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
		
		dat.tokenRead.charRead = dat.charRead;
		dat.tokenRead.reset();
		
		dat.tokChoose.charRead = dat.charRead;
		dat.tokChoose.reset();

		dat.charRead.reset();
		dat.strRead.reset();
	}
	
	public void cleanupTestFile() {
		dat.charRead = null;
		dat.strRead = null;
		
		dat.tokChoose.charRead = null;
		dat.tokChoose.reset();
		
		dat.tokenRead.charRead = null;
		dat.tokenRead.reset();
				
		FileNode2Utils.closeNormalFile(
			(FileNode2) dat.testFileNode, dat.fileDat);
		dat.testFileNode = null;
		dat.fileDat = null;
		return;
	}
	
	public boolean runTestFile() {
		int moveResult;
		Token tok;
		int id;
		TokenReader tokRead;
		TokenReaderData tokReadDat;
		boolean isError;
		
		out.println(indentString() + "File: " +
			StringUtils.javaStringFromInt32String(
				(CommonInt32Array) dat.testFileNode.sortObject));
		
		dat.indentCount += 1; // now in File

		out.println(indentString() + "Outputing tokens");

		dat.indentCount += 1; // now in tokens
		
		tokRead = (TokenReader) dat.tokenRead;
		tokReadDat = (TokenReaderData) tokRead.getData();
		
		moveResult = dat.tokenRead.move(ModuleMoveDirection.TO_NEXT);

		isError = false;
		
		while(true) {
			if(moveResult == ModuleMoveResult.AT_END)
				break;
			
			if(moveResult != ModuleMoveResult.SUCCESS) {
				out.println(indentString()
					+ "Bad move result: moveResult="
					+ moveResult);
				isError = true;
				break;
			}
			
			if(tokReadDat.state != TokenReaderData.STATE_HAVE_TOKEN) {
				out.println(indentString()
					+ "TokenReader has bad state: state="
					+ tokReadDat.state);
				isError = true;
				break;
			}
			
			tok = tokReadDat.resultToken;
			id = dat.utils.getSymbolIdPrimary(tok);
			
			dumpToken(tok);
						
			moveResult = tokRead.move(ModuleMoveDirection.TO_NEXT);
		}
		
		dat.indentCount -= 1; // now in file
		
		// check modules

		out.println(indentString() + "Outputing module info");
		
		dat.indentCount += 1; // now in module info
		
		checkModule(dat.charRead.getData(), "CharReader");
		checkModule(dat.tokenRead.dat, "TokenReader");
		
		dat.indentCount -= 2; // now outside
		
		return isError;
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
	
	private String getTokenString(Token tok) {
		long stringLen = 0;
		String s = null;
		
		CommonError e1;
		
		StringReader2Data strDat = (StringReader2Data) dat.strRead.getData();
		
		if(tok.pastIndex.index > tok.startIndex.index)
			stringLen = tok.pastIndex.index - tok.startIndex.index;
		
		if(stringLen > 1000) return "STRING_TOO_LONG";

		dat.strRead.readJavaStringThrow(tok.startIndex, stringLen);

		if(strDat.state !=
			CharReaderData.STATE_HAVE_JAVA_STRING_BUFFER) {
			
			e1 = new CommonError();
			e1.id = CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}
		
		s = strDat.resultJavaStringBuilder.toString();
		
		if(false) {
			s = getIndexString(tok.startIndex)
				+ " " + s + " "
				+ getIndexString(tok.pastIndex);
		}
		
		return "(" + s + ")";
	}
	
	private void dumpStringGuts(Token tok) {
		TokenString t2;
		Token tInner;
		TokenContainer t4;
		TokenIntegerSimple t5;
		int cat;
		
		int i;
		int len;
		
		t2 = (TokenString) tok;
		
		i = 0;
		len = t2.elements.size();
		while(i < len) {
			tInner = (Token) t2.elements.get(i);
			cat = dat.utils.getSymbolIdCategory(tInner);
			
			if(cat == Symbols.TOKEN_CATEGORY_XML) {
				dumpTokenXml(tInner);
				i += 1;
				continue;
			}
			
			dumpToken(tInner);
			i += 1;
			continue;
		}
		
		return;
	}
	
	private void dumpTokenXml(Token tok) {
		TokenContainer t2;
		TokenIntegerSimple t3;
		Token t4;
		int id;
		
		id = dat.utils.getSymbolIdPrimary(tok);
		
		if(id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC) {
			t2 = (TokenContainer) tok;
			
			out.println(indentString()
				+ "TOKEN_XML_REFERNCE_CHARACTER_NUMERIC "
				+ getTokenString(tok));
			
			dat.indentCount += 1;
			dumpToken(t2.tok[0]);
			dat.indentCount -= 1;
			return;
			
		}

		if(id == Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY) {
			t2 = (TokenContainer) tok;
			
			out.println(indentString()
				+ "TOKEN_XML_REFERNCE_CHARACTER_ENTITY "
				+ getTokenString(tok));

			dat.indentCount += 1;
			dumpTokenXml(t2.tok[0]);
			dat.indentCount -= 1;
			return;
		}
		
		if(id == Symbols.TOKEN_XML_NAME) {
			out.println(indentString()
				+ "TOKEN_XML_NAME"
				+ getTokenString(tok));
			return;
		}

		dumpToken(tok);
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
					+ getTokenString(tokIntSimple));

				dat.indentCount += 1;

				out.println(indentString()
					+ "RADIX (" + tokIntSimple.radix + ")");

				dat.indentCount -= 1;
				return;
			}

			if(id == Symbols.TOKEN_INTEGER_FULL) {
				tInt = (TokenIntegerFull) tok;

				out.println(indentStr + "TOKEN_INTEGER_FULL "
					+ getTokenString(tok));

				dat.indentCount += 1;
				indentStr = indentString();

				if(tInt.integer != null) {
					out.println(indentStr + "INTEGER "
						+ getTokenString(tInt.integer));
				}

				if(tInt.flagNumberNeg) {
					out.println(indentStr + "NUMBER_NEGATIVE");
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
					+ getTokenString(tok));

				dat.indentCount += 1;
				indentStr = indentString();

				if(tFloat.integer != null) {
					out.println(indentStr + "INTEGER "
						+ getTokenString(tFloat.integer));
				}

				if(tFloat.fraction != null) {
					out.println(indentStr + "FRACTION "
						+ getTokenString(tFloat.fraction));
				}

				if(tFloat.exponent != null) {
					out.println(indentStr + "EXPONENT "
						+ getTokenString(tFloat.exponent));
				}

				if(tFloat.flagNumberNeg) {
					out.println(indentStr + "NUMBER_NEGATIVE");
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
				+ getTokenString(tok));
			
			dat.indentCount += 1;

			tCont = (TokenContainer) tok;
			dumpToken(tCont.tok[0]);

			dat.indentCount -= 1;
			return;
		}

		if(id == Symbols.TOKEN_COMMENT_SINGLE_LINE) {
			out.println(indentStr + "TOKEN_COMMENT_SINGLE_LINE "
				+ getTokenString(tok));
			
			dat.indentCount += 1;

			tCont = (TokenContainer) tok;
			dumpToken(tCont.tok[0]);

			dat.indentCount -= 1;
			return;
		}
		
		if(id == Symbols.TOKEN_STRING) {
			out.println(indentStr + "TOKEN_STRING "
				+ getTokenString(tok));
			
			dat.indentCount += 1;
			dumpStringGuts(tok);
			dat.indentCount -= 1;
			return;
		}

		if(id == Symbols.TOKEN_CHARACTER) {
			out.println(indentStr + "TOKEN_CHARACTER "
				+ getTokenString(tok));
			
			dat.indentCount += 1;
			dumpStringGuts(tok);
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
				+ getTokenString(tok));

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
		case Symbols.TOKEN_STRING_ESCAPE_IGNORED_RETURN:
			tokenName = "TOKEN_STRING_ESCAPE_IGNORED_RETURN";
			break;
		}

		if(tokenName != null) {
			out.println(indentStr + tokenName + " "
				+ getTokenString(tok));
			return;
		}
		
		dumpTokenGeneral(tok);
		return;
	}
	
	private void dumpTokenGeneral(Token tok) {
		out.println(indentString() + "TOKEN " + getTokenString(tok));
		
		dat.indentCount += 1;
		
		out.println(indentString()
			+ "id=(" + dat.utils.getSymbolIdCategory(tok)
			+ "," + dat.utils.getSymbolIdPrimary(tok) + ")");
		
		dat.indentCount -= 1;
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
				
				//dumpContext(be.context);

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
		CommonInt32Array nativePath =
			PathUtils.combineManyPaths(pathList, '/');
		
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
	
	private String stringEmptyToNull(String s) {
		if(s == null) return null;
		if(s.equals("")) return null;
		return s;
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
		TokenTest o = new TokenTest();
		o.dat = new TokenTestData();
		o.out = System.out;

		o.dat.args = args;

		o.run();
	}
}
