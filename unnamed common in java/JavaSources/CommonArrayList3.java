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

public class CommonArrayList3
	extends BufferNode
	implements CommonArrayList {
	
	private short minBlockSize;
	private short growStepSize;
	private short shrinkStepSize;

	private CommonArrayList3 ranges;

	private boolean haveInit;
	
	public void initMinBlockSize(short size) {
		if(haveInit) makeIllegalState("already initialized");
		minBlockSize = size;
	}
	
	public void initGrowStepSize(short size) {
		if(haveInit) makeIllegalState("already initialized");
		growStepSize = size;
	}
	
	public void initShrinkStepSize(short size) {
		if(haveInit) makeIllegalState("already initialized");
		shrinkStepSize = size;
	}
	
	public void initEnableLarge(boolean large) {
		if(haveInit) throw makeIllegalState("already initialized");
		
		if(large) {
			ranges = new CommonArrayList3();
			ranges.initMinBlockSize((short) 2);
			ranges.initGrowStepSize((short) 2);
			ranges.initShrinkStepSize((short) 0);
			ranges.initEnableLarge(false);
			ranges.init();
		}
		
		if(!large) ranges = null;
	}

	public void init() {
		if(haveInit) throw makeIllegalState("already initialized");
		haveInit = true;
	}
	
	public int size() {
		int i;
		int len;
		int total;
		
		total = 0;
		
		if(ranges != null) {
			i = 0;
			len = ranges.size();
			while(i < len) {
				BufferNode bn = (BufferNode) ranges.get(i);
				total += bn.used;
				i += 1;
			}
		}

		if(ranges == null) {
			total += used;
		}
		
		return total;
	}
	
	public Object get(int index) {
		int i;
		int len;
		int total;
		
		total = 0;

		if(ranges != null) {
			i = 0;
			len = ranges.size();
			while(i < len) {
				BufferNode bn = (BufferNode) ranges.get(i);
				
				if(index < total + bn.used) {
					Object[] arr = (Object[]) theObject;
					return arr[index - total];
				}
				
				total += bn.used;
				i += 1;
			}
			
			throw new IndexOutOfBoundsException(null);
		}

		if(ranges == null) {
			if(index < total + used) {
				Object[] arr = (Object[]) theObject;
				return arr[index - total];
			}
			
			total += used;
			
			throw new IndexOutOfBoundsException(null);
		}

		throw new IllegalStateException();
	}

	public Object set(int index, Object o) {
		int i;
		int len;
		int total;
		
		total = 0;

		if(ranges != null) {
			i = 0;
			len = ranges.size();
			while(i < len) {
				BufferNode bn = (BufferNode) ranges.get(i);
				
				if(index < total + bn.used) {
					Object[] arr = (Object[]) theObject;
					arr[index - total] = o;
				}
				
				total += bn.used;
				i += 1;
			}
			
			throw new IndexOutOfBoundsException(null);
		}

		if(ranges == null) {
			if(index < total + used) {
				Object[] arr = (Object[]) theObject;
				arr[index - total] = o;
			}
			
			total += used;
			
			throw new IndexOutOfBoundsException(null);
		}

		throw new IllegalStateException();
	}
	
	public void add(Object o) {
		//store.add(o);
	}
	
	public void addAt(int index, Object o) {
		//store.add(index, o);
	}
	
	public void remove(Object o) {
		//store.remove(o);
	}
	
	public void removeAt(int index) {
		//store.remove(index);
	}
	
	public void clear() {
		//store.clear();
	}
	
	public Object[] toArray() {
		//return store.toArray();
		return null;
	}
	
	//public Iterator<Object> iterator() {
	//	return store.iterator();
	//}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}
}
