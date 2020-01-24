package com.sos.JSHelper.Options;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionStringWVariables extends SOSOptionElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionStringWVariables.class);
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

    public String getOriginalValue() {
        return strOriginalValue;
    }

    public void doReSubstitution() {
        try {
            String strT = substituteAllDate();
            super.setValue(strT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getValue() {
        String strT = strValue;
        try {
            if (objParentClass != null && objParentClass.gflgSubsituteVariables) {
                strT = substituteAllDate();
            } else {
                strT = substituteAllDate();
            }
            super.setValue(strT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strT;
    }

    @Override
    public void setValue(final String pstrStringValue) {
        strOriginalValue = pstrStringValue;
        super.setValue(pstrStringValue);
    }

}
