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

public class StringReader3 implements StringReaderAccess {
	public StringReader3Data dat;
	public GeneralUtils utils;
	public CharReaderAccess charRead;

	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList list = makeArrayList();
		if(charRead != null) list.add(charRead);
		return list;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.probBag.reset();
		
		dat.state = BaseModuleData.STATE_START;
	}
		
	public void readChar(TextIndex ti, BufferIndex bi) {
		Throwable ex;
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;		
		
		ex = null;

		try {
			charRead.readCharThrow(ti, bi);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) fireRuntimeError(ex);
		
		return;
	}
	
	public void readJavaStringThrow(TextIndex ti, long len) {
		long iDone;
		int state;
		int charLength;
		
		CharReader3Data charDat = (CharReader3Data) charRead.getData();
		CharReaderParams prms = charDat.charReadParams;

		TextIndex ti2;
		BufferIndex bi2;
		
		long goodCharCount;
		long badCharCount;
		
		ti2 = prms.tiTemp2;
		bi2 = prms.biTemp2;

		StringBuilder sb = new StringBuilder();
			
		utils.copyTextIndex(ti2, ti);
		bi2.versionNumber = ReadBufferPartial.VERSION_NUMBER_INVALID;
		
		iDone = 0;
		goodCharCount = 0;
		badCharCount = 0;
		
		while(iDone < len) {
			charRead.readCharThrow(ti2, bi2);
			state = charDat.state;
			
			if(state == CharReader3Data.STATE_HAVE_CHAR) {
				charLength = charDat.resultCharLength;

				goodCharCount += 1;

				// add char
				sb.appendCodePoint(charDat.resultChar);

				iDone += charLength;
				utils.textIndexSkip(ti2, charLength);
				utils.bufferIndexSkip(bi2, charLength);
				continue;
			}
			
			if(state == CharReader3Data.STATE_END_OF_STREAM) {
				java.io.PrintStream out = System.out;
				out.println("index," + ti2.index);
				out.println("tiIndex," + ti2.index + ",len," + len);
				
				
				throw makeIndexOutOfBoundsException(null);
			}
			
			if(state == CharReader3Data.STATE_HAVE_BAD_CHAR) {
				badCharCount += 1;
				
				charLength = charDat.resultCharLength;
				iDone += charLength;
				utils.textIndexSkip(ti2, charLength);
				utils.bufferIndexSkip(bi2, charLength);
				continue;
			}
			
			throw makeInvalidEnum("CharReader has bad state");
		}
		
		dat.resultJavaStringBuilder = sb;

		dat.state = StringReader2Data.STATE_HAVE_JAVA_STRING_BUFFER;
		dat.resultBadCharCount = badCharCount;
		dat.resultGoodCharCount = goodCharCount;
		
		return;
	}	
	
	public void readJavaString(TextIndex ti, long len) {

		Throwable ex;
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;		
		
		ex = null;

		try {
			readJavaStringThrow(ti, len);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) fireRuntimeError(ex);
		
		return;
	}
	
	public void readUtf32Throw(TextIndex ti, long len) {
		
		long iDone;
		int state;
		int charLength;
		
		CharReader3Data charDat = (CharReader3Data) charRead.getData();
		CharReaderParams prms = charDat.charReadParams;
				
		TextIndex ti2;
		BufferIndex bi2;
		
		long goodCharCount;
		long badCharCount;
		
		CommonInt8Array scratch;
		
		//scratch = new byte[4];
		scratch = CommonUtils.makeInt8Array(4);
		
		ti2 = prms.tiTemp2;
		bi2 = prms.biTemp2;

		BufferCombo str = makeBufferCombo();
			
		utils.copyTextIndex(ti2, ti);
		bi2.versionNumber = ReadBufferPartial.VERSION_NUMBER_INVALID;
		
		iDone = 0;
		goodCharCount = 0;
		badCharCount = 0;
		
		while(iDone < len) {
			charRead.readCharThrow(ti2, bi2);
			state = charDat.state;
			
			if(state == CharReader3Data.STATE_HAVE_CHAR) {
				charLength = charDat.resultCharLength;

				goodCharCount += 1;

				// add char
				StringUtils.utf32AppendCodePoint(
					str, charDat.resultChar, scratch);

				iDone += charLength;
				utils.textIndexSkip(ti2, charLength);
				utils.bufferIndexSkip(bi2, charLength);
				continue;
			}
			
			if(state == CharReader3Data.STATE_END_OF_STREAM)
				throw makeIndexOutOfBoundsException(null);
			
			if(state == CharReader3Data.STATE_HAVE_BAD_CHAR) {
				badCharCount += 1;
				
				charLength = charDat.resultCharLength;
				iDone += charLength;
				utils.textIndexSkip(ti2, charLength);
				utils.bufferIndexSkip(bi2, charLength);
				continue;
			}
			
			throw makeInvalidEnum("CharReader has bad state");
		}
		
		dat.resultBufferString = str;

		dat.state = StringReader2Data.STATE_HAVE_BUFFER_STRING;
		dat.resultBadCharCount = badCharCount;
		dat.resultGoodCharCount = goodCharCount;
		
		return;
	}	
	
	public void readUtf32(TextIndex ti, long len) {
		Throwable ex;

		if(dat.state == BaseModuleData.STATE_STUCK)
			return;		
		
		ex = null;

		try {
			readUtf32Throw(ti, len);
		} catch(Throwable e2) {
			ex = e2;
		}

		if(ex != null) fireRuntimeError(ex);
		
		return;
	}
	
	private void fireRuntimeError(Throwable ex) {
		if(ex == null) return;
		
		dat.probBag.addProblem(ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
		dat.state = BaseModuleData.STATE_STUCK;
		dat.stuckState = StuckStates.STATE_PERMANENT;
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}

	private RuntimeException makeIndexOutOfBoundsException(String msg) {
		if(msg != null)
			return new IndexOutOfBoundsException(msg);
		
		return new IndexOutOfBoundsException();
	}

	private BufferCombo makeBufferCombo() {
		BufferCombo bc;
		
		bc = new BufferCombo();
		bc.init();
		return bc;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
