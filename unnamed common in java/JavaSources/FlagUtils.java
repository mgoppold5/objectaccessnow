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

package unnamed.common;

public class FlagUtils {
	public static short packInt16WithInt8(byte f1, byte f2) {
		short f;
		short tempFlags;
		
		f = 0;
		
		tempFlags = f1;
		tempFlags &= 0xFF;
		
		f |= tempFlags;
		
		tempFlags = f2;
		tempFlags &= 0xFF;
		
		f |= tempFlags << 8;
		
		return f;
	}
	
	public static int packInt32WithInt16(short f1, short f2) {
		int f;
		int tempFlags;
		
		f = 0;
		
		tempFlags = f1;
		tempFlags &= 0xFFFF;
		
		f |= tempFlags;
		
		tempFlags = f2;
		tempFlags &= 0xFFFF;
		
		f |= tempFlags << 16;
		
		return f;
	}
	
	public static int packInt32WithInt8(
		byte f1, byte f2, byte f3, byte f4) {
		
		return packInt32WithInt16(
			packInt16WithInt8(f1, f2),
			packInt16WithInt8(f3, f4));
	}
	
	public static long packInt64WithInt32(int f1, int f2) {
		long f;
		long tempFlags;
		
		f = 0;
		
		tempFlags = f1;
		tempFlags &= 0xFFFFFFFF;
		
		f |= tempFlags;
		
		tempFlags = f2;
		tempFlags &= 0xFFFFFFFF;
		
		f |= tempFlags << 32;
		
		return f;
	}
	
	public static long packInt64WithInt16(
		short f1, short f2, short f3, short f4) {
		
		return packInt64WithInt32(
			packInt32WithInt16(f1, f2),
			packInt32WithInt16(f3, f4));
	}
	
	public static byte unpackInt8FromInt16(short f1, int flagIntNum) {
		if(flagIntNum >= 2)
			throw makeIndexOutOfBoundsException(
				"flag integer number too big");
		
		return (byte) (f1 >> (flagIntNum * 8));
	}
	
	public static short unpackInt16FromInt32(int f1, int flagIntNum) {
		if(flagIntNum >= 2)
			throw makeIndexOutOfBoundsException(
				"flag integer number too big");
			
		return (short) (f1 >> (flagIntNum * 16));
	}
	
	public static byte unpackInt8FromInt32(int f1, int flagIntNum) {
		if(flagIntNum >= 4)
			throw makeIndexOutOfBoundsException(
				"flag integer number too big");
		
		return (byte) (f1 >> (flagIntNum * 8));
	}
	
	public static short unpackInt32FromInt64(int f1, int flagIntNum) {
		if(flagIntNum >= 2)
			throw makeIndexOutOfBoundsException(
				"flag integer number too big");
			
		return (short) (f1 >> (flagIntNum * 32));
	}
	
	public static short unpackInt16FromInt64(long f1, int flagIntNum) {
		if(flagIntNum >= 4)
			throw makeIndexOutOfBoundsException(
				"flag integer number too big");
			
		return (short) (f1 >> (flagIntNum * 16));
	}
	
	public static boolean getFlagInt8(byte f, int flagNum) {
		if(flagNum >= 8)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		return ((f >>> flagNum) & 1) == 1;
	}

	public static boolean getFlagInt16(short f, int flagNum) {
		if(flagNum >= 16)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		return ((f >>> flagNum) & 1) == 1;
	}
	
	public static boolean getFlagInt32(int f, int flagNum) {
		if(flagNum >= 32)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		return ((f >>> flagNum) & 1) == 1;
	}

	public static boolean getFlagInt64(long f, int flagNum) {
		if(flagNum >= 64)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		return ((f >>> flagNum) & 1) == 1;
	}
	
	public static byte setFlagInt8(byte f1, int flagNum, boolean value) {
		byte f;
		
		if(flagNum >= 8)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		f = f1;
		
		if(value) f |= (1 << flagNum);
		if(!value) f &= ~(1 << flagNum);
		return f;
	}

	public static short setFlagInt16(short f1, int flagNum, boolean value) {
		short f;
		
		if(flagNum >= 16)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		f = f1;
		
		if(value) f |= (1 << flagNum);
		if(!value) f &= ~(1 << flagNum);
		return f;
	}

	public static int setFlagInt32(int f1, int flagNum, boolean value) {
		int f;
		
		if(flagNum >= 32)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		f = f1;
		
		if(value) f |= (1 << flagNum);
		if(!value) f &= ~(1 << flagNum);
		return f;
	}
	
	public static long setFlagInt64(long f1, int flagNum, boolean value) {
		long f;
		
		if(flagNum >= 64)
			throw makeIndexOutOfBoundsException(
				"flag number too big");
		
		f = f1;
		
		if(value) f |= (1 << flagNum);
		if(!value) f &= ~(1 << flagNum);
		return f;
	}
	
	private static RuntimeException makeIndexOutOfBoundsException(
		String msg) {
		
		if(msg == null)
			return new IndexOutOfBoundsException();
		
		return new IndexOutOfBoundsException(msg);
	}
}
