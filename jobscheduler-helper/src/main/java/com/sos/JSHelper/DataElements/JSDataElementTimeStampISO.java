package com.sos.JSHelper.DataElements;

import java.text.ParseException;

import com.sos.JSHelper.Exceptions.FormatPatternException;

public class JSDataElementTimeStampISO extends JSDataElementDate {

    private static final String CLASSNAME = "JSDataElementTimeStampISO";

    JSDataElementTimeStampISO() {
        init();
    }

    public JSDataElementTimeStampISO(final String pPstrValue) {
        super(pPstrValue);
        init();
    }

    public JSDataElementTimeStampISO(final JSDataElementDate pdteDate) {
        this(pdteDate.getValue());
    }

    public JSDataElementTimeStampISO(final String pPstrValue, final String pPstrDescription) {
        super(pPstrValue, pPstrDescription);
        init();
    }

    public JSDataElementTimeStampISO(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    private void init() {
        setParsePattern(JSDateFormat.dfTIMESTAMPS);
        setFormatPattern(JSDateFormat.dfISO);
    }

    @Override
    public String getFormattedValue() {
        try {
            this.setValue(getValueISO());
        } catch (final Exception objException) {
            //
        }
        return this.getValue();
    }

    public String getValueISO() throws Exception {
        final String conMethodName = CLASSNAME + "::Value";
        setParsePattern(JSDateFormat.dfTIMESTAMPS);
        setFormatPattern(JSDateFormat.dfISO);
        try {
            getParsePattern().parse(this.getValue());
        } catch (final ParseException e) {
            throw new FormatPatternException(conMethodName + ": the value '" + this.getValue() + "' does not correspond with the pattern "
                    + getParsePattern().toPattern());
        }
        final JSDateFormat df = new JSDateFormat(JSDateFormat.dfISO.toPattern());
        return df.format(getDateObject());
    }

}