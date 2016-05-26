package com.sos.dialog.components;

import java.io.File;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSFileNameSelector extends SOSPreferenceStoreText {

    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());
    private String strFileName = "";

    public String getFileName() {
        return strFileName;
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
        return new MouseListener() {

            @Override
            public void mouseUp(final MouseEvent arg0) {
                //
            }

            @Override
            public void mouseDown(final MouseEvent arg0) {
                //
            }

            @Override
            public void mouseDoubleClick(final MouseEvent arg0) {
                String strT = "";
                strFileName = getText();
                String strKey = strPreferenceStoreKey + "/" + "lastFileName";
                if (strFileName.trim().length() <= 0) {
                    strFileName = prefs.get(strKey, "");
                }
                strT = openDirectoryFile("*.*", strFileName);
                if (strT.trim().length() > 0) {
                    File objFile = new File(strT);
                    if (objFile.canRead()) {
                        setText(objFile.getAbsoluteFile().toString());
                        prefs.put(strKey, strT);
                        strFileName = strT;
                        setText(objFile.getName());
                    } else {
                        throw new JobSchedulerException(String.format("File '%1$s' not found or is not readable", strT));
                    }
                }
            }
        };
    }

    public String openDirectoryFile(final String mask, final String pstrDirectoryName) {
        String filename = "";
        FileDialog fdialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        fdialog.setFilterPath(pstrDirectoryName);
        String filterMask = mask.replaceAll("\\\\", "");
        filterMask = filterMask.replaceAll("\\^.", "");
        filterMask = filterMask.replaceAll("\\$", "");
        fdialog.setFilterExtensions(new String[] { filterMask });
        filename = fdialog.open();
        if (filename == null || filename.trim().isEmpty()) {
            filename = "";
        } else {
            filename = filename.replaceAll("\\\\", "/");
        }
        return filename;
    }

    public void refreshContent() {
        //
    }

    public SOSFileNameSelector(final Composite pobjComposite, final int arg1) {
        super(pobjComposite, arg1);
        addFocusListener(getFocusAdapter());
        setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        addMouseListener(getMouseListener());
    }

}