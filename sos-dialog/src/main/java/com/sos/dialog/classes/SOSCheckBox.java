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

import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.dialog.Globals;

/** @author KB */
public class SOSCheckBox extends Button {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());

    private final Vector<Object> objControlList = new Vector<>();

    /**
	 *
	 */
    public SOSCheckBox(final Composite parent, final int style) {
        super(parent, SWT.CHECK | SWT.FLAT);
        addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                // setEnabledDisabled();
            }
        });
    }

    public void addChild(final Object... pobjC) {
        for (Object control : pobjC) {
            objControlList.add(control);
        }
    }

    public void addChild(final Control pobjC) {
        objControlList.add(pobjC);
    }

    public Vector<Object> getControlList() {
        return objControlList;
    }

    public void setOneOfUs() {
        if (getSelection() == true) {
            for (Object objC : objControlList) {
                if (objC == this) {
                    // nothing to do
                } else {
                    if (objC instanceof SOSCheckBox) {
                        SOSCheckBox objBx = (SOSCheckBox) objC;
                        objBx.setSelection(false);
                    }
                }
            }
        }

    }

    // disable all fields which are "childs" of this checkbox.
    public void setEnabledDisabled() {
        boolean flgT = true;
        if (getSelection() == true) {
        } else {
            flgT = false;
        }
        for (Object objO : objControlList) {
            if (objO instanceof Control) {
                Control objC = (Control) objO;
                objC.setEnabled(flgT);
                setNoFocusColor(objC);
            }
        }
    }

    public void setNoFocusColor(final Control objControl) {
        SOSOptionElement objOptionElement1 = (SOSOptionElement) objControl.getData();
        if (objOptionElement1.isProtected() == true || objControl.isEnabled() == false) {
            objControl.setBackground(Globals.getProtectedFieldColor());
        } else {
            if (objControl.isEnabled()) {
                objControl.setBackground(Globals.getFieldBackground());
            } else {
                objControl.setBackground(Globals.getFieldBackground4Disabled());

            }
        }
        if (objOptionElement1.getControlType().equals("checkbox")) {
            objControl.setBackground(Globals.getCompositeBackground());
        }
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
