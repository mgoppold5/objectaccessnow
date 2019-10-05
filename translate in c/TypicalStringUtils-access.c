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

/*
 * Class with simple string functions.
 */

int32 TypicalStringUtils_unsafeStrGetLength(
	const int8 *str1);
int32 TypicalStringUtils_unicodeUtf8LengthFromFirstCodeUnit(
	int8 c1);
int32 TypicalStringUtils_unicodeUtf8IsFollowingCodeUnitValid(
	int8 cn);
int32 TypicalStringUtils_unicodeUtf8HighOrderBitsFromFirstCodeUnit(
	int8 c1, int32 utf8Len);
int32 TypicalStringUtils_unicodeUtf8FollowingCodeUnitBits(
	int8 cn);
TypicalString * TypicalStringUtils_typicalStringFromUnsafeStr(
	const int8 *str1);
