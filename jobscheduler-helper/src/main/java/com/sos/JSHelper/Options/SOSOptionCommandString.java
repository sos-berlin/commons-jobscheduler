package com.sos.JSHelper.Options;

/** @author KB */
public class SOSOptionCommandString extends SOSOptionHexString {

    private static final long serialVersionUID = 2326011361040152247L;
    private final String conClassName = "SOSOptionCommandString";
    public SOSOptionRegExp command_delimiter = new SOSOptionRegExp(null, conClassName + ".command_delimiter", 
            "Command delimiter characters are specified using this par", "%%", "%%", true);

    public SOSOptionCommandString(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public String[] values() throws Exception {
        return this.getValue().split(command_delimiter.getValue());
    }

    public String[] split() throws Exception {
        return this.getValue().split(";");
    }

}