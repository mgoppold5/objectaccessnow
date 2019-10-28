#
# Copyright (c) 2017 Mike Goppold von Lobsdorf
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License along
# with this program; if not, write to the Free Software Foundation, Inc.,
# 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
#

import os
import sys

def runWithCheck(cmd):
	r = os.system(cmd)
	if(r != 0):
		raise Exception("runWithCheck," + str(r) + "," + cmd)

def loadStringListFromFile(path):
	lst = []
	f = open(path, "r")
	while(True):
		str = f.readline()
		if(str is None): break
		if(len(str) == 0): break
		str = str.strip()
		lst.append(str)
	return lst

def makeDirs2(theDir):
	if(os.path.isdir(theDir)): return
	os.makedirs(theDir)

def join(path1, path2):
	if(path1 is None): return path2
	if(path2 is None): return path1
	return path1 + "/" + path2

class DirInfo:
	def __init__(self):
		self.grpSpecialDir = None
		self.conventionName = None
		self.srcDir = None
		self.logicCount = 0
		self.docCount = 0

def doName(dInfo, grpPath, nm):
	QUOTE = "\""
	logicExt = ".java"
	docExt = ".doc.xml"

	trgDir = join(dInfo.srcDir, grpPath)
	
	srcPath = join(dInfo.srcDir, nm + logicExt)
	trgPath = join(trgDir, nm + logicExt)

	if(os.path.isfile(srcPath)):
		makeDirs2(trgDir)

		runWithCheck("mv"
			+ " " + QUOTE + srcPath + QUOTE
			+ " " + QUOTE + trgDir + QUOTE)
	
	if(not os.path.isfile(trgPath)):
		#raise Exception("logic not exist: " + nm)
		#print("logic not exist: " + nm)
		pass

	if(os.path.isfile(trgPath)):
		dInfo.logicCount += 1

	srcPath = join(dInfo.srcDir, nm + docExt)
	trgPath = join(trgDir, nm + docExt)

	if(os.path.isfile(srcPath)):
		makeDirs2(trgDir)

		runWithCheck("mv"
			+ " " + QUOTE + srcPath + QUOTE
			+ " " + QUOTE + trgDir + QUOTE)

	if(os.path.isfile(trgPath)):
		dInfo.docCount += 1

def doDir(dInfo, grpPath):
	listExt = ".list"

	conventionName = dInfo.conventionName
	if(conventionName is None):
		conventionName = "standard"

	prefPath = join(dInfo.grpSpecialDir, conventionName)
	prefPath = join(prefPath, grpPath)

	if(not os.path.isdir(prefPath)):
		raise Exception("group dir not exist: " + prefPath)

	dirList1 = os.listdir(prefPath)

	logicCount1 = dInfo.logicCount
	docCount1 = dInfo.docCount

	dInfo.logicCount = 0
	dInfo.docCount = 0

	logicCount2 = 0
	docCount2 = 0

	dirList2 = []
    
	for nm in dirList1:
		nm2 = None
		if(nm.endswith(listExt)):
			nm2 = nm[0:len(nm) - len(listExt)]
        
		if(nm2 is None):
			nm2 = nm
        
		if(not nm2 in dirList2):
			if(not nm2 is None):
				dirList2.append(nm2)

	for nm in dirList2:
		prefPath2 = join(prefPath, nm)
		grpPath2 = join(grpPath, nm)
		
		ok = False
		logicCount3 = 0
		docCount3 = 0

		if(os.path.isdir(prefPath2)):
			doDir(dInfo, grpPath2)

			logicCount3 += dInfo.logicCount
			docCount3 += dInfo.docCount

			dInfo.logicCount = 0
			dInfo.docCount = 0

			ok = True

		if(os.path.isfile(prefPath2 + listExt)):
			nList = loadStringListFromFile(prefPath2 + listExt)

			for nm3 in nList:
				if(len(nm3) == 0): continue

				doName(dInfo, grpPath2, nm3)

				logicCount3 += dInfo.logicCount
				docCount3 += dInfo.docCount

				dInfo.logicCount = 0
				dInfo.docCount = 0

			ok = True
        
		if(ok):
			if(not grpPath2 is None):
				print(grpPath2 + "," + str(logicCount3))

			logicCount2 += logicCount3
			docCount2 += docCount3
		
		if(not ok):
			raise Exception("bad file in Groups tree: " + nm)

	dInfo.logicCount = logicCount1 + logicCount2
	dInfo.docCount = docCount1 + docCount2

def main():
	dInfo = DirInfo()
	dInfo.grpSpecialDir = "ProjectInfo/Groups"
	dInfo.srcDir = "JavaSources"

	args = sys.argv

	grpPath = None

	i = 1
	count = len(args)
	while(i < count):
		arg = args[i]

		nextArg = None
		if(i + 1 < count): nextArg = args[i + 1]

		cmdLen = 0

		if(not arg.startswith("--")):
			print("argument not a --command")
			return

		if(arg == "--convention-name"):
			dInfo.conventionName = nextArg
			cmdLen = 2

		if(arg == "--group-path"):
			grpPath = nextArg
			cmdLen = 2

		if(cmdLen == 0):
			print("command not recognized: " + arg)
			return

		i += cmdLen

	doDir(dInfo, grpPath)

main()
