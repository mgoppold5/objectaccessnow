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
 * This object stores handles.
 */

package unnamed.file.system;

import unnamed.common.*;

public class HandleSet {
	public static final int INVALID_HANDLE = 0;

	private CommonArrayList a;
	
	private static final int HANDLE_MAX = 100 * 1024;
	private static final int FIRST_HANDLE = 1;
	
	public void init() {
		a = makeArrayList();
	}
	
	public int alloc(Object newObject) {
		int i;
		int h;
		HandleRecord rec;
		
		if(newObject == null)
			throw makeNullPointer(
				"To make a handle, object must not be null");
		
		i = a.size();
		if(i == 0) {
			h = FIRST_HANDLE;
			rec = new HandleRecord();
			rec.handleNumber = h;
			rec.objectRef = newObject;
			a.add(rec);
			return h;
		}
		
		i -= 1;
		rec = (HandleRecord) a.get(i);
		h = rec.handleNumber;
		h += 1;
		if(h < HANDLE_MAX) {
			rec = new HandleRecord();
			rec.handleNumber = h;
			rec.objectRef = newObject;
			a.add(rec);
			return h;
		}
		
		i = 0;
		h = 1;
		while(i < a.size()) {
			rec = (HandleRecord) a.get(i);
			if(h < rec.handleNumber) {
				rec = new HandleRecord();
				rec.handleNumber = h;
				rec.objectRef = newObject;
				a.addAt(i, rec);
				return h;
			}
			
			if(h == rec.handleNumber) {
				// number already taken
				h += 1;
				i += 1;
				continue;
			}
			
			// h < theInt.value, never happens
			i += 1;
		}
		
		throw new OutOfMemoryError();
	}
	
	public void free(int h) {
		int tooHigh;
		int tooLow;
		int guess;
		HandleRecord rec;
		int existingH;
		
		tooHigh = a.size();
		tooLow = 0;
		
		while(true) {
			if(tooLow == tooHigh)
				throw makeInvalidHandle();

			guess = (tooHigh + tooLow) / 2;
			
			rec = (HandleRecord) a.get(guess);
			existingH = rec.handleNumber;
			
			if(existingH > h) {
				tooHigh = guess;
				continue;
			}
			
			if(existingH < h) {
				tooLow = guess;
				continue;
			}
			
			// found our handle
			a.removeAt(guess);
			return;
		}
	}
	
	public Object getObject(int h) {
		int tooHigh;
		int tooLow;
		int guess;
		HandleRecord rec;
		int existingH;
		
		tooHigh = a.size();
		tooLow = 0;
		
		while(true) {
			if(tooLow == tooHigh)
				throw makeInvalidHandle();

			guess = (tooHigh + tooLow) / 2;
			
			rec = (HandleRecord) a.get(guess);
			existingH = rec.handleNumber;
			
			if(existingH > h) {
				tooHigh = guess;
				continue;
			}
			
			if(existingH < h) {
				tooLow = guess;
				continue;
			}
			
			// found our record
			return rec.objectRef;
		}
	}
	
	public int getHandleAtIndex(int index) {
		HandleRecord rec = (HandleRecord) a.get(index);
		return rec.handleNumber;
	}
	
	public int count() {
		return a.size();
	}

	private CommonError makeInvalidHandle() {
		CommonError e1;
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_HANDLE;
		return e1;
	}
	
	private RuntimeException makeNullPointer(String msg) {
		if(msg == null) return new NullPointerException();
		
		return new NullPointerException(msg);
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
