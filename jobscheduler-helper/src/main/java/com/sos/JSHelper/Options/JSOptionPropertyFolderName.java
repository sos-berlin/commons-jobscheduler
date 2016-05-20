package com.sos.JSHelper.Options;

public class JSOptionPropertyFolderName extends SOSOptionFolderName {

    private static final long serialVersionUID = -5962741104646578748L;
    private static final String CLASSNAME = "JSOptionPropertyFolderName";

    public JSOptionPropertyFolderName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription,
            final String pPstrValue, final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, "Java-Property user.dir", pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    @Override
    public void setValue(String pstrFolderName) {
        final String conMethodName = CLASSNAME + "::Value";
        pstrFolderName = objParentClass.checkIsFileWritable(pstrFolderName, conMethodName);
        super.setValue(pstrFolderName);
        if (isNotEmpty(strValue)) {
            System.setProperty("user.dir", strValue);
        }
    }

    @Override
    public String getValue() {
        return System.getProperty("user.dir");
    }

}