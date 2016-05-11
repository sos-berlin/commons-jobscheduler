package com.sos.dialog.interfaces;

import org.eclipse.swt.widgets.Composite;

/** @author KB */
public interface ISOSTabItem {

    public void createTabItemComposite();

    public void createComposite();

    public boolean validateData();

    public boolean setParent(final Composite pobjParent);

}