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

QUOTE = "\""

env = os.environ
path1 = env["JRE_HOME"]
cmd1 = path1 + "/bin/java"

grammarDir1 = "../test java compiler arith parser/grammars"
grammarDir2 = QUOTE + grammarDir1 + QUOTE

testDir1 = "../test java compiler arith parser/tests"
testDir2 = QUOTE + testDir1 + QUOTE

cmd2 = (cmd1 + " -jar test.jar"
	+ " --create-file-path-node " + grammarDir2
	+ " --open-local-file-system " + grammarDir2

	+ " --grammar-dir " + grammarDir2
	+ " --grammar-file arith-simple-1-0.grm"

	+ " --create-file-path-node " + testDir2
	+ " --open-local-file-system " + testDir2

	+ " --test-dir " + testDir2
	+ " --test-file test1.txt")

os.system(cmd2)
