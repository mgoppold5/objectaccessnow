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

package unnamed.common;

public class StorageBlockCursor implements StorageCursorAccess {
	private StorageRangeAccess r;
	private StorageRangeMetrics m;

	private short minBlockSize;
	private short growStepSize;
	private int maxGrowCount;
	
	private boolean haveInit;

	private boolean cursorRunning;
	private short accessType;
	private int alloc;
	
	private long cursorStart;
	private long cursorCommited;
	private long cursorStoredToMem;
	private long cursorPreLoaded;
	private long storagePointer;
	
	private CommonArrayList bufList;
	private BufferNode oldBuf;
	
	private int currentGrowNum;
	
	private BufferNode zeroBuf;
	
	private CommonInt32Array int32Masks;
	private CommonInt64Array int64Masks;
	
	// debug
	public long trace1Count;
	public long trace2Count;
	public long trace3Count;
	
	public void initRangeRef(StorageRangeAccess pR) {
		if(haveInit) throw makeIllegalState("already initialized");
		
		if(pR == null) {
			r = null;
			m = null;
			return;
		}
		
		if(!pR.getHaveInit())
			throw makeIllegalState("not initialized");
		
		r = pR;
		initRangeMetrics();
	}
	
	private void initRangeMetrics() {
		m = new StorageRangeMetrics();
		
		m.clientUnitSize = r.getClientUnitSize();
		m.memoryUnitSize = r.getMemoryUnitSize();
		m.strictAlignSize = r.getStrictAlignSize();
	}
	
	private void initPatchBuffers() {
		int mult;
		
		mult = 1 << (m.strictAlignSize - m.memoryUnitSize);

		zeroBuf = null;
		if(m.memoryUnitSize == 4) zeroBuf = createInt16Buf(mult);
		if(m.memoryUnitSize == 6) zeroBuf = createInt64Buf(mult);
		myZeroBuf(zeroBuf, m.memoryUnitSize);
	}
	
	private void initJunk() {
		int32Masks = BitMaskUtils.lowerBitsInit32((short) 32);
		int64Masks = BitMaskUtils.lowerBitsInit64((short) 64);

		bufList = makeArrayList();
	}
	
	public void initMinBlockSize(short size) {
		if(haveInit) makeIllegalState("already initialized");
		minBlockSize = size;
	}
	
	public void initGrowStepSize(short size) {
		if(haveInit) makeIllegalState("already initialized");
		growStepSize = size;
	}
	
	public void initMaxGrowCount(int count) {
		if(haveInit) makeIllegalState("already initialized");
		maxGrowCount = count;
	}
	
	public void init() {
		if(haveInit) throw makeIllegalState("already initialized");
		if(r == null) throw new NullPointerException();
		
		if(minBlockSize < m.strictAlignSize) minBlockSize = m.strictAlignSize;
		
		initPatchBuffers();
		initJunk();
		
		maxGrowCount += 1;
		
		haveInit = true;
	}
	
	public boolean getHaveInit() {return haveInit;}
	
	public short getClientUnitSize() {return m.clientUnitSize;}
	public short getStrictAlignSize() {return m.strictAlignSize;}
	
	public long getLength() {return r.getLength();}
	
	public void setLength(long len) {
		if(cursorRunning) throw makeIllegalState(null);
		r.setLength(len);
	}

	public long getSuggestLength() {return r.getSuggestLength();}
	public void setSuggestLength(long len) {r.setSuggestLength(len);}
	
	public int getAccessType() {return accessType;}

	public void setAccessType(short pAccessType) {
		if(cursorRunning) throw makeIllegalState(null);
		accessType = pAccessType;
	}

	public boolean getIsCursorRunning() {return cursorRunning;}
	
	public void setIsCursorRunning(boolean running) {
		boolean ok;
		
		if(!cursorRunning && !running) return;
		if(cursorRunning && running) throw makeIllegalState(null);
		
		if(!cursorRunning && running) {
			// startup
			
			// count the number of startups
			trace2Count += 1;
			
			
			// RANGE CHECKS
			//
			
			long len1 = r.getLength();

			if(accessType == StorageAccessTypes.TYPE_READ
				|| accessType == StorageAccessTypes.TYPE_WRITE_REPLACE) {

				if(len1 < storagePointer)
					throw makeOutOfBounds(null);
			}

			if(accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {
				ok = true;

				long minLen = storagePointer;

				minLen = PowerUnitUtils.convertInt64(
					minLen,
					m.clientUnitSize, m.strictAlignSize,
					false);
				minLen = PowerUnitUtils.convertInt64(
					minLen,
					m.strictAlignSize, m.clientUnitSize,
					false);

				if(len1 < minLen) ok = false;

				long maxLen = storagePointer;

				maxLen = PowerUnitUtils.convertInt64(
					maxLen,
					m.clientUnitSize, m.strictAlignSize,
					true);
				maxLen = PowerUnitUtils.convertInt64(
					maxLen,
					m.strictAlignSize, m.clientUnitSize,
					true);

				if(len1 > maxLen) ok = false;

				if(!ok) {
					throw makeOutOfBounds(null);
				}
			}
			
			
			// GET CURSOR READY
			//

			currentGrowNum = 0;
			
			cursorStart = storagePointer;
			cursorCommited = storagePointer;
			cursorStoredToMem = storagePointer;
			cursorPreLoaded = storagePointer;
			
			cursorRunning = true;
			alloc = 0;
			return;
		}

		if(cursorRunning && !running) {
			// shutdown
			
			commitFinal();
			bufList.clear();
			clearOldBuf();
			currentGrowNum = 0;

			storagePointer = cursorCommited;
			alloc = 0;
			cursorRunning = false;
			return;
		}
		
		throw makeIllegalState(null);
	}
	
	public long getStoragePointer() {
		if(cursorRunning) return cursorStoredToMem;
		return storagePointer;
	}
	
	public void setStoragePointer(long pointer) {
		if(cursorRunning) throw makeIllegalState(null);
		storagePointer = pointer;
	}
	
	
	// Helper functions for getAlloc/setAlloc/moveForward
	//

	private void myZeroAlignUnits(
		BufferNode buf, int start, int len) {
		
		int alignToMemMult = 1 << (m.strictAlignSize - m.memoryUnitSize);
		
		int i;
		int i64;
		int i16;
		
		if(m.memoryUnitSize == 4) {
			short zero = 0;
			CommonInt16Array arr = (CommonInt16Array) buf.theObject;
			
			i = 0;
			while(i < len) {
				int start2 = (start + i) * alignToMemMult;
				
				i16 = 0;
				while(i16 < alignToMemMult) {
					arr.aryPtr[start2 + i16] = zero;
					
					i16 += 1;
				}
				
				i += 1;
			}

			return;
		}

		if(m.memoryUnitSize == 6) {
			long zero = 0;
			CommonInt64Array arr = (CommonInt64Array) buf.theObject;
			
			i = 0;
			while(i < len) {
				int start2 = (start + i) * alignToMemMult;
				
				i64 = 0;
				while(i64 < alignToMemMult) {
					arr.aryPtr[start2 + i64] = zero;
					
					i64 += 1;
				}
				
				i += 1;
			}

			return;
		}
		
		throw makeIllegalState(null);
	}

	private void myZeroClientUnits(
		BufferNode buf, int start, int len) {
		
		int i;
		int sectionLen;
		
		int leadFract;
		int trailFract;
		
		int len2;

		int patchPos1;
		int patchPos2;
		int patchPos3;

		
		int alignToClientMult = 1 << (m.strictAlignSize - m.clientUnitSize);
		int clientToBitMult = 1 << (m.clientUnitSize - 0);
		
		leadFract = start % alignToClientMult;

		// length to end from aligned position
		len2 = leadFract + len;
		
		// position in client units, inside align unit
		trailFract = len2 % alignToClientMult;

		int start2 = start;
		
		start2 = PowerUnitUtils.convertInt32(
			start2,
			m.clientUnitSize, m.strictAlignSize,
			false);
		
		len2 = PowerUnitUtils.convertInt32(
			len2,
			m.clientUnitSize, m.strictAlignSize,
			false);

		boolean enablePatch1;
		boolean enablePatch2;
		boolean enableBlockLoadAndStore;

		//BufferNode zeroBuf = myGetZeroBuf(fh);
		
		patchPos1 = 0;
		patchPos2 = 0;
		patchPos3 = 0;
		
		i = 0;
		while(true) {
			enablePatch1 = (i == 0 && leadFract > 0);
			enablePatch2 = false;
			enableBlockLoadAndStore = false;
			
			sectionLen = 0;
			
			if(i < len2) {
				enableBlockLoadAndStore = true;
				sectionLen = len2 - i;

				if(enablePatch1) {
					enableBlockLoadAndStore = false;
					
					if(sectionLen > 1)
						sectionLen = 1;
				}
			}
			
			if(i >= len2) {
				if(i > len2 || trailFract == 0) break;
				
				sectionLen = 1;

				enablePatch2 = true;
			}

			if(enablePatch1 || enablePatch2) {
				patchPos3 = alignToClientMult * clientToBitMult;
				
				if(enablePatch1 && enablePatch2) {
					patchPos1 = leadFract * clientToBitMult;
					patchPos2 = trailFract * clientToBitMult;
				}

				if(enablePatch1 && !enablePatch2) {
					patchPos1 = leadFract * clientToBitMult;
					patchPos2 = alignToClientMult * clientToBitMult;
				}

				if(!enablePatch1 && enablePatch2) {
					patchPos1 = 0;
					patchPos2 = trailFract * clientToBitMult;
				}
			}
			
			if(sectionLen == 0)
				throw makeIllegalState(null);

			if(enableBlockLoadAndStore) {
				myZeroAlignUnits(buf, start2 + i, sectionLen);
			}

			if(enablePatch1 || enablePatch2) {
				BufferFastCopyUtils.copyPartial(
					zeroBuf, m.memoryUnitSize, 0,
					buf, m.memoryUnitSize, start2 + i,
					m.strictAlignSize,
					patchPos1, patchPos2 - patchPos1,
					int32Masks, int64Masks);
			}
			
			i += sectionLen;
		}
		
		return;
	}
	
	private void saveOldBuf(BufferNode buf) {
		if(oldBuf != null)
		if(buf.growNum <= oldBuf.growNum) return;

		oldBuf = buf;
	}

	private void clearOldBuf() {
		oldBuf = null;
	}
	
	private int getMiddleBufFract() {
		int alignToClientMult = 1 << (m.strictAlignSize - m.clientUnitSize);
		
		int fract = (int) (cursorCommited % alignToClientMult);
		return fract;
	}

	private long getCommited() {
		return cursorCommited - cursorStart;
	}
	
	private int getUsed() {
		int used = (int) (cursorStoredToMem - cursorCommited);
		return used;
	}
	
	private int getPreLoaded() {
		int avail = (int) (cursorPreLoaded - cursorStoredToMem);
		return avail;
	}
	
	private int calcGrowNum(int pAlloc) {
		int i;
		int count;
		int theSize;
		int blockLen2;
		
		i = 0;
		count = maxGrowCount;
		while(i < count) {
			theSize = minBlockSize + i * growStepSize;
			
			if(theSize < m.clientUnitSize) {
				i += 1;
				continue;
			}
			
			theSize -= m.clientUnitSize;
			
			blockLen2 = 1 << theSize;
			
			if(pAlloc > blockLen2) {
				i += 1;
				continue;
			}
			
			// found good size
			return i;
		}
		
		throw makeIllegalState(null);
	}
	
	private int calcGrowNum2(int pAlloc) {
		int growNum = calcGrowNum(pAlloc);
		long commitLen = getCommited();
		
		while(true) {
			int blockLen = calcBlockClientLen(growNum);
			
			if(blockLen < commitLen) {
				if(growNum + 1 < maxGrowCount) {
					growNum += 1;
					continue;
				}
			}
			
			break;
		}
		
		return growNum;
	}

	private int calcGrowNum3(int pAlloc) {
		int growNum = calcGrowNum2(pAlloc);
		
		if(growNum < currentGrowNum) growNum = currentGrowNum;
		return growNum;
	}
	
	private int calcBlockClientLen(int growNum) {
		int theSize = minBlockSize + growNum * growStepSize;
		
		if(theSize < m.clientUnitSize)
			throw makeIllegalState(null);
		
		int len = 1 << (theSize - m.clientUnitSize);
		return len;
	}
	
	private int calcBlockAlignLen(int growNum) {
		int theSize = minBlockSize + growNum * growStepSize;
		
		if(theSize < m.strictAlignSize)
			throw makeIllegalState(null);
		
		int len = 1 << (theSize - m.strictAlignSize);
		return len;
	}
	
	private BufferNode wrapBuf(Object o, int growNum) {
		BufferNode buf = new BufferNode();
		buf.theObject = o;
		buf.growNum = growNum;
		return buf;
	}
	
	private Object makeBufArray(int growNum) {
		int theSize = minBlockSize + growNum * growStepSize;
		
		if(theSize < m.memoryUnitSize)
			throw makeIllegalState(null);
		
		int len = 1 << (theSize - m.memoryUnitSize);
		
		if(m.memoryUnitSize == 4) {
			CommonInt16Array arr = makeInt16Array(len);
			return arr;
		}

		if(m.memoryUnitSize == 6) {
			CommonInt64Array arr = makeInt64Array(len);
			return arr;
		}
		
		throw makeIllegalState(null);
	}
	
	private void commit() {
		if(bufList.size() == 0) return;
		
		BufferNode middleBuf = (BufferNode) bufList.get(0);
		int middleBlockLen = calcBlockClientLen(middleBuf.growNum);
		
		int fract = getMiddleBufFract();
		int used = getUsed();
		
		if(fract + used >= middleBlockLen) {
			if(accessType == StorageAccessTypes.TYPE_WRITE_REPLACE
				|| accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {

				r.access(
					accessType, cursorCommited,
					middleBuf, fract, middleBlockLen - fract);
			}
			
			cursorCommited += middleBlockLen - fract;
			saveOldBuf(middleBuf);
			bufList.removeAt(0);
			return;
		}
	}

	private void commitFinal() {
		if(bufList.size() == 0) return;
		
		BufferNode middleBuf = (BufferNode) bufList.get(0);
		//int middleBlockLen = calcBlockClientLen(middleBuf.growNum);

		int used = getUsed();
		int fract = getMiddleBufFract();
		
		if(accessType == StorageAccessTypes.TYPE_WRITE_REPLACE
			|| accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {

			r.access(
				accessType, cursorCommited,
				middleBuf, fract, used);
		}

		cursorCommited += used;
		saveOldBuf(middleBuf);
		bufList.removeAt(0);
		return;
	}
	

	// getAlloc/setAlloc/moveForward
	//
	
	public int getAlloc() {return alloc;}
	
	public void setAlloc(int pAlloc) {
		int growNum;
		int bufLen;
		int fract;
		int used;
		int preLoadLen;
		int bufPos;
		boolean foundLimit;
		
		BufferNode newBuf;
		BufferNode currentBuf;
		BufferNode otherBuf;
		BufferNode replaceBuf;
		
		int i;
		int i2;
		int bufCount;
		
		if(!cursorRunning) throw makeIllegalState(null);

		if(alloc == 0 && pAlloc == 0) return;
		if(pAlloc == alloc) return;
		
		if(pAlloc > alloc) {
			preLoadLen = getPreLoaded();

			if(pAlloc <= preLoadLen) {
				alloc = pAlloc;
				return;
			}
			
			i = 0;
			bufCount = bufList.size();
			foundLimit = false;
			
			fract = getMiddleBufFract();
			used = getUsed();
			preLoadLen = getPreLoaded();
			
			while(true) {
				if(foundLimit) throw makeOutOfBounds(null);
				
				if(i >= bufCount) {
					if(i >= 2) throw makeOutOfBounds(null);
					
					growNum = calcGrowNum3(pAlloc);
					
					if(oldBuf != null)
					if(growNum == oldBuf.growNum) {
						bufList.add(oldBuf);
						bufCount = bufList.size();
						clearOldBuf();

						currentGrowNum = growNum;
						continue;
					}
					
					newBuf = wrapBuf(makeBufArray(growNum), growNum);
					bufList.add(newBuf);
					bufCount = bufList.size();
					
					// count number of array allocations
					trace1Count += 1;
					
					currentGrowNum = growNum;
					continue;
				}
				
				currentBuf = (BufferNode) bufList.get(i);
				bufLen = calcBlockClientLen(currentBuf.growNum);
				
				bufPos = fract + used + preLoadLen;
				
				i2 = 0;
				while(i2 < i) {
					otherBuf = (BufferNode) bufList.get(i2);
					int otherLen = calcBlockClientLen(otherBuf.growNum);
					
					if(bufPos < otherLen) throw makeIllegalState(null);
					
					bufPos -= otherLen;
					i2 += 1;
				}
				
				// bufPos is now calculated
				
				if(bufPos < bufLen) {
					// TRY TO PRE LOAD INTO CURRENT BUFFER
					//
					
					int avail = bufLen - bufPos;
					
					if(accessType == StorageAccessTypes.TYPE_READ
						|| accessType == StorageAccessTypes.TYPE_WRITE_REPLACE) {

						long rangeLen = r.getLength();
						long availToEnd = 0;

						if(cursorPreLoaded < rangeLen)
							availToEnd = rangeLen - cursorPreLoaded;

						if(avail > availToEnd) {
							avail = (int) availToEnd;
							foundLimit = true;
						}
					}
					
					if(avail == 0) throw makeOutOfBounds(null);

					if(accessType == StorageAccessTypes.TYPE_READ) {
						r.access(
							accessType, cursorPreLoaded,
							currentBuf, bufPos, avail);
					}

					if(accessType == StorageAccessTypes.TYPE_WRITE_REPLACE
						|| accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {

						myZeroClientUnits(
							currentBuf, bufPos, avail);
					}

					cursorPreLoaded += avail;

					preLoadLen = getPreLoaded();

					if(pAlloc <= preLoadLen) {
						alloc = pAlloc;
						return;
					}
					
					continue;
				}
				
				if(i + 1 >= bufCount) {
					// TRY TO MAKE TAIL BUFFER LARGER
					//
					
					growNum = calcGrowNum3(pAlloc);
					
					if(growNum <= currentBuf.growNum) {
						// ALREADY LARGE
						
						i += 1;
						continue;
					}
					
					replaceBuf = null;
					if(oldBuf != null)
					if(oldBuf.growNum >= growNum) {
						replaceBuf = oldBuf;
						clearOldBuf();
					}
					
					if(replaceBuf == null) {
						replaceBuf = wrapBuf(makeBufArray(growNum), growNum);

						// count number of array allocations
						trace1Count += 1;
					}
					
					int alignLen = calcBlockAlignLen(currentBuf.growNum);
					
					BufferFastCopyUtils.copyStrict(
						currentBuf, m.memoryUnitSize, 0,
						replaceBuf, m.memoryUnitSize, 0,
						m.strictAlignSize, alignLen,
						int32Masks);
					
					// do replacement, and continue
					bufList.set(i, replaceBuf);
					saveOldBuf(currentBuf);
					continue;
				}
				
				// try next buffer
				i += 1;
				continue;
			}
			
			// UNREACHABLE
		}

		if(pAlloc < alloc) {
			if(accessType == StorageAccessTypes.TYPE_WRITE_REPLACE
				|| accessType == StorageAccessTypes.TYPE_WRITE_APPEND) {
				
				i = 0;
				bufCount = bufList.size();
				foundLimit = false;

				fract = getMiddleBufFract();
				used = getUsed();
				
				int shrinkLen = alloc - pAlloc;
				
				bufPos = fract + used + pAlloc;

				while(i < bufCount) {
					currentBuf = (BufferNode) bufList.get(i);
					bufLen = calcBlockClientLen(currentBuf.growNum);

					if(bufPos >= bufLen) {
						bufPos -= bufLen;
						i += 1;
						continue;
					}
										
					if(bufPos + shrinkLen > bufLen) {
						int shrinkLen2 = bufLen - bufPos;
						
						myZeroClientUnits(
							currentBuf, bufPos, shrinkLen2);
						
						bufPos = 0;
						shrinkLen -= shrinkLen2;
						i += 1;
						continue;
					}
					
					myZeroClientUnits(
						currentBuf, bufPos, shrinkLen);
					shrinkLen = 0;
					break;
				}
				
				if(shrinkLen > 0) throw makeIllegalState(null);
				
				alloc = pAlloc;
				return;
			}
			
			if(accessType == StorageAccessTypes.TYPE_READ) {
				// norhing special to do
				
				alloc = pAlloc;
				return;
			}

			throw makeIllegalState(null);
		}
			
		throw makeIllegalState(null);
	}
	
	public void moveForward() {
		if(alloc == 0) return;

		int used = getUsed();
		int fract = getMiddleBufFract();
		
		int bufCount = bufList.size();
		if(bufCount == 0) throw makeIllegalState(null);
		
		BufferNode middleBuf = (BufferNode) bufList.get(0);
		int bufLen = calcBlockClientLen(middleBuf.growNum);

		if(fract + used + alloc < bufLen) {
			cursorStoredToMem += alloc;
			alloc = 0;
			return;
		}
		
		cursorStoredToMem += alloc;
		alloc = 0;
		commit();
		return;
	}

	public byte getValue8(int index) {return (byte) getValue32(index);}

	public void setValue8(int index, byte value) {
		int v2 = value;
		v2 &= 0xFF;
		setValue32(index, v2);
	}
	
	public short getValue16(int index) {return (short) getValue32(index);}

	public void setValue16(int index, short value) {
		int v2 = value;
		v2 &= 0xFFFF;
		setValue32(index, v2);
	}
	
	public int getValue32(int index) {
		if(index >= alloc) throw makeOutOfBounds(null);
		
		int i;
		int bufCount;
		int accum;
		int accum1;
		long accum2;
		boolean ok;
		
		BufferNode buf;
		int bufLen;
		int index2;
		
		buf = null;
		index2 = 0;
		
		int used = getUsed();
		int fract = getMiddleBufFract();
		
		index2 = fract + used + index;
		
		ok = false;
		bufCount = bufList.size();
		i = 0;
		while(i < bufCount) {
			buf = (BufferNode) bufList.get(i);
			bufLen = calcBlockClientLen(buf.growNum);
			
			if(index2 < bufLen) {
				ok = true;
				break;
			}
			
			index2 -= bufLen;
			i += 1;
			continue;
		}
		
		if(!ok) throw makeOutOfBounds(null);
		
		if(m.memoryUnitSize == 4) {
			CommonInt16Array arr = (CommonInt16Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				i = 0;
				accum = 0;
				while(i < 2) {
					accum1 = arr.aryPtr[index2 * clientToMemMult + i];
					accum1 &= 0xFFFF;
					
					accum |= accum1 << (i * 16);
					
					i += 1;
				}
				
				return accum;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 16 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];
				
				accum = arr.aryPtr[index3];
				accum &= 0xFFFF;
				accum >>= (maskLen * index4);
				accum &= mask;
				return accum;
			}
		}
		
		if(m.memoryUnitSize == 6) {
			CommonInt64Array arr = (CommonInt64Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				accum = (int) arr.aryPtr[index2 * clientToMemMult];
				return accum;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 64 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];
				
				accum2 = arr.aryPtr[index3];
				accum2 >>= (maskLen * index4);
				accum2 &= mask;
				return (int) accum2;
			}
		}

		throw makeIllegalState(null);
	}
	
	public void setValue32(int index, int value) {
		if(index >= alloc) throw makeOutOfBounds(null);
		
		int i;
		int bufCount;
		int accum;
		int accum1;
		long accum2;
		boolean ok;
		
		BufferNode buf;
		int bufLen;
		int index2;
		
		buf = null;
		index2 = 0;
		
		int used = getUsed();
		int fract = getMiddleBufFract();
		
		index2 = fract + used + index;
		
		ok = false;
		bufCount = bufList.size();
		i = 0;
		while(i < bufCount) {
			buf = (BufferNode) bufList.get(i);
			bufLen = calcBlockClientLen(buf.growNum);
			
			if(index2 < bufLen) {
				ok = true;
				break;
			}
			
			index2 -= bufLen;
			i += 1;
			continue;
		}
		
		if(!ok) throw makeOutOfBounds(null);
		
		if(m.memoryUnitSize == 4) {
			CommonInt16Array arr = (CommonInt16Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				int max1 = 2;
				if(max1 > clientToMemMult) max1 = clientToMemMult;
				
				i = 0;
				accum = 0;
				while(i < max1) {
					accum = value >> (i * 16);
					arr.aryPtr[index2 * clientToMemMult + i] = (short) accum;
					i += 1;
				}
				
				while(i < clientToMemMult) {
					arr.aryPtr[index2 * clientToMemMult + i] = 0;
					i += 1;
				}
				
				return;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 16 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];
				
				accum = value;
				accum &= mask;
				
				arr.aryPtr[index3] &= ~(mask << (maskLen * index4));
				arr.aryPtr[index3] |= (accum << (maskLen * index4));
				return;
			}
		}
		
		if(m.memoryUnitSize == 6) {
			CommonInt64Array arr = (CommonInt64Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				i = 0;
				accum = value;
				arr.aryPtr[index2 * clientToMemMult + i] = accum;
				i += 1;
				
				while(i < clientToMemMult) {
					arr.aryPtr[index2 * clientToMemMult + i] = 0;
					i += 1;
				}
				
				return;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 64 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];

				accum = value;
				accum &= mask;
				
				arr.aryPtr[index3] &= ~(mask << (maskLen * index4));
				arr.aryPtr[index3] &= (accum << (maskLen * index4));
				return;
			}
		}

		throw makeIllegalState(null);
	}

	public long getValue64(int index) {
		if(index >= alloc) throw makeOutOfBounds(null);
		
		int i;
		int bufCount;
		long accum;
		long accum1;
		long accum2;
		boolean ok;
		
		BufferNode buf;
		int bufLen;
		int index2;
		
		buf = null;
		index2 = 0;
		
		int used = getUsed();
		int fract = getMiddleBufFract();
		
		index2 = fract + used + index;
		
		ok = false;
		bufCount = bufList.size();
		i = 0;
		while(i < bufCount) {
			buf = (BufferNode) bufList.get(i);
			bufLen = calcBlockClientLen(buf.growNum);
			
			if(index2 < bufLen) {
				ok = true;
				break;
			}
			
			index2 -= bufLen;
			i += 1;
			continue;
		}
		
		if(!ok) throw makeOutOfBounds(null);

		if(m.memoryUnitSize == 4) {
			CommonInt16Array arr = (CommonInt16Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				i = 0;
				accum = 0;
				while(i < 4) {
					accum1 = arr.aryPtr[index2 * clientToMemMult + i];
					accum1 &= 0xFFFF;
					
					accum |= accum1 << (i * 16);
					
					i += 1;
				}
				
				return accum;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 16 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];
				
				accum = arr.aryPtr[index3];
				accum &= 0xFFFF;
				accum >>= (maskLen * index4);
				accum &= mask;
				return accum;
			}
		}
		
		if(m.memoryUnitSize == 6) {
			CommonInt64Array arr = (CommonInt64Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				accum = arr.aryPtr[index2 * clientToMemMult];
				return accum;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 64 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];
				
				accum2 = arr.aryPtr[index3];
				accum2 >>= (maskLen * index4);
				accum2 &= mask;
				return accum2;
			}
		}

		throw makeIllegalState(null);
	}
	
	public void setValue64(int index, long value) {
		if(index >= alloc) throw makeOutOfBounds(null);
		
		int i;
		int bufCount;
		long accum;
		long accum1;
		long accum2;
		boolean ok;
		
		BufferNode buf;
		int bufLen;
		int index2;
		
		int used = getUsed();
		int fract = getMiddleBufFract();
		
		index2 = fract + used + index;
		
		buf = null;
		ok = false;
		bufCount = bufList.size();
		i = 0;
		while(i < bufCount) {
			buf = (BufferNode) bufList.get(i);
			bufLen = calcBlockClientLen(buf.growNum);
			
			if(index2 < bufLen) {
				ok = true;
				break;
			}
			
			index2 -= bufLen;
			i += 1;
			continue;
		}
		
		if(!ok) throw makeOutOfBounds(null);

		if(m.memoryUnitSize == 4) {
			CommonInt16Array arr = (CommonInt16Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				int max1 = 4;
				if(max1 > clientToMemMult) max1 = clientToMemMult;
				
				i = 0;
				accum = 0;
				while(i < max1) {
					accum = value >> (i * 16);
					arr.aryPtr[index2 * clientToMemMult + i] = (short) accum;
					i += 1;
				}
				
				while(i < clientToMemMult) {
					arr.aryPtr[index2 * clientToMemMult + i] = 0;
					i += 1;
				}
				
				return;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 16 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];
				
				accum = value;
				accum &= mask;
				
				arr.aryPtr[index3] &= ~(mask << (maskLen * index4));
				arr.aryPtr[index3] |= (accum << (maskLen * index4));
				return;
			}
		}
		
		if(m.memoryUnitSize == 6) {
			CommonInt64Array arr = (CommonInt64Array) buf.theObject;

			if(m.clientUnitSize >= m.memoryUnitSize) {
				int clientToMemMult = 1 << (m.clientUnitSize - m.memoryUnitSize);

				i = 0;
				accum = value;
				arr.aryPtr[index2 * clientToMemMult + i] = accum;
				i += 1;
				
				while(i < clientToMemMult) {
					arr.aryPtr[index2 * clientToMemMult + i] = 0;
					i += 1;
				}
				
				return;
			}

			if(m.clientUnitSize < m.memoryUnitSize) {
				int memToClientMult = 1 << (m.memoryUnitSize - m.clientUnitSize);
				
				int index3 = index2 / memToClientMult;
				int index4 = index2 % memToClientMult;
				
				int maskLen = 64 / memToClientMult;
				int mask = int32Masks.aryPtr[maskLen];

				accum = value;
				accum &= mask;
				
				arr.aryPtr[index3] &= ~(mask << (maskLen * index4));
				arr.aryPtr[index3] &= (accum << (maskLen * index4));
				return;
			}
		}

		throw makeIllegalState(null);
	}
	
	private void myZeroBuf(BufferNode buf,
		short memoryUnitSize) {
		
		Object obj = buf.theObject;

		boolean ok;
		int i;
		int len;
		
		ok = false;
		
		if(memoryUnitSize == 3) {
			CommonIntArrayUtils.zero8((CommonInt8Array) obj);
			ok = true;
		}
		
		if(memoryUnitSize == 4) {
			CommonIntArrayUtils.zero16((CommonInt16Array) obj);
			ok = true;
		}

		if(memoryUnitSize == 5) {
			CommonIntArrayUtils.zero32((CommonInt32Array) obj);
			ok = true;
		}

		if(memoryUnitSize == 6) {
			CommonIntArrayUtils.zero64((CommonInt64Array) obj);
			ok = true;
		}
		
		if(!ok) throw makeIllegalState(null);
		return;
	}

	
	/*
	public void myPrintJunk() {
		int i;
		BufferNode buf = middleBuf;
		short[] arr = (short[]) buf.theObject;
		
		System.out.println("(");
		
		i = 0;
		while(i < 4) {
			myPrintThis(arr[i]);
			i += 1;
		}
		
		System.out.println(")");
	}

	public void myPrintThis(short a) {
		int b;
		
		b = a & 0xFF;
		
		if(b >= 'a' && b <= 'z') System.out.println((char) b);
		else System.out.println(b);
		
		b = (a >> 8) & 0xFF;
		
		if(b >= 'a' && b <= 'z') System.out.println((char) b);
		else System.out.println(b);
	}
	*/
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}
	
	private CommonError makeOutOfBounds(String msg) {
		CommonError e4;
		
		e4 = new CommonError();
		e4.id = unnamed.common.CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e4.msg = msg;
		return e4;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private BufferNode createInt16Buf(int len) {
		return wrapBuf(makeInt16Array(len), 0);
	}

	private BufferNode createInt64Buf(int len) {
		return wrapBuf(makeInt64Array(len), 0);
	}

	private CommonInt16Array makeInt16Array(int len) {
		return CommonUtils.makeInt16Array(len);
	}

	private CommonInt64Array makeInt64Array(int len) {
		return CommonUtils.makeInt64Array(len);
	}
}
