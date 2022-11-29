package com.sos.vfs.common.options;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.JSJobChain;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionArrayList;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBackgroundServiceTransferMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandScript;
import com.sos.JSHelper.Options.SOSOptionCommandScriptFile;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionIniFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod.enuJSTransferModes;
import com.sos.JSHelper.Options.SOSOptionJadeOperation;
import com.sos.JSHelper.Options.SOSOptionJobChainNode;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPlatform;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionRelOp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.Options.SOSOptionZeroByteTransfer;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "SOSBaseOptionsSuperClass", description = "SOSBaseOptionsSuperClass")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSBaseOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -4445655877481869778L;
    private static final String CLASS_NAME = SOSBaseOptionsSuperClass.class.getSimpleName();

    public SOSBaseOptionsSuperClass() {
        currentClass = getClass();
    }

    @JSOptionDefinition(name = "Lazy_Connection_Mode", description = "Connect to Target as late as possible", key = "Lazy_Connection_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean lazyConnectionMode = new SOSOptionBoolean(this, CLASS_NAME + ".Lazy_Connection_Mode",
            "Connect to Target as late as possible", "false", "false", false);

    @JSOptionDefinition(name = "polling_wait_4_Source_Folder", description = "During polling", key = "polling_wait_4_Source_Folder", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollingWait4SourceFolder = new SOSOptionBoolean(this, CLASS_NAME + ".polling_wait_4_Source_Folder", "During polling",
            "false", "false", true);

    @JSOptionDefinition(name = "polling_server", description = "act as a polling server", key = "polling_server", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean pollingServer = new SOSOptionBoolean(this, CLASS_NAME + ".polling_server", "act as a polling server", "false", "false",
            false);

    @JSOptionDefinition(name = "polling_end_at", description = "PollingServer should stop at the specified date/time", key = "polling_end_at", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingEndAt = new SOSOptionTime(this, CLASS_NAME + ".polling_end_at", "Polling should stop at the specified date/time", "0",
            "0", false);

    @JSOptionDefinition(name = "polling_server_poll_forever", description = "poll forever", key = "polling_server_poll_forever", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollingServerPollForever = new SOSOptionBoolean(this, CLASS_NAME + ".polling_server_poll_forever", "poll forever",
            "false", "false", true);

    @JSOptionDefinition(name = "polling_server_duration", description = "How long the PollingServer should run", key = "polling_server_duration", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingServerDuration = new SOSOptionTime(this, CLASS_NAME + ".polling_server_duration",
            "How long the PollingServer should run", "0", "0", false);

    @JSOptionDefinition(name = "mail_on_success", description = "Send a Mail in case of sucess", key = "mail_on_success", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailOnSuccess = new SOSOptionBoolean(this, CLASS_NAME + ".mail_on_success", "Send a Mail in case of sucess", "false",
            "false", false);

    @JSOptionDefinition(name = "mail_on_error", description = "Send a Mail in case of error", key = "mail_on_error", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailOnError = new SOSOptionBoolean(this, CLASS_NAME + ".mail_on_error", "Send a Mail in case of sucess", "false", "false",
            false);

    @JSOptionDefinition(name = "mail_on_empty_files", description = "Send a Mail in case of empty files", key = "mail_on_empty_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailOnEmptyFiles = new SOSOptionBoolean(this, CLASS_NAME + ".mail_on_empty_files", "Send a Mail in case of empty files",
            "false", "false", false);

    @JSOptionDefinition(name = "keep_modification_date", description = "Keep Modification Date of File", key = "keep_modification_date", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean keepModificationDate = new SOSOptionBoolean(this, CLASS_NAME + ".keep_modification_date",
            "Keep Modification Date of File", "false", "false", false);

    @JSOptionDefinition(name = "cumulate_files", description = "cumulate (all) files into one file by append", key = "cumulate_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean cumulateFiles = new SOSOptionBoolean(this, CLASS_NAME + ".cumulate_files", "cumulate (all) files into one file by append",
            "false", "false", false);

    @JSOptionDefinition(name = "cumulative_filename", description = "Name of File into which all files hat to be cumulated", key = "cumulative_filename", type = "SOSOptionFileName", mandatory = true)
    public SOSOptionFileName cumulativeFileName = new SOSOptionFileName(this, CLASS_NAME + ".cumulative_filename",
            "Name of File into which all files hat to be cumulated", "", "", false);

    @JSOptionDefinition(name = "cumulative_file_separator", description = "Text which has to beplaced between cumulated files", key = "cumulative_file_separator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString cumulativeFileSeparator = new SOSOptionString(this, CLASS_NAME + ".cumulative_file_separator",
            "Text which has to beplaced between cumulated files", "", "", false);

    @JSOptionDefinition(name = "cumulative_file_delete", description = "Delete cumulative file before starting transfer", key = "cumulative_file_delete", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean cumulativeFileDelete = new SOSOptionBoolean(this, CLASS_NAME + ".cumulative_file_delete",
            "Delete cumulative file before starting transfer", "false", "false", false);

    @JSOptionDefinition(name = "post_command_disable_for_skipped_transfer", description = "Disable Command to be execute after transfer", key = "post_command_disable_for_skipped_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean post_command_disable_for_skipped_transfer = new SOSOptionBoolean(this, CLASS_NAME
            + ".post_command_disable_for_skipped_transfer", "", "false", "false", false);

    @JSOptionDefinition(name = "pre_command_enable_for_skipped_transfer", description = "Enable Command to be execute before transfer", key = "pre_command_enable_for_skipped_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean pre_command_enable_for_skipped_transfer = new SOSOptionBoolean(this, CLASS_NAME
            + ".pre_command_enable_for_skipped_transfer", "", "false", "false", false);

    @JSOptionDefinition(name = "PollKeepConnection", description = "Keep connection while polling", key = "PollKeepConnection", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollKeepConnection = new SOSOptionBoolean(this, CLASS_NAME + ".PollKeepConnection", "Keep connection while polling",
            "false", "false", true);

    @JSOptionDefinition(name = "FileNameEncoding", description = "Set the encoding-type of a file name", key = "FileNameEncoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString fileNameEncoding = new SOSOptionString(this, CLASS_NAME + ".FileNameEncoding", "Set the encoding-type of a file name", "",
            "ISO-8859-1", false);

    @JSOptionDefinition(name = "History_File_Append_Mode", description = "Specifies wether the History File has to be written in append mode", key = "History_File_Append_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean historyFileAppendMode = new SOSOptionBoolean(this, CLASS_NAME + ".History_File_Append_Mode",
            "Specifies wether the History File has to be written in append mode", "true", "true", false);

    @JSOptionDefinition(name = "HistoryEntries", description = "List of additional entries for the transfer history record.", key = "HistoryEntries", type = "SOSOptionArrayList", mandatory = false)
    public SOSOptionArrayList historyEntries = new SOSOptionArrayList(this, CLASS_NAME + ".HistoryEntries",
            "List of additional entries for the transfer history record.", "", "", false);

    @JSOptionDefinition(name = "SendTransferHistory", description = "If this option is set to true, the transfer history will be sent to the background service.", key = "SendTransferHistory", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean sendTransferHistory = new SOSOptionBoolean(this, CLASS_NAME + ".SendTransferHistory",
            "If this option is set to true, the transfer history will be sent to the background service.", "false", "false", false);

    @JSOptionDefinition(name = "writeTransferHistory", description = "If this option is set to true, the transfer history will be stored in the DB.", key = "writeTransferHistory", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean writeTransferHistory = new SOSOptionBoolean(this, CLASS_NAME + ".writeTransferHistory",
            "If this option is set to true, the transfer history will be be stored in the DB.", "true", "true", false);

    @JSOptionDefinition(name = "Scheduler_Transfer_Method", description = "The technical method of how to communicate with the JobScheduler", key = "Scheduler_Transfer_Method", type = "SOSOptionJSTransferMethod", mandatory = true)
    public SOSOptionBackgroundServiceTransferMethod schedulerTransferMethod = new SOSOptionBackgroundServiceTransferMethod(this, CLASS_NAME
            + ".Scheduler_Transfer_Method", "The technical method of how to communicate with the JobScheduler", enuJSTransferModes.udp.description,
            enuJSTransferModes.udp.description, true);

    @JSOptionDefinition(name = "IntegrityHashType", description = "", key = "integrity_hash_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString integrityHashType = new SOSOptionString(this, CLASS_NAME + ".integrity_hash_type",
            "The Type of the integrity hash, e.g. md5", "md5", "md5", false);
    public SOSOptionString securityHashType = (SOSOptionString) integrityHashType.setAlias("security_hash_type");

    @JSOptionDefinition(name = "ConcurrentTransfer", description = "", key = "Concurrent_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean concurrentTransfer = new SOSOptionBoolean(this, CLASS_NAME + ".Concurrent_Transfer", "Process transfers simultaneously",
            "false", "false", false);

    @JSOptionDefinition(name = "CheckIntegrityHash", description = "", key = "check_integrity_hash", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkIntegrityHash = new SOSOptionBoolean(this, CLASS_NAME + ".check_integrity_hash", "Calculates the integrity hash",
            "false", "false", false);
    public SOSOptionBoolean checkSecurityHash = (SOSOptionBoolean) checkIntegrityHash.setAlias("check_security_hash");

    @JSOptionDefinition(name = "MaxConcurrentTransfers", description = "", key = "Max_Concurrent_Transfers", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger maxConcurrentTransfers = new SOSOptionInteger(this, CLASS_NAME + ".Max_Concurrent_Transfers",
            "Maximum Numbers of parallel transfers", "5", "1", false);

    @JSOptionDefinition(name = "CreateIntegrityHashFile", description = "", key = "create_integrity_hash_file", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createIntegrityHashFile = new SOSOptionBoolean(this, CLASS_NAME + ".create_integrity_hash_file",
            "Flag if an integrity hash file will be created on the target", "false", "false", false);
    public SOSOptionBoolean createSecurityHashFile = (SOSOptionBoolean) createIntegrityHashFile.setAlias("create_security_hash_file");

    @JSOptionDefinition(name = "BufferSize", description = "", key = "buffer_Size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger bufferSize = new SOSOptionInteger(this, CLASS_NAME + ".buffer_Size", "This parameter specifies the interval in seconds",
            "32000", "4096", false);

    @JSOptionDefinition(name = "create_order", description = "Activate file-order creation With this parameter it is possible to specif", key = "create_order", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createOrder = new SOSOptionBoolean(this, CLASS_NAME + ".create_order",
            "Activate file-order creation With this parameter it is possible to specif", "false", "false", false);

    @JSOptionDefinition(name = "create_orders_for_all_files", description = "Create a file-order for every file in the result-list", key = "create_orders_for_all_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createOrdersForAllFiles = new SOSOptionBoolean(this, CLASS_NAME + ".create_orders_for_all_files",
            "Create a file-order for every file in the result-list", "false", "false", false);

    @JSOptionDefinition(name = "create_orders_for_new_files", description = "Create a file-order for each new file in the result-list", key = "create_orders_for_new_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createOrdersForNewFiles = new SOSOptionBoolean(this, CLASS_NAME + ".create_orders_for_new_files",
            "Create a file-order for each new file in the result-list", "false", "false", false);

    @JSOptionDefinition(name = "param_name_for_path", description = "Sets the name of the parameter for the transfered file", key = "param_name_for_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString paramNameForPath = new SOSOptionString(this, CLASS_NAME + ".param_name_for_path",
            "Sets the name of the parameter for the transfered file", "", "scheduler_file_path", false);

    @JSOptionDefinition(name = "expected_size_of_result_set", description = "number of expected hits in result-list", key = "expected_size_of_result_set", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger expectedSizeOfResultSet = new SOSOptionInteger(this, CLASS_NAME + ".expected_size_of_result_set",
            "number of expected hits in result-list", "0", "0", false);

    @JSOptionDefinition(name = "max_file_age", description = "maximum age of a file Specifies the maximum age of a file. If a file", key = "max_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime maxFileAge = new SOSOptionTime(this, CLASS_NAME + ".max_file_age",
            "maximum age of a file Specifies the maximum age of a file. If a file", "0", "0", false);
    public SOSOptionTime fileAgeMaximum = (SOSOptionTime) maxFileAge.setAlias(CLASS_NAME + ".FileAgeMaximum");

    @JSOptionDefinition(name = "max_file_size", description = "maximum size of a file Specifies the maximum size of a file in", key = "max_file_size", type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize maxFileSize = new SOSOptionFileSize(this, CLASS_NAME + ".max_file_size",
            "maximum size of a file Specifies the maximum size of a file in", "-1", "-1", false);
    public SOSOptionFileSize fileSizeMaximum = (SOSOptionFileSize) maxFileSize.setAlias(CLASS_NAME + ".FileSizeMaximum");

    @JSOptionDefinition(name = "min_file_age", description = "minimum age of a file Specifies the minimum age of a files. If the fi", key = "min_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime minFileAge = new SOSOptionTime(this, CLASS_NAME + ".min_file_age",
            "minimum age of a file Specifies the minimum age of a files. If the fi", "0", "0", false);
    public SOSOptionTime fileAgeMinimum = (SOSOptionTime) minFileAge.setAlias(CLASS_NAME + ".FileAgeMinimum");

    @JSOptionDefinition(name = "min_file_size", description = "minimum size of one or multiple files Specifies the minimum size of one", key = "min_file_size", type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize minFileSize = new SOSOptionFileSize(this, CLASS_NAME + ".min_file_size",
            "minimum size of one or multiple files Specifies the minimum size of one", "-1", "-1", false);
    public SOSOptionFileSize fileSizeMinimum = (SOSOptionFileSize) minFileSize.setAlias(CLASS_NAME + ".FileSizeMinimum");

    @JSOptionDefinition(name = "MergeOrderParameter", description = "Merge created order parameter with parameter of current order", key = "MergeOrderParameter", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mergeOrderParameter = new SOSOptionBoolean(this, CLASS_NAME + ".MergeOrderParameter",
            "Merge created order parameter with parameter of current order", "false", "false", false);

    @JSOptionDefinition(name = "next_state", description = "The first node to execute in a jobchain The name of the node of a jobchai", key = "next_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode nextState = new SOSOptionJobChainNode(this, CLASS_NAME + ".next_state",
            "The first node to execute in a jobchain The name of the node of a jobchai", "", "", false);

    @JSOptionDefinition(name = "on_empty_result_set", description = "Set next node on empty result set The next Node (Step, Job) to execute i", key = "on_empty_result_set", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode onEmptyResultSet = new SOSOptionJobChainNode(this, CLASS_NAME + ".on_empty_result_set",
            "Set next node on empty result set The next Node (Step, Job) to execute i", "", "", false);

    @JSOptionDefinition(name = "order_jobscheduler_host", description = "Name of Jobscheduler Host where the order have to be started", key = "order_jobscheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName orderJobschedulerHost = new SOSOptionHostName(this, CLASS_NAME + ".order_jobscheduler_host",
            "Name of Jobscheduler Host where the order have to be started", "", "", false);

    @JSOptionDefinition(name = "order_jobscheduler_port", description = "The port of the JobScheduler node", key = "order_jobscheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber orderJobschedulerPort = new SOSOptionPortNumber(this, CLASS_NAME + ".order_jobscheduler_port",
            "The port of the JobScheduler node", "", "4444", false);

    @JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch", key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString orderJobchainName = new SOSOptionString(this, CLASS_NAME + ".order_jobchain_name",
            "The name of the jobchain which belongs to the order The name of the jobch", "", "", false);

    @JSOptionDefinition(name = "raise_error_if_result_set_is", description = "raise error on expected size of result-set With this parameter it is poss", key = "raise_error_if_result_set_is", type = "SOSOptionRelOp", mandatory = false)
    public SOSOptionRelOp raiseErrorIfResultSetIs = new SOSOptionRelOp(this, CLASS_NAME + ".raise_error_if_result_set_is",
            "raise error on expected size of result-set With this parameter it is poss", "", "", false);

    @JSOptionDefinition(name = "result_list_file", description = "Name of the result-list file If the value of this parameter specifies a v", key = "result_list_file", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName resultListFile = new SOSOptionFileName(this, CLASS_NAME + ".result_list_file",
            "Name of the result-list file If the value of this parameter specifies a v", "", "", false);

    @JSOptionDefinition(name = "scheduler_file_name", description = "Name of the file to process for a file-order", key = "scheduler_file_name", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName schedulerFileName = new SOSOptionFileName(this, CLASS_NAME + ".scheduler_file_name",
            "Name of the file to process for a file-order", "", "", false);

    @JSOptionDefinition(name = "scheduler_file_parent", description = "pathanme of the file to process for a file-order", key = "scheduler_file_parent", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName schedulerFileParent = new SOSOptionFileName(this, CLASS_NAME + ".scheduler_file_parent",
            "pathanme of the file to process for a file-order", "", "", false);

    @JSOptionDefinition(name = "scheduler_file_path", description = "file to process for a file-order Using Directory Monitoring with", key = "scheduler_file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName schedulerFilePath = new SOSOptionFileName(this, CLASS_NAME + ".scheduler_file_path",
            "file to process for a file-order Using Directory Monitoring with", "", "", false);

    @JSOptionDefinition(name = "scheduler_sosfileoperations_resultsetsize", description = "The amount of hits in the result set of the operation", key = "scheduler_sosfileoperations_resultsetsize", type = "SOSOptionsInteger", mandatory = false)
    public SOSOptionInteger schedulerSosFileOperationsResultsetSize = new SOSOptionInteger(this, CLASS_NAME
            + ".scheduler_sosfileoperations_resultsetsize", "The amount of hits in the result set of the operation", "", "", false);
    public SOSOptionInteger resultSetSize = (SOSOptionInteger) schedulerSosFileOperationsResultsetSize.setAlias(CLASS_NAME + ".ResultSetSize");

    @JSOptionDefinition(name = "skip_first_files", description = "number of files to remove from the top of the result-set The numbe", key = "skip_first_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skipFirstFiles = new SOSOptionInteger(this, CLASS_NAME + ".skip_first_files",
            "number of files to remove from the top of the result-set The numbe", "0", "0", false);
    public SOSOptionInteger noOfFirstFiles2Skip = (SOSOptionInteger) skipFirstFiles.setAlias(CLASS_NAME + ".NoOfFirstFiles2Skip");

    @JSOptionDefinition(name = "skip_last_files", description = "number of files to remove from the bottom of the result-set The numbe", key = "skip_last_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skipLastFiles = new SOSOptionInteger(this, CLASS_NAME + ".skip_last_files",
            "number of files to remove from the bottom of the result-set The numbe", "0", "0", false);
    public SOSOptionInteger noOfLastFiles2Skip = (SOSOptionInteger) skipLastFiles.setAlias(CLASS_NAME + ".NoOfLastFiles2Skip");

    @JSOptionDefinition(name = "Max_Files", description = "Maximum number of files to process", key = "Max_Files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger maxFiles = new SOSOptionInteger(this, CLASS_NAME + ".Max_Files", "Maximum number of files to process", "-1", "-1", false);

    @JSOptionDefinition(name = "check_steady_count", description = "Number of tries for Steady check", key = "check_steady_count", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger checkSteadyCount = new SOSOptionInteger(this, CLASS_NAME + ".check_steady_count", "Number of tries for Steady check",
            "10", "10", false);

    @JSOptionDefinition(name = "check_steady_state_interval", description = "The intervall for steady state checking", key = "check_steady_state_interval", type = "SOSOptionFileTime", mandatory = false)
    public SOSOptionTime checkSteadyStateInterval = new SOSOptionTime(this, CLASS_NAME + ".check_steady_state_interval",
            "The intervall for steady state checking", "1", "1", false);

    @JSOptionDefinition(name = "Check_Steady_State_Of_Files", description = "Check wether a file is beeing modified", key = "Check_Steady_State_Of_Files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkSteadyStateOfFiles = new SOSOptionBoolean(this, CLASS_NAME + ".Check_Steady_State_Of_Files",
            "Check wether a file is beeing modified", "false", "false", false);

    @JSOptionDefinition(name = "PollErrorState", description = "Next state in Chain if no files found", key = "Poll_Error_State", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionJobChainNode pollErrorState = new SOSOptionJobChainNode(this, CLASS_NAME + ".Poll_Error_State",
            "Next state in Chain if no files found", "", "", false);
    public SOSOptionJobChainNode noFilesState = (SOSOptionJobChainNode) pollErrorState.setAlias("No_files_state");

    @JSOptionDefinition(name = "Steady_state_error_state", description = "Next state in JobChain if check steady state did not comes to an normal end", key = "Steady_state_error_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode steadyStateErrorState = new SOSOptionJobChainNode(this, CLASS_NAME + ".Steady_state_error_state",
            "Next state in JobChain if check steady state did not comes to an normal end", "", "", false);

    @JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean makeDirs = new SOSOptionBoolean(this, CLASS_NAME + ".make_Dirs", "Create missing Directory on Target", "true", "true",
            false);
    public SOSOptionBoolean createFoldersOnTarget = (SOSOptionBoolean) makeDirs.setAlias("create_folders_on_target");

    @JSOptionDefinition(name = "File_List_Name", description = "File with a list of file names", key = "File_List_Name", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName fileListName = new SOSOptionInFileName(this, CLASS_NAME + ".File_List_Name", "File with a list of file names", "", "",
            false);

    @JSOptionDefinition(name = "Create_Result_Set", description = "Write the ResultSet to a file", key = "Create_Result_Set", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createResultSet = new SOSOptionBoolean(this, CLASS_NAME + ".Create_Result_Set", "Write the ResultSet to a file", "false",
            "false", false);
    public SOSOptionBoolean createResultList = (SOSOptionBoolean) createResultSet.setAlias("create_result_list");

    @JSOptionDefinition(name = "ResultSetFileName", description = "Name of a File with a filelist or a resultlist", key = "Result_Set_FileName", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionOutFileName resultSetFileName = new SOSOptionOutFileName(this, CLASS_NAME + ".Result_Set_File_Name",
            "Name of a File with a filelist or a resultlist", "", "", false);

    @JSOptionDefinition(name = "source_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionFolderName sourceDir = new SOSOptionFolderName(this, CLASS_NAME + ".source_dir",
            "local_dir Local directory into which or from which", "", "", false);

    @JSOptionDefinition(name = "target_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionFolderName targetDir = new SOSOptionFolderName(this, CLASS_NAME + ".target_dir", "target_dir directory into which or from which",
            "", "", false);

    @JSOptionDefinition(name = "append_files", description = "This parameter specifies whether the content of a", key = "append_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean appendFiles = new SOSOptionBoolean(this, CLASS_NAME + ".append_files",
            "This parameter specifies whether the content of a", "false", "false", false);

    @JSOptionDefinition(name = "atomic_prefix", description = "This parameter specifies whether target files shou", key = "atomic_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomicPrefix = new SOSOptionString(this, CLASS_NAME + ".atomic_prefix",
            "This parameter specifies whether target files shou", "", "", false);

    @JSOptionDefinition(name = "atomic_suffix", description = "This parameter specifies whether target files shou", key = "atomic_suffix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomicSuffix = new SOSOptionString(this, CLASS_NAME + ".atomic_suffix",
            "This parameter specifies whether target files shou", "", "", false);

    @JSOptionDefinition(name = "banner_footer", description = "Name der Template-Datei für Protokoll-Ende This p", key = "banner_footer", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName bannerFooter = new SOSOptionInFileName(this, CLASS_NAME + ".banner_footer",
            "Name der Template-Datei für Protokoll-Ende This p", "", "", false);

    @JSOptionDefinition(name = "banner_header", description = "Name of Template-File for log-File", key = "banner_header", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName bannerHeader = new SOSOptionInFileName(this, CLASS_NAME + ".banner_header", "Name of Template-File for log-File", "",
            "", false);

    @JSOptionDefinition(name = "check_interval", description = "This parameter specifies the interval in seconds", key = "check_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger checkInterval = new SOSOptionInteger(this, CLASS_NAME + ".check_interval",
            "This parameter specifies the interval in seconds", "60", "60", false);

    @JSOptionDefinition(name = "check_retry", description = "This parameter specifies whether a file transfer", key = "check_retry", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger checkRetry = new SOSOptionInteger(this, CLASS_NAME + ".check_retry", "This parameter specifies whether a file transfer",
            "0", "0", false);

    @JSOptionDefinition(name = "check_size", description = "This parameter determines whether the original f", key = "check_size", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkSize = new SOSOptionBoolean(this, CLASS_NAME + ".check_size", "This parameter determines whether the original f",
            "true", "true", false);
    public SOSOptionBoolean checkFileSizeAfterTransfer = (SOSOptionBoolean) checkSize.setAlias(CLASS_NAME + ".CheckFileSizeAfterTransfer");

    @JSOptionDefinition(name = "compress_files", description = "This parameter specifies whether the content of the source files", key = "compress_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean compressFiles = new SOSOptionBoolean(this, CLASS_NAME + ".compress_files",
            "This parameter specifies whether the content of the source files", "false", "false", false);

    @JSOptionDefinition(name = "compressed_file_extension", description = "Additional file-name extension for compressed files This parameter spe", key = "compressed_file_extension", type = "SOSOptionString", mandatory = false)
    public SOSOptionString compressedFileExtension = new SOSOptionString(this, CLASS_NAME + ".compressed_file_extension",
            "Additional file-name extension for compressed files This parameter spe", ".gz", ".gz", false);

    @JSOptionDefinition(name = "file_path", description = "This parameter is used alternatively to the parame", key = "file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName filePath = new SOSOptionFileName(this, CLASS_NAME + ".file_path", "This parameter is used alternatively to the parame",
            "", "", false);

    @JSOptionDefinition(name = "file_spec", description = "file_spec This parameter expects a regular expressi", key = "file_spec", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp fileSpec = new SOSOptionRegExp(this, CLASS_NAME + ".file_spec", "file_spec This parameter expects a regular expressi",
            "^.*$", "^.*$", false);
    public SOSOptionRegExp fileNameRegExp = (SOSOptionRegExp) fileSpec.setAlias(CLASS_NAME + ".FileNameRegExp");
    public SOSOptionRegExp fileNamePatternRegExp = (SOSOptionRegExp) fileSpec.setAlias(CLASS_NAME + ".FileNamePatternRegExp");

    @JSOptionDefinition(name = "force_files", description = "This parameter specifies whether an error should b", key = "force_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean forceFiles = new SOSOptionBoolean(this, CLASS_NAME + ".force_files", "This parameter specifies whether an error should b",
            "true", "true", false);
    public SOSOptionBoolean errorOnNoDataFound = (SOSOptionBoolean) forceFiles.setAlias("error_on_no_data_found", "error_when_no_data_found");

    @JSOptionDefinition(name = "history", description = "This parameter causes a history file to be written", key = "history", type = "SOSOptionOutFileName", mandatory = false)
    public SOSOptionOutFileName history = new SOSOptionOutFileName(this, CLASS_NAME + ".history",
            "This parameter causes a history file to be written", "", "", false);
    public SOSOptionOutFileName historyFileName = (SOSOptionOutFileName) history.setAlias("history_file_name");
    public SOSOptionOutFileName sosFtpHistoryFileName = (SOSOptionOutFileName) history.setAlias(CLASS_NAME + ".SOSFtpHistoryFileName");

    @JSOptionDefinition(name = "jump_command", description = "This parameter specifies a command that is to be e", key = "jump_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpCommand = new SOSOptionString(this, CLASS_NAME + ".jump_command", "This parameter specifies a command that is to be e",
            "", "", false);

    @JSOptionDefinition(name = "jump_command_delimiter", description = "Command delimiter for jump pre and post commands", key = "jump_command_delimiter", type = "SOSOptionString", mandatory = true)
    public SOSOptionString jumpCommandDelimiter = new SOSOptionString(this, CLASS_NAME + ".jump_command_delimiter",
            "Command delimiter for jump pre and post commands", ";", ";", true);

    @JSOptionDefinition(name = "jump_command_script", description = "This parameter can be used as an alternative to ju", key = "jump_command_script", type = "SOSOptionCommandScript", mandatory = false)
    public SOSOptionCommandScript jumpCommandScript = new SOSOptionCommandScript(this, CLASS_NAME + ".jump_command_script",
            "This parameter can be used as an alternative to ju", "", "", false);

    @JSOptionDefinition(name = "jump_command_script_file", description = "This parameter can be used as an alternative to ju", key = "jump_command_script_file", type = "SOSOptionCommandScriptFile", mandatory = false)
    public SOSOptionCommandScriptFile jumpCommandScriptFile = new SOSOptionCommandScriptFile(this, CLASS_NAME + ".jump_command_script_file",
            "This parameter can be used as an alternative to ju", "", "", false);

    @JSOptionDefinition(name = "jump_host", description = "When using a jump_host then files are first transf", key = "jump_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName jumpHost = new SOSOptionHostName(this, CLASS_NAME + ".jump_host", "When using a jump_host then files are first transf",
            "", "", false);

    @JSOptionDefinition(name = "jump_password", description = "Password for authentication with the jump_host.", key = "jump_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword jumpPassword = new SOSOptionPassword(this, CLASS_NAME + ".jump_password",
            "Password for authentication with the jump_host.", "", "", false);

    @JSOptionDefinition(name = "jump_port", description = "Port on the jump_host by which files should be tra", key = "jump_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber jumpPort = new SOSOptionPortNumber(this, CLASS_NAME + ".jump_port",
            "Port on the jump_host by which files should be tra", "22", "22", false);

    @JSOptionDefinition(name = "jump_protocol", description = "When using a jump_host then files are first transf", key = "jump_protocol", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProtocol = new SOSOptionString(this, CLASS_NAME + ".jump_protocol",
            "When using a jump_host then files are first transf", "sftp", "sftp", false);

    @JSOptionDefinition(name = "jump_proxy_host", description = "The value of this parameter is the host name or th", key = "jump_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProxyHost = new SOSOptionString(this, CLASS_NAME + ".jump_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @JSOptionDefinition(name = "jump_proxy_password", description = "This parameter specifies the password for the prox", key = "jump_proxy_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProxyPassword = new SOSOptionString(this, CLASS_NAME + ".jump_proxy_password",
            "This parameter specifies the password for the prox", "", "", false);

    @JSOptionDefinition(name = "jump_proxy_port", description = "This parameter specifies the port of a proxy that", key = "jump_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProxyPort = new SOSOptionString(this, CLASS_NAME + ".jump_proxy_port",
            "This parameter specifies the port of a proxy that", "", "", false);

    @JSOptionDefinition(name = "jump_proxy_user", description = "The value of this parameter specifies the user acc", key = "jump_proxy_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jumpProxyUser = new SOSOptionUserName(this, CLASS_NAME + ".jump_proxy_user",
            "The value of this parameter specifies the user acc", "", "", false);

    @JSOptionDefinition(name = "jump_proxy_protocol", description = "Jump Proxy protocol", key = "jump_proxy_protocol", type = "SOSOptionProxyProtocol", mandatory = false)
    public SOSOptionProxyProtocol jumpProxyProtocol = new SOSOptionProxyProtocol(this, CLASS_NAME + ".jump_proxy_protocol", "Jump Proxy protocol",
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    @JSOptionDefinition(name = "jump_ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "jump_ssh_auth_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName jumpSshAuthFile = new SOSOptionInFileName(this, CLASS_NAME + ".jump_ssh_auth_file",
            "This parameter specifies the path and name of a us", "", "", false);

    @JSOptionDefinition(name = "jump_ssh_auth_method", description = "This parameter specifies the authentication method", key = "jump_ssh_auth_method", type = "SOSOptionString", mandatory = false)
    public SOSOptionAuthenticationMethod jumpSshAuthMethod = new SOSOptionAuthenticationMethod(this, CLASS_NAME + ".jump_ssh_auth_method",
            "This parameter specifies the authentication method", "", "", false);

    @JSOptionDefinition(name = "jump_user", description = "User name for authentication with the jump_host.", key = "jump_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jumpUser = new SOSOptionUserName(this, CLASS_NAME + ".jump_user", "User name for authentication with the jump_host.", "",
            "", false);

    @JSOptionDefinition(name = "jump_dir", description = "This parameter specifies the directory on the jump host", key = "jump_dir", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpDir = new SOSOptionString(this, CLASS_NAME + ".jump_dir", "This parameter specifies the directory on the jump host",
            "/tmp", "/tmp", false);

    @JSOptionDefinition(name = "jump_strict_hostKey_checking", description = "Check the hostkey against known hosts for SSH", key = "jump_strict_hostKey_checking", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jumpStrictHostkeyChecking = new SOSOptionBoolean(this, CLASS_NAME + ".jump_strict_hostkey_checking",
            "Check the hostkey against known hosts for SSH", "false", "false", false);

    @JSOptionDefinition(name = "jump_platform", description = "This parameter specifies the platform on the jump host", key = "jump_platform", type = "SOSOptionPlatform", mandatory = false)
    public SOSOptionPlatform jump_platform = new SOSOptionPlatform(this, CLASS_NAME + ".jump_platform",
            "This parameter specifies the platform on the jump host", SOSOptionPlatform.enuValidPlatforms.unix.name(),
            SOSOptionPlatform.enuValidPlatforms.unix.name(), false);

    @JSOptionDefinition(name = "jump_configuration_files", description = "Configuration file with JCraft settings located on the YADE client system", key = "jump_configuration_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpConfigurationFiles = new SOSOptionString(this, CLASS_NAME + ".jump_configuration_files",
            "Configuration file with JCraft settings located on the YADE client system", "", "", false);

    @JSOptionDefinition(name = "jump_server_alive_interval", description = "Sets the interval to send a keep-alive message. can contains not integer value", key = "jump_server_alive_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_server_alive_interval = new SOSOptionString(this, CLASS_NAME + ".jump_server_alive_interval",
            "Sets the interval to send a keep-alive message", "", "", false);

    @JSOptionDefinition(name = "jump_server_alive_count_max", description = "Sets the number of keep-alive messages which may be sent without receiving any messages back from the server.", key = "jump_server_alive_count_max", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jump_server_alive_count_max = new SOSOptionInteger(this, CLASS_NAME + ".jump_server_alive_count_max",
            "Sets the number of keep-alive messages which may be sent without receiving any messages back from the server.", "", "", false);

    @JSOptionDefinition(name = "jump_connect_timeout", description = "Sets the interval for jump socket connect", key = "jump_connect_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_connect_timeout = new SOSOptionString(this, CLASS_NAME + ".jump_connect_timeout",
            "Sets the interval for session connect", "", "", false);

    @JSOptionDefinition(name = "jump_channel_connect_timeout", description = "Sets the interval for channel connect", key = "jump_channel_connect_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_channel_connect_timeout = new SOSOptionString(this, CLASS_NAME + ".jump_channel_connect_timeout",
            "Sets the interval for cannel connect", "", "", false);

    @JSOptionDefinition(name = "local_dir", description = "local_dir Local directory into which or from which", key = "local_dir", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName localDir = new SOSOptionFolderName(this, CLASS_NAME + ".local_dir",
            "local_dir Local directory into which or from which", "", "", false);

    @JSOptionDefinition(name = "mandator", description = "This parameter specifies the mandator for which a", key = "mandator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mandator = new SOSOptionString(this, CLASS_NAME + ".mandator", "This parameter specifies the mandator for which a", "SOS",
            "SOS", false);

    @JSOptionDefinition(name = "operation", description = "Operation to be executed send, receive, remove,", key = "operation", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionJadeOperation operation = new SOSOptionJadeOperation(this, CLASS_NAME + ".operation",
            "Operation to be executed send, receive, remove,", "send", "send", true);

    @JSOptionDefinition(name = "overwrite_files", description = "This parameter specifies if existing files should", key = "overwrite_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean overwriteFiles = new SOSOptionBoolean(this, CLASS_NAME + ".overwrite_files",
            "This parameter specifies if existing files should", "true", "true", false);

    @JSOptionDefinition(name = "jump_pre_command", description = "Command, which has to be executed on the jump host for each file before the transfer started.", key = "jump_pre_command", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPreCommand = new SOSOptionCommandString(this, CLASS_NAME + ".jump_pre_command",
            "Commands, which has to be executed on the jump host for each file before the transfer started.", "", "", false);

    @JSOptionDefinition(name = "jump_post_command_on_success", description = "Command, which has to be executed on the jump host for each file after the transfer of the file succesfull ended.", key = "jump_post_command_on_success", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostCommandOnSuccess = new SOSOptionCommandString(this, CLASS_NAME + ".jump_post_command_on_success",
            "Commands, which has to be executed on the jump host for each file after the transfer of the file succesfull ended.", "", "", false);

    @JSOptionDefinition(name = "jump_pre_transfer_commands", description = "Commands, which has to be executed on the jump host before the transfer started.", key = "jump_pre_transfer_commands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPreTransferCommands = new SOSOptionCommandString(this, CLASS_NAME + ".jump_pre_transfer_commands",
            "Commands, which has to be executed on the jump host before the transfer started.", "", "", false);

    @JSOptionDefinition(name = "jump_post_transfer_commands_on_success", description = "Commands, which has to be executed on the jump host after the transfer ended successful.", key = "jump_post_transfer_commands_on_success", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostTransferCommandsOnSuccess = new SOSOptionCommandString(this, CLASS_NAME
            + ".jump_post_transfer_commands_on_success", "Commands, which has to be executed on the jump host after the transfer ended successful.",
            "", "", false);

    @JSOptionDefinition(name = "jump_post_transfer_commands_on_error", description = "Commands, which has to be executed on the jump host after the transfer ended with errors.", key = "jump_post_transfer_commands_on_error", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostTransferCommandsOnError = new SOSOptionCommandString(this, CLASS_NAME
            + ".jump_post_transfer_commands_on_error", "Commands, which has to be executed on the jump host after the transfer ended with errors.",
            "", "", false);

    @JSOptionDefinition(name = "jump_post_transfer_commands_final", description = "Commands, which has to be executed on the jump host after the transfer ended independet of the transfer status.", key = "jump_post_transfer_commands_final", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostTransferCommandsFinal = new SOSOptionCommandString(this, CLASS_NAME + ".jump_post_transfer_commands_final",
            "Commands, which has to be executed on the jump host after the transfer ended independet of the transfer status.", "", "", false);

    @JSOptionDefinition(name = "jump_preferred_authentications", description = "This parameter specifies preferred authentication methods,e.g password,publickey,...", key = "jump_preferred_authentications", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_preferred_authentications = new SOSOptionString(this, CLASS_NAME + ".jump_preferred_authentications",
            "This parameter specifies the preferred authentication methods", "", "", false);

    @JSOptionDefinition(name = "jump_required_authentications", description = "This parameter specifies the required authentication methods,e.g password,publickey,...", key = "jump_required_authentications", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_required_authentications = new SOSOptionString(this, CLASS_NAME + ".jump_required_authentications",
            "This parameter specifies the required authentication methods", "", "", false);

    @JSOptionDefinition(name = "jump_passphrase", description = "This parameter specifies the passphrase", key = "jump_passphrase", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_passphrase = new SOSOptionString(this, CLASS_NAME + ".jump_passphrase", "This parameter specifies the passphrase", "",
            "", false);

    @JSOptionDefinition(name = "jump_use_credential_store", description = "", key = "jump_use_credential_store", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_use_credential_store = new SOSOptionBoolean(this, CLASS_NAME + ".jump_use_credential_store", "", "false", "false",
            false);

    @JSOptionDefinition(name = "jump_CredentialStore_FileName", description = "", key = "jump_CredentialStore_FileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName jump_CredentialStore_FileName = new SOSOptionInFileName(this, CLASS_NAME + ".jump_CredentialStore_FileName", "", "",
            "", false);

    @JSOptionDefinition(name = "jump_CredentialStore_AuthenticationMethod", description = "", key = "jump_CredentialStore_AuthenticationMethod", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_CredentialStore_AuthenticationMethod = new SOSOptionString(this, CLASS_NAME
            + ".jump_CredentialStore_AuthenticationMethod", "", "privatekey", "privatekey", false);

    @JSOptionDefinition(name = "jump_CredentialStore_KeyFileName", description = "", key = "jump_CredentialStore_KeyFileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName jump_CredentialStore_KeyFileName = new SOSOptionInFileName(this, CLASS_NAME + ".jump_CredentialStore_KeyFileName", "",
            "", "", false);

    @JSOptionDefinition(name = "jump_CredentialStore_Password", description = "", key = "jump_CredentialStore_Password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword jump_CredentialStore_Password = new SOSOptionPassword(this, CLASS_NAME + ".jump_CredentialStore_Password", "", "", "",
            false);

    @JSOptionDefinition(name = "jump_CredentialStore_KeyPath", description = "", key = "jump_CredentialStore_KeyPath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_CredentialStore_KeyPath = new SOSOptionString(this, CLASS_NAME + ".jump_CredentialStore_KeyPath", "", "", "", false);

    @JSOptionDefinition(name = "poll_interval", description = "This parameter specifies the interval in seconds", key = "poll_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionTime pollInterval = new SOSOptionTime(this, CLASS_NAME + ".poll_interval", "This parameter specifies the interval in seconds",
            "60", "60", false);

    @JSOptionDefinition(name = "Waiting_for_Late_comers", description = "Wait an additional interval for late comers", key = "Waiting_for_Late_comers", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean waitingForLateComers = new SOSOptionBoolean(this, CLASS_NAME + ".Waiting_for_Late_comers",
            "Wait an additional interval for late comers", "false", "false", false);

    @JSOptionDefinition(name = "poll_minfiles", description = "This parameter specifies the number of files tha", key = "poll_minfiles", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger pollMinfiles = new SOSOptionInteger(this, CLASS_NAME + ".poll_minfiles",
            "This parameter specifies the number of files tha", "0", "0", false);

    @JSOptionDefinition(name = "PollingDuration", description = "The duration of the polling period", key = "PollingDuration", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingDuration = new SOSOptionTime(this, CLASS_NAME + ".PollingDuration", "The duration of the polling period", "0", "0",
            false);

    @JSOptionDefinition(name = "poll_timeout", description = "This parameter specifies the time in minutes, how", key = "poll_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger pollTimeout = new SOSOptionInteger(this, CLASS_NAME + ".poll_timeout",
            "This parameter specifies the time in minutes, how", "0", "0", false);

    @JSOptionDefinition(name = "profile", description = "The Name of a Profile-Section to be executed", key = "profile", type = "SOSOptionString", mandatory = false)
    public SOSOptionString profile = new SOSOptionString(this, CLASS_NAME + ".profile", "The Name of a Profile-Section to be executed", "", "",
            false);

    public SOSOptionString sectionName = (SOSOptionString) profile.setAlias(CLASS_NAME + ".SectionName");

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, CLASS_NAME + ".protocol",
            "Type of requested Datatransfer The values ftp, sftp", "ftp", "ftp", true);

    public SOSOptionTransferType transferProtocol = (SOSOptionTransferType) protocol.setAlias(CLASS_NAME + ".TransferProtocol");

    @JSOptionDefinition(name = "recursive", description = "This parameter specifies if files from subdirector", key = "recursive", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean recursive = new SOSOptionBoolean(this, CLASS_NAME + ".recursive", "This parameter specifies if files from subdirector",
            "false", "false", false);

    public SOSOptionBoolean IncludeSubdirectories = (SOSOptionBoolean) recursive.setAlias("include_sub_directories");

    public SOSOptionBoolean recurseSubFolders = (SOSOptionBoolean) recursive.setAlias(CLASS_NAME + ".RecurseSubFolders");

    @JSOptionDefinition(name = "remote_dir", description = "remote_dir Directory at the FTP/SFTP server from wh", key = "remote_dir", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName remoteDir = new SOSOptionFolderName(this, CLASS_NAME + ".remote_dir",
            "remote_dir Directory at the FTP/SFTP server from wh", ".", ".", false);

    @JSOptionDefinition(name = "remove_files", description = "This parameter specifies whether files on the FTP/", key = "remove_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean removeFiles = new SOSOptionBoolean(this, CLASS_NAME + ".remove_files",
            "This parameter specifies whether files on the FTP/", "false", "false", false);

    public SOSOptionBoolean deleteFilesAfterTransfer = (SOSOptionBoolean) removeFiles.setAlias(CLASS_NAME + ".DeleteFilesAfterTransfer");

    public SOSOptionBoolean deleteFilesOnSource = (SOSOptionBoolean) removeFiles.setAlias(CLASS_NAME + ".DeleteFilesOnSource");

    @JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement", type = "SOSOptionString", mandatory = false)
    public SOSOptionString replacement = new SOSOptionString(this, CLASS_NAME + ".replacement", "String for replacement of matching character seque",
            null, null, false);

    public SOSOptionString ReplaceWith = (SOSOptionString) replacement.setAlias(CLASS_NAME + ".ReplaceWith");

    @JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replacing = new SOSOptionRegExp(this, CLASS_NAME + ".replacing", "Regular expression for filename replacement with", "",
            "", false);

    public SOSOptionRegExp ReplaceWhat = (SOSOptionRegExp) replacing.setAlias(CLASS_NAME + ".ReplaceWhat");

    @JSOptionDefinition(name = "scheduler_host", description = "This parameter specifies the host name or IP addre", key = "scheduler_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName schedulerHost = new SOSOptionHostName(this, CLASS_NAME + ".scheduler_host",
            "This parameter specifies the host name or IP addre", "", "", false);

    public SOSOptionHostName backgroundServiceHost = (SOSOptionHostName) schedulerHost.setAlias("Background_Service_Host");

    @JSOptionDefinition(name = "scheduler_job_chain", description = "The name of a job chain for Managed File Transfer", key = "scheduler_job_chain", type = "JSJobChain", mandatory = false)
    public JSJobChain schedulerJobChain = new JSJobChain(this, CLASS_NAME + ".scheduler_job_chain", "The name of a job chain for Background Service",
            "/sos/jade/jade_history", "/sos/jade/jade_history", false);

    public JSJobChain backgroundServiceJobChainName = (JSJobChain) schedulerJobChain.setAlias("BackgroundService_Job_Chain_Name");

    @JSOptionDefinition(name = "scheduler_port", description = "The port for which a Job Scheduler for Managed File Trans", key = "scheduler_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber schedulerPort = new SOSOptionPortNumber(this, CLASS_NAME + ".scheduler_port",
            "The port for which a Job Scheduler for Managed File Trans", "0", "4444", false);

    public SOSOptionPortNumber backgroundServicePort = (SOSOptionPortNumber) schedulerPort.setAlias("Background_Service_Port",
            "Background_Service_PortNumber");

    @JSOptionDefinition(name = "Restart", description = "Set Restart/Resume Mode for Transfer", key = "Restart", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean restart = new SOSOptionBoolean(this, CLASS_NAME + ".Restart", "Set Restart/Resume Mode for Transfer", "false", "false",
            false);

    public SOSOptionBoolean resumeTransfer = (SOSOptionBoolean) restart.setAlias(CLASS_NAME + "Resume", CLASS_NAME + "Resume_Transfer");

    @JSOptionDefinition(name = "settings", description = "Name of INI-File which contains the transfer profiles to execute", key = "settings", type = "SOSOptionIniFileName", mandatory = false)
    public SOSOptionIniFileName settings = new SOSOptionIniFileName(this, CLASS_NAME + ".settings",
            "Name of INI-File which contains the transfer profiles to execute", "", "", false);

    public SOSOptionIniFileName configurationFile = (SOSOptionIniFileName) settings.setAlias("JADE_Configuration_File", "JADE_Config_File",
            "Configuration", "JADE_Configuration", "JADE_INI_FILE");

    public SOSOptionIniFileName sosIniFileName = (SOSOptionIniFileName) settings.setAlias(CLASS_NAME + ".SOSIniFileName");

    @JSOptionDefinition(name = "skip_transfer", description = "If this Parameter is set to true then", key = "skip_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean skipTransfer = new SOSOptionBoolean(this, CLASS_NAME + ".skip_transfer", "If this Parameter is set to true then", "false",
            "false", false);

    @JSOptionDefinition(name = "transactional", description = "This parameter specifies if file transfers should", key = "transactional", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean transactional = new SOSOptionBoolean(this, CLASS_NAME + ".transactional",
            "This parameter specifies if file transfers should", "false", "false", false);

    public SOSOptionBoolean transactionMode = (SOSOptionBoolean) transactional.setAlias(CLASS_NAME + ".TransactionMode");

    @JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode", mandatory = false)
    public SOSOptionTransferMode transferMode = new SOSOptionTransferMode(this, CLASS_NAME + ".transfer_mode", "Type of Character-Encoding Transfe",
            "binary", "binary", false);

    @JSOptionDefinition(name = "verbose", description = "The granuality of (Debug-)Messages The verbosit", key = "verbose", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger verbose = new SOSOptionInteger(this, CLASS_NAME + ".verbose", "The granuality of (Debug-)Messages The verbosit", "1",
            "10", false);

    public SOSOptionInteger verbosityLevel = (SOSOptionInteger) verbose.setAlias(CLASS_NAME + ".VerbosityLevel");

    @JSOptionDefinition(name = "zero_byte_transfer", description = "This parameter specifies whether zero byte files", key = "zero_byte_transfer", type = "SOSOptionZeroByteTransfer", mandatory = false)
    public SOSOptionZeroByteTransfer zeroByteTransfer = new SOSOptionZeroByteTransfer(this, CLASS_NAME + ".zero_byte_transfer",
            "This parameter specifies whether zero byte files", "yes", "yes", false);
    public SOSOptionZeroByteTransfer transferZeroByteFiles = (SOSOptionZeroByteTransfer) zeroByteTransfer.setAlias("transfer_zero_byte_files");

    @JSOptionDefinition(name = "ProtocolCommandListener", description = "Activate the logging for Apache ftp client", key = "protocol_command_listener", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean protocolCommandListener = new SOSOptionBoolean(this, CLASS_NAME + ".protocol_command_listener",
            "Activate the logging for Apache ftp client", "false", "false", true);

    @JSOptionDefinition(name = "system_property_files", description = "List of the java property files separated by semicolon", key = "system_property_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString system_property_files = new SOSOptionString(this, CLASS_NAME + ".system_property_files",
            "List of the java property files separated by semicolon", "", "", false);

    @JSOptionDefinition(name = "updateConfiguration", description = "determines if a YADE configuration should be updated with the given XML snippet", key = "updateConfiguration", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean updateConfiguration = new SOSOptionBoolean(this, CLASS_NAME + ".updateConfiguration",
            "determines if a YADE configuration should be updated with the given XML snippet", "", "", false);

    @JSOptionDefinition(name = "xmlUpdate", description = "the XML configuration snippet to update a YADE configuration with", key = "xmlUpdate", type = "SOSOptionString", mandatory = false)
    public SOSOptionString xmlUpdate = new SOSOptionString(this, CLASS_NAME + ".xmlUpdate",
            "the XML configuration snippet to update a YADE configuration with", "", "", false);

    @JSOptionDefinition(name = "connection_error_retry_count_max", description = "The connection_error_retry_count_max", key = "connection_error_retry_count_max", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger connection_error_retry_count_max = new SOSOptionInteger(this, CLASS_NAME + ".connection_error_retry_count_max",
            "The connection_error_retry_count_max", "0", "0", false);

    @JSOptionDefinition(name = "connection_error_retry_interval", description = "The connection_error_retry_interval in seconds", key = "connection_error_retry_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionString connection_error_retry_interval = new SOSOptionString(this, CLASS_NAME + ".connection_error_retry_interval",
            "The connection_error_retry_interval", "0s", "0s", false);

    @JSOptionDefinition(name = "return-values", description = "environment variable transfer history", key = "return-values", type = "SOSOptionString", mandatory = false)
    public SOSOptionString return_values = new SOSOptionString(this, CLASS_NAME + ".return-values", "environment variable transfer history", "", "",
            false);

    // see sos.net.ssh.SOSSSHJobOptionsSuperClass ssh_provider
    // for default value handling see com.sos.vfs.sftp.SOSSFTP
    @JSOptionDefinition(name = "ssh_provider", description = "ssh provider implementation", key = "ssh_provider", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_provider = new SOSOptionString(this, CLASS_NAME + ".ssh_provider", "ssh provider", "", "", false);

    // for default value handling see com.sos.vfs.webdav.SOSWebDAV
    @JSOptionDefinition(name = "webdav_provider", description = "webdav provider implementation", key = "webdav_provider", type = "SOSOptionString", mandatory = false)
    public SOSOptionString webdav_provider = new SOSOptionString(this, CLASS_NAME + ".webdav_provider", "webdav provider", "", "", false);

    // for default value handling see com.sos.vfs.smb.SOSSMB
    @JSOptionDefinition(name = "smb_provider", description = "smb provider implementation", key = "smb_provider", type = "SOSOptionString", mandatory = false)
    public SOSOptionString smb_provider = new SOSOptionString(this, CLASS_NAME + ".smb_provider", "smb provider", "", "", false);

}