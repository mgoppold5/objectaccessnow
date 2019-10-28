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

#
# This is a generic test compile script
#

import os

javaDir = os.environ["JDK_HOME"]

def runWithCheck(cmd):
	r = os.system(cmd)
	if(r != 0):
		raise Exception("runWithCheck," + str(r) + "," + cmd)

def main():
	runWithCheck("mkdir -p classes")
	runWithCheck(javaDir + "/bin/javac -d classes JavaSources/*.java")
	runWithCheck(javaDir + "/bin/jar cfm test.jar ProjectInfo/manifest.txt -C classes .")

main()
