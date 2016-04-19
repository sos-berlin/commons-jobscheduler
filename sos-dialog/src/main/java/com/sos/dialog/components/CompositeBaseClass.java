/**
 *
 */
package com.sos.dialog.components;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

import com.sos.JSHelper.Options.IValueChangedListener;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.dialog.Globals;
import com.sos.dialog.classes.SOSCTabFolder;
import com.sos.dialog.classes.SOSCTabItem;
import com.sos.dialog.classes.SOSCheckBox;
import com.sos.dialog.interfaces.ICompositeBaseAbstract;
import com.sos.dialog.interfaces.ISOSTabItem;
import com.sos.dialog.layouts.Gridlayout;

/** @author KB */
public abstract class CompositeBaseClass<T> extends Composite implements ISOSTabItem, ICompositeBaseAbstract {

    protected Logger logger = Logger.getLogger(this.getClass());
    protected Composite objParent = null;
    protected T objJadeOptions = null;
    protected ControlCreator objCC = null;
    protected Composite composite = this;
    protected boolean flgCompositeIsCreated = false;
    private final String conClassName = this.getClass().getSimpleName();
    private static int intCompositeStyle = SWT.None;
    private String strWindowTitle = "WindowTitle";
    public static boolean gflgCreateControlsImmediate = true;

    public CompositeBaseClass(final SOSCTabFolder pobjCTabFolder, final T objOptions) {
        super(pobjCTabFolder, intCompositeStyle);
        objJadeOptions = objOptions;
        getControlCreator(this);
    }

    public CompositeBaseClass(final Composite parent, final T objOptions) {
        super(parent, intCompositeStyle);
        objJadeOptions = objOptions;
        getControlCreator(this);
    }

    public CompositeBaseClass(final Composite parent) {
        super(parent, SWT.None);
        getControlCreator(this);
    }

    private void getControlCreator(final Composite pobjParentComposite) {
        composite = pobjParentComposite;
        objParent = pobjParentComposite;
        setLayout(Gridlayout.get4ColumnLayout());
        objCC = new ControlCreator(this);
        setBackground(Globals.getCompositeBackground());
    }

    @Override
    public boolean setParent(final Composite pobjParent) {
        super.setParent(pobjParent);
        return true;
    }

    @Override
    public void createTabItemComposite() {
        try (WaitCursor objWC = new WaitCursor()) {
            Globals.redraw(false);
            if (!flgCompositeIsCreated) {
                objCC = new ControlCreator(composite);
                createComposite();
                logger.debug("createTabItemComposite " + conClassName);
                doResize();
            }
        } catch (Exception e) {
        } finally {
            flgCompositeIsCreated = true;
            Globals.redraw(true);
        }
    }

    protected void doResize() {
        Globals.redraw(true);
    }

    protected SelectionAdapter EnableOneOfUsOrNoneListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            setOneOfUsOrNone(e);
        }
    };

    public void setOneOfUsOrNone(final SelectionEvent e) {
        Object objO = e.getSource();
        if (objO instanceof SOSCheckBox) {
            SOSCheckBox objCB = (SOSCheckBox) objO;
            if (objCB.getSelection()) {
                for (Object objC1 : objCB.getControlList()) {
                    if (objC1 instanceof SOSOptionElement) {
                        SOSOptionElement objBx = (SOSOptionElement) objC1;
                        if ("true".equalsIgnoreCase(objBx.Value())) {
                            objBx.Value("false");
                        }
                    }
                }
            }
        }

    }

    protected SelectionAdapter EnableFieldsListener = new SelectionAdapter() {

        @Override
        public void widgetSelected(final SelectionEvent e) {
            enableFields();
        }
    };

    protected void enableFields() {
    }

    @Override
    public boolean validateData() {
        return true;
    }

    @Override
    public void dispose() {
        if (!composite.isDisposed()) {
            for (Control objContr : composite.getChildren()) {
                Object objO = objContr.getData();
                if (objO instanceof IValueChangedListener) {
                    SOSOptionElement objV = (SOSOptionElement) objO;

                }
                Listener[] objL = objContr.getListeners(SWT.ALL);
                for (Listener listener : objL) {
                    objContr.removeListener(SWT.ALL, listener);
                }
            }
            super.dispose();
            logger = null;
            objCC = null;
            objJadeOptions = null;
            composite.dispose();
        }
    }

    protected ISOSTabItem createTab(SOSCTabFolder pobjMainTabFolder, Composite pobjComposite, final String pstrI18NKey) {
        SOSCTabItem tbtmItem = pobjMainTabFolder.getTabItem(pstrI18NKey);
        tbtmItem.setComposite((ISOSTabItem) pobjComposite);
        tbtmItem.setControl(pobjComposite);
        return (ISOSTabItem) pobjComposite;
    }

    @Override
    public void createGroup(final Composite parent) {
    }

    @Override
    public void init() {
    }

    @Override
    public Composite createComposite(final Composite parent) {
        return this;
    }

    @Override
    public String getWindowTitle() {
        return strWindowTitle;
    }

    @Override
    public void setWindowTitle(final String pstrWindowTitle) {
        strWindowTitle = pstrWindowTitle;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}