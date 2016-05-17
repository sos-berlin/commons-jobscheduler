package com.sos.VirtualFileSystem.Options;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.KeePassDataBase;
import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.KeePassDataBaseManager;
import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.v1.Entry;
import com.sos.CredentialStore.KeePass.pl.sind.keepass.kdb.v1.KeePassDataBaseV1;
import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.CredentialStore.exceptions.CredentialStoreEntryExpired;
import com.sos.CredentialStore.exceptions.CredentialStoreKeyNotFound;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.i18n.annotation.I18NResourceBundle;

@JSOptionClass(name = "SOSConnection2OptionsAlternate", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2OptionsAlternate extends SOSConnection2OptionsSuperClass {

    private final String conClassName = this.getClass().getSimpleName();
    private KeePassDataBase keePassDb = null;
    private KeePassDataBaseV1 kdb1 = null;
    private static final Logger LOGGER = Logger.getLogger(SOSConnection2OptionsAlternate.class);
    private static final long serialVersionUID = 5924032437179660014L;
    private String strAlternativePrefix = "";
    public boolean isSource = false;

    public SOSConnection2OptionsAlternate() {
    }

    public SOSConnection2OptionsAlternate(final String pstrPrefix) {
        strAlternativePrefix = pstrPrefix;
    }

    public SOSConnection2OptionsAlternate(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public SOSConnection2OptionsAlternate(final HashMap<String, String> pobjJSSettings) throws Exception {
        super(pobjJSSettings);
        getAlternativeOptions().setAllOptions(pobjJSSettings, "alternative_" + strAlternativePrefix);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
    }

    public SOSConnection2OptionsAlternate(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
        strAlternativePrefix = pstrPrefix;
        setAllOptions(pobjJSSettings, strAlternativePrefix);
        setChildClasses(pobjJSSettings, pstrPrefix);
    }

    @JSOptionDefinition(name = "PreTransferCommands", description = "FTP commands, which has to be executed before the transfer started.",
            key = "PreTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString PreTransferCommands = new SOSOptionCommandString(this, conClassName + ".pre_transfer_commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);

    public SOSOptionCommandString PreFtpCommands = (SOSOptionCommandString) PreTransferCommands.SetAlias("pre_transfer_commands");

    public String getPreTransferCommands() {
        return PreTransferCommands.Value();
    }

    public SOSConnection2OptionsAlternate setPreTransferCommands(final String pstrValue) {
        PreTransferCommands.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.",
            key = "PostTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString PostTransferCommands = new SOSOptionCommandString(this, conClassName + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);

    public SOSOptionCommandString PostFtpCommands = (SOSOptionCommandString) PostTransferCommands.SetAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return PostTransferCommands.Value();
    }

    public SOSConnection2OptionsAlternate setPostTransferCommands(final String pstrValue) {
        PostTransferCommands.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "IgnoreCertificateError", description = "Ignore a SSL Certificate Error", key = "IgnoreCertificateError",
            type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean IgnoreCertificateError = new SOSOptionBoolean(this, conClassName + ".IgnoreCertificateError", "Ignore a SSL Certificate Error",
            "true", "true", true);

    public boolean getIgnoreCertificateError() {
        return IgnoreCertificateError.value();
    }

    public SOSConnection2OptionsAlternate setIgnoreCertificateError(final boolean pflgValue) {
        IgnoreCertificateError.value(pflgValue);
        return this;
    }

    @JSOptionDefinition(name = "AlternateOptionsUsed", description = "Alternate Options used for connection and/or authentication",
            key = "AlternateOptionsUsed", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean AlternateOptionsUsed = new SOSOptionBoolean(this, conClassName + ".AlternateOptionsUsed",
            "Alternate Options used for connection and/or authentication", "false", "false", false);

    public String getAlternateOptionsUsed() {
        return AlternateOptionsUsed.Value();
    }

    public SOSConnection2OptionsAlternate setAlternateOptionsUsed(final String pstrValue) {
        AlternateOptionsUsed.Value(pstrValue);
        return this;
    }

    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix = "alternate_")
    private SOSConnection2OptionsAlternate objAlternativeOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix = "proxy_")
    private SOSConnection2OptionsAlternate objProxyOptions = null;
    @JSOptionClass(description = "", name = "SOSConnection2OptionsAlternate", prefix = "jump_")
    private SOSConnection2OptionsAlternate objJumpServerOptions = null;
    @JSOptionClass(description = "", name = "SOSCredentialStoreOptions")
    private SOSCredentialStoreOptions objCredentialStoreOptions = null;

    public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
        strAlternativePrefix = pstrPrefix;
        getCredentialStore().setAllOptions(pobjJSSettings, strAlternativePrefix);
        getAlternativeOptions().setAllOptions(pobjJSSettings, "alternative_" + strAlternativePrefix);
        getAlternativeOptions().setAllOptions(pobjJSSettings, "alternate_" + strAlternativePrefix);
        getProxyOptions().setAllOptions(pobjJSSettings, "proxy_" + strAlternativePrefix);
        getJumpServerOptions().setAllOptions(pobjJSSettings, "jump_" + strAlternativePrefix);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
    }

    public SOSConnection2OptionsAlternate getAlternativeOptions() {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate();
        }
        return objAlternativeOptions;
    }

    public SOSConnection2OptionsAlternate getProxyOptions() {
        if (objProxyOptions == null) {
            objProxyOptions = new SOSConnection2OptionsAlternate();
        }
        return objProxyOptions;
    }

    public SOSConnection2OptionsAlternate getJumpServerOptions() {
        if (objJumpServerOptions == null) {
            objJumpServerOptions = new SOSConnection2OptionsAlternate();
        }
        return objJumpServerOptions;
    }

    public SOSCredentialStoreOptions getCredentialStore() {
        if (objCredentialStoreOptions == null) {
            objCredentialStoreOptions = new SOSCredentialStoreOptions();
        }
        return objCredentialStoreOptions;
    }

    public void checkCredentialStoreOptions() {
        if (objCredentialStoreOptions.useCredentialStore.isTrue()) {
            LOGGER.trace("entering checkCredentialStoreOptions ");
            objCredentialStoreOptions.credentialStoreFileName.CheckMandatory(true);
            objCredentialStoreOptions.credentialStoreKeyPath.CheckMandatory(true);
            String strPassword = null;
            File fleKeyFile = null;
            if (objCredentialStoreOptions.credentialStoreKeyFileName.isDirty()) {
                fleKeyFile = new File(objCredentialStoreOptions.credentialStoreKeyFileName.Value());
            }
            if (objCredentialStoreOptions.credentialStorePassword.isDirty()) {
                strPassword = objCredentialStoreOptions.credentialStorePassword.Value();
            }
            File fleKeePassDataBase = new File(objCredentialStoreOptions.credentialStoreFileName.Value());
            try {
                keePassDb = KeePassDataBaseManager.openDataBase(fleKeePassDataBase, fleKeyFile, strPassword);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
                throw new JobSchedulerException(e);
            }
            kdb1 = (KeePassDataBaseV1) keePassDb;
            Entry objEntry = kdb1.getEntry(objCredentialStoreOptions.credentialStoreKeyPath.Value());
            if (objEntry == null) {
                throw new CredentialStoreKeyNotFound(objCredentialStoreOptions);
            }
            Date objExpDate = objEntry.ExpirationDate();
            if (new Date().after(objExpDate)) {
                throw new CredentialStoreEntryExpired(objExpDate);
            }
            boolean flgHideValuesFromCredentialStore = false;
            if (!objEntry.Url().isEmpty()) {
                LOGGER.trace(objEntry.Url());
                String strUrl = objEntry.Url();
                try {
                    URL objURL = new URL(strUrl);
                    setIfNotDirty(host, objURL.getHost());
                    String strPort = String.valueOf(objURL.getPort());
                    if (isEmpty(strPort) || "-1".equals(strPort)) {
                        strPort = String.valueOf(objURL.getDefaultPort());
                    }
                    setIfNotDirty(port, strPort);
                    setIfNotDirty(protocol, objURL.getProtocol());
                    String strUserInfo = objURL.getUserInfo();
                    String[] strU = strUserInfo.split(":");
                    setIfNotDirty(user, strU[0]);
                    if (strU.length > 1) {
                        setIfNotDirty(password, strU[1]);
                    }
                    String strAuthority = objURL.getAuthority();
                    String[] strA = strAuthority.split("@");
                } catch (MalformedURLException e) {
                    // not a valid url. ignore it, because it could be a host
                    // name only
                }
            }
            if (isNotEmpty(objEntry.UserName())) {
                user.Value(objEntry.UserName());
                user.setHideValue(flgHideValuesFromCredentialStore);
            }
            if (isNotEmpty(objEntry.Password())) {
                password.Value(objEntry.Password());
                password.setHideValue(flgHideValuesFromCredentialStore);
            }
            if (isNotEmpty(objEntry.Url())) {
                setIfNotDirty(host, objEntry.Url());
                host.setHideValue(flgHideValuesFromCredentialStore);
            }
            objEntry.ExpirationDate();
            if (hostName.isNotDirty()) {
                hostName.Value(objEntry.getUrl().toString());
            }
            if (objCredentialStoreOptions.credentialStoreExportAttachment.isTrue()) {
                File fleO = objEntry.saveAttachmentAsFile(objCredentialStoreOptions.credentialStoreExportAttachment2FileName.Value());
                if (objCredentialStoreOptions.credentialStoreDeleteExportedFileOnExit.isTrue()) {
                    fleO.deleteOnExit();
                }
            }
            if (objCredentialStoreOptions.credentialStoreProcessNotesParams.isTrue()) {
                commandLineArgs(objEntry.getNotesText());
            }
        }
    }

    protected void setIfNotDirty(final SOSOptionElement objOption, final String pstrValue) {
        if (objOption.isNotDirty() && isNotEmpty(pstrValue)) {
            LOGGER.trace("setValue = " + pstrValue);
            objOption.Value(pstrValue);
        }
    }

    @Override
    public void checkMandatory() {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    public SOSConnection2OptionsAlternate Alternatives() {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate("");
        }
        return objAlternativeOptions;
    }

    public void AlternativeOptions(final SOSConnection2OptionsAlternate pobjAlternativeOptions) {
        objAlternativeOptions = pobjAlternativeOptions;
    }

    public boolean optionsHaveMinRequirements() {
        if (AlternateOptionsUsed.isTrue()) {
            return false;
        }
        if ("local".equalsIgnoreCase(protocol.Value())) {
            return true;
        }
        if (host.isNotDirty() || host.IsEmpty()) {
            return false;
        }
        if (protocol.Value().matches("https?")) {
            return true;
        }
        if (user.isNotDirty() || user.IsEmpty()) {
            return false;
        }
        return true;
    }

}