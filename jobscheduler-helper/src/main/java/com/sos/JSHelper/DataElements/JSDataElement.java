package com.sos.JSHelper.DataElements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.FormatPatternException;
import com.sos.JSHelper.io.Files.JSXMLFile;

/** @author EQCPN */
public class JSDataElement extends JSToolBox {

    private static final String CLASSNAME = "JSDataElement";
    public static boolean flgXMLTagOnSingleLine = false;
    public static boolean flgTrimValues = false;
    public static boolean flgOmitEmptyXMLTags = true;
    public static boolean flgExceptionOnFieldTruncation = true;
    private String strValue = "";
    private String strDefaultValue = "";
    private String strDescription = "";
    private int intSize = 0;
    public int intPos = 0;
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

    public void isDirty(final boolean pflgIsDirty) {
        flgIsDirty = pflgIsDirty;
    }

    public boolean isDirty() {
        return flgIsDirty;
    }

    public void isCData(final boolean pflgIsCData) {
        flgIsCData = pflgIsCData;
    }

    public boolean isCData() {
        return flgIsCData;
    }

    @Override
    public String toString() {
        return strDescription + ": " + strValue;
    }

    public JSDataElement() {
        doInit();
    }

    public JSDataElement(final String pstrValue) {
        Value(pstrValue);
        doInit();
    }

    public JSDataElement(final String pstrValue, final String pstrDescription) {
        this.Value(pstrValue);
        this.Description(pstrDescription);
        doInit();
    }

    public JSDataElement(final String pstrValue, final String pstrDescription, final int pintSize, final int pintPos,
            final String pstrFormatString, final String pstrColumnHeader, final String pstrXMLTagName) {
        doInit();
        this.Value(pstrValue);
        this.Description(pstrDescription);
        this.Size(pintSize);
        intPos = pintPos;
        this.FormatString(pstrFormatString.trim());
        this.Description(pstrDescription);
        if (pstrColumnHeader.length() <= 0) {
            this.ColumnHeader(pstrDescription);
        } else {
            this.ColumnHeader(pstrColumnHeader);
        }
        if (pstrXMLTagName.length() <= 0) {
            this.XMLTagName(pstrDescription);
        } else {
            this.XMLTagName(pstrXMLTagName);
        }
    }

    public JSDataElement ColumnHeader(final String pstrColumnHeader) {
        if (pstrColumnHeader != null) {
            strColumnHeader = pstrColumnHeader;
        }
        return this;
    }

    public String ColumnHeader() {
        if (strColumnHeader == null) {
            strColumnHeader = "";
        }
        return strColumnHeader;
    }

    public JSDataElement Title(final String pstrTitle) {
        if (pstrTitle != null) {
            strTitle = pstrTitle;
        }
        return this;
    }

    public String Title() {
        if (strTitle == null) {
            strTitle = "";
        }
        return strTitle;
    }

    public void Value(final JSDataElement objDE) {
        Value(objDE.Value());
    }

    public void Value(final String pstrValue) {
        if (pstrValue != null) {
            if (!strValue.equals(pstrValue)) {
                isDirty(true);
                strValue = pstrValue;
                if (flgTrimValue || flgTrimValues) {
                    strValue = strValue.trim();
                }
            }
        } else {
            strValue = "";
        }

    }

    public String Value() {
        if (strValue == null) {
            strValue = "";
        }
        return strValue;
    }

    public JSDataElement DefaultValue(final String pstrDefaultValue) {
        strDefaultValue = pstrDefaultValue;
        return this;
    }

    public String DefaultValue() {
        return strDefaultValue;
    }

    public JSDataElement Description(final String pstrDescription) {
        if (pstrDescription != null) {
            strDescription = pstrDescription;
        }
        return this;
    }

    public String Description() {
        if (strDescription == null) {
            strDescription = "";
        }
        return strDescription;
    }

    public JSDataElement Size(final int pintSize) {
        intSize = pintSize;
        return this;
    }

    public JSDataElement Size(final Integer pintSize) {
        intSize = pintSize;
        return this;
    }

    public int Size() {
        return intSize;
    }

    public Integer ISize() {
        return new Integer(intSize);
    }

    public JSDataElement XMLTagName(final String pstrXMLTagName) {
        if (pstrXMLTagName != null) {
            strXMLTagName = pstrXMLTagName;
        }
        return this;
    }

    public String XMLTagName() {
        if (strXMLTagName == null) {
            strXMLTagName = "";
        }
        return strXMLTagName;
    }

    public String toXml(final String pstrTagName) {
        if (OmittXMLTag() == true) {
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
            if (FormattedValue().length() <= 0) {
                return "";
            }
        }
        String strT = "";
        if (this.Value().length() > 0) {
            strT = "<" + pstrTagName + ">";
            if (this.isCData()) {
                strT += "<![CDATA[" + FormattedValue() + "]]>";
            } else {
                strT += FormattedValue();
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
        return this.toXml(this.XMLTagName());
    }

    public void toXml(final JSXMLFile pobjXMLFile) throws Exception {
        pobjXMLFile.WriteLine(this.toXml());
    }

    public void FormatString(final String pstrFormatString) {
        if (!isEmpty(pstrFormatString)) {
            strFormatString = pstrFormatString;
        } else {
            if (isEmpty(strFormatString)) {
                strFormatString = pstrFormatString;
            }
        }
    }

    public void FormatString(final JSDateFormat pJSDateFormat) {
        this.FormatString(pJSDateFormat.toPattern());
    }

    public String FormatString() {
        if (strFormatString == null) {
            strFormatString = "";
        }
        return strFormatString;
    }

    public void doInit() {
        //
    }

    public String FormattedValue() {
        return this.Value();
    }

    public JSDataElement OmitXMLTag(final boolean pflgOmitXMLTag) {
        flgOmitXMLTag = pflgOmitXMLTag;
        return this;
    }

    public boolean OmittXMLTag() {
        boolean flgF = flgOmitXMLTag;
        if (flgOmitXMLTag) {
            flgF = true;
        }
        return flgF;
    }

    public boolean IsEmpty() {
        return this.Value().trim().length() == 0;
    }

    public boolean IsNotEmpty() {
        return !IsEmpty();
    }

    public boolean hasValue() {
        return this.Value().trim().length() > 0;
    }

    public void TrimValue(final boolean pflgTrimValue) {
        flgTrimValue = pflgTrimValue;
    }

    public void RecordValue(final StringBuffer pstrRecord) {
        this.Value(pstrRecord.substring(intPos, intPos + intSize).trim());
    }

    public void BuildRecord(final StringBuffer pstrRecord) throws Exception {
        String strVal = this.Value();
        if (strVal.length() > intSize || this.FormatString().length() > 0) {
            strVal = FormattedValue();
            if (strVal.length() > intSize && flgExceptionOnFieldTruncation) {
                throw new Exception("Value truncated. max " + intSize + ", actual " + this.Value().length() + ". Description:" + this.Description()
                        + ". Value = '" + this.Value() + "', " + FormattedValue());
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
        if (isNotEmpty(strFormatPattern)) {
            if (!flgAllowEmptyValue || !"".equals(this.Value().trim())) {
                final Pattern objP = Pattern.compile(strFormatPattern);
                final Matcher objM = objP.matcher(this.Value());
                if (!objM.find()) {
                    throw new FormatPatternException("the value '" + this.Value() + "' does not correspond with the pattern " + strFormatPattern);
                }
            }
        }
    }

    public boolean isEqual(final JSDataElement pobjO) {
        boolean flgC = false;
        if (pobjO != null) {
            flgC = this.Value().equalsIgnoreCase(pobjO.Value());
        }
        return flgC;
    }

    public String SQLValue() {
        String strT = this.Value();
        strT = strT.replace("'", "''");
        return "'" + strT + "'";
    }

    @Override
    public boolean equals(final Object pobjO) {
        if (pobjO instanceof String) {
            final String strT = (String) pobjO;
            return this.Value().equals(strT);
        }
        if (pobjO instanceof JSDataElement) {
            return this.Value().equals(((JSDataElement) pobjO).Value());
        }
        return pobjO.equals(this);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
