package sos.scheduler.ftp;
import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.configuration.SOSConfiguration;
import sos.net.SOSFileTransfer;
import sos.net.sosftp.SOSFTPCommandReceive;
import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.util.SOSSchedulerLogger;
import sos.util.SOSString;

import com.sos.JSHelper.Basics.JSVersionInfo;

/**
 * FTP File Transfer
 *
 * @author Andreas Püschel 
 * @author Mürüvet Öksüz
 * 
 * 2009-02-22: added SOSCommand.getExternalPassword
 * 2009-02-00: Redesign from Configuration and call SOSFTP 
 */
public class JobSchedulerFTPReceive extends JobSchedulerJob {

	private static final String	conPrefixFTP	= "ftp_";

	private static final String	conSettingFILE_SPEC	= "file_spec";

	@SuppressWarnings("unused")
	private final String conSVNVersion = "$Id: JobSchedulerFTPReceive.java 15345 2011-10-19 13:26:57Z kb $";

	/** The FTP server will always reply the ftp error codes, 
	 * see http://www.the-eggman.com/seminars/ftp_error_codes.html */
	public static final int	ERROR_CODE								= 300;
	private SOSString		sosString								= new SOSString();
	int						pollTimeout								= 0;
	int						pollIntervall							= 60;
	int						pollMinFiles							= 1;
	String					pollFilesErrorState						= "";
	int						iSetbackCount							= 1;
	private boolean			flgUseOrderSetBack						= true;
	private final String	conVarname_setback						= "setback";
	private final String	conVarname_setback_count				= "setback_count";
	private final String	conVarname_ftp_file_path				= "ftp_file_path";
	private final String	conVarname_ftp_result_error_message		= "ftp_result_error_message";
	private final String	conVarname_ftp_result_files				= "ftp_result_files";
	private final String	conVarname_ftp_result_zero_byte_files	= "ftp_result_zero_byte_files";
	private final String	conVarname_ftp_result_filenames			= "ftp_result_filenames";
	private final String	conVarname_ftp_result_filepaths			= "ftp_result_filepaths";
	private final String	conVarname_ftp_check_parallel			= "ftp_check_parallel";
	private final String	conVarname_file_spec					= conSettingFILE_SPEC;

	public boolean spooler_process() {
		boolean checkParallel = false;
		boolean parallelTransfer = false;
		String parallelTransferCheckSetback = "00:00:60";
		int parallelTransferCheckRetry = 60;
		Variable_set params = null;
		boolean rc = false;
		boolean isFilePath = false;
		boolean orderSelfDestruct = false;
		Properties schedulerParams = null;
		try {
			try {
				this.setLogger(new SOSSchedulerLogger(spooler_log));
				getLogger().debug(JSVersionInfo.getVersionString());
				getLogger().debug(conSVNVersion);
				params = getParameters();
				schedulerParams = getSchedulerParameterAsProperties(params);
				checkParallel = sosString.parseToBoolean(sosString.parseToString(schedulerParams.get("check_parallel")));
				parallelTransfer = sosString.parseToBoolean(sosString.parseToString(schedulerParams.get("parallel")));
			}
			catch (Exception e) {
				rc = false;
				e.printStackTrace(System.err);
				throw (new Exception("could not process job parameters: " + e));
			}
			try { // to check parameters
				if (checkParallel) {
					boolean bSuccess = true;
					String[] paramNames = sosString.parseToString(spooler.variables().names()).split(";");
					for (int i = 0; i < paramNames.length; i++) {
						if (paramNames[i].startsWith("ftp_check_receive_" + normalize(spooler_task.order().id()) + ".")) {
							if (sosString.parseToString(spooler.var(paramNames[i])).equals("0")) {
								// Anzahl der Wiederholung merken
								String sRetry = sosString.parseToString(spooler.variables().var("cur_transfer_retry" + normalize(spooler_task.order().id())));
								int retry = sRetry.length() == 0 ? 0 : Integer.parseInt(sRetry);
								--retry;
								spooler.variables().set_var("cur_transfer_retry" + normalize(spooler_task.order().id()), String.valueOf(retry));
								if (retry == 0) {
									getLogger().debug("terminated cause max order setback reached: " + paramNames[i]);
									spooler.variables().set_var("terminated_cause_max_order_setback_" + normalize(spooler_task.order().id()), "1");
									return false;
								}
								getLogger().debug("launch setback: " + parallelTransferCheckRetry + " * " + parallelTransferCheckSetback);
								spooler_task.order().setback();
								return false;
							}
							else
								if (sosString.parseToString(spooler.var(paramNames[i])).equals("1")) {
									getLogger().debug("successfully terminated: " + paramNames[i]);
								}
								else
									if (sosString.parseToString(spooler.var(paramNames[i])).equals("2")) {
										bSuccess = false;
										getLogger().debug("terminated with error : " + paramNames[i]);
									}
						}
					}
					return bSuccess;
				}
				else
					if (schedulerParams.get("parent_order_id") != null) {
						// Hauptauftrag wurde wegen Erreichens von ftp_parallel_check_retry beendet -> die restlichen Unterauftrüge sollen
						// nicht durchlaufen
						String state = spooler.variables().var(
								"terminated_cause_max_order_setback_" + normalize(sosString.parseToString(schedulerParams.get("ftp_parent_order_id"))));
						if (state.equals("1"))
							return false;
					}
				if (sosString.parseToString(schedulerParams.get("file_path")).length() > 0) {
					isFilePath = true;
				}
				else {
					isFilePath = false;
				}
			}
			catch (Exception e) {
				rc = false;
				e.printStackTrace(System.err);
				throw (new Exception("invalid or insufficient parameters: " + e));
			}
			try { // to process ftp
				Vector<String> filelist = null;
				String remoteDir = sosString.parseToString(schedulerParams.get("remoteDir"));
				// parallel Transfer
				if (parallelTransfer && !isFilePath) {
					// nur die filelist holen um Parallelen transfer zu ermöglichen
					schedulerParams.put("skip_transfer", "yes");
//					createIncludeConfigurationFile("sos/net/sosftp/Configuration.xml", "sos.net.sosftp.Configuration.xml");// Alle
					// Parametern
					// sind hier
					// auch gültig
					SOSConfiguration con = new SOSConfiguration(null, schedulerParams, sosString.parseToString(schedulerParams.get("settings")),
							sosString.parseToString(schedulerParams.get("profile")),
							// "sos/net/sosftp/Configuration.xml",
//							"sos/scheduler/ftp/SOSFTPConfiguration.xml", new SOSSchedulerLogger(spooler_log));
					null, new SOSSchedulerLogger(spooler_log));
					con.checkConfigurationItems();
					SOSFTPCommandReceive ftpCommand = new SOSFTPCommandReceive(con, new SOSSchedulerLogger(spooler_log));
					ftpCommand.setSchedulerJob(this);
					rc = ftpCommand.transfer();
					filelist = ftpCommand.getFilelist();
					Iterator<String> iterator = filelist.iterator();
					if (spooler_job.order_queue() == null) {
						// parallel Transfer for standalone Job
						while (iterator.hasNext()) {
							String fileName = sosString.parseToString(iterator.next());
							String fileSpec = schedulerParams.containsKey(conVarname_file_spec) ? sosString.parseToString(schedulerParams.get(conVarname_file_spec))
									: ".*";
							Pattern pattern = Pattern.compile(fileSpec, 0);
							Matcher matcher = pattern.matcher(fileName);
							if (matcher.find()) {
								Variable_set newParams = params;
								newParams.set_var(conVarname_ftp_file_path, (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir + "/")
										+ fileName);
								spooler_log.info("launching job for parallel transfer with parameter: ftp_file_path "
										+ (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir + "/") + fileName);
								spooler.job(spooler_task.job().name()).start(params);
							}
						}
						return false;
					}
					else {
						// parallel Transfer for order job
						while (iterator.hasNext()) {
							String fileName = (String) iterator.next();
							String fileSpec = schedulerParams.containsKey(conSettingFILE_SPEC) ? sosString.parseToString(schedulerParams.get(conSettingFILE_SPEC)) : ".*";
							Pattern pattern = Pattern.compile(fileSpec, 0);
							Matcher matcher = pattern.matcher(fileName);
							if (matcher.find()) {
								Variable_set newParams = spooler.create_variable_set();
								if (spooler_task.params() != null)
									newParams.merge(params);
								newParams.set_var(conVarname_ftp_file_path, (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir + "/")
										+ fileName);
								newParams.set_var("ftp_parent_order_id", spooler_task.order().id());
								newParams.set_var("ftp_order_self_destruct", "1");
								Order newOrder = spooler.create_order();
								newOrder.set_state(spooler_task.order().state());
								newOrder.set_params(newParams);
								spooler_task.order().job_chain().add_order(newOrder);
								getLogger().info(
										"launching order for parallel transfer with parameter: ftp_file_path "
												+ (remoteDir.endsWith("/") || remoteDir.endsWith("\\") ? remoteDir : remoteDir + "/") + fileName);
								spooler.variables().set_var("ftp_order", normalize(spooler_task.order().id()) + "." + normalize(newOrder.id()) + "." + "0");
								spooler.variables().set_var("ftp_check_receive_" + normalize(spooler_task.order().id()) + "." + normalize(newOrder.id()), "0");
							}
						}
						// am aktuellen Auftrag speichern, dass im Wiederholungsfall per setback() nicht erneut Auftrüge erzeugt werden
						// sollen, sondern dass deren Erledigungszustand geprüft wird:
						spooler_task.order().params().set_var(conVarname_ftp_check_parallel, "yes");
						spooler_job.set_delay_order_after_setback(1, parallelTransferCheckSetback);
						spooler_job.set_max_order_setbacks(parallelTransferCheckRetry);
						spooler_task.order().setback();
						spooler.variables().set_var("cur_transfer_retry" + normalize(spooler_task.order().id()), String.valueOf(parallelTransferCheckRetry));
						return false;
					}
				}
				// kb 2011-04-27 no more longer needed due to too much trouble with this file / concept
//				 createIncludeConfigurationFile("sos/net/sosftp/Configuration.xml", "sos.net.sosftp.Configuration.xml");// Alle Parametern
				// sind hier auch
				// gültig
				SOSConfiguration con = new SOSConfiguration(null, schedulerParams, sosString.parseToString(schedulerParams.get("settings")),
						sosString.parseToString(schedulerParams.get("profile")),
						// "sos/net/sosftp/Configuration.xml",
//						"sos/scheduler/ftp/SOSFTPConfiguration.xml", new SOSSchedulerLogger(spooler_log));
				null, new SOSSchedulerLogger(spooler_log));
				con.checkConfigurationItems();
				sos.net.sosftp.SOSFTPCommandReceive ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(con, new SOSSchedulerLogger(spooler_log));
				ftpCommand.setSchedulerJob(this);
				rc = ftpCommand.transfer();
				createOrderParameter(ftpCommand);
				if (parallelTransfer && isFilePath && spooler_job.order_queue() != null) {
					spooler.variables().set_var(
							"ftp_check_receive_" + normalize(params.var("ftp_parent_order_id")) + "." + normalize(spooler_task.order().id()), "1");
				}
				processResult(rc, "");
				spooler_job.set_state_text(ftpCommand.getState() != null ? ftpCommand.getState() : "");
				return (spooler_task.job().order_queue() == null) ? false : rc;
			}
			catch (Exception e) {
				rc = false;
				if (parallelTransfer && isFilePath && spooler_job.order_queue() != null) {
					spooler.variables().set_var(
							"ftp_check_receive_" + normalize(normalize(params.var("ftp_parent_order_id"))) + "." + normalize(spooler_task.order().id()), "2");
				}
				spooler_job.set_state_text("could not process file transfer: " + e);
				throw (new Exception("could not process file transfer: " + e, e));
			}
			finally {
				if (parallelTransfer) {
					if (orderSelfDestruct) {
						// positiven Endzustand für den parallel gestarteten Auftrag finden
						String state = "";
						sos.spooler.Job_chain_node node = spooler_task.order().job_chain_node();
						while (node != null) {
							node = node.next_node();
							if (node != null)
								state = node.state();
						}
						// Endzustand
						spooler_task.order().set_state(state);
					}
				}
			}
		}
		catch (Exception e) {
			processResult(false, e.toString());
			spooler_log.warn("ftp processing failed: " + e.toString());
			if (spooler_job.order_queue() != null) {
				if (spooler_task.order() != null && spooler_task.order().params() != null) {
					spooler_task.order().params().set_var(conVarname_setback_count, "");
				}
			}
			return false;
		}
	}

	private String normalize(String str) {
		return str.replaceAll(",", "_");
	}

	/**
	 * do nothing, entry point for subclasses
	 * @param flgOperationWasSuccessful
	 * @param message
	 */
	protected void processResult(boolean rc, String message) {
		// do nothing, entry point for subclasses
	}

	/**
	 * Auftragsgesteuerte und 
	 * @return
	 * @throws Exception
	 */
	private Variable_set getParameters() throws Exception {
		try {
			Variable_set params = spooler.create_variable_set();
			// Parameter auslesen
			if (spooler_task.params() != null)
				params.merge(spooler_task.params());
			if (spooler_job.order_queue() != null && spooler_task.order().params() != null) {
				params.merge(spooler_task.order().params());
				Variable_set orderParams = spooler_task.order().params();
				String setbackCount = orderParams.value(conVarname_setback_count);
				getLogger().debug9("setback_count read: " + setbackCount);
				if (setbackCount != null && setbackCount.length() > 0) {
					iSetbackCount = Integer.parseInt(setbackCount);
					iSetbackCount++;
				}
				orderParams.set_var(conVarname_setback_count, "" + iSetbackCount);
			}
			return params;
		}
		catch (Exception e) {
			throw new Exception("error occurred reading Parameter: " + e.getMessage());
		}
	}

	/**
	 * Convert parameters from sos.spooler.Variable_set in java.utile.Properties.
	 * 
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private Properties getSchedulerParameterAsProperties(Variable_set params) throws Exception {
		Properties schedulerParams = new Properties();// hier variable_set in Properties casten
		try {
			if (params == null)
				return new Properties();
			String[] names = params.names().split(";");
			getLogger().debug9("names " + params.names());
			for (int i = 0; i < names.length; i++) {
				String key = names[i];
				String val = params.var(names[i]);
				// Alle Parametern hatten bis jetzt den Präfix ftp_ gehabt. SOSFTP kennt die ftp_ Präfixe nicht.
				// Deshalb werden die Präfixen gekürzt.
				if (key.startsWith(conPrefixFTP) && key.length() > conPrefixFTP.length()) {
					key = key.substring(conPrefixFTP.length());
				}
				getLogger().debug("param [" + key + "=" + val + "]");
				schedulerParams.put(key, val);
			}
			if (sosString.parseToString(schedulerParams.get("use_order_set_back")).length() > 0) {
				flgUseOrderSetBack = sosString.parseToBoolean(schedulerParams.get("use_order_set_back"));
			}
			// Einige Defaults hinzufügen
			schedulerParams.put("operation", "receive");
			try {
				schedulerParams.put("mail_smtp", spooler_log.mail().smtp());
				schedulerParams.put("mail_queue_dir", spooler_log.mail().queue_dir());
				schedulerParams.put("mail_from", spooler_log.mail().from());
			}
			catch (Exception e) {
				schedulerParams.put("mail_smtp", "localhost");
				schedulerParams.put("mail_queue_dir", "");
				schedulerParams.put("mail_from", "SOSFTP");
			}
			String fileNotificationTo = sosString.parseToString(schedulerParams.get("file_notification_to"));
			String fileNotificationSubject = sosString.parseToString(schedulerParams.get("file_notification_subject"));
			String fileNotificationBody = sosString.parseToString(schedulerParams.get("file_notification_body"));
			if (fileNotificationTo != null && fileNotificationTo.length() > 0) {
				if (fileNotificationSubject == null || fileNotificationSubject.length() == 0) {
					if (spooler_job.order_queue() != null) {
						fileNotificationSubject = "[info] Job Chain: " + spooler_task.order().job_chain().name() + ", Order: " + spooler_task.order().id()
								+ ", Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
					}
					else {
						fileNotificationSubject = "[info] Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
					}
					schedulerParams.put("file_notification_subject", fileNotificationSubject);
				}
				if (fileNotificationBody == null || fileNotificationBody.length() == 0) {
					fileNotificationBody = "The following files have been received:\n\n";
					schedulerParams.put("file_notification_body", fileNotificationBody);
				}
			}
			String fileZeroByteNotificationTo = sosString.parseToString(schedulerParams.get("file_zero_byte_notification_to"));
			String fileZeroByteNotificationSubject = sosString.parseToString(schedulerParams.get("file_zero_byte_notification_subject"));
			String fileZeroByteNotificationBody = sosString.parseToString(schedulerParams.get("file_zero_byte_notification_body"));
			if (fileZeroByteNotificationTo != null && fileZeroByteNotificationTo.length() > 0) {
				if (fileZeroByteNotificationSubject == null || fileZeroByteNotificationSubject.length() == 0) {
					if (spooler_job.order_queue() != null) {
						fileZeroByteNotificationSubject = "[warning] Job Chain: " + spooler_task.order().job_chain().name() + ", Order: "
								+ spooler_task.order().id() + ", Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
					}
					else {
						fileZeroByteNotificationSubject = "[warning] Job: " + spooler_job.name() + " (" + spooler_job.title() + "), Task: " + spooler_task.id();
					}
					schedulerParams.put("file_zero_byte_notification_subject", fileZeroByteNotificationSubject);
				}
				if (fileZeroByteNotificationBody == null || fileZeroByteNotificationBody.length() == 0) {
					fileZeroByteNotificationBody = "The following files have been received and were removed due to zero byte constraints:\n\n";
					schedulerParams.put("file_zero_byte_notification_body", fileZeroByteNotificationBody);
				}
			}
			return schedulerParams;
		}
		catch (Exception e) {
			throw new Exception("error occurred reading Parameter: " + e.getMessage());
		}
	}

	/**
	 * 
	 * \brief createOrderParameter
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param ftpCommand
	 * @throws Exception
	 */
	private void createOrderParameter(SOSFTPCommandReceive ftpCommand) throws Exception {
		try {
			String fileNames = "";
			String filePaths = "";
			/**
			 * Dieses codeteil liefert *alle* Dateinamen, die in dem Verzeichnis stehen
			 * gefordert ist aber die Namen der Dateien zu liefern, die auch tatsächlich übertragen wurden.
			 * Und die stehen nicht im Vector, der mit getfileList geliefert wird.
			 * 
			 */
			Variable_set objParams = null;
			if (spooler_job.order_queue() != null) {
				if (spooler_task.order() != null && spooler_task.order().params() != null) {
					objParams = spooler_task.order().params();
				}
			}
			else {
				objParams = spooler_task.params();
			}
			if (objParams != null) {
				Vector<File> transfFiles = ftpCommand.getTransferredFilelist();
				if (transfFiles.size() > 0) {
					for (File curFile : transfFiles) {
						filePaths += curFile.getAbsolutePath() + ";";
						fileNames += curFile.getName() + ";";
					}
					// remove last ";"
					filePaths = filePaths.substring(0, filePaths.length() - 1);
					fileNames = fileNames.substring(0, fileNames.length() - 1);
				}
				// Die Anzahl in count ist redundant. Eigentlich ist transfFiles.size entscheidend
				int count = ftpCommand.getOfTransferFilesCount();
				// Wo ist das dokumentiert, dass diese Order-/Job-Parameter versorgt werden? Im XML des Jobs unter jobdoc
				objParams.set_var(conVarname_ftp_result_files, Integer.toString(count));
				objParams.set_var(conVarname_ftp_result_zero_byte_files, Integer.toString(ftpCommand.getZeroByteCount()));
				objParams.set_var(conVarname_ftp_result_filenames, fileNames);
				objParams.set_var(conVarname_ftp_result_filepaths, filePaths);
				objParams.set_var(conVarname_setback_count, "");
			}
		}
		catch (Exception e) {
			throw new Exception("error occurred creating order Parameter: " + e.getMessage());
		}
	}

	/**
	 * Der Aufruf findet von sos.net.sosftp.SOSFTPCommandReceive.java statt
	 * Die Methode polling ruft Methoden eines Auftragsgesteuerte Jobs und weil die sosftp Klassen Schedulerfrei arbeitet
	 * wird die Methode wieder hier aufgerufen.
	 * 
	 * 
	 * @param filelist
	 * @param isFilePath
	 * @param filePath
	 * @param ftpClient
	 * @param fileSpec
	 * @param recursive
	 * @param forceFiles
	 * @return
	 * @throws Exception
	 */
	public boolean polling(Vector<String> filelist, boolean isFilePath, String filePath, SOSFileTransfer ftpClient, String fileSpec, boolean recursive,
			boolean forceFiles, int pollTimeout, int pollIntervall, int pollMinFiles, String pollFilesErrorState1) throws Exception {
		double delay = pollIntervall;
		getLogger().debug("calling: " + sos.util.SOSClassUtil.getMethodName());
		// getLogger().debug9("lösch mich " + pollTimeout + " " + pollIntervall + " " + pollMinFiles + " setbackcount" + iSetbackCount);
		if (pollTimeout > 0) {
			// before any processing, check if files are available
			boolean flgStopPolling = false;
			boolean giveUpPoll = false;
			Iterator<String> iterator = filelist.iterator();
			double nrOfTries = (pollTimeout * 60) / delay;
			int tries = 0;

			if (isFilePath == false) {
			}
			
			while (!flgStopPolling && !giveUpPoll) {
				tries++;
				int matchedFiles = 0;
				while (iterator.hasNext()) {
					String fileName = (String) iterator.next();
					File file = new File(fileName);
					String strFileName4Matcher = file.getName();
					boolean found = false;
					if (isFilePath) {
						try {
							// we are already in the directory, so use only name:
							long si = ftpClient.size(strFileName4Matcher);
							if (si > -1)
								found = true;
						}
						catch (Exception e) {
							getLogger().debug9("File " + fileName + " not found.");
						}
						if (found) {
							matchedFiles++;
							getLogger().debug8("Found matching file " + fileName);
						}
					}
					else {
						Pattern pattern = Pattern.compile(fileSpec, 0);
						// Matcher matcher = pattern.matcher(fileName);
						Matcher matcher = pattern.matcher(strFileName4Matcher);
						if (matcher.find()) {
							matchedFiles++;
							getLogger().debug8("Found matching file " + fileName);
						} // ^(.*/)2.*\.txt$
					}
				} // iterator.hasNext()
				getLogger().debug3(matchedFiles + " matching files found");
				if (matchedFiles < pollMinFiles) {
					if (flgUseOrderSetBack && (spooler_job.order_queue() != null && spooler_task.order() != null)) {
						/**
						 * kb 2011-07-18
						 * JS fragen, wie der SetBackCount ist. die andere Methode funktioniert nicht, die Var hat immer den Wert 1
						 * Es ist müssig, nach dem Fehler zu suchen, wenn der JS das direkt anbietet.
						 */
						iSetbackCount = spooler_task.order().setback_count();
						flgStopPolling = true;
						Variable_set orderParams = spooler_task.order().params();
						getLogger().info("setback_count is now: " + iSetbackCount + " , maximum number of setbacks: " + nrOfTries);
						if (iSetbackCount >= nrOfTries) {
							orderParams.set_var(conVarname_setback_count, "");
							getLogger().info("give up polling due to max setbacks reached");
							giveUpPoll = true;
						}
						else {
							getLogger().info(matchedFiles + " matching files found." + pollMinFiles + " files required, setting back order.");
							spooler_job.set_delay_order_after_setback(1, delay);
							/**
							 * ist das eigentlich notwendig? der API-Job überwacht doch bereits die Anzahl setBack's
							 * Ja, das ist notwendig. im Job werden die max Anzahl setbacks nicht gesetzt, sondern
							 * nur über die Parameter im Order oder Job 
							 */
							spooler_job.set_max_order_setbacks((int) nrOfTries);
							spooler_task.order().setback();
							iSetbackCount++;
							return false;
						}
					}
					else { // simple job
						if (tries < nrOfTries) {
							Thread.sleep((long) delay * 1000);
							spooler_job.set_state_text("Polling for files... ");
							if (isFilePath) {
								filelist = new Vector<String>();
								filelist.add(filePath);
							}
							else {
								filelist = ftpClient.nList(recursive);
							}
							for (int i = 0; i < filelist.size(); i++) {
								getLogger().debug9(i + " filelist 2 -> " + filelist.get(i));
							}
							iterator = filelist.iterator();
						}
						else {
							giveUpPoll = true;
						}
					}
				}
				else {
					flgStopPolling = true;
					spooler_job.set_state_text("");
				}
				if (giveUpPoll) {
					// keep configuration order monitor from repeating:
					spooler_task.order().params().set_var(conVarname_setback, "false");
					String message = "Failed to find at least " + pollMinFiles + " files matching \"" + fileSpec + "\" ";
					if (isFilePath) {
						message = "Failed to find file \"" + filePath + "\" ";
					}
					message += "after triggering for " + pollTimeout + " minutes.";
					getLogger().debug(message);
					if (matchedFiles > 0) {
						message += " (only " + matchedFiles + " files found)";
					}
					
					if (pollFilesErrorState1 != null && pollFilesErrorState1.length() > 0) {
						getLogger().debug("set order-state to " + pollFilesErrorState1);
						spooler_task.order().set_state(pollFilesErrorState1);
						spooler_task.order().params().set_var(conVarname_ftp_result_error_message, message);
					}
					if (forceFiles) {
						spooler_log.warn(message);
						String body = message + "\n";
						body += "See attached logfile for details.";
						spooler_log.mail().set_body(body);
						spooler_task.end();
					}
					else {
						spooler_log.info(message);
						return true;
					}
					return false;
				}
			}
		}
		return true;
	}
}
