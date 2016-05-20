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
    public SOSOptionFolderName liveFolderName = new SOSOptionFolderName(this, CLASSNAME + ".live_folder_name", "", " ", " ", true);

    public SOSOptionFolderName getLiveFolderName() {
        return liveFolderName;
    }

    public void setLiveFolderName(final SOSOptionFolderName pLiveFolderName) {
        liveFolderName = pLiveFolderName;
    }

    @JSOptionDefinition(name = "output_folder_name", description = "", key = "output_folder_name", type = "SOSOptionString", mandatory = true)
    public SOSOptionFolderName outputFolderName = new SOSOptionFolderName(this, CLASSNAME + ".output_folder_name", "", "", "", true);

    public SOSOptionFolderName getOutputFolderName() {
        return outputFolderName;
    }

    public void setOutputFolderName(final SOSOptionFolderName pOutputFolderName) {
        outputFolderName = pOutputFolderName;
    }

    @Override
    public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.setSettings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void checkMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.checkMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void commandLineArgs(final String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }
    
}