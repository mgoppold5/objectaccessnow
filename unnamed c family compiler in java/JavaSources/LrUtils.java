/*
 * Copyright (c) 2016 Mike Goppold von Lobsdorf
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

public class LrUtils {
	public void init() {}
	
	public short getRulePrecedenceToken(
		GrammarRule ruleRec,
		TypeAndObject[] names) {
		
		// by default, a rule's precedence token
		// is the rightmost token on the rule RHS
		
		int i;
		int len;
		short unspecifiedTok;
		short nameNum;
		
		unspecifiedTok = (short) 0;
		
		if(ruleRec.precedenceTok != 0)
			return ruleRec.precedenceTok;
		
		len = ruleRec.rightArray.length;
		i = len;
		
		if(i == 0) return unspecifiedTok;
		
		while(i > 0) {
			i -= 1;
			
			nameNum = ruleRec.rightArray.aryPtr[i];
			
			if(!nameIsToken(names[nameNum]))
				continue;
			
			// found rightmost token number
			return nameNum;
		}
		
		return unspecifiedTok;
	}
	
	public CommonArrayList makePrecedenceSpectrumStackSimple(
		TypeAndObject[] names) {
		
		CommonArrayList stack;
		int nameNum;
		int nameCount;
		TypeAndObject nameRec;
		
		stack = makeArrayList();
		
		nameNum = 0;
		nameCount = names.length;
		while(nameNum < nameCount) {
			nameRec = names[nameNum];
			
			if(!nameIsSpectrum(nameRec)) {
				nameNum += 1;
				continue;
			}
			
			stack.add(nameRec);
			
			nameNum += 1;
		}
		
		return stack;
	}
	
	public void addReduceRules(
		BufferNode ruleStore,
		LrConfigGroup[] cfgArray, int k, CommonInt16Array ctx) {
		
		LrConfigGroup cfg;
		CommonInt32 intRec;
		int i;
		int count;
		
		count = cfgArray.length;
		i = 0;
		while(i < count) {
			cfg = cfgArray[i];
			
			if(!existsContext(k, ctx, cfg.contextData)) {
				i += 1;
				continue;
			}
			
			intRec = getInt32FromStack(ruleStore, ruleStore.used);
			intRec.value = cfg.ruleNumber;
			ruleStore.used += 1;
			
			i += 1;
		}
		
		return;
	}
	
	public LrState getState(
		CommonArrayList stateGrpStore, LrState st, int k) {
		
		SortParams sortRec;
		LrStateGroup stGrp;
		LrState existSt;
		
		sortRec = makeSortParams();
		stGrp = null;
		
		stateGrpBinaryLookup(stateGrpStore, st, sortRec);
		
		if(!sortRec.foundExisting) return null;
		
		stGrp = (LrStateGroup) stateGrpStore.get(sortRec.index);
				
		stateBinaryLookup(stGrp.states, st, k, sortRec);
		
		if(!sortRec.foundExisting) return null;
		
		existSt = (LrState) stGrp.states.get(sortRec.index);
		return existSt;
	}
	
	public void addState(
		CommonArrayList stateGrpStore, LrState st, int k) {
		
		SortParams sortRec;
		LrStateGroup stGrp;
		
		sortRec = makeSortParams();
		stGrp = null;
		
		stateGrpBinaryLookup(stateGrpStore, st, sortRec);
		
		if(!sortRec.foundExisting) {
			stGrp = new LrStateGroup();
			stGrp.commonStateCore = copyStateCore(st);
			stGrp.states = makeArrayList();
			
			stateGrpStore.addAt(sortRec.index, stGrp);
		}
		
		if(sortRec.foundExisting) {
			stGrp = (LrStateGroup) stateGrpStore.get(sortRec.index);
		}
		
		stateBinaryLookup(stGrp.states, st, k, sortRec);
		
		if(sortRec.foundExisting)
			throw makeIllegalState(
				"duplicate lr state already exists");
		
		stGrp.states.addAt(sortRec.index, st);
		return;
	}
	
	public LrConfigGroup getConfig(
		CommonArrayList configStore, LrConfigGroup cfg) {
		
		LrConfigGroup existCfg;
		SortParams sortRec;
		
		sortRec = makeSortParams();
		
		configBinaryLookup(configStore, cfg, sortRec);
		
		if(!sortRec.foundExisting) return null;
		
		existCfg = (LrConfigGroup) configStore.get(sortRec.index);
		return existCfg;
	}

	public void addConfig(
		CommonArrayList configStore, LrConfigGroup cfg) {
		
		LrConfigGroup existCfg;
		SortParams sortRec;
		
		sortRec = makeSortParams();
		
		configBinaryLookup(configStore, cfg, sortRec);
		
		if(sortRec.foundExisting)
			throw makeIllegalState(
				"duplicate lr config group already exists");
		
		configStore.addAt(sortRec.index, cfg);
		return;
	}
	
	public LrEdge getEdge(
		CommonArrayList edgeStore, short transitionSym) {
		
		//TypeAndObjectSortedByInt32 ent;
		TypeAndObject ent;
		LrEdge existEdg;
		SortParams sortRec;
		
		sortRec = makeSortParams();
		
		edgeBinaryLookup(edgeStore, transitionSym, sortRec);
		
		if(!sortRec.foundExisting) return null;
		
		ent = (TypeAndObject)
			edgeStore.get(sortRec.index);
		existEdg = (LrEdge) ent.theObject;
		return existEdg;
	}

	public void addEdge(
		CommonArrayList edgeStore, LrEdge edg) {
		
		SortParams sortRec;
		
		sortRec = makeSortParams();
		
		edgeBinaryLookup(edgeStore, edg.transitionSym, sortRec);
		
		if(sortRec.foundExisting)
			throw makeIllegalState(
				"duplicate lr edge already exists");
		
		edgeStore.addAt(sortRec.index, makeEdgeSortEntry(edg));
		return;
	}
	
	private void stateCompareCore(
		LrState st1, LrState st2, CompareParams compPar) {
		
		int cnt1;
		int cnt2;
		int cnt3;
		int i;
		LrConfigGroup cfg1;
		LrConfigGroup cfg2;
		
		compPar.greater = false;
		compPar.less = false;
		
		cnt1 = st1.nucleusCfgs.length;
		cnt2 = st2.nucleusCfgs.length;
		
		cnt3 = cnt1;
		if(cnt2 < cnt3) cnt3 = cnt2;
		
		i = 0;
		while(i < cnt3) {
			cfg1 = st1.nucleusCfgs[i];
			cfg2 = st2.nucleusCfgs[i];
			
			if(cfg1.ruleNumber > cfg2.ruleNumber) {
				compPar.greater = true;
				return;
			}

			if(cfg1.ruleNumber < cfg2.ruleNumber) {
				compPar.less = true;
				return;
			}
			
			if(cfg1.markerPosition > cfg2.markerPosition) {
				compPar.greater = true;
				return;
			}

			if(cfg1.markerPosition < cfg2.markerPosition) {
				compPar.less = true;
				return;
			}
			
			i += 1;
		}
		
		if(cnt1 > cnt2) {
			compPar.greater = true;
			return;
		}

		if(cnt1 < cnt2) {
			compPar.less = true;
			return;
		}
		
		return;
	}
	
	private void stateGrpBinaryLookup(
		CommonArrayList store, LrState st,
		SortParams sortRec) {
		
		LrStateGroup ent;
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compPar;
		
		compPar = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (LrStateGroup) store.get(i);
			
			stateCompareCore(
				st, ent.commonStateCore, compPar);
			
			if(compPar.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compPar.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}
		
		// unreachable
	}
	
	private void stateCompareContexts(
		LrState st1, LrState st2, int k, CompareParams compPar) {
		
		int count;
		int i;
		int len1;
		int len2;
		int len3;
		int j;
		CommonInt16Array ctx1;
		CommonInt16Array ctx2;
		
		compPar.greater = false;
		compPar.less = false;
		
		count = st1.nucleusCfgs.length;
		if(count != st2.nucleusCfgs.length)
			throw makeUnknownError(
				"bad sort state, two states have different cores");		
		
		if(k < 1)
			// no contexts, so they are equal
			return;
		
		i = 0;
		while(i < count) {
			LrConfigGroup cg5;
			
			cg5 = st1.nucleusCfgs[i];
			ctx1 = cg5.contextData;
			
			cg5 = st2.nucleusCfgs[i];
			ctx2 = cg5.contextData;
			
			len1 = ctx1.length;
			len2 = ctx2.length;
			
			len3 = len1;
			if(len2 < len3) len3 = len2;
			
			j = 0;
			while(j < len3) {
				if(ctx1.aryPtr[j] > ctx2.aryPtr[j]) {
					compPar.greater = true;
					return;
				}

				if(ctx1.aryPtr[j] < ctx2.aryPtr[j]) {
					compPar.less = true;
					return;
				}
				
				j += 1;
			}
			
			if(len1 > len2) {
				compPar.greater = true;
				return;
			}

			if(len1 < len2) {
				compPar.less = true;
				return;
			}
			
			i += 1;
		}
		
		return;
	}

	private void stateBinaryLookup(
		CommonArrayList store, LrState st,
		int k,
		SortParams sortRec) {
		
		LrState ent;
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compPar;
		
		compPar = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (LrState) store.get(i);
			
			stateCompareContexts(st, ent, k, compPar);
			
			if(compPar.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compPar.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}
		
		// unreachable
	}
	
	private LrState copyStateCore(LrState st) {
		LrState stateCore;
		int count;
		int i;
		
		stateCore = new LrState();
		
		count = st.nucleusCfgs.length;
		stateCore.nucleusCfgs = new LrConfigGroup[count];
		
		i = 0;
		while(i < count) {
			stateCore.nucleusCfgs[i] =
				copyConfigCore(st.nucleusCfgs[i]);
			i += 1;
		}
		
		return stateCore;
	}
	
	private void configCompareCore(
		LrConfigGroup cfg1, LrConfigGroup cfg2, CompareParams compPar) {
		
		compPar.greater = false;
		compPar.less = false;
		
		if(cfg1.ruleNumber > cfg2.ruleNumber) {
			compPar.greater = true;
			return;
		}

		if(cfg1.ruleNumber < cfg2.ruleNumber) {
			compPar.less = true;
			return;
		}
		
		if(cfg1.markerPosition > cfg2.markerPosition) {
			compPar.greater = true;
			return;
		}

		if(cfg1.markerPosition < cfg2.markerPosition) {
			compPar.less = true;
			return;
		}
		
		return;
	}

	private void configBinaryLookup(
		CommonArrayList store, LrConfigGroup cfg,
		SortParams sortRec) {
		
		LrConfigGroup ent;
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compPar;
		
		compPar = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (LrConfigGroup) store.get(i);
			
			configCompareCore(cfg, ent, compPar);
			
			if(compPar.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compPar.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}
		
		// unreachable
	}
	
	private void edgeCompareSym(
		short sym1, short sym2, CompareParams compPar) {
		
		compPar.greater = false;
		compPar.less = false;
		
		if(sym1 > sym2) {
			compPar.greater = true;
			return;
		}

		if(sym1 < sym2) {
			compPar.less = true;
			return;
		}
		
		return;
	}

	private void edgeBinaryLookup(
		CommonArrayList store, short transitionSym,
		SortParams sortRec) {
		
		//ThingSortedByInt32 ent;
		TypeAndObject ent;
		int minIndex;
		int maxIndex;
		int i;
		CompareParams compPar;
		
		compPar = sortRec.compPar;
		
		minIndex = 0;
		maxIndex = store.size();
		
		while(true) {
			i = (minIndex + maxIndex) / 2;

			if(minIndex == maxIndex) {
				sortRec.index = i;
				sortRec.foundExisting = false;
				return;
			}
			
			ent = (TypeAndObject) store.get(i);
			
			CommonInt32 hldr = (CommonInt32) ent.sortObject;
			edgeCompareSym(
				transitionSym,
				(short) hldr.value,
				compPar);
			
			if(compPar.greater) {
				minIndex = i + 1;
				continue;
			}
			
			if(compPar.less) {
				maxIndex = i;
				continue;
			}
			
			sortRec.index = i;
			sortRec.foundExisting = true;
			return;
		}

		// unreachable
	}
	
	private LrConfigGroup copyConfigCore(LrConfigGroup existCfg) {
		LrConfigGroup cfg;
		
		cfg = new LrConfigGroup();
		cfg.ruleNumber = existCfg.ruleNumber;
		cfg.markerPosition = existCfg.markerPosition;
		return cfg;
	}
	
	private TypeAndObject makeEdgeSortEntry(LrEdge edg) {
		//TypeAndObjectSortedByInt32 ent;
		TypeAndObject ent;
		CommonInt32 sortInt;
		
		ent = new TypeAndObject();
		sortInt = new CommonInt32();
		
		sortInt.value = edg.transitionSym;
		
		ent.sortObject = sortInt;
		ent.theObject = edg;
		return ent;
	}
		
	private boolean existsContext(int k,
		CommonInt16Array testStr, CommonInt16Array data) {
		
		int i;
		int j;
		int len1;
		int len2;
		boolean match;
		int pos;
		
		if(k < 1) return true;
		
		if(testStr.length != k)
			throw makeIllegalState("context length is not k");
		
		len1 = data.length;
		
		if(len1 % k != 0)
			throw makeIllegalState("context data length is not based ok k");
		
		len2 = len1 / k;
		
		i = 0;
		pos = 0;
		while(i < len2) {
			j = 0;
			match = true;
			while(j < k) {
				if(data.aryPtr[pos + j] != testStr.aryPtr[j]) {
					match = false;
					break;
				}
				j += 1;
			}
			
			if(match) return true;
			
			i += 1;
			pos += k;
		}
		
		return false;
	}
	
	private boolean nameIsSpectrum(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_PRECEDENCE_SPECTRUM)
			return false;
		
		return true;
	}

	private boolean nameIsToken(
		TypeAndObject nameObject) {
		if(nameObject == null) return false;
		
		if(nameObject.theType != GrammarNameTypes.TYPE_TOKEN_DEF)
			return false;
		
		return true;
	}
	
	private CommonInt32 getInt32FromStack(
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
	
	private CommonInt32 makeInt32() {
		return new CommonInt32();
	}

	private CommonError makeUnknownError(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_UNKNOWN;
		e1.msg = msg;
		return e1;
	}
	
	private RuntimeException makeIllegalState(String msg) {
		if(msg == null) return new IllegalStateException();
		
		return new IllegalStateException(msg);
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private SortParams makeSortParams() {
		SortParams sortRec;

		sortRec = new SortParams();
		sortRec.init();
		return sortRec;
	}
}
