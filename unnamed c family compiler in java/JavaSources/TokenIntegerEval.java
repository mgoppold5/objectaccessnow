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

package unnamed.family.compiler;

import unnamed.common.*;

public class TokenIntegerEval implements BaseModule {
	public TokenIntegerEvalData dat;
	public GeneralUtils utils;
	public StringReaderAccess strRead;

	public BaseModuleData getData() {return dat;}

	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, strRead);

		return o;
	}
	
	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		
		if(utils == null) return true;
		if(strRead == null) return true;
		
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void readInteger32FromFull() {
		int i;
		
		readInteger64FromFull();
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;
		
		i = (int) dat.resultInt64;
		if(i != dat.resultInt64) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeIntegerOverflow(dat.fullIntTok.startIndex));
			return;
		}
		
		dat.resultInt32 = i;
		dat.state = TokenIntegerEvalData.STATE_HAVE_INT_32;
		return;
	}

	public void readInteger32FromSimple() {
		int i;
		
		readInteger64FromSimple();
		
		if(dat.state == BaseModuleData.STATE_STUCK)
			return;
		
		i = (int) dat.resultInt64;
		if(i != dat.resultInt64) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_PERMANENT;
			dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
				makeIntegerOverflow(dat.fullIntTok.startIndex));
			return;
		}
		
		dat.resultInt32 = i;
		dat.state = TokenIntegerEvalData.STATE_HAVE_INT_32;
		return;
	}
	
	public void readInteger64FromFull() {
		readInteger64(dat.fullIntTok.integer, dat.fullIntTok.radix);
		return;
	}

	public void readInteger64FromSimple() {
		readInteger64(dat.simpleIntTok.integer, dat.simpleIntTok.radix);
		return;
	}
	
	public void readInteger64(Token integerData, short radix) {
		long len;
		int i;
		int c;
		int count;
		int state;
		CommonInt32Array s;
		boolean wasDigit;
		long lowerHalfMask;
		long r;
		short digit;
		boolean pastLimit;
		
		StringReader2Data strDat = (StringReader2Data) strRead.getData();
		
		// accumulator limbs, the lower halfs of each 64 bit int
		// make up a number
		LimbElement limb;
		
		len = integerData.pastIndex.index - integerData.startIndex.index;
		strRead.readUtf32(integerData.startIndex, len);
		
		state = strDat.state;
		
		if(state == BaseModuleData.STATE_STUCK) {
			dat.state = BaseModuleData.STATE_STUCK;
			dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
			return;
		}
		
		if(state != CharReaderData.STATE_HAVE_BUFFER_STRING)
			throw makeInvalidEnum("CharReader state bad");
		
		s = StringUtils.int32StringFromUtf32(strDat.resultBufferString);
		
		lowerHalfMask = utils.lowerBitsMask64.aryPtr[
			TokenIntegerEvalData.LIMB_HALF_LEN];
		
		limb = new LimbElement();
		i = 0;
		count = s.length;
		wasDigit = false;
		
		if(limb.capacity < 1)
			LimbUtils.allocLimbs(limb);

		if(radix > 16 || radix < 2)
			throw makeInvalidEnum("radix not supported");

		while(i < count) {
			wasDigit = false;
			digit = 0;
			c = s.aryPtr[i];

			if(c == '_') {
				i += 1;
				continue;
			}
			
			if(utils.isHexChar(c)) {
				digit = utils.getHexCharValue(c);

				if(digit < radix) wasDigit = true;
			}
			
			if(!wasDigit) {
				i += 1;
				continue;
			}
			
			LimbUtils.multLimbs(limb, radix);
			limb.a.aryPtr[0] += digit;
			LimbUtils.touchLimb(limb, 0);

			
			LimbUtils.adjustLimbs(limb, lowerHalfMask);

			if(limb.used >= limb.capacity)
				LimbUtils.allocLimbs(limb);

			pastLimit = false;
			if(limb.used > 2) pastLimit = true;
			if(limb.used == 2)
			if(FlagUtils.getFlagInt64(limb.a.aryPtr[1],
				TokenIntegerEvalData.LIMB_HALF_LEN - 1)) 
				pastLimit = true;

			if(pastLimit) {
				dat.state = BaseModuleData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_PERMANENT;
				dat.probBag.addProblem(ProblemLevels.LEVEL_LANG_ERROR,
					makeIntegerOverflow(integerData.startIndex));
				return;
			}
			

			i += 1;
			continue;			
		}

		r = 0;
		if(limb.used >= 2) r |= limb.a.aryPtr[1] << 32;
		if(limb.used >= 1) r |= limb.a.aryPtr[0];

		dat.resultInt64 = r;
		dat.state = TokenIntegerEvalData.STATE_HAVE_INT_64;
		return;
	}
	
	/*
	private String printArray(long[] a) {
		int i;
		int count;
		
		StringBuilder sb = new StringBuilder();
		
		i = 0;
		count = a.length;
		sb.append("[");
		while(i < count) {
			if(i > 0) sb.append(",");
			sb.append(a[i]);
			i += 1;
		}
		sb.append("]");
		return sb.toString();
	}
	*/
	
	private CommonError makeInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		return e1;
	}
	
	private LangError makeIntegerOverflow(TextIndex ti) {
		TextIndex context;
		LangError e3;
		
		context = new TextIndex();
		utils.copyTextIndex(context, ti);
		
		e3 = new LangError();
		e3.id = LangErrors.ERROR_INTEGER_OVERFLOW;
		e3.context = context;
		return e3;
	}
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
