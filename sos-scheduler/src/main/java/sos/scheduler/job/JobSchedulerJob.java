package sos.scheduler.job;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import sos.connection.SOSConnection;
import sos.settings.SOSConnectionSettings;
import sos.settings.SOSProfileSettings;
import sos.settings.SOSSettings;
import sos.spooler.Job_impl;
import sos.util.SOSArguments;
import sos.util.SOSLogger;
import sos.util.SOSSchedulerLogger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * Base class for Scheduler Jobs
 *
 * @author Andreas Liebert
 */
public class JobSchedulerJob extends Job_impl {

	private static String			conClassName		= "JobSchedulerJob";
	/** logging */
	protected SOSSchedulerLogger	sosLogger			= null;

	/** Database connection */
	private SOSConnection			sosConnection		= null;

	/** Settings from the database */
	private SOSConnectionSettings	connectionSettings	= null;

	/** job Settings */
	private SOSSettings				jobSettings			= null;

	/** Job Properties */
	private Properties				jobProperties		= null;

	/** task id assigned by the Job Scheduler */
	private int						jobId				= 0;

	/** configured job name Scheduler */
	private String					jobName				= null;

	/** configured job folder */
	private String					jobFolder			= null;

	/** configured job title */
	private String					jobTitle			= null;

	/** name of the application (for settings) */
	protected String				application			= new String("");

	public SOSConnection ConnectToJSDataBase() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::ConnectToJSDataBase";

		try {
			this.setJobSettings(new SOSProfileSettings(spooler.ini_path()));
			this.setJobProperties(jobSettings.getSection("spooler"));
			if (this.getJobProperties().isEmpty())
				throw new JobSchedulerException("no settings found in section [spooler] of configuration file: " + spooler.ini_path());
			if (this.getJobProperties().getProperty("db") == null || this.getJobProperties().getProperty("db").length() == 0)
				throw new JobSchedulerException("no settings found for entry [db] in section [spooler] of configuration file: " + spooler.ini_path());
			if (this.getJobProperties().getProperty("db_class") == null || this.getJobProperties().getProperty("db_class").length() == 0)
				throw new JobSchedulerException("no settings found for entry [db_class] in section [spooler] of configuration file: " + spooler.ini_path());

			if (this.getLogger() != null)
				sosLogger.debug6("connecting to database...");

			this.setConnection(getSchedulerConnection(this.getJobSettings(), this.getLogger()));
			this.getConnection().connect();

			this.setConnectionSettings(new SOSConnectionSettings(this.getConnection(), "SETTINGS", this.getLogger()));

			if (this.getLogger() != null)
				this.getLogger().debug6("..successfully connected to JobScheduler database.");
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			spooler_log.info("connect to database failed: " + e);
			spooler_log.info("running without database...");
		}

		return sosConnection;
	} // private SOSConnection ConnectToJSDataBase

	/*
	 * initializes the database connection to the database configured
	 * in config/factory.ini <br/>
	 * @see sos.spooler.Job_impl#spooler_init()
	 */
	@Override
	public boolean spooler_init() {

		try {
			boolean rc = super.spooler_init();
			if (!rc) {
				return false;
			}

			this.setLogger(new SOSSchedulerLogger(spooler_log));

			if (spooler_job != null && getJobSettings() != null)
				setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));
			if (spooler_task != null)
				this.setJobId(spooler_task.id());
			if (spooler_job != null)
				this.setJobName(spooler_job.name());
			if (spooler_job != null)
				this.setJobFolder(spooler_job.folder_path());
			if (spooler_job != null)
				this.setJobTitle(spooler_job.title());
			this.getSettings();
			return true;
		}
		catch (Exception e) {
			spooler_log.error(e.getMessage());
			return false;
		}
	}

	/**
	 * Closes the database connection
	 */
	@Override
	public void spooler_exit() {

		try {
			try { // to close the database connection
				if (sosConnection != null) {
					spooler_log.debug6("spooler_exit(): disconnecting.. ..");
					sosConnection.disconnect();
					sosConnection = null;
				}
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				sosConnection = null;
				spooler_log.warn("spooler_exit(): disconnect failed: " + e.toString());
			}

			spooler_log.info("Job " + this.getJobName() + " terminated.");
		}
		catch (Exception e) {
		} // no errror processing at job level
	}

	/**
	 * @return Returns the jobSettings.
	 */
	public SOSSettings getJobSettings() {
		return jobSettings;
	}

	/**
	 * @param jobSettings
	 *            The jobSettings to set.
	 */
	public void setJobSettings(final SOSSettings jobSettings) {
		this.jobSettings = jobSettings;
	}

	/**
	 * @return Returns the sosConnection.
	 */
	public SOSConnection getConnection() {
		if (sosConnection == null) {
			ConnectToJSDataBase();
		}
		return sosConnection;
	}

	/**
	 * @param psosConnection
	 *            The sosConnection to set.
	 */
	public void setConnection(final SOSConnection psosConnection) {
		if (sosConnection != null && sosConnection.equals(psosConnection) == false) {
			try {
				sosConnection.disconnect();
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace(System.err);
			}
			sosConnection = null;

		}
		sosConnection = psosConnection;
	}

	/**
	 * @return Returns the sosLogger.
	 */
	public SOSSchedulerLogger getLogger() {
		return sosLogger;
	}

	/**
	 * @param sosLogger1
	 *            The sosLogger to set.
	 */
	public void setLogger(final SOSSchedulerLogger sosLogger1) {
		sosLogger = sosLogger1;
	}

	/**
	 * @return Returns the jobProperties.
	 */
	public Properties getJobProperties() {
		return jobProperties;
	}

	/**
	 * @param jobProperties
	 *            The jobProperties to set.
	 */
	public void setJobProperties(final Properties jobProperties) {
		this.jobProperties = jobProperties;
	}

	/**
	 * @return Returns the connectionSettings.
	 */
	public SOSConnectionSettings getConnectionSettings() {
		return connectionSettings;
	}

	/**
	 * @param connectionSettings The connectionSettings to set.
	 */
	public void setConnectionSettings(final SOSConnectionSettings connectionSettings) {
		this.connectionSettings = connectionSettings;
	}

	/**
	 * @return Returns the jobId.
	 */
	protected int getJobId() {
		return jobId;
	}

	/**
	 * @param jobId The jobId to set.
	 */
	protected void setJobId(final int jobId) {
		this.jobId = jobId;
	}

	/**
	 * @return Returns the jobName.
	 */
	protected String getJobName() {
		return jobName;
	}

	/**
	 * @param jobName The jobName to set.
	 */
	protected void setJobName(final String jobName) {
		this.jobName = jobName;
	}
	
	/**
	 * @return Returns the jobFolder.
	 */
	protected String getJobFolder() {
		return jobFolder;
	}

	/**
	 * @param jobFolder The jobFolder to set.
	 */
	protected void setJobFolder(final String jobFolder) {
		this.jobFolder = jobFolder;
	}

	/**
	 * @return Returns the jobTitle.
	 */
	protected String getJobTitle() {
		if (jobTitle == null) {
			jobTitle = spooler_task.job().title();
		}
		return jobTitle;
	}

	/**
	 * @param jobTitle The jobTitle to set.
	 */
	protected void setJobTitle(final String jobTitle) {
		this.jobTitle = jobTitle;
	}

	/**
	 * @param application The application to set.
	 */
	protected void setApplication(final String application) {
		this.application = application;
	}

	/**
	 * @return Returns the application.
	 */
	protected String getApplication() {
		return application;
	}

	/**
	 * returns the Settings for the Job Scheduler as SOSSettings object
	 * @param factoryIni Path to factory.ini (delivered by spooler.ini_path() )
	 * @throws Exception
	 */
	public static SOSSettings getSchedulerSettings(final String factoryIni) throws Exception {
		SOSSettings schedulerSettings = new SOSProfileSettings(factoryIni);
		return schedulerSettings;
	}

	/**
	 * Returns a SOSConnection object for the database connection of the Job Scheduler
	 * The database connection must be opened with connect() and closed with disconnect()
	 *
	 * @param schedulerSettings Job Scheduler Settings
	 * @throws Exception
	 */
	public static SOSConnection getSchedulerConnection(final SOSSettings schedulerSettings) throws Exception {
		return getSchedulerConnection(schedulerSettings, null);
	}

	/**
	 * Returns a SOSConnection object for the database connection of the Job Scheduler
	 * The database connection must be opened with connect() and closed with disconnect()
	 *
	 * @param schedulerSettings Job Scheduler Settings
	 * @param log SOSLogger, which will be used by the database connection
	 * @throws Exception
	 */
	public static SOSConnection getSchedulerConnection(final SOSSettings schedulerSettings, final SOSLogger log) throws Exception {
		String dbProperty = schedulerSettings.getSection("spooler").getProperty("db").replaceAll("jdbc:", "-url=jdbc:");
		dbProperty = dbProperty.substring(dbProperty.indexOf('-'));
		if (dbProperty.endsWith("-password="))
			dbProperty = dbProperty.substring(0, dbProperty.length() - 10);
		SOSArguments arguments = new SOSArguments(dbProperty);

		SOSConnection conn;
		if (log != null) {
			conn = SOSConnection.createInstance(schedulerSettings.getSection("spooler").getProperty("db_class"), arguments.as_string("-class=", ""),
					arguments.as_string("-url=", ""), arguments.as_string("-user=", ""), arguments.as_string("-password=", ""), log);
		}
		else {
			conn = SOSConnection.createInstance(schedulerSettings.getSection("spooler").getProperty("db_class"), arguments.as_string("-class=", ""),
					arguments.as_string("-url=", ""), arguments.as_string("-user=", ""), arguments.as_string("-password=", ""));
		}

		return conn;
	}

	/**
	 * read initial job settings
	 */
	private boolean getSettings() {

		try {
			
			if (spooler_job == null) {
				return false;
			}

			File schedulerIniFile = new File(spooler.ini_path());
			if (!schedulerIniFile.canRead()){
				spooler_log.debug("No ini file found. Continuing without settings.");
				return false;
			}
			setJobSettings(new SOSProfileSettings(spooler.ini_path()));
			setJobProperties(getJobSettings().getSection("job " + spooler_job.name()));

			if (getJobProperties().isEmpty())
				return false;

			if (getJobProperties().getProperty("delay_after_error") != null) {
				String[] delays = getJobProperties().getProperty("delay_after_error").toString().split(";");
				if (delays.length > 0)
					spooler_job.clear_delay_after_error();
				for (String delay2 : delays) {
					String[] delay = delay2.split(":");
					spooler_job.set_delay_after_error(Integer.parseInt(delay[0]), delay[1]);
				}
			}

			return true;

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			spooler_log.error(e.getMessage());
			return false;
		}
	}

	protected URL createURL(final String fileName) throws Exception {
		URL url = null;
		try {
			url = new URL(fileName);
		}
		catch (MalformedURLException ex) {
			try {
				File f = new File(fileName);
				String path = f.getCanonicalPath();
				if (fileName.startsWith("/")) {
					path = fileName;
				}

				String fs = System.getProperty("file.separator");
				if (fs.length() == 1) {
					char sep = fs.charAt(0);
					if (sep != '/')
						path = path.replace(sep, '/');
					if (path.charAt(0) != '/')
						path = '/' + path;
				}
				if (!path.startsWith("file://")) {
					path = "file://" + path;
				}
				url = new URL(path);
			}
			catch (MalformedURLException e) {
				throw new Exception("error in createURL(): " + e.getMessage());
			}
		}
		return url;
	}

	protected URI createURI(final String fileName) throws Exception {
		URI uri = null;
		try {
			uri = new URI(fileName);
		}
		// catch (URISyntaxException ex) {
		catch (Exception e) {
			try {
				File f = new File(fileName);
				String path = f.getCanonicalPath();
				if (fileName.startsWith("/")) {
					path = fileName;
				}

				String fs = System.getProperty("file.separator");
				if (fs.length() == 1) {
					char sep = fs.charAt(0);
					if (sep != '/')
						path = path.replace(sep, '/');
					if (path.charAt(0) != '/')
						path = '/' + path;
				}
				if (!path.startsWith("file://")) {
					path = "file://" + path;
				}
				uri = new URI(path);
			}
			// catch (URISyntaxException ex) {
			catch (Exception ex) {
				throw new Exception("error in createURI(): " + e.getMessage());
			}
		}
		return uri;
	}

	protected File createFile(final String fileName) throws Exception {
		try {
			if (fileName == null || fileName.length() == 0) {
				throw new Exception("empty file name provided");
			}

			if (fileName.startsWith("file://")) {
				return new File(createURI(fileName));
			}
			else {
				return new File(fileName);
			}
		}
		catch (Exception e) {
			throw new Exception("error in createFile() [" + fileName + "]: " + e.getMessage());
		}
	}

}
