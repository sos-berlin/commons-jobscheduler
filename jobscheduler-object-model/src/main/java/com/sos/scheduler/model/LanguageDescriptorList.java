package com.sos.scheduler.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sos.JSHelper.interfaces.ISOSComboItem;

/** @author KB */
public class LanguageDescriptorList {

    public static final String JAVAX_SCRIPT_RHINO = "javax.script:rhino";
    public static final String JAVAX_SCRIPT_ECMASCRIPT = "javax.script:ecmascript";
    public static final String JAVAX_JAVASCRIPT = "java:javascript";
    public static final String PL_SQL = "plsql";
    public static final String PERL_SCRIPT = "perlScript";
    public static final String VB_SCRIPT = "VBScript";
    public static final String VB_SCRIPT_CONTROL = "scriptcontrol:vbscript";
    public static final String JAVASCRIPT = "javascript";
    public static final String SHELL = "shell";
    public static final String JAVA = "java";
    public static final String DOTNET = "dotnet";
    public static final String SQL_PLUS = "sql*plus";
    public static final String SSH = "ssh";
    public static final String POWERSHELL = "powershell";

    private static List<LanguageDescriptor> lstLanguages = Arrays.asList(
            new LanguageDescriptor(SHELL, 0, false, SHELL, "", "", true, false),
            new LanguageDescriptor(JAVA, 1, false, JAVA, "", "", true, true), 
            new LanguageDescriptor(DOTNET, 2, false, DOTNET, "", "", true, true), 
            new LanguageDescriptor(JAVAX_JAVASCRIPT, 3, false, JAVAX_JAVASCRIPT, "", "", true, true),
            new LanguageDescriptor(PERL_SCRIPT, 4, false, PERL_SCRIPT, "", "",true, true), 
            new LanguageDescriptor(POWERSHELL, 5, false, POWERSHELL, "", "", true, true), 
            new LanguageDescriptor(VB_SCRIPT, 6, false, VB_SCRIPT, "", "", true, true), 
            new LanguageDescriptor(VB_SCRIPT_CONTROL, 7, false, VB_SCRIPT_CONTROL, "", "", true, true), 
            new LanguageDescriptor(JAVAX_SCRIPT_RHINO, 8, false, JAVAX_SCRIPT_RHINO, "", "", true, true), 
            new LanguageDescriptor(JAVAX_SCRIPT_ECMASCRIPT, 9, false, JAVAX_SCRIPT_ECMASCRIPT, "", "", true, true), 
            new LanguageDescriptor(JAVASCRIPT, 10, false, JAVASCRIPT, "", "", true, true)
            );

    public LanguageDescriptorList() {
    }

    public static String[] getLanguages4APIJobs() {
        List<String> lstL = new ArrayList<String>();
        for (LanguageDescriptor objL : lstLanguages) {
            if (objL.isIsAPIL()) {
                lstL.add(objL.getLanguageName());
            }
        }
        return lstL.toArray(new String[lstL.size()]);
    }

    public static String[] getLanguages4Monitor() {
        List<String> lstL = new ArrayList<String>();
        for (LanguageDescriptor objL : lstLanguages) {
            if (objL.isIsMonitorL()) {
                lstL.add(objL.getLanguageName());
            }
        }
        return lstL.toArray(new String[lstL.size()]);
    }

    public static ArrayList<ISOSComboItem> getComboItems4APIJobs() {
        ArrayList<ISOSComboItem> lstL = new ArrayList<ISOSComboItem>();
        for (LanguageDescriptor objL : lstLanguages) {
            if (objL.isIsAPIL()) {
                lstL.add(objL);
            }
        }
        return lstL;
    }

    public static LanguageDescriptor getLanguageDescriptor(final int pintLang) {
        for (LanguageDescriptor objL : lstLanguages) {
            if (objL.getLanguageNumber() == pintLang) {
                return objL;
            }
        }
        return null;
    }

    public static LanguageDescriptor getLanguageDescriptor4Class(final String pstrClassName) {
        if (pstrClassName != null && !pstrClassName.isEmpty()) {
            for (LanguageDescriptor objL : lstLanguages) {
                if (objL.getClassName().equalsIgnoreCase(pstrClassName)) {
                    return objL;
                }
            }
        }
        return null;
    }

    public static LanguageDescriptor getDefaultLanguage() {
        return lstLanguages.get(0);
    }

    public static LanguageDescriptor getDefaultLanguage4Monitor() {
        return lstLanguages.get(2);
    }

    public static LanguageDescriptor getLanguageDescriptor(final String pstrLanguage) {
        if (pstrLanguage != null && !pstrLanguage.isEmpty()) {
            for (LanguageDescriptor objL : lstLanguages) {
                if (objL.getLanguageName().equalsIgnoreCase(pstrLanguage.toLowerCase())) {
                    return objL;
                }
            }
        }
        return null;
    }

    public static String getJavaClassName4HiddenJob(final int pintLang) {
        String strR = "";
        LanguageDescriptor objL = getLanguageDescriptor(pintLang);
        if (objL != null) {
            strR = objL.getClassName();
        }
        return strR;
    }

    public static boolean isHiddenJobLanguage(final int pintLang) {
        LanguageDescriptor objL = getLanguageDescriptor(pintLang);
        return objL.isHiddenL();
    }

}