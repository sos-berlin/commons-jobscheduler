/**
 *
 */
package com.sos.dialog.classes;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sos.dialog.Globals;

/** @author KB */
public class SOSSashForm extends SashForm {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id: SOSSashForm.java 23811 2014-04-15 15:45:10Z kb $";
    private final Vector<Control> objControlList = new Vector<Control>();

    private final WindowsSaver objPersistenceStore;
    private final SashForm objSash = this;

    /**
	 *
	 */
    public SOSSashForm(final Composite parent, final int style, final String strPersistenceStoreKey) {
        super(parent, style);

        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        setSashWidth(6);
        setBounds(7, 0, 0, 0);
        this.setBackground(Globals.getCompositeBackground());

        objPersistenceStore = new WindowsSaver(this.getClass(), parent.getShell(), 940, 600);
        objPersistenceStore.setKey(strPersistenceStoreKey);

        addControlListener(new ControlAdapter() {

            @Override
            public void controlResized(final ControlEvent e) {
                logger.trace("sashForm resized");
            }
        });
        addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                saveSize();
            }
        });

    }

    public void restoreSize() {
        objPersistenceStore.loadSash(objSash);
    }

    public void saveSize() {
        objPersistenceStore.saveSash(objSash);
    }

    public void addChild(final Control pobjC) {
        objControlList.add(pobjC);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
