package com.sos.localization;

import java.util.HashMap;

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

    public void setOperation(SOSOptionString p_Operation) {
        this.Operation = p_Operation;
    }

    @JSOptionDefinition(name = "PropertyFileNamePrefix", description = "", key = "PropertyFileNamePrefix", type = "SOSOptionString", mandatory = true)
    public SOSOptionString propertyFileNamePrefix = new SOSOptionString(this, CLASSNAME + ".PropertyFileNamePrefix", "", " ", " ", true);

    public SOSOptionString getPropertyFileNamePrefix() {
        return propertyFileNamePrefix;
    }

    public void setPropertyFileNamePrefix(SOSOptionString pPropertyFileNamePrefix) {
        this.propertyFileNamePrefix = pPropertyFileNamePrefix;
    }

    @JSOptionDefinition(name = "SourceFolderName", description = "The Folder, which has all the I18N Property files.", key = "SourceFolderName", type = "SOSOptionFolderName", mandatory = true)
    public SOSOptionFolderName sourceFolderName = new SOSOptionFolderName(this, CLASSNAME + ".SourceFolderName",
            "The Folder, which has all the I18N Property files.", " ", " ", true);

    public SOSOptionFolderName getSourceFolderName() {
        return sourceFolderName;
    }

    public void setSourceFolderName(SOSOptionFolderName pSourceFolderName) {
        this.sourceFolderName = pSourceFolderName;
    }

    public PropertyFactoryOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public PropertyFactoryOptionsSuperClass(HashMap<String, String> JSSettings) {
        this();
        this.setAllOptions(JSSettings);
    }

    @Override
    public void setAllOptions(HashMap<String, String> settings) {
        super.setAllOptions(settings);
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
    public void commandLineArgs(String[] pstrArgs) {
        super.commandLineArgs(pstrArgs);
        this.setAllOptions(super.getSettings());
    }

}