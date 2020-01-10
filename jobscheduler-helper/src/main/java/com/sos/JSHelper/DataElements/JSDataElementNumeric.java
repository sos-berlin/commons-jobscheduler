package com.sos.JSHelper.DataElements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSDataElementNumeric extends JSDataElement {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSDataElementNumeric.class);
    private int intDecimal = 0;

    public JSDataElementNumeric() {
        //
    }

    public JSDataElementNumeric(final String pstrValue) {
        super.setValue(pstrValue);
    }

    public JSDataElementNumeric(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    public int getMinSize() {
        return 0;
    }

    public void setMinSize(final int pintMinSize) {
        //
    }

    public int getMaxSize() {
        return 0;
    }

    public void setMaxSize(final int pintMaxSize) {
        //
    }

    @Override
    public String getFormattedValue() {
        final String strFormat = super.getFormatString();
        if (isNotEmpty(strFormat)) {
            try {
                final String strFormatted = String.format("%1$" + strFormat, new Integer(this.getValue()));
                return strFormatted.trim();
            } catch (final Exception numberFormException) {
                return super.getValue();
            }
        } else {
            return super.getValue();
        }
    }

    public void setDecimal(final int pintDecimal) {
        LOGGER.info("JSDataElementNumeric.Decimal()" + pintDecimal);
        intDecimal = pintDecimal;
    }

    public int getDecimal() {
        return intDecimal;
    }

    public int getIntegerValue() {
        return new Integer(super.getValue()).intValue();
    }

    public JSDataElementNumeric subtract(final JSDataElementNumeric pobjDataElementNumeric) {
        final Integer intValue = new Integer(getIntegerValue() - pobjDataElementNumeric.getIntegerValue());
        this.setValue(intValue.toString());
        return this;
    }

    public JSDataElementNumeric add(final JSDataElementNumeric pobjDataElementNumeric) {
        final Integer intValue = new Integer(getIntegerValue() + pobjDataElementNumeric.getIntegerValue());
        this.setValue(intValue.toString());
        return this;
    }

    @Override
    public String getSQLValue() {
        if (isEmpty(this.getValue())) {
            return "null";
        }
        return this.getValue();
    }

}