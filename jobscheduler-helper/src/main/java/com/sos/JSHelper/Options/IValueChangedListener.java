package com.sos.JSHelper.Options;

/** @author KB */
public interface IValueChangedListener {

    public void ValueHasChanged(final SOSOptionElement pobjOptionElement);

    public void ValidationError(final SOSValidationError pobjVE);

}