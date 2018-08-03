package com.sos.VirtualFileSystem.Options;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.log4j.Logger;

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
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassPath;

import sos.configuration.SOSConfiguration;
import sos.net.mail.options.SOSSmtpMailOptions;
import sos.util.SOSLogger;
import sos.util.SOSStandardLogger;

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
    private String originalSettingsFile = null;
    private boolean deleteSettingsFileOnExit = false;
    @JSOptionClass(description = "objConnectionOptions", name = "SOSConnection2Options")
    private SOSConnection2Options objConnectionOptions;
    @JSOptionClass(description = "objMailOptions", name = "objMailOptions")
    private SOSSmtpMailOptions objMailOptions;
    private String jobSchedulerId;
    private String jobChain;
    private String jobChainNodeName;
    private String job;
    private String orderId;
    private String taskId;
    private Long parentTransferId;
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
            this.getSource().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.getTarget().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.getText(), SOSOptionTransferType.enuTransferTypes.webdav.getText());
            this.getSource().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.getText(),
                    SOSOptionTransferType.enuTransferTypes.webdav.getText());
            this.getTarget().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.getText(),
                    SOSOptionTransferType.enuTransferTypes.webdav.getText());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.getSource().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.getTarget().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case http:
            authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.getSource().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            this.getTarget().authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.getText(), SOSOptionTransferType.enuTransferTypes.http.getText());
            this.getSource().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.getText(),
                    SOSOptionTransferType.enuTransferTypes.http.getText());
            this.getTarget().protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.http.getText(),
                    SOSOptionTransferType.enuTransferTypes.http.getText());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.getSource().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            this.getTarget().port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
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
            protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.getText(), SOSOptionTransferType.enuTransferTypes.webdav.getText());
            port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        default:
            break;
        }
        this.changeDefaults(penuTransferTypeSource, this.getSource());
        this.changeDefaults(penuTransferTypeTarget, this.getTarget());
    }

    public SOSFTPOptions() {
        super();
    }

    @Deprecated
    public SOSFTPOptions(final JSListener pobjListener) {
        super(pobjListener);
    }

    public SOSFTPOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
    }

    private void changeDefaults(final SOSOptionTransferType.enuTransferTypes penuTransferType, final SOSConnection2OptionsAlternate pobjOpt) {
        switch (penuTransferType) {
        case webdav:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.url.text, enuAuthenticationMethods.url.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.webdav.getText(), SOSOptionTransferType.enuTransferTypes.webdav
                    .getText());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4http, SOSOptionPortNumber.conPort4http);
            break;
        case local:
        case file:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.local.getText(), SOSOptionTransferType.enuTransferTypes.local
                    .getText());
            pobjOpt.port.changeDefaults(0, 0);
            break;
        case ftp:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.ftp.getText(), SOSOptionTransferType.enuTransferTypes.ftp
                    .getText());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4FTP, SOSOptionPortNumber.conPort4FTP);
            break;
        case sftp:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.sftp.getText(), SOSOptionTransferType.enuTransferTypes.sftp
                    .getText());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4SFTP, SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftps:
            pobjOpt.authMethod.changeDefaults(enuAuthenticationMethods.password.text, enuAuthenticationMethods.password.text);
            pobjOpt.protocol.changeDefaults(SOSOptionTransferType.enuTransferTypes.ftps.getText(), SOSOptionTransferType.enuTransferTypes.ftps
                    .getText());
            pobjOpt.port.changeDefaults(SOSOptionPortNumber.conPort4FTPS, SOSOptionPortNumber.conPort4FTPS);
            break;
        default:
            break;
        }
    }

    public SOSSmtpMailOptions getMailOptions() {
        if (objMailOptions == null) {
            objMailOptions = new SOSSmtpMailOptions();
        }
        return objMailOptions;
    }

    public void setChildClasses(final Properties pobjProperties) {
        HashMap<String, String> map = new HashMap<String, String>((Map) pobjProperties);
        try {
            this.setChildClasses(map);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private void setChildClasses(final HashMap<String, String> JSSettings) {
        try {
            if (objConnectionOptions == null) {
                objConnectionOptions = new SOSConnection2Options(JSSettings);
                // objMailOptions = new SOSSmtpMailOptions(JSSettings);
            } else {
                objConnectionOptions.setPrefixedValues(JSSettings);
            }
            if (objMailOptions == null) {
                objMailOptions = new SOSSmtpMailOptions(JSSettings);
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    public void adjustDefaults() {
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.move.getText())) {
            removeFiles.value(true);
            removeFiles.setProtected(operation.isProtected());
        }
        if (transactionMode.isTrue() && !isAtomicTransfer()) {
            atomicSuffix.setValue("~");
        }
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.getlist.getText())) {
            removeFiles.value(false);
        }
        String localDir = this.localDir.getValue();
        if (isEmpty(localDir)) {
            this.localDir.set(sourceDir);
            localDir = this.localDir.getValue();
        }
        checkReplaceAndReplacing(getTarget());
        checkReplaceAndReplacing(getSource());
        if (replacing.isNotEmpty() && replacement.isNull()) {
            replacement.setValue("");
        }
        if (this.getTarget().protocol.isDirty() && !protocol.isDirty()) {
            protocol.set(this.getTarget().protocol);
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(this.getSource().protocol, this.getSource().port, this.getSource().host);
        setDefaultHostPort(this.getTarget().protocol, this.getTarget().port, this.getTarget().host);
        setDefaultHostPort(this.getSource().getAlternatives().protocol, this.getSource().getAlternatives().port, this.getSource()
                .getAlternatives().host);
        setDefaultHostPort(this.getTarget().getAlternatives().protocol, this.getTarget().getAlternatives().port, this.getTarget()
                .getAlternatives().host);
        getDataSourceType();
        getDataTargetType();
    }

    @Override
    public void checkMandatory() {
        if (flgCheckMandatoryDone) {
            return;
        }
        operation.checkMandatory();
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.move.getText())) {
            removeFiles.value(true);
        }
        if (transactionMode.isTrue() && !isAtomicTransfer()) {
            atomicSuffix.setValue("~");
        }
        if (operation.getValue().equalsIgnoreCase(enuJadeOperations.getlist.getText())) {
            removeFiles.setFalse();
        }
        checkURLParameter(this.getConnectionOptions().getSource());
        checkURLParameter(this.getConnectionOptions().getTarget());
        String localDir = this.localDir.getValue();
        if (isEmpty(localDir)) {
            this.localDir.setValue(sourceDir.getValue());
            localDir = this.localDir.getValue();
            localDir += "";
        }
        if (getSource().url.isDirty() && sourceDir.isNotDirty()) {
            sourceDir.setValue(getSource().url.getFolderName());
        }
        if (getTarget().url.isDirty() && targetDir.isNotDirty()) {
            targetDir.setValue(getTarget().url.getFolderName());
        }
        checkCredentialStore(getSource());
        checkCredentialStore(getTarget());
        if (getSource().replacing.isNotEmpty() && getSource().replacement.isNotEmpty()) {
            removeFiles.setFalse();
        }
        super.checkMandatory();
        if (localDir.startsWith("\\\\")) {
            while (localDir.indexOf("\\") != -1) {
                localDir = localDir.replace('\\', '/');
            }
        }
        this.localDir.setValue(localDir);
        if (localDir.startsWith(conURIPrefixFILE) && !(new File(createURI(localDir)).exists())) {
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0010.params(localDir));
        }
        checkReplaceAndReplacing(getTarget());
        checkReplaceAndReplacing(getSource());
        if (replacing.isNotEmpty() && replacement.isNull()) {
            replacement.setValue("");
        }
        if (replacing.IsEmpty() && replacement.isNotEmpty()) {
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
            if (!"ftp".equalsIgnoreCase(protocol.getValue())) {
                throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_0040.params(strAppendFilesKey, protocol.getValue()));
            }
        }
        if (this.getTarget().protocol.isDirty() && !protocol.isDirty()) {
            protocol.setValue(this.getTarget().protocol.getValue());
        }
        setDefaultHostPort(protocol, port, host);
        setDefaultHostPort(this.getSource().protocol, this.getSource().port, this.getSource().host);
        setDefaultHostPort(this.getTarget().protocol, this.getTarget().port, this.getTarget().host);
        setDefaultHostPort(this.getSource().getAlternatives().protocol, this.getSource().getAlternatives().port, this.getSource()
                .getAlternatives().host);
        setDefaultHostPort(this.getTarget().getAlternatives().protocol, this.getTarget().getAlternatives().port, this.getTarget()
                .getAlternatives().host);
        setDefaultAuth(this.getSource().protocol, this.getSource());
        setDefaultAuth(this.getTarget().protocol, this.getTarget());
        setDefaultAuth(this.getSource().getAlternatives().protocol, this.getSource().getAlternatives());
        setDefaultAuth(this.getTarget().getAlternatives().protocol, this.getTarget().getAlternatives());
        if (filePath.isDirty() && fileSpec.isDirty()) {
            filePath.setValue("");
        }
        if (filePath.IsEmpty() && sourceDir.IsEmpty() && this.getSource().directory.IsEmpty() && fileListName.IsEmpty()) {
            throw new JobSchedulerException(String.format("SOSVfs-E-0000: one of these parameters must be specified: '%1$s', '%2$s', '%3$s'", filePath
                    .getShortKey(), "source_dir", fileListName.getShortKey()));
        }

        if (protocolCommandListener.isDirty()) {
            if (getSource().protocolCommandListener.isNotDirty()) {
                getSource().protocolCommandListener.value(protocolCommandListener.value());
            }
            if (getTarget().protocolCommandListener.isNotDirty()) {
                getTarget().protocolCommandListener.value(protocolCommandListener.value());
            }
        }

        getDataSourceType();
        getDataTargetType();
        if (checkNotProcessedOptions.value()) {
            this.checkNotProcessedOptions();
        }
        flgCheckMandatoryDone = true;
    }

    private void checkReplaceAndReplacing(final SOSConnection2OptionsSuperClass pobjO) {
        if (pobjO.replacing.isNotEmpty() && pobjO.replacement.isNull()) {
            pobjO.replacement.setValue("");
        }
        if (pobjO.replacing.IsEmpty() && pobjO.replacement.isNotEmpty()) {
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
            objConn.authMethod.setValue(enuAuthenticationMethods.url);
            objConn.sshAuthMethod.setValue(enuAuthenticationMethods.url);
        }
        if (transferType == enuTransferTypes.local) {
            objConn.user.setValue(System.getProperty("user.name"));
            objConn.user.setNotDirty();
            this.user.setValue(System.getProperty("user.name"));
            this.user.setNotDirty();
        }
    }

    private void setDefaultHostPort(final SOSOptionTransferType pobjTransferTyp, final SOSOptionPortNumber pobjPort,
            final SOSOptionHostName pobjHost) {
        enuTransferTypes transferType = pobjTransferTyp.getEnum();
        switch (transferType) {
        case sftp:
            pobjPort.setDefaultValue("" + SOSOptionPortNumber.conPort4SFTP);
            break;
        case ftp:
            pobjPort.setDefaultValue("" + SOSOptionPortNumber.conPort4FTP);
            break;
        case zip:
        case file:
        case local:
            pobjPort.setDefaultValue("0");
            if (pobjHost.isNotDirty() || "localhost".equalsIgnoreCase(pobjHost.getValue()) || "127.0.0.1".equalsIgnoreCase(pobjHost.getValue())) {
                pobjHost.setValue(SOSOptionHostName.getLocalHost());
                pobjHost.setNotDirty();
            }
            break;
        case ftps:
            pobjPort.setDefaultValue("" + SOSOptionPortNumber.conPort4FTPS);
            break;
        case webdav:
        case http:
        case https:
            if (pobjHost.getValue().toLowerCase().startsWith("https://")) {
                pobjPort.setDefaultValue("" + SOSOptionPortNumber.conPort4https);
            } else {
                pobjPort.setDefaultValue("" + SOSOptionPortNumber.conPort4http);
            }
            break;
        default:
            break;
        }
        if (pobjPort.isNotDirty()) {
            pobjPort.setValue(pobjPort.getDefaultValue());
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
            strRet += sosOptionElement.getKey() + "=" + sosOptionElement.getValue();
        }
        return strRet;
    }

    @Override
    public boolean isAtomicTransfer() {
        boolean flgIsAtomicTransfer = atomicPrefix.isNotEmpty() || atomicSuffix.isNotEmpty();
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
            LOGGER.error(e.getMessage());
            throw new JobSchedulerException(SOSVfsMessageCodes.SOSVfs_E_153.params("command lines args"), e);
        }
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        HashMap<String, String> hshMap = pobjJSSettings;
        if (!flgSettingsFileProcessed && !flgReadSettingsFileIsActive && configurationFile.isNotEmpty()) {
            flgReadSettingsFileIsActive = true;
            hshMap = readSettingsFile();
            flgReadSettingsFileIsActive = false;
            flgSettingsFileProcessed = true;
        }
        setChildClasses(hshMap);
    }

    private boolean isEmpty(HashMap<String, String> params, String key) {
        return ((params.get(key) == null) || params.get(key).length() == 0);
    }

    private boolean isEqualIgnoreCase(HashMap<String, String> params, String key, String value) {
        return ((params.get(key) != null) && params.get(key).equalsIgnoreCase(value));
    }

    private void handleFileOrderSource(HashMap<String, String> params) {
        boolean b = false;
        b = ((isEmpty(params, "file_path") || isEqualIgnoreCase(params, "file_path", "${scheduler_file_path}")) && isEmpty(params, "source_dir")
                && isEmpty(params, "local_dir") && isEmpty(params, "file_spec"));
        if (b && !isEmpty(params, "scheduler_file_path")) {
            LOGGER.debug(String.format("Using value from parameter SCHEDULER_FILE_PATH %s for the parameter file_path, as no file_path, local_dir, "
                    + "file_spec or source_dir has been specified", params.get("scheduler_file_path")));
            params.put("file_path", params.get("scheduler_file_path"));
        }
    }

    public void setAllOptions2(HashMap<String, String> params) {
        Map<String, String> mapFromIniFile = new HashMap<String, String>();
        if (!flgSettingsFileProcessed && !flgReadSettingsFileIsActive && params.containsKey("settings") && params.containsKey("profile")) {
            this.settings.setValue(params.get("settings"));
            this.profile.setValue(params.get("profile"));
            flgReadSettingsFileIsActive = true;
            mapFromIniFile = readSettingsFile(params);
            flgReadSettingsFileIsActive = false;
            flgSettingsFileProcessed = true;
        }
        params.putAll(mapFromIniFile);
        handleFileOrderSource(params);
        objSettings = params;
        super.setSettings(params);
        super.setAllOptions(params);
        setChildClasses(params);
    }

    public HashMap<String, String> readSettingsFile() {
        return readSettingsFile(null);
    }

    public HashMap<String, String> readSettingsFile(Map<String, String> beatParams) {
        settings.checkMandatory();
        profile.checkMandatory();
        HashMap<String, String> map = new HashMap<String, String>();
        SOSStandardLogger sosLogger = null;
        SOSConfiguration conf = null;
        Properties properties = new Properties();
        try {
            LOGGER.debug(String.format("readSettingsFile: settings=%s", settings.getValue()));
            sosLogger = new SOSStandardLogger(0);
            getEnvVars();
            conf = new SOSConfiguration(settings.getValue(), profile.getValue());
            Properties profileProps = conf.getParameterAsProperties();
            if (profileProps.isEmpty()) {
                String strM = SOSVfsMessageCodes.SOSVfs_E_0060.params(profile.getValue(), settings.getValue());
                throw new JobSchedulerException(strM);
            }
            conf = new SOSConfiguration(settings.getValue(), "globals");
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
            props4Substitute.put("profile", profile.getValue());
            props4Substitute.put("settings", settings.getValue());
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
                        switch (key) {
                        case "source_pre_command":
                        case "source_post_command":
                        case "source_tfn_post_command":

                        case "target_pre_command":
                        case "target_post_command":
                        case "target_tfn_post_command":
                            
                        case "jump_post_transfer_commands_on_error":
                        case "jump_post_transfer_commands_final":
                        case "jump_post_transfer_commands_on_success":
                        case "jump_pre_transfer_commands":
                            
                        case "file_path":
                            break;
                        default:
                            if (!SOSKeePassPath.hasKeePassVariables(value)) {
                                LOGGER.warn(SOSVfsMessageCodes.SOSVfs_W_0070.params(value, key));
                            }
                        }
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
                    conf = new SOSConfiguration(settings.getValue(), include);
                    Properties includedProps = conf.getParameterAsProperties(includePrefix);
                    if (includedProps.isEmpty()) {
                        String strM = SOSVfsMessageCodes.SOSVfs_E_0000.params(include, settings.getValue());
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
        value = " " + value.toLowerCase().replaceAll("(\\$|%)\\{(source|target)(transfer)?filename\\}", "").replaceAll(
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

    public boolean oneOrMoreSingleFilesSpecified() {
        return filePath.isNotEmpty() || fileListName.isNotEmpty();
    }

    public String getDataTargetType() {
        String strDataTargetType = "";
        if (OPERATION_SEND.equalsIgnoreCase(operation.getValue())) {
            strDataTargetType = protocol.getValue();
            if (strDataTargetType.isEmpty()) {
                strDataTargetType = enuTransferTypes.local.getText();
            }
            copyValue(sourceDir, localDir);
            copyValue(targetDir, remoteDir);
            copyValue(this.getSource().directory, localDir);
            copyValue(this.getTarget().directory, remoteDir);
            changeOptions(this.getConnectionOptions().getTarget());
        } else if (conOperationRECEIVE.equalsIgnoreCase(operation.getValue())) {
            strDataTargetType = enuTransferTypes.local.getText();
            copyValue(sourceDir, remoteDir);
            copyValue(targetDir, localDir);
            copyValue(this.getSource().directory, remoteDir);
            copyValue(this.getTarget().directory, localDir);
            changeOptions2Local(this.getConnectionOptions().getTarget());
        } else {
            strDataTargetType = this.getConnectionOptions().getTarget().protocol.getValue();
            if (strDataTargetType.isEmpty()) {
                strDataTargetType = enuTransferTypes.local.getText();
            }
            changeDirValues();
        }
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_262.params(strDataTargetType));
        return strDataTargetType;
    }

    private void changeOptions(final SOSConnection2OptionsAlternate objT) {
        objT.host.set(host);
        LOGGER.debug("prefix_host = " + objT.host.getValue());
        objT.user.setValue(user.getValue());
        objT.password.set(password);
        objT.port.set(port);
        objT.protocol.set(protocol);
        objT.passiveMode.set(passiveMode);
        objT.transferMode.set(transferMode);
        objT.sshAuthFile.setIfNotDirty(sshAuthFile);
        objT.sshAuthMethod.setIfNotDirty(sshAuthMethod);
        SOSConnection2OptionsSuperClass objAlt = objT.getAlternatives();
        objAlt.host.setValue(alternativeHost.getValue());
        objAlt.port.value(alternativePort.value());
        objAlt.protocol.setValue(protocol.getValue());
        objAlt.passiveMode.setValue(alternativePassiveMode.getValue());
        objAlt.transferMode.setValue(alternativeTransferMode.getValue());
    }

    private void changeOptions2Local(final SOSConnection2OptionsAlternate objT) {
        objT.host.setValue(SOSOptionHostName.getLocalHost());
        objT.user.setValue("");
        objT.password.setValue("");
        objT.port.value(0);
        objT.protocol.setValue("local");
        objT.passiveMode.setValue("");
        objT.transferMode.setValue("");
        SOSConnection2OptionsSuperClass objAlt = objT.getAlternatives();
        objAlt.host.setValue(objT.host.getValue());
        objAlt.port.value(0);
        objAlt.protocol.setValue("local");
        objAlt.passiveMode.setValue("");
        objAlt.transferMode.setValue("");
    }

    private void copyValue(final SOSOptionElement objTo, final SOSOptionElement objFrom) {
        if (objTo.isNotDirty()) {
            objTo.setValue(objFrom.getValue());
        }
    }

    public String getDataSourceType() {
        String strDataSourceType = "";
        if (OPERATION_SEND.equalsIgnoreCase(operation.getValue())) {
            strDataSourceType = enuTransferTypes.local.getText();
            changeDirValues();
            SOSConnection2OptionsAlternate objT = this.getConnectionOptions().getSource();
            objT.host.setValue(SOSOptionHostName.getLocalHost());
            objT.port.value(0);
            objT.protocol.setValue(strDataSourceType);
            objT = this.getConnectionOptions().getTarget();
            objT.host = host;
            objT.port = port;
            objT.protocol = protocol;
            objT.user = user;
            objT.password = password;
            objT.sshAuthFile = sshAuthFile;
            objT.sshAuthMethod = sshAuthMethod;
            objT.passiveMode = passiveMode;
            SOSConnection2OptionsSuperClass objAlt = objT.getAlternatives();
            objAlt.host.setValue(alternativeHost.getValue());
            objAlt.port.value(alternativePort.value());
            objAlt.protocol.setValue(protocol.getValue());
            objAlt.passiveMode.setValue(alternativePassiveMode.getValue());
        } else if (conOperationRECEIVE.equalsIgnoreCase(operation.getValue())) {
            strDataSourceType = protocol.getValue();
            if (strDataSourceType.isEmpty()) {
                strDataSourceType = enuTransferTypes.local.getText();
            }
            changeDirValues4Receive();
            SOSConnection2OptionsAlternate objT = this.getConnectionOptions().getSource();
            objT.host.setValue(host.getValue());
            objT.port.value(port.value());
            objT.protocol.setValue(protocol.getValue());
            objT.passiveMode.setValue(passiveMode.getValue());
            objT.user = user;
            objT.password = password;
            objT.sshAuthFile = sshAuthFile;
            objT.sshAuthMethod = sshAuthMethod;
            objT = this.getConnectionOptions().getTarget();
            objT.host.setValue(SOSOptionHostName.getLocalHost());
            objT.port.value(0);
            objT.protocol.setValue(enuTransferTypes.local.getText());
            SOSConnection2OptionsSuperClass objAlt = objT.getAlternatives();
            objAlt.host.setValue(alternativeHost.getValue());
            objAlt.port.value(alternativePort.value());
            objAlt.protocol.setValue(protocol.getValue());
            objAlt.passiveMode.setValue(alternativePassiveMode.getValue());
        } else {
            strDataSourceType = this.getConnectionOptions().getSource().protocol.getValue();
            if (strDataSourceType.isEmpty()) {
                strDataSourceType = enuTransferTypes.local.getText();
            }
            changeDirValues();
        }
        LOGGER.debug(SOSVfsMessageCodes.SOSVfs_D_199.params(strDataSourceType));
        return strDataSourceType;
    }

    private void changeDirValues() {
        changeValue(sourceDir, localDir);
        changeValue(targetDir, remoteDir);
        changeValue(localDir, sourceDir);
        changeValue(remoteDir, targetDir);
        changeValue(getSource().folderName, sourceDir);
        changeValue(getTarget().folderName, targetDir);
    }

    private void changeDirValues4Receive() {
        changeValue(sourceDir, remoteDir);
        changeValue(targetDir, localDir);
    }

    private void changeValue(final SOSOptionElement pobjTarget, final SOSOptionElement pobjSource) {
        if (pobjTarget.IsEmpty() && !pobjSource.IsEmpty()) {
            if (pobjSource instanceof SOSOptionPassword) {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(pobjTarget.getKey(), "*****"));
            } else {
                LOGGER.trace(SOSVfsMessageCodes.SOSVfs_I_263.params(pobjTarget.getKey(), pobjSource.getValue()));
            }
            pobjTarget.set(pobjSource);
        }
    }

    public boolean isDoNotOverwrite() {
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
        return this.getReplacing().isNotEmpty();
    }

    public SOSConnection2OptionsAlternate getSource() {
        return getConnectionOptions().getSource();
    }

    public SOSConnection2OptionsAlternate getTarget() {
        return getConnectionOptions().getTarget();
    }

    public boolean isNeedTargetClient() {
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

    public void clearJumpParameter() {
        String strNullString = null;
        jumpUser.setValue(strNullString);
        jumpPassword.setValue(strNullString);
        jumpProtocol.setValue(strNullString);
        jumpHost.setValue(strNullString);
        jumpSshAuthMethod.setValue(strNullString);
        jumpCommand.setValue(strNullString);
        host.setValue(strNullString);
    }

    public boolean isFilePollingEnabled() {
        boolean flgFilePollingEnabled = false;
        if ((pollTimeout.isDirty() || pollingDuration.isDirty()) && skipTransfer.isFalse()) {
            flgFilePollingEnabled = true;
        }
        return flgFilePollingEnabled;
    }

    public String dirtyString() {
        String strD = "\n" + super.dirtyString();
        strD += "\n" + getSource().dirtyString();
        strD += "\n" + getTarget().dirtyString();
        return strD;
    }

    @Override
    public SOSOptionRegExp getReplacing() {
        SOSOptionRegExp objR = super.getReplacing();
        if (getTarget().getReplacing().isDirty()) {
            objR = getTarget().getReplacing();
        }
        return objR;
    }

    @Override
    public SOSOptionString getReplacement() {
        SOSOptionString objR = super.getReplacement();
        if (getTarget().getReplacement().isDirty()) {
            objR = getTarget().getReplacement();
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

    public void setOriginalSettingsFile(String val) {
        this.originalSettingsFile = val;
    }

    public String getOriginalSettingsFile() {
        return this.originalSettingsFile;
    }

    public void setDeleteSettingsFileOnExit(boolean val) {
        this.deleteSettingsFileOnExit = val;
    }

    public boolean getDeleteSettingsFileOnExit() {
        return this.deleteSettingsFileOnExit;
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String jobSchedulerId) {
        this.jobSchedulerId = jobSchedulerId;
    }

    public String getJobChain() {
        return jobChain;
    }

    public void setJobChain(String jobChain) {
        this.jobChain = jobChain;
    }

    public String getJobChainNodeName() {
        return jobChainNodeName;
    }

    public void setJobChainNodeName(String jobChainNodeName) {
        this.jobChainNodeName = jobChainNodeName;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getParentTransferId() {
        return parentTransferId;
    }

    public void setParentTransferId(Long parentTransferId) {
        this.parentTransferId = parentTransferId;
    }

}