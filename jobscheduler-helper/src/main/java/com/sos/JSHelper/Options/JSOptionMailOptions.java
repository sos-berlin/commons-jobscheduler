package com.sos.JSHelper.Options;

public class JSOptionMailOptions extends SOSOptionElement {

    private static final long serialVersionUID = 6484264503878247054L;
    private JSMailOptions objMailOptions = null;

    public JSOptionMailOptions(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        super.intOptionType = isOptionTypeOptions;
        objMailOptions = new JSMailOptions();
    }

    public void setValue(JSMailOptions pobjOptionsClass) {
        objMailOptions = pobjOptionsClass;
    }

    public JSMailOptions value() {
        if (objMailOptions == null) {
            objMailOptions = new JSMailOptions();
        }
        return objMailOptions;
    }

}