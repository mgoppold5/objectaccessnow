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
 * This Java class is an enum for errors in this package.
 */

package unnamed.common;

public class CommonErrorTypes {
	// CommonError
	public static final int ERROR_UNKNOWN = 1;
	public static final int ERROR_INVALID_ENUM = 2;
	public static final int ERROR_INVALID_HANDLE = 3;
	public static final int ERROR_UNHANDLED_TYPE = 4;
	public static final int ERROR_NULL_POINTER = 5;
	
	public static final int ERROR_UNEXPECTED_END_OF_STREAM = 11;
	public static final int ERROR_UNEXPECTED_RECORD = 12;
	public static final int ERROR_UNRECOGNIZED_RECORD = 13;
	public static final int ERROR_UNEXPECTED_OBJECT = 14;
	public static final int ERROR_OUT_OF_BOUNDS = 15;

	public static final int ERROR_PATH_NOT_FOUND = 21;
	public static final int ERROR_PATH_ALREADY_EXISTS = 22;
	public static final int ERROR_MALFORMED_PATH = 23;
//	public static final int ERROR_INVALID_PATH = 24;
	public static final int ERROR_NAME_NOT_FOUND = 25;
	public static final int ERROR_OBJECT_NOT_FOUND = 26;
	
	public static final int ERROR_INTEGER_OVERFLOW = 31;
	
	// FileSystemError
	public static final int ERROR_DIRECTORY_NOT_FOUND = 101;
	public static final int ERROR_DIRECTORY_ALREADY_EXISTS = 102;
	public static final int ERROR_FILE_NOT_FOUND = 103;
	public static final int ERROR_FILE_ALREADY_EXISTS = 104;
	public static final int ERROR_FILE_INDEX_OUT_OF_BOUNDS = 105;

	// FileSystemIOError
	public static final int ERROR_FS_IO = 111;
	
	// TextError
	public static final int ERROR_UNKNOWN_CHARACTER_ENCODING = 151;
	public static final int ERROR_INVALID_CHARACTER = 152;
	public static final int ERROR_UNEXPECTED_CHARACTER = 153;
//	public static final int ERROR_UNEXPECTED_TOKEN = 154;
}
