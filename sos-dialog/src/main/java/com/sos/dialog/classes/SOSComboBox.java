/**
 *
 */
package com.sos.dialog.classes;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** @author KB */
public class SOSComboBox extends CCombo {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id: SOSComboBox.java 23878 2014-04-22 18:02:41Z kb $";
    @SuppressWarnings("unused")
    private Logger logger = Logger.getLogger(this.getClass());

    private Vector<Control> objControlList = new Vector<>();

    /**
	 *
	 */
    public SOSComboBox(final Composite parent, final int style) {
        super(parent, SWT.CHECK | SWT.FLAT);
        addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                setEnabledDisabled();
            }
        });
    }

    public void addChild(final Control pobjC) {
        objControlList.add(pobjC);
    }

    public void setEnabledDisabled() {
        boolean flgT = true;
        // if (getSelection() == true) {
        // }
        // else {
        // flgT = false;
        // }
        for (Control objC : objControlList) {
            objC.setEnabled(flgT);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        objControlList = null;
        logger = null;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
