package com.sos.dialog.auth;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sos.dialog.swtdesigner.SWTResourceManager;

public class SOSLoginForm extends Composite {

    private Button btnOk;
    private Button btnCancel;
    private Label lblUser;
    private Label lblPassword;
    private Text user;
    private Text password;
    private Label lbMessage;

    public SOSLoginForm(Composite parent, int style) {
        super(parent, style);
        setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
        setLayout(new GridLayout(3, false));
        createContent();
    }

    private void createContent() {
        lblUser = new Label(this, SWT.NONE);
        lblUser.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblUser.setText("User");
        user = new Text(this, SWT.BORDER);
        user.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        lblPassword = new Label(this, SWT.NONE);
        lblPassword.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPassword.setText("Password");
        password = new Text(this, SWT.BORDER + SWT.PASSWORD);
        password.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        new Label(this, SWT.NONE);
        btnOk = new Button(this, SWT.NONE);
        GridData gd_btnOk = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
        gd_btnOk.widthHint = 91;
        gd_btnOk.minimumWidth = 100;
        btnOk.setLayoutData(gd_btnOk);
        btnOk.setText("Ok");
        this.getShell().setDefaultButton(btnOk);
        btnCancel = new Button(this, SWT.NONE);
        GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnCancel.minimumWidth = 100;
        gd_btnCancel.widthHint = 101;
        btnCancel.setLayoutData(gd_btnCancel);
        btnCancel.setText("Cancel");
        new Label(this, SWT.NONE);
        new Label(this, SWT.NONE);
        new Label(this, SWT.NONE);
        new Label(this, SWT.NONE);
        lbMessage = new Label(this, SWT.NONE);
        lbMessage.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        lbMessage.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    public Button getBtnOk() {
        return btnOk;
    }

    public Button getBtnCancel() {
        return btnCancel;
    }

    public Text getUser() {
        return user;
    }

    public Text getPassword() {
        return password;
    }

    public void setMsg(String msg) {
        lbMessage.setText(msg);
        this.layout();
    }

}