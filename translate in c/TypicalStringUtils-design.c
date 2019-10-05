/*
 * Copyright (C) 2019  Mike Goppold von Lobsdorf
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

int32 TypicalStringUtils_unsafeStrGetLength(const int8 *str1) {
	int32 i;

	i = 0;
	while(str1[i] != 0) {
		i += 1;
	}
	
	return i;
}

int32 TypicalStringUtils_unicodeUtf8LengthFromFirstCodeUnit(int8 c1) {
	// Note.  If the code point is only 1 byte in length,
	// this function returns 0.
	//
	// For a length of 1, this function returns 0.
	// For lengths 2 to 4, this function is accurate.
	// For lengths greater or equal to 5,
	// this function indicates an invalid first code unit.
	
	int32 i;
	int32 theBit;
	
	i = 0;
	while(i < 8) {
		theBit = c1 >> (7 - i);
		if((theBit & 1) == 0) return i;
		i += 1;
	}
	
	return i;
}

int32 TypicalStringUtils_unicodeUtf8IsFollowingCodeUnitValid(int8 cn) {
	int twoBits;
	
	twoBits = cn >> 6;
	if((twoBits & 2) == 2) return BASE_TRUE;
	return BASE_FALSE;
}

int32 TypicalStringUtils_unicodeUtf8HighOrderBitsFromFirstCodeUnit(int8 c1, int32 utf8Len) {
	int32 i;
	int32 theBits;
	int32 oneBit;
	int32 len2 = utf8Len + 1;
	
	i = 0;
	theBits = (c1 & 0xff);
	while(i < len2) {
		oneBit = 1 << (7 - i);
		theBits = theBits & (~oneBit);
		i += 1;
	}
	
	return theBits;
}

int32 TypicalStringUtils_unicodeUtf8FollowingCodeUnitBits(int8 cn) {
	return cn & 0xcf;
}


TypicalString * TypicalStringUtils_typicalStringFromUnsafeStr(
	const int8 *str1) {

	int32 length;
	int32 i;
	int32 j;
	int32 c;
	int32 utf8Len;
	int32 c2;
	int32 str2Pos;

	length = TypicalStringUtils_unsafeStrGetLength(str1);
	
	TypicalString *str2 = new TypicalString();
	//str2->data = (int8 *) malloc(length);
	str2->data = new int32[length];

	i = 0;
	while(i < length) {
		c = str1[i];
		utf8Len = TypicalStringUtils_unicodeUtf8LengthFromFirstCodeUnit(
			(int8) c);
		
		if(utf8Len == 0) {
			str2->data[str2Pos] = str1[i];
			i += 1;
			str2Pos += 1;
			continue;
		}
		
		if(utf8Len >= 5
			|| i + utf8Len > length) {
			
			InvalidEncodedSequenceException *e3 =
				new InvalidEncodedSequenceException();
			e3->id = TypicalExceptionTypes_INVALID_ENCODED_SEQUENCE_ID;
			e3->position = i;
			e3->length = 1;
			throw e3;
		}
		
		c2 = TypicalStringUtils_unicodeUtf8HighOrderBitsFromFirstCodeUnit(
			c, utf8Len);
		
		j = 1;
		while(j < utf8Len) {
			c2 <<= 6;
			
			if(!TypicalStringUtils_unicodeUtf8IsFollowingCodeUnitValid(
				str1[i + j])) {

				InvalidEncodedSequenceException *e3 =
					new InvalidEncodedSequenceException();
				e3->id = TypicalExceptionTypes_INVALID_ENCODED_SEQUENCE_ID;
				e3->position = i;
				e3->length = utf8Len;
				throw e3;
			}
			
			c2 |= TypicalStringUtils_unicodeUtf8FollowingCodeUnitBits(
				str1[i + j]);
			
			j += 1;
		}
		
		str2->data[str2Pos] = str1[i];
		
		i += utf8Len;
		str2Pos += 1;
	}

	str2->length = str2Pos;
	str2->capacity = length;
	return str2;
}

