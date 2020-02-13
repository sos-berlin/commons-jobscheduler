package com.sos.VirtualFileSystem.Options;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
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
import com.sos.JSHelper.Options.SOSOptionProcessID;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionRelOp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.Options.SOSOptionZeroByteTransfer;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

@JSOptionClass(name = "SOSFtpOptionsSuperClass", description = "SOSFtpOptionsSuperClass")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public abstract class SOSFtpOptionsSuperClass extends JSOptionsClass implements ISOSConnectionOptions, ISOSAuthenticationOptions, ISOSFtpOptions {

    protected Messages objMsg = new Messages(this.getClass().getAnnotation(I18NResourceBundle.class).baseName());
    private static final long serialVersionUID = -4445655877481869778L;
    private static final String CLASSNAME = "SOSFtpOptionsSuperClass";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSFtpOptionsSuperClass.class);

    public SOSFtpOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSFtpOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSFtpOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @JSOptionDefinition(name = "TFN_Post_Command", description = "Post commands executed after creating the final TargetFile", key = "TFN_Post_Command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString tfnPostCommand = new SOSOptionString(this, CLASSNAME + ".TFN_Post_Command",
            "Post commands executed after creating the final TargetFileName", "", "", false);

    public SOSOptionString getTfnPostCommand() {
        return tfnPostCommand;
    }

    public SOSFtpOptionsSuperClass setTfnPostCommand(final SOSOptionString pstrValue) {
        tfnPostCommand = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "polling_wait_4_Source_Folder", description = "During polling", key = "polling_wait_4_Source_Folder", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollingWait4SourceFolder = new SOSOptionBoolean(this, CLASSNAME + ".polling_wait_4_Source_Folder", "During polling",
            "false", "false", true);

    public String getPollingWait4SourceFolder() {
        return pollingWait4SourceFolder.getValue();
    }

    public SOSFtpOptionsSuperClass setPollingWait4SourceFolder(final String pstrValue) {
        pollingWait4SourceFolder.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "include", description = "the include directive as an option", key = "include", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include = new SOSOptionString(this, CLASSNAME + ".include", "the include directive as an option", "", "", false);

    public String getinclude() {
        return include.getValue();
    }

    public SOSFtpOptionsSuperClass setinclude(final String pstrValue) {
        include.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "use_filters", description = "Use filters for source and/or Targe", key = "use_filters", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean useFilters = new SOSOptionBoolean(this, CLASSNAME + ".use_filters", "Use filters for source and/or Targe", "false",
            "false", false);

    public SOSOptionBoolean getUseFilters() {
        return useFilters;
    }

    public SOSFtpOptionsSuperClass setUseFilters(final SOSOptionBoolean pstrValue) {
        useFilters = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "is_fragment", description = "Mark an profile as a fragment", key = "is_fragment", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean isFragment = new SOSOptionBoolean(this, CLASSNAME + ".is_fragment", "Mark an profile as a fragment", "false", "false",
            false);

    public String getIsFragment() {
        return isFragment.getValue();
    }

    public SOSFtpOptionsSuperClass setIsFragment(final String pstrValue) {
        isFragment.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "reuse_connection", description = "reuse the current connections for all transfers", key = "reuse_connection", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean reuseConnection = new SOSOptionBoolean(this, CLASSNAME + ".reuse_connection",
            "reuse the current connections for all transfers", "false", "false", false);

    public String getReuseConnection() {
        return reuseConnection.getValue();
    }

    public SOSFtpOptionsSuperClass setReuseConnection(final String pstrValue) {
        reuseConnection.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_server", description = "act as a polling server", key = "polling_server", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean pollingServer = new SOSOptionBoolean(this, CLASSNAME + ".polling_server", "act as a polling server", "false", "false",
            false);

    public String getPollingServer() {
        return pollingServer.getValue();
    }

    public SOSFtpOptionsSuperClass setPollingServer(final String pstrValue) {
        pollingServer.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_end_at", description = "PollingServer should stop at the specified date/time", key = "polling_end_at", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingEndAt = new SOSOptionTime(this, CLASSNAME + ".polling_end_at", "Polling should stop at the specified date/time", "0",
            "0", false);

    public String getPollingEndAt() {
        return pollingEndAt.getValue();
    }

    public SOSFtpOptionsSuperClass setPollingEndAt(final String pstrValue) {
        pollingEndAt.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_server_poll_forever", description = "poll forever", key = "polling_server_poll_forever", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollingServerPollForever = new SOSOptionBoolean(this, CLASSNAME + ".polling_server_poll_forever", "poll forever", "false",
            "false", true);

    public String getPollingServerPollForever() {
        return pollingServerPollForever.getValue();
    }

    public SOSFtpOptionsSuperClass setPollingServerPollForever(final String pstrValue) {
        pollingServerPollForever.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "polling_server_duration", description = "How long the PollingServer should run", key = "polling_server_duration", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingServerDuration = new SOSOptionTime(this, CLASSNAME + ".polling_server_duration",
            "How long the PollingServer should run", "0", "0", false);

    public String getPollingServerDuration() {
        return pollingServerDuration.getValue();
    }

    public SOSFtpOptionsSuperClass setPollingServerDuration(final String pstrValue) {
        pollingServerDuration.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Lazy_Connection_Mode", description = "Connect to Target as late as possible", key = "Lazy_Connection_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean lazyConnectionMode = new SOSOptionBoolean(this, CLASSNAME + ".Lazy_Connection_Mode",
            "Connect to Target as late as possible", "false", "false", false);

    public String getLazyConnectionMode() {
        return lazyConnectionMode.getValue();
    }

    public SOSFtpOptionsSuperClass setLazyConnectionMode(final String pstrValue) {
        lazyConnectionMode.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "platform", description = "platform on which the app is running", key = "platform", type = "SOSOptionString", mandatory = false)
    public SOSOptionPlatform platform = new SOSOptionPlatform(this, CLASSNAME + ".platform", "platform on which the app is running", "", "", false);

    public String getPlatform() {
        return platform.getValue();
    }

    public SOSFtpOptionsSuperClass setPlatform(final String pstrValue) {
        platform.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_success", description = "Send a Mail in case of sucess", key = "mail_on_success", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailOnSuccess = new SOSOptionBoolean(this, CLASSNAME + ".mail_on_success", "Send a Mail in case of sucess", "false",
            "false", false);

    public String getMailOnSuccess() {
        return mailOnSuccess.getValue();
    }

    public SOSFtpOptionsSuperClass setMailOnSuccess(final String pstrValue) {
        mailOnSuccess.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_error", description = "Send a Mail in case of error", key = "mail_on_error", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailOnError = new SOSOptionBoolean(this, CLASSNAME + ".mail_on_error", "Send a Mail in case of sucess", "false", "false",
            false);

    public String getMailOnError() {
        return mailOnError.getValue();
    }

    public SOSFtpOptionsSuperClass setMailOnError(final String pstrValue) {
        mailOnError.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_empty_files", description = "Send a Mail in case of empty files", key = "mail_on_empty_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mailOnEmptyFiles = new SOSOptionBoolean(this, CLASSNAME + ".mail_on_empty_files", "Send a Mail in case of empty files",
            "false", "false", false);

    public String getMailOnEmptyFiles() {
        return mailOnEmptyFiles.getValue();
    }

    public SOSFtpOptionsSuperClass setMailOnEmptyFiles(final String pstrValue) {
        mailOnEmptyFiles.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "title", description = "The Title for a section /profile", key = "title", type = "SOSOptionString", mandatory = false)
    public SOSOptionString title = new SOSOptionString(this, CLASSNAME + ".title", "The Title for a section /profile", "", "", false);

    public String getTitle() {
        return title.getValue();
    }

    public SOSFtpOptionsSuperClass setTitle(final String pstrValue) {
        title.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "keep_modification_date", description = "Keep Modification Date of File", key = "keep_modification_date", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean keepModificationDate = new SOSOptionBoolean(this, CLASSNAME + ".keep_modification_date", "Keep Modification Date of File",
            "false", "false", false);

    public String getKeepModificationDate() {
        return keepModificationDate.getValue();
    }

    public SOSFtpOptionsSuperClass setKeepModificationDate(final String pstrValue) {
        keepModificationDate.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulate_files", description = "cumulate (all) files into one file by append", key = "cumulate_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean cumulateFiles = new SOSOptionBoolean(this, CLASSNAME + ".cumulate_files", "cumulate (all) files into one file by append",
            "false", "false", false);

    public String getCumulateFiles() {
        return cumulateFiles.getValue();
    }

    public SOSFtpOptionsSuperClass setCumulateFiles(final String pstrValue) {
        cumulateFiles.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_filename", description = "Name of File into which all files hat to be cumulated", key = "cumulative_filename", type = "SOSOptionFileName", mandatory = true)
    public SOSOptionFileName cumulativeFileName = new SOSOptionFileName(this, CLASSNAME + ".cumulative_filename",
            "Name of File into which all files hat to be cumulated", "", "", false);

    public String getCumulativeFilename() {
        return cumulativeFileName.getValue();
    }

    public SOSFtpOptionsSuperClass setCumulativeFilename(final String pstrValue) {
        cumulativeFileName.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_file_separator", description = "Text which has to beplaced between cumulated files", key = "cumulative_file_separator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString cumulativeFileSeparator = new SOSOptionString(this, CLASSNAME + ".cumulative_file_separator",
            "Text which has to beplaced between cumulated files", "", "", false);

    public String getCumulativeFileSeparator() {
        return cumulativeFileSeparator.getValue();
    }

    public SOSFtpOptionsSuperClass setCumulativeFileSeparator(final String pstrValue) {
        cumulativeFileSeparator.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_file_delete", description = "Delete cumulative file before starting transfer", key = "cumulative_file_delete", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean cumulativeFileDelete = new SOSOptionBoolean(this, CLASSNAME + ".cumulative_file_delete",
            "Delete cumulative file before starting transfer", "false", "false", false);

    public String getCumulativeFileDelete() {
        return cumulativeFileDelete.getValue();
    }

    public SOSFtpOptionsSuperClass setCumulativeFileDelete(final String pstrValue) {
        cumulativeFileDelete.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Post_Command", description = "FTP-Command to be executed after transfer", key = "Post_Command", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString postCommand = new SOSOptionCommandString(this, CLASSNAME + ".Post_Command",
            "FTP-Command to be executed after transfer", "", "", false);

    public String getPostCommand() {
        return postCommand.getValue();
    }

    public SOSFtpOptionsSuperClass setPostCommand(final String pstrValue) {
        postCommand.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "post_command_disable_for_skipped_transfer", description = "Disable Command to be execute after transfer", key = "post_command_disable_for_skipped_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean post_command_disable_for_skipped_transfer = new SOSOptionBoolean(this, CLASSNAME
            + ".post_command_disable_for_skipped_transfer", "", "false", "false", false);

    @JSOptionDefinition(name = "Pre_Command", description = "FTP-Command to be execute before transfer", key = "Pre_Command", type = "SOSOptionString  ", mandatory = false)
    public SOSOptionCommandString preCommand = new SOSOptionCommandString(this, CLASSNAME + ".Pre_Command", "", "", "", false);

    public String getPreCommand() {
        return preCommand.getValue();
    }

    public SOSFtpOptionsSuperClass setPreCommand(final String pstrValue) {
        preCommand.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "pre_command_enable_for_skipped_transfer", description = "Enable Command to be execute before transfer", key = "pre_command_enable_for_skipped_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean pre_command_enable_for_skipped_transfer = new SOSOptionBoolean(this, CLASSNAME
            + ".pre_command_enable_for_skipped_transfer", "", "false", "false", false);

    @JSOptionDefinition(name = "PollKeepConnection", description = "Keep connection while polling", key = "PollKeepConnection", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollKeepConnection = new SOSOptionBoolean(this, CLASSNAME + ".PollKeepConnection", "Keep connection while polling",
            "false", "false", true);

    public String getPollKeepConnection() {
        return pollKeepConnection.getValue();
    }

    public SOSFtpOptionsSuperClass setPollKeepConnection(final String pstrValue) {
        pollKeepConnection.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "FileNameEncoding", description = "Set the encoding-type of a file name", key = "FileNameEncoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString fileNameEncoding = new SOSOptionString(this, CLASSNAME + ".FileNameEncoding", "Set the encoding-type of a file name", "",
            "ISO-8859-1", false);

    public String getFileNameEncoding() {
        return fileNameEncoding.getValue();
    }

    public SOSFtpOptionsSuperClass setFileNameEncoding(final String pstrValue) {
        fileNameEncoding.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "History_File_Append_Mode", description = "Specifies wether the History File has to be written in append mode", key = "History_File_Append_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean historyFileAppendMode = new SOSOptionBoolean(this, CLASSNAME + ".History_File_Append_Mode",
            "Specifies wether the History File has to be written in append mode", "true", "true", false);

    public SOSOptionBoolean getHistoryFileAppendMode() {
        return historyFileAppendMode;
    }

    public SOSFtpOptionsSuperClass setHistoryFileAppendMode(final SOSOptionBoolean pstrValue) {
        historyFileAppendMode = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "HistoryEntries", description = "List of additional entries for the transfer history record.", key = "HistoryEntries", type = "SOSOptionArrayList", mandatory = false)
    public SOSOptionArrayList historyEntries = new SOSOptionArrayList(this, CLASSNAME + ".HistoryEntries",
            "List of additional entries for the transfer history record.", "", "", false);

    public String getHistoryEntries() {
        return historyEntries.getValue();
    }

    public SOSFtpOptionsSuperClass setHistoryEntries(final String pstrValue) {
        historyEntries.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "SendTransferHistory", description = "If this option is set to true, the transfer history will be sent to the background service.", key = "SendTransferHistory", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean sendTransferHistory = new SOSOptionBoolean(this, CLASSNAME + ".SendTransferHistory",
            "If this option is set to true, the transfer history will be sent to the background service.", "false", "false", false);

    public String getSendTransferHistory() {
        return sendTransferHistory.getValue();
    }

    public SOSFtpOptionsSuperClass setSendTransferHistory(final String pstrValue) {
        sendTransferHistory.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "writeTransferHistory", description = "If this option is set to true, the transfer history will be stored in the DB.", key = "writeTransferHistory", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean writeTransferHistory = new SOSOptionBoolean(this, CLASSNAME + ".writeTransferHistory",
            "If this option is set to true, the transfer history will be be stored in the DB.", "true", "true", false);

    public String getWriteTransferHistory() {
        return writeTransferHistory.getValue();
    }

    public SOSFtpOptionsSuperClass setWriteTransferHistory(final String value) {
        writeTransferHistory.setValue(value);
        return this;
    }

    @JSOptionDefinition(name = "Scheduler_Transfer_Method", description = "The technical method of how to communicate with the JobScheduler", key = "Scheduler_Transfer_Method", type = "SOSOptionJSTransferMethod", mandatory = true)
    public SOSOptionBackgroundServiceTransferMethod schedulerTransferMethod = new SOSOptionBackgroundServiceTransferMethod(this, CLASSNAME
            + ".Scheduler_Transfer_Method", "The technical method of how to communicate with the JobScheduler", enuJSTransferModes.udp.description,
            enuJSTransferModes.udp.description, true);

    public String getSchedulerTransferMethod() {
        return schedulerTransferMethod.getValue();
    }

    public SOSFtpOptionsSuperClass setSchedulerTransferMethod(final String pstrValue) {
        schedulerTransferMethod.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PreFtpCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreFtpCommands", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString preFtpCommands = new SOSOptionCommandString(this, CLASSNAME + ".Pre_Ftp_Commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);
    public SOSOptionCommandString preTransferCommands = (SOSOptionCommandString) preFtpCommands.setAlias("pre_transfer_commands");

    public String getPreFtpCommands() {
        return preFtpCommands.getValue();
    }

    public SOSFtpOptionsSuperClass setPreFtpCommands(final String pstrValue) {
        preFtpCommands.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.", key = "PostTransferCommands", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString postTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);
    public SOSOptionString postFtpCommands = (SOSOptionString) postTransferCommands.setAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return postTransferCommands.getValue();
    }

    public SOSFtpOptionsSuperClass setPostTransferCommands(final String pstrValue) {
        postTransferCommands.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "IntegrityHashType", description = "", key = "integrity_hash_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString integrityHashType = new SOSOptionString(this, CLASSNAME + ".integrity_hash_type",
            "The Type of the integrity hash, e.g. md5", "md5", "md5", false);
    public SOSOptionString securityHashType = (SOSOptionString) integrityHashType.setAlias("security_hash_type");

    @JSOptionDefinition(name = "DecompressAfterTransfer", description = "", key = "Decompress_After_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean decompressAfterTransfer = new SOSOptionBoolean(this, CLASSNAME + ".Decompress_After_Transfer",
            "Decompress zipped-files after transfer", "false", "false", false);

    @JSOptionDefinition(name = "ConcurrentTransfer", description = "", key = "Concurrent_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean concurrentTransfer = new SOSOptionBoolean(this, CLASSNAME + ".Concurrent_Transfer", "Process transfers simultaneously",
            "false", "false", false);

    @JSOptionDefinition(name = "CheckIntegrityHash", description = "", key = "check_integrity_hash", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkIntegrityHash = new SOSOptionBoolean(this, CLASSNAME + ".check_integrity_hash", "Calculates the integrity hash",
            "false", "false", false);
    public SOSOptionBoolean checkSecurityHash = (SOSOptionBoolean) checkIntegrityHash.setAlias("check_security_hash");

    @JSOptionDefinition(name = "MaxConcurrentTransfers", description = "", key = "Max_Concurrent_Transfers", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger maxConcurrentTransfers = new SOSOptionInteger(this, CLASSNAME + ".Max_Concurrent_Transfers",
            "Maximum Numbers of parallel transfers", "5", "1", false);

    @JSOptionDefinition(name = "CreateIntegrityHashFile", description = "", key = "create_integrity_hash_file", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createIntegrityHashFile = new SOSOptionBoolean(this, CLASSNAME + ".create_integrity_hash_file",
            "Flag if an integrity hash file will be created on the target", "false", "false", false);
    public SOSOptionBoolean createSecurityHashFile = (SOSOptionBoolean) createIntegrityHashFile.setAlias("create_security_hash_file");

    @JSOptionDefinition(name = "BufferSize", description = "", key = "buffer_Size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger bufferSize = new SOSOptionInteger(this, CLASSNAME + ".buffer_Size", "This parameter specifies the interval in seconds",
            "32000", "4096", false);

    @JSOptionDefinition(name = "create_order", description = "Activate file-order creation With this parameter it is possible to specif", key = "create_order", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createOrder = new SOSOptionBoolean(this, CLASSNAME + ".create_order",
            "Activate file-order creation With this parameter it is possible to specif", "false", "false", false);

    public SOSOptionBoolean getCreateOrder() {
        return createOrder;
    }

    public void setCreateOrder(final SOSOptionBoolean pCreateOrder) {
        createOrder = pCreateOrder;
    }

    @JSOptionDefinition(name = "create_orders_for_all_files", description = "Create a file-order for every file in the result-list", key = "create_orders_for_all_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createOrdersForAllFiles = new SOSOptionBoolean(this, CLASSNAME + ".create_orders_for_all_files",
            "Create a file-order for every file in the result-list", "false", "false", false);

    public SOSOptionBoolean getCreateOrdersForAllFiles() {
        return createOrdersForAllFiles;
    }

    public void setCreateOrdersForAllFiles(final SOSOptionBoolean pCreateOrdersForAllFiles) {
        createOrdersForAllFiles = pCreateOrdersForAllFiles;
    }

    @JSOptionDefinition(name = "create_orders_for_new_files", description = "Create a file-order for each new file in the result-list", key = "create_orders_for_new_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createOrdersForNewFiles = new SOSOptionBoolean(this, CLASSNAME + ".create_orders_for_new_files",
            "Create a file-order for each new file in the result-list", "false", "false", false);

    public SOSOptionBoolean getCreateOrdersForNewFiles() {
        return createOrdersForNewFiles;
    }

    public void setCreateOrdersForNewFiles(final SOSOptionBoolean pCreateOrdersForNewFiles) {
        createOrdersForNewFiles = pCreateOrdersForNewFiles;
    }

    @JSOptionDefinition(name = "param_name_for_path", description = "Sets the name of the parameter for the transfered file", key = "param_name_for_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString paramNameForPath = new SOSOptionString(this, CLASSNAME + ".param_name_for_path",
            "Sets the name of the parameter for the transfered file", "", "scheduler_file_path", false);

    public SOSOptionString getParamNameForPath() {
        return paramNameForPath;
    }

    public void getParamNameForPath(final SOSOptionString pParamNameForPath) {
        paramNameForPath = pParamNameForPath;
    }

    @JSOptionDefinition(name = "expected_size_of_result_set", description = "number of expected hits in result-list", key = "expected_size_of_result_set", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger expectedSizeOfResultSet = new SOSOptionInteger(this, CLASSNAME + ".expected_size_of_result_set",
            "number of expected hits in result-list", "0", "0", false);

    public SOSOptionInteger getExpectedSizeOfResultSet() {
        return expectedSizeOfResultSet;
    }

    public void setExpectedSizeOfResultSet(final SOSOptionInteger pExpectedSizeOfResultSet) {
        expectedSizeOfResultSet = pExpectedSizeOfResultSet;
    }

    @JSOptionDefinition(name = "file", description = "File or Folder to watch for Checked file or directory Supports", key = "file", type = "SOSOptionString", mandatory = true)
    public SOSOptionFileName file = new SOSOptionFileName(this, CLASSNAME + ".file", "File or Folder to watch for Checked file or directory Supports",
            ".", ".", true);

    @JSOptionDefinition(name = "target", description = "target or Folder to watch for Checked target or directory Supports", key = "target", type = "SOSOptionString", mandatory = true)
    public SOSOptionFileName target = new SOSOptionFileName(this, CLASSNAME + ".target",
            "target or Folder to watch for Checked target or directory Supports", ".", ".", true);

    public SOSOptionFileName getFile() {
        return file;
    }

    public void setFile(final SOSOptionFileName pFile) {
        file = pFile;
    }

    public SOSOptionFileName FileName = (SOSOptionFileName) file.setAlias(CLASSNAME + ".FileName");
    @JSOptionDefinition(name = "max_file_age", description = "maximum age of a file Specifies the maximum age of a file. If a file", key = "max_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime maxFileAge = new SOSOptionTime(this, CLASSNAME + ".max_file_age",
            "maximum age of a file Specifies the maximum age of a file. If a file", "0", "0", false);
    public SOSOptionTime fileAgeMaximum = (SOSOptionTime) maxFileAge.setAlias(CLASSNAME + ".FileAgeMaximum");

    public SOSOptionTime getMaxFileAge() {
        return maxFileAge;
    }

    public void setMaxFileAge(final SOSOptionTime pMaxFileAge) {
        maxFileAge = pMaxFileAge;
    }

    @JSOptionDefinition(name = "max_file_size", description = "maximum size of a file Specifies the maximum size of a file in", key = "max_file_size", type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize maxFileSize = new SOSOptionFileSize(this, CLASSNAME + ".max_file_size",
            "maximum size of a file Specifies the maximum size of a file in", "-1", "-1", false);
    public SOSOptionFileSize fileSizeMaximum = (SOSOptionFileSize) maxFileSize.setAlias(CLASSNAME + ".FileSizeMaximum");

    public SOSOptionFileSize getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(final SOSOptionFileSize pMaxFileSize) {
        maxFileSize = pMaxFileSize;
    }

    @JSOptionDefinition(name = "min_file_age", description = "minimum age of a file Specifies the minimum age of a files. If the fi", key = "min_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime minFileAge = new SOSOptionTime(this, CLASSNAME + ".min_file_age",
            "minimum age of a file Specifies the minimum age of a files. If the fi", "0", "0", false);
    public SOSOptionTime fileAgeMinimum = (SOSOptionTime) minFileAge.setAlias(CLASSNAME + ".FileAgeMinimum");

    public SOSOptionTime getMinFileAge() {
        return minFileAge;
    }

    public void setMinFileAge(final SOSOptionTime pMinFileAge) {
        minFileAge = pMinFileAge;
    }

    @JSOptionDefinition(name = "min_file_size", description = "minimum size of one or multiple files Specifies the minimum size of one", key = "min_file_size", type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize minFileSize = new SOSOptionFileSize(this, CLASSNAME + ".min_file_size",
            "minimum size of one or multiple files Specifies the minimum size of one", "-1", "-1", false);
    public SOSOptionFileSize fileSizeMinimum = (SOSOptionFileSize) minFileSize.setAlias(CLASSNAME + ".FileSizeMinimum");

    public SOSOptionFileSize getMinFileSize() {
        return minFileSize;
    }

    public void setMinFileSize(final SOSOptionFileSize pMinFileSize) {
        minFileSize = pMinFileSize;
    }

    @JSOptionDefinition(name = "MergeOrderParameter", description = "Merge created order parameter with parameter of current order", key = "MergeOrderParameter", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mergeOrderParameter = new SOSOptionBoolean(this, CLASSNAME + ".MergeOrderParameter",
            "Merge created order parameter with parameter of current order", "false", "false", false);

    public String getMergeOrderParameter() {
        return mergeOrderParameter.getValue();
    }

    public SOSFtpOptionsSuperClass setMergeOrderParameter(final String pstrValue) {
        mergeOrderParameter.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "next_state", description = "The first node to execute in a jobchain The name of the node of a jobchai", key = "next_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode nextState = new SOSOptionJobChainNode(this, CLASSNAME + ".next_state",
            "The first node to execute in a jobchain The name of the node of a jobchai", "", "", false);

    public SOSOptionJobChainNode getNextState() {
        return nextState;
    }

    public void setNextState(final SOSOptionJobChainNode pNextState) {
        nextState = pNextState;
    }

    @JSOptionDefinition(name = "on_empty_result_set", description = "Set next node on empty result set The next Node (Step, Job) to execute i", key = "on_empty_result_set", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode onEmptyResultSet = new SOSOptionJobChainNode(this, CLASSNAME + ".on_empty_result_set",
            "Set next node on empty result set The next Node (Step, Job) to execute i", "", "", false);

    public SOSOptionJobChainNode getOnEmptyResultSet() {
        return onEmptyResultSet;
    }

    public void setOnEmptyResultSet(final SOSOptionJobChainNode pOnEmptyResultSet) {
        onEmptyResultSet = pOnEmptyResultSet;
    }

    @JSOptionDefinition(name = "order_jobscheduler_host", description = "Name of Jobscheduler Host where the order have to be started", key = "order_jobscheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName orderJobschedulerHost = new SOSOptionHostName(this, CLASSNAME + ".order_jobscheduler_host",
            "Name of Jobscheduler Host where the order have to be started", "", "", false);

    public SOSOptionHostName getOrderJobschedulerHost() {
        return orderJobschedulerHost;
    }

    public void setOrderJobschedulerHost(final SOSOptionHostName hostName) {
        orderJobschedulerHost = hostName;
    }

    @JSOptionDefinition(name = "order_jobscheduler_port", description = "The port of the JobScheduler node", key = "order_jobscheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber orderJobschedulerPort = new SOSOptionPortNumber(this, CLASSNAME + ".order_jobscheduler_port",
            "The port of the JobScheduler node", "", "4444", false);

    public SOSOptionPortNumber getOrderJobschedulerPort() {
        return orderJobschedulerPort;
    }

    public void setOrderJobschedulerPort(final SOSOptionPortNumber portNumber) {
        orderJobschedulerPort = portNumber;
    }

    @JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch", key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString orderJobchainName = new SOSOptionString(this, CLASSNAME + ".order_jobchain_name",
            "The name of the jobchain which belongs to the order The name of the jobch", "", "", false);

    public SOSOptionString getOrderJobchainName() {
        return orderJobchainName;
    }

    public void setOrderJobchainName(final SOSOptionString pOrderJobchainName) {
        orderJobchainName = pOrderJobchainName;
    }

    @JSOptionDefinition(name = "raise_error_if_result_set_is", description = "raise error on expected size of result-set With this parameter it is poss", key = "raise_error_if_result_set_is", type = "SOSOptionRelOp", mandatory = false)
    public SOSOptionRelOp raiseErrorIfResultSetIs = new SOSOptionRelOp(this, CLASSNAME + ".raise_error_if_result_set_is",
            "raise error on expected size of result-set With this parameter it is poss", "", "", false);

    public SOSOptionRelOp getRaiseErrorIfResultSetIs() {
        return raiseErrorIfResultSetIs;
    }

    public void setRaiseErrorIfResultSetIs(final SOSOptionRelOp pRaiseErrorIfResultSetIs) {
        raiseErrorIfResultSetIs = pRaiseErrorIfResultSetIs;
    }

    @JSOptionDefinition(name = "result_list_file", description = "Name of the result-list file If the value of this parameter specifies a v", key = "result_list_file", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName resultListFile = new SOSOptionFileName(this, CLASSNAME + ".result_list_file",
            "Name of the result-list file If the value of this parameter specifies a v", "", "", false);

    public SOSOptionFileName getResultListFile() {
        return resultListFile;
    }

    public void setResultListFile(final SOSOptionFileName pResultListFile) {
        resultListFile = pResultListFile;
    }

    @JSOptionDefinition(name = "scheduler_file_name", description = "Name of the file to process for a file-order", key = "scheduler_file_name", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName schedulerFileName = new SOSOptionFileName(this, CLASSNAME + ".scheduler_file_name",
            "Name of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getSchedulerFileName() {
        return schedulerFileName;
    }

    public void setSchedulerFileName(final SOSOptionFileName pSchedulerFileName) {
        schedulerFileName = pSchedulerFileName;
    }

    @JSOptionDefinition(name = "scheduler_file_parent", description = "pathanme of the file to process for a file-order", key = "scheduler_file_parent", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName schedulerFileParent = new SOSOptionFileName(this, CLASSNAME + ".scheduler_file_parent",
            "pathanme of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getSchedulerFileParent() {
        return schedulerFileParent;
    }

    public void setSchedulerFileParent(final SOSOptionFileName pSchedulerFileParent) {
        schedulerFileParent = pSchedulerFileParent;
    }

    @JSOptionDefinition(name = "scheduler_file_path", description = "file to process for a file-order Using Directory Monitoring with", key = "scheduler_file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName schedulerFilePath = new SOSOptionFileName(this, CLASSNAME + ".scheduler_file_path",
            "file to process for a file-order Using Directory Monitoring with", "", "", false);

    public SOSOptionFileName getSchedulerFilePath() {
        return schedulerFilePath;
    }

    public void setSchedulerFilePath(final SOSOptionFileName pSchedulerFilePath) {
        schedulerFilePath = pSchedulerFilePath;
    }

    @JSOptionDefinition(name = "scheduler_sosfileoperations_resultsetsize", description = "The amount of hits in the result set of the operation", key = "scheduler_sosfileoperations_resultsetsize", type = "SOSOptionsInteger", mandatory = false)
    public SOSOptionInteger schedulerSosFileOperationsResultsetSize = new SOSOptionInteger(this, CLASSNAME
            + ".scheduler_sosfileoperations_resultsetsize", "The amount of hits in the result set of the operation", "", "", false);
    public SOSOptionInteger resultSetSize = (SOSOptionInteger) schedulerSosFileOperationsResultsetSize.setAlias(CLASSNAME + ".ResultSetSize");

    public SOSOptionInteger getSchedulerSosFileOperationsResultsetSize() {
        return schedulerSosFileOperationsResultsetSize;
    }

    public void setSchedulerSosFileOperationsResultsetSize(final SOSOptionInteger pSchedulerSosFileOperationsResultsetSize) {
        schedulerSosFileOperationsResultsetSize = pSchedulerSosFileOperationsResultsetSize;
    }

    @JSOptionDefinition(name = "skip_first_files", description = "number of files to remove from the top of the result-set The numbe", key = "skip_first_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skipFirstFiles = new SOSOptionInteger(this, CLASSNAME + ".skip_first_files",
            "number of files to remove from the top of the result-set The numbe", "0", "0", false);
    public SOSOptionInteger noOfFirstFiles2Skip = (SOSOptionInteger) skipFirstFiles.setAlias(CLASSNAME + ".NoOfFirstFiles2Skip");

    public SOSOptionInteger getSkipFirstFiles() {
        return skipFirstFiles;
    }

    public void setSkipFirstFiles(final SOSOptionInteger pSkipFirstFiles) {
        skipFirstFiles = pSkipFirstFiles;
    }

    @JSOptionDefinition(name = "skip_last_files", description = "number of files to remove from the bottom of the result-set The numbe", key = "skip_last_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skipLastFiles = new SOSOptionInteger(this, CLASSNAME + ".skip_last_files",
            "number of files to remove from the bottom of the result-set The numbe", "0", "0", false);
    public SOSOptionInteger noOfLastFiles2Skip = (SOSOptionInteger) skipLastFiles.setAlias(CLASSNAME + ".NoOfLastFiles2Skip");

    public SOSOptionInteger getSkipLastFiles() {
        return skipLastFiles;
    }

    public void setSkipLastFiles(final SOSOptionInteger pSkipLastFiles) {
        skipLastFiles = pSkipLastFiles;
    }

    @JSOptionDefinition(name = "Max_Files", description = "Maximum number of files to process", key = "Max_Files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger maxFiles = new SOSOptionInteger(this, CLASSNAME + ".Max_Files", "Maximum number of files to process", "-1", "-1", false);

    public String getMaxFiles() {
        return maxFiles.getValue();
    }

    public SOSFtpOptionsSuperClass setMaxFiles(final String pstrValue) {
        maxFiles.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "check_steady_count", description = "Number of tries for Steady check", key = "check_steady_count", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger checkSteadyCount = new SOSOptionInteger(this, CLASSNAME + ".check_steady_count", "Number of tries for Steady check", "10",
            "10", false);

    public String getCheckSteadyCount() {
        return checkSteadyCount.getValue();
    }

    public SOSFtpOptionsSuperClass setCheckSteadyCount(final String pstrValue) {
        checkSteadyCount.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "check_steady_state_interval", description = "The intervall for steady state checking", key = "check_steady_state_interval", type = "SOSOptionFileTime", mandatory = false)
    public SOSOptionTime checkSteadyStateInterval = new SOSOptionTime(this, CLASSNAME + ".check_steady_state_interval",
            "The intervall for steady state checking", "1", "1", false);

    public String getCheckSteadyStateInterval() {
        return checkSteadyStateInterval.getValue();
    }

    public SOSFtpOptionsSuperClass setCheckSteadyStateInterval(final String pstrValue) {
        checkSteadyStateInterval.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Check_Steady_State_Of_Files", description = "Check wether a file is beeing modified", key = "Check_Steady_State_Of_Files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkSteadyStateOfFiles = new SOSOptionBoolean(this, CLASSNAME + ".Check_Steady_State_Of_Files",
            "Check wether a file is beeing modified", "false", "false", false);

    public String getCheckSteadyStateOfFiles() {
        return checkSteadyStateOfFiles.getValue();
    }

    public SOSFtpOptionsSuperClass setCheckSteadyStateOfFiles(final String pstrValue) {
        checkSteadyStateOfFiles.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PollErrorState", description = "Next state in Chain if no files found", key = "Poll_Error_State", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionJobChainNode pollErrorState = new SOSOptionJobChainNode(this, CLASSNAME + ".Poll_Error_State",
            "Next state in Chain if no files found", "", "", false);
    public SOSOptionJobChainNode noFilesState = (SOSOptionJobChainNode) pollErrorState.setAlias("No_files_state");

    public String getPollErrorState() {
        return pollErrorState.getValue();
    }

    public SOSFtpOptionsSuperClass setPollErrorState(final String pstrValue) {
        pollErrorState.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Steady_state_error_state", description = "Next state in JobChain if check steady state did not comes to an normal end", key = "Steady_state_error_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode steadyStateErrorState = new SOSOptionJobChainNode(this, CLASSNAME + ".Steady_state_error_state",
            "Next state in JobChain if check steady state did not comes to an normal end", "", "", false);

    public String getSteadyStateErrorState() {
        return steadyStateErrorState.getValue();
    }

    public SOSFtpOptionsSuperClass setSteadyStateErrorState(final String pstrValue) {
        steadyStateErrorState.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean makeDirs = new SOSOptionBoolean(this, CLASSNAME + ".make_Dirs", "Create missing Directory on Target", "true", "true",
            false);
    public SOSOptionBoolean createFoldersOnTarget = (SOSOptionBoolean) makeDirs.setAlias("create_folders_on_target");

    public String getMakeDirs() {
        return makeDirs.getValue();
    }

    public SOSFtpOptionsSuperClass setMakeDirs(final String pstrValue) {
        makeDirs.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "File_List_Name", description = "File with a list of file names", key = "File_List_Name", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName fileListName = new SOSOptionInFileName(this, CLASSNAME + ".File_List_Name", "File with a list of file names", "", "",
            false);

    public String getFileListName() {
        return fileListName.getValue();
    }

    public SOSFtpOptionsSuperClass setFileListName(final String pstrValue) {
        fileListName.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "Create_Result_Set", description = "Write the ResultSet to a file", key = "Create_Result_Set", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createResultSet = new SOSOptionBoolean(this, CLASSNAME + ".Create_Result_Set", "Write the ResultSet to a file", "false",
            "false", false);
    public SOSOptionBoolean createResultList = (SOSOptionBoolean) createResultSet.setAlias("create_result_list");

    public String getCreateResultSet() {
        return createResultSet.getValue();
    }

    public SOSFtpOptionsSuperClass setCreateResultSet(final String pstrValue) {
        createResultSet.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "ResultSetFileName", description = "Name of a File with a filelist or a resultlist", key = "Result_Set_FileName", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionOutFileName resultSetFileName = new SOSOptionOutFileName(this, CLASSNAME + ".Result_Set_File_Name",
            "Name of a File with a filelist or a resultlist", "", "", false);

    public String getResultSetFileName() {
        return resultSetFileName.getValue();
    }

    public SOSFtpOptionsSuperClass setResultSetFileName(final String pstrValue) {
        resultSetFileName.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "source_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionFolderName sourceDir = new SOSOptionFolderName(this, CLASSNAME + ".source_dir",
            "local_dir Local directory into which or from which", "", "", false);

    @JSOptionDefinition(name = "target_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionFolderName targetDir = new SOSOptionFolderName(this, CLASSNAME + ".target_dir", "target_dir directory into which or from which",
            "", "", false);

    @JSOptionDefinition(name = "account", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionString account = new SOSOptionString(this, CLASSNAME + ".account", "Optional account info for authentication with an", "", "",
            false);

    @Override
    public SOSOptionString getAccount() {
        return account;
    }

    @Override
    public void setAccount(final SOSOptionString p_account) {
        account = p_account;
    }

    @JSOptionDefinition(name = "alternative_account", description = "Alternative parameter for the primary parameter", key = "alternative_account", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternativeAccount = new SOSOptionString(this, CLASSNAME + ".alternative_account",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getAlternativeAccount() {
        return alternativeAccount;
    }

    @Override
    public void setAlternativeAccount(final SOSOptionString pAlternativeAccount) {
        alternativeAccount = pAlternativeAccount;
    }

    @JSOptionDefinition(name = "alternative_host", description = "Alternative parameter for the primary parameter", key = "alternative_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName alternativeHost = new SOSOptionHostName(this, CLASSNAME + ".alternative_host",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionHostName getAlternativeHost() {
        return alternativeHost;
    }

    @Override
    public void setAlternativeHost(final SOSOptionHostName pAlternativeHost) {
        alternativeHost = pAlternativeHost;
    }

    @JSOptionDefinition(name = "alternative_passive_mode", description = "Alternative parameter for the primary parameter", key = "alternative_passive_mode", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternativePassiveMode = new SOSOptionString(this, CLASSNAME + ".alternative_passive_mode",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getAlternativePassiveMode() {
        return alternativePassiveMode;
    }

    @Override
    public void setAlternativePassiveMode(final SOSOptionString pAlternativePassiveMode) {
        alternativePassiveMode = pAlternativePassiveMode;
    }

    @JSOptionDefinition(name = "alternative_password", description = "Alternative parameter for the primary parameter", key = "alternative_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword alternativePassword = new SOSOptionPassword(this, CLASSNAME + ".alternative_password",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionPassword getAlternativePassword() {
        return alternativePassword;
    }

    @Override
    public void setAlternativePassword(final SOSOptionPassword pAlternativePassword) {
        alternativePassword = pAlternativePassword;
    }

    @JSOptionDefinition(name = "alternative_port", description = "Alternative parameter for the primary parameter", key = "alternative_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber alternativePort = new SOSOptionPortNumber(this, CLASSNAME + ".alternative_port",
            "Alternative parameter for the primary parameter", "21", "21", false);

    @Override
    public SOSOptionPortNumber getAlternativePort() {
        return alternativePort;
    }

    @Override
    public void setAlternativePort(final SOSOptionPortNumber pAlternativePort) {
        alternativePort = pAlternativePort;
    }

    public SOSOptionPortNumber AlternativePortNumber = (SOSOptionPortNumber) alternativePort.setAlias(CLASSNAME + ".AlternativePortNumber");

    @JSOptionDefinition(name = "alternative_remote_dir", description = "Alternative parameter for the primary parameter", key = "alternative_remote_dir", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternativeRemoteDir = new SOSOptionString(this, CLASSNAME + ".alternative_remote_dir",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getAlternativeRemoteDir() {
        return alternativeRemoteDir;
    }

    @Override
    public void setAlternativeRemoteDir(final SOSOptionString pAlternativeRemoteDir) {
        alternativeRemoteDir = pAlternativeRemoteDir;
    }

    @JSOptionDefinition(name = "alternative_transfer_mode", description = "Alternative parameter for the primary parameter", key = "alternative_transfer_mode", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternativeTransferMode = new SOSOptionString(this, CLASSNAME + ".alternative_transfer_mode",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getAlternativeTransferMode() {
        return alternativeTransferMode;
    }

    @Override
    public void setAlternativeTransferMode(final SOSOptionString pAlternativeTransferMode) {
        alternativeTransferMode = pAlternativeTransferMode;
    }

    @JSOptionDefinition(name = "alternative_user", description = "Alternative parameter for the primary parameter", key = "alternative_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName alternativeUser = new SOSOptionUserName(this, CLASSNAME + ".alternative_user",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionUserName getAlternativeUser() {
        return alternativeUser;
    }

    @Override
    public void setAlternativeUser(final SOSOptionUserName pAlternativeUser) {
        alternativeUser = pAlternativeUser;
    }

    @JSOptionDefinition(name = "append_files", description = "This parameter specifies whether the content of a", key = "append_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean appendFiles = new SOSOptionBoolean(this, CLASSNAME + ".append_files", "This parameter specifies whether the content of a",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getAppendFiles() {
        return appendFiles;
    }

    @Override
    public void setAppendFiles(final SOSOptionBoolean pAppendFiles) {
        appendFiles = pAppendFiles;
    }

    @JSOptionDefinition(name = "atomic_prefix", description = "This parameter specifies whether target files shou", key = "atomic_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomicPrefix = new SOSOptionString(this, CLASSNAME + ".atomic_prefix",
            "This parameter specifies whether target files shou", "", "", false);

    @Override
    public SOSOptionString getAtomicPrefix() {
        return atomicPrefix;
    }

    @Override
    public void setAtomicPrefix(final SOSOptionString pAtomicPrefix) {
        atomicPrefix = pAtomicPrefix;
    }

    @JSOptionDefinition(name = "atomic_suffix", description = "This parameter specifies whether target files shou", key = "atomic_suffix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomicSuffix = new SOSOptionString(this, CLASSNAME + ".atomic_suffix",
            "This parameter specifies whether target files shou", "", "", false);

    @Override
    public SOSOptionString getAtomicSuffix() {
        return atomicSuffix;
    }

    @Override
    public void setAtomicSuffix(final SOSOptionString pAtomicSuffix) {
        atomicSuffix = pAtomicSuffix;
    }

    @JSOptionDefinition(name = "banner_footer", description = "Name der Template-Datei für Protokoll-Ende This p", key = "banner_footer", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName bannerFooter = new SOSOptionInFileName(this, CLASSNAME + ".banner_footer",
            "Name der Template-Datei für Protokoll-Ende This p", "", "", false);

    @Override
    public SOSOptionInFileName getBannerFooter() {
        return bannerFooter;
    }

    @Override
    public void setBannerFooter(final SOSOptionInFileName pBannerFooter) {
        bannerFooter = pBannerFooter;
    }

    @JSOptionDefinition(name = "banner_header", description = "Name of Template-File for log-File", key = "banner_header", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName bannerHeader = new SOSOptionInFileName(this, CLASSNAME + ".banner_header", "Name of Template-File for log-File", "",
            "", false);

    @Override
    public SOSOptionInFileName getBannerHeader() {
        return bannerHeader;
    }

    @Override
    public void setBannerHeader(final SOSOptionInFileName pBannerHeader) {
        bannerHeader = pBannerHeader;
    }

    @JSOptionDefinition(name = "check_interval", description = "This parameter specifies the interval in seconds", key = "check_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger checkInterval = new SOSOptionInteger(this, CLASSNAME + ".check_interval",
            "This parameter specifies the interval in seconds", "60", "60", false);

    @Override
    public SOSOptionInteger getCheckInterval() {
        return checkInterval;
    }

    @Override
    public void setCheckInterval(final SOSOptionInteger pCheckInterval) {
        checkInterval = pCheckInterval;
    }

    @JSOptionDefinition(name = "check_retry", description = "This parameter specifies whether a file transfer", key = "check_retry", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger checkRetry = new SOSOptionInteger(this, CLASSNAME + ".check_retry", "This parameter specifies whether a file transfer",
            "0", "0", false);

    @Override
    public SOSOptionInteger getCheckRetry() {
        return checkRetry;
    }

    @Override
    public void setCheckRetry(final SOSOptionInteger pCheckRetry) {
        checkRetry = pCheckRetry;
    }

    @JSOptionDefinition(name = "check_size", description = "This parameter determines whether the original f", key = "check_size", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean checkSize = new SOSOptionBoolean(this, CLASSNAME + ".check_size", "This parameter determines whether the original f",
            "true", "true", false);
    public SOSOptionBoolean checkFileSizeAfterTransfer = (SOSOptionBoolean) checkSize.setAlias(CLASSNAME + ".CheckFileSizeAfterTransfer");

    @JSOptionDefinition(name = "classpath_base", description = "The parameter is used during installation of this", key = "classpath_base", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName classpathBase = new SOSOptionFolderName(this, CLASSNAME + ".classpath_base",
            "The parameter is used during installation of this", "", "", false);

    @Override
    public SOSOptionFolderName getClasspathBase() {
        return classpathBase;
    }

    @Override
    public void setClasspathBase(final SOSOptionFolderName pClasspathBase) {
        classpathBase = pClasspathBase;
    }

    @JSOptionDefinition(name = "compress_files", description = "This parameter specifies whether the content of the source files", key = "compress_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean compressFiles = new SOSOptionBoolean(this, CLASSNAME + ".compress_files",
            "This parameter specifies whether the content of the source files", "false", "false", false);

    @JSOptionDefinition(name = "compressed_file_extension", description = "Additional file-name extension for compressed files This parameter spe", key = "compressed_file_extension", type = "SOSOptionString", mandatory = false)
    public SOSOptionString compressedFileExtension = new SOSOptionString(this, CLASSNAME + ".compressed_file_extension",
            "Additional file-name extension for compressed files This parameter spe", ".gz", ".gz", false);

    @JSOptionDefinition(name = "current_pid", description = "This parameter is used for Unix systems and - as o", key = "current_pid", type = "SOSOptionProcessID", mandatory = false)
    public SOSOptionProcessID currentPid = new SOSOptionProcessID(this, CLASSNAME + ".current_pid",
            "This parameter is used for Unix systems and - as o", "", "", false);

    @Override
    public SOSOptionProcessID getCurrentPid() {
        return currentPid;
    }

    @Override
    public void setCurrentPid(final SOSOptionProcessID pCurrentPid) {
        currentPid = pCurrentPid;
    }

    @JSOptionDefinition(name = "file_path", description = "This parameter is used alternatively to the parame", key = "file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName filePath = new SOSOptionFileName(this, CLASSNAME + ".file_path", "This parameter is used alternatively to the parame",
            "", "", false);

    @Override
    public SOSOptionFileName getFilePath() {
        return filePath;
    }

    @Override
    public void setFilePath(final SOSOptionFileName pFilePath) {
        filePath = pFilePath;
    }

    @JSOptionDefinition(name = "file_spec", description = "file_spec This parameter expects a regular expressi", key = "file_spec", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp fileSpec = new SOSOptionRegExp(this, CLASSNAME + ".file_spec", "file_spec This parameter expects a regular expressi",
            "^.*$", "^.*$", false);
    public SOSOptionRegExp fileNameRegExp = (SOSOptionRegExp) fileSpec.setAlias(CLASSNAME + ".FileNameRegExp");
    public SOSOptionRegExp fileNamePatternRegExp = (SOSOptionRegExp) fileSpec.setAlias(CLASSNAME + ".FileNamePatternRegExp");

    @Override
    public SOSOptionRegExp getFileSpec() {
        return fileSpec;
    }

    @Override
    public void setFileSpec(final SOSOptionRegExp pFileSpec) {
        fileSpec = pFileSpec;
    }

    @JSOptionDefinition(name = "force_files", description = "This parameter specifies whether an error should b", key = "force_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean forceFiles = new SOSOptionBoolean(this, CLASSNAME + ".force_files", "This parameter specifies whether an error should b",
            "true", "true", false);
    public SOSOptionBoolean errorOnNoDataFound = (SOSOptionBoolean) forceFiles.setAlias("error_on_no_data_found", "error_when_no_data_found");

    @Override
    public SOSOptionBoolean getForceFiles() {
        return forceFiles;
    }

    @Override
    public void setForceFiles(final SOSOptionBoolean pForceFiles) {
        forceFiles = pForceFiles;
    }

    @JSOptionDefinition(name = "history", description = "This parameter causes a history file to be written", key = "history", type = "SOSOptionOutFileName", mandatory = false)
    public SOSOptionOutFileName history = new SOSOptionOutFileName(this, CLASSNAME + ".history", "This parameter causes a history file to be written",
            "", "", false);
    public SOSOptionOutFileName historyFileName = (SOSOptionOutFileName) history.setAlias("history_file_name");
    public SOSOptionOutFileName sosFtpHistoryFileName = (SOSOptionOutFileName) history.setAlias(CLASSNAME + ".SOSFtpHistoryFileName");

    @Override
    public SOSOptionOutFileName getHistory() {
        return history;
    }

    @Override
    public void setHistory(final SOSOptionOutFileName pHistory) {
        history = pHistory;
    }

    @JSOptionDefinition(name = "history_repeat", description = "The parameter is used in order to synchronize para", key = "history_repeat", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger historyRepeat = new SOSOptionInteger(this, CLASSNAME + ".history_repeat",
            "The parameter is used in order to synchronize para", "3", "3", false);

    @Override
    public SOSOptionInteger getHistoryRepeat() {
        return historyRepeat;
    }

    @Override
    public void setHistoryRepeat(final SOSOptionInteger pHistoryRepeat) {
        historyRepeat = pHistoryRepeat;
    }

    @JSOptionDefinition(name = "history_repeat_interval", description = "The parameter is used in order to synchronize para", key = "history_repeat_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger historyRepeatInterval = new SOSOptionInteger(this, CLASSNAME + ".history_repeat_interval",
            "The parameter is used in order to synchronize para", "1", "1", false);

    @Override
    public SOSOptionInteger getHistoryRepeatInterval() {
        return historyRepeatInterval;
    }

    @Override
    public void setHistoryRepeatInterval(final SOSOptionInteger pHistoryRepeatInterval) {
        historyRepeatInterval = pHistoryRepeatInterval;
    }

    @JSOptionDefinition(name = "host", description = "Host-Name This parameter specifies th", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, CLASSNAME + ".host", "Host-Name This parameter specifies th", "", "", false);
    public SOSOptionHostName hostName = (SOSOptionHostName) host.setAlias(CLASSNAME + ".HostName");

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public void setHost(final SOSOptionHostName pHost) {
        host = pHost;
    }

    @JSOptionDefinition(name = "http_proxy_host", description = "The value of this parameter is the host name or th", key = "http_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString httpProxyHost = new SOSOptionString(this, CLASSNAME + ".http_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @Override
    public SOSOptionString getHttpProxyHost() {
        return httpProxyHost;
    }

    @Override
    public void setHttpProxyHost(final SOSOptionString pHttpProxyHost) {
        httpProxyHost = pHttpProxyHost;
    }

    @JSOptionDefinition(name = "http_proxy_port", description = "This parameter specifies the port of a proxy that", key = "http_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString httpProxyPort = new SOSOptionString(this, CLASSNAME + ".http_proxy_port",
            "This parameter specifies the port of a proxy that", "", "", false);

    @Override
    public SOSOptionString getHttpProxyPort() {
        return httpProxyPort;
    }

    @Override
    public void setHttpProxyPort(final SOSOptionString pHttpProxyPort) {
        httpProxyPort = pHttpProxyPort;
    }

    @JSOptionDefinition(name = "jump_command", description = "This parameter specifies a command that is to be e", key = "jump_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpCommand = new SOSOptionString(this, CLASSNAME + ".jump_command", "This parameter specifies a command that is to be e",
            "", "", false);

    @Override
    public SOSOptionString getJumpCommand() {
        return jumpCommand;
    }

    @Override
    public void setJumpCommand(final SOSOptionString pJumpCommand) {
        jumpCommand = pJumpCommand;
    }

    @JSOptionDefinition(name = "jump_command_delimiter", description = "Command delimiter for jump pre and post commands", key = "jump_command_delimiter", type = "SOSOptionString", mandatory = true)
    public SOSOptionString jumpCommandDelimiter = new SOSOptionString(this, CLASSNAME + ".jump_command_delimiter",
            "Command delimiter for jump pre and post commands", ";", ";", true);

    @Override
    public SOSOptionString getJumpCommandDelimiter() {
        return jumpCommandDelimiter;
    }

    @Override
    public void setJumpCommandDelimiter(final SOSOptionString pJumpCommandDelimiter) {
        jumpCommandDelimiter = pJumpCommandDelimiter;
    }

    @JSOptionDefinition(name = "jump_command_script", description = "This parameter can be used as an alternative to ju", key = "jump_command_script", type = "SOSOptionCommandScript", mandatory = false)
    public SOSOptionCommandScript jumpCommandScript = new SOSOptionCommandScript(this, CLASSNAME + ".jump_command_script",
            "This parameter can be used as an alternative to ju", "", "", false);

    @Override
    public SOSOptionCommandScript getJumpCommandScript() {
        return jumpCommandScript;
    }

    @Override
    public void setJumpCommandScript(final SOSOptionCommandScript pJumpCommandScript) {
        jumpCommandScript = pJumpCommandScript;
    }

    @JSOptionDefinition(name = "jump_command_script_file", description = "This parameter can be used as an alternative to ju", key = "jump_command_script_file", type = "SOSOptionCommandScriptFile", mandatory = false)
    public SOSOptionCommandScriptFile jumpCommandScriptFile = new SOSOptionCommandScriptFile(this, CLASSNAME + ".jump_command_script_file",
            "This parameter can be used as an alternative to ju", "", "", false);

    @Override
    public SOSOptionCommandScriptFile getJumpCommandScriptFile() {
        return jumpCommandScriptFile;
    }

    @Override
    public void setJumpCommandScriptFile(final SOSOptionCommandScriptFile pJumpCommandScriptFile) {
        jumpCommandScriptFile = pJumpCommandScriptFile;
    }

    @JSOptionDefinition(name = "jump_host", description = "When using a jump_host then files are first transf", key = "jump_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName jumpHost = new SOSOptionHostName(this, CLASSNAME + ".jump_host", "When using a jump_host then files are first transf",
            "", "", false);

    @Override
    public SOSOptionHostName getJumpHost() {
        return jumpHost;
    }

    @Override
    public void setJumpHost(final SOSOptionHostName pJumpHost) {
        jumpHost = pJumpHost;
    }

    @JSOptionDefinition(name = "jump_ignore_error", description = "Should the value true be specified, then execution", key = "jump_ignore_error", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jumpIgnoreError = new SOSOptionBoolean(this, CLASSNAME + ".jump_ignore_error",
            "Should the value true be specified, then execution", "false", "false", false);

    @Override
    public SOSOptionBoolean getJumpIgnoreError() {
        return jumpIgnoreError;
    }

    @Override
    public void setJumpIgnoreError(final SOSOptionBoolean pJumpIgnoreError) {
        jumpIgnoreError = pJumpIgnoreError;
    }

    @JSOptionDefinition(name = "jump_ignore_signal", description = "Should the value true be specified, t", key = "jump_ignore_signal", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jumpIgnoreSignal = new SOSOptionBoolean(this, CLASSNAME + ".jump_ignore_signal", "Should the value true be specified, t",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getJumpIgnoreSignal() {
        return jumpIgnoreSignal;
    }

    @Override
    public void setJumpIgnoreSignal(final SOSOptionBoolean pJumpIgnoreSignal) {
        jumpIgnoreSignal = pJumpIgnoreSignal;
    }

    @JSOptionDefinition(name = "jump_ignore_stderr", description = "This job checks if any output to stderr has been c", key = "jump_ignore_stderr", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jumpIgnoreStderr = new SOSOptionBoolean(this, CLASSNAME + ".jump_ignore_stderr",
            "This job checks if any output to stderr has been c", "false", "false", false);

    @Override
    public SOSOptionBoolean getJumpIgnoreStderr() {
        return jumpIgnoreStderr;
    }

    @Override
    public void setJumpIgnoreStderr(final SOSOptionBoolean pJumpIgnoreStderr) {
        jumpIgnoreStderr = pJumpIgnoreStderr;
    }

    @JSOptionDefinition(name = "jump_password", description = "Password for authentication with the jump_host.", key = "jump_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword jumpPassword = new SOSOptionPassword(this, CLASSNAME + ".jump_password",
            "Password for authentication with the jump_host.", "", "", false);

    @Override
    public SOSOptionPassword getJumpPassword() {
        return jumpPassword;
    }

    @Override
    public void setJumpPassword(final SOSOptionPassword pJumpPassword) {
        jumpPassword = pJumpPassword;
    }

    @JSOptionDefinition(name = "jump_port", description = "Port on the jump_host by which files should be tra", key = "jump_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber jumpPort = new SOSOptionPortNumber(this, CLASSNAME + ".jump_port",
            "Port on the jump_host by which files should be tra", "22", "22", false);

    @Override
    public SOSOptionPortNumber getJumpPort() {
        return jumpPort;
    }

    @Override
    public void setJumpPort(final SOSOptionPortNumber pJumpPort) {
        jumpPort = pJumpPort;
    }

    @JSOptionDefinition(name = "jump_protocol", description = "When using a jump_host then files are first transf", key = "jump_protocol", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProtocol = new SOSOptionString(this, CLASSNAME + ".jump_protocol",
            "When using a jump_host then files are first transf", "sftp", "sftp", false);

    @Override
    public SOSOptionString getJumpProtocol() {
        return jumpProtocol;
    }

    @Override
    public void setJumpProtocol(final SOSOptionString pJumpProtocol) {
        jumpProtocol = pJumpProtocol;
    }

    @JSOptionDefinition(name = "jump_proxy_host", description = "The value of this parameter is the host name or th", key = "jump_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProxyHost = new SOSOptionString(this, CLASSNAME + ".jump_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @Override
    public SOSOptionString getJumpProxyHost() {
        return jumpProxyHost;
    }

    @Override
    public void setJumpProxyHost(final SOSOptionString pJumpProxyHost) {
        jumpProxyHost = pJumpProxyHost;
    }

    @JSOptionDefinition(name = "jump_proxy_password", description = "This parameter specifies the password for the prox", key = "jump_proxy_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProxyPassword = new SOSOptionString(this, CLASSNAME + ".jump_proxy_password",
            "This parameter specifies the password for the prox", "", "", false);

    @Override
    public SOSOptionString getJumpProxyPassword() {
        return jumpProxyPassword;
    }

    @Override
    public void setJumpProxyPassword(final SOSOptionString pJumpProxyPassword) {
        jumpProxyPassword = pJumpProxyPassword;
    }

    @JSOptionDefinition(name = "jump_proxy_port", description = "This parameter specifies the port of a proxy that", key = "jump_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpProxyPort = new SOSOptionString(this, CLASSNAME + ".jump_proxy_port",
            "This parameter specifies the port of a proxy that", "", "", false);

    @Override
    public SOSOptionString getJumpProxyPort() {
        return jumpProxyPort;
    }

    @Override
    public void setJumpProxyPort(final SOSOptionString pJumpProxyPort) {
        jumpProxyPort = pJumpProxyPort;
    }

    @JSOptionDefinition(name = "jump_proxy_user", description = "The value of this parameter specifies the user acc", key = "jump_proxy_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jumpProxyUser = new SOSOptionUserName(this, CLASSNAME + ".jump_proxy_user",
            "The value of this parameter specifies the user acc", "", "", false);

    @Override
    public SOSOptionUserName getJumpProxyUser() {
        return jumpProxyUser;
    }

    @Override
    public void setJumpProxyUser(final SOSOptionUserName pJumpProxyUser) {
        jumpProxyUser = pJumpProxyUser;
    }

    @JSOptionDefinition(name = "jump_proxy_protocol", description = "Jump Proxy protocol", key = "jump_proxy_protocol", type = "SOSOptionProxyProtocol", mandatory = false)
    public SOSOptionProxyProtocol jumpProxyProtocol = new SOSOptionProxyProtocol(this, CLASSNAME + ".jump_proxy_protocol", "Jump Proxy protocol",
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getJumpProxyProtocol() {
        return jumpProxyProtocol;
    }

    public void setJumpProxyProtocol(SOSOptionProxyProtocol val) {
        jumpProxyProtocol = val;
    }

    @JSOptionDefinition(name = "jump_simulate_shell", description = "Should the value true be specified for this parame", key = "jump_simulate_shell", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jumpSimulateShell = new SOSOptionBoolean(this, CLASSNAME + ".jump_simulate_shell",
            "Should the value true be specified for this parame", "false", "false", false);

    @Override
    public SOSOptionBoolean getJumpSimulateShell() {
        return jumpSimulateShell;
    }

    @Override
    public void setJumpSimulateShell(final SOSOptionBoolean pJumpSimulateShell) {
        jumpSimulateShell = pJumpSimulateShell;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_inactivity_timeout", description = "If no new characters are written to stdout or stde", key = "jump_simulate_shell_inactivity_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jumpSimulateShellInactivityTimeout = new SOSOptionInteger(this, CLASSNAME + ".jump_simulate_shell_inactivity_timeout",
            "If no new characters are written to stdout or stde", "", "", false);

    @Override
    public SOSOptionInteger getJumpSimulateShellInactivityTimeout() {
        return jumpSimulateShellInactivityTimeout;
    }

    @Override
    public void setJumpSimulateShellInactivityTimeout(final SOSOptionInteger pJumpSimulateShellInactivityTimeout) {
        jumpSimulateShellInactivityTimeout = pJumpSimulateShellInactivityTimeout;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_login_timeout", description = "If no new characters are written to stdout or stde", key = "jump_simulate_shell_login_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jumpSimulateShellLoginTimeout = new SOSOptionInteger(this, CLASSNAME + ".jump_simulate_shell_login_timeout",
            "If no new characters are written to stdout or stde", "", "", false);

    @Override
    public SOSOptionInteger getJumpSimulateShellLoginTimeout() {
        return jumpSimulateShellLoginTimeout;
    }

    @Override
    public void setJumpSimulateShellLoginTimeout(final SOSOptionInteger pJumpSimulateShellLoginTimeout) {
        jumpSimulateShellLoginTimeout = pJumpSimulateShellLoginTimeout;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_prompt_trigger", description = "The expected command line prompt. Using this promp", key = "jump_simulate_shell_prompt_trigger", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpSimulateShellPromptTrigger = new SOSOptionString(this, CLASSNAME + ".jump_simulate_shell_prompt_trigger",
            "The expected command line prompt. Using this promp", "", "", false);

    @Override
    public SOSOptionString getJumpSimulateShellPromptTrigger() {
        return jumpSimulateShellPromptTrigger;
    }

    @Override
    public void setJumpSimulateShellPromptTrigger(final SOSOptionString pJumpSimulateShellPromptTrigger) {
        jumpSimulateShellPromptTrigger = pJumpSimulateShellPromptTrigger;
    }

    @JSOptionDefinition(name = "jump_ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "jump_ssh_auth_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName jumpSshAuthFile = new SOSOptionInFileName(this, CLASSNAME + ".jump_ssh_auth_file",
            "This parameter specifies the path and name of a us", "", "", false);

    @Override
    public SOSOptionInFileName getJumpSshAuthFile() {
        return jumpSshAuthFile;
    }

    @Override
    public void setJumpSshAuthFile(final SOSOptionInFileName pJumpSshAuthFile) {
        jumpSshAuthFile = pJumpSshAuthFile;
    }

    @JSOptionDefinition(name = "jump_ssh_auth_method", description = "This parameter specifies the authentication method", key = "jump_ssh_auth_method", type = "SOSOptionString", mandatory = false)
    public SOSOptionAuthenticationMethod jumpSshAuthMethod = new SOSOptionAuthenticationMethod(this, CLASSNAME + ".jump_ssh_auth_method",
            "This parameter specifies the authentication method", "", "", false);

    @Override
    public SOSOptionAuthenticationMethod getJumpSshAuthMethod() {
        return jumpSshAuthMethod;
    }

    @Override
    public void setJumpSshAuthMethod(final SOSOptionAuthenticationMethod pJumpSshAuthMethod) {
        jumpSshAuthMethod = pJumpSshAuthMethod;
    }

    @JSOptionDefinition(name = "jump_user", description = "User name for authentication with the jump_host.", key = "jump_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jumpUser = new SOSOptionUserName(this, CLASSNAME + ".jump_user", "User name for authentication with the jump_host.", "",
            "", false);

    @Override
    public SOSOptionUserName getJumpUser() {
        return jumpUser;
    }

    @Override
    public void setJumpUser(final SOSOptionUserName pJumpUser) {
        jumpUser = pJumpUser;
    }

    @JSOptionDefinition(name = "jump_dir", description = "This parameter specifies the directory on the jump host", key = "jump_dir", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpDir = new SOSOptionString(this, CLASSNAME + ".jump_dir", "This parameter specifies the directory on the jump host",
            "/tmp", "/tmp", false);

    public SOSOptionString getJumpDir() {
        return jumpDir;
    }

    public void setJumpDir(final SOSOptionString val) {
        jumpDir = val;
    }

    @JSOptionDefinition(name = "jump_strict_hostKey_checking", description = "Check the hostkey against known hosts for SSH", key = "jump_strict_hostKey_checking", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jumpStrictHostkeyChecking = new SOSOptionBoolean(this, CLASSNAME + ".jump_strict_hostkey_checking",
            "Check the hostkey against known hosts for SSH", "false", "false", false);

    public SOSOptionBoolean getJumpStrictHostKeyChecking() {
        return jumpStrictHostkeyChecking;
    }

    public void setJumpStrictHostKeyChecking(final String value) {
        jumpStrictHostkeyChecking.setValue(value);
    }

    @JSOptionDefinition(name = "jump_platform", description = "This parameter specifies the platform on the jump host", key = "jump_dir", type = "SOSOptionPlatform", mandatory = false)
    public SOSOptionPlatform jumpPlatform = new SOSOptionPlatform(this, CLASSNAME + ".jump_platform",
            "This parameter specifies the platform on the jump host", SOSOptionPlatform.enuValidPlatforms.unix.name(),
            SOSOptionPlatform.enuValidPlatforms.unix.name(), false);

    public SOSOptionPlatform getJumpPlatform() {
        return jumpPlatform;
    }

    public void setJumpPlatform(final SOSOptionPlatform val) {
        jumpPlatform = val;
    }

    @JSOptionDefinition(name = "jump_configuration_files", description = "Configuration file with JCraft settings located on the YADE client system", key = "jump_configuration_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jumpConfigurationFiles = new SOSOptionString(this, CLASSNAME + ".jump_configuration_files",
            "Configuration file with JCraft settings located on the YADE client system", "", "", false);

    public SOSOptionString getJumpConfigurationFiles() {
        return jumpConfigurationFiles;
    }

    public void setJumpConfigurationFiles(SOSOptionString jumpConfigurationFiles) {
        this.jumpConfigurationFiles = jumpConfigurationFiles;
    }

    @JSOptionDefinition(name = "jump_server_alive_interval", description = "Sets the interval to send a keep-alive message. can contains not integer value", key = "jump_server_alive_interval", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_server_alive_interval = new SOSOptionString(this, CLASSNAME + ".jump_server_alive_interval",
            "Sets the interval to send a keep-alive message", "", "", false);

    @JSOptionDefinition(name = "jump_server_alive_count_max", description = "Sets the number of keep-alive messages which may be sent without receiving any messages back from the server.", key = "jump_server_alive_count_max", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jump_server_alive_count_max = new SOSOptionInteger(this, CLASSNAME + ".jump_server_alive_count_max",
            "Sets the number of keep-alive messages which may be sent without receiving any messages back from the server.", "", "", false);

    @JSOptionDefinition(name = "jump_session_connect_timeout", description = "Sets the interval for session connect", key = "jump_session_connect_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_session_connect_timeout = new SOSOptionString(this, CLASSNAME + ".jump_session_connect_timeout",
            "Sets the interval for session connect", "", "", false);

    @JSOptionDefinition(name = "jump_channel_connect_timeout", description = "Sets the interval for channel connect", key = "jump_channel_connect_timeout", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_channel_connect_timeout = new SOSOptionString(this, CLASSNAME + ".jump_channel_connect_timeout",
            "Sets the interval for cannel connect", "", "", false);

    @JSOptionDefinition(name = "local_dir", description = "local_dir Local directory into which or from which", key = "local_dir", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName localDir = new SOSOptionFolderName(this, CLASSNAME + ".local_dir",
            "local_dir Local directory into which or from which", "", "", false);

    @Override
    public SOSOptionFolderName getLocalDir() {
        return localDir;
    }

    @Override
    public void setLocalDir(final SOSOptionFolderName pLocalDir) {
        localDir = pLocalDir;
    }

    @JSOptionDefinition(name = "mandator", description = "This parameter specifies the mandator for which a", key = "mandator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mandator = new SOSOptionString(this, CLASSNAME + ".mandator", "This parameter specifies the mandator for which a", "SOS",
            "SOS", false);

    @Override
    public SOSOptionString getMandator() {
        return mandator;
    }

    @Override
    public void setMandator(final SOSOptionString pMandator) {
        mandator = pMandator;
    }

    @JSOptionDefinition(name = "operation", description = "Operation to be executed send, receive, remove,", key = "operation", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionJadeOperation operation = new SOSOptionJadeOperation(this, CLASSNAME + ".operation",
            "Operation to be executed send, receive, remove,", "send", "send", true);

    @Override
    public SOSOptionJadeOperation getOperation() {
        return operation;
    }

    @Override
    public void setOperation(final SOSOptionJadeOperation pOperation) {
        operation = pOperation;
    }

    @JSOptionDefinition(name = "overwrite_files", description = "This parameter specifies if existing files should", key = "overwrite_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean overwriteFiles = new SOSOptionBoolean(this, CLASSNAME + ".overwrite_files",
            "This parameter specifies if existing files should", "true", "true", false);

    @Override
    public SOSOptionBoolean getOverwriteFiles() {
        return overwriteFiles;
    }

    @Override
    public void setOverwriteFiles(final SOSOptionBoolean pOverwriteFiles) {
        overwriteFiles = pOverwriteFiles;
    }

    @JSOptionDefinition(name = "jump_pre_command", description = "Command, which has to be executed on the jump host for each file before the transfer started.", key = "jump_pre_command", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPreCommand = new SOSOptionCommandString(this, CLASSNAME + ".jump_pre_command",
            "Commands, which has to be executed on the jump host for each file before the transfer started.", "", "", false);

    public SOSOptionCommandString getJumpPreCommand() {
        return jumpPreCommand;
    }

    public void setJumpPreCommand(SOSOptionCommandString jumpPreCommand) {
        this.jumpPreCommand = jumpPreCommand;
    }

    @JSOptionDefinition(name = "jump_post_command_on_success", description = "Command, which has to be executed on the jump host for each file after the transfer of the file succesfull ended.", key = "jump_post_command_on_success", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostCommandOnSuccess = new SOSOptionCommandString(this, CLASSNAME + ".jump_post_command_on_success",
            "Commands, which has to be executed on the jump host for each file after the transfer of the file succesfull ended.", "", "", false);

    public SOSOptionCommandString getJumpPostCommandOnSuccess() {
        return jumpPostCommandOnSuccess;
    }

    public void setJumpPostCommandOnSuccess(SOSOptionCommandString jumpPostCommandOnSuccess) {
        this.jumpPostCommandOnSuccess = jumpPostCommandOnSuccess;
    }

    @JSOptionDefinition(name = "jump_pre_transfer_commands", description = "Commands, which has to be executed on the jump host before the transfer started.", key = "jump_pre_transfer_commands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPreTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".jump_pre_transfer_commands",
            "Commands, which has to be executed on the jump host before the transfer started.", "", "", false);

    public SOSOptionCommandString getJumpPreTransferCommands() {
        return jumpPreTransferCommands;
    }

    public void setJumpPreTransferCommands(SOSOptionCommandString jumpPreTransferCommands) {
        this.jumpPreTransferCommands = jumpPreTransferCommands;
    }

    @JSOptionDefinition(name = "jump_post_transfer_commands_on_success", description = "Commands, which has to be executed on the jump host after the transfer ended successful.", key = "jump_post_transfer_commands_on_success", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostTransferCommandsOnSuccess = new SOSOptionCommandString(this, CLASSNAME
            + ".jump_post_transfer_commands_on_success", "Commands, which has to be executed on the jump host after the transfer ended successful.",
            "", "", false);

    public SOSOptionCommandString getJumpPostTransferCommandsOnSuccess() {
        return jumpPostTransferCommandsOnSuccess;
    }

    public void setJumpPostTransferCommandsOnSuccess(SOSOptionCommandString jumpPostTransferCommandsOnSuccess) {
        this.jumpPostTransferCommandsOnSuccess = jumpPostTransferCommandsOnSuccess;
    }

    @JSOptionDefinition(name = "jump_post_transfer_commands_on_error", description = "Commands, which has to be executed on the jump host after the transfer ended with errors.", key = "jump_post_transfer_commands_on_error", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostTransferCommandsOnError = new SOSOptionCommandString(this, CLASSNAME
            + ".jump_post_transfer_commands_on_error", "Commands, which has to be executed on the jump host after the transfer ended with errors.",
            "", "", false);

    public SOSOptionCommandString getJumpPostTransferCommandsOnError() {
        return jumpPostTransferCommandsOnError;
    }

    public void setJumpPostTransferCommandsOnError(SOSOptionCommandString jumpPostTransferCommandsOnError) {
        this.jumpPostTransferCommandsOnError = jumpPostTransferCommandsOnError;
    }

    @JSOptionDefinition(name = "jump_post_transfer_commands_final", description = "Commands, which has to be executed on the jump host after the transfer ended independet of the transfer status.", key = "jump_post_transfer_commands_final", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jumpPostTransferCommandsFinal = new SOSOptionCommandString(this, CLASSNAME + ".jump_post_transfer_commands_final",
            "Commands, which has to be executed on the jump host after the transfer ended independet of the transfer status.", "", "", false);

    public SOSOptionCommandString getJumpPostTransferCommandsFinal() {
        return jumpPostTransferCommandsFinal;
    }

    public void setJumpPostTransferCommandsFinal(SOSOptionCommandString jumpPostTransferCommandsFinal) {
        this.jumpPostTransferCommandsFinal = jumpPostTransferCommandsFinal;
    }

    @JSOptionDefinition(name = "jump_preferred_authentications", description = "This parameter specifies preferred authentication methods,e.g password,publickey,...", key = "jump_preferred_authentications", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_preferred_authentications = new SOSOptionString(this, CLASSNAME + ".jump_preferred_authentications",
            "This parameter specifies the preferred authentication methods", "", "", false);

    @JSOptionDefinition(name = "jump_required_authentications", description = "This parameter specifies the required authentication methods,e.g password,publickey,...", key = "jump_required_authentications", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_required_authentications = new SOSOptionString(this, CLASSNAME + ".jump_required_authentications",
            "This parameter specifies the required authentication methods", "", "", false);

    @JSOptionDefinition(name = "jump_passphrase", description = "This parameter specifies the passphrase", key = "jump_passphrase", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_passphrase = new SOSOptionString(this, CLASSNAME + ".jump_passphrase", "This parameter specifies the passphrase", "",
            "", false);

    @JSOptionDefinition(name = "jump_use_credential_store", description = "", key = "jump_use_credential_store", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_use_credential_store = new SOSOptionBoolean(this, CLASSNAME + ".jump_use_credential_store", "", "false", "false",
            false);

    @JSOptionDefinition(name = "jump_CredentialStore_FileName", description = "", key = "jump_CredentialStore_FileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName jump_CredentialStore_FileName = new SOSOptionInFileName(this, CLASSNAME + ".jump_CredentialStore_FileName", "", "", "",
            false);

    @JSOptionDefinition(name = "jump_CredentialStore_AuthenticationMethod", description = "", key = "jump_CredentialStore_AuthenticationMethod", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_CredentialStore_AuthenticationMethod = new SOSOptionString(this, CLASSNAME
            + ".jump_CredentialStore_AuthenticationMethod", "", "privatekey", "privatekey", false);

    @JSOptionDefinition(name = "jump_CredentialStore_KeyFileName", description = "", key = "jump_CredentialStore_KeyFileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName jump_CredentialStore_KeyFileName = new SOSOptionInFileName(this, CLASSNAME + ".jump_CredentialStore_KeyFileName", "",
            "", "", false);

    @JSOptionDefinition(name = "jump_CredentialStore_Password", description = "", key = "jump_CredentialStore_Password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword jump_CredentialStore_Password = new SOSOptionPassword(this, CLASSNAME + ".jump_CredentialStore_Password", "", "", "",
            false);

    @JSOptionDefinition(name = "jump_CredentialStore_KeyPath", description = "", key = "jump_CredentialStore_KeyPath", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_CredentialStore_KeyPath = new SOSOptionString(this, CLASSNAME + ".jump_CredentialStore_KeyPath", "", "", "", false);

    @JSOptionDefinition(name = "passive_mode", description = "passive_mode Passive mode for FTP is often used wit", key = "passive_mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean passiveMode = new SOSOptionBoolean(this, CLASSNAME + ".passive_mode",
            "passive_mode Passive mode for FTP is often used wit", "false", "false", false);

    @Override
    public SOSOptionBoolean getPassiveMode() {
        return passiveMode;
    }

    @Override
    public void setPassiveMode(final SOSOptionBoolean pPassiveMode) {
        passiveMode = pPassiveMode;
    }

    public SOSOptionBoolean FTPTransferModeIsPassive = (SOSOptionBoolean) passiveMode.setAlias(CLASSNAME + ".FTPTransferModeIsPassive");

    @JSOptionDefinition(name = "password", description = "Password for UserID Password for a", key = "password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword password = new SOSOptionPassword(this, CLASSNAME + ".password", "Password for UserID Password for a", "", "", false);

    @Override
    public SOSOptionPassword getPassword() {
        return password;
    }

    @Override
    public void setPassword(final SOSOptionPassword pPassword) {
        password = pPassword;
    }

    @JSOptionDefinition(name = "passphrase", description = "Passphrase", key = "passphrase", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword passphrase = new SOSOptionPassword(this, CLASSNAME + ".passphrase", "Passphrase", "", "", false);

    @Override
    public SOSOptionPassword getPassphrase() {
        return passphrase;
    }

    @Override
    public void setPassphrase(final SOSOptionPassword val) {
        passphrase = val;
    }

    @JSOptionDefinition(name = "poll_interval", description = "This parameter specifies the interval in seconds", key = "poll_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionTime pollInterval = new SOSOptionTime(this, CLASSNAME + ".poll_interval", "This parameter specifies the interval in seconds",
            "60", "60", false);

    @Override
    public SOSOptionTime getPollInterval() {
        return pollInterval;
    }

    @Override
    public void setPollInterval(final SOSOptionTime pPollInterval) {
        pollInterval = pPollInterval;
    }

    @JSOptionDefinition(name = "Waiting_for_Late_comers", description = "Wait an additional interval for late comers", key = "Waiting_for_Late_comers", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean waitingForLateComers = new SOSOptionBoolean(this, CLASSNAME + ".Waiting_for_Late_comers",
            "Wait an additional interval for late comers", "false", "false", false);

    public String getWaitingForLateComers() {
        return waitingForLateComers.getValue();
    }

    public SOSFtpOptionsSuperClass setWaitingForLateComers(final String pstrValue) {
        waitingForLateComers.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "poll_minfiles", description = "This parameter specifies the number of files tha", key = "poll_minfiles", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger pollMinfiles = new SOSOptionInteger(this, CLASSNAME + ".poll_minfiles",
            "This parameter specifies the number of files tha", "0", "0", false);

    @Override
    public SOSOptionInteger getPollMinfiles() {
        return pollMinfiles;
    }

    @Override
    public void setPollMinfiles(final SOSOptionInteger pPollMinfiles) {
        pollMinfiles = pPollMinfiles;
    }

    @JSOptionDefinition(name = "PollingDuration", description = "The duration of the polling period", key = "PollingDuration", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingDuration = new SOSOptionTime(this, CLASSNAME + ".PollingDuration", "The duration of the polling period", "0", "0",
            false);

    public String getPollingDuration() {
        return pollingDuration.getValue();
    }

    public SOSFtpOptionsSuperClass setPollingDuration(final String pstrValue) {
        pollingDuration.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "poll_timeout", description = "This parameter specifies the time in minutes, how", key = "poll_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger pollTimeout = new SOSOptionInteger(this, CLASSNAME + ".poll_timeout", "This parameter specifies the time in minutes, how",
            "0", "0", false);

    @Override
    public SOSOptionInteger getPollTimeout() {
        return pollTimeout;
    }

    @Override
    public void setPollTimeout(final SOSOptionInteger pPollTimeout) {
        pollTimeout = pPollTimeout;
    }

    @JSOptionDefinition(name = "port", description = "Port-Number to be used for Data-Transfer", key = "port", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber port = new SOSOptionPortNumber(this, CLASSNAME + ".port", "Port-Number to be used for Data-Transfer", "21", "21",
            true);

    @Override
    public SOSOptionPortNumber getPort() {
        return port;
    }

    @Override
    public void setPort(final SOSOptionPortNumber pPort) {
        port = pPort;
    }

    @JSOptionDefinition(name = "ppid", description = "This parameter is used for Unix systems and - as o", key = "ppid", type = "SOSOptionProcessID", mandatory = false)
    public SOSOptionProcessID ppid = new SOSOptionProcessID(this, CLASSNAME + ".ppid", "This parameter is used for Unix systems and - as o", "", "",
            false);

    @Override
    public SOSOptionProcessID getPpid() {
        return ppid;
    }

    @Override
    public void setPpid(final SOSOptionProcessID pPpid) {
        ppid = pPpid;
    }

    public SOSOptionProcessID ParentProcessID = (SOSOptionProcessID) ppid.setAlias(CLASSNAME + ".ParentProcessID");

    @JSOptionDefinition(name = "profile", description = "The Name of a Profile-Section to be executed", key = "profile", type = "SOSOptionString", mandatory = false)
    public SOSOptionString profile = new SOSOptionString(this, CLASSNAME + ".profile", "The Name of a Profile-Section to be executed", "", "", false);

    @Override
    public SOSOptionString getProfile() {
        return profile;
    }

    @Override
    public void setProfile(final SOSOptionString pProfile) {
        profile = pProfile;
    }

    public SOSOptionString sectionName = (SOSOptionString) profile.setAlias(CLASSNAME + ".SectionName");

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, CLASSNAME + ".protocol",
            "Type of requested Datatransfer The values ftp, sftp", "ftp", "ftp", true);

    @Override
    public SOSOptionTransferType getProtocol() {
        return protocol;
    }

    @Override
    public void setProtocol(final SOSOptionTransferType pProtocol) {
        protocol = pProtocol;
    }

    public SOSOptionTransferType transferProtocol = (SOSOptionTransferType) protocol.setAlias(CLASSNAME + ".TransferProtocol");

    @JSOptionDefinition(name = "recursive", description = "This parameter specifies if files from subdirector", key = "recursive", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean recursive = new SOSOptionBoolean(this, CLASSNAME + ".recursive", "This parameter specifies if files from subdirector",
            "false", "false", false);

    public SOSOptionBoolean IncludeSubdirectories = (SOSOptionBoolean) recursive.setAlias("include_sub_directories");

    @Override
    public SOSOptionBoolean getRecursive() {
        return recursive;
    }

    @Override
    public void setRecursive(final SOSOptionBoolean pRecursive) {
        recursive = pRecursive;
    }

    public SOSOptionBoolean recurseSubFolders = (SOSOptionBoolean) recursive.setAlias(CLASSNAME + ".RecurseSubFolders");

    @JSOptionDefinition(name = "remote_dir", description = "remote_dir Directory at the FTP/SFTP server from wh", key = "remote_dir", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName remoteDir = new SOSOptionFolderName(this, CLASSNAME + ".remote_dir",
            "remote_dir Directory at the FTP/SFTP server from wh", ".", ".", false);

    @Override
    public SOSOptionFolderName getRemoteDir() {
        return remoteDir;
    }

    @Override
    public void setRemoteDir(final SOSOptionFolderName pRemoteDir) {
        remoteDir = pRemoteDir;
    }

    @JSOptionDefinition(name = "remove_files", description = "This parameter specifies whether files on the FTP/", key = "remove_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean removeFiles = new SOSOptionBoolean(this, CLASSNAME + ".remove_files",
            "This parameter specifies whether files on the FTP/", "false", "false", false);

    public SOSOptionBoolean deleteFilesAfterTransfer = (SOSOptionBoolean) removeFiles.setAlias(CLASSNAME + ".DeleteFilesAfterTransfer");

    public SOSOptionBoolean deleteFilesOnSource = (SOSOptionBoolean) removeFiles.setAlias(CLASSNAME + ".DeleteFilesOnSource");

    @Override
    public SOSOptionBoolean getRemoveFiles() {
        return removeFiles;
    }

    @Override
    public void setRemoveFiles(final SOSOptionBoolean pRemoveFiles) {
        removeFiles = pRemoveFiles;
    }

    @JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement", type = "SOSOptionString", mandatory = false)
    public SOSOptionString replacement = new SOSOptionString(this, CLASSNAME + ".replacement", "String for replacement of matching character seque",
            null, null, false);

    @Override
    public SOSOptionString getReplacement() {
        return replacement;
    }

    @Override
    public void setReplacement(final SOSOptionString pReplacement) {
        replacement = pReplacement;
    }

    public SOSOptionString ReplaceWith = (SOSOptionString) replacement.setAlias(CLASSNAME + ".ReplaceWith");

    @JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replacing = new SOSOptionRegExp(this, CLASSNAME + ".replacing", "Regular expression for filename replacement with", "", "",
            false);

    @Override
    public SOSOptionRegExp getReplacing() {
        return replacing;
    }

    @Override
    public void setReplacing(final SOSOptionRegExp pReplacing) {
        replacing = pReplacing;
    }

    public SOSOptionRegExp ReplaceWhat = (SOSOptionRegExp) replacing.setAlias(CLASSNAME + ".ReplaceWhat");

    @JSOptionDefinition(name = "root", description = "The parameter specifies the directory in which thi", key = "root", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName root = new SOSOptionFolderName(this, CLASSNAME + ".root", "The parameter specifies the directory in which thi", "", "",
            false);

    @Override
    public SOSOptionFolderName getRoot() {
        return root;
    }

    @Override
    public void setRoot(final SOSOptionFolderName pRoot) {
        root = pRoot;
    }

    public SOSOptionFolderName tempFolderName = (SOSOptionFolderName) root.setAlias(CLASSNAME + ".TempFolderName");

    @JSOptionDefinition(name = "scheduler_host", description = "This parameter specifies the host name or IP addre", key = "scheduler_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName schedulerHost = new SOSOptionHostName(this, CLASSNAME + ".scheduler_host",
            "This parameter specifies the host name or IP addre", "", "", false);

    public SOSOptionHostName backgroundServiceHost = (SOSOptionHostName) schedulerHost.setAlias("Background_Service_Host");

    @Override
    public SOSOptionHostName getSchedulerHost() {
        return schedulerHost;
    }

    @Override
    public void setSchedulerHost(final SOSOptionHostName pSchedulerHost) {
        schedulerHost = pSchedulerHost;
    }

    @JSOptionDefinition(name = "scheduler_job_chain", description = "The name of a job chain for Managed File Transfer", key = "scheduler_job_chain", type = "JSJobChain", mandatory = false)
    public JSJobChain schedulerJobChain = new JSJobChain(this, CLASSNAME + ".scheduler_job_chain", "The name of a job chain for Background Service",
            "/sos/jade/jade_history", "/sos/jade/jade_history", false);

    public JSJobChain backgroundServiceJobChainName = (JSJobChain) schedulerJobChain.setAlias("BackgroundService_Job_Chain_Name");

    @Override
    public JSJobChain getSchedulerJobChain() {
        return schedulerJobChain;
    }

    @Override
    public void setSchedulerJobChain(final JSJobChain pSchedulerJobChain) {
        schedulerJobChain = pSchedulerJobChain;
    }

    @JSOptionDefinition(name = "scheduler_port", description = "The port for which a Job Scheduler for Managed File Trans", key = "scheduler_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber schedulerPort = new SOSOptionPortNumber(this, CLASSNAME + ".scheduler_port",
            "The port for which a Job Scheduler for Managed File Trans", "0", "4444", false);

    public SOSOptionPortNumber backgroundServicePort = (SOSOptionPortNumber) schedulerPort.setAlias("Background_Service_Port",
            "Background_Service_PortNumber");

    @Override
    public SOSOptionPortNumber getSchedulerPort() {
        return schedulerPort;
    }

    @Override
    public void setSchedulerPort(final SOSOptionPortNumber pSchedulerPort) {
        schedulerPort = pSchedulerPort;
    }

    @JSOptionDefinition(name = "Restart", description = "Set Restart/Resume Mode for Transfer", key = "Restart", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean restart = new SOSOptionBoolean(this, CLASSNAME + ".Restart", "Set Restart/Resume Mode for Transfer", "false", "false",
            false);

    public SOSOptionBoolean resumeTransfer = (SOSOptionBoolean) restart.setAlias(CLASSNAME + "Resume", CLASSNAME + "Resume_Transfer");

    public String getRestart() {
        return restart.getValue();
    }

    public SOSFtpOptionsSuperClass setRestart(final String pstrValue) {
        restart.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "settings", description = "Name of INI-File which contains the transfer profiles to execute", key = "settings", type = "SOSOptionIniFileName", mandatory = false)
    public SOSOptionIniFileName settings = new SOSOptionIniFileName(this, CLASSNAME + ".settings",
            "Name of INI-File which contains the transfer profiles to execute", "", "", false);

    public SOSOptionIniFileName configurationFile = (SOSOptionIniFileName) settings.setAlias("JADE_Configuration_File", "JADE_Config_File",
            "Configuration", "JADE_Configuration", "JADE_INI_FILE");

    public SOSOptionIniFileName sosIniFileName = (SOSOptionIniFileName) settings.setAlias(CLASSNAME + ".SOSIniFileName");

    @Override
    public SOSOptionIniFileName getSettings() {
        return settings;
    }

    @Override
    public void setSettings(final SOSOptionIniFileName pSettings) {
        settings = pSettings;
    }

    @JSOptionDefinition(name = "skip_transfer", description = "If this Parameter is set to true then", key = "skip_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean skipTransfer = new SOSOptionBoolean(this, CLASSNAME + ".skip_transfer", "If this Parameter is set to true then", "false",
            "false", false);

    @Override
    public SOSOptionBoolean getSkipTransfer() {
        return skipTransfer;
    }

    @Override
    public void setSkipTransfer(final SOSOptionBoolean pSkipTransfer) {
        skipTransfer = pSkipTransfer;
    }

    @JSOptionDefinition(name = "ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "ssh_auth_file", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName sshAuthFile = new SOSOptionInFileName(this, CLASSNAME + ".ssh_auth_file",
            "This parameter specifies the path and name of a us", "", "", false);

    public SOSOptionInFileName authFile = (SOSOptionInFileName) sshAuthFile.setAlias(CLASSNAME + ".auth_file");

    @Override
    public SOSOptionInFileName getSshAuthFile() {
        return sshAuthFile;
    }

    @Override
    public void setSshAuthFile(final SOSOptionInFileName pSshAuthFile) {
        sshAuthFile = pSshAuthFile;
    }

    @JSOptionDefinition(name = "ssh_auth_method", description = "This parameter specifies the authentication method", key = "ssh_auth_method", type = "SOSOptionStringValueList", mandatory = false)
    public SOSOptionAuthenticationMethod sshAuthMethod = new SOSOptionAuthenticationMethod(this, CLASSNAME + ".ssh_auth_method",
            "This parameter specifies the authentication method", "publickey", "publickey", false);

    public SOSOptionAuthenticationMethod authMethod = (SOSOptionAuthenticationMethod) sshAuthMethod.setAlias(CLASSNAME + ".auth_method");

    @Override
    public SOSOptionAuthenticationMethod getSshAuthMethod() {
        return sshAuthMethod;
    }

    @Override
    public void setSshAuthMethod(final SOSOptionAuthenticationMethod pSshAuthMethod) {
        sshAuthMethod = pSshAuthMethod;
    }

    @JSOptionDefinition(name = "ssh_proxy_host", description = "The value of this parameter is the host name or th", key = "ssh_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshProxyHost = new SOSOptionString(this, CLASSNAME + ".ssh_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @Override
    public SOSOptionString getSshProxyHost() {
        return sshProxyHost;
    }

    @Override
    public void setSshProxyHost(final SOSOptionString pSshProxyHost) {
        sshProxyHost = pSshProxyHost;
    }

    @JSOptionDefinition(name = "ssh_proxy_password", description = "This parameter specifies the password for the prox", key = "ssh_proxy_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshProxyPassword = new SOSOptionString(this, CLASSNAME + ".ssh_proxy_password",
            "This parameter specifies the password for the prox", "", "", false);

    @Override
    public SOSOptionString getSshProxyPassword() {
        return sshProxyPassword;
    }

    @Override
    public void setSshProxyPassword(final SOSOptionString pSshProxyPassword) {
        sshProxyPassword = pSshProxyPassword;
    }

    @JSOptionDefinition(name = "ssh_proxy_port", description = "This parameter specifies the port number of the pr", key = "ssh_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshProxyPort = new SOSOptionString(this, CLASSNAME + ".ssh_proxy_port",
            "This parameter specifies the port number of the pr", "", "", false);

    @Override
    public SOSOptionString getSshProxyPort() {
        return sshProxyPort;
    }

    @Override
    public void setSshProxyPort(final SOSOptionString pSshProxyPort) {
        sshProxyPort = pSshProxyPort;
    }

    @JSOptionDefinition(name = "ssh_proxy_user", description = "The value of this parameter specifies the user acc", key = "ssh_proxy_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionString sshProxyUser = new SOSOptionString(this, CLASSNAME + ".ssh_proxy_user",
            "The value of this parameter specifies the user acc", "", "", false);

    @Override
    public SOSOptionString getSshProxyUser() {
        return sshProxyUser;
    }

    @Override
    public void setSshProxyUser(final SOSOptionString pSshProxyUser) {
        sshProxyUser = pSshProxyUser;
    }

    @JSOptionDefinition(name = "transactional", description = "This parameter specifies if file transfers should", key = "transactional", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean transactional = new SOSOptionBoolean(this, CLASSNAME + ".transactional",
            "This parameter specifies if file transfers should", "false", "false", false);

    @Override
    public SOSOptionBoolean getTransactional() {
        return transactional;
    }

    @Override
    public void setTransactional(final SOSOptionBoolean pTransactional) {
        transactional = pTransactional;
    }

    public SOSOptionBoolean transactionMode = (SOSOptionBoolean) transactional.setAlias(CLASSNAME + ".TransactionMode");

    @JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode", mandatory = false)
    public SOSOptionTransferMode transferMode = new SOSOptionTransferMode(this, CLASSNAME + ".transfer_mode", "Type of Character-Encoding Transfe",
            "binary", "binary", false);

    @Override
    public SOSOptionTransferMode getTransferMode() {
        return transferMode;
    }

    @Override
    public void setTransferMode(final SOSOptionTransferMode pTransferMode) {
        transferMode = pTransferMode;
    }

    @JSOptionDefinition(name = "user", description = "UserID of user in charge User name", key = "user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName user = new SOSOptionUserName(this, CLASSNAME + ".user", "UserID of user in charge User name", "", "anonymous", false);

    @Override
    public SOSOptionUserName getUser() {
        return user;
    }

    @Override
    public void setUser(final SOSOptionUserName pUser) {
        user = pUser;
    }

    @JSOptionDefinition(name = "verbose", description = "The granuality of (Debug-)Messages The verbosit", key = "verbose", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger verbose = new SOSOptionInteger(this, CLASSNAME + ".verbose", "The granuality of (Debug-)Messages The verbosit", "1", "10",
            false);

    @Override
    public SOSOptionInteger getVerbose() {
        return verbose;
    }

    @Override
    public void setVerbose(final SOSOptionInteger pVerbose) {
        verbose = pVerbose;
    }

    public SOSOptionInteger verbosityLevel = (SOSOptionInteger) verbose.setAlias(CLASSNAME + ".VerbosityLevel");

    @JSOptionDefinition(name = "zero_byte_transfer", description = "This parameter specifies whether zero byte files", key = "zero_byte_transfer", type = "SOSOptionZeroByteTransfer", mandatory = false)
    public SOSOptionZeroByteTransfer zeroByteTransfer = new SOSOptionZeroByteTransfer(this, CLASSNAME + ".zero_byte_transfer",
            "This parameter specifies whether zero byte files", "yes", "yes", false);
    public SOSOptionZeroByteTransfer transferZeroByteFiles = (SOSOptionZeroByteTransfer) zeroByteTransfer.setAlias("transfer_zero_byte_files");

    public void setAllOptions(final Properties pobjProperties) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        HashMap<String, String> map = new HashMap<String, String>((Map) pobjProperties);
        try {
            super.setAllOptions(map);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
    }

    @Override
    public void checkMandatory() throws com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

    @Override
    public SOSOptionString getProxyHost() {
        return null;
    }

    @Override
    public SOSOptionPassword getProxyPassword() {
        return null;
    }

    @Override
    public SOSOptionPortNumber getProxyPort() {
        return null;
    }

    @Override
    public SOSOptionUserName getProxyUser() {
        return null;
    }

    @Override
    public void setProxyHost(final SOSOptionString proxyHost) {
        //
    }

    @Override
    public void setProxyPassword(final SOSOptionPassword proxyPassword) {
        //
    }

    @Override
    public void setProxyPort(final SOSOptionPortNumber proxyPort) {
        //
    }

    @Override
    public void setProxyUser(final SOSOptionUserName proxyUser) {
        //
    }

    @Override
    public SOSOptionInFileName getAuthFile() {
        return sshAuthFile;
    }

    @Override
    public SOSOptionAuthenticationMethod getAuthMethod() {
        return sshAuthMethod;
    }

    @Override
    public void setAuthFile(final SOSOptionInFileName authFile) {
        sshAuthFile = authFile;
    }

    @Override
    public void setAuthMethod(final SOSOptionAuthenticationMethod authMethod) {
        //
    }

    @Override
    public SOSOptionRegExp getFileSpec2() {
        return null;
    }

    @Override
    public void setFileSpec2(final SOSOptionRegExp p_file_spec2) {
        //
    }

    @Override
    public SOSOptionFolderName sourceDir() {
        return sourceDir;
    }

    @Override
    public SOSOptionFolderName targetDir() {
        return targetDir;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raiseExceptionOnError = new SOSOptionBoolean(this, CLASSNAME + ".raise_exception_on_error",
            "Raise an Exception if an error occured", "true", "true", true);

    public SOSOptionBoolean getRaiseExceptionOnError() {
        return raiseExceptionOnError;
    }

    public void setRaiseExceptionOnError(final SOSOptionBoolean raiseExceptionOnError) {
        this.raiseExceptionOnError = raiseExceptionOnError;
    }

    @JSOptionDefinition(name = "ProtocolCommandListener", description = "Activate the logging for Apache ftp client", key = "protocol_command_listener", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean protocolCommandListener = new SOSOptionBoolean(this, CLASSNAME + ".protocol_command_listener",
            "Activate the logging for Apache ftp client", "false", "false", true);

    @JSOptionDefinition(name = "system_property_files", description = "List of the java property files separated by semicolon", key = "system_property_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString system_property_files = new SOSOptionString(this, CLASSNAME + ".system_property_files",
            "List of the java property files separated by semicolon", "", "", false);

    @JSOptionDefinition(name = "updateConfiguration", description = "determines if a YADE configuration should be updated with the given XML snippet", key = "updateConfiguration", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean updateConfiguration = new SOSOptionBoolean(this, CLASSNAME + ".updateConfiguration",
            "determines if a YADE configuration should be updated with the given XML snippet", "", "", false);

    public SOSOptionBoolean getUpdateConfiguration() {
        return updateConfiguration;
    }

    public void setUpdateConfiguration(SOSOptionBoolean updateConfiguration) {
        this.updateConfiguration = updateConfiguration;
    }

    @JSOptionDefinition(name = "xmlUpdate", description = "the XML configuration snippet to update a YADE configuration with", key = "xmlUpdate", type = "SOSOptionString", mandatory = false)
    public SOSOptionString xmlUpdate = new SOSOptionString(this, CLASSNAME + ".xmlUpdate",
            "the XML configuration snippet to update a YADE configuration with", "", "", false);

    public SOSOptionString getXmlUpdate() {
        return xmlUpdate;
    }

    public void setXmlUpdate(SOSOptionString xmlUpdate) {
        this.xmlUpdate = xmlUpdate;
    }

    @JSOptionDefinition(name = "connection_error_retry_count_max", description = "The connection_error_retry_count_max", key = "connection_error_retry_count_max", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger connection_error_retry_count_max = new SOSOptionInteger(this, CLASSNAME + ".connection_error_retry_count_max",
            "The connection_error_retry_count_max", "3", "3", false);

    @JSOptionDefinition(name = "connection_error_retry_interval", description = "The connection_error_retry_interval in seconds", key = "connection_error_retry_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionString connection_error_retry_interval = new SOSOptionString(this, CLASSNAME + ".connection_error_retry_interval",
            "The connection_error_retry_interval", "0s", "0s", false);

}