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

/**
 * \class JSOptionsClass
 * Super-Klasse f�r alle JS-Option-Klassen.
 *
 * \brief
 *
 * \section intro_sec Introduction
 * Diese Klasse bietet Optionen (Parameter) f�r die Ausf�hrung eines Jobs an,
 * die allen JS-Jobs gemeinsam sind.
 *
 * \subsection optionen_definieren Wie werden Werte f�r Optionen definiert?
 *
 * Optionen werden definiert �ber eine HashMap vom Typ <String, String>.
 * Dabei ist der erste "String" der Name (Schl�ssel) und der zweite "String" der Wert
 * f�r die Option.
 * Die HashMap wird (normalerweise) in der super-Klasse
 * BasicWorkFlow:: aufgebaut und dann an diese Klasse "durchgereicht".
 *
 * Optionen k�nnen auch direkt durch Verwendung der Properties dieser Klasse gesetzt
 * und auch abgefragt werden.
 *
 *
 * Optionen, die durch Environment-Variable des Job-Environment gesetzt werden, �berschreiben
 * dabei die durch die HashMap gesetzten Werte.
 * Environment-Variablen werden ausgewertet, wenn alle Optionen �ber die HashMap gesetzt werden
 * (und nur dann).
 * Die Auswertung geschieht in der Methode {@link #getEnvironmentVariables()}.
 * Die Environment-Variablen (siehe weiter unten) werden jedoch nicht ausgewertet, wenn
 * die Option \c {@link #IgnoreEnvironmentVariables(boolean)} auf \c true gesetzt ist.
 *
 *
 *
 * <TABLE>
 * <TR>
 * <TD><b>Option</b></TD>
 * <TD><b>Typ</b></TD>
 * <TD><b>InitialWert</b></TD>
 * <TD><b>Beschreibung</b></TD>
 * </TR>

 * <TR>
 * <TD>{@link #TestMode(boolean)}</TD>
 * <TD>bool</TD>
 * <TD>false</TD>
 * <TD>
 * Hiermit wird der Testmodus des Jobs aktiviert.
 * Im Testmodus werden Diagnosemeldungen ausgegeben.
 * </TD>
 * </TR>
 *
 * <TR>
 * <TD>{@link #Debug(boolean)}</TD>
 * <TD>bool</TD>
 * <TD>false</TD>
 * <TD>
 * Hiermit wird der DebugModus des Jobs aktiviert.
 * Im Testmodus werden Diagnosemeldungen ausgegeben, falls die
 * Programme diese Option auswerten.
 * </TD>
 * </TR>

 * <TR>
 * <TD>{@link #DebugLevel(int)}DebugLevel </TD>
 * <TD>int</TD>
 * <TD>0</TD>
 * <TD>
 * Diagnosemeldungen werden �ber eine Level-Nummer klassifiziert.
 * '9' bedeutet dabei am wenigstens wichtig und '0' kennzeichnet wichtige
 * Diagnosemeldungen.
 * Es werden alle Meldungen ausgegeben, deren Level-Nummer kleiner oder gleich
 * dem DebugLevel ist.
 * </TD>
 * </TR>

 * </TABLE>
 *
 * \subsection table-environment-variables Tabelle der Environment-Variablen
 *
 * <TABLE>
 * <TR>
 * <TD><b>EnvironmentVariable</b></TD>
 * <TD><b>ClassProperty</b></TD>
 * </TR>

 * <TR>
 * <TD>JS_TEST_MODE</TD>
 * <TD>{@link #TestMode(boolean)}</TD>
 * </TR>
 *
 * <TR><TD>JS_DEBUG</TD>
 * <TD>{@link #Debug(boolean)}</TD></TR>
 *
 * <TR><TD>JS_DEBUG_LEVEL</TD>
 * <TD>{@link #DebugLevel(int)}</TD></TR>
 *
 * <TR><TD>JS_IGNORE_TL</TD>
 * <TD>{@link #IgnoreTrafficLights(boolean)}</TD></TR>
 *
 * <TR><TD>JS_INBOUND</TD>
 * <TD>{@link #InboundRootDir(String)}</TD></TR>
 *
 * <TR><TD>JS_OUTBOUND</TD>
 * <TD>{@link #OutboundRootDir(String)}</TD></TR>
 *
 * <TR><TD>JS_SCHEMA_DIR</TD>
 * <TD>{@link #SchemaDir(String)}</TD></TR>
 *
 * <TR><TD>JS_IGNORE_PENDING_STATUS</TD>
 * <TD>{@link #IgnorePendingStatus(boolean)}</TD></TR>
 *
 * </TABLE>
 *
 * \subsection env-example Ein Beispiel f�r die Verwendung der Variablen
 *
 * \code
 * export JS_SERVERNAME="JSLevDevel"
 * export JS_TEST_MODE=Y

 * export JS_ENVIRONMENT=Test
 * export JS_IGNORE_TL=Y
 * export JS_DEBUG=N
 * export JS_DEBUG_LEVEL=9

 * \endcode
 * \section samples Some Examples
 *
 * \code
 *   .... code goes here ...
 * \endcode
 *
 * <p style="text-align:center">
 * <br />---------------------------------------------------------------------------
 * <br /> APL/Software GmbH - Berlin
 * <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
 * <br />Donnerstag, 16. Oktober 2008, sgx2343 (sgx2343)
 * <br />---------------------------------------------------------------------------
 * </p>
 * \author sgx2343
 * @version $Id$
 * \see reference
 *
 */
@JSOptionClass(name = "JSOptionsClass", description = "JSOptionsClass")
@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class JSOptionsClass extends I18NBase implements IJSArchiverOptions, Serializable {
	protected Msg					objMsg							= null;
	private final String			conClassName					= this.getClass().getSimpleName();
	private static final Logger		logger							= Logger.getLogger(JSOptionsClass.class);
	private static final long		serialVersionUID				= 8497293387023797049L;
	public static boolean			gflgUseBase64ForObject			= true;
	public static boolean			flgIncludeProcessingInProgress	= false;
	public boolean					gflgSubsituteVariables			= true;
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

		/**
		 *
		 * \brief Code
		 *
		 * \details
		 * Liefert den Wert (S oder V) f�r einen PriceType.
		 *
		 * \return String
		 *
		 */
		public int Code() {
			return intType;
		}
	}
	private StringBuffer				strBuffer						= new StringBuffer("");
	public String						TestVar							= "Wert von TestVar";
	/**
	 * \var conEnvVarJS_TEST_MODE
	 *
	 * Mit dieser EnvironmentVariablen kann der Test-Modus ein- bzw. ausgeschaltet werden.
	 * Die Variable ist vom Typ boolean.
	 *
	 * \see TestMode
	 * \see TestMode(boolean)
	 */
	private static String				conEnvVarJS_TEST_MODE			= new String("JS_TEST_MODE");
	protected final String				conNullButMandatory				= "%3$s : Setting %1$s (%2$s) is mandatory, must be not null.%n";
	protected final String				conChangedMsg					= "%3$s: changed from '%1$s' to '%2$s'.";
	/** HashMap Settings: HashMap contains the JS-Options (Settings) */
	protected HashMap<String, String>	objSettings						= null;
	protected HashMap<String, String>	objProcessedOptions				= null;
	protected Class						objClassName4PreferenceStore	= this.getClass();
	// global Option: DateFormatMask - Mask for a general date format in JADE
	// global Option: TimeFormatMask - Mask for a general time format in JADE
	/**
	 * \option BaseDirectory
	 * \type SOSOptionFolderName
	 * \brief BaseDirectory - A Base Directory for all relative FileNames
	 *
	 * \details
	 * A Base Directory for all relative FileNames used by SOSOptionFileName
	 *
	 * \mandatory: false
	 *
	 * \created 28.03.2014 14:51:50 by KB
	 */
	@JSOptionDefinition(name = "BaseDirectory", description = "A Base Directory for all relative FileNames used by SOSOptionFileName", key = "Base_Directory", type = "SOSOptionFolderName", mandatory = false)
	public SOSOptionFolderName			BaseDirectory					= new SOSOptionFolderName( // ...
																				this, // ....
																				conClassName + ".Base_Directory", // ...
																				"A Base Directory for all relative FileNames used by SOSOptionFileName", // ...
																				"env:user.dir", // ...
																				"env:user.dir", // ...
																				false);

	public SOSOptionFolderName getBaseDirectory() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getBaseDirectory";
		return BaseDirectory;
	} // public String getBaseDirectory

	public JSOptionsClass setBaseDirectory(final SOSOptionFolderName pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setBaseDirectory";
		BaseDirectory = pstrValue;
		return this;
	} // public JSOptionsClass setBaseDirectory
	/**
	 * \option DateFormatMask
	 * \type SOSOptionString
	 * \brief DateFormatMask - General Mask for date fomatting
	 *
	 * \details
	 * General Mask for date fomatting
	 *
	 * \mandatory: false
	 *
	 * \created 28.03.2014 12:30:53 by KB
	 */
	@JSOptionDefinition(name = "DateFormatMask", description = "General Mask for date fomatting", key = "Date_Format_Mask", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	DateFormatMask	= new SOSOptionString( // ...
													this, // ....
													conClassName + ".Date_Format_Mask", // ...
													"General Mask for date fomatting", // ...
													"yyyy-MM-dd", // ...
													"yyyy-MM-dd", // ...
													false);

	public SOSOptionString getDateFormatMask() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getDateFormatMask";
		return DateFormatMask;
	} // public String getDateFormatMask

	public JSOptionsClass setDateFormatMask(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setDateFormatMask";
		DateFormatMask = pstrValue;
		return this;
	} // public JSOptionsClass setDateFormatMask
	/**
	 * \option TimeFormatMask
	 * \type SOSOptionString
	 * \brief TimeFormatMask - General Mask for time formatting
	 *
	 * \details
	 * General Mask for time formatting
	 *
	 * \mandatory: false
	 *
	 * \created 28.03.2014 12:08:23 by KB
	 */
	@JSOptionDefinition(name = "TimeFormatMask", description = "General Mask for time formatting", key = "Time_Format_Mask", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	TimeFormatMask	= new SOSOptionString( // ...
													this, // ....
													conClassName + ".Time_Format_Mask", // ...
													"General Mask for time formatting", // ...
													"HH:mm:ss", // ...
													"HH:mm:ss", // ...
													false);

	public SOSOptionString getTimeFormatMask() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTimeFormatMask";
		return TimeFormatMask;
	} // public String getTimeFormatMask

	public JSOptionsClass setTimeFormatMask(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setTimeFormatMask";
		TimeFormatMask = pstrValue;
		return this;
	} // public JSOptionsClass setTimeFormatMask
	/**
	 * \option Scheduler_Hot_Folder
	 * \type SOSOptionFolderName
	 * \brief Scheduler_Hot_Folder - Pathname to the JobScheduler live-folder
	 *
	 * \details
	 * Pathname to the JobScheduler live-folder
	 *
	 * \mandatory: false
	 *
	 * \created 27.06.2012 17:31:46 by KB
	 */
	@JSOptionDefinition(name = "Scheduler_Hot_Folder", description = "Pathname to the JobScheduler live-folder", key = "Scheduler_Hot_Folder", type = "SOSOptionFolderName", mandatory = true)
	public SOSOptionFolderName	Scheduler_Hot_Folder	= new SOSOptionFolderName(
														// ...
																this, // ....
																conClassName + ".Scheduler_Hot_Folder", // ...
																"Pathname to the JobScheduler live-folder", // ...
																"${SCHEDULER_DATA}/config/live", // ...
																"", // ...
																true);

	public String getScheduler_Hot_Folder() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getScheduler_Hot_Folder";
		return Scheduler_Hot_Folder.Value();
	} // public String getScheduler_Hot_Folder

	public JSOptionsClass setScheduler_Hot_Folder(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setScheduler_Hot_Folder";
		Scheduler_Hot_Folder.Value(pstrValue);
		return this;
	} // public JSOptionsClass setScheduler_Hot_Folder
	/**
	 * \option Scheduler_Data
	 * \type SOSOptionFolderName
	 * \brief Scheduler_Data - Data Folder of JobScheduler Installation
	 *
	 * \details
	 * Data Folder of JobScheduler Installation
	 *
	 * \mandatory: false
	 *
	 * \created 27.06.2012 17:07:08 by KB
	 */
	@JSOptionDefinition(name = "Scheduler_Data", description = "Data Folder of JobScheduler Installation", key = "Scheduler_Data", type = "SOSOptionFolderName", mandatory = false)
	public SOSOptionFolderName	Scheduler_Data	= new SOSOptionFolderName(
												// ...
														this, // ....
														conClassName + ".Scheduler_Data", // ...
														"Data Folder of JobScheduler Installation", // ...
														"env:SCHEDULER_DATA", // ...
														"env:SCHEDULER_DATA", // ...
														false);

	public String getScheduler_Data() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getScheduler_Data";
		return Scheduler_Data.Value();
	} // public String getScheduler_Data

	public JSOptionsClass setScheduler_Data(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setScheduler_Data";
		Scheduler_Data.Value(pstrValue);
		return this;
	} // public JSOptionsClass setScheduler_Data
	/**
	 * \option Scheduler_Home
	 * \type SOSOptionFileName
	 * \brief Scheduler_Home - Home/Root Folder of JobScheduler
	 *
	 * \details
	 * OptionDescription
	 *
	 * \mandatory: true
	 *
	 * \created 27.06.2012 16:56:45 by KB
	 */
	@JSOptionDefinition(name = "Scheduler_Home", description = "Home Root Folder of JobScheduler", key = "Scheduler_Home", type = "SOSOptionFileName", mandatory = true)
	public SOSOptionFolderName	Scheduler_Home	= new SOSOptionFolderName(
												// ...
														this, // ....
														conClassName + ".Scheduler_Home", // ...
														"Home Root Folder of JobScheduler", // ...
														"env:SCHEDULER_HOME", // ...
														"env:SCHEDULER_HOME", // ...
														false);

	public String getScheduler_Home() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getScheduler_Home";
		return Scheduler_Home.Value();
	} // public String getScheduler_Home

	public JSOptionsClass setScheduler_Home(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setScheduler_Home";
		Scheduler_Home.Value(pstrValue);
		return this;
	} // public JSOptionsClass setScheduler_Home
	@JSOptionDefinition(name = "Local_user", description = "I18N is for internationalization of Application", key = "Local_user", type = "SOSOptionUserName", mandatory = true)
	public SOSOptionUserName	UserName	= new SOSOptionUserName(
											// ...
													this, // ....
													conClassName + ".local_user", // ...
													"Name of local user", // ...
													System.getProperty("user.name"), // ...
													System.getProperty("user.name"), // ...
													true);
	/**
	 * \option Locale
	 * \type SOSOptionLocale
	 * \brief Locale - Define the locale for the I18N-Subsystem
	 *
	 * \details
	 * I18N is for internationalization of Applications
	 *
	 * \mandatory: true
	 *
	 * \created 14.05.2011 17:42:51 by KB
	 */
	@JSOptionDefinition(name = "Locale", description = "I18N is for internationalization of Application", key = "Locale", type = "SOSOptionString", mandatory = true)
	public SOSOptionLocale		Locale		= new SOSOptionLocale(
											// ...
													this, // ....
													conClassName + ".Locale", // ...
													"I18N is for internationalization of Application", // ...
													"env:SOS_LOCALE", // ...
													java.util.Locale.getDefault().toString(), // ...
													true);

	public java.util.Locale getI18NLocale() {
		return new java.util.Locale(Locale.Value());
	}

	public String getLocale() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getLocale";
		return Locale.Value();
	} // public String getLocale

	@Override
	public void setLocale(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setLocale";
		Locale.Value(pstrValue);
	} // public JSOptionsClass setLocale
	/**
	 * \option CheckNotProcessedOptions
	 * \type SOSOptionBoolean
	 * \brief CheckNotProcessedOptions - Check and report not processed options
	 *
	 * \details
	 * If this Option is set to true, all not processed or recognized options are reported as a warning
	 *
	 * \mandatory: false
	 *
	 * \created 22.03.2011 11:49:13 by KB
	 */
	@JSOptionDefinition(name = "CheckNotProcessedOptions", description = "If this Option is set to true, all not processed or recognized options are reported as a warning", key = "CheckNotProcessedOptions", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	CheckNotProcessedOptions	= new SOSOptionBoolean(
																// ...
																this, // ....
																conClassName + ".CheckNotProcessedOptions", // ...
																"If this Option is set to true, all not processed or recognized options are reported as a warning", // ...
																"false", // ...
																"false", // ...
																false);

	public String getCheckNotProcessedOptions() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getCheckNotProcessedOptions";
		return CheckNotProcessedOptions.Value();
	} // public String getCheckNotProcessedOptions

	public JSOptionsClass setCheckNotProcessedOptions(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setCheckNotProcessedOptions";
		CheckNotProcessedOptions.Value(pstrValue);
		return this;
	} // public JSOptionsClass setCheckNotProcessedOptions
	/**
	 * \option XmlId
	 * \type SOSOptionString
	 * \brief XmlId - The ID-Attribute of the XML-Base tag for Serialization
	 *
	 * \details
	 * This ist the ...
	 *
	 * \mandatory: true
	 *
	 * \created 07.01.2011 18:56:23 by KB
	 */
	@JSOptionDefinition(name = "XmlId", description = "This ist the ...", key = "XmlId", type = "SOSOptionString", mandatory = true)
	public SOSOptionString	XmlId	= new SOSOptionString(
									// ...
											this, // ....
											conClassName + ".XmlId", // ...
											"This ist the ...", // ...
											"root", // ...
											"root", // ...
											true);

	public String getXmlId() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getXmlId";
		return XmlId.Value();
	} // public String getXmlId

	public JSOptionsClass setXmlId(final String pstrValue) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setXmlId";
		XmlId.Value(pstrValue);
		return this;
	} // public JSOptionsClass setXmlId
	@JSOptionDefinition(name = "ArchiverOptions", value = "", description = "Optionen f�r die Dateiarchivierung", key = "", type = "JSOptionClass", mandatory = false)
	private JSArchiverOptions	objArchiverOptions	= null;
	/**
	 * \brief TestMode - Option: Test Modus schalten
	 *
	 * \details
	 *
	 */
	@JSOptionDefinition(name = "TestMode", value = "false", description = "Test Modus schalten ", key = "TestMode", type = "JSOptionBoolean", mandatory = false)
	public SOSOptionBoolean		TestMode			= new SOSOptionBoolean(this, // Verweis auf die
															// SOSOptionClass-Instanz
															conClassName + ".TestMode", // Schl�ssel, i.d.r. identisch
															// mit dem Namen der Option
															"Test Modus schalten ", // Kurzbeschreibung
															"false", // Wert
															"false", // defaultwert
															false // Option muss einen Wert haben
													);
	/**
	 * \brief Debug - Option: Debug-Modus schalten (true/false)
	 *
	 * \details
	 *
	 */
	@JSOptionDefinition(name = "Debug", value = "false", description = "Debug-Modus schalten (true/false)", key = "Debug", type = "JSOptionBoolean", mandatory = false)
	public SOSOptionBoolean		Debug				= new SOSOptionBoolean(this, // Verweis auf die
															// SOSOptionClass-Instanz
															conClassName + ".Debug", // Schl�ssel, i.d.r. identisch mit
															// dem Namen der Option
															"Debug-Modus schalten (true/false)", // Kurzbeschreibung
															"false", // Wert
															"false", // defaultwert
															false // Option muss einen Wert haben
													);
	/**
	 * \brief DebugLevel - Option: DebugLevel
	 *
	 * \details
	 *
	 */
	@JSOptionDefinition(name = "DebugLevel", value = "0", description = "DebugLevel", key = "DebugLevel", type = "JSOptionInteger", mandatory = false)
	public SOSOptionInteger		DebugLevel			= new SOSOptionInteger(this, // Verweis auf die
															// SOSOptionClass-Instanz
															conClassName + ".DebugLevel", // Schl�ssel, i.d.r. identisch
															// mit dem Namen der Option
															"DebugLevel", // Kurzbeschreibung
															"0", // Wert
															"0", // defaultwert
															false // Option muss einen Wert haben
													);
	/** \var int DebugLevel: Specify the Debug-Level */
	// private int objDebugLevel = 0;
	// private final String conDebugLevelSettingsKey = this.conClassName + ".DebugLevel";
	/**
	 * \option log_filename
	 * \type SOSOptionFileName
	 * \brief log_filename - Name der Datei mit den Logging Eintr�gen
	 *
	 * \details
	 * Name der Datei mit den Logging-Eintr�gen
	 *
	 * \mandatory: false
	 *
	 * \created 20.01.2012 10:24:18 by KB
	 */
	@JSOptionDefinition(name = "log_filename", description = "Name der Datei mit den Logging-Eintr�gen", key = "log_filename", type = "SOSOptionFileName", mandatory = false)
	public SOSOptionLogFileName	log_filename		= new SOSOptionLogFileName(
													// ...
															this, // ....
															conClassName + ".log_filename", // ...
															"Name der Datei mit den Logging-Eintr�gen", // ...
															"stdout", // ...
															"stdout", // ...
															false);

	public SOSOptionLogFileName getlog_filename() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getlog_filename";
		return log_filename;
	} // public String getlog_filename

	public void setlog_filename(final SOSOptionLogFileName pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setlog_filename";
		log_filename = pstrValue;
		// return this;
	} // public JSOptionsClass setlog_filename
	/**
	 * \option log4jPropertyFileName
	 * \type SOSOptionInFileName
	 * \brief log4jPropertyFileName - Name of the Log4J Property File
	 *
	 * \details
	 * Name of the LOG4J Property File
	 *
	 * \mandatory: false
	 *
	 * \created 20.01.2012 10:12:02 by KB
	 */
	@JSOptionDefinition(name = "log4jPropertyFileName", description = "Name of the LOG4J Property File", key = "log4j_Property_FileName", type = "SOSOptionInFileName", mandatory = false)
	public SOSOptionLog4JPropertyFile	log4jPropertyFileName	= new SOSOptionLog4JPropertyFile(
																// ...
																		this, // ....
																		conClassName + ".log4j_Property_FileName", // ...
																		"Name of the LOG4J Property File", // ...
																		"env:log4j.configuration", // ...
																		"./log4j.properties", // ...
																		false);

	public SOSOptionLog4JPropertyFile log4jPropertyFileName() {
		return log4jPropertyFileName;
	}

	public void log4jPropertyFileName(SOSOptionLog4JPropertyFile pobjO) {
		log4jPropertyFileName = pobjO;
	}

	public String getlog4jPropertyFileName() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getlog4jPropertyFileName";
		return log4jPropertyFileName.Value();
	} // public String getlog4jPropertyFileName

	public JSOptionsClass setlog4jPropertyFileName(final String pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setlog4jPropertyFileName";
		log4jPropertyFileName.Value(pstrValue);
		return this;
	} // public JSOptionsClass setlog4jPropertyFileName
	/**
	 * \option ApplicationName
	 * \type SOSOptionString
	 * \brief ApplicationName - Name of the Application
	 *
	 * \details
	 * Name of the Application
	 *
	 * \mandatory: false
	 *
	 * \created 07.05.2014 17:15:00 by KB
	 */
	@JSOptionDefinition(name = "ApplicationName", description = "Name of the Application", key = "ApplicationName", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	ApplicationName	= new SOSOptionString( // ...
													this, // ....
													conClassName + ".ApplicationName", // ...
													"Name of the Application", // ...
													"env:SOSApplicationName", // ...
													"env:SOSApplicationName", // ...
													false);

	public SOSOptionString ApplicationName() {
		return ApplicationName;
	}

	public SOSOptionString getApplicationName() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getApplicationName";
		return ApplicationName;
	} // public String getApplicationName

	public JSOptionsClass setApplicationName(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setApplicationName";
		ApplicationName = pstrValue;
		return this;
	} // public JSOptionsClass setApplicationName
	/**
	 * \option ApplicationDocuUrl
	 * \type SOSOptionUrl
	 * \brief ApplicationDocuUrl - The Url of the Documentation of this Application
	 *
	 * \details
	 * The Url of the Documentation of this Application
	 *
	 * \mandatory: false
	 *
	 * \created 07.05.2014 17:16:17 by KB
	 */
	@JSOptionDefinition(name = "ApplicationDocuUrl", description = "The Url of the Documentation of this Application", key = "ApplicationDocuUrl", type = "SOSOptionUrl", mandatory = false)
	public SOSOptionUrl	ApplicationDocuUrl	= new SOSOptionUrl( // ...
													this, // ....
													conClassName + ".ApplicationDocuUrl", // ...
													"The Url of the Documentation of this Application", // ...
													"env:SOSApplicationDocuUrl", // ...
													"env:SOSApplicationDocuUrl", // ...
													false);

	public SOSOptionUrl getApplicationDocuUrl() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getApplicationDocuUrl";
		return ApplicationDocuUrl;
	} // public String getApplicationDocuUrl

	public JSOptionsClass setApplicationDocuUrl(final SOSOptionUrl pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setApplicationDocuUrl";
		ApplicationDocuUrl = pstrValue;
		return this;
	} // public JSOptionsClass setApplicationDocuUrl
	/**
	 * \option AllowEmptyParameterList
	 * \type SOSOptionBoolean
	 * \brief AllowEmptyParameterList - If true, an empty parameter list leads not into an error
	 *
	 * \details
	 * If true, an empty parameter list leads not into an error
	 *
	 * \mandatory: false
	 *
	 * \created 07.05.2014 17:19:04 by KB
	 */
	@JSOptionDefinition(name = "AllowEmptyParameterList", description = "If true, an empty parameter list leads not into an error", key = "AllowEmptyParameterList", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	AllowEmptyParameterList	= new SOSOptionBoolean( // ...
															this, // ....
															conClassName + ".AllowEmptyParameterList", // ...
															"If true, an empty parameter list leads not into an error", // ...
															"true", // ...
															"true", // ...
															false);

	public SOSOptionBoolean getAllowEmptyParameterList() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getAllowEmptyParameterList";
		return AllowEmptyParameterList;
	} // public String getAllowEmptyParameterList

	public JSOptionsClass setAllowEmptyParameterList(final SOSOptionBoolean pstrValue) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllowEmptyParameterList";
		AllowEmptyParameterList = pstrValue;
		return this;
	} // public JSOptionsClass setAllowEmptyParameterList
	/** \var boolean IgnoreEnvironmentVariables: Environment-Variable nicht auswerten */
	private boolean						flgIgnoreEnvironmentVariables	= false;
	// private final String conIgnoreEnvironmentVariablesSettingsKey = conClassName + ".Ignore_Environment_Variables";
	/** spooler_log Logger: The global SOS-Logging Class */
	// private sos.spooler.Log objLogger = null;
	protected boolean					flgSetAllOptions				= false;
	private final String				strOptionNamePrefix				= "-";
	private String						strCommandLineArgs[];
	/** String CurrentNodeName: Logischer Name f�r eine System-Instanz */
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
			// TODO: handle exception
		}
	} // public JSOptionsClass

	public JSOptionsClass(final HashMap<String, String> pobjSettings) {
		this();
		this.Settings(pobjSettings);
		ArchiverOptions();
	}

	public String getPrefix() {
		return strAlternativePrefix;
	}

	protected String concatIfNotEmpty(final String pstrValue) {
		if (isNotEmpty(pstrValue)) {
			return "\n" + pstrValue;
		}
		return pstrValue;
	}

	/**
	 * ---------------------------------------------------------------------------
	 * <method type="smcw" version="1.0"> <name>Settings</name> <title>HashMap
	 * containing the JS-Settings</title> <description> <para> HashMap
	 * conaining the JS-Settings </para> </description> <params> </params>
	 * <keywords> <keyword>Options</keyword> <keyword>BasisClass</keyword>
	 * </keywords> <categories> <category>Options</category> </categories>
	 * </method>
	 * ----------------------------------------------------------------------------
	 */
	/**
	 * \brief HashMap contains the JS-Settings
	 *
	 * @return Returns the Settings.
	 */
	public HashMap<String, String> Settings() {
		if (objSettings == null) {
			objSettings = new HashMap<String, String>();
		}
		return objSettings;
	} // HashMap Settings()

	public HashMap<String, String> Settings4StepName() {
		HashMap<String, String> objS = new HashMap<String, String>();
		int intStartPos = strCurrentNodeName.length() + 1;
		for (final Map.Entry<String, String> mapItem : Settings().entrySet()) {
			String strMapKey = mapItem.getKey();
			String strValue = mapItem.getValue();
			if (strMapKey.indexOf("/") != -1) {
				if (strMapKey.startsWith(strCurrentNodeName + "/")) {
					strMapKey = strMapKey.substring(intStartPos);
				}
				else {
					strValue = null;
				}
			}
			if (strValue != null) {
				logger.debug(strMapKey + " = " + strValue);
				objS.put(strMapKey, strValue);
			}
		}
		return objS;
	} // HashMap Settings()

	/**
	 *
	 * \brief TempDirName
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String TempDirName() {
		return this.TempDir();
	}

	/**
	 *
	 * \brief TempDir
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String TempDir() {
		return strTempDirName;
	}

	/**
	 *
	 * \brief TempDir
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrTempDirName
	 */
	public void TempDir(final String pstrTempDirName) {
		strTempDirName = pstrTempDirName;
	}

	/**
	 *
	 * \brief UserDir
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String UserDir() {
		return strUserDirName;
	}

	/**
	 *
	 * \brief UserDir
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrUserDirName
	 */
	public void UserDir(final String pstrUserDirName) {
		strUserDirName = pstrUserDirName;
	}

	/**
	 * \brief Settings - HashMap conaining the JS-Settings
	 *
	 * @param pobjSettings
	 *            The HashMap Settings to set.
	 * @throws Exception
	 */
	public void Settings(final HashMap<String, String> pobjSettings) {
		// setAllOptions(pobjSettings);
		objSettings = pobjSettings;
		setAllCommonOptions(pobjSettings);
	} // Settings(HashMap pobjSettings)

	/**
	 * \brief getItem - Wert f�r eine Option liefern
	 *
	 * \details
	 * Ermittelt den Wert f�r eine angegebene Option
	 *
	 * Der Name der Option [CurrentNodeName/][classname.]optionname kann entweder in
	 * kompletter Gro�-/Kleinschreibung
	 * oder in vollst�ndiger Kleinschreibung geschrieben werden. Dann wird
	 * er auch erkannt.
	 * classname darf weggelassen werden.
	 * 'CurrentNodeName' darf auch weggelassen werden.
	 *
	 * Durch die HashMap wird sequentiell durchgegangen, um auch MixedCase Schl�sselworte zu erwischen
	 * zus�tzlich evtl. bereits beim Schreiben in die HashMap alles auf reine Kleinschreibung umsetzen.
	 *
	 * \return String der Wert f�r die angeforderte Option oder null
	 *
	 * \see getIntItem
	 * \see getBoolItem
	 *
	 * @param pstrKey der Name der Option als [classname.]optionname
	 */
	public String getItem(final String pstrKey) {
		String strTemp = ""; // null;
		String strKey = pstrKey;
		if (isEmpty(pstrKey)) {
			return strTemp;
		}
		if (objProcessedOptions == null) {
			objProcessedOptions = new HashMap<String, String>();
		}
		strKey = strKey.replaceAll("_", "");
		/**
		 * durch die HashMap sequentiell durchgehen, um auch MixedCase Schl�sselworte zu erwischen
		 * zus�tzlich evtl. bereits beim Schreiben in die HashMap alles auf reine Kleinschreibung umsetzen.
		 */
		String strLSKey = "";
		if (strCurrentNodeName.length() > 0) {
			strLSKey = strCurrentNodeName + "/" + pstrKey.replaceAll("_", "");
			for (final Object element : objSettings.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				String strMapKey = mapItem.getKey().toString();
				strMapKey = strMapKey.replaceAll("_", "");
				if (strLSKey.equalsIgnoreCase(strMapKey)) {
					if (mapItem.getValue() != null) {
						strTemp = mapItem.getValue().toString();
					}
					else {
						strTemp = null;
					}
					objProcessedOptions.put(strMapKey, strTemp);
					return strTemp;
				}
			}
		} // if (strCurrentNodeName.length() > 0 )
		for (final Object element : objSettings.entrySet()) {
			final Map.Entry mapItem = (Map.Entry) element;
			String strMapKey = mapItem.getKey().toString();
			String lstrMapKey = strMapKey.replaceAll("_", "");
			if (strKey.equalsIgnoreCase(lstrMapKey)) {
				if (mapItem.getValue() != null) {
					strTemp = mapItem.getValue().toString();
				}
				else {
					strTemp = null;
				}
				objProcessedOptions.put(strMapKey, strTemp);
				return strTemp;
			}
		}
		/**
		 * wenn wir hier angekommen sind, dann wurde mit dem "richtigen" Wert (in Gro�-/Kleinschreibung und
		 * mit Klassennamen) nichts gefunden. Jetzt wird ohne Klassenname im Schl�ssel gesucht.
		 */
		int i = strKey.indexOf('.'); // KlassenName . Name
		if (i > 0) { // Key contains/started with ClassName ...
			strLSKey = "";
			strKey = strKey.substring(++i); // get key without classname
			if (strCurrentNodeName.length() > 0) {
				strLSKey = strCurrentNodeName + "/" + strKey;
				for (final Object element : objSettings.entrySet()) {
					final Map.Entry mapItem = (Map.Entry) element;
					String strMapKey = mapItem.getKey().toString();
					String lstrMapKey = strMapKey.replaceAll("_", "");
					if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
						if (mapItem.getValue() != null) {
							strTemp = mapItem.getValue().toString();
						}
						else {
							strTemp = null;
						}
						objProcessedOptions.put(strMapKey, strTemp);
						return strTemp;
					}
				} // for (Iterator iterator = objSettings.entrySet().iterator(); iterator.hasNext();)
			}
			for (final Object element : objSettings.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				String strMapKey = mapItem.getKey().toString();
				String lstrMapKey = strMapKey.replaceAll("_", "");
				if (strLSKey.length() > 0) {
					if (strLSKey.equalsIgnoreCase(lstrMapKey)) {
						if (mapItem.getValue() != null) {
							strTemp = mapItem.getValue().toString();
						}
						else {
							strTemp = null;
						}
						objProcessedOptions.put(strMapKey, strTemp);
						return strTemp;
					}
				}
				if (strKey.equalsIgnoreCase(lstrMapKey)) {
					if (mapItem.getValue() != null) {
						strTemp = mapItem.getValue().toString();
					}
					else {
						strTemp = null;
					}
					objProcessedOptions.put(strMapKey, strTemp);
					return strTemp;
				}
			} // for (Iterator iterator = objSettings.entrySet().iterator(); iterator.hasNext();)
		} // (i > 0)
			// hier kann es nur noch "null" sein, also Schl�ssel nicht gefunden in den Optionen
		if (strTemp == null || strTemp.length() <= 0) {
			strTemp = null;
		}
		return strTemp;
	} // protected String getItem(String pstrKey)

	/**
	 *
	 * \brief ReportNotProcessedOptions
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @return
	 */
	public boolean ReportNotProcessedOptions() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CheckNotProcessedOptions";
		boolean flgIsOK = true;
		int intNumberOfNotProcessedOptions = 0;
		if (objSettings != null) {
			for (final Map.Entry<String, String> mapItem : objSettings.entrySet()) {
				String strMapKey = mapItem.getKey().toString();
				String strT = objProcessedOptions.get(strMapKey);
				if (strT == null) {
					String strValue = null;
					if (mapItem.getValue() != null) {
						strValue = mapItem.getValue().toString();
					}
					else {
						strValue = null;
					}
					logger.warn(String.format("SOSOPT-W-001: Option '%1$s' with value '%2$s' is unknown and not processed", strMapKey, strValue));
					flgIsOK = false;
					intNumberOfNotProcessedOptions++;
				}
			}
		}
		return flgIsOK;
	} // private boolean CheckNotProcessedOptions

	public HashMap<String, String> getProcessedOptions() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getProcessedOptions";
		if (objProcessedOptions == null) {
			objProcessedOptions = new HashMap<String, String>();
		}
		return objProcessedOptions;
	} // public HashMap<String, String> getProcessedOptions

	public void addProcessedOptions(final HashMap<String, String> phsmMap) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::addProcessedOptions";
		this.getProcessedOptions();
		objProcessedOptions.putAll(phsmMap);
	} // public void addProcessedOptions

	/**
	 * \brief getItem - Wert oder Vorbesetzung f�r eine Option liefern
	 *
	 * \details
	 * Ermittelt den Wert f�r eine angegebene Option
	 *
	 * Der Name der Option [classname.]optionname kann entweder in kompletter Gro�-/Kleinschreibung
	 * oder in vollst�ndiger Kleinschreibung geschrieben werden. Dann wird
	 * er auch erkannt. classname darf weggelassen werden.
	 *
	 * Wenn die Option nicht definiert wurde (in der HasMap), dann wird der Wert f�r die Vorbesetzung
	 * zur�ckgeliefert.
	 *
	 * \return String der Wert f�r die angeforderte Option oder Vorbesetzung oder null
	 *
	 * @see getItem
	 * \see getIntItem
	 * \see getBoolItem
	 *
	 * @param String pstrKey der Name der Option als [classname.]optionname
	 * @param String pstrDefaultValue die Vorbesetzung (Default-Value) der Option
	 */
	public String getItem(final String pstrKey, final String pstrDefaultValue) {
		String strT = this.getItem(pstrKey);
		if (isEmpty(strT)) {
			strT = pstrDefaultValue;
		}
		return strT;
	}

	/**
	 * \brief getIntItem - IntegerWert f�r eine Option liefern
	 *
	 * \details
	 * Ermittelt den Integer-Wert f�r eine angegebene Option
	 *
	 * Der Name der Option [classname.]optionname kann entweder in kompletter Gro�-/Kleinschreibung
	 * oder in vollst�ndiger Kleinschreibung geschrieben werden. Dann wird
	 * er auch erkannt. classname darf weggelassen werden.
	 *
	 * \exception NumberFormatException
	 *
	 * \return String der Wert f�r die angeforderte Option oder null
	 *
	 * \see getItem
	 * \see getBoolItem
	 *
	 * @param pstrKey der Name der Option als [classname.]optionname
	 */
	protected int getIntItem(final String pstrKey) {
		return String2Integer(this.getItem(pstrKey));
	}

	/**
	 * \brief String2Integer - wandelt String in Integer
	 *
	 * \details
	 *
	 * \exception NumberFormatException
	 *
	 * \return int
	 *
	 * @param pstrValue
	 */
	protected int String2Integer(final String pstrValue) {
		int intT = 0;
		if (isNotEmpty(pstrValue)) {
			try {
				intT = Integer.parseInt(pstrValue);
			}
			catch (final NumberFormatException e) {
				throw new RuntimeException("format Exception raised", e);
			}
		}
		return intT;
	}

	/**
	 * \brief getBoolItem - Ermittelt den Boolean-Wert f�r eine angegebene Option
	 *
	 * Der Name der Option [classname.]optionname kann entweder in kompletter Gro�-/Kleinschreibung
	 * oder in vollst�ndiger Kleinschreibung geschrieben werden. Dann wird
	 * er auch erkannt. classname darf weggelassen werden.
	 *
	 * Die Strings "1" und "y" liefern den Wert "true", alle anderen Werte werden als "false"
	 * interpretiert.
	 *
	 * \return String der Wert f�r die angeforderte Option oder null
	 *
	 * \see getItem
	 * \see getIntItem
	 *
	 * @param pstrKey der Name der Option als [classname.]optionname
	 */
	protected boolean getBoolItem(final String pstrKey) {
		boolean flgT = false;
		if (isNotEmpty(pstrKey)) {
			flgT = String2Bool(this.getItem(pstrKey));
		}
		return flgT;
	}

	/**
	 *
	 * \brief getBoolItem
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @param pstrKey
	 * @param pflgDefault
	 * @return
	 */
	protected boolean getBoolItem(final String pstrKey, final boolean pflgDefault) {
		boolean flgT = false;
		if (isNotEmpty(pstrKey)) {
			flgT = String2Bool(this.getItem(pstrKey));
		}
		else {
			flgT = pflgDefault;
		}
		return flgT;
	}

	/**
	 *
	 * \brief String to Boolean
	 *
	 *
	 * \return boolean
	 *
	 * @param pstrS
	 */
	@Override
	public boolean String2Bool(final String pstrVal) {
		boolean flgT = false;
		if (isNotEmpty(pstrVal)) {
			if (pstrVal.equals("1") || pstrVal.equalsIgnoreCase("y") || pstrVal.equalsIgnoreCase("yes") || pstrVal.equalsIgnoreCase("j")
					|| pstrVal.equalsIgnoreCase("on") || pstrVal.equalsIgnoreCase("true") || pstrVal.equalsIgnoreCase("wahr")) {
				flgT = true;
			}
		}
		return flgT;
	}

	/**
	 *
	 * \brief toOut
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrS
	 * @throws Exception
	 */
	protected void toOut(final String pstrS) throws Exception {
		String strT = pstrS;
		if (!strT.contains(conClassName)) {
			strT += getAllOptionsAsString();
		}
		// if (objLogger != null) {
		// objLogger.info(strT);
		// }
		// else {
		System.out.println(strT);
		// }
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
		String strT = null;
		try {
			strT = getAllOptionsAsString();
		}
		catch (final Exception e) {
			throw new JobSchedulerException("toString failed", e);
		}
		return strT;
	}

	public String dirtyString() {
		//		String strT = objParentClass.toString() + " -> " + objParentClass.hashCode();
		String strT = "";
		try {
			strT += "\n" + getAllOptionsAsString(IterationTypes.DirtyToString);
		}
		catch (final Exception e) {
			throw new JobSchedulerException("dirtyString failed", e);
		}
		return strT;
	}

	public void Options2ClipBoard() {
		final String conMethodName = conClassName + "::Options2ClipBoard";
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
		}
		else {
			strT = this.dirtyString();
		}
		System.out.println(strT);
	}

	/**
	 *
	 * \brief getAllOptionsAsString
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 * @throws Exception
	 */
	private String getAllOptionsAsString(final IterationTypes penuIterationType) {
		String strT = ""; // conClassName + "\n";
		if (objParentClass != null) {
			strT += IterateAllDataElementsByAnnotation(objParentClass, this, penuIterationType, new StringBuffer(""));
		}
		return strT;
	}

	private String getAllOptionsAsString() {
		return getAllOptionsAsString(IterationTypes.toString);
	}

	/**
	 * \brief setAllCommonOptions - Aus der HashMap werden die Werte �bernommen
	 *
	 * Optionen (auch Settings genannt) werden in Form einer HashMap mit den Paaren (Name, Wert)
	 * gehalten. Hier werden diese den Werten der jeweiligen Property zugewiesen.
	 *
	 * \return void
	 *
	 * @param JSSettings
	 * @throws Exception
	 */
	protected void setAllCommonOptions(final HashMap<String, String> JSSettings) {
		objSettings = JSSettings;
		if (objParentClass != null) {
			// && this.objParentClass.isAnnotationPresent(JSOptionClass.class)) {
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.countSegmentFields, strBuffer);
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setRecord, strBuffer);
		}
		else {
		}
		// this.TestMode(this.getBoolItem(this.conTestModeSettingsKey));
		// this.Debug(this.getBoolItem(this.conDebugSettingsKey));
		// this.DebugLevel(this.getIntItem(this.conDebugLevelSettingsKey));
		UserDir.MapValue(); // Wert aus der HashTable �bernehmen
		/**
		 * Die folgenden Variablen werden �ber das Environment gesetzt
		 * und �berschreiben die in den Settings gemachten Angaben.
		 *
		 */
		if (this.IgnoreEnvironmentVariables()) {
			; // nothing to do
		}
		else {
			getEnvironmentVariables();
		}
	}

	/**
	 *
	 * \brief getEnvironmentVariables
	 *
	 * \details
	 *
	 * \return JSOptionsClass
	 *
	 * @return
	 * @throws Exception
	 */
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

	/**
	 *
	 * \brief CheckMandatory
	 *
	 * \details
	 * Der Aufruf dieser Methode pr�ft alle obligatorisch anzugebenden Optionen.
	 * Wird f�r eine Option kein Wert gefunden, so wird eine Exception ausgel�st.
	 *
	 * Die Methode wird grunds�tzlich von der abgeleiteten Klasse �berschrieben,
	 * da die obligatorsichen Optionen von Klasse zu Klasse unterschiedlich sind.
	 *
	 * \return void
	 *
	 * @throws Exception
	 */
	public void CheckMandatory() throws Exception {
		if (objParentClass != null) {
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.CheckMandatory, strBuffer);
		}
	}

	/**
	 * \brief TestMode - TestMode
	 *
	 * @return Returns the TestMode.
	 */
	public boolean TestMode() {
		return TestMode.value();
	} // boolean TestMode()

	/**
	 * \brief TestMode - TestMode
	 *
	 * @param pflgTestMode
	 *            The boolean TestMode to set.
	 */
	public void TestMode(final boolean pflgTestMode) {
		TestMode.value(pflgTestMode);
	} // public void TestMode(boolean pflgTestMode)

	/**
	 * Debug - Debug-Mode on or off
	 *
	 * @return Returns the Debug.
	 */
	public boolean Debug() {
		return Debug.value();
	} // boolean Debug()

	/**
	 * Debug - Debug-Mode on or off
	 *
	 * @param pflgDebug
	 *            The boolean Debug to set.
	 */
	public void Debug(final boolean pflgDebug) {
		Debug.value(pflgDebug);
	} // public void Debug(boolean pflgDebug)

	/**
	 * \brief DebugLevel - Specify the Debug-Level
	 *
	 * \details
	 * Diagnosemeldungen werden �ber eine Level-Nummer klassifiziert.
	 * '9' bedeutet dabei am wenigstens wichtig und '0' kennzeichnet wichtige
	 * Diagnosemeldungen.
	 * Es werden alle Meldungen ausgegeben, deren Level-Nummer kleiner oder gleich
	 * dem DebugLevel ist.

	 * @return Returns the DebugLevel.
	 */
	public int DebugLevel() {
		return DebugLevel.value();
	} // int DebugLevel()

	/**
	 * \copydoc ::DebugLevel()
	 * @param pobjDebugLevel
	 *            The int DebugLevel to set.
	 */
	public void DebugLevel(final int pintDebugLevel) {
		DebugLevel.value(pintDebugLevel);
	} // public void DebugLevel(int pobjDebugLevel)

	/**
	 *
	 * \brief CheckFileIsReadable
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @param pstrFileName
	 * @param pstrMethodName
	 * @return
	 * @throws Exception
	 */
	protected String CheckFileIsReadable(final String pstrFileName, final String pstrMethodName) {
		if (isNotEmpty(pstrFileName)) {
			final File fleF = new File(pstrFileName);
			String strT = null;
			if (fleF.exists() == false) {
				strT = String.format("%2$s: File '%1$s' does not exist.", pstrFileName, pstrMethodName);
				this.SignalError(new JSExceptionInputFileNotFound(strT), strT);
			}
			if (fleF.canRead() == false) {
				strT = String.format("%2$s: File '%1$s'. canRead returns false. Check permissions.", pstrFileName, pstrMethodName);
				this.SignalError(new JSExceptionFileNotReadable(strT), strT);
			}
		}
		return pstrFileName;
	} // CheckFileIsReadable

	/**
	 *
	 * \brief CheckFolder
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @param pstrFileName
	 * @param pstrMethodName
	 * @param flgCreateIfNotExist
	 * @return
	 * @throws Exception
	 */
	public String CheckFolder(String pstrFileName, final String pstrMethodName, final Boolean flgCreateIfNotExist) {
		if (isNotEmpty(pstrFileName)) {
			final String strSep = System.getProperty("file.separator");
			if (pstrFileName.endsWith(strSep) == false) {
				pstrFileName += strSep;
			}
			final File fleF = new File(pstrFileName);
			if (fleF.exists() == false) {
				if (!flgCreateIfNotExist) {
					this.SignalError(String.format("%2$s: Folder '%1$s' does not exist.", pstrFileName, pstrMethodName));
				}
				else {
					fleF.mkdir();
					SignalInfo(String.format("%2$s: Folder '%1$s' created.", pstrFileName, pstrMethodName));
				}
			}
			if (fleF.canRead() == false) {
				this.SignalError(String.format("%2$s: File '%1$s'. canRead returns false. Check permissions.", pstrFileName, pstrMethodName));
			}
		}
		return pstrFileName;
	} // CheckFolder

	/**
	 *
	 * \brief CheckIsFileWritable - pr�ft ob eine Datei ge-/�berschrieben werden darf
	 *
	 * \return String
	 *
	 * @param pstrFileName
	 * @param pstrMethodName
	 * @return String - den Namen der Datei
	 * @throws Exception
	 */
	public String CheckIsFileWritable(final String pstrFileName, final String pstrMethodName) {
		String strT = null;
		if (isNotEmpty(pstrFileName)) {
			// message("pstrFileName = " + pstrFileName);
			try {
				final File fleF = new File(pstrFileName);
				if (fleF.exists()) {
				}
				else {
					fleF.createNewFile();
				}
				if (fleF.canWrite()) {
					// nothing to do
				}
				else {
					strT = String.format("%2$s: File '%1$s'. canWrite returns false. Check permissions.", pstrFileName, pstrMethodName);
					strT += fleF.toString();
				}
			} // try
			catch (final Exception objException) {
				strT = String.format("%2$s: File '%1$s'. Exception thrown. Check permissions.", pstrFileName, pstrMethodName);
				final JobSchedulerException objJSEx = new JobSchedulerException(strT, objException);
				this.SignalError(objJSEx, strT);
			}
			finally {
				//
			} // finally
			if (strT != null) {
				this.SignalError(strT);
			}
		}
		return pstrFileName;
	} // CheckFileIsWritable

	/**
	 *
	 * \brief getVal
	 *
	 * \return String
	 *
	 * @param pstrS
	 */
	protected String getVal(final String pstrS) {
		final String strT = "";
		if (pstrS == null) {
			return strT.toString();
		}
		return pstrS.toString();
	}

	/**
	 *
	 * \brief ArchiverOptions
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public JSArchiverOptions ArchiverOptions() {
		if (objArchiverOptions == null) {
			// TODO: falls objSettings = null, dann die Optionen einzeln setzen.
			objArchiverOptions = new JSArchiverOptions();
			objArchiverOptions.registerMessageListener(this);
			if (objSettings != null) {
				objArchiverOptions.setAllOptions(objSettings);
			}
		}
		return objArchiverOptions;
	}

	/**
	 * \brief NormalizeDirectoryName - Directory-Name normalisieren und substituieren
	 *
	 * \details
	 * Es wird ein "Path-Separator" an den Verzeichnisnamen angeh�ngt, falls dieser
	 * noch nicht als Zeichen vorhanden ist.
	 *
	 * Ausserdem werden Variable im Namen durch den aktuellen Wert ersetzt.
	 *
	 * \return String
	 *
	 * @param pstrDirectoryName
	 */
	protected String NormalizeDirectoryName(final String pstrDirectoryName) {
		String strT = pstrDirectoryName;
		if (strT == null || strT.trim().length() <= 0) {
			// strT = "/";
			strT = "";
		}
		else {
			if (strT.endsWith(File.separator) == false && strT.endsWith("/") == false) {
				strT += "/";
			}
			if (strT.startsWith("./")) {
				strT = strT.substring(2);
				try {
					strT = AbsolutFileName(strT);
				} // try
				catch (final Exception objException) {
					// TODO: handle exception
				}
				finally {
					//
				} // finally
			}
			strT = SubstituteVariables(strT);
		}
		return strT;
	}

	/**
	 *
	 * \brief NormalizeFileName - Variable im Dateinamen ersetzen
	 *
	 * \return String
	 *
	 * @param pstrFileName
	 */
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

	/**
	 *
	 * \brief AbsolutFileName - liefert einen absoluten Dateinamen f�r eine Datei
	 *
	 * \details

	 * \return String
	 *
	 * @param pstrFileName
	 * @return
	 * @throws Exception
	 */
	public String AbsolutFileName(final String pstrFileName) throws Exception {
		// String strA = new File(pstrFileName).getAbsolutePath();
		// return strA;
		// System.out.println("User.dir = " + System.getProperty("user.dir") );
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
			strT = strT.substring(2); // beginnt mit "0" zu z�hlen
		}
		strT = new File(strT).toURI().toURL().toString();
		if (strT.startsWith("file:")) {
			strT = strT.substring(5);
		}
		return strT;
	}

	/**
	 *
	 * \brief SubstituteVariables - Variable in den Parameter-Werten ersetzen
	 *
	 * @param pstrValue
	 * @return String Wert ohne Variable
	 */
	public String SubstituteVariables(final String pstrValue) {
		String strT = pstrValue;
		if (strT == null) {
			return null;
		}
		int i = -1;
		i = strT.indexOf("$");
		if (i >= 0) {
		}
		strT = strT.replace("//", "/");
		return strT;
	}

	/* ---------------------------------------------------------------------------
	<method type="smcw" version="1.0">
	<name>IgnoreEnvironmentVariables</name>
	<title>Environment-Variable nicht auswerten</title>
	<description>
	<para>
	Environment-Variable nicht auswerten
	</para>
	<para>
	Initial-Wert (Default) ist "false" (ohne Anf�hrungszeichen).
	</para>
	<mandatory>true</mandatory>
	</description>
	<params>
		<param name="param1" type=" " ref="byref|byvalue|out" >
			<para>
			</para>
		</param>
	</params>
	<keywords>
		<keyword>EnvironmentVariable</keyword>
	</keywords>
	<categories>
	<category>Schnittstellenmonitoring</category>
	</categories>
	</method>
	---------------------------------------------------------------------------- */
	/*!
	 * IgnoreEnvironmentVariables - Environment-Variable nicht auswerten
	 *
	 * Getter: Environment-Variable nicht auswerten
	 *
	 * Example:
	 *
	 * @return Returns the IgnoreEnvironmentVariables.
	 */
	public boolean IgnoreEnvironmentVariables() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::IgnoreEnvironmentVariables";
		return flgIgnoreEnvironmentVariables;
	} // boolean IgnoreEnvironmentVariables()

	/*!
	 * IgnoreEnvironmentVariables - Environment-Variable nicht auswerten
	 *
	 * Setter: Environment-Variable nicht auswerten
	 *
	 * @param pflgIgnoreEnvironmentVariables: The boolean IgnoreEnvironmentVariables to set.
	 */
	public JSOptionsClass IgnoreEnvironmentVariables(final boolean pflgIgnoreEnvironmentVariables) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::IgnoreEnvironmentVariables";
		flgIgnoreEnvironmentVariables = pflgIgnoreEnvironmentVariables;
		return this;
	} // public void IgnoreEnvironmentVariables(boolean pflgIgnoreEnvironmentVariables)

	/**
	 *
	 * \brief SplitString - Zerlegt einen String in ein Array
	 *
	 * \details
	 * Ein String, der durch die Zeichen ",", "|" oder ";" (Komma, senkrechter Strich, Semikolon)
	 * in Substrings unterteilt ist, wird als Array zur�ckgeliefert.
	 * Dabei ist jeder Substring ein Element des String-Arrays.
	 *
	 * \return String[]
	 *
	 * @param pstrStr
	 */
	protected String[] SplitString(final String pstrStr) {
		if (pstrStr == null) {
			return null;
		}
		return pstrStr.trim().split("[;|,]");
	}

	/**
	 * \brief CheckNull - L�st eine Exception aus, wenn wer Wert f�r eine Option "null" ist
	 *
	 * \return void
	 *
	 * @param pstrMethodName - Diese Methode ruft
	 * @param pstrTitel		- die Beschreibung der Option
	 * @param pstrOptionName - Der Name in den Settings
	 * @param pstrOptionValue - Der Wert
	 * @throws Exception
	 */
	protected void CheckNull(final String pstrMethodName, final String pstrTitel, final String pstrOptionName, final String pstrOptionValue) throws Exception {
		if (isEmpty(pstrOptionValue)) {
			this.SignalError(String.format(conNullButMandatory, pstrMethodName, pstrTitel, pstrOptionName));
		}
	}
	public String	gstrApplicationName		= "JobScheduler";
	public String	gstrApplicationDocuUrl	= "http://docu.sos-berlin.com";

	public void CommandLineArgs(final String pstrArgs) {
		StrTokenizer objT = new StrTokenizer(pstrArgs);
		String[] strA = objT.getTokenArray();
		CommandLineArgs(strA);
	}

	/**
	 *
	 * \brief CommandLineArgs - �bernehmen der Options/Settings aus der Kommandozeile
	 *
	 * \details

	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	public void CommandLineArgs(final String[] pstrArgs) {
		final String conMethodName = conClassName + "::CommandLineArgs ";
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
			if (flgOption == true) {
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
						strOptionValue = StripQuotes(strOptionValue);
						strOptionName = strOptionName.substring(0, intESPos);
						objSettings.put(strOptionName, strOptionValue);
						if(strOptionName.contains("password")){
						    this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, "*****"));
						}else{
						    this.SignalDebug(String.format("%1$s: Name = %2$s, Wert = %3$s", conMethodName, strOptionName, strOptionValue));
						}
						flgOption = true; // next tooken must be an option
					}
				}
				else {
					logger.warn(String.format("'%1$s' seems to be an unsupported (positional) parameter. ignored", strCommandLineArg));
				}
			}
			else {
				if (strOptionName != null) {
					strOptionValue = strCommandLineArg;
					flgOption = true;
					objSettings.put(strOptionName, strOptionValue);
					if(strOptionName.contains("password")){
			            this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName, "*****"));
					}else{
			            this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName, strOptionValue));
					}
					strOptionName = null;
				}
			}
		}
		final String strPropertyFileName = this.getItem("PropertyFileName", "");
		if (strPropertyFileName.length() > 0) {
			LoadProperties(strPropertyFileName);
			strOptionName = null;
			flgOption = true;
			for (final String strCommandLineArg : strCommandLineArgs) {
				if (flgOption == true) {
					if (strCommandLineArg.substring(0, l).equalsIgnoreCase(strOptionNamePrefix)) {
						strOptionName = strCommandLineArg.substring(l);
						flgOption = false;
					}
				}
				else {
					if (strOptionName != null) {
						strOptionValue = strCommandLineArg;
						flgOption = true;
						objSettings.put(strOptionName, strOptionValue);
						this.SignalDebug(String.format("%1$s: CmdSettings. Name = %2$s, value = %3$s", conMethodName, strOptionName, strOptionValue));
						strOptionName = null;
					}
				}
			}
			message(conMethodName + ": Property-File loaded. " + strPropertyFileName);
		}
		DumpSettings();
		// (hoffentlich) �berschrieben von der erbenden Klasse
		setAllOptions(objSettings);
	}

	/**
	 *
	 * \brief CommandLineArgs
	 *
	 * \details
	 * Mit dieser MEthode werden die Kommando-Zeilen-Argumente, wie sie beim Start des Programms angegeben und an diese
	 * Klasse �bergeben wurden, an die rufende Routine zur�ckgegeben.
	 *
	 * \return String[]
	 *
	 * @return
	 */
	public String[] CommandLineArgs() {
		return strCommandLineArgs;
	}

	/**
	 *
	 * \brief LoadProperties - Optionen aus Property-Datei laden
	 *
	 * \details
	 * L�dt die Werte f�r die Optionen aus einer Java-Properties-Datei.
	 * Der Name der Datei ist als Parameter anzugeben.
	 *
	 * \return void
	 *
	 * @param pstrPropertiesFileName
	 * @throws Exception
	 */
	public void LoadProperties(final String pstrPropertiesFileName) {
		final String conMethodName = conClassName + "::LoadProperties ";
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
					if (strTemp != null && strTemp.length() > 0 && strTemp.equalsIgnoreCase(".") == false) {
						objSettings.put(strMapKey, strTemp);
					}
				}
			}
			// (hoffentlich) �berschrieben von der erbenden Klasse
			message(conMethodName + ": Property-File loaded");
			setAllOptions(objSettings);
			setAllCommonOptions(objSettings);
		}
		catch (Exception e) {
			throw new JobSchedulerException(e);
		}
		// return void;
	} // public void LoadProperties }

	/**
	 *
	 * \brief LoadSystemProperties
	 *
	 * \details
	 * This Method is to get all relevant Values for the Options in the Class from
	 * the System.properties.
	 *
	 * \return void
	 *
	 * @param pstrPropertiesFileName
	 * @throws Exception
	 */
	public void LoadSystemProperties() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::LoadSystemProperties ";
		Properties objProp = new Properties();
		objProp = System.getProperties();
		LoadProperties(objProp);
		// return void;
	} // public void LoadSystemProperties }

	/**
	 *
	 * \brief LoadProperties
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param pobjProp
	 * @throws Exception
	 */
	public void LoadProperties(final Properties pobjProp) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::LoadProperties";
		this.Settings();
		for (final Object element : pobjProp.entrySet()) {
			@SuppressWarnings("rawtypes")
			final Map.Entry mapItem = (Map.Entry) element;
			final String strMapKey = mapItem.getKey().toString();
			if (mapItem.getValue() != null) {
				final String strTemp = mapItem.getValue().toString();
				logger.debug("Property " + strMapKey + " = " + strTemp);
				if (strTemp != null && strTemp.length() > 0 && strTemp.equalsIgnoreCase(".") == false) {
					objSettings.put(strMapKey, strTemp);
				}
			}
		}
		// (hoffentlich) �berschrieben von der erbenden Klasse
		try {
			setAllOptions(objSettings);
		}
		catch (Exception e) {
			throw new JobSchedulerException("setAllOptions returns an error:", e);
		}
		setAllCommonOptions(objSettings);
	} // private void LoadProperties

	/**
	 *
	 * \brief setAllOptions
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @param hshMap
	 */
	public void setAllOptions(final HashMap<String, String> pobjJSSettings, final String pstrAlternativePrefix) throws Exception {
		if (strAlternativePrefix.length() <= 0) {
			strAlternativePrefix = pstrAlternativePrefix;
			if (objParentClass != null) {
				IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.setPrefix, strBuffer);
			}
		}
//		else {
//			logger.trace(String.format("SOSOPT-I-002: Alternate Prefix already set to %1$s, but %2$s as new given", strAlternativePrefix, pstrAlternativePrefix));
//		}
		this.setAllOptions(pobjJSSettings);
	}

	public void setAllOptions(final HashMap<String, String> hshMap) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		setAllCommonOptions(hshMap);
	} // public void setAllOptions}

	/*!
	 * \brief CurrentNodeName - Logischer Name f�r eine System-Instanz
	 *
	 * \details
	 * Getter: Logischer Name f�r eine System-Instanz
	 *
	 * Example:
	 *
	 * @return Returns the CurrentNodeName.
	 */
	public String CurrentNodeName() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentNodeName";
		return strCurrentNodeName;
	} // String CurrentNodeName()

	/*!
	 * CurrentNodeName - Logischer Name f�r eine System-Instanz
	 *
	 * Setter: Logischer Name f�r eine System-Instanz
	 *
	 * @param pstrCurrentNodeName: The String CurrentNodeName to set.
	 */
	public JSOptionsClass CurrentNodeName(final String pstrCurrentNodeName) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentNodeName";
		strCurrentNodeName = pstrCurrentNodeName;
		return this;
	} // public void CurrentNodeName(String pstrCurrentNodeName)

	/*!
	 * CurrentJobName
	 *
	 * Setter: Name eines Jobs
	 *
	 * @param pstrCurrentJobName: The String CurrentJobName to set.
	 */
	public JSOptionsClass CurrentJobName(final String pstrCurrentJobName) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentJobName";
		strCurrentJobName = pstrCurrentJobName;
		return this;
	} // public void CurrentJobName(String pstrCurrentJobName)

	/*!
	 * \brief CurrentJobName
	 *
	 * \details
	 * Getter: Name eines Jobs
	 *
	 * Example:
	 *
	 * @return Returns the CurrentJobName.
	 */
	public String CurrentJobName() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentJobName";
		return strCurrentJobName;
	} // String CurrentJobName()

	/*!
	 * CurrentJobId
	 *
	 * Setter: Id einer Job Task
	 *
	 * @param pintCurrentJobId: The Integer CurrentJobId to set.
	 */
	public JSOptionsClass CurrentJobId(final int pintCurrentJobId) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentJobId";
		intCurrentJobId = pintCurrentJobId;
		return this;
	} // public void CurrentJobId(String pstrCurrentJobId)

	/*!
	 * \brief CurrentJobId
	 *
	 * \details
	 * Getter: Id einer Job Task
	 *
	 * Example:
	 *
	 * @return Returns the CurrentJobId.
	 */
	public int CurrentJobId() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentJobId";
		return intCurrentJobId;
	} // String CurrentJobId()

	/*!
	 * CurrentJobFolder
	 *
	 * Setter: Folder eines Jobs
	 *
	 * @param pstrCurrentJobFolder: The String CurrentJobFolder to set.
	 */
	public JSOptionsClass CurrentJobFolder(final String pstrCurrentJobFolder) throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentJobFolder";
		strCurrentJobFolder = pstrCurrentJobFolder;
		return this;
	} // public void CurrentJobFolder(String pstrCurrentJobFolder)

	/*!
	 * \brief CurrentJobFolder
	 *
	 * \details
	 * Getter: Folder eines Jobs
	 *
	 * Example:
	 *
	 * @return Returns the CurrentJobFolder.
	 */
	public String CurrentJobFolder() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CurrentJobFolder";
		return strCurrentJobFolder;
	} // String CurrentJobFolder()

	/**
	 *
	 * \brief OptionByName - dynamischer Aufruf eines getters der Option-Klasse
	 *
	 * \details
	 * Liefert f�r eine als String �bergebenen Namen einer Option das Resultat, das
	 * geliefert w�rde, wenn der "getter" f�r diese Option gerufen worden w�re.
	 *
	 * Diese Technik wird verwendet, um zum Beispiel in Werten von Optionen auf den
	 * Wert einer anderen Option zu referenzieren (mit der Technik, den Namen des
	 * "getters" im Wert der Option zu verwenden)
	 *
	 * Achtung: funktioniert zur Zeit nur mit "gettern" und nur mit solchen, die
	 * einen Return-Wert vom Typ "String" liefern.
	 *
	 * \return String
	 *
	 * @param strOptionName
	 */
	@SuppressWarnings("unchecked")
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
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getOptionValue";
		String strValue = null;
		Field objField = null;
		try {
			objField = c.getField(pstrOptionName);
			Object objO = objField.get(this);
			if (objO instanceof String) {
				strValue = (String) objField.get(this);
			}
			else {
				if (objO instanceof SOSOptionElement) {
					SOSOptionElement objDE = (SOSOptionElement) objO;
					strValue = objDE.Value();
				}
			}
		} // try
		catch (final NoSuchFieldException objException) {
			Method objMethod;
			try {
				/**
				 * Die Methode, die gesucht wird, hat keine Parameter, weil
				 * diese ein "getter" ist.
				 * Deshalb in "getMethod" als zweites Argument "null", andernfalls
				 * erwischt die JVM eine andere Methode (zum Beispiel den "setter").
				 */
				objMethod = c.getMethod(pstrOptionName, null);
				/**
				 * ein "getter" hat keine Parameter, deshalb auch hier wieder
				 * als zweites Argument "null" angeben.
				 */
				strValue = (String) objMethod.invoke(this, null);
			}
			catch (final SecurityException exception) {
				exception.printStackTrace();
			}
			catch (final NoSuchMethodException exception) {
				// c = super.getClass();
				// try {
				// objMethod = c.getMethod(strOptionName, null);
				// strValue = (String) objMethod.invoke(this, null);
				// } // try
				// catch (Exception objExc) {
				// objExc.printStackTrace();
				// }
				// finally {
				// //
				// } // finally
			}
			catch (final IllegalArgumentException exception) {
				exception.printStackTrace();
			}
			catch (final InvocationTargetException exception) {
				exception.printStackTrace();
			}
			catch (final IllegalAccessException exception) {
				exception.printStackTrace();
			}
		}
		catch (final IllegalAccessException exception) {
			exception.printStackTrace();
		}
		return strValue;
	}
	private static Properties	objP	= new Properties();

	public Properties getTextProperties() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getTextProperties";
		if (objP == null) {
			objP = new Properties();
		}
		return objP;
	} // private Properties getTextProperties

	/**
	 *
	*
	* \brief replaceVars - replace all vars in a string and return the string
	*
	* \details
	*
	* \return String
	*
	 */
	public String replaceVars(final String pstrReplaceIn) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::replaceVars";
		getTextProperties();
		try {
			// zusaetzliche Parameter generieren
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
		// String strParamNameEnclosedInPercentSigns = ".*%\\{([^%]+)\\}.*";
		// %{variableName}
		String strParamNameEnclosedInPercentSigns = "^.*(\\$|%)\\{([^%\\}]+)\\}.*$";
		//		 String strParamNameEnclosedInPercentSigns = "^.*%\\{([^\\}]+).*$";
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
		//		pstrReplaceIn = pstrReplaceIn.replaceAll("\\$\\$N\\$\\$", "\n");
		return strNewString;
	}// private String replaceVars

	public void DumpSettings() {
		final String conMethodName = conClassName + "::DumpSettings";
		for (final Map.Entry<String,String> mapItem : objSettings.entrySet()) {
			final String strMapKey = mapItem.getKey().toString();
			if (mapItem.getValue() != null) {
				if(mapItem.getKey().contains("password")){
			       this.SignalDebug(conMethodName + ": Key = " + strMapKey + " --> " + "*****");
			    } else {
			       this.SignalDebug(conMethodName + ": Key = " + strMapKey + " --> " + mapItem.getValue().toString());
			    }
			}
		}
	}

	// - <remark who='EQALS' when='Freitag, 8. Mai 2009' id='MehrereExportSQL' >
	/**
	 * \change Freitag, 8. Mai 2009 EQALS MehrereExportSQL
	 * Mehr als ExportSQLs verarbeiten k�nnen
	 * @throws Exception
	 */
	// - <newcode>
	public String getIndexedItem(final String pstrIndexedKey, final String pstrDescription, final String pstrDelimiter) {
		String strT = "";
		final JSOptionValueList optionValueList = new JSOptionValueList(this, pstrIndexedKey, pstrDescription, "", true);
		strT = optionValueList.concatenatedValue(pstrDelimiter);
		return strT;
	}

	// - </newcode>
	// - </remark> <!-- id=<MehrereExportSQL> -->
	public Object deepCopy(final Object pobj2Copy) throws Exception {
		ByteArrayOutputStream bufOutStream = new ByteArrayOutputStream();
		ObjectOutputStream outStream = new ObjectOutputStream(bufOutStream);
		// Objekt im byte-Array speichern
		outStream.writeObject(pobj2Copy);
		outStream.close();
		// Pufferinhalt abrufen
		byte[] buffer = bufOutStream.toByteArray();
		// ObjectInputStream erzeugen
		ByteArrayInputStream bufInStream = new ByteArrayInputStream(buffer);
		ObjectInputStream inStream = new ObjectInputStream(bufInStream);
		// Objekt wieder auslesen
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

	   
	public StringBuffer IterateAllDataElementsByAnnotation(final Class<?> objC, final Object objP, final IterationTypes enuIterate4What, StringBuffer pstrBuffer) {
		final String conMethodName = conClassName + "::IterateAllDataElementsByAnnotation";
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
						if (objDE == null) {
							// final JSOptionsClass objO = (JSOptionsClass) objField.get(objField);
							// if (objO != null) {
							// System.out.println("oooooooField : " + objField.getName());
							//
							// this.IterateAllDataElementsByAnnotation(objO.getClass(), objO, enuIterate4What, pstrBuffer);
							// }
							// else {
							// System.out.println("*******Field : " + objField.getName());
							// final Object objO2 = objField.getDeclaringClass();
							// System.out.println(objField.getType().toString());
							// // objField.Iterate(enuIterate4What);
							// // this.IterateAllDataElementsByAnnotation(objO.getClass(), objO, enuIterate4What, pstrBuffer);
							// }
						}
						else {
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
								// continue;
							}
							if (enuIterate4What == IterationTypes.getRecord) {
								// objDE.BuildRecord(pstrBuffer);
								// continue;
							}
							if (enuIterate4What == IterationTypes.CheckMandatory) {
								objDE.CheckMandatory();
							}
							if (enuIterate4What == IterationTypes.toOut) {
								System.out.println(objDE.toString());
							}
							if (enuIterate4What == IterationTypes.toString) {
								pstrBuffer.append(addNewLine(objDE.toString()));
							}
							if (enuIterate4What == IterationTypes.DirtyToString) {
								if (objDE.isDirty() == true) {
									pstrBuffer.append(addNewLine(objDE.DirtyToString()));
								}
							}
							if (enuIterate4What == IterationTypes.createXML) {
								strXML.append(objDE.toXml());
							}
							if (enuIterate4What == IterationTypes.setDefaultValues) {
								final String strV = objDE.Value();
								if (strV.length() <= 0) {
									objDE.Value(objDE.DefaultValue());
								}
								// continue;
							}
							if (enuIterate4What == IterationTypes.clearAllValues) {
								objDE.Value("");
								// continue;
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
									pstrBuffer.append(addNewLine(strT));
								}
							}
							if (enuIterate4What == IterationTypes.countSegmentFields) {
								// System.out.println("+++++Field : " + objField.getName());
							}
							/**
							 * hier versuchen wir herauszubekommen, ob es
							 * in dieser Instanz (weitere) geschachtelte Optionen gibt.
							 * Falls ja, wird die geforderte Funktion auch f�r alle diese
							 * Instanzen ausgef�hrt.
							 */
							IterateAllDataElementsByAnnotation(objDE.getClass(), objDE, enuIterate4What, pstrBuffer);
						}
					} // if (objField.isAnnotationPresent(IDocSegmentField.class))
				} // try
				catch (final ClassCastException objException) {
				}
				catch (final Exception objE) {
					objE.printStackTrace();
					throw new RuntimeException(objE);
				}
				finally {
					//
				} // finally
			}
			if (enuIterate4What == IterationTypes.createXML) { // CreateXML
				strXML.append("</" + strT + ">");
				pstrBuffer = strXML;
			}
		}
		catch (final Exception objException) {
			objException.printStackTrace();
			throw new RuntimeException(objException);
		}
		finally {
			SOSOptionElement.gflgProcessHashMap = false;
			//
		} // finally
		return pstrBuffer;
	} // private void AllDataElements

	private String addNewLine(final String pstrV) {
		if (isNotEmpty(pstrV)) {
			return pstrV + "\n";
		}
		return pstrV;
	}

	public StringBuffer Iterate(final IterationTypes enuIterate4What) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Iterate";
		StringBuffer strB = new StringBuffer();
		if (objParentClass != null) {
			strB = IterateAllDataElementsByAnnotation(objParentClass, this, enuIterate4What, strB);
		}
		return strB;
	} // public StringBuffer Iterate

	/**
	 *
	 * \brief getItems
	 *
	 * \details
	 * Indizierte Option lesen
	 * Z. B. Key = SqlStatements: Dann werden alle Keys gelesen SqlStatements, SqlStatements1, ..
	 *
	 * \return Vector<String>
	 *
	 * @param pstrIndexedKey
	 * @return
	 * @throws Exception
	 */
	public Vector<String> getItems(final String pstrIndexedKey) {
		String strT;
		final Vector<String> objItems = new Vector<String>();
		final StringBuffer sb = new StringBuffer();
		strT = getItem(pstrIndexedKey);
		if (strT != null) {
			objItems.addElement(strT);
		}
		int i = 1;
		while ((strT = getItem(pstrIndexedKey + Integer.toString(i++))) != null) {
			objItems.addElement(strT);
			if (i > 2) {
				sb.append(";");
			}
			sb.append(strT);
		}
		return objItems;
	}

	/**
	 *
	 * \brief putObject
	 *
	 * \details
	 * This methods write the content of a JSOptionClass to a file.
	 * <code>
	 * 	public void TestSerialize() throws Exception {

		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::TestSerialize";

		String strParameterName = "user";
		String strCmdLineArgs[] = {"-" +  strParameterName, "JunitTestUser"};
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		objOptions.CommandLineArgs(strCmdLineArgs);
		assertEquals (strParameterName, "JunitTestUser", objOptions.user.Value());

		String strSerializedFileName = "c:/temp/test.object";
		objOptions.putObject(strSerializedFileName);

		System.setProperty(strParameterName, "sos-user2");
		objOptions.LoadSystemProperties();
		assertEquals (strParameterName, "sos-user2", objOptions.user.Value());

		SOSSSHJobOptions objO2 = new SOSSSHJobOptions();
		objO2 = (SOSSSHJobOptions) JSOptionsClass.getObject(strSerializedFileName);
		assertEquals (strParameterName, "JunitTestUser", objO2.user.Value());

	} // private void TestSerialize
	 </code>
	 *
	 *
	 * \return void
	 *
	 * @param pstrFileName
	 */
	public void putObject(final String pstrFileName) {
		try {
			JSFile objFile = new JSFile(pstrFileName);
			String encoded = getObjectAsString();
			objFile.Write(encoded);
			objFile.close();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured writing object to file: " + e);
		}
	}

	/**
	 *
	 * \brief getObjectAsString
	 *
	 * \details
	 *
	 * \return String
	 *
	 * @return
	 */
	public String getObjectAsString() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getObjectAsString";
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(this);
			oos.close();
			String encoded = "";
			if (gflgUseBase64ForObject == true) {
				encoded = new String(Base64.encodeBase64(bos.toByteArray()));
			}
			else {
				encoded = new String(bos.toByteArray());
			}
			return encoded;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured getting object as String: " + e);
		}
	} // private String getObjectAsString

	/**
	 *
	 * \brief getObject
	 *
	 * \details
	 * This methods read a JSOptionClass from a file and return the content of the
	 * file as an instance of JSOptionClass.
	 * <code>
	 * 	public void TestSerialize() throws Exception {

		@SuppressWarnings("unused")
		final String	conMethodName	= conClassName + "::TestSerialize";

		String strParameterName = "user";
		String strCmdLineArgs[] = {"-" +  strParameterName, "JunitTestUser"};
		SOSSSHJobOptions objOptions = new SOSSSHJobOptions();

		objOptions.CommandLineArgs(strCmdLineArgs);
		assertEquals (strParameterName, "JunitTestUser", objOptions.user.Value());

		String strSerializedFileName = "c:/temp/test.object";
		objOptions.putObject(strSerializedFileName);

		System.setProperty(strParameterName, "sos-user2");
		objOptions.LoadSystemProperties();
		assertEquals (strParameterName, "sos-user2", objOptions.user.Value());

		SOSSSHJobOptions objO2 = new SOSSSHJobOptions();
		objO2 = (SOSSSHJobOptions) JSOptionsClass.getObject(strSerializedFileName);
		assertEquals (strParameterName, "JunitTestUser", objO2.user.Value());

	} // private void TestSerialize
	 </code>
	 * \return JSOptionsClass
	 *
	 * @param pstrFileName
	 * @return
	 */
	public static JSOptionsClass getObject(final String pstrFileName) {
		try {
			JSOptionsClass schedulerObject;
			String encoded = new JSTextFile(pstrFileName).File2String();
			if (encoded == null || encoded.length() == 0) {
				return null;
			}
			byte[] serializedObject;
			if (gflgUseBase64ForObject == true) {
				serializedObject = Base64.decodeBase64(encoded.getBytes());
			}
			else {
				serializedObject = encoded.getBytes();
			}
			ByteArrayInputStream bis = new ByteArrayInputStream(serializedObject);
			ObjectInputStream ois = new ObjectInputStream(bis);
			schedulerObject = (JSOptionsClass) ois.readObject();
			ois.close();
			return schedulerObject;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured reading object from file: " + e);
		}
	}

	public void LoadXML(final JSXMLFile pobjXMLFile) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::LoadXML";
		this.LoadXML(pobjXMLFile, null);
	}

	/**
	 *
	 * \brief LoadXML
	 * Load Options from XML-String
	 * \details
	 *
	 * \return Properties = the xml as property-object
	 *
	 * @param pstrXMLAsString
	 * @param pstrXPathExpr = if null, the full XML-String is loaded
	 */
	public Properties LoadXML(final JSXMLFile pobjXMLFile, final String pstrXPathExpr) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::LoadXML";
		DOMParser parser = new DOMParser();
		Properties objProp = new Properties();
		try {
			parser.setFeature("http://xml.org/sax/features/validation", false);
			// The parser will validate the document only if a grammar is specified.
			// parser.setFeature("http://xml.org/sax/features/validation/dynamic", true);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new JobSchedulerException(String.format("SAXException %1$s", pobjXMLFile.getAbsolutePath()), e);
		}
		try {
			// parser.parse(new ByteArrayInputStream(pstrXMLAsString.getBytes()));
			parser.parse(pobjXMLFile.getAbsolutePath());
			Document document = parser.getDocument();
			if (isEmpty(pstrXPathExpr) == true) {
				traverse(document, objProp);
			}
			else {
				// SOSXMLXPath xPath = new SOSXMLXPath(pobjXMLFile.getAbsolutePath());
				// geht so nicht bei Saxon9HE
				// Node objTempNode = xPath.selectSingleNode(document, pstrXPathExpr);
				// if (objTempNode == null) {
				// throw new JobSchedulerException(String.format("No Node using xPath '%1$2' found", pstrXPathExpr));
				// }
				// traverse(objTempNode, objProp);
			}
			LoadProperties(objProp);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new JobSchedulerException(String.format("Exception %1$s", pobjXMLFile.getAbsolutePath()), e);
		}
		return objProp;
	} // private void LoadXML

	/**
	 *
	 * \brief traverse
	 * For each child with a value create a property-entry
	 * \details
	 *
	 * \return void
	 *
	 * @param node
	 * @param objProp
	 */
	private void traverse(final Node node, Properties objProp) {
		int type = node.getNodeType();
		logger.debug("NodeType = " + type);
		String strNodeName = node.getNodeName();
		String strNodeValue = node.getNodeValue();
		logger.debug("<" + strNodeName + ">" + strNodeValue);
		if (type == Node.ELEMENT_NODE) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node objN = children.item(i);
				if (objN.getNodeType() == Node.TEXT_NODE) {
					strNodeValue = objN.getNodeValue();
					if (isNotEmpty(strNodeValue)) {
						objProp.put(strNodeName, objN.getNodeValue());
					}
					break;
				}
			}
		}
		// Verarbeitet die Liste der Kindknoten durch rekursiven Abstieg
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			traverse(children.item(i), objProp);
		}
	}

	/**
	 *
	 * \brief toXMLFile
	 * serialize the options to XML-File
	 * \details
	 * Returns an instance of JSXMLFile with serialized options-class.
	 *
	 * \return JSXMLFile - the instance of the created xml-file
	 *
	 * @param pstrXMLFileName - The name (including path) of the XML-File
	 */
	public JSXMLFile toXMLFile(final String pstrXMLFileName) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::toXMLFile";
		JSXMLFile objXF;
		try {
			objXF = new JSXMLFile(pstrXMLFileName);
			objXF.writeXMLDeclaration();
			objXF.comment(String.format("Created by %1$s", conClassName));
			objXF.Write(this.toXML());
			objXF.close();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Exception:", e);
		}
		return objXF;
	} // private JSXMLFile toXMLFile

	/**
	 *
	 * \brief toXML
	 *
	 * \details
	 *
	 * \return StringBuffer
	 *
	 * @return
	 */
	public StringBuffer toXML() {
		return Iterate(IterationTypes.createXML);
	}

	@SuppressWarnings("unchecked")
	public HashMap<String, String> DeletePrefix(final HashMap<String, String> phsmParameters, final String pstrPrefix) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::DeletePrefix";
		String strTemp;
		HashMap<String, String> hsmNewMap = new HashMap<String, String>();
		if (phsmParameters != null) {
			// First put into the hashmap these items which do not starts with the prefix
			for (final Map.Entry<String, String> mapItem : phsmParameters.entrySet()) {
				String strMapKey = mapItem.getKey();
				if (mapItem.getValue() != null) {
					strTemp = mapItem.getValue();
				}
				else {
					strTemp = null;
				}
				if (strMapKey.startsWith(pstrPrefix) == false) {
					hsmNewMap.put(strMapKey, strTemp);
				}
			}
			// Then put into the hashmap these items which starts with the prefix. First, delete prefix
			for (final Map.Entry<String, String> mapItem : phsmParameters.entrySet()) {
				String strMapKey = mapItem.getKey();
				if (mapItem.getValue() != null) {
					strTemp = mapItem.getValue();
				}
				else {
					strTemp = null;
				}
				// hsmNewMap.put(strMapKey, strTemp);
				// logger.debug("strMapKey:" + strMapKey + "   strTemp:" +strTemp + "pstrPrefix:" + pstrPrefix );
				if (strMapKey.startsWith(pstrPrefix)) {
					// TODO avoid java.util.ConcurrentModificationException
					// (http://java.sun.com/javase/6/docs/api/java/util/Iterator.html#remove() )
					// phsmParameters.remove(strMapKey);
					strMapKey = strMapKey.replaceAll(pstrPrefix, "");
					// logger.debug("strMapKey after replace:" + strMapKey );
					hsmNewMap.put(strMapKey, strTemp);
					mapItem.setValue("\n");
				}
				else { // possible case: <nodeName>/<prefix><name> -> <nodeName>/<name>
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
	} // private HashMap <String, String> DeletePrefix

	public String getPid() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getPid";
		String pid = ManagementFactory.getRuntimeMXBean().getName();
		String strA[] = pid.split("@");
		pid = strA[0];
		return pid;
	} // public String getPid

	/**
	 * Test if an option is present in the option class. The test is not case sensitive.
	 * @param optionName
	 * @return
	 */
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
	public Preferences	objPreferenceStore	= null;

	public Preferences getPreferenceStore() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getPreferenceStore";
		if (objPreferenceStore == null) {
			objPreferenceStore = Preferences.userNodeForPackage(objParentClass);
		}
		return objPreferenceStore;
	} // private Preferences getPreferenceStore

	public void initializeOptionValues() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::InitializeOptionValues";
		if (objParentClass != null) {
			getPreferenceStore();
			objClassName4PreferenceStore = objParentClass;
			StringBuffer strB = new StringBuffer();
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.LoadValues, strB);
		}
	} // private void InitializeOptionValues

	public void storeOptionValues() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::storeOptionValues";
		if (objParentClass != null) {
			getPreferenceStore();
			objClassName4PreferenceStore = objParentClass;
			StringBuffer strB = new StringBuffer();
			IterateAllDataElementsByAnnotation(objParentClass, this, IterationTypes.StoreValues, strB);
		}
	} // private void storeOptionValues

	protected void setIfNotDirty(final SOSOptionElement objOption, final String pstrValue) {
		if (objOption.isNotDirty() && isNotEmpty(pstrValue)) {
			logger.trace("setValue = " + pstrValue);
			objOption.Value(pstrValue);
		}
	}

} // public class JSOptionsClass
