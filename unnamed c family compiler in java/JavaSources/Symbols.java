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

package unnamed.family.compiler;

public class Symbols {
	public static final int SYMBOL_UNSPECIFIED = 0;

	
	// Basic tokens
	//
	
	public static final int TOKEN_CATEGORY_BASIC = 10;
	
	public static final int TOKEN_END_OF_STREAM = 11;
	public static final int TOKEN_UNEXPECTED_END_OF_STREAM = 12;
	public static final int TOKEN_IDENTIFIER = 13;
	public static final int TOKEN_BAD_SPAN = 14;
	
	
	// Number tokens
	//
	
	public static final int TOKEN_CATEGORY_NUMBER = 20;
	
	public static final int TOKEN_INTEGER_DATA = 21;
	public static final int TOKEN_INTEGER_FULL = 22;
	public static final int TOKEN_INTEGER_SIMPLE = 23;
	public static final int TOKEN_FLOAT_FULL = 24;
	
	
	// Whitespace tokens
	//
	
	public static final int TOKEN_CATEGORY_WHITESPACE = 30;
	
	public static final int TOKEN_TABS_AND_SPACES = 31;
	public static final int TOKEN_LINE_RETURN = 32;

	
	// Comment tokens
	//
	
	public static final int TOKEN_CATEGORY_COMMENT = 40;
	
	public static final int TOKEN_COMMENT_SINGLE_LINE = 41;
	public static final int TOKEN_COMMENT_MULTI_LINE = 42;
	
	
	// String tokens
	//
	
	public static final int TOKEN_CATEGORY_STRING = 60;

	public static final int TOKEN_STRING = 61;
	public static final int TOKEN_STRING_SPAN = 63;
	public static final int TOKEN_STRING_ESCAPE_NAME = 64;
	public static final int TOKEN_STRING_ESCAPE_VERBATIM = 65;
	public static final int TOKEN_STRING_ESCAPE_INTEGER = 66;
	public static final int TOKEN_STRING_ESCAPE_IGNORED = 68;
	public static final int TOKEN_STRING_ESCAPE_IGNORED_RETURN = 69;
	
	public static final int TOKEN_STRING_ESCAPE_NAME_ALERT = 71;
	public static final int TOKEN_STRING_ESCAPE_NAME_BACKSPACE = 72;
	public static final int TOKEN_STRING_ESCAPE_NAME_FORM_FEED = 73;
	public static final int TOKEN_STRING_ESCAPE_NAME_NEW_LINE = 74;
	public static final int TOKEN_STRING_ESCAPE_NAME_CARRIAGE_RETURN = 75;
	public static final int TOKEN_STRING_ESCAPE_NAME_TAB = 76;
	public static final int TOKEN_STRING_ESCAPE_NAME_VERTICAL_TAB = 77;
	
	
	// Character tokens
	//
	
	// CHARACTER is a specific form of STRING
	public static final int TOKEN_CATEGORY_CHARACTER = 80;

	// A character token is specified by TokenString,
	// and it contains string elements with category STRING.
	// The TokenString, for a CHARACTER, usually just
	// contains one STRING_SPAN.
	public static final int TOKEN_CHARACTER = 81;
	

	// General data stuff
	//
	
	public static final int TOKEN_CATEGORY_KEYWORDS_DATA = 200;
	
	public static final int TOKEN_KEYWORD_NULL = 201;
	public static final int TOKEN_KEYWORD_FALSE = 202;
	public static final int TOKEN_KEYWORD_TRUE = 203;
	
	
	public static final int GRAM_CATEGORY_DATA = 210;
	
	public static final int GRAM_LIST = 211;
	public static final int GRAM_DICT = 212;
	public static final int GRAM_DICT_ENTRY = 213;
	public static final int GRAM_SEQUENCE = 214;
	public static final int GRAM_SEQUENCE_INTEGER = 215;
	public static final int GRAM_SEQUENCE_IDENTIFIER = 216;


	// General punctuation tokens
	//
	
	public static final int TOKEN_CATEGORY_PUNCTUATION = 400;

	public static final int TOKEN_COMMA = 401;
	public static final int TOKEN_COLON = 402;
	public static final int TOKEN_SEMICOLON = 403;
	public static final int TOKEN_DOT = 404;
	public static final int TOKEN_QUESTION_MARK = 405;
	public static final int TOKEN_QUOTE = 406;
	
	public static final int TOKEN_LPAREN = 411; // (
	public static final int TOKEN_RPAREN = 412; // )
	public static final int TOKEN_LBRACK = 413; // [
	public static final int TOKEN_RBRACK = 414; // ]
	public static final int TOKEN_LBRACE = 415; // {
	public static final int TOKEN_RBRACE = 416; // }

	
	public static final int TOKEN_CATEGORY_PUNCTUATION_SPECIAL = 490;
	
	public static final int TOKEN_ARROW = 491; // ->
	public static final int TOKEN_SCOPE = 492; // ::
	
	
	// XML stuff
	//
	
	public static final int TOKEN_CATEGORY_XML = 700;
	
	public static final int TOKEN_XML_NAME = 701;
	public static final int TOKEN_XML_REFERENCE_CHARACTER_ENTITY = 702;
	public static final int TOKEN_XML_REFERENCE_CHARACTER_NUMERIC = 703;
	
	public static final int TOKEN_XML_TAG_FINISH = 711;
	public static final int TOKEN_XML_TAG_FINISH_WITH_END_MARK = 712;

	public static final int TOKEN_XML_COMMENT = 721;
	public static final int TOKEN_XML_COMMENT_END = 722;
	public static final int TOKEN_XML_CDATA = 723;
	public static final int TOKEN_XML_CDATA_END = 724;
	
	
	public static final int GRAM_CATEGORY_XML = 750;
	
	public static final int GRAM_XML_CONTENT = 751;
	public static final int GRAM_XML_ATTRIBUTE = 752;

	public static final int GRAM_XML_TAG = 761;
	public static final int GRAM_XML_TAG_WITH_END_MARK = 762;

	
	// Grammar files stuff
	//
	
	public static final int TOKEN_CATEGORY_KEYWORDS_GRAMMAR = 800;
	
	public static final int TOKEN_GRAMMAR_KEYWORD_RULE_LEFT = 801;
	public static final int TOKEN_GRAMMAR_KEYWORD_RULE_RIGHT = 802;
	public static final int TOKEN_GRAMMAR_KEYWORD_REDUCE_GRAM = 803;
	public static final int TOKEN_GRAMMAR_KEYWORD_PRECEDENCE = 804;
	
	public static final int TOKEN_GRAMMAR_KEYWORD_TOKEN = 811;
	public static final int TOKEN_GRAMMAR_KEYWORD_GRAM = 812;
	public static final int TOKEN_GRAMMAR_KEYWORD_ID = 813;

	public static final int TOKEN_GRAMMAR_KEYWORD_PRECEDENCE_SPECTRUM = 821;
	public static final int TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_LEFT = 822;
	public static final int TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_RIGHT = 823;
	public static final int TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_NON = 824;

	public static final int TOKEN_GRAMMAR_KEYWORD_ROOT_VARIABLE = 831;
	public static final int TOKEN_GRAMMAR_KEYWORD_END_MARKER = 832;
	public static final int TOKEN_GRAMMAR_KEYWORD_INCLUDE_GRAMMAR = 833;


	public static final int GRAM_CATEGORY_GRAMMAR = 900;
	
	public static final int GRAM_GRAMMAR_RULE_LEFT = 901;
	public static final int GRAM_GRAMMAR_RULE_RIGHT = 902;
	public static final int GRAM_GRAMMAR_REDUCE_GRAM = 903;
	public static final int GRAM_GRAMMAR_PRECEDENCE = 904;

	public static final int GRAM_GRAMMAR_TOKEN = 911;
	public static final int GRAM_GRAMMAR_GRAM = 912;
	public static final int GRAM_GRAMMAR_ID = 913;
	
	public static final int GRAM_GRAMMAR_PRECEDENCE_SPECTRUM = 921;
	public static final int GRAM_GRAMMAR_ASSOCIATIVITY = 922;

	public static final int GRAM_GRAMMAR_ROOT_VARIABLE = 931;
	public static final int GRAM_GRAMMAR_END_MARKER = 932;
	public static final int GRAM_GRAMMAR_INCLUDE_GRAMMAR = 933;
	
	public static final int GRAM_GRAMMAR_RULE_RIGHT_SEQUENCE = 941;
	public static final int GRAM_GRAMMAR_RULE_MODIFIER_SEQUENCE = 942;
		

	// C Family Old Include stuff (preprocessor)
	//
	
	public static final int TOKEN_CATEGORY_KEYWORDS_OLD_INCLUDE = 1100;

	public static final int TOKEN_OLD_INCLUDE_KEYWORD_INCLUDE = 1101;

	
	// Math and logic tokens
	//
	
	public static final int TOKEN_CATEGORY_MATH_AND_LOGIC = 2000;
	
	public static final int TOKEN_ASSIGN = 2001;
	
	public static final int TOKEN_PLUS = 2011;
	public static final int TOKEN_MINUS = 2012;
	public static final int TOKEN_TIMES = 2013;
	public static final int TOKEN_DIVIDE = 2014;
	public static final int TOKEN_MOD = 2015;
	public static final int TOKEN_UMINUS = 2016;
	
	public static final int TOKEN_PLUS_ASSIGN = 2021;
	public static final int TOKEN_MINUS_ASSIGN = 2022;
	public static final int TOKEN_TIMES_ASSIGN = 2023;
	public static final int TOKEN_DIVIDE_ASSIGN = 2024;
	public static final int TOKEN_MOD_ASSIGN = 2025;
	
	public static final int TOKEN_INCREMENT = 2031;
	public static final int TOKEN_DECREMENT = 2032;
	
	public static final int TOKEN_AND_BITWISE = 2041;
	public static final int TOKEN_OR_BITWISE = 2042;
	public static final int TOKEN_XOR_BITWISE = 2043;
	
	public static final int TOKEN_AND_BITWISE_ASSIGN = 2051;
	public static final int TOKEN_OR_BITWISE_ASSIGN = 2052;
	public static final int TOKEN_XOR_BITWISE_ASSIGN = 2053;

	public static final int TOKEN_NOT_BITWISE = 2061;
	
	public static final int TOKEN_SHIFT_LEFT = 2071;
	public static final int TOKEN_SHIFT_RIGHT = 2072;
	// public static final int TOKEN_SHIFT_LEFT_LOGICAL = 2073;
	public static final int TOKEN_SHIFT_RIGHT_LOGICAL = 2074;
	
	public static final int TOKEN_SHIFT_LEFT_ASSIGN = 2081;
	public static final int TOKEN_SHIFT_RIGHT_ASSIGN = 2082;
	// public static final int TOKEN_SHIFT_LEFT_LOGICAL_ASSIGN = 2082;
	public static final int TOKEN_SHIFT_RIGHT_LOGICAL_ASSIGN = 2084;

	public static final int TOKEN_AND_LOGICAL = 2091;
	public static final int TOKEN_OR_LOGICAL = 2092;
	
	public static final int TOKEN_NOT_LOGICAL = 2101;

	public static final int TOKEN_EQUAL = 2111;
	public static final int TOKEN_NOT_EQUAL = 2112;

	public static final int TOKEN_LESS = 2121;
	public static final int TOKEN_LESS_EQUAL = 2122;
	public static final int TOKEN_GREATER = 2123;
	public static final int TOKEN_GREATER_EQUAL = 2124;

	
	// Expression stuff
	//
	
	public static final int TOKEN_CATEGORY_EXPRESSION = 2200;
	
	public static final int TOKEN_KEYWORD_NEW = 2201;
	public static final int TOKEN_KEYWORD_CAST = 2202;
	public static final int TOKEN_KEYWORD_INSTANCE_OF = 2203;
	
	public static final int TOKEN_KEYWORD_PAREN_PARAMETERS = 2211;
	public static final int TOKEN_KEYWORD_PAREN_PARAMETERS_DEFINITION = 2212;
	public static final int TOKEN_KEYWORD_PAREN_EXPRESSION = 2213;
	public static final int TOKEN_KEYWORD_PAREN_TYPE = 2214;
	public static final int TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS = 2215;
	public static final int TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS_DEFINITION
		= 2216;

	public static final int TOKEN_KEYWORD_PAREN_ARRAY_ACCESS = 2221;
	public static final int TOKEN_KEYWORD_PAREN_ARRAY_INIT = 2222;
	public static final int TOKEN_KEYWORD_PAREN_LIST_ACCESS = 2223;
	public static final int TOKEN_KEYWORD_PAREN_LIST_INIT = 2224;
	public static final int TOKEN_KEYWORD_PAREN_DICT_ACCESS = 2225;
	public static final int TOKEN_KEYWORD_PAREN_DICT_INIT = 2226;
	

	public static final int GRAM_CATEGORY_EXPRESSION = 2300;
	
	public static final int GRAM_EXPRESSION_UNARY_PREFIX = 2301;
	public static final int GRAM_EXPRESSION_UNARY_POSTFIX = 2302;
	public static final int GRAM_EXPRESSION_BINARY = 2303;
	//public static final int GRAM_EXPRESSION_TERNARY = 2304;
	
	public static final int GRAM_EXPRESSION_SIMPLE = 2311;
	public static final int GRAM_EXPRESSION_PAREN = 2312;
	public static final int GRAM_EXPRESSION_PAREN_EMPTY = 2313;
	public static final int GRAM_EXPRESSION_BRACK = 2314;
	public static final int GRAM_EXPRESSION_BRACK_EMPTY = 2315;
	
	public static final int GRAM_EXPRESSION_ARRAY_ACCESS = 2321;
	public static final int GRAM_EXPRESSION_ARRAY_TYPE = 2322;
	public static final int GRAM_EXPRESSION_FUNCTION_CALL = 2323;
	public static final int GRAM_EXPRESSION_ALLOCATION_CALL = 2324;
	public static final int GRAM_EXPRESSION_ALLOCATION_ARRAY = 2325;
	public static final int GRAM_EXPRESSION_CAST = 2326;
	public static final int GRAM_EXPRESSION_INSTANCE_OF = 2327;

	public static final int GRAM_EXPRESSION_TYPE_WITH_PARAMETERS = 2331;
	

	// Statement stuff
	//

	public static final int TOKEN_CATEGORY_STATEMENT = 2500;
	
	public static final int TOKEN_KEYWORD_IF = 2551;
	public static final int TOKEN_KEYWORD_THEN = 2552;
	public static final int TOKEN_KEYWORD_ELSE = 2553;
	public static final int TOKEN_KEYWORD_WHILE = 2554;
	public static final int TOKEN_KEYWORD_TRY = 2555;
	public static final int TOKEN_KEYWORD_CATCH = 2556;
	public static final int TOKEN_KEYWORD_SWITCH = 2557;
	
	public static final int TOKEN_KEYWORD_BREAK = 2561;
	public static final int TOKEN_KEYWORD_CONTINUE = 2562;
	public static final int TOKEN_KEYWORD_RETURN = 2563;
	public static final int TOKEN_KEYWORD_THROW = 2564;
	
	//public static final int TOKEN_KEYWORD_LABEL = 2571;
	public static final int TOKEN_KEYWORD_CASE = 2572;
	public static final int TOKEN_KEYWORD_DEFAULT = 2573;
	
	
	public static final int GRAM_CATEGORY_STATEMENT = 2600;
	
	public static final int GRAM_STATEMENT_EXPRESSION = 2601;
	public static final int GRAM_STATEMENT_VARIABLE_DEF = 2602;
	//public static final int GRAM_STATEMENT_BLOCK = 3101;
	//public static final int GRAM_STATEMENT_BLOCK_EMPTY = 3102;
	
	public static final int GRAM_STATEMENT_IF_THEN = 2651;
	public static final int GRAM_STATEMENT_IF_THEN_ELSE = 2652;
	public static final int GRAM_STATEMENT_WHILE = 2653;
	public static final int GRAM_STATEMENT_TRY = 2654;
	public static final int GRAM_STATEMENT_CATCH = 2655;
	public static final int GRAM_STATEMENT_SWITCH = 2656;
			
	public static final int GRAM_STATEMENT_BREAK = 2661;
	public static final int GRAM_STATEMENT_BREAK_WITH_LABEL = 2662;
	public static final int GRAM_STATEMENT_CONTINUE = 2663;
	public static final int GRAM_STATEMENT_CONTINUE_WITH_LABEL = 2664;
	public static final int GRAM_STATEMENT_RETURN = 2665;
	public static final int GRAM_STATEMENT_RETURN_WITH_EXPRESSION = 2666;
	public static final int GRAM_STATEMENT_THROW = 2667;

	public static final int GRAM_STATEMENT_LABEL = 2671;
	public static final int GRAM_STATEMENT_CASE = 2672;
	public static final int GRAM_STATEMENT_DEFAULT = 2673;
	
	
	
	// Class stuff
	//
	
	public static final int TOKEN_CATEGORY_MODULE_STRUCTURE = 3000;

	
	public static final int TOKEN_KEYWORD_CLASS = 3021;
	public static final int TOKEN_KEYWORD_INTERFACE = 3022;
	public static final int TOKEN_KEYWORD_EXTENDS = 3023;
	public static final int TOKEN_KEYWORD_IMPLEMENTS = 3024;
	
	public static final int TOKEN_KEYWORD_IMPORT = 3031;
	public static final int TOKEN_KEYWORD_USING = 3032;
	public static final int TOKEN_KEYWORD_PACKAGE = 3033;
	public static final int TOKEN_KEYWORD_NAMESPACE = 3034;

	
	public static final int GRAM_CATEGORY_MODULE_STRUCTURE = 3050;
	
	public static final int GRAM_FUNCTION_INIT = 3051;
	public static final int GRAM_FUNCTION_DEF_SIMPLE = 3052;
	public static final int GRAM_FUNCTION_DEF_FULL = 3053;
	public static final int GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS = 3054;
	public static final int GRAM_FUNCTION_DEF_FULL_WITH_INIT = 3055;
	public static final int GRAM_FUNCTION_DEF_FULL_WITH_MODIFIERS_WITH_INIT
		= 3056;

	public static final int GRAM_VARIABLE_INIT = 3061;
	public static final int GRAM_VARIABLE_DEF_SIMPLE = 3062;
	public static final int GRAM_VARIABLE_DEF_FULL = 3063;
	public static final int GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS = 3064;
	public static final int GRAM_VARIABLE_DEF_FULL_WITH_INIT = 3065;
	public static final int GRAM_VARIABLE_DEF_FULL_WITH_MODIFIERS_WITH_INIT
		= 3066;
	//public static final int GRAM_VARIABLE_BLOCK = 3103;
	//public static final int GRAM_VARIABLE_BLOCK_EMPTY = 3104;	

	public static final int GRAM_CLASS_DEF = 3071;
	public static final int GRAM_CLASS_DEF_WITH_MODIFIERS = 3072;
	public static final int GRAM_MODULE_EXTENDS = 3073;
	public static final int GRAM_MODULE_IMPLEMENTS = 3074;
	public static final int GRAM_MODULE_EXTENDS_AND_IMPLEMENTS = 3075;
	//public static final int GRAM_CLASS_BLOCK = 3105;
	//public static final int GRAM_CLASS_BLOCK_EMPTY = 3106;
	
	public static final int GRAM_PACKAGE_DEF = 3081;
	public static final int GRAM_IMPORT_DEF = 3082;
	//public static final int GRAM_PACKAGE_BLOCK = 3107;
	//public static final int GRAM_PACKAGE_BLOCK_EMPTY = 3108;
	

	// Blocks
	//

	public static final int GRAM_CATEGORY_BLOCKS = 3100;

	public static final int GRAM_STATEMENT_BLOCK = 3101;
	public static final int GRAM_STATEMENT_BLOCK_EMPTY = 3102;
	public static final int GRAM_VARIABLE_BLOCK = 3103;
	public static final int GRAM_VARIABLE_BLOCK_EMPTY = 3104;
	public static final int GRAM_CLASS_BLOCK = 3105;
	public static final int GRAM_CLASS_BLOCK_EMPTY = 3106;
	public static final int GRAM_PACKAGE_BLOCK = 3107;
	public static final int GRAM_PACKAGE_BLOCK_EMPTY = 3108;

	
	// Access modifier keyword tokens
	//
	
	public static final int TOKEN_CATEGORY_ACCESS_MODIFIER = 3200;
	
	public static final int TOKEN_KEYWORD_PUBLIC = 3201;
	public static final int TOKEN_KEYWORD_INTERNAL = 3202;
	public static final int TOKEN_KEYWORD_PROTECTED = 3203;
	public static final int TOKEN_KEYWORD_PRIVATE = 3204;
	
	public static final int TOKEN_KEYWORD_CONSTANT = 3211;
	public static final int TOKEN_KEYWORD_FINAL = 3212;
	
	public static final int TOKEN_KEYWORD_STATIC = 3221;
	public static final int TOKEN_KEYWORD_VIRTUAL = 3222;
	public static final int TOKEN_KEYWORD_ABSTRACT = 3223;
	public static final int TOKEN_KEYWORD_OVERRIDE = 3224;
}
