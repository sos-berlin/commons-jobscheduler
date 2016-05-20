package com.sos.JSHelper.io.Files;

import java.util.HashMap;
import java.util.Map;

/** @author KB */
public class SOSProfileSection {

    public String strSectionName;
    public Map<String, SOSProfileEntry> mapEntries;

    public SOSProfileSection() {
        //
    }

    public SOSProfileSection(String pstrSectionName) {
        strSectionName = pstrSectionName;
        mapEntries = new HashMap<String, SOSProfileEntry>();
    }

    public SOSProfileEntry addEntry(String pstrEntryName, String pstrEntryValue) {
        SOSProfileEntry objPE = this.getEntry(pstrEntryName);
        if (objPE == null) {
            objPE = new SOSProfileEntry(pstrEntryName, pstrEntryValue);
            Map m = this.getEntries();
            m.put(pstrEntryName.toLowerCase(), objPE);
        } else {
            objPE.setValue(pstrEntryValue);
        }
        return objPE;
    }

    public SOSProfileEntry deleteEntry(String pstrEntryName) {
        SOSProfileEntry objPE = this.getEntry(pstrEntryName);
        if (objPE == null) {
        } else {
            Map m = this.getEntries();
            m.remove(pstrEntryName.toLowerCase());
        }
        return objPE;
    }

    public Map<String, SOSProfileEntry> getEntries() {
        if (mapEntries == null) {
            mapEntries = new HashMap<String, SOSProfileEntry>();
        }

        return mapEntries;
    }

    public SOSProfileEntry getEntry(String pstrKey) {
        return (SOSProfileEntry) mapEntries.get(pstrKey.toLowerCase());
    }

    public String getName() {
        return strSectionName;
    }

    public void setName(String string) {
        strSectionName = string;
    }

    public String toString() {
        return "[" + strSectionName + "]\n";
    }

    public String toXML() {
        return "";
    }

}