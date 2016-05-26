package com.sos.dialog.classes;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/** @author KB */
public class SOSComboBox extends CCombo {

    private Vector<Control> objControlList = new Vector<>();

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
        for (Control objC : objControlList) {
            objC.setEnabled(flgT);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        objControlList = null;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}