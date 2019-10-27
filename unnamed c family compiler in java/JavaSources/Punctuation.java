/*
 * Copyright (c) 2013-2014 Mike Goppold von Lobsdorf
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

public class Punctuation {
	public static final String PUNCT_GENERAL_COMMA = ",";
	public static final String PUNCT_GENERAL_COLON = ":";
	public static final String PUNCT_GENERAL_SEMICOLON = ";";
	public static final String PUNCT_GENERAL_DOT = ".";
	public static final String PUNCT_GENERAL_QUESTION_MARK = "?";
	
	public static final String PUNCT_GENERAL_QUOTE1 = "\'";
	public static final String PUNCT_GENERAL_QUOTE2 = "\"";
	
	public static final String PUNCT_GENERAL_LPAREN = "(";
	public static final String PUNCT_GENERAL_RPAREN = ")";
	public static final String PUNCT_GENERAL_LBRACK = "[";
	public static final String PUNCT_GENERAL_RBRACK = "]";
	public static final String PUNCT_GENERAL_LBRACE = "{";
	public static final String PUNCT_GENERAL_RBRACE = "}";
	
	public static final String PUNCT_C_ASSIGN = "=";
	
	public static final String PUNCT_C_PLUS = "+";
	public static final String PUNCT_C_MINUS = "-";
	public static final String PUNCT_C_TIMES = "*";
	public static final String PUNCT_C_DIVIDE = "/";
	public static final String PUNCT_C_MOD = "%";
	
	public static final String PUNCT_C_PLUS_ASSIGN = "+=";
	public static final String PUNCT_C_MINUS_ASSIGN = "-=";
	public static final String PUNCT_C_TIMES_ASSIGN = "*=";
	public static final String PUNCT_C_DIVIDE_ASSIGN = "/=";
	public static final String PUNCT_C_MOD_ASSIGN = "%=";
	
	public static final String PUNCT_C_INCREMENT = "++";
	public static final String PUNCT_C_DECREMENT = "--";
	
	public static final String PUNCT_C_AND_BITWISE = "&";
	public static final String PUNCT_C_OR_BITWISE = "|";
	public static final String PUNCT_C_XOR_BITWISE = "^";
	
	public static final String PUNCT_C_AND_BITWISE_ASSIGN = "&=";
	public static final String PUNCT_C_OR_BITWISE_ASSIGN = "|=";
	public static final String PUNCT_C_XOR_BITWISE_ASSIGN = "^=";
	
	public static final String PUNCT_C_NOT_BITWISE = "~";
	
	public static final String PUNCT_C_SHIFT_LEFT = "<<";
	public static final String PUNCT_C_SHIFT_RIGHT = ">>";
	//public static final String PUNCT_C_SHIFT_LEFT_LOGICAL
	public static final String PUNCT_C_SHIFT_RIGHT_LOGICAL = ">>>";
	
	public static final String PUNCT_C_SHIFT_LEFT_ASSIGN = "<<=";
	public static final String PUNCT_C_SHIFT_RIGHT_ASSIGN = ">>=";
	//public static final String PUNCT_C_SHIFT_LEFT_LOGICAL_ASSIGN
	public static final String PUNCT_C_SHIFT_RIGHT_LOGICAL_ASSIGN
		= ">>>=";
	
	public static final String PUNCT_C_AND_LOGICAL = "&&";
	public static final String PUNCT_C_OR_LOGICAL = "||";
	
	public static final String PUNCT_C_NOT_LOGICAL = "!";
	
	public static final String PUNCT_C_EQUAL = "==";
	public static final String PUNCT_C_NOT_EQUAL = "!=";
	
	public static final String PUNCT_C_LESS = "<";
	public static final String PUNCT_C_LESS_EQUAL = "<=";
	public static final String PUNCT_C_GREATER = ">";
	public static final String PUNCT_C_GREATER_EQUAL = ">=";
	
	public static final String PUNCT_C_ARROW = "->";
	public static final String PUNCT_C_SCOPE = "::";
	
	public static final String PUNCT_XML_CDATA_START
		= "<![CDATA[";
	
	public static final String PUNCT_XML_COMMENT_START
		= "<!--";
}
