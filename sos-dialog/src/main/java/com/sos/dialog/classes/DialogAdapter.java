package com.sos.dialog.classes;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.sos.dialog.interfaces.ICompositeBaseAbstract;
import com.sos.dialog.interfaces.IDialogActionHandler;
import com.sos.dialog.menu.SOSMenueEvent;
import com.sos.dialog.message.ErrorLog;

/** @author KB */
public class DialogAdapter extends Dialog {

    protected Object result;
    protected Shell shell;
    private WindowsSaver objFormHelper;
    private IDialogActionHandler objDialogActionHandler = null;
    private Composite objC = null;
    private GridLayout gridLayout = null;
    private GridData GridData4Column = null;

    public DialogAdapter(final Shell parent, final int style) {
        super(parent, style);
        init(parent);
    }

    public DialogAdapter(final Shell parent, final String pstrPreferenceStoreKey) {
        super(parent, SWT.NONE);
        init(parent);
        objFormHelper.setKey(pstrPreferenceStoreKey);
    }

    private void init(final Shell parent) {
        shell = new Shell(parent, SWT.ON_TOP | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
        objFormHelper = new WindowsSaver(this.getClass(), shell, 643, 600);
        setText("SWT Dialog");
    }

    public Object open(final IDialogActionHandler pobjDialogActionHandler, final ICompositeBaseAbstract objChildComposite) {
        shell.setRedraw(false);
        shell.open();
        set4ColumnLayout(shell);
        if (objC == null) {
            objC = createContents();
        }
        objDialogActionHandler = pobjDialogActionHandler;
        objChildComposite.createComposite(objC);
        set4ColumnLayout((Composite) objChildComposite);
        ((Group) objC).setText(objChildComposite.getWindowTitle());
        shell.setText(objChildComposite.getWindowTitle());
        objC.layout(true, true);
        shell.layout(true, true);
        shell.setRedraw(true);
        return null;
    }

    public Object open(final IDialogActionHandler pobjDialogActionHandler) {
        objDialogActionHandler = pobjDialogActionHandler;
        return open();
    }

    private void saveWindowPosAndSize() {
        objFormHelper.saveWindow();
    }

    public Object open() {
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    public Composite createContents() {
        objFormHelper = new WindowsSaver(this.getClass(), shell, 400, 200);
        objFormHelper.restoreWindow();
        shell.addListener(SWT.Traverse, new Listener() {

            @Override
            public void handleEvent(final Event event) {
                switch (event.detail) {
                case SWT.TRAVERSE_ESCAPE:
                    closeShell();
                    event.detail = SWT.TRAVERSE_NONE;
                    event.doit = false;
                    break;
                }
            }
        });
        shell.addControlListener(new ControlAdapter() {

            @Override
            public void controlMoved(final ControlEvent e) {
                saveWindowPosAndSize();
            }

            @Override
            public void controlResized(final ControlEvent e) {
                saveWindowPosAndSize();
            }
        });
        Composite composite = new Group(shell, SWT.NONE | SWT.RESIZE);
        set4ColumnLayout(composite);
        composite.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                //
            }
        });
        composite.layout(true, true);
        Group grp2 = new Group(shell, SWT.None);
        grp2.setLayout(new GridLayout(4, false));
        grp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
        SOSButton btnOK = new SOSButton(grp2, "Dialog_L_ok");
        shell.setDefaultButton(btnOK);
        btnOK.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 2, 1));
        btnOK.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                doOK();
            }
        });
        SOSButton btnCancel = new SOSButton(grp2, "Dialog_L_cancel");
        btnCancel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
        btnCancel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                doCancel();
            }
        });
        objC = composite;
        return composite;
    }

    public Shell getShell() {
        return shell;
    }

    public void set4ColumnLayout(final Composite objC) {
        objC.setLayout(get4ColumnLayout());
        if (GridData4Column == null) {
            GridData4Column = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        }
        objC.setLayoutData(GridData4Column);
    }

    public GridLayout get4ColumnLayout() {
        if (gridLayout == null) {
            gridLayout = new GridLayout(1, true);
            gridLayout.makeColumnsEqualWidth = true;
            gridLayout.horizontalSpacing = 4;
            gridLayout.verticalSpacing = 5;
            gridLayout.marginBottom = 0;
            gridLayout.marginLeft = 1;
            gridLayout.marginRight = 1;
            gridLayout.marginTop = 2;
            gridLayout.marginHeight = 2;
        }
        return gridLayout;
    }

    public void doCancel() {
        SOSMenueEvent objME = new SOSMenueEvent();
        objDialogActionHandler.doCancel(objME);
        if (objME.doIt) {
            closeShell();
        }
    }

    public void doEdit() {
        SOSMenueEvent objME = new SOSMenueEvent();
        objDialogActionHandler.doEdit(objME);
        if (objME.doIt) {
            closeShell();
        }
    }

    public void doOK() {
        SOSMenueEvent objME = new SOSMenueEvent();
        objDialogActionHandler.doOK(objME);
        if (objME.doIt) {
            closeShell();
        } else {
            new ErrorLog(objME.getMessage());
        }
    }

    public void doDelete() {
        SOSMenueEvent objME = new SOSMenueEvent();
        objDialogActionHandler.doDelete(objME);
        if (objME.doIt) {
            closeShell();
        }
    }

    public void doClose() {
        SOSMenueEvent objME = new SOSMenueEvent();
        objDialogActionHandler.doClose(objME);
        if (objME.doIt) {
            closeShell();
        }
    }

    private void closeShell() {
        shell.close();
    }

    public boolean doValidation() {
        SOSMenueEvent objME = new SOSMenueEvent();
        objDialogActionHandler.doValidation(objME);
        if (objME.doIt) {
            closeShell();
            return true;
        }
        return true;
    }

}