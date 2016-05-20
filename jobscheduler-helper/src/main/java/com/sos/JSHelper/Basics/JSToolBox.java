package com.sos.JSHelper.Basics;

import java.io.BufferedWriter;
import java.io.File;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.localization.Messages;

public class JSToolBox extends JSListenerClass {

    private static final Logger LOGGER = Logger.getLogger(JSToolBox.class);
    private static final String ENVVAR_SOS_LOCALE = "SOS_LOCALE";
    protected final String EMPTY_STRING = "";
    BufferedWriter objOut = null;
    protected Messages Messages = null;
    protected boolean flgStackTracePrinted = false;

    public JSToolBox() {
        //
    }

    public JSToolBox(final String pstrResourceBundleName) {
        setMessageResource(pstrResourceBundleName);
    }

    public JSToolBox(final String pstrResourceBundleName, final String pstrLocale) {
        Messages = new Messages(pstrResourceBundleName, new Locale(pstrLocale));
    }

    public Messages getMessageObject() {
        return Messages;
    }

    public String getMethodName() {
        try {
            StackTraceElement trace[] = new Throwable().getStackTrace();
            String lineNumber = trace[1].getLineNumber() > 0 ? "(" + trace[1].getLineNumber() + ")" : "";
            return trace[1].getClassName() + "." + trace[1].getMethodName() + lineNumber;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "";
    }

    public String getClassName() throws Exception {
        StackTraceElement trace[] = new Throwable().getStackTrace();
        return trace[1].getClassName();
    }

    protected void setMessageResource(final String pstrResourceBundleName) {
        String strSOSLocale = System.getenv(ENVVAR_SOS_LOCALE);
        if (strSOSLocale == null) {
            Messages = new Messages(pstrResourceBundleName, Locale.getDefault());
        } else {
            Messages = new Messages(pstrResourceBundleName, new Locale(strSOSLocale));
        }
    }

    protected String makeFullPathName(final String pstrPathname, final String pstrFileName) {
        String strT = pstrFileName;
        String normalizedFilename = pstrFileName.replace('\\', '/');
        String normalizedPathname = pstrPathname.replace('\\', '/');
        if (!normalizedFilename.startsWith(normalizedPathname)) {
            if (normalizedPathname.endsWith("/")) {
                strT = pstrPathname + strT;
            } else {
                strT = pstrPathname + "/" + strT;
            }
        }
        return strT;
    }

    public String getI18N(final String pstrI18NKey) {
        String strM = Messages.getMsg(pstrI18NKey);
        strM = pstrI18NKey + ": " + strM;
        return strM;
    }

    public String boolean2String(final boolean pflgFlag) {
        String strRet = "true";
        if (!pflgFlag) {
            strRet = "false";
        }
        return strRet;
    }

    public String addSingleQuotes(final String pstrS) {
        final String strT = "'" + pstrS.replaceAll("'", "''") + "'";
        return strT;
    }

    protected String addQuotes(final String pstrS) {
        return "\"" + quotes2DoubleQuotes(pstrS) + "\"";
    }

    protected String quotes2DoubleQuotes(final String pstrS) {
        String strT = pstrS;
        if (strT != null) {
            strT = pstrS.replaceAll("\"", "\"\"");
        }
        return strT;
    }

    protected double toDouble(final String pstrV) throws Exception {
        final String conMethodName = conClassName + "::toDouble";
        double dblT = 0.0;
        String strT = pstrV.trim();
        final int intLen = strT.length();
        if (intLen > 0) {
            if (strT.endsWith("-")) {
                strT = "-" + strT.substring(0, intLen - 1);
            } else {
                if (strT.startsWith("+")) {
                    strT = strT.substring(1, intLen);
                } else {
                    if (strT.endsWith("+")) {
                        strT = strT.substring(0, intLen - 1);
                    }
                }
            }
            try {
                dblT = new Double(strT);
            } catch (final Exception objException) {
                try {
                    final NumberFormat numberF = NumberFormat.getNumberInstance(Locale.GERMAN);
                    numberF.setGroupingUsed(false);
                    numberF.setParseIntegerOnly(false);
                    numberF.setMaximumFractionDigits(5);
                    numberF.setMinimumFractionDigits(0);
                    final Number number = numberF.parse(strT);
                    dblT = number instanceof Double ? number.doubleValue() : new Double(number.doubleValue());
                } catch (final ParseException e) {
                    try {
                        final NumberFormat numberF = NumberFormat.getNumberInstance(Locale.US);
                        numberF.setGroupingUsed(false);
                        numberF.setParseIntegerOnly(false);
                        final Number number = numberF.parse(strT);
                        dblT = number instanceof Double ? number.doubleValue() : new Double(number.doubleValue());
                    } catch (final ParseException e1) {
                        LOGGER.error(e1.getMessage(), e1);
                        signalError(conMethodName + ": could not convert '" + strT + "' to double");
                    }
                } catch (final NumberFormatException e) {
                    signalError(conMethodName + ": could not convert '" + strT + "' to double");
                    dblT = 0.0;
                }
            }
        }
        return dblT;
    }

    public String creationTimeStamp() {
        return getISODate();
    }

    public String creationTimeStamp(final String pstrDate) throws Exception {
        return this.creationTimeStamp(pstrDate, "010203");
    }

    public String creationTimeStamp(final String pstrDate, final String pstrTime) throws Exception {
        String strD = "";
        String strT = "";
        if (!pstrDate.isEmpty() && !pstrTime.isEmpty()) {
            try {
                long lngTemp = Integer.parseInt(pstrDate);
                lngTemp = Integer.parseInt(pstrTime);
                strD = pstrDate.substring(0, 3 + 1) + "-" + pstrDate.substring(4, 5 + 1) + "-" + pstrDate.substring(6, 7 + 1) + "T";
                strT = pstrTime.substring(0, 1 + 1) + ":" + pstrTime.substring(2, 3 + 1) + ":" + pstrTime.substring(4, 5 + 1);
            } catch (final RuntimeException e) {
                strD = pstrDate + "T";
                strT = "??:??:??";
            }
        }
        return strD + strT;
    }

    @Override
    public String getDateTimeFormatted(final String pstrEditMask) {
        final java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(pstrEditMask);
        final java.util.Calendar now = java.util.Calendar.getInstance();
        return formatter.format(now.getTime()).toString();
    }

    public Date now() {
        final java.util.Calendar now = java.util.Calendar.getInstance();
        return now.getTime();
    }

    @Override
    public String getISODate() {
        return getDateTimeFormatted("yyyy-MM-dd'T'HH:mm:ss");
    }

    @Override
    public String getTime() {
        return getDateTimeFormatted("HH:mm:ss");
    }

    public String getHHIISS() {
        return getDateTimeFormatted("HHmmss");
    }

    public String getDate() {
        return getDateTimeFormatted("yyyy-MM-dd");
    }

    public String getYYYYMMDD() {
        return getDateTimeFormatted("yyyyMMdd");
    }

    public String environmentVariable(final String pstrVariableName) {
        final String conMethodName = conClassName + "::EnvironmentVariable";
        String strValue = null;
        if (isNotEmpty(pstrVariableName)) {
            strValue = System.getenv(pstrVariableName);
            if (isEmpty(strValue)) {
                strValue = System.getProperty(pstrVariableName);
            }
            if (isNotEmpty(strValue)) {
                strValue = stripQuotes(strValue);
                signalDebug(String.format("%s: %s = %s", conMethodName, pstrVariableName, strValue));
            }
        }
        return strValue;
    }

    public String stripQuotes(final String pstrS) {
        String strR = pstrS;
        if (pstrS.startsWith("\"") && pstrS.endsWith("\"")) {
            strR = pstrS.substring(1, pstrS.length() - 1);
            strR = strR.replaceAll("\"\"", "\"");
        }
        return strR;
    }

    protected boolean isNotEqual(final String pstrActual, final String pstrNew) {
        Boolean flgT = false;
        if (pstrActual == null && pstrNew == null) {
            flgT = false;
        } else {
            if (pstrActual == null || !pstrActual.equalsIgnoreCase(pstrNew.toString())) {
                flgT = true;
            }
        }
        return flgT;
    }

    public boolean isNotEmpty(final String pstrValue) {
        return pstrValue != null && !pstrValue.trim().isEmpty();
    }

    public boolean isNotEmpty(final StringBuffer pstrS) {
        if (pstrS != null && pstrS.length() > 0) {
            return true;
        }
        return false;
    }

    protected boolean isEmpty(final String pstrFileName) {
        return pstrFileName == null || pstrFileName.length() <= 0;
    }

    public boolean isNotNull(final Object pobjObject) {
        return pobjObject != null;
    }

    public boolean isNull(final Object pobjObject) {
        return pobjObject == null;
    }

    protected String notNull(final String pstrS) {
        if (pstrS == null) {
            return "";
        } else {
            return pstrS;
        }
    }

    public final static String repeatString(final String str, int length) {
        final StringBuilder sb = new StringBuilder();
        if (str != null) {
            while (length > 0) {
                sb.append(str);
                --length;
            }
        }
        return sb.toString();
    }

    public void raiseJSException(final String pstrExceptionText) throws Exception {
        throw new JobSchedulerException(pstrExceptionText);
    }

    public String stackTrace2String(final Exception e) {
        String strT = e.getMessage() + "\n";
        final StackTraceElement arrStack[] = e.getStackTrace();
        for (final StackTraceElement objS : arrStack) {
            strT += objS.toString() + "\n";
        }
        return strT;
    }

    public static void notImplemented() {
        throw new JobSchedulerException("Method/Functionality not implemented presently.");
    }

    public String addFileSeparator(final String str) {
        return str.endsWith("/") || str.endsWith("\\") ? str : str + "/";
    }

    public String adjustFileSeparator(final String pstrValue) {
        String str = pstrValue;
        if (!(str.startsWith("\\\\") || str.startsWith("//"))) {
            str = pstrValue.replaceAll("\\\\", "/");
            str = str.replaceAll("//", "/");
        }
        return str;
    }

    public static URL fileAsURL(final File file) {
        URL result = null;
        try {
            result = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new JobSchedulerException("Error to convert file object to URL.", e);
        }
        return result;
    }

    public static File urlAsFile(final URL url) {
        File result = null;
        try {
            result = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new JobSchedulerException("Error to convert URL object to File.", e);
        }
        return result;
    }

    protected String bigInt2String(final BigInteger pbigI) {
        if (pbigI != null) {
            long lngT = pbigI.longValue();
            return String.valueOf(lngT);
        }
        return "";
    }

    public boolean string2Bool(final String pstrVal) {
        boolean flgT = false;
        if (isNotEmpty(pstrVal)
                && ("1".equals(pstrVal) || "y".equalsIgnoreCase(pstrVal) || "yes".equalsIgnoreCase(pstrVal) || "j".equalsIgnoreCase(pstrVal)
                        || "on".equalsIgnoreCase(pstrVal) || "true".equalsIgnoreCase(pstrVal) || "wahr".equalsIgnoreCase(pstrVal))) {
            flgT = true;
        }
        return flgT;
    }

    public static String escapeHTML(final String s) {
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            switch (c) {
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            case '"':
                sb.append("&quot;");
                break;
            case '\u00E0':
                sb.append("&agrave;");
                break;
            case '\u00C0':
                sb.append("&Agrave;");
                break;
            case '\u00E2':
                sb.append("&acirc;");
                break;
            case '\u00C2':
                sb.append("&Acirc;");
                break;
            case '\u00E4':
                sb.append("&auml;");
                break;
            case '\u00C4':
                sb.append("&Auml;");
                break;
            case '\u00E5':
                sb.append("&aring;");
                break;
            case '\u00C5':
                sb.append("&Aring;");
                break;
            case '\u00E6':
                sb.append("&aelig;");
                break;
            case '\u00C6':
                sb.append("&AElig;");
                break;
            case '\u00E7':
                sb.append("&ccedil;");
                break;
            case '\u00C7':
                sb.append("&Ccedil;");
                break;
            case '\u00E9':
                sb.append("&eacute;");
                break;
            case '\u00C9':
                sb.append("&Eacute;");
                break;
            case '\u00E8':
                sb.append("&egrave;");
                break;
            case '\u00C8':
                sb.append("&Egrave;");
                break;
            case '\u00EA':
                sb.append("&ecirc;");
                break;
            case '\u00CA':
                sb.append("&Ecirc;");
                break;
            case '\u00EB':
                sb.append("&euml;");
                break;
            case '\u00CB':
                sb.append("&Euml;");
                break;
            case '\u00EF':
                sb.append("&iuml;");
                break;
            case '\u00CF':
                sb.append("&Iuml;");
                break;
            case '\u00F4':
                sb.append("&ocirc;");
                break;
            case '\u00D4':
                sb.append("&Ocirc;");
                break;
            case '\u00F6':
                sb.append("&ouml;");
                break;
            case '\u00D6':
                sb.append("&Ouml;");
                break;
            case '\u00F8':
                sb.append("&oslash;");
                break;
            case '\u00D8':
                sb.append("&Oslash;");
                break;
            case '\u00DF':
                sb.append("&szlig;");
                break;
            case '\u00F9':
                sb.append("&ugrave;");
                break;
            case '\u00D9':
                sb.append("&Ugrave;");
                break;
            case '\u00FB':
                sb.append("&ucirc;");
                break;
            case '\u00DB':
                sb.append("&Ucirc;");
                break;
            case '\u00FC':
                sb.append("&uuml;");
                break;
            case '\u00DC':
                sb.append("&Uuml;");
                break;
            case '\u00AE':
                sb.append("&reg;");
                break;
            case '\u00A9':
                sb.append("&copy;");
                break;
            case '\u20AC':
                sb.append("&euro;");
                break;
            // be carefull with this one (non-breaking white space)
            case ' ':
                sb.append("&nbsp;");
                break;
            default:
                sb.append(c);
                break;
            }
        }
        return sb.toString();
    }

}