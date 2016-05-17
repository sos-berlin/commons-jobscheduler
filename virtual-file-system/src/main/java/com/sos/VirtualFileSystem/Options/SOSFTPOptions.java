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

    private static final String OPERATION_SEND = "send";
    private static final long serialVersionUID = -8219289268940238015L;
    private static final Logger LOGGER = Logger.getLogger(SOSFTPOptions.class);
    private Map<String, String> dmzOptions = new HashMap<String, String>();
    private Properties propSOSFtpEnvironmentVars = null;
    private Properties schedulerParams = null;
    private boolean flgCheckMandatoryDone = false;
    private boolean flgReadSettingsFileIsActive = false;
    private boolean flgSettingsFileProcessed = false;
    private Properties propAllEnvironmentVariables = null;
    @JSOptionClass(description = "objConnectionOptions", name = "SOSConnection2Options")
    private SOSConnection2Options objConnectionOptions;
    @JSOptionClass(description = "objMailOptions", name = "objMailOptions")
    private SOSSmtpMailOptions objMailOptions;
    public static final String conURIPrefixFILE = "file://";
    public static final String conSchedulerEnvVarPrefix = "scheduler_param_";
    public static final String conSOSFtpEnvVarPrefix = "sosftp_";
    public static final String conSystemPropertyFILE_SEPARATOR = "file.separator";
    public static final String conOperationRECEIVE = "receive";
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

    public SOSFTPOptions(final SOSOptionTransferType.enuTransferTypes penuTransferType) {
        super();
        switch (penuTransferType) {
        case webdav:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Source().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Target().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            this.Source().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(),
                    SOSOptionTransferType.enuTransferTypes.webdav.Text());
            this.Target().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(),
                    SOSOptionTransferType.enuTransferTypes.webdav.Text());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Source().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Target().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case http:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Source().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.Target().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.Text(), SOSOptionTransferType.enuTransferTypes.http.Text());
            this.Source().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.Text(),
                    SOSOptionTransferType.enuTransferTypes.http.Text());
            this.Target().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.Text(),
                    SOSOptionTransferType.enuTransferTypes.http.Text());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Source().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.Target().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
    }

    public SOSFTPOptions(final SOSOptionTransferType.enuTransferTypes penuTransferTypeSource,
            final SOSOptionTransferType.enuTransferTypes penuTransferTypeTarget) {
        super();
        switch (penuTransferTypeSource) {
        case webdav:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(), SOSOptionTransferType.enuTransferTypes.webdav.Text());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
        this.changeDefaults(penuTransferTypeSource, this.Source());
        this.changeDefaults(penuTransferTypeTarget, this.Target());
    }

    private void changeDefaults(final SOSOptionTransferType.enuTransferTypes penuTransferType, final SOSConnection2OptionsAlternate pobjOpt) {
        switch (penuTransferType) {
        case webdav:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.Text(),
                    SOSOptionTransferType.enuTransferTypes.webdav.Text());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case local:
        case file:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.local.Text(), SOSOptionTransferType.enuTransferTypes.local.Text());
            pobjOpt.port.changeDefaults(0, 0);
            break;
        case ftp:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.ftp.Text(), SOSOptionTransferType.enuTransferTypes.ftp.Text());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4FTP, SOSOptionPortNumber.conPort4FTP);
            break;
        case sftp:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.sftp.Text(), SOSOptionTransferType.enuTransferTypes.sftp.Text());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4SFTP, SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftps:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.ftps.Text(), SOSOptionTransferType.enuTransferTypes.ftps.Text());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4FTPS, SOSOptionPortNumber.conPort4FTPS);
            break;
        default:
            break;
        }
    }

    public SOSFTPOptions() {
        super();
    }

    public SOSSmtpMailOptions getMailOptions() {
        if (objMailOptions == null) {
            objMailOptions = new SOSSmtpMailOptions();
        }
        return objMailOptions;
    }

    @Deprecated
    public SOSFTPOptions(final JSListener pobjListener) {
        super(pobjListener);
    }

    public SOSFTPOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    public void setChildClasses(final Properties pobjProperties) {
        HashMap<String, String> map = new HashMap<String, String>((Map) pobjProperties);
        try {
            this.setChildClasses(map);
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }

    private void setChildClasses(final HashMap<String, String> JSSettings) {
        try {
            if (objConnectionOptions == null) {
                objConnectionOptions = new SOSConnection2Options(JSSettings);
                objMailOptions = new SOSSmtpMailOptions(JSSettings);
            } else {
                objConnectionOptions.setPrefixedValues(JSSettings);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void adjustDefaults() {
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.move.Text())) {
            removeFiles.value(true);
            removeFiles.setProtected(operation.isProtected());
        }
        if (transactionMode.isTrue() && !isAtomicTransfer()) {
            atomicSuffix.Value("~");
        }
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.getlist.Text())) {
            removeFiles.value(false);
        }
        String localDir = this.localDir.Value();
        if (isEmpty(localDir)) {
            this.localDir.Set(sourceDir);
            localDir = this.localDir.Value();
        }
        checkReplaceAndReplacing(Target());
        checkReplaceAndReplacing(Source());
        if (replacing.IsNotEmpty() && replacement.IsNull()) {
            replacement.Value("");
        }
        if (this.Target().protocol.isDirty() && !protocol.isDirty()) {
            protocol.Set(this.Target().protocol);
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(this.Source().protocol, this.Source().port, this.Source().host);
        setDefaultHostPort(this.Target().protocol, this.Target().port, this.Target().host);
        setDefaultHostPort(this.Source().Alternatives().protocol, this.Source().Alternatives().port, this.Source().Alternatives().host);
        setDefaultHostPort(this.Target().Alternatives().protocol, this.Target().Alternatives().port, this.Target().Alternatives().host);
        getDataSourceType();
        getDataTargetType();
    }

    @Override
    public void checkMandatory() {
        if (flgCheckMandatoryDone) {
            return;
        }
        operation.CheckMandatory();
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.move.Text())) {
            removeFiles.value(true);
        }
        if (transactionMode.isTrue() && !isAtomicTransfer()) {
            atomicSuffix.Value("~");
        }
        if (operation.Value().equalsIgnoreCase(enuJadeOperations.getlist.Text())) {
            removeFiles.setFalse();
        }
        checkURLParameter(this.getConnectionOptions().Source());
        checkURLParameter(this.getConnectionOptions().Target());
        String localDir = this.localDir.Value();
        if (isEmpty(localDir)) {
            this.localDir.Value(sourceDir.Value());
            localDir = this.localDir.Value();
            localDir += "";
        }
        if (Source().url.isDirty() && sourceDir.isNotDirty()) {
            sourceDir.Value(Source().url.getFolderName());
        }
        if (Target().url.isDirty() && targetDir.isNotDirty()) {
            targetDir.Value(Target().url.getFolderName());
        }
        checkCredentialStore(Source());
        checkCredentialStore(Target());
        if (Source().replacing.IsNotEmpty() && Source().replacement.IsNotEmpty()) {
            removeFiles.setFalse();
        }
        super.checkMandatory();
        if (localDir.startsWith("\\\\")) {
            while (localDir.indexOf("\\") != -1) {
                localDir = localDir.replace('\\', '/');
            }
        }
        this.localDir.Value(localDir);
        if (localDir.startsWith(conURIPrefixFILE) && !(new File(createURI(localDir)).exists())) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0010.params(localDir));
        }
        checkReplaceAndReplacing(Target());
        checkReplaceAndReplacing(Source());
        if (replacing.IsNotEmpty() && replacement.IsNull()) {
            replacement.Value("");
        }
        if (replacing.IsEmpty() && replacement.IsNotEmpty()) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0020.params(replacement.getKey(), replacing.getKey()));
        }
        if (appendFiles.value()) {
            String strAppendFilesKey = appendFiles.getKey();
            if (isAtomicTransfer()) {
                String strT = getOptionNamesAsString(new SOSOptionElement[] { atomicPrefix, atomicSuffix });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0050.params(strAppendFilesKey, strT));
            }
            if (compressFiles.value()) {
                String strT = getOptionNamesAsString(new SOSOptionElement[] { compressFiles });
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0050.params(strAppendFilesKey, strT));
            }
            if (!"ftp".equalsIgnoreCase(protocol.Value())) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0040.params(strAppendFilesKey, protocol.Value()));
            }
        }
        if (this.Target().protocol.isDirty() && !protocol.isDirty()) {
            protocol.Value(this.Target().protocol.Value());
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
        if (filePath.isDirty() && fileSpec.isDirty()) {
            filePath.Value("");
        }
        if (filePath.IsEmpty() && sourceDir.IsEmpty() && this.Source().directory.IsEmpty() && fileListName.IsEmpty()) {
            throw new JobSchedulerException(String.format("SOSVfs-E-0000: one of these parameters must be specified: '%1$s', '%2$s', '%3$s'",
                    filePath.getShortKey(), "source_dir", fileListName.getShortKey()));
        }

        if (protocolCommandListener.isDirty()) {
            if (Source().protocolCommandListener.isNotDirty()) {
                Source().protocolCommandListener.value(protocolCommandListener.value());
            }
            if (Target().protocolCommandListener.isNotDirty()) {
                Target().protocolCommandListener.value(protocolCommandListener.value());
            }
        }

        getDataSourceType();
        getDataTargetType();
        if (CheckNotProcessedOptions.value()) {
            this.CheckNotProcessedOptions();
        }
        flgCheckMandatoryDone = true;
    }

    private void checkReplaceAndReplacing(final SOSConnection2OptionsSuperClass pobjO) {
        if (pobjO.replacing.IsNotEmpty() && pobjO.replacement.IsNull()) {
            pobjO.replacement.Value("");
        }
        if (pobjO.replacing.IsEmpty() && pobjO.replacement.IsNotEmpty()) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0020.params(pobjO.replacement.getKey(), pobjO.replacing.getKey()));
        }
    }

    private void checkCredentialStore(final SOSConnection2OptionsAlternate pobjO) {
        if (pobjO.getCredentialStore() != null) {
            pobjO.checkCredentialStoreOptions();
        }
    }

    private void checkURLParameter(final SOSConnection2OptionsAlternate pobjO) {
        if (pobjO.url.isDirty()) {
            pobjO.url.getOptions(pobjO);
        }
    }

    private void setDefaultAuth(final SOSOptionTransferType pobjTransferTyp, final SOSConnection2OptionsAlternate objConn) {
        enuTransferTypes transferType = pobjTransferTyp.getEnum();
        if ((transferType == enuTransferTypes.http || transferType == enuTransferTypes.https || transferType == enuTransferTypes.webdav)
                && !objConn.authMethod.isDirty() && !objConn.sshAuthMethod.isDirty()) {
            objConn.authMethod.Value(enuAuthenticationMethods.url);
            objConn.sshAuthMethod.Value(enuAuthenticationMethods.url);
        }
    }

    private void setDefaultHostPort(final SOSOptionTransferType pobjTransferTyp, final SOSOptionPortNumber pobjPort, final SOSOptionHostName pobjHost) {
        enuTransferTypes transferType = pobjTransferTyp.getEnum();
        switch (transferType) {
        case sftp:
            pobjPort.DefaultValue("" + SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftp:
            pobjPort.DefaultValue("" + SOSOptionPortNumber.conPort4FTP);
            break;
        case zip:
        case file:
        case local:
            pobjPort.DefaultValue("0");
            if (pobjHost.isNotDirty() || "localhost".equalsIgnoreCase(pobjHost.Value()) || "127.0.0.1".equalsIgnoreCase(pobjHost.Value())) {
                pobjHost.Value(SOSOptionHostName.getLocalHost());
            }
            break;
        case ftps:
            pobjPort.DefaultValue("" + SOSOptionPortNumber.conPort4FTPS);
            break;
        case webdav:
        case http:
        case https:
            if (pobjHost.Value().toLowerCase().startsWith("https://")) {
                pobjPort.DefaultValue("" + SOSOptionPortNumber.conPort4https);
            } else {
                pobjPort.DefaultValue("" + SOSOptionPortNumber.conPort4http);
            }
            break;
        default:
            break;
        }
        if (pobjPort.isNotDirty()) {
            pobjPort.Value(pobjPort.DefaultValue());
            pobjPort.setNotDirty();
            pobjPort.setProtected(pobjTransferTyp.isProtected());
        }
    }

    private String getOptionNamesAsString(final SOSOptionElement[] objA) {
        String strRet = "";
        for (SOSOptionElement sosOptionElement : objA) {
            if (!strRet.isEmpty()) {
                strRet += ", ";
            }
            strRet += sosOptionElement.getKey() + "=" + sosOptionElement.Value();
        }
        return strRet;
    }

    @Override
    public boolean isAtomicTransfer() {
        boolean flgIsAtomicTransfer = atomicPrefix.IsNotEmpty() || atomicSuffix.IsNotEmpty();
        return flgIsAtomicTransfer;
    }

    private Properties getEnvVars() throws Exception {
        try {
            propSOSFtpEnvironmentVars = new Properties();
            schedulerParams = new Properties();
            int intSOSFtpEnvVarPrefixLen = conSOSFtpEnvVarPrefix.length();
            Map<String, String> objM = System.getenv();
            propAllEnvironmentVariables = new Properties();
            propAllEnvironmentVariables.putAll(objM);
            for (Object k : propAllEnvironmentVariables.keySet()) {
                String key = (String) k;
                String value = (String) propAllEnvironmentVariables.get(k);
                if (key.startsWith(conSOSFtpEnvVarPrefix)) {
                    key = key.substring(intSOSFtpEnvVarPrefixLen);
                    propSOSFtpEnvironmentVars.setProperty(key, value);
                    continue;
                }
                if (key.startsWith("current_pid") || key.startsWith("ppid")) {
                    propSOSFtpEnvironmentVars.setProperty(key, value);
                    continue;
                }
                if (key.indexOf(conSchedulerEnvVarPrefix) > -1) {
                    schedulerParams.setProperty(key.substring(conSchedulerEnvVarPrefix.length()), value);
                    continue;
                }
            }
            return propSOSFtpEnvironmentVars;
        } catch (Exception e) {
            String strM = SOSVfsMessageCodes.SOSVfs_E_161.params("reading environment", e.toString());
            LOGGER.error(strM, e);
            throw new JobSchedulerException(strM, e);
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
        boolean flgFound = false;
        for (int i = 0; i < pstrArgs.length; i++) {
            String strParam = pstrArgs[i];
            if (strParam.toLowerCase().startsWith("-settings")) {
                pstrArgs[i] = "-ignored=ignored";
                flgFound = true;
            }
        }
        if (flgFound) {
            super.commandLineArgs(pstrArgs);
        }
    }

    @Override
    public void commandLineArgs(final String pstrArgs) {
        try {
            this.commandLineArgs(pstrArgs.split(" "));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_153.params("command lines args"), e);
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
        HashMap<String, String> hshMap = pobjJSSettings;
        if (!flgSettingsFileProcessed && !flgReadSettingsFileIsActive && configurationFile.IsNotEmpty()) {
            flgReadSettingsFileIsActive = true;
            hshMap = ReadSettingsFile();
            flgReadSettingsFileIsActive = false;
            flgSettingsFileProcessed = true;
        }
        setChildClasses(hshMap);
    }

    private boolean isEmpty(HashMap<String, String> params, String key) {
        return ((params.get(key) == null) || params.get(key).length() == 0);
    }

    private void handleFileOrderSource(HashMap<String, String> params) {
        boolean b = false;
        b = (isEmpty(params, "file_path") && isEmpty(params, "source_dir") && isEmpty(params, "local_dir") && isEmpty(params, "file_spec"));
        if (b && !isEmpty(params, "scheduler_file_path")) {
            LOGGER.debug(String.format("Using value from parameter SCHEDULER_FILE_PATH %s for the parameter file_path, as no file_path, local_dir, "
                    + "file_spec or source_dir has been specified", params.get("scheduler_file_path")));
            params.put("file_path", params.get("scheduler_file_path"));
        }
    }

    public void setAllOptions2(HashMap<String, String> params) {
        Map<String, String> mapFromIniFile = new HashMap<String, String>();
        if (!flgSettingsFileProcessed && !flgReadSettingsFileIsActive && params.containsKey("settings") && params.containsKey("profile")) {
            this.settings.Value(params.get("settings"));
            this.profile.Value(params.get("profile"));
            flgReadSettingsFileIsActive = true;
            mapFromIniFile = ReadSettingsFile(params);
            flgReadSettingsFileIsActive = false;
            flgSettingsFileProcessed = true;
        }
        flgSetAllOptions = true;
        params.putAll(mapFromIniFile);
        handleFileOrderSource(params);
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
            if (profileProps.isEmpty()) {
                String strM = SOSVfsMessageCodes.SOSVfs_E_0060.params(profile.Value(), settings.Value());
                throw new JobSchedulerException(strM);
            }
            conf = new SOSConfiguration(settings.Value(), "globals", sosLogger);
            Properties globalsProps = conf.getParameterAsProperties();
            globalsProps = resolveIncludes(globalsProps, sosLogger);
            properties.putAll(globalsProps);
            profileProps = resolveIncludes(profileProps, sosLogger);
            properties.putAll(profileProps);
            // Additional Variables
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
                    value = substituteVariables(value, propSOSFtpEnvironmentVars);
                    value = substituteVariables(value, propAllEnvironmentVariables);
                    value = substituteVariables(value, schedulerParams);
                    if (hasVariableToSubstitute(value)) {
                        String strM = SOSVfsMessageCodes.SOSVfs_W_0070.params(value, key);
                        LOGGER.warn(strM);
                    }
                    value = unescape(value);
                }
                map.put(key, value);
            }
            super.setAllOptions(map);
            setChildClasses(map);
        } catch (JobSchedulerException e) {
            LOGGER.error("ReadSettingsFile(): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("ReadSettingsFile(): " + e.getMessage());
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
                if (includePrefix == null) {
                    continue;
                }
                for (String include : includes) {
                    include = include.trim();
                    conf = new SOSConfiguration(settings.Value(), include, sosLogger);
                    Properties includedProps = conf.getParameterAsProperties(includePrefix);
                    if (includedProps.isEmpty()) {
                        String strM = SOSVfsMessageCodes.SOSVfs_E_0000.params(include, settings.Value());
                        throw new JobSchedulerException(strM);
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
        boolean flgResult = false;
        value =
                " "
                        + value.toLowerCase().replaceAll("(\\$|%)\\{(source|target)(transfer)?filename\\}", "").replaceAll(
                                "%(source|target)(transfer)?filename%", "");
        if (value.matches("^.*[^\\\\](\\$|%)\\{[^/\\}\\\\]+\\}.*$") || value.matches("^.*[^\\\\]%[^/%\\\\]+%.*$")) {
            flgResult = true;
        }
        return flgResult;
    }

    private String unescape(String value) {
        return value.replaceAll("\\\\((?:\\$|%)\\{[^/\\}\\\\]+\\})", "$1").replaceAll("\\\\(%[^/%\\\\]+%)", "$1");
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
                String strValue = (String) prop.get(key);
                String strSearchFor = startPrefix + key + endPrefix;
                int pos1 = -1;
                int pos2 = 0;
                while (true) {
                    pos1 = txt.indexOf(strSearchFor, pos2);
                    if (pos1 == -1) {
                        break;
                    }
                    int intEscaped = txt.indexOf("\\" + strSearchFor);
                    if (intEscaped > -1 && intEscaped == pos1 - 1) {
                        pos1 = -1;
                    }
                    pos2 = pos1 + strSearchFor.length();
                    if (pos1 > -1 && pos2 > pos1) {
                        txt = txt.substring(0, pos1) + strValue + txt.substring(pos2);
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
        return filePath.IsNotEmpty() || fileListName.IsNotEmpty();
    }

    public String getDataTargetType() {
        String strDataTargetType = "";
        if (OPERATION_SEND.equalsIgnoreCase(operation.Value())) {
            strDataTargetType = protocol.Value();
            if (strDataTargetType.isEmpty()) {
                strDataTargetType = enuTransferTypes.local.Text();
            }
            CopyValue(sourceDir, localDir);
            CopyValue(targetDir, remoteDir);
            CopyValue(this.Source().directory, localDir);
            CopyValue(this.Target().directory, remoteDir);
            changeOptions(this.getConnectionOptions().Target());
        } else if (conOperationRECEIVE.equalsIgnoreCase(operation.Value())) {
            strDataTargetType = enuTransferTypes.local.Text();
            CopyValue(sourceDir, remoteDir);
            CopyValue(targetDir, localDir);
            CopyValue(this.Source().directory, remoteDir);
            CopyValue(this.Target().directory, localDir);
            changeOptions2Local(this.getConnectionOptions().Target());
        } else {
            strDataTargetType = this.getConnectionOptions().Target().protocol.Value();
            if (strDataTargetType.isEmpty()) {
                strDataTargetType = enuTransferTypes.local.Text();
            }
            changeDirValues();
        }
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_262.params(strDataTargetType));
        return strDataTargetType;
    }

    private void changeOptions(final SOSConnection2OptionsAlternate objT) {
        objT.host.Set(host);
        LOGGER.debug("prefix_host = " + objT.host.Value());
        objT.user.Value(user.Value());
        objT.password.Set(password);
        objT.port.Set(port);
        objT.protocol.Set(protocol);
        objT.passiveMode.Set(passiveMode);
        objT.transferMode.Set(transferMode);
        objT.sshAuthFile.SetIfNotDirty(sshAuthFile);
        objT.sshAuthMethod.SetIfNotDirty(sshAuthMethod);
        SOSConnection2OptionsSuperClass objAlt = objT.Alternatives();
        objAlt.host.Value(alternativeHost.Value());
        objAlt.port.value(alternativePort.value());
        objAlt.protocol.Value(protocol.Value());
        objAlt.passiveMode.Value(alternativePassiveMode.Value());
        objAlt.transferMode.Value(alternativeTransferMode.Value());
    }

    private void changeOptions2Local(final SOSConnection2OptionsAlternate objT) {
        objT.host.Value(SOSOptionHostName.getLocalHost());
        objT.user.Value("");
        objT.password.Value("");
        objT.port.value(0);
        objT.protocol.Value("local");
        objT.passiveMode.Value("");
        objT.transferMode.Value("");
        SOSConnection2OptionsSuperClass objAlt = objT.Alternatives();
        objAlt.host.Value(objT.host.Value());
        objAlt.port.value(0);
        objAlt.protocol.Value("local");
        objAlt.passiveMode.Value("");
        objAlt.transferMode.Value("");
    }

    private void CopyValue(final SOSOptionElement objTo, final SOSOptionElement objFrom) {
        if (objTo.isNotDirty()) {
            objTo.Value(objFrom.Value());
        }
    }

    public String getDataSourceType() {
        String strDataSourceType = "";
        if (OPERATION_SEND.equalsIgnoreCase(operation.Value())) {
            strDataSourceType = enuTransferTypes.local.Text();
            changeDirValues();
            SOSConnection2OptionsAlternate objT = this.getConnectionOptions().Source();
            objT.host.Value(SOSOptionHostName.getLocalHost());
            objT.port.value(0);
            objT.protocol.Value(strDataSourceType);
            objT = this.getConnectionOptions().Target();
            objT.host = host;
            objT.port = port;
            objT.protocol = protocol;
            objT.user = user;
            objT.password = password;
            objT.sshAuthFile = sshAuthFile;
            objT.sshAuthMethod = sshAuthMethod;
            objT.passiveMode = passiveMode;
            SOSConnection2OptionsSuperClass objAlt = objT.Alternatives();
            objAlt.host.Value(alternativeHost.Value());
            objAlt.port.value(alternativePort.value());
            objAlt.protocol.Value(protocol.Value());
            objAlt.passiveMode.Value(alternativePassiveMode.Value());
        } else if (conOperationRECEIVE.equalsIgnoreCase(operation.Value())) {
            strDataSourceType = protocol.Value();
            if (strDataSourceType.isEmpty()) {
                strDataSourceType = enuTransferTypes.local.Text();
            }
            changeDirValues4Receive();
            SOSConnection2OptionsAlternate objT = this.getConnectionOptions().Source();
            objT.host.Value(host.Value());
            objT.port.value(port.value());
            objT.protocol.Value(protocol.Value());
            objT.passiveMode.Value(passiveMode.Value());
            objT.user = user;
            objT.password = password;
            objT.sshAuthFile = sshAuthFile;
            objT.sshAuthMethod = sshAuthMethod;
            objT = this.getConnectionOptions().Target();
            objT.host.Value(SOSOptionHostName.getLocalHost());
            objT.port.value(0);
            objT.protocol.Value(enuTransferTypes.local.Text());
            SOSConnection2OptionsSuperClass objAlt = objT.Alternatives();
            objAlt.host.Value(alternativeHost.Value());
            objAlt.port.value(alternativePort.value());
            objAlt.protocol.Value(protocol.Value());
            objAlt.passiveMode.Value(alternativePassiveMode.Value());
        } else {
            strDataSourceType = this.getConnectionOptions().Source().protocol.Value();
            if (strDataSourceType.isEmpty()) {
                strDataSourceType = enuTransferTypes.local.Text();
            }
            changeDirValues();
        }
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_199.params(strDataSourceType));
        return strDataSourceType;
    }

    private void changeDirValues() {
        ChangeValue(sourceDir, localDir);
        ChangeValue(targetDir, remoteDir);
        ChangeValue(localDir, sourceDir);
        ChangeValue(remoteDir, targetDir);
        ChangeValue(Source().folderName, sourceDir);
        ChangeValue(Target().folderName, targetDir);
    }

    private void changeDirValues4Receive() {
        ChangeValue(sourceDir, remoteDir);
        ChangeValue(targetDir, localDir);
    }

    private void ChangeValue(final SOSOptionElement pobjTarget, final SOSOptionElement pobjSource) {
        if (pobjTarget.IsEmpty() && !pobjSource.IsEmpty()) {
            if (pobjSource instanceof SOSOptionPassword) {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(pobjTarget.getKey(), "*****"));
            } else {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(pobjTarget.getKey(), pobjSource.Value()));
            }
            pobjTarget.Set(pobjSource);
        }
    }

    public boolean DoNotOverwrite() {
        return !overwriteFiles.value() && !appendFiles.value();
    }

    public SOSConnection2Options getConnectionOptions() {
        if (objConnectionOptions == null) {
            objConnectionOptions = new SOSConnection2Options();
        }
        return objConnectionOptions;
    }

    public void setConnectionOptions(final SOSConnection2Options connectionOptions) {
        objConnectionOptions = connectionOptions;
    }

    public boolean isReplaceReplacingInEffect() {
        return this.getReplacing().IsNotEmpty();
    }

    public SOSConnection2OptionsAlternate Source() {
        return getConnectionOptions().Source();
    }

    public SOSConnection2OptionsAlternate Target() {
        return getConnectionOptions().Target();
    }

    public boolean NeedTargetClient() {
        boolean flgNeedTargetClient = true;
        switch (operation.value()) {
        case delete:
        case getlist:
        case rename:
            flgNeedTargetClient = false;
            break;
        default:
            break;
        }
        return flgNeedTargetClient;
    }

    public SOSFTPOptions getClone() {
        SOSFTPOptions objClone = new SOSFTPOptions();
        String strB = this.getOptionsAsCommandLine();
        objClone.commandLineArgs(strB);
        return objClone;
    }

    public void ClearJumpParameter() {
        String strNullString = null;
        jumpUser.Value(strNullString);
        jumpPassword.Value(strNullString);
        jumpProtocol.Value(strNullString);
        jumpHost.Value(strNullString);
        jumpSshAuthMethod.Value(strNullString);
        jumpCommand.Value(strNullString);
        host.Value(strNullString);
    }

    public boolean isFilePollingEnabled() {
        boolean flgFilePollingEnabled = false;
        if ((pollTimeout.isDirty() || pollingDuration.isDirty()) && skipTransfer.isFalse()) {
            flgFilePollingEnabled = true;
        }
        return flgFilePollingEnabled;
    }

    public String DirtyString() {
        String strD = "\n" + super.dirtyString();
        strD += "\n" + Source().dirtyString();
        strD += "\n" + Target().dirtyString();
        return strD;
    }

    @Override
    public SOSOptionRegExp getReplacing() {
        SOSOptionRegExp objR = super.getReplacing();
        if (Target().getReplacing().isDirty()) {
            objR = Target().getReplacing();
        }
        return objR;
    }

    @Override
    public SOSOptionString getReplacement() {
        SOSOptionString objR = super.getReplacement();
        if (Target().getReplacement().isDirty()) {
            objR = Target().getReplacement();
        }
        return objR;
    }

    @Override
    public SOSOptionBoolean getRaiseExceptionOnError() {
        return super.getRaiseExceptionOnError();
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