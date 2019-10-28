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

def main():
	QUOTE = "\""

	subDirList = loadStringListFromFile("ProjectInfo/special-folder.list")
	otherFolderList = loadStringListFromFile("ProjectInfo/project-stack.list")

	for otherFolder in otherFolderList:
		for subDir in subDirList:
			fromDir = "../" + otherFolder + "/" + subDir
			if(not os.path.isdir(fromDir)): continue
			runWithCheck("cp --recursive --preserve=timestamps"
				+ " " + QUOTE + fromDir + QUOTE
				+ " .")

main()
