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

package unnamed.family.compiler;

import unnamed.common.*;

public class CursorAllocHelper {
	private static final int TYPE_GROUP = 1;
	private static final int TYPE_POOL = 2;
	
	private short growStepSize;
	
	private ElementAllocHelper elemHelp;
	private int elementId;
	
	private boolean haveInit;

	// GROUP_LEN must be greater than zero
	private static final int GROUP_LEN = 2;
	private CommonNode group;
	
	private int pos;
	
	
	public void initGrowStepSize(short size) {
		if(haveInit) makeIllegalState("already initialized");
		growStepSize = size;
	}

	public void initElementAllocHelper(
		ElementAllocHelper pElemHelp, int pElementId) {
		
		if(haveInit) throw makeIllegalState("already initialized");
		elemHelp = pElemHelp;
		elementId = pElementId;
	}
	
	public void init() {
		if(haveInit) throw makeIllegalState("already initialized");
		if(elemHelp == null) throw new NullPointerException();
		
		if(growStepSize < 1) growStepSize = 1;
		
		group = new CommonNode();
		group.theType = TYPE_GROUP;
		group.theObject = makeCommonNodeArray(GROUP_LEN + 1);
		
		haveInit = true;
	}

	public boolean getHaveInit() {return haveInit;}

	public int getCursorPosition() {return pos;}
	
	public void setCursorPosition(int pPos) {
		if(pPos > pos) throw new IndexOutOfBoundsException();
		pos = pPos;
	}

	public Object makeElement() {
		Object o;
		
		if(true) {
			while(true) {
				o = makeElementInner();

				if(o != null) {
					pos += 1;
					return o;
				}

				continue;
			}
		}
		
		return elemHelp.makeElement(elementId);
	}
	
	private Object makeElementInner() {
		int nodeNum;
		int prevPosLimit;
		int nextPosLimit;
		
		CommonNode[] groupArr = (CommonNode[]) group.theObject;
		CommonNode node;
		Object[] arr;
	
		nodeNum = 0;
		prevPosLimit = 0;
		nextPosLimit = 0;
		arr = null;
		while(nodeNum < GROUP_LEN) {
			if(groupArr[nodeNum] != null) {
				node = groupArr[nodeNum];
				arr = (Object[]) node.theObject;
				
				if(arr != null) {
					prevPosLimit = arr.length;
				}
			}
			
			if(pos < prevPosLimit) return arr[pos];
			
			if(groupArr[nodeNum] != null) {
				nodeNum += 1;
				continue;
			}
			
			nextPosLimit = prevPosLimit;
			if(nextPosLimit == 0) nextPosLimit = 1;
			nextPosLimit *= (1 << growStepSize);

			groupArr[nodeNum] = createPool(prevPosLimit, nextPosLimit);

			//prevPosLimit = nextPosLimit;
			continue;
		}
		
		nextPosLimit = prevPosLimit;
		if(nextPosLimit == 0) nextPosLimit = 1;
		nextPosLimit *= (1 << growStepSize);

		combinePools(prevPosLimit, nextPosLimit);
		return null;
	}
	
	private void combinePools(int prevPosLimit, int nextPosLimit) {
		int nodeNum;
		
		CommonNode[] groupArr = (CommonNode[]) group.theObject;
		CommonNode node;
		Object[] arr;
		
		nodeNum = GROUP_LEN;

		if(groupArr[nodeNum] != null) makeIllegalState(null);
		
		groupArr[nodeNum] = createPool(prevPosLimit, nextPosLimit);

		CommonNode combineNode = groupArr[nodeNum];
		Object[] combineArr = (Object[]) combineNode.theObject;
		
		nodeNum = 0;
		prevPosLimit = 0;
		while(nodeNum < GROUP_LEN) {
			node = groupArr[nodeNum];
			arr = (Object[]) node.theObject;
			nextPosLimit = arr.length;
			
			moveRefs(
				combineArr, prevPosLimit,
				arr, prevPosLimit,
				nextPosLimit - prevPosLimit);
			
			prevPosLimit = nextPosLimit;
			nodeNum += 1;
		}
		
		nodeNum = 0;
		while(nodeNum < GROUP_LEN) {
			groupArr[nodeNum] = null;
			nodeNum += 1;
		}
		
		groupArr[0] = groupArr[GROUP_LEN];
		groupArr[GROUP_LEN] = null;

		return;
	}
	
	private CommonNode createPool(int prevPosLimit, int nextPosLimit) {
		int i;
		Object[] arr;
		
		arr = makeObjectArray(nextPosLimit);
		i = prevPosLimit;
		while(i < nextPosLimit) {
			arr[i] = elemHelp.makeElement(elementId);
			i += 1;
		}
		
		CommonNode pool = new CommonNode();
		pool.theType = TYPE_POOL;
		pool.theObject = arr;
		
		return pool;
	}
	
	private void moveRefs(
		Object[] dst, int dstStart,
		Object[] src, int srcStart,
		int len) {
		
		int i;

		i = 0;
		while(i < len) {
			dst[dstStart + i] = src[srcStart + i];
			src[srcStart + i] = null;
			
			i += 1;
		}
		
		return;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg != null)
			return new IllegalStateException(msg);
		
		return new IllegalStateException();
	}
	
	public CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	public Object[] makeCommonNodeArray(int len) {
		return new CommonNode[len];
	}

	public Object[] makeObjectArray(int len) {
		return new Object[len];
	}
}
