package com.sos.localization;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionString;

@JSOptionClass(name = "PropertyFactoryOptionsSuperClass", description = "PropertyFactoryOptionsSuperClass")
public class PropertyFactoryOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    private static final String CLASSNAME = "PropertyFactoryOptionsSuperClass";

    @JSOptionDefinition(name = "Operation", description = "", key = "Operation", type = "SOSOptionString", mandatory = false)
    public SOSOptionString Operation = new SOSOptionString(this, CLASSNAME + ".Operation", "", "merge", "merge", false);

    public SOSOptionString getOperation() {
        return Operation;
    }

    public SOSOptionString Operation() {
        return Operation;
    }

    public void setOperation(SOSOptionString p_Operation) {
        this.Operation = p_Operation;
    }

    public void Operation(SOSOptionString p_Operation) {
        this.Operation = p_Operation;
    }

    @JSOptionDefinition(name = "PropertyFileNamePrefix", description = "", key = "PropertyFileNamePrefix", type = "SOSOptionString", mandatory = true)
    public SOSOptionString PropertyFileNamePrefix = new SOSOptionString(this, CLASSNAME + ".PropertyFileNamePrefix", "", " ", " ", true);

    public SOSOptionString getPropertyFileNamePrefix() {
        return PropertyFileNamePrefix;
    }

    public SOSOptionString PropertyFileNamePrefix() {
        return PropertyFileNamePrefix;
    }

    public void setPropertyFileNamePrefix(SOSOptionString p_PropertyFileNamePrefix) {
        this.PropertyFileNamePrefix = p_PropertyFileNamePrefix;
    }

    public void PropertyFileNamePrefix(SOSOptionString p_PropertyFileNamePrefix) {
        this.PropertyFileNamePrefix = p_PropertyFileNamePrefix;
    }

    @JSOptionDefinition(name = "SourceFolderName", description = "The Folder, which has all the I18N Property files.", key = "SourceFolderName", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName SourceFolderName = new SOSOptionFolderName(this, CLASSNAME + ".SourceFolderName",
            "The Folder, which has all the I18N Property files.", " ", " ", true);

    public SOSOptionFolderName getSourceFolderName() {
        return SourceFolderName;
    }

    public SOSOptionFolderName SourceFolderName() {
        return SourceFolderName;
    }

    public void setSourceFolderName(SOSOptionFolderName p_SourceFolderName) {
        this.SourceFolderName = p_SourceFolderName;
    }

    public void SourceFolderName(SOSOptionFolderName p_SourceFolderName) {
        this.SourceFolderName = p_SourceFolderName;
    }

    public PropertyFactoryOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public PropertyFactoryOptionsSuperClass(HashMap<String, String> JSSettings) {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
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