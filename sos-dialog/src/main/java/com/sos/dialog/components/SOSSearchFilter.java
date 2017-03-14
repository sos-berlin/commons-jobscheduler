package com.sos.dialog.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.sos.hibernate.classes.SOSSearchFilterData;

public class SOSSearchFilter {

    private Text edSearchField;
    private Button btnOk;
    private Button btnAsRegularExpression;
    private SOSSearchFilterData sosSearchFilterData;
    private String searchField;
    private boolean enableFilterCheckbox = false;
    private Shell dialogShell;
    private Button btnFilter;
    final int conDefaultPort = 40444;
    Composite parent;

    public SOSSearchFilter(Shell parentShell) {
        this.dialogShell = parentShell;
    }

    public SOSSearchFilterData execute(String searchField) {
        Display display = Display.getDefault();
        this.searchField = searchField;
        Shell shell = showForm(display, dialogShell);
        new Label(dialogShell, SWT.NONE);
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return sosSearchFilterData;
    }

    /** @wbp.parser.entryPoint */
    private Shell showForm(final Display display, Shell parentShell) {
        dialogShell = new Shell(parentShell, SWT.CLOSE | SWT.TITLE | SWT.BORDER | SWT.OK | SWT.APPLICATION_MODAL);
        dialogShell.setMinimumSize(new Point(300, 200));
        dialogShell.setSize(348, 200);
        dialogShell.setLayout(new GridLayout(2, false));
        parent = dialogShell;
        createContent();
        dialogShell.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                //
            }
        });
        dialogShell.pack();
        if (enableFilterCheckbox) {
            btnFilter = new Button(dialogShell, SWT.CHECK);
            btnFilter.setText("Filter");
            new Label(dialogShell, SWT.NONE);
        }
        dialogShell.open();
        return dialogShell;
    }

    private void createContent() {
        Label lblExpressionFor = new Label(dialogShell, SWT.NONE);
        lblExpressionFor.setText("Expression for search");
        new Label(dialogShell, SWT.NONE);
        edSearchField = new Text(dialogShell, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        edSearchField.setText(this.searchField);
        GridData gd_edSearchField = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_edSearchField.widthHint = 150;
        edSearchField.setLayoutData(gd_edSearchField);
        edSearchField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                //
            }
        });
        Button btnAsWildcard = new Button(dialogShell, SWT.RADIO);
        btnAsWildcard.setSelection(true);
        btnAsWildcard.setText("Search with wildcards");
        btnOk = new Button(dialogShell, SWT.NONE);
        GridData gd_btnOk = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnOk.widthHint = 64;
        btnOk.setLayoutData(gd_btnOk);
        btnOk.setText("Ok");
        btnOk.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                sosSearchFilterData = new SOSSearchFilterData();
                sosSearchFilterData.setRegularExpression(btnAsRegularExpression.getSelection());
                sosSearchFilterData.setSearchfield(edSearchField.getText());
                if (btnFilter != null) {
                    sosSearchFilterData.setFiltered(btnFilter.getSelection());
                } else {
                    sosSearchFilterData.setFiltered(false);
                }
                dialogShell.dispose();
            }
        });
        dialogShell.setDefaultButton(btnOk);
        btnAsRegularExpression = new Button(dialogShell, SWT.RADIO);
        btnAsRegularExpression.setText("Regular expression");
        Button btnCancel = new Button(dialogShell, SWT.NONE);
        GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnCancel.widthHint = 64;
        btnCancel.setLayoutData(gd_btnCancel);
        btnCancel.setText("Cancel");
        btnCancel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                dialogShell.dispose();
            }
        });
    }

    public SOSSearchFilterData getSosSearchFilterData() {
        return sosSearchFilterData;
    }

    public void setEnableFilterCheckbox(boolean enableFilterCheckbox) {
        this.enableFilterCheckbox = enableFilterCheckbox;
    }

}