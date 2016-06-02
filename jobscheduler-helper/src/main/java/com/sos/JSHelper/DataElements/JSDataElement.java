package com.sos.JSHelper.DataElements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.FormatPatternException;
import com.sos.JSHelper.io.Files.JSXMLFile;

/** @author EQCPN */
public class JSDataElement extends JSToolBox {

    private String strValue = "";
    private String strDefaultValue = "";
    private String strDescription = "";
    private int intSize = 0;
    private String strTitle = "";
    private String strColumnHeader = "";
    private String strXMLTagName = "";
    private boolean flgOmitXMLTag = false;
    private String strFormatString = "";
    private String strFormatPattern = "";
    private boolean flgAllowEmptyValue = true;
    private boolean flgTrimValue = false;
    private boolean flgIsCData = false;
    private boolean flgIsDirty = false;
    public static boolean flgXMLTagOnSingleLine = false;
    public static boolean flgTrimValues = false;
    public static boolean flgOmitEmptyXMLTags = true;
    public static boolean flgExceptionOnFieldTruncation = true;
    public int intPos = 0;

    public JSDataElement() {
        doInit();
    }

    public JSDataElement(final String pstrValue) {
        setValue(pstrValue);
        doInit();
    }

    public JSDataElement(final String pstrValue, final String pstrDescription) {
        this.setValue(pstrValue);
        this.description(pstrDescription);
        doInit();
    }

    public JSDataElement(final String pstrValue, final String pstrDescription, final int pintSize, final int pintPos, final String pstrFormatString,
            final String pstrColumnHeader, final String pstrXMLTagName) {
        doInit();
        this.setValue(pstrValue);
        this.description(pstrDescription);
        this.size(pintSize);
        intPos = pintPos;
        this.setFormatString(pstrFormatString.trim());
        this.description(pstrDescription);
        if (pstrColumnHeader.length() <= 0) {
            this.columnHeader(pstrDescription);
        } else {
            this.columnHeader(pstrColumnHeader);
        }
        if (pstrXMLTagName.length() <= 0) {
            this.xmlTagName(pstrDescription);
        } else {
            this.xmlTagName(pstrXMLTagName);
        }
    }

    public void setDirty(final boolean pflgIsDirty) {
        flgIsDirty = pflgIsDirty;
    }

    public boolean isDirty() {
        return flgIsDirty;
    }

    public void setCData(final boolean pflgIsCData) {
        flgIsCData = pflgIsCData;
    }

    public boolean isCData() {
        return flgIsCData;
    }

    @Override
    public String toString() {
        return strDescription + ": " + strValue;
    }

    public JSDataElement columnHeader(final String pstrColumnHeader) {
        if (pstrColumnHeader != null) {
            strColumnHeader = pstrColumnHeader;
        }
        return this;
    }

    public String getColumnHeader() {
        if (strColumnHeader == null) {
            strColumnHeader = "";
        }
        return strColumnHeader;
    }

    public JSDataElement title(final String pstrTitle) {
        if (pstrTitle != null) {
            strTitle = pstrTitle;
        }
        return this;
    }

    public String getTitle() {
        if (strTitle == null) {
            strTitle = "";
        }
        return strTitle;
    }

    public void setValue(final JSDataElement objDE) {
        setValue(objDE.getValue());
    }

    public void setValue(final String pstrValue) {
        if (pstrValue != null) {
            if (!strValue.equals(pstrValue)) {
                setDirty(true);
                strValue = pstrValue;
                if (flgTrimValue || flgTrimValues) {
                    strValue = strValue.trim();
                }
            }
        } else {
            strValue = "";
        }

    }

    public String getValue() {
        if (strValue == null) {
            strValue = "";
        }
        return strValue;
    }

    public JSDataElement defaultValue(final String pstrDefaultValue) {
        strDefaultValue = pstrDefaultValue;
        return this;
    }

    public String getDefaultValue() {
        return strDefaultValue;
    }

    public JSDataElement description(final String pstrDescription) {
        if (pstrDescription != null) {
            strDescription = pstrDescription;
        }
        return this;
    }

    public String getDescription() {
        if (strDescription == null) {
            strDescription = "";
        }
        return strDescription;
    }

    public JSDataElement size(final int pintSize) {
        intSize = pintSize;
        return this;
    }

    public JSDataElement size(final Integer pintSize) {
        intSize = pintSize;
        return this;
    }

    public int getIntSize() {
        return intSize;
    }

    public Integer getIntegerSize() {
        return new Integer(intSize);
    }

    public JSDataElement xmlTagName(final String pstrXMLTagName) {
        if (pstrXMLTagName != null) {
            strXMLTagName = pstrXMLTagName;
        }
        return this;
    }

    public String getXMLTagName() {
        if (strXMLTagName == null) {
            strXMLTagName = "";
        }
        return strXMLTagName;
    }

    public String toXml(final String pstrTagName) {
        if (isOmittXMLTag()) {
            if (this instanceof JSDataElementDouble) {
                final JSDataElementDouble objT = (JSDataElementDouble) this;
                if (objT.dblValue == 0.0) {
                    return "";
                }
            }
            if (this instanceof JSDataElementDate) {
                final JSDataElementDate objD = (JSDataElementDate) this;
                if (objD.isEmpty()) {
                    return "";
                }
            }
            if (getFormattedValue().isEmpty()) {
                return "";
            }
        }
        String strT = "";
        if (!this.getValue().isEmpty()) {
            strT = "<" + pstrTagName + ">";
            if (this.isCData()) {
                strT += "<![CDATA[" + getFormattedValue() + "]]>";
            } else {
                strT += getFormattedValue();
            }
            strT += "</" + pstrTagName + ">";
        } else {
            strT = "<" + pstrTagName + "/>";
        }
        if (flgXMLTagOnSingleLine) {
            strT += "\n";
        }
        return strT;
    }

    public String toXml() {
        return this.toXml(this.getXMLTagName());
    }

    public void toXml(final JSXMLFile pobjXMLFile) throws Exception {
        pobjXMLFile.writeLine(this.toXml());
    }

    public void setFormatString(final String pstrFormatString) {
        if (!isEmpty(pstrFormatString)) {
            strFormatString = pstrFormatString;
        } else {
            if (isEmpty(strFormatString)) {
                strFormatString = pstrFormatString;
            }
        }
    }

    public void setFormatString(final JSDateFormat pJSDateFormat) {
        this.setFormatString(pJSDateFormat.toPattern());
    }

    public String getFormatString() {
        if (strFormatString == null) {
            strFormatString = "";
        }
        return strFormatString;
    }

    public void doInit() {
        //
    }

    public String getFormattedValue() {
        return this.getValue();
    }

    public JSDataElement omitXMLTag(final boolean pflgOmitXMLTag) {
        flgOmitXMLTag = pflgOmitXMLTag;
        return this;
    }

    public boolean isOmittXMLTag() {
        return flgOmitXMLTag;
    }

    public boolean isEmpty() {
        return this.getValue().trim().isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public boolean hasValue() {
        return !this.getValue().trim().isEmpty();
    }

    public void setTrimValue(final boolean pflgTrimValue) {
        flgTrimValue = pflgTrimValue;
    }

    public void setRecordValue(final StringBuffer pstrRecord) {
        this.setValue(pstrRecord.substring(intPos, intPos + intSize).trim());
    }

    public void buildRecord(final StringBuffer pstrRecord) throws Exception {
        String strVal = this.getValue();
        if (strVal.length() > intSize || !this.getFormatString().isEmpty()) {
            strVal = getFormattedValue();
            if (strVal.length() > intSize && flgExceptionOnFieldTruncation) {
                throw new Exception("Value truncated. max " + intSize + ", actual " + this.getValue().length() + ". Description:"
                        + this.getDescription() + ". Value = '" + this.getValue() + "', " + getFormattedValue());
            }
        }
        doReplace(pstrRecord, intPos, intPos + intSize, strVal);
    }

    protected StringBuffer doReplace(final StringBuffer pstrS, final int pintPos, final int pintPos2, final String pstrR) {
        final int intlSize = pintPos2 - pintPos;
        final int intLen = pstrS.length();
        if (intLen < pintPos2) {
            pstrS.append(repeatString(" ", pintPos2 - intLen + 1));
        }
        String strT = pstrR + repeatString(" ", intlSize);
        strT = strT.substring(0, intlSize);
        pstrS.replace(pintPos, pintPos2, strT);
        return pstrS;
    }

    public void setFormatPattern(final String pstrFormatPattern) {
        if (pstrFormatPattern != null) {
            strFormatPattern = pstrFormatPattern;
        }
    }

    public void allowEmptyValue(final boolean pflgAllowEmptyValue) {
        flgAllowEmptyValue = pflgAllowEmptyValue;
    }

    public void checkFormatPattern() throws FormatPatternException {
        if (isNotEmpty(strFormatPattern) && (!flgAllowEmptyValue || !"".equals(this.getValue().trim()))) {
            final Pattern objP = Pattern.compile(strFormatPattern);
            final Matcher objM = objP.matcher(this.getValue());
            if (!objM.find()) {
                throw new FormatPatternException("the value '" + this.getValue() + "' does not correspond with the pattern " + strFormatPattern);
            }
        }
    }

    public boolean isEqual(final JSDataElement pobjO) {
        boolean flgC = false;
        if (pobjO != null) {
            flgC = this.getValue().equalsIgnoreCase(pobjO.getValue());
        }
        return flgC;
    }

    public String getSQLValue() {
        String strT = this.getValue();
        strT = strT.replace("'", "''");
        return "'" + strT + "'";
    }

    @Override
    public boolean equals(final Object pobjO) {
        if (pobjO instanceof String) {
            final String strT = (String) pobjO;
            return this.getValue().equals(strT);
        }
        if (pobjO instanceof JSDataElement) {
            return this.getValue().equals(((JSDataElement) pobjO).getValue());
        }
        return pobjO.equals(this);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

}