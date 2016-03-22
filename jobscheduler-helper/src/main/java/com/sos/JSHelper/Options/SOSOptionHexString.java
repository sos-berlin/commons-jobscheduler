package com.sos.JSHelper.Options;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSOptionHexString extends SOSOptionFileString {

    private static final long serialVersionUID = 5459978964312384049L;
    private static final Logger LOGGER = Logger.getLogger(SOSOptionHexString.class);

    public SOSOptionHexString(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    @Override
    public String Value() {
        String strV = strValue;
        if (isNotEmpty(strV) && isHex(strV)) {
            try {
                strV = new String(fromHexString(strV), "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (IllegalArgumentException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (strV.indexOf("&") > -1) {
            strV = unescapeXML(strV);
        }
        return strV;
    }

    public String unescapeXML() {
        return unescapeXML(strValue);
    }

    public String unescapeXML(final String pstrValue) {
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
        return newValue;
    }

    public byte[] fromHexString() {
        return this.fromHexString(strValue);
    }

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
        } else if ('a' <= c && c <= 'f') {
            return c - 'a' + 0xa;
        } else if ('A' <= c && c <= 'F') {
            return c - 'A' + 0xa;
        } else {
            throw new JobSchedulerException("Invalid hex character: " + c);
        }
    }

    public boolean isHex() {
        boolean flgRet = this.isHex(strValue);
        return flgRet;
    }

    public final boolean isHex(final String pstrHexString) {
        boolean flgRet = false;
        if (isNotEmpty(pstrHexString)) {
            flgRet = true;
            for (int i = 0; i < pstrHexString.length(); i++) {
                if (!isHexStringChar(pstrHexString.charAt(i))) {
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
