package com.sos.JSHelper.io.Files;


/** @author KB */
public class SOSProfileEntry {

    private String strName;
    private String strValue;

    public SOSProfileEntry() {
        //
    }

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

    public String getName() {
        return strName;
    }

    public String getValue() {
        return strValue;
    }

    public void setName(String string) {
        strName = string;
    }

    public void setValue(String string) {
        strValue = string;
    }

}