package com.sos.JSHelper.DataElements;

public class JSDataElementDateISO extends JSDataElement {

    public JSDataElementDateISO() {
        //
    }

    public JSDataElementDateISO(String pstrValue) {
        super(pstrValue);
    }

    public JSDataElementDateISO(String pstrValue, String pstrDescription) {
        super(pstrValue, pstrDescription);
    }

    public JSDataElementDateISO(String pstrValue, String pstrDescription, int pintSize, int pintPos, String pstrFormatString,
            String pstrColumnHeader, String pstrXMLTagName) {
        super(pstrValue, pstrDescription, pintSize, pintPos, pstrFormatString, pstrColumnHeader, pstrXMLTagName);
    }

    @Override
    public void doInit() {
        super.Description("DateISO");
        super.ColumnHeader("DateISO");
        super.XMLTagName("DateISO");
        super.OmitXMLTag(true);
    }

}