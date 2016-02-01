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
	public static boolean			gflgUseBase64ForObject			= true;
	public static boolean			flgIncludeProcessingInProgress	= false;
	public boolean					gflgSubsituteVariables			= true;
	public String					TestVar							= "Wert von TestVar";
	
	protected Msg					objMsg							= null;
	protected static final String	conParamNamePrefixALTERNATIVE	= "alternative_";
	protected static final String	conParamNamePrefixJUMP			= "jump_";
	protected static final String	conParamNamePrefixTARGET		= "target_";
	protected static final String	conParamNamePrefixSOURCE		= "source_";
	protected static final String	conParamNamePrefixPROXY			= "proxy_";
	protected enum IterationTypes {
        setRecord(1), getRecord(2), toOut(3), createXML(4), setDefaultValues(5), clearAllValues(6), countSegmentFields(7), CheckMandatory(12), setPrefix(14), 
        toString(13), getCommandLine(14), DirtyToString(15), getKeyValuePair(16), LoadValues(17), StoreValues(18), getQuotedCommandLine(19);
        private int intType;

		IterationTypes(final int pintType) {
			intType = pintType;
		}

		public int Code() {
			return intType;
		}
	}
	protected final String				conNullButMandatory				= "%3$s : Setting %1$s (%2$s) is mandatory, must be not null.%n";
	protected final String				conChangedMsg					= "%3$s: changed from '%1$s' to '%2$s'.";
	protected HashMap<String, String>	objSettings						= null;
	protected HashMap<String, String>	objProcessedOptions				= null;
	protected Class						objClassName4PreferenceStore	= this.getClass();
	
	private static final long			serialVersionUID				= 8497293387023797049L;
	private final String				className						= this.getClass().getSimpleName();
	private static final Logger			LOGGER							= Logger.getLogger(JSOptionsClass.class);
	private static String				ENV_VAR_TEST_MODE				= new String("JS_TEST_MODE");
	private StringBuffer				strBuffer						= new StringBuffer("");
		
	@JSOptionDefinition(name = "BaseDirectory", description = "A Base Directory for all relative FileNames used by SOSOptionFileName", key = "Base_Directory", type = "SOSOptionFolderName", mandatory = false)
	public SOSOptionFolderName			BaseDirectory					= new SOSOptionFolderName(
																				this,
																				className + ".Base_Directory", 
																				"A Base Directory for all relative FileNames used by SOSOptionFileName", 
																				"env:user.dir",
																				"env:user.dir",
																				false);

	public SOSOptionFolderName getBaseDirectory() {
		return BaseDirectory;
	}

	public JSOptionsClass setBaseDirectory(final SOSOptionFolderName val) {
		BaseDirectory = val;
		return this;
	} 
	
	@JSOptionDefinition(name = "DateFormatMask", description = "General Mask for date fomatting", key = "Date_Format_Mask", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	DateFormatMask	= new SOSOptionString(
													this, 
													className + ".Date_Format_Mask", 
													"General Mask for date fomatting", 
													"yyyy-MM-dd", 
													"yyyy-MM-dd",
													false);

	public SOSOptionString getDateFormatMask() {
		return DateFormatMask;
	}

	public JSOptionsClass setDateFormatMask(final SOSOptionString val) {
		DateFormatMask = val;
		return this;
	} 
	
	@JSOptionDefinition(name = "TimeFormatMask", description = "General Mask for time formatting", key = "Time_Format_Mask", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	TimeFormatMask	= new SOSOptionString( 
													this, 
													className + ".Time_Format_Mask", 
													"General Mask for time formatting", 
													"HH:mm:ss", 
													"HH:mm:ss", 
													false);

	public SOSOptionString getTimeFormatMask() {
		return TimeFormatMask;
	}
	
	public JSOptionsClass setTimeFormatMask(final SOSOptionString val) {
		TimeFormatMask = val;
		return this;
	} 
	
	@JSOptionDefinition(name = "Scheduler_Hot_Folder", description = "Pathname to the JobScheduler live-folder", key = "Scheduler_Hot_Folder", type = "SOSOptionFolderName", mandatory = true)
	public SOSOptionFolderName	Scheduler_Hot_Folder	= new SOSOptionFolderName(
																this, 
																className + ".Scheduler_Hot_Folder", 
																"Pathname to the JobScheduler live-folder", 
																"${SCHEDULER_DATA}/config/live", 
																"", 
																true);

	public String getScheduler_Hot_Folder() {
		return Scheduler_Hot_Folder.Value();
	}
	
	public JSOptionsClass setScheduler_Hot_Folder(final String val) {
		Scheduler_Hot_Folder.Value(val);
		return this;
	} 

	@JSOptionDefinition(name = "Scheduler_Data", description = "Data Folder of JobScheduler Installation", key = "Scheduler_Data", type = "SOSOptionFolderName", mandatory = false)
	public SOSOptionFolderName	Scheduler_Data	= new SOSOptionFolderName(
														this, 
														className + ".Scheduler_Data", 
														"Data Folder of JobScheduler Installation", 
														"env:SCHEDULER_DATA",
														"env:SCHEDULER_DATA", 
														false);

	public String getScheduler_Data() {
		return Scheduler_Data.Value();
	}
	public JSOptionsClass setScheduler_Data(final String val) {
		Scheduler_Data.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(name = "Scheduler_Home", description = "Home Root Folder of JobScheduler", key = "Scheduler_Home", type = "SOSOptionFileName", mandatory = true)
	public SOSOptionFolderName	Scheduler_Home	= new SOSOptionFolderName(
														this, 
														className + ".Scheduler_Home", 
														"Home Root Folder of JobScheduler", 
														"env:SCHEDULER_HOME", 
														"env:SCHEDULER_HOME", 
														false);

	public String getScheduler_Home() {
		return Scheduler_Home.Value();
	}
	
	public JSOptionsClass setScheduler_Home(final String val) {
		Scheduler_Home.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(name = "Local_user", description = "I18N is for internationalization of Application", key = "Local_user", type = "SOSOptionUserName", mandatory = true)
	public SOSOptionUserName	UserName	= new SOSOptionUserName(
													this, 
													className + ".local_user", 
													"Name of local user", 
													System.getProperty("user.name"), 
													System.getProperty("user.name"), 
													true);
	@JSOptionDefinition(name = "Locale", description = "I18N is for internationalization of Application", key = "Locale", type = "SOSOptionString", mandatory = true)
	public SOSOptionLocale		Locale		= new SOSOptionLocale(
													this, 
													className + ".Locale", 
													"I18N is for internationalization of Application", 
													"env:SOS_LOCALE",
													java.util.Locale.getDefault().toString(), 
													true);

	public java.util.Locale getI18NLocale() {
		return new java.util.Locale(Locale.Value());
	}

	public String getLocale() {
		return Locale.Value();
	}
	
	@Override
	public void setLocale(final String val) {
		Locale.Value(val);
	} 
	
	@JSOptionDefinition(name = "CheckNotProcessedOptions", description = "If this Option is set to true, all not processed or recognized options are reported as a warning", key = "CheckNotProcessedOptions", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	CheckNotProcessedOptions	= new SOSOptionBoolean(
																this, 
																className + ".CheckNotProcessedOptions", 
																"If this Option is set to true, all not processed or recognized options are reported as a warning", 
																"false", 
																"false", 
																false);

	public String getCheckNotProcessedOptions() {
		return CheckNotProcessedOptions.Value();
	} 
	
	public JSOptionsClass setCheckNotProcessedOptions(final String val) {
		CheckNotProcessedOptions.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(name = "XmlId", description = "This ist the ...", key = "XmlId", type = "SOSOptionString", mandatory = true)
	public SOSOptionString	XmlId	= new SOSOptionString(
											this,
											className + ".XmlId", 
											"This ist the ...", 
											"root", 
											"root", 
											true);

	public String getXmlId() throws Exception {
		return XmlId.Value();
	} 
	
	public JSOptionsClass setXmlId(final String val) throws Exception {
		XmlId.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(name = "ArchiverOptions", value = "", description = "Optionen f�r die Dateiarchivierung", key = "", type = "JSOptionClass", mandatory = false)
	private JSArchiverOptions	objArchiverOptions	= null;

	@JSOptionDefinition(name = "TestMode", value = "false", description = "Test Modus schalten ", key = "TestMode", type = "JSOptionBoolean", mandatory = false)
	public SOSOptionBoolean		TestMode			= new SOSOptionBoolean(this, 
															className + ".TestMode",
															"Test Modus schalten ", 
															"false",
															"false",
															false 
													);
	@JSOptionDefinition(name = "Debug", value = "false", description = "Debug-Modus schalten (true/false)", key = "Debug", type = "JSOptionBoolean", mandatory = false)
	public SOSOptionBoolean		Debug				= new SOSOptionBoolean(this, 
															className + ".Debug", 
															"Debug-Modus schalten (true/false)", 
															"false", 
															"false", 
															false
													);
	@JSOptionDefinition(name = "DebugLevel", value = "0", description = "DebugLevel", key = "DebugLevel", type = "JSOptionInteger", mandatory = false)
	public SOSOptionInteger		DebugLevel			= new SOSOptionInteger(this, 
															className + ".DebugLevel", 
															"DebugLevel", 
															"0", 
															"0", 
															false 
													);
	
	@JSOptionDefinition(name = "log_filename", description = "Name der Datei mit den Logging-Eintr�gen", key = "log_filename", type = "SOSOptionFileName", mandatory = false)
	public SOSOptionLogFileName	log_filename		= new SOSOptionLogFileName(
															this, 
															className + ".log_filename", 
															"Name der Datei mit den Logging-Eintr�gen",
															"stdout", 
															"stdout", 
															false);

	public SOSOptionLogFileName getlog_filename() {
		return log_filename;
	} 
	
	public void setlog_filename(final SOSOptionLogFileName val) {
		log_filename = val;
	} 
	
	@JSOptionDefinition(name = "log4jPropertyFileName", description = "Name of the LOG4J Property File", key = "log4j_Property_FileName", type = "SOSOptionInFileName", mandatory = false)
	public SOSOptionLog4JPropertyFile	log4jPropertyFileName	= new SOSOptionLog4JPropertyFile(
																		this, 
																		className + ".log4j_Property_FileName", 
																		"Name of the LOG4J Property File", 
																		"env:log4j.configuration", 
																		"./log4j.properties", 
																		false);

	public SOSOptionLog4JPropertyFile log4jPropertyFileName() {
		return log4jPropertyFileName;
	}

	public void log4jPropertyFileName(SOSOptionLog4JPropertyFile pobjO) {
		log4jPropertyFileName = pobjO;
	}

	public String getlog4jPropertyFileName() {
		return log4jPropertyFileName.Value();
	} 
	
	public JSOptionsClass setlog4jPropertyFileName(final String val) {
		log4jPropertyFileName.Value(val);
		return this;
	} 
	
	@JSOptionDefinition(name = "ApplicationName", description = "Name of the Application", key = "ApplicationName", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	ApplicationName	= new SOSOptionString( 
													this, 
													className + ".ApplicationName", 
													"Name of the Application", 
													"env:SOSApplicationName", 
													"env:SOSApplicationName", 
													false);

	public SOSOptionString ApplicationName() {
		return ApplicationName;
	}

	public SOSOptionString getApplicationName() {
		return ApplicationName;
	} 
	
	public JSOptionsClass setApplicationName(final SOSOptionString val) {
		ApplicationName = val;
		return this;
	} 
	
	@JSOptionDefinition(name = "ApplicationDocuUrl", description = "The Url of the Documentation of this Application", key = "ApplicationDocuUrl", type = "SOSOptionUrl", mandatory = false)
	public SOSOptionUrl	ApplicationDocuUrl	= new SOSOptionUrl( 
													this, 
													className + ".ApplicationDocuUrl", 
													"The Url of the Documentation of this Application", 
													"env:SOSApplicationDocuUrl", 
													"env:SOSApplicationDocuUrl",
													false);

	public SOSOptionUrl getApplicationDocuUrl() {
		return ApplicationDocuUrl;
	} 
	
	public JSOptionsClass setApplicationDocuUrl(final SOSOptionUrl val) {
		ApplicationDocuUrl = val;
		return this;
	} 
	
	@JSOptionDefinition(name = "AllowEmptyParameterList", description = "If true, an empty parameter list leads not into an error", key = "AllowEmptyParameterList", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	AllowEmptyParameterList	= new SOSOptionBoolean( 
															this, 
															className + ".AllowEmptyParameterList",
															"If true, an empty parameter list leads not into an error", 
															"true", 
															"true", 
															false);

	public SOSOptionBoolean getAllowEmptyParameterList() {
		return AllowEmptyParameterList;
	} 
	
	public JSOptionsClass setAllowEmptyParameterList(final SOSOptionBoolean val) {
		AllowEmptyParameterList = val;
		return this;
	} 
	
	private boolean						flgIgnoreEnvironmentVariables	= false;
	protected boolean					flgSetAllOptions				= false;
	private final String				strOptionNamePrefix				= "-";
	private String						strCommandLineArgs[];
	private String						strCurrentNodeName				= "";
	private String						strCurrentJobName				= "";
	private int							intCurrentJobId					= 0;
	private String						strCurrentJobFolder				= "";
	private final String				conFilePathSeparator			= File.separator;
	public final static String			newLine							= System.getProperty("line.separator");
	private String						strTempDirName					= System.getProperty("java.io.tmpdir") + conFilePathSeparator;
	private String						strUserDirName					= System.getProperty("user.dir") + conFilePathSeparator;
	public JSOptionPropertyFolderName	UserDir							= new JSOptionPropertyFolderName(this, "", "", "", "", false);
	@SuppressWarnings("rawtypes")
	public Class						objParentClass					= this.getClass();
	protected String					strAlternativePrefix			= "";

	public JSOptionsClass() {
		try {
			objMsg = new Msg(new BundleBaseName(this.getClass().getAnnotation(I18NResourceBundle.class).baseName()));
		}
		catch (Exception e) {
		}
	} 
	
	public JSOptionsClass(final HashMap<String, String> settings) {
		this();
		this.Settings(settings);
		ArchiverOptions();
	}

	public String getPrefix() {
		return strAlternativePrefix;
	}

	protected String concatIfNotEmpty(final String val) {
		if (isNotEmpty(val)) {
			return "\n" + val;
		}
		return val;
	}

	public HashMap<String, String> Settings() {
		if (objSettings == null) {
			objSettings = new HashMap<String, String>();
		}
		return objSettings;
	} 
	
	public HashMap<String, String> Settings4StepName() {
		HashMap<String, String> map = new HashMap<String, String>();
		int intStartPos = strCurrentNodeName.length() + 1;
		for (final Map.Entry<String, String> mapItem : Settings().entrySet()) {
			String key = mapItem.getKey();
			String value = mapItem.getValue();
			if (key.indexOf("/") != -1) {
				if (key.startsWith(strCurrentNodeName + "/")) {
					key = key.substring(intStartPos);
				}
				else {
					value = null;
				}
			}
			if (value != null) {
				LOGGER.debug(key + " = " + value);
				map.put(key, value);
			}
		}
		return map;
	} 
	
	public String TempDirName() {
		return this.TempDir();
	}

	public String TempDir() {
		return strTempDirName;
	}

	public void TempDir(final String val) {
		strTempDirName = val;
	}

	public String UserDir() {
		return strUserDirName;
	}

	public void UserDir(final String val) {
		strUserDirName = val;
	}

	public void Settings(final HashMap<String, String> settings) {
		objSettings = settings;
		setAllCommonOptions(settings);
	} 
	
	public String getItem(final String pstrKey) {
		String value = ""; // null;
		String key = pstrKey;
		if (isEmpty(pstrKey)) {
			return value;
		}
		if (objProcessedOptions == null) {
			objProcessedOptions = new HashMap<String, String>();
		}
		key = key.replaceAll("_", "");
		String strLSKey = "";
		if (strCurrentNodeName.length() > 0) {
			strLSKey = strCurrentNodeName + "/" + pstrKey.replaceAll("_", "");
			for (final Object element : objSettings.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				String strMapKey = mapItem.getKey().toString();
				strMapKey = strMapKey.replaceAll("_", "");
				if (strLSKey.equalsIgnoreCase(strMapKey)) {
					if (mapItem.getValue() != null) {
						value = mapItem.getValue().toString();
					}
					else {
						value = null;
					}
					objProcessedOptions.put(strMapKey, value);
					return value;
				}
			}
		}
		for (final Object element : objSettings.entrySet()) {
			final Map.Entry mapItem = (Map.Entry) element;
			String strMapKey = mapItem.getKey().toString();
			String lstrMapKey = strMapKey.replaceAll("_", "");
			if (key.equalsIgnoreCase(lstrMapKey)) {
				if (mapItem.getValue() != null) {
					value = mapItem.getValue().toString();
				}
				else {
					value = null;
				}
				objProcessedOptions.put(strMapKey, value);
				return value;
			}
		}
		/**
		 * wenn wir hier angekommen sind, dann wurde mit dem "richtigen" Wert (in Gro�-/Kleinschreibung und
		 * mit Klassennamen) nichts gefunden. Jetzt wird ohne Klassenname im Schl�ssel gesucht.
		 */
		int i = key.indexOf('.'); // KlassenName . Name
		if (i > 0) { // Key contains/started with ClassName ...
			strLSKey = "";
			key = key.substring(++i); // get key without classname
			if (strCurrentNodeName.length() > 0) {
				strLSKey = strCurrentNodeName + "/" + key;
				for (final Object element : objSettings.entrySet()) {
					final Map.Entry mapItem = (Map.Entry) element;
					String strMapKey = mapItem.getKey().toString();
					String lstrMapKey = strMapKey.replaceAll("_", "");
					if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
						if (mapItem.getValue() != null) {
							value = mapItem.getValue().toString();
						}
						else {
							value = null;
						}
						objProcessedOptions.put(strMapKey, value);
						return value;
					}
				} 
			}
			for (final Object element : objSettings.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				String strMapKey = mapItem.getKey().toString();
				String lstrMapKey = strMapKey.replaceAll("_", "");
				if (strLSKey.length() > 0) {
					if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
						if (mapItem.getValue() != null) {
							value = mapItem.getValue().toString();
						}
						else {
							value = null;
						}
						objProcessedOptions.put(strMapKey, value);
						return value;
					}
				}
				if (key.equalsIgnoreCase(lstrMapKey)) {
					if (mapItem.getValue() != null) {
						value = mapItem.getValue().toString();
					}
					else {
						value = null;
					}
					objProcessedOptions.put(strMapKey, value);
					return value;
				}
			} 
		} 
		if (value == null || value.length() <= 0) {
			value = null;
		}
		return value;
	} 
	
	public boolean ReportNotProcessedOptions() {
		boolean ok = true;
		if (objSettings != null) {
			for (final Map.Entry<String, String> mapItem : objSettings.entrySet()) {
				String key = mapItem.getKey().toString();
				String processedValue = objProcessedOptions.get(key);
				if (processedValue == null) {
					String value = null;
					if (mapItem.getValue() != null) {
						value = mapItem.getValue().toString();
					}
					else {
						value = null;
					}
					LOGGER.warn(String.format("SOSOPT-W-001: Option '%1$s' with value '%2$s' is unknown and not processed", key, value));
					ok = false;
				}
			}
		}
		return ok;
	} 
	
	public HashMap<String, String> getProcessedOptions() {
		if (objProcessedOptions == null) {
			objProcessedOptions = new HashMap<String, String>();
		}
		return objProcessedOptions;
	} 
	
	public void addProcessedOptions(final HashMap<String, String> map) {
		this.getProcessedOptions();
		objProcessedOptions.putAll(map);
	} 
	
	public String getItem(final String key, final String defaultValue) {
		String value = this.getItem(key);
		if (isEmpty(value)) {
			value = defaultValue;
		}
		return value;
	}

	protected int getIntItem(final String key) {
		return String2Integer(this.getItem(key));
	}

	protected int String2Integer(final String val) {
		int result = 0;
		if (isNotEmpty(val)) {
			try {
				result = Integer.parseInt(val);
			}
			catch (final NumberFormatException e) {
				throw new RuntimeException("format Exception raised", e);
			}
		}
		return result;
	}

	protected boolean getBoolItem(final String key) {
		boolean result = false;
		if (isNotEmpty(key)) {
			result = String2Bool(this.getItem(key));
		}
		return result;
	}

	protected boolean getBoolItem(final String key, final boolean defaultResult) {
		boolean result = false;
		if (isNotEmpty(key)) {
			result = String2Bool(this.getItem(key));
		}
		else {
			result = defaultResult;
		}
		return result;
	}

	@Override
	public boolean String2Bool(final String val) {
		boolean result = false;
		if (isNotEmpty(val)) {
			if (val.equals("1") 
				|| val.equalsIgnoreCase("y") 
				|| val.equalsIgnoreCase("yes") 
				|| val.equalsIgnoreCase("j")
				|| val.equalsIgnoreCase("on") 
				|| val.equalsIgnoreCase("true") 
				|| val.equalsIgnoreCase("wahr")) {
				result = true;
			}
		}
		return result;
	}

	protected void toOut(final String val) throws Exception {
		String out = val;
		if (!out.contains(className)) {
			out += getAllOptionsAsString();
		}
		System.out.println(out);
	}

	protected void toOut() {
		try {
			this.toOut(getAllOptionsAsString());
		}
		catch (final JobSchedulerException e) {
			throw e;
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		String txt = null;
		try {
			txt = getAllOptionsAsString();
		}
		catch (final Exception e) {
			throw new JobSchedulerException("toString failed", e);
		}
		return txt;
	}

	public String dirtyString() {
		String txt = "";
		try {
			txt += "\n" + getAllOptionsAsString(IterationTypes.DirtyToString);
		}
		catch (final Exception e) {
			throw new JobSchedulerException("dirtyString failed", e);
		}
		return txt;
	}

	public void Options2ClipBoard() {
		String txt = "";
		if (objSettings != null) {
			for (final Object element : objSettings.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				final String key = mapItem.getKey().toString();
				if (mapItem.getValue() != null) {
					String value = mapItem.getValue().toString();
					txt += "\n" + key + "=" + value;
				}
			}
		}
		else {
			txt = this.dirtyString();
		}
		System.out.println(txt);
	}

	private String getAllOptionsAsString(final IterationTypes type) {
		String txt = ""; 
		if (objParentClass != null) {
			txt += IterateAllDataElementsByAnnotation(objParentClass, this, type, new StringBuffer(""));
		}
		return txt;
	}

	private String getAllOptionsAsString() {
		return getAllOptionsAsString(IterationTypes.toString);
	}

	protected void setAllCommonOptions(final HashMap<String, String> settings) {
		objSettings = settings;
		
		if (objParentClass != null) {
			//IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.countSegmentFields, strBuffer);
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setRecord, strBuffer);
		}
		
		UserDir.MapValue();
		
		if(!this.IgnoreEnvironmentVariables()) {
			getEnvironmentVariables();
		}
	}

	public JSOptionsClass getEnvironmentVariables() {
		String txt = EnvironmentVariable(JSOptionsClass.ENV_VAR_TEST_MODE);
		if (isNotEmpty(txt)) {
			this.TestMode(String2Bool(txt));
		}
		txt = EnvironmentVariable("JS_DEBUG");
		if (isNotEmpty(txt)) {
			this.Debug(String2Bool(txt));
		}
		txt = EnvironmentVariable("JS_DEBUG_LEVEL");
		if (isNotEmpty(txt)) {
			this.DebugLevel(String2Integer(txt));
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
	
	public void TestMode(final boolean val) {
		TestMode.value(val);
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
	
	protected String CheckFileIsReadable(final String filePath, final String methodName) {
		if (isNotEmpty(filePath)) {
			final File file = new File(filePath);
			String msg = null;
			if (file.exists() == false) {
				msg = String.format("%2$s: File '%1$s' does not exist.", filePath, methodName);
				this.SignalError(new JSExceptionInputFileNotFound(msg), msg);
			}
			if (file.canRead() == false) {
				msg = String.format("%2$s: File '%1$s'. canRead returns false. Check permissions.", filePath, methodName);
				this.SignalError(new JSExceptionFileNotReadable(msg), msg);
			}
		}
		return filePath;
	} 
	
	public String CheckFolder(String filePath, final String methodName, final Boolean createIfNotExist) {
		if (isNotEmpty(filePath)) {
			final String sep = System.getProperty("file.separator");
			if (filePath.endsWith(sep) == false) {
				filePath += sep;
			}
			final File file = new File(filePath);
			if (file.exists() == false) {
				if (!createIfNotExist) {
					this.SignalError(String.format("%2$s: Folder '%1$s' does not exist.", filePath, methodName));
				}
				else {
					file.mkdir();
					SignalInfo(String.format("%2$s: Folder '%1$s' created.", filePath, methodName));
				}
			}
			if (file.canRead() == false) {
				this.SignalError(String.format("%2$s: File '%1$s'. canRead returns false. Check permissions.", filePath, methodName));
			}
		}
		return filePath;
	} 
	
	public String CheckIsFileWritable(final String filePath, final String methodName) {
		String msg = null;
		if (isNotEmpty(filePath)) {
			try {
				final File file = new File(filePath);
				if (!file.exists()) {
					file.createNewFile();
				}
				if (!file.canWrite()) {
					msg = String.format("%2$s: File '%1$s'. canWrite returns false. Check permissions.", filePath, methodName);
					msg += file.toString();
				}
			}
			catch (final Exception ex) {
				msg = String.format("%2$s: File '%1$s'. Exception thrown. Check permissions.", filePath, methodName);
				final JobSchedulerException objJSEx = new JobSchedulerException(msg, ex);
				this.SignalError(objJSEx, msg);
			}
			if (msg != null) {
				this.SignalError(msg);
			}
		}
		return filePath;
	} 
	
	protected String getVal(final String val) {
		return val == null ? "" : val.toString();
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

	protected String NormalizeDirectoryName(final String directory) {
		String path = directory;
		if (path == null || path.trim().length() <= 0) {
			path = "";
		}
		else {
			if (path.endsWith(File.separator) == false && path.endsWith("/") == false) {
				path += "/";
			}
			if (path.startsWith("./")) {
				path = path.substring(2);
				try {
					path = AbsolutFileName(path);
				} 
				catch (final Exception ex) {
				}
			}
			path = SubstituteVariables(path);
		}
		return path;
	}

	protected String NormalizeFileName(final String name) {
		return SubstituteVariables(name);
	}

	public String NormalizeFileName(final String file, final String directory) {
		String path = file;
		if (file.indexOf("/") < 0) {
			path = NormalizeDirectoryName(directory) + file;
		}
		return path;
	}

	public String AbsolutFileName(final String file) throws Exception {
		String path = file;
		if (path == null) {
			return path;
		}
		if (path.startsWith("file:")) {
			return path.substring(5);
		}
		if (path.startsWith("/")) {
			return path;
		}
		if (path.startsWith("./")) {
			path = path.substring(2); 
		}
		path = new File(path).toURI().toURL().toString();
		if (path.startsWith("file:")) {
			path = path.substring(5);
		}
		return path;
	}

	public String SubstituteVariables(final String val) {
		return val == null ? null : val.replace("//", "/");
	}

	public boolean IgnoreEnvironmentVariables() {
		return flgIgnoreEnvironmentVariables;
	} 
	
	public JSOptionsClass IgnoreEnvironmentVariables(final boolean val) throws Exception {
		flgIgnoreEnvironmentVariables = val;
		return this;
	} 

	protected String[] SplitString(final String txt) {
		return txt == null ? null : txt.trim().split("[;|,]");
	}

	protected void CheckNull(final String methodName, final String titel, final String name, final String value) throws Exception {
		if (isEmpty(value)) {
			this.SignalError(String.format(conNullButMandatory, methodName, titel, name));
		}
	}
	
	public String	gstrApplicationName		= "JobScheduler";
	public String	gstrApplicationDocuUrl	= "http://docu.sos-berlin.com";

	public void CommandLineArgs(final String txt) {
		StrTokenizer st = new StrTokenizer(txt);
		CommandLineArgs(st.getTokenArray());
	}

	public void CommandLineArgs(final String[] args) {
		final String methodName = className + "::CommandLineArgs ";
		if (AllowEmptyParameterList.isFalse()) {
			if (args.length <= 0) {
				throw new ParametersMissingButRequiredException(ApplicationName.Value(), ApplicationDocuUrl.Value());
			}
		}
		strCommandLineArgs = args;
		
		boolean flgOption = true;
		String name = null;
		String value = null;
		this.Settings();
		final int l = strOptionNamePrefix.length();
		
		for (final String argument : strCommandLineArgs) {
			if (flgOption == true) {
				if (argument.length() < l) {
					continue;
				}
				if (argument.substring(0, l).equalsIgnoreCase(strOptionNamePrefix)) {
					name = argument.substring(l);
					flgOption = false;
					// name and value separated by an equalsign?
					int pos = name.indexOf("=");
					if (pos > 0) {
						value = name.substring(pos + 1);
						value = StripQuotes(value);
						name = name.substring(0, pos);
						objSettings.put(name, value);
						if(name.contains("password")){
						    this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", methodName, name, "*****"));
						}else{
						    this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", methodName, name, value));
						}
						flgOption = true; // next tooken must be an option
					}
				}
				else {
					LOGGER.warn(String.format("'%1$s' seems to be an unsupported (positional) parameter. ignored", argument));
				}
			}
			else {
				if (name != null) {
					value = argument;
					flgOption = true;
					objSettings.put(name, value);
					if(name.contains("password")){
			            this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", methodName, name, "*****"));
					}else{
			            this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", methodName, name, value));
					}
					name = null;
				}
			}
		}
		final String propertyFileName = this.getItem("PropertyFileName", "");
		if (propertyFileName.length() > 0) {
			LoadProperties(propertyFileName);
			name = null;
			flgOption = true;
			for (final String argument : strCommandLineArgs) {
				if (flgOption == true) {
					if (argument.substring(0, l).equalsIgnoreCase(strOptionNamePrefix)) {
						name = argument.substring(l);
						flgOption = false;
					}
				}
				else {
					if (name != null) {
						value = argument;
						flgOption = true;
						objSettings.put(name, value);
						this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", methodName, name, value));
						name = null;
					}
				}
			}
			message(methodName + ": Property-File loaded. " + propertyFileName);
		}
		DumpSettings();
		setAllOptions(objSettings);
	}

	public String[] CommandLineArgs() {
		return strCommandLineArgs;
	}

	public void LoadProperties(final String propertiesFileName) {
		final String methodName = className + "::LoadProperties ";
		try {
			final Properties properties = new Properties();
			properties.load(new FileInputStream(propertiesFileName));
			message(methodName + ": PropertyFile red. Name '" + propertiesFileName + "'.");
			this.Settings();
			for (final Object element : properties.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				final String strMapKey = mapItem.getKey().toString();
				if (mapItem.getValue() != null) {
					final String strTemp = mapItem.getValue().toString();
					if (strTemp != null && strTemp.length() > 0 && strTemp.equalsIgnoreCase(".") == false) {
						objSettings.put(strMapKey, strTemp);
					}
				}
			}
			message(methodName + ": Property-File loaded");
			setAllOptions(objSettings);
			setAllCommonOptions(objSettings);
		}
		catch (Exception e) {
			throw new JobSchedulerException(e);
		}
	} 
	
	public void LoadSystemProperties() throws Exception {
		Properties properties = new Properties();
		properties = System.getProperties();
		LoadProperties(properties);
	} 
	
	public void LoadProperties(final Properties properties) {
		this.Settings();
		
		for (final Object element : properties.entrySet()) {
			@SuppressWarnings("rawtypes")
			final Map.Entry mapItem = (Map.Entry) element;
			final String key = mapItem.getKey().toString();
			if (mapItem.getValue() != null) {
				final String value = mapItem.getValue().toString();
				LOGGER.debug("Property " + key + " = " + value);
				if (value != null && value.length() > 0 && value.equalsIgnoreCase(".") == false) {
					objSettings.put(key, value);
				}
			}
		}
		try {
			setAllOptions(objSettings);
		}
		catch (Exception e) {
			throw new JobSchedulerException("setAllOptions returns an error:", e);
		}
		setAllCommonOptions(objSettings);
	} 
	
	public void setAllOptions(final HashMap<String, String> settings, final String alternativePrefix) throws Exception {
		if (strAlternativePrefix.length() <= 0) {
			strAlternativePrefix = alternativePrefix;
			if (objParentClass != null) {
				IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setPrefix, strBuffer);
			}
		}
		this.setAllOptions(settings);
	}

	public void setAllOptions(final HashMap<String, String> map) {
		setAllCommonOptions(map);
	} 

	public String CurrentNodeName() {
		return strCurrentNodeName;
	} 
	
	public JSOptionsClass CurrentNodeName(final String val) throws Exception {
		strCurrentNodeName = val;
		return this;
	} 
	
	public JSOptionsClass CurrentJobName(final String val) throws Exception {
		strCurrentJobName = val;
		return this;
	} 

	public String CurrentJobName() {
		return strCurrentJobName;
	} 
	
	public JSOptionsClass CurrentJobId(final int val) throws Exception {
		intCurrentJobId = val;
		return this;
	} 

	public int CurrentJobId() {
		return intCurrentJobId;
	} 
	
	public JSOptionsClass CurrentJobFolder(final String val) throws Exception {
		strCurrentJobFolder = val;
		return this;
	} 

	public String CurrentJobFolder() {
		return strCurrentJobFolder;
	} 
	
	@SuppressWarnings("unchecked")
	public String OptionByName(final String name) {
		String value = null;
		Class c = this.getClass();
		value = getOptionValue(c, name);
		if (value == null) {
			c = objParentClass.getClass();
			value = getOptionValue(c, name);
		}
		return value;
	}

	private String getOptionValue(final Class c, final String name) {
		String value = null;
		Field field = null;
		try {
			field = c.getField(name);
			Object obj = field.get(this);
			if (obj instanceof String) {
				value = (String) field.get(this);
			}
			else {
				if (obj instanceof SOSOptionElement) {
					SOSOptionElement objDE = (SOSOptionElement) obj;
					value = objDE.Value();
				}
			}
		}
		catch (final NoSuchFieldException ex) {
			Method method;
			try {
				method = c.getMethod(name, null);
				value = (String)method.invoke(this, null);
			}
			catch (final SecurityException e) {
				e.printStackTrace();
			}
			catch (final NoSuchMethodException e) {
			}
			catch (final IllegalArgumentException e) {
				e.printStackTrace();
			}
			catch (final InvocationTargetException e) {
				e.printStackTrace();
			}
			catch (final IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		catch (final IllegalAccessException e) {
			e.printStackTrace();
		}
		return value;
	}
	private static Properties	objP	= new Properties();

	public Properties getTextProperties() {
		if (objP == null) {
			objP = new Properties();
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
		}
		catch (Exception uhe) {
		}
		String strVal = "";
		String strKey = "";
		String strParamNameEnclosedInPercentSigns = "^.*(\\$|%)\\{([^%\\}]+)\\}.*$";
		/**
		 * Problem hier:
		 * Wenn der gesamte String verwendet wird, so ist spaetestens beim file_spec
		 * keine korrekte Substitution mehr moeglich.
		 * Liegt warscheinlich am regexp pattern.
		 */
		String strNewString = "";
		if (isNotNull(pstrReplaceIn)) {
			try {
				String[] strA = pstrReplaceIn.split("\\n");
				for (String string : strA) {
					while (string.matches(strParamNameEnclosedInPercentSigns)) {
						strKey = string.replaceFirst(strParamNameEnclosedInPercentSigns, "$2");
						if (strKey.equalsIgnoreCase("uuid")) {
							continue;
						}
						String strPP = "(\\$|%)\\{" + strKey + "\\}";
						strVal = this.OptionByName(strKey);
						if (isNotNull(strVal)) {
							strVal = strVal.replace('\\', '/');
							string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
						}
						else {
							strVal = (String) objP.get(strKey);
							if (strVal != null) {
								string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
							}
							else {
								strVal = Settings().get(strKey);
								if (strVal != null) {
									string = string.replaceAll(strPP, Matcher.quoteReplacement(strVal));
								}
								else {
									string = string.replaceAll(strPP, "?" + Matcher.quoteReplacement(strKey) + "?");
								}
							}
						}
					}
					strNewString += string + "\n";
				}
			}
			catch (Exception e) { // intentionally no error, wrong regexp ?
			}
		}
		strNewString = strNewString.replaceFirst("\n$", "");
		return strNewString;
	}
	
	public void DumpSettings() {
		final String methodName = className + "::DumpSettings";
		for (final Map.Entry<String,String> mapItem : objSettings.entrySet()) {
			final String key = mapItem.getKey().toString();
			if (mapItem.getValue() != null) {
				if(mapItem.getKey().contains("password")){
			       this.SignalDebug(methodName + ": Key = " + key + " --> " + "*****");
			    } else {
			       this.SignalDebug(methodName + ": Key = " + key + " --> " + mapItem.getValue().toString());
			    }
			}
		}
	}

	public String getIndexedItem(final String key, final String description, final String delimiter) {
		final JSOptionValueList optionValueList = new JSOptionValueList(this, key, description, "", true);
		return optionValueList.concatenatedValue(delimiter);
	}

	public Object deepCopy(final Object obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(baos);
		out.writeObject(obj);
		out.close();
		
		byte[] buffer = baos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
		ObjectInputStream in = new ObjectInputStream(bais);
		return in.readObject();
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

	   
	public StringBuffer IterateAllDataElementsByAnnotation(final Class<?> instance, final Object object, final IterationTypes operation, StringBuffer buffer) {
		final String methodName = className + "::IterateAllDataElementsByAnnotation";
		if (instance == null) {
			throw new JobSchedulerException(methodName + ": instance is null");
		}
		Field field = null;
		SOSOptionElement.gflgProcessHashMap = true;
		try {
			final Field fields[] = instance.getFields();
			final StringBuffer xml = new StringBuffer("");
			String className = instance.getName();
			if (operation == IterationTypes.createXML) {
				xml.append("<" + className + " id=" + XmlId.QuotedValue() + ">");
			}
			for (final Field forField : fields) {
				field = forField;
				try {
					if (field.isAnnotationPresent(JSOptionDefinition.class)) {
						final SOSOptionElement element = (SOSOptionElement) field.get(object);
						if (element != null) {
							switch(operation){
								case LoadValues:
											SOSOptionElement.gflgProcessHashMap = true;
											element.loadValues();
											break;
											
								case StoreValues:
											element.storeValues();
											break;
											
								case setPrefix:
											element.setPrefix(strAlternativePrefix);
											break;
											
								case setRecord:
											SOSOptionElement.gflgProcessHashMap = true;
											element.gflgProcessHashMap = true;
											element.MapValue();
											break;
											
								case CheckMandatory:
											element.CheckMandatory();
											break;
											
								case toOut:
											System.out.println(element.toString());
											break;
											
								case toString:
											buffer.append(addNewLine(element.toString()));
											break;
											
								case DirtyToString:
											if (element.isDirty() == true) {
												buffer.append(addNewLine(element.DirtyToString()));
											}
											break;
											
								case createXML:
											xml.append(element.toXml());
											break;
											
								case setDefaultValues:
											if (element.Value().length() <= 0) {
												element.Value(element.DefaultValue());
											}
											break;
											
								case clearAllValues:
											element.Value("");
											break;
											
								case getCommandLine:
											buffer.append(element.toCommandLine());
											break;
											
								case getQuotedCommandLine:
											buffer.append(element.toQuotedCommandLine());
											break;
											
								case getKeyValuePair:
											String val = element.toKeyValuePair(strAlternativePrefix);
											if (isNotEmpty(val)) {
												buffer.append(addNewLine(val));
											}
											break;
								default:
									//getRecord
									//countSegmentFields
									break;
							}
							
							//execute the same operation for the childs recursive 
							IterateAllDataElementsByAnnotation(element.getClass(), element, operation, buffer);
						}
					} 
				}
				catch (final ClassCastException ex) {
				}
				catch (final Exception ex) {
					ex.printStackTrace();
					throw new RuntimeException(ex);
				}
				finally {
				}
			}
			if (operation == IterationTypes.createXML) { // CreateXML
				xml.append("</" + className + ">");
				buffer = xml;
			}
		}
		catch (final Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		finally {
			SOSOptionElement.gflgProcessHashMap = false;
		} 
		return buffer;
	} 

	private String addNewLine(final String val) {
		if (isNotEmpty(val)) {
			return val + "\n";
		}
		return val;
	}

	public StringBuffer Iterate(final IterationTypes operation) {
		StringBuffer sb = new StringBuffer();
		if (objParentClass != null) {
			sb = IterateAllDataElementsByAnnotation(objParentClass, this, operation, sb);
		}
		return sb;
	}
	
	public Vector<String> getItems(final String key) {
		
		final Vector<String> items = new Vector<String>();
		final StringBuffer sb = new StringBuffer();
		String value = getItem(key);
		if (value != null) {
			items.addElement(value);
		}
		int i = 1;
		while ((value = getItem(key + Integer.toString(i++))) != null) {
			items.addElement(value);
			if (i > 2) {
				sb.append(";");
			}
			sb.append(value);
		}
		return items;
	}

	public void putObject(final String path) {
		try {
			JSFile file = new JSFile(path);
			file.Write(getObjectAsString());
			file.close();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured writing object to file: " + e);
		}
	}

	public String getObjectAsString() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(baos);
			out.writeObject(this);
			out.close();
			String encoded = "";
			if (gflgUseBase64ForObject == true) {
				encoded = new String(Base64.encodeBase64(baos.toByteArray()));
			}
			else {
				encoded = new String(baos.toByteArray());
			}
			return encoded;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured getting object as String: " + e);
		}
	} 
	
	public static JSOptionsClass getObject(final String path) {
		try {
			String encoded = new JSTextFile(path).File2String();
			if (encoded == null || encoded.length() == 0) {
				return null;
			}
			byte[] bytes;
			if (gflgUseBase64ForObject == true) {
				bytes = Base64.decodeBase64(encoded.getBytes());
			}
			else {
				bytes = encoded.getBytes();
			}
			
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ObjectInputStream(bais);
			JSOptionsClass obj = (JSOptionsClass) in.readObject();
			in.close();
			
			return obj;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured reading object from file: " + e);
		}
	}

	public void LoadXML(final JSXMLFile path) {
		this.LoadXML(path, null);
	}

	public Properties LoadXML(final JSXMLFile file, final String xpath) {
		DOMParser parser = new DOMParser();
		Properties properties = new Properties();
		try {
			parser.setFeature("http://xml.org/sax/features/validation", false);
			// The parser will validate the document only if a grammar is specified.
			// parser.setFeature("http://xml.org/sax/features/validation/dynamic", true);
		}
		catch (Exception e) {
			throw new JobSchedulerException(String.format("SAXException %1$s", file.getAbsolutePath()), e);
		}
		try {
			parser.parse(file.getAbsolutePath());
			Document document = parser.getDocument();
			if (isEmpty(xpath) == true) {
				traverse(document, properties);
			}
			LoadProperties(properties);
		}
		catch (Exception e) {
			throw new JobSchedulerException(String.format("Exception %1$s", file.getAbsolutePath()), e);
		}
		return properties;
	} 
	
	private void traverse(final Node node, Properties properties) {
		int type = node.getNodeType();
		LOGGER.debug("NodeType = " + type);
		
		String name = node.getNodeName();
		String value = node.getNodeValue();
		LOGGER.debug("<" + name + ">" + value);
		
		if (type == Node.ELEMENT_NODE) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node item = children.item(i);
				if (item.getNodeType() == Node.TEXT_NODE) {
					value = item.getNodeValue();
					if (isNotEmpty(value)) {
						properties.put(name, item.getNodeValue());
					}
					break;
				}
			}
		}
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			traverse(children.item(i), properties);
		}
	}

	public JSXMLFile toXMLFile(final String path) {
		JSXMLFile file;
		try {
			file = new JSXMLFile(path);
			file.writeXMLDeclaration();
			file.comment(String.format("Created by %1$s", className));
			file.Write(this.toXML());
			file.close();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Exception:", e);
		}
		return file;
	} 

	public StringBuffer toXML() {
		return Iterate(IterationTypes.createXML);
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> DeletePrefix(final HashMap<String, String> parameters, final String prefix) {
		String value;
		HashMap<String, String> map = new HashMap<String, String>();
		if (parameters != null) {
			// First put into the hashmap these items which do not starts with the prefix
			for (final Map.Entry<String, String> mapItem : parameters.entrySet()) {
				String key = mapItem.getKey();
				if (mapItem.getValue() != null) {
					value = mapItem.getValue();
				}
				else {
					value = null;
				}
				if (key.startsWith(prefix) == false) {
					map.put(key, value);
				}
			}
			// Then put into the hashmap these items which starts with the prefix. First, delete prefix
			for (final Map.Entry<String, String> mapItem : parameters.entrySet()) {
				String key = mapItem.getKey();
				if (mapItem.getValue() != null) {
					value = mapItem.getValue();
				}
				else {
					value = null;
				}
				if (key.startsWith(prefix)) {
					// TODO avoid java.util.ConcurrentModificationException
					// (http://java.sun.com/javase/6/docs/api/java/util/Iterator.html#remove() )
					// phsmParameters.remove(strMapKey);
					key = key.replaceAll(prefix, "");
					map.put(key, value);
					mapItem.setValue("\n");
				}
				else { // possible case: <nodeName>/<prefix><name> -> <nodeName>/<name>
					String txt = "/" + prefix;
					if (key.contains(txt)) {
						key = key.replace(txt, "/");
						map.put(key, value);
						mapItem.setValue("\n");
					}
				}
			}
		}
		return map;
	} 
	
	public String getPid() {
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		return pid.split("@")[0];
	} 
	
	public boolean isOption(final String name) {
		String optionNameInLowerCases = name.toLowerCase();
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
	public Preferences	objPreferenceStore	= null;

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
			StringBuffer sb = new StringBuffer();
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.LoadValues, sb);
		}
	} 
	
	public void storeOptionValues() {
		if (objParentClass != null) {
			getPreferenceStore();
			objClassName4PreferenceStore = objParentClass;
			StringBuffer sb = new StringBuffer();
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.StoreValues, sb);
		}
	} 
	
	protected void setIfNotDirty(final SOSOptionElement option, final String value) {
		if (option.isNotDirty() && isNotEmpty(value)) {
			LOGGER.trace("setValue = " + value);
			option.Value(value);
		}
	}

} 