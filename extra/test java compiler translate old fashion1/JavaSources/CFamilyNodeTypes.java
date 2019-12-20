/*
 * Copyright (c) 2016-2017 Mike Goppold von Lobsdorf
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

public class CFamilyNodeTypes {
	// major types
	public static final int TYPE_UNSPECIFIED = 0;
	public static final int TYPE_NONE = 1;
	//public static final int TYPE_CHUNK = 2;
	public static final int TYPE_DEFINITION = 3;
	public static final int TYPE_IDENTIFIER_REF = 4;
	public static final int TYPE_IMPORT = 5;
	public static final int TYPE_EXPRESSION = 6;
	//public static final int TYPE_ERA_CONTAINER = 7;

	
	// DEFINITION sub types
	//
	
	public static final int TYPE_CHUNK_STACK = 21;
	public static final int TYPE_CHUNK_NAMESPACE = 22;
	public static final int TYPE_SPECIAL_NAMESPACE = 23;

	public static final int TYPE_PACKAGE = 31;
	public static final int TYPE_CLASS = 32;
	public static final int TYPE_INTERFACE = 33;
	public static final int TYPE_VARIABLE = 34;
	public static final int TYPE_FUNCTION = 35;
	
	public static final int TYPE_VOID = 41;
	public static final int TYPE_ALIAS = 42;
	public static final int TYPE_POWER2_NUMBER = 43;
	public static final int TYPE_POWER2_ARRAY = 44;
	public static final int TYPE_REF_ARRAY = 45;
	public static final int TYPE_GENERICS_CLASS = 46;
	
	//public static final int TYPE_VOID = 31;
	//public static final int TYPE_ALIAS = 32;
	//public static final int TYPE_INTEGER = 33;
	//public static final int TYPE_STRING = 34;
	//public static final int TYPE_POWER2 = 35;
	//public static final int TYPE_NUMBER_FORMAT = 36;
	
	//public static final int TYPE_POLY_NAMESPACE = 41;
	//public static final int TYPE_POLY_NAME = 42;

	//// POLY_NAME sub types
	//public static final int TYPE_POWER2_NUMBER_FORMAT = 51;
	//public static final int TYPE_ARRAY = 52;
	//public static final int TYPE_GENERICS_CLASS = 53;

	
	// IDENTIFIER_REF sub types
	public static final int TYPE_IDENTIFIER_USE = 51;
	public static final int TYPE_IDENTIFIER_GET = 52;
	public static final int TYPE_IDENTIFIER_SET = 53;
	public static final int TYPE_IDENTIFIER_GET_AND_SET = 54;

	// IMPORT sub types
	public static final int TYPE_IMPORT_PARAMETER = 61;
	public static final int TYPE_IMPORT_PACKAGE_THING = 62;
	public static final int TYPE_IMPORT_PACKAGE_EVERYTHING = 63;

	//// ERA_CONTAINER sub types
	//public static final int TYPE_ERA_CONTAINER_LEVEL1 = 61;
	//public static final int TYPE_ERA_CONTAINER_LEVEL2 = 62;

	//// CHUNK sub types
	//public static final int TYPE_CHUNK_STACK = 71;
	//public static final int TYPE_CHUNK_NAMESPACE = 72;
}
