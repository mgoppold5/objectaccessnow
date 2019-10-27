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

package unnamed.common;

import java.util.ArrayList;
import java.util.Iterator;

public class CommonArrayList2 
	implements CommonArrayList, Iterable<Object> {
	
	private ArrayList<Object> store;
	
	public void init() {
		store = new ArrayList<Object>();
	}
	
	public int size() {
		return store.size();
	}
	
	public Object get(int index) {
		return store.get(index);
	}

	public Object set(int index, Object o) {
		return store.set(index, o);
	}
	
	public void add(Object o) {
		store.add(o);
	}
	
	public void addAt(int index, Object o) {
		store.add(index, o);
	}
	
	public void remove(Object o) {
		store.remove(o);
	}
	
	public void removeAt(int index) {
		store.remove(index);
	}
	
	public void clear() {
		store.clear();
	}
	
	public Object[] toArray() {
		return store.toArray();
	}
	
	public Iterator<Object> iterator() {
		return store.iterator();
	}
}
