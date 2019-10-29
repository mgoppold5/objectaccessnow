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

public class CsvTest {
	public CsvTestData dat;
	public PrintStream out;
	
	public void initOnce() {
		dat.directory = makeArrayList();

		dat.utils = new GeneralUtils();
		dat.utils.init();

		dat.linkUtils = new PublicLinkUtils();
		dat.linkUtils.init();
		
		dat.tokChoose = new TokenChooser();
		dat.tokChoose.dat = new TokenChooserData();
		dat.tokChoose.dat.init();
		dat.tokChoose.utils = dat.utils;
		//dat.tokChoose.charRead = dat.charRead;

		dat.csvRead = new CsvBaseReader();
		dat.csvRead.dat = new CsvBaseReaderData();
		dat.csvRead.dat.init();
		dat.csvRead.utils = dat.utils;
		//dat.csvRead.charRead = dat.charRead;
		dat.csvRead.tokChoose = dat.tokChoose;
		dat.csvRead.initHelpers(dat.utils);
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

		dat.tokChoose.charRead = dat.charRead;
		dat.tokChoose.reset();
		
		dat.csvRead.charRead = dat.charRead;
		dat.csvRead.reset();
		
		dat.charRead.reset();
		dat.strRead.reset();
	}
	
	public void cleanupTestFile() {
		dat.charRead = null;
		dat.strRead = null;
		
		dat.tokChoose.charRead = null;
		dat.tokChoose.reset();
		
		dat.csvRead.charRead = null;
		dat.csvRead.reset();
		
		FileNode2Utils.closeNormalFile(
			(FileNode2) dat.testFileNode, dat.fileDat);
		dat.testFileNode = null;
		dat.fileDat = null;
		return;
	}
	
	public boolean runTestFile() {
		Symbol sym;
		int id;
		boolean isError;
		
		int moveResult;
		int indentBak1;
				
		out.println(indentString() + "File: " +
			StringUtils.javaStringFromInt32String(
				(CommonInt32Array) dat.testFileNode.sortObject));

		dat.indentCount += 1; // now in File
		
		out.println(indentString() + "Outputing tree");

		dat.indentCount += 1; // now in tree
		indentBak1 = dat.indentCount;
		
		dat.csvRead.move(ModuleMoveDirection.TO_NEXT);
		
		isError = false;
		
		while(true) {
			if(dat.csvRead.dat.state == BaseModuleData.STATE_STUCK) {
				out.println(indentString()
					+ "STUCK with stuckState="
					+ dat.csvRead.dat.stuckState);
				isError = true;
				break;
			}
			
			if(dat.csvRead.dat.state == SimpleReaderData.STATE_HAVE_SYMBOL) {
				sym = dat.csvRead.getSymbol();
				id = dat.utils.getSymbolIdPrimary(sym);
				
				if(sym.symbolType == SymbolTypes.TYPE_TOKEN) {
					dumpToken((Token) sym);

					
					if(id == Symbols.TOKEN_END_OF_STREAM
						|| id == Symbols.TOKEN_UNEXPECTED_END_OF_STREAM)

						break;
				}
				
				if(sym.symbolType == SymbolTypes.TYPE_GRAM) {
					dumpGram((Gram) sym);
				}
			} else {
				out.println(indentString() + "unknown state");
			}
						
			moveResult = dat.csvRead.move(
				ModuleMoveDirection.TO_FIRST_CHILD);
			
			if(moveResult == ModuleMoveResult.SUCCESS) {
				dat.indentCount += 1;
				continue;
			}
			
			if(moveResult != ModuleMoveResult.AT_END) {
				dumpBadMove(moveResult);
				isError = true;
				break;
			}
			
			// node does not have a child
			//System.out.println("before to-next");
			moveResult = dat.csvRead.move(ModuleMoveDirection.TO_NEXT);
			//System.out.println("after to-next");
			
			while(moveResult == ModuleMoveResult.AT_END) {
				moveResult = dat.csvRead.move(
					ModuleMoveDirection.TO_PARENT);
				
				if(moveResult == ModuleMoveResult.AT_END) {
					out.println(indentString()
						+ "Could not move to parent");
					isError = true;
					break;
				}

				dat.indentCount -= 1;
				
				//System.out.println("Holy crap");
				moveResult = dat.csvRead.move(
					ModuleMoveDirection.TO_NEXT);
			}
			
			if(moveResult != ModuleMoveResult.SUCCESS) {
				dumpBadMove(moveResult);
				isError = true;
				break;
			}
			
			continue;
		}
		
		dat.indentCount = indentBak1;
		
		dat.indentCount -= 1; // now in File
		
		// check modules

		out.println(indentString() + "Outputing module info");
		
		dat.indentCount += 1; // now in module info
		
		checkModule(dat.charRead.getData(), "CharReader");
		checkModule(dat.csvRead.getData(), "CsvBaseReader");
		
		dat.indentCount -= 1; // now in file

		dat.indentCount -= 1; // now outside
		
		return isError;
	}
	
	private void dumpBadMove(int moveResult) {
		out.println(indentString()
			+ "moveResult=" + moveResult);
		return;
	}
	
		
	private String getSymbolString(Symbol sym) {
		return getRangeString(sym.startIndex, sym.pastIndex);
	}
	
	private String getRangeString(TextIndex start, TextIndex past) {
		long stringLen = 0;
		String s = null;
		
		CommonError e1;
		
		StringReader2Data strDat = (StringReader2Data) dat.strRead.getData();
		
		if(past.index > start.index)
			stringLen = past.index - start.index;
		
		if(stringLen > 1000) return "STRING_TOO_LONG";

		dat.strRead.readJavaStringThrow(start, stringLen);

		if(strDat.state != 
			CharReaderData.STATE_HAVE_JAVA_STRING_BUFFER) {
			
			e1 = new CommonError();
			e1.id = CommonErrorTypes.ERROR_INVALID_ENUM;
			throw e1;
		}
		
		s = strDat.resultJavaStringBuilder.toString();
		
		if(false) {
			s = getIndexString(start)
				+ " " + s + " "
				+ getIndexString(past);
		}
		
		return "(" + s + ")";
	}
	
	private void dumpToken(Token tok) {
		TokenContainer tokCon;
		TokenIntegerSimple tokInt;
		String tokenName;
		String msg;
		int id;
		
		id = dat.utils.getSymbolIdPrimary(tok);
		
		if(id == Symbols.TOKEN_END_OF_STREAM) {
			out.println(indentString() + "TOKEN_END_OF_STREAM");
			return;
		}
		
		if(id == Symbols.TOKEN_INTEGER_SIMPLE) {
			tokInt = (TokenIntegerSimple) tok;
			
			out.println(indentString()
				+ "TOKEN_INTEGER_SIMPLE "
				+ getSymbolString(tok));
			
			dat.indentCount += 1;
			
			out.println(indentString()
				+ "RADIX (" + tokInt.radix + ")");

			dat.indentCount -= 1;
			return;
		}
		
		tokenName = null;

		switch(id) {
		case Symbols.TOKEN_XML_REFERENCE_CHARACTER_NUMERIC:
			tokenName = "TOKEN_XML_REFERENCE_CHARACTER_NUMERIC";
			break;
		case Symbols.TOKEN_XML_REFERENCE_CHARACTER_ENTITY:
			tokenName = "TOKEN_XML_REFERENCE_CHARACTER_ENTITY";
			break;
		case Symbols.TOKEN_XML_CDATA:
			tokenName = "TOKEN_XML_CDATA";
			break;
		case Symbols.TOKEN_XML_COMMENT:
			tokenName = "TOKEN_XML_COMMENT";
			break;
		}
		
		if(tokenName != null) {
			tokCon = (TokenContainer) tok;

			out.println(indentString() + tokenName + " "
				+ getSymbolString(tok));

			dat.indentCount += 1;
			dumpToken(tokCon.tok[0]);
			dat.indentCount -= 1;
			return;
		}
		
		switch(id) {
		case Symbols.TOKEN_STRING_SPAN:
			tokenName = "TOKEN_STRING_SPAN";
			break;
		}
		
		if(tokenName != null) {
			msg = indentString() + tokenName + " "
				+ getSymbolString(tok);
			out.println(msg);
			return;
		}

		switch(id) {
		case Symbols.TOKEN_LINE_RETURN:
			tokenName = "TOKEN_LINE_RETURN";
			break;
		case Symbols.TOKEN_COMMA:
			tokenName = "TOKEN_COMMA";
			break;
		case Symbols.TOKEN_BAD_SPAN:
			tokenName = "TOKEN_BAD_SPAN";
			break;
		case Symbols.TOKEN_XML_NAME:
			tokenName = "TOKEN_XML_NAME";
			break;
		case Symbols.TOKEN_XML_TAG_FINISH:
			tokenName = "TOKEN_XML_TAG_WAS_START_TAG";
			break;
		case Symbols.TOKEN_XML_TAG_FINISH_WITH_END_MARK:
			tokenName = "TOKEN_XML_TAG_WAS_EMPTY_TAG";
			break;
		}
		
		if(tokenName != null) {
			out.println(indentString() + tokenName + " "
				+ getSymbolString(tok));
			return;
		}
		
		dumpTokenGeneral(tok);
		return;
	}

	private void dumpTokenGeneral(Token tok) {
		out.println(indentString() + "TOKEN " + getSymbolString(tok));
		
		dat.indentCount += 1;
		
		out.println(indentString()
			+ "id=(" + dat.utils.getSymbolIdCategory(tok)
			+ "," + dat.utils.getSymbolIdPrimary(tok) + ")");
		
		dat.indentCount -= 1;
		return;
	}
	
	private void dumpGramGeneral(Gram grm) {
		out.println(indentString() + "GRAM " + getSymbolString(grm));
		
		dat.indentCount += 1;
		
		out.println(indentString()
			+ "id=(" + dat.utils.getSymbolIdCategory(grm)
				+ "," + dat.utils.getSymbolIdPrimary(grm) + ")");
		
		dat.indentCount -= 1;
		return;
	}
	
	private void dumpGram(Gram grm) {
		String gramName;
		int id;
		
		gramName = null;
		
		id = dat.utils.getSymbolIdPrimary(grm);
		
		switch(id) {
		case Symbols.GRAM_XML_CONTENT:
			gramName = "GRAM_XML_CONTENT";
			break;
		case Symbols.GRAM_XML_TAG:
			gramName = "GRAM_XML_TAG";
			break;
		case Symbols.GRAM_XML_TAG_WITH_END_MARK:
			gramName = "GRAM_XML_TAG_STOP";
			break;
		case Symbols.GRAM_XML_ATTRIBUTE:
			gramName = "GRAM_XML_ATTRIBUTE";
			break;
		case Symbols.GRAM_LIST:
			gramName = "GRAM_LIST";
			break;
		case Symbols.GRAM_DICT:
			gramName = "GRAM_DICT";
			break;
		case Symbols.GRAM_DICT_ENTRY:
			gramName = "GRAM_DICT_ENTRY";
			break;
		}
		
		if(gramName != null) {
			out.println(indentString()
				+ gramName + " "
				+ getSymbolString(grm));
			return;
		}
				
		dumpGramGeneral(grm);
		return;
	}

	private String getIndexString(TextIndex ti) {
		return "(" + ti.index
			+ "," + ti.line
			+ "," + ti.indexWithinLine
			+ ")";
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

	private void checkModule(BaseModuleData modDat, String moduleName) {
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
		
		return;
	}
	
	public void dumpProblemContainer(ProblemContainer probBag) {
		int probNum;
		int probCount;
		Class c;
		
		probNum = 0;
		probCount = probBag.problems.size();
		while(probNum < probCount) {
			Problem prob = (Problem) probBag.problems.get(probNum);
			
			out.println(indentString() + "Problem with "
				+ "problemLevel=" + prob.problemLevel);
			
			dat.indentCount += 1;
			
			c = prob.errorObject.getClass();
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
		CsvTest o = new CsvTest();
		o.dat = new CsvTestData();
		o.out = System.out;
		
		o.dat.args = args;
		
		o.run();
	}

}
