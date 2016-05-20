package com.sos.JSHelper.DataElements;

public class JSDataElementDouble extends JSDataElementNumeric {

    double dblValue = 0;

    public JSDataElementDouble() {
        super();
    }

    public JSDataElementDouble(String pstrValue) {
        super.setValue(pstrValue);
        dblValue = getDouble();
    }

    public JSDataElementDouble(String pPstrValue, String pstrDescription) {
        this(pPstrValue);
        this.description(pstrDescription);
    }

    public JSDataElementDouble(String pPstrValue, String pPstrDescription, int pPintSize, int pPintPos, String pPstrFormatString,
            String pPstrColumnHeader, String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    public JSDataElementDouble(double pdblValue) {
        this.Value(pdblValue);
        dblValue = pdblValue;
    }

    public void Value(double pdblValue) {
        super.setValue(new Double(pdblValue).toString());
        dblValue = pdblValue;
    }

    public double getDouble() {
        return dblValue;
    }

    @Override
    public void doInit() {
        super.setFormatString("15.3f");
        super.description("Double");
        super.columnHeader("Double");
        super.xmlTagName("Double");
    }

    protected double toDouble() throws Exception {
        dblValue = toDouble(this.getValue());
        return dblValue;
    }

    @Override
    public String getFormattedValue() {
        String strFormat = super.getFormatString();
        if (isNotEmpty(strFormat)) {
            String strFormatted = String.format("%1$" + strFormat, dblValue);
            strFormatted = strFormatted.replace(",", ".");
            return strFormatted.trim();
        }
        return super.getValue();
    }

    @Override
    public void setValue(final String pstrValue) {
        try {
            dblValue = toDouble(pstrValue);
            super.setValue(new Double(dblValue).toString());
        } catch (Exception objException) {
            // TO DO: handle exception
        }
    }

}