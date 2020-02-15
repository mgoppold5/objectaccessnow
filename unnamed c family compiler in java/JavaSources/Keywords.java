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

public class Keywords {
	// General data keywords
	//
	
	public static final String KEYWORD_DATA_NULL = "null";
	public static final String KEYWORD_DATA_FALSE = "false";
	public static final String KEYWORD_DATA_TRUE = "true";

	
	// Grammar file keywords
	//

	public static final String KEYWORD_GRAMMAR_RULE_LEFT = "left";
	public static final String KEYWORD_GRAMMAR_RULE_RIGHT = "right";
	public static final String KEYWORD_GRAMMAR_REDUCE_GRAM = "reduce";
	public static final String KEYWORD_GRAMMAR_PRECEDENCE = "precedence";
	public static final String KEYWORD_GRAMMAR_PRECEDENCE_SHORT = "prec";
	public static final String KEYWORD_GRAMMAR_TOKEN = "token";
	public static final String KEYWORD_GRAMMAR_GRAM = "gram";
	public static final String KEYWORD_GRAMMAR_ID = "id";
	public static final String KEYWORD_GRAMMAR_PRECEDENCE_SPECTRUM = "precedence_spectrum";
	public static final String KEYWORD_GRAMMAR_PRECEDENCE_SPECTRUM_SHORT = "prec_spectrum";
	public static final String KEYWORD_GRAMMAR_ASSOCIATIVITY_LEFT = "left_assoc";
	public static final String KEYWORD_GRAMMAR_ASSOCIATIVITY_RIGHT = "right_assoc";
	public static final String KEYWORD_GRAMMAR_ASSOCIATIVITY_NON = "non_assoc";
	public static final String KEYWORD_GRAMMAR_ROOT_VARIABLE = "root_variable";
	public static final String KEYWORD_GRAMMAR_ROOT_VARIABLE_SHORT = "root_var";
	public static final String KEYWORD_GRAMMAR_END_MARKER = "end_marker";
	public static final String KEYWORD_GRAMMAR_END_MARKER_SHORT = "end_mark";
	public static final String KEYWORD_GRAMMAR_INCLUDE_GRAMMAR = "include_grammar";


	// Expression keywords
	//

	public static final String KEYWORD_EXPRESSION_NEW = "new";
	public static final String KEYWORD_EXPRESSION_INSTANCE_OF = "instanceof";
	public static final String KEYWORD_EXPRESSION_CAST = "cast";
	public static final String KEYWORD_PAREN_PARAMETERS = "p";
	public static final String KEYWORD_PAREN_PARAMETERS_DEFINITION = "pdef";
	public static final String KEYWORD_PAREN_EXPRESSION = "e";
	public static final String KEYWORD_PAREN_TYPE = "t";
	public static final String KEYWORD_PAREN_TYPE_PARAMETERS = "tp";
	public static final String KEYWORD_PAREN_TYPE_PARAMETERS_DEFINITION = "tpdef";
	public static final String KEYWORD_PAREN_ARRAY_ACCESS = "aa";
	public static final String KEYWORD_PAREN_ARRAY_INIT = "ai";
	public static final String KEYWORD_PAREN_LIST_ACCESS = "la";
	public static final String KEYWORD_PAREN_LIST_INIT = "li";
	public static final String KEYWORD_PAREN_DICT_ACCESS = "da";
	public static final String KEYWORD_PAREN_DICT_INIT = "di";
	

	// Statement keywords
	//

	public static final String KEYWORD_STATEMENT_IF = "if";
	public static final String KEYWORD_STATEMENT_ELSE = "else";
	public static final String KEYWORD_STATEMENT_WHILE = "while";
	public static final String KEYWORD_STATEMENT_TRY = "try";
	public static final String KEYWORD_STATEMENT_CATCH = "catch";
	public static final String KEYWORD_STATEMENT_SWITCH = "switch";
	public static final String KEYWORD_STATEMENT_BREAK = "break";
	public static final String KEYWORD_STATEMENT_CONTINUE = "continue";
	public static final String KEYWORD_STATEMENT_RETURN = "return";
	public static final String KEYWORD_STATEMENT_THROW = "throw";
	public static final String KEYWORD_STATEMENT_CASE = "case";
	public static final String KEYWORD_STATEMENT_DEFAULT = "default";


	// Module structure keywords
	//

	public static final String KEYWORD_MODULE_STRUCTURE_CLASS = "class";
	public static final String KEYWORD_MODULE_STRUCTURE_INTERFACE = "interface";
	public static final String KEYWORD_MODULE_STRUCTURE_EXTENDS = "extends";
	public static final String KEYWORD_MODULE_STRUCTURE_IMPLEMENTS = "implements";
	public static final String KEYWORD_MODULE_STRUCTURE_IMPORT = "import";
	public static final String KEYWORD_MODULE_STRUCTURE_USING = "using";
	public static final String KEYWORD_MODULE_STRUCTURE_PACKAGE = "package";
	public static final String KEYWORD_MODULE_STRUCTURE_NAMESPACE = "namespace";
	
	
	// Access modifier keywords
	//

	public static final String KEYWORD_ACCESS_MODIFIER_PUBLIC = "public";
	public static final String KEYWORD_ACCESS_MODIFIER_INTERNAL = "internal";
	public static final String KEYWORD_ACCESS_MODIFIER_PROTECTED = "protected";
	public static final String KEYWORD_ACCESS_MODIFIER_PRIVATE = "private";
	public static final String KEYWORD_ACCESS_MODIFIER_CONSTANT = "const";
	public static final String KEYWORD_ACCESS_MODIFIER_FINAL = "final";
	public static final String KEYWORD_ACCESS_MODIFIER_STATIC = "static";
	public static final String KEYWORD_ACCESS_MODIFIER_VIRTUAL = "virtual";
	public static final String KEYWORD_ACCESS_MODIFIER_ABSTRACT = "abstract";
	public static final String KEYWORD_ACCESS_MODIFIER_OVERRIDE = "override";
	
	
	// Old Include keywords
	//
	
	public static final String KEYWORD_OLD_INCLUDE_INCLUDE = "include";
}
