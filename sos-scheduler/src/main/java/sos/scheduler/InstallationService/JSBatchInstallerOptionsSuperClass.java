package sos.scheduler.InstallationService;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "JSBatchInstallerOptionsSuperClass", description = "JSBatchInstallerOptionsSuperClass")
public class JSBatchInstallerOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 9068599714451980002L;
    private static final String CLASSNAME = "JSBatchInstallerOptionsSuperClass";

    @JSOptionDefinition(name = "local_dir", description = "Path to the folder with the generated installation files.", key = "local_dir",
            type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName local_dir = new SOSOptionFolderName(this, CLASSNAME + ".local_dir", 
            "Path to the folder with the generated installation files.", " ", " ", true);

    public SOSOptionFolderName getlocal_dir() {
        return local_dir;
    }

    public void setlocal_dir(SOSOptionFolderName p_local_dir) {
        this.local_dir = p_local_dir;
    }

    @JSOptionDefinition(name = "update", description = "False: Ignore value of 'LastRun'", key = "update", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean update = new SOSOptionBoolean(this, CLASSNAME + ".update", "False: Ignore value of LastRun", "false", "false", false);

    public SOSOptionBoolean getupdate() {
        return update;
    }

    public void setupdate(SOSOptionBoolean p_update) {
        this.update = p_update;
    }

    @JSOptionDefinition(name = "filter_install_host", description = "Only installations are executed which belongs to this host.", key = "filter_install_host",
            type = "SOSOptionHostName", mandatory = false)
    public SOSOptionHostName filter_install_host = new SOSOptionHostName(this, CLASSNAME + ".filter_install_host", 
            "Only installations are executed which belongs to this host.", " ", " ", false);

    public SOSOptionHostName getfilter_install_host() {
        return filter_install_host;
    }

    public void setfilter_install_host(SOSOptionHostName p_filter_install_host) {
        this.filter_install_host = p_filter_install_host;
    }

    @JSOptionDefinition(name = "filter_install_port", description = "Only installations are executed which belongs to this port.", key = "filter_install_port",
            type = "SOSOptionPortNumber", mandatory = false)
    public SOSOptionPortNumber filter_install_port = new SOSOptionPortNumber(this, CLASSNAME + ".filter_install_port", 
            "Only installations are executed which belongs to this port.", "0", "0", false);

    public SOSOptionPortNumber getfilter_install_port() {
        return filter_install_port;
    }

    public void setfilter_install_port(SOSOptionPortNumber p_filter_install_port) {
        this.filter_install_port = p_filter_install_port;
    }

    @JSOptionDefinition(name = "installation_definition_file", description = "XML file with installation elements. One element per installation.",
            key = "installation_definition_file", type = "SOSOptionInFileName", mandatory = true)
    public SOSOptionInFileName installation_definition_file = new SOSOptionInFileName(this, CLASSNAME + ".installation_definition_file", 
            "XML file with installation elements. One element per installation.", " ", " ", true);

    public SOSOptionInFileName getinstallation_definition_file() {
        return installation_definition_file;
    }

    public void setinstallation_definition_file(SOSOptionInFileName p_installation_definition_file) {
        this.installation_definition_file = p_installation_definition_file;
    }

    @JSOptionDefinition(name = "installation_setup_filename", description = "Name of the jar file that contains the setup.",
            key = "installation_setup_filename", type = "SOSOptionString", mandatory = true)
    public SOSOptionString installation_setup_filename = new SOSOptionString(this, CLASSNAME + ".installation_setup_filename", 
            "Name of the jar file that contains the setup.", "scheduler_agent.jar", "scheduler_agent.jar", true);

    public SOSOptionString getinstallation_setup_filename() {
        return installation_setup_filename;
    }

    public void setinstallation_setup_filename(SOSOptionString p_installation_setup_filename) {
        this.installation_setup_filename = p_installation_setup_filename;
    }

    @JSOptionDefinition(name = "installation_job_chain", description = "Job chain with the steps for transfer the installation files and perfo",
            key = "installation_job_chain", type = "JSOptionJobChainName", mandatory = true)
    public JSJobChainName installation_job_chain = new JSJobChainName(this, CLASSNAME + ".installation_job_chain", 
            "Job chain with the steps for transfer the installation files and perfo", "automatic_installation", "automatic_installation", true);

    public JSJobChainName getinstallation_job_chain() {
        return installation_job_chain;
    }

    public void setinstallation_job_chain(JSJobChainName p_installation_job_chain) {
        this.installation_job_chain = p_installation_job_chain;
    }

    public JSBatchInstallerOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JSBatchInstallerOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSBatchInstallerOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}