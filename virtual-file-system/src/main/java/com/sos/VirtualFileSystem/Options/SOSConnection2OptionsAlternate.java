package com.sos.VirtualFileSystem.Options;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.linguafranca.pwdb.Entry;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionObject;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.keepass.SOSKeePassDatabase;
import com.sos.keepass.SOSKeePassPath;

import sos.util.SOSString;

@JSOptionClass(name = "SOSConnection2OptionsAlternate", description = "Options for a connection to an uri (server, site, e.g.)")
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSConnection2OptionsAlternate extends SOSConnection2OptionsSuperClass {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SOSConnection2OptionsAlternate.class);
    private static final String CLASSNAME = SOSConnection2OptionsAlternate.class.getSimpleName();
    private String strAlternativePrefix = "";
    public boolean isSource = false;

    public SOSConnection2OptionsAlternate() {
        //
    }

    public SOSConnection2OptionsAlternate(final String prefix) {
        strAlternativePrefix = prefix;
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

    @JSOptionDefinition(name = "PreTransferCommands", description = "FTP commands, which has to be executed before the transfer started.", key = "PreTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString preTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".pre_transfer_commands",
            "FTP commands, which has to be executed before the transfer started.", "", "", false);

    public SOSOptionCommandString preFtpCommands = (SOSOptionCommandString) preTransferCommands.setAlias("pre_transfer_commands");

    public String getPreTransferCommands() {
        return preTransferCommands.getValue();
    }

    public SOSConnection2OptionsAlternate setPreTransferCommands(final String val) {
        preTransferCommands.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "PostTransferCommands", description = "FTP commands, which has to be executed after the transfer ended.", key = "PostTransferCommands", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommands = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_Commands",
            "FTP commands, which has to be executed after the transfer ended.", "", "", false);

    public SOSOptionCommandString postFtpCommands = (SOSOptionCommandString) postTransferCommands.setAlias("post_Transfer_commands");

    public String getPostTransferCommands() {
        return postTransferCommands.getValue();
    }

    public SOSConnection2OptionsAlternate setPostTransferCommands(final String val) {
        postTransferCommands.setValue(val);
        return this;
    }

    @JSOptionDefinition(name = "post_transfer_commands_on_error", description = "Commands, which has to be executed after the transfer ended with errors.", key = "post_transfer_commands_on_error", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommandsOnError = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_commands_on_error",
            "Commands, which has to be executed after the transfer ended with errors.", "", "", false);

    @JSOptionDefinition(name = "post_transfer_commands_final", description = "Commands, which has to be executed always after the transfer ended independent of "
            + "the transfer status.", key = "post_transfer_commands_final", type = "SOSOptionCommandString", mandatory = false)
    public SOSOptionCommandString postTransferCommandsFinal = new SOSOptionCommandString(this, CLASSNAME + ".post_transfer_commands_final",
            "Commands, which has to be executed always after the transfer ended independent of the transfer status.", "", "", false);

    @JSOptionDefinition(name = "IgnoreCertificateError", description = "Ignore a SSL Certificate Error", key = "IgnoreCertificateError", type = "SOSOptionBoolean", mandatory = true)
    public SOSOptionBoolean ignoreCertificateError = new SOSOptionBoolean(this, CLASSNAME + ".IgnoreCertificateError",
            "Ignore a SSL Certificate Error", "true", "true", true);

    public boolean getIgnoreCertificateError() {
        return ignoreCertificateError.value();
    }

    public SOSConnection2OptionsAlternate setIgnoreCertificateError(final boolean val) {
        ignoreCertificateError.value(val);
        return this;
    }

    @JSOptionDefinition(name = "command_delimiter", description = "Command delimiter for pre and post commands", key = "command_delimiter", type = "SOSOptionString", mandatory = false)
    public SOSOptionString commandDelimiter = new SOSOptionString(this, CLASSNAME + ".command_delimiter",
            "Command delimiter for pre and post commands", ";", ";", true);

    @JSOptionDefinition(name = "AlternateOptionsUsed", description = "Alternate Options used for connection and/or authentication", key = "AlternateOptionsUsed", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean alternateOptionsUsed = new SOSOptionBoolean(this, CLASSNAME + ".AlternateOptionsUsed",
            "Alternate Options used for connection and/or authentication", "false", "false", false);

    public String getAlternateOptionsUsed() {
        return alternateOptionsUsed.getValue();
    }

    public SOSConnection2OptionsAlternate setAlternateOptionsUsed(final String val) {
        alternateOptionsUsed.setValue(val);
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
        if (objCredentialStoreOptions.useCredentialStore.isTrue()) {
            LOGGER.trace("entering checkCredentialStoreOptions ");
            objCredentialStoreOptions.credentialStoreFileName.checkMandatory(true);

            String keePassPassword = null;
            String keePassKeyFile = null;
            if (objCredentialStoreOptions.credentialStorePassword.isDirty()) {
                keePassPassword = objCredentialStoreOptions.credentialStorePassword.getValue();
            }
            if (objCredentialStoreOptions.credentialStoreKeyFileName.isDirty()) {
                keePassKeyFile = objCredentialStoreOptions.credentialStoreKeyFileName.getValue();
            }
            SOSKeePassDatabase kpd = null;
            try {
                kpd = new SOSKeePassDatabase(Paths.get(objCredentialStoreOptions.credentialStoreFileName.getValue()));
                if (keePassKeyFile == null) {
                    kpd.load(keePassPassword);
                } else {
                    kpd.load(keePassPassword, Paths.get(keePassKeyFile));
                }
                keePass2Options(kpd);
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                throw new JobSchedulerException(e);
            }
        }
    }

    private Entry<?, ?, ?, ?> keePass2OptionsByKeePassSyntax(final SOSKeePassDatabase kpd) throws Exception {
        Entry<?, ?, ?, ?> entry = keePass2OptionByKeePassSyntax(kpd, host, null);
        entry = keePass2OptionByKeePassSyntax(kpd, port, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, protocol, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, user, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, password, entry);

        entry = keePass2OptionByKeePassSyntax(kpd, proxyHost, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, proxyPort, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, proxyProtocol, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, proxyUser, entry);
        entry = keePass2OptionByKeePassSyntax(kpd, proxyPassword, entry);

        return entry;
    }

    private Entry<?, ?, ?, ?> keePass2OptionsByKeePassDefault(final SOSKeePassDatabase kpd) throws Exception {
        String keyPath = objCredentialStoreOptions.credentialStoreKeyPath.getValue();
        if (keyPath.isEmpty()) {
            throw new Exception("credentialStoreKeyPath is missing");
        }
        LOGGER.debug(String.format("keyPath=%s", keyPath));
        Entry<?, ?, ?, ?> entry = getKeePassEntry(kpd, keyPath);
        if (!entry.getUrl().isEmpty()) {
            try {
                // Possible Elements of an URL are:
                //
                // http://hans:geheim@www.example.org:80/demo/example.cgi?land=de&stadt=aa#geschichte
                // | | | | | | | |
                // | | | host | url-path searchpart fragment
                // | | password port
                // | user
                // protocol
                //
                // ftp://<user>:<password>@<host>:<port>/<url-path>;type=<typecode>
                // see
                // http://docs.oracle.com/javase/7/docs/api/java/net/URL.html
                URL url = new URL(entry.getUrl());
                setIfNotDirty(host, url.getHost());
                String urlPort = String.valueOf(url.getPort());
                if (isEmpty(urlPort) || urlPort.equals("-1")) {
                    urlPort = String.valueOf(url.getDefaultPort());
                }
                setIfNotDirty(port, urlPort);
                setIfNotDirty(protocol, url.getProtocol());
                String urlUserInfo = url.getUserInfo();
                String[] ui = urlUserInfo.split(":");
                setIfNotDirty(user, ui[0]);
                if (ui.length > 1) {
                    setIfNotDirty(password, ui[1]);
                }
            } catch (MalformedURLException e) {
                //
            }
        }
        boolean hideValue = false;
        if (isNotEmpty(entry.getUsername())) {
            user.setValue(entry.getUsername());
            user.setHideValue(hideValue);
        }
        if (isNotEmpty(entry.getPassword())) {
            password.setValue(entry.getPassword());
            password.setHideValue(hideValue);
        }
        if (isNotEmpty(entry.getUrl())) {
            setIfNotDirty(host, entry.getUrl());
            host.setHideValue(hideValue);
        }
        if (hostName.isNotDirty()) {
            hostName.setValue(entry.getUrl().toString());
        }
        return entry;
    }

    @JSOptionDefinition(name = "keepass_database", description = "Keepass database", key = "keepass_database", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject keepass_database = new SOSOptionObject(this, CLASSNAME + ".keepass_database", "Keepass database", "", "", false);

    @JSOptionDefinition(name = "keepass_database_entry", description = "Keepass entry", key = "keepass_database_entry", type = "SOSOptionObject", mandatory = false)
    public SOSOptionObject keepass_database_entry = new SOSOptionObject(this, CLASSNAME + ".keepass_database_entry", "Keepass entry", "", "", false);

    private void keePass2Options(final SOSKeePassDatabase kpd) throws Exception {
        Entry<?, ?, ?, ?> entry = keePass2OptionsByKeePassSyntax(kpd);
        if (entry == null) {
            entry = keePass2OptionsByKeePassDefault(kpd);
        }
        if (objCredentialStoreOptions.credentialStoreExportAttachment.isTrue()) {
            keepass_database.value(kpd);
            keepass_database_entry.value(entry);
            /** Path attachmentTargetFile = Paths.get(objCredentialStoreOptions.credentialStoreExportAttachment2FileName.getValue()); try {
             * kpd.exportAttachment2File(entry, attachmentTargetFile); } catch (Exception e) { LOGGER.error(e.getMessage()); throw new JobSchedulerException(e);
             * } if (objCredentialStoreOptions.credentialStoreDeleteExportedFileOnExit.isTrue()) { attachmentTargetFile.toFile().deleteOnExit(); } */
        }
    }

    private Entry<?, ?, ?, ?> getKeePassEntry(final SOSKeePassDatabase kpd, final String entryPath) throws Exception {
        Entry<?, ?, ?, ?> entry = kpd.getEntryByPath(entryPath);
        if (entry == null) {
            throw new Exception(String.format("[%s][%s]entry not found", objCredentialStoreOptions.credentialStoreFileName.getValue(), entryPath));
        }
        if (entry.getExpires()) {
            throw new Exception(String.format("[%s][%s]entry is expired (%s)", objCredentialStoreOptions.credentialStoreFileName.getValue(),
                    entryPath, entry.getExpiryTime()));
        }
        return entry;
    }

    private Entry<?, ?, ?, ?> keePass2OptionByKeePassSyntax(final SOSKeePassDatabase kpd, final SOSOptionElement option,
            Entry<?, ?, ?, ?> previousEntry) throws Exception {
        SOSKeePassPath keePassPath = new SOSKeePassPath(kpd.isKDBX(), option.getValue(), objCredentialStoreOptions.credentialStoreKeyPath.getValue());
        Entry<?, ?, ?, ?> entry = null;
        String fileName = objCredentialStoreOptions.credentialStoreFileName.getValue();
        String optionName = option.getShortKey();
        if (keePassPath.isValid()) {
            LOGGER.debug(String.format("[%s]set from %s", optionName, keePassPath.toString()));
            if (previousEntry == null || !keePassPath.getEntryPath().equals(previousEntry.getPath())) {
                entry = getKeePassEntry(kpd, keePassPath.getEntry());
            } else {
                entry = previousEntry;
            }
            String value = entry.getProperty(keePassPath.getPropertyName());
            if (value == null) {
                throw new Exception(String.format("[%s][%s][%s]value is null", fileName, optionName, keePassPath.toString()));
            }
            option.setValue(value);
        } else {
            LOGGER.debug(String.format("[%s]skip", optionName));
        }
        return entry == null ? previousEntry : entry;
    }

    protected void setIfNotDirty(final SOSOptionElement option, final String value) {
        if (option.isNotDirty() && isNotEmpty(value)) {
            if (option instanceof SOSOptionPassword) {
                option.setValue(value);
            } else {
                LOGGER.trace("setValue = " + value);
            }
            option.setValue(value);
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

    public SOSConnection2OptionsAlternate getAlternatives() {
        if (objAlternativeOptions == null) {
            objAlternativeOptions = new SOSConnection2OptionsAlternate("");
        }
        return objAlternativeOptions;
    }

    public void setAlternativeOptions(final SOSConnection2OptionsAlternate val) {
        objAlternativeOptions = val;
    }

    public boolean optionsHaveMinRequirements() {
        if (alternateOptionsUsed.isTrue()) {
            return false;
        }
        if ("local".equalsIgnoreCase(protocol.getValue())) {
            return true;
        }
        if (host.isNotDirty() || host.IsEmpty()) {
            return false;
        }
        if (protocol.getValue().matches("https?")) {
            return true;
        }
        if (user.isNotDirty() || user.IsEmpty()) {
            return false;
        }
        return true;
    }

}