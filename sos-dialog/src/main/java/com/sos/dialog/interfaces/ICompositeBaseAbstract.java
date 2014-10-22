package com.sos.dialog.interfaces;

import org.eclipse.swt.widgets.Composite;

public interface ICompositeBaseAbstract {

	public abstract void createGroup(final Composite parent);

	public abstract void init();

	public abstract Composite createComposite(final Composite parent);

	public abstract String getWindowTitle();

	public abstract void setWindowTitle(final String pstrWindowTitle);

}