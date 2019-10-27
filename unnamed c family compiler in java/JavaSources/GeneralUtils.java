/*
 * Copyright (c) 2013-2017 Mike Goppold von Lobsdorf
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

public class GeneralUtils {
	public CommonInt32Array lowerBitsMask;
	public CommonInt64Array lowerBitsMask64;

	public void init() {
		lowerBitsMask = BitMaskUtils.lowerBitsInit32((short) 32);
		lowerBitsMask64 = BitMaskUtils.lowerBitsInit64((short) 64);
	}
	
	public boolean isXmlChar(int fullChar, boolean strict) {
		/* is a Char according to the XMl 1.1 spec.
		restricted characters are discouraged, 
		so if strict is true, restricted characters fail */
			
		if((fullChar >= 0x1) && (fullChar <= 0xD7FF)) {
			if(!strict) return true;
			
			if(fullChar <= 0x1F) {
				if((fullChar >= 0x1) && (fullChar <= 0x8))
					return false;
				
				if((fullChar >= 0xB) && (fullChar <= 0xC))
					return false;
				
				if((fullChar >= 0xE) && (fullChar <= 0x1F))
					return false;
				
				return true;
			}
			
			if(fullChar >= 0x7f) {
				if((fullChar >= 0x7F) && (fullChar <= 0x84))
					return false;
				
				if((fullChar >= 0x86) && (fullChar <= 0x9F))
					return false;
			
				return true;
			}
			
			return true;
		}
		
		if((fullChar >= 0xE000) && (fullChar <= 0xFFFD)) {
			if(!strict) return true;
			
			if((fullChar >= 0xFDD0) && (fullChar <= 0xFDDF))
				return false;
			
			return true;
		}
		
		if((fullChar >= 0x10000) && (fullChar <= 0x10FFFF)) {
			if(!strict) return true;
			
			int repeatedRange = fullChar & 0xFFFF;
			
			if((repeatedRange >= 0xFFFE) && (repeatedRange <= 0xFFFF))
				return false;
			
			return true;
		}
		
		return false;
	}

	public boolean isXmlNameStartChar(int fullChar, boolean strict) {
		// is a NameStartChar according to the XMl 1.1 spec
		
		if(fullChar <= 0x200D) {
			if(fullChar < 0xC0) {
				if((fullChar >= 'A') && (fullChar <= 'Z'))
					return true;
				
				if((fullChar >= 'a') && (fullChar <= 'z'))
					return true;
				
				switch(fullChar) {
				case ':':
					return true;
				case '_':
					return true;
				}
				
				return false;
			}
			
			if(fullChar <= 0x2FF) {		
				if((fullChar >= 0xC0) && (fullChar <= 0xD6))
					return true;
				
				if((fullChar >= 0xD8) && (fullChar <= 0xF6))
					return true;
				
				if((fullChar >= 0xF8) && (fullChar <= 0x2FF))
					return true;
				
				return false;
			}
			
			if((fullChar >= 0x370) && (fullChar <= 0x37D))
				return true;
			
			if((fullChar >= 0x37F) && (fullChar <= 0x1FFF))
				return true;
				
			if((fullChar >= 0x200C) && (fullChar <= 0x200D))
				return true;
			
			return false;
		}
				
		if(fullChar <= 0xD7FF) {
			if((fullChar >= 0x2070) && (fullChar <= 0x218F))
				return true;
			
			if((fullChar >= 0x2C00) && (fullChar <= 0x2FEF))
				return true;
			
			if((fullChar >= 0x3001) && (fullChar <= 0xD7FF))
				return true;
			
			return false;
		}
		
		if(fullChar <= 0xFFFD) {
			if((fullChar >= 0xF900) && (fullChar <= 0xFDCF))
				return true;
			
			if((fullChar >= 0xFDF0) && (fullChar <= 0xFFFD))
				return true;
		
			return false;
		}
				
		if((fullChar >= 0x10000) && (fullChar <= 0xEFFFF))
			return true;
		
		return false;
	}

	public boolean isXmlNameChar(int fullChar, boolean strict) {
		// is a NameChar according to the XMl 1.1 spec
		
		if(isXmlNameStartChar(fullChar, strict))
			return true;

		if(fullChar <= 0x2040) {
			if(fullChar <= 0x2FF) {
				if((fullChar >= '0') && (fullChar <= '9'))
					return true;
				
				switch(fullChar) {
				case '-':
				case '.':
				case 0xB7:
					return true;
				}
				
				return false;
			}
			
			if((fullChar >= 0x300) && (fullChar <= 0x36F))
				return true;
			
			if((fullChar >= 0x203F) && (fullChar <= 0x2040))
				return true;
		}
				
		return false;
	}
	
	public boolean isCIdentifierStartChar(int fullChar) {
		switch(fullChar) {
		case ':':
		case ' ':
		case 0x9:
		case 0xA:
		case 0xD:
			return false;
		}
		
		return isXmlNameStartChar(fullChar, true);
	}

	public boolean isCIdentifierChar(int fullChar) {
		switch(fullChar) {
		case ':':
		case '-':
		case '.':
		case ' ':
		case 0x9:
		case 0xA:
		case 0xD:
			return false;
		}

		return isXmlNameChar(fullChar, true);
	}
	
	public boolean isBasicLatinLetterChar(int fullChar) {
		if(fullChar > 0x7F) return false;
		
		if((fullChar >= 'A') && (fullChar <= 'Z'))
			return true;

		if((fullChar >= 'a') && (fullChar <= 'z'))
			return true;

		return false;
	}
	
	public boolean isWhitespaceChar(int fullChar) {
		if(fullChar == ' ' || fullChar == 0x9
			|| fullChar == 0xD || fullChar == 0xA)
			return true;
		
		return false;
	}
	
	public boolean isHexChar(int fullChar) {
		if(fullChar > 0x7F) return false;
		
		if((fullChar >= '0') && (fullChar <= '9'))
			return true;

		if((fullChar >= 'A') && (fullChar <= 'F'))
			return true;

		if((fullChar >= 'a') && (fullChar <= 'f'))
			return true;

		return false;
	}

	public boolean isDecimalChar(int fullChar) {		
		if((fullChar >= '0') && (fullChar <= '9'))
			return true;

		return false;
	}

	public boolean isOctalChar(int fullChar) {		
		if((fullChar >= '0') && (fullChar <= '7'))
			return true;

		return false;
	}

	public boolean isBinaryChar(int fullChar) {		
		if((fullChar >= '0') && (fullChar <= '1'))
			return true;

		return false;
	}
	
	public short getHexCharValue(int fullChar) {
		if((fullChar >= '0') && (fullChar <= '9'))
			return (short) (fullChar - '0');

		if((fullChar >= 'A') && (fullChar <= 'F'))
			return (short) ((fullChar - 'A') + 10);

		if((fullChar >= 'a') && (fullChar <= 'f'))
			return (short) ((fullChar - 'a') + 10);
		
		throw new IndexOutOfBoundsException(
			"character is not hex");
	}
	
	public short getDecimalCharValue(int fullChar) {
		if((fullChar >= '0') && (fullChar <= '9'))
			return (short) (fullChar - '0');

		throw new IndexOutOfBoundsException(
			"character is not decimal");
	}

	public int getCodeUnitLittleEndian(
		CommonInt8Array buf, int index, int unitSize) {
		
		int accumulator;
		int b1;
		int byteIndex;

		byteIndex = 0;
		accumulator = 0;
		while(byteIndex < unitSize) {
			b1 = buf.aryPtr[index * unitSize + byteIndex];
			b1 = b1 & 0xFF; // get value from 0..255
			
			accumulator |= b1 << (byteIndex * 8);
			
			byteIndex += 1;
		}

		return accumulator;
	}
	
	public void bufferIndexSkip(BufferIndex bi, int skipLength) {		
		bi.index += skipLength;
		if(bi.zoomValid) bi.zoomElementIndex += skipLength;
		return;
	}
	
	public void textIndexSkip(TextIndex ri, int skipLength) {
		ri.index += skipLength;
		ri.indexWithinLine += skipLength;
		return;
	}
	
	public void textIndexSkipReturn(TextIndex ri) {
		ri.line += 1;
		ri.indexWithinLine = 0;
		return;
	}
	
	public void charReaderContextSkip(CharReaderContext cc) {
		int skipLength;
		
		skipLength = cc.resultCharLength;
		
		textIndexSkip(cc.ti, skipLength);
		bufferIndexSkip(cc.bi, skipLength);
	}

	public void copyBufferIndex(BufferIndex dst, BufferIndex src) {
		dst.versionNumber = src.versionNumber;
		dst.index = src.index;
		dst.zoomValid = src.zoomValid;
		dst.zoomComboIndex = src.zoomComboIndex;
		dst.zoomElementIndex = src.zoomElementIndex;
	}
	
	public void copyTextIndex(TextIndex dst, TextIndex src) {
		dst.index = src.index;
		dst.line = src.line;
		dst.indexWithinLine = src.indexWithinLine;
		return;
	}
	
	public void copyCharReaderContext(
		CharReaderContext dst, CharReaderContext src) {
		
		copyTextIndex(dst.ti, src.ti);
		copyBufferIndex(dst.bi, src.bi);
		dst.state = src.state;
		dst.resultCharLength = src.resultCharLength;
		dst.resultChar = src.resultChar;
	}
	
	public void getCharReaderStartIndex(TextIndex ti, CharReaderParams prms) {
		long start;
		
		start = 0;
		
		if(prms != null) {
			if(prms.id == CharReaderParamsTypes.PARAMS_TYPE_FOR_MEMORY)
			if(prms.readBuf != null)
			if(prms.readBuf.startIndex > start)
				start = prms.readBuf.startIndex;

			if(prms.streamStartIndex != null)
			if(prms.streamStartIndex.index > start) {
				ti.index = prms.streamStartIndex.index;
				ti.indexWithinLine = prms.streamStartIndex.indexWithinLine;
				ti.line = prms.streamStartIndex.line;
				return;
			}
		}
			
		ti.index = start;
		ti.indexWithinLine = 0;
		ti.line = 0;
		return;
	}
		
	public CharReaderContext getCharReaderContext(
		CommonArrayList store, int index) {
		
		int count;
		CharReaderContext cc;
		
		count = store.size();
		while(count <= index) {
			store.add(makeCharReaderContext());
			count = store.size();
		}
		
		cc = (CharReaderContext) store.get(index);
		return cc;
	}

	public CommonInt32 getInt32FromStack(
		BufferNode storeHdr, int index) {
		
		int count;
		CommonInt32 num;
		CommonArrayList store;
		
		store = (CommonArrayList) storeHdr.theObject;
		
		count = store.size();
		while(count <= index) {
			store.add(makeInt32());
			count = store.size();
		}
		
		num = (CommonInt32) store.get(index);
		return num;
	}

	public CompareParams getCompareParamsFromStack(
		BufferNode storeHdr, int index) {
		
		int count;
		CompareParams compRec;
		CommonArrayList store;
		
		store = (CommonArrayList) storeHdr.theObject;
		
		count = store.size();
		while(count <= index) {
			store.add(makeCompareParams());
			count = store.size();
		}
		
		compRec = (CompareParams) store.get(index);
		return compRec;
	}

	public SortParams getSortParamsFromStack(
		BufferNode storeHdr, int index) {
		
		int count;
		SortParams sortRec;
		CommonArrayList store;
		
		store = (CommonArrayList) storeHdr.theObject;
		
		count = store.size();
		while(count <= index) {
			store.add(makeSortParams());
			count = store.size();
		}
		
		sortRec = (SortParams) store.get(index);
		return sortRec;
	}

	public CommonInt32Array getRightLengthInt32StringFromStack(
		BufferNode storeHdr, int index) {
		
		int count;
		CommonInt32Array str;
		CommonArrayList store;
		
		store = (CommonArrayList) storeHdr.theObject;
		
		count = store.size();
		while(count <= index) {
			store.add(makeInt32Array(count));
			count = store.size();
		}
		
		str = (CommonInt32Array) store.get(index);
		return str;
	}
	
	public void stringMatchSimple(CommonInt32Array correctString, int c, 
		CommonInt32 index, 
		CommonBoolean didAdvance, CommonBoolean finished) {
		
		didAdvance.value = false;
		
		if(finished.value) return;
		
		if(index.value >= correctString.length) {
			finished.value = true;
			return;
		}

		if(c != correctString.aryPtr[index.value]) {
			finished.value = true;
			return;
		}
		
		index.value += 1;
		didAdvance.value = true;
		
		if(index.value >= correctString.length) {
			finished.value = true;
			return;
		}
		
		// not finished matching
		return;
	}
	
	public void copyToArrayFromList(Object[] dst, CommonArrayList src) {
		int len;
		int i;
		
		len = src.size();
		if(len != dst.length)
			throw new IndexOutOfBoundsException();
		
		i = 0;
		while(i < len) {
			dst[i] = src.get(i);
			i += 1;
		}
		return;
	}

	public void copyToListFromArray(CommonArrayList dst, Object[] src) {
		int len;
		int i;
		
		len = src.length;
		
		i = 0;
		while(i < len) {
			dst.add(src[i]);
			i += 1;
		}
		return;
	}
	
	public int getObjectNumberFromList(CommonArrayList theList, Object obj) {
		int i;
		int len;

		i = 0;
		len = theList.size();
		while(i < len) {
			if(obj == theList.get(i)) return i;
			i += 1;
		}
		
		throw makeObjectNotFound("Object not found in list");
	}
	
	private int getSymbolIdItem(Symbol sym, int index) {
		if(sym.id == null) return Symbols.SYMBOL_UNSPECIFIED;
		if(sym.id.length <= index) return Symbols.SYMBOL_UNSPECIFIED;
		
		return sym.id.aryPtr[index];
	}
	
	public int getSymbolIdCategory(Symbol sym) {
		return getSymbolIdItem(sym, 0);
	}

	public int getSymbolIdPrimary(Symbol sym) {
		return getSymbolIdItem(sym, 1);
	}

	public int getIdLenFromSymbol(Symbol sym) {
		if(sym == null) throw makeNullPointer(null);
		return sym.id.length;
	}

	public int getIdLenFromSymbolId(SymbolId symId) {
		if(symId == null) throw makeNullPointer(null);
		return symId.length;
	}
	
	public void setSymbolIdLen2(Symbol sym, int id1, int id2) {
		if(sym == null) throw makeNullPointer(null);
		if(sym.id.length < 2) throw makeIndexOutOfBounds(null);
		
		sym.id.aryPtr[0] = id1;
		sym.id.aryPtr[1] = id2;
		sym.id.length = 2;
	}
	
	private void myCopyArray(
		CommonInt32Array dst, int dstStart,
		CommonInt32Array src, int srcStart,
		int len) {
		
		int i;
		
		i = 0;
		while(i < len) {
			dst.aryPtr[dstStart + i] = src.aryPtr[srcStart + i];
			i += 1;
		}
		
		return;
	}

	private void myCopyArray2(CommonInt32Array dst, CommonInt32Array src, int len) {
		myCopyArray(dst, 0, src, 0, len);
	}
	
	public void copySymbolIdToSymbolFromSymbol(Symbol symDst, Symbol symSrc) {
		if(symSrc.id.length > symDst.id.length)
			throw makeIndexOutOfBounds(null);
		
		myCopyArray2(symDst.id, symSrc.id, symSrc.id.length);
		symDst.id.length = symSrc.id.length;
		return;
	}

	public void copySymbolId(SymbolId symIdDst, SymbolId symIdSrc) {
		if(symIdSrc.length > symIdDst.length)
			symIdDst.aryPtr = new int[symIdSrc.length];

		myCopyArray2(symIdDst, symIdSrc, symIdSrc.length);
		symIdDst.length = symIdSrc.length;
		return;
	}

	public void copySymbolIdToSymbolFromSymbolId(Symbol symDst, SymbolId symIdSrc) {
		if(symIdSrc.length > symDst.id.length)
			throw makeIndexOutOfBounds(null);
		
		myCopyArray2(symDst.id, symIdSrc, symIdSrc.length);
		symDst.id.length = symIdSrc.length;
		return;
	}

	/*
	public void copySymbolIdToSymbolIdFromNativeSymbolId(SymbolId symIdDst, int[] srcId) {}
	public void copySymbolIdToNativeSymbolIdFromSymbolId(int[] idDst, SymbolId symIdSrc) {}
	public void copySymbolIdToNativeSymbolIdFromSymbol(int[] idDst, Symbol sym) {}
	*/

	public void allocNewSymbolId(Symbol sym, int idLen) {
		if(sym == null) throw makeNullPointer(null);
		sym.id = makeSymbolId(idLen);
	}

	public int getTokenChildCount(TokenContainer tokCon) {
		if(tokCon == null) throw makeNullPointer(null);
		return tokCon.childCount;
	}

	public int getGramChildCount(GramContainer grmCon) {
		if(grmCon == null) throw makeNullPointer(null);
		return grmCon.childCount;
	}

	public void allocNewTokenArrayForTokenContainer(
		TokenContainer tokCon, int childCount) {
		
		if(tokCon == null) throw makeNullPointer(null);
		tokCon.tok = new Token[childCount];
		tokCon.childCount = childCount;
	}

	public void allocNewSymbolArrayForGramContainer(
		GramContainer grmCon, int childCount) {
		
		if(grmCon == null) throw makeNullPointer(null);
		grmCon.sym = new Symbol[childCount];
		grmCon.childCount = childCount;
	}
	
	public long getSymbolSpanLen(Symbol sym) {
		long i1 = sym.startIndex.index;
		long i2 = sym.pastIndex.index;
		
		if(i2 > i1) return i2 - i1;
		return 0;
	}

	public String dumpIntArray(CommonInt32Array a) {
		StringBuilder sb;
		int i;
		int count;
		
		sb = new StringBuilder();
		count = a.length;
		i = 0;
		while(i < count) {
			if(i == 0) sb.append("[");
			else sb.append(",");
			
			sb.append(a.aryPtr[i]);
			
			i += 1;
		}
	
		sb.append("]");
		
		return sb.toString();
	}
	
	private CommonError makeNullPointer(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = CommonErrorTypes.ERROR_NULL_POINTER;
		e1.msg = msg;
		return e1;
	}

	private CommonError makeObjectNotFound(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_OBJECT_NOT_FOUND;
		e1.msg = msg;
		return e1;
	}
	
	private CommonError makeIndexOutOfBounds(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_OUT_OF_BOUNDS;
		e1.msg = msg;
		return e1;
	}
	
	private CommonInt32 makeInt32() {
		return new CommonInt32();
	}
	
	private CommonInt32Array makeInt32Array(int len) {
		return CommonUtils.makeInt32Array(len);
	}
	
	private CompareParams makeCompareParams() {
		return new CompareParams();
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;

		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}

	private CharReaderContext makeCharReaderContext() {
		CharReaderContext cc;

		cc = new CharReaderContext();
		cc.init();
		return cc;
	}
	
	public SymbolId makeSymbolId(int idLen) {
		SymbolId id = new SymbolId();
		id.aryPtr = new int[idLen];
		id.length = idLen;
		id.capacity = idLen;
		return id;
	}
}
