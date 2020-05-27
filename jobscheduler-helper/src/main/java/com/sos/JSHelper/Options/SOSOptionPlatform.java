package com.sos.JSHelper.Options;

public class SOSOptionPlatform extends SOSOptionStringValueList {

    private static final long serialVersionUID = 1272813840437569113L;

    public enum enuValidPlatforms {
        unix, linux, hpux, aix, windows, bs2000
    }

    private enuValidPlatforms enuPlatform = null;

    public SOSOptionPlatform(final JSOptionsClass parent, final String key, final String description, final String value, final String defaultValue,
            final boolean isMandatory) {
        super(parent, key, description, value, defaultValue, isMandatory);
    }

    public boolean isBS2000() {
        return enuPlatform.equals(enuValidPlatforms.bs2000);
    }

    public boolean isWindows() {
        return enuPlatform.equals(enuValidPlatforms.windows);
    }

    public boolean isUnix() {
        return enuPlatform.equals(enuValidPlatforms.unix);
    }

    public boolean isLinux() {
        return enuPlatform.equals(enuValidPlatforms.linux);
    }

    public String getPathDelimiter() {
        if (isWindows() == true) {
            return "\\";
        } else {
            if (isBS2000() == true) {
                return "";
            }
        }
        return "/";
    }

    @Override
    public void setValue(final String val) {
        enuPlatform = enuValidPlatforms.valueOf(val);
        super.setValue(val);
    }

    @Override
    public String[] getValueList() {

        if (strValueList == null) {
            strValueList = new String[] {};
            createValueList("unix;linux;hpux;aix;windows;bs2000");
        }

        return strValueList;
    }

}
