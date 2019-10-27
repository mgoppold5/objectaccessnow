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

public class ListUtils {
	public static void listExtend(CommonArrayList dst, CommonArrayList src) {
		int i;
		int count;
		
		i = 0;
		count = src.size();
		while(i < count) {
			dst.add(src.get(i));
			i += 1;
		}
		
		return;
	}
	
	public static void removeObjectFromList(CommonArrayList lst, Object o) {
		int i;
		int count;
		
		i = 0;
		count = lst.size();
		while(i < count) {
			if(o == lst.get(i)) {
				lst.removeAt(i);
				count = lst.size();
				continue;
			}
			
			i += 1;
		}
	}

	public static CommonInt32Array getFirstStringFromStringList(
		CommonArrayList strList) {
		
		int count;
		CommonInt32Array str;
		
		if(strList == null) return null;
		
		count = strList.size();
		
		if(count == 0) return null;
		
		str = (CommonInt32Array) strList.get(0);

		return str;
	}

	public static CommonInt32Array getLastStringFromStringList(
		CommonArrayList strList) {
		
		int count;
		CommonInt32Array str;
		
		if(strList == null) return null;
		
		count = strList.size();
		
		if(count == 0) return null;
		
		str = (CommonInt32Array) strList.get(count - 1);

		return str;
	}
}
