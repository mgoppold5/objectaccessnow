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
import unnamed.file.system.*;

public class CharReader2 implements CharReaderAccess {
	public CharReader2Data dat;
	public GeneralUtils utils;

	public long trace1Count;
	public long trace2Count;
	public long trace3Count;
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
		
		int bufferCount; // how many BufferElements in our BufferCombo
		BufferElement be; // current BufferElement
		boolean isAtEnd; // iff the TextIndex is at the end of the stream
		boolean startAgain; // start from scratch
		int unitIndex; // index into the char, in code units
		int unitCount; // how many code units this char has
		int c1; // first code unit
		int cn; // n'th code unit
		int i; // general purpose integer
		int fullChar; // a unicode code point
		int beUsed; // how much of the BufferElement is used
		
		CharReaderParams prms = dat.charReadParams;

		CharReaderParamsFile fileParams;
		ReadBuffer readBuf;
		ReadBufferPartial bufPart;
		TextIndex tiTemp;
		BufferIndex biTemp;
		ReadStrategy strat;
		
		TextIndex streamPastIndex2;
		
		readBuf = prms.readBuf;
		tiTemp = prms.tiTemp1;
		biTemp = prms.biTemp1;
		bufPart = null;
		fileParams = null;
		strat = null;
		
		if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_FILE) {
			fileParams = (CharReaderParamsFile) prms;
			strat = fileParams.strat;
			strat.readDirection = ReadDirection.DIRECTION_FORWARD;
			bufPart = (ReadBufferPartial) readBuf;
		}
		
		if(prms.streamStartIndex != null)
		if(ti.index < prms.streamStartIndex.index) {
			dat.state = CharReader2Data.STATE_END_OF_STREAM;
			return;
		}
		
		// 6 for the maximum number of code units, under UTF8.
		// set streamPastIndex2 if there is a possibility
		// that the stream end would interfere
		streamPastIndex2 = null;
		if(prms.streamPastIndex != null)
		if(ti.index + 6 > prms.streamPastIndex.index)
			streamPastIndex2 = prms.streamPastIndex;

		trace1Count += 1;
		
		if(bi.versionNumber != readBuf.versionNumber
			|| !bi.zoomValid) {

			getBufferIndexThrow(ti, bi);

			if(bi.versionNumber != readBuf.versionNumber
				|| !bi.zoomValid)
				throw makeUnknownError(null);
		}
		
		utils.copyTextIndex(tiTemp, ti);
		utils.copyBufferIndex(biTemp, bi);
		
		c1 = 0;
		fullChar = 0;
		unitIndex = 0;
		unitCount = 1;
		
		if(dat.encoding == CharacterEncoding.UTF_8)
		while(true) {
			startAgain = false;

			if(biTemp.versionNumber != readBuf.versionNumber
				|| !biTemp.zoomValid) {

				getBufferIndexThrow(tiTemp, biTemp);
				
				if(biTemp.versionNumber != readBuf.versionNumber
					|| !biTemp.zoomValid)
					throw makeUnknownError(null);
			}

			isAtEnd = false;

			bufferCount = readBuf.bufferCombo.bufferList.size();

			beUsed = 0;
			be = null;
			if(biTemp.zoomComboIndex < bufferCount) {
				be = (BufferElement) readBuf.bufferCombo.bufferList.get(
					biTemp.zoomComboIndex);
				beUsed = be.used;
			}

			while(biTemp.zoomElementIndex >= beUsed) {
				if(biTemp.zoomComboIndex + 1 >= bufferCount) {
					if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_MEMORY) {
						isAtEnd = true;
						break;
					}
					
					if(bufPart != null) {
						if(bufPart.isAtEnd) {
							isAtEnd = true;
							break;
						}

						// we are at end of buffer
						// need to trigger a read
						biTemp.versionNumber =
							ReadBufferPartial.VERSION_NUMBER_INVALID;
						startAgain = true;
						break;
					}
					
					throw makeInvalidEnum("Unknown ReadBuffer type");
				}

				biTemp.zoomElementIndex -= beUsed;
				biTemp.zoomComboIndex += 1;
				
				be = (BufferElement) readBuf.bufferCombo.bufferList.get(
					biTemp.zoomComboIndex);
				beUsed = be.used;
			}

			// outside the inner while, go to start of outer while
			if(startAgain) continue;

			if(streamPastIndex2 != null)
			if(tiTemp.index >= streamPastIndex2.index)
				isAtEnd = true;
			
			if(unitIndex == 0) {
				if(isAtEnd) {
					dat.state = CharReader2Data.STATE_END_OF_STREAM;
					return;
				}

				c1 = be.buf.aryPtr[be.start + biTemp.zoomElementIndex];
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

					dat.state = CharReader2Data.STATE_HAVE_CHAR;
					dat.resultChar = fullChar;
					dat.resultCharLength = unitCount;
					return;
				}

				if((unitCount == 1) || (unitCount == 7)) {
					// invalid character

					dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
						makeInvalidChar(prms.context, ti));
					
					dat.resultCharLength = 1;
					dat.state = CharReader2Data.STATE_HAVE_BAD_CHAR;
					return;
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
				
				dat.resultCharLength = unitIndex;
				dat.state = CharReader2Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			// ready to read
			cn = be.buf.aryPtr[be.start + biTemp.zoomElementIndex];
			cn = cn & 0xFF;

			// highest bits must be b10 in order to
			// encode a correct continuation byte
			if((cn >> 6) != 2) {
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeInvalidChar(prms.context, ti));

				dat.resultCharLength = unitIndex;
				dat.state = CharReader2Data.STATE_HAVE_BAD_CHAR;
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
				dat.state = CharReader2Data.STATE_HAVE_CHAR;
				return;
			}

			// read next byte
			continue;
		}
		
		if(dat.encoding == CharacterEncoding.UTF_16_LE)
		while(true) {
			startAgain = false;

			if(biTemp.versionNumber != readBuf.versionNumber
				|| !biTemp.zoomValid) {

				getBufferIndexThrow(tiTemp, biTemp);

				if(biTemp.versionNumber != readBuf.versionNumber
					|| !biTemp.zoomValid)
					throw makeUnknownError(null);
			}

			bufferCount = readBuf.bufferCombo.bufferList.size();

			isAtEnd = false;

			beUsed = 0;
			be = null;
			if(biTemp.zoomComboIndex < bufferCount) {
				be = (BufferElement) readBuf.bufferCombo.bufferList.get(
					biTemp.zoomComboIndex);
				beUsed = be.used;
			}

			while(biTemp.zoomElementIndex >= beUsed) {
				if(biTemp.zoomComboIndex + 1 >= bufferCount) {
					if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_MEMORY) {
						isAtEnd = true;
						break;
					}
					
					if(bufPart != null) {
						if(bufPart.isAtEnd) {
							isAtEnd = true;
							break;
						}

						// we are at end of buffer
						// need to trigger a read
						biTemp.versionNumber =
							ReadBufferPartial.VERSION_NUMBER_INVALID;
						startAgain = true;
						break;
					}
					
					throw makeInvalidEnum("Unknown ReadBuffer type");
				}

				biTemp.zoomElementIndex -= beUsed;
				biTemp.zoomComboIndex += 1;
				
				be = (BufferElement) readBuf.bufferCombo.bufferList.get(
					biTemp.zoomComboIndex);
				beUsed = be.used;
			}

			// outside the inner while, go to start of outer while
			if(startAgain) continue;

			if(streamPastIndex2 != null)
			if(tiTemp.index >= streamPastIndex2.index)
				isAtEnd = true;

			if(unitIndex == 0) {
				if(isAtEnd) {
					dat.state = CharReader2Data.STATE_END_OF_STREAM;
					return;
				}

				c1 = utils.getCodeUnitLittleEndian(
					be.buf, be.start + biTemp.zoomElementIndex,
					dat.codeUnitSize);

				if((c1 >= 0xD800) && (c1 <= 0xDBFF)) {
					// is a 2 part character

					unitCount = 2;

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
					
					dat.resultCharLength = 1;
					dat.state = CharReader2Data.STATE_HAVE_BAD_CHAR;
					return;
				}

				fullChar = c1;

				dat.resultChar = fullChar;
				dat.resultCharLength = unitCount;
				dat.state = CharReader2Data.STATE_HAVE_CHAR;
				return;
			}

			// 2nd code unit

			if(isAtEnd) {
				// invalid character

				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR, 
					makeInvalidChar(prms.context, ti));
				
				dat.resultCharLength = unitIndex;
				dat.state = CharReader2Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			cn = utils.getCodeUnitLittleEndian(
				be.buf, be.start + biTemp.zoomElementIndex,
				dat.codeUnitSize);

			if((cn < 0xDC00) || (cn > 0xDFFF)) {
				// not a continuaton code unit

				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeInvalidChar(prms.context, ti));
				
				dat.resultCharLength = unitIndex;
				dat.state = CharReader2Data.STATE_HAVE_BAD_CHAR;
				return;
			}

			fullChar = ((c1 - 0xD800) << 10) + (cn - 0xDC00);

			dat.resultChar = fullChar;
			dat.resultCharLength = unitCount;
			dat.state = CharReader2Data.STATE_HAVE_CHAR;
			return;
		}

		if(dat.encoding == CharacterEncoding.UTF_32_LE)
		while(true) {
			startAgain = false;

			if(biTemp.versionNumber != readBuf.versionNumber
				|| !biTemp.zoomValid) {

				getBufferIndexThrow(tiTemp, biTemp);

				if(biTemp.versionNumber != readBuf.versionNumber
					|| !biTemp.zoomValid)
					throw makeUnknownError(null);
			}

			isAtEnd = false;

			bufferCount = readBuf.bufferCombo.bufferList.size();

			beUsed = 0;
			be = null;
			if(biTemp.zoomComboIndex < bufferCount) {
				be = (BufferElement) readBuf.bufferCombo.bufferList.get(
					biTemp.zoomComboIndex);
				beUsed = be.used;
			}

			while(biTemp.zoomElementIndex >= beUsed) {
				if(biTemp.zoomComboIndex + 1 >= bufferCount) {
					if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_MEMORY) {
						isAtEnd = true;
						break;
					}
					
					if(bufPart != null) {
						if(bufPart.isAtEnd) {
							isAtEnd = true;
							break;
						}

						// we are at end of buffer
						// need to trigger a read
						biTemp.versionNumber =
							ReadBufferPartial.VERSION_NUMBER_INVALID;
						startAgain = true;
						break;
					}
					
					throw makeInvalidEnum("Unknown ReadBuffer type");
				}

				biTemp.zoomElementIndex -= beUsed;
				biTemp.zoomComboIndex += 1;
				
				be = (BufferElement) readBuf.bufferCombo.bufferList.get(
					biTemp.zoomComboIndex);
				beUsed = be.used;
			}

			// outside the inner while, go to start of outer while
			if(startAgain) continue;

			if(streamPastIndex2 != null)
			if(tiTemp.index >= streamPastIndex2.index)
				isAtEnd = true;
			
			if(isAtEnd) {
				dat.state = CharReader2Data.STATE_END_OF_STREAM;
				return;
			}

			c1 = utils.getCodeUnitLittleEndian(
				be.buf, be.start + biTemp.zoomElementIndex,
				dat.codeUnitSize);

			fullChar = c1;

			dat.state = CharReader2Data.STATE_HAVE_CHAR;
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

		CharReaderParams prms = dat.charReadParams;

		ex = null;

		try {
			readCharThrow(ti, bi);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) fireRuntimeError(ex);
		
		return;
	}
	
	public void bufferIndexCalcZoom(ReadBuffer x, BufferIndex bi) {
		int amountToMove;
		BufferElement be;
		int bufferCount;
		
		amountToMove = bi.index;
		bi.zoomComboIndex = 0;
		bi.zoomElementIndex = 0;
		
		if(amountToMove == 0) {
			bi.zoomValid = true;
			bi.versionNumber = x.versionNumber;
			return;
		}
		
		bufferCount = x.bufferCombo.bufferList.size();
		
		if(bufferCount == 0)
			throw new IndexOutOfBoundsException(
				"buffer is too small");
		
		be = (BufferElement) x.bufferCombo.bufferList.get(0);
		
		while(true) {
			if(amountToMove > be.used) {
				if(bi.zoomComboIndex + 1 >= bufferCount)
					throw makeIndexOutOfBoundsException(
						"buffer is too small");
				
				amountToMove -= be.used;
				bi.zoomComboIndex += 1;
				be = (BufferElement) x.bufferCombo.bufferList.get(
					bi.zoomComboIndex);
				continue;
			}
			
			if(amountToMove == be.used) {
				if(bi.zoomComboIndex + 1 >= bufferCount) {
					bi.zoomElementIndex = amountToMove;
					bi.zoomValid = true;
					bi.versionNumber = x.versionNumber;
					return;
				}
				
				amountToMove -= be.used;
				bi.zoomComboIndex += 1;
				bi.zoomElementIndex = 0;
				bi.zoomValid = true;
				bi.versionNumber = x.versionNumber;
				return;
			}
			
			bi.zoomElementIndex = amountToMove;
			bi.zoomValid = true;
			bi.versionNumber = x.versionNumber;
			return;
		}
		
		// unreachable
	}
	
	public void getBufferIndexThrow(
		TextIndex ti, BufferIndex bi) {
		
		long fileLength;
		long iBackward;
		long iForward;
		boolean read;
		ReadBuffer readBuf;
		long currentIndex;
		
		CharReaderParams prms = dat.charReadParams;
		
		CharReaderParamsFile fileParams;
		ReadStrategy strat;
		ReadBufferPartial bufPart;
		
		trace2Count += 1;
		
		read = false;
		readBuf = prms.readBuf;
		
		fileParams = null;
		strat = null;
		bufPart = null;
		currentIndex = ti.index;
				
		if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_FILE) {
			fileParams = (CharReaderParamsFile) prms;
			strat = fileParams.strat;
			bufPart = (ReadBufferPartial) readBuf;
		}
		
		if(bufPart != null) {
			if(!read)
			if(!bufPart.isAtBeginning)
			if(currentIndex < bufPart.startIndex) {
				read = true;
			}
			
			if(!read)
			if(!bufPart.isAtEnd)
			if(currentIndex >
					bufPart.startIndex + bufPart.bufferCombo.length) {
				read = true;
			}

			if(!read)
			if(!bufPart.isAtBeginning) {
				iBackward = 0;
				if(currentIndex > strat.minBehindAmount)
					iBackward = currentIndex - strat.minBehindAmount;

				if(iBackward < bufPart.startIndex)
					read = true;
			}

			if(!read)
			if(!bufPart.isAtEnd) {
				if(bufPart.startIndex + bufPart.bufferCombo.length
					< currentIndex + strat.minAheadAmount) {
					
					read = true;
				}
			}
		}
		
		if(!read) {
			if((currentIndex >= readBuf.startIndex)
				&& (currentIndex <=
				readBuf.startIndex + readBuf.bufferCombo.length)) {

				bi.index = (int)
					(currentIndex - readBuf.startIndex);
				bi.zoomValid = false;
				bufferIndexCalcZoom(readBuf, bi);
				return;
			}
		}
		
		if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_FILE) {
			readFileThrow(fileParams.fileDat, bufPart, strat,
				currentIndex, prms.streamStartIndex, prms.streamPastIndex);
		}
		
		if((currentIndex >= readBuf.startIndex)
			&& (currentIndex <=
			readBuf.startIndex + readBuf.bufferCombo.length)) {
			
			bi.index = (int)
				(currentIndex - readBuf.startIndex);
			bi.zoomValid = false;
			bufferIndexCalcZoom(readBuf, bi);
			return;
		}
		
		if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_MEMORY) {
			throw makeIndexOutOfBoundsException(
				"Attempt to read outside the range of the stream");
		}
		
		throw makeUnknownError(
			"read from source but could not get buffer index");
	}
	
	public void getBufferIndex(
		TextIndex ti, BufferIndex bi) {

		Throwable ex;
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;
		
		CharReaderParams prms = dat.charReadParams;
		
		ex = null;

		try {
			getBufferIndexThrow(ti, bi);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) fireRuntimeError(ex);
		
		return;
	}
	
	public void readFileThrow(FileRef fileDat,
		ReadBufferPartial x, ReadStrategy strat, 
		long currentIndex,
		StreamIndex streamStartIndex, StreamIndex streamPastIndex) {
		
		long fileLength;
		int extendForward;
		int extendBackward;
		long i1;
		long i2;
		int i3;
		BufferElement be;
		int amountDone;
		int space;
		
		long rangeStartIndex;
		long rangePastIndex;

		long spaceBehind;
		long spaceAhead;
		
		// if the buffer changed size/position
		boolean updated;
		
		// iff currentIndex moved outside the excerpt
		boolean movedOutside;
		
		// iff we should start a new buffer
		boolean reBuf;
		
		
		trace3Count += 1;
		
		updated = false;
		
		fileLength = fileDat.fs.getFileLength(fileDat.fileHandle);
		
		rangeStartIndex = 0;
		if(streamStartIndex != null)
		if(rangeStartIndex < streamStartIndex.index)
			rangeStartIndex = streamStartIndex.index;
		
		rangePastIndex = fileLength;
		if(streamPastIndex != null)
		if(rangePastIndex > streamPastIndex.index)
			rangePastIndex = streamPastIndex.index;

		if(currentIndex < rangeStartIndex || currentIndex > rangePastIndex)
			throw makeIndexOutOfBoundsException(
				"A request was made to read"
					+ " outside the bounds of the stream.");
		
		movedOutside = false;
		reBuf = false;
		extendForward = 0;
		extendBackward = 0;
		
		// check buf ranges
		
		if(!reBuf)
		if(x.startIndex < rangeStartIndex)
			reBuf = true;

		if(!reBuf)
		if(x.startIndex + x.bufferCombo.length > rangePastIndex)
			reBuf = true;
		
		if(!reBuf)
		if(currentIndex > x.startIndex + x.bufferCombo.length) {
			movedOutside = true;

			// amount past the buffer
			i1 = currentIndex - x.startIndex - x.bufferCombo.length;

			if(i1 <= strat.readBehindAmount) {
				// new buffer augments old buffer

				i2 = i1 + strat.readAheadAmount;
				i3 = (int) i2;
				if(i3 != i2)
					throw makeIOError("Read overflow");

				// extend forward
				extendForward = i3;

				if(strat.readBehindAmount > currentIndex - x.startIndex) {
					// should extend backward too

					extendBackward = strat.readBehindAmount
						- ((int)(currentIndex - x.startIndex));
				}
			} else {
				//abandon old buffer

				reBuf = true;
			}
		}

		if(!reBuf)
		if(currentIndex < x.startIndex) {
			movedOutside = true;

			// amount before the buffer
			i1 = x.startIndex - currentIndex;

			if(i1 <= strat.readAheadAmount) {
				// new buffer augments old buffer

				i2 = i1 + strat.readBehindAmount;

				i3 = (int) i2;
				if(i3 != i2)
					throw makeIOError("Read overflow");

				// extend backward
				extendBackward = i3;

				if(strat.readAheadAmount >
					x.startIndex - currentIndex + x.bufferCombo.length) {
					// should extend forward too

					extendForward = strat.readAheadAmount
						- ((int)(x.startIndex - currentIndex
							+ x.bufferCombo.length));
				}
			} else {
				// abandon old buffer

				reBuf = true;
			}
		}
			
		if(!reBuf)
		if(!movedOutside) {
			i1 = x.startIndex + x.bufferCombo.length - currentIndex;
			if(i1 < strat.readAheadAmount) {
				extendForward = strat.readAheadAmount - (int) i1;
			}
			
			if(strat.readDirection == ReadDirection.DIRECTION_FORWARD) {
				extendForward = strat.readAheadAmount;
			}
			
			i1 = currentIndex - x.startIndex;
			if(i1 < strat.readBehindAmount) {
				extendBackward = strat.readBehindAmount - (int) i1;
			}

			if(strat.readDirection == ReadDirection.DIRECTION_BACKWARD) {
				extendBackward = strat.readBehindAmount;
			}
		}
		
		if(reBuf) {
			//System.out.println("rebuf," + x.bufferCombo.bufferList.size());
			extendBackward = strat.readBehindAmount;
			extendForward = strat.readAheadAmount;
			
			Object[] bufList2 = x.bufferCombo.bufferList.toArray();
			
			int bufNum;
			int bufCount;
			
			bufNum = 0;
			bufCount = bufList2.length;
			while(bufNum < bufCount) {
				be = (BufferElement) bufList2[bufNum];
				
				x.bufferCombo.bufferList.remove(be);
				freeBufferElement(x, be);
				
				bufNum += 1;
			}
			
			x.bufferCombo.bufferList.clear();
			x.bufferCombo.length = 0;
			x.startIndex = currentIndex;
			
			updated = true;
		}
		
		// do not need reBuf and movedOutside anymore
		
		// trim backward
		
		spaceBehind = x.startIndex - rangeStartIndex;
		if(extendBackward > spaceBehind)
			extendBackward = (int) spaceBehind;
		
		// trim forward
		
		spaceAhead = rangePastIndex - (x.startIndex + x.bufferCombo.length);
		if(extendForward > spaceAhead)
			extendForward = (int) spaceAhead;
				
		if(x.bufferCombo.length == 0) {
			// a little optimization, making the read a completely
			// forward one if possible
			
			i1 = extendForward + extendBackward;
			i3 = (int) i1;
			if(i1 != i3)
				throw makeIOError("Read overflow");
			
			// make it forward
			x.startIndex -= extendBackward;
			extendForward = i3;
			extendBackward = 0;
		}
		
		i1 = x.bufferCombo.length + extendBackward + extendForward;
		i3 = (int) i1;
		if(i3 != i1)
			throw makeIOError("Read overflow");

		
		if(extendBackward > 0) {
			// Extend backwards
			// 

			amountDone = 0;
	
			// using a while and breaks for gotos
			while(true) {
				// fill up existing buffer
				//
				
				if(x.bufferCombo.bufferList.size() == 0)
					break;

				be = (BufferElement) x.bufferCombo.bufferList.get(0);

				if(be.used == 0) {
					// reposition the start to the end of the buffer
					be.start = be.capacity;
				}

				if(be.start == 0) break;

				i3 = extendBackward;
				if(be.start < i3) i3 = be.start;
				
				readFileDoRead(fileDat, x.startIndex - i3, be, be.start - i3, i3);
				
				// commit
				x.startIndex -= i3;
				be.start -= i3;
				x.bufferCombo.length += i3;
				amountDone += i3;

				// this is not a real loop
				break;
			}
			
			be = null;
			
			while(amountDone < extendBackward) {
				// make a new buffer
				be = allocBufferElement(x, strat.specificBufferSize);
				be.start = be.capacity;
				
				// add it
				x.bufferCombo.bufferList.addAt(0, be);
				//System.out.println("add," + x.bufferCombo.bufferList.size());
				
				i3 = extendBackward - amountDone;
				if(be.start < i3) i3 = be.start;
				
				readFileDoRead(fileDat, x.startIndex - i3, be, be.start - i3, i3);
				
				// commit
				x.startIndex -= i3;
				be.start -= i3;
				x.bufferCombo.length += i3;
				amountDone += i3;

				be = null;
			}
		}
		
		if(extendForward > 0) {
			// Extend forwards
			// 
			
			amountDone = 0;

			// using a while and breaks for gotos
			while(true) {
				// fill up existing buffer
				//
				
				i3 = x.bufferCombo.bufferList.size();
				if(i3 == 0)
					break;

				be = (BufferElement) x.bufferCombo.bufferList.get(i3 - 1);

				if(be.used == 0) {
					// reposition the start to the beginning of the buffer
					be.start = 0;
				}

				if(be.capacity <= be.start + be.used) break;

				space = be.capacity - be.start - be.used;

				i3 = extendForward;
				if(space < i3) i3 = space;
				
				readFileDoRead(fileDat, x.startIndex + x.bufferCombo.length,
					be, be.start + be.used, i3);
				
				// commit
				be.used += i3;
				x.bufferCombo.length += i3;
				amountDone += i3;

				// this is not a real loop
				break; 
			}

			be = null;
			
			while(amountDone < extendForward) {
				// make a new buffer
				
				be = allocBufferElement(x, strat.specificBufferSize);
				be.start = 0;
				
				// add it
				x.bufferCombo.bufferList.add(be);
				//System.out.println("add," + x.bufferCombo.bufferList.size());
				
				space = be.capacity;
				
				i3 = extendForward - amountDone;
				if(space < i3) i3 = space;
				
				readFileDoRead(fileDat, x.startIndex + x.bufferCombo.length,
					be, be.start + be.used, i3);
				
				// commit
				be.used += i3;
				x.bufferCombo.length += i3;
				amountDone += i3;

				be = null;
			}
		}
		
		if(extendBackward > 0 || extendForward > 0)
			updated = true;
		
		if(!updated) {
			updateMarkers(x, rangeStartIndex, rangePastIndex);
			return;
		}
		
		shrinkBuffer(x, strat, currentIndex);

		x.incrementVersion();
		updateMarkers(x, rangeStartIndex, rangePastIndex);
		return;
	}

	public void readFile(FileRef fileDat,
		ReadBufferPartial x, ReadStrategy strat,
		long currentIndex,
		StreamIndex streamStartIndex, StreamIndex streamPastIndex) {
		
		Throwable ex;

		if(dat.state == BaseModuleData.STATE_STUCK)
			return;

		ex = null;
		
		try {
			readFileThrow(fileDat, x, strat,
				currentIndex, streamStartIndex, streamPastIndex);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) fireRuntimeError(ex);
		
		return;
	}

	private void readFileDoRead(FileRef fileDat, long filePos,
		BufferElement be, int pos, int len) {
		
		long filePos2;
		int unitSize;
		
		unitSize = dat.codeUnitSize;
		
		fileDat.fs.setFilePointer(fileDat.fileHandle, filePos * unitSize);
		
		fileDat.fs.readFile(fileDat.fileHandle, be.buf,
			pos * unitSize, len * unitSize);
				
		filePos2 = fileDat.fs.getFilePointer(fileDat.fileHandle);
		
		if(filePos2 != (filePos + len) * unitSize)
			throw makeIOError("File pointer is wrong");
	}

	private void updateMarkers(ReadBufferPartial x,
		long rangeStartIndex, long rangePastIndex) {
		
		x.isAtBeginning = false;
		if(x.startIndex <= rangeStartIndex)
			x.isAtBeginning = true;

		x.isAtEnd = false;
		if(x.startIndex + x.bufferCombo.length >= rangePastIndex)
			x.isAtEnd = true;
	}
	
	private boolean sizeIsSmall(ReadStrategy strat,
		int specificBufferCount, int bufferLength) {
		int i3;
		
		if(specificBufferCount >= strat.idealSpecificBufferCount)
			return false;
		
		if(bufferLength >= strat.idealBufferSize)
			return false;
		
		return true;
	}
	
	private boolean shrinkBuffer(ReadBufferPartial x, ReadStrategy strat,
		long currentIndex) {
		
		boolean updated;
		boolean updatedThisRound;
		long i1;
		long i2;
		int i3;
		BufferElement be;
		
		updated = false;
		
		if(sizeIsSmall(strat,
			x.bufferCombo.bufferList.size(), x.bufferCombo.length))
			return updated;
		
		while(true) {
			updatedThisRound = false;
			
			// eliminate from begining once, then from end once,
			// if something was updated then repeat all over again
		
			while(true) {
				// Eliminate from beginning of the buffer
				//

				if(strat.readDirection == ReadDirection.DIRECTION_BACKWARD)
					// this means that the most recent read was growing
					// backward, so we must keep everything in this
					// direction
					break;

				if(strat.readBehindAmount >= currentIndex)
					break;

				i1 = currentIndex - strat.readBehindAmount;

				i3 = x.bufferCombo.bufferList.size();
				if(i3 == 0) break;

				be = (BufferElement) x.bufferCombo.bufferList.get(0);

				i2 = x.startIndex + be.used;
				if(i2 > i1) break;

				if(sizeIsSmall(strat, i3 - 1, x.bufferCombo.length - be.used))
					break;

				// commit
				x.bufferCombo.length -= be.used;
				x.startIndex += be.used;
				x.bufferCombo.bufferList.remove(be);
				//System.out.println("remove," + x.bufferCombo.bufferList.size());
				freeBufferElement(x, be);
				//x.incrementVersion();
				
				updatedThisRound = true;
				break;
			}

			while(true) {
				// Eliminate from end of the buffer
				//

				if(strat.readDirection == ReadDirection.DIRECTION_FORWARD)
					// this means that the most recent read was growing
					// forward, so we must keep everything in this
					// direction
					break;

				i1 = currentIndex + strat.readAheadAmount;

				if(i1 >= x.startIndex + x.bufferCombo.length)
					break;
				
				i3 = x.bufferCombo.bufferList.size();
				if(i3 == 0) break;

				be = (BufferElement) x.bufferCombo.bufferList.get(i3 - 1);

				i2 = x.startIndex + x.bufferCombo.length - be.used;
				if(i2 < i1) break;

				if(sizeIsSmall(strat, i3 - 1, x.bufferCombo.length - be.used))
					break;

				// commit
				x.bufferCombo.length -= be.used;
				x.bufferCombo.bufferList.remove(be);
				//System.out.println("remove," + x.bufferCombo.bufferList.size());
				freeBufferElement(x, be);
				//x.incrementVersion();

				updatedThisRound = true;
				break;
			}
			
			if(updatedThisRound) {
				updated = true;
				continue;
			}

			// didnt update anything, time to stop
			break;
		}
		
		return updated;
	}

	private BufferElement allocBufferElement(ReadBufferPartial x, int dataSize) {
		BufferElement be;
		boolean handled;
		
		be = makeBufferElement();
		be.used = 0;
		be.start = 0;
		
		handled = false;
		
		if(!handled)
		if(x.extraBufData1 != null)
		if(x.extraBufData1.length == dataSize * dat.codeUnitSize) {
			be.buf = x.extraBufData1;
			be.capacity = dataSize;
			x.extraBufData1 = null;
			handled = true;
		}
		
		if(!handled)
		if(x.extraBufData2 != null)
		if(x.extraBufData2.length == dataSize * dat.codeUnitSize) {
			be.buf = x.extraBufData2;
			be.capacity = dataSize;
			x.extraBufData2 = null;
			handled = true;
		}
		
		if(!handled) {
			//be.buf = new byte[dataSize * dat.codeUnitSize];
			be.buf = CommonUtils.makeInt8Array(
				dataSize * dat.codeUnitSize);
			be.capacity = dataSize;
			onFreshAlloc();
		}
				
		return be;
	}
	
	private void freeBufferElement(ReadBufferPartial x, BufferElement be) {
		CommonInt8Array bufData;
		boolean handled;
		
		bufData = be.buf;
		
		if(bufData == null)
			throw new NullPointerException();
				
		handled = false;
		
		if(!handled)
		if(x.extraBufData1 == null) {
			x.extraBufData1 = bufData;
			handled = true;
		}
		
		if(!handled)
		if(x.extraBufData2 == null) {
			x.extraBufData2 = bufData;
			handled = true;
		}
		
		if(!handled)
		if(x.extraBufData1.length != bufData.length) {
			x.extraBufData1 = bufData;
			handled = true;
			onPermanentFree();
		}
		
		if(!handled)
		if(x.extraBufData2.length != bufData.length) {
			x.extraBufData2 = bufData;
			handled = true;
			onPermanentFree();
		}
		
		if(!handled) {
			// forget bufData
			onPermanentFree();
		}

		be.buf = null;
		return;
	}

	private void onFreshAlloc() {
		freshAllocCount += 1;
	}
	
	private void onPermanentFree() {
		permFreeCount += 1;
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
	
	private BufferElement makeBufferElement() {
		BufferElement be;
		
		be = new BufferElement();
		be.init();
		return be;
	}
}
