package com.sos.JSHelper.Options;

public class SOSOptionBoolean extends SOSOptionElement {

    private static final long serialVersionUID = -955477664516893069L;
    public final String ControlType = "checkbox";

    public SOSOptionBoolean(final JSOptionsClass pobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        intOptionType = isOptionTypeBoolean;
        this.setValue(pPstrValue);
        this.setNotDirty();
    }

    @Override
    public String getControlType() {
        return ControlType;
    }

    @Override
    public void setValue(final String pstrValue) {
        if (isNotEmpty(pstrValue)) {
            super.setValue(pstrValue);
            flgValue = string2Bool();
        } else {
            super.setValue("");
        }
        flgValue = string2Bool(strValue);
    }

    public void value(final boolean pflgValue) {
        if (pflgValue != flgValue) {
            flgValue = pflgValue;
            if (pflgValue) {
                this.setValue("true");
            } else {
                this.setValue("false");
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