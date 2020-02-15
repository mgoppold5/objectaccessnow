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

import unnamed.common.*;

public class TokenUtils {
	public TypeAndObject[] cFamilyPunct2TokenMap;
	
	public TypeAndObject[] jsonPunct2TokenMap;
	public TypeAndObject[] jsonKeyword2TokenMap;
	
	public TypeAndObject[] grammarKeyword2TokenMap;
	
	public TypeAndObject[] cFamilyKeyword2TokenMap;
	
	public TypeAndObject[] cFamilyOldIncludeKeyword2TokenMap;
			
	public void init() {
		cFamilyPunct2TokenMap = initCFamilyPunct();
		jsonPunct2TokenMap = initJsonPunct();
		jsonKeyword2TokenMap = initJsonKeyword();
		grammarKeyword2TokenMap = initGrammarKeyword();
		cFamilyKeyword2TokenMap = initCFamilyKeyword();
		cFamilyOldIncludeKeyword2TokenMap = initCFamilyOldIncludeKeyword();
	}
	
	private TypeAndObject[] initCFamilyPunct() {
		CommonArrayList map = makeArrayList();
		SortParams sortRec = makeSortParams();
		sortRec.init();
		
		addCGeneralPunct(map, sortRec);
		addCMathAndLogicPunct(map, sortRec);
		
		return mapListToArray(map);
	}

	private TypeAndObject[] initJsonPunct() {
		CommonArrayList map = makeArrayList();
		SortParams sortRec = makeSortParams();
		
		addJsonGeneralPunct(map, sortRec);
		addJsonQuotingPunct(map, sortRec);
		
		return mapListToArray(map);
	}
	
	private TypeAndObject[] initJsonKeyword() {
		CommonArrayList map = makeArrayList();
		SortParams sortRec = makeSortParams();
		
		addJsonKeywords(map, sortRec);
		
		return mapListToArray(map);
	}

	private TypeAndObject[] initGrammarKeyword() {
		CommonArrayList map = makeArrayList();
		SortParams sortRec = makeSortParams();
		
		addGrammarKeywords(map, sortRec);
		
		return mapListToArray(map);
	}
	
	public TypeAndObject[] initCFamilyKeyword() {
		CommonArrayList map = makeArrayList();
		SortParams sortRec = makeSortParams();
		
		addAccessModifierKeywords(map, sortRec);
		addModuleStructureKeywords(map, sortRec);
		addStatementKeywords(map, sortRec);
		addDataKeywords(map, sortRec);
		addExpressionKeywords(map, sortRec);
		addParenthesisTypeKeywords(map, sortRec);
		
		return mapListToArray(map);
	}

	public TypeAndObject[] initCFamilyOldIncludeKeyword() {
		CommonArrayList map = makeArrayList();
		SortParams sortRec = makeSortParams();
		
		//addAccessModifierKeywords(map, sortRec);
		//addModuleStructureKeywords(map, sortRec);
		//addStatementKeywords(map, sortRec);
		//addDataKeywords(map, sortRec);
		//addExpressionKeywords(map, sortRec);
		//addParenthesisTypeKeywords(map, sortRec);
		
		addOldIncludeKeywords(map, sortRec);
		
		return mapListToArray(map);
	}
	
	
	
	private TypeAndObject newMatch(String str, int catId, int primaryId) {
		TypeAndObject entry;
		SymbolId spec;
				
		spec = makeSymbolId(2);
		spec.aryPtr[0] = catId;
		spec.aryPtr[1] = primaryId;
		
		CommonInt32Array nameStr = StringUtils.int32StringFromJavaString(str);
		
		entry = new TypeAndObject();
		entry.sortObject = nameStr;
		entry.theObject = spec;
		
		return entry;
	}
		
	private void addMatchSort(CommonArrayList map,
		TypeAndObject entry, SortParams sortRec) {
		
		CommonError e1;
				
		SortUtils.int32StringBinaryLookupSimple(
			map, (CommonInt32Array) entry.sortObject, sortRec);
		
		if(sortRec.foundExisting) {
			// duplicate
			
			e1 = new CommonError();
			e1.id = unnamed.common.CommonErrorTypes.ERROR_UNKNOWN;
			e1.msg = "string collision in set";
			throw e1;
		}

		map.addAt(sortRec.index, entry);
		return;
	}
	
	private void copyToArrayFromList(Object[] a1, CommonArrayList a2) {
		int len;
		int i;
		
		len = a2.size();
		if(len != a1.length)
			throw new IndexOutOfBoundsException();
		
		i = 0;
		while(i < len) {
			a1[i] = a2.get(i);
			
			i += 1;
		}
		
		return;
	}
	
	private TypeAndObject[] mapListToArray(CommonArrayList map) {
		TypeAndObject[] a;
		
		a = new TypeAndObject[map.size()];
		copyToArrayFromList(a, map);
		
		return a;
	}
		
	private void addJsonQuotingPunct(CommonArrayList map, SortParams sortRec) {
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_PUNCTUATION;
		
		entry = newMatch(Punctuation.PUNCT_GENERAL_QUOTE2,
			cat,
			Symbols.TOKEN_QUOTE);
		addMatchSort(map, entry, sortRec);
	}

	private void addJsonGeneralPunct(CommonArrayList map, SortParams sortRec) {
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_PUNCTUATION;
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_COMMA,
			cat,
			Symbols.TOKEN_COMMA);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_COLON,
			cat,
			Symbols.TOKEN_COLON);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_LBRACK,
			cat,
			Symbols.TOKEN_LBRACK);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_RBRACK,
			cat,
			Symbols.TOKEN_RBRACK);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_LBRACE,
			cat,
			Symbols.TOKEN_LBRACE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_RBRACE,
			cat,
			Symbols.TOKEN_RBRACE);
		addMatchSort(map, entry, sortRec);
	}
	
	private void addJsonKeywords(CommonArrayList map, SortParams sortRec) {
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_KEYWORDS_DATA;

		entry = newMatch(
			Keywords.KEYWORD_DATA_NULL,
			cat,
			Symbols.TOKEN_KEYWORD_NULL);
		addMatchSort(map, entry, sortRec);
	}

	private void addCGeneralPunct(CommonArrayList map, SortParams sortRec) {
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_PUNCTUATION;

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_COMMA,
			cat,
			Symbols.TOKEN_COMMA);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_COLON,
			cat,
			Symbols.TOKEN_COLON);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_SEMICOLON,
			cat,
			Symbols.TOKEN_SEMICOLON);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_DOT,
			cat,
			Symbols.TOKEN_DOT);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_QUESTION_MARK,
			cat,
			Symbols.TOKEN_QUESTION_MARK);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_LPAREN,
			cat,
			Symbols.TOKEN_LPAREN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_RPAREN,
			cat,
			Symbols.TOKEN_RPAREN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_LBRACK,
			cat,
			Symbols.TOKEN_LBRACK);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_RBRACK,
			cat,
			Symbols.TOKEN_RBRACK);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_GENERAL_LBRACE,
			cat,
			Symbols.TOKEN_LBRACE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_GENERAL_RBRACE,
			cat,
			Symbols.TOKEN_RBRACE);
		addMatchSort(map, entry, sortRec);
	}
	
	private void addCMathAndLogicPunct(CommonArrayList map, SortParams sortRec) {
		TypeAndObject entry;
		int cat;
		
		cat = Symbols.TOKEN_CATEGORY_MATH_AND_LOGIC;

		entry = newMatch(
			Punctuation.PUNCT_C_ASSIGN,
			cat,
			Symbols.TOKEN_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_PLUS,
			cat,
			Symbols.TOKEN_PLUS);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_MINUS,
			cat,
			Symbols.TOKEN_MINUS);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_TIMES,
			cat,
			Symbols.TOKEN_TIMES);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_DIVIDE,
			cat,
			Symbols.TOKEN_DIVIDE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_MOD,
			cat,
			Symbols.TOKEN_MOD);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_PLUS_ASSIGN,
			cat,
			Symbols.TOKEN_PLUS_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_MINUS_ASSIGN,
			cat,
			Symbols.TOKEN_MINUS_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_TIMES_ASSIGN,
			cat,
			Symbols.TOKEN_TIMES_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_DIVIDE_ASSIGN,
			cat,
			Symbols.TOKEN_DIVIDE_ASSIGN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_MOD_ASSIGN,
			cat,
			Symbols.TOKEN_MOD_ASSIGN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_INCREMENT,
			cat,
			Symbols.TOKEN_INCREMENT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_DECREMENT,
			cat,
			Symbols.TOKEN_DECREMENT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_AND_BITWISE,
			cat,
			Symbols.TOKEN_AND_BITWISE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_OR_BITWISE,
			cat,
			Symbols.TOKEN_OR_BITWISE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_XOR_BITWISE,
			cat,
			Symbols.TOKEN_XOR_BITWISE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_AND_BITWISE_ASSIGN,
			cat,
			Symbols.TOKEN_AND_BITWISE_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_OR_BITWISE_ASSIGN,
			cat,
			Symbols.TOKEN_OR_BITWISE_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_XOR_BITWISE_ASSIGN,
			cat,
			Symbols.TOKEN_XOR_BITWISE_ASSIGN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_NOT_BITWISE,
			cat,
			Symbols.TOKEN_NOT_BITWISE);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_SHIFT_LEFT,
			cat,
			Symbols.TOKEN_SHIFT_LEFT);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_SHIFT_RIGHT,
			cat,
			Symbols.TOKEN_SHIFT_RIGHT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_SHIFT_RIGHT_LOGICAL,
			cat,
			Symbols.TOKEN_SHIFT_RIGHT_LOGICAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_SHIFT_LEFT_ASSIGN,
			cat,
			Symbols.TOKEN_SHIFT_LEFT_ASSIGN);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_SHIFT_RIGHT_ASSIGN,
			cat,
			Symbols.TOKEN_SHIFT_RIGHT_ASSIGN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_SHIFT_RIGHT_LOGICAL_ASSIGN,
			cat,
			Symbols.TOKEN_SHIFT_RIGHT_LOGICAL_ASSIGN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_AND_LOGICAL,
			cat,
			Symbols.TOKEN_AND_LOGICAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_OR_LOGICAL,
			cat,
			Symbols.TOKEN_OR_LOGICAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_NOT_LOGICAL,
			cat,
			Symbols.TOKEN_NOT_LOGICAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_EQUAL,
			cat,
			Symbols.TOKEN_EQUAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_NOT_EQUAL,
			cat,
			Symbols.TOKEN_NOT_EQUAL);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_LESS,
			cat,
			Symbols.TOKEN_LESS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_LESS_EQUAL,
			cat,
			Symbols.TOKEN_LESS_EQUAL);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Punctuation.PUNCT_C_GREATER,
			cat,
			Symbols.TOKEN_GREATER);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Punctuation.PUNCT_C_GREATER_EQUAL,
			cat,
			Symbols.TOKEN_GREATER_EQUAL);
		addMatchSort(map, entry, sortRec);
	}

	private void addGrammarKeywords(CommonArrayList map, SortParams sortRec) {
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_KEYWORDS_GRAMMAR;

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_TOKEN,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_TOKEN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_GRAM,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_GRAM);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_ID,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_ID);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_ASSOCIATIVITY_LEFT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_LEFT);
		addMatchSort(map, entry, sortRec);
	
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_ASSOCIATIVITY_RIGHT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_RIGHT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_ASSOCIATIVITY_NON,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_ASSOCIATIVITY_NON);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_RULE_LEFT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_RULE_LEFT);
		addMatchSort(map, entry, sortRec);
	
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_RULE_RIGHT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_RULE_RIGHT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_ROOT_VARIABLE,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_ROOT_VARIABLE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_ROOT_VARIABLE_SHORT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_ROOT_VARIABLE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_END_MARKER,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_END_MARKER);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_END_MARKER_SHORT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_END_MARKER);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_PRECEDENCE_SPECTRUM,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_PRECEDENCE_SPECTRUM);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_PRECEDENCE_SPECTRUM_SHORT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_PRECEDENCE_SPECTRUM);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_REDUCE_GRAM,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_REDUCE_GRAM);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_PRECEDENCE,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_PRECEDENCE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_PRECEDENCE_SHORT,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_PRECEDENCE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_GRAMMAR_INCLUDE_GRAMMAR,
			cat,
			Symbols.TOKEN_GRAMMAR_KEYWORD_INCLUDE_GRAMMAR);
		addMatchSort(map, entry, sortRec);
	}	

	private void addAccessModifierKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_ACCESS_MODIFIER;

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_PUBLIC,
			cat,
			Symbols.TOKEN_KEYWORD_PUBLIC);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_INTERNAL,
			cat,
			Symbols.TOKEN_KEYWORD_INTERNAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_PROTECTED,
			cat,
			Symbols.TOKEN_KEYWORD_PROTECTED);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_PRIVATE,
			cat,
			Symbols.TOKEN_KEYWORD_PRIVATE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_STATIC,
			cat,
			Symbols.TOKEN_KEYWORD_STATIC);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_OVERRIDE,
			cat,
			Symbols.TOKEN_KEYWORD_OVERRIDE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_FINAL,
			cat,
			Symbols.TOKEN_KEYWORD_FINAL);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_CONSTANT,
			cat,
			Symbols.TOKEN_KEYWORD_CONSTANT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_ABSTRACT,
			cat,
			Symbols.TOKEN_KEYWORD_ABSTRACT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_ACCESS_MODIFIER_VIRTUAL,
			cat,
			Symbols.TOKEN_KEYWORD_VIRTUAL);
		addMatchSort(map, entry, sortRec);
	}	

	private void addModuleStructureKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_MODULE_STRUCTURE;

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_CLASS,
			cat,
			Symbols.TOKEN_KEYWORD_CLASS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_INTERFACE,
			cat,
			Symbols.TOKEN_KEYWORD_INTERFACE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_EXTENDS,
			cat,
			Symbols.TOKEN_KEYWORD_EXTENDS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_IMPLEMENTS,
			cat,
			Symbols.TOKEN_KEYWORD_IMPLEMENTS);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_IMPORT,
			cat,
			Symbols.TOKEN_KEYWORD_IMPORT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_PACKAGE,
			cat,
			Symbols.TOKEN_KEYWORD_PACKAGE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_USING,
			cat,
			Symbols.TOKEN_KEYWORD_USING);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_MODULE_STRUCTURE_NAMESPACE,
			cat,
			Symbols.TOKEN_KEYWORD_NAMESPACE);
		addMatchSort(map, entry, sortRec);
	}	

	private void addStatementKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_STATEMENT;

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_IF,
			cat,
			Symbols.TOKEN_KEYWORD_IF);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_ELSE,
			cat,
			Symbols.TOKEN_KEYWORD_ELSE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_WHILE,
			cat,
			Symbols.TOKEN_KEYWORD_WHILE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_TRY,
			cat,
			Symbols.TOKEN_KEYWORD_TRY);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_CATCH,
			cat,
			Symbols.TOKEN_KEYWORD_CATCH);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_SWITCH,
			cat,
			Symbols.TOKEN_KEYWORD_SWITCH);
		addMatchSort(map, entry, sortRec);
		
		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_BREAK,
			cat,
			Symbols.TOKEN_KEYWORD_BREAK);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_CONTINUE,
			cat,
			Symbols.TOKEN_KEYWORD_CONTINUE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_RETURN,
			cat,
			Symbols.TOKEN_KEYWORD_RETURN);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_THROW,
			cat,
			Symbols.TOKEN_KEYWORD_THROW);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_CASE,
			cat,
			Symbols.TOKEN_KEYWORD_CASE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_STATEMENT_DEFAULT,
			cat,
			Symbols.TOKEN_KEYWORD_DEFAULT);
		addMatchSort(map, entry, sortRec);
	}

	private void addDataKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_KEYWORDS_DATA;

		entry = newMatch(
			Keywords.KEYWORD_DATA_FALSE,
			cat,
			Symbols.TOKEN_KEYWORD_FALSE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_DATA_TRUE,
			cat,
			Symbols.TOKEN_KEYWORD_TRUE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_DATA_NULL,
			cat,
			Symbols.TOKEN_KEYWORD_NULL);
		addMatchSort(map, entry, sortRec);
	}	

	private void addExpressionKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_EXPRESSION;

		entry = newMatch(
			Keywords.KEYWORD_EXPRESSION_NEW,
			cat,
			Symbols.TOKEN_KEYWORD_NEW);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_EXPRESSION_INSTANCE_OF,
			cat,
			Symbols.TOKEN_KEYWORD_INSTANCE_OF);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_EXPRESSION_CAST,
			cat,
			Symbols.TOKEN_KEYWORD_CAST);
		addMatchSort(map, entry, sortRec);
	}

	private void addParenthesisTypeKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_EXPRESSION;

		entry = newMatch(
			Keywords.KEYWORD_PAREN_PARAMETERS,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_PARAMETERS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_PARAMETERS_DEFINITION,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_PARAMETERS_DEFINITION);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_EXPRESSION,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_EXPRESSION);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_TYPE,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_TYPE);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_TYPE_PARAMETERS,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_TYPE_PARAMETERS_DEFINITION,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_TYPE_PARAMETERS_DEFINITION);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_ARRAY_ACCESS,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_ARRAY_ACCESS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_ARRAY_INIT,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_ARRAY_INIT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_LIST_ACCESS,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_LIST_ACCESS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_LIST_INIT,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_LIST_INIT);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_DICT_ACCESS,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_DICT_ACCESS);
		addMatchSort(map, entry, sortRec);

		entry = newMatch(
			Keywords.KEYWORD_PAREN_DICT_INIT,
			cat,
			Symbols.TOKEN_KEYWORD_PAREN_DICT_INIT);
		addMatchSort(map, entry, sortRec);
	}

	private void addOldIncludeKeywords(
		CommonArrayList map, SortParams sortRec) {
		
		TypeAndObject entry;
		int cat;

		cat = Symbols.TOKEN_CATEGORY_KEYWORDS_OLD_INCLUDE;

		entry = newMatch(
			Keywords.KEYWORD_OLD_INCLUDE_INCLUDE,
			cat,
			Symbols.TOKEN_OLD_INCLUDE_KEYWORD_INCLUDE);
		addMatchSort(map, entry, sortRec);
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;

		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}
	
	public SymbolId makeSymbolId(int idLen) {
		SymbolId symId = new SymbolId();
		
		symId.aryPtr = new int[idLen];
		symId.capacity = idLen;
		symId.length = idLen;
		
		return symId;
	}
}
