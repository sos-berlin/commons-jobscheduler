package com.sos.scheduler.converter.graphviz;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFolderName;

@JSOptionClass(name = "JSObjects2GraphvizOptionsSuperClass", description = "JSObjects2GraphvizOptionsSuperClass")
public class JSObjects2GraphvizOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -1572067790397775406L;
    private static final String CLASSNAME = "JSObjects2GraphvizOptionsSuperClass";

    public JSObjects2GraphvizOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JSObjects2GraphvizOptionsSuperClass(final JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSObjects2GraphvizOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @JSOptionDefinition(name = "live_folder_name", description = "", key = "live_folder_name", type = "SOSOptionString", mandatory = true)
    public SOSOptionFolderName live_folder_name = new SOSOptionFolderName(this, CLASSNAME + ".live_folder_name", "", " ", " ", true);

    public SOSOptionFolderName getlive_folder_name() {
        return live_folder_name;
    }

    public void setlive_folder_name(final SOSOptionFolderName p_live_folder_name) {
        live_folder_name = p_live_folder_name;
    }

    @JSOptionDefinition(name = "output_folder_name", description = "", key = "output_folder_name", type = "SOSOptionString", mandatory = true)
    public SOSOptionFolderName output_folder_name = new SOSOptionFolderName(this, CLASSNAME + ".output_folder_name", "", "", "", true);

    public SOSOptionFolderName getoutput_folder_name() {
        return output_folder_name;
    }

    public void setoutput_folder_name(final SOSOptionFolderName p_output_folder_name) {
        output_folder_name = p_output_folder_name;
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        objSettings = pobjJSSettings;
        super.setAllOptions(pobjJSSettings);
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
    public void CommandLineArgs(final String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}