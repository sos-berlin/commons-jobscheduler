package com.sos.JSHelper.Options;

/** @author KB */
public interface IValueChangedListener {

    public void valueHasChanged(final SOSOptionElement pobjOptionElement);

    public void validationError(final SOSValidationError pobjVE);

}