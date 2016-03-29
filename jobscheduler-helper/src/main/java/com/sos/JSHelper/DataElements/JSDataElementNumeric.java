package com.sos.JSHelper.DataElements;

public class JSDataElementNumeric extends JSDataElement {

    private int intDecimal = 0;

    public JSDataElementNumeric() {
    }

    public JSDataElementNumeric(final String pstrValue) {
        super.Value(pstrValue);
    }

    public JSDataElementNumeric(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    public int MinSize() {
        return 0;
    }

    public void MinSize(final int pintMinSize) {

    }

    public int MaxSize() {
        return 0;
    }

    public void MaxSize(final int pintMaxSize) {

    }

    @Override
    public String FormattedValue() {
        final String strFormat = super.FormatString();
        if (isNotEmpty(strFormat)) {
            try {
                final String strFormatted = String.format("%1$" + strFormat, new Integer(this.Value()));
                return strFormatted.trim();
            } catch (final Exception numberFormException) {
                return super.Value();
            }
        } else {
            return super.Value();
        }
    }

    public void Decimal(final int pintDecimal) {
        System.out.println("JSDataElementNumeric.Decimal()" + pintDecimal);
        intDecimal = pintDecimal;
    }

    public int Decimal() {
        return intDecimal;
    }

    public int IntValue() {
        return new Integer(super.Value()).intValue();
    }

    public JSDataElementNumeric Subtract(final JSDataElementNumeric pobjDataElementNumeric) {
        final Integer intValue = new Integer(IntValue() - pobjDataElementNumeric.IntValue());
        this.Value(intValue.toString());
        return this;
    }

    public JSDataElementNumeric Add(final JSDataElementNumeric pobjDataElementNumeric) {
        final Integer intValue = new Integer(IntValue() + pobjDataElementNumeric.IntValue());
        this.Value(intValue.toString());
        return this;
    }

    @Override
    public String SQLValue() {
        if (isEmpty(this.Value())) {
            return "null";
        }
        return this.Value();
    }
    
}