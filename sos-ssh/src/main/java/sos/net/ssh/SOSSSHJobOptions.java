package sos.net.ssh;

import java.util.HashMap;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
public class SOSSSHJobOptions extends SOSSSHJobOptionsSuperClass {

    private static final long serialVersionUID = 2072083231341151442L;

    public SOSSSHJobOptions() {
        init();
    }

    private void init() {
        objParentClass = this.getClass();
    }

    @Deprecated
    public SOSSSHJobOptions(final JSListener pobjListener) {
        super(pobjListener);
        init();
    }

    public SOSSSHJobOptions(final HashMap<String, String> JSSettings) throws Exception {
        super(JSSettings);
        init();
        setChildClasses(JSSettings, "");
    }

    @Override
    public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
        super.setChildClasses(pobjJSSettings, pstrPrefix);
    }

    @Override
    public void CheckMandatory() {
        command.command_delimiter.Value(command_delimiter.Value());
        super.CheckMandatory();
        if (!(auth_method.isPassword() || auth_method.isPublicKey())) {
            throw new JSExceptionMandatoryOptionMissing("ErrSSH010 invalid or no AuthenticationMethod specified");
        }
        if (auth_method.isPassword() && password.IsEmpty()) {
            throw new JSExceptionMandatoryOptionMissing(
                    "ErrSSH020 AuthenticationMethod 'password' requires a Password, but no password was specified");
        }
        if (auth_method.isPublicKey()) {
            if (auth_file.IsNotEmpty()) {
                auth_file.CheckMandatory(true);
            } else {
                throw new JSExceptionMandatoryOptionMissing(
                        "ErrSSH050 AuthenticationMethod 'publickey' requires a keyfile, but no keyfile was specified");
            }
        }
    }

    public boolean commandSpecified() {
        return command.isDirty() && command_script.isDirty() && command_script_file.isDirty();
    }

    public boolean isScript() throws Exception {
        return !command_script.IsEmpty() || !command_script_file.IsEmpty();
    }

}