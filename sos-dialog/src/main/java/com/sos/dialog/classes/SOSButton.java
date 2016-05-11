package com.sos.dialog.classes;

import static com.sos.dialog.Globals.MsgHandler;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sos.dialog.message.DialogMsg;
import com.sos.dialog.swtdesigner.SWTResourceManager;
import com.sos.localization.SOSMsg;

/** @author KB */
public class SOSButton extends Button {

    private final Vector<Control> objControlList = new Vector<>();

    public SOSButton(final Composite parent, final String pstrI18NKey) {
        super(parent, SWT.PUSH | SWT.FLAT);
        SOSMsg objM = null;
        if (pstrI18NKey.toLowerCase().startsWith("dialog_")) {
            objM = new DialogMsg(pstrI18NKey);
        } else {
            objM = MsgHandler.newMsg(pstrI18NKey);
        }
        setText(objM.label());
        setImage(SWTResourceManager.getImageFromResource(objM.icon()));
        String strAcc = objM.accelerator();
        setToolTipText(objM.tooltip());
        addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                //
            }
        });
    }

    public void addChild(final Control pobjC) {
        objControlList.add(pobjC);
    }

    public void setEnabledDisabled() {
        boolean flgT = true;
        if (!getSelection()) {
            flgT = false;
        }
        for (Control objC : objControlList) {
            objC.setEnabled(flgT);
        }
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}