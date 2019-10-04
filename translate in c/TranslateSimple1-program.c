/*
 * Copyright (C) 2019  Mike Goppold von Lobsdorf
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

/*
 * The complete TranslateSimple1 program
 */

/*
 * Compile with "g++ TranslateSimple1-program.c -I /usr/include -l X11"
 */

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xos.h>

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#include "BaseTypes.c"
#include "TypicalObject-access.c"
#include "TypicalString-access.c"
#include "RuntimeException-access.c"
#include "OutOfMemoryException-access.c"
#include "OutOfBoundsException-access.c"
#include "TypicalStringUtils-access.c"
#include "TypicalStringUtils-design.c"
#include "TypicalInt8Array-access.c"
#include "TypicalInt8Array-design.c"
#include "TypicalStringUtils2-access.c"
#include "TypicalStringUtils2-design.c"
#include "ConsoleUtils-access.c"
#include "ConsoleUtils-design.c"

#include "TranslateSimple1-access.c"
#include "TranslateSimple1-design.c"
#include "TranslateSimple1-main.c"
