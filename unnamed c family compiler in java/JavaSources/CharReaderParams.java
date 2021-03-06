/*
 * Copyright (c) 2015-2016 Mike Goppold von Lobsdorf
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

public class CharReaderParams {
	public int id;
	
	public ReadBuffer readBuf;
	public ContextNode context;
	
	public TextIndex streamStartIndex;
	public TextIndex streamPastIndex;
	
	public TextIndex tiTemp1;
	public BufferIndex biTemp1;
	public TextIndex tiTemp2;
	public BufferIndex biTemp2;
	
	public void init() {
		tiTemp1 = new TextIndex();
		biTemp1 = new BufferIndex();
		tiTemp2 = new TextIndex();
		biTemp2 = new BufferIndex();
	}
}
