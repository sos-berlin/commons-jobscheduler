/*
 * Copyright 2009 Lukasz Wozniak
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.sos.VirtualFileSystem.CredentialStore.KeePass.pl.sind.keepass.util;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Utility class to handle binary data.<br>
 * 
 * @author Lukasz Wozniak
 * 
 */
public class Utils {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	/**
	 * Unpacks date stored in kdb format.
	 * 
	 * @param d
	 * @return
	 */
	public static Date unpackDate(final byte[] d) {
		// Byte bits: 00000000 11111111 22222222 33333333 44444444
		// Contents : 00YYYYYY YYYYYYMM MMDDDDDH HHHHMMMM MMSSSSSS
		int year = d[0] << 6 | d[1] >> 2 & 0x0000003F;
		int month = (d[1] & 0x00000003) << 2 | d[2] >> 6 & 0x00000003;
		int day = d[2] >> 1 & 0x0000001F;
		int hour = (d[2] & 0x00000001) << 4 | d[3] >> 4 & 0x0000000F;
		int minute = (d[3] & 0x0000000F) << 2 | d[4] >> 6 & 0x00000003;
		int second = d[4] & 0x0000003F;
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month - 1, day, hour, minute, second);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static byte[] packDate(final Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		// Byte bits: 00000000 11111111 22222222 33333333 44444444
		// Contents : 00YYYYYY YYYYYYMM MMDDDDDH HHHHMMMM MMSSSSSS
		byte[] bytes = new byte[5];
		int s = c.get(Calendar.SECOND);
		int m = c.get(Calendar.MINUTE);
		int h = c.get(Calendar.HOUR_OF_DAY);
		int d = c.get(Calendar.DATE);
		int mm = c.get(Calendar.MONTH) + 1;
		int y = c.get(Calendar.YEAR);
		bytes[4] = (byte) (m << 6 | s);
		bytes[3] = (byte) (m >> 2 | h << 4);
		bytes[2] = (byte) (h >> 4 | d << 1 | mm << 6);
		bytes[1] = (byte) (mm >> 2 | y << 2);
		bytes[0] = (byte) (y >> 6);
		return bytes;
	}

	/**
	 * Creates byte array representation of HEX string.<br>
	 * 
	 * @param s
	 *            string to parse
	 * @return
	 */
	public static byte[] fromHexString(final String s) {
		int length = s.length() / 2;
		byte[] bytes = new byte[length];
		for (int i = 0; i < length; i++) {
			bytes[i] = (byte) (Character.digit(s.charAt(i * 2), 16) << 4 | Character.digit(s.charAt(i * 2 + 1), 16));
		}
		return bytes;
	}

	/**
	 * Creates HEX String representation of supplied byte array.<br/>
	 * Each byte is represented by a double character element from 00 to ff
	 * 
	 * @param fieldData
	 *            to be tringed
	 * @return
	 */
	public static String toHexString(final byte[] fieldData) {
		StringBuilder sb = new StringBuilder();
		for (byte element : fieldData) {
			int v = element & 0xFF;
			if (v <= 0xF) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(v));
		}
		return sb.toString();
	}

	public static int bytesToInt(final byte[] data) {
		int value = data[3] & 0xff;
		value = value << 8;
		value |= data[2] & 0xff;
		value <<= 8;
		value |= data[1] & 0xff;
		value <<= 8;
		value |= data[0] & 0xff;
		return value;
	}

	public static byte[] intTobytes(final int value) {
		byte[] bytes = new byte[4];
		intTobytes(value, bytes);
		return bytes;
	}

	public static void intTobytes(final int value, final byte[] bytes) {
		bytes[3] = (byte) (value >> 24 & 0xff);
		bytes[2] = (byte) (value >> 16 & 0xff);
		bytes[1] = (byte) (value >> 8 & 0xff);
		bytes[0] = (byte) (value & 0xff);
	}

	public static void shortTobytes(final short value, final byte[] bytes) {
		bytes[1] = (byte) (value >> 8 & 0xff);
		bytes[0] = (byte) (value & 0xff);
	}

	public static short bytesToShort(final byte[] data) {
		short value = (short) (data[1] & 0xff);
		value <<= 8;
		value |= data[0] & 0xff;
		return value;
	}
}
