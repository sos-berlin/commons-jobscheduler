package com.sos.JSHelper.Options;

/** @author KB */
public class JSJobName extends SOSOptionString {

    private static final long serialVersionUID = 8113399938792071983L;

    public JSJobName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public void setValue(final String jobFolder, final String jobName) {
        this.setValue(new String(jobFolder + "/").replaceAll("\\", "/").replaceAll("/{2,}", "/") + jobName);
    }

}