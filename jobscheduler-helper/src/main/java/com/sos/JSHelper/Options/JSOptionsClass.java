package com.sos.JSHelper.Options;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;

import org.apache.commons.lang3.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Exceptions.ParametersMissingButRequiredException;
import com.sos.i18n.I18NBase;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "JSOptionsClass", description = "JSOptionsClass")
@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class JSOptionsClass extends I18NBase implements Serializable {

    private static final long serialVersionUID = 8497293387023797049L;
    private static final String CLASS_NAME = JSOptionsClass.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(JSOptionsClass.class);

    public Class<?> objParentClass = this.getClass();
    public HashMap<String, String> objSettings = null;

    private static Properties properties = new Properties();
    private final StringBuilder strBuffer = new StringBuilder("");
    private final String optionNamePrefix = "-";
    private final String filePathSeparator = File.separator;

    private HashMap<String, String> processedOptions = null;
    private Preferences preferenceStore = null;

    private String alternativePrefix = "";
    private String commandLineArgs[];
    private String currentNodeName = "";

    protected enum IterationTypes {
        setRecord(1), getRecord(2), toOut(3), createXML(4), setDefaultValues(5), clearAllValues(6), countSegmentFields(7), CheckMandatory(
                12), setPrefix(14), toString(13), getCommandLine(14), DirtyToString(15), getKeyValuePair(16), LoadValues(17), StoreValues(
                        18), getQuotedCommandLine(19);

        private int intType;

        IterationTypes(final int pintType) {
            intType = pintType;
        }

        public int getCode() {
            return intType;
        }
    }

    public JSOptionsClass() {
        try {
            new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));
        } catch (Exception e) {
            // handle exception
        }
    }

    public JSOptionsClass(final HashMap<String, String> settings) {
        this();
        this.setSettings(settings);
    }

    @JSOptionDefinition(name = "BaseDirectory", description = "A Base Directory for all relative FileNames used by SOSOptionFileName", key = "Base_Directory", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName baseDirectory = new SOSOptionFolderName(this, CLASS_NAME + ".Base_Directory",
            "A Base Directory for all relative FileNames used by SOSOptionFileName", "env:user.dir", "env:user.dir", false);

    @JSOptionDefinition(name = "DateFormatMask", description = "General Mask for date fomatting", key = "Date_Format_Mask", type = "SOSOptionString", mandatory = false)
    public SOSOptionString dateFormatMask = new SOSOptionString(this, CLASS_NAME + ".Date_Format_Mask", "General Mask for date fomatting",
            "yyyy-MM-dd", "yyyy-MM-dd", false);

    @JSOptionDefinition(name = "TimeFormatMask", description = "General Mask for time formatting", key = "Time_Format_Mask", type = "SOSOptionString", mandatory = false)
    public SOSOptionString timeFormatMask = new SOSOptionString(this, CLASS_NAME + ".Time_Format_Mask", "General Mask for time formatting",
            "HH:mm:ss", "HH:mm:ss", false);

    @JSOptionDefinition(name = "Scheduler_Hot_Folder", description = "Pathname to the JobScheduler live-folder", key = "Scheduler_Hot_Folder", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName schedulerHotFolder = new SOSOptionFolderName(this, CLASS_NAME + ".Scheduler_Hot_Folder",
            "Pathname to the JobScheduler live-folder", "${SCHEDULER_DATA}/config/live", "", true);

    @JSOptionDefinition(name = "Scheduler_Data", description = "Data Folder of JobScheduler Installation", key = "Scheduler_Data", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName schedulerData = new SOSOptionFolderName(this, CLASS_NAME + ".Scheduler_Data",
            "Data Folder of JobScheduler Installation", "env:SCHEDULER_DATA", "env:SCHEDULER_DATA", false);

    @JSOptionDefinition(name = "Scheduler_Home", description = "Home Root Folder of JobScheduler", key = "Scheduler_Home", type = "SOSOptionFileName", mandatory = true)
    public SOSOptionFolderName schedulerHome = new SOSOptionFolderName(this, CLASS_NAME + ".Scheduler_Home", "Home Root Folder of JobScheduler",
            "env:SCHEDULER_HOME", "env:SCHEDULER_HOME", false);

    @JSOptionDefinition(name = "Local_user", description = "I18N is for internationalization of Application", key = "Local_user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName userName = new SOSOptionUserName(this, CLASS_NAME + ".local_user", "Name of local user", System.getProperty("user.name"),
            System.getProperty("user.name"), true);

    @JSOptionDefinition(name = "Locale", description = "I18N is for internationalization of Application", key = "Locale", type = "SOSOptionString", mandatory = true)
    public SOSOptionLocale locale = new SOSOptionLocale(this, CLASS_NAME + ".Locale", "I18N is for internationalization of Application",
            "env:SOS_LOCALE", java.util.Locale.getDefault().toString(), true);

    @JSOptionDefinition(name = "CheckNotProcessedOptions", description = "If this Option is set to true, all not processed or recognized options "
            + "are reported as a warning", key = "CheckNotProcessedOptions", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkNotProcessedOptions = new SOSOptionBoolean(this, CLASS_NAME + ".CheckNotProcessedOptions",
            "If this Option is set to true, all not processed or recognized options are reported as a warning", "false", "false", false);

    @JSOptionDefinition(name = "XmlId", description = "This ist the ...", key = "XmlId", type = "SOSOptionString", mandatory = true)
    public SOSOptionString xmlId = new SOSOptionString(this, CLASS_NAME + ".XmlId", "This ist the ...", "root", "root", true);

    @JSOptionDefinition(name = "TestMode", value = "false", description = "Test Modus schalten ", key = "TestMode", type = "JSOptionBoolean", mandatory = false)
    public SOSOptionBoolean testMode = new SOSOptionBoolean(this, CLASS_NAME + ".TestMode", "Test Modus schalten ", "false", "false", false);

    @JSOptionDefinition(name = "Debug", value = "false", description = "Debug-Modus schalten (true/false)", key = "Debug", type = "JSOptionBoolean", mandatory = false)
    public SOSOptionBoolean debug = new SOSOptionBoolean(this, CLASS_NAME + ".Debug", "Debug-Modus schalten (true/false)", "false", "false", false);

    @JSOptionDefinition(name = "DebugLevel", value = "0", description = "DebugLevel", key = "DebugLevel", type = "JSOptionInteger", mandatory = false)
    public SOSOptionInteger debugLevel = new SOSOptionInteger(this, CLASS_NAME + ".DebugLevel", "DebugLevel", "0", "0", false);

    @JSOptionDefinition(name = "log_filename", description = "Name der Datei mit den Logging-Einträgen", key = "log_filename", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionLogFileName logFilename = new SOSOptionLogFileName(this, CLASS_NAME + ".log_filename", "Name der Datei mit den Logging-Einträgen",
            "stdout", "stdout", false);

    public SOSOptionLogFileName getLogFilename() {
        return logFilename;
    }

    public void setLogFilename(final SOSOptionLogFileName pstrValue) {
        logFilename = pstrValue;
    }

    @JSOptionDefinition(name = "log4jPropertyFileName", description = "Name of the LOG4J Property File", key = "log4j_Property_FileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName log4jPropertyFileName = new SOSOptionInFileName(this, CLASS_NAME + ".log4j_Property_FileName",
            "Name of the LOG4J Property File", "env:log4j.configuration", "./log4j.properties", false);

    @JSOptionDefinition(name = "ApplicationName", description = "Name of the Application", key = "ApplicationName", type = "SOSOptionString", mandatory = false)
    public SOSOptionString applicationName = new SOSOptionString(this, CLASS_NAME + ".ApplicationName", "Name of the Application",
            "env:SOSApplicationName", "env:SOSApplicationName", false);

    @JSOptionDefinition(name = "ApplicationDocuUrl", description = "The Url of the Documentation of this Application", key = "ApplicationDocuUrl", type = "SOSOptionUrl", mandatory = false)
    public SOSOptionUrl applicationDocuUrl = new SOSOptionUrl(this, CLASS_NAME + ".ApplicationDocuUrl",
            "The Url of the Documentation of this Application", "env:SOSApplicationDocuUrl", "env:SOSApplicationDocuUrl", false);

    @JSOptionDefinition(name = "AllowEmptyParameterList", description = "If true, an empty parameter list leads not into an error", key = "AllowEmptyParameterList", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean allowEmptyParameterList = new SOSOptionBoolean(this, CLASS_NAME + ".AllowEmptyParameterList",
            "If true, an empty parameter list leads not into an error", "true", "true", false);

    public String getPrefix() {
        return alternativePrefix;
    }

    public HashMap<String, String> settings() {
        if (objSettings == null) {
            objSettings = new HashMap<String, String>();
        }
        return objSettings;
    }

    public void setSettings(final HashMap<String, String> settings) {
        objSettings = settings;
        setAllCommonOptions(settings);
    }

    public String getItem(final String pstrKey) {
        String strTemp = "";
        String strKey = pstrKey;
        if (isEmpty(pstrKey)) {
            return strTemp;
        }
        if (processedOptions == null) {
            processedOptions = new HashMap<String, String>();
        }
        strKey = strKey.replaceAll("_", "");
        String strLSKey = "";
        if (!currentNodeName.isEmpty()) {
            strLSKey = currentNodeName + "/" + pstrKey.replaceAll("_", "");
            for (Map.Entry<String, String> mapItem : objSettings.entrySet()) {
                String strMapKey = mapItem.getKey();
                strMapKey = strMapKey.replaceAll("_", "");
                if (strLSKey.equalsIgnoreCase(strMapKey)) {
                    if (mapItem.getValue() != null) {
                        strTemp = mapItem.getValue();
                    } else {
                        strTemp = null;
                    }
                    processedOptions.put(strMapKey, strTemp);
                    return strTemp;
                }
            }
        }
        for (Map.Entry<String, String> mapItem : objSettings.entrySet()) {
            String strMapKey = mapItem.getKey();
            String lstrMapKey = strMapKey.replaceAll("_", "");
            if (strKey.equalsIgnoreCase(lstrMapKey)) {
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue();
                } else {
                    strTemp = null;
                }
                processedOptions.put(strMapKey, strTemp);
                return strTemp;
            }
        }
        int i = strKey.indexOf('.');
        if (i > 0) {
            strLSKey = "";
            strKey = strKey.substring(++i);
            if (!currentNodeName.isEmpty()) {
                strLSKey = currentNodeName + "/" + strKey;
                for (Map.Entry<String, String> mapItem : objSettings.entrySet()) {
                    String strMapKey = mapItem.getKey();
                    String lstrMapKey = strMapKey.replaceAll("_", "");
                    if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
                        if (mapItem.getValue() != null) {
                            strTemp = mapItem.getValue();
                        } else {
                            strTemp = null;
                        }
                        processedOptions.put(strMapKey, strTemp);
                        return strTemp;
                    }
                }
            }
            for (Map.Entry<String, String> mapItem : objSettings.entrySet()) {
                String strMapKey = mapItem.getKey();
                String lstrMapKey = strMapKey.replaceAll("_", "");
                if (!strLSKey.isEmpty() && strLSKey.equalsIgnoreCase(lstrMapKey)) {
                    if (mapItem.getValue() != null) {
                        strTemp = mapItem.getValue();
                    } else {
                        strTemp = null;
                    }
                    processedOptions.put(strMapKey, strTemp);
                    return strTemp;
                }
                if (strKey.equalsIgnoreCase(lstrMapKey)) {
                    if (mapItem.getValue() != null) {
                        strTemp = mapItem.getValue().toString();
                    } else {
                        strTemp = null;
                    }
                    processedOptions.put(strMapKey, strTemp);
                    return strTemp;
                }
            }
        }
        if (strTemp == null || strTemp.isEmpty()) {
            strTemp = null;
        }
        return strTemp;
    }

    public boolean checkNotProcessedOptions() {
        boolean flgIsOK = true;
        // int intNumberOfNotProcessedOptions = 0;
        if (objSettings != null) {
            for (Map.Entry<String, String> mapItem : objSettings.entrySet()) {
                String strMapKey = mapItem.getKey();
                String strT = processedOptions.get(strMapKey);
                if (strT == null) {
                    String strValue = null;
                    if (mapItem.getValue() != null) {
                        strValue = mapItem.getValue();
                    } else {
                        strValue = null;
                    }
                    LOGGER.warn(String.format("SOSOPT-W-001: Option '%1$s' with value '%2$s' is unknown and not processed", strMapKey, strValue));
                    flgIsOK = false;
                    // intNumberOfNotProcessedOptions++;
                }
            }
        }
        return flgIsOK;
    }

    public HashMap<String, String> getProcessedOptions() {
        if (processedOptions == null) {
            processedOptions = new HashMap<String, String>();
        }
        return processedOptions;
    }

    public void addProcessedOptions(final HashMap<String, String> map) {
        this.getProcessedOptions();
        processedOptions.putAll(map);
    }

    public String getItem(final String key, final String defaultValue) {
        String value = this.getItem(key);
        if (isEmpty(value)) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public boolean string2Bool(final String pstrVal) {
        boolean flgT = false;
        if (isNotEmpty(pstrVal) && ("1".equals(pstrVal) || "y".equalsIgnoreCase(pstrVal) || "yes".equalsIgnoreCase(pstrVal) || "j".equalsIgnoreCase(
                pstrVal) || "on".equalsIgnoreCase(pstrVal) || "true".equalsIgnoreCase(pstrVal) || "wahr".equalsIgnoreCase(pstrVal))) {
            flgT = true;
        }
        return flgT;
    }

    private void toOut(final String msg) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            String strT = msg;
            if (!strT.contains(CLASS_NAME)) {
                strT += getAllOptionsAsString();
            }
            LOGGER.debug(strT);
        }
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
        try {
            return getAllOptionsAsString();
        } catch (final Exception e) {
            throw new JobSchedulerException("toString failed", e);
        }
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

    private String getAllOptionsAsString(final IterationTypes penuIterationType) {
        StringBuilder sb = new StringBuilder();
        if (objParentClass != null) {
            sb.append(iterateAllDataElementsByAnnotation(objParentClass, this, penuIterationType, new StringBuilder("")));
        }
        return sb.toString();
    }

    private String getAllOptionsAsString() {
        return getAllOptionsAsString(IterationTypes.toString);
    }

    private void setAllCommonOptions(final HashMap<String, String> JSSettings) {
        objSettings = JSSettings;
        if (objParentClass != null) {
            iterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.countSegmentFields, strBuffer);
            iterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setRecord, strBuffer);
        }
    }

    public void checkMandatory() throws Exception {
        if (objParentClass != null) {
            iterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.CheckMandatory, strBuffer);
        }
    }

    public String substituteVariables(final String pstrValue) {
        String strT = pstrValue;
        if (strT == null) {
            return null;
        }

        strT = strT.replace("//", "/");
        return strT;
    }

    protected String[] splitString(final String pstrStr) {
        if (pstrStr == null) {
            return null;
        }
        return pstrStr.trim().split("[;|,]");
    }

    protected void checkNull(final String pstrMethodName, final String pstrTitel, final String pstrOptionName, final String pstrOptionValue)
            throws Exception {
        if (isEmpty(pstrOptionValue)) {
            this.signalError(pstrOptionName + " is mandatory, must be not null");
        }
    }

    public void commandLineArgs(final String pstrArgs) {
        StrTokenizer objT = new StrTokenizer(pstrArgs);
        String[] strA = objT.getTokenArray();
        commandLineArgs(strA);
    }

    public void commandLineArgs(final String[] args) {
        final String conMethodName = CLASS_NAME + "::CommandLineArgs ";
        if (allowEmptyParameterList.isFalse() && args.length <= 0) {
            throw new ParametersMissingButRequiredException(applicationName.getValue(), applicationDocuUrl.getValue());
        }
        commandLineArgs = args;
        boolean flgOption = true;
        String strOptionName = null;
        String strOptionValue = null;
        this.settings();
        final int l = optionNamePrefix.length();
        for (final String strCommandLineArg : commandLineArgs) {
            if (flgOption) {
                if (strCommandLineArg.length() < l) {
                    continue;
                }
                if (strCommandLineArg.substring(0, l).equalsIgnoreCase(optionNamePrefix)) {
                    strOptionName = strCommandLineArg.substring(l);
                    flgOption = false;
                    // name and value separated by an equalsign?
                    int intESPos = strOptionName.indexOf("=");
                    if (intESPos > 0) {
                        strOptionValue = strOptionName.substring(intESPos + 1);
                        strOptionValue = stripQuotes(strOptionValue);
                        strOptionName = strOptionName.substring(0, intESPos);
                        objSettings.put(strOptionName, strOptionValue);
                        if ("password".equalsIgnoreCase(strOptionName) || "proxy_password".equalsIgnoreCase(strOptionName)) {
                            this.signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, "*****"));
                        } else {
                            this.signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, strOptionValue));
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
                        this.signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, "*****"));
                    } else {
                        this.signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, strOptionValue));
                    }
                    strOptionName = null;
                }
            }
        }
        final String strPropertyFileName = this.getItem("PropertyFileName", "");
        if (!strPropertyFileName.isEmpty()) {
            loadProperties(strPropertyFileName);
            strOptionName = null;
            flgOption = true;
            for (final String strCommandLineArg : commandLineArgs) {
                if (flgOption) {
                    if (strCommandLineArg.substring(0, l).equalsIgnoreCase(optionNamePrefix)) {
                        strOptionName = strCommandLineArg.substring(l);
                        flgOption = false;
                    }
                } else {
                    if (strOptionName != null) {
                        strOptionValue = strCommandLineArg;
                        flgOption = true;
                        objSettings.put(strOptionName, strOptionValue);
                        if ("password".equalsIgnoreCase(strOptionName) || "proxy_password".equalsIgnoreCase(strOptionName)) {
                            this.signalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName, "*****"));
                        } else {
                            this.signalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName,
                                    strOptionValue));
                        }
                        strOptionName = null;
                    }
                }
            }
            message(conMethodName + ": Property-File loaded. " + strPropertyFileName);
        }
        dumpSettings();
        setAllOptions(objSettings);
    }

    public String[] commandLineArgs() {
        return commandLineArgs;
    }

    private void loadProperties(final String fileName) {
        final String method = CLASS_NAME + "::LoadProperties ";
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(fileName));
            message(method + ": PropertyFile read. Name '" + fileName + "'.");
            this.settings();
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                if (property.getValue() != null) {
                    final String value = property.getValue().toString();
                    if (value != null && !value.isEmpty() && !".".equalsIgnoreCase(value)) {
                        objSettings.put(property.getKey().toString(), value);
                    }
                }
            }
            message(method + ": Property-File loaded");
            setAllOptions(objSettings);
            setAllCommonOptions(objSettings);
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void loadProperties(final Properties properties) {
        this.settings();
        for (Map.Entry<Object, Object> mapItem : properties.entrySet()) {
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

    public void setAllOptions(final HashMap<String, String> settings, final String prefix) throws Exception {
        if (alternativePrefix.isEmpty()) {
            alternativePrefix = prefix;
            if (objParentClass != null) {
                iterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setPrefix, strBuffer);
            }
        }
        this.setAllOptions(settings);
    }

    public void setAllOptions(final HashMap<String, String> val) {
        setAllCommonOptions(val);
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public JSOptionsClass setCurrentNodeName(final String val) {
        currentNodeName = val;
        return this;
    }

    private String getOptionByName(final String optionName) {
        String value = null;
        Class<?> clazz = this.getClass();
        value = getOptionValue(clazz, optionName);
        if (value == null) {
            clazz = objParentClass.getClass();
            value = getOptionValue(clazz, optionName);
        }
        return value;
    }

    private String getOptionValue(final Class<?> clazz, final String optionName) {
        String value = null;
        Field field = null;
        try {
            field = clazz.getField(optionName);
            Object obj = field.get(this);
            if (obj instanceof String) {
                value = (String) field.get(this);
            } else {
                if (obj instanceof SOSOptionElement) {
                    SOSOptionElement el = (SOSOptionElement) obj;
                    value = el.getValue();
                }
            }
        } catch (final NoSuchFieldException objException) {
            Method method;
            try {
                method = clazz.getMethod(optionName);
                value = (String) method.invoke(this);
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
            LOGGER.error(exception.toString(), exception);
        }
        return value;
    }

    public Properties getTextProperties() {
        if (properties == null) {
            properties = new Properties();
        }
        return properties;
    }

    public String replaceVars(final String pstrReplaceIn) {
        getTextProperties();
        try {
            properties.put("date", SOSOptionTime.getCurrentDateAsString(dateFormatMask.getValue()));
            properties.put("time", SOSOptionTime.getCurrentTimeAsString(timeFormatMask.getValue()));
            properties.put("local_user", System.getProperty("user.name"));
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            properties.put("localhost", localMachine.getHostName());
            properties.put("local_host_ip", localMachine.getHostAddress());
            properties.put("tempdir", System.getProperty("java.io.tmpdir") + filePathSeparator);
            properties.put("temp", System.getProperty("java.io.tmpdir") + filePathSeparator);
            properties.put("uuid", SOSOptionRegExp.getUUID());
            properties.put("timestamp", SOSOptionRegExp.getUnixTimeStamp());
            properties.put("sqltimestamp", SOSOptionRegExp.getSqlTimeStamp());
            properties.put("weekofyear", SOSOptionTime.getCurrentDateAsString("yyyyw"));
            properties.put("currentweek", SOSOptionTime.getCurrentDateAsString("w"));
        } catch (Exception uhe) {
            //
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
                        String strPP = "(\\$|%)\\{" + Matcher.quoteReplacement(strKey) + "\\}";
                        strVal = this.getOptionByName(strKey);
                        if (isNotNull(strVal)) {
                            strVal = strVal.replace('\\', '/');
                            string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
                        } else {
                            strVal = (String) properties.get(strKey);
                            if (strVal != null) {
                                string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
                            } else {
                                strVal = settings().get(strKey);
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

    private void dumpSettings() {
        final String conMethodName = CLASS_NAME + "::DumpSettings";
        for (Map.Entry<String, String> mapItem : objSettings.entrySet()) {
            final String strMapKey = mapItem.getKey();
            if (mapItem.getValue() != null) {
                String strTemp = mapItem.getValue();
                if ("ftp_password".equals(strMapKey)) {
                    strTemp = "***";
                }
                if ("password".equalsIgnoreCase(strMapKey)) {
                    this.signalDebug(conMethodName + ": Key = " + strMapKey + " --> " + "*****");
                } else {
                    this.signalDebug(conMethodName + ": Key = " + strMapKey + " --> " + strTemp);
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

    @SuppressWarnings("unused")
    private Object deepCopy(final Object pobj2Copy) throws Exception {
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
        return iterate(IterationTypes.getCommandLine).toString();
    }

    public String getOptionsAsQuotedCommandLine() {
        return iterate(IterationTypes.getQuotedCommandLine).toString();
    }

    public String getOptionsAsKeyValuePairs() {
        return iterate(IterationTypes.getKeyValuePair).toString();
    }

    private StringBuilder iterateAllDataElementsByAnnotation(final Class<?> clazz, final Object element, final IterationTypes enuIterate4What,
            StringBuilder sb) {
        if (clazz == null) {
            throw new JobSchedulerException(String.format("[%s::IterateAllDataElementsByAnnotation]clazz is null", CLASS_NAME));
        }

        SOSOptionElement.gflgProcessHashMap = true;
        try {
            final Field fields[] = clazz.getFields();
            final StringBuilder xml = new StringBuilder("");
            String clazzName = clazz.getName();
            if (enuIterate4What.equals(IterationTypes.createXML)) {
                xml.append("<" + clazzName + " id=" + xmlId.getQuotedValue() + ">");
            }
            for (final Field field : fields) {

                try {
                    if (field.isAnnotationPresent(JSOptionDefinition.class)) {

                        final SOSOptionElement el = (SOSOptionElement) field.get(element);
                        if (el != null) {
                            if (enuIterate4What.equals(IterationTypes.LoadValues)) {
                                SOSOptionElement.gflgProcessHashMap = true;
                                el.loadValues();
                            } else if (enuIterate4What.equals(IterationTypes.StoreValues)) {
                                el.storeValues();
                            } else if (enuIterate4What.equals(IterationTypes.setPrefix)) {
                                el.setPrefix(alternativePrefix);
                            } else if (enuIterate4What.equals(IterationTypes.setRecord)) {
                                SOSOptionElement.gflgProcessHashMap = true;
                                // objDE.gflgProcessHashMap = true;
                                el.mapValue();
                            } else if (enuIterate4What.equals(IterationTypes.CheckMandatory)) {
                                el.checkMandatory();
                            } else if (enuIterate4What.equals(IterationTypes.toOut)) {
                                LOGGER.debug(el.toString());
                            } else if (enuIterate4What.equals(IterationTypes.toString)) {
                                sb.append(el.toString() + "\n");
                            } else if (enuIterate4What.equals(IterationTypes.DirtyToString) && el.isDirty()) {
                                sb.append(el.getDirtyToString() + "\n");
                            } else if (enuIterate4What.equals(IterationTypes.createXML)) {
                                //xml.append(el.toXml());
                            } else if (enuIterate4What.equals(IterationTypes.setDefaultValues)) {
                                final String strV = el.getValue();
                                if (strV.isEmpty()) {
                                    el.setValue(el.getDefaultValue());
                                }
                            } else if (enuIterate4What.equals(IterationTypes.clearAllValues)) {
                                el.setValue("");
                            } else if (enuIterate4What.equals(IterationTypes.getCommandLine)) {
                                sb.append(el.toCommandLine());
                            } else if (enuIterate4What.equals(IterationTypes.getQuotedCommandLine)) {
                                sb.append(el.toQuotedCommandLine());
                            } else if (enuIterate4What.equals(IterationTypes.getKeyValuePair)) {
                                clazzName = el.toKeyValuePair(alternativePrefix);
                                if (isNotEmpty(clazzName)) {
                                    sb.append(clazzName + "\n");
                                }
                            }
                            iterateAllDataElementsByAnnotation(el.getClass(), el, enuIterate4What, sb);
                        }
                    }
                } catch (final ClassCastException objException) {
                    //
                } catch (final Exception objE) {
                    throw new RuntimeException(objE);
                }
            }
            if (enuIterate4What.equals(IterationTypes.createXML)) {
                xml.append("</" + clazzName + ">");
                sb = xml;
            }
        } catch (final Exception x) {
            throw new RuntimeException(x);
        } finally {
            SOSOptionElement.gflgProcessHashMap = false;
        }
        return sb;
    }

    private StringBuilder iterate(final IterationTypes enuIterate4What) {
        StringBuilder sb = new StringBuilder();
        if (objParentClass != null) {
            sb = iterateAllDataElementsByAnnotation(objParentClass, this, enuIterate4What, sb);
        }
        return sb;
    }

    public HashMap<String, String> deletePrefix(final HashMap<String, String> phsmParameters, final String pstrPrefix) {
        String strTemp;
        HashMap<String, String> hsmNewMap = new HashMap<String, String>();
        if (phsmParameters != null) {
            for (Map.Entry<String, String> mapItem : phsmParameters.entrySet()) {
                String strMapKey = mapItem.getKey();
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue();
                } else {
                    strTemp = null;
                }
                if (!strMapKey.startsWith(pstrPrefix)) {
                    hsmNewMap.put(strMapKey, strTemp);
                }
            }
            for (Map.Entry<String, String> mapItem : phsmParameters.entrySet()) {
                String strMapKey = mapItem.getKey();
                if (mapItem.getValue() != null) {
                    strTemp = mapItem.getValue();
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

    public Preferences getPreferenceStore() {
        if (preferenceStore == null) {
            preferenceStore = Preferences.userNodeForPackage(objParentClass);
        }
        return preferenceStore;
    }

    protected void setIfNotDirty(final SOSOptionElement objOption, final String pstrValue) {
        if (objOption.isNotDirty() && isNotEmpty(pstrValue)) {
            LOGGER.trace("setValue = " + pstrValue);
            objOption.setValue(pstrValue);
        }
    }

}