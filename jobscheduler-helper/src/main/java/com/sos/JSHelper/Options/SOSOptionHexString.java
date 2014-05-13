/**
 *
 */
package com.sos.JSHelper.Options;

import java.io.UnsupportedEncodingException;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * @author KB
 *
 */
public class SOSOptionHexString extends SOSOptionFileString {

	/**
	 *
	 */
	private static final long	serialVersionUID	= 5459978964312384049L;
	@SuppressWarnings("unused")
	private final String		conClassName		= "SOSOptionHexString";

	/**
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 */
	public SOSOptionHexString(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
			final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
	}

	@Override
	public String Value() {

		/**
		 * TODO reconvert XML Entities. Create a separate Methode: reconvertXMLEntities()
		 * this Method should be implemented in one of the base classes
		 */
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Value";

		String strV = strValue;
		if (isNotEmpty(strV) && isHex(strV)) {
			try {
				strV = new String(fromHexString(strV), "US-ASCII");
			}
			catch (UnsupportedEncodingException e) {
				e.printStackTrace(System.err);
			}
			catch (IllegalArgumentException e) {
				e.printStackTrace(System.err);
			}
		}
		if (strV.indexOf("&") > -1) {
			strV = unescapeXML(strV);
		}

		return strV;
	} // private String Value

	/**
	 * Unescapes a string containing XML entity escapes to a string containing the actual Unicode characters corresponding to the escapes.

	 *  Supports only the five basic XML entities (gt, lt, quot, amp, apos).
	 *
	 *  see http://www.w3.org/TR/1998/REC-xml-19980210#AVNormalize for further explanations
	 *
	 *  	\n - Line Feed - 0x0A - 10 decimal - LF
			\r - Carriage Return - 0X0D - 13 decimal - CR
			\t - tab - 0x09 - 9 decimal - ht (horizontal tab)

			For detailed hex, decimal values refer: http://web.cs.mun.ca/~michael/c/ascii-table.html

			CR+LF: DEC TOPS-10, RT-11 and most other early non-Unix and non-IBM OSes, CP/M, MP/M, DOS (MS-DOS, PC-DOS, etc.), Atari TOS, OS/2, Microsoft Windows, Symbian OS, Palm OS
			LF+CR: Acorn BBC spooled text output.
			CR: Commodore 8-bit machines, Acorn BBC, TRS-80, Apple II family, Mac OS up to version 9 and OS-9
			LF: Multics, Unix and Unix-like systems (GNU/Linux, AIX, Xenix, Mac OS X, FreeBSD, etc.), BeOS, Amiga, RISC OS, and others. However, in tty 'raw mode', CR+LF is used for output and CR is used for input.
			RS: QNX pre-POSIX implementation.

			For more details on \n, \r \t refer:
			http://en.wikipedia.org/wiki/Newline
			http://en.wikipedia.org/wiki/Carriage_return
			http://en.wikipedia.org/wiki/Horizontal_tab

			To use \n \r \t in html for you can use the below codes:
			\n in html == &#10; or &#x0A; linux, Unix and Mac OS X
			\r in html == &#13; or &#x0D; Mac(classic)
			\r\n in html == &#13;&#10; or &#x0D;&#x0A; Windows
			\t in html == &#9; or &#x09;
	 * @return
	 */
	public String unescapeXML() {
		return unescapeXML(strValue);
	}

	public String unescapeXML(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::unescapeXML";

		String newValue = pstrValue;
		if (newValue.indexOf("&") != -1) {
			newValue = newValue.replaceAll("&quot;", "\"");
			newValue = newValue.replaceAll("&lt;", "<");
			newValue = newValue.replaceAll("&gt;", ">");
			newValue = newValue.replaceAll("&amp;", "&");
			newValue = newValue.replaceAll("&apos;", "'");
			newValue = newValue.replaceAll("&#13;", "\r");
			newValue = newValue.replaceAll("&#x0d;", "\r");
			newValue = newValue.replaceAll("&#xd;", "\r");
			newValue = newValue.replaceAll("&#09;", "\t");
			newValue = newValue.replaceAll("&#9;", "\t");
			newValue = newValue.replaceAll("&#10;", "\n");
			newValue = newValue.replaceAll("&#x0a;", "\n");
			newValue = newValue.replaceAll("&#xa;", "\n");
		}
		// "&#13;&#10;"  &#x0d; &#x0a;
		return newValue;

	} // private String unescapeXML

	public byte[] fromHexString() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::fromHexString";

		return this.fromHexString(strValue);

	} // private byte[] fromHexString

	public byte[] fromHexString(final String s) throws IllegalArgumentException {

		int stringLength = s.length();
		if ((stringLength & 0x1) != 0) {
			throw new JobSchedulerException(String.format("fromHexString '%1$s' requires an even number of hex characters", s));
		}
		byte[] b = new byte[stringLength / 2];

		for (int i = 0, j = 0; i < stringLength; i += 2, j++) {
			int high = charToNibble(s.charAt(i));
			int low = charToNibble(s.charAt(i + 1));
			b[j] = (byte) (high << 4 | low);
		}
		return b;
	}

	private int charToNibble(final char c) {

		if ('0' <= c && c <= '9') {
			return c - '0';
		}
		else
			if ('a' <= c && c <= 'f') {
				return c - 'a' + 0xa;
			}
			else
				if ('A' <= c && c <= 'F') {
					return c - 'A' + 0xa;
				}
				else {
					throw new JobSchedulerException("Invalid hex character: " + c);
				}
	}

	public boolean isHex() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::isHex";

		boolean flgRet = this.isHex(strValue);

		return flgRet;
	} // private boolean isHex

	public final boolean isHex(final String pstrHexString) {
		boolean flgRet = false;

		if (isNotEmpty(pstrHexString)) {
			flgRet = true;
			for (int i = 0; i < pstrHexString.length(); i++) {
				if (isHexStringChar(pstrHexString.charAt(i)) == false) {
					flgRet = false;
					break;
				}
			}
		}
		return flgRet;
	}

	public final boolean isHexStringChar(final char c) {
		return Character.isDigit(c) || Character.isWhitespace(c) || "0123456789abcdefABCDEF".indexOf(c) >= 0;
	}

}
