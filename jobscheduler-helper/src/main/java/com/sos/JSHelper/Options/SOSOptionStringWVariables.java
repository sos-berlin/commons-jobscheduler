package com.sos.JSHelper.Options;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionStringWVariables extends SOSOptionElement {

    private static final Logger LOGGER = Logger.getLogger(SOSOptionStringWVariables.class);
    private static final long serialVersionUID = 3890065543134955852L;
    protected String strOriginalValue = "";

    public SOSOptionStringWVariables(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        intOptionType = isOptionTypeString;
    }

    public final String substituteAllDate() throws Exception {
        String temp = strValue;
        if (strValue.indexOf("[") > -1) {
            String targetFileName = strValue;
            temp = substituteDateMask(targetFileName);
            while (!targetFileName.equals(temp)) {
                targetFileName = temp;
                temp = substituteDateMask(targetFileName);
            }
        }
        return temp;
    }

    private String substituteDateMask(String targetFilename) throws Exception {
        final String conVarName = "[date:";
        try {
            if (targetFilename.matches("(.*)(\\" + conVarName + ")([^\\]]*)(\\])(.*)")) {
                int posBegin = targetFilename.indexOf(conVarName);
                if (posBegin > -1) {
                    int posEnd = targetFilename.indexOf("]", posBegin + 6);
                    if (posEnd > -1) {
                        String strDateMask = targetFilename.substring(posBegin + 6, posEnd);
                        if (strDateMask.isEmpty()) {
                            strDateMask = SOSOptionTime.dateTimeFormat;
                        }
                        String strDateTime = SOSOptionTime.getCurrentTimeAsString(strDateMask);
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

    public String OriginalValue() {
        return strOriginalValue;
    }

    public void doReSubstitution() {
        try {
            String strT = substituteAllDate();
            super.Value(strT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String Value() {
        String strT = strValue;
        try {
            if (objParentClass != null && objParentClass.gflgSubsituteVariables) {
                strT = substituteAllDate();
            } else {
                strT = substituteAllDate();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strT;
    }

    @Override
    public void Value(final String pstrStringValue) {
        strOriginalValue = pstrStringValue;
        super.Value(pstrStringValue);
    }

}
