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

public class CharWriter3 implements BaseModule {
	public CharWriter3Data dat;
	public GeneralUtils utils;
	
	public BaseModuleData getData() {return dat;}
	public CommonArrayList getChildModules() {return null;}
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
	
	// fills high bits with 111... (ones) then 0 (zero) pattern
	private static int fillHighByte(int theByte, int bitLength) {
		int i;
		int r;
		
		r = theByte;
		
		// zero bit at bitLength
		r &= (-1) ^ (1 << bitLength);
				
		i = bitLength + 1; // skip the position where the zero goes
		while(i < 8) {
			r |= 1 << i;

			i += 1;
		}
				
		return r;
	}
	
	public void writeCharThrow(TextIndex ti) {
		int unitIndex; // index into the char, in code units
		int unitCount; // how many code units this char has
		int fullChar; // a unicode code point
		
		CharReaderParams prms = dat.charReadParams;
		StorageCursorAccess curWrite = dat.curWrite;

		fullChar = dat.resultChar;

		if(fullChar < 0) {
			throw makeInvalidChar(prms.context, ti);
		}
		
		if(fullChar >= 0xD800 && fullChar <= 0xDFFF) {
			throw makeInvalidChar(prms.context, ti);
		}

		if(fullChar > 0x10FFFF) {
			throw makeInvalidChar(prms.context, ti);
		}

		if(dat.encoding == CharacterEncoding.UTF_8)
		while(true) {
			// simple case with 7 bit character
			if(fullChar < (1 << 8)) {
				unitIndex = 0;
				unitCount = 1;
				curWrite.setAlloc(unitCount);
				curWrite.setValue8(unitIndex, (byte) fullChar);
				curWrite.moveForward();
				dat.resultCharLength = unitCount;
				dat.state = CharWriter3Data.STATE_WROTE_CHAR;
				return;
			}
			
			// first possibility and inital conditions
			int highBits = 5;
			unitCount = 1;

			while(true) {
				int holdBits = highBits + unitCount * 6;

				// test if character fits
				if(fullChar < (1 << holdBits)) {
					break;
				}

				highBits -= 1;
				unitCount += 1;

				if(highBits == 0)
					throw makeInvalidChar(prms.context, ti);
			}

			// highBits and unitCount calculated

			curWrite.setAlloc(unitCount);
			
			int theByte;
			
			
			// leading byte calc
			unitIndex = 0;
			theByte = fullChar >> (unitCount * 6);
			theByte = fillHighByte(theByte, highBits);
			//buf[0] = (byte) theByte;
			curWrite.setValue8(unitIndex, (byte) theByte);

			unitIndex = 1;
			while(unitIndex < unitCount) {
				// continuation bytes highest to lowest
				theByte = fullChar >> ((unitCount - unitIndex) * 6);
				theByte = fillHighByte(theByte, 6);

				//buf[i] = (byte) theByte;
				curWrite.setValue8(unitIndex, (byte) theByte);

				unitIndex += 1;
			}
			
			curWrite.moveForward();
			dat.resultCharLength = unitCount;
			dat.state = CharWriter3Data.STATE_WROTE_CHAR;
			return;
		}
		
		if(dat.encoding == CharacterEncoding.UTF_16_LE)
		while(true) {
			if(fullChar < 0x010000) {
				unitIndex = 0;
				unitCount = 1;
				curWrite.setAlloc(unitCount);
				curWrite.setValue16(unitIndex, (short) fullChar);
				curWrite.moveForward();
				dat.resultCharLength = unitCount;
				dat.state = CharWriter3Data.STATE_WROTE_CHAR;
				return;
			}
			
			unitCount = 2;
			curWrite.setAlloc(unitCount);
			
			// 20 bits total

			int theShort;
			
			// top 10 bits
			unitIndex = 0;
			theShort = (fullChar >> 10) & 0x3FF;
			theShort += 0xD800;
			curWrite.setValue16(unitIndex, (short) theShort);
			
			// low 10 bits
			unitIndex = 1;
			theShort = fullChar & 0x3FF;
			theShort += 0xDC00;
			curWrite.setValue16(unitIndex, (short) theShort);
			
			curWrite.moveForward();
			dat.resultCharLength = unitCount;
			dat.state = CharWriter3Data.STATE_WROTE_CHAR;
			return;
		}

		if(dat.encoding == CharacterEncoding.UTF_32_LE)
		while(true) {
			unitIndex = 0;
			unitCount = 1;
			curWrite.setAlloc(unitCount);
			curWrite.setValue32(unitIndex, fullChar);
			curWrite.moveForward();
			dat.resultCharLength = unitCount;
			dat.state = CharWriter3Data.STATE_WROTE_CHAR;
			return;
		}
		
		// unknown encoding
		throw makeUnknownCharEncoding(null);
	}
	
	public void writeChar(TextIndex ti) {
		Throwable ex;
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;		

		//CharReaderParams prms = dat.charReadParams;

		ex = null;

		try {
			writeCharThrow(ti);
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

	private CommonError makeInvalidChar(ContextNode ctx, TextIndex ti) {
		TextIndex context;
		CommonError e1;
		
		context = null;
		if(ti != null) {
			context = new TextIndex();
			utils.copyTextIndex(context, ti);
		}

		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_CHARACTER;
		e1.context = context;
		return e1;
	}
	
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
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

	private CommonError makeUnknownCharEncoding(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_UNKNOWN_CHARACTER_ENCODING;
		e1.msg = msg;
		return e1;
	}
	
	private FileSystemError makeIOError(String msg) {
		FileSystemError e1;
		
		e1 = new FileSystemIoError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_FS_IO;
		e1.msg = msg;
		return e1;
	}
	
	private RuntimeException makeIndexOutOfBoundsException(String msg) {
		if(msg != null)
			return new IndexOutOfBoundsException(msg);
		
		return new IndexOutOfBoundsException();
	}
}
