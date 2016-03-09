package com.sos.dialog.auth;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SOSLoginDialog extends Dialog {

    protected Object result;
    protected Shell shell;
    private String user;
    private String password;
    private boolean rememberMe;
    private SOSLoginForm loginForm;
    private boolean cancel;
    private String message = "";

    /** Create the dialog.
     * 
     * @param parent
     * @param style */
    public SOSLoginDialog(Shell parent, int style) {
        super(parent, style);
    }

    /** Open the dialog.
     * 
     * @return the result */
    public Object open() {
        createContents();
        shell.open();
        shell.layout();
        Display display = getParent().getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return result;
    }

    /** Create contents of the dialog. */
    private void createContents() {
        cancel = false;
        // shell = new Shell(getParent(), SWT.ON_TOP | SWT.DIALOG_TRIM |
        // SWT.APPLICATION_MODAL);
        shell = new Shell(getParent(), SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.OK | SWT.APPLICATION_MODAL);
        shell.setSize(300, 180);
        shell.setText(getText());
        shell.setLayout(new FillLayout(SWT.HORIZONTAL));

        loginForm = new SOSLoginForm(shell, SWT.NONE);
        loginForm.setMsg(message);
        loginForm.getBtnOk().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                user = loginForm.getUser().getText();
                password = loginForm.getPassword().getText();
                shell.close();
                // rememberMe = loginForm.getBtnRememberMe().getSelection();

            }
        });

        loginForm.getBtnCancel().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                user = null;
                cancel = true;
                password = null;
                shell.close();
            }
        });
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void close() {
        shell.close();
    }

    public void setMsg(String msg) {
        message = msg;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
