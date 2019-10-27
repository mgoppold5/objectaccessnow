/*
 * Copyright (c) 2016 Mike Goppold von Lobsdorf
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

package unnamed.common;

import java.util.ArrayList;

public class JavaListUtils {
	public static ArrayList<Object> listToJavaList(
		CommonArrayList src) {
		
		ArrayList<Object> dst;
		int i;
		int count;
		
		if(src == null) return null;

		dst = new ArrayList<Object>();
		i = 0;
		count = src.size();
		while(i < count) {
			dst.add(src.get(i));
			i += 1;
		}
		
		return dst;
	}
	
	public static ArrayList<String> listToJavaStringList(
		CommonArrayList src) {
		
		ArrayList<String> dst;
		int i;
		int count;
		
		if(src == null) return null;

		dst = new ArrayList<String>();
		i = 0;
		count = src.size();
		while(i < count) {
			dst.add((String) src.get(i));
			i += 1;
		}
		
		return dst;
	}
	
	public static CommonArrayList oldJavaStringListToList(
		ArrayList src) {
		
		CommonArrayList dst;
		int i;
		int count;
		
		if(src == null) return null;
		
		dst = makeArrayList();
		i = 0;
		count = src.size();
		while(i < count) {
			dst.add((String) src.get(i));
			i += 1;
		}
		
		return dst;
	}
	
	private static CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
