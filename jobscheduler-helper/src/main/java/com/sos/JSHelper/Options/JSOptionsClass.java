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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
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

    public Class<?> currentClass = this.getClass();

    private static Properties properties = new Properties();
    private HashMap<String, String> settings = null;
    private final StringBuilder buffer = new StringBuilder("");
    private final String optionNamePrefix = "-";
    private final String filePathSeparator = File.separator;

    private HashMap<String, String> processedOptions = null;
    private Preferences preferenceStore = null;

    private String alternativePrefix = "";
    private String commandLineArgs[];
    private String currentNodeName = "";

    private enum IterationTypes {
        setRecord, getRecord, toOut, setDefaultValues, clearAllValues, CheckMandatory, setPrefix, toString, getCommandLine, DirtyToString, getKeyValuePair, LoadValues, StoreValues, getQuotedCommandLine;

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
        this.setAllOptions(settings);
    }

    private void loadProperties(final String fileName) {
        final String method = CLASS_NAME + "::LoadProperties ";
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream(fileName));
            message(method + ": PropertyFile read. Name '" + fileName + "'.");
            getSettings();
            for (Map.Entry<Object, Object> property : properties.entrySet()) {
                if (property.getValue() != null) {
                    final String value = property.getValue().toString();
                    if (value != null && !value.isEmpty() && !".".equalsIgnoreCase(value)) {
                        settings.put(property.getKey().toString(), value);
                    }
                }
            }
            message(method + ": Property-File loaded");
            setAllCommonOptions(settings);
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void loadProperties(final Properties properties) {
        getSettings();
        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            final String key = property.getKey().toString();
            if (property.getValue() != null) {
                final String value = property.getValue().toString();
                LOGGER.debug("Property " + key + " = " + value);
                if (value != null && !value.isEmpty() && !".".equalsIgnoreCase(value)) {
                    settings.put(key, value);
                }
            }
        }
        setAllCommonOptions(settings);
    }

    public HashMap<String, String> getSettings() {
        if (settings == null) {
            settings = new HashMap<String, String>();
        }
        return settings;
    }

    public void setAllOptions(final HashMap<String, String> params, final String prefix) throws Exception {
        settings = params;
        if (alternativePrefix.isEmpty()) {
            alternativePrefix = prefix;
        }
        setAllCommonOptions(settings, alternativePrefix);
    }

    public void setAllOptions(final HashMap<String, String> params) {
        settings = params;
        setAllCommonOptions(settings);
    }

    private void setAllCommonOptions(final HashMap<String, String> params) {
        setAllCommonOptions(params, null);
    }

    private void setAllCommonOptions(final HashMap<String, String> params, String prefix) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(String.format("[setAllCommonOptions][%s]%s", getClass().getSimpleName(), prefix == null ? "" : prefix));
        }
        settings = params;
        if (currentClass != null) {
            iterateAllDataElementsByAnnotation(currentClass, this, IterationTypes.setRecord, buffer, prefix);
        }
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
            for (Map.Entry<String, String> mapItem : settings.entrySet()) {
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
        for (Map.Entry<String, String> mapItem : settings.entrySet()) {
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
                for (Map.Entry<String, String> mapItem : settings.entrySet()) {
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
            for (Map.Entry<String, String> mapItem : settings.entrySet()) {
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
        boolean result = true;
        // int intNumberOfNotProcessedOptions = 0;
        if (settings != null) {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String key = entry.getKey();
                if (processedOptions.get(key) == null) {
                    String value = entry.getValue();
                    LOGGER.warn(String.format("SOSOPT-W-001: Option '%1$s' with value '%2$s' is unknown and not processed", key, value));
                    result = false;
                    // intNumberOfNotProcessedOptions++;
                }
            }
        }
        return result;
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
    public boolean string2Bool(final String val) {
        if (isNotEmpty(val) && ("1".equals(val) || "y".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val) || "j".equalsIgnoreCase(val) || "on"
                .equalsIgnoreCase(val) || "true".equalsIgnoreCase(val) || "wahr".equalsIgnoreCase(val))) {
            return true;
        }
        return false;
    }

    private void toOut(final String msg) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            if (msg.contains(CLASS_NAME)) {
                LOGGER.debug(msg);
            } else {
                LOGGER.debug(msg + getAllOptionsAsString());
            }
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
        try {
            return new StringBuilder("\n").append(getAllOptionsAsString(IterationTypes.DirtyToString).trim()).toString();
        } catch (final Exception e) {
            throw new JobSchedulerException("dirtyString failed", e);
        }
    }

    private String getAllOptionsAsString(final IterationTypes type) {
        StringBuilder sb = new StringBuilder();
        if (currentClass != null) {
            sb.append(iterateAllDataElementsByAnnotation(currentClass, this, type, new StringBuilder(""), null));
        }
        return sb.toString();
    }

    private String getAllOptionsAsString() {
        return getAllOptionsAsString(IterationTypes.toString);
    }

    public void checkMandatory() throws Exception {
        if (currentClass != null) {
            // if (LOGGER.isDebugEnabled()) {
            // LOGGER.debug(String.format("[checkMandatory]%s", getClass().getSimpleName()));
            // }

            iterateAllDataElementsByAnnotation(currentClass, this, IterationTypes.CheckMandatory, buffer, null);
        }
    }

    public String substituteVariables(final String value) {
        if (value == null) {
            return null;
        }
        return value.replace("//", "/");
    }

    protected String[] splitString(final String value) {
        if (value == null) {
            return null;
        }
        return value.trim().split("[;|,]");
    }

    protected void checkNull(final String method, final String titel, final String name, final String value) throws Exception {
        if (isEmpty(value)) {
            signalError(name + " is mandatory, must be not null");
        }
    }

    public void commandLineArgs(final String args) {
        commandLineArgs(new StrTokenizer(args).getTokenArray());
    }

    public void commandLineArgs(final String[] args) {
        final String method = CLASS_NAME + "::CommandLineArgs ";
        if (args.length <= 0) {
            throw new JobSchedulerException("missing parameters");
        }
        commandLineArgs = args;
        boolean isOption = true;
        String name = null;
        String value = null;
        getSettings();
        final int l = optionNamePrefix.length();
        for (final String arg : commandLineArgs) {
            if (isOption) {
                if (arg.length() < l) {
                    continue;
                }
                if (arg.substring(0, l).equalsIgnoreCase(optionNamePrefix)) {
                    name = arg.substring(l);
                    isOption = false;
                    // name and value separated by an equalsign?
                    int pos = name.indexOf("=");
                    if (pos > 0) {
                        value = name.substring(pos + 1);
                        value = stripQuotes(value);
                        name = name.substring(0, pos);
                        settings.put(name, value);
                        if ("password".equalsIgnoreCase(name) || "proxy_password".equalsIgnoreCase(name)) {
                            signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", method, name, "*****"));
                        } else {
                            signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", method, name, value));
                        }
                        isOption = true;
                    }
                }
            } else {
                if (name != null) {
                    value = arg;
                    isOption = true;
                    settings.put(name, value);
                    if ("password".equalsIgnoreCase(name) || "proxy_password".equalsIgnoreCase(name)) {
                        signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", method, name, "*****"));
                    } else {
                        signalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", method, name, value));
                    }
                    name = null;
                }
            }
        }
        final String propertyFileName = this.getItem("PropertyFileName", "");
        if (!propertyFileName.isEmpty()) {
            loadProperties(propertyFileName);
            name = null;
            isOption = true;
            for (final String arg : commandLineArgs) {
                if (isOption) {
                    if (arg.substring(0, l).equalsIgnoreCase(optionNamePrefix)) {
                        name = arg.substring(l);
                        isOption = false;
                    }
                } else {
                    if (name != null) {
                        value = arg;
                        isOption = true;
                        settings.put(name, value);
                        if ("password".equalsIgnoreCase(name) || "proxy_password".equalsIgnoreCase(name)) {
                            signalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", method, name, "*****"));
                        } else {
                            signalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", method, name, value));
                        }
                        name = null;
                    }
                }
            }
            message(method + ": Property-File loaded. " + propertyFileName);
        }
        dumpSettings();
        setAllOptions(settings);
    }

    public String[] commandLineArgs() {
        return commandLineArgs;
    }

    public String getCurrentNodeName() {
        return currentNodeName;
    }

    public JSOptionsClass setCurrentNodeName(final String val) {
        currentNodeName = val;
        return this;
    }

    private String getOptionByName(final String name) {
        String value = null;
        Class<?> clazz = this.getClass();
        value = getOptionValue(clazz, name);
        if (value == null) {
            clazz = currentClass.getClass();
            value = getOptionValue(clazz, name);
        }
        return value;
    }

    private String getOptionValue(final Class<?> clazz, final String name) {
        String value = null;
        Field field = null;
        try {
            field = clazz.getField(name);
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
                method = clazz.getMethod(name);
                method.setAccessible(true);
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

    public String replaceVars(final String value) {
        getTextProperties();
        try {
            properties.put("date", SOSOptionTime.getCurrentDateAsString(dateFormatMask.getValue()));
            properties.put("time", SOSOptionTime.getCurrentTimeAsString(timeFormatMask.getValue()));
            properties.put("local_user", System.getProperty("user.name"));
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            properties.put("localhost", localHost.getHostName());
            properties.put("local_host_ip", localHost.getHostAddress());
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
        String paramNameEnclosedInPercentSigns = "^.*(\\$|%)\\{([^%\\}]+)\\}.*$";

        String key = "";
        String val = "";
        StringBuilder result = new StringBuilder();
        if (isNotNull(value)) {
            try {
                String[] arr = value.split("\\n");
                for (String string : arr) {
                    while (string.matches(paramNameEnclosedInPercentSigns)) {
                        key = string.replaceFirst(paramNameEnclosedInPercentSigns, "$2");
                        if ("uuid".equalsIgnoreCase(key)) {
                            continue;
                        }
                        String var = "(\\$|%)\\{" + Matcher.quoteReplacement(key) + "\\}";
                        val = this.getOptionByName(key);
                        if (isNotNull(val)) {
                            val = val.replace('\\', '/');
                            string = string.replaceAll(var, Matcher.quoteReplacement(val));
                        } else {
                            val = (String) properties.get(key);
                            if (val != null) {
                                string = string.replaceAll(var, Matcher.quoteReplacement(val));
                            } else {
                                val = getSettings().get(key);
                                if (val != null) {
                                    string = string.replaceAll(var, Matcher.quoteReplacement(val));
                                } else {
                                    string = string.replaceAll(var, "?" + Matcher.quoteReplacement(key) + "?");
                                }
                            }
                        }
                    }
                    result.append(string).append("\n");
                }
            } catch (Exception e) {
                // intentionally no error, wrong regexp ?
            }
        }
        return result.toString().replaceFirst("\n$", "");
    }

    private void dumpSettings() {
        final String method = CLASS_NAME + "::DumpSettings";
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            final String key = entry.getKey();
            if (entry.getValue() != null) {
                String value = entry.getValue();
                if ("ftp_password".equals(key)) {
                    value = "***";
                } else if ("password".equalsIgnoreCase(key)) {
                    signalDebug(method + ": Key = " + key + " --> " + "*****");
                } else {
                    signalDebug(method + ": Key = " + key + " --> " + value);
                }
            }
        }
    }

    public String getIndexedItem(final String key, final String description, final String delimiter) {
        final JSOptionValueList list = new JSOptionValueList(this, key, description, "", true);
        return list.concatenatedValue(delimiter);
    }

    @SuppressWarnings("unused")
    private Object deepCopy(final Object obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        byte[] buffer = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
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
            StringBuilder sb, String prefix) {
        if (clazz == null) {
            throw new JobSchedulerException(String.format("[%s::IterateAllDataElementsByAnnotation]clazz is null", CLASS_NAME));
        }

        SOSOptionElement.gflgProcessHashMap = true;
        try {

            String clazzName = clazz.getName();
            List<Field> fields = Arrays.stream(clazz.getFields()).filter(f -> f.isAnnotationPresent(JSOptionDefinition.class)).collect(Collectors
                    .toList());

            // if (LOGGER.isTraceEnabled()) {
            // LOGGER.trace(String.format("[iterateAllDataElementsByAnnotation][%s]%s annotated fields", clazz.getSimpleName(), fields.size()));
            // }

            for (final Field field : fields) {
                try {
                    final SOSOptionElement el = (SOSOptionElement) field.get(element);
                    if (el != null) {
                        if (enuIterate4What.equals(IterationTypes.setRecord)) {
                            if (prefix != null) {
                                el.setPrefix(prefix);
                            }
                            SOSOptionElement.gflgProcessHashMap = true;
                            // objDE.gflgProcessHashMap = true;
                            el.mapValue();
                        } else if (enuIterate4What.equals(IterationTypes.CheckMandatory)) {
                            el.checkMandatory();
                        } else if (enuIterate4What.equals(IterationTypes.setPrefix)) {
                            el.setPrefix(alternativePrefix);
                        } else if (enuIterate4What.equals(IterationTypes.setDefaultValues)) {
                            final String val = el.getValue();
                            if (val.isEmpty()) {
                                el.setValue(el.getDefaultValue());
                            }
                        } else if (enuIterate4What.equals(IterationTypes.LoadValues)) {
                            SOSOptionElement.gflgProcessHashMap = true;
                            el.loadValues();
                        } else if (enuIterate4What.equals(IterationTypes.StoreValues)) {
                            el.storeValues();
                        } else if (enuIterate4What.equals(IterationTypes.toOut)) {
                            LOGGER.debug(el.toString());
                        } else if (enuIterate4What.equals(IterationTypes.toString)) {
                            sb.append(el.toString() + "\n");
                        } else if (enuIterate4What.equals(IterationTypes.DirtyToString) && el.isDirty()) {
                            sb.append(el.getDirtyToString() + "\n");

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
                        // recursion disabled due an option element not have the JSOptionDefinition annotated fields
                        // iterateAllDataElementsByAnnotation(el.getClass(), el, enuIterate4What, sb, prefix);
                    }

                } catch (final ClassCastException e) {
                    //
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
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
        if (currentClass != null) {
            sb = iterateAllDataElementsByAnnotation(currentClass, this, enuIterate4What, sb, null);
        }
        return sb;
    }

    public HashMap<String, String> deletePrefix(final HashMap<String, String> params, final String prefix) {

        HashMap<String, String> result = new HashMap<String, String>();
        if (params != null) {
            String longPrefix = "/" + prefix;
            for (Map.Entry<String, String> param : params.entrySet()) {
                String key = param.getKey();
                String value = param.getValue();
                if (key.startsWith(prefix)) {
                    key = key.replaceAll(prefix, "");
                    result.put(key, value);
                    param.setValue("\n");
                } else {
                    if (key.contains(longPrefix)) {
                        key = key.replace(longPrefix, "/");
                        result.put(key, value);
                        param.setValue("\n");
                    } else {
                        result.put(key, value);
                    }
                }
            }
        }
        return result;
    }

    public Preferences getPreferenceStore() {
        if (preferenceStore == null) {
            preferenceStore = Preferences.userNodeForPackage(currentClass);
        }
        return preferenceStore;
    }

    protected void setIfNotDirty(final SOSOptionElement el, final String value) {
        if (el.isNotDirty() && isNotEmpty(value)) {
            LOGGER.trace("setValue = " + value);
            el.setValue(value);
        }
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

    @JSOptionDefinition(name = "log_filename", description = "Name der Datei mit den Logging-Einträgen", key = "log_filename", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionLogFileName logFilename = new SOSOptionLogFileName(this, CLASS_NAME + ".log_filename", "Name der Datei mit den Logging-Einträgen",
            "stdout", "stdout", false);

    public SOSOptionLogFileName getLogFilename() {
        return logFilename;
    }

    public void setLogFilename(final SOSOptionLogFileName val) {
        logFilename = val;
    }

    @JSOptionDefinition(name = "log4jPropertyFileName", description = "Name of the LOG4J Property File", key = "log4j_Property_FileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName log4jPropertyFileName = new SOSOptionInFileName(this, CLASS_NAME + ".log4j_Property_FileName",
            "Name of the LOG4J Property File", "env:log4j.configuration", "./log4j.properties", false);

    public String getPrefix() {
        return alternativePrefix;
    }

}