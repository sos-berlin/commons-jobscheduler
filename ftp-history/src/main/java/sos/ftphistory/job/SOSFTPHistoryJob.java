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

/** @author robert ehrlich */
public class SOSFTPHistoryJob extends JobSchedulerJobAdapter {

    private static final String OPERATION_SEND = "send";
    private static final String OPERATION_RECEIVE = "receive";
    private static final String PARAM_FILE_NAME = "scheduler_file_path";
    private static final String NULL_VALUE = "n/a";
    private static final String CREATED_BY = "sos";
    private static final int POSITION_REPEAT_COUNT = 3;
    private static final int POSITION_REPEAT_INTERVAL = 1;
    private String _filePath = "";
    private String _filePrefix = "-in tab -csv -field-names";
    private LinkedHashMap<String, String> _mappings = null;
    private LinkedHashMap<String, String> _recordExcludedParameterNames = null;
    private LinkedHashMap<String, String> _recordExtraParameterNames = null;
    private int _recordSkippedCount = 0;
    private int _recordSkippedErrorCount = 0;
    private int _recordFoundCount = 0;
    private boolean _exit = false;

    @Override
    public boolean spooler_process() {
        boolean rc = true;
        long recordCount = 0;
        Variable_set parameters = null;
        try {
            init();
            parameters = spooler.create_variable_set();
            if (spooler_task.params() != null) {
                parameters.merge(spooler_task.params());
            }
            if (spooler_job.order_queue() != null) {
                parameters.merge(spooler_task.order().params());
            }
            setConnection(SOSFTPHistory.getConnection(spooler, getConnection(), parameters, getLogger()));
            recordCount = this.doImport(parameters);
            getLogger().info("records: imported = " + recordCount + " ( found = " + _recordFoundCount + " skipped = " + _recordSkippedCount + " skipped [error] = "
                            + _recordSkippedErrorCount + " )");
            return spooler_job.order_queue() != null ? rc : false;
        } catch (Exception e) {
            spooler_log.error("error occurred " + e.getMessage());
            return false;
        }
    }

    public void init() throws Exception {
        initRecordMappings();
        initRecordExcludedParameterNames();
        _recordExtraParameterNames = null;
        _recordSkippedCount = 0;
        _recordSkippedErrorCount = 0;
        _recordFoundCount = 0;
        _exit = false;
    }

    private void initRecordMappings() {
        _mappings = new LinkedHashMap<String, String>();
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

    public long doImport(final Variable_set parameters) throws Exception {
        long recordCount = 0;
        boolean isImportFile = false;
        try {
            isImportFile = parameters.value(PARAM_FILE_NAME) != null && !parameters.value(PARAM_FILE_NAME).isEmpty();
            if (isImportFile) {
                getLogger().debug1("parameter [" + PARAM_FILE_NAME + "] found. make import file");
                recordCount = importFile(parameters);
            } else {
                getLogger().debug1("parameter [" + PARAM_FILE_NAME + "] not found. make import order");
                _recordFoundCount++;
                recordCount = importOrder(parameters);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : " + e.getMessage(), e);
        }
        return recordCount;
    }

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
                    params += !params.isEmpty() ? "," + mappings_val : mappings_val;
                }
            }
            throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : missing parameters for import order = " + params);
        }
        return recordCount;
    }

    public long importFile(final Variable_set parameters) throws Exception {
        long recordCount = 0;
        String filePrefix = _filePrefix;
        JSCsvFile hwFile = null;
        LinkedHashMap<String, String> hshRecordFields = null;
        LinkedHashMap<String, String> hshRecordExtraFields = null;
        String localFilename = null;
        long position = 0;
        long fileSize = 0;
        boolean foundPosition = false;
        int positionRepeatCount = POSITION_REPEAT_COUNT;
        int positionRepeatInterval = POSITION_REPEAT_INTERVAL;
        long importFileSize = 0;
        StringBuilder sql = null;
        try {
            if (parameters.value("file_prefix") != null && !parameters.value("file_prefix").isEmpty()) {
                filePrefix = parameters.value("file_prefix");
            }
            if (filePrefix.toLowerCase().indexOf("-class=") != -1 || filePrefix.toLowerCase().indexOf("-conn-str=") > -1) {
                throw new JSNotImplementedException("Database queries not supported anymore");
            } else {
                String fileName = parameters.value(PARAM_FILE_NAME);
                if (fileName == null || fileName.isEmpty()) {
                    throw new JobSchedulerException("missing parameter \"" + PARAM_FILE_NAME + "\" for importFile");
                }
                hwFile = new JSCsvFile(fileName);
                hwFile.CheckColumnCount(false);
                if (!hwFile.exists()) {
                    throw new JobSchedulerException("file does not exist: " + hwFile.getAbsolutePath());
                }
                if (!hwFile.canRead()) {
                    throw new JobSchedulerException("cannot access file: " + hwFile.getAbsolutePath());
                }
                this.getLogger().info("importing entries from file [" + hwFile.getAbsolutePath() + "]");
                this.getLogger().debug("opening file source: " + filePrefix + hwFile.getAbsolutePath());
                localFilename = hwFile.getName().toLowerCase();
                importFileSize = hwFile.length();
                this.getLogger().info("getting file position for  local filename = " + localFilename + " (current import file size = " + importFileSize + ")");
                sql = new StringBuilder("select \"POSITION\",\"FILE_SIZE\" from ").append(SOSFTPHistory.TABLE_FILES_POSITIONS).append(" ")
                        .append("where \"LOCAL_FILENAME\" = '").append(SOSFTPHistory.getNormalizedField(getConnection(), localFilename, 255)).append("'");
                try {
                    if (parameters.value("position_repeat_count") != null && !parameters.value("position_repeat_count").isEmpty()) {
                        positionRepeatCount = Integer.parseInt(parameters.value("position_repeat_count"));
                        if (positionRepeatCount <= 0) {
                            positionRepeatCount = POSITION_REPEAT_COUNT;
                        }
                    }
                } catch (Exception e) {
                    positionRepeatCount = POSITION_REPEAT_COUNT;
                }
                try {
                    if (parameters.value("position_repeat_interval") != null && !parameters.value("position_repeat_interval").isEmpty()) {
                        positionRepeatInterval = Integer.parseInt(parameters.value("position_repeat_interval"));
                        if (positionRepeatInterval <= 0) {
                            positionRepeatInterval = POSITION_REPEAT_INTERVAL;
                        }
                    }
                } catch (Exception e) {
                    positionRepeatInterval = POSITION_REPEAT_INTERVAL;
                }
                for (int p = 0; p < positionRepeatCount; p++) {
                    HashMap<?, ?> recordPos = this.getConnection().getSingle(sql.toString());
                    if (recordPos != null && !recordPos.isEmpty()) {
                        fileSize = Long.parseLong(recordPos.get("file_size").toString());
                        if (importFileSize < fileSize) {
                            this.getLogger().debug1("last found file position in database: " + recordPos.get("position").toString()
                                + " (position will be not used : current import file size(" + importFileSize + ") < db file size(" + fileSize + ") )");
                        } else {
                            position = Long.parseLong(recordPos.get("position").toString());
                            this.getLogger().debug1("last found file position in database: " + position + " (position will be used)");
                        }
                        foundPosition = true;
                    } else {
                        this.getLogger().debug1("not found file position for \"" + localFilename + "\" in database : try in " + positionRepeatInterval + "s again");
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
                    getLogger().error("error occurred importing file line " + (_recordFoundCount + 1) + " (record " + _recordFoundCount + ") : " + e.getMessage());
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
            if (foundPosition && position < _recordFoundCount) {
                try {
                    sql = new StringBuilder("update ").append(SOSFTPHistory.TABLE_FILES_POSITIONS).append(" ").append("set \"FILE_SIZE\" = ")
                            .append(importFileSize).append(", ").append("    \"POSITION\" = ").append(_recordFoundCount).append(" ")
                            .append("where \"LOCAL_FILENAME\" = '").append(SOSFTPHistory.getNormalizedField(getConnection(), localFilename, 255)).append("'");
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
                    // ignore this error
                }
        }
        return recordCount;
    }

    public LinkedHashMap<String, String> checkRecordeCustomFields(final LinkedHashMap<String, String> recordExtraParameters) throws Exception {
        LinkedHashMap<String, String> paramsExtra = new LinkedHashMap<String, String>();
        if (_recordExtraParameterNames == null)
            _recordExtraParameterNames = new LinkedHashMap<String, String>();
        try {
            if (recordExtraParameters != null && !recordExtraParameters.isEmpty()) {
                Iterator<Entry<String, String>> it = recordExtraParameters.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, String> entry = it.next();
                    String field = entry.getKey().toUpperCase();
                    String val = entry.getValue();
                    try {
                        String checkedField = _recordExtraParameterNames.get(field);
                        if (checkedField != null) {
                            if ("1".equals(checkedField)) {
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

    private void doLineDebug(final boolean isOrder, final String msg) throws Exception {
        if (isOrder) {
            getLogger().debug2(msg);
        } else {
            getLogger().debug9(msg);
        }
    }

    private boolean importLine(final LinkedHashMap<String, String> hstRecordFields, final LinkedHashMap<String, String> phshRecordCustomFields,
            final boolean isOrder) throws Exception {
        StringBuilder sql = new StringBuilder();
        try {
            String operation = getRecordValue(hstRecordFields, "mapping_operation");
            doLineDebug(isOrder, "record " + _recordFoundCount + ": operation = " + operation);
            String status = getRecordValue(hstRecordFields, "mapping_status");
            doLineDebug(isOrder, "record " + _recordFoundCount + ": status = " + status);
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
            if (phshRecordCustomFields != null && !phshRecordCustomFields.isEmpty()) {
                for (String key : phshRecordCustomFields.keySet()) {
                    String entry = phshRecordCustomFields.get(key);
                    doLineDebug(isOrder, "record " + _recordFoundCount + ": " + key.toLowerCase() + " = " + entry);
                }
            }
            sql.append("select \"ID\" ").append("from ").append(SOSFTPHistory.TABLE_FILES).append(" ").append("where \"MANDATOR\" = '").append(mandator)
                .append("' and ").append("       \"SOURCE_HOST\" = '").append(source_host).append("' and ").append("       \"SOURCE_HOST_IP\" = '")
                .append(source_host_ip + "' and ").append("       \"SOURCE_DIR\" = '").append(source_dir).append("' and ").append("       \"SOURCE_FILENAME\" = '")
                .append(source_filename).append("' and ").append("       \"SOURCE_USER\" = '").append(source_user).append("' and ").append("       \"MD5\" = '")
                .append(md5).append("'");
            String files_id = getConnection().getSingleValue(sql.toString());
            if (files_id == null || files_id.isEmpty() || "0".equals(files_id)) {
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
                insert1.set("CREATED_BY", CREATED_BY);
                insert1.set_direct("MODIFIED", "%now");
                insert1.set("MODIFIED_BY", CREATED_BY);
                getConnection().execute(insert1.make_cmd());
                if (getConnection() instanceof SOSDB2Connection) {
                    files_id = getConnection().getSingleValue("values identity_val_local()");
                } else {
                    files_id = getConnection().getLastSequenceValue(SOSFTPHistory.SEQ_TABLE_FILES);
                }
                if (files_id == null || files_id.isEmpty() || "0".equals(files_id)) {
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
            if (phshRecordCustomFields != null && !phshRecordCustomFields.isEmpty()) {
                Iterator<Entry<String, String>> it = phshRecordCustomFields.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, String> entry = it.next();
                    String val = entry.getValue();
                    if (val == null || val.isEmpty()) {
                        val = "NULL";
                    } else {
                        val = SOSFTPHistory.getNormalizedField(getConnection(), val, 255);
                    }
                    insert2.set(entry.getKey(), val);
                }
            }
            String g = getConnection().getSingleValue("select \"GUID\" from " + SOSFTPHistory.TABLE_FILES_HISTORY + " where \"GUID\" = '" + guid + "'");
            if (g == null || g.isEmpty() || isOrder) {
                insert2.set_direct("MODIFIED", "%now");
                insert2.set("MODIFIED_BY", CREATED_BY);
                insert2.set_direct("CREATED", "%now");
                insert2.set("CREATED_BY", CREATED_BY);
                getConnection().execute(insert2.make_cmd());
                return true;
            } else {
                if (isOrder) {
                    Update_cmd update1 = new Update_cmd(getConnection(), getLogger(), SOSFTPHistory.TABLE_FILES_HISTORY);
                    update1.withQuote = true;
                    update1.set_where("GUID='" + guid + "'");
                    update1.copyFieldsFrom(insert2);
                    update1.set_direct("MODIFIED", "%now");
                    update1.set("MODIFIED_BY", CREATED_BY);
                    getConnection().execute(update1.make_cmd());
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException(SOSClassUtil.getMethodName() + " : " + e.getMessage());
        }
        return false;
    }

    protected String getRecordValue(final HashMap<String, String> parameters, final String mappingName) throws Exception {
        return getRecordValue(parameters, mappingName, null);
    }

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
        if ("mapping_operation".equals(mappingName)) {
            len = 30;
            attr_val = attr_val.toLowerCase();
        } else if ("mapping_mandator".equals(mappingName)) {
            len = 30;
            if (attr_val.isEmpty()) {
                attr_val = "sos";
            }
            attr_val = attr_val.toLowerCase();
        } else if ("mapping_source_host".equals(mappingName)) {
            len = 128;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_target_host", OPERATION_SEND);
            }
        } else if ("mapping_source_host_ip".equals(mappingName)) {
            len = 30;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_target_host_ip", OPERATION_SEND);
            }
        } else if ("mapping_source_user".equals(mappingName)) {
            len = 128;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_target_user", OPERATION_SEND);
            }
        } else if ("mapping_source_dir".equals(mappingName)) {
            len = 255;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_target_dir", OPERATION_SEND);
            }
        } else if ("mapping_source_filename".equals(mappingName)) {
            len = 255;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_target_filename", OPERATION_SEND);
            }
        } else if ("mapping_guid".equals(mappingName)) {
            len = 40;
        } else if ("mapping_transfer_timestamp".equals(mappingName)) {
            try {
                SOSDate.getDateTimeAsString(attr_val, "yyyy-MM-dd HH:mm:ss");
            } catch (Exception e) {
                throw new JobSchedulerException("illegal value for parameter [" + attr_name + "] found [yyyy-MM-dd HH:mm:ss]: " + attr_val);
            }
        } else if ("mapping_file_size".equals(mappingName) || "mapping_pid".equals(mappingName) || "mapping_ppid".equals(mappingName)
                || "mapping_port".equals(mappingName) || "mapping_jump_port".equals(mappingName)) {
            if (attr_val.isEmpty()) {
                attr_val = "0";
            } else {
                try {
                    Integer.parseInt(attr_val);
                } catch (Exception e) {
                    throw new JobSchedulerException("illegal non-numeric value for parameter [" + attr_name + "]: " + attr_val);
                }
            }
        } else if ("mapping_target_host".equals(mappingName)) {
            len = 128;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_source_host", OPERATION_SEND);
            }
        } else if ("mapping_target_host_ip".equals(mappingName)) {
            len = 30;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_source_host_ip", OPERATION_SEND);
            }
        } else if ("mapping_target_user".equals(mappingName)) {
            len = 128;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_source_user", OPERATION_SEND);
            }
        } else if ("mapping_target_dir".equals(mappingName)) {
            len = 255;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_source_dir", OPERATION_SEND);
            }
        } else if ("mapping_target_filename".equals(mappingName)) {
            len = 255;
            if (operation != null && OPERATION_RECEIVE.equals(operation)) {
                attr_val = getRecordValue(record, "mapping_source_filename", OPERATION_SEND);
            }
        } else if ("mapping_protocol".equals(mappingName)) {
            len = 10;
        } else if ("mapping_md5".equals(mappingName)) {
            len = 50;
        } else if ("mapping_status".equals(mappingName)) {
            len = 30;
        } else if ("mapping_last_error_message".equals(mappingName)) {
            len = 255;
        } else if ("mapping_log_filename".equals(mappingName)) {
            len = 255;
        } else if ("mapping_jump_host".equals(mappingName)) {
            len = 128;
        } else if ("mapping_jump_host_ip".equals(mappingName)) {
            len = 30;
        } else if ("mapping_jump_user".equals(mappingName)) {
            len = 128;
        } else if ("mapping_jump_protocol".equals(mappingName)) {
            len = 10;
        }
        if (attr_val.isEmpty()) {
            attr_val = NULL_VALUE;
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