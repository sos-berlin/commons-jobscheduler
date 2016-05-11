package com.sos.dialog.components;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

public class SOSFolderNameSelector extends SOSPreferenceStoreText {

    private String strFolderName = "";
    private Menu objContextMenu = null;
    private String strI18NKey = "";
    public boolean flgIsFileFromLiveFolder = false;

    public SOSFolderNameSelector(Composite pobjComposite, int arg1) {
        super(pobjComposite, arg1);
        addFocusListener(getFocusAdapter());
        setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        addMouseListener(getMouseListener());
        addContextMenue();
    }

    @Override
    public void setLayoutData(Object pobjLayoutData) {
        super.setLayoutData(pobjLayoutData);
    }

    public String getFolderName() {
        return strFolderName;
    }

    private void addContextMenue() {
        if (objContextMenu == null) {
            objContextMenu = getMenu();
            if (objContextMenu == null) {
                objContextMenu = new Menu(getShell(), SWT.POP_UP);
            }
            MenuItem item = null;
            item = new MenuItem(objContextMenu, SWT.PUSH);
            item.addListener(SWT.Selection, CopyToClipboardListener());
            item.setText("Copy to Clipboard");
            item = new MenuItem(objContextMenu, SWT.PUSH);
            item.addListener(SWT.Selection, OpenListener());
            item.setText("Open");
            setMenu(objContextMenu);
        }
    }

    public void setI18NKey(final String pstrI18NKey) {
        strI18NKey = pstrI18NKey;
    }

    public String getI18NKey() {
        return strI18NKey;
    }

    private Listener CopyToClipboardListener() {
        return new Listener() {

            @Override
            public void handleEvent(Event e) {
                //
            }
        };
    }

    private Listener OpenListener() {
        return new Listener() {

            @Override
            public void handleEvent(Event e) {
                String strLastFolderName = getText();
                if (strLastFolderName.trim().isEmpty()) {
                    strLastFolderName = readPreferenceStore();
                }
                String strT = openDirectory(strLastFolderName);
                if (!strT.trim().isEmpty()) {
                    setText(strT);
                } else {
                    e.doit = false;
                }
            }
        };
    }

    private FocusAdapter getFocusAdapter() {
        return new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                selectAll();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                //
            }
        };
    }

    private MouseListener getMouseListener() {
        return (new MouseListener() {

            @Override
            public void mouseUp(MouseEvent arg0) {
                //
            }

            @Override
            public void mouseDown(MouseEvent arg0) {
                //
            }

            @Override
            public void mouseDoubleClick(MouseEvent arg0) {
                String strT = "";
                strFolderName = strT;
                String strLastFolderName = readPreferenceStore();
                strT = openDirectory(strLastFolderName);
                if (!strT.isEmpty()) {
                    File objFile = new File(strT);
                    if (objFile.canRead()) {
                        setText(objFile.getAbsoluteFile().toString());
                        strFolderName = strT;
                        writePreferenceStore(strFolderName);
                    }
                }
            }
        });
    }

    public String openDirectory(final String pstrDirectoryName) {
        String filename = "";
        DirectoryDialog fdialog = new DirectoryDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        fdialog.setFilterPath(pstrDirectoryName);
        filename = fdialog.open();
        if (filename == null || filename.trim().isEmpty()) {
            return filename;
        }
        filename = filename.replaceAll("\\\\", "/");
        return filename;
    }

    public void refreshContent() {
        //
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}