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

TypicalInt8Array * TypicalStringUtils2_TypicalInt8ArrayFromUnsafeStr(
	const int8 *str1) {

	int32 length;
	int32 i;

	length = TypicalStringUtils_UnsafeStrGetLength(str1);
	
	TypicalInt8Array *str2 = new TypicalInt8Array();
	//str2->data = (int8 *) malloc(length);
	str2->theAry = new int8[length];

	i = 0;
	while(i < length) {
		str2->theAry[i] = str1[i];
		i += 1;
	}

	str2->length = length;
	str2->capacity = length;
	return str2;
}

