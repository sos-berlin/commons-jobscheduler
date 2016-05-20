package com.sos.JSHelper.io.Files;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
@JSOptionClass(name = "JSOptionsClass", description = "JSOptionsClass")
public class JSIniFile extends JSTextFile {

    private static final long serialVersionUID = -1960627326921434568L;
    private static final Logger LOGGER = Logger.getLogger(JSIniFile.class);
    private final Map<String, SOSProfileSection> mapSections = new HashMap<String, SOSProfileSection>();
    private String strSectionName = null;
    private boolean flgIgnoreDuplicateSections = true;
    private boolean flgIsDirty = false;
    private SOSProfileSection objCurrentSection = null;
    private JSTextFile objSaveFile = null;

    public JSIniFile(String pstrFileName) {
        super(pstrFileName);
        if (fleFile.exists()) {
            getIniFile();
        }
    }

    private void getIniFile() {
        String line = null;
        try {
            StringBuffer strB = new StringBuffer();
            while ((strB = this.getLine()) != null) {
                line = strB.toString().trim();
                LOGGER.debug(line);
                int intLineLength = line.length();
                if (intLineLength <= 0 || line.startsWith(";") || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("[")) {
                    if (!line.endsWith("]")) {
                        throw new JobSchedulerException("] expected in section header: " + line);
                    }
                    String strSectionName = line.substring(1, intLineLength - 1).trim();
                    if (!strSectionName.isEmpty()) {
                        this.objCurrentSection = addSection(strSectionName);
                    }
                } else {
                    if (this.objCurrentSection == null) {
                        throw new IOException("[sectionname]-header expected");
                    } else {
                        final int index = line.indexOf('=');
                        if (index < 0) {
                            throw new JobSchedulerException("key/value pair without =  : " + line);
                        }
                        final String strKey = line.substring(0, index).trim();
                        final String strValue = line.substring(index + 1).trim();
                        addEntry(strKey, strValue);
                    }
                }
            }
            this.close();
            flgIsDirty = false;
        } catch (final Exception e) {
            throw new JobSchedulerException(e.toString() + "\n\nFile name is: " + strFileName);
        }
    }

    public SOSProfileEntry addEntry(final String strKey, final String strValue) {
        SOSProfileEntry objE = null;
        if (this.objCurrentSection != null) {
            objE = this.objCurrentSection.addEntry(strKey, strValue);
        }
        return objE;
    }

    public SOSProfileSection addSection(final String pstrSectionName) {
        SOSProfileSection objNewSection = (SOSProfileSection) this.mapSections.get(pstrSectionName);
        if (objNewSection == null) {
            objNewSection = new SOSProfileSection(pstrSectionName);
            this.mapSections.put(pstrSectionName.toLowerCase(), objNewSection);
            flgIsDirty = true;
        } else {
            if (!this.flgIgnoreDuplicateSections) {
                throw new JobSchedulerException("Section '" + pstrSectionName + "' duplicated.");
            }
        }
        return objNewSection;
    }

    public SOSProfileSection getSection(final String pstrSectionName) {
        SOSProfileSection objNewSection = (SOSProfileSection) this.mapSections.get(pstrSectionName);
        return objNewSection;
    }

    public boolean isDirty() {
        return this.flgIsDirty;
    }

    public Map<String, SOSProfileSection> getSections() {
        return this.mapSections;
    }

    public String getValue(final String pstrEntryName) throws IOException {
        final String strDefaultValue = null;
        return this.getPropertyString(this.strSectionName, pstrEntryName, strDefaultValue);
    }

    public String getValue(final String pstrEntryName, final String pstrDefaultValue) throws IOException {
        return this.getPropertyString(this.strSectionName, pstrEntryName, pstrDefaultValue);
    }

    public void setValue(final String pstrEntryName, final String pstrEntryValue) {
        //
    }

    public void setSectionName(final String pstrSectionName) {
        this.strSectionName = pstrSectionName;
    }

    public String getSectionName() {
        return this.strSectionName;
    }

    public String getPropertyString(final String pstrSection, final String key, final String defaultValue) {
        if (pstrSection == null) {
            throw new JobSchedulerException("[section] name missing");
        }
        final SOSProfileSection map = (SOSProfileSection) this.mapSections.get(pstrSection.toLowerCase());
        if (map != null) {
            final SOSProfileEntry objPE = map.getEntry(key.toLowerCase());
            if (objPE != null) {
                return objPE.getValue();
            }
        }
        return defaultValue;
    }

    public int getPropertyInt(final String section, final String key, final int defaultValue) {
        final String s = this.getPropertyString(section, key, null);
        if (s != null) {
            return Integer.parseInt(s);
        }
        return defaultValue;
    }

    public boolean getPropertyBool(final String pstrSection, final String key, final boolean defaultValue) {
        final String s = this.getPropertyString(pstrSection, key, null);
        if (s != null) {
            return "true".equalsIgnoreCase(s);
        }
        return defaultValue;
    }

    public String getProfileName() {
        return this.strFileName;
    }

    public void setProfileName(final String pstrProfileName) {
        this.strFileName = pstrProfileName;
    }

    @Override
    public String toString() {
        String strT = "";
        int j = 0;
        for (final Iterator i = this.getSections().entrySet().iterator(); i.hasNext();) {
            final Map.Entry e = (Map.Entry) i.next();
            LOGGER.debug(j++ + ": " + e.getKey());
            final SOSProfileSection objPS = (SOSProfileSection) e.getValue();
            LOGGER.debug(objPS.getName() + " - " + objPS.getEntries().size());
            strT = strT.concat(objPS.toString());
        }
        return strT;
    }

    public void saveAs(final String pstrSaveAsFileName) {
        LOGGER.debug("SaveAs = " + pstrSaveAsFileName);
        objSaveFile = new JSTextFile(pstrSaveAsFileName);
        this.save(objSaveFile);
    }

    public void save() {
        objSaveFile = new JSTextFile(strFileName);
        this.save(objSaveFile);
    }

    public void save(final JSTextFile pobjSaveFile1) {
        Map<String, SOSProfileSection> objS = this.getSections();
        LOGGER.debug("number of sections = " + objS.size());
        try {
            for (SOSProfileSection objPS : objS.values()) {
                pobjSaveFile1.writeLine(" ");
                pobjSaveFile1.writeLine("[" + objPS.getName() + "]");
                LOGGER.debug(objPS.getName());
                for (SOSProfileEntry objEntry : objPS.getEntries().values()) {
                    pobjSaveFile1.writeLine(objEntry.getName() + "=" + objEntry.getValue());
                    LOGGER.debug("     " + objEntry.getName() + " = " + objEntry.getValue());
                }
            }
            pobjSaveFile1.close();
            LOGGER.debug("File saved " + pobjSaveFile1.getAbsolutePath());
        } catch (Exception e) {
            throw new JobSchedulerException(e.getMessage());
        }
    }

}