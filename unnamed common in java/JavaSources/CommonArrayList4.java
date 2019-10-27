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

package unnamed.common;

public class CommonArrayList4
	extends BufferNode
	implements CommonArrayList {
	
	private boolean haveInit;
	
	private Object[] arrPtr;
	private int capacity;
	private int length;
	
	public void init() {
		if(haveInit) throw makeIllegalState("already initialized");
		haveInit = true;
	}
	
	public int size() {return length;}
	
	public Object get(int index) {
		if(index >= length)
			throw makeIndexOutOfBoundsException(null);

		return arrPtr[index];
	}

	public Object set(int index, Object o) {
		if(index >= length)
			throw makeIndexOutOfBoundsException(null);

		Object prevObj = arrPtr[index];
		arrPtr[index] = o;
		return prevObj;
	}
	
	public void add(Object o) {
		if(length < capacity) {
			arrPtr[length] = o;
			length += 1;
			return;
		}
		
		grow();

		if(length < capacity) {
			arrPtr[length] = o;
			length += 1;
			return;
		}
		
		throw makeIllegalState(null);
	}
	
	public void addAt(int index, Object o) {
		if(index > length)
			throw makeIndexOutOfBoundsException(null);
		
		if(length < capacity) {
			move2(
				arrPtr, index + 1,
				arrPtr, index,
				length - index);
			arrPtr[index] = o;
			length += 1;
			return;
		}
		
		grow();

		if(length < capacity) {
			move2(
				arrPtr, index + 1,
				arrPtr, index,
				length - index);
			arrPtr[index] = o;
			length += 1;
			return;
		}
		
		System.out.println("len," + length);
		System.out.println("cap," + capacity);

		throw makeIllegalState(null);
	}
	
	public void remove(Object o) {
		int i;
		
		i = 0;
		while(i < length) {
			if(o == arrPtr[i]) {
				removeAt(i);
				return;
			}
			
			i += 1;
		}
		
		throw makeObjectNotFound(null);
	}
	
	public void removeAt(int index) {
		if(index >= length)
			throw makeIndexOutOfBoundsException(null);

		arrPtr[index] = null;
		move1(
			arrPtr, index,
			arrPtr, index + 1,
			length - index - 1);
		length -= 1;

		return;
	}
	
	public void clear() {
		int i;
		
		i = 0;
		while(i < length) {
			arrPtr[i] = null;
			i += 1;
		}
		
		length = 0;
		return;
	}
	
	public Object[] toArray() {
		Object[] newArrPtr = new Object[length];
		copy1(newArrPtr, 0, arrPtr, 0, length);
		return newArrPtr;
	}
	
	private void grow() {
		int newCap;

		newCap = 2;
		while(newCap <= capacity)
			newCap *= 2;
		
		Object[] newArrPtr = new Object[newCap];
		move1(newArrPtr, 0, arrPtr, 0, length);
		arrPtr = newArrPtr;
		capacity = newCap;
		return;
	}
	
	private void move1(
		Object[] dst, int dstPos,
		Object[] src, int srcPos,
		int len) {
		
		int i;

		i = 0;
		while(i < len) {
			dst[dstPos + i] = src[srcPos + i];
			src[srcPos + i] = null;
			i += 1;
		}
		
		return;
	}

	private void move2(
		Object[] dst, int dstPos,
		Object[] src, int srcPos,
		int len) {
		
		int i;

		i = len;
		while(i > 0) {
			i -= 1;
			dst[dstPos + i] = src[srcPos + i];
			src[srcPos + i] = null;
		}
		
		return;
	}	
	
	private void copy1(
		Object[] dst, int dstPos,
		Object[] src, int srcPos,
		int len) {
		
		int i;

		i = 0;
		while(i < len) {
			dst[dstPos + i] = src[srcPos + i];
			i += 1;
		}
		
		return;
	}
	
	//public Iterator<Object> iterator() {
	//	return store.iterator();
	//}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}

	private CommonError makeObjectNotFound(String msg) {
		CommonError e3;
		
		e3 = new CommonError();
		e3.id = CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e3.msg = msg;
		return e3;
	}

	private RuntimeException makeIndexOutOfBoundsException(String msg) {
		if(msg != null) return new IndexOutOfBoundsException(msg);
		return new IndexOutOfBoundsException();
	}
}
