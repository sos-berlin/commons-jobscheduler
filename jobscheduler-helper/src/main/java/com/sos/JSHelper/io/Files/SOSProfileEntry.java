package com.sos.JSHelper.io.Files;

/** @author KB */
public class SOSProfileEntry {

    private String strName;
    private String strValue;

    public SOSProfileEntry(String pstrEntryName) {
        this(pstrEntryName, "");
    }

    public SOSProfileEntry(String pstrEntryName, String pstrEntryValue) {
        strName = pstrEntryName;
        strValue = pstrEntryValue;
    }

    public String toString() {
        return (strName + "=" + strValue + "\n");
    }

    public String toXML() {
        String strT = "";
        strT = strT.concat("<" + strName + ">\n");
        strT = strT.concat(strValue + "\n");
        return strT.concat("</" + strName + ">\n");
    }

    public String Name() {
        return strName;
    }

    public String Value() {
        return strValue;
    }

    public void Name(String string) {
        strName = string;
    }

    public void Value(String string) {
        strValue = string;
    }

}