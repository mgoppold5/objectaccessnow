/*
 * Copyright (c) 2015-2017 Mike Goppold von Lobsdorf
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

public class TokenStringEvalData extends BaseModuleData {
	// given
	//
	
	//public CharReaderParams charReadParams;
	public TokenString strTok;
	
	// output
	//
	
	public String resultJavaString;
	public CommonInt32Array resultInt32String;
	
	// states
	//
	
	public static final int STATE_HAVE_JAVA_STRING = 11;
	public static final int STATE_HAVE_INT32_STRING = 12;
}
