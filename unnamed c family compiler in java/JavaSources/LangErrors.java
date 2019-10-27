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
 * This Java class contains type codes for source file errors.
 */

package unnamed.family.compiler;

public class LangErrors {	
	public static final int ERROR_XML_REFERENCE_BAD = 1011;
	public static final int ERROR_XML_COMMENT_UNTERMINATED = 1012;
	public static final int ERROR_XML_CDATA_UNTERMINATED = 1013;
	
	public static final int ERROR_BAD_SPAN = 1021;
	public static final int ERROR_SYMBOL_UNEXPECTED = 1022;
	public static final int ERROR_TOKEN_UNEXPECTED = 1023;
	
	public static final int ERROR_COMMENT_UNTERMINATED = 1031;
	
	public static final int ERROR_INCLUDE_LOOP = 1041;
	
	public static final int ERROR_NAME_ALREADY_EXISTS = 1051;
	
	public static final int ERROR_INTEGER_OVERFLOW = 1061;
	
	public static final int ERROR_STRING_ESCAPE_BAD = 1071;
	public static final int ERROR_STRING_UNTERMINATED = 1072;
	public static final int ERROR_STRING_UNIMPLEMENTED = 1073;
	
	public static final int ERROR_GRAMMAR_UNKNOWN_NAME = 1081;
	public static final int ERROR_GRAMMAR_UNSPECIFIED_NAME = 1082;
	public static final int ERROR_GRAMMAR_UNEXPECTED_NAME_TYPE = 1083;
	public static final int ERROR_GRAMMAR_DUPLICATE_NAME = 1084;
	
	public static final int
		ERROR_GRAMMAR_LR_FIRST_SETS_BAD_VARIABLES = 1091;
	public static final int
		ERROR_GRAMMAR_LR_ROOT_VARIABLE_NOT_COMPATIBLE = 1092;
	
	public static final int
		ERROR_TOKEN_NOT_IN_GRAMMAR = 1101;
}
