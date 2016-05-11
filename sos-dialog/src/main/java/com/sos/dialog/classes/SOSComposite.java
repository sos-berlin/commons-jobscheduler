package com.sos.dialog.classes;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sos.dialog.Globals;

/** @author KB */
public class SOSComposite extends Composite {

    private final Vector<Control> objControlList = new Vector<Control>();

    public static SOSComposite newComposite(final Control comp) {
        return new SOSComposite((Composite) comp, SWT.None);
    }

    public SOSComposite(final Composite parent, final int style) {
        super(parent, SWT.None);
        this.setBackground(Globals.getCompositeBackground());
        GridLayout gridLayout = new GridLayout(1, false);
        setLayout(gridLayout);
    }

    public void addChild(final Control pobjC) {
        objControlList.add(pobjC);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void dispose() {
        super.dispose();
    }

}