/*
 * Copyright (c) 2013-2017 Mike Goppold von Lobsdorf
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

public class CharReader2Data extends BaseModuleData {
	public CharReaderParams charReadParams;
	
	public int encoding; // CharacterEncoding
	public int codeUnitSize; // size in bytes
	
	public int resultChar;
	public int resultCharLength;
	
	public static final int STATE_END_OF_STREAM = 11;
	public static final int STATE_HAVE_CHAR = 12;
	public static final int STATE_HAVE_BAD_CHAR = 13;
	
	public static final int UNICODE_MAX_CODE_UNIT_PER_CODE_POINT = 6;
	
	public void init() {
		super.init();
		
		encoding = CharacterEncoding.UTF_8;
		codeUnitSize = CharacterEncodingUtils.getCodeUnitSize(encoding);
	}
}
	