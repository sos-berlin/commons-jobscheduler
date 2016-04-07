package com.sos.JSHelper.Options;

import java.util.Vector;

public class JSOptionValueList extends SOSOptionString {

    public static final String conValueListDelimiters = "[;,|]";
    private static final long serialVersionUID = -402205746280480952L;
    protected String[] strValueList;
    public final String ControlType = "combo";

    @Override
    public String getControlType() {
        return ControlType;
    }

    public JSOptionValueList(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        valueList(pPstrValue.split(conValueListDelimiters));
    }

    public JSOptionValueList(final JSOptionsClass pobjParent, final String pstrIndexedKey, final String pstrDescription,
            final String pstrDefaultValue, final boolean pflgIsMandatory) {
        super(pobjParent, pstrIndexedKey, pstrDescription, null, pstrDefaultValue, pflgIsMandatory);
        if (isNotEmpty(pstrIndexedKey)) {
            IndexedKey(pstrIndexedKey);
        }
    }

    private void IndexedKey(final String pstrIndexedKey) {
        String strT;
        final Vector<String> objValueList = new Vector<String>();
        final StringBuffer sb = new StringBuffer();
        strT = objParentClass.getItem(pstrIndexedKey);
        if (strT != null) {
            objValueList.addElement(strT);
        }
        int i = 1;
        while ((strT = objParentClass.getItem(pstrIndexedKey + Integer.toString(i++))) != null) {
            objValueList.addElement(strT);
            if (i > 2) {
                sb.append(";");
            }
            sb.append(strT);
        }
        strValueList = objValueList.toArray(new String[0]);
    }

    public String ElementAt(final int pintIdx) {
        if (strValueList == null) {
            return "";
        }
        if (pintIdx >= 0 && pintIdx <= strValueList.length) {
            return strValueList[pintIdx];
        }
        return "";
    }

    @Override
    public void Value(final String pstrValueList) {
        String strT = pstrValueList;
        if (isNotEmpty(pstrValueList) && (pstrValueList.contains(";") || pstrValueList.contains("|") || pstrValueList.contains(","))) {
            strValueList = strT.split(conValueListDelimiters);
            strT = strValueList[0];
        } else if (isNotEmpty(pstrValueList)
                && (isNull(strValueList) || strValueList.length == 0 || (strValueList.length == 1 && isEmpty(strValueList[0])))) {
            strValueList = new String[] { pstrValueList };
        }
        super.Value(strT);
    }

    public void AppendValue(final String pstrValueList) throws Exception {
        if (isNotEmpty(pstrValueList)) {
            if (isNotEmpty(super.Value())) {
                strValue += ";" + pstrValueList;
                String[] strarrT = pstrValueList.split(conValueListDelimiters);
                int intLengthT = strarrT.length;
                int intLengthActual = strValueList.length;
                String[] strNew = new String[intLengthT + intLengthActual];
                System.arraycopy(strValueList, 0, strNew, 0, intLengthActual);
                System.arraycopy(strarrT, 0, strNew, intLengthActual, intLengthT);
                strValueList = strNew;
            } else {
                super.Value(pstrValueList);
                strValueList = pstrValueList.split(conValueListDelimiters);
            }
        }
    }

    public void valueList(final String[] pstrValueArray) {
        strValueList = pstrValueArray;
    }

    public String[] valueList() {
        if (strValueList == null) {
            strValueList = new String[] { "" };
        }
        return strValueList;
    }

    public String concatenatedValue(final String pstrDelimiter) {
        final StringBuilder strB = new StringBuilder();
        String strT = "";
        if (strValueList == null) {
            return strT;
        }
        for (int i = 0; i < strValueList.length; i++) {
            strT = strValueList[i];
            if (i > 0 && !strValueList[i - 1].trim().endsWith(pstrDelimiter)) {
                strB.append(pstrDelimiter);
            }
            strB.append(strT);
        }
        return strB.toString();
    }

    public boolean contains(final String pstrValue2Find) {
        final boolean flgFound = false;
        if (strValueList == null) {
            return flgFound;
        }
        for (final String element : strValueList) {
            if (pstrValue2Find.equalsIgnoreCase(element)) {
                return true;
            }
        }
        return flgFound;
    }

    public int IndexOf(final String pstrValue2Find) {
        for (int i = 0; i < strValueList.length; i++) {
            if (pstrValue2Find.equalsIgnoreCase(strValueList[i])) {
                return i;
            }
        }
        return -1;
    }

    public String[] ValueList() {
        return strValueList;
    }

}