package sos.scheduler.file;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
@JSOptionClass(name = "JobSchedulerFolderTreeOptionsSuperClass", description = "JobSchedulerFolderTreeOptionsSuperClass")
public class JobSchedulerFolderTreeOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1785419532839028016L;
    private final String conClassName = "JobSchedulerFolderTreeOptionsSuperClass";

    @JSOptionDefinition(name = "file_path", description = "This parameter is used alternatively to the parame", key = "file_path", type = "SOSOptionFileName",
            mandatory = false)
    public SOSOptionFileName file_path = new SOSOptionFileName(this, conClassName + ".file_path", "This parameter is used alternatively to the parame", " ", 
            " ", false);

    public SOSOptionFileName getfile_path() {
        return file_path;
    }

    public void setfile_path(SOSOptionFileName p_file_path) {
        this.file_path = p_file_path;
    }

    public JobSchedulerFolderTreeOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JobSchedulerFolderTreeOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JobSchedulerFolderTreeOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
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