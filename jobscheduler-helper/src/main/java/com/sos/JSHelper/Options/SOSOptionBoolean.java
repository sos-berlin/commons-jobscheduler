package com.sos.JSHelper.Options;

public class SOSOptionBoolean extends SOSOptionElement {

    private static final long serialVersionUID = -955477664516893069L;
    public final String ControlType = "checkbox";

    @Override
    public String getControlType() {
        return ControlType;
    }

    public SOSOptionBoolean(final JSOptionsClass pobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        intOptionType = isOptionTypeBoolean;
        this.Value(pPstrValue);
        this.setNotDirty();
    }

    @Override
    public void Value(final String pstrValue) {
        if (isNotEmpty(pstrValue)) {
            super.Value(pstrValue);
            flgValue = String2Bool();
        } else {
            super.Value("");
        }
        flgValue = string2Bool(strValue);
    }

    public void value(final boolean pflgValue) {
        if (pflgValue != flgValue) {
            flgValue = pflgValue;
            if (pflgValue) {
                this.Value("true");
            } else {
                this.Value("false");
            }
        }
    }

    public boolean value() {
        return flgValue;
    }

    public boolean isTrue() {
        return flgValue;
    }

    public boolean isFalse() {
        return !flgValue;
    }

    public void setTrue() {
        this.value(true);
    }

    public void setFalse() {
        this.value(false);
    }

}
