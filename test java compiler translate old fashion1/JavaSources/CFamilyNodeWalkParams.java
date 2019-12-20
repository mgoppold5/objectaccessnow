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

public class CFamilyNodeWalkParams {
	public static final int ACTION_COLLECT_NAMES = 1;
	public static final int ACTION_COLLECT_TYPES = 2;

	public static final int EXTENSION_VARIETY_EXTENDS = 1;
	public static final int EXTENSION_VARIETY_IMPLEMENTS = 2;
	public static final int EXTENSION_VARIETY_EXTENDS_AND_IMPLEMENTS = 3;
	
	// walking state variables
	public int majorAction;
	public int extensionVariety;
	public boolean existsContent;
	public CFamilyNode2 packageForFile;
}
