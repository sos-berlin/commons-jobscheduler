package com.sos.JSHelper.Options;

import java.util.regex.Matcher;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionFileName extends SOSOptionStringWVariables {

    private static final long serialVersionUID = -4059135218882474551L;
    public final String ControlType = "file";
    protected JSFile objFile = null;

    public SOSOptionFileName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        this.getJSFile();
    }

    public SOSOptionFileName(final String pstrFileName) {
        super(null, "", "", pstrFileName, "", false);
    }

    @Override
    public String getControlType() {
        return ControlType;
    }

    public String getValueWithFileSeparator() {
        String strT = strValue.trim();
        if (isNotEmpty() && !(strValue.endsWith("/") || strValue.endsWith("\\"))) {
            strT = strValue + "/";
        }
        return strT;
    }

    public JSFile getJSFile() {
        if (objFile == null && isNotEmpty(strValue)) {
            objFile = new JSFile(strValue);
        }
        return objFile;
    }

    public void setRelativeValue(final String pstrStringValue) {
        strOriginalValue = pstrStringValue;
        String strT = pstrStringValue;
        if (strT.length() > 2 && (strT.startsWith("./") || strT.startsWith(".\\"))) {
            strT = strT.replaceFirst("\\.", Matcher.quoteReplacement(getUserDir()));
        }
        super.setValue(strT);
    }

    public String getRelativeValue() {
        return strOriginalValue;
    }

}