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

public class TokenChooser 
	implements BaseModule {
	
	public TokenChooserData dat;
	public CharReaderAccess charRead;
	public GeneralUtils utils;
	private SymbolAllocHelper allocHelp;
	
	public void setAllocHelper(SymbolAllocHelper pAllocHelp) {
		allocHelp = pAllocHelp;
	}
	
	public BaseModuleData getData() {return dat;}

	public CommonArrayList getChildModules() {
		CommonArrayList o;
		
		o = makeArrayList();
		
		addExistingModule(o, charRead);
		
		return o;
	}

	public CommonArrayList getUsedProblemContainers() {return null;}
	
	public boolean checkModuleConfig() {
		if(dat == null) return true;
		//if(dat.charReadParams == null) return true;
		
		if(utils == null) return true;
		if(charRead == null) return true;
		
		return false;
	}
	
	public CommonError getErrorFromModuleConfig() {return null;}
	
	public void reset() {
		dat.possibleHelpers.clear();
		dat.doneHelpers.clear();
		dat.chosenHelpers.clear();
		dat.chosenHelper = null;
		
		dat.resultCount = 0;
		
		dat.state = BaseModuleData.STATE_START;
		return;
	}
	
	public void chooseToken(CharReaderContext cc) {
		TokenHelper tokHelp;
		TokenHelperData helpDat;
		Object[] helpers;
		boolean prevCharWas13;
		
		CharReader2Data charDat = (CharReader2Data) charRead.getData();
		
		dat.state = TokenChooserData.STATE_BUSY;
		
		int helpNum;
		int helpCount;
		
		helpNum = 0;
		helpCount = dat.possibleHelpers.size();
		
		while(helpNum < helpCount) {
			tokHelp = (TokenHelper) dat.possibleHelpers.get(helpNum);

			tokHelp.reset();
			
			helpNum += 1;
		}

		prevCharWas13 = false;
		
		while(true) {
			charRead.readChar(cc.ti, cc.bi);

			cc.state = charDat.state;
			cc.resultCharLength = charDat.resultCharLength;
			
			if(cc.state == BaseModuleData.STATE_STUCK) {
				dat.state = TokenChooserData.STATE_STUCK;
				dat.stuckState = StuckStates.STATE_DUE_TO_DESCENDANTS;
				return;
			}

			if(cc.state == CharReaderData.STATE_HAVE_BAD_CHAR
				|| cc.state == CharReaderData.STATE_HAVE_CHAR) {
				
				helpers = dat.possibleHelpers.toArray();
				
				if(cc.state == CharReaderData.STATE_HAVE_CHAR) {
					cc.resultChar = charDat.resultChar;
					
					if(prevCharWas13)
					if(cc.resultChar != 0xA) {
						utils.textIndexSkipReturn(cc.ti);
						prevCharWas13 = false;
					}
				}
				
				helpNum = 0;
				helpCount = helpers.length;
				
				while(helpNum < helpCount) {
					tokHelp = (TokenHelper) helpers[helpNum];
					
					//if(allocHelp == null) throw new NullPointerException();
					tokHelp.setAllocHelper(allocHelp);
					tokHelp.advanceOneChar(cc);
					tokHelp.setAllocHelper(null);

					helpDat = (TokenHelperData) tokHelp.getData();

					if(helpDat.state == BaseModuleData.STATE_STUCK) {
						dat.state = TokenChooserData.STATE_STUCK;
						dat.stuckState =
							StuckStates.STATE_DUE_TO_DESCENDANTS;
					}
					
					if(helpDat.state == TokenHelperData.STATE_DONE) {
						if(helpDat.match)
							dat.doneHelpers.add(tokHelp);
						
						if(!helpDat.match)
							transferAllocCounts(helpDat);

						dat.possibleHelpers.remove(tokHelp);
					}
					
					helpNum += 1;
				}
				
				if(dat.state == TokenChooserData.STATE_STUCK)
					return;
				
				if(dat.possibleHelpers.size() == 0) {
					chooseTokenFinish();
					return;
				}
				
				if(cc.state == CharReaderData.STATE_HAVE_CHAR) {
					cc.resultChar = charDat.resultChar;
					
					if(prevCharWas13)
					if(cc.resultChar == 0xA) {
						utils.charReaderContextSkip(cc);
						utils.textIndexSkipReturn(cc.ti);
						prevCharWas13 = false;
						continue;
					}
					
					if(cc.resultChar == 0xA) {
						utils.charReaderContextSkip(cc);
						utils.textIndexSkipReturn(cc.ti);
						continue;
					}
					
					if(cc.resultChar == 0xD) {
						prevCharWas13 = true;
						utils.charReaderContextSkip(cc);
						continue;
					}
				}
				
				// goto next char
				utils.charReaderContextSkip(cc);
				continue;
			}
			
			if(cc.state == CharReaderData.STATE_END_OF_STREAM) {
				if(prevCharWas13) {
					utils.textIndexSkipReturn(cc.ti);
					prevCharWas13 = false;
				}
				
				helpers = dat.possibleHelpers.toArray();
				
				helpNum = 0;
				helpCount = helpers.length;
				
				while(helpNum < helpCount) {
					tokHelp = (TokenHelper) helpers[helpNum];
					
					//if(allocHelp == null) throw new NullPointerException();
					tokHelp.setAllocHelper(allocHelp);
					tokHelp.processEndOfStream(cc);
					tokHelp.setAllocHelper(null);

					helpDat = (TokenHelperData) tokHelp.getData();

					if(helpDat.state == BaseModuleData.STATE_STUCK) {
						dat.state = TokenChooserData.STATE_STUCK;
						dat.stuckState =
							StuckStates.STATE_DUE_TO_DESCENDANTS;
					}
					
					if(helpDat.state == TokenHelperData.STATE_DONE) {
						if(helpDat.match)
							dat.doneHelpers.add(tokHelp);

						if(!helpDat.match)
							transferAllocCounts(helpDat);
						
						dat.possibleHelpers.remove(tokHelp);
					}
					
					helpNum += 1;
				}
				
				if(dat.state == TokenChooserData.STATE_STUCK)
					return;
				
				chooseTokenFinish();
				return;
			}
			
			throwInvalidEnum("CharReader has an invalid state");
		}
		
		// unreachable
	}
	
	private void chooseTokenFinish() {
		TokenHelper tokHelp;
		TokenHelperData helpDat;
		int tokenCount;
		long tokenLength;
		
		tokenCount = 0;
		tokenLength = 0;
		
		int helpNum;
		int helpCount;
		
		helpNum = 0;
		helpCount = dat.doneHelpers.size();
		
		while(helpNum < helpCount) {
			tokHelp = (TokenHelper) dat.doneHelpers.get(helpNum);
			helpDat = (TokenHelperData) tokHelp.getData();

			if(helpDat.resultLength > tokenLength) {
				tokenCount = 1;
				tokenLength = helpDat.resultLength;
				helpNum += 1;
				continue;
			}

			if(helpDat.resultLength == tokenLength) {
				tokenCount += 1;
				helpNum += 1;
				continue;
			}
			
			helpNum += 1;
		}

		if(tokenCount == 0) {
			dat.resultCount = 0;
			dat.state = TokenChooserData.STATE_CHOSEN;
			return;
		}
		
		helpNum = 0;
		helpCount = dat.doneHelpers.size();
		
		while(helpNum < helpCount) {
			tokHelp = (TokenHelper) dat.doneHelpers.get(helpNum);
			helpDat = (TokenHelperData) tokHelp.getData();

			if(helpDat.resultLength == tokenLength) {
				dat.chosenHelpers.add(tokHelp);
			}
			
			transferAllocCounts(helpDat);
			
			helpNum += 1;
		}
		
		if(tokenCount == 1) {
			helpNum = 0;
			helpCount = dat.chosenHelpers.size();
			
			while(helpNum < helpCount) {
				tokHelp = (TokenHelper) dat.chosenHelpers.get(helpNum);

				dat.chosenHelper = tokHelp;

				helpNum += 1;
			}
		}
		
		dat.resultCount = tokenCount;
		dat.state = TokenChooserData.STATE_CHOSEN;
		return;
	}

	private void throwInvalidEnum(String msg) {
		CommonError e1;
		
		e1 = new CommonError();
		e1.id = unnamed.common.CommonErrorTypes.ERROR_INVALID_ENUM;
		e1.msg = msg;
		throw e1;
	}
	
	private void addExistingModule(CommonArrayList o, BaseModule child) {
		if(child != null) o.add(child);
		return;
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
	
	private void transferAllocCounts(TokenHelperData helpDat) {
		long aCount;

		aCount = helpDat.traceOldAllocCount;
		helpDat.traceOldAllocCount = 0;
		dat.traceOldAllocCount += aCount;

		if(aCount > 0) {
			//Class c = helpDat.getClass();
			//System.out.println("BadHelper," + c.getName());
		}

		aCount = helpDat.traceNewAllocCount;
		helpDat.traceNewAllocCount = 0;
		dat.traceNewAllocCount += aCount;

		if(aCount > 0) {
			//Class c = helpDat.getClass();
			//System.out.println("GoodHelper," + c.getName());
		}
	}
}
