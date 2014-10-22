/**
 * 
 */
package com.sos.JSHelper.Options;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;

import com.sos.JSHelper.interfaces.IJobSchedulerLoggingAppender;
import com.sos.JSHelper.io.Files.JSTextFile;

/**
 * @author KB
 *
 */
public class SOSOptionLog4JPropertyFile extends SOSOptionInFileName {
	private static final long				serialVersionUID					= -5291704259398563937L;
	public static final String				conLOG4J_PROPERTIESDefaultFileName	= "log4j.properties";
	@SuppressWarnings("unused")
	private final String					conClassName						= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String				conSVNVersion						= "$Id$";
	@SuppressWarnings("unused")
	private final Logger					logger								= Logger.getLogger(this.getClass());
	private static Logger					objCurrentLog						= null;																	/*!<  Ausgabe via individuellem Logger (log4j.category.xxx) */
	private static Logger					objRootLog							= null;																	/*!<  Ausgabe via RootLogger (log4j.rootCategory) */
	private static String					strPropfileName						= null;
	private String							strParentClassName					= conClassName;
	private Level							objLevel							= null;
	private final boolean					flgPrintComputerName				= false;
	public static boolean					flgUseJobSchedulerLog4JAppender		= false;
	private IJobSchedulerLoggingAppender	objLoggingAppender					= null;

	public void setLoggingAppender(IJobSchedulerLoggingAppender pobjLoggingAppender) {
		objLoggingAppender = pobjLoggingAppender;
	}

	/**
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 */
	public SOSOptionLog4JPropertyFile(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
			boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
		// TODO Auto-generated constructor stub
	}

	public Logger getLoggerInstance(final String pstrParentClassName) {
		if (this.Value() == null || this.Value().equalsIgnoreCase("./" + conLOG4J_PROPERTIESDefaultFileName)) {
			JSOptionsClass objO = new JSOptionsClass();
			String strF = objO.log4jPropertyFileName.Value();
			if (strF != null) {
				strPropfileName = strF;
			}
		}
		else {
			strPropfileName = this.Value();
			if (strPropfileName.equalsIgnoreCase("null") == true) {
				BasicConfigurator.configure();
				return Logger.getRootLogger();
			}
		}
		strParentClassName = pstrParentClassName;
		/**
		 * Anlegen der Properties-Datei, falls diese fehlt
		 */
		JSTextFile objLog4JPropertyFile = new JSTextFile(strPropfileName);
		boolean flgNew = false;
		boolean flgPropFileIsOk = false;
		/**
		 * canWrite is not working on a non-existing file.
		 * that's why we check the parent of the file
		 */
		if (objLog4JPropertyFile.exists() == false && objLog4JPropertyFile.getParentFile().canWrite() == true) { // if we can't write we should avoid an exception
			try {
				objLog4JPropertyFile.WriteLine("log4j.rootCategory=info, stdout");
				if (flgUseJobSchedulerLog4JAppender == false) {
					objLog4JPropertyFile.WriteLine("log4j.appender.stdout=org.apache.log4j.ConsoleAppender");
				}
				else {
					/**	
					 * Spezialität für den JobScheduler Logger:
					 * von aussen steuern. bei junit-tests ist der consoleappender richtig, sonst nicht.
					 */
					objLog4JPropertyFile.WriteLine("log4j.appender.stdout=com.sos.scheduler.JobSchedulerLog4JAppender");
				}
				objLog4JPropertyFile.WriteLine("log4j.appender.stdout.layout=org.apache.log4j.PatternLayout");
				objLog4JPropertyFile.WriteLine("log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n");
				objLog4JPropertyFile.close();
				flgNew = true;
				flgPropFileIsOk = true;
			}
			catch (Exception e) {
				System.err.println(conClassName + ": unable to create the log4j-property-file " + objLog4JPropertyFile.getAbsolutePath());
				e.printStackTrace();
				flgPropFileIsOk = false;
			}
		}
		else {
			flgPropFileIsOk = true;
		}
		/**
		 * Vorbereitung des Logging-Environments
		 */
		objRootLog = Logger.getRootLogger();
		if (flgPropFileIsOk == true) {
			PropertyConfigurator.configure(objLog4JPropertyFile.getAbsolutePath());
			// TODO exception abfangen und direkt konfigurieren
		}
		else {
			/**
			 * hier jetzt direkt konfigurieren
			 */
			try {
				PatternLayout layout = new PatternLayout();
				layout.setConversionPattern("%5p [%t] (%p-%F::%M:%L) - %m%n");
				Appender consoleAppender = null;
				if (flgUseJobSchedulerLog4JAppender == true && objLoggingAppender != null) {
					if (objLoggingAppender instanceof Appender) {
						consoleAppender = (Appender) objLoggingAppender;
						consoleAppender.setLayout(layout);
					}
				}
				else {
					consoleAppender = new ConsoleAppender(layout);
				}
				objRootLog.addAppender(consoleAppender);
				// ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF:
				objRootLog.setLevel(Level.INFO);
				objRootLog.debug("Log4J configured programmatically");
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (pstrParentClassName.equals(conClassName)) {
			objCurrentLog = objRootLog;
		}
		else {
			objCurrentLog = Logger.getLogger(strParentClassName);
		}
		if (flgNew) {
			objRootLog.warn("log4j-property-file '" + objLog4JPropertyFile.getAbsolutePath() + "' does not exist - a default-file was created");
			objRootLog.debug("using log4j-property-file " + objLog4JPropertyFile.getAbsolutePath());
			objRootLog.warn("all log-entries will be written to the console");
		}
		objLevel = objCurrentLog.getLevel();
		return Logger.getRootLogger();
	}
}
