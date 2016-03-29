package com.sos.JSHelper.DataElements;

public class JSDataElementDouble extends JSDataElementNumeric {

    double dblValue = 0;

    public JSDataElementDouble() {
        super();
    }

    public JSDataElementDouble(String pstrValue) {
        super.Value(pstrValue);
        dblValue = getDouble();
    }

    public JSDataElementDouble(String pPstrValue, String pstrDescription) {
        this(pPstrValue);
        this.Description(pstrDescription);
    }

    public JSDataElementDouble(String pPstrValue, String pPstrDescription, int pPintSize, int pPintPos, String pPstrFormatString, String pPstrColumnHeader,
            String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    public JSDataElementDouble(double pdblValue) {
        this.Value(pdblValue);
        dblValue = pdblValue;
    }

    public void Value(double pdblValue) {
        super.Value(new Double(pdblValue).toString());
        dblValue = pdblValue;
    }

    public double getDouble() {
        return dblValue;
    }

    @Override
    public void doInit() {
        super.FormatString("15.3f");
        super.Description("Double");
        super.ColumnHeader("Double");
        super.XMLTagName("Double");
    }

    protected double toDouble() throws Exception {
        dblValue = toDouble(this.Value());
        return dblValue;
    }

    @Override
    public String FormattedValue() {
        String strFormat = super.FormatString();
        if (isNotEmpty(strFormat)) {
            String strFormatted = String.format("%1$" + strFormat, dblValue);
            strFormatted = strFormatted.replace(",", ".");
            return strFormatted.trim();
        }
        return super.Value();
    }

    @Override
    public void Value(final String pstrValue) {
        try {
            dblValue = toDouble(pstrValue);
            super.Value(new Double(dblValue).toString());
        } catch (Exception objException) {
            // TO DO: handle exception
        }
    }
    
}