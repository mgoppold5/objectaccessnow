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

public class CharReader3 implements CharReaderAccess {
	public CharReader3Data dat;
	public GeneralUtils utils;

	public long trace1Count;
	public long trace2Count;
	public long trace3Count;
	public long trace4Count;
	public long trace5Count;
	
	public long freshAllocCount;
	public long permFreeCount;
	
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
	
	public void readCharThrow(
		TextIndex ti, BufferIndex bi) {
		
		boolean isAtEnd; // iff the TextIndex is at the end of the stream
		int unitIndex; // index into the char, in code units
		int unitCount; // how many code units this char has
		int c1; // first code unit
		int cn; // n'th code unit
		int i; // general purpose integer
		int fullChar; // a unicode code point

		boolean ok;
		
		long storagePointer2;
		int availUnits;
		
		CharReaderParams prms = dat.charReadParams;
		StorageCursorAccess curRead = dat.curRead;
		ReadStrategy3 strat = dat.strat;
		
		CharReaderParamsFile fileParams;
		TextIndex tiTemp;
		BufferIndex biTemp;
		
		TextIndex streamPastIndex2;
		
		tiTemp = prms.tiTemp1;
		biTemp = prms.biTemp1;
		fileParams = null;
		
		
		if(prms.streamStartIndex != null)
		if(ti.index < prms.streamStartIndex.index) {
			dat.state = CharReader3Data.STATE_END_OF_STREAM;
			return;
		}
		

		// 6 for the maximum number of code units, under UTF8.
		// set streamPastIndex2 if there is a possibility
		// that the stream end would interfere
		streamPastIndex2 = null;
		if(prms.streamPastIndex != null) {
			streamPastIndex2 = prms.streamPastIndex;

			if(ti.index >= streamPastIndex2.index) {
				dat.state = CharReader3Data.STATE_END_OF_STREAM;
				return;
			}
			
			if(ti.index + CharReader2Data.UNICODE_MAX_CODE_UNIT_PER_CODE_POINT
				<= streamPastIndex2.index)
				
				// no possibility of interference
				streamPastIndex2 = null;
		}
		
		availUnits = CharReader2Data.UNICODE_MAX_CODE_UNIT_PER_CODE_POINT;
		if(streamPastIndex2 != null) {
			availUnits = (int) (streamPastIndex2.index - ti.index);
		}
		
		
		short clientUnitSize = curRead.getClientUnitSize();
		short strictAlignSize = curRead.getStrictAlignSize();

		int readBehindAmount2;
		
		long pos = curRead.getStoragePointer();
		
		ok = true;
		if(ti.index < pos) ok = false;
		if(ti.index + availUnits
			> pos + curRead.getAlloc()) ok = false;
		if(!curRead.getIsCursorRunning()) ok = false;
		
		if(!ok) {
			long min;

			readBehindAmount2 = strat.readBehindAmount;
			if(readBehindAmount2 > ti.index)
				readBehindAmount2 = (int) ti.index;

			min = ti.index - readBehindAmount2;

			min = PowerUnitUtils.convertInt64(
				min, clientUnitSize, strictAlignSize, false);
			min = PowerUnitUtils.convertInt64(
				min, strictAlignSize, clientUnitSize, false);

			if(prms.streamStartIndex != null)
			if(prms.streamStartIndex.index > min)
				min = prms.streamStartIndex.index;

			long max;

			max = ti.index
				+ availUnits
				+ strat.readAheadAmount;

			max = PowerUnitUtils.convertInt64(
				max, clientUnitSize, strictAlignSize, true);
			max = PowerUnitUtils.convertInt64(
				max, strictAlignSize, clientUnitSize, true);

			if(prms.streamPastIndex != null)
			if(prms.streamPastIndex.index < max)
				max = prms.streamPastIndex.index;

			int len = (int) (max - min);
			if(len != max - min) throw makeOutOfBounds(null);
			
			
			//long savePos = pos;
			//long saveTiIndex = ti.index;
			//long saveMin = min;
			//long saveLen = len;

			//System.out.println("CurBad," + ti.index + "," + pos + "," + min);

			ok = true;
			if(!curRead.getIsCursorRunning()) ok = false;

			if(!ok) {
				if(curRead.getIsCursorRunning())
					curRead.setIsCursorRunning(false);

				curRead.setStoragePointer(min);
				curRead.setIsCursorRunning(true);
				curRead.setAlloc(len);

				// count the number of cursor restarts
				trace1Count += 1;
				
				pos = min;
			}

			ok = true;
			if(ti.index < pos) ok = false;
			
			if(!ok) {
				if(curRead.getIsCursorRunning())
					curRead.setIsCursorRunning(false);

				curRead.setStoragePointer(min);
				curRead.setIsCursorRunning(true);
				curRead.setAlloc(len);

				// count the number of cursor restarts
				trace2Count += 1;
				
				pos = min;
			}
			
			ok = true;
			if(ti.index + availUnits
				> pos + curRead.getAlloc()) ok = false;
			
			if(!ok) {
				int alloc;
				alloc = curRead.getAlloc();
				
				
				ok = false;
				if(pos + alloc + strat.readAheadAmount >= min) ok = true;
				if(pos > min) ok = false;
				
				if(ok) {
					alloc = curRead.getAlloc();
					//System.out.println("Alloc," + alloc);

					if(pos + alloc > min) {
						alloc = (int) (min - pos);
						curRead.setAlloc(alloc);
					}
					
					// advanced to the end of the buffer
					
					curRead.moveForward();
					pos += alloc;
					
					// advance again, to get to /min/

					alloc = (int) (min - pos);
					curRead.setAlloc(alloc);
					curRead.moveForward();
					pos = min;
					
					// configure alloc, with the right length

					curRead.setAlloc(len);

					// count advances
					trace3Count += 1;
				}
			}

			ok = true;
			if(ti.index + availUnits
				> pos + curRead.getAlloc()) ok = false;
			
			if(!ok) {
				//System.out.println("CurRestart," + savePos + "," + saveTiIndex + "," + saveMin + "," + saveLen);
				
				if(curRead.getIsCursorRunning())
					curRead.setIsCursorRunning(false);

				curRead.setStoragePointer(min);
				curRead.setIsCursorRunning(true);
				curRead.setAlloc(len);

				// count the number of cursor restarts
				trace5Count += 1;
				
				pos = min;
			}
		}
		
		
		utils.copyTextIndex(tiTemp, ti);
		utils.copyBufferIndex(biTemp, bi);
		
		int bufPos = (int) (ti.index - pos);
		
		c1 = 0;
		fullChar = 0;
		unitIndex = 0;
		unitCount = 1;
		isAtEnd = false;
		
		//if(streamPastIndex2 == null) {
		//	curRead.setAlloc(unitCount);
		//}
		
		//if(streamPastIndex2 != null) {
		//	if(unitCount > availUnits) unitCount = availUnits;
		//	curRead.setAlloc(unitCount);
		//}
				
		if(dat.encoding == CharacterEncoding.UTF_8)
		while(true) {
			if(unitIndex >= unitCount) isAtEnd = true;
			
			if(unitIndex == 0) {
				if(isAtEnd) {
					dat.state = CharReader3Data.STATE_END_OF_STREAM;
					return;
				}

				//c1 = be.buf[be.start + biTemp.zoomElementIndex];
				c1 = curRead.getValue8(bufPos + unitIndex);
				c1 = c1 & 0xFF;

				// look for the first 0 in the leading byte,
				// starting from the left most bit, going right
				unitCount = 0;
				i = 7;
				while(i > 0)
				{
					if(((c1 >> i) & 1) == 0)
						break;

					unitCount += 1;
					i -= 1;
				}

				if(unitCount == 0) {
					// This insicates a char one byte long

					// the algorithm gets it wrong so, correct it
					unitCount = 1;

					// the left most bit is zero, so
					// this character is one byte long

					fullChar = c1;

					dat.state = CharReader3Data.STATE_HAVE_CHAR;
					dat.resultChar = fullChar;
					dat.resultCharLength = unitCount;
					return;
				}

				if((unitCount == 1) || (unitCount == 7)) {
					// invalid character

					dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
						makeInvalidChar(prms.context, ti));
					
					dat.resultCharLength = 1;
					dat.state = CharReader3Data.STATE_HAVE_BAD_CHAR;
					return;
				}
				
				// unitCount is good, so get cursor ready

				//if(streamPastIndex2 == null) {
				//	curRead.setAlloc(unitCount);
				//}

				if(streamPastIndex2 != null) {
					if(unitCount > availUnits) unitCount = availUnits;
					//curRead.setAlloc(unitCount);
				}

				// commit to character, the data bits within the
				// first byte
				fullChar = c1 & utils.lowerBitsMask.aryPtr[8 - 1 - unitCount];

				unitIndex += 1;
				biTemp.zoomElementIndex += 1;
				tiTemp.index += 1;
				tiTemp.indexWithinLine += 1;
				continue;
			}

			// on n'th unit

			if(isAtEnd) {
				// invalid character

				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeInvalidChar(prms.context, ti));
				
				// update cursor alloc,
				// to cover the good bytes in the sequence
				//curRead.setAlloc(unitIndex);
				
				dat.resultCharLength = unitIndex;
				dat.state = CharReader3Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			// ready to read
			//cn = be.buf[be.start + biTemp.zoomElementIndex];
			cn = curRead.getValue8(bufPos + unitIndex);
			cn = cn & 0xFF;

			// highest bits must be b10 in order to
			// encode a correct continuation byte
			if((cn >> 6) != 2) {
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeInvalidChar(prms.context, ti));
				
				// update cursor alloc,
				// to cover the good bytes in the sequence
				//curRead.setAlloc(unitIndex);

				dat.resultCharLength = unitIndex;
				dat.state = CharReader3Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			// commit to character, the right 6 bits
			fullChar <<= 6;
			fullChar |= cn & utils.lowerBitsMask.aryPtr[6];

			unitIndex += 1;
			biTemp.zoomElementIndex += 1;
			tiTemp.index += 1;
			tiTemp.indexWithinLine += 1;

			if(unitIndex >= unitCount) {
				dat.resultChar = fullChar;
				dat.resultCharLength = unitCount;
				dat.state = CharReader3Data.STATE_HAVE_CHAR;
				return;
			}

			// read next byte
			continue;
		}
		
		if(dat.encoding == CharacterEncoding.UTF_16_LE)
		while(true) {
			if(unitIndex >= unitCount) isAtEnd = true;
			
			if(unitIndex == 0) {
				if(isAtEnd) {
					dat.state = CharReader3Data.STATE_END_OF_STREAM;
					return;
				}

				//c1 = utils.getCodeUnitLittleEndian(
				//	be.buf, be.start + biTemp.zoomElementIndex,
				//	dat.codeUnitSize);
				c1 = curRead.getValue16(bufPos + unitIndex);
				c1 = c1 & 0xFFFF;

				if((c1 >= 0xD800) && (c1 <= 0xDBFF)) {
					// is a 2 part character

					unitCount = 2;
					
					// unitCount is good, so get cursor ready

					//if(streamPastIndex2 == null) {
					//	curRead.setAlloc(unitCount);
					//}

					if(streamPastIndex2 != null) {
						if(unitCount > availUnits) unitCount = availUnits;
						//curRead.setAlloc(unitCount);
					}

					// read next unit
					unitIndex += 1;
					biTemp.zoomElementIndex += 1;
					tiTemp.index += 1;
					tiTemp.indexWithinLine += 1;
					continue;
				}

				if((c1 >= 0xDC00) && (c1 <= 0xDFFF)) {
					// invalid first unit

					dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, 
						makeInvalidChar(prms.context, ti));
					
					// update cursor alloc,
					// to cover the good Int16 in the sequence
					//curRead.setAlloc(unitCount);
				
					dat.resultCharLength = 1;
					dat.state = CharReader3Data.STATE_HAVE_BAD_CHAR;
					return;
				}

				fullChar = c1;

				dat.resultChar = fullChar;
				dat.resultCharLength = unitCount;
				dat.state = CharReader3Data.STATE_HAVE_CHAR;
				return;
			}

			// 2nd code unit

			if(isAtEnd) {
				// invalid character

				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, 
					makeInvalidChar(prms.context, ti));
				
				// update cursor alloc,
				// to cover the good Int16 in the sequence
				//curRead.setAlloc(unitIndex);
				
				dat.resultCharLength = unitIndex;
				dat.state = CharReader3Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			//cn = utils.getCodeUnitLittleEndian(
			//	be.buf, be.start + biTemp.zoomElementIndex,
			//	dat.codeUnitSize);
			cn = curRead.getValue16(bufPos + unitIndex);
			cn = cn & 0xFFFF;
			

			if((cn < 0xDC00) || (cn > 0xDFFF)) {
				// not a continuaton code unit

				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeInvalidChar(prms.context, ti));

				// update cursor alloc,
				// to cover the good Int16 in the sequence
				//curRead.setAlloc(unitIndex);
				
				dat.resultCharLength = unitIndex;
				dat.state = CharReader3Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			fullChar = ((c1 - 0xD800) << 10) + (cn - 0xDC00);

			dat.resultChar = fullChar;
			dat.resultCharLength = unitCount;
			dat.state = CharReader3Data.STATE_HAVE_CHAR;
			return;
		}

		if(dat.encoding == CharacterEncoding.UTF_32_LE)
		while(true) {
			if(unitIndex >= unitCount) isAtEnd = true;
			
			if(isAtEnd) {
				dat.state = CharReader3Data.STATE_END_OF_STREAM;
				return;
			}

			//c1 = utils.getCodeUnitLittleEndian(
			//	be.buf, be.start + biTemp.zoomElementIndex,
			//	dat.codeUnitSize);
			c1 = curRead.getValue32(bufPos + unitIndex);

			fullChar = c1;

			dat.state = CharReader3Data.STATE_HAVE_CHAR;
			dat.resultChar = fullChar;
			dat.resultCharLength = unitCount;
			return;
		}
		
		// unknown encoding
		throw makeUnknownCharEncoding(null);
	}
	
	public void readChar(TextIndex ti, BufferIndex bi) {
		Throwable ex;
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;		

		//CharReaderParams prms = dat.charReadParams;

		ex = null;

		try {
			readCharThrow(ti, bi);
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
		
		context = new TextIndex();
		utils.copyTextIndex(context, ti);

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
	
	private CommonError makeOutOfBounds(String msg) {
		CommonError e4;
		
		e4 = new CommonError();
		e4.id = unnamed.common.CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e4.msg = msg;
		return e4;
	}
}
