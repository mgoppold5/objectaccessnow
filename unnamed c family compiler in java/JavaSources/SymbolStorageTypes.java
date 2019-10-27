/*
 * Copyright (c) 2017 Mike Goppold von Lobsdorf
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

public class SymbolStorageTypes {
	public static final int TYPE_UNINITIALIZED = 0;
	
	public static final int TYPE_TOKEN = 11;
	public static final int TYPE_GRAM = 12;
	
	public static final int TYPE_TOKEN_CONTAINER = 13;
	public static final int TYPE_GRAM_CONTAINER = 14;
	
	public static final int TYPE_TOKEN_FLOAT_FULL = 15;
	public static final int TYPE_TOKEN_INTEGER_FULL = 16;
	public static final int TYPE_TOKEN_INTEGER_SIMPLE = 17;

	public static final int TYPE_TOKEN_TABS_AND_SPACES = 21;

	public static final int TYPE_TOKEN_STRING = 22;
	public static final int TYPE_GRAM_CONTAINER_LIST = 23;
	public static final int TYPE_GRAM_SIMPLE_READER_STRING = 24;
}
