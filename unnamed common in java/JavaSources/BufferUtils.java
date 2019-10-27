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
 * This Java class contains functions which deal with buffers.
 */

package unnamed.common;

public class BufferUtils {
	private static final int BUFFER_MAX_POW = 28;
	private static final int BUFFER_GOOD_POW = 12;
	
	// unitSize is generally a global,
	// which once used to create buffers,
	// must be the same for all calls on those buffers
	
	// buffers made with different unitSize are incompatible
	
	private static void copyBufferToBuffer(
		CommonInt8Array dst, int dstPos,
		CommonInt8Array src, int srcPos,
		int len, int unitSize) {
		
		int i;
		int startBytes1;
		int startBytes2;
		int copyBytes;
		
		startBytes1 = srcPos * unitSize;
		startBytes2 = dstPos * unitSize;
		copyBytes = len * unitSize;
		
		i = 0;
		while(i < copyBytes) {
			dst.aryPtr[startBytes2 + i] = src.aryPtr[startBytes1 + i];
			
			i += 1;
		}
	}

	private static void copyElementToElement(
		BufferElement dst, int dstPos,
		BufferElement src, int srcPos,
		int len, int unitSize) {
		
		copyBufferToBuffer(dst.buf, dstPos,
			src.buf, srcPos,
			len, unitSize);
	}
	
	public static void growElementForAppend(BufferElement b,
		int len, int unitSize) {
		
		int space;
		int newCap;
		int bufferMax;
		
		if(b.used == 0) {
			b.start = 0;
		}
		
		space = b.capacity - b.start - b.used;
		if(len <= space) return;
		
		newCap = 2;
		bufferMax = 1 << BUFFER_MAX_POW;
		while(newCap < b.used + len) {
			newCap *= 2;
			if(newCap > bufferMax)
				throw new OutOfMemoryError();
		}
		
		//CommonInt8Array buffer2 = new byte[newCap * unitSize];
		CommonInt8Array buffer2 = CommonUtils.makeInt8Array(
			newCap * unitSize);

		if(b.used > 0) {
			copyBufferToBuffer(buffer2, 0,
				b.buf, b.start,
				b.used, unitSize);
		}
			
		b.buf = buffer2;
		b.start = 0;
		b.capacity = newCap;
		return;
	}

	public static void growElementForPrepend(BufferElement b,
		int len, int unitSize) {
		
		int space;
		int newCap;
		int bufferMax;
		
		if(b.used == 0) {
			b.start = b.capacity;
		}
		
		space = b.start;
		if(len <= space) return;

		newCap = 2;
		bufferMax = 1 << BUFFER_MAX_POW;
		while(newCap < b.used + len) {
			newCap *= 2;
			if(newCap > bufferMax)
				throw new OutOfMemoryError();
		}
		
		//CommonInt8Array buffer2 = new byte[newCap * unitSize];
		CommonInt8Array buffer2 = CommonUtils.makeInt8Array(
			newCap * unitSize);

		if(b.used > 0) {
			copyBufferToBuffer(buffer2, newCap - b.used,
				b.buf, b.start,
				b.used, unitSize);
		}
		
		b.buf = buffer2;
		b.start = newCap - b.used;
		b.capacity = newCap;
		return;
	}
	
	public static void growComboForAppend(BufferCombo bc,
		int len, int unitSize) {
		
		BufferElement be;
		int space;
		
		if(bc.length + len > (1 << BUFFER_MAX_POW))
			throw new OutOfMemoryError();
		
		if(bc.bufferList.size() > 0) {
			be = (BufferElement) bc.bufferList.get(
				bc.bufferList.size() - 1);

			if(be.used == 0) {
				growElementForAppend(be, len, unitSize);
				return;
			}

			space = be.capacity - be.start - be.used;
			if(space >= len) return;
			
			// if space is zero, then the element is set
			// up to be prepended, and we should start a
			// new element
			if(space != 0)
			// element meant to be appended
			if(be.used + len <= (1 << BUFFER_GOOD_POW)) {
				growElementForAppend(be, len, unitSize);
				return;
			}
		}
		
		be = makeBufferElement();
		growElementForAppend(be, len, unitSize);
		bc.bufferList.add(be);
		return;
	}
	
	public static void growComboForPrepend(BufferCombo bc,
		int len, int unitSize) {
		
		BufferElement be;
		int space;

		if(bc.length + len > (1 << BUFFER_MAX_POW))
			throw new OutOfMemoryError();

		if(bc.bufferList.size() > 0) {
			be = (BufferElement) bc.bufferList.get(0);

			if(be.used == 0) {
				growElementForPrepend(be, len, unitSize);
				return;
			}
			
			space = be.start;
			if(space >= len) return;
			
			// if space is zero, then the element is set
			// up to be appended, and we should start a new
			// element
			if(space != 0)
			// element meant to be prepended
			if(be.used + len <= (1 << BUFFER_GOOD_POW)) {
				growElementForPrepend(be, len, unitSize);
				return;
			}
		}
		
		be = makeBufferElement();		
		growElementForPrepend(be, len, unitSize);
		bc.bufferList.addAt(0, be);
		return;
	}
	
	public static int getComboLength(BufferCombo bc) {
		int len1;
		int i;
		int bufferListSize;
		BufferElement be;

		len1 = 0;
		i = 0;
		bufferListSize = bc.bufferList.size();
		while(i < bufferListSize) {
			be = (BufferElement) bc.bufferList.get(i);
		
			len1 += be.used;
			
			i += 1;
		}
		
		return len1;
	}
	
	public static BufferElement makeIntoOne(BufferCombo bc, int unitSize) {
		int len1;
		BufferElement beAccum;
		BufferElement be;
		int i;
		int iAccum;
		int bufferListSize;
		
		len1 = getComboLength(bc);
		
		if(len1 > (1 << BUFFER_MAX_POW))
			throw new OutOfMemoryError();
		
		beAccum = makeBufferElement();
		growElementForAppend(beAccum, len1, unitSize);
		
		i = 0;
		iAccum = 0;
		bufferListSize = bc.bufferList.size();
		while(i < bufferListSize) {
			be = (BufferElement) bc.bufferList.get(i);

			copyElementToElement(beAccum, beAccum.start + iAccum, be, be.start,
				be.used, unitSize);
			iAccum += be.used;
			
			i += 1;
		}
		
		beAccum.used = iAccum;
		return beAccum;
	}

	public static void appendBufferToElement(BufferElement beDest,
		CommonInt8Array buf, int bufLen, int unitSize) {
		
		growElementForAppend(beDest, bufLen, unitSize);
		
		copyBufferToBuffer(beDest.buf, beDest.start + beDest.used,
			buf, 0,
			bufLen, unitSize);
		
		beDest.used += bufLen;
		return;
	}

	public static void prependBufferToElement(BufferElement beDest,
		CommonInt8Array buf, int bufLen, int unitSize) {
		
		growElementForPrepend(beDest, bufLen, unitSize);
		
		if(beDest.start < bufLen)
			throw makeUnknownError(
				"allocated buffer isnt big enough");
		
		copyBufferToBuffer(beDest.buf, beDest.start - bufLen,
			buf, 0,
			bufLen, unitSize);
		
		beDest.start -= bufLen;
		beDest.used += bufLen;
		return;
	}
	
	public static void appendBufferToCombo(BufferCombo bcDest,
		CommonInt8Array buf, int bufLen, int unitSize) {
		
		BufferElement beDest;

		growComboForAppend(bcDest, bufLen, unitSize);
		
		if(bcDest.bufferList.size() == 0)
			throw makeUnknownError(
				"allocated buffer is gone");
					
		beDest = (BufferElement) bcDest.bufferList.get(
			bcDest.bufferList.size() - 1);

		copyBufferToBuffer(beDest.buf, beDest.start + beDest.used,
			buf, 0,
			bufLen, unitSize);
		
		beDest.used += bufLen;
		bcDest.length += bufLen;
		return;
	}

	public static void prependBufferToCombo(BufferCombo bcDest,
		CommonInt8Array buf, int bufLen, int unitSize) {
		
		BufferElement beDest;

		growComboForPrepend(bcDest, bufLen, unitSize);
		
		if(bcDest.bufferList.size() == 0)
			throw makeUnknownError(
				"allocated buffer is gone");
		
		beDest = (BufferElement) bcDest.bufferList.get(0);
		
		if(beDest.start < bufLen)
			throw makeUnknownError(
				"allocated buffer is not big enough");
			
		copyBufferToBuffer(beDest.buf, beDest.start - bufLen,
			buf, 0,
			bufLen, unitSize);
		
		beDest.start -= bufLen;
		beDest.used += bufLen;
		bcDest.length += bufLen;
		return;
	}
	
	public static void appendElementToCombo(BufferCombo bcDest,
		BufferElement be, int unitSize) {
		
		BufferElement beDest;
		
		growComboForAppend(bcDest, be.used, unitSize);
		
		if(bcDest.bufferList.size() == 0)
			throw makeUnknownError(
				"allocated buffer is gone");
			
		beDest = (BufferElement) bcDest.bufferList.get(
			bcDest.bufferList.size() - 1);
		
		copyElementToElement(beDest, beDest.start + beDest.used,
			be, be.start,
			be.used, unitSize);
		beDest.used += be.used;
		bcDest.length += be.used;
		return;
	}
	
	public static void prependElementToCombo(BufferCombo bcDest,
		BufferElement be, int unitSize) {
		
		BufferElement beDest;

		growComboForPrepend(bcDest, be.used, unitSize);
		
		if(bcDest.bufferList.size() == 0)
			throw makeUnknownError(
				"allocated buffer is gone");

		beDest = (BufferElement) bcDest.bufferList.get(0);
				
		if(beDest.start < be.used)
			throw makeUnknownError(
				"allocated buffer is not big enough");
			
		copyElementToElement(beDest, beDest.start - be.used,
			be, be.start,
			be.used, unitSize);
		beDest.start -= be.used;
		beDest.used += be.used;
		bcDest.length += be.used;
		return;
	}
	
	public static void appendComboToCombo(BufferCombo bcDest,
		BufferCombo bc, int unitSize) {
		
		BufferElement beAccum;
		BufferElement be;
		int len1;
		int len2;
		int i;
		int iAccum;
		int bufferListSize;
		
		len1 = getComboLength(bcDest);
		len2 = getComboLength(bc);
		
		if(len1 + len2 > (1 << BUFFER_MAX_POW))
			throw new OutOfMemoryError();
		
		growComboForAppend(bcDest, len2, unitSize);

		if(bcDest.bufferList.size() == 0)
			throw makeUnknownError(
				"allocated buffer is gone");
					
		beAccum = (BufferElement) bcDest.bufferList.get(
			bcDest.bufferList.size() - 1);
		
		iAccum = 0;
		i = 0;
		bufferListSize = bc.bufferList.size();
		while(i < bufferListSize) {
			be = (BufferElement) bc.bufferList.get(i);

			copyElementToElement(beAccum, beAccum.start + beAccum.used + iAccum,
				be, be.start,
				be.used, unitSize);
			iAccum += be.used;
			
			i += 1;
		}
		
		beAccum.used += iAccum;
		bcDest.length += iAccum;
		return;
	}

	public static void prependComboToCombo(BufferCombo bcDest,
		BufferCombo bc, int unitSize) {
	
		BufferElement beAccum;
		BufferElement be;
		int len1;
		int len2;
		int i;
		int iAccum;
		int bufferListSize;

		len1 = getComboLength(bcDest);
		len2 = getComboLength(bc);
		
		if(len1 + len2 > (1 << BUFFER_MAX_POW))
			throw new OutOfMemoryError();
		
		growComboForPrepend(bcDest, len2, unitSize);

		if(bcDest.bufferList.size() == 0)
			throw makeUnknownError(
				"allocated buffer is gone");
		
		beAccum = (BufferElement) bcDest.bufferList.get(0);
				
		if(beAccum.start < len2)
			throw makeUnknownError(
				"allocated buffer is not big enough");
			
		iAccum = 0;
		bufferListSize = bc.bufferList.size();
		i = bufferListSize;
		while(i > 0) {
			be = (BufferElement) bc.bufferList.get(i);
			
			copyElementToElement(beAccum, beAccum.start - iAccum - be.used,
				be, be.start,
				be.used, unitSize);
			iAccum += be.used;
			
			i += 1;
		}
		
		beAccum.start -= iAccum;
		beAccum.used += iAccum;
		bcDest.length += iAccum;
		return;
	}
	
	private static CommonError makeUnknownError(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_UNKNOWN;
		e1.msg = msg;
		return e1;
	}
	
	private static BufferElement makeBufferElement() {
		BufferElement be;
		
		be = new BufferElement();
		be.init();
		return be;
	}
}
