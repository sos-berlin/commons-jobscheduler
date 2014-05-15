/**
 *
 */
package com.sos.dialog.classes;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author KB
 *
 */
public class SOSCheckBox extends Button {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());

	private final Vector <Control> objControlList = new Vector <> ();

	/**
	 *
	 */
	public SOSCheckBox(final Composite parent, final int style) {
		super(parent,SWT.CHECK | SWT.FLAT);
		addSelectionListener(new SelectionAdapter() {
			@Override public void widgetSelected(final SelectionEvent e) {
				setEnabledDisabled();
			}
		});
	}

	public void addChild (final Control pobjC) {
		objControlList.add(pobjC);
	}

	public void setEnabledDisabled () {
		boolean flgT = true;
		if (getSelection() == true) {
		}
		else {
			flgT = false;
		}
		for (Control objC : objControlList) {
			objC.setEnabled(flgT);
		}
	}
	@Override protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
