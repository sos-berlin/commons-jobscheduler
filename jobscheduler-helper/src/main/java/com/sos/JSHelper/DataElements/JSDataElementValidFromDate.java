package com.sos.JSHelper.DataElements;

public class JSDataElementValidFromDate extends JSDataElementDateISO {

    JSDataElementValidFromDate() {
        //
    }

    public JSDataElementValidFromDate(final String pPstrValue) {
        super(pPstrValue);
    }

    public JSDataElementValidFromDate(final JSDataElementDate pdteDate) {
        this(pdteDate.getValue());
    }

    public JSDataElementValidFromDate(final String pPstrValue, final String pPstrDescription) {
        super(pPstrValue, pPstrDescription);
    }

    public JSDataElementValidFromDate(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    public String getFormattedValue() {
        String strT = getValue();
        if (getFormatString().equals(JSDateFormat.dfDATE.toPattern()) || getFormatString().equals(JSDateFormat.dfDATE_SHORT.toPattern())) {
            strT = strT.substring(0, 9 + 1);
        }
        this.setValue(strT);
        return this.getValue();
    }

}