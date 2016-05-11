package com.sos.VirtualFileSystem.Options;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

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
import com.sos.JSHelper.Options.SOSOptionEncoding;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionIniFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod.enuJSTransferModes;
import com.sos.JSHelper.Options.SOSOptionJadeOperation;
import com.sos.JSHelper.Options.SOSOptionJobChainNode;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPlatform;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProcessID;
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
    private final String className = SOSFtpOptionsSuperClass.class.getSimpleName();
    private final static Logger LOGGER = Logger.getLogger(SOSFtpOptionsSuperClass.class);

    @JSOptionDefinition(name = "TFN_Post_Command", description = "Post commands executed after creating the final TargetFile", key = "TFN_Post_Command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString TFN_Post_Command = new SOSOptionString(this, className + ".TFN_Post_Command",
            "Post commands executed after creating the final TargetFileName", "", "", false);

    public SOSOptionString getTFN_Post_Command() {
        return TFN_Post_Command;
    }

    public SOSFtpOptionsSuperClass setTFN_Post_Command(final SOSOptionString val) {
        TFN_Post_Command = val;
        return this;
    }

    @JSOptionDefinition(name = "polling_wait_4_Source_Folder", description = "During polling", key = "polling_wait_4_Source_Folder", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean pollingWait4SourceFolder = new SOSOptionBoolean(this, className + ".polling_wait_4_Source_Folder", "During polling",
            "false", "false", true);

    public String getPollingWait4SourceFolder() {
        return pollingWait4SourceFolder.Value();
    }

    public SOSFtpOptionsSuperClass setPollingWait4SourceFolder(final String val) {
        pollingWait4SourceFolder.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "include", description = "the include directive as an option", key = "include", type = "SOSOptionString", mandatory = false)
    public SOSOptionString include = new SOSOptionString(this, className + ".include", "the include directive as an option", "", "", false);

    public String getinclude() {
        return include.Value();
    }

    public SOSFtpOptionsSuperClass setinclude(final String val) {
        include.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "use_filters", description = "Use filters for source and/or Targe", key = "use_filters", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean use_filters = new SOSOptionBoolean(this, className + ".use_filters", "Use filters for source and/or Targe", "false",
            "false", false);

    public SOSOptionBoolean getuse_filters() {
        return use_filters;
    }

    public SOSFtpOptionsSuperClass setuse_filters(final SOSOptionBoolean val) {
        use_filters = val;
        return this;
    }

    @JSOptionDefinition(name = "is_fragment", description = "Mark an profile as a fragment", key = "is_fragment", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean isFragment = new SOSOptionBoolean(this, className + ".is_fragment", "Mark an profile as a fragment", "false", "false",
            false);

    public String getis_fragment() {
        return isFragment.Value();
    }

    public SOSFtpOptionsSuperClass setis_fragment(final String val) {
        isFragment.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "reuse_connection", description = "reuse the current connections for all transfers", key = "reuse_connection", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean reuseConnection = new SOSOptionBoolean(this, className + ".reuse_connection",
            "reuse the current connections for all transfers", "false", "false", false);

    public String getreuse_connection() {
        return reuseConnection.Value();
    }

    public SOSFtpOptionsSuperClass setreuse_connection(final String val) {
        reuseConnection.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "polling_server", description = "act as a polling server", key = "polling_server", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean PollingServer = new SOSOptionBoolean(this, className + ".polling_server", "act as a polling server", "false", "false",
            false);

    public String getpolling_server() {
        return PollingServer.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_server(final String val) {
        PollingServer.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "polling_end_at", description = "PollingServer should stop at the specified date/time", key = "polling_end_at", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingEndAt = new SOSOptionTime(this, className + ".polling_end_at", "Polling should stop at the specified date/time", "0",
            "0", false);

    public String getpolling_end_at() {
        return pollingEndAt.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_end_at(final String val) {
        pollingEndAt.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "polling_server_poll_forever", description = "poll forever", key = "polling_server_poll_forever", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean PollingServerPollForever = new SOSOptionBoolean(this, className + ".polling_server_poll_forever", "poll forever",
            "false", "false", true);

    public String getpolling_server_poll_forever() {
        return PollingServerPollForever.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_server_poll_forever(final String val) {
        PollingServerPollForever.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "polling_server_duration", description = "How long the PollingServer should run", key = "polling_server_duration", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime pollingServerDuration = new SOSOptionTime(this, className + ".polling_server_duration",
            "How long the PollingServer should run", "0", "0", false);

    public String getpolling_server_duration() {
        return pollingServerDuration.Value();
    }

    public SOSFtpOptionsSuperClass setpolling_server_duration(final String val) {
        pollingServerDuration.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Lazy_Connection_Mode", description = "Connect to Target as late as possible", key = "Lazy_Connection_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean LazyConnectionMode = new SOSOptionBoolean(this, className + ".Lazy_Connection_Mode",
            "Connect to Target as late as possible", "false", "false", false);

    public String getLazy_Connection_Mode() {
        return LazyConnectionMode.Value();
    }

    public SOSFtpOptionsSuperClass setLazy_Connection_Mode(final String val) {
        LazyConnectionMode.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "platform", description = "platform on which the app is running", key = "platform", type = "SOSOptionString", mandatory = false)
    public SOSOptionPlatform platform = new SOSOptionPlatform(this, className + ".platform", "platform on which the app is running", "", "", false);

    public String getplatform() {
        return platform.Value();
    }

    public SOSFtpOptionsSuperClass setplatform(final String val) {
        platform.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_success", description = "Send a Mail in case of sucess", key = "mail_on_success", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_on_success = new SOSOptionBoolean(this, className + ".mail_on_success", "Send a Mail in case of sucess", "false",
            "false", false);

    public String getmail_on_success() {
        return mail_on_success.Value();
    }

    public SOSFtpOptionsSuperClass setmail_on_success(final String val) {
        mail_on_success.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_error", description = "Send a Mail in case of error", key = "mail_on_error", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_on_error = new SOSOptionBoolean(this, className + ".mail_on_error", "Send a Mail in case of sucess", "false",
            "false", false);

    public String getmail_on_error() {
        return mail_on_error.Value();
    }

    public SOSFtpOptionsSuperClass setmail_on_error(final String val) {
        mail_on_error.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "mail_on_empty_files", description = "Send a Mail in case of empty files", key = "mail_on_empty_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean mail_on_empty_files = new SOSOptionBoolean(this, className + ".mail_on_empty_files",
            "Send a Mail in case of empty files", "false", "false", false);

    public String getmail_on_empty_files() {
        return mail_on_empty_files.Value();
    }

    public SOSFtpOptionsSuperClass setmail_on_empty_files(final String val) {
        mail_on_empty_files.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "title", description = "The Title for a section /profile", key = "title", type = "SOSOptionString", mandatory = false)
    public SOSOptionString title = new SOSOptionString(this, className + ".title", "The Title for a section /profile", "", "", false);

    public String gettitle() {
        return title.Value();
    }

    public SOSFtpOptionsSuperClass settitle(final String val) {
        title.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "keep_modification_date", description = "Keep Modification Date of File", key = "keep_modification_date", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean keep_modification_date = new SOSOptionBoolean(this, className + ".keep_modification_date",
            "Keep Modification Date of File", "false", "false", false);

    public SOSOptionBoolean KeepModificationDate = (SOSOptionBoolean) keep_modification_date.SetAlias("KeepModificationate");

    public String getkeep_modification_date() {
        return keep_modification_date.Value();
    }

    public SOSFtpOptionsSuperClass setkeep_modification_date(final String val) {
        keep_modification_date.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "cumulate_files", description = "cumulate (all) files into one file by append", key = "cumulate_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CumulateFiles = new SOSOptionBoolean(this, className + ".cumulate_files", "cumulate (all) files into one file by append",
            "false", "false", false);

    public String getcumulate_files() {
        return CumulateFiles.Value();
    }

    public SOSFtpOptionsSuperClass setcumulate_files(final String val) {
        CumulateFiles.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_filename", description = "Name of File into which all files hat to be cumulated", key = "cumulative_filename", type = "SOSOptionFileName", mandatory = true)
    public SOSOptionFileName CumulativeFileName = new SOSOptionFileName(this, className + ".cumulative_filename",
            "Name of File into which all files hat to be cumulated", "", "", false);

    public String getcumulative_filename() {
        return CumulativeFileName.Value();
    }

    public SOSFtpOptionsSuperClass setcumulative_filename(final String val) {
        CumulativeFileName.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_file_separator", description = "Text which has to beplaced between cumulated files", key = "cumulative_file_separator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString CumulativeFileSeparator = new SOSOptionString(this, className + ".cumulative_file_separator",
            "Text which has to beplaced between cumulated files", "", "", false);

    public String getcumulative_file_separator() {
        return CumulativeFileSeparator.Value();
    }

    public SOSFtpOptionsSuperClass setcumulative_file_separator(final String val) {
        CumulativeFileSeparator.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "cumulative_file_delete", description = "Delete cumulative file before starting transfer", key = "cumulative_file_delete", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CumulativeFileDelete = new SOSOptionBoolean(this, className + ".cumulative_file_delete",
            "Delete cumulative file before starting transfer", "false", "false", false);

    public String getcumulative_file_delete() {
        return CumulativeFileDelete.Value();
    }

    public SOSFtpOptionsSuperClass setcumulative_file_delete(final String val) {
        CumulativeFileDelete.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Post_Command", description = "FTP-Command to be executed after transfer", key = "Post_Command", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString Post_Command = new SOSOptionCommandString(this, className + ".Post_Command",
            "FTP-Command to be executed after transfer", "", "", false);

    public String getPost_Command() {
        return Post_Command.Value();
    }

    public SOSFtpOptionsSuperClass setPost_Command(final String val) {
        Post_Command.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Pre_Command", description = "FTP-Command to be execute before transfer", key = "Pre_Command", type = "SOSOptionString  ", mandatory = false)
    public SOSOptionCommandString Pre_Command = new SOSOptionCommandString(this, className + ".Pre_Command", "", "", "", false);

    public String getPre_Command() {
        return Pre_Command.Value();
    }

    public SOSFtpOptionsSuperClass setPre_Command(final String val) {
        Pre_Command.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "CheckServerFeatures", description = "The available features of a ftp-server", key = "Check_Server_Features", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckServerFeatures = new SOSOptionBoolean(this, className + ".Check_Server_Features",
            "The available features of a ftp-server", "false", "false", false);

    @Override
    public SOSOptionBoolean CheckServerFeatures() {
        return CheckServerFeatures;
    }

    public String getCheckServerFeatures() {
        return CheckServerFeatures.Value();
    }

    public SOSFtpOptionsSuperClass setCheckServerFeatures(final String val) {
        CheckServerFeatures.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "PollKeepConnection", description = "Keep connection while polling", key = "PollKeepConnection", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean PollKeepConnection = new SOSOptionBoolean(this, className + ".PollKeepConnection", "Keep connection while polling",
            "false", "false", true);

    public String getPollKeepConnection() {
        return PollKeepConnection.Value();
    }

    public SOSFtpOptionsSuperClass setPollKeepConnection(final String val) {
        PollKeepConnection.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "FileNameEncoding", description = "Set the encoding-type of a file name", key = "FileNameEncoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionString FileNameEncoding = new SOSOptionString(this, className + ".FileNameEncoding", "Set the encoding-type of a file name", "",
            "ISO-8859-1", false);

    public String getFileNameEncoding() {
        return FileNameEncoding.Value();
    }

    public SOSFtpOptionsSuperClass setFileNameEncoding(final String val) {
        FileNameEncoding.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "ControlEncoding", description = "Specify the encoding-type, e.g. utf-8, used by the server", key = "ControlEncoding", type = "SOSOptionString", mandatory = false)
    public SOSOptionEncoding ControlEncoding = new SOSOptionEncoding(this, className + ".ControlEncoding",
            "Specify the encoding-type, e.g. utf-8, used by the server", "", "", false);

    public String getControlEncoding() {
        return ControlEncoding.Value();
    }

    public SOSFtpOptionsSuperClass setControlEncoding(final String val) {
        ControlEncoding.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "History_File_Append_Mode", description = "Specifies wether the History File has to be written in append mode", key = "History_File_Append_Mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean HistoryFileAppendMode = new SOSOptionBoolean(this, className + ".History_File_Append_Mode",
            "Specifies wether the History File has to be written in append mode", "true", "true", false);

    public SOSOptionBoolean getHistoryFileAppendMode() {
        return HistoryFileAppendMode;
    }

    public SOSFtpOptionsSuperClass setHistoryFileAppendMode(final SOSOptionBoolean val) {
        HistoryFileAppendMode = val;
        return this;
    }

    @JSOptionDefinition(name = "HistoryEntries", description = "List of additional entries for the transfer history record.", key = "HistoryEntries", type = "SOSOptionArrayList", mandatory = false)
    public SOSOptionArrayList HistoryEntries = new SOSOptionArrayList(this, className + ".HistoryEntries",
            "List of additional entries for the transfer history record.", "", "", false);

    public String getHistoryEntries() {
        return HistoryEntries.Value();
    }

    public SOSFtpOptionsSuperClass setHistoryEntries(final String val) {
        HistoryEntries.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "SendTransferHistory", description = "If this option is set to true, the transfer history will be sent to the background service.", key = "SendTransferHistory", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean SendTransferHistory = new SOSOptionBoolean(this, className + ".SendTransferHistory",
            "If this option is set to true, the transfer history will be sent to the background service.", "false", "false", false);

    public String getSendTransferHistory() {
        return SendTransferHistory.Value();
    }

    public SOSFtpOptionsSuperClass setSendTransferHistory(final String val) {
        SendTransferHistory.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Scheduler_Transfer_Method", description = "The technical method of how to communicate with the JobScheduler", key = "Scheduler_Transfer_Method", type = "SOSOptionJSTransferMethod", mandatory = true)
    public SOSOptionBackgroundServiceTransferMethod Scheduler_Transfer_Method = new SOSOptionBackgroundServiceTransferMethod(this, className
            + ".Scheduler_Transfer_Method", "The technical method of how to communicate with the JobScheduler", enuJSTransferModes.udp.description,
            enuJSTransferModes.udp.description, true);

    public String getScheduler_Transfer_Method() {
        return Scheduler_Transfer_Method.Value();
    }

    public SOSFtpOptionsSuperClass setScheduler_Transfer_Method(final String val) {
        Scheduler_Transfer_Method.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "PreFtpCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreFtpCommands", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString PreFtpCommands = new SOSOptionCommandString(this, className + ".Pre_Ftp_Commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);

    public SOSOptionCommandString PreTransferCommands = (SOSOptionCommandString) PreFtpCommands.SetAlias("pre_transfer_commands");

    public String getPreFtpCommands() {
        return PreFtpCommands.Value();
    }

    public SOSFtpOptionsSuperClass setPreFtpCommands(final String val) {
        PreFtpCommands.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.", key = "PostTransferCommands", type = "SOSOptionString", mandatory = false)
    public SOSOptionCommandString PostTransferCommands = new SOSOptionCommandString(this, className + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);

    public SOSOptionString PostFtpCommands = (SOSOptionString) PostTransferCommands.SetAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return PostTransferCommands.Value();
    }

    public SOSFtpOptionsSuperClass setPostTransferCommands(final String val) {
        PostTransferCommands.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "IntegrityHashType", description = "", key = "integrity_hash_type", type = "SOSOptionString", mandatory = false)
    public SOSOptionString IntegrityHashType = new SOSOptionString(this, className + ".integrity_hash_type",
            "The Type of the integrity hash, e.g. md5", "md5", "md5", false);

    public SOSOptionString SecurityHashType = (SOSOptionString) IntegrityHashType.SetAlias("security_hash_type");

    @JSOptionDefinition(name = "DecompressAfterTransfer", description = "", key = "Decompress_After_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean DecompressAfterTransfer = new SOSOptionBoolean(this, className + ".Decompress_After_Transfer",
            "Decompress zipped-files after transfer", "false", "false", false);

    @JSOptionDefinition(name = "ConcurrentTransfer", description = "", key = "Concurrent_Transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean ConcurrentTransfer = new SOSOptionBoolean(this, className + ".Concurrent_Transfer", "Process transfers simultaneously",
            "false", "false", false);

    @JSOptionDefinition(name = "CheckIntegrityHash", description = "", key = "check_integrity_hash", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckIntegrityHash = new SOSOptionBoolean(this, className + ".check_integrity_hash", "Calculates the integrity hash",
            "false", "false", false);

    public SOSOptionBoolean CheckSecurityHash = (SOSOptionBoolean) CheckIntegrityHash.SetAlias("check_security_hash");

    @JSOptionDefinition(name = "MaxConcurrentTransfers", description = "", key = "Max_Concurrent_Transfers", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger MaxConcurrentTransfers = new SOSOptionInteger(this, className + ".Max_Concurrent_Transfers",
            "Maximum Numbers of parallel transfers", "5", "1", false);
    @JSOptionDefinition(name = "CreateIntegrityHashFile", description = "", key = "create_integrity_hash_file", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CreateIntegrityHashFile = new SOSOptionBoolean(this, className + ".create_integrity_hash_file",
            "Flag if an integrity hash file will be created on the target", "false", "false", false);

    public SOSOptionBoolean CreateSecurityHashFile = (SOSOptionBoolean) CreateIntegrityHashFile.SetAlias("create_security_hash_file");

    @JSOptionDefinition(name = "BufferSize", description = "", key = "buffer_Size", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger BufferSize = new SOSOptionInteger(this, className + ".buffer_Size", "This parameter specifies the interval in seconds",
            "32000", "4096", false);

    @JSOptionDefinition(name = "create_order", description = "Activate file-order creation With this parameter it is possible to specif", key = "create_order", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_order = new SOSOptionBoolean(this, className + ".create_order",
            "Activate file-order creation With this parameter it is possible to specif", "false", "false", false);

    public SOSOptionBoolean getcreate_order() {
        return create_order;
    }

    public void setcreate_order(final SOSOptionBoolean val) {
        create_order = val;
    }

    @JSOptionDefinition(name = "create_orders_for_all_files", description = "Create a file-order for every file in the result-list", key = "create_orders_for_all_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_orders_for_all_files = new SOSOptionBoolean(this, className + ".create_orders_for_all_files",
            "Create a file-order for every file in the result-list", "false", "false", false);

    public SOSOptionBoolean getcreate_orders_for_all_files() {
        return create_orders_for_all_files;
    }

    public void setcreate_orders_for_all_files(final SOSOptionBoolean val) {
        create_orders_for_all_files = val;
    }

    @JSOptionDefinition(name = "expected_size_of_result_set", description = "number of expected hits in result-list", key = "expected_size_of_result_set", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger expected_size_of_result_set = new SOSOptionInteger(this, className + ".expected_size_of_result_set",
            "number of expected hits in result-list", "0", "0", false);

    public SOSOptionInteger getexpected_size_of_result_set() {
        return expected_size_of_result_set;
    }

    public void setexpected_size_of_result_set(final SOSOptionInteger val) {
        expected_size_of_result_set = val;
    }

    @JSOptionDefinition(name = "file", description = "File or Folder to watch for Checked file or directory Supports", key = "file", type = "SOSOptionString", mandatory = true)
    public SOSOptionFileName file = new SOSOptionFileName(this, className + ".file",
            "File or Folder to watch for Checked file or directory Supports", ".", ".", true);

    @JSOptionDefinition(name = "target", description = "target or Folder to watch for Checked target or directory Supports", key = "target", type = "SOSOptionString", mandatory = true)
    public SOSOptionFileName target = new SOSOptionFileName(this, className + ".target",
            "target or Folder to watch for Checked target or directory Supports", ".", ".", true);

    public SOSOptionFileName getfile() {
        return file;
    }

    public void setfile(final SOSOptionFileName val) {
        file = val;
    }

    public SOSOptionFileName FileName = (SOSOptionFileName) file.SetAlias(className + ".FileName");

    @JSOptionDefinition(name = "max_file_age", description = "maximum age of a file Specifies the maximum age of a file. If a file", key = "max_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime max_file_age = new SOSOptionTime(this, className + ".max_file_age",
            "maximum age of a file Specifies the maximum age of a file. If a file", "0", "0", false);

    public SOSOptionTime getmax_file_age() {
        return max_file_age;
    }

    public void setmax_file_age(final SOSOptionTime val) {
        max_file_age = val;
    }

    public SOSOptionTime FileAgeMaximum = (SOSOptionTime) max_file_age.SetAlias(className + ".FileAgeMaximum");

    @JSOptionDefinition(name = "max_file_size", description = "maximum size of a file Specifies the maximum size of a file in", key = "max_file_size", type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize max_file_size = new SOSOptionFileSize(this, className + ".max_file_size",
            "maximum size of a file Specifies the maximum size of a file in", "-1", "-1", false);

    public SOSOptionFileSize getmax_file_size() {
        return max_file_size;
    }

    public void setmax_file_size(final SOSOptionFileSize val) {
        max_file_size = val;
    }

    public SOSOptionFileSize FileSizeMaximum = (SOSOptionFileSize) max_file_size.SetAlias(className + ".FileSizeMaximum");

    @JSOptionDefinition(name = "min_file_age", description = "minimum age of a file Specifies the minimum age of a files. If the fi", key = "min_file_age", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime min_file_age = new SOSOptionTime(this, className + ".min_file_age",
            "minimum age of a file Specifies the minimum age of a files. If the fi", "0", "0", false);

    public SOSOptionTime getmin_file_age() {
        return min_file_age;
    }

    public void setmin_file_age(final SOSOptionTime val) {
        min_file_age = val;
    }

    public SOSOptionTime FileAgeMinimum = (SOSOptionTime) min_file_age.SetAlias(className + ".FileAgeMinimum");

    @JSOptionDefinition(name = "min_file_size", description = "minimum size of one or multiple files Specifies the minimum size of one", key = "min_file_size", type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize min_file_size = new SOSOptionFileSize(this, className + ".min_file_size",
            "minimum size of one or multiple files Specifies the minimum size of one", "-1", "-1", false);

    public SOSOptionFileSize getmin_file_size() {
        return min_file_size;
    }

    public void setmin_file_size(final SOSOptionFileSize val) {
        min_file_size = val;
    }

    public SOSOptionFileSize FileSizeMinimum = (SOSOptionFileSize) min_file_size.SetAlias(className + ".FileSizeMinimum");

    @JSOptionDefinition(name = "MergeOrderParameter", description = "Merge created order parameter with parameter of current order", key = "MergeOrderParameter", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean MergeOrderParameter = new SOSOptionBoolean(this, className + ".MergeOrderParameter",
            "Merge created order parameter with parameter of current order", "false", "false", false);

    public String getMergeOrderParameter() {
        return MergeOrderParameter.Value();
    }

    public SOSFtpOptionsSuperClass setMergeOrderParameter(final String val) {
        MergeOrderParameter.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "next_state", description = "The first node to execute in a jobchain The name of the node of a jobchai", key = "next_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode next_state = new SOSOptionJobChainNode(this, className + ".next_state",
            "The first node to execute in a jobchain The name of the node of a jobchai", "", "", false);

    public SOSOptionJobChainNode getnext_state() {
        return next_state;
    }

    public void setnext_state(final SOSOptionJobChainNode val) {
        next_state = val;
    }

    @JSOptionDefinition(name = "on_empty_result_set", description = "Set next node on empty result set The next Node (Step, Job) to execute i", key = "on_empty_result_set", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode on_empty_result_set = new SOSOptionJobChainNode(this, className + ".on_empty_result_set",
            "Set next node on empty result set The next Node (Step, Job) to execute i", "", "", false);

    public SOSOptionJobChainNode geton_empty_result_set() {
        return on_empty_result_set;
    }

    public void seton_empty_result_set(final SOSOptionJobChainNode val) {
        on_empty_result_set = val;
    }

    @JSOptionDefinition(name = "order_jobscheduler_host", description = "Name of Jobscheduler Host where the order have to be started", key = "order_jobscheduler_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName order_jobscheduler_host = new SOSOptionHostName(this, className + ".order_jobscheduler_host",
            "Name of Jobscheduler Host where the order have to be started", "", "", false);

    public SOSOptionHostName getorder_jobscheduler_host() {
        return order_jobscheduler_host;
    }

    public void setorder_jobscheduler_host(final SOSOptionHostName val) {
        order_jobscheduler_host = val;
    }

    @JSOptionDefinition(name = "order_jobscheduler_port", description = "The port of the JobScheduler node", key = "order_jobscheduler_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber order_jobscheduler_port = new SOSOptionPortNumber(this, className + ".order_jobscheduler_port",
            "The port of the JobScheduler node", "", "4444", false);

    public SOSOptionPortNumber getorder_jobscheduler_port() {
        return order_jobscheduler_port;
    }

    public void setorder_jobscheduler_port(final SOSOptionPortNumber val) {
        order_jobscheduler_port = val;
    }

    @JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch", key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString order_jobchain_name = new SOSOptionString(this, className + ".order_jobchain_name",
            "The name of the jobchain which belongs to the order The name of the jobch", "", "", false);

    public SOSOptionString getorder_jobchain_name() {
        return order_jobchain_name;
    }

    public void setorder_jobchain_name(final SOSOptionString val) {
        order_jobchain_name = val;
    }

    @JSOptionDefinition(name = "raise_error_if_result_set_is", description = "raise error on expected size of result-set With this parameter it is poss", key = "raise_error_if_result_set_is", type = "SOSOptionRelOp", mandatory = false)
    public SOSOptionRelOp raise_error_if_result_set_is = new SOSOptionRelOp(this, className + ".raise_error_if_result_set_is",
            "raise error on expected size of result-set With this parameter it is poss", "", "", false);

    public SOSOptionRelOp getraise_error_if_result_set_is() {
        return raise_error_if_result_set_is;
    }

    public void setraise_error_if_result_set_is(final SOSOptionRelOp val) {
        raise_error_if_result_set_is = val;
    }

    @JSOptionDefinition(name = "result_list_file", description = "Name of the result-list file If the value of this parameter specifies a v", key = "result_list_file", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName result_list_file = new SOSOptionFileName(this, className + ".result_list_file",
            "Name of the result-list file If the value of this parameter specifies a v", "", "", false);

    public SOSOptionFileName getresult_list_file() {
        return result_list_file;
    }

    public void setresult_list_file(final SOSOptionFileName val) {
        result_list_file = val;
    }

    @JSOptionDefinition(name = "scheduler_file_name", description = "Name of the file to process for a file-order", key = "scheduler_file_name", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_name = new SOSOptionFileName(this, className + ".scheduler_file_name",
            "Name of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getscheduler_file_name() {
        return scheduler_file_name;
    }

    public void setscheduler_file_name(final SOSOptionFileName val) {
        scheduler_file_name = val;
    }

    @JSOptionDefinition(name = "scheduler_file_parent", description = "pathanme of the file to process for a file-order", key = "scheduler_file_parent", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_parent = new SOSOptionFileName(this, className + ".scheduler_file_parent",
            "pathanme of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getscheduler_file_parent() {
        return scheduler_file_parent;
    }

    public void setscheduler_file_parent(final SOSOptionFileName val) {
        scheduler_file_parent = val;
    }

    @JSOptionDefinition(name = "scheduler_file_path", description = "file to process for a file-order Using Directory Monitoring with", key = "scheduler_file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_path = new SOSOptionFileName(this, className + ".scheduler_file_path",
            "file to process for a file-order Using Directory Monitoring with", "", "", false);

    public SOSOptionFileName getscheduler_file_path() {
        return scheduler_file_path;
    }

    public void setscheduler_file_path(final SOSOptionFileName val) {
        scheduler_file_path = val;
    }

    @JSOptionDefinition(name = "scheduler_sosfileoperations_resultsetsize", description = "The amount of hits in the result set of the operation", key = "scheduler_sosfileoperations_resultsetsize", type = "SOSOptionsInteger", mandatory = false)
    public SOSOptionInteger scheduler_sosfileoperations_resultsetsize = new SOSOptionInteger(this, className
            + ".scheduler_sosfileoperations_resultsetsize", "The amount of hits in the result set of the operation", "", "", false);

    public SOSOptionInteger getscheduler_sosfileoperations_resultsetsize() {
        return scheduler_sosfileoperations_resultsetsize;
    }

    public void setscheduler_sosfileoperations_resultsetsize(final SOSOptionInteger val) {
        scheduler_sosfileoperations_resultsetsize = val;
    }

    public SOSOptionInteger ResultSetSize = (SOSOptionInteger) scheduler_sosfileoperations_resultsetsize.SetAlias(className + ".ResultSetSize");

    @JSOptionDefinition(name = "skip_first_files", description = "number of files to remove from the top of the result-set The numbe", key = "skip_first_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skip_first_files = new SOSOptionInteger(this, className + ".skip_first_files",
            "number of files to remove from the top of the result-set The numbe", "0", "0", false);

    public SOSOptionInteger getskip_first_files() {
        return skip_first_files;
    }

    public void setskip_first_files(final SOSOptionInteger val) {
        skip_first_files = val;
    }

    public SOSOptionInteger NoOfFirstFiles2Skip = (SOSOptionInteger) skip_first_files.SetAlias(className + ".NoOfFirstFiles2Skip");

    @JSOptionDefinition(name = "skip_last_files", description = "number of files to remove from the bottom of the result-set The numbe", key = "skip_last_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skip_last_files = new SOSOptionInteger(this, className + ".skip_last_files",
            "number of files to remove from the bottom of the result-set The numbe", "0", "0", false);

    public SOSOptionInteger getskip_last_files() {
        return skip_last_files;
    }

    public void setskip_last_files(final SOSOptionInteger val) {
        skip_last_files = val;
    }

    public SOSOptionInteger NoOfLastFiles2Skip = (SOSOptionInteger) skip_last_files.SetAlias(className + ".NoOfLastFiles2Skip");

    @JSOptionDefinition(name = "Max_Files", description = "Maximum number of files to process", key = "Max_Files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger MaxFiles = new SOSOptionInteger(this, className + ".Max_Files", "Maximum number of files to process", "-1", "-1", false);

    public String getMax_Files() {
        return MaxFiles.Value();
    }

    public SOSFtpOptionsSuperClass setMax_Files(final String pstrValue) {
        MaxFiles.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "check_steady_count", description = "Number of tries for Steady check", key = "check_steady_count", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger CheckSteadyCount = new SOSOptionInteger(this, className + ".check_steady_count", "Number of tries for Steady check",
            "10", "10", false);

    public String getcheck_steady_count() {
        return CheckSteadyCount.Value();
    }

    public SOSFtpOptionsSuperClass setcheck_steady_count(final String val) {
        CheckSteadyCount.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "check_steady_state_interval", description = "The intervall for steady state checking", key = "check_steady_state_interval", type = "SOSOptionFileTime", mandatory = false)
    public SOSOptionTime check_steady_state_interval = new SOSOptionTime(this, className + ".check_steady_state_interval",
            "The intervall for steady state checking", "1", "1", false);

    public SOSOptionTime CheckSteadyStateInterval = (SOSOptionTime) check_steady_state_interval.SetAlias("check_steady_state_interval");

    public String getcheck_steady_state_interval() {
        return check_steady_state_interval.Value();
    }

    public SOSFtpOptionsSuperClass setcheck_steady_state_interval(final String val) {
        check_steady_state_interval.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Check_Steady_State_Of_Files", description = "Check wether a file is beeing modified", key = "Check_Steady_State_Of_Files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CheckSteadyStateOfFiles = new SOSOptionBoolean(this, className + ".Check_Steady_State_Of_Files",
            "Check wether a file is beeing modified", "false", "false", false);

    public String getCheckSteadyStateOfFiles() {
        return CheckSteadyStateOfFiles.Value();
    }

    public SOSFtpOptionsSuperClass setCheckSteadyStateOfFiles(final String val) {
        CheckSteadyStateOfFiles.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "PollErrorState", description = "Next state in Chain if no files found", key = "Poll_Error_State", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionJobChainNode PollErrorState = new SOSOptionJobChainNode(this, className + ".Poll_Error_State",
            "Next state in Chain if no files found", "", "", false);

    public SOSOptionJobChainNode NoFilesState = (SOSOptionJobChainNode) PollErrorState.SetAlias("No_files_state");

    public String getPollErrorState() {
        return PollErrorState.Value();
    }

    public SOSFtpOptionsSuperClass setPollErrorState(final String val) {
        PollErrorState.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Steady_state_error_state", description = "Next state in JobChain if check steady state did not comes to an normal end", key = "Steady_state_error_state", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode Steady_state_error_state = new SOSOptionJobChainNode(this, className + ".Steady_state_error_state",
            "Next state in JobChain if check steady state did not comes to an normal end", "", "", false);

    public SOSOptionJobChainNode SteadyStateErrorState = (SOSOptionJobChainNode) Steady_state_error_state.SetAlias("SteadyErrorState");

    public String getSteady_state_error_state() {
        return Steady_state_error_state.Value();
    }

    public SOSFtpOptionsSuperClass setSteady_state_error_state(final String val) {
        Steady_state_error_state.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "make_Dirs", description = "Create missing Directory on Target", key = "make_Dirs", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean makeDirs = new SOSOptionBoolean(this, className + ".make_Dirs", "Create missing Directory on Target", "true", "true",
            false);

    public SOSOptionBoolean createFoldersOnTarget = (SOSOptionBoolean) makeDirs.SetAlias("create_folders_on_target");

    public String getmake_Dirs() {
        return makeDirs.Value();
    }

    public SOSFtpOptionsSuperClass setmake_Dirs(final String val) {
        makeDirs.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "File_List_Name", description = "File with a list of file names", key = "File_List_Name", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName FileListName = new SOSOptionInFileName(this, className + ".File_List_Name", "File with a list of file names", "", "",
            false);

    public String getFileListName() {
        return FileListName.Value();
    }

    public SOSFtpOptionsSuperClass setFileListName(final String val) {
        FileListName.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "Create_Result_Set", description = "Write the ResultSet to a file", key = "Create_Result_Set", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean CreateResultSet = new SOSOptionBoolean(this, className + ".Create_Result_Set", "Write the ResultSet to a file", "false",
            "false", false);

    public SOSOptionBoolean CreateResultList = (SOSOptionBoolean) CreateResultSet.SetAlias("create_result_list");

    public String getCreateResultSet() {
        return CreateResultSet.Value();
    }

    public SOSFtpOptionsSuperClass setCreateResultSet(final String val) {
        CreateResultSet.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "ResultSetFileName", description = "Name of a File with a filelist or a resultlist", key = "Result_Set_FileName", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionOutFileName ResultSetFileName = new SOSOptionOutFileName(this, className + ".Result_Set_File_Name",
            "Name of a File with a filelist or a resultlist", "", "", false);

    public String getResultSetFileName() {
        return ResultSetFileName.Value();
    }

    public SOSFtpOptionsSuperClass setResultSetFileName(final String val) {
        ResultSetFileName.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "source_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionFolderName SourceDir = new SOSOptionFolderName(this, className + ".source_dir",
            "local_dir Local directory into which or from which", "", "", false);

    @JSOptionDefinition(name = "target_dir", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionFolderName TargetDir = new SOSOptionFolderName(this, className + ".target_dir", "target_dir directory into which or from which",
            "", "", false);

    @JSOptionDefinition(name = "account", description = "Optional account info for authentication with an", key = "account", type = "SOSOptionString", mandatory = false)
    public SOSOptionString account = new SOSOptionString(this, className + ".account", "Optional account info for authentication with an", "", "",
            false);

    @Override
    public SOSOptionString getaccount() {
        return account;
    }

    @Override
    public void setaccount(final SOSOptionString val) {
        account = val;
    }

    @JSOptionDefinition(name = "alternative_account", description = "Alternative parameter for the primary parameter", key = "alternative_account", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_account = new SOSOptionString(this, className + ".alternative_account",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getalternative_account() {
        return alternative_account;
    }

    @Override
    public void setalternative_account(final SOSOptionString val) {
        alternative_account = val;
    }

    @JSOptionDefinition(name = "alternative_host", description = "Alternative parameter for the primary parameter", key = "alternative_host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName alternative_host = new SOSOptionHostName(this, className + ".alternative_host",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionHostName getalternative_host() {
        return alternative_host;
    }

    @Override
    public void setalternative_host(final SOSOptionHostName val) {
        alternative_host = val;
    }

    @JSOptionDefinition(name = "alternative_passive_mode", description = "Alternative parameter for the primary parameter", key = "alternative_passive_mode", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_passive_mode = new SOSOptionString(this, className + ".alternative_passive_mode",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getalternative_passive_mode() {
        return alternative_passive_mode;
    }

    @Override
    public void setalternative_passive_mode(final SOSOptionString val) {
        alternative_passive_mode = val;
    }

    @JSOptionDefinition(name = "alternative_password", description = "Alternative parameter for the primary parameter", key = "alternative_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword alternative_password = new SOSOptionPassword(this, className + ".alternative_password",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionPassword getalternative_password() {
        return alternative_password;
    }

    @Override
    public void setalternative_password(final SOSOptionPassword val) {
        alternative_password = val;
    }

    @JSOptionDefinition(name = "alternative_port", description = "Alternative parameter for the primary parameter", key = "alternative_port", type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber alternative_port = new SOSOptionPortNumber(this, className + ".alternative_port",
            "Alternative parameter for the primary parameter", "21", "21", false);

    @Override
    public SOSOptionPortNumber getalternative_port() {
        return alternative_port;
    }

    @Override
    public void setalternative_port(final SOSOptionPortNumber val) {
        alternative_port = val;
    }

    public SOSOptionPortNumber AlternativePortNumber = (SOSOptionPortNumber) alternative_port.SetAlias(className + ".AlternativePortNumber");

    @JSOptionDefinition(name = "alternative_remote_dir", description = "Alternative parameter for the primary parameter", key = "alternative_remote_dir", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_remote_dir = new SOSOptionString(this, className + ".alternative_remote_dir",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getalternative_remote_dir() {
        return alternative_remote_dir;
    }

    @Override
    public void setalternative_remote_dir(final SOSOptionString val) {
        alternative_remote_dir = val;
    }

    @JSOptionDefinition(name = "alternative_transfer_mode", description = "Alternative parameter for the primary parameter", key = "alternative_transfer_mode", type = "SOSOptionString", mandatory = false)
    public SOSOptionString alternative_transfer_mode = new SOSOptionString(this, className + ".alternative_transfer_mode",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionString getalternative_transfer_mode() {
        return alternative_transfer_mode;
    }

    @Override
    public void setalternative_transfer_mode(final SOSOptionString val) {
        alternative_transfer_mode = val;
    }

    @JSOptionDefinition(name = "alternative_user", description = "Alternative parameter for the primary parameter", key = "alternative_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName alternative_user = new SOSOptionUserName(this, className + ".alternative_user",
            "Alternative parameter for the primary parameter", "", "", false);

    @Override
    public SOSOptionUserName getalternative_user() {
        return alternative_user;
    }

    @Override
    public void setalternative_user(final SOSOptionUserName val) {
        alternative_user = val;
    }

    @JSOptionDefinition(name = "append_files", description = "This parameter specifies whether the content of a", key = "append_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean append_files = new SOSOptionBoolean(this, className + ".append_files",
            "This parameter specifies whether the content of a", "false", "false", false);

    @Override
    public SOSOptionBoolean getappend_files() {
        return append_files;
    }

    @Override
    public void setappend_files(final SOSOptionBoolean val) {
        append_files = val;
    }

    @JSOptionDefinition(name = "atomic_prefix", description = "This parameter specifies whether target files shou", key = "atomic_prefix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomic_prefix = new SOSOptionString(this, className + ".atomic_prefix",
            "This parameter specifies whether target files shou", "", "", false);

    @Override
    public SOSOptionString getatomic_prefix() {
        return atomic_prefix;
    }

    @Override
    public void setatomic_prefix(final SOSOptionString val) {
        atomic_prefix = val;
    }

    @JSOptionDefinition(name = "atomic_suffix", description = "This parameter specifies whether target files shou", key = "atomic_suffix", type = "SOSOptionString", mandatory = false)
    public SOSOptionString atomic_suffix = new SOSOptionString(this, className + ".atomic_suffix",
            "This parameter specifies whether target files shou", "", "", false);

    @Override
    public SOSOptionString getatomic_suffix() {
        return atomic_suffix;
    }

    @Override
    public void setatomic_suffix(final SOSOptionString val) {
        atomic_suffix = val;
    }

    @JSOptionDefinition(name = "banner_footer", description = "Name der Template-Datei für Protokoll-Ende This p", key = "banner_footer", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName banner_footer = new SOSOptionInFileName(this, className + ".banner_footer",
            "Name der Template-Datei für Protokoll-Ende This p", "", "", false);

    @Override
    public SOSOptionInFileName getbanner_footer() {
        return banner_footer;
    }

    @Override
    public void setbanner_footer(final SOSOptionInFileName val) {
        banner_footer = val;
    }

    @JSOptionDefinition(name = "banner_header", description = "Name of Template-File for log-File", key = "banner_header", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName banner_header = new SOSOptionInFileName(this, className + ".banner_header", "Name of Template-File for log-File", "",
            "", false);

    @Override
    public SOSOptionInFileName getbanner_header() {
        return banner_header;
    }

    @Override
    public void setbanner_header(final SOSOptionInFileName val) {
        banner_header = val;
    }

    @JSOptionDefinition(name = "check_interval", description = "This parameter specifies the interval in seconds", key = "check_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger check_interval = new SOSOptionInteger(this, className + ".check_interval",
            "This parameter specifies the interval in seconds", "60", "60", false);

    @Override
    public SOSOptionInteger getcheck_interval() {
        return check_interval;
    }

    @Override
    public void setcheck_interval(final SOSOptionInteger val) {
        check_interval = val;
    }

    @JSOptionDefinition(name = "check_retry", description = "This parameter specifies whether a file transfer", key = "check_retry", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger check_retry = new SOSOptionInteger(this, className + ".check_retry", "This parameter specifies whether a file transfer",
            "0", "0", false);

    @Override
    public SOSOptionInteger getcheck_retry() {
        return check_retry;
    }

    @Override
    public void setcheck_retry(final SOSOptionInteger val) {
        check_retry = val;
    }

    @JSOptionDefinition(name = "check_size", description = "This parameter determines whether the original f", key = "check_size", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean check_size = new SOSOptionBoolean(this, className + ".check_size", "This parameter determines whether the original f",
            "true", "true", false);

    public SOSOptionBoolean CheckFileSizeAfterTransfer = (SOSOptionBoolean) check_size.SetAlias(className + ".CheckFileSizeAfterTransfer");

    @JSOptionDefinition(name = "classpath_base", description = "The parameter is used during installation of this", key = "classpath_base", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName classpath_base = new SOSOptionFolderName(this, className + ".classpath_base",
            "The parameter is used during installation of this", "", "", false);

    @Override
    public SOSOptionFolderName getclasspath_base() {
        return classpath_base;
    }

    @Override
    public void setclasspath_base(final SOSOptionFolderName val) {
        classpath_base = val;
    }

    @JSOptionDefinition(name = "compress_files", description = "This parameter specifies whether the content of the source files", key = "compress_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean compress_files = new SOSOptionBoolean(this, className + ".compress_files",
            "This parameter specifies whether the content of the source files", "false", "false", false);

    @JSOptionDefinition(name = "compressed_file_extension", description = "Additional file-name extension for compressed files This parameter spe", key = "compressed_file_extension", type = "SOSOptionString", mandatory = false)
    public SOSOptionString compressed_file_extension = new SOSOptionString(this, className + ".compressed_file_extension",
            "Additional file-name extension for compressed files This parameter spe", ".gz", ".gz", false);

    @JSOptionDefinition(name = "current_pid", description = "This parameter is used for Unix systems and - as o", key = "current_pid", type = "SOSOptionProcessID", mandatory = false)
    public SOSOptionProcessID current_pid = new SOSOptionProcessID(this, className + ".current_pid",
            "This parameter is used for Unix systems and - as o", "", "", false);

    @Override
    public SOSOptionProcessID getcurrent_pid() {
        return current_pid;
    }

    @Override
    public void setcurrent_pid(final SOSOptionProcessID val) {
        current_pid = val;
    }

    @JSOptionDefinition(name = "file_path", description = "This parameter is used alternatively to the parame", key = "file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName file_path = new SOSOptionFileName(this, className + ".file_path", "This parameter is used alternatively to the parame",
            "", "", false);

    @Override
    public SOSOptionFileName getfile_path() {
        return file_path;
    }

    @Override
    public void setfile_path(final SOSOptionFileName val) {
        file_path = val;
    }

    @JSOptionDefinition(name = "file_spec", description = "file_spec This parameter expects a regular expressi", key = "file_spec", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp file_spec = new SOSOptionRegExp(this, className + ".file_spec", "file_spec This parameter expects a regular expressi",
            "^.*$", "^.*$", false);

    public SOSOptionRegExp FileNameRegExp = (SOSOptionRegExp) file_spec.SetAlias(className + ".FileNameRegExp");

    @Override
    public SOSOptionRegExp getfile_spec() {
        return file_spec;
    }

    @Override
    public void setfile_spec(final SOSOptionRegExp val) {
        file_spec = val;
    }

    public SOSOptionRegExp FileNamePatternRegExp = (SOSOptionRegExp) file_spec.SetAlias(className + ".FileNamePatternRegExp");

    @JSOptionDefinition(name = "force_files", description = "This parameter specifies whether an error should b", key = "force_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean force_files = new SOSOptionBoolean(this, className + ".force_files",
            "This parameter specifies whether an error should b", "true", "true", false);

    public SOSOptionBoolean ErrorOnNoDataFound = (SOSOptionBoolean) force_files.SetAlias("error_on_no_data_found", "error_when_no_data_found");

    @Override
    public SOSOptionBoolean getforce_files() {
        return force_files;
    }

    @Override
    public void setforce_files(final SOSOptionBoolean val) {
        force_files = val;
    }

    @JSOptionDefinition(name = "history", description = "This parameter causes a history file to be written", key = "history", type = "SOSOptionOutFileName", mandatory = false)
    public SOSOptionOutFileName history = new SOSOptionOutFileName(this, className + ".history",
            "This parameter causes a history file to be written", "", "", false);

    public SOSOptionOutFileName HistoryFileName = (SOSOptionOutFileName) history.SetAlias("history_file_name");

    @Override
    public SOSOptionOutFileName gethistory() {
        return history;
    }

    @Override
    public void sethistory(final SOSOptionOutFileName val) {
        history = val;
    }

    public SOSOptionOutFileName SOSFtpHistoryFileName = (SOSOptionOutFileName) history.SetAlias(className + ".SOSFtpHistoryFileName");

    @JSOptionDefinition(name = "history_repeat", description = "The parameter is used in order to synchronize para", key = "history_repeat", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger history_repeat = new SOSOptionInteger(this, className + ".history_repeat",
            "The parameter is used in order to synchronize para", "3", "3", false);

    @Override
    public SOSOptionInteger gethistory_repeat() {
        return history_repeat;
    }

    @Override
    public void sethistory_repeat(final SOSOptionInteger val) {
        history_repeat = val;
    }

    @JSOptionDefinition(name = "history_repeat_interval", description = "The parameter is used in order to synchronize para", key = "history_repeat_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger history_repeat_interval = new SOSOptionInteger(this, className + ".history_repeat_interval",
            "The parameter is used in order to synchronize para", "1", "1", false);

    @Override
    public SOSOptionInteger gethistory_repeat_interval() {
        return history_repeat_interval;
    }

    @Override
    public void sethistory_repeat_interval(final SOSOptionInteger val) {
        history_repeat_interval = val;
    }

    @JSOptionDefinition(name = "host", description = "Host-Name This parameter specifies th", key = "host", type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName host = new SOSOptionHostName(this, className + ".host", "Host-Name This parameter specifies th", "", "", false);

    @Override
    public SOSOptionHostName gethost() {
        return host;
    }

    @Override
    public void sethost(final SOSOptionHostName val) {
        host = val;
    }

    public SOSOptionHostName HostName = (SOSOptionHostName) host.SetAlias(className + ".HostName");

    @JSOptionDefinition(name = "http_proxy_host", description = "The value of this parameter is the host name or th", key = "http_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString http_proxy_host = new SOSOptionString(this, className + ".http_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @Override
    public SOSOptionString gethttp_proxy_host() {
        return http_proxy_host;
    }

    @Override
    public void sethttp_proxy_host(final SOSOptionString val) {
        http_proxy_host = val;
    }

    @JSOptionDefinition(name = "http_proxy_port", description = "This parameter specifies the port of a proxy that", key = "http_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString http_proxy_port = new SOSOptionString(this, className + ".http_proxy_port",
            "This parameter specifies the port of a proxy that", "", "", false);

    @Override
    public SOSOptionString gethttp_proxy_port() {
        return http_proxy_port;
    }

    @Override
    public void sethttp_proxy_port(final SOSOptionString val) {
        http_proxy_port = val;
    }

    @JSOptionDefinition(name = "jump_command", description = "This parameter specifies a command that is to be e", key = "jump_command", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_command = new SOSOptionString(this, className + ".jump_command",
            "This parameter specifies a command that is to be e", "", "", false);

    @Override
    public SOSOptionString getjump_command() {
        return jump_command;
    }

    @Override
    public void setjump_command(final SOSOptionString val) {
        jump_command = val;
    }

    @JSOptionDefinition(name = "jump_command_delimiter", description = "Command delimiter characters are specified using t", key = "jump_command_delimiter", type = "SOSOptionString", mandatory = true)
    public SOSOptionString jump_command_delimiter = new SOSOptionString(this, className + ".jump_command_delimiter",
            "Command delimiter characters are specified using t", "%%", "%%", true);

    @Override
    public SOSOptionString getjump_command_delimiter() {
        return jump_command_delimiter;
    }

    @Override
    public void setjump_command_delimiter(final SOSOptionString val) {
        jump_command_delimiter = val;
    }

    @JSOptionDefinition(name = "jump_command_script", description = "This parameter can be used as an alternative to ju", key = "jump_command_script", type = "SOSOptionCommandScript", mandatory = false)
    public SOSOptionCommandScript jump_command_script = new SOSOptionCommandScript(this, className + ".jump_command_script",
            "This parameter can be used as an alternative to ju", "", "", false);

    @Override
    public SOSOptionCommandScript getjump_command_script() {
        return jump_command_script;
    }

    @Override
    public void setjump_command_script(final SOSOptionCommandScript val) {
        jump_command_script = val;
    }

    @JSOptionDefinition(name = "jump_command_script_file", description = "This parameter can be used as an alternative to ju", key = "jump_command_script_file", type = "SOSOptionCommandScriptFile", mandatory = false)
    public SOSOptionCommandScriptFile jump_command_script_file = new SOSOptionCommandScriptFile(this, className + ".jump_command_script_file",
            "This parameter can be used as an alternative to ju", "", "", false);

    @Override
    public SOSOptionCommandScriptFile getjump_command_script_file() {
        return jump_command_script_file;
    }

    @Override
    public void setjump_command_script_file(final SOSOptionCommandScriptFile val) {
        jump_command_script_file = val;
    }

    @JSOptionDefinition(name = "jump_pre_command", description = "Command, which has to be executed on the jump host for each file before the transfer started.", key = "jump_pre_command", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jump_pre_command = new SOSOptionCommandString(this, className + ".jump_pre_command",
            "Commands, which has to be executed on the jump host for each file before the transfer started.", "", "", false);

    @JSOptionDefinition(name = "jump_post_command_on_success", description = "Command, which has to be executed on the jump host for each file after the transfer of the file succesfull ended.", key = "jump_post_command_on_success", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jump_post_command_on_success = new SOSOptionCommandString(this, className + ".jump_post_command_on_success",
            "Commands, which has to be executed on the jump host for each file after the transfer of the file succesfull ended.", "", "", false);

    @JSOptionDefinition(name = "jump_pre_transfer_commands", description = "Commands, which has to be executed on the jump host before the transfer started.", key = "jump_pre_transfer_commands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jump_pre_transfer_commands = new SOSOptionCommandString(this, className + ".jump_pre_transfer_commands",
            "Commands, which has to be executed on the jump host before the transfer started.", "", "", false);

    @JSOptionDefinition(name = "jump_post_transfer_commands_on_success", description = "Commands, which has to be executed on the jump host after the transfer ended successful.", key = "jump_post_transfer_commands_on_success", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jump_post_transfer_commands_on_success = new SOSOptionCommandString(this, className
            + ".jump_post_transfer_commands_on_success", "Commands, which has to be executed on the jump host after the transfer ended successful.",
            "", "", false);

    @JSOptionDefinition(name = "jump_post_transfer_commands_on_error", description = "Commands, which has to be executed on the jump host after the transfer ended with errors.", key = "jump_post_transfer_commands_on_error", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jump_post_transfer_commands_on_error = new SOSOptionCommandString(this, className
            + ".jump_post_transfer_commands_on_error", "Commands, which has to be executed on the jump host after the transfer ended with errors.",
            "", "", false);

    @JSOptionDefinition(name = "jump_post_transfer_commands_final", description = "Commands, which has to be executed on the jump host after the transfer ended independet of the transfer status.", key = "jump_post_transfer_commands_final", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString jump_post_transfer_commands_final = new SOSOptionCommandString(this, className
            + ".jump_post_transfer_commands_final",
            "Commands, which has to be executed on the jump host after the transfer ended independet of the transfer status.", "", "", false);

    @JSOptionDefinition(name = "jump_host", description = "When using a jump_host then files are first transf", key = "jump_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName jump_host = new SOSOptionHostName(this, className + ".jump_host", "When using a jump_host then files are first transf",
            "", "", false);

    @Override
    public SOSOptionHostName getjump_host() {
        return jump_host;
    }

    @Override
    public void setjump_host(final SOSOptionHostName val) {
        jump_host = val;
    }

    @JSOptionDefinition(name = "jump_ignore_error", description = "Should the value true be specified, then execution", key = "jump_ignore_error", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_ignore_error = new SOSOptionBoolean(this, className + ".jump_ignore_error",
            "Should the value true be specified, then execution", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_ignore_error() {
        return jump_ignore_error;
    }

    @Override
    public void setjump_ignore_error(final SOSOptionBoolean val) {
        jump_ignore_error = val;
    }

    @JSOptionDefinition(name = "jump_ignore_signal", description = "Should the value true be specified, t", key = "jump_ignore_signal", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_ignore_signal = new SOSOptionBoolean(this, className + ".jump_ignore_signal",
            "Should the value true be specified, t", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_ignore_signal() {
        return jump_ignore_signal;
    }

    @Override
    public void setjump_ignore_signal(final SOSOptionBoolean val) {
        jump_ignore_signal = val;
    }

    @JSOptionDefinition(name = "jump_ignore_stderr", description = "This job checks if any output to stderr has been c", key = "jump_ignore_stderr", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_ignore_stderr = new SOSOptionBoolean(this, className + ".jump_ignore_stderr",
            "This job checks if any output to stderr has been c", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_ignore_stderr() {
        return jump_ignore_stderr;
    }

    @Override
    public void setjump_ignore_stderr(final SOSOptionBoolean val) {
        jump_ignore_stderr = val;
    }

    @JSOptionDefinition(name = "jump_password", description = "Password for authentication with the jump_host.", key = "jump_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionPassword jump_password = new SOSOptionPassword(this, className + ".jump_password",
            "Password for authentication with the jump_host.", "", "", false);

    @Override
    public SOSOptionPassword getjump_password() {
        return jump_password;
    }

    @Override
    public void setjump_password(final SOSOptionPassword val) {
        jump_password = val;
    }

    @JSOptionDefinition(name = "jump_port", description = "Port on the jump_host by which files should be tra", key = "jump_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber jump_port = new SOSOptionPortNumber(this, className + ".jump_port",
            "Port on the jump_host by which files should be tra", "22", "22", false);

    @Override
    public SOSOptionPortNumber getjump_port() {
        return jump_port;
    }

    @Override
    public void setjump_port(final SOSOptionPortNumber val) {
        jump_port = val;
    }

    @JSOptionDefinition(name = "jump_protocol", description = "When using a jump_host then files are first transf", key = "jump_protocol", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_protocol = new SOSOptionString(this, className + ".jump_protocol",
            "When using a jump_host then files are first transf", "sftp", "sftp", false);

    @Override
    public SOSOptionString getjump_protocol() {
        return jump_protocol;
    }

    @Override
    public void setjump_protocol(final SOSOptionString val) {
        jump_protocol = val;
    }

    @JSOptionDefinition(name = "jump_proxy_host", description = "The value of this parameter is the host name or th", key = "jump_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_proxy_host = new SOSOptionString(this, className + ".jump_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @Override
    public SOSOptionString getjump_proxy_host() {
        return jump_proxy_host;
    }

    @Override
    public void setjump_proxy_host(final SOSOptionString val) {
        jump_proxy_host = val;
    }

    @JSOptionDefinition(name = "jump_proxy_password", description = "This parameter specifies the password for the prox", key = "jump_proxy_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_proxy_password = new SOSOptionString(this, className + ".jump_proxy_password",
            "This parameter specifies the password for the prox", "", "", false);

    @Override
    public SOSOptionString getjump_proxy_password() {
        return jump_proxy_password;
    }

    @Override
    public void setjump_proxy_password(final SOSOptionString val) {
        jump_proxy_password = val;
    }

    @JSOptionDefinition(name = "jump_proxy_port", description = "This parameter specifies the port of a proxy that", key = "jump_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_proxy_port = new SOSOptionString(this, className + ".jump_proxy_port",
            "This parameter specifies the port of a proxy that", "", "", false);

    @Override
    public SOSOptionString getjump_proxy_port() {
        return jump_proxy_port;
    }

    @Override
    public void setjump_proxy_port(final SOSOptionString val) {
        jump_proxy_port = val;
    }

    @JSOptionDefinition(name = "jump_proxy_user", description = "The value of this parameter specifies the user acc", key = "jump_proxy_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jump_proxy_user = new SOSOptionUserName(this, className + ".jump_proxy_user",
            "The value of this parameter specifies the user acc", "", "", false);

    @Override
    public SOSOptionUserName getjump_proxy_user() {
        return jump_proxy_user;
    }

    @Override
    public void setjump_proxy_user(final SOSOptionUserName val) {
        jump_proxy_user = val;
    }

    @JSOptionDefinition(name = "jump_proxy_protocol", description = "Jump Proxy protocol", key = "jump_proxy_protocol", type = "SOSOptionProxyProtocol", mandatory = false)
    public SOSOptionProxyProtocol jump_proxy_protocol = new SOSOptionProxyProtocol(this, className + ".jump_proxy_protocol", "Jump Proxy protocol",
            SOSOptionProxyProtocol.Protocol.http.name(), SOSOptionProxyProtocol.Protocol.http.name(), false);

    public SOSOptionProxyProtocol getjump_proxy_protocol() {
        return jump_proxy_protocol;
    }

    public void setjump_proxy_protocol(SOSOptionProxyProtocol val) {
        jump_proxy_protocol = val;
    }

    @JSOptionDefinition(name = "jump_simulate_shell", description = "Should the value true be specified for this parame", key = "jump_simulate_shell", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_simulate_shell = new SOSOptionBoolean(this, className + ".jump_simulate_shell",
            "Should the value true be specified for this parame", "false", "false", false);

    @Override
    public SOSOptionBoolean getjump_simulate_shell() {
        return jump_simulate_shell;
    }

    @Override
    public void setjump_simulate_shell(final SOSOptionBoolean val) {
        jump_simulate_shell = val;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_inactivity_timeout", description = "If no new characters are written to stdout or stde", key = "jump_simulate_shell_inactivity_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jump_simulate_shell_inactivity_timeout = new SOSOptionInteger(this,
            className + ".jump_simulate_shell_inactivity_timeout", "If no new characters are written to stdout or stde", "", "", false);

    @Override
    public SOSOptionInteger getjump_simulate_shell_inactivity_timeout() {
        return jump_simulate_shell_inactivity_timeout;
    }

    @Override
    public void setjump_simulate_shell_inactivity_timeout(final SOSOptionInteger val) {
        jump_simulate_shell_inactivity_timeout = val;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_login_timeout", description = "If no new characters are written to stdout or stde", key = "jump_simulate_shell_login_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger jump_simulate_shell_login_timeout = new SOSOptionInteger(this, className + ".jump_simulate_shell_login_timeout",
            "If no new characters are written to stdout or stde", "", "", false);

    @Override
    public SOSOptionInteger getjump_simulate_shell_login_timeout() {
        return jump_simulate_shell_login_timeout;
    }

    @Override
    public void setjump_simulate_shell_login_timeout(final SOSOptionInteger val) {
        jump_simulate_shell_login_timeout = val;
    }

    @JSOptionDefinition(name = "jump_simulate_shell_prompt_trigger", description = "The expected command line prompt. Using this promp", key = "jump_simulate_shell_prompt_trigger", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_simulate_shell_prompt_trigger = new SOSOptionString(this, className + ".jump_simulate_shell_prompt_trigger",
            "The expected command line prompt. Using this promp", "", "", false);

    @Override
    public SOSOptionString getjump_simulate_shell_prompt_trigger() {
        return jump_simulate_shell_prompt_trigger;
    }

    @Override
    public void setjump_simulate_shell_prompt_trigger(final SOSOptionString val) {
        jump_simulate_shell_prompt_trigger = val;
    }

    @JSOptionDefinition(name = "jump_ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "jump_ssh_auth_file", type = "SOSOptionString", mandatory = false)
    public SOSOptionInFileName jump_ssh_auth_file = new SOSOptionInFileName(this, className + ".jump_ssh_auth_file",
            "This parameter specifies the path and name of a us", "", "", false);

    @Override
    public SOSOptionInFileName getjump_ssh_auth_file() {
        return jump_ssh_auth_file;
    }

    @Override
    public void setjump_ssh_auth_file(final SOSOptionInFileName val) {
        jump_ssh_auth_file = val;
    }

    @JSOptionDefinition(name = "jump_ssh_auth_method", description = "This parameter specifies the authentication method", key = "jump_ssh_auth_method", type = "SOSOptionString", mandatory = false)
    public SOSOptionAuthenticationMethod jump_ssh_auth_method = new SOSOptionAuthenticationMethod(this, className + ".jump_ssh_auth_method",
            "This parameter specifies the authentication method", "", "", false);

    @Override
    public SOSOptionAuthenticationMethod getjump_ssh_auth_method() {
        return jump_ssh_auth_method;
    }

    @Override
    public void setjump_ssh_auth_method(final SOSOptionAuthenticationMethod val) {
        jump_ssh_auth_method = val;
    }

    @JSOptionDefinition(name = "jump_user", description = "User name for authentication with the jump_host.", key = "jump_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionUserName jump_user = new SOSOptionUserName(this, className + ".jump_user", "User name for authentication with the jump_host.",
            "", "", false);

    @Override
    public SOSOptionUserName getjump_user() {
        return jump_user;
    }

    @Override
    public void setjump_user(final SOSOptionUserName val) {
        jump_user = val;
    }

    @JSOptionDefinition(name = "jump_dir", description = "This parameter specifies the directory on the jump host", key = "jump_dir", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_dir = new SOSOptionString(this, className + ".jump_dir", "This parameter specifies the directory on the jump host",
            "/tmp", "/tmp", false);

    public SOSOptionString getjump_dir() {
        return jump_dir;
    }

    public void setjump_dir(final SOSOptionString val) {
        jump_dir = val;
    }

    @JSOptionDefinition(name = "jump_strict_hostKey_checking", description = "Check the hostkey against known hosts for SSH", key = "jump_strict_hostKey_checking", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean jump_strict_hostkey_checking = new SOSOptionBoolean(this, className + ".jump_strict_hostkey_checking",
            "Check the hostkey against known hosts for SSH", "false", "false", false);

    public SOSOptionBoolean getjump_strict_hostKey_checking() {
        return jump_strict_hostkey_checking;
    }

    public void setjump_strict_hostKey_checking(final String value) {
        jump_strict_hostkey_checking.Value(value);
    }

    @JSOptionDefinition(name = "jump_platform", description = "This parameter specifies the platform on the jump host", key = "jump_dir", type = "SOSOptionPlatform", mandatory = false)
    public SOSOptionPlatform jump_platform = new SOSOptionPlatform(this, className + ".jump_platform",
            "This parameter specifies the platform on the jump host", SOSOptionPlatform.enuValidPlatforms.unix.name(),
            SOSOptionPlatform.enuValidPlatforms.unix.name(), false);

    public SOSOptionPlatform getjump_platform() {
        return jump_platform;
    }

    public void setjump_platform(final SOSOptionPlatform val) {
        jump_platform = val;
    }

    @JSOptionDefinition(name = "jump_configuration_files", description = "Configuration file with JCraft settings located on the YADE client system", key = "jump_configuration_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString jump_configuration_files = new SOSOptionString(this, className + ".jump_configuration_files",
            "Configuration file with JCraft settings located on the YADE client system", "", "", false);

    @JSOptionDefinition(name = "local_dir", description = "local_dir Local directory into which or from which", key = "local_dir", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName local_dir = new SOSOptionFolderName(this, className + ".local_dir",
            "local_dir Local directory into which or from which", "", "", false);

    @Override
    public SOSOptionFolderName getlocal_dir() {
        return local_dir;
    }

    @Override
    public void setlocal_dir(final SOSOptionFolderName val) {
        local_dir = val;
    }

    @JSOptionDefinition(name = "mandator", description = "This parameter specifies the mandator for which a", key = "mandator", type = "SOSOptionString", mandatory = false)
    public SOSOptionString mandator = new SOSOptionString(this, className + ".mandator", "This parameter specifies the mandator for which a", "SOS",
            "SOS", false);

    @Override
    public SOSOptionString getmandator() {
        return mandator;
    }

    @Override
    public void setmandator(final SOSOptionString val) {
        mandator = val;
    }

    @JSOptionDefinition(name = "operation", description = "Operation to be executed send, receive, remove,", key = "operation", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionJadeOperation operation = new SOSOptionJadeOperation(this, className + ".operation",
            "Operation to be executed send, receive, remove,", "send", "send", true);

    @Override
    public SOSOptionJadeOperation getoperation() {
        return operation;
    }

    @Override
    public void setoperation(final SOSOptionJadeOperation val) {
        operation = val;
    }

    @JSOptionDefinition(name = "overwrite_files", description = "This parameter specifies if existing files should", key = "overwrite_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean overwrite_files = new SOSOptionBoolean(this, className + ".overwrite_files",
            "This parameter specifies if existing files should", "true", "true", false);

    @Override
    public SOSOptionBoolean getoverwrite_files() {
        return overwrite_files;
    }

    @Override
    public void setoverwrite_files(final SOSOptionBoolean val) {
        overwrite_files = val;
    }

    @JSOptionDefinition(name = "passive_mode", description = "passive_mode Passive mode for FTP is often used wit", key = "passive_mode", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean passive_mode = new SOSOptionBoolean(this, className + ".passive_mode",
            "passive_mode Passive mode for FTP is often used wit", "false", "false", false);

    @Override
    public SOSOptionBoolean getpassive_mode() {
        return passive_mode;
    }

    @Override
    public void setpassive_mode(final SOSOptionBoolean val) {
        passive_mode = val;
    }

    public SOSOptionBoolean FTPTransferModeIsPassive = (SOSOptionBoolean) passive_mode.SetAlias(className + ".FTPTransferModeIsPassive");
    @JSOptionDefinition(name = "password", description = "Password for UserID Password for a", key = "password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword password = new SOSOptionPassword(this, className + ".password", "Password for UserID Password for a", "", "", false);

    @Override
    public SOSOptionPassword getpassword() {
        return password;
    }

    @Override
    public void setpassword(final SOSOptionPassword val) {
        password = val;
    }

    @JSOptionDefinition(name = "poll_interval", description = "This parameter specifies the interval in seconds", key = "poll_interval", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionTime poll_interval = new SOSOptionTime(this, className + ".poll_interval", "This parameter specifies the interval in seconds",
            "60", "60", false);

    @Override
    public SOSOptionTime getpoll_interval() {
        return poll_interval;
    }

    @Override
    public void setpoll_interval(final SOSOptionTime val) {
        poll_interval = val;
    }

    @JSOptionDefinition(name = "Waiting_for_Late_comers", description = "Wait an additional interval for late comers", key = "Waiting_for_Late_comers", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean WaitingForLateComers = new SOSOptionBoolean(this, className + ".Waiting_for_Late_comers",
            "Wait an additional interval for late comers", "false", "false", false);

    public String getWaiting_for_Late_comers() {
        return WaitingForLateComers.Value();
    }

    public SOSFtpOptionsSuperClass setWaiting_for_Late_comers(final String val) {
        WaitingForLateComers.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "poll_minfiles", description = "This parameter specifies the number of files tha", key = "poll_minfiles", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger poll_minfiles = new SOSOptionInteger(this, className + ".poll_minfiles",
            "This parameter specifies the number of files tha", "0", "0", false);

    @Override
    public SOSOptionInteger getpoll_minfiles() {
        return poll_minfiles;
    }

    @Override
    public void setpoll_minfiles(final SOSOptionInteger val) {
        poll_minfiles = val;
    }

    @JSOptionDefinition(name = "PollingDuration", description = "The duration of the polling period", key = "PollingDuration", type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime PollingDuration = new SOSOptionTime(this, className + ".PollingDuration", "The duration of the polling period", "0", "0",
            false);

    public String getPollingDuration() {
        return PollingDuration.Value();
    }

    public SOSFtpOptionsSuperClass setPollingDuration(final String val) {
        PollingDuration.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "poll_timeout", description = "This parameter specifies the time in minutes, how", key = "poll_timeout", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger poll_timeout = new SOSOptionInteger(this, className + ".poll_timeout",
            "This parameter specifies the time in minutes, how", "0", "0", false);

    @Override
    public SOSOptionInteger getpoll_timeout() {
        return poll_timeout;
    }

    @Override
    public void setpoll_timeout(final SOSOptionInteger val) {
        poll_timeout = val;
    }

    @JSOptionDefinition(name = "port", description = "Port-Number to be used for Data-Transfer", key = "port", type = "SOSOptionPortNumber", mandatory = true)
    public SOSOptionPortNumber port =
            new SOSOptionPortNumber(this, className + ".port", "Port-Number to be used for Data-Transfer", "21", "21", true);

    @Override
    public SOSOptionPortNumber getport() {
        return port;
    }

    @Override
    public void setport(final SOSOptionPortNumber val) {
        port = val;
    }

    @JSOptionDefinition(name = "ppid", description = "This parameter is used for Unix systems and - as o", key = "ppid", type = "SOSOptionProcessID", mandatory = false)
    public SOSOptionProcessID ppid = new SOSOptionProcessID(this, className + ".ppid", "This parameter is used for Unix systems and - as o", "", "",
            false);

    @Override
    public SOSOptionProcessID getppid() {
        return ppid;
    }

    @Override
    public void setppid(final SOSOptionProcessID val) {
        ppid = val;
    }

    public SOSOptionProcessID ParentProcessID = (SOSOptionProcessID) ppid.SetAlias(className + ".ParentProcessID");

    @JSOptionDefinition(name = "profile", description = "The Name of a Profile-Section to be executed", key = "profile", type = "SOSOptionString", mandatory = false)
    public SOSOptionString profile = new SOSOptionString(this, className + ".profile", "The Name of a Profile-Section to be executed", "", "", false);

    @Override
    public SOSOptionString getprofile() {
        return profile;
    }

    @Override
    public void setprofile(final SOSOptionString val) {
        profile = val;
    }

    public SOSOptionString SectionName = (SOSOptionString) profile.SetAlias(className + ".SectionName");

    @JSOptionDefinition(name = "protocol", description = "Type of requested Datatransfer The values ftp, sftp", key = "protocol", type = "SOSOptionStringValueList", mandatory = true)
    public SOSOptionTransferType protocol = new SOSOptionTransferType(this, className + ".protocol",
            "Type of requested Datatransfer The values ftp, sftp", "ftp", "ftp", true);

    @Override
    public SOSOptionTransferType getprotocol() {
        return protocol;
    }

    @Override
    public void setprotocol(final SOSOptionTransferType val) {
        protocol = val;
    }

    public SOSOptionTransferType TransferProtocol = (SOSOptionTransferType) protocol.SetAlias(className + ".TransferProtocol");

    @JSOptionDefinition(name = "recursive", description = "This parameter specifies if files from subdirector", key = "recursive", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean recursive = new SOSOptionBoolean(this, className + ".recursive", "This parameter specifies if files from subdirector",
            "false", "false", false);

    public SOSOptionBoolean IncludeSubdirectories = (SOSOptionBoolean) recursive.SetAlias("include_sub_directories");

    @Override
    public SOSOptionBoolean getrecursive() {
        return recursive;
    }

    @Override
    public void setrecursive(final SOSOptionBoolean val) {
        recursive = val;
    }

    public SOSOptionBoolean RecurseSubFolders = (SOSOptionBoolean) recursive.SetAlias(className + ".RecurseSubFolders");

    @JSOptionDefinition(name = "remote_dir", description = "remote_dir Directory at the FTP/SFTP server from wh", key = "remote_dir", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName remote_dir = new SOSOptionFolderName(this, className + ".remote_dir",
            "remote_dir Directory at the FTP/SFTP server from wh", ".", ".", false);

    @Override
    public SOSOptionFolderName getremote_dir() {
        return remote_dir;
    }

    @Override
    public void setremote_dir(final SOSOptionFolderName val) {
        remote_dir = val;
    }

    @JSOptionDefinition(name = "remove_files", description = "This parameter specifies whether files on the FTP/", key = "remove_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean remove_files = new SOSOptionBoolean(this, className + ".remove_files",
            "This parameter specifies whether files on the FTP/", "false", "false", false);

    public SOSOptionBoolean DeleteFilesAfterTransfer = (SOSOptionBoolean) remove_files.SetAlias(className + ".DeleteFilesAfterTransfer");

    public SOSOptionBoolean DeleteFilesOnSource = (SOSOptionBoolean) remove_files.SetAlias(className + ".DeleteFilesOnSource");

    @Override
    public SOSOptionBoolean getremove_files() {
        return remove_files;
    }

    @Override
    public void setremove_files(final SOSOptionBoolean val) {
        remove_files = val;
    }

    @JSOptionDefinition(name = "replacement", description = "String for replacement of matching character seque", key = "replacement", type = "SOSOptionString", mandatory = false)
    public SOSOptionString replacement = new SOSOptionString(this, className + ".replacement", "String for replacement of matching character seque",
            null, null, false);

    @Override
    public SOSOptionString getreplacement() {
        return replacement;
    }

    @Override
    public void setreplacement(final SOSOptionString val) {
        replacement = val;
    }

    public SOSOptionString ReplaceWith = (SOSOptionString) replacement.SetAlias(className + ".ReplaceWith");

    @JSOptionDefinition(name = "replacing", description = "Regular expression for filename replacement with", key = "replacing", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replacing = new SOSOptionRegExp(this, className + ".replacing", "Regular expression for filename replacement with", "",
            "", false);

    @Override
    public SOSOptionRegExp getreplacing() {
        return replacing;
    }

    @Override
    public void setreplacing(final SOSOptionRegExp val) {
        replacing = val;
    }

    public SOSOptionRegExp ReplaceWhat = (SOSOptionRegExp) replacing.SetAlias(className + ".ReplaceWhat");

    @JSOptionDefinition(name = "root", description = "The parameter specifies the directory in which thi", key = "root", type = "SOSOptionFolderName", mandatory = false)
    public SOSOptionFolderName root = new SOSOptionFolderName(this, className + ".root", "The parameter specifies the directory in which thi", "",
            "", false);

    @Override
    public SOSOptionFolderName getroot() {
        return root;
    }

    @Override
    public void setroot(final SOSOptionFolderName val) {
        root = val;
    }

    public SOSOptionFolderName TempFolderName = (SOSOptionFolderName) root.SetAlias(className + ".TempFolderName");

    @JSOptionDefinition(name = "scheduler_host", description = "This parameter specifies the host name or IP addre", key = "scheduler_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionHostName scheduler_host = new SOSOptionHostName(this, className + ".scheduler_host",
            "This parameter specifies the host name or IP addre", "", "", false);

    public SOSOptionHostName BackgroundServiceHost = (SOSOptionHostName) scheduler_host.SetAlias("Background_Service_Host");

    @Override
    public SOSOptionHostName getscheduler_host() {
        return scheduler_host;
    }

    @Override
    public void setscheduler_host(final SOSOptionHostName val) {
        scheduler_host = val;
    }

    @JSOptionDefinition(name = "scheduler_job_chain", description = "The name of a job chain for Managed File Transfer", key = "scheduler_job_chain", type = "JSJobChain", mandatory = false)
    public JSJobChain scheduler_job_chain = new JSJobChain(this, className + ".scheduler_job_chain",
            "The name of a job chain for Background Service", "/sos/jade/jade_history", "/sos/jade/jade_history", false);

    public JSJobChain BackgroundServiceJobChainName = (JSJobChain) scheduler_job_chain.SetAlias("BackgroundService_Job_Chain_Name");

    @Override
    public JSJobChain getscheduler_job_chain() {
        return scheduler_job_chain;
    }

    @Override
    public void setscheduler_job_chain(final JSJobChain val) {
        scheduler_job_chain = val;
    }

    @JSOptionDefinition(name = "scheduler_port", description = "The port for which a Job Scheduler for Managed File Trans", key = "scheduler_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionPortNumber scheduler_port = new SOSOptionPortNumber(this, className + ".scheduler_port",
            "The port for which a Job Scheduler for Managed File Trans", "0", "4444", false);

    public SOSOptionPortNumber BackgroundServicePort = (SOSOptionPortNumber) scheduler_port.SetAlias("Background_Service_Port",
            "Background_Service_PortNumber");

    @Override
    public SOSOptionPortNumber getscheduler_port() {
        return scheduler_port;
    }

    @Override
    public void setscheduler_port(final SOSOptionPortNumber val) {
        scheduler_port = val;
    }

    @JSOptionDefinition(name = "Restart", description = "Set Restart/Resume Mode for Transfer", key = "Restart", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean Restart = new SOSOptionBoolean(this, className + ".Restart", "Set Restart/Resume Mode for Transfer", "false", "false",
            false);

    public SOSOptionBoolean ResumeTransfer = (SOSOptionBoolean) Restart.SetAlias(className + "Resume", className + "Resume_Transfer");

    public String getRestart() {
        return Restart.Value();
    }

    public SOSFtpOptionsSuperClass setRestart(final String val) {
        Restart.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "settings", description = "Name of INI-File which contains the transfer profiles to execute", key = "settings", type = "SOSOptionIniFileName", mandatory = false)
    public SOSOptionIniFileName settings = new SOSOptionIniFileName(this, className + ".settings",
            "Name of INI-File which contains the transfer profiles to execute", "", "", false);

    public SOSOptionIniFileName ConfigurationFile = (SOSOptionIniFileName) settings.SetAlias("JADE_Configuration_File", "JADE_Config_File",
            "Configuration", "JADE_Configuration", "JADE_INI_FILE");

    public SOSOptionIniFileName SOSIniFileName = (SOSOptionIniFileName) settings.SetAlias(className + ".SOSIniFileName");

    @Override
    public SOSOptionIniFileName getsettings() {
        return settings;
    }

    @Override
    public void setsettings(final SOSOptionIniFileName val) {
        settings = val;
    }

    @JSOptionDefinition(name = "skip_transfer", description = "If this Parameter is set to true then", key = "skip_transfer", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean skip_transfer = new SOSOptionBoolean(this, className + ".skip_transfer", "If this Parameter is set to true then",
            "false", "false", false);

    @Override
    public SOSOptionBoolean getskip_transfer() {
        return skip_transfer;
    }

    @Override
    public void setskip_transfer(final SOSOptionBoolean val) {
        skip_transfer = val;
    }

    @JSOptionDefinition(name = "ssh_auth_file", description = "This parameter specifies the path and name of a us", key = "ssh_auth_file", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName ssh_auth_file = new SOSOptionInFileName(this, className + ".ssh_auth_file",
            "This parameter specifies the path and name of a us", "", "", false);

    public SOSOptionInFileName auth_file = (SOSOptionInFileName) ssh_auth_file.SetAlias(className + ".auth_file");

    @Override
    public SOSOptionInFileName getssh_auth_file() {
        return ssh_auth_file;
    }

    @Override
    public void setssh_auth_file(final SOSOptionInFileName val) {
        ssh_auth_file = val;
    }

    @JSOptionDefinition(name = "ssh_auth_method", description = "This parameter specifies the authentication method", key = "ssh_auth_method", type = "SOSOptionStringValueList", mandatory = false)
    public SOSOptionAuthenticationMethod ssh_auth_method = new SOSOptionAuthenticationMethod(this, className + ".ssh_auth_method",
            "This parameter specifies the authentication method", "publickey", "publickey", false);

    public SOSOptionAuthenticationMethod auth_method = (SOSOptionAuthenticationMethod) ssh_auth_method.SetAlias(className + ".auth_method");

    @Override
    public SOSOptionAuthenticationMethod getssh_auth_method() {
        return ssh_auth_method;
    }

    @Override
    public void setssh_auth_method(final SOSOptionAuthenticationMethod val) {
        ssh_auth_method = val;
    }

    @JSOptionDefinition(name = "ssh_proxy_host", description = "The value of this parameter is the host name or th", key = "ssh_proxy_host", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_host = new SOSOptionString(this, className + ".ssh_proxy_host",
            "The value of this parameter is the host name or th", "", "", false);

    @Override
    public SOSOptionString getssh_proxy_host() {
        return ssh_proxy_host;
    }

    @Override
    public void setssh_proxy_host(final SOSOptionString val) {
        ssh_proxy_host = val;
    }

    @JSOptionDefinition(name = "ssh_proxy_password", description = "This parameter specifies the password for the prox", key = "ssh_proxy_password", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_password = new SOSOptionString(this, className + ".ssh_proxy_password",
            "This parameter specifies the password for the prox", "", "", false);

    @Override
    public SOSOptionString getssh_proxy_password() {
        return ssh_proxy_password;
    }

    @Override
    public void setssh_proxy_password(final SOSOptionString val) {
        ssh_proxy_password = val;
    }

    @JSOptionDefinition(name = "ssh_proxy_port", description = "This parameter specifies the port number of the pr", key = "ssh_proxy_port", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_port = new SOSOptionString(this, className + ".ssh_proxy_port",
            "This parameter specifies the port number of the pr", "", "", false);

    @Override
    public SOSOptionString getssh_proxy_port() {
        return ssh_proxy_port;
    }

    @Override
    public void setssh_proxy_port(final SOSOptionString val) {
        ssh_proxy_port = val;
    }

    @JSOptionDefinition(name = "ssh_proxy_user", description = "The value of this parameter specifies the user acc", key = "ssh_proxy_user", type = "SOSOptionString", mandatory = false)
    public SOSOptionString ssh_proxy_user = new SOSOptionString(this, className + ".ssh_proxy_user",
            "The value of this parameter specifies the user acc", "", "", false);

    @Override
    public SOSOptionString getssh_proxy_user() {
        return ssh_proxy_user;
    }

    @Override
    public void setssh_proxy_user(final SOSOptionString val) {
        ssh_proxy_user = val;
    }

    @JSOptionDefinition(name = "transactional", description = "This parameter specifies if file transfers should", key = "transactional", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean transactional = new SOSOptionBoolean(this, className + ".transactional",
            "This parameter specifies if file transfers should", "false", "false", false);

    @Override
    public SOSOptionBoolean gettransactional() {
        return transactional;
    }

    @Override
    public void settransactional(final SOSOptionBoolean val) {
        transactional = val;
    }

    public SOSOptionBoolean TransactionMode = (SOSOptionBoolean) transactional.SetAlias(className + ".TransactionMode");

    @JSOptionDefinition(name = "transfer_mode", description = "Type of Character-Encoding Transfe", key = "transfer_mode", type = "SOSOptionTransferMode", mandatory = false)
    public SOSOptionTransferMode transfer_mode = new SOSOptionTransferMode(this, className + ".transfer_mode", "Type of Character-Encoding Transfe",
            "binary", "binary", false);

    @Override
    public SOSOptionTransferMode gettransfer_mode() {
        return transfer_mode;
    }

    @Override
    public void settransfer_mode(final SOSOptionTransferMode val) {
        transfer_mode = val;
    }

    @JSOptionDefinition(name = "user", description = "UserID of user in charge User name", key = "user", type = "SOSOptionUserName", mandatory = true)
    public SOSOptionUserName user = new SOSOptionUserName(this, className + ".user", "UserID of user in charge User name", "", "anonymous", false);

    @Override
    public SOSOptionUserName getuser() {
        return user;
    }

    @Override
    public void setuser(final SOSOptionUserName val) {
        user = val;
    }

    @JSOptionDefinition(name = "verbose", description = "The granuality of (Debug-)Messages The verbosit", key = "verbose", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger verbose = new SOSOptionInteger(this, className + ".verbose", "The granuality of (Debug-)Messages The verbosit", "1",
            "10", false);

    @Override
    public SOSOptionInteger getverbose() {
        return verbose;
    }

    @Override
    public void setverbose(final SOSOptionInteger val) {
        verbose = val;
    }

    public SOSOptionInteger VerbosityLevel = (SOSOptionInteger) verbose.SetAlias(className + ".VerbosityLevel");

    @JSOptionDefinition(name = "zero_byte_transfer", description = "This parameter specifies whether zero byte files", key = "zero_byte_transfer", type = "SOSOptionZeroByteTransfer", mandatory = false)
    public SOSOptionZeroByteTransfer zero_byte_transfer = new SOSOptionZeroByteTransfer(this, className + ".zero_byte_transfer",
            "This parameter specifies whether zero byte files", "yes", "yes", false);

    public SOSOptionZeroByteTransfer TransferZeroByteFiles = (SOSOptionZeroByteTransfer) zero_byte_transfer.SetAlias("transfer_zero_byte_files");

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

    @SuppressWarnings("unused")
    private String getAllOptionsAsString() {
        String str = className + "\n";
        str += this.toString();
        return str;
    }

    public void setAllOptions(final Properties pobjProperties) {
        HashMap<String, String> map = new HashMap<String, String>((Map) pobjProperties);
        try {
            super.setAllOptions(map);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> settings) {
        flgSetAllOptions = true;
        objSettings = settings;
        super.Settings(objSettings);
        super.setAllOptions(settings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(final String[] args) {
        super.CommandLineArgs(args);
        this.setAllOptions(super.objSettings);
    }

    @Override
    public SOSOptionHostName getHost() {
        return host;
    }

    @Override
    public SOSOptionPortNumber getPort() {
        return port;
    }

    @Override
    public SOSOptionString getProxy_host() {
        return null;
    }

    @Override
    public SOSOptionPassword getProxy_password() {
        return null;
    }

    @Override
    public SOSOptionPortNumber getProxy_port() {
        return null;
    }

    @Override
    public SOSOptionUserName getProxy_user() {
        return null;
    }

    @Override
    public void setHost(final SOSOptionHostName val) {
        this.sethost(val);
    }

    @Override
    public void setPort(final SOSOptionPortNumber val) {

    }

    @Override
    public void setProxy_host(final SOSOptionString val) {

    }

    @Override
    public void setProxy_password(final SOSOptionPassword val) {

    }

    @Override
    public void setProxy_port(final SOSOptionPortNumber val) {

    }

    @Override
    public void setProxy_user(final SOSOptionUserName val) {

    }

    @Override
    public SOSOptionInFileName getAuth_file() {
        return ssh_auth_file;
    }

    @Override
    public SOSOptionAuthenticationMethod getAuth_method() {
        return ssh_auth_method;
    }

    @Override
    public SOSOptionPassword getPassword() {
        return password;
    }

    @Override
    public SOSOptionUserName getUser() {
        return user;
    }

    @Override
    public void setAuth_file(final SOSOptionInFileName val) {
        ssh_auth_file = val;
    }

    @Override
    public void setAuth_method(final SOSOptionAuthenticationMethod val) {

    }

    @Override
    public void setPassword(final SOSOptionPassword val) {

    }

    @Override
    public void setUser(final SOSOptionUserName val) {
        user.Value(val.Value());
    }

    @Override
    public SOSOptionRegExp getfile_spec2() {
        return null;
    }

    @Override
    public void setfile_spec2(final SOSOptionRegExp val) {

    }

    @Override
    public SOSOptionFolderName SourceDir() {
        return SourceDir;
    }

    @Override
    public SOSOptionFolderName TargetDir() {
        return TargetDir;
    }

    @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean raise_exception_on_error = new SOSOptionBoolean(this, className + ".raise_exception_on_error",
            "Raise an Exception if an error occured", "true", "true", true);

    public SOSOptionBoolean getraise_exception_on_error() {
        return raise_exception_on_error;
    }

    public void setraise_exception_on_error(final SOSOptionBoolean val) {
        this.raise_exception_on_error = val;
    }

    @JSOptionDefinition(name = "ProtocolCommandListener", description = "Activate the logging for Apache ftp client", key = "protocol_command_listener", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean ProtocolCommandListener = new SOSOptionBoolean(this, className + ".protocol_command_listener",
            "Activate the logging for Apache ftp client", "false", "false", true);

    @JSOptionDefinition(name = "system_property_files", description = "List of the java property files separated by semicolon", key = "system_property_files", type = "SOSOptionString", mandatory = false)
    public SOSOptionString system_property_files = new SOSOptionString(this, className + ".system_property_files",
            "List of the java property files separated by semicolon", "", "", false);

    @JSOptionDefinition(name = "updateConfiguration", description = "determines if a YADE configuration should be updated with the given XML snippet", key = "updateConfiguration", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean updateConfiguration = new SOSOptionBoolean(this, className + ".updateConfiguration",
            "determines if a YADE configuration should be updated with the given XML snippet", "", "", false);

    public SOSOptionBoolean getUpdateConfiguration() {
        return updateConfiguration;
    }

    public void setUpdateConfiguration(SOSOptionBoolean updateConfiguration) {
        this.updateConfiguration = updateConfiguration;
    }

    @JSOptionDefinition(name = "xmlUpdate", description = "the XML configuration snippet to update a YADE configuration with", key = "xmlUpdate", type = "SOSOptionString", mandatory = false)
    public SOSOptionString xmlUpdate = new SOSOptionString(this, className + ".xmlUpdate",
            "the XML configuration snippet to update a YADE configuration with", "", "", false);

    public SOSOptionString getXmlUpdate() {
        return xmlUpdate;
    }

    public void setXmlUpdate(SOSOptionString xmlUpdate) {
        this.xmlUpdate = xmlUpdate;
    }

}