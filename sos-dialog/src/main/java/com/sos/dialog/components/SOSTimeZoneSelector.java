package com.sos.dialog.components;

import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Point;
import org.joda.time.DateTimeZone;

public class SOSTimeZoneSelector {

    private Button btnOk;
    private CCombo cbTimeZone;
    private String timeZone;
    private Shell dialogShell;
    Composite parent;

    public SOSTimeZoneSelector(Shell parentShell) {
        this.dialogShell = parentShell;
    }

    public String execute(String timeZone) {
        this.timeZone = timeZone;
        Display display = Display.getDefault();
        Shell shell = showForm(display, dialogShell, timeZone);
        new Label(dialogShell, SWT.NONE);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.timeZone;
    }

    private Shell showForm(final Display display, Shell parentShell, String timeZone) {
        dialogShell = new Shell(parentShell, SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.OK | SWT.APPLICATION_MODAL);
        dialogShell.setMinimumSize(new Point(200, 100));
        dialogShell.setSize(200, 100);
        dialogShell.setLayout(new GridLayout(2, false));
        parent = dialogShell;
        createContent();
        cbTimeZone.setText(timeZone);
        dialogShell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {

            }
        });
        dialogShell.pack();
        dialogShell.open();
        return dialogShell;
    }

    private void createContent() {
        Label lblExpressionFor = new Label(dialogShell, SWT.NONE);
        lblExpressionFor.setText("Timezone");
        new Label(dialogShell, SWT.NONE);
        cbTimeZone = new CCombo(parent, SWT.BORDER);
        GridData gdTimeZone = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
        gdTimeZone.widthHint = 200;
        gdTimeZone.minimumWidth = 200;
        cbTimeZone.setLayoutData(gdTimeZone);
        fillTimeZones();
        btnOk = new Button(dialogShell, SWT.NONE);
        GridData gd_btnOk = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnOk.widthHint = 64;
        btnOk.setLayoutData(gd_btnOk);
        btnOk.setText("Ok");
        btnOk.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                timeZone = cbTimeZone.getText();
                dialogShell.dispose();
            }
        });
        dialogShell.setDefaultButton(btnOk);
    }

    public String getTimeZone() {
        return cbTimeZone.getText();
    }

    private void fillTimeZones() {
        Set<String> setOfTimeZones = DateTimeZone.getAvailableIDs();
        cbTimeZone.setItems(setOfTimeZones.toArray(new String[setOfTimeZones.size()]));
    }

}