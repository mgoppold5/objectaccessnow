/*
 * Copyright (c) 2013-2014 Mike Goppold von Lobsdorf
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
 * This Java classs contains the data for XmlBaseReader.
 */

package unnamed.family.compiler;

import unnamed.common.*;

public class SimpleReaderNode {
	public int id;
	
	// general states
	public static final int STATE_FIRST_STEP = 1;
	public static final int STATE_AT_END = 2;
	public static final int STATE_WALKING_CHILDREN = 3;
	public static final int STATE_FINALIZE = 4;
	public static final int STATE_BEFORE_READ = 5;
	public static final int STATE_AFTER_READ = 6;
	
	int state;
	int miniState;
	int lastKnownDirection;
	
	public boolean decoration;
	
	public Symbol sym;
	public Throwable err;
}
