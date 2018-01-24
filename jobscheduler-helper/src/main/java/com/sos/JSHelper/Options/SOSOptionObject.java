package com.sos.JSHelper.Options;

public class SOSOptionObject extends SOSOptionElement {

    private static final long serialVersionUID = -955477664516893069L;
    private Object _value;

    public SOSOptionObject(final JSOptionsClass parent, final String key, final String description, final String value, final String defaultValue,
            final boolean isMandatory) {
        super(parent, key, description, value, defaultValue, isMandatory);
        this.setValue(value);
        this.setNotDirty();
    }

    public void value(final Object o) {
        _value = o;
    }

    public Object value() {
        return _value;
    }
}