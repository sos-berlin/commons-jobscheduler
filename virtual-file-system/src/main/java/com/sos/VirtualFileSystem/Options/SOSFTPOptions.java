package com.sos.VirtualFileSystem.Options;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;

import sos.configuration.SOSConfiguration;
import sos.net.mail.options.SOSSmtpMailOptions;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSFTPOptions extends SOSFtpOptionsSuperClass {

    private static final long serialVersionUID = -8219289268940238015L;
    private static final String OPERATION_SEND = "send";
    private static final String OPERATION_RECEIVE = "receive";
    private static final Logger LOGGER = Logger.getLogger(VFSFactory.getLoggerName());
    private Properties environmentVars = null;
    private Properties allEnvironmentVars = null;
    private Properties schedulerParams = null;
    private boolean checkMandatoryDone = false;
    private boolean readSettingsFileIsActive = false;
    private boolean settingsFileProcessed = false;
    private Map<String, String> dmzOptions = new HashMap<String, String>();
    @JSOptionClass(description = "connectionOptions", name = "SOSConnection2Options")
    private SOSConnection2Options connectionOptions;
    @JSOptionClass(description = "mailOptions", name = "mailOptions")
    private SOSSmtpMailOptions mailOptions;
    public static final String conURIPrefixFILE = "file://";
    public static final String conSchedulerEnvVarPrefix = "scheduler_param_";
    public static final String conSOSFtpEnvVarPrefix = "sosftp_";
    public static final String conSystemPropertyFILE_SEPARATOR = "file.separator";
    public boolean flgCumulativeTargetDeleted = false;

    private final Map<String, String> includeDirectives = new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;
        {
            put("include", "");
            put("source_include", "source_");
            put("target_include", "target_");
            put("jump_include", "jump_");
            put("alternate_source_include", "alternative_source_");
            put("alternate_target_include", "alternative_target_");
            put("source_alternate_include", "alternative_source_");
            put("target_alternate_include", "alternative_target_");
            put("alternative_source_include", "alternative_source_");
            put("alternative_target_include", "alternative_target_");
            put("source_alternative_include", "alternative_source_");
            put("target_alternative_include", "alternative_target_");
            // used by SOSFTP?
            put("alternate_include", "alternate_");
            put("alternatesource_include", "alternatesource_");
            put("alternatetarget_include", "alternatetarget_");
        }
    };

    public SOSFTPOptions(final SOSOptionTransferType.enuTransferTypes transferType) {
        super();
        switch (transferType) {
        case webdav:
            auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Source().auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Target().auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            this.Source().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            this.Target().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Source().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Target().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case http:
            auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Source().auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Target().auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.Text(), SOSOptionTransferType.enuTransferTypes.http.Text());
            this.Source().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.Text(), SOSOptionTransferType.enuTransferTypes.http.Text());
            this.Target().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.Text(), SOSOptionTransferType.enuTransferTypes.http.Text());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Source().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Target().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
    }

    public SOSFTPOptions(final SOSOptionTransferType.enuTransferTypes transferTypeSource,
            final SOSOptionTransferType.enuTransferTypes transferTypeTarget) {
        super();
        switch (transferTypeSource) {
        case webdav:
            auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
        this.changeDefaults(transferTypeSource, this.Source());
        this.changeDefaults(transferTypeTarget, this.Target());
    }

    private void changeDefaults(final SOSOptionTransferType.enuTransferTypes transferType, final SOSConnection2OptionsAlternate options) {
        switch (transferType) {
        case webdav:
            options.auth_method.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            options.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case local:
        case file:
            options.auth_method.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.local.Text(), SOSOptionTransferType.enuTransferTypes.local.Text());
            options.port.changeDefaults(0, 0);
            break;
        case ftp:
            options.auth_method.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.ftp.Text(), SOSOptionTransferType.enuTransferTypes.ftp.Text());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4FTP, SOSOptionPortNumber.conPort4FTP);
            break;
        case sftp:
            options.auth_method.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.sftp.Text(), SOSOptionTransferType.enuTransferTypes.sftp.Text());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4SFTP, SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftps:
            options.auth_method.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            options.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.ftps.Text(), SOSOptionTransferType.enuTransferTypes.ftps.Text());
            options.port.changeDefaults(SOSOptionPortNumber.conPort4FTPS, SOSOptionPortNumber.conPort4FTPS);
            break;
        default:
            break;
        }
    }

    public SOSFTPOptions() {
        super();
    }

    public SOSSmtpMailOptions getMailOptions() {
        if (mailOptions == null) {
            mailOptions = new SOSSmtpMailOptions();
        }
        return mailOptions;
    }

    @Deprecated
    public SOSFTPOptions(final JSListener listener) {
        super(listener);
    }

    public SOSFTPOptions(final HashMap<String, String> settings) throws Exception {
        super(settings);
    }

    public void setChildClasses(final Properties properties) {
        HashMap<String, String> map = new HashMap<String, String>((Map) properties);
        try {
            this.setChildClasses(map);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void setChildClasses(final HashMap<String, String> settings) {
        try {
            if (connectionOptions == null) {
                connectionOptions = new SOSConnection2Options(settings);
                mailOptions = new SOSSmtpMailOptions(settings);
            } else {
                connectionOptions.setPrefixedValues(settings);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void adjustDefaults() {
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.move.Text())) {
            remove_files.value(true);
            remove_files.setProtected(operation.isProtected());
        }
        if (TransactionMode.isTrue()) {
            if (!isAtomicTransfer()) {
                atomic_suffix.Value("~");
            }
        }
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.getlist.Text())) {
            remove_files.value(false);
        }
        String localDir = local_dir.Value();
        if (isEmpty(localDir)) {
            local_dir.Set(SourceDir);
            localDir = local_dir.Value();
        }
        checkReplaceAndReplacing(Target());
        checkReplaceAndReplacing(Source());
        if (replacing.IsNotEmpty() && replacement.IsNull()) {
            replacement.Value("");
        }
        if (this.Target().protocol.isDirty()) {
            if (!protocol.isDirty()) {
                protocol.Set(this.Target().protocol);
            }
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(this.Source().protocol, this.Source().port, this.Source().host);
        setDefaultHostPort(this.Target().protocol, this.Target().port, this.Target().host);
        setDefaultHostPort(this.Source().Alternatives().protocol, this.Source().Alternatives().port, 
                this.Source().Alternatives().host);
        setDefaultHostPort(this.Target().Alternatives().protocol, this.Target().Alternatives().port, 
                this.Target().Alternatives().host);
        getDataSourceType();
        getDataTargetType();
    }

    @Override
    public void CheckMandatory() {
        if (checkMandatoryDone) {
            return;
        }
        operation.CheckMandatory();
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.move.Text())) {
            remove_files.value(true);
        }
        if (TransactionMode.isTrue()) {
            if (!isAtomicTransfer()) {
                atomic_suffix.Value("~");
            }
        }
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.getlist.Text())) {
            remove_files.setFalse();
        }
        checkURLParameter(this.getConnectionOptions().Source());
        checkURLParameter(this.getConnectionOptions().Target());
        String localDir = local_dir.Value();
        if (isEmpty(localDir)) {
            local_dir.Value(SourceDir.Value());
            localDir = local_dir.Value();
            localDir += "";
        }
        if (Source().url.isDirty() && SourceDir.isNotDirty()) {
            SourceDir.Value(Source().url.getFolderName());
        }
        if (Target().url.isDirty() && TargetDir.isNotDirty()) {
            TargetDir.Value(Target().url.getFolderName());
        }
        checkCredentialStore(Source());
        checkCredentialStore(Target());
        if (Source().replacing.IsNotEmpty() && Source().replacement.IsNotEmpty()) {
            remove_files.setFalse();
        }
        super.CheckMandatory();
        if (localDir.startsWith("\\\\")) {
            while (localDir.indexOf("\\") != -1) {
                localDir = localDir.replace('\\', '/');
            }
        }
        local_dir.Value(localDir);
        if (localDir.startsWith(conURIPrefixFILE)) {
            if (!new File(createURI(localDir)).exists()) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0010.params(localDir));
            }
        }
        checkReplaceAndReplacing(Target());
        checkReplaceAndReplacing(Source());
        if (replacing.IsNotEmpty() && replacement.IsNull()) {
            replacement.Value("");
        }
        if (replacing.IsEmpty() && replacement.IsNotEmpty()) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0020.params(replacement.getKey(), replacing.getKey()));
        }
        if (append_files.value()) {
            String appendFilesKey = append_files.getKey();
            if (isAtomicTransfer()) {
                String msg = getOptionNamesAsString(new SOSOptionElement[] { atomic_prefix, atomic_suffix });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0050.params(appendFilesKey, msg));
            }
            if (compress_files.value()) {
                String msg = getOptionNamesAsString(new SOSOptionElement[] { compress_files });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0050.params(appendFilesKey, msg));
            }
            if (compress_files.value()) {
                String msg = getOptionNamesAsString(new SOSOptionElement[] { append_files, compress_files });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0030.params(msg));
            }
            if (!"ftp".equalsIgnoreCase(protocol.Value())) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0040.params(appendFilesKey, protocol.Value()));
            }
        }
        if (this.Target().protocol.isDirty()) {
            if (protocol.isDirty()) {
                protocol.Value(this.Target().protocol.Value());
            }
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(this.Source().protocol, this.Source().port, this.Source().host);
        setDefaultHostPort(this.Target().protocol, this.Target().port, this.Target().host);
        setDefaultHostPort(this.Source().Alternatives().protocol, this.Source().Alternatives().port, this.Source().Alternatives().host);
        setDefaultHostPort(this.Target().Alternatives().protocol, this.Target().Alternatives().port, this.Target().Alternatives().host);
        setDefaultAuth(this.Source().protocol, this.Source());
        setDefaultAuth(this.Target().protocol, this.Target());
        setDefaultAuth(this.Source().Alternatives().protocol, this.Source().Alternatives());
        setDefaultAuth(this.Target().Alternatives().protocol, this.Target().Alternatives());
        if (file_path.isDirty()) {
            if (file_spec.isDirty()) {
                file_path.Value("");
            }
        }
        if (file_path.IsEmpty() && SourceDir.IsEmpty() && this.Source().Directory.IsEmpty() && FileListName.IsEmpty()) {
            throw new JobSchedulerException(String.format("SOSVfs-E-0000: one of these parameters must be specified: '%1$s', '%2$s', '%3$s'", file_path.getShortKey(), "source_dir", FileListName.getShortKey()));
        }
        if (ProtocolCommandListener.isDirty()) {
            if (Source().ProtocolCommandListener.isNotDirty()) {
                Source().ProtocolCommandListener.value(ProtocolCommandListener.value());
            }
            if (Target().ProtocolCommandListener.isNotDirty()) {
                Target().ProtocolCommandListener.value(ProtocolCommandListener.value());
            }
        }
        getDataSourceType();
        getDataTargetType();
        if (CheckNotProcessedOptions.value()) {
            this.CheckNotProcessedOptions();
        }
        checkMandatoryDone = true;
    }

    private void checkReplaceAndReplacing(final SOSConnection2OptionsSuperClass options) {
        if (options.replacing.IsNotEmpty() && options.replacement.IsNull()) {
            options.replacement.Value("");
        }
        if (options.replacing.IsEmpty() && options.replacement.IsNotEmpty()) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0020.params(options.replacement.getKey(), options.replacing.getKey()));
        }
    }

    private void checkCredentialStore(final SOSConnection2OptionsAlternate options) {
        if (options.getCredentialStore() != null) {
            options.checkCredentialStoreOptions();
        }
    }

    private void checkURLParameter(final SOSConnection2OptionsAlternate options) {
        if (options.url.isDirty()) {
            options.url.getOptions(options);
        }
    }

    private void setDefaultAuth(final SOSOptionTransferType optionTransferType, final SOSConnection2OptionsAlternate options) {
        enuTransferTypes transferType = optionTransferType.getEnum();
        if (transferType == enuTransferTypes.http || transferType == enuTransferTypes.https || transferType == enuTransferTypes.webdav) {
            if (!options.auth_method.isDirty() && !options.ssh_auth_method.isDirty()) {
                options.auth_method.Value(enuAuthenticationMethods.url);
                options.ssh_auth_method.Value(enuAuthenticationMethods.url);
            }
        }
    }

    private void setDefaultHostPort(final SOSOptionTransferType optionTransferType, final SOSOptionPortNumber optionPort,
            final SOSOptionHostName optionHost) {
        enuTransferTypes transferType = optionTransferType.getEnum();
        switch (transferType) {
        case sftp:
            optionPort.DefaultValue("" + SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftp:
            optionPort.DefaultValue("" + SOSOptionPortNumber.conPort4FTP);
            break;
        case zip:
        case file:
        case local:
            optionPort.DefaultValue("0");
            if (optionHost.isNotDirty() || "localhost".equalsIgnoreCase(optionHost.Value()) || "127.0.0.1".equalsIgnoreCase(optionHost.Value())) {
                optionHost.Value(SOSOptionHostName.getLocalHost());
            }
            break;
        case ftps:
            optionPort.DefaultValue("" + SOSOptionPortNumber.conPort4FTPS);
            break;
        case webdav:
        case http:
        case https:
            if (optionHost.Value().toLowerCase().startsWith("https://")) {
                optionPort.DefaultValue("" + SOSOptionPortNumber.conPort4https);
            } else {
                optionPort.DefaultValue("" + SOSOptionPortNumber.conPort4http);
            }
            break;
        default:
            break;
        }
        if (optionPort.isNotDirty()) {
            optionPort.Value(optionPort.DefaultValue());
            optionPort.setNotDirty();
            optionPort.setProtected(optionTransferType.isProtected());
        }
    }

    private boolean isSourceDirSpecified() {
        boolean result = false;
        if (local_dir.IsEmpty() && SourceDir.IsEmpty() && Source().FolderName.IsEmpty()) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    private String getOptionNamesAsString(final SOSOptionElement[] objA) {
        String name = "";
        for (SOSOptionElement sosOptionElement : objA) {
            if (!name.isEmpty()) {
                name += ", ";
            }
            name += sosOptionElement.getKey() + "=" + sosOptionElement.Value();
        }
        return name;
    }

    @Override
    public boolean isAtomicTransfer() {
        return atomic_prefix.IsNotEmpty() || atomic_suffix.IsNotEmpty();
    }

    private Properties getEnvVars() throws Exception {
        try {
            environmentVars = new Properties();
            schedulerParams = new Properties();
            int envVarPrefixLen = conSOSFtpEnvVarPrefix.length();
            Map<String, String> systemEnv = System.getenv();
            allEnvironmentVars = new Properties();
            allEnvironmentVars.putAll(systemEnv);
            for (Object k : allEnvironmentVars.keySet()) {
                String key = (String) k;
                String value = (String) allEnvironmentVars.get(k);
                if (key.startsWith(conSOSFtpEnvVarPrefix)) {
                    key = key.substring(envVarPrefixLen);
                    environmentVars.setProperty(key, value);
                    continue;
                }
                if (key.startsWith("current_pid") || key.startsWith("ppid")) {
                    environmentVars.setProperty(key, value);
                    continue;
                }
                if (key.indexOf(conSchedulerEnvVarPrefix) > -1) {
                    schedulerParams.setProperty(key.substring(conSchedulerEnvVarPrefix.length()), value);
                    continue;
                }
            }
            return environmentVars;
        } catch (Exception e) {
            String msg = SOSVfsMessageCodes.SOSVfs_E_161.params("reading environment", e.toString());
            LOGGER.error(msg, e);
            throw new JobSchedulerException(msg, e);
        }
    }

    @Override
    public void CommandLineArgs(final String[] args) {
        super.CommandLineArgs(args);
        this.setAllOptions(super.objSettings);
        boolean found = false;
        for (int i = 0; i < args.length; i++) {
            String param = args[i];
            if (param.toLowerCase().startsWith("-settings")) {
                args[i] = "-ignored=ignored";
                found = true;
            }
        }
        if (found) {
            super.CommandLineArgs(args);
        }
    }

    @Override
    public void CommandLineArgs(final String args) {
        try {
            this.CommandLineArgs(args.split(" "));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_153.params("command lines args"), e);
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> settings) {
        flgSetAllOptions = true;
        objSettings = settings;
        super.Settings(objSettings);
        super.setAllOptions(settings);
        flgSetAllOptions = false;
        HashMap<String, String> map = settings;
        if (!settingsFileProcessed && !readSettingsFileIsActive) {
            if (ConfigurationFile.IsNotEmpty()) {
                readSettingsFileIsActive = true;
                map = ReadSettingsFile();
                readSettingsFileIsActive = false;
                settingsFileProcessed = true;
            }
        }
        setChildClasses(map);
    }

    public void setAllOptions2(HashMap<String, String> params) {
        Map<String, String> map = new HashMap<String, String>();
        if (!settingsFileProcessed && !readSettingsFileIsActive) {
            if (params.containsKey("settings") && params.containsKey("profile")) {
                this.settings.Value(params.get("settings"));
                this.profile.Value(params.get("profile"));
                readSettingsFileIsActive = true;
                map = ReadSettingsFile(params);
                readSettingsFileIsActive = false;
                settingsFileProcessed = true;
            }
        }
        flgSetAllOptions = true;
        params.putAll(map);
        objSettings = params;
        super.Settings(params);
        super.setAllOptions(params);
        setChildClasses(params);
        flgSetAllOptions = false;
    }

    public HashMap<String, String> ReadSettingsFile() {
        return ReadSettingsFile(null);
    }

    public HashMap<String, String> ReadSettingsFile(Map<String, String> beatParams) {
        settings.CheckMandatory();
        profile.CheckMandatory();
        HashMap<String, String> map = new HashMap<String, String>();
        SOSStandardLogger sosLogger = null;
        SOSConfiguration conf = null;
        Properties properties = new Properties();
        try {
            sosLogger = new SOSStandardLogger(0);
            getEnvVars();
            conf = new SOSConfiguration(settings.Value(), profile.Value(), sosLogger);
            Properties profileProps = conf.getParameterAsProperties();
            if (profileProps.size() <= 0) {
                String msg = SOSVfsMessageCodes.SOSVfs_E_0060.params(profile.Value(), settings.Value());
                throw new JobSchedulerException(msg);
            }
            conf = new SOSConfiguration(settings.Value(), "globals", sosLogger);
            Properties globalsProps = conf.getParameterAsProperties();
            String globalsJavaPropertiyFiles = globalsProps.getProperty(system_property_files.getShortKey());
            globalsProps = resolveIncludes(globalsProps, sosLogger);
            properties.putAll(globalsProps);
            profileProps = resolveIncludes(profileProps, sosLogger);
            properties.putAll(profileProps);
            String currentJavaPropertyFiles = properties.getProperty(system_property_files.getShortKey());
            if (globalsJavaPropertiyFiles != null && currentJavaPropertyFiles != null && !globalsJavaPropertiyFiles.equals(currentJavaPropertyFiles)) {
                properties.put(system_property_files.getShortKey(), globalsJavaPropertiyFiles + ";" + currentJavaPropertyFiles);
            }
            properties.put("uuid", UUID.randomUUID().toString());
            properties.put("date", SOSOptionTime.getCurrentDateAsString());
            properties.put("time", SOSOptionTime.getCurrentTimeAsString("hh:mm:ss"));
            properties.put("local_user", System.getProperty("user.name"));
            Properties props4Substitute = new Properties();
            props4Substitute.put("profile", profile.Value());
            props4Substitute.put("settings", settings.Value());
            try {
                java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
                properties.put("localhost", localMachine.getHostName());
                properties.put("local_host_ip", localMachine.getHostAddress());
            } catch (Exception e) {
                LOGGER.debug(e.toString());
                properties.put("localhost", "localhost");
                properties.put("local_host_ip", "127.0.0.1");
            }
            for (Map.Entry<Object, Object> e : properties.entrySet()) {
                String key = (String) e.getKey();
                String value = (String) e.getValue();
                if (beatParams != null && beatParams.containsKey(key)) {
                    value = beatParams.get(key);
                }
                if (hasVariableToSubstitute(value) == true && gflgSubsituteVariables == true) {
                    LOGGER.trace("ReadSettingsFile() - key = " + key + ", value = " + value);
                    value = substituteVariables(value, properties);
                    value = substituteVariables(value, props4Substitute);
                    value = substituteVariables(value, environmentVars);
                    value = substituteVariables(value, allEnvironmentVars);
                    value = substituteVariables(value, schedulerParams);
                    if (hasVariableToSubstitute(value)) {
                        String msg = SOSVfsMessageCodes.SOSVfs_W_0070.params(value, key);
                        LOGGER.warn(msg);
                    }
                    value = unescape(value);
                }
                map.put(key, value);
            }
            super.setAllOptions(map);
            setChildClasses(map);
        } catch (JobSchedulerException e) {
            LOGGER.error("ReadSettingsFile(): " + e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("ReadSettingsFile(): " + e.getMessage(), e);
            throw new JobSchedulerException(e);
        }
        return map;
    }

    private Properties resolveIncludes(Properties props, SOSLogger sosLogger) throws Exception {
        return resolveIncludes(props, "", sosLogger);
    }

    private Properties resolveIncludes(Properties props, String prefix, SOSLogger sosLogger) throws Exception {
        Properties allIncludedProps = new Properties();
        SOSConfiguration conf = null;
        if (prefix == null) {
            prefix = "";
        }
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String key = (String) e.getKey();
            key = key.substring(prefix.length());
            String value = (String) e.getValue();
            if (isIncludeDirective(key)) {
                String[] includes = value.split("[;|,]");
                String includePrefix = getIncludePrefix(prefix + key);
                if (includePrefix == null)
                    continue;
                for (String include : includes) {
                    include = include.trim();
                    conf = new SOSConfiguration(settings.Value(), include, sosLogger);
                    Properties includedProps = conf.getParameterAsProperties(includePrefix);
                    if (includedProps.size() <= 0) {
                        String msg = SOSVfsMessageCodes.SOSVfs_E_0000.params(include, settings.Value());
                        throw new JobSchedulerException(msg);
                    }
                    includedProps = resolveIncludes(includedProps, includePrefix, sosLogger);
                    allIncludedProps.putAll(includedProps);
                }
            }
        }
        for (Map.Entry<Object, Object> e : allIncludedProps.entrySet()) {
            if (!props.containsKey(e.getKey())) {
                props.put(e.getKey(), e.getValue());
            }
        }
        return props;
    }

    private boolean hasVariableToSubstitute(String value) {
        boolean result = false;
        value = " " + value.toLowerCase().replaceAll("(\\$|%)\\{(source|target)(transfer)?filename\\}", "")
                .replaceAll("%(source|target)(transfer)?filename%", "");
        if (value.matches("^.*[^\\\\](\\$|%)\\{[^/\\}\\\\]+\\}.*$") || value.matches("^.*[^\\\\]%[^/%\\\\]+%.*$")) {
            result = true;
        }
        return result;
    }

    private String unescape(String value) {
        return value.replaceAll("\\\\((?:\\$|%)\\{[^/\\}\\\\]+\\})", "$1").replaceAll("\\\\(%[^/%\\\\]+%)", "$1");
    }

    private boolean isIniComment(final String txt) {
        return txt.trim().startsWith(";");
    }

    public boolean isIncludeDirective(final String includeDirective) {
        return includeDirectives.containsKey(includeDirective);
    }

    public String getIncludePrefix(final String includeDirective) {
        return includeDirectives.get(includeDirective);
    }

    public String substituteVariables(String txt, final Properties prop) {
        Map<String, String> startEndCharsForSubstitute = new HashMap<String, String>();
        startEndCharsForSubstitute.put("${", "}");
        startEndCharsForSubstitute.put("%{", "}");
        startEndCharsForSubstitute.put("%", "%");
        for (Map.Entry<String, String> e : startEndCharsForSubstitute.entrySet()) {
            txt = substituteVariables(txt, prop, e.getKey(), e.getValue());
        }
        return txt;
    }

    public String substituteVariables(String txt, final Properties prop, final String startPrefix, final String endPrefix) {
        try {
            for (Object k : prop.keySet()) {
                String key = (String) k;
                String value = (String) prop.get(key);
                String search = startPrefix + key + endPrefix;
                int pos1 = -1;
                int pos2 = 0;
                while (true) {
                    pos1 = txt.indexOf(search, pos2);
                    if (pos1 == -1) {
                        break;
                    }
                    int intEscaped = txt.indexOf("\\" + search);
                    if (intEscaped > -1 && intEscaped == pos1 - 1) {
                        pos1 = -1;
                    }
                    pos2 = pos1 + search.length();
                    if (pos1 > -1 && pos2 > pos1) {
                        txt = txt.substring(0, pos1) + value + txt.substring(pos2);
                    }
                }
            }
            return txt;
        } catch (Exception e) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_197.params(txt), e);
        }
    }

    protected URI createURI(final String fileName) {
        URI uri = null;
        try {
            uri = new URI(fileName);
        } catch (Exception e) {
            try {
                File f = new File(fileName);
                String path = f.getCanonicalPath();
                if (fileName.startsWith("/")) {
                    path = fileName;
                }
                String fs = System.getProperty(conSystemPropertyFILE_SEPARATOR);
                if (fs.length() == 1) {
                    char sep = fs.charAt(0);
                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }
                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }
                if (!path.startsWith(conURIPrefixFILE)) {
                    path = conURIPrefixFILE + path;
                }
                uri = new URI(path);
            } catch (Exception ex) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_198.params(e.getMessage()));
            }
        }
        return uri;
    }

    public boolean OneOrMoreSingleFilesSpecified() {
        return file_path.IsNotEmpty() || FileListName.IsNotEmpty();
    }

    public String getDataTargetType() {
        String targetType = "";
        if (operation.Value().equalsIgnoreCase(OPERATION_SEND)) {
            targetType = protocol.Value();
            if (targetType.isEmpty()) {
                targetType = enuTransferTypes.local.Text();
            }
            CopyValue(SourceDir, local_dir);
            CopyValue(TargetDir, remote_dir);
            CopyValue(this.Source().Directory, local_dir);
            CopyValue(this.Target().Directory, remote_dir);
            changeOptions(this.getConnectionOptions().Target());
        } else {
            if (operation.Value().equalsIgnoreCase(OPERATION_RECEIVE)) {
                targetType = enuTransferTypes.local.Text();
                CopyValue(SourceDir, remote_dir);
                CopyValue(TargetDir, local_dir);
                CopyValue(this.Source().Directory, remote_dir);
                CopyValue(this.Target().Directory, local_dir);
                changeOptions2Local(this.getConnectionOptions().Target());
            } else {
                targetType = this.getConnectionOptions().Target().protocol.Value();
                if (targetType.isEmpty()) {
                    targetType = enuTransferTypes.local.Text();
                }
                changeDirValues();
            }
        }
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_262.params(targetType));
        return targetType;
    }

    private void changeOptions(final SOSConnection2OptionsAlternate options) {
        options.host.Set(host);
        LOGGER.debug("prefix_host = " + options.host.Value());
        options.user.Value(user.Value());
        options.password.Set(password);
        options.port.Set(port);
        options.protocol.Set(protocol);
        options.passive_mode.Set(passive_mode);
        options.transfer_mode.Set(transfer_mode);
        options.ssh_auth_file.SetIfNotDirty(ssh_auth_file);
        options.ssh_auth_method.SetIfNotDirty(ssh_auth_method);
        SOSConnection2OptionsSuperClass alternativesOptions = options.Alternatives();
        alternativesOptions.host.Value(alternative_host.Value());
        alternativesOptions.port.value(alternative_port.value());
        alternativesOptions.protocol.Value(protocol.Value());
        alternativesOptions.passive_mode.Value(alternative_passive_mode.Value());
        alternativesOptions.transfer_mode.Value(alternative_transfer_mode.Value());
    }

    private void changeOptions2Local(final SOSConnection2OptionsAlternate options) {
        options.host.Value(SOSOptionHostName.getLocalHost());
        options.user.Value("");
        options.password.Value("");
        options.port.value(0);
        options.protocol.Value("local");
        options.passive_mode.Value("");
        options.transfer_mode.Value("");
        SOSConnection2OptionsSuperClass alternativesOptions = options.Alternatives();
        alternativesOptions.host.Value(options.host.Value());
        alternativesOptions.port.value(0);
        alternativesOptions.protocol.Value("local");
        alternativesOptions.passive_mode.Value("");
        alternativesOptions.transfer_mode.Value("");
    }

    private void CopyValue(final SOSOptionElement to, final SOSOptionElement from) {
        if (to.isNotDirty()) {
            to.Value(from.Value());
        }
    }

    public String getDataSourceType() {
        String type = "";
        if (operation.Value().equalsIgnoreCase(OPERATION_SEND)) {
            type = enuTransferTypes.local.Text();
            changeDirValues();
            SOSConnection2OptionsAlternate options = this.getConnectionOptions().Source();
            options.host.Value(SOSOptionHostName.getLocalHost());
            options.port.value(0);
            options.protocol.Value(type);
            options = this.getConnectionOptions().Target();
            options.host = host;
            options.port = port;
            options.protocol = protocol;
            options.user = user;
            options.password = password;
            options.ssh_auth_file = ssh_auth_file;
            options.ssh_auth_method = ssh_auth_method;
            options.passive_mode = passive_mode;
            SOSConnection2OptionsSuperClass alternativesOptions = options.Alternatives();
            alternativesOptions.host.Value(alternative_host.Value());
            alternativesOptions.port.value(alternative_port.value());
            alternativesOptions.protocol.Value(protocol.Value());
            alternativesOptions.passive_mode.Value(alternative_passive_mode.Value());
        } else {
            if (operation.Value().equalsIgnoreCase(OPERATION_RECEIVE)) {
                type = protocol.Value();
                if (type.length() <= 0) {
                    type = enuTransferTypes.local.Text();
                }
                changeDirValues4Receive();
                SOSConnection2OptionsAlternate options = this.getConnectionOptions().Source();
                options.host.Value(host.Value());
                options.port.value(port.value());
                options.protocol.Value(protocol.Value());
                options.passive_mode.Value(passive_mode.Value());
                options.user = user;
                options.password = password;
                options.ssh_auth_file = ssh_auth_file;
                options.ssh_auth_method = ssh_auth_method;
                options = this.getConnectionOptions().Target();
                options.host.Value(SOSOptionHostName.getLocalHost());
                options.port.value(0);
                options.protocol.Value(enuTransferTypes.local.Text());
                SOSConnection2OptionsSuperClass alternativesOptions = options.Alternatives();
                alternativesOptions.host.Value(alternative_host.Value());
                alternativesOptions.port.value(alternative_port.value());
                alternativesOptions.protocol.Value(protocol.Value());
                alternativesOptions.passive_mode.Value(alternative_passive_mode.Value());
            } else {
                type = this.getConnectionOptions().Source().protocol.Value();
                if (type.isEmpty()) {
                    type = enuTransferTypes.local.Text();
                }
                changeDirValues();
            }
        }
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_199.params(type));
        return type;
    }

    private void ReplicateConnectionOptions(SOSConnection2OptionsAlternate options) {
        options.host.Value(SOSOptionHostName.getLocalHost());
        options.port.value(0);
        options.protocol.Value(enuTransferTypes.local.Text());
        options = this.getConnectionOptions().Target();
        options.host = host;
        options.port = port;
        options.protocol = protocol;
        options.user = user;
        options.password = password;
        options.ssh_auth_file = ssh_auth_file;
        options.ssh_auth_method = ssh_auth_method;
        options.passive_mode = passive_mode;
    }

    private void changeDirValues() {
        ChangeValue(SourceDir, local_dir);
        ChangeValue(TargetDir, remote_dir);
        ChangeValue(local_dir, SourceDir);
        ChangeValue(remote_dir, TargetDir);
        ChangeValue(Source().FolderName, SourceDir);
        ChangeValue(Target().FolderName, TargetDir);
    }

    private void changeDirValues4Receive() {
        ChangeValue(SourceDir, remote_dir);
        ChangeValue(TargetDir, local_dir);
    }

    private void ChangeValue(final SOSOptionElement target, final SOSOptionElement source) {
        if (target.IsEmpty() && !source.IsEmpty()) {
            if (source instanceof SOSOptionPassword) {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(target.getKey(), "*****"));
            } else {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(target.getKey(), source.Value()));
            }
            target.Set(source);
        }
    }

    public boolean DoNotOverwrite() {
        return overwrite_files.value() == false && append_files.value() == false;
    }

    public SOSConnection2Options getConnectionOptions() {
        if (connectionOptions == null) {
            connectionOptions = new SOSConnection2Options();
        }
        return connectionOptions;
    }

    public void setConnectionOptions(final SOSConnection2Options options) {
        connectionOptions = options;
    }

    public boolean isReplaceReplacingInEffect() {
        return this.getreplacing().IsNotEmpty();
    }

    public SOSConnection2OptionsAlternate Source() {
        return getConnectionOptions().Source();
    }

    public SOSConnection2OptionsAlternate Target() {
        return getConnectionOptions().Target();
    }

    public boolean NeedTargetClient() {
        boolean need = true;
        switch (operation.value()) {
        case delete:
        case getlist:
        case rename:
            need = false;
            break;
        default:
            break;
        }
        return need;
    }

    public SOSFTPOptions getClone() {
        SOSFTPOptions options = new SOSFTPOptions();
        String args = this.getOptionsAsCommandLine();
        options.CommandLineArgs(args);
        return options;
    }

    public void ClearJumpParameter() {
        String nullString = null;
        jump_user.Value(nullString);
        jump_password.Value(nullString);
        jump_protocol.Value(nullString);
        jump_host.Value(nullString);
        jump_ssh_auth_method.Value(nullString);
        jump_command.Value(nullString);
        host.Value(nullString);
    }

    public boolean isFilePollingEnabled() {
        boolean enabled = false;
        if ((poll_timeout.isDirty() || PollingDuration.isDirty()) && skip_transfer.isFalse()) {
            enabled = true;
        }
        return enabled;
    }

    public String DirtyString() {
        String val = "\n" + super.dirtyString();
        val += "\n" + Source().dirtyString();
        val += "\n" + Target().dirtyString();
        return val;
    }

    @Override
    public SOSOptionRegExp getreplacing() {
        SOSOptionRegExp regEx = super.getreplacing();
        if (Target().getreplacing().isDirty()) {
            regEx = Target().getreplacing();
        }
        return regEx;
    }

    @Override
    public SOSOptionString getreplacement() {
        SOSOptionString val = super.getreplacement();
        if (Target().getreplacement().isDirty()) {
            val = Target().getreplacement();
        }
        return val;
    }

    @Override
    public SOSOptionBoolean getraise_exception_on_error() {
        return super.getraise_exception_on_error();
    }

    public Map<String, String> getDmzOptions() {
        return dmzOptions;
    }

    public void setDmzOption(String dmzOptionKey, String dmzOptionValue) {
        dmzOptions.put(dmzOptionKey, dmzOptionValue);
    }

    public String getDmzOption(String dmzOptionKey) {
        return dmzOptions.getOrDefault(dmzOptionKey, "");
    }
}
