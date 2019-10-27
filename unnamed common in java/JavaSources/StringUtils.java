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

/*
 * This Java class contains functions which deal with strings
 */

package unnamed.common;

public class StringUtils {
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

	public static void setCodeUnit(
		CommonInt8Array buf, int index, int unitSize, int codeUnit) {
		
		int i;
		int baseIndex;
		
		baseIndex = index * unitSize;
		
		i = 0;
		while(i < unitSize) {
			buf.aryPtr[baseIndex + i] = (byte) (codeUnit >>> (i * 8));
			
			i += 1;
		}
		
		return;
	}

	public static int getCodeUnit(
		CommonInt8Array buf, int index, int unitSize) {
		
		int accumulator;
		int b1;
		int i;
		int baseIndex;
		
		baseIndex = index * unitSize;

		i = 0;
		accumulator = 0;
		while(i < unitSize) {
			b1 = buf.aryPtr[baseIndex + i];
			b1 = b1 & 0xFF; // get value from 0..255
			
			accumulator |= b1 << (i * 8);
			
			i += 1;
		}

		return accumulator;
	}
	
	public static void utf8FromChar(int theChar, CommonInt8Array buf, CommonInt32 len) {
		int c;
		int highBits;
		int continueBytes;
		int holdBits;
		int i;
		int theByte;

		if(buf.length < 6)
			throw new IndexOutOfBoundsException();
		
		c = theChar;
		if(c < 0) 
			throw makeInvalidChar("character is negative");
		
		// simple case with 7 bit character
		if(c < (1 << 8)) {
			buf.aryPtr[0] = (byte) c;
			len.value = 1;
			return;
		}
		
		// first possibility and inital conditions
		highBits = 5;
		continueBytes = 1;
		
		while(true) {
			holdBits = highBits + continueBytes * 6;
			
			// test if character fits
			if(c < (1 << holdBits)) {
				break;
			}
			
			highBits -= 1;
			continueBytes += 1;
			
			if(highBits == 0)
				throw makeInvalidChar("character is too large");
		}
		
		// highBits and continueBytes calculated
		
		// leading byte calc
		theByte = c >> (continueBytes * 6);
		theByte = fillHighByte(theByte, highBits);
		buf.aryPtr[0] = (byte) theByte;
		
		i = 0;
		while(i < continueBytes) {
			// continuation bytes highest to lowest
			theByte = c >> ((continueBytes - i - 1) * 6);
			theByte = fillHighByte(theByte, 6);

			buf.aryPtr[i + 1] = (byte) theByte;
			
			i += 1;
		}
		
		len.value = 1 + continueBytes;
		return;
	}
	
	public static BufferCombo utf8FromJavaString(String s) {
		int i;
		char c1;
		char c2;
		int fullChar;

		BufferCombo accum = makeBufferCombo();
		char[] charArray = s.toCharArray();
		int len = charArray.length;
		//byte[] byteBuf = new byte[6];
		CommonInt8Array byteBuf = CommonUtils.makeInt8Array(6);
		CommonInt32 byteCount = new CommonInt32();
		
		i = 0;
		while(i < len) {
			c1 = charArray[i];
			
			if((c1 >= 0xD800) && (c1 <= 0xDBFF)) {
				if(i + 1 >= len)
					throw makeInvalidChar("invalid utf16 sequence");

				c2 = charArray[i + 1];
				if((c2 < 0xDC00) || (c2 > 0xDFFF))
					throw makeInvalidChar("invalid utf16 sequence");
				
				fullChar = ((c1 - 0xD800) << 10) + (c2 - 0xDC00);
				
				utf8FromChar(fullChar, byteBuf, byteCount);
				BufferUtils.appendBufferToCombo(accum,
					byteBuf, byteCount.value, 1);
				
				i += 2;
				continue;
			}
			
			if((c1 >= 0xDC00) && (c1 <= 0xDFFF))
				throw makeInvalidChar("invalid utf16 sequence");
			
			fullChar = c1;
			
			utf8FromChar(fullChar, byteBuf, byteCount);
			BufferUtils.appendBufferToCombo(accum,
				byteBuf, byteCount.value, 1);

			i += 1;
		}
		
		return accum;
	}
	
	public static CommonInt32Array int32StringFromUtf32(BufferCombo str) {
		int elem;
		int elemCount;
		int unit;
		int unitCount;
		int i;
		int len;
		CommonInt32Array chars2;
		
		BufferElement beRec;
		
		len = 0;
		elemCount = str.bufferList.size();
		elem = 0;
		while(elem < elemCount) {
			beRec = (BufferElement) str.bufferList.get(elem);

			len += beRec.used;
			
			elem += 1;
		}
		
		//chars2 = new int[len];
		chars2 = CommonUtils.makeInt32Array(len);

		i = 0;
		elem = 0;
		while(elem < elemCount) {
			beRec = (BufferElement) str.bufferList.get(elem);

			unitCount = beRec.used;
			unit = 0;
			while(unit < unitCount) {
				chars2.aryPtr[i] = getCodeUnit(
					beRec.buf, unit + beRec.start, 4);
				
				unit += 1;
				i += 1;
			}
			
			elem += 1;
		}
		
		return chars2;
	}
		
	private static CommonInt32Array int32StringFromJavaCharArray(char[] chars1) {
		int i1;
		int i2;
		int count1;
		int count2;
		char c1;
		char c2;
		int fullChar;
		CommonInt32Array chars2;
		
		i1 = 0;
		count1 = chars1.length;
		count2 = 0;
		
		while(i1 < count1) {
			c1 = chars1[i1];

			if((c1 >= 0xD800) && (c1 <= 0xDBFF)) {
				if(i1 + 1 >= count1)
					throw makeInvalidChar("invalid utf16 sequence");

				c2 = chars1[i1 + 1];
				if((c2 < 0xDC00) || (c2 > 0xDFFF))
					throw makeInvalidChar("invalid utf16 sequence");
				
				count2 += 1;
				i1 += 2;
				continue;
			}

			if((c1 >= 0xDC00) && (c1 <= 0xDFFF))
				throw makeInvalidChar("invalid utf16 sequence");

			count2 += 1;
			i1 += 1;
			continue;
		}
		
		//chars2 = new int[count2];
		chars2 = CommonUtils.makeInt32Array(count2);
		i1 = 0;
		i2 = 0;

		while(i1 < count1) {
			c1 = chars1[i1];

			if((c1 >= 0xD800) && (c1 <= 0xDBFF)) {
				if(i1 + 1 >= count1)
					throw makeInvalidChar("invalid utf16 sequence");

				c2 = chars1[i1 + 1];
				if((c2 < 0xDC00) || (c2 > 0xDFFF))
					throw makeInvalidChar("invalid utf16 sequence");
				
				fullChar = ((c1 - 0xD800) << 10) + (c2 - 0xDC00);
				chars2.aryPtr[i2] = fullChar;
				
				i1 += 2;
				i2 += 1;
				continue;
			}

			if((c1 >= 0xDC00) && (c1 <= 0xDFFF))
				throw makeInvalidChar("invalid utf16 sequence");
			
			fullChar = chars1[i1];
			chars2.aryPtr[i2] = fullChar;

			i1 += 1;
			i2 += 1;
			continue;
		}
		
		if(i2 != count2)
			throw makeUnknownError("String calculations are wrong");
		
		return chars2;
	}
	
	public static CommonInt32Array int32StringFromJavaString(String s) {
		char[] chars1;
		
		if(s == null)
			return null;
		
		chars1 = s.toCharArray();
		return int32StringFromJavaCharArray(chars1);
	}
	
	public static String javaStringFromInt32String(CommonInt32Array chars) {
		StringBuilder sb;
		int i;
		int count;
		
		sb = new StringBuilder();
		i = 0;
		count = chars.length;
		
		while(i < count) {
			sb.appendCodePoint(chars.aryPtr[i]);

			i += 1;
		}
		
		return sb.toString();
	}

	public static void int32CompareSimple(
		int num1, int num2,
		CompareParams compRes) {
				
		compRes.greater = false;
		compRes.less = false;
		
		if(num1 > num2) {
			compRes.greater = true;
			return;
		}
		
		if(num1 < num2) {
			compRes.less = true;
			return;
		}
		
		// they are equal
		return;
	}

	public static void int32StringCompareSimple(
		CommonInt32Array s1, CommonInt32Array s2,
		CompareParams compRes) {
		
		int len1;
		int len2;
		int len3;
		int i;
		
		compRes.greater = false;
		compRes.less = false;

		len1 = s1.length;
		len2 = s2.length;
		
		len3 = len1;
		if(len2 < len1)
			len3 = len2;
		
		i = 0;
		while(i < len3) {
			if(s1.aryPtr[i] > s2.aryPtr[i]) {
				compRes.greater = true;
				return;
			}
			
			if(s1.aryPtr[i] < s2.aryPtr[i]) {
				compRes.less = true;
				return;
			}
			
			i += 1;
			continue;
		}
		
		if(len1 > len2) {
			compRes.greater = true;
			return;
		}
		
		if(len1 < len2) {
			compRes.less = true;
			return;
		}
		
		// they are equal
		return;
	}

	public static void int16StringCompareSimple(
		CommonInt16Array s1, CommonInt16Array s2,
		CompareParams compRes) {
		
		int len1;
		int len2;
		int len3;
		int i;
		
		compRes.greater = false;
		compRes.less = false;

		len1 = s1.length;
		len2 = s2.length;
		
		len3 = len1;
		if(len2 < len1)
			len3 = len2;
		
		i = 0;
		while(i < len3) {
			if(s1.aryPtr[i] > s2.aryPtr[i]) {
				compRes.greater = true;
				return;
			}
			
			if(s1.aryPtr[i] < s2.aryPtr[i]) {
				compRes.less = true;
				return;
			}
			
			i += 1;
			continue;
		}
		
		if(len1 > len2) {
			compRes.greater = true;
			return;
		}
		
		if(len1 < len2) {
			compRes.less = true;
			return;
		}
		
		// they are equal
		return;
	}
	
	public static CommonInt32Array int32StringExtractRange(
		CommonInt32Array chars1, int start, int length) {
		
		int i;
		int len2;
		CommonInt32Array chars2;
		
		len2 = chars1.length;
		
		if(start > len2)
			throw makeIndexOutOfBounds(
				"start index for string is out of bounds");
		
		if(start + length > len2)
			throw makeIndexOutOfBounds(
				"span for string is out of bounds");
		
		//chars2 = new int[length];
		chars2 = CommonUtils.makeInt32Array(length);
		i = 0;
				
		while(i < length) {
			chars2.aryPtr[i] = chars1.aryPtr[i + start];
			
			i += 1;
		}
		
		return chars2;
	}

	public static CommonInt16Array int16StringExtractRange(
		CommonInt16Array chars1, int start, int length) {
		
		int i;
		int len2;
		CommonInt16Array chars2;
		
		len2 = chars1.length;
		
		if(start > len2)
			throw makeIndexOutOfBounds(
				"start index for string is out of bounds");
		
		if(start + length > len2)
			throw makeIndexOutOfBounds(
				"span for string is out of bounds");
		
		chars2 = CommonUtils.makeInt16Array(length);
		i = 0;
				
		while(i < length) {
			chars2.aryPtr[i] = chars1.aryPtr[i + start];
			
			i += 1;
		}
		
		return chars2;
	}
	
	public static CommonInt32Array int32StringSortChars(CommonInt32Array chars1) {
		CommonInt32Array chars;
		int i;
		int len;
		int temp1;
		int temp2;
		boolean didUpdate;
		
		//chars = ArrayUtils.copyInt32Array(chars1);
		chars = CommonIntArrayUtils.copy32(chars1);
		len = chars.length;

		didUpdate = true;
		while(didUpdate) {
			didUpdate = false;
			
			i = 1;
			while(i < len) {
				if(chars.aryPtr[i - 1] > chars.aryPtr[i]) {
					// swap
					temp1 = chars.aryPtr[i - 1];
					temp2 = chars.aryPtr[i];
					chars.aryPtr[i - 1] = temp2;
					chars.aryPtr[i] = temp1;

					didUpdate = true;					
				}
				
				i += 1;
			}
		}
		
		return chars;
	}
	
	public static boolean int32StringContainsChar(
		CommonInt32Array chars, int testChar) {
		
		int i;
		int len;
		
		len = chars.length;
		i = 0;
		while(i < len) {
			if(testChar == chars.aryPtr[i])
				return true;
			
			i += 1;
		}
		
		return false;
	}

	public static void int32StringReplaceChar(
		CommonInt32Array path, int fromChar, int toChar) {
		
		int len;
		int i;

		if(fromChar == toChar) return;
		
		i = 0;
		len = path.length;
		while(i < len) {
			if(path.aryPtr[i] == fromChar) path.aryPtr[i] = toChar;
			i += 1;
		}
		
		return;
	}

	public static boolean int32StringContainsCharSorted(
		CommonInt32Array chars, int testChar) {
		
		int i;
		int minIndex;
		int maxIndex;
		
		minIndex = 0;
		maxIndex = chars.length;
		
		while(true) {
			if(minIndex == maxIndex)
				return false;
			
			i = (minIndex + maxIndex) / 2;
			
			if(testChar > chars.aryPtr[i]) {
				minIndex = i + 1;
				continue;
			}
			
			if(testChar < chars.aryPtr[i]) {
				maxIndex = i;
				continue;
			}
			
			// found a match
			return true;
		}
		
		// UNREACHABLE
	}
	
	public static void utf32AppendCodePoint(
		BufferCombo str, int codePoint, CommonInt8Array scratch) {
		
		if(scratch.length < 4)
			throw new IndexOutOfBoundsException();
		
		setCodeUnit(scratch, 0, 4, codePoint);
		BufferUtils.appendBufferToCombo(str, scratch, 1, 4);
		return;
	}
	
	public static void utf32AppendUtf32(
		BufferCombo strDest, BufferCombo strSrc) {
		
		BufferUtils.appendComboToCombo(strDest, strSrc, 4);
	}
	
	private static CommonError makeInvalidChar(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_INVALID_CHARACTER;
		e1.msg = msg;
		return e1;
	}
	
	private static CommonError makeUnknownError(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNKNOWN;
		e1.msg = msg;
		return e1;
	}
	
	private static RuntimeException makeIndexOutOfBounds(String msg) {
		if(msg == null) return new IndexOutOfBoundsException();
		
		return new IndexOutOfBoundsException(msg);
	}
	
	private static BufferCombo makeBufferCombo() {
		BufferCombo bc;
		
		bc = new BufferCombo();
		bc.init();
		return bc;
	}
}
