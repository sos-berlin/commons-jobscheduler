package com.sos.JSHelper.Options;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Archiver.IJSArchiverOptions;
import com.sos.JSHelper.Archiver.JSArchiverOptions;
import com.sos.JSHelper.Exceptions.JSExceptionFileNotReadable;
import com.sos.JSHelper.Exceptions.JSExceptionInputFileNotFound;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Exceptions.ParametersMissingButRequiredException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.JSHelper.io.Files.JSXMLFile;
import com.sos.i18n.I18NBase;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NResourceBundle;


@JSOptionClass(name = "JSOptionsClass", description = "JSOptionsClass")
@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class JSOptionsClass extends I18NBase implements IJSArchiverOptions, Serializable {

    protected static final String conParamNamePrefixALTERNATIVE = "alternative_";
    protected static final String conParamNamePrefixJUMP = "jump_";
    protected static final String conParamNamePrefixTARGET = "target_";
    protected static final String conParamNamePrefixSOURCE = "source_";
    protected final String conNullButMandatory = "%3$s : Setting %1$s (%2$s) is mandatory, must be not null.%n";
    protected final String conChangedMsg = "%3$s: changed from '%1$s' to '%2$s'.";
    protected HashMap<String, String> objSettings = null;
    protected HashMap<String, String> objProcessedOptions = null;
    protected Class objClassName4PreferenceStore = this.getClass();
    protected Msg objMsg = null;
    protected String strAlternativePrefix = "";
    protected boolean flgSetAllOptions = false;
    private static final String CLASS_NAME = "JSOptionsClass";
    private static final Logger LOGGER = Logger.getLogger(JSOptionsClass.class);
    private static final long serialVersionUID = 8497293387023797049L;
    private static String conEnvVarJS_TEST_MODE = new String("JS_TEST_MODE");
    private static Properties objP = new Properties();
    private final StringBuffer strBuffer = new StringBuffer("");
    private boolean flgIgnoreEnvironmentVariables = false;
    private final String strOptionNamePrefix = "-";
    private String strCommandLineArgs[];
    private String strCurrentNodeName = "";
    private String strCurrentJobName = "";
    private int intCurrentJobId = 0;
    private String strCurrentJobFolder = "";
    private final String conFilePathSeparator = File.separator;
    private String strTempDirName = System.getProperty("java.io.tmpdir") + conFilePathSeparator;
    private String strUserDirName = System.getProperty("user.dir") + conFilePathSeparator;
    public final static String newLine = System.getProperty("line.separator");
    public JSOptionPropertyFolderName UserDir = new JSOptionPropertyFolderName(this, "", "", "", "", false);
    public static boolean gflgUseBase64ForObject = true;
    public static boolean flgIncludeProcessingInProgress = false;
    public boolean gflgSubsituteVariables = true;
    public String TestVar = "Wert von TestVar";
    public Class objParentClass = this.getClass();
    public String gstrApplicationName = "JobScheduler";
    public String gstrApplicationDocuUrl = "http://docu.sos-berlin.com";
    public Preferences objPreferenceStore = null;

    protected enum IterationTypes {
        setRecord(1), getRecord(2), toOut(3), createXML(4), setDefaultValues(5), clearAllValues(6), countSegmentFields(7), CheckMandatory(12), setPrefix(
                14), toString(13), getCommandLine(14), DirtyToString(15), getKeyValuePair(16), LoadValues(17), StoreValues(18), getQuotedCommandLine(
                19);

        private int intType;

        IterationTypes(final int pintType) {
            intType = pintType;
        }

        public int Code() {
            return intType;
        }
    }

    public JSOptionsClass() {
        try {
            objMsg = new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));
        } catch (Exception e) {
            // handle exception
        }
    }

    public JSOptionsClass(final HashMap<String, String> pobjSettings) {
        this();
        this.Settings(pobjSettings);
        ArchiverOptions();
    }

    @JSOptionDefinition(name = "BaseDirectory", description = "A Base Directory for all relative FileNames used by SOSOptionFileName", 
            key = "Base_Directory", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName BaseDirectory = new SOSOptionFolderName(this, CLASS_NAME + ".Base_Directory", 
            "A Base Directory for all relative FileNames used by SOSOptionFileName", "env:user.dir", "env:user.dir", false);

    public SOSOptionFolderName getBaseDirectory() {
        return BaseDirectory;
    }

    public JSOptionsClass setBaseDirectory(final SOSOptionFolderName pstrValue) {
        BaseDirectory = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "DateFormatMask", description = "General Mask for date fomatting", key = "Date_Format_Mask", type = "SOSOptionString", 
            mandatory = false)
    public SOSOptionString DateFormatMask = new SOSOptionString(this, CLASS_NAME + ".Date_Format_Mask", "General Mask for date fomatting", 
            "yyyy-MM-dd", "yyyy-MM-dd", false);

    public SOSOptionString getDateFormatMask() {
        return DateFormatMask;
    }

    public JSOptionsClass setDateFormatMask(final SOSOptionString pstrValue) {
        DateFormatMask = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "TimeFormatMask", description = "General Mask for time formatting", key = "Time_Format_Mask", type = "SOSOptionString", 
            mandatory = false)
    public SOSOptionString TimeFormatMask = new SOSOptionString(this, CLASS_NAME + ".Time_Format_Mask", "General Mask for time formatting", 
            "HH:mm:ss", "HH:mm:ss", false);

    public SOSOptionString getTimeFormatMask() {
        return TimeFormatMask;
    }

    public JSOptionsClass setTimeFormatMask(final SOSOptionString pstrValue) {
        TimeFormatMask = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Scheduler_Hot_Folder", description = "Pathname to the JobScheduler live-folder", key = "Scheduler_Hot_Folder", 
            type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName Scheduler_Hot_Folder = new SOSOptionFolderName(this, CLASS_NAME + ".Scheduler_Hot_Folder", 
            "Pathname to the JobScheduler live-folder", "${SCHEDULER_DATA}/config/live", "", true);

    public String getScheduler_Hot_Folder() {
        return Scheduler_Hot_Folder.Value();
    }

    public JSOptionsClass setScheduler_Hot_Folder(final String pstrValue) {
        Scheduler_Hot_Folder.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Scheduler_Data", description = "Data Folder of JobScheduler Installation", key = "Scheduler_Data", 
            type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName Scheduler_Data = new SOSOptionFolderName(this, CLASS_NAME + ".Scheduler_Data", "Data Folder of JobScheduler Installation", 
            "env:SCHEDULER_DATA", "env:SCHEDULER_DATA", false);

    public String getScheduler_Data() {
        return Scheduler_Data.Value();
    }

    public JSOptionsClass setScheduler_Data(final String pstrValue) {
        Scheduler_Data.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Scheduler_Home", description = "Home Root Folder of JobScheduler", key = "Scheduler_Home", type = "SOSOptionFileName", 
            mandatory = true)
    public SOSOptionFolderName Scheduler_Home = new SOSOptionFolderName(this, CLASS_NAME + ".Scheduler_Home", "Home Root Folder of JobScheduler", 
            "env:SCHEDULER_HOME", "env:SCHEDULER_HOME", false);

    public String getScheduler_Home() {
        return Scheduler_Home.Value();
    }

    public JSOptionsClass setScheduler_Home(final String pstrValue) {
        Scheduler_Home.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Local_user", description = "I18N is for internationalization of Application", key = "Local_user", 
            type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName UserName = new SOSOptionUserName(this, CLASS_NAME + ".local_user", "Name of local user", 
            System.getProperty("user.name"), System.getProperty("user.name"), true);
    
    @JSOptionDefinition(name = "Locale", description = "I18N is for internationalization of Application", key = "Locale", 
            type = "SOSOptionString", mandatory = true)
    public SOSOptionLocale Locale = new SOSOptionLocale(this, CLASS_NAME + ".Locale", "I18N is for internationalization of Application", 
            "env:SOS_LOCALE", java.util.Locale.getDefault().toString(), true);

    public java.util.Locale getI18NLocale() {
        return new java.util.Locale(Locale.Value());
    }

    public String getLocale() {
        return Locale.Value();
    }

    @Override
    public void setLocale(final String pstrValue) {
        Locale.Value(pstrValue);
    }

    @JSOptionDefinition(name = "CheckNotProcessedOptions", description = "If this Option is set to true, all not processed or recognized options "
            + "are reported as a warning", key = "CheckNotProcessedOptions", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckNotProcessedOptions = new SOSOptionBoolean(this, CLASS_NAME + ".CheckNotProcessedOptions", 
            "If this Option is set to true, all not processed or recognized options are reported as a warning", "false", "false", false);

    public String getCheckNotProcessedOptions() {
        return CheckNotProcessedOptions.Value();
    }

    public JSOptionsClass setCheckNotProcessedOptions(final String pstrValue) {
        CheckNotProcessedOptions.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "XmlId", description = "This ist the ...", key = "XmlId", type = "SOSOptionString", mandatory = true)
    public SOSOptionString XmlId = new SOSOptionString(this, CLASS_NAME + ".XmlId", "This ist the ...", "root", "root", true);

    public String getXmlId() throws Exception {
        return XmlId.Value();
    }

    public JSOptionsClass setXmlId(final String pstrValue) throws Exception {
        XmlId.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "ArchiverOptions", value = "", description = "Optionen f�r die Dateiarchivierung", key = "", type = "JSOptionClass", 
            mandatory = false)
    private JSArchiverOptions objArchiverOptions = null;

    @JSOptionDefinition(name = "TestMode", value = "false", description = "Test Modus schalten ", key = "TestMode", type = "JSOptionBoolean", mandatory = false)
    public SOSOptionBoolean TestMode = new SOSOptionBoolean(this, CLASS_NAME + ".TestMode", "Test Modus schalten ", "false", "false", false);

    @JSOptionDefinition(name = "Debug", value = "false", description = "Debug-Modus schalten (true/false)", key = "Debug", type = "JSOptionBoolean", 
            mandatory = false)
    public SOSOptionBoolean Debug = new SOSOptionBoolean(this, CLASS_NAME + ".Debug", "Debug-Modus schalten (true/false)", "false", "false", false);

    @JSOptionDefinition(name = "DebugLevel", value = "0", description = "DebugLevel", key = "DebugLevel", type = "JSOptionInteger", mandatory = false)
    public SOSOptionInteger DebugLevel = new SOSOptionInteger(this, CLASS_NAME + ".DebugLevel", "DebugLevel", "0", "0", false);
    
    @JSOptionDefinition(name = "log_filename", description = "Name der Datei mit den Logging-Eintr�gen", key = "log_filename", 
            type = "SOSOptionFileName", mandatory = false)
    public SOSOptionLogFileName log_filename = new SOSOptionLogFileName(this, CLASS_NAME + ".log_filename", "Name der Datei mit den Logging-Eintr�gen", 
            "stdout", "stdout", false);

    public SOSOptionLogFileName getlog_filename() {
        return log_filename;
    }

    public void setlog_filename(final SOSOptionLogFileName pstrValue) {
        log_filename = pstrValue;
    }

    @JSOptionDefinition(name = "log4jPropertyFileName", description = "Name of the LOG4J Property File", key = "log4j_Property_FileName", 
            type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName log4jPropertyFileName = new SOSOptionInFileName(this, CLASS_NAME + ".log4j_Property_FileName", 
            "Name of the LOG4J Property File", "env:log4j.configuration", "./log4j.properties", false);

    public String getlog4jPropertyFileName() {
        return log4jPropertyFileName.Value();
    }

    public JSOptionsClass setlog4jPropertyFileName(final String pstrValue) {
        log4jPropertyFileName.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "ApplicationName", description = "Name of the Application", key = "ApplicationName", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ApplicationName = new SOSOptionString(this, CLASS_NAME + ".ApplicationName", "Name of the Application", 
            "env:SOSApplicationName", "env:SOSApplicationName", false);

    public SOSOptionString getApplicationName() {
        return ApplicationName;
    }

    public JSOptionsClass setApplicationName(final SOSOptionString pstrValue) {
        ApplicationName = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "ApplicationDocuUrl", description = "The Url of the Documentation of this Application", key = "ApplicationDocuUrl", 
            type = "SOSOptionUrl", mandatory = false)
    public SOSOptionUrl ApplicationDocuUrl = new SOSOptionUrl(this, CLASS_NAME + ".ApplicationDocuUrl", "The Url of the Documentation of this Application", 
            "env:SOSApplicationDocuUrl", "env:SOSApplicationDocuUrl", false);

    public SOSOptionUrl getApplicationDocuUrl() {
        return ApplicationDocuUrl;
    }

    public JSOptionsClass setApplicationDocuUrl(final SOSOptionUrl pstrValue) {
        ApplicationDocuUrl = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "AllowEmptyParameterList", description = "If true, an empty parameter list leads not into an error", key = "AllowEmptyParameterList", 
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean AllowEmptyParameterList = new SOSOptionBoolean(this, CLASS_NAME + ".AllowEmptyParameterList", 
            "If true, an empty parameter list leads not into an error", "true", "true", false);

    public SOSOptionBoolean getAllowEmptyParameterList() {
        return AllowEmptyParameterList;
    }

    public JSOptionsClass setAllowEmptyParameterList(final SOSOptionBoolean pstrValue) {
        AllowEmptyParameterList = pstrValue;
        return this;
    }


    public String getPrefix() {
        return strAlternativePrefix;
    }

    public HashMap<String, String> Settings() {
        if (objSettings == null) {
            objSettings = new HashMap<String, String>();
        }
        return objSettings;
    }

    public HashMap<String, String> Settings4StepName() {
        HashMap<String, String> objS = new HashMap<String, String>();
        int intStartPos = strCurrentNodeName.length() + 1;
        for (final Object element : Settings().entrySet()) {
            final Map.Entry<String, String> mapItem = (Map.Entry<String, String>) element;
            String strMapKey = mapItem.getKey().toString();
            String strValue = mapItem.getValue();
            if (strMapKey.indexOf("/") != -1) {
                if (strMapKey.startsWith(strCurrentNodeName + "/")) {
                    strMapKey = strMapKey.substring(intStartPos);
                } else {
                    strValue = null;
                }
            }
            if (strValue != null) {
                LOGGER.debug(strMapKey + " = " + strValue);
                objS.put(strMapKey, strValue);
            }
        }
        return objS;
    }

    public String TempDirName() {
        return this.TempDir();
    }

    public String TempDir() {
        return strTempDirName;
    }

    public void TempDir(final String pstrTempDirName) {
        strTempDirName = pstrTempDirName;
    }

    public String UserDir() {
        return strUserDirName;
    }

    public void UserDir(final String pstrUserDirName) {
        strUserDirName = pstrUserDirName;
    }

    public void Settings(final HashMap<String, String> pobjSettings) {
        objSettings = pobjSettings;
        setAllCommonOptions(pobjSettings);
    }

    public String getItem(final String pstrKey) {
        String strTemp = "";
        String strKey = pstrKey;
        if (isEmpty(pstrKey)) {
            return strTemp;
        }
        if (objProcessedOptions == null) {
            objProcessedOptions = new HashMap<String, String>();
        }
        strKey = strKey.replaceAll("_", "");
        String strLSKey = "";
        if (!strCurrentNodeName.isEmpty()) {
            strLSKey = strCurrentNodeName + "/" + pstrKey.replaceAll("_", "");
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry mapItem = (Map.Entry) element;
                String strMapKey = mapItem.getKey().toString();
                strMapKey = strMapKey.replaceAll("_", "");
                if (strLSKey.equalsIgnoreCase(strMapKey)) {
                    if (mapItem.getValue() != null) {
                        strTemp = mapItem.getValue().toString();
                    } else {
                        strTemp = null;
                    }
                    objProcessedOptions.put(strMapKey, strTemp);
                    return strTemp;
                }
            }
        }
        for (final Object element : objSettings.entrySet()) {
            final Map.Entry mapItem = (Map.Entry) element;
            String strMapKey = mapItem.getKey().toString();
            String lstrMapKey = strMapKey.replaceAll("_", "");
            if (strKey.equalsIgnoreCase(lstrMapKey)) {
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue().toString();
                } else {
                    strTemp = null;
                }
                objProcessedOptions.put(strMapKey, strTemp);
                return strTemp;
            }
        }
        int i = strKey.indexOf('.');
        if (i > 0) {
            strLSKey = "";
            strKey = strKey.substring(++i);
            if (!strCurrentNodeName.isEmpty()) {
                strLSKey = strCurrentNodeName + "/" + strKey;
                for (final Object element : objSettings.entrySet()) {
                    final Map.Entry mapItem = (Map.Entry) element;
                    String strMapKey = mapItem.getKey().toString();
                    String lstrMapKey = strMapKey.replaceAll("_", "");
                    if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
                        if (mapItem.getValue() != null) {
                            strTemp = mapItem.getValue().toString();
                        } else {
                            strTemp = null;
                        }
                        objProcessedOptions.put(strMapKey, strTemp);
                        return strTemp;
                    }
                }
            }
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry mapItem = (Map.Entry) element;
                String strMapKey = mapItem.getKey().toString();
                String lstrMapKey = strMapKey.replaceAll("_", "");
                if (!strLSKey.isEmpty()) {
                    if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
                        if (mapItem.getValue() != null) {
                            strTemp = mapItem.getValue().toString();
                        } else {
                            strTemp = null;
                        }
                        objProcessedOptions.put(strMapKey, strTemp);
                        return strTemp;
                    }
                }
                if (strKey.equalsIgnoreCase(lstrMapKey)) {
                    if (mapItem.getValue() != null) {
                        strTemp = mapItem.getValue().toString();
                    } else {
                        strTemp = null;
                    }
                    objProcessedOptions.put(strMapKey, strTemp);
                    return strTemp;
                }
            }
        }
        if (strTemp == null || strTemp.isEmpty()) {
            strTemp = null;
        }
        return strTemp;
    }

    public boolean CheckNotProcessedOptions() {
        boolean flgIsOK = true;
        int intNumberOfNotProcessedOptions = 0;
        if (objSettings != null) {
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry<String, String> mapItem = (Map.Entry<String, String>) element;
                String strMapKey = mapItem.getKey().toString();
                String strT = objProcessedOptions.get(strMapKey);
                if (strT == null) {
                    String strValue = null;
                    if (mapItem.getValue() != null) {
                        strValue = mapItem.getValue().toString();
                    } else {
                        strValue = null;
                    }
                    LOGGER.warn(String.format("SOSOPT-W-001: Option '%1$s' with value '%2$s' is unknown and not processed", strMapKey, strValue));
                    flgIsOK = false;
                    intNumberOfNotProcessedOptions++;
                }
            }
        }
        return flgIsOK;
    }

    public HashMap<String, String> getProcessedOptions() {
        if (objProcessedOptions == null) {
            objProcessedOptions = new HashMap<String, String>();
        }
        return objProcessedOptions;
    }

    public void addProcessedOptions(final HashMap<String, String> phsmMap) {
        this.getProcessedOptions();
        objProcessedOptions.putAll(phsmMap);
    }

    public String getItem(final String pstrKey, final String pstrDefaultValue) {
        String strT = this.getItem(pstrKey);
        if (isEmpty(strT)) {
            strT = pstrDefaultValue;
        }
        return strT;
    }

    protected int getIntItem(final String pstrKey) {
        return String2Integer(this.getItem(pstrKey));
    }

    protected int String2Integer(final String pstrValue) {
        int intT = 0;
        if (isNotEmpty(pstrValue)) {
            try {
                intT = Integer.parseInt(pstrValue);
            } catch (final NumberFormatException e) {
                throw new RuntimeException("format Exception raised", e);
            }
        }
        return intT;
    }

    protected boolean getBoolItem(final String pstrKey) {
        boolean flgT = false;
        if (isNotEmpty(pstrKey)) {
            flgT = String2Bool(this.getItem(pstrKey));
        }
        return flgT;
    }

    protected boolean getBoolItem(final String pstrKey, final boolean pflgDefault) {
        boolean flgT = false;
        if (isNotEmpty(pstrKey)) {
            flgT = String2Bool(this.getItem(pstrKey));
        } else {
            flgT = pflgDefault;
        }
        return flgT;
    }

    @Override
    public boolean String2Bool(final String pstrVal) {
        boolean flgT = false;
        if (isNotEmpty(pstrVal)) {
            if ("1".equals(pstrVal) || "y".equalsIgnoreCase(pstrVal) || "yes".equalsIgnoreCase(pstrVal) || "j".equalsIgnoreCase(pstrVal)
                    || "on".equalsIgnoreCase(pstrVal) || "true".equalsIgnoreCase(pstrVal) || "wahr".equalsIgnoreCase(pstrVal)) {
                flgT = true;
            }
        }
        return flgT;
    }

    protected void toOut(final String pstrS) throws Exception {
        String strT = pstrS;
        if (!strT.contains(CLASS_NAME)) {
            strT += getAllOptionsAsString();
        }
        LOGGER.debug(strT);
    }

    protected void toOut() {
        try {
            this.toOut(getAllOptionsAsString());
        } catch (final JobSchedulerException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        String strT = null;
        try {
            strT = getAllOptionsAsString();
        } catch (final Exception e) {
            throw new JobSchedulerException("toString failed", e);
        }
        return strT;
    }

    public String dirtyString() {
        String strT = "";
        try {
            strT += "\n" + getAllOptionsAsString(IterationTypes.DirtyToString);
        } catch (final Exception e) {
            throw new JobSchedulerException("dirtyString failed", e);
        }
        return strT;
    }

    public void Options2ClipBoard() {
        String strT = "";
        if (objSettings != null) {
            for (final Object element : objSettings.entrySet()) {
                final Map.Entry mapItem = (Map.Entry) element;
                final String strMapKey = mapItem.getKey().toString();
                if (mapItem.getValue() != null) {
                    String strTemp = mapItem.getValue().toString();
                    strT += "\n" + strMapKey + "=" + strTemp;
                }
            }
        } else {
            strT = this.dirtyString();
        }
        LOGGER.debug(strT);
    }

    private String getAllOptionsAsString(final IterationTypes penuIterationType) {
        String strT = "";
        if (objParentClass != null) {
            strT += IterateAllDataElementsByAnnotation(objParentClass, this, penuIterationType, new StringBuffer(""));
        }
        return strT;
    }

    private String getAllOptionsAsString() {
        return getAllOptionsAsString(IterationTypes.toString);
    }

    protected void setAllCommonOptions(final HashMap<String, String> JSSettings) {
        objSettings = JSSettings;
        if (objParentClass != null) {
            IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.countSegmentFields, strBuffer);
            IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setRecord, strBuffer);
        }
        UserDir.MapValue();
        if (!this.IgnoreEnvironmentVariables()) {
            getEnvironmentVariables();
        }
    }

    public JSOptionsClass getEnvironmentVariables() {
        String strT = EnvironmentVariable(JSOptionsClass.conEnvVarJS_TEST_MODE);
        if (isNotEmpty(strT)) {
            this.TestMode(String2Bool(strT));
        }
        strT = EnvironmentVariable("JS_DEBUG");
        if (isNotEmpty(strT)) {
            this.Debug(String2Bool(strT));
        }
        strT = EnvironmentVariable("JS_DEBUG_LEVEL");
        if (isNotEmpty(strT)) {
            this.DebugLevel(String2Integer(strT));
        }
        return this;
    }

    public void CheckMandatory() throws Exception {
        if (objParentClass != null) {
            IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.CheckMandatory, strBuffer);
        }
    }

    public boolean TestMode() {
        return TestMode.value();
    }

    public void TestMode(final boolean pflgTestMode) {
        TestMode.value(pflgTestMode);
    }

    public boolean Debug() {
        return Debug.value();
    }

    public void Debug(final boolean pflgDebug) {
        Debug.value(pflgDebug);
    }

    public int DebugLevel() {
        return DebugLevel.value();
    }

    public void DebugLevel(final int pintDebugLevel) {
        DebugLevel.value(pintDebugLevel);
    }

    protected String CheckFileIsReadable(final String pstrFileName, final String pstrMethodName) {
        if (isNotEmpty(pstrFileName)) {
            final File fleF = new File(pstrFileName);
            String strT = null;
            if (!fleF.exists()) {
                strT = String.format("%2$s: File '%1$s' does not exist.", pstrFileName, pstrMethodName);
                this.SignalError(new JSExceptionInputFileNotFound(strT), strT);
            }
            if (!fleF.canRead()) {
                strT = String.format("%2$s: File '%1$s'. canRead returns false. Check permissions.", pstrFileName, pstrMethodName);
                this.SignalError(new JSExceptionFileNotReadable(strT), strT);
            }
        }
        return pstrFileName;
    }

    public String CheckFolder(String pstrFileName, final String pstrMethodName, final Boolean flgCreateIfNotExist) {
        if (isNotEmpty(pstrFileName)) {
            final String strSep = System.getProperty("file.separator");
            if (!pstrFileName.endsWith(strSep)) {
                pstrFileName += strSep;
            }
            final File fleF = new File(pstrFileName);
            if (!fleF.exists()) {
                if (!flgCreateIfNotExist) {
                    this.SignalError(String.format("%2$s: Folder '%1$s' does not exist.", pstrFileName, pstrMethodName));
                } else {
                    fleF.mkdir();
                    SignalInfo(String.format("%2$s: Folder '%1$s' created.", pstrFileName, pstrMethodName));
                }
            }
            if (!fleF.canRead()) {
                this.SignalError(String.format("%2$s: File '%1$s'. canRead returns false. Check permissions.", pstrFileName, pstrMethodName));
            }
        }
        return pstrFileName;
    }

    public String CheckIsFileWritable(final String pstrFileName, final String pstrMethodName) {
        String strT = null;
        if (isNotEmpty(pstrFileName)) {
            try {
                final File fleF = new File(pstrFileName);
                if (!fleF.exists()) {
                    fleF.createNewFile();
                }
                if (!fleF.canWrite()) {
                    strT = String.format("%2$s: File '%1$s'. canWrite returns false. Check permissions.", pstrFileName, pstrMethodName);
                    strT += fleF.toString();
                }
            } catch (final Exception objException) {
                strT = String.format("%2$s: File '%1$s'. Exception thrown. Check permissions.", pstrFileName, pstrMethodName);
                final JobSchedulerException objJSEx = new JobSchedulerException(strT, objException);
                this.SignalError(objJSEx, strT);
            }
            if (strT != null) {
                this.SignalError(strT);
            }
        }
        return pstrFileName;
    }

    protected String getVal(final String pstrS) {
        final String strT = "";
        if (pstrS == null) {
            return strT.toString();
        }
        return pstrS.toString();
    }

    @Override
    public JSArchiverOptions ArchiverOptions() {
        if (objArchiverOptions == null) {
            objArchiverOptions = new JSArchiverOptions();
            objArchiverOptions.registerMessageListener(this);
            if (objSettings != null) {
                objArchiverOptions.setAllOptions(objSettings);
            }
        }
        return objArchiverOptions;
    }

    protected String NormalizeDirectoryName(final String pstrDirectoryName) {
        String strT = pstrDirectoryName;
        if (strT == null || strT.trim().isEmpty()) {
            strT = "";
        } else {
            if (!strT.endsWith(File.separator) && !strT.endsWith("/")) {
                strT += "/";
            }
            if (strT.startsWith("./")) {
                strT = strT.substring(2);
                try {
                    strT = AbsolutFileName(strT);
                }
                catch (final Exception objException) {
                    // handle exception
                }
            }
            strT = SubstituteVariables(strT);
        }
        return strT;
    }

    protected String NormalizeFileName(final String pstrFileName) {
        String strT = pstrFileName;
        strT = SubstituteVariables(pstrFileName);
        return strT;
    }

    public String NormalizeFileName(final String pstrFileName, final String pstrDirectoryName) {
        String strNewFileName = pstrFileName;
        if (pstrFileName.indexOf("/") < 0) {
            strNewFileName = NormalizeDirectoryName(pstrDirectoryName) + pstrFileName;
        }
        return strNewFileName;
    }

    public String AbsolutFileName(final String pstrFileName) throws Exception {
        String strT = pstrFileName;
        if (strT == null) {
            return strT;
        }
        if (strT.startsWith("file:")) {
            strT = strT.substring(5);
            return strT;
        }
        if (strT.startsWith("/")) {
            return strT;
        }
        if (strT.startsWith("./")) {
            strT = strT.substring(2);
        }
        strT = new File(strT).toURI().toURL().toString();
        if (strT.startsWith("file:")) {
            strT = strT.substring(5);
        }
        return strT;
    }

    public String SubstituteVariables(final String pstrValue) {
        String strT = pstrValue;
        if (strT == null) {
            return null;
        }
        int i = -1;
        i = strT.indexOf("$");
        strT = strT.replace("//", "/");
        return strT;
    }

    public boolean IgnoreEnvironmentVariables() {
        return flgIgnoreEnvironmentVariables;
    }

    public JSOptionsClass IgnoreEnvironmentVariables(final boolean pflgIgnoreEnvironmentVariables) throws Exception {
        flgIgnoreEnvironmentVariables = pflgIgnoreEnvironmentVariables;
        return this;
    }

    protected String[] SplitString(final String pstrStr) {
        if (pstrStr == null) {
            return null;
        }
        return pstrStr.trim().split("[;|,]");
    }

    protected void CheckNull(final String pstrMethodName, final String pstrTitel, final String pstrOptionName, final String pstrOptionValue) throws Exception {
        if (isEmpty(pstrOptionValue)) {
            this.SignalError(String.format(conNullButMandatory, pstrMethodName, pstrTitel, pstrOptionName));
        }
    }

    public void CommandLineArgs(final String pstrArgs) {
        StrTokenizer objT = new StrTokenizer(pstrArgs);
        String[] strA = objT.getTokenArray();
        CommandLineArgs(strA);
    }

    public void CommandLineArgs(final String[] pstrArgs) {
        final String conMethodName = CLASS_NAME + "::CommandLineArgs ";
        if (AllowEmptyParameterList.isFalse()) {
            if (pstrArgs.length <= 0) {
                throw new ParametersMissingButRequiredException(ApplicationName.Value(), ApplicationDocuUrl.Value());
            }
        }
        strCommandLineArgs = pstrArgs;
        boolean flgOption = true;
        String strOptionName = null;
        String strOptionValue = null;
        this.Settings();
        final int l = strOptionNamePrefix.length();
        for (final String strCommandLineArg : strCommandLineArgs) {
            if (flgOption) {
                if (strCommandLineArg.length() < l) {
                    continue;
                }
                if (strCommandLineArg.substring(0, l).equalsIgnoreCase(strOptionNamePrefix)) {
                    strOptionName = strCommandLineArg.substring(l);
                    flgOption = false;
                    // name and value separated by an equalsign?
                    int intESPos = strOptionName.indexOf("=");
                    if (intESPos > 0) {
                        strOptionValue = strOptionName.substring(intESPos + 1);
                        strOptionName = strOptionName.substring(0, intESPos);
                        objSettings.put(strOptionName, strOptionValue);
                        if ("password".equalsIgnoreCase(strOptionName) || "proxy_password".equalsIgnoreCase(strOptionName)) {
                            this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, "*****"));
                        } else {
                            this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, strOptionValue));
                        }
                        flgOption = true;
                    }
                }
            } else {
                if (strOptionName != null) {
                    strOptionValue = strCommandLineArg;
                    flgOption = true;
                    objSettings.put(strOptionName, strOptionValue);
                    if ("password".equalsIgnoreCase(strOptionName) || "proxy_password".equalsIgnoreCase(strOptionName)) {
                        this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, "*****"));
                    } else {
                        this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, strOptionValue));
                    }
                    strOptionName = null;
                }
            }
        }
        final String strPropertyFileName = this.getItem("PropertyFileName", "");
        if (!strPropertyFileName.isEmpty()) {
            LoadProperties(strPropertyFileName);
            strOptionName = null;
            flgOption = true;
            for (final String strCommandLineArg : strCommandLineArgs) {
                if (flgOption) {
                    if (strCommandLineArg.substring(0, l).equalsIgnoreCase(strOptionNamePrefix)) {
                        strOptionName = strCommandLineArg.substring(l);
                        flgOption = false;
                    }
                } else {
                    if (strOptionName != null) {
                        strOptionValue = strCommandLineArg;
                        flgOption = true;
                        objSettings.put(strOptionName, strOptionValue);
                        if ("password".equalsIgnoreCase(strOptionName) || "proxy_password".equalsIgnoreCase(strOptionName)) {
                            this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName, "*****"));
                        } else {
                            this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName, strOptionValue));
                        }
                        strOptionName = null;
                    }
                }
            }
            message(conMethodName + ": Property-File loaded. " + strPropertyFileName);
        }
        DumpSettings();
        setAllOptions(objSettings);
    }

    public String[] CommandLineArgs() {
        return strCommandLineArgs;
    }

    public void LoadProperties(final String pstrPropertiesFileName) {
        final String conMethodName = CLASS_NAME + "::LoadProperties ";
        try {
            final Properties objProp = new Properties();
            objProp.load(new FileInputStream(pstrPropertiesFileName));
            message(conMethodName + ": PropertyFile red. Name '" + pstrPropertiesFileName + "'.");
            this.Settings();
            for (final Object element : objProp.entrySet()) {
                final Map.Entry mapItem = (Map.Entry) element;
                final String strMapKey = mapItem.getKey().toString();
                if (mapItem.getValue() != null) {
                    final String strTemp = mapItem.getValue().toString();
                    if (strTemp != null && !strTemp.isEmpty() && !".".equalsIgnoreCase(strTemp)) {
                        objSettings.put(strMapKey, strTemp);
                    }
                }
            }
            message(conMethodName + ": Property-File loaded");
            setAllOptions(objSettings);
            setAllCommonOptions(objSettings);
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void LoadSystemProperties() throws Exception {
        Properties objProp = new Properties();
        objProp = System.getProperties();
        LoadProperties(objProp);
    }

    public void LoadProperties(final Properties pobjProp) {
        this.Settings();
        for (final Object element : pobjProp.entrySet()) {
            final Map.Entry mapItem = (Map.Entry) element;
            final String strMapKey = mapItem.getKey().toString();
            if (mapItem.getValue() != null) {
                final String strTemp = mapItem.getValue().toString();
                LOGGER.debug("Property " + strMapKey + " = " + strTemp);
                if (strTemp != null && !strTemp.isEmpty() && !".".equalsIgnoreCase(strTemp)) {
                    objSettings.put(strMapKey, strTemp);
                }
            }
        }
        try {
            setAllOptions(objSettings);
        } catch (Exception e) {
            throw new JobSchedulerException("setAllOptions returns an error:", e);
        }
        setAllCommonOptions(objSettings);
    }

    public void setAllOptions(final HashMap<String, String> pobjJSSettings, final String pstrAlternativePrefix) throws Exception {
        if (strAlternativePrefix.isEmpty()) {
            strAlternativePrefix = pstrAlternativePrefix;
            if (objParentClass != null) {
                IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setPrefix, strBuffer);
            }
        }
        this.setAllOptions(pobjJSSettings);
    }

    public void setAllOptions(final HashMap<String, String> hshMap) {
        setAllCommonOptions(hshMap);
    }

    public String CurrentNodeName() {
        return strCurrentNodeName;
    }

    public JSOptionsClass CurrentNodeName(final String pstrCurrentNodeName) throws Exception {
        strCurrentNodeName = pstrCurrentNodeName;
        return this;
    }

    public JSOptionsClass CurrentJobName(final String pstrCurrentJobName) throws Exception {
        strCurrentJobName = pstrCurrentJobName;
        return this;
    }

    public String CurrentJobName() {
        return strCurrentJobName;
    }

    public JSOptionsClass CurrentJobId(final int pintCurrentJobId) throws Exception {
        intCurrentJobId = pintCurrentJobId;
        return this;
    }

    public int CurrentJobId() {
        return intCurrentJobId;
    }

    public JSOptionsClass CurrentJobFolder(final String pstrCurrentJobFolder) throws Exception {
        strCurrentJobFolder = pstrCurrentJobFolder;
        return this;
    }

    public String CurrentJobFolder() {
        return strCurrentJobFolder;
    }

    public String OptionByName(final String pstrOptionName) {
        String strValue = null;
        Class c = this.getClass();
        strValue = getOptionValue(c, pstrOptionName);
        if (strValue == null) {
            c = objParentClass.getClass();
            strValue = getOptionValue(c, pstrOptionName);
        }
        return strValue;
    }

    private String getOptionValue(final Class c, final String pstrOptionName) {
        String strValue = null;
        Field objField = null;
        try {
            objField = c.getField(pstrOptionName);
            Object objO = objField.get(this);
            if (objO instanceof String) {
                strValue = (String) objField.get(this);
            } else {
                if (objO instanceof SOSOptionElement) {
                    SOSOptionElement objDE = (SOSOptionElement) objO;
                    strValue = objDE.Value();
                }
            }
        } catch (final NoSuchFieldException objException) {
            Method objMethod;
            try {
                objMethod = c.getMethod(pstrOptionName, null);
                strValue = (String) objMethod.invoke(this, null);
            } catch (final SecurityException exception) {
                LOGGER.error(exception.getMessage(), exception);
            } catch (final NoSuchMethodException exception) {
                // no handling for NoSuchMethodException
            } catch (final IllegalArgumentException exception) {
                LOGGER.error(exception.getMessage(), exception);
            } catch (final InvocationTargetException exception) {
                LOGGER.error(exception.getMessage(), exception);
            } catch (final IllegalAccessException exception) {
                LOGGER.error(exception.getMessage(), exception);
            }
        } catch (final IllegalAccessException exception) {
            LOGGER.error(exception.getMessage(), exception);
        }
        return strValue;
    }

    public Properties getTextProperties() {
        if (objP == null) {
            objP = objP = new Properties();
        }
        return objP;
    }

    public String replaceVars(final String pstrReplaceIn) {
        getTextProperties();
        try {
            objP.put("date", SOSOptionTime.getCurrentDateAsString(DateFormatMask.Value()));
            objP.put("time", SOSOptionTime.getCurrentTimeAsString(TimeFormatMask.Value()));
            objP.put("local_user", System.getProperty("user.name"));
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            objP.put("localhost", localMachine.getHostName());
            objP.put("local_host_ip", localMachine.getHostAddress());
            objP.put("tempdir", System.getProperty("java.io.tmpdir") + conFilePathSeparator);
            objP.put("temp", System.getProperty("java.io.tmpdir") + conFilePathSeparator);
            objP.put("uuid", SOSOptionRegExp.getUUID());
            objP.put("timestamp", SOSOptionRegExp.getUnixTimeStamp());
            objP.put("sqltimestamp", SOSOptionRegExp.getSqlTimeStamp());
            objP.put("weekofyear", SOSOptionTime.getCurrentDateAsString("yyyyw"));
            objP.put("currentweek", SOSOptionTime.getCurrentDateAsString("w"));
        } catch (Exception uhe) {
        }
        String strVal = "";
        String strKey = "";
        String strParamNameEnclosedInPercentSigns = "^.*(\\$|%)\\{([^%\\}]+)\\}.*$";
        String strNewString = "";
        if (isNotNull(pstrReplaceIn)) {
            try {
                String[] strA = pstrReplaceIn.split("\\n");
                for (String string : strA) {
                    while (string.matches(strParamNameEnclosedInPercentSigns)) {
                        strKey = string.replaceFirst(strParamNameEnclosedInPercentSigns, "$2");
                        if ("uuid".equalsIgnoreCase(strKey)) {
                            continue;
                        }
                        String strPP = "(\\$|%)\\{" + strKey + "\\}";
                        strVal = this.OptionByName(strKey);
                        if (isNotNull(strVal)) {
                            strVal = strVal.replace('\\', '/');
                            string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
                        } else {
                            strVal = (String) objP.get(strKey);
                            if (strVal != null) {
                                string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
                            } else {
                                strVal = Settings().get(strKey);
                                if (strVal != null) {
                                    string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
                                } else {
                                    string = string.replaceAll(strPP, "?" + Matcher.quoteReplacement(strKey) + "?");
                                }
                            }
                        }
                    }
                    strNewString += string + "\n";
                }
            } catch (Exception e) { 
                // intentionally no error, wrong regexp ?
            }
        }
        strNewString = strNewString.replaceFirst("\n$", "");
        return strNewString;
    }

    public void DumpSettings() {
        final String conMethodName = CLASS_NAME + "::DumpSettings";
        for (final Object element : objSettings.entrySet()) {
            final Map.Entry mapItem = (Map.Entry) element;
            final String strMapKey = mapItem.getKey().toString();
            if (mapItem.getValue() != null) {
                String strTemp = mapItem.getValue().toString();
                if ("ftp_password".equals(strMapKey)) {
                    strTemp = "***";
                }
                if ("password".equalsIgnoreCase(strMapKey)) {
                    this.SignalDebug(conMethodName + ": Key = " + strMapKey + " --> " + "*****");
                } else {
                    this.SignalDebug(conMethodName + ": Key = " + strMapKey + " --> " + strTemp);
                }
            }
        }
    }

    public String getIndexedItem(final String pstrIndexedKey, final String pstrDescription, final String pstrDelimiter) {
        String strT = "";
        final JSOptionValueList optionValueList = new JSOptionValueList(this, pstrIndexedKey, pstrDescription, "", true);
        strT = optionValueList.concatenatedValue(pstrDelimiter);
        return strT;
    }

    public Object deepCopy(final Object pobj2Copy) throws Exception {
        ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
        ObjectOutputStream outStream = new ObjectOutputStream(bufOutStream);
        outStream.writeObject(pobj2Copy);
        outStream.close();
        byte[] buffer = bufOutStream.toByteArray();
        ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
        ObjectInputStream inStream = new ObjectInputStream(bufInStream);
        Object objDeepCopy = inStream.readObject();
        return objDeepCopy;
    }

    public String getOptionsAsCommandLine() {
        return populateOptions(IterationTypes.getCommandLine);
    }

    public String getOptionsAsQuotedCommandLine() {
        return populateOptions(IterationTypes.getQuotedCommandLine);
    }

    public String getOptionsAsKeyValuePairs() {
        return populateOptions(IterationTypes.getKeyValuePair);
    }

    private String populateOptions(final IterationTypes intIt) {
        return Iterate(intIt).toString();
    }

    public StringBuffer IterateAllDataElementsByAnnotation(final Class<?> objC, final Object objP, final IterationTypes enuIterate4What,
            StringBuffer pstrBuffer) {
        final String conMethodName = CLASS_NAME + "::IterateAllDataElementsByAnnotation";
        String strCommandLineOptions = "";
        if (objC == null) {
            throw new JobSchedulerException(conMethodName + ": objSegment is null, but must be not null");
        }
        Field objField = null;
        SOSOptionElement.gflgProcessHashMap = true;
        try {
            final Field objFields[] = objC.getFields();
            final StringBuffer strXML = new StringBuffer("");
            String strT = objC.getName();
            if (enuIterate4What == IterationTypes.createXML) {
                strXML.append("<" + strT + " id=" + XmlId.QuotedValue() + ">");
            }
            for (final Field objField2 : objFields) {
                objField = objField2;
                try {
                    if (objField.isAnnotationPresent(JSOptionDefinition.class)) {
                        final SOSOptionElement objDE = (SOSOptionElement) objField.get(objP);
                        if (objDE != null) {
                            if (enuIterate4What == IterationTypes.LoadValues) {
                                SOSOptionElement.gflgProcessHashMap = true;
                                objDE.loadValues();
                            }
                            if (enuIterate4What == IterationTypes.StoreValues) {
                                objDE.storeValues();
                            }
                            if (enuIterate4What == IterationTypes.setPrefix) {
                                objDE.setPrefix(strAlternativePrefix);
                            }
                            if (enuIterate4What == IterationTypes.setRecord) {
                                SOSOptionElement.gflgProcessHashMap = true;
                                objDE.gflgProcessHashMap = true;
                                objDE.MapValue();
                            }
                            if (enuIterate4What == IterationTypes.CheckMandatory) {
                                objDE.CheckMandatory();
                            }
                            if (enuIterate4What == IterationTypes.toOut) {
                                LOGGER.debug(objDE.toString());
                            }
                            if (enuIterate4What == IterationTypes.toString) {
                                pstrBuffer.append(objDE.toString() + "\n");
                            }
                            if (enuIterate4What == IterationTypes.DirtyToString) {
                                if (objDE.isDirty()) {
                                    pstrBuffer.append(objDE.DirtyToString() + "\n");
                                }
                            }
                            if (enuIterate4What == IterationTypes.createXML) {
                                strXML.append(objDE.toXml());
                            }
                            if (enuIterate4What == IterationTypes.setDefaultValues) {
                                final String strV = objDE.Value();
                                if (strV.isEmpty()) {
                                    objDE.Value(objDE.DefaultValue());
                                }
                            }
                            if (enuIterate4What == IterationTypes.clearAllValues) {
                                objDE.Value("");
                            }
                            if (enuIterate4What == IterationTypes.getCommandLine) {
                                pstrBuffer.append(objDE.toCommandLine());
                            }
                            if (enuIterate4What == IterationTypes.getQuotedCommandLine) {
                                pstrBuffer.append(objDE.toQuotedCommandLine());
                            }
                            if (enuIterate4What == IterationTypes.getKeyValuePair) {
                                strT = objDE.toKeyValuePair(strAlternativePrefix);
                                if (isNotEmpty(strT)) {
                                    pstrBuffer.append(strT + "\n");
                                }
                            }
                            IterateAllDataElementsByAnnotation(objDE.getClass(), objDE, enuIterate4What, pstrBuffer);
                        }
                    }
                } catch (final ClassCastException objException) {
                    
                } catch (final Exception objE) {
                    throw new RuntimeException(objE);
                }
            }
            if (enuIterate4What == IterationTypes.createXML) {
                strXML.append("</" + strT + ">");
                pstrBuffer = strXML;
            }
        } catch (final Exception objException) {
            throw new RuntimeException(objException);
        } finally {
            SOSOptionElement.gflgProcessHashMap = false;
        }
        return pstrBuffer;
    }

    public StringBuffer Iterate(final IterationTypes enuIterate4What) {
        StringBuffer strB = new StringBuffer();
        if (objParentClass != null) {
            strB = IterateAllDataElementsByAnnotation(objParentClass, this, enuIterate4What, strB);
        }
        return strB;
    }

    public Vector<String> getItems(final String pstrIndexedKey) {
        String strT;
        final Vector<String> objItems = new Vector<String>();
        strT = getItem(pstrIndexedKey);
        if (strT != null) {
            objItems.addElement(strT);
        }
        int i = 1;
        while ((strT = getItem(pstrIndexedKey + Integer.toString(i++))) != null) {
            objItems.addElement(strT);
        }
        return objItems;
    }

    public void putObject(final String pstrFileName) {
        try {
            JSFile objFile = new JSFile(pstrFileName);
            String encoded = getObjectAsString();
            objFile.Write(encoded);
            objFile.close();
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured writing object to file: " + e);
        }
    }

    public String getObjectAsString() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.close();
            String encoded = "";
            if (gflgUseBase64ForObject) {
                encoded = new String(Base64.encodeBase64(bos.toByteArray()));
            } else {
                encoded = new String(bos.toByteArray());
            }
            return encoded;
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured getting object as String: " + e);
        }
    }

    public static JSOptionsClass getObject(final String pstrFileName) {
        try {
            JSOptionsClass schedulerObject;
            String encoded = new JSTextFile(pstrFileName).File2String();
            if (encoded == null || encoded.isEmpty()) {
                return null;
            }
            byte[] serializedObject;
            if (gflgUseBase64ForObject) {
                serializedObject = Base64.decodeBase64(encoded.getBytes());
            } else {
                serializedObject = encoded.getBytes();
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(serializedObject);
            ObjectInputStream ois = new ObjectInputStream(bis);
            schedulerObject = (JSOptionsClass) ois.readObject();
            ois.close();
            return schedulerObject;
        } catch (Exception e) {
            throw new JobSchedulerException("Error occured reading object from file: " + e);
        }
    }

    public void LoadXML(final JSXMLFile pobjXMLFile) {
        this.LoadXML(pobjXMLFile, null);
    }

    public Properties LoadXML(final JSXMLFile pobjXMLFile, final String pstrXPathExpr) {
        DOMParser parser = new DOMParser();
        Properties objProp = new Properties();
        try {
            parser.setFeature("http://xml.org/sax/features/validation", false);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(String.format("SAXException ", pobjXMLFile.getAbsolutePath()));
        }
        try {
            parser.parse(pobjXMLFile.getAbsolutePath());
            Document document = parser.getDocument();
            if (isEmpty(pstrXPathExpr) == true) {
                traverse(document, objProp);
            }
            LoadProperties(objProp);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(String.format("Exception ", pobjXMLFile.getAbsolutePath()));
        }
        return objProp;
    }

    private void traverse(final Node node, final Properties objProp) {
        int type = node.getNodeType();
        LOGGER.debug("NodeType = " + type);
        String strNodeName = node.getNodeName();
        String strNodeValue = node.getNodeValue();
        LOGGER.debug("<" + strNodeName + ">" + strNodeValue);
        if (type == Node.ELEMENT_NODE) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node objN = children.item(i);
                if (objN.getNodeType() == Node.TEXT_NODE) {
                    strNodeValue = objN.getNodeValue();
                    if (isNotEmpty(strNodeValue))
                        objProp.put(strNodeName, objN.getNodeValue());
                    break;
                }
            }
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            traverse(children.item(i), objProp);
        }
    }

    public JSXMLFile toXMLFile(final String pstrXMLFileName) {
        JSXMLFile objXF;
        try {
            objXF = new JSXMLFile(pstrXMLFileName);
            objXF.writeXMLDeclaration();
            objXF.comment(String.format("Created by %1$s", CLASS_NAME));
            objXF.Write(this.toXML());
            objXF.close();
        } catch (Exception e) {
            throw new JobSchedulerException("Exception:", e);
        }
        return objXF;
    }

    public StringBuffer toXML() {
        return Iterate(IterationTypes.createXML);
    }

    public HashMap<String, String> DeletePrefix(final HashMap<String, String> phsmParameters, final String pstrPrefix) {
        String strTemp;
        HashMap<String, String> hsmNewMap = new HashMap<String, String>();
        if (phsmParameters != null) {
            for (final Object element : phsmParameters.entrySet()) {
                final Map.Entry<String, String> mapItem = (Map.Entry<String, String>) element;
                String strMapKey = mapItem.getKey().toString();
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue().toString();
                } else {
                    strTemp = null;
                }
                if (!strMapKey.startsWith(pstrPrefix)) {
                    hsmNewMap.put(strMapKey, strTemp);
                }
            }
            for (final Object element : phsmParameters.entrySet()) {
                final Map.Entry<String, String> mapItem = (Map.Entry<String, String>) element;
                String strMapKey = mapItem.getKey().toString();
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue().toString();
                } else {
                    strTemp = null;
                }
                if (strMapKey.startsWith(pstrPrefix)) {
                    strMapKey = strMapKey.replaceAll(pstrPrefix, "");
                    hsmNewMap.put(strMapKey, strTemp);
                    mapItem.setValue("\n");
                } else {
                    String strP = "/" + pstrPrefix;
                    if (strMapKey.contains(strP)) {
                        strMapKey = strMapKey.replace(strP, "/");
                        hsmNewMap.put(strMapKey, strTemp);
                        mapItem.setValue("\n");
                    }
                }
            }
        }
        return hsmNewMap;
    }

    public String getPid() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        String strA[] = pid.split("@");
        pid = strA[0];
        return pid;
    }

    public boolean isOption(final String optionName) {
        String optionNameInLowerCases = optionName.toLowerCase();
        Field[] fields = this.getClass().getFields();
        for (Field f : fields) {
            Annotation[] annotations = f.getDeclaredAnnotations();
            for (Annotation a : annotations) {
                if (a instanceof JSOptionDefinition) {
                    JSOptionDefinition od = (JSOptionDefinition) a;
                    if (od.name().toLowerCase().equals(optionNameInLowerCases)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Preferences getPreferenceStore() {
        if (objPreferenceStore == null) {
            objPreferenceStore = Preferences.userNodeForPackage(objParentClass);
        }
        return objPreferenceStore;
    }

    public void initializeOptionValues() {
        if (objParentClass != null) {
            getPreferenceStore();
            objClassName4PreferenceStore = objParentClass;
            StringBuffer strB = new StringBuffer();
            IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.LoadValues, strB);
        }
    }

    public void storeOptionValues() {
        if (objParentClass != null) {
            getPreferenceStore();
            objClassName4PreferenceStore = objParentClass;
            StringBuffer strB = new StringBuffer();
            IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.StoreValues, strB);
        }
    }

}
