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

    private static final long serialVersionUID = 5924032437179660014L;
    private static final Logger LOGGER = Logger.getLogger(SOSConnection2OptionsAlternate.class);
    private final String className = this.getClass().getSimpleName();
    private KeePassDataBase keePassDb = null;
    private KeePassDataBaseV1 kdb1 = null;
    private String strAlternativePrefix = "";
    public boolean isSource = false;

    @JSOptionDefinition(name = "PreTransferCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString PreTransferCommands = new SOSOptionCommandString(this, className + ".pre_transfer_commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);
    public SOSOptionCommandString PreFtpCommands = (SOSOptionCommandString) PreTransferCommands.SetAlias("pre_transfer_commands");

    public String getPreTransferCommands() {
        return PreTransferCommands.Value();
    }

    public SOSConnection2OptionsAlternate setPreTransferCommands(final String val) {
        PreTransferCommands.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.", key = "PostTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString PostTransferCommands = new SOSOptionCommandString(this, className + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);
    public SOSOptionCommandString PostFtpCommands = (SOSOptionCommandString) PostTransferCommands.SetAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return PostTransferCommands.Value();
    }

    public SOSConnection2OptionsAlternate setPostTransferCommands(final String val) {
        PostTransferCommands.Value(val);
        return this;
    }

    @JSOptionDefinition(name = "post_transfer_commands_on_error", description = "Commands, which has to be executed after the transfer ended with errors.", key = "post_transfer_commands_on_error", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString post_transfer_commands_on_error = new SOSOptionCommandString(this, className + ".post_transfer_commands_on_error",
            "Commands, which has to be executed after the transfer ended with errors.", "", "", false);

    @JSOptionDefinition(name = "post_transfer_commands_final", description = "Commands, which has to be executed always after the transfer ended independent of "
            + "the transfer status.", key = "post_transfer_commands_final", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString post_transfer_commands_final = new SOSOptionCommandString(this, className + ".post_transfer_commands_final",
            "Commands, which has to be executed always after the transfer ended independent of the transfer status.", "", "", false);

    @JSOptionDefinition(name = "IgnoreCertificateError", description = "Ignore a SSL Certificate Error", key = "IgnoreCertificateError", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean IgnoreCertificateError = new SOSOptionBoolean(this, className + ".IgnoreCertificateError",
            "Ignore a SSL Certificate Error", "true", "true", true);

    public boolean getIgnoreCertificateError() {
        return IgnoreCertificateError.value();
    }

    public SOSConnection2OptionsAlternate setIgnoreCertificateError(final boolean val) {
        IgnoreCertificateError.value(val);
        return this;
    }

    @JSOptionDefinition(name = "AlternateOptionsUsed", description = "Alternate Options used for connection and/or authentication", key = "AlternateOptionsUsed", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean AlternateOptionsUsed = new SOSOptionBoolean(this, className + ".AlternateOptionsUsed",
            "Alternate Options used for connection and/or authentication", "false", "false", false);

    public String getAlternateOptionsUsed() {
        return AlternateOptionsUsed.Value();
    }

    public SOSConnection2OptionsAlternate setAlternateOptionsUsed(final String val) {
        AlternateOptionsUsed.Value(val);
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

    public SOSConnection2OptionsAlternate() {
    }

    public SOSConnection2OptionsAlternate(final String val) {
        strAlternativePrefix = val;
    }

    public SOSConnection2OptionsAlternate(final JSListener listener) {
        this();
        this.registerMessageListener(listener);
    }

    public SOSConnection2OptionsAlternate(final HashMap<String, String> settings) throws Exception {
        super(settings);
        getAlternativeOptions().setAllOptions(settings, "alternative_" + strAlternativePrefix);
        this.addProcessedOptions(objAlternativeOptions.getProcessedOptions());
    }

    public SOSConnection2OptionsAlternate(final HashMap<String, String> settings, final String prefix) throws Exception {
        strAlternativePrefix = prefix;
        setAllOptions(settings, strAlternativePrefix);
        setChildClasses(settings, prefix);
    }

    public void setChildClasses(final HashMap<String, String> settings, final String prefix) throws Exception {
        strAlternativePrefix = prefix;
        getCredentialStore().setAllOptions(settings, strAlternativePrefix);
        getAlternativeOptions().setAllOptions(settings, "alternative_" + strAlternativePrefix);
        getAlternativeOptions().setAllOptions(settings, "alternate_" + strAlternativePrefix);
        getProxyOptions().setAllOptions(settings, "proxy_" + strAlternativePrefix);
        getJumpServerOptions().setAllOptions(settings, "jump_" + strAlternativePrefix);
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
        if (objCredentialStoreOptions.use_credential_Store.isTrue()) {
            LOGGER.trace("entering checkCredentialStoreOptions ");
            objCredentialStoreOptions.CredentialStore_FileName.CheckMandatory(true);
            objCredentialStoreOptions.CredentialStore_KeyPath.CheckMandatory(true);
            String keyPassword = null;
            File keyFile = null;
            if (objCredentialStoreOptions.CredentialStore_KeyFileName.isDirty()) {
                keyFile = new File(objCredentialStoreOptions.CredentialStore_KeyFileName.Value());
            }
            if (objCredentialStoreOptions.CredentialStore_password.isDirty()) {
                keyPassword = objCredentialStoreOptions.CredentialStore_password.Value();
            }
            File keePassDataBase = new File(objCredentialStoreOptions.CredentialStore_FileName.Value());
            try {
                keePassDb = KeePassDataBaseManager.openDataBase(keePassDataBase, keyFile, keyPassword);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
                throw new JobSchedulerException(e);
            }
            kdb1 = (KeePassDataBaseV1) keePassDb;
            Entry entry = kdb1.getEntry(objCredentialStoreOptions.CredentialStore_KeyPath.Value());
            if (entry == null) {
                throw new CredentialStoreKeyNotFound(objCredentialStoreOptions);
            }
            Date expDate = entry.ExpirationDate();
            if (new Date().after(expDate)) {
                throw new CredentialStoreEntryExpired(expDate);
            }
            boolean hideValuesFromCredentialStore = false;
            if (!entry.Url().isEmpty()) {
                try {
                    URL url = new URL(entry.Url());
                    setIfNotDirty(host, url.getHost());
                    String entryPort = String.valueOf(url.getPort());
                    if (isEmpty(entryPort) || entryPort.equals("-1")) {
                        entryPort = String.valueOf(url.getDefaultPort());
                    }
                    setIfNotDirty(port, entryPort);
                    setIfNotDirty(protocol, url.getProtocol());
                    String entryUserInfo = url.getUserInfo();
                    String[] arr = entryUserInfo.split(":");
                    setIfNotDirty(user, arr[0]);
                    if (arr.length > 1) {
                        setIfNotDirty(password, arr[1]);
                    }
                    String entryAuthority = url.getAuthority();
                    arr = entryAuthority.split("@");
                } catch (MalformedURLException e) {
                    //
                }
            }
            if (isNotEmpty(entry.UserName())) {
                user.Value(entry.UserName());
                user.setHideValue(hideValuesFromCredentialStore);
            }
            if (isNotEmpty(entry.Password())) {
                password.Value(entry.Password());
                password.setHideValue(hideValuesFromCredentialStore);
            }
            if (isNotEmpty(entry.Url())) {
                setIfNotDirty(host, entry.Url());
                host.setHideValue(hideValuesFromCredentialStore);
            }
            entry.ExpirationDate();
            if (HostName.isNotDirty()) {
                HostName.Value(entry.getUrl().toString());
            }
            if (objCredentialStoreOptions.CredentialStore_ExportAttachment.isTrue()) {
                File fleO = entry.saveAttachmentAsFile(objCredentialStoreOptions.CredentialStore_ExportAttachment2FileName.Value());
                if (objCredentialStoreOptions.CredentialStore_DeleteExportedFileOnExit.isTrue()) {
                    fleO.deleteOnExit();
                }
            }
            if (objCredentialStoreOptions.CredentialStore_ProcessNotesParams.isTrue()) {
                CommandLineArgs(entry.getNotesText());
            }
        }
    }

    protected void setIfNotDirty(final SOSOptionElement option, final String value) {
        if (option.isNotDirty() && isNotEmpty(value)) {
            if (option instanceof SOSOptionPassword) {
                LOGGER.trace("setValue = *****");
            } else {
                LOGGER.trace("setValue = " + value);
            }
            option.Value(value);
        }
    }

    @Override
    public void CheckMandatory() {
        try {
            super.CheckMandatory();
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

    public void AlternativeOptions(final SOSConnection2OptionsAlternate val) {
        objAlternativeOptions = val;
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