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

javaDir = os.environ["JDK_HOME"]

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

class DirInfo:
	def __init__(self):
		self.grpCompileSpecialDir = None
		self.conventionName = None
		self.grpPath = None
		self.clsDir = None
		self.srcDir = None
		self.libDir = None

def join(path1, path2):
	if(path1 is None): return path2
	if(path2 is None): return path1
	return path1 + "/" + path2

def filesInTree(path, ext):
	comboStr = ""
	dirList = os.listdir(path)
	for nm in dirList:
		path2 = join(path, nm)

		if(os.path.isdir(path2)):
			comboStr += filesInTree(path2, ext)
			continue

		ok = False
		if(nm.endswith(ext)):
			if(os.path.isfile(path2)):
				ok = True

		if(ok):
			# does not create quoted parameters
			comboStr += " " + path2
			continue

	return comboStr

def compileStack(dInfo):
	QUOTE = "\""
	logicExt = ".java"
	listExt = ".list"
	javaClassPath = None
	javaClassPathSep = ":"
	conventionName = None

	conventionName = dInfo.conventionName
	if(conventionName is None):
		conventionName = "standard"

	grpCompileSpecialDir2 = join(dInfo.grpCompileSpecialDir, conventionName)

	regularName1 = dInfo.grpPath.replace("/", "-")
	pref = join(grpCompileSpecialDir2, regularName1 + listExt)

	grpCompileList = loadStringListFromFile(pref)

	count = len(grpCompileList)



	i = 0
	
	while(i < count):
		pth = grpCompileList[i]
		libDir2 = join("..", dInfo.libDir)
		jarRegularName = pth.replace("/", "-")
		if(not os.path.isdir(libDir2)): break
	
		jarPath = join(libDir2, jarRegularName + ".jar")
		if(not os.path.isfile(jarPath)): break;
		
		isAppend = (not javaClassPath is None)
		if(isAppend):
			javaClassPath += javaClassPathSep + jarPath
		if(not isAppend):
			javaClassPath = jarPath
		
		i += 1
		
	while(i < count):
		pth = grpCompileList[i]
		if(len(pth) == 0):
			i += 1
			continue

		regularName = pth.replace("/", "-")

		clsDir2 = join(dInfo.clsDir, pth)
		makeDirs2(clsDir2)
		
		srcDir2 = dInfo.srcDir + "/" + pth
		if(not os.path.isdir(srcDir2)):
			print("logic not found in path: " + srcDir2)
			return
		
		cmd = javaDir + "/bin/javac"
		if(not javaClassPath is None):
			cmd += " -cp " + QUOTE + javaClassPath + QUOTE
		cmd += " -d " + QUOTE + clsDir2 + QUOTE
		cmd += filesInTree(srcDir2, logicExt)
		runWithCheck(cmd)

		jarName = regularName + ".jar"
		cmd = javaDir + "/bin/jar cf"
		cmd += " " + jarName
		cmd += " -C " + QUOTE + clsDir2 + QUOTE
		cmd += " ."
		runWithCheck(cmd)
		
		isAppend = (not javaClassPath is None)
		if(isAppend):
			javaClassPath += javaClassPathSep + jarName
		if(not isAppend):
			javaClassPath = jarName

		i += 1

def main():
	dInfo = DirInfo()
	dInfo.grpCompileSpecialDir = "ProjectInfo/GroupCompileLists"
	dInfo.srcDir = "JavaSources"
	dInfo.clsDir = "classes"
	dInfo.libDir = None

	args = sys.argv

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
			dInfo.grpPath = nextArg
			cmdLen = 2

		if(arg == "--extra-library-dir"):
			dInfo.libDir = nextArg
			cmdLen = 2

		if(cmdLen == 0):
			print("command not recognized: " + arg)
			return

		i += cmdLen

	if(dInfo.grpPath is None):
		print("--group-path not specified")
		return

	compileStack(dInfo)

main()
