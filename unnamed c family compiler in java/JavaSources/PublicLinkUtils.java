/*
 * Copyright (c) 2017 Mike Goppold von Lobsdorf
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

public class PublicLinkUtils {
	public void init() {}
	
	public CharReaderAccess createCharReader2FromFileContext(
		FileNode fileContext, FileRef fileDat, GeneralUtils utils) {
		
		CharReader2 charRead = new CharReader2();
		charRead.dat = new CharReader2Data();
		charRead.dat.init();
		charRead.utils = utils;
		
		CharReaderParamsFile charReadParams = makeCharReaderParamsFile();
		charReadParams.id =
			CharReaderParamsTypes.PARAMS_TYPE_FOR_FILE;
		charReadParams.fileDat = fileDat;
		charReadParams.readBuf = makeReadBufferPartial();
		charReadParams.context = fileContext;
		
		ReadStrategy strat = charReadParams.strat;
		strat.minAheadAmount = 100;
		strat.minBehindAmount = 100;
		strat.readAheadAmount = 10000;
		strat.readBehindAmount = 10000;
		strat.specificBufferSize = 40000;
		strat.idealBufferSize = 80000;
		strat.idealSpecificBufferCount = 2;
		
		charRead.dat.charReadParams = charReadParams;
		
		return charRead;
	}

	public StringReaderAccess createStringReader2FromFileContext(
		FileNode fileContext, FileRef fileDat, GeneralUtils utils) {
		
		StringReader2 strRead = new StringReader2();
		strRead.dat = new StringReader2Data();
		strRead.dat.init();
		strRead.utils = utils;
		strRead.charRead = createCharReader2FromFileContext(
			fileContext, fileDat, utils);
		
		return strRead;
	}

	public void initFileContext(FileNode2 fileContext, FileRef2 fileDat) {
		int fileHandle = fileDat.fileHandle;
		
		fileDat.fs.initFileClientUnitSize(fileHandle, (short) 3);
		fileDat.fs.initFileMemoryUnitSize(fileHandle, (short) 4);
		fileDat.fs.initFileStrictAlignSize(fileHandle, (short) 4);
		fileDat.fs.initFile(fileHandle);
	}
	
	public CharReaderAccess createCharReader3FromFileContext(
		FileNode2 fileContext, FileRef2 fileDat, GeneralUtils utils) {
		
		StorageCursorAccess cur = createCursorStyle1FromFileContext(
			fileContext, fileDat);
		
		cur.setAccessType((short) StorageAccessTypes.TYPE_READ);

		CharReader3 charRead = new CharReader3();
		charRead.dat = new CharReader3Data();
		charRead.dat.init();
		charRead.dat.encoding = CharacterEncoding.UTF_8;
		charRead.dat.codeUnitSize = 1;
		charRead.utils = utils;
		
		ReadStrategy3 strat = new ReadStrategy3();
		strat.readAheadAmount = 10000;
		strat.readBehindAmount = 10000;
		
		CharReaderParamsFile charReadParams = makeCharReaderParamsFile();
		charReadParams.id =
			CharReaderParamsTypes.PARAMS_TYPE_FOR_FILE;
		//charReadParams.fileDat = fileDat;
		//charReadParams.readBuf = makeReadBufferPartial();
		charReadParams.context = fileContext;
				
		charReadParams.streamPastIndex = new TextIndex();
		charReadParams.streamPastIndex.index = cur.getLength();
		
		charRead.dat.curRead = cur;
		charRead.dat.strat = strat;
		charRead.dat.charReadParams = charReadParams;
		
		return charRead;
	}
	
	public StringReaderAccess createStringReader3FromFileContext(
		FileNode2 fileContext, FileRef2 fileDat, GeneralUtils utils) {
		
		StringReader3 strRead = new StringReader3();
		strRead.dat = new StringReader3Data();
		strRead.dat.init();
		strRead.utils = utils;
		strRead.charRead = createCharReader3FromFileContext(
			fileContext, fileDat, utils);
		
		return strRead;
	}

	public CharWriter3 createCharWriter3FromFileContextAndCursor(
		FileNode2 fileContext, FileRef2 fileDat, StorageCursorAccess cur, GeneralUtils utils) {
		
		CharWriter3 charWrite = new CharWriter3();
		charWrite.dat = new CharWriter3Data();
		charWrite.dat.init();
		charWrite.dat.encoding = CharacterEncoding.UTF_8;
		charWrite.dat.codeUnitSize = 1;
		charWrite.utils = utils;
		
		CharReaderParamsFile charReadParams = makeCharReaderParamsFile();
		charReadParams.id =
			CharReaderParamsTypes.PARAMS_TYPE_FOR_FILE;
		charReadParams.context = fileContext;
		
		charWrite.dat.curWrite = cur;
		charWrite.dat.charReadParams = charReadParams;
		
		return charWrite;
	}

	public StringWriter3 createStringWriter3FromFileContextAndCursor(
		FileNode2 fileContext, FileRef2 fileDat,
		StorageCursorAccess cur,
		GeneralUtils utils) {
		
		StringWriter3 strWrite = new StringWriter3();
		strWrite.dat = new StringWriter3Data();
		strWrite.dat.init();
		strWrite.utils = utils;
		strWrite.charWrite = createCharWriter3FromFileContextAndCursor(
			fileContext, fileDat, cur, utils);
		
		return strWrite;
	}

	public StorageCursorAccess createCursorStyle1FromFileContext(
		FileNode2 fileContext, FileRef2 fileDat) {
		
		// This cursor is good for reading,
		// even in small patches,
		// with many cursor restarts
	
		StorageRangeFromPath r = new StorageRangeFromPath();
		r.initFileRef(fileDat.fs, fileDat.fileHandle);
		r.init();
				
		StorageBlockCursor cur = new StorageBlockCursor();
		cur.initRangeRef(r);
		
		if(true) {
			cur.initMinBlockSize((short) 8);   // 32 bytes
			cur.initGrowStepSize((short) 4);   // 16
			cur.initMaxGrowCount(4);           // 16 ^ 4 = 4096 * 16
			// 32 times 4096 times 16 = 2,097,152 bytes
		}

		if(false) {
			cur.initMinBlockSize((short) 12);  // 512 bytes
			cur.initGrowStepSize((short) 4);   // 16
			cur.initMaxGrowCount(3);           // 16 ^ 3 = 4096
			// 512 times 4096 = 2,097,152 bytes
		}

		if(false) {
			cur.initMinBlockSize((short) 16);  // 512 * 16 bytes
			cur.initGrowStepSize((short) 4);   // 16
			cur.initMaxGrowCount(2);           // 16 ^ 2 = 256
			// 512 times 16 times 256 = 2,097,152 bytes
		}
		
		cur.init();
		return cur;
	}

	public StorageCursorAccess createCursorStyle2FromFileContext(
		FileNode2 fileContext, FileRef2 fileDat) {
	
		// This cursor is good for quickly writing a file,
		// from beginning to end

		StorageRangeFromPath r = new StorageRangeFromPath();
		r.initFileRef(fileDat.fs, fileDat.fileHandle);
		r.init();
				
		StorageBlockCursor cur = new StorageBlockCursor();
		cur.initRangeRef(r);
		
		if(false) {
			cur.initMinBlockSize((short) 12);  // 512 bytes
			cur.initGrowStepSize((short) 4);   // 16
			cur.initMaxGrowCount(3);           // 16 ^ 3 = 4096
			// 512 times 4096 = 2,097,152 bytes
		}

		if(true) {
			cur.initMinBlockSize((short) 16);  // 512 * 16 bytes
			cur.initGrowStepSize((short) 4);   // 16
			cur.initMaxGrowCount(2);           // 16 ^ 2 = 256
			// 512 times 16 times 256 = 2,097,152 bytes
		}
		
		cur.init();
		return cur;
		
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
}
