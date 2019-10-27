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

public class StringWriter3 implements BaseModule {
	public StringWriter3Data dat;
	public GeneralUtils utils;
	public CharWriter3 charWrite;

	public BaseModuleData getData() {return dat;}
	
	public CommonArrayList getChildModules() {
		CommonArrayList list = makeArrayList();
		if(charWrite != null) list.add(charWrite);
		return list;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		if(utils == null) return true;
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.probBag.reset();
		
		dat.state = BaseModuleData.STATE_START;
	}
	
	public void writeInt32StringThrow(TextIndex ti) {
		int iDone;
		int state;
		int charLength;
		int len;
		
		CommonInt32Array str = dat.resultString;
		
		CharWriter3Data charDat = (CharWriter3Data) charWrite.getData();
		CharReaderParams prms = charDat.charReadParams;

		TextIndex ti2;
		BufferIndex bi2;
		
		ti2 = prms.tiTemp2;
		bi2 = prms.biTemp2;

		if(ti != null) {
			utils.copyTextIndex(ti2, ti);
			bi2.versionNumber = ReadBufferPartial.VERSION_NUMBER_INVALID;
		}

		if(ti == null) {
			ti2.index = 0;
			ti2.indexWithinLine = 0;
			ti2.line = 0;
			bi2.versionNumber = ReadBufferPartial.VERSION_NUMBER_INVALID;
		}
		
		iDone = 0;
		len = str.length;
		
		while(iDone < len) {
			charDat.resultChar = str.aryPtr[iDone];
			
			charWrite.writeCharThrow(ti2);
			
			state = charDat.state;
			
			if(state == CharWriter3Data.STATE_WROTE_CHAR) {
				charLength = charDat.resultCharLength;

				iDone += 1;
				utils.textIndexSkip(ti2, charLength);
				utils.bufferIndexSkip(bi2, charLength);
				continue;
			}
						
			throw makeInvalidEnum("CharWriter has bad state");
		}
		
		dat.state = StringWriter3Data.STATE_WROTE_STRING;
		return;
	}	
	
	public void writeInt32String(TextIndex ti) {
		Throwable ex;
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;		
		
		ex = null;

		try {
			writeInt32StringThrow(ti);
		} catch(Throwable e2) {
			ex = e2;
		}
		
		if(ex != null) fireRuntimeError(ex);
		
		return;
	}
		
	private void fireRuntimeError(Throwable ex) {
		if(ex == null) return;
		
		dat.probBag.addProblem(ProblemLevels.LEVEL_RUNTIME_ERROR, ex);
		dat.state = BaseModuleData.STATE_STUCK;
		dat.stuckState = StuckStates.STATE_PERMANENT;
	}

	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}

	private RuntimeException makeIndexOutOfBoundsException(String msg) {
		if(msg != null)
			return new IndexOutOfBoundsException(msg);
		
		return new IndexOutOfBoundsException();
	}

	private BufferCombo makeBufferCombo() {
		BufferCombo bc;
		
		bc = new BufferCombo();
		bc.init();
		return bc;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
