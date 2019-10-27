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

/*
 * This Java class contains functions which deal with path
 * strings.
 */

package unnamed.common;

public class PathUtils {
	//public static final int[] GENERAL_FILE_PATH_SEPARATOR_SET
	//	= {'\\', '/', ' '};
	
	//public static final int[] REGULAR_FILE_PATH_SEPARATOR_SET
	//	= {'\\', '/', ' ', '.'};
	
	//// ref: https://en.wikipedia.org/wiki/Filename#Reserved_characters_and_words
	//public static final int[] FILE_PATH_SPECIAL_CHAR_SET
	//	= {'?', '%', '*', ':', '|', '\"', '<', '>', ';'};
	
	// these are in addition to those for FILE_PATH
	//public static final int[] FILE_NAME_SPECIAL_CHAR_SET
	//	= {'\\', '/'};
	
	public static boolean isPurePath(CommonInt32Array path, CommonInt32Array separators) {
		int iChar;
		int charCount;
		int c;
		int nameLen;
		
		if(path == null)
			return true;
				
		charCount = path.length;
		
		iChar = 0;
		nameLen = 0;
		
		// this function returns false if the string
		// begins/ends with a separator, or if 2 separators
		// connect
		while(iChar < charCount) {
			c = path.aryPtr[iChar];
			
			if(!StringUtils.int32StringContainsCharSorted(separators, c)) {
				nameLen += 1;
				iChar += 1;
				continue;
			}
			
			if(nameLen == 0)
				// names should not be zero length
				return false;

			nameLen = 0;
			iChar += 1;
			continue;
		}
		
		if(nameLen == 0)
			// names should not be zero length
			return false;
		
		// path is good
		return true;
	}
	
	public static boolean pathHasBadChars(CommonInt32Array path, CommonInt32Array badChars) {
		int iChar;
		int charCount;
		int c;
		
		if(path == null)
			return false;
				
		charCount = path.length;
		
		iChar = 0;
		while(iChar < charCount) {
			c = path.aryPtr[iChar];
			
			if(StringUtils.int32StringContainsCharSorted(badChars, c))
				return true;
			
			iChar += 1;
		}
		
		// path is good
		return false;
	}
	
	/*
	public static boolean isGeneralFilePath(CommonInt32Array path) {
		int[] sepSet = StringUtils.int32StringSortChars(
			GENERAL_FILE_PATH_SEPARATOR_SET);
		if(!isPurePath(path, sepSet))
			return false;
		
		int[] badSet = StringUtils.int32StringSortChars(
			FILE_PATH_SPECIAL_CHAR_SET);
		if(pathHasBadChars(path, badSet))
			return false;
		
		return true;
	}

	public static boolean isRegularFilePath(CommonInt32Array path) {
		int[] sepSet = StringUtils.int32StringSortChars(
			REGULAR_FILE_PATH_SEPARATOR_SET);
		if(!isPurePath(path, sepSet))
			return false;

		int[] badSet = StringUtils.int32StringSortChars(
			FILE_PATH_SPECIAL_CHAR_SET);
		if(pathHasBadChars(path, badSet))
			return false;
		
		return true;
	}
	
	public static boolean isRegularFileName(CommonInt32Array name) {
		if(!isRegularFilePath(name)) 
			return false;
		
		int[] badSet = StringUtils.int32StringSortChars(
			FILE_NAME_SPECIAL_CHAR_SET);
		if(pathHasBadChars(name, badSet))
			return false;
		
		return true;
	}
	*/
	
	public static CommonInt32Array combineManyPaths(
		CommonArrayList pathList, int separatorChar) {
		
		
		int i;
		int iCount;
		int j;
		int jCount;
		int len1;
		int len2;
		CommonInt32Array retPath;
		CommonInt32Array srcPath;
		
		iCount = pathList.size();
		if(iCount == 0) return null;

		len1 = 0;
		i = 0;
		while(i < iCount) {
			if(i > 0) len1 += 1;
			srcPath = (CommonInt32Array) pathList.get(i);
			len1 += srcPath.length;
			i += 1;
		}
		
		//retPath = new int[len1];
		retPath = CommonUtils.makeInt32Array(len1);

		i = 0;
		len2 = 0;
		while(i < iCount) {
			if(i > 0) {
				retPath.aryPtr[len2] = separatorChar;
				len2 += 1;
			}
			
			srcPath = (CommonInt32Array) pathList.get(i);
			j = 0;
			jCount = srcPath.length;
			while(j < jCount) {
				retPath.aryPtr[len2 + j] = srcPath.aryPtr[j];
				j += 1;
			}
			
			len2 += jCount;
			i += 1;
		}
		
		if(len2 != len1)
			throw new IndexOutOfBoundsException();
		
		return retPath;
	}

	public static CommonInt32Array combine2OptionalPaths(
		CommonInt32Array path, CommonInt32Array extend, int separatorChar) {
		
		CommonArrayList pathList = makeArrayList();
		
		if(path != null) pathList.add(path);
		if(extend != null) pathList.add(extend);
		
		return combineManyPaths(pathList, separatorChar);
	}
	
	public static CommonArrayList splitPath(
		CommonInt32Array path, int separatorChar) {
		
		CommonArrayList dstPathList;
		int i;
		int j;
		int start;
		int span;
		int len;
		CommonInt32Array innerPath;
		
		dstPathList = makeArrayList();
		
		if(path == null) return dstPathList;
		
		i = 0;
		start = 0;
		len = path.length;
		while(i < len) {
			if(path.aryPtr[i] == separatorChar) {
				span = i - start;
				//innerPath = new int[span];
				innerPath = CommonUtils.makeInt32Array(span);
				j = 0;
				while(j < span) {
					innerPath.aryPtr[j] = path.aryPtr[start + j];
					j += 1;
				}
				
				dstPathList.add(innerPath);
				i += 1;
				start = i;
				continue;
			}
			
			i += 1;
		}
		
		span = i - start;
		//innerPath = new int[span];
		innerPath = CommonUtils.makeInt32Array(span);
		j = 0;
		while(j < span) {
			innerPath.aryPtr[j] = path.aryPtr[start + j];
			j += 1;
		}
		
		dstPathList.add(innerPath);

		return dstPathList;
	}

	private static CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
