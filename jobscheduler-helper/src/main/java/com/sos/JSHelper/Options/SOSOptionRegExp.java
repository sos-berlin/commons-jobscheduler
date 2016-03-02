package com.sos.JSHelper.Options;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class SOSOptionRegExp extends SOSOptionStringWVariables {

    private static final long serialVersionUID = 8393808803161272343L;
    private static final Logger LOGGER = Logger.getLogger(SOSOptionRegExp.class);
    private static final HashMap<String, String> defaultProposals = new HashMap<>();
    private Pattern objCurrentPattern = null;
    private int intRegExpFlags = Pattern.CASE_INSENSITIVE;
    private Matcher matcher = null;
    private String strMatchValue = "";
    private Vector<String> lstMatchValues = null;

    public SOSOptionRegExp(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public int getRegExpFlags() {
        return intRegExpFlags;
    }

    public void setRegExpFlags(final int pintRegExpFlags) {
        intRegExpFlags = pintRegExpFlags;
    }

    public String getRegExpFlagsText() {
        String msg = "";
        if (has(intRegExpFlags, Pattern.CANON_EQ))
            msg += "CANON_EQ ";
        if (has(intRegExpFlags, Pattern.CASE_INSENSITIVE))
            msg += "CASE_INSENSITIVE ";
        if (has(intRegExpFlags, Pattern.COMMENTS))
            msg += "COMMENTS ";
        if (has(intRegExpFlags, Pattern.DOTALL))
            msg += "DOTALL ";
        if (has(intRegExpFlags, Pattern.MULTILINE))
            msg += "MULTILINE ";
        if (has(intRegExpFlags, Pattern.UNICODE_CASE))
            msg += "UNICODE_CASE ";
        if (has(intRegExpFlags, Pattern.UNIX_LINES))
            msg += "UNIX_LINES";
        return msg;
    }

    private boolean has(final int flags, final int f) {
        return (flags & f) > 0;
    }

    public String doReplace(final String pstrSourceString, final String pstrReplacementPattern) throws Exception {
        final String methodName = "SOSOptionRegExp::doReplace";
        String strTargetString = pstrSourceString;
        try {
            strTargetString = replaceGroups(strTargetString, pstrReplacementPattern);
            strTargetString = substituteAllDate(strTargetString);
            strTargetString = substituteAllFilename(strTargetString, pstrSourceString);
            strTargetString = substituteTimeStamp(strTargetString);
            strTargetString = substituteUUID(strTargetString);
            strTargetString = substituteSQLTimeStamp(strTargetString);
            Matcher m = Pattern.compile("\\[[^\\]]*\\]").matcher(strTargetString);
            if (m.find()) {
                throw new JobSchedulerException(String.format("unsupported variable found: ' %1$s'", m.group()));
            }
            return strTargetString;
        } catch (Exception e) {
            throw new JobSchedulerException(methodName + ": " + e.getMessage(), e);
        }
    }

    public String replaceGroups(final String pstrSourceString, final String replacement) throws Exception {
        String result = "";
        if (replacement == null) {
            throw new JobSchedulerException("replacements missing: 0 replacements defined");
        }
        Pattern p = Pattern.compile(strValue, intRegExpFlags);
        Matcher m = p.matcher(pstrSourceString);
        if (!m.find()) {
            return pstrSourceString;
        }
        String[] replacements = replacement.split(";");
        int intGroupCount = m.groupCount();
        if (intGroupCount == 0) {
            result = pstrSourceString.substring(0, m.start()) + replacements[0] + pstrSourceString.substring(m.end());
        } else {
            int index = 0;
            for (int i = 1; i <= intGroupCount; i++) {
                int intStart = m.start(i);
                if (intStart >= 0 && i <= replacements.length) {
                    String strRepl = replacements[i - 1].trim();
                    if (strRepl.length() > 0) {
                        if (strRepl.contains("\\")) {
                            strRepl = strRepl.replaceAll("\\\\-", "");
                            for (int j = 1; j <= intGroupCount; j++) {
                                strRepl = strRepl.replaceAll("\\\\" + j, m.group(j));
                            }
                        }
                        result += pstrSourceString.substring(index, intStart) + strRepl;
                    }
                }
                index = m.end(i);
            }
            result += pstrSourceString.substring(index);
        }
        return result;
    }

    public String doRegExpReplace(final String pstrSourceString, final String pstrReplacementPattern) throws Exception {
        final String methodName = "SOSOptionRegExp::doRegExpReplace";
        String strTargetString = pstrSourceString;
        try {
            Pattern pattern = Pattern.compile(strValue);
            Matcher matcher1 = pattern.matcher(pstrSourceString);
            strTargetString = matcher1.replaceAll(pstrReplacementPattern);
            strTargetString = substituteAllDate(strTargetString);
            strTargetString = substituteAllFilename(strTargetString, pstrSourceString);
            return strTargetString;
        } catch (Exception e) {
            throw new JobSchedulerException(methodName + ": " + e.getMessage(), e);
        }
    }

    public String substituteAllDate(String targetFilename) throws Exception {
        String temp = substituteFirstDate(targetFilename);
        while (!targetFilename.equals(temp)) {
            targetFilename = temp;
            temp = substituteFirstDate(targetFilename);
        }
        return temp;
    }

    private String substituteFirstDate(String targetFilename) throws Exception {
        final String conVarName = "[date:";
        try {
            if (targetFilename.matches("(.*)(\\" + conVarName + ")([^\\]]*)(\\])(.*)")) {
                int posBegin = targetFilename.indexOf(conVarName);
                if (posBegin > -1) {
                    int posEnd = targetFilename.indexOf("]", posBegin + 6);
                    if (posEnd > -1) {
                        String strDateMask = targetFilename.substring(posBegin + 6, posEnd);
                        String strDateTime = JSDataElementDate.getCurrentTimeAsString(strDateMask);
                        String strT = (posBegin > 0 ? targetFilename.substring(0, posBegin) : "") + strDateTime;
                        if (targetFilename.length() > posEnd) {
                            strT += targetFilename.substring(posEnd + 1);
                        }
                        targetFilename = strT;
                    }
                }
            }
            return targetFilename;
        } catch (Exception e) {
            throw new JobSchedulerException("error substituting [date:]: " + e.getMessage(), e);
        }
    }

    public String substituteAllFilename(String targetFilename, final String original) throws Exception {
        String temp = substituteFirstFilename(targetFilename, original);
        while (!targetFilename.equals(temp)) {
            targetFilename = temp;
            temp = substituteFirstFilename(targetFilename, original);
        }
        return temp;
    }

    private String substituteUUID(String strValue) throws Exception {
        Matcher matcher1 = Pattern.compile("\\[uuid:([^\\]]*)\\]", intRegExpFlags).matcher(strValue);
        if (matcher1.find()) {
            if ("".equals(matcher1.group(1))) {
                strValue = strValue.replaceFirst("\\[uuid:\\]", getUUID());
            }
        }
        return strValue;
    }

    public static String getUUID() {
        return UUID.randomUUID().toString();
    }

    private String substituteTimeStamp(String pstrValue) throws Exception {
        Matcher matcher1 = Pattern.compile("\\[timestamp:([^\\]]*)\\]", intRegExpFlags).matcher(pstrValue);
        if (matcher1.find()) {
            if ("".equals(matcher1.group(1))) {
                pstrValue = pstrValue.replaceFirst("\\[timestamp:\\]", getUnixTimeStamp());
            }
        }
        return pstrValue;
    }

    public static String getUnixTimeStamp() {
        return String.valueOf(System.nanoTime());
    }

    private String substituteSQLTimeStamp(String strValue) throws Exception {
        Matcher matcher1 = Pattern.compile("\\[sqltimestamp:([^\\]]*)\\]", intRegExpFlags).matcher(strValue);
        if (matcher1.find()) {
            if ("".equals(matcher1.group(1))) {
                strValue = strValue.replaceFirst("\\[sqltimestamp:\\]", new Timestamp(new Date().getTime()).toString());
            }
        }
        return strValue;
    }

    public static String getSqlTimeStamp() {
        return new Timestamp(new Date().getTime()).toString();
    }

    private String substituteFirstFilename(String targetFilename, final String original) throws Exception {
        Matcher matcher1 = Pattern.compile("\\[filename:([^\\]]*)\\]", intRegExpFlags).matcher(targetFilename);
        if (matcher1.find()) {
            if ("".equals(matcher1.group(1))) {
                targetFilename = targetFilename.replaceFirst("\\[filename:\\]", original);
            } else if ("lowercase".equals(matcher1.group(1))) {
                targetFilename = targetFilename.replaceFirst("\\[filename:lowercase\\]", original.toLowerCase());
            } else if ("uppercase".equals(matcher1.group(1))) {
                targetFilename = targetFilename.replaceFirst("\\[filename:uppercase\\]", original.toUpperCase());
            }
        }
        return targetFilename;
    }

    public Pattern getPattern(final String pstrValue) {
        strValue = pstrValue;
        return this.getPattern();
    }

    public Pattern getPattern() {
        Pattern p = null;
        try {
            p = Pattern.compile(strValue, intRegExpFlags);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        objCurrentPattern = p;
        return p;
    }

    public void addValue(final String pstrValue4Matching) {
        if (lstMatchValues == null) {
            lstMatchValues = new Vector();
        }
        lstMatchValues.add(pstrValue4Matching);
    }

    public boolean match(final String pstrValue4Matching) {
        strMatchValue = "";
        if (objCurrentPattern == null) {
            this.getPattern();
        }
        boolean flgFound = false;
        matcher = objCurrentPattern.matcher(pstrValue4Matching);
        if (matcher.find()) {
            flgFound = true;
            strMatchValue = pstrValue4Matching;
        }
        if (!flgFound && lstMatchValues != null) {
            for (String strValue4Matching : lstMatchValues) {
                Pattern p = Pattern.compile(strValue4Matching, intRegExpFlags);
                matcher = objCurrentPattern.matcher(pstrValue4Matching);
                if (matcher.find()) {
                    flgFound = true;
                    strMatchValue = pstrValue4Matching;
                    break;
                }
            }
        }
        return flgFound;
    }

    public String getGroup(final int pintGroupNo) {
        String strRetVal = null;
        this.match(strMatchValue);
        if (matcher != null) {
            strRetVal = matcher.group(pintGroupNo);
        }
        return strRetVal;
    }

    @Override
    public void Value(final String pstrValue) {
        super.Value(pstrValue);
        if (isNotEmpty(strValue)) {
            try {
                Pattern.compile(strValue);
            } catch (PatternSyntaxException exception) {
                String strT = String.format("The RegExp '%1$s' is invalid", strValue);
                SOSValidationError objVE = new SOSValidationError(strT);
                objVE.setException(new JobSchedulerException(strT, exception));
            }
        }
    }

    @Override
    public void addProposal(final String pstrProposal) {
        if (pstrProposal != null && !pstrProposal.trim().isEmpty()) {
            SOSOptionRegExp.defaultProposals.put(pstrProposal, pstrProposal);
        }
    }

    @Override
    public String[] getAllProposals(String text) {
        String[] proposals = SOSOptionRegExp.defaultProposals.keySet().toArray(new String[0]);
        return proposals;
    }
    
}
