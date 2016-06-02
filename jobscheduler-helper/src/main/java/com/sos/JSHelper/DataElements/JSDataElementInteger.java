package com.sos.JSHelper.DataElements;

public class JSDataElementInteger extends JSDataElementNumeric {

    private int intValue = 0;

    public JSDataElementInteger() {
        super.setTrimValue(true);
    }

    public JSDataElementInteger(final String pstrValue) {
        super.setValue(pstrValue);
        super.setTrimValue(true);
        getInt();
    }

    public JSDataElementInteger(final int pintValue) {
        intValue = pintValue;
        super.setTrimValue(true);
        this.setValue(pintValue);
    }

    public JSDataElementInteger(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    @Override
    public void setValue(final String pstrValue) {
        super.setValue(pstrValue);
        getInt();
    }

    public void setValue(final int pintValue) {
        intValue = pintValue;
        super.setValue(new Integer(pintValue).toString());
    }

    public int getInt() {
        try {
            intValue = 0;
            String strT = super.getValue().trim();
            final int intLen = strT.length();
            if (intLen > 0) {
                if (strT.endsWith("-")) {
                    strT = "-" + strT.substring(0, intLen - 1);
                } else {
                    if (strT.startsWith("+")) {
                        strT = strT.substring(1, intLen);
                    } else {
                        if (strT.endsWith("+")) {
                            strT = strT.substring(0, intLen - 1);
                        }
                    }
                }
                intValue = Integer.parseInt(strT);
            }
        } catch (final NumberFormatException e) {
            intValue = 0;
        }
        return intValue;
    }

    @Override
    public String getFormattedValue() {
        final String strFormat = super.getFormatString();
        if (isNotEmpty(strFormat.trim())) {
            String strFormatted = String.format("%1$" + strFormat, getInt());
            strFormatted = strFormatted.trim();
            if (super.getMaxSize() != 0 && strFormatted.length() > super.getMaxSize()) {
                strFormatted = "";
                for (int i = 0; i < super.getMaxSize(); i++) {
                    strFormatted += "*";
                }
            }
            return strFormatted;
        } else {
            return super.getValue();
        }
    }

    @Override
    public void doInit() {
        super.setMaxSize(15);
        super.setFormatString("-,15d");
        super.description("Integer");
        super.columnHeader("Integer");
        super.xmlTagName("Integer");
    }

}