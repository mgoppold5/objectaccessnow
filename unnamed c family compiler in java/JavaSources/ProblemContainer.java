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
 * This Java class interface defines a thing that stores and returns
 * errors and warnings.
 */

package unnamed.family.compiler;

import unnamed.common.*;

public class ProblemContainer {
	public CommonArrayList problems;
	
	public int problemCount;
	public int runtimeErrorCount;
	public int langErrorCount;
	public int langWarningCount;
	
	public void init() {
		problems = makeArrayList();
		
		reset();
	}
	
	public void reset() {
		problems.clear();
		
		problemCount = 0;
		runtimeErrorCount = 0;
		langErrorCount = 0;
		langWarningCount = 0;		
	}
	
	public void addProblem(int problemLevel, Throwable ex) {
		Problem prob = new Problem();
		prob.problemLevel = problemLevel;
		prob.errorObject = ex;
		
		problemCount += 1;
		
		switch(problemLevel) {
		case ProblemLevels.LEVEL_RUNTIME_ERROR:
			runtimeErrorCount += 1;
			break;
		case ProblemLevels.LEVEL_LANG_ERROR:
			langErrorCount += 1;
			break;
		case ProblemLevels.LEVEL_LANG_WARNING:
			langWarningCount += 1;
			break;
		}
		
		problems.add(prob);
	}
	
	private CommonArrayList makeArrayList() {
		return CommonUtils.makeArrayList();
	}
}
