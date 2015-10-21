package sos.ftphistory.job;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import sos.connection.SOSDB2Connection;
import sos.connection.SOSPgSQLConnection;
import sos.ftphistory.sql.Insert_cmd;
import sos.ftphistory.sql.Update_cmd;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSDate;

import com.sos.JSHelper.Exceptions.JSNotImplementedException;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSCsvFile;

//import sos.hostware.Record;

/**
 *
 * @author robert.ehrlich@sos-berlin.com
 *
 *         This job is used to import sosftphistory records from a csv file or
 *         from a database or directly from orders
 *
 */
public class SOSFTPHistoryJob extends JobSchedulerJobAdapter {
	/**
	 * file name samples: - default with field names : -in tab -csv -field-names
	 * -> separator=; delimiter=" - sample without field names: -in
	 * -type=(guid,mandator,transfer_timestamp:Datetime('yyyy-mm-dd HH:MM:SS
	 * '),pid,ppid,operation,localhost,localhost_ip,local_user,remote_host,remote_host_ip,remote_user,protocol,port,local_dir,remote_dir,local_filename,remote_filename,md5,status,last_error_message,log_filename,file_size,jump_host,jump_host_ip,jump_user,jump_protocol,jump_port)
	 * tab -csv | - sample: -in tab -quote='x' -tab='y' -field-names ->
	 * separator=y delimiter=x - sample for ODBC: -in odbc
	 * -conn-str='DRIVER=Microsoft Access Driver (*.mdb);DBQ=c:\my_app\app.mdb'
	 * SELECT <FIELDS> FROM
	 * <TABLE>
	 * - sample for JDBC: -in jdbc -class=oracle.jdbc.driver.OracleDriver
	 * jdbc:oracle:thin:@localhost:1521:orcl -user=appman -password=appman
	 * SELECT <FIELDS> FROM
	 * <TABLE>
	 */
	@SuppressWarnings("unused")
	private final String conClassName = "SOSFTPHistoryJob";
	public final String conSVNVersion = "$Id: SOSDataExchangeEngine.java 19091 2013-02-08 12:49:32Z kb $";

	private String _filePath = "";
	/** hostWare file name prefix for info file */
	private String _filePrefix = "-in tab -csv -field-names";
	/** Anzahl Wiederholungen, um die File Position in der DB zu lesen */
	/** kann als paramater "position_repeat_count" übergeben werden */
	private final int _positionRepeatCount = 3;
	/** Zeit (in Sekunden) zwischen der Wiederholundgen */
	/** kann als paramater "position_repeat_interval" übergeben werden */
	private final int _positionRepeatInterval = 1;
	private LinkedHashMap<String, String> _mappings = null;
	private LinkedHashMap<String, String> _recordExcludedParameterNames = null;
	private LinkedHashMap<String, String> _recordExtraParameterNames = null;
	private int _recordSkippedCount = 0;
	private int _recordSkippedErrorCount = 0;
	private int _recordFoundCount = 0;
	private boolean _exit = false;
	/** zum Switchen source-target */
	private final String _operationSend = "send";
	/** zum Switchen source-target */
	private final String _operationReceive = "receive";
	/** wenn diese parameter gesetz - import file ausführen, sonst import order */
	// private String _paramFileName = "sosftp_history_file";
	private final String _paramFileName = "scheduler_file_path";
	/**
	 * fehlende Werte bei den einigen Spalten
	 * (remote_host_ip,localhost_ip,local_filename
	 * ,remote_filename,file_size,md5) mit diesem Wert füllen
	 */
	private final String _nullValue = "n/a";
	/** Status in der letzten import Zeil : success oder error */
	private String _lastStatus = "";
	private final String _errorStatus = "error";
	private final String _createdBy = "sos";

	@Override
	public boolean spooler_process() {
		boolean rc = true;
		long recordCount = 0;
		Variable_set parameters = null;
		try {
			init();
			parameters = spooler.create_variable_set();
			if (spooler_task.params() != null)
				parameters.merge(spooler_task.params());
			if (spooler_job.order_queue() != null)
				parameters.merge(spooler_task.order().params());
			setConnection(SOSFTPHistory.getConnection(spooler, getConnection(), parameters, getLogger()));
			recordCount = this.doImport(parameters);
			getLogger().info(
					"records: imported = " + recordCount + " ( found = " + _recordFoundCount + " skipped = " + _recordSkippedCount + " skipped [error] = "
							+ _recordSkippedErrorCount + " )");
			return spooler_job.order_queue() != null ? rc : false;
		} catch (Exception e) {
			spooler_log.error("error occurred " + e.getMessage());
			return false;
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	public void init() throws Exception {
		initRecordMappings();
		initRecordExcludedParameterNames();
		_recordExtraParameterNames = null;
		_recordSkippedCount = 0;
		_recordSkippedErrorCount = 0;
		_recordFoundCount = 0;
		_exit = false;
	}

	/**
	 * Feldermapping
	 *
	 * Auflistung von Felder, die immer geliefert werden müssen - per UDP(als
	 * Auftragparameter) oder .CSV
	 */
	private void initRecordMappings() {
		_mappings = new LinkedHashMap<String, String>();
		// key -
		// value - Order Parameter oder Spalte in der CSV Datei
		_mappings.put("mapping_operation", "operation");
		_mappings.put("mapping_mandator", "mandator");
		_mappings.put("mapping_source_host", "localhost");
		_mappings.put("mapping_source_host_ip", "localhost_ip");
		_mappings.put("mapping_source_user", "local_user");
		_mappings.put("mapping_source_dir", "local_dir");
		_mappings.put("mapping_source_filename", "local_filename");
		_mappings.put("mapping_md5", "md5");
		_mappings.put("mapping_file_size", "file_size");
		_mappings.put("mapping_guid", "guid");
		_mappings.put("mapping_transfer_timestamp", "transfer_timestamp");
		_mappings.put("mapping_pid", "pid");
		_mappings.put("mapping_ppid", "ppid");
		_mappings.put("mapping_target_host", "remote_host");
		_mappings.put("mapping_target_host_ip", "remote_host_ip");
		_mappings.put("mapping_target_user", "remote_user");
		_mappings.put("mapping_target_dir", "remote_dir");
		_mappings.put("mapping_target_filename", "remote_filename");
		_mappings.put("mapping_protocol", "protocol");
		_mappings.put("mapping_port", "port");
		_mappings.put("mapping_status", "status");
		_mappings.put("mapping_last_error_message", "last_error_message");
		_mappings.put("mapping_log_filename", "log_filename");
		_mappings.put("mapping_jump_host", "jump_host");
		_mappings.put("mapping_jump_host_ip", "jump_host_ip");
		_mappings.put("mapping_jump_user", "jump_user");
		_mappings.put("mapping_jump_protocol", "jump_protocol");
		_mappings.put("mapping_jump_port", "jump_port");
	}

	/**
	 * bei SOSFTP ist es möglich "custom" Felder zu definieren, die bei UDP als
	 * Auftragsparameter mitgeschickt werden. Damit man diese Felder
	 * identifizieren kann, werden hier Parameter defininiert, die beim Auftrag
	 * dabei sind, aber keine "custom" Felder sind
	 *
	 * ? alternativ Metadaten der Tabelle lesen (Spalten) und mit den
	 * Auftragsparameter vergleichen
	 */
	private void initRecordExcludedParameterNames() {
		_recordExcludedParameterNames = new LinkedHashMap<String, String>();
		_recordExcludedParameterNames.put("db_driver", "1");
		_recordExcludedParameterNames.put("db_password", "1");
		_recordExcludedParameterNames.put("db_url", "1");
		_recordExcludedParameterNames.put("db_user", "1");
		_recordExcludedParameterNames.put("db_class", "1");
		_recordExcludedParameterNames.put("scheduler_order_configuration_loaded", "1");
		_recordExcludedParameterNames.put("configuration_file", "1");
		_recordExcludedParameterNames.put("file_prefix", "1");
	}

	/**
	 * Auftrag oder CSV Import
	 *
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public long doImport(final Variable_set parameters) throws Exception {
		long recordCount = 0;
		boolean isImportFile = false;
		try {
			isImportFile = parameters.value(_paramFileName) != null && parameters.value(_paramFileName).length() > 0;
			if (isImportFile) {
				getLogger().debug1("parameter [" + _paramFileName + "] found. make import file");
				recordCount = importFile(parameters);
			} else {// from order
				getLogger().debug1("parameter [" + _paramFileName + "] not found. make import order");
				_recordFoundCount++;
				recordCount = importOrder(parameters);
			}
		} catch (Exception e) {
			throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : " + e.getMessage(), e);
		}
		return recordCount;
	}

	/**
	 * UDP Auftrag importieren
	 *
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public long importOrder(final Variable_set parameters) throws Exception {
		long recordCount = 0;
		LinkedHashMap<String, String> recordParameters = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> recordExtraParameters = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> allParameters = new LinkedHashMap<String, String>();
		String[] params_names = parameters.names().split(";");

		for (int i = 0; i < params_names.length; i++) {
			if (_mappings.containsValue(params_names[i])) {
				recordParameters.put(params_names[i], parameters.value(params_names[i]));
			} else {
				if (_recordExcludedParameterNames != null && !_recordExcludedParameterNames.containsKey(params_names[i].toLowerCase())) {
					recordExtraParameters.put(params_names[i].toUpperCase(), parameters.value(params_names[i]));
				}
			}
			allParameters.put(params_names[i], parameters.value(params_names[i]));
		}

		if (recordParameters != null && _mappings != null && recordParameters.size() == _mappings.size()) {
			recordExtraParameters = checkRecordeCustomFields(recordExtraParameters);
			try {
				this.getLogger().info("importing from order");
				if (importLine(recordParameters, recordExtraParameters, true)) {
					getConnection().commit();
					recordCount++;
				} else {
					getConnection().rollback();
					_recordSkippedCount++;
				}
			} catch (Exception e) {
				_recordSkippedErrorCount++;
				try {
					getConnection().rollback();
				} catch (Exception ex) {
				}
				String message = "error occurred importing order : " + e.getMessage();
				getLogger().error(message);
				throw new JobSchedulerException(message);
			}
		} else {
			Iterator<Entry<String, String>> it = _mappings.entrySet().iterator();
			String params = "";
			while (it.hasNext()) {
				Entry<String, String> entry = it.next();
				String mappings_val = entry.getValue();
				if (!allParameters.containsKey(mappings_val)) {
					params += params.length() > 0 ? "," + mappings_val : mappings_val;
				}
			}
			throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : missing parameters for import order = " + params);
		}
		return recordCount;
	}

	/**
	 * .CSV Datei importieren
	 *
	 * @param parameters
	 * @return
	 * @throws Exception
	 */
	public long importFile(final Variable_set parameters) throws Exception {
		long recordCount = 0;
		String filePrefix = _filePrefix;
		// sos.hostware.File hwFile = null;
		JSCsvFile hwFile = null;
		LinkedHashMap<String, String> hshRecordFields = null;
		LinkedHashMap<String, String> hshRecordExtraFields = null;
		String localFilename = null;
		long position = 0;
		long fileSize = 0;
		boolean foundPosition = false;
		int positionRepeatCount = _positionRepeatCount;
		int positionRepeatInterval = _positionRepeatInterval;
		long importFileSize = 0;
		StringBuffer sql = null;
		try {
			if (parameters.value("file_prefix") != null && parameters.value("file_prefix").length() > 0) {
				filePrefix = parameters.value("file_prefix");
			}
			if (filePrefix.toLowerCase().indexOf("-class=") != -1 || filePrefix.toLowerCase().indexOf("-conn-str=") > -1) {
				// this.getLogger().info("importing entries from database query ["
				// + filePrefix + "]");
				// this.getLogger().debug("opening database source: " +
				// filePrefix);
				// hwFile.open(filePrefix);
				throw new JSNotImplementedException("Database queries not supported anymore");
			} else {
				// erwartet wird eine
				// <filename>{sos[date:yyyyMMddHHmmssSSS]sos}.csv Datei
				// siehe SOSFTPHistoryReceiveMonitor.spooler_process_before()
				String fileName = parameters.value(_paramFileName);
				if (fileName == null || fileName.length() == 0) {
					throw new JobSchedulerException("missing parameter \"" + _paramFileName + "\" for importFile");
				}
				hwFile = new JSCsvFile(fileName);
				hwFile.CheckColumnCount(false);
				if (!hwFile.exists())
					throw new JobSchedulerException("file does not exist: " + hwFile.getAbsolutePath());
				if (!hwFile.canRead())
					throw new JobSchedulerException("cannot access file: " + hwFile.getAbsolutePath());

				this.getLogger().info("importing entries from file [" + hwFile.getAbsolutePath() + "]");
				this.getLogger().debug("opening file source: " + filePrefix + hwFile.getAbsolutePath());
				// hwFile.open(filePrefix.trim() + " " +
				// hwFile.getAbsolutePath());
				// <filename>{sos[date:yyyyMMddHHmmssSSS]sos}.csv
				localFilename = hwFile.getName().toLowerCase();
				importFileSize = hwFile.length();
				this.getLogger().info("getting file position for  local filename = " + localFilename + " (current import file size = " + importFileSize + ")");
				// Position wurde in der
				// SOSFTPHistoryReceiveMonitor.fillPosition() inserted
				sql = new StringBuffer("select \"POSITION\",\"FILE_SIZE\" from " + SOSFTPHistory.TABLE_FILES_POSITIONS + " ")
						.append("where \"LOCAL_FILENAME\" = '" + SOSFTPHistory.getNormalizedField(getConnection(), localFilename, 255) + "'");
				try {
					if (parameters.value("position_repeat_count") != null && parameters.value("position_repeat_count").length() > 0) {
						positionRepeatCount = Integer.parseInt(parameters.value("position_repeat_count"));
						if (positionRepeatCount <= 0) {
							positionRepeatCount = _positionRepeatCount;
						}
					}
				} catch (Exception e) {
					positionRepeatCount = _positionRepeatCount;
				}
				try {
					if (parameters.value("position_repeat_interval") != null && parameters.value("position_repeat_interval").length() > 0) {
						positionRepeatInterval = Integer.parseInt(parameters.value("position_repeat_interval"));
						if (positionRepeatInterval <= 0) {
							positionRepeatInterval = _positionRepeatInterval;
						}
					}
				} catch (Exception e) {
					positionRepeatInterval = _positionRepeatInterval;
				}
				for (int p = 0; p < positionRepeatCount; p++) {
					HashMap<?, ?> recordPos = this.getConnection().getSingle(sql.toString());
					if (recordPos != null && recordPos.size() > 0) {
						fileSize = Long.parseLong(recordPos.get("file_size").toString());
						if (importFileSize < fileSize) {
							this.getLogger().debug1(
									"last found file position in database: " + recordPos.get("position").toString()
											+ " (position will be not used : current import file size(" + importFileSize + ") < db file size(" + fileSize
											+ ") )");
						} else {
							position = Long.parseLong(recordPos.get("position").toString());
							this.getLogger().debug1("last found file position in database: " + position + " (position will be used)");
						}
						foundPosition = true;
					} else {
						this.getLogger().debug1(
								"not found file position for \"" + localFilename + "\" in database : try in " + positionRepeatInterval + "s again");
					}
					if (foundPosition) {
						break;
					} else {
						Thread.sleep(positionRepeatInterval * 1000);
					}
				}
				if (!foundPosition) {
					this.getLogger().debug1("not found file position for \"" + localFilename + "\" in database : position will be skipped");
				}
			}

			String[] strValues = null;
			hwFile.loadHeaders();
			String[] strHeader = hwFile.Headers();

			for (String header : strHeader) {
				getLogger().debug1("Header-Field:" + header);
			}

			while ((strValues = hwFile.readCSVLine()) != null) {
				_recordFoundCount++;
				// position ?
				if (position >= _recordFoundCount) {
					_recordSkippedCount++;
					continue;
				}
				hshRecordFields = new LinkedHashMap<String, String>();
				hshRecordExtraFields = new LinkedHashMap<String, String>();
				int j = 0;
				for (String val : strValues) {
					String strFieldName = strHeader[j++];
					if (val == null) {
						val = "";
					}
					if (_mappings.containsValue(strFieldName)) {
						hshRecordFields.put(strFieldName, val);
					} else {
						hshRecordExtraFields.put(strFieldName.toUpperCase(), val);
					}
				}
				hshRecordExtraFields = checkRecordeCustomFields(hshRecordExtraFields);
				try {
					if (importLine(hshRecordFields, hshRecordExtraFields, false)) {
						getConnection().commit();
						getLogger().debug1("record " + _recordFoundCount + " imported");
						recordCount++;
					} else {
						getConnection().rollback();
						_recordSkippedCount++;
						getLogger().debug1("record " + _recordFoundCount + " skipped");
					}
				} catch (Exception e) {
					_recordSkippedErrorCount++;
					getLogger().error(
							"error occurred importing file line " + (_recordFoundCount + 1) + " (record " + _recordFoundCount + ") : " + e.getMessage());
					try {
						getConnection().rollback();
					} catch (Exception ex) {
					}
					if (_exit) {
						break;
					}
				}
			}
			hwFile.close();
			// wird auch upgedated, wenn aktuelle FileSize < als DB FileSize
			if (foundPosition && position < _recordFoundCount) {
				try {
					sql = new StringBuffer("update " + SOSFTPHistory.TABLE_FILES_POSITIONS + " ").append("set \"FILE_SIZE\" = " + importFileSize + ", ")
							.append("    \"POSITION\" = " + _recordFoundCount + " ")
							.append("where \"LOCAL_FILENAME\" = '" + SOSFTPHistory.getNormalizedField(getConnection(), localFilename, 255) + "'");
					getConnection().execute(sql.toString());
					getConnection().commit();
				} catch (Exception ee) {
					getConnection().rollback();
				}
			}
		} catch (Exception e) {
			throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : " + e.getMessage(), e);
		} finally {
			if (hwFile != null)
				try {
					hwFile.close();
				} catch (Exception ex) {
				} // ignore this error
		}
		return recordCount;
	}

	/**
	 * Prüfen, ob "custom" Felder in der Datenbank vorhanden sind
	 *
	 * @param recordExtraParameters
	 * @return
	 * @throws Exception
	 */
	public LinkedHashMap<String, String> checkRecordeCustomFields(final LinkedHashMap<String, String> recordExtraParameters) throws Exception {
		LinkedHashMap<String, String> paramsExtra = new LinkedHashMap<String, String>();
		if (_recordExtraParameterNames == null)
			_recordExtraParameterNames = new LinkedHashMap<String, String>();
		try {
			if (recordExtraParameters != null && recordExtraParameters.size() > 0) {
				Iterator<Entry<String, String>> it = recordExtraParameters.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					String field = entry.getKey().toUpperCase();
					String val = entry.getValue();
					try {
						String checkedField = _recordExtraParameterNames.get(field);
						if (checkedField != null) {
							if (checkedField.equals("1")) {
								paramsExtra.put(field, val);
							}
						} else {
							getConnection().getSingleValue("select \"" + field + "\" from " + SOSFTPHistory.TABLE_FILES_HISTORY + " where 1=2");
							paramsExtra.put(field, val);
							_recordExtraParameterNames.put(field, "1");
						}
					} catch (Exception e) {
						_recordExtraParameterNames.put(field, "0");
						if (getConnection() instanceof SOSPgSQLConnection) {
							getConnection().rollback();
						}
					}
				}
			}
		} catch (Exception e) {
			paramsExtra = null;
		}
		return paramsExtra;
	}

	/**
	 *
	 * @param isOrder
	 * @param msg
	 * @throws Exception
	 */
	private void doLineDebug(final boolean isOrder, final String msg) throws Exception {
		if (isOrder) {
			getLogger().debug2(msg);
		} else {
			getLogger().debug9(msg);
		}
	}

	/**
	 * Ein Auftrag bzw eine Zeile der .CSV Datei importieren
	 *
	 * @param hstRecordFields
	 * @param phshRecordCustomFields
	 * @param isOrder
	 * @return
	 * @throws Exception
	 */
	private boolean importLine(final LinkedHashMap<String, String> hstRecordFields, final LinkedHashMap<String, String> phshRecordCustomFields,
			final boolean isOrder) throws Exception {
		StringBuffer sql = new StringBuffer();
		try { // to import the order parameters
				// Operation als 1te
			String operation = getRecordValue(hstRecordFields, "mapping_operation");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": operation = " + operation);
			// status als 2te
			String status = getRecordValue(hstRecordFields, "mapping_status");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": status = " + status);
			_lastStatus = status.toLowerCase();
			String mandator = getRecordValue(hstRecordFields, "mapping_mandator");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": mandator = " + mandator);
			String source_host = getRecordValue(hstRecordFields, "mapping_source_host", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": source_host = " + source_host);
			String source_host_ip = getRecordValue(hstRecordFields, "mapping_source_host_ip", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": source_host_ip = " + source_host_ip);
			String source_user = getRecordValue(hstRecordFields, "mapping_source_user", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": source_user = " + source_user);
			String source_dir = getRecordValue(hstRecordFields, "mapping_source_dir", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": source_dir = " + source_dir);
			String source_filename = getRecordValue(hstRecordFields, "mapping_source_filename", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": source_filename = " + source_filename);
			String md5 = getRecordValue(hstRecordFields, "mapping_md5");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": md5 = " + md5);
			String file_size = getRecordValue(hstRecordFields, "mapping_file_size");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": file_size = " + file_size);
			String guid = getRecordValue(hstRecordFields, "mapping_guid");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": guid = " + guid);
			String transfer_timestamp = getRecordValue(hstRecordFields, "mapping_transfer_timestamp");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": transfer_timestamp = " + transfer_timestamp);
			String pid = getRecordValue(hstRecordFields, "mapping_pid");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": pid = " + pid);
			String ppid = getRecordValue(hstRecordFields, "mapping_ppid");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": ppid = " + ppid);
			String target_host = getRecordValue(hstRecordFields, "mapping_target_host", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": target_host = " + target_host);
			String target_host_ip = getRecordValue(hstRecordFields, "mapping_target_host_ip", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": target_host_ip = " + target_host_ip);
			String target_user = getRecordValue(hstRecordFields, "mapping_target_user", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": target_user = " + target_user);
			String target_dir = getRecordValue(hstRecordFields, "mapping_target_dir", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": target_dir = " + target_dir);
			String target_filename = getRecordValue(hstRecordFields, "mapping_target_filename", operation);
			doLineDebug(isOrder, "record " + _recordFoundCount + ": target_filename = " + target_filename);
			String protocol = getRecordValue(hstRecordFields, "mapping_protocol");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": protocol = " + protocol);
			String port = getRecordValue(hstRecordFields, "mapping_port");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": port = " + port);
			String last_error_message = getRecordValue(hstRecordFields, "mapping_last_error_message");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": last_error_message = " + last_error_message);
			String log_filename = getRecordValue(hstRecordFields, "mapping_log_filename");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": log_filename = " + log_filename);
			String jump_host = getRecordValue(hstRecordFields, "mapping_jump_host");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": jump_host = " + jump_host);
			String jump_host_ip = getRecordValue(hstRecordFields, "mapping_jump_host_ip");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": jump_host_ip = " + jump_host_ip);
			String jump_user = getRecordValue(hstRecordFields, "mapping_jump_user");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": jump_user = " + jump_user);
			String jump_protocol = getRecordValue(hstRecordFields, "mapping_jump_protocol");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": jump_protocol = " + jump_protocol);
			String jump_port = getRecordValue(hstRecordFields, "mapping_jump_port");
			doLineDebug(isOrder, "record " + _recordFoundCount + ": jump_port = " + jump_port);

			if (phshRecordCustomFields != null && phshRecordCustomFields.size() > 0) {
				for (String key : phshRecordCustomFields.keySet()) {
					String entry = phshRecordCustomFields.get(key);
					doLineDebug(isOrder, "record " + _recordFoundCount + ": " + key.toLowerCase() + " = " + entry);
				}
				// Iterator<?> it =
				// phshRecordCustomFields.entrySet().iterator();
				// while (it.hasNext()) {
				// Map.Entry entry = (Map.Entry) it.next();
				// doLineDebug(isOrder, "record " + _recordFoundCount + ": " +
				// entry.getKey().toString().toLowerCase() + " = " +
				// entry.getValue());
				// }
			}
			sql.append("select \"ID\" ").append("from " + SOSFTPHistory.TABLE_FILES + " ").append("where \"MANDATOR\" = '" + mandator + "' and ")
					.append("       \"SOURCE_HOST\" = '" + source_host + "' and ").append("       \"SOURCE_HOST_IP\" = '" + source_host_ip + "' and ")
					.append("       \"SOURCE_DIR\" = '" + source_dir + "' and ").append("       \"SOURCE_FILENAME\" = '" + source_filename + "' and ")
					.append("       \"SOURCE_USER\" = '" + source_user + "' and ").append("       \"MD5\" = '" + md5 + "'");

			String files_id = getConnection().getSingleValue(sql.toString());

			if (files_id == null || files_id.length() == 0 || files_id.equals("0")) {
				Insert_cmd insert1 = new Insert_cmd(getConnection(), getLogger(), SOSFTPHistory.TABLE_FILES);
				insert1.withQuote = true;
				insert1.set("MANDATOR", mandator);
				insert1.set("SOURCE_HOST", source_host);
				insert1.set("SOURCE_HOST_IP", source_host_ip);
				insert1.set("SOURCE_DIR", source_dir);
				insert1.set("SOURCE_FILENAME", source_filename);
				insert1.set("SOURCE_USER", source_user);
				insert1.set("MD5", md5);
				insert1.set_num("FILE_SIZE", file_size);
				insert1.set_direct("CREATED", "%now");
				insert1.set("CREATED_BY", _createdBy);
				insert1.set_direct("MODIFIED", "%now");
				insert1.set("MODIFIED_BY", _createdBy);
				getConnection().execute(insert1.make_cmd());

				if (getConnection() instanceof SOSDB2Connection) {
					files_id = getConnection().getSingleValue("values identity_val_local()");
				} else {
					files_id = getConnection().getLastSequenceValue(SOSFTPHistory.SEQ_TABLE_FILES);
				}
				if (files_id == null || files_id.length() == 0 || files_id.equals("0")) {
					throw new JobSchedulerException("not found lastSequenceValue: SEQ [" + SOSFTPHistory.SEQ_TABLE_FILES + "] for table "
							+ SOSFTPHistory.TABLE_FILES);
				}
			}

			Insert_cmd insert2 = new Insert_cmd(getConnection(), getLogger(), SOSFTPHistory.TABLE_FILES_HISTORY);
			insert2.withQuote = true;
			insert2.set("GUID", guid);
			insert2.set("SOSFTP_ID", files_id);
			insert2.set("OPERATION", operation);
			insert2.set("TRANSFER_TIMESTAMP", transfer_timestamp);
			insert2.set_num("PID", pid);
			insert2.set_num("PPID", ppid);
			insert2.set("TARGET_HOST", target_host);
			insert2.set("TARGET_HOST_IP", target_host);
			insert2.set("TARGET_USER", target_user);
			insert2.set("TARGET_DIR", target_dir);
			insert2.set("TARGET_FILENAME", target_filename);
			insert2.set("PROTOCOL", protocol);
			insert2.set_num("PORT", port);
			insert2.set("STATUS", status);
			insert2.setNull("LAST_ERROR_MESSAGE", last_error_message);
			insert2.setNull("LOG_FILENAME", log_filename);
			insert2.setNull("JUMP_HOST", jump_host);
			insert2.setNull("JUMP_HOST_IP", jump_host_ip);
			insert2.setNull("JUMP_USER", jump_user);
			insert2.setNull("JUMP_PROTOCOL", jump_protocol);
			insert2.set_numNull("JUMP_PORT", jump_port);

			if (phshRecordCustomFields != null && phshRecordCustomFields.size() > 0) {
				Iterator<Entry<String, String>> it = phshRecordCustomFields.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> entry = it.next();
					String val = entry.getValue();
					if (val == null || val.length() == 0) {
						val = "NULL";
					} else {
						val = SOSFTPHistory.getNormalizedField(getConnection(), val, 255);
					}
					insert2.set(entry.getKey(), val);
				}
			}

			String g = getConnection().getSingleValue("select \"GUID\" from " + SOSFTPHistory.TABLE_FILES_HISTORY + " where \"GUID\" = '" + guid + "'");
			if (g == null || g.length() == 0 || isOrder) {
				insert2.set_direct("MODIFIED", "%now");
				insert2.set("MODIFIED_BY", _createdBy);
				insert2.set_direct("CREATED", "%now");
				insert2.set("CREATED_BY", _createdBy);
				getConnection().execute(insert2.make_cmd());
				return true;
			} else {
				if (isOrder) {
					Update_cmd update1 = new Update_cmd(getConnection(), getLogger(), SOSFTPHistory.TABLE_FILES_HISTORY);
					update1.withQuote = true;

					update1.set_where("GUID='" + guid + "'");
					update1.copyFieldsFrom(insert2);

					update1.set_direct("MODIFIED", "%now");
					update1.set("MODIFIED_BY", _createdBy);
					getConnection().execute(update1.make_cmd());

				}
				// bei order warning, bei file skip
			}
		} catch (Exception e) {
			throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : " + e.getMessage());
		}
		return false;
	}

	/**
	 *
	 * @param parameters
	 * @param mappingName
	 * @return
	 * @throws Exception
	 */
	protected String getRecordValue(final HashMap<String, String> parameters, final String mappingName) throws Exception {
		return getRecordValue(parameters, mappingName, null);
	}

	/**
	 * Prüfung ob bestimmte Felder leer, numerisch ... sind Der Wert wird an die
	 * Tabellenfeldlänge abgeschnitten(manuell definiert todo - Metadaten lesen)
	 *
	 * Folgende Felder dürfen leer sein:
	 *
	 * remote_host_ip localhost_ip local_filename, remote_filename file_size md5
	 *
	 * @param record
	 * @param mappingName
	 * @param operation
	 * @return
	 * @throws Exception
	 */
	private String getRecordValue(final HashMap<String, String> record, final String mappingName, final String operation) throws Exception {
		int len = -1;
		String attr_name = _mappings.get(mappingName);
		if (attr_name == null) {
			throw new JobSchedulerException("no found mapping name \"" + mappingName + "\"");
		}
		String attr_val = record.get(attr_name);
		if (attr_val == null) {
			_exit = true;
			throw new JobSchedulerException("no found attr name \"" + attr_name + "\"");
		}
		attr_val = attr_val.trim();
		if (mappingName.equals("mapping_operation")) {
			len = 30;
			attr_val = attr_val.toLowerCase();
		} else if (mappingName.equals("mapping_mandator")) {
			len = 30;
			if (attr_val.length() == 0) {
				attr_val = "sos";
			}
			attr_val = attr_val.toLowerCase();
		} else if (mappingName.equals("mapping_source_host")) {
			len = 128;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_target_host", _operationSend);
			}
		} else if (mappingName.equals("mapping_source_host_ip")) {
			len = 30;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_target_host_ip", _operationSend);
			}
		} else if (mappingName.equals("mapping_source_user")) {
			len = 128;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_target_user", _operationSend);
			}
		} else if (mappingName.equals("mapping_source_dir")) {
			len = 255;
			// is possibly empty for file path selection
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_target_dir", _operationSend);
			}
		} else if (mappingName.equals("mapping_source_filename")) {
			len = 255;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_target_filename", _operationSend);
			}
		} else if (mappingName.equals("mapping_guid")) {
			len = 40;
		} else if (mappingName.equals("mapping_transfer_timestamp")) {
			try {
				SOSDate.getDateTimeAsString(attr_val, "yyyy-MM-dd HH:mm:ss");
			} catch (Exception e) {
				throw new JobSchedulerException("illegal value for parameter [" + attr_name + "] found [yyyy-MM-dd HH:mm:ss]: " + attr_val);
			}
		} else if (mappingName.equals("mapping_file_size") || mappingName.equals("mapping_pid") || mappingName.equals("mapping_ppid")
				|| mappingName.equals("mapping_port") || mappingName.equals("mapping_jump_port")) {
			if (attr_val.length() == 0) {
				attr_val = "0";
			} else {
				try {
					Integer.parseInt(attr_val);
				} catch (Exception e) {
					throw new JobSchedulerException("illegal non-numeric value for parameter [" + attr_name + "]: " + attr_val);
				}
			}
		} else if (mappingName.equals("mapping_target_host")) {
			len = 128;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_source_host", _operationSend);
			}
		} else if (mappingName.equals("mapping_target_host_ip")) {
			len = 30;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_source_host_ip", _operationSend);
			}
		} else if (mappingName.equals("mapping_target_user")) {
			len = 128;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_source_user", _operationSend);
			}
		} else if (mappingName.equals("mapping_target_dir")) {
			len = 255;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_source_dir", _operationSend);
			}
		} else if (mappingName.equals("mapping_target_filename")) {
			len = 255;
			if (operation != null && operation.equals(_operationReceive)) {
				attr_val = getRecordValue(record, "mapping_source_filename", _operationSend);
			}
		} else if (mappingName.equals("mapping_protocol")) {
			len = 10;
		} else if (mappingName.equals("mapping_md5")) {
			len = 50;
		} else if (mappingName.equals("mapping_status")) {
			len = 30;
		} else if (mappingName.equals("mapping_last_error_message")) {
			len = 255;
		} else if (mappingName.equals("mapping_log_filename")) {
			len = 255;
		} else if (mappingName.equals("mapping_jump_host")) {
			len = 128;
		} else if (mappingName.equals("mapping_jump_host_ip")) {
			len = 30;
		} else if (mappingName.equals("mapping_jump_user")) {
			len = 128;
		} else if (mappingName.equals("mapping_jump_protocol")) {
			len = 10;
		}
		if(attr_val.length() == 0){
			attr_val = _nullValue;
		}
		return len > 0 ? SOSFTPHistory.getNormalizedField(getConnection(), attr_val, len) : attr_val;
	}

	public String getFilePath() {
		return _filePath;
	}

	public void setFilePath(final String path) {
		_filePath = path;
	}

	public String getFilePrefix() {
		return _filePrefix;
	}

	public void setFilePrefix(final String prefix) {
		_filePrefix = prefix;
	}

	public LinkedHashMap<String, String> getMappings() {
		return _mappings;
	}

	public void setMappings(final LinkedHashMap<String, String> mappings) {
		_mappings = mappings;
	}
}
