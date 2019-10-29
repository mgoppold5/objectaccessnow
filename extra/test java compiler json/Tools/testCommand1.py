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


if(False):
	testDir1 = "../test java compiler json/tests/regular"
	testDir2 = QUOTE + testDir1 + QUOTE

	cmd2 = (
		cmd1 + " -jar test.jar"

		+ " --create-file-path-node " + testDir2
		+ " --open-local-file-system " + testDir2

		+ " --test-dir " + testDir2
		+ " --test-file test2.json"
		)

	os.system(cmd2)

