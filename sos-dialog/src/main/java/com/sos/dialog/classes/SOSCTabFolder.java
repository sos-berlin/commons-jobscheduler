package com.sos.dialog.classes;

import static com.sos.dialog.Globals.MsgHandler;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.sos.JSHelper.interfaces.IDirty;
import com.sos.dialog.Globals;
import com.sos.dialog.components.CompositeBaseClass;
import com.sos.dialog.components.SOSCursor;
import com.sos.dialog.components.WaitCursor;
import com.sos.dialog.interfaces.ISOSControlProperties;
import com.sos.dialog.interfaces.ISOSTabItem;
import com.sos.dialog.layouts.Gridlayout;
import com.sos.dialog.message.ErrorLog;
import com.sos.dialog.swtdesigner.SWTResourceManager;

/** @author KB */
public class SOSCTabFolder extends CTabFolder {

    public static final String conTABITEM_I18NKEY = "key";
    public static final String conTABITEM_SOSITEM = "SOSCTABITEM";
    public static final String conCOMPOSITE_OBJECT_KEY = "composite";
    private static final Logger LOGGER = Logger.getLogger(SOSCTabFolder.class);
    public boolean ItemsHasClose = true;
    public boolean gflgCreateControlsImmediate = true;
    public boolean flgRejectTabItemSelection = false;

    public SOSCTabFolder(final Composite parent, final int style) {
        super(parent, style);
        final CTabFolder objTabF = this;
        objTabF.setSimple(true);
        setMaximizeVisible(false);
        this.setBorderVisible(false);
        setTabHeight(getTabHeight() + 6);
        this.setBackground(Globals.getCompositeBackground());
        this.setSelectionBackground(new Color[] { Globals.getFieldHasFocusBackground(), Globals.getCompositeBackground(),
                Globals.getCompositeBackground() }, new int[] { 50, 100 }, true);
        Gridlayout.set4ColumnLayout(this);

        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(final FocusEvent e) {
                // TODO validate?
            }

            @Override
            public void focusGained(final FocusEvent e) {
                LOGGER.trace("focusGained");
                SOSCTabFolder o = (SOSCTabFolder) e.widget;
                Object d = e.data;
                Object f = e.getSource();
                handleSelection();
            }
        });

        this.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                LOGGER.trace("CTabFolder Item selected");
                if (flgRejectTabItemSelection == true) {
                    return;
                }
                CTabItem objSelectedItem = getSelection();
                Composite objCurrComposite = (Composite) objSelectedItem.getControl();
                if (objCurrComposite instanceof ISOSTabItem) {
                    if (!CompositeBaseClass.gflgCreateControlsImmediate) {
                        CompositeBaseClass<?> objBC = (CompositeBaseClass<?>) objCurrComposite;
                        objBC.createTabItemComposite();
                        doResize();
                    }
                }
                CTabItem tbiLastSelected = (CTabItem) getData("lastSelected");
                if (tbiLastSelected == objSelectedItem) {
                    return;
                }
                if (tbiLastSelected != null) {
                    try {
                        Composite objC = (Composite) tbiLastSelected.getControl();
                        if (objC != null && objC.isDisposed()) {
                            objC.dispose();
                        }
                    } catch (Exception e) {
                    }
                }
                setData("lastSelected", objSelectedItem);
                event.doit = true;
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub
            }
        });
        this.addListener(SWT.MouseDoubleClick, Globals.listener);
        this.addListener(SWT.MouseDown, Globals.listener);
        this.addListener(SWT.MouseMove, Globals.listener);
        this.addListener(SWT.MouseUp, Globals.listener);
        this.addListener(SWT.MouseEnter, Globals.listener);
        this.addListener(SWT.MouseExit, Globals.listener);
        this.addListener(SWT.MouseWheel, Globals.listener);
        this.addListener(SWT.MouseHorizontalWheel, Globals.listener);
        this.addListener(SWT.MouseDoubleClick, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                maximizeToSashForm();
                event.doit = true;
            }
        });
        Menu contextMenu = new Menu(this);
        MenuItem close = new MenuItem(contextMenu, SWT.NONE);
        close.setText("Close");
        close.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                CTabItem objTabItem = getSelection();
                event.doit = closeTabItem(objTabItem);
            }
        });
        MenuItem closeOthers = new MenuItem(contextMenu, SWT.NONE);
        closeOthers.setText("Close Others");
        closeOthers.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                CTabItem objTabItem = getSelection();
                for (CTabItem objTI : getItems()) {
                    if (objTI != objTabItem) {
                        closeTabItem(objTI);
                    }
                }
            }
        });
        MenuItem closeAll = new MenuItem(contextMenu, SWT.NONE);
        closeAll.setText("Close All");
        closeAll.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                closeAllTabItems();
                event.doit = true;
            }
        });
        this.setMenu(contextMenu);
        this.addCTabFolder2Listener(new CTabFolder2Adapter() {

            public void itemClosed(final CTabFolderEvent event) {

            }

            @Override
            public void close(final CTabFolderEvent event) {
                SOSCTabItem objI = (SOSCTabItem) event.item;
                if (objI != null) {
                    event.doit = closeTabItem(objI);
                }
            }

            @Override
            public void minimize(final CTabFolderEvent event) {
            }

            @Override
            public void maximize(final CTabFolderEvent event) {
            }

            @Override
            public void restore(final CTabFolderEvent event) {
            }

            @Override
            public void showList(final CTabFolderEvent event) {
            }
        });
    }

    public void closeAllTabItems() {
        for (CTabItem objTI : getItems()) {
            closeTabItem(objTI);
        }
    }

    private boolean closeTabItem(final CTabItem pobjTabItem) {
        boolean flgDoClose = true;
        try {
            int buttonID = SWT.NO;

            if (pobjTabItem != null) {
                Object objO = pobjTabItem.getData();
                if (objO instanceof IDirty) {
                    IDirty objDH = (IDirty) objO;
                    if (objDH.isDirty()) {
                    } else {
                        buttonID = SWT.NO;
                    }
                }
                switch (buttonID) {
                case SWT.CANCEL:
                    flgDoClose = false;
                    break;
                case SWT.YES:
                case SWT.NO:
                    Control objCOT = (Control) pobjTabItem.getData(conCOMPOSITE_OBJECT_KEY);
                    if (objCOT != null) {
                        pobjTabItem.setControl(null);
                        objCOT.dispose();
                        objCOT = null;
                    }
                    if (pobjTabItem instanceof SOSCTabItem) {
                        ((SOSCTabItem) pobjTabItem).dispose();
                    } else {
                        pobjTabItem.dispose();
                    }
                    LOGGER.debug("tabitem disposed");
                    flgDoClose = true;
                    break;
                default:
                    break;
                }
            }
            return flgDoClose;
        } catch (Exception e) {
            new ErrorLog("problem", e);
        }
        return flgDoClose;
    }

    private void handleSelection() {
        try (SOSCursor objCurs = new SOSCursor().showWait()) {
            LOGGER.debug("handleSelection");
            CTabItem objSelectedItem = getSelection();
            CTabItem tbiLastSelected = (CTabItem) getData("lastSelected");
            if (tbiLastSelected != null) {
                Composite objC = (Composite) tbiLastSelected.getControl();
            }
            setData("lastSelected", objSelectedItem);
            Composite objComposite = (Composite) objSelectedItem.getData(conCOMPOSITE_OBJECT_KEY);
            if (objComposite instanceof ISOSTabItem) {
                ISOSTabItem objCurrTab = (ISOSTabItem) objComposite;
            }
            doResize();
        } catch (Exception e) {
            new ErrorLog("problem", e);
        } finally {
        }
    }

    public SOSCTabItem getTabItem(final Object pobjObject) {
        SOSCTabItem objT = null;
        for (CTabItem objTI : this.getItems()) {
            if (objTI.getData().equals(pobjObject)) {
                objT = (SOSCTabItem) objTI.getData(conTABITEM_SOSITEM);
                return objT;
            }
        }
        return objT;
    }

    public SOSCTabItem getTabItem(final String pstrCaption, final ISOSTabItem pobjTabComposite) {
        SOSCTabItem objTab = getTabItem(pstrCaption);
        objTab.setData(conCOMPOSITE_OBJECT_KEY, pobjTabComposite);
        return objTab;
    }

    public SOSCTabItem getTabItem(final String pstrI18NKey) {
        SOSCTabItem objTabItem = null;
        try (WaitCursor objWC = new WaitCursor()) {
            for (CTabItem objTI : this.getItems()) {
                String strI18NKey = (String) objTI.getData(conTABITEM_I18NKEY);
                if (strI18NKey != null && strI18NKey.equals(pstrI18NKey)) {
                    objTabItem = (SOSCTabItem) objTI.getData(conTABITEM_SOSITEM);
                    return objTabItem;
                }
            }
            objTabItem = new SOSCTabItem(this, SWT.NONE);
            SOSMsgControl objMsg = MsgHandler.newMsg(pstrI18NKey);
            objMsg.Control(objTabItem);
            objTabItem.setData(conTABITEM_I18NKEY, pstrI18NKey);
            objTabItem.setFont(Globals.stFontRegistry.get("tabitem-text"));
            objTabItem.setImage(SWTResourceManager.getImageFromResource(objMsg.icon()));
            objTabItem.setShowClose(ItemsHasClose);
            objTabItem.setToolTipText(objMsg.tooltip());
            Composite composite = new SOSComposite(this, SWT.None);
            objTabItem.setControl(composite);
            objTabItem.setData(conCOMPOSITE_OBJECT_KEY, composite);
            objTabItem.setData(conTABITEM_SOSITEM, objTabItem);
            Gridlayout.set4ColumnLayout(composite);
        } catch (Exception e) {
            new ErrorLog("problem", e);
        }
        return objTabItem;
    }

    public SOSCTabItem setFocus(final Object pobjObject) {
        SOSCTabItem objTI = getTabItem(pobjObject);
        for (CTabItem objT : this.getItems()) {
            Object objO = objT.getData();
            if (objO != null && objO.equals(pobjObject)) {
                this.setSelection(objT);
                return objTI;
            }
        }
        return null;
    }

    private void doHandleEvent(final SelectionEvent arg0) {
        CTabItem objTI = (CTabItem) arg0.item;
        LOGGER.debug(objTI.getText() + " selected");
        Object objCP = objTI.getData();
        if (objCP != null && objCP instanceof ISOSControlProperties) {
            ISOSControlProperties objC2 = (ISOSControlProperties) objCP;
            objC2.selectChild();
        }
        handleSelection();
        if ((arg0.stateMask & SWT.CTRL) != 0) {
            maximizeToSashForm();
        }
    }

    private void maximizeToSashForm() {
        Object objO1 = this.getParent();
        if (objO1 instanceof SashForm) {
            SashForm objSash = (SashForm) objO1;
            if (objSash.getMaximizedControl() != null) {
                objSash.setMaximizedControl(null);
            } else {
                objSash.setMaximizedControl(this);
            }
        }
    }

    public void activateTab(final String pstrTabKey) {
        for (CTabItem objTabItem : getItems()) {
            String strT = (String) objTabItem.getData(conTABITEM_I18NKEY);
            if (strT != null && strT.equalsIgnoreCase(pstrTabKey)) {
                ISOSTabItem objCurrTab = (ISOSTabItem) objTabItem.getData(conCOMPOSITE_OBJECT_KEY);
                if (objCurrTab != null) {
                    objCurrTab.createTabItemComposite();
                    doResize();
                }
                break;
            }
        }
    }

    public void doResize() {
        if (getParent() != null) {
            globalShell().setRedraw(false);
            globalShell().layout(true, true);
            globalShell().redraw();
            globalShell().update();
            globalShell().setRedraw(true);
        }
    }

    @Override
    public void setSelection(final int intTabIndex) {
        super.setSelection(intTabIndex);
        globalShell().layout(true, true);
    }

    private Shell globalShell() {
        return Display.getCurrent().getActiveShell();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
