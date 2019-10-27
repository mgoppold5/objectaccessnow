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

/*
 * This module contains fast copy functions.
 */

package unnamed.common;

//import unnamed.file.system.FileRecord2;

public class BufferFastCopyUtils {
	/*
	private void copyToByteBufFromNormalBuf(
		FileRecord2 fh,
		CommonInt8Array byteBuf, int byteBufAlignStart,
		BufferNode buf, int bufAlignStart,
		int alignLen) {
		
		boolean ok;
		int i1;
		int i2;
		int i3;

		int alignToMemoryMult = 1 << (fh.strictAlignSize - fh.memoryUnitSize);
		int alignToByteMult = 1 << (fh.strictAlignSize - NATIVE_UNIT_SIZE);

		int memoryToByteMult = 1 << (fh.memoryUnitSize - NATIVE_UNIT_SIZE);
		
		ok = false;
		
		int bytePos1, bytePos2, bytePos3;
		int memPos1, memPos2;
		
		if(fh.memoryUnitSize == 4) {
			CommonInt16Array buf2 = (CommonInt16Array) buf.theObject;
			
			i1 = 0;
			while(i1 < alignLen) {
				bytePos1 = (byteBufAlignStart + i1) * alignToByteMult;
				memPos1 = (bufAlignStart + i1) * alignToMemoryMult;

				i2 = 0;
				while(i2 < alignToMemoryMult) {
					bytePos2 = bytePos1 + i2 * memoryToByteMult;
					memPos2 = memPos1 + i2;
					
					short num;
					
					num = buf2[memPos2];
					byteBuf[bytePos2 + 0] = FlagUtils.unpackInt8FromInt16(
						num, 0);
					byteBuf[bytePos2 + 1] = FlagUtils.unpackInt8FromInt16(
						num, 1);
					
					i2 += 1;
				}
				
				i1 += 1;
			}
			
			ok = true;
		}

		if(fh.memoryUnitSize == 6) {
			CommonInt64Array buf2 = (CommonInt64Array) buf.theObject;
			
			i1 = 0;
			while(i1 < alignLen) {
				bytePos1 = (byteBufAlignStart + i1) * alignToByteMult;
				memPos1 = (bufAlignStart + i1) * alignToMemoryMult;

				i2 = 0;
				while(i2 < alignToMemoryMult) {
					bytePos2 = bytePos1 + i2 * memoryToByteMult;
					memPos2 = memPos1 + i2;

					long num2 = buf2[memPos2];
					
					i3 = 0;
					while(i3 < 4) {
						bytePos3 = bytePos2 + i3 * 2;
						
						short num;

						num = FlagUtils.unpackInt16FromInt64(
							num2, i3);
						byteBuf[bytePos3 + 0] = FlagUtils.unpackInt8FromInt16(
							num, 0);
						byteBuf[bytePos3 + 1] = FlagUtils.unpackInt8FromInt16(
							num, 1);
						
						i3 += 1;
					}
					
					i2 += 1;
				}
				
				i1 += 1;
			}
			
			ok = true;
		}
		
		if(!ok)
			throw makeIllegalState(null);
		
		return;
	}
	
	private void copyFromByteBufToNormalBuf(
		FileRecord2 fh,
		CommonInt8Array byteBuf, int byteBufAlignStart,
		BufferNode buf, int bufAlignStart,
		int alignLen) {

		boolean ok;
		int i1;
		int i2;

		int alignToMemoryMult = 1 << (fh.strictAlignSize - fh.memoryUnitSize);
		int alignToByteMult = 1 << (fh.strictAlignSize - NATIVE_UNIT_SIZE);

		int memoryToByteMult = 1 << (fh.memoryUnitSize - NATIVE_UNIT_SIZE);
		
		ok = false;
		
		int bytePos1, bytePos2;
		int memPos1, memPos2;
		
		if(fh.memoryUnitSize == 4) {
			CommonInt16Array buf2 = (CommonInt16Array) buf.theObject;
			
			i1 = 0;
			while(i1 < alignLen) {
				bytePos1 = (byteBufAlignStart + i1) * alignToByteMult;
				memPos1 = (bufAlignStart + i1) * alignToMemoryMult;

				i2 = 0;
				while(i2 < alignToMemoryMult) {
					bytePos2 = bytePos1 + i2 * memoryToByteMult;
					memPos2 = memPos1 + i2;
					
					buf2[memPos2] = FlagUtils.packInt16WithInt8(
						byteBuf[bytePos2],
						byteBuf[bytePos2 + 1]);
					
					i2 += 1;
				}
				
				i1 += 1;
			}
			
			ok = true;
		}

		if(fh.memoryUnitSize == 6) {
			CommonInt64Array buf2 = (CommonInt64Array) buf.theObject;
			
			i1 = 0;
			while(i1 < alignLen) {
				bytePos1 = (byteBufAlignStart + i1) * alignToByteMult;
				memPos1 = (bufAlignStart + i1) * alignToMemoryMult;

				i2 = 0;
				while(i2 < alignToMemoryMult) {
					bytePos2 = bytePos1 + i2 * memoryToByteMult;
					memPos2 = memPos1 + i2;
					
					buf2[memPos2] = FlagUtils.packInt64WithInt16(
						FlagUtils.packInt16WithInt8(
							byteBuf[bytePos2 + 0],
							byteBuf[bytePos2 + 1]),
						FlagUtils.packInt16WithInt8(
							byteBuf[bytePos2 + 2],
							byteBuf[bytePos2 + 3]),
						FlagUtils.packInt16WithInt8(
							byteBuf[bytePos2 + 4],
							byteBuf[bytePos2 + 5]),
						FlagUtils.packInt16WithInt8(
							byteBuf[bytePos2 + 6],
							byteBuf[bytePos2 + 7]));
					
					i2 += 1;
				}
				
				i1 += 1;
			}
			
			ok = true;
		}
		
		if(!ok)
			throw makeIllegalState(null);
		
		return;
	}
	*/
	
	private static int getInnerPowerUnitMaskInt32(
		short innerUnitSize, CommonInt32Array int32Masks) {
		
		int mult = 1 << innerUnitSize;
		return int32Masks.aryPtr[mult];
	}
	
	private static int getInnerPowerUnitInt32(
		int innerUnitMask, short innerUnitSize, int pos,
		int outerNum) {
		
		int mult = 1 << innerUnitSize;
		int shift = mult * pos;
		
		int num;
		
		num = outerNum >> shift;
		num &= innerUnitMask;
		
		return num;
	}
	
	private static int setInnerPowerUnitInt32(
		int innerUnitMask, short innerUnitSize, int pos,
		int outerNum, int innerNum) {

		int mult = 1 << innerUnitSize;
		int shift = mult * pos;
		
		int num;
		int mask2;
		
		num = outerNum;
		
		// clear inner num bits
		mask2 = innerUnitMask << shift;
		num &= ~mask2;
		
		// insert inner num
		mask2 = innerNum & innerUnitMask;
		mask2 = mask2 << shift;
		num |= mask2;
		
		return num;
	}

	private static long getInnerPowerUnitInt64(
		long innerUnitMask, short innerUnitSize, int pos,
		long outerNum) {
		
		int mult = 1 << innerUnitSize;
		int shift = mult * pos;
		
		long num;
		
		num = outerNum >> shift;
		num &= innerUnitMask;
		
		return num;
	}
	
	private static long setInnerPowerUnitInt64(
		long innerUnitMask, short innerUnitSize, int pos,
		long outerNum, long innerNum) {

		int mult = 1 << innerUnitSize;
		int shift = mult * pos;
		
		long num;
		long mask2;
		
		num = outerNum;
		
		// clear inner num bits
		mask2 = innerUnitMask << shift;
		num &= ~mask2;
		
		// insert inner num
		mask2 = innerNum & innerUnitMask;
		mask2 = mask2 << shift;
		num |= mask2;
		
		return num;
	}
	
	/*
	private void slowCopyToByteBufFromByteBuf(
		FileRecord2 fh,
		CommonInt8Array toByteBuf, int toBufClientStart,
		CommonInt8Array fromByteBuf, int fromBufClientStart,
		int clientLen) {
		
		int mult;
		int mask;
		
		int i1;
		int i2;
		
		int toPos1;
		int fromPos1;
		
		int fract1;
		int fract2;

		if(fh.clientUnitSize >= NATIVE_UNIT_SIZE) {
			mult = 1 << (fh.clientUnitSize - NATIVE_UNIT_SIZE);
			
			i1 = 0;
			while(i1 < clientLen) {
				toPos1 = (toBufClientStart + i1) * mult;
				fromPos1 = (fromBufClientStart + i1) * mult;
				
				i2 = 0;
				while(i2 < mult) {
					toByteBuf[toPos1 + i2] = fromByteBuf[fromPos1 + i2];
					i2 += 1;
				}
				
				i1 += 1;
			}
		}
		
		if(NATIVE_UNIT_SIZE > fh.clientUnitSize) {
			mult = 1 << (NATIVE_UNIT_SIZE - fh.clientUnitSize);
			mask = getInnerPowerUnitMaskInt32(fh.clientUnitSize);
			
			i1 = 0;
			while(i1 < clientLen) {
				fract1 = (toBufClientStart + i1) % mult;
				fract2 = (fromBufClientStart + i1) % mult;
				
				toPos1 = (toBufClientStart + i1) / mult;
				fromPos1 = (fromBufClientStart + i1) / mult;
				
				if(fract1 != fract2)
					throw makeIllegalState(null);
				
				byte num2;
				byte innerNum;
				
				innerNum = (byte) getInnerPowerUnitInt32(
					mask, fh.clientUnitSize, fract1,
					fromByteBuf[fromPos1]);
				num2 = (byte) setInnerPowerUnitInt32(
					mask, fh.clientUnitSize, fract1,
					toByteBuf[toPos1],
					innerNum);
				toByteBuf[toPos1] = num2;

				i1 += 1;
			}
		}
		
		return;
	}
	*/
	
	private static int intersectLen(
		int start1, int len1,
		int start2, int len2) {
		
		int past1 = start1 + len1;
		int past2 = start2 + len2;
		
		int past3 = past1;
		if(past2 < past3) past3 = past2;
		
		int start3 = start1;
		if(start2 > start3) start3 = start2;
		
		int len3 = 0;
		if(past3 > start3) len3 = past3 - start3;
		
		return len3;
	}
	
	public static void copyStrict(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize, int alignLen,
		CommonInt32Array int32Masks)
	{
		copy(
			fromBuf, fromUnitSize, fromAlignStart,
			toBuf, toUnitSize, toAlignStart,
			alignUnitSize, alignLen,
			false, int32Masks);
	}

	public static void copyClassic(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize, int alignLen,
		CommonInt32Array int32Masks)
	{
		copy(
			fromBuf, fromUnitSize, fromAlignStart,
			toBuf, toUnitSize, toAlignStart,
			alignUnitSize, alignLen,
			true, int32Masks);
	}

	public static void copy(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize, int alignLen,
		boolean classic, CommonInt32Array int32Masks) {
		
		boolean canUse16;
		canUse16 = (fromUnitSize <= 4 && toUnitSize <= 4);
		
		if(canUse16) {
			copyAlign16(
				fromBuf, fromUnitSize, fromAlignStart,
				toBuf, toUnitSize, toAlignStart,
				alignUnitSize, alignLen,
				classic, int32Masks);
			return;
		}

		boolean canUse64;
		canUse64 = (fromUnitSize <= 6 && toUnitSize <= 6);

		if(canUse64) {
			copyAlign64(
				fromBuf, fromUnitSize, fromAlignStart,
				toBuf, toUnitSize, toAlignStart,
				alignUnitSize, alignLen,
				classic, int32Masks);
			return;
		}

		throw makeIllegalState(null);
	}
	
	public static void copyAlign64(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize, int alignLen,
		boolean classic,
		CommonInt32Array int32Masks)
	{
		long num64;
		
		int i;
		int i64;
		
		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;
		
		if(alignUnitSize < fromUnitSize
			|| alignUnitSize < toUnitSize)
			throw makeIllegalState(null);
		
		boolean canUse64;
		
		canUse64 = (fromUnitSize <= 6 && toUnitSize <= 6);

		if(canUse64) {
			boolean goDown64; 

			//boolean getFromParent64;
			boolean getFromArray64;
			boolean setToArray64;
			//boolean setToParent64;

			goDown64 = (fromUnitSize < 6 || toUnitSize < 6);

			//getFromParent64 = (fromUnitSize > 6);
			getFromArray64 = (fromUnitSize == 6);
			setToArray64 = (toUnitSize == 6);
			//setToParent64 = (toUnitSize > 6);

			CommonInt64Array fromArray64;
			CommonInt64Array toArray64;

			fromArray64 = null;
			if(getFromArray64) fromArray64 = (CommonInt64Array) fromBuf.theObject;
			toArray64 = null;
			if(setToArray64) toArray64 = (CommonInt64Array) toBuf.theObject;
			
			int alignToInt64Mult = 1 << (alignUnitSize - 6);
			
			i = 0;
			while(i < alignLen) {
				i64 = 0;
				
				fromStart2 = (fromAlignStart + i) * alignToInt64Mult;
				toStart2 = (toAlignStart + i) * alignToInt64Mult;
				
				while(i64 < alignToInt64Mult) {
					num64 = 0;
					
					fromStart3 = fromStart2 + i64;
					toStart3 = toStart2 + i64;

					if(getFromArray64) {
						num64 = fromArray64.aryPtr[fromStart3];
					}

					if(goDown64) {
						num64 = copy64(
							fromBuf, fromUnitSize, fromStart3,
							toBuf, toUnitSize, toStart3,
							num64,
							classic, int32Masks);
					}

					if(setToArray64) {
						toArray64.aryPtr[toStart3] = num64;
					}

					i64 += 1;
				}

				i += 1;
			}
			
			return;
		}

		throw makeIllegalState(null);
	}
	
	public static void copyAlign16(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize, int alignLen,
		boolean classic, CommonInt32Array int32Masks)
	{
		short num16;
		
		int i;
		int i16;
		
		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;
		
		if(alignUnitSize < fromUnitSize
			|| alignUnitSize < toUnitSize)
			throw makeIllegalState(null);
		
		boolean canUse16;
		
		canUse16 = (fromUnitSize <= 4 && toUnitSize <= 4);

		if(canUse16) {
			boolean goDown16;

			//boolean getFromParent16;
			boolean getFromArray16;
			boolean setToArray16;
			//boolean setToParent16;

			goDown16 = (fromUnitSize < 4 || toUnitSize < 4);

			//getFromParent16 = (fromUnitSize > 4);
			getFromArray16 = (fromUnitSize == 4);
			setToArray16 = (toUnitSize == 4);
			//setToParent16 = (toUnitSize > 4);

			CommonInt16Array fromArray16;
			CommonInt16Array toArray16;

			fromArray16 = null;
			if(getFromArray16) fromArray16 = (CommonInt16Array) fromBuf.theObject;
			toArray16 = null;
			if(setToArray16) toArray16 = (CommonInt16Array) toBuf.theObject;
			
			if(goDown16 && !classic) throw makeIllegalState(null);

			int alignToInt16Mult = 1 << (alignUnitSize - 4);
			
			i = 0;
			while(i < alignLen) {
				i16 = 0;

				fromStart2 = (fromAlignStart + i) * alignToInt16Mult;
				toStart2 = (toAlignStart + i) * alignToInt16Mult;

				while(i16 < alignToInt16Mult) {
					num16 = 0;

					fromStart3 = fromStart2 + i16;
					toStart3 = toStart2 + i16;

					if(getFromArray16) {
						num16 = fromArray16.aryPtr[fromStart3];
					}

					if(goDown16)
						num16 = copy16Classic(
							fromBuf, fromUnitSize, fromStart3,
							toBuf, toUnitSize, toStart3,
							num16,
							int32Masks);

					if(setToArray16) {
						toArray16.aryPtr[toStart3] = num16;
					}

					i16 += 1;
				}

				i += 1;
			}
			
			return;
		}

		throw makeIllegalState(null);
	}

	public static long copy64(
		BufferNode fromBuf, short fromUnitSize, int from64Start,
		BufferNode toBuf, short toUnitSize, int to64Start,
		long inNum64,
		boolean classic,
		CommonInt32Array int32Masks) {
		
		int i16;
		short num16;
		long num64;
		
		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;

		int mask16 = int32Masks.aryPtr[16];
		
		boolean goDown;
		boolean getFromParent;
		boolean setToParent;
		boolean getFromArray;
		boolean setToArray;
		
		goDown = (fromUnitSize < 4 || toUnitSize < 4);

		getFromParent = (fromUnitSize > 4);
		getFromArray = (fromUnitSize == 4);
		setToArray = (toUnitSize == 4);
		setToParent = (toUnitSize > 4);
		
		num64 = 0;
		if(getFromParent) num64 = inNum64;

		CommonInt16Array fromArray16;
		CommonInt16Array toArray16;

		fromArray16 = null;
		if(getFromArray) fromArray16 = (CommonInt16Array) fromBuf.theObject;
		toArray16 = null;
		if(setToArray) toArray16 = (CommonInt16Array) toBuf.theObject;

		if(goDown && !classic) throw makeIllegalState(null);
		
		int int64ToInt16Mult = 1 << (6 - 4);
		
		fromStart2 = from64Start * int64ToInt16Mult;
		toStart2 = to64Start * int64ToInt16Mult;
		
		i16 = 0;
		while(i16 < int64ToInt16Mult) {
			num16 = 0;
			
			fromStart3 = fromStart2 + i16;
			toStart3 = toStart2 + i16;

			if(getFromParent) {
				num16 |= (short) getInnerPowerUnitInt64(
					mask16, (short) 4, i16,
					num64);
			}

			if(getFromArray) {
				num16 |= fromArray16.aryPtr[fromStart3];
			}

			if(goDown)
				num16 = copy16Classic(fromBuf, fromUnitSize, fromStart3,
					toBuf, toUnitSize, toStart3,
					num16,
					int32Masks);
			
			if(setToArray) {
				toArray16.aryPtr[toStart3] = num16;
			}

			if(setToParent) {
				num64 = setInnerPowerUnitInt64(
					mask16, (short) 4, i16,
					num64, num16);
			}
			
			i16 += 1;
			continue;
		}
		
		return num64;
	}

	public static short copy16Classic(
		BufferNode fromBuf, short fromUnitSize, int from16Start,
		BufferNode toBuf, short toUnitSize, int to16Start,
		short inNum16,
		CommonInt32Array int32Masks) {

		int i8;
		short num16;
		byte num8;
		
		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;

		int mask8 = int32Masks.aryPtr[8];
		
		boolean goDown;
		boolean getFromParent;
		boolean setToParent;
		boolean getFromArray;
		boolean setToArray;
		
		goDown = (fromUnitSize < 3 || toUnitSize < 3);

		getFromParent = (fromUnitSize > 3);
		getFromArray = (fromUnitSize == 3);
		setToArray = (toUnitSize == 3);
		setToParent = (toUnitSize > 3);
		
		if(goDown) throw makeIllegalState(null);
		
		CommonInt8Array fromArray8;
		CommonInt8Array toArray8;

		fromArray8 = null;
		if(getFromArray) fromArray8 = (CommonInt8Array) fromBuf.theObject;
		toArray8 = null;
		if(setToArray) toArray8 = (CommonInt8Array) toBuf.theObject;
		
		int int16ToInt8Mult = 1 << (4 - 3);
		
		fromStart2 = from16Start * int16ToInt8Mult;
		toStart2 = to16Start * int16ToInt8Mult;

		num16 = 0;
		if(getFromParent) num16 = inNum16;

		i8 = 0;
		while(i8 < int16ToInt8Mult) {
			num8 = 0;

			fromStart3 = fromStart2 + i8;
			toStart3 = toStart2 + i8;
			
			if(getFromParent) {
				num8 |= (byte) getInnerPowerUnitInt32(
					mask8, (short) 3, i8,
					num16);
			}

			if(getFromArray) {
				num8 |= fromArray8.aryPtr[fromStart3];
			}

			if(setToArray) {
				toArray8.aryPtr[toStart3] = num8;
			}

			if(setToParent) {
				num16 = (short) setInnerPowerUnitInt32(
					mask8, (short) 3, i8,
					num16, num8);
			}
			
			i8 += 1;
			continue;
		}
		
		return num16;
	}

	public static void copyPartial(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize,
		int start, int len,
		CommonInt32Array int32Masks, CommonInt64Array int64Masks)
	{
		boolean canUse16;
		canUse16 = (fromUnitSize <= 4 && toUnitSize <= 4);
		
		if(canUse16) {
			copyAlign16Partial(
				fromBuf, fromUnitSize, fromAlignStart,
				toBuf, toUnitSize, toAlignStart,
				alignUnitSize,
				start, len, 
				int32Masks);
			return;
		}

		boolean canUse64;
		canUse64 = (fromUnitSize <= 6 && toUnitSize <= 6);

		if(canUse64) {
			copyAlign64Partial(
				fromBuf, fromUnitSize, fromAlignStart,
				toBuf, toUnitSize, toAlignStart,
				alignUnitSize,
				start, len,
				int32Masks, int64Masks);
			return;
		}

		throw makeIllegalState(null);
	}

	public static void copyAlign64Partial(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize,
		int start, int len,
		CommonInt32Array int32Masks, CommonInt64Array int64Masks)
	{
		long num64;
		long inNum64;
		long targetNum64;
		
		int i64;
		
		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;
		
		if(alignUnitSize < fromUnitSize
			|| alignUnitSize < toUnitSize)
			throw makeIllegalState(null);

		boolean canUse64;
		
		canUse64 = (fromUnitSize <= 6 && toUnitSize <= 6);

		if(canUse64) {
			boolean goDown64; 
			//boolean getFromParent64;
			//boolean getTargetParent64;
			boolean getFromArray64;
			boolean getTargetArray64;
			boolean setToArray64;
			//boolean setToParent64;

			goDown64 = (fromUnitSize < 6 || toUnitSize < 6);

			//getFromParent64 = (fromUnitSize > 6);
			//getTargetParent64 = (toUnitSize > 6);
			getFromArray64 = (fromUnitSize == 6);
			getTargetArray64 = (toUnitSize == 6);
			setToArray64 = (toUnitSize == 6);
			//setToParent64 = (toUnitSize > 6);

			CommonInt64Array fromArray64;
			CommonInt64Array toArray64;

			fromArray64 = null;
			if(getFromArray64) fromArray64 = (CommonInt64Array) fromBuf.theObject;
			toArray64 = null;
			if(setToArray64) toArray64 = (CommonInt64Array) toBuf.theObject;
			
			int i64Start;
			int i64Len;
			int crossLen;
			int range64Start;
			int range64Len;

			boolean crossIsSomething;
			boolean crossIsPartial;
			
			int alignToInt64Mult = 1 << (alignUnitSize - 6);

			fromStart2 = fromAlignStart * alignToInt64Mult;
			toStart2 = toAlignStart * alignToInt64Mult;

			i64 = 0;
			i64Len = 64;
			while(i64 < alignToInt64Mult) {
				num64 = 0;
				inNum64 = 0;
				targetNum64 = 0;
				range64Start = 0;
				range64Len = 0;

				i64Start = i64 * 64;

				crossLen = intersectLen(
					i64Start, i64Len,
					start, len);

				crossIsSomething = (crossLen > 0);
				crossIsPartial = (crossLen < 64);

				if(crossIsSomething) {
					if(start > i64Start) range64Start = start - i64Start;
					range64Len = crossLen;
				}

				
				fromStart3 = fromStart2 + i64;
				toStart3 = toStart2 + i64;

				if(getFromArray64)
				if(crossIsSomething)
					inNum64 |= fromArray64.aryPtr[fromStart3];

				if(getTargetArray64)
				if(crossIsPartial)
					targetNum64 |= toArray64.aryPtr[toStart3];

				if(!goDown64)
					num64 |= copy64Extract(
						inNum64, targetNum64,
						range64Start, range64Len,
						int64Masks);
				
				if(goDown64)
					num64 = copy64Partial(
						fromBuf, fromUnitSize, fromStart3,
						toBuf, toUnitSize, toStart3,
						inNum64, targetNum64,
						range64Start, range64Len,
						int32Masks);
				
				if(setToArray64) {
					toArray64.aryPtr[toStart3] = num64;
				}

				i64 += 1;
			}
			
			return;
		}

		throw makeIllegalState(null);
	}
	
	public static void copyAlign16Partial(
		BufferNode fromBuf, short fromUnitSize, int fromAlignStart,
		BufferNode toBuf, short toUnitSize, int toAlignStart,
		short alignUnitSize,
		int start, int len,
		CommonInt32Array int32Masks) {
		
		short inNum16;
		short targetNum16;
		short num16;
		
		int i16;
		
		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;
		
		if(alignUnitSize < fromUnitSize
			|| alignUnitSize < toUnitSize)
			throw makeIllegalState(null);
		
		boolean canUse16;
		
		canUse16 = (fromUnitSize <= 4 && toUnitSize <= 4);

		if(canUse16) {
			boolean goDown16;
			//boolean getFromParent16;
			//boolean getTargetParent16;
			boolean getFromArray16;
			boolean getTargetArray16;
			boolean setToArray16;
			//boolean setToParent64;

			goDown16 = (fromUnitSize < 4 || toUnitSize < 4);

			//getFromParent16 = (fromUnitSize > 4);
			//getTargetParent16 = (toUnitSize > 4);
			getFromArray16 = (fromUnitSize == 4);
			getTargetArray16 = (toUnitSize == 4);
			setToArray16 = (toUnitSize == 4);
			//setToParent16 = (toUnitSize > 4);
			
			if(goDown16) throw makeIllegalState(null);
			

			CommonInt16Array fromArray16;
			CommonInt16Array toArray16;

			fromArray16 = null;
			if(getFromArray16) fromArray16 = (CommonInt16Array) fromBuf.theObject;
			toArray16 = null;
			if(setToArray16) toArray16 = (CommonInt16Array) toBuf.theObject;
			
			int i16Start;
			int i16Len;
			int crossLen;
			int range16Start;
			int range16Len;

			boolean crossIsSomething;
			boolean crossIsPartial;
			
			int alignToInt16Mult = 1 << (alignUnitSize - 4);

			fromStart2 = fromAlignStart * alignToInt16Mult;
			toStart2 = toAlignStart * alignToInt16Mult;

			i16 = 0;
			i16Len = 16;
			while(i16 < alignToInt16Mult) {
				num16 = 0;
				inNum16 = 0;
				targetNum16 = 0;
				range16Start = 0;
				range16Len = 0;

				i16Start = i16 * 16;

				crossLen = intersectLen(
					i16Start, i16Len,
					start, len);

				crossIsSomething = (crossLen > 0);
				crossIsPartial = (crossLen < 16);

				if(crossIsSomething) {
					if(start > i16Start) range16Start = start - i16Start;
					range16Len = crossLen;
				}
				
				fromStart3 = fromStart2 + i16;
				toStart3 = toStart2 + i16;

				if(getFromArray16)
				if(crossIsSomething)
					inNum16 |= fromArray16.aryPtr[fromStart3];

				if(getTargetArray16)
				if(crossIsPartial)
					targetNum16 |= toArray16.aryPtr[toStart3];

				num16 |= copy16Extract(
					inNum16, targetNum16,
					range16Start, range16Len,
					int32Masks);
				
				if(setToArray16) {
					toArray16.aryPtr[toStart3] = num16;
				}

				i16 += 1;
			}
			
			return;
		}

		throw makeIllegalState(null);
	}
	
	public static long copy64Partial(
		BufferNode fromBuf, short fromUnitSize, int from64Start,
		BufferNode toBuf, short toUnitSize, int to64Start,
		long inNum64, long targetNum64,
		int start, int len,
		CommonInt32Array int32Masks) {
		
		int i16;
		short num16;
		short inNum16;
		short targetNum16;
		long num64;

		int fromStart2;
		int toStart2;
		int fromStart3;
		int toStart3;
				
		int mask16 = int32Masks.aryPtr[16];
		
		boolean goDown;
		boolean getFromParent;
		boolean getTargetParent;
		boolean getFromArray;
		boolean getTargetArray;
		boolean setToArray;
		boolean setToParent;
		
		goDown = (fromUnitSize < 4 || toUnitSize < 4);

		getFromParent = (fromUnitSize > 4);
		getTargetParent = (toUnitSize > 4);
		getFromArray = (fromUnitSize == 4);
		getTargetArray = (toUnitSize == 4);
		setToArray = (toUnitSize == 4);
		setToParent = (toUnitSize > 4);
		
		if(goDown) throw makeIllegalState(null);

		CommonInt16Array fromArray16;
		CommonInt16Array toArray16;

		fromArray16 = null;
		if(getFromArray) fromArray16 = (CommonInt16Array) fromBuf.theObject;
		toArray16 = null;
		if(setToArray) toArray16 = (CommonInt16Array) toBuf.theObject;
		
		int i16Start;
		int i16Len;
		int crossLen;
		int range16Start;
		int range16Len;
		
		boolean crossIsSomething;
		boolean crossIsPartial;

		int int64ToInt16Mult = 1 << (6 - 4);
		
		fromStart2 = from64Start * int64ToInt16Mult;
		toStart2 = to64Start * int64ToInt16Mult;
		
		num64 = 0;
		i16 = 0;
		i16Len = 16;
		while(i16 < int64ToInt16Mult) {
			num16 = 0;
			inNum16 = 0;
			targetNum16 = 0;
			range16Start = 0;
			range16Len = 0;
			
			i16Start = i16 * 16;
			
			crossLen = intersectLen(
				i16Start, i16Len,
				start, len);
			
			crossIsSomething = (crossLen > 0);
			crossIsPartial = (crossLen < 16);
			
			if(crossIsSomething) {
				if(start > i16Start) range16Start = start - i16Start;
				range16Len = crossLen;
			}
			
			fromStart3 = fromStart2 + i16;
			toStart3 = toStart2 + i16;
			
			if(getFromParent)
			if(crossIsSomething)
				inNum16 |= (short) getInnerPowerUnitInt64(
					mask16, (short) 4, i16,
					inNum64);
			
			if(getTargetParent)
			if(crossIsPartial)
				targetNum16 |= (short) getInnerPowerUnitInt64(
					mask16, (short) 4, i16,
					targetNum64);

			if(getFromArray)
			if(crossIsSomething)
				inNum16 |= fromArray16.aryPtr[fromStart3];

			if(getTargetArray)
			if(crossIsPartial)
				targetNum16 |= toArray16.aryPtr[toStart3];
			
			num16 = copy16Extract(
				inNum16, targetNum16,
				range16Start, range16Len,
				int32Masks);

			if(setToArray) {
				toArray16.aryPtr[toStart3] = num16;
			}

			if(setToParent) {
				num64 = setInnerPowerUnitInt64(
					mask16, (short) 4, i16,
					num64, num16);
			}
			
			i16 += 1;
			continue;
		}
		
		return num64;
	}
	
	public static long copy64Extract(
		long inNum64, long targetNum64,
		int start, int len,
		CommonInt64Array int64Masks) {
		
		// start and len are in units of bits

		short num64;
		
		int past = start + len;
		
		if(past > 64)
			throw makeIllegalState(null);
		
		num64 = 0;

		long beforeMask = int64Masks.aryPtr[start];
		num64 |= (targetNum64 & beforeMask);
		
		long lenMask = int64Masks.aryPtr[len];
		num64 |= (inNum64 & (lenMask << start));
		
		long afterMask = int64Masks.aryPtr[64 - past];
		num64 |= (targetNum64 & (afterMask << past));
		
		return num64;
	}
	
	public static short copy16Extract(
		short inNum16, short targetNum16,
		int start, int len,
		CommonInt32Array int32Masks) {
		
		// start and len are in units of bits

		short num16;
		
		int past = start + len;
		
		if(past > 16)
			throw makeIllegalState(null);
		
		num16 = 0;

		int beforeMask = int32Masks.aryPtr[start];
		num16 |= (targetNum16 & beforeMask);
		
		int lenMask = int32Masks.aryPtr[len];
		num16 |= (inNum16 & (lenMask << start));
		
		int afterMask = int32Masks.aryPtr[16 - past];
		num16 |= (targetNum16 & (afterMask << past));
		
		return num16;
	}

	private static RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}	
}
