package com.sos.JSHelper.Options;

public class SOSOptionString extends SOSOptionElement {

    private static final long serialVersionUID = -7931744980509663560L;
    public final String ControlType = "text";

    public SOSOptionString(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        this.intOptionType = isOptionTypeString;
    }

    public SOSOptionString(final String pstrValue) {
        this(null, "", "", pstrValue, pstrValue, false);
    }

}