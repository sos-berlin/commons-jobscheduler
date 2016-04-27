/*
 * Created on 03.03.2011 To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sos.scheduler.InstallationService.batchInstallationModel;

import java.io.File;

import org.apache.log4j.Logger;

import sos.scheduler.InstallationService.batchInstallationModel.installations.Globals;
import sos.scheduler.InstallationService.batchInstallationModel.installations.Installation;

public class JSinstallation extends Installation {

    protected Globals globals;
    private File installationFile = null;
    private static Logger logger = Logger.getLogger(JSBatchInstallerExecuter.class);

    private String getValue(String installationValue, String globalValue) {
        if (globalValue == null) {
            globalValue = "";
        }
        if (installationValue == null) {
            installationValue = "";
        }
        if (!installationValue.isEmpty() || "".equals(globalValue)) {
            return installationValue;
        } else {
            return globalValue;
        }
    }

    private Short getValue(Short installationValue, Short globalValue) {
        if (globalValue == null) {
            globalValue = 0;
        }
        if (installationValue == null) {
            installationValue = 0;
        }
        if (installationValue != 0 || globalValue == 0) {
            return installationValue;
        } else {
            return globalValue;
        }
    }

    public File getInstallationFile(File configurationPath) {
        if (installationFile == null) {
            installationFile = new File(configurationPath, this.getHost() + "_" + this.getSchedulerPort() + ".xml");
        }
        return installationFile;
    }

    private String replace(String parameterValue, String parameterName, String newValue) {
        return parameterValue.replaceAll("\\$\\{" + parameterName + "\\}", newValue);
    }

    private String replaceAll(String value) {
        if (value == null) {
            return value;
        }
        value = this.replace(value, "host", this.getHost());
        value = this.replace(value, "licence", this.getLicence());
        value = this.replace(value, "licence_options", this.getLicenceOptions());
        value = this.replace(value, "install_path", this.getInstallPath());
        value = this.replace(value, "scheduler_port", String.valueOf(this.getSchedulerPort()));
        value = this.replace(value, "scheduler_allowed_host", this.getSchedulerAllowedHost());
        value = this.replace(value, "scheduler_id", this.getSchedulerId());
        value = this.replace(value, "userPathPanelElement", this.getUserPathPanelElement());
        // FTP
        value = this.replace(value, "ftp_local_dir", this.getFtp().getLocalDir());
        value = this.replace(value, "ftp_password", this.getFtp().getPassword());
        value = this.replace(value, "ftp_port", String.valueOf(this.getFtp().getPort()));
        value = this.replace(value, "ftp_localDir", this.getFtp().getLocalDir());
        value = this.replace(value, "ftp_remote_dir", this.getFtp().getRemoteDir());
        value = this.replace(value, "ftp_user", this.getFtp().getUser());
        // SSH
        value = this.replace(value, "auth_method", this.getSsh().getAuthMethod());
        value = this.replace(value, "command", this.getSsh().getCommand());
        value = this.replace(value, "password", this.getSsh().getPassword());
        value = this.replace(value, "sudo_password", this.getSsh().getSudoPassword());
        value = this.replace(value, "port", String.valueOf(this.getSsh().getPort()));
        value = this.replace(value, "user", this.getSsh().getUser());
        if (this.installationFile == null) {
            logger.debug("Installationfile is not set. Will not be replaces");
            this.installationFile = new File("");
        } else {
            if (!"".equals(this.installationFile.getName())) {
                value = this.replace(value, "installation_file", this.installationFile.getName());
            }
            value = this.replace(value, "installation_file", this.installationFile.getName());
        }
        return value;
    }

    public void doReplacing() {
        this.setHost(replaceAll(this.getHost()));
        this.setLicence(replaceAll(this.getLicence()));
        this.setLicenceOptions(replaceAll(this.getLicenceOptions()));
        this.setInstallPath(replaceAll(this.getInstallPath()));
        this.setSchedulerAllowedHost(replaceAll(this.getSchedulerAllowedHost()));
        this.setSchedulerId(replaceAll(this.getSchedulerId()));
        this.setUserPathPanelElement(replaceAll(this.getUserPathPanelElement()));
        this.getFtp().setLocalDir(replaceAll(this.getFtp().getLocalDir()));
        this.getFtp().setPassword(replaceAll(this.getFtp().getPassword()));
        this.getFtp().setRemoteDir(replaceAll(this.getFtp().getRemoteDir()));
        this.getFtp().setUser(replaceAll(this.getFtp().getUser()));
        this.getSsh().setAuthMethod(replaceAll(this.getSsh().getAuthMethod()));
        this.getSsh().setAuthFile(replaceAll(this.getSsh().getAuthFile()));
        this.getSsh().setCommand(replaceAll(this.getSsh().getCommand()));
        this.getSsh().setSudoPassword(replaceAll(this.getSsh().getSudoPassword()));
        this.getSsh().setPassword(replaceAll(this.getSsh().getPassword()));
        this.getSsh().setUser(replaceAll(this.getSsh().getUser()));
    }

    public void setValues(Installation installation) {
        this.setLicence(getValue(installation.getLicence(), globals.getLicence()));
        this.setLicenceOptions(getValue(installation.getLicenceOptions(), globals.getLicenceOptions()));
        this.setSchedulerPort(getValue(installation.getSchedulerPort(), globals.getSchedulerPort()));
        this.setInstallPath(getValue(installation.getInstallPath(), globals.getInstallPath()));
        this.setSchedulerAllowedHost(getValue(installation.getSchedulerAllowedHost(), globals.getSchedulerAllowedHost()));
        this.setSchedulerId(getValue(installation.getSchedulerId(), globals.getSchedulerId()));
        this.setUserPathPanelElement(getValue(installation.getUserPathPanelElement(), globals.getUserPathPanelElement()));
        this.setHost(installation.getHost());
        this.setLastRun(installation.getLastRun());
        installation.getFtp().setLocalDir(getValue(installation.getFtp().getLocalDir(), globals.getFtp().getLocalDir()));
        installation.getFtp().setPassword(getValue(installation.getFtp().getPassword(), globals.getFtp().getPassword()));
        installation.getFtp().setPort(getValue(installation.getFtp().getPort(), globals.getFtp().getPort()));
        installation.getFtp().setRemoteDir(getValue(installation.getFtp().getRemoteDir(), globals.getFtp().getRemoteDir()));
        installation.getFtp().setUser(getValue(installation.getFtp().getUser(), globals.getFtp().getUser()));
        this.setFtp(installation.getFtp());
        installation.getSsh().setAuthMethod(getValue(installation.getSsh().getAuthMethod(), globals.getSsh().getAuthMethod()));
        installation.getSsh().setCommand(getValue(installation.getSsh().getCommand(), globals.getSsh().getCommand()));
        installation.getSsh().setPassword(getValue(installation.getSsh().getPassword(), globals.getSsh().getPassword()));
        installation.getSsh().setSudoPassword(getValue(installation.getSsh().getSudoPassword(), globals.getSsh().getSudoPassword()));
        installation.getSsh().setPort(getValue(installation.getSsh().getPort(), globals.getSsh().getPort()));
        installation.getSsh().setUser(getValue(installation.getSsh().getUser(), globals.getSsh().getUser()));
        installation.getSsh().setAuthFile(getValue(installation.getSsh().getAuthFile(), globals.getSsh().getAuthFile()));
        this.setSsh(installation.getSsh());
    }

}