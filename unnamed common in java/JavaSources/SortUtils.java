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

package unnamed.common;

public class SortUtils {
	public static void int32StringBinaryLookupSimple(
		CommonArrayList store, CommonInt32Array name,
		SortParams sortRec) {
		
		TypeAndObject ent;
		CommonInt32Array entStr;
		
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compRes;
		
		compRes = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (TypeAndObject) store.get(i);
			entStr = (CommonInt32Array) ent.sortObject;
			
			StringUtils.int32StringCompareSimple(name, entStr, compRes);
			
			if(compRes.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compRes.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}
		
		// unreachable
	}

	public static void int16StringBinaryLookupSimple(
		CommonArrayList store, CommonInt16Array str,
		SortParams sortRec) {
		
		TypeAndObject ent;
		CommonInt16Array entStr;
		
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compRes;
		
		compRes = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (TypeAndObject) store.get(i);
			entStr = (CommonInt16Array) ent.sortObject;
			
			StringUtils.int16StringCompareSimple(str, entStr, compRes);
			
			if(compRes.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compRes.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}
		
		// unreachable
	}

	public static void int32BinaryLookupSimple(
		CommonArrayList store, int num,
		SortParams sortRec) {
		
		TypeAndObject ent;
		CommonInt32 entNum;
		
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compRes;
		
		compRes = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (TypeAndObject) store.get(i);
			entNum = (CommonInt32) ent.sortObject;
			
			StringUtils.int32CompareSimple(num, entNum.value, compRes);
			
			if(compRes.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compRes.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}
		
		// unreachable
	}
}