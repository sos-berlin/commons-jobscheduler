package sos.net.ssh;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;

public class SOSSSHJobOptions extends SOSSSHJobOptionsSuperClass {

    private static final long serialVersionUID = 2072083231341151442L;
    private final static String CLASS_NAME = SOSSSHJobOptions.class.getSimpleName();

    public SOSSSHJobOptions() {
        init();
    }

    private void init() {
        currentClass = this.getClass();
    }

    public SOSSSHJobOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        init();
        setChildClasses(JSSettings, "");
    }

    @JSOptionDefinition(name = "credential_store_fileName", description = "", key = "credential_store_fileName", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName credential_store_filename = new SOSOptionInFileName(this, CLASS_NAME + ".credential_store_fileName", "", "", "",
            false);

    @JSOptionDefinition(name = "credential_store_key_filename", description = "", key = "credential_store_key_filename", type = "SOSOptionInFileName", mandatory = false)
    public SOSOptionInFileName credential_store_key_filename = new SOSOptionInFileName(this, CLASS_NAME + ".credential_store_key_filename", "", "",
            "", false);

    @JSOptionDefinition(name = "credential_store_password", description = "", key = "credential_store_password", type = "SOSOptionPassword", mandatory = false)
    public SOSOptionPassword credential_store_password = new SOSOptionPassword(this, CLASS_NAME + ".credential_store_password", "", "", "", false);

    @JSOptionDefinition(name = "credential_store_entry_path", description = "", key = "credential_store_entry_path", type = "SOSOptionString", mandatory = false)
    public SOSOptionString credential_store_entry_path = new SOSOptionString(this, CLASS_NAME + ".credential_store_entry_path", "", "", "", false);

    @Override
    public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
        super.setChildClasses(pobjJSSettings, pstrPrefix);
    }

    @Override
    public void checkMandatory() {
        command.command_delimiter.setValue(commandDelimiter.getValue());
        super.checkMandatory();
        if (!(authMethod.isPassword() || authMethod.isPublicKey())) {
            throw new JSExceptionMandatoryOptionMissing("ErrSSH010 invalid or no AuthenticationMethod specified");
        }
        if (authMethod.isPassword() && password.IsEmpty()) {
            throw new JSExceptionMandatoryOptionMissing(
                    "ErrSSH020 AuthenticationMethod 'password' requires a Password, but no password was specified");
        }
        if (authMethod.isPublicKey()) {
            if (authFile.isNotEmpty()) {
                authFile.checkMandatory(true);
            } else {
                throw new JSExceptionMandatoryOptionMissing(
                        "ErrSSH050 AuthenticationMethod 'publickey' requires a keyfile, but no keyfile was specified");
            }
        }
    }

    public boolean commandSpecified() {
        return command.isDirty() && commandScript.isDirty() && commandScriptFile.isDirty();
    }

    public boolean isScript() throws Exception {
        return !commandScript.IsEmpty() || !commandScriptFile.IsEmpty();
    }

}