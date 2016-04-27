package com.sos.JSHelper.DataElements;

public class JSDataElementInteger extends JSDataElementNumeric {

    private int intValue = 0;

    public JSDataElementInteger() {
        super.TrimValue(true);
    }

    public JSDataElementInteger(final String pstrValue) {
        super.Value(pstrValue);
        super.TrimValue(true);
        getInt();
    }

    public JSDataElementInteger(final int pintValue) {
        intValue = pintValue;
        super.TrimValue(true);
        this.Value(pintValue);
    }

    public JSDataElementInteger(final String pPstrValue, final String pPstrDescription, final int pPintSize, final int pPintPos,
            final String pPstrFormatString, final String pPstrColumnHeader, final String pPstrXMLTagName) {
        super(pPstrValue, pPstrDescription, pPintSize, pPintPos, pPstrFormatString, pPstrColumnHeader, pPstrXMLTagName);
    }

    @Override
    public void Value(final String pstrValue) {
        super.Value(pstrValue);
        getInt();
    }

    public void Value(final int pintValue) {
        intValue = pintValue;
        super.Value(new Integer(pintValue).toString());
    }

    public int getInt() {
        try {
            intValue = 0;
            String strT = super.Value().trim();
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
    public String FormattedValue() {
        final String strFormat = super.FormatString();
        if (isNotEmpty(strFormat.trim())) {
            String strFormatted = String.format("%1$" + strFormat, getInt());
            strFormatted = strFormatted.trim();
            if (super.MaxSize() != 0 && strFormatted.length() > super.MaxSize()) {
                strFormatted = "";
                for (int i = 0; i < super.MaxSize(); i++) {
                    strFormatted += "*";
                }
            }
            return strFormatted;
        } else {
            return super.Value();
        }
    }

    @Override
    public void doInit() {
        super.MaxSize(15);
        super.FormatString("-,15d");
        super.Description("Integer");
        super.ColumnHeader("Integer");
        super.XMLTagName("Integer");
    }

}