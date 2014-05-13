/*
 * CronConverter.java
 * Created on 20.08.2007
 *
 */
package sos.scheduler.cron;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sos.util.SOSDate;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Logging.Log4JHelper;
import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.Commands;
import com.sos.scheduler.model.objects.JSObjJob;
import com.sos.scheduler.model.objects.JSObjJobChain;
import com.sos.scheduler.model.objects.JSObjOrder;
import com.sos.scheduler.model.objects.Job;
import com.sos.scheduler.model.objects.JobChain;
import com.sos.scheduler.model.objects.JobChains;
import com.sos.scheduler.model.objects.Jobs;
import com.sos.scheduler.model.objects.Order;
import com.sos.scheduler.model.objects.Spooler;
import com.sos.scheduler.model.objects.Spooler.Config;

/**
 * This Class converts a crontab file to a JobScheduler XML Configuration
 *
 * @author Andreas Liebert
 */
@SuppressWarnings("deprecation")
public class CronConverter extends JSToolBox {

	private static final String				conTRUE						= "true";

	private static final String				conSystemCrontabName		= "/etc/crontab";

	protected static Logger					logger						= Logger.getLogger(CronConverter.class);

	private static final String				conStateERROR				= "!error";
	private static final String				conStateSUCCESS				= "success";
	private static final String				conAttributeERROR_STATE		= "error_state";
	private static final String				conAttributeNEXT_STATE		= "next_state";
	private static final String				conTagJOB_CHAIN_NODE		= "job_chain_node";
	private static final String				conAttributeJOB_CHAIN		= "job_chain";
	private static final String				conAttributeID				= "id";
	private static final String				conAttributeSTATE			= "state";
	private static final String				conTagADD_ORDER				= "add_order";
	private static final String				conAttributeNAME			= "name";
	private static final String				conTagJOB					= "job";
	private static final String				conAttributeORDER			= "order";
	private static final String				conAttributeTITLE			= "title";
	private final String					conClassName				= "CronConverter";
	private static final String				conTagRUN_TIME				= "run_time";
	private static final String				conOptionCREATE_MOCK		= "createMock";
	private static final String				conOptionCREATE_JobChains	= "createJobChains";
	private static final String				conNewline					= "\n";
	private static final String				conAttributeLANGUAGE		= "language";
	private static final String				conTagSCRIPT				= "script";
	private static final String				conTagSPOOLER				= "spooler";
	private static final String				conOptionTIMEOUT			= "timeout";
	private static final String				conOptionCHANGEUSER			= "changeuser";
	private static final String				conOptionVERBOSE			= "v";
	private static final String				conOptionTARGET				= "target";
	private static final String				conOptionsSYSTAB			= "systab";
	private static final String				conOptionCRONTAB			= "crontab";
	private final DocumentBuilderFactory	docFactory					= DocumentBuilderFactory.newInstance();
	private final DocumentBuilder			docBuilder;
	/**
	 * Regular Expression
	 * -?([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+([^\s]+)\s+(.+)$
	 * matches cron lines such as
	 * 59 23  *  *  *   /usr/bin/xmessage.sh
	 * with grouping
	 */
	private final static String				cronRegEx					= "-?([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.+)$";

	/**
	 * Regular Expression for system crontab. has one more column (user)
	 */
	private final static String				cronRegExSystem				= "-?([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+(.+)$";

	protected Pattern						cronRegExPattern;

	protected Pattern						cronRegExSystemPattern;
	//	private final SOSLogger					logger;

	private boolean							systemCronTab				= false;
	private final boolean					flgCreateExtensionTags		= false;
	private boolean							oldRunTime					= false;
	private boolean							usedNewRunTime				= false;
	private final boolean					flgCreateSubFolderStructure	= true;
	private String							changeUserCommand			= "";
	private String							strCronLine					= "";

	/**
	 * Regular Expression
	 * (@reboot|@yearly|@monthly|@weekly|@daily|@hourly)\s+(.+)$
	 * matches cron lines with aliases such as
	 * @monthly   test -x /usr/sbin/texpire && /usr/sbin/texpire
	 */
	private final static String				cronRegExAlias				= "(@reboot|@yearly|@annually|@monthly|@weekly|@daily|@midnight|@hourly)\\s+(.+)$";
	protected Pattern						cronRegExAliasPattern;

	/**
	 * Regular Expression
	 * ^\s*#\s*(.+)
	 * matches cron comment lines
	 */
	private final static String				cronRegExComment			= "^\\s*#\\s*(.+)";
	protected Pattern						cronRegExCommentPattern;

	/**
	 * Regular Expression
	 * \s*job_name\s*=\s*(.+)
	 * matches comments which set a job name
	 */
	private final static String				cronRegExJobName			= "\\s*job_name\\s*=\\s*(.+)";
	private final static String				cronRegExPuppetName			= "\\s*Puppet Name\\s*:\\s*(.+)";
	protected Pattern						cronRegExJobNamePattern;
	protected Pattern						cronRegExPuppetNamePattern;

	/**
	 * Regular Expression
	 * \s*job_title\s*=\s*(.+)
	 * matches comments which set a job title
	 */
	private final static String				cronRegExJobTitle			= "\\s*job_title\\s*=\\s*(.+)";
	protected Pattern						cronRegExJobTitlePattern;

	/**
	 * Regular Expression
	 * \s*job_timeout\s*=\s*(.+)
	 * matches comments which set a job timeout
	 */
	private final static String				cronRegExJobTimeout			= "\\s*job_timeout\\s*=\\s*(.+)";
	protected Pattern						cronRegExJobTimeoutPattern;

	/**
	 * Regular Expression
	 * ^\s*(\w+)\s*=\s*(.+)
	 * matches environment variable settings
	 */
	private final static String				cronRegExEnvironment		= "^\\s*(\\w+)\\s*=\\s*(.+)";
	protected Pattern						cronRegExEnvironmentPattern;

	/**
	 * Regular Expression
	 * [^\s]* /[^\s]*
	 * matches a path (at least one "/")
	 */
	private final static String				commandRegEx				= "[^\\s]*/[^\\s]*";
	private final Pattern					commandRegExPattern;

	/**
	 * Regular expression
	 * (.*)_(\d*)$
	 * Matches incremented job names
	 */
	private final static String				jobNameRegEx				= "(.*)_(\\d*)$";
	private final Pattern					jobNameRegExPattern;
	private HashSet<String>					skipLines					= new HashSet<String>();
	private HashSet<String>					reservedJobNames			= new HashSet<String>();
	private String							timeout						= "600";
	protected Pattern						currentCronPattern;
	protected String						strBaseDirectory			= "CronTabConverter";
	protected String						strMockCommand				= "ping -n 20 localhost";
	private String							lastComment					= "";

	public boolean isCreateAMock() {
		return flgCreateAMock;
	}

	public void setCreateAMock(final boolean createAMok) {
		flgCreateAMock = createAMok;
	}

	public boolean isCreateJobChainJobs() {
		return flgCreateJobChainJobs;
	}

	public void setCreateJobChainJobs(final boolean createJobChainJobs) {
		flgCreateJobChainJobs = createJobChainJobs;
	}

	private boolean	flgCreateAMock			= false;
	private boolean	flgCreateJobChainJobs	= false;

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		Logger logger = Logger.getLogger(CronConverter.class);
		@SuppressWarnings("unused")
		Log4JHelper objLogger = null;

		objLogger = new Log4JHelper(null);

		logger = Logger.getRootLogger();
		logger.info("SOS CronConverter - Main"); //$NON-NLS-1$

		/*try {
			  test();
			} catch(Exception ex){
				ex.printStackTrace();
			}
		*/
		try {
			//SOSArguments arguments = new SOSArguments(args);
			SOSLogger sosLogger;
			String sourceFile = "";
			String targetFile = "";
			String changeUser = "";
			File source = null;
			File target = null;
			int logLevel = 0;
			boolean sysTab = false;
			boolean useOldRunTime = false;
			String jobTimeout = "";
			/*
			try {
				sourceFile = arguments.as_string("-crontab=");
				if (sourceFile.equalsIgnoreCase("/etc/crontab")) sysTab = true;
				targetFile = arguments.as_string("-target=");
				logLevel = arguments.as_int("-v=",SOSStandardLogger.INFO);
				sysTab = arguments.as_bool("-system=",sysTab);
				useOldRunTime = arguments.as_bool("-oldRunTime=",false);
				changeUser = arguments.as_string("-change-user=", "su");
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
				showUsage();
				System.exit(0);
			}*/
			Options options = new Options();
			OptionBuilder.withArgName("0|1");
			OptionBuilder.hasArg();
			OptionBuilder.withDescription("set to 1 if source is the system crontab (with user field)");

			Option optSysTab = OptionBuilder.create(conOptionsSYSTAB);

			OptionBuilder.withArgName("file");
			OptionBuilder.hasArgs();
			OptionBuilder.isRequired();
			OptionBuilder.withDescription("crontab file");

			Option optSourceFile = OptionBuilder.create(conOptionCRONTAB);
			OptionBuilder.withArgName("file");
			OptionBuilder.hasArgs();
			OptionBuilder.isRequired();
			OptionBuilder.withDescription("xml configuration file");

			Option optTargetFile = OptionBuilder.create(conOptionTARGET);
			OptionBuilder.withArgName("level");
			OptionBuilder.hasArg();
			OptionBuilder.withType(new Integer(0));
			OptionBuilder.withDescription("loglevel [0=info] [1=debug1]...[9=debug]");

			Option optLogLevel = OptionBuilder.create(conOptionVERBOSE);

			OptionBuilder.withArgName("command");
			OptionBuilder.hasArgs();
			OptionBuilder.withDescription("change user command for -systab=1. 'su' or 'sudo' or define your own command using $SCHEDULER_CRONTAB_USER.");

			Option optChangeUser = OptionBuilder.create(conOptionCHANGEUSER);

			OptionBuilder.withArgName("seconds");
			OptionBuilder.hasArg();
			OptionBuilder.withDescription("job timeout (0 for unlimited");
			@SuppressWarnings("unused")
			Option optTimeOut = OptionBuilder.create(conOptionTIMEOUT);
			@SuppressWarnings("unused")
			Option optOldRunTime = new Option("oldRunTime", "");

			OptionBuilder.withArgName("true|false");
			OptionBuilder.hasArg();
			OptionBuilder.withDescription("set to true if script has to be mok'ed");
			Option optCreateMok = OptionBuilder.create(conOptionCREATE_MOCK);

			OptionBuilder.withArgName("true|false");
			OptionBuilder.hasArg();
			OptionBuilder.withDescription("set to true if you want to create jobChains and jobs");
			Option optCreateJobChains = OptionBuilder.create(conOptionCREATE_JobChains);

			options.addOption(optSysTab);
			options.addOption(optSourceFile);
			options.addOption(optTargetFile);
			options.addOption(optLogLevel);
			options.addOption(optChangeUser);
			options.addOption(optCreateMok);
			options.addOption(optCreateJobChains);

			CommandLineParser parser = new GnuParser();
			CommandLine line = null;
			try {
				line = parser.parse(options, args);
			}
			catch (Exception e) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("cronconverter", options, true);
				System.exit(0);
			}

			sourceFile = getWholeArgument(line.getOptionValues(conOptionCRONTAB));
			if (sourceFile.equalsIgnoreCase(conSystemCrontabName)) {
				sysTab = true;
			}
			targetFile = getWholeArgument(line.getOptionValues(conOptionTARGET));

			String ll = line.getOptionValue(conOptionVERBOSE, "" + SOSStandardLogger.INFO);
			logLevel = Integer.parseInt(ll);
			if (line.hasOption(optSysTab.getOpt())) {
				sysTab = line.getOptionValue(optSysTab.getOpt()).trim().equals("1");
			}
			useOldRunTime = line.hasOption("oldRunTime");
			changeUser = "";
			if (line.hasOption(conOptionCHANGEUSER)) {
				changeUser = getWholeArgument(line.getOptionValues(conOptionCHANGEUSER));
			}

			jobTimeout = line.getOptionValue(conOptionTIMEOUT);
			if (logLevel == 0)
				logLevel = SOSLogger.INFO;
			sosLogger = new SOSStandardLogger(logLevel);

			target = new File(targetFile);
			source = new File(sourceFile);

			CronConverter cc = new CronConverter(sosLogger);
			if (jobTimeout != null && jobTimeout.length() > 0) {
				cc.setTimeout(jobTimeout);
			}
			cc.setChangeUserCommand(changeUser);
			if (line.hasOption(conOptionCREATE_MOCK)) { // JITL-28
				cc.setCreateAMock(line.getOptionValue(conOptionCREATE_MOCK).equalsIgnoreCase(conTRUE));
			}
			if (line.hasOption(conOptionCREATE_JobChains)) { // JITL-28
				cc.setCreateJobChainJobs(line.getOptionValue(conOptionCREATE_JobChains).equalsIgnoreCase(conTRUE));
			}

			cc.setSystemCronTab(sysTab);
			cc.oldRunTime = useOldRunTime;
			cc.cronFile2SchedulerXMLFile(source, target);

			SchedulerObjectFactory objSchedulerObjectFactory = new SchedulerObjectFactory();
			objSchedulerObjectFactory.initMarshaller(Spooler.class);

			Spooler objSchedulerConfig = (Spooler) objSchedulerObjectFactory.unMarshall(target);

			Config objConfig = objSchedulerConfig.getConfig().get(0);
			logger.debug(objConfig.getPort());
			logger.debug(objConfig.getTcpPort());
			logger.debug(objConfig.getUdpPort());

			//			for (ProcessClass objProcessClass : objConfig.getProcessClasses().getProcessClass()) {
			//				logger.debug("ProcessClass = " + objProcessClass.getName());
			//			}
			//
			String strPathName = new File(sourceFile).getParent();
			strPathName += "/live/";
			new File(strPathName).mkdirs();
			Jobs objJobs = objConfig.getJobs();
			if (objJobs != null) {
				for (Job objJob : objConfig.getJobs().getJob()) {
					String strJobName = objJob.getName();
					objJob.setName("");
					objJob.setParent(objSchedulerObjectFactory);
					logger.debug("job name = " + strJobName);
					File fleTargetFile = new File(strPathName + strJobName + JSObjJob.fileNameExtension);
					objJob.marshal(fleTargetFile);
				}
			}
			else {
				logger.debug("no jobs found");
			}

			JobChains objJobchains = objConfig.getJobChains();
			if (objJobchains != null) {
				for (JobChain objJobchain : objConfig.getJobChains().getJobChain()) {
					String strJobChainName = objJobchain.getName();
					objJobchain.setName("");
					objJobchain.setParent(objSchedulerObjectFactory);
					logger.debug("chain name = " + strJobChainName);
					File fleTargetFile = new File(strPathName + strJobChainName + JSObjJobChain.fileNameExtension);
					objJobchain.marshal(fleTargetFile);
				}
			}
			else {
				logger.debug("no chains found");
			}

			Commands objCommands = objConfig.getCommands();
			if (objCommands != null) {
				for (Object objO : objCommands.getAddJobsOrAddOrderOrCheckFolders()) {
					System.out.println(objO.getClass().getName());
					JAXBElement objJ = (JAXBElement) objO;
					if (objO instanceof Order) {
						Order objOrder = (Order) objO;
						JSObjOrder objJSO = new JSObjOrder(objSchedulerObjectFactory);
						objJSO.getOrderFromXMLString(objOrder.toXMLString());
						String strOrderFileName = objJSO.createFileName(strPathName);
						logger.debug("order name = " + strOrderFileName);
						File fleTargetFile = new File(strOrderFileName);
						objOrder.marshal(fleTargetFile);
					}
				}
			}
			else {
				logger.debug("no orders found");
			}
			logger.debug("ready");
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String getWholeArgument(final String[] optionValues) {
		String value = "";
		for (int i = 0; i < optionValues.length; i++) {
			value += optionValues[i];
			if (i + 1 < optionValues.length)
				value += " ";
		}
		return value;
	}

	@SuppressWarnings("unused")
	private static void test() throws Exception {
		SOSLogger log = null; //new SOSStandardLogger(SOSLogger.debug);
		CronConverter cc = new CronConverter(log);
		//		File inputFile = new File("J:\\E\\java\\al\\sos.scheduler\\config\\crontab");
		//		File outputFile = new File("J:\\E\\java\\al\\sos.scheduler\\config\\scheduler_cron.xml");
		File inputFile = new File("c:/temp/cronfile.cron");
		File outputFile = new File("c:/temp/scheduler_cron.xml");

		cc.cronFile2SchedulerXMLFile(inputFile, outputFile);

		// von http://www.newbie-net.de/anleitung_cron.html
		//Document job = cc.createJobElement("5 22  *  *  *   test -x /usr/sbin/texpire && /usr/sbin/texpire");
		//Document job = cc.createJobElement("5 22  *  *  *   test -x bla");
		//Document job = cc.createJobElement("5 22  3  *  *   test -x bla");
		//Document job = cc.createJobElement("5 22  3,5,8-12  *  *   test -x bla");
		//Document job = cc.createJobElement("1  0  *  *  1   /usr/bin/xsoftwaresamba.sh");

		//Ein Bindestrich  -  gibt einen Zeitraum an. Jeden Tag von 12-24 Uhr (jede Stunde) ...:
		//Document job = cc.createJobElement("0  12-24  *  *  *   /usr/bin/xsoftwaresamba.sh");

		// Ein Schrägstrich  /  teilt einen Zeitraum ein. Zwischen 6 und 23 Uhr alle 15 Minuten ...:
		//Document job = cc.createJobElement("*/15  6-23  *  *  *   /usr/bin/xsoftwaresamba.sh");

		// Jeden Tag um 0:00 und um 12:00 Uhr wird das Script xmessage.sh aufgerufen:
		//Document job = cc.createJobElement("0  0,12  *  *  *  /usr/bin/xmesssage.sh");

		// von http://docs.phplist.com/CronJobExamples
		// ...I want the script to run every 15 minutes starting at 5 p.m. (17) and running through 11:00 p.m. (23). This should happen Tue. (2) through Sat. (6)
		//Document job = cc.createJobElement("*/15 17-23 * * 2,3,4,5,6 /sbin/phplist -pprocessqueue");
		//Document job = cc.createJobElement("*/15 17-23 * * Tue-sat /sbin/phplist -pprocessqueue");

		//Document job = cc.createJobElement("15,30,45,00 * * * * /var/www/www.domain.com/phplist phplist-2.10.2/bin/phplist -p processqueue > /dev/null");

		// von http://www.pantz.org/os/linux/programs/cron.shtml
		// This line executes the "ping" and the "ls" command every 12am and 12pm on the 1st day of every 2nd month
		// Document job = cc.createJobElement("0 0,12 1 */2 * /sbin/ping -c 192.168.0.1; ls -la >>/var/log/cronrun");

		// von http://www.monetizers.com/cronjob.php
		// every day 23 minutes after every even hour (0:23, 2:23, ...)
		//Document job = cc.createJobElement("23 0-23/2 * * * $HOME/report.sh");

		/*StringWriter out = new StringWriter();
		OutputFormat format = new OutputFormat(job);
		format.setIndenting(true);
		format.setIndent(2);
		XMLSerializer serializer = new XMLSerializer(out, format);
		serializer.serialize(job);
		log.info(out.toString());*/

	}

	public CronConverter(final SOSLogger log) throws Exception {
		docBuilder = docFactory.newDocumentBuilder();
		cronRegExAliasPattern = Pattern.compile(cronRegExAlias);
		cronRegExPattern = Pattern.compile(cronRegEx);
		cronRegExSystemPattern = Pattern.compile(cronRegExSystem);
		cronRegExCommentPattern = Pattern.compile(cronRegExComment);
		cronRegExEnvironmentPattern = Pattern.compile(cronRegExEnvironment);
		commandRegExPattern = Pattern.compile(commandRegEx);
		jobNameRegExPattern = Pattern.compile(jobNameRegEx);
		cronRegExJobNamePattern = Pattern.compile(cronRegExJobName);
		cronRegExPuppetNamePattern = Pattern.compile(cronRegExPuppetName);
		cronRegExJobTitlePattern = Pattern.compile(cronRegExJobTitle);
		cronRegExJobTimeoutPattern = Pattern.compile(cronRegExJobTimeout);

		currentCronPattern = cronRegExPattern;
		//		logger = log;
	}

	public void cronFile2SchedulerXMLFile(final File cronFile, final File schedulerXML) throws Exception {
		try {
			Document configurationDocument = cronFile2SchedulerXML(cronFile, new HashMap<String, Element>());
			logger.debug("writing " + schedulerXML.getAbsolutePath());
			OutputStream fout = new FileOutputStream(schedulerXML, false);
			OutputStreamWriter out = new OutputStreamWriter(fout, "UTF-8");
			OutputFormat format = new OutputFormat(configurationDocument);
			format.setEncoding("UTF-8");
			format.setIndenting(true);
			format.setIndent(2);
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.setNamespaces(true);
			serializer.serialize(configurationDocument);
			out.close();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error writing JobScheduler configuration file: " + e, e);
		}
	}

	/**
	 * Converts a crontab to a Scheduler XML as DOM document and
	 * provides easy access to the job elements by putting them to the vector
	 * jobs
	 * @param cronFile crontab file
	 * @param cron2jobMapping empty vector which will be filled with cron lines mapped to job DOM Elements
	 * @return DOM Document with scheduler configuration
	 * @throws Exception
	 */
	public Document cronFile2SchedulerXML(final File cronFile, final HashMap<String, Element> cron2jobMapping) throws Exception {
		try {
			HashSet<String> jobNames = new HashSet<String>();
			if (reservedJobNames != null) {
				jobNames.addAll(reservedJobNames);
			}
			HashMap<String, String> environmentVariables = new HashMap<String, String>();
			Document configurationDoc = docBuilder.newDocument();
			Element spoolerElement = configurationDoc.createElement(conTagSPOOLER);
			configurationDoc.appendChild(spoolerElement);
			Element configElement = configurationDoc.createElement("config");
			spoolerElement.appendChild(configElement);
			Element jobsElement = configurationDoc.createElement("jobs");
			configElement.appendChild(jobsElement);

			BufferedReader in = new BufferedReader(new FileReader(cronFile));
			Vector<Element> vecJobs = new Vector<Element>();
			Vector<String> vecCronRecords = new Vector<String>();
			lastComment = "";
			String lastCommentJobName = "";
			String lastCommentJobTitle = "";
			String lastCommentJobTimeout = "";

			while ((strCronLine = in.readLine()) != null) {
				if (strCronLine.trim().length() == 0) {
					lastComment = "";
					continue;
				}
				if (skipLines != null && skipLines.contains(strCronLine)) {
					logger.debug("Skipping line " + strCronLine);
					lastComment = "";
					lastCommentJobName = "";
					lastCommentJobTitle = "";
					lastCommentJobTimeout = "";
					continue;
				}

				Matcher commentMatcher = cronRegExCommentPattern.matcher(strCronLine);
				if (commentMatcher.matches()) {
					Matcher jobNameMatcher = cronRegExJobNamePattern.matcher(commentMatcher.group(1));
					Matcher puppetNameMatcher = cronRegExPuppetNamePattern.matcher(commentMatcher.group(1));
					Matcher jobTitleMatcher = cronRegExJobTitlePattern.matcher(commentMatcher.group(1));
					Matcher jobTimeoutMatcher = cronRegExJobTimeoutPattern.matcher(commentMatcher.group(1));
					if (jobNameMatcher.matches()) {
						lastCommentJobName = jobNameMatcher.group(1);
						lastCommentJobName = lastCommentJobName.replaceAll(" ", "_");
						logger.debug("Found job name in comment: " + lastCommentJobName);
						continue;
					}
					if (puppetNameMatcher.matches()) {
						lastCommentJobName = puppetNameMatcher.group(1).trim();
						if (isEmpty(lastCommentJobTitle)) {
							lastCommentJobTitle = lastCommentJobName;
						}
						lastCommentJobName = lastCommentJobName.trim().replaceAll(" ", "_");
						logger.debug("Found job name in comment: " + lastCommentJobName);
						continue;
					}
					if (jobTitleMatcher.matches()) {
						lastCommentJobTitle = jobTitleMatcher.group(1);
						logger.debug("Found job title in comment: " + lastCommentJobTitle);
						continue;
					}
					if (jobTimeoutMatcher.matches()) {
						lastCommentJobTimeout = jobTimeoutMatcher.group(1);
						logger.debug("Found job timeout in comment: " + lastCommentJobTimeout);
						continue;
					}
					if (isNotEmpty(lastComment)) {
						lastComment += conNewline;
					}
					lastComment += commentMatcher.group(1);
					continue;
				}

				Matcher environmentMatcher = cronRegExEnvironmentPattern.matcher(strCronLine);
				if (environmentMatcher.matches()) {
					String envName = environmentMatcher.group(1);
					String envValue = environmentMatcher.group(2);
					logger.debug("Found environment variable [" + envName + "]: " + envValue);
					if (envValue.startsWith("\"") && envValue.endsWith("\"")) {
						envValue = envValue.substring(1, envValue.length() - 1);
					}
					environmentVariables.put(envName, envValue);
					lastComment = "";
				}

				Matcher cronMatcher = currentCronPattern.matcher(strCronLine);
				Matcher cronAliasMatcher = cronRegExAliasPattern.matcher(strCronLine);
				if (cronMatcher.matches() || cronAliasMatcher.matches()) {
					vecCronRecords.add(strCronLine);
					Document jobDocument = createJobElement(strCronLine, environmentVariables);
					Element jobElement = jobDocument.getDocumentElement();
					//NodeList jobChildren = jobElement.getChildNodes();
					//Element paramsElement = null;
					/*for (int i=0; i<jobChildren.getLength() && paramsElement==null;i++){
						Node currentNode = jobChildren.item(i);
						if (currentNode.getNodeName().equals("params")) paramsElement = (Element) currentNode;
					}*/

					boolean jobNameChanged = false;
					if (isNotEmpty(lastCommentJobName)) {
						lastCommentJobName = lastCommentJobName.replaceAll(">", "_");
						jobElement.setAttribute(conAttributeNAME, lastCommentJobName.replaceAll("/", "_"));
						lastCommentJobName = "";
					}
					if (isNotEmpty(lastCommentJobTitle)) {
						jobElement.setAttribute(conAttributeTITLE, lastCommentJobTitle);
						lastCommentJobTitle = "";
					}
					if (isNotEmpty(lastCommentJobTimeout)) {
						jobElement.setAttribute(conOptionTIMEOUT, lastCommentJobTimeout);
						lastCommentJobTimeout = "";
					}
					String jobName = jobElement.getAttribute(conAttributeNAME);
					while (jobNames.contains(jobName)) {
						//						logger.debug("Configuration already contains a job named \"" + jobName + "\". Looking for new name.");
						jobName = incrementJobName(jobName);
						jobNameChanged = true;
					}
					if (jobNameChanged) {
						logger.debug("Setting new job name \"" + jobName + "\"");
						jobElement.setAttribute(conAttributeNAME, jobName);
					}
					jobNames.add(jobName);
					Node importedJob = configurationDoc.importNode(jobElement, true);
					cron2jobMapping.put(strCronLine, jobElement);

					if (isNotEmpty(lastComment)) {
						// change "--" to "__" due to problems with xml-comment parser
						lastComment = lastComment.replaceAll("--", "__");
						Comment jobComment = configurationDoc.createComment(lastComment);
						jobsElement.appendChild(jobComment);
					}
					jobsElement.appendChild(importedJob);
					lastComment = "";
					vecJobs.add(jobElement);
				}
			}
			if (isCreateJobChainJobs()) {
				Element jobChainsElement = configurationDoc.createElement("job_chains");
				configElement.appendChild(jobChainsElement);

				int i = 0;
				for (Element objJobElement : vecJobs) {
					strCronLine = vecCronRecords.get(i++);
					Document objJCD = createJobChainElement(objJobElement);
					Element objJC = objJCD.getDocumentElement();
					jobChainsElement.appendChild(configurationDoc.importNode(objJC, true));
				}

				Element commandsElement = configurationDoc.createElement("commands");
				configElement.appendChild(commandsElement);

				i = 0;
				for (Element objJobElement : vecJobs) {
					strCronLine = vecCronRecords.get(i++);
					Document objJCD = createOrderElement(objJobElement);
					Element objJC = objJCD.getDocumentElement();
					commandsElement.appendChild(configurationDoc.importNode(objJC, true));
				}

			}
			in.close();
			return configurationDoc;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error converting file " + cronFile.getAbsolutePath() + " to JobScheduler XML: " + e, e);
		}

	}

	private String incrementJobName(String jobName) {
		Matcher jobNameMatcher = jobNameRegExPattern.matcher(jobName);
		if (jobNameMatcher.matches()) {
			String baseName = jobNameMatcher.group(1);
			String counter = jobNameMatcher.group(2);
			int iCounter = Integer.parseInt(counter);
			iCounter++;
			jobName = baseName + "_" + iCounter;
		}
		else {
			jobName = jobName + "_1";
		}
		return jobName;
	}

	public Document createJobElement(final String cronline) throws Exception {
		return createJobElement(cronline, new HashMap<String, String>());
	}

	public Document createJobChainElement(final Element pobjJobElement) throws DOMException, Exception {
		Document jobchain = docBuilder.newDocument();
		Element jobChainElement = jobchain.createElement(conAttributeJOB_CHAIN);
		jobChainElement.setAttribute(conAttributeNAME, pobjJobElement.getAttribute(conAttributeNAME));

		if (flgCreateExtensionTags == true) {
			jobChainElement.appendChild(createExtension(jobchain));
		}
		Element jobChainNode = jobchain.createElement(conTagJOB_CHAIN_NODE);
		jobChainNode.setAttribute(conAttributeSTATE, "100");
		jobChainNode.setAttribute(conTagJOB, getNameWithoutPath(pobjJobElement.getAttribute(conAttributeNAME)));
		jobChainNode.setAttribute(conAttributeNEXT_STATE, conStateSUCCESS);
		jobChainNode.setAttribute(conAttributeERROR_STATE, conStateERROR);
		jobChainElement.appendChild(jobChainNode);

		jobChainNode = jobchain.createElement(conTagJOB_CHAIN_NODE);
		jobChainNode.setAttribute(conAttributeSTATE, conStateSUCCESS);
		jobChainElement.appendChild(jobChainNode);

		jobChainNode = jobchain.createElement(conTagJOB_CHAIN_NODE);
		jobChainNode.setAttribute(conAttributeSTATE, conStateERROR);
		jobChainElement.appendChild(jobChainNode);

		jobchain.appendChild(jobChainElement);
		return jobchain;

	}

	private Element createExtension(final Document pobjDoc) throws DOMException, Exception {
		Element objExtensions = pobjDoc.createElement("extensions");
		Element objExtension = pobjDoc.createElementNS("www.sos-berlin.com/schema/joe", "extension");
		Element objGenerator = pobjDoc.createElement("generator");
		objExtension.appendChild(objGenerator);
		objGenerator.setAttribute("name", "CronConverter");
		objGenerator.setAttribute("date", SOSDate.getCurrentTimeAsString());
		objGenerator.setAttribute("vendor", "www.sos-berlin.com");

		Element objComments = pobjDoc.createElement("comment");
		Node objCommentNode = pobjDoc.createCDATASection(lastComment);
		objComments.appendChild(objCommentNode);
		objExtension.appendChild(objComments);

		Element objDocu = pobjDoc.createElement("docu");
		Node objDocuNode = pobjDoc.createCDATASection(strCronLine);
		objDocu.appendChild(objDocuNode);
		objGenerator.appendChild(objDocu);
		objExtensions.appendChild(objExtension);

		return objExtensions;
	}

	//	private Node createExtension (Element pobjParentElement) {
	//		Document
	//		Element objElement = pobjParentElement.createEle
	//	}

	public Document createOrderElement(final Element pobjJobElement) throws Exception {
		Document objOrderDocument = docBuilder.newDocument();
		Element addOrderElement = objOrderDocument.createElement(conTagADD_ORDER);
		String strOrderID = pobjJobElement.getAttribute(conAttributeNAME);
		addOrderElement.setAttribute(conAttributeID, getNameWithoutPath(strOrderID));
		addOrderElement.setAttribute(conAttributeTITLE, pobjJobElement.getAttribute(conAttributeNAME));
		addOrderElement.setAttribute(conAttributeJOB_CHAIN, pobjJobElement.getAttribute(conAttributeNAME));

		if (flgCreateExtensionTags == true) {
			addOrderElement.appendChild(createExtension(objOrderDocument));
		}
		Element runTimeElement = objOrderDocument.createElement(conTagRUN_TIME);

		logger.debug(strCronLine);
		cronRegExPattern = Pattern.compile(cronRegEx);
		Matcher cronRegExMatcher = cronRegExPattern.matcher(strCronLine);
		createRunTime(cronRegExMatcher, runTimeElement);

		addOrderElement.appendChild(runTimeElement);

		objOrderDocument.appendChild(addOrderElement);
		return objOrderDocument;
	}

	private String getNameWithoutPath(final String pstrName) {
		String strRet = pstrName;
		int i = pstrName.lastIndexOf("/");

		// Order id must not contain the path-name
		if (i != -1) {
			strRet = pstrName.substring(i + 1);
		}

		return strRet;
	}

	public Document createJobElement(String cronline, final HashMap<String, String> environmentVariables) throws Exception {
		try {
			logger.info("processing line: " + cronline);
			Document eleJob = docBuilder.newDocument();
			Element jobElement = eleJob.createElement(conTagJOB);

			Matcher cronRegExAliasMatcher = cronRegExAliasPattern.matcher(cronline);
			if (cronRegExAliasMatcher.matches()) {
				logger.debug("Current line matches pattern " + cronRegExAlias);
				cronline = convertAlias(cronRegExAliasMatcher);
			}
			Matcher cronRegExMatcher = cronRegExPattern.matcher(cronline);
			int commandIndex = 6;

			if (isSystemCronTab()) {
				commandIndex = 7;
				cronRegExMatcher = cronRegExSystemPattern.matcher(cronline);
			}
			if (!cronRegExMatcher.matches()) {
				throw new JobSchedulerException("Fail to parse cron line \"" + cronline + "\"");
			}
			String jobname = getJobName(cronRegExMatcher.group(commandIndex));
			jobElement.setAttribute(conAttributeNAME, jobname);
			if (isCreateJobChainJobs()) {
				jobElement.setAttribute(conAttributeORDER, "yes");
			}
			else {
				jobElement.setAttribute(conAttributeORDER, "no");
			}
			jobElement.setAttribute(conAttributeTITLE, "Cron Job " + cronRegExMatcher.group(commandIndex).trim());
			//jobElement.setAttribute("replace", "yes");
			if (timeout != null && !timeout.equals("0")) {
				jobElement.setAttribute(conOptionTIMEOUT, timeout);
			}

			String schedulerUser = "";
			String command = cronRegExMatcher.group(commandIndex);
			if (isSystemCronTab()) {
				schedulerUser = cronRegExMatcher.group(6);
				command = (changeUserCommand + " " + command).trim();
			}

			if (flgCreateExtensionTags == true) {
				jobElement.appendChild(createExtension(eleJob));
			}

			if (isCreateJobChainJobs() == false) {
				logger.debug("Creating params element");
				Element paramsElement = eleJob.createElement("params");
				logger.debug("Creating param element (command)");
				Element paramCommandElement = eleJob.createElement("param");
				paramCommandElement.setAttribute("name", "command");
				paramCommandElement.setAttribute("value", command);
				paramsElement.appendChild(paramCommandElement);
				jobElement.appendChild(paramsElement);
			}

			logger.debug("Creating script element");
			Element scriptElement = eleJob.createElement(conTagSCRIPT);
			scriptElement.setAttribute(conAttributeLANGUAGE, "shell");
			String script = conNewline;
			if (isNotEmpty(schedulerUser)) {
				script += "export SCHEDULER_CRONTAB_USER=" + schedulerUser + conNewline;
			}
			Iterator<String> envIter = environmentVariables.keySet().iterator();
			// set environment variables on job
			while (envIter.hasNext()) {
				String envName = envIter.next().toString();
				String envValue = environmentVariables.get(envName).toString();
				script += envName + "=" + envValue + conNewline;
				script += "export " + envName;
			}

			script += "echo created by " + conClassName + ", at " + SOSDate.getCurrentTimeAsString() + conNewline;
			if (isCreateAMock() == true) {
				script += "echo mock-mode: " + command + conNewline;
				if (isNotEmpty(strMockCommand)) {
					script += strMockCommand + conNewline;
				}
				script += "exit 0" + conNewline;
			}
			else {
				script += command;
			}

			Node scriptData = eleJob.createCDATASection(script);
			scriptElement.appendChild(scriptData);
			jobElement.appendChild(scriptElement);

			if (isCreateJobChainJobs() == false) {
				Element runTimeElement = eleJob.createElement(conTagRUN_TIME);
				createRunTime(cronRegExMatcher, runTimeElement);
				if (usedNewRunTime && oldRunTime) {
					// workaround while <month> Element is not available
					// can later be deleted (keep only else branch)
					usedNewRunTime = false;
					Document runTimeDocument = docBuilder.newDocument();
					runTimeDocument.appendChild(runTimeDocument.importNode(runTimeElement, true));
					StringWriter out = new StringWriter();
					OutputFormat format = new OutputFormat(runTimeDocument);
					format.setIndenting(true);
					format.setIndent(2);
					format.setOmitXMLDeclaration(true);
					XMLSerializer serializer = new XMLSerializer(out, format);
					serializer.serialize(runTimeDocument);
					Comment runTimeComment = eleJob.createComment("This run_time is currently not supported:\n" + out.toString());
					jobElement.appendChild(runTimeComment);
				}
				else {
					jobElement.appendChild(runTimeElement);
				}
			}

			eleJob.appendChild(jobElement);
			return eleJob;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error occured creating job from cron line: " + cronline, e);
		}
	}

	private void createRunTime(final Matcher pcronRegExMatcher, final Element runTimeElement) throws Exception {
		try {
			if (!pcronRegExMatcher.matches()) {
				throw new JobSchedulerException("Fail to parse cron line \"" + strCronLine + "\", regexp is " + pcronRegExMatcher.toString());
			}

			String minutes = pcronRegExMatcher.group(1);
			String hours = pcronRegExMatcher.group(2);
			String days = pcronRegExMatcher.group(3);
			String months = pcronRegExMatcher.group(4);
			String weekdays = pcronRegExMatcher.group(5);

			if (minutes.equals("@reboot")) {
				runTimeElement.setAttribute("once", "yes");
				return;
			}
			Vector<Element> childElements = new Vector<Element>();
			Element periodElement = runTimeElement.getOwnerDocument().createElement("period");

			logger.debug("processing hours [" + hours + "] and minutes [" + minutes + "]");
			if (minutes.startsWith("*")) {
				if (minutes.equalsIgnoreCase("*")) {
					// every minute
					periodElement.setAttribute("repeat", "60");
				}
				else { // repeat interval is given
					String repeat = minutes.substring(2);
					repeat = formatTwoDigits(repeat);
					periodElement.setAttribute("repeat", "00:" + repeat);
				}
				if (hours.startsWith("*")) {
					if (!hours.equalsIgnoreCase("*")) {
						// repeat interval is given for hours and minutes. Doesn't make sense.
						// e.g. */2 */3 every 3 hours repeat every 2 minutes
						throw new JobSchedulerException("Combination of minutes and hours not supported: " + minutes + " " + hours);
					}
					// every hour: keep interval from minutes
					childElements.add(periodElement);
				}
				else {
					logger.debug("Found specific hours, creating periods with begin and end.");
					String[] hourArray = hours.split(",");
					for (int i = 0; i < hourArray.length; i++) {
						String currentHour = hourArray[i];
						if (currentHour.indexOf("/") != -1) {
							String[] additionalHours = getArrayFromColumn(currentHour);
							hourArray = combineArrays(hourArray, additionalHours);
							continue;
						}
						String[] currentHourArray = currentHour.split("-");
						Element currentPeriodElement = (Element) periodElement.cloneNode(true);
						String beginHour = currentHourArray[0];

						int iEndHour = (Integer.parseInt(beginHour) + 1) % 24;
						// workaround, bis endhour am nächsten Tag erlaubt
						if (iEndHour == 0)
							iEndHour = 24;
						String endHour = "" + iEndHour;
						if (currentHourArray.length > 1)
							endHour = currentHourArray[1];
						beginHour = formatTwoDigits(beginHour);
						endHour = formatTwoDigits(endHour);
						currentPeriodElement.setAttribute("begin", beginHour + ":00");
						currentPeriodElement.setAttribute("end", endHour + ":00");
						childElements.add(currentPeriodElement);
					}
				}
			} // end if  minutes.startsWith("*")
			else { // one or more minutes are fixed
				String[] minutesArray = getArrayFromColumn(minutes);
				for (String element : minutesArray) {
					Element currentPeriodElement = (Element) periodElement.cloneNode(true);
					String currentMinute = element;

					currentMinute = formatTwoDigits(currentMinute);
					if (hours.startsWith("*")) {
						currentPeriodElement.setAttribute("absolute_repeat", "01:00");
						usedNewRunTime = true;
						if (!hours.equalsIgnoreCase("*")) {// repeat interval is given for hours
							String repeat = hours.substring(2);
							repeat = formatTwoDigits(repeat);
							currentPeriodElement.setAttribute("absolute_repeat", repeat + ":00");
						}
						currentPeriodElement.setAttribute("begin", "00:" + currentMinute);
						childElements.add(currentPeriodElement);
					}
					else { //fixed hour(s) is set
						String[] hourArray = hours.split(",");
						for (String element2 : hourArray) {
							currentPeriodElement = (Element) periodElement.cloneNode(true);
							String currentHour = element2;
							if (currentHour.indexOf("-") == -1) {
								// fixed hour and fixed minute --> create single_start
								currentHour = formatTwoDigits(currentHour);
								currentPeriodElement.setAttribute("single_start", currentHour + ":" + currentMinute);
							}
							else {
								// range of hours is set, create begin and end attributes
								String[] currentHourArray = currentHour.split("[-/]");
								int beginHour = Integer.parseInt(currentHourArray[0]);
								int endHour = Integer.parseInt(currentHourArray[1]);
								int beginMinute = Integer.parseInt(currentMinute);
								int endMinute = beginMinute + 1;
								// workaround, bis endhour am nächsten Tag erlaubt
								endMinute = beginMinute;
								if (endMinute == 60) {
									endMinute = 0;
									endHour = endHour + 1;
								}
								endHour = endHour % 24;
								// workaround, bis endhour am nächsten Tag erlaubt
								if (endHour == 0)
									endHour = 24;
								String stepSize = "1";
								if (currentHourArray.length == 3) {
									stepSize = formatTwoDigits(currentHourArray[2]);
								}
								currentPeriodElement.setAttribute("absolute_repeat", stepSize + ":00");
								usedNewRunTime = true;
								currentPeriodElement.setAttribute("begin", formatTwoDigits(beginHour) + ":" + formatTwoDigits(beginMinute));
								currentPeriodElement.setAttribute("end", formatTwoDigits(endHour) + ":" + formatTwoDigits(endMinute));
							}
							childElements.add(currentPeriodElement);
						}
					}
				}

			}

			logger.debug("processing days [" + days + "]");
			boolean monthDaysSet = false;
			if (days.startsWith("*")) {
				if (days.equals("*")) {
					// every day - do nothing, just keep periods
				}
				else {
					// repeat interval is given for days
					// this is not possible in the JobScheduler but can be poorly emulated
					Element monthDaysElement = runTimeElement.getOwnerDocument().createElement("monthdays");
					String repeat = days.substring(2);
					int iRepeat = Integer.parseInt(repeat);
					// use only 30 days
					for (int i = 1; i <= 30; i = i + iRepeat) {
						String day = "" + i;
						addDay(day, monthDaysElement, childElements);
					}
					childElements.clear();
					childElements.add(monthDaysElement);
					monthDaysSet = true;
				}
			}
			else {
				Element monthDaysElement = runTimeElement.getOwnerDocument().createElement("monthdays");
				String[] daysArray = getArrayFromColumn(days);
				for (String day : daysArray) {
					addDay(day, monthDaysElement, childElements);
				}
				childElements.clear();
				childElements.add(monthDaysElement);
				monthDaysSet = true;
			}

			if (!weekdays.equals("*") && monthDaysSet) {
				logger.info("Weekdays will not be processed as days are already set in current line.");
			}
			else {
				logger.debug("processing weekdays [" + weekdays + "]");
				weekdays = replaceDayNames(weekdays);
				if (weekdays.startsWith("*/"))
					throw new JobSchedulerException("Repeat intervals for the weekdays column [" + weekdays
							+ "] are not supported. Please use the days column.");
				if (weekdays.equals("*")) {
					// all weekdays, do nothing
				}
				else {
					Element weekDaysElement = runTimeElement.getOwnerDocument().createElement("weekdays");
					String[] daysArray = getArrayFromColumn(weekdays);
					for (String day : daysArray) {
						addDay(day, weekDaysElement, childElements);
					}
					childElements.clear();
					childElements.add(weekDaysElement);
				}
			}

			logger.debug("processing months [" + months + "]");
			if (months.startsWith("*")) {
				if (months.equals("*")) {
					// every month - do nothing
				}
				else {
					months = replaceMonthNames(months);
					// repeat interval is given for months
					// this is not possible in the JobScheduler but can be poorly emulated
					Vector<Element> newChildElements = new Vector<Element>();
					String repeat = months.substring(2);
					int iRepeat = Integer.parseInt(repeat);

					for (int i = 1; i <= 12; i = i + iRepeat) {
						String month = "" + i;
						Element monthElement = runTimeElement.getOwnerDocument().createElement("month");
						usedNewRunTime = true;
						monthElement.setAttribute("month", month);
						Iterator<Element> iter = childElements.iterator();
						while (iter.hasNext()) {
							Element child = iter.next();
							monthElement.appendChild(child.cloneNode(true));
						}
						newChildElements.add(monthElement);
					}
					childElements = newChildElements;
				}
			}
			else {// list of months is given
				Vector<Element> newChildElements = new Vector<Element>();
				String[] monthArray = getArrayFromColumn(months);
				for (String month : monthArray) {
					Element monthElement = runTimeElement.getOwnerDocument().createElement("month");
					usedNewRunTime = true;
					monthElement.setAttribute("month", month);
					Iterator<Element> iter = childElements.iterator();
					while (iter.hasNext()) {
						Element child = iter.next();
						monthElement.appendChild(child.cloneNode(true));
					}
					newChildElements.add(monthElement);
				}
				childElements = newChildElements;
			}

			// add topmost child elements to run_time element
			Iterator<Element> iter = childElements.iterator();
			while (iter.hasNext()) {
				Element someElement = iter.next();
				runTimeElement.appendChild(someElement);
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error creating run time: " + e, e);
		}

	}

	private static String[] combineArrays(final String[] hourArray, final String[] additionalHours) {
		String[] newArray = new String[hourArray.length + additionalHours.length];
		for (int i = 0; i < hourArray.length; i++) {
			newArray[i] = hourArray[i];
		}
		for (int i = 0; i < additionalHours.length; i++) {
			newArray[i + hourArray.length] = additionalHours[i];
		}
		return newArray;
	}

	private void addDay(final String day, final Element parentDaysElement, final Vector<Element> childElements) throws Exception {
		logger.debug("adding day: " + day);
		Element dayElement = parentDaysElement.getOwnerDocument().createElement("day");
		dayElement.setAttribute("day", day);
		Iterator<Element> iter = childElements.iterator();
		while (iter.hasNext()) {
			Element child = iter.next();
			dayElement.appendChild(child.cloneNode(true));
		}
		parentDaysElement.appendChild(dayElement);
	}

	private String[] getArrayFromColumn(final String column) {
		String[] elements = column.split(",");
		Vector<String> result = new Vector<String>();
		for (String element : elements) {
			if (element.indexOf("-") == -1) {
				result.add(element);
			}
			else {
				String[] range = element.split("[-/]");
				if (range.length < 2 || range.length > 3) {
					try {
						logger.warn("unknown crontab synthax: " + element);
					}
					catch (Exception e) {
					}
				}
				else {
					int from = Integer.parseInt(range[0]);
					int to = Integer.parseInt(range[1]);
					int stepSize = 1;
					// if e.g. 8-20/2
					if (range.length == 3)
						stepSize = Integer.parseInt(range[2]);
					for (int j = from; j <= to; j = j + stepSize)
						result.add("" + j);
				}
			}
		}
		String[] dummy = new String[1];
		return result.toArray(dummy);
	}

	private static String replaceDayNames(String element) {
		element = element.replaceAll("(?i)mon", "1");
		element = element.replaceAll("(?i)tue", "2");
		element = element.replaceAll("(?i)wed", "3");
		element = element.replaceAll("(?i)thu", "4");
		element = element.replaceAll("(?i)fri", "5");
		element = element.replaceAll("(?i)sat", "6");
		element = element.replaceAll("(?i)sun", "7");
		return element;
	}

	private static String replaceMonthNames(String element) {
		element = element.replaceAll("(?i)jan", "1");
		element = element.replaceAll("(?i)feb", "2");
		element = element.replaceAll("(?i)mar", "3");
		element = element.replaceAll("(?i)apr", "4");
		element = element.replaceAll("(?i)may", "5");
		element = element.replaceAll("(?i)jun", "6");
		element = element.replaceAll("(?i)jul", "7");
		element = element.replaceAll("(?i)aug", "8");
		element = element.replaceAll("(?i)sep", "9");
		element = element.replaceAll("(?i)oct", "10");
		element = element.replaceAll("(?i)nov", "11");
		element = element.replaceAll("(?i)dec", "12");
		return element;
	}

	@SuppressWarnings("unused")
	private String getJobName(String pstrCommand) {
		Matcher commandMatcher = commandRegExPattern.matcher(pstrCommand);
		if (commandMatcher.find()) {
			pstrCommand = commandMatcher.group();
		}
		else {
			int space = pstrCommand.indexOf(" ");
			if (space != -1) {
				pstrCommand = pstrCommand.substring(0, space);
			}
		}

		if (pstrCommand.startsWith("\"")) {
			pstrCommand = pstrCommand.substring(1);
		}

		if (pstrCommand.endsWith("\"")) {
			pstrCommand = pstrCommand.substring(0, pstrCommand.length() - 1);
		}

		if (pstrCommand.startsWith("/") || pstrCommand.startsWith("\\")) {
			pstrCommand = pstrCommand.substring(1);
		}

		int i = pstrCommand.indexOf("}");
		if (i >= 0) {
			pstrCommand = pstrCommand.substring(i + 1);
		}

		if (flgCreateSubFolderStructure == false) {
			pstrCommand = pstrCommand.replaceAll("/", "_");
			if (pstrCommand.startsWith("_")) {
				pstrCommand = pstrCommand.substring(1);
			}
		}
		else {
			if (pstrCommand.startsWith("/")) {
				pstrCommand = pstrCommand.substring(1);
			}
		}

		if (isNotEmpty(strBaseDirectory)) {
			pstrCommand = strBaseDirectory + "/" + pstrCommand;
		}
		return pstrCommand;
	}

	private String convertAlias(final Matcher matcher) throws Exception {
		logger.debug("Converting alias...");
		try {
			String alias = matcher.group(1);
			String rest = matcher.group(2);
			String result = "";
			if (alias.equals("@yearly") || alias.equals("@annually"))
				result = "0 0 1 1 * ";
			if (alias.equals("@monthly"))
				result = "0 0 1 * * ";
			if (alias.equals("@weekly"))
				result = "0 0 * * 0 ";
			if (alias.equals("@daily") || alias.equals("@midnight"))
				result = "0 0 * * * ";
			if (alias.equals("@hourly"))
				result = "0 * * * * ";
			if (alias.equals("@reboot"))
				result = "@reboot @reboot @reboot @reboot @reboot";
			result += rest;
			logger.debug("Alias converted to " + result);
			return result;
		}
		catch (Exception e) {
			throw new JobSchedulerException("Error converting alias: " + e, e);
		}
	}

	private static String formatTwoDigits(final String number) {
		if (number.length() == 1)
			return "0" + number;
		return number;
	}

	private static String formatTwoDigits(final int number) {
		return formatTwoDigits("" + number);
	}

	/**
	 * This function does not anylyse the crontab, it only
	 * returns what type of crontab the converter is configured
	 * to parse
	 * @return the  value for systemCronTab
	 */
	public boolean isSystemCronTab() {
		return systemCronTab;
	}

	/**
	 * Sets if the current crontab is a system crontab
	 * which has one more column (user)
	 * @param systemCronTab true=current crontab is a system crontab
	 */
	public void setSystemCronTab(final boolean systemCronTab) {
		if (systemCronTab) {
			currentCronPattern = cronRegExSystemPattern;
		}
		else
			currentCronPattern = cronRegExPattern;
		this.systemCronTab = systemCronTab;
	}

	/**
	 * @return the oldRunTime
	 */
	protected boolean isOldRunTime() {
		return oldRunTime;
	}

	/**
	 * @param oldRunTime the oldRunTime to set
	 */
	protected void setOldRunTime(final boolean oldRunTime) {
		this.oldRunTime = oldRunTime;
	}

	/**
	 * @return the skipLines
	 */
	public HashSet<String> getSkipLines() {
		return skipLines;
	}

	/**
	 * The cron lines contained in this set will be skipped when
	 * converting a cron file
	 * @param skipLines
	 */
	public void setSkipLines(final HashSet<String> skipLines) {
		this.skipLines = skipLines;
	}

	/**
	 * @return the reservedJobNames
	 */
	public HashSet<String> getReservedJobNames() {
		return reservedJobNames;
	}

	/**
	 * Sets reserved job names. Reserved job names will not be used as job names.
	 * @param reservedJobNames HashSet of reserved job names
	 */
	public void setReservedJobNames(final HashSet<String> reservedJobNames) {
		this.reservedJobNames = reservedJobNames;
	}

	/**
	 * @return the docBuilder
	 */
	protected DocumentBuilder getDocBuilder() {
		return docBuilder;
	}

	/**
	 * @return the changeUserCommand
	 */
	public String getChangeUserCommand() {
		return changeUserCommand;
	}

	/**
	 * @param changeUserCommand the changeUserCommand to set
	 */
	public void setChangeUserCommand(String changeUserCommand) {
		if (changeUserCommand.equalsIgnoreCase("su"))
			changeUserCommand = "su $SCHEDULER_CRONTAB_USER -c";
		if (changeUserCommand.equalsIgnoreCase("sudo"))
			changeUserCommand = "sudo -u $SCHEDULER_CRONTAB_USER";
		this.changeUserCommand = changeUserCommand;
	}

	/**
	 * @return the timeout
	 */
	public String getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(final String timeout) {
		this.timeout = timeout;
	}
}
