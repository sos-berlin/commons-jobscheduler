package com.sos.JSHelper.DataElements;

import java.text.ParseException;

import com.sos.JSHelper.Exceptions.FormatPatternException;

public class JSDataElementTimeStampISO extends JSDataElementDate {

    private static final String CLASSNAME = "JSDataElementTimeStampISO";

    JSDataElementTimeStampISO() {
        init();
    }

    private void init() {
        setParsePattern(JSDateFormat.dfTIMESTAMPS);
        setFormatPattern(JSDateFormat.dfISO);
    }

    public JSDataElementTimeStampISO(final String pPstrValue) {
        super(pPstrValue);
        init();
    }

    public JSDataElementTimeStampISO(final JSDataElementDate pdteDate) {
        this(pdteDate.Value());
    }

    public JSDataElementTimeStampISO(final String pPstrValue, final String pPstrDescription) {
        super(pPstrValue, pPstrDescription);
        init();
    }

    public JSDataElementTimeStampISO(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    @Override
    public String FormattedValue() {
        try {
            this.Value(ValueISO());
        } catch (final Exception objException) {
            //
        }
        return this.Value();
    }

    public String ValueISO() throws Exception {
        final String methodName = CLASSNAME + "::Value";
        setParsePattern(JSDateFormat.dfTIMESTAMPS);
        setFormatPattern(JSDateFormat.dfISO);
        try {
            getParsePattern().parse(this.Value());
        } catch (final ParseException e) {
            throw new FormatPatternException(methodName + ": the value '" + this.Value() + "' does not correspond with the pattern "
                    + getParsePattern().toPattern());
        }
        final JSDateFormat df = new JSDateFormat(JSDateFormat.dfISO.toPattern());
        return df.format(getDateObject());
    }

}