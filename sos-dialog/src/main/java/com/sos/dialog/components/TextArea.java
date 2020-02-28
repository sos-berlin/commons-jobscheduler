package com.sos.dialog.components;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.vzurczak.main.XmlRegion;
import net.vzurczak.main.XmlRegionAnalyzer;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.shell.CmdShell;
import com.sos.dialog.Globals;
import com.sos.dialog.classes.WindowsSaver;
import com.sos.dialog.swtdesigner.SWTResourceManager;

/** @author Uwe Risse */
public class TextArea extends StyledText {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextArea.class);
    private WindowsSaver objFormPosSizeHandler = null;
    private String strPreferenceStoreKey = "";
    private StyledText objThis = this;
    boolean flgInit = false;

    public TextArea(final Composite pobjComposite) {
        this(pobjComposite, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
        createContextMenue();
        this.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
    }

    public TextArea(final Composite pobjComposite, final String pstrPreferenceStoreKey) {
        this(pobjComposite);
        objFormPosSizeHandler = new WindowsSaver(this.getClass(), pobjComposite.getShell(), 640, 480);
        strPreferenceStoreKey = pstrPreferenceStoreKey;
        objFormPosSizeHandler.setKey(pstrPreferenceStoreKey);
        refreshContent();
    }

    public TextArea(final Composite pobjComposite, final int arg1) {
        super(pobjComposite, arg1);
        this.setBackground(Globals.getFieldBackground());
        addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(final VerifyEvent e) {
                //
            }
        });
        addMouseListener(new MouseListener() {

            @Override
            public void mouseUp(final MouseEvent event) {
                if (event.button == 3) {
                    LOGGER.debug("button2");
                }
            }

            @Override
            public void mouseDown(final MouseEvent arg0) {
                //
            }

            @Override
            public void mouseDoubleClick(final MouseEvent arg0) {
                //
            }
        });
        addHelpListener(new HelpListener() {

            @Override
            public void helpRequested(final HelpEvent objHelpEvent) {
                //
            }
        });
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(final KeyEvent e) {
                e.doit = false;
                if ((e.stateMask & SWT.MOD1) == SWT.MOD1) {
                    switch (e.keyCode) {
                    case 'a':
                        doSelectAll();
                        return;
                    case 'i':
                        doInsertFile();
                        return;
                    case 'r':
                        doReadFile();
                        return;
                    case 'f':
                        changeFont();
                        return;
                    case 'x':
                        startExternalEditor();
                        return;
                    default:
                        break;
                    }
                }
                if (((e.stateMask & (SWT.MOD1 | SWT.ALT)) == (SWT.MOD1 | SWT.ALT)) && e.keyCode == 's') {
                    saveFile();
                    return;
                }
                e.doit = true;
                return;
            }
        });
        final GridData gridData_1 = new GridData(GridData.FILL, GridData.FILL, true, true, 4, 1);
        gridData_1.minimumHeight = 40;
        gridData_1.widthHint = 454;
        gridData_1.heightHint = 139;
        setLayoutData(gridData_1);
        addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                //
            }
        });
    }

    public StyledText getControl() {
        return this;
    }

    public void setFormHandler(final WindowsSaver pobjFormHandler) {
        objFormPosSizeHandler = pobjFormHandler;
    }

    public void createContextMenue() {
        Menu objContextMenu = getMenu();
        if (objContextMenu == null) {
            objContextMenu = new Menu(this.getControl());
        }
        boolean flgIsEditable = getEditable();
        MenuItem itemCopy = new MenuItem(objContextMenu, SWT.PUSH);
        itemCopy.addListener(SWT.Selection, getCopyListener());
        itemCopy.setText("Copy");
        if (flgIsEditable) {
            MenuItem itemCut = new MenuItem(objContextMenu, SWT.PUSH);
            itemCut.addListener(SWT.Selection, getCutListener());
            itemCut.setText("Cut");
            MenuItem itemPaste = new MenuItem(objContextMenu, SWT.PUSH);
            itemPaste.addListener(SWT.Selection, getPasteListener());
            itemPaste.setText("Paste");
        }
        MenuItem itemSelectAll = new MenuItem(objContextMenu, SWT.PUSH);
        itemSelectAll.addListener(SWT.Selection, getSelectAllListener());
        itemSelectAll.setText("Select &All\tCtrl+A");
        itemSelectAll.setAccelerator(SWT.MOD1 + 'A');
        new MenuItem(objContextMenu, SWT.SEPARATOR);
        MenuItem itemStartExternalEditor = new MenuItem(objContextMenu, SWT.PUSH);
        itemStartExternalEditor.addListener(SWT.Selection, getStartExternalEditorListener());
        itemStartExternalEditor.setText("Start external Editor\tCtrl+X");
        itemSelectAll.setAccelerator(SWT.MOD1 + 'X');
        setMenu(objContextMenu);
        MenuItem itemSelectFont = new MenuItem(objContextMenu, SWT.PUSH);
        itemSelectFont.addListener(SWT.Selection, getSelectFontListener());
        itemSelectFont.setText("Select Font\tCtrl+F");
        itemSelectAll.setAccelerator(SWT.MOD1 + 'F');
        new MenuItem(objContextMenu, SWT.SEPARATOR);
        MenuItem itemSaveAs = new MenuItem(objContextMenu, SWT.PUSH);
        itemSaveAs.addListener(SWT.Selection, getSaveAsListener());
        itemSaveAs.setText("Save as ...\tCtrl+Alt+S");
        itemSelectAll.setAccelerator(SWT.MOD1 + SWT.ALT + 'S');
        if (flgIsEditable) {
            MenuItem itemReadFrom = new MenuItem(objContextMenu, SWT.PUSH);
            itemReadFrom.addListener(SWT.Selection, getReadFileListener());
            itemReadFrom.setText("Read from ...\tCtrl+R");
            itemSelectAll.setAccelerator(SWT.MOD1 + 'R');
            MenuItem itemInsertFrom = new MenuItem(objContextMenu, SWT.PUSH);
            itemInsertFrom.addListener(SWT.Selection, getInsertFileListener());
            itemInsertFrom.setText("Insert from ...\tCtrl+I");
            itemSelectAll.setAccelerator(SWT.MOD1 + 'I');
        }
    }

    private Listener getSaveAsListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("save as was pressed....");
                saveFile();
            }
        };
    }

    private Listener getStartExternalEditorListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                startExternalEditor();
            }
        };
    }

    public void startExternalEditor() {
        String text = getText();
        try {
            JSFile objTempF = new JSFile(File.createTempFile("SOS-JOE", ".xml").getAbsolutePath());
            objTempF.write(text);
            CmdShell objShell = new CmdShell();
            String strCommandString = String.format("uedit32.exe \"%1$s\"", objTempF);
            objShell.setCommand(strCommandString);
            objShell.run();
            if (objShell.getExitValue() != 0) {
                throw new JobSchedulerException(String.format("Command '%1$s' returns with error '%2$s'", strCommandString, objShell.getExitValue()));
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
        return;
    }

    private Listener getSelectFontListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("'Select Font' was pressed....");
                changeFont();
            }
        };
    }

    private Listener getReadFileListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("getReadFileListener was pressed....");
                doReadFile();
            }
        };
    }

    private Listener getInsertFileListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("getInsertFileListener was pressed....");
                doInsertFile();
            }
        };
    }

    private Listener getCopyListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("getCopyListener was pressed....");
                doCopy();
            }
        };
    }

    private Listener getPasteListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("getCopyListener was pressed....");
                doPaste();
            }
        };
    }

    private Listener getCutListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("getCopyListener was pressed....");
                doCut();
            }
        };
    }

    private Listener getSelectAllListener() {
        return new Listener() {

            @Override
            public void handleEvent(final Event e) {
                LOGGER.debug("getSelectAllListener was pressed....");
                doSelectAll();
            }
        };
    }

    private void doCopy() {
        this.copy();
    }

    private void doPaste() {
        this.paste();
    }

    private void doCut() {
        this.cut();
    }

    private void doSelectAll() {
        this.selectAll();
    }

    public void refreshContent() {
        flgInit = true;
        SOSFontDialog objFontDialog = new SOSFontDialog(getFont().getFontData()[0], getForeground().getRGB());
        objFontDialog.setKey(strPreferenceStoreKey + "font");
        objFontDialog.readFontData();
        setFont(objFontDialog.getFontData(), objFontDialog.getForeGround());
        flgInit = false;
    }

    public void setFont(final FontData f, final RGB foreGround) {
        setFont(SWTResourceManager.getFont(f.getLocale(), f.getHeight(), f.getStyle()));
        setForeground(SWTResourceManager.getColor(foreGround));
    }

    public void changeFont() {
        SOSFontDialog fd = new SOSFontDialog(getFont().getFontData()[0], getForeground().getRGB());
        fd.setKey(strPreferenceStoreKey + "font");
        fd.setParent(getShell());
        fd.show(getDisplay());
        setFont(fd.getFontData(), fd.getForeGround());
    }

    private void doReadFile() {
        String strFileName = doSelectFile("LastSelectedFile4Read");
        String strContent = null;
        if ((strContent = getFileContent(strFileName)) != null) {
            setText(strContent);
        }
    }

    private void doInsertFile() {
        String strFileName = doSelectFile("LastSelectedFile4Insert");
        String strContent = null;
        if ((strContent = getFileContent(strFileName)) != null) {
            this.insert(strContent);
        }
    }

    private String getFileContent(final String pstrFileName) {
        String strContent = null;
        if (pstrFileName != null) {
            JSFile objFile = new JSFile(pstrFileName);
            strContent = objFile.getContent();
        }
        return strContent;
    }

    private String doSelectFile(final String pstrLRUKey) {
        String strSelectedFileName = "";
        try {
            FileDialog fdialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
            if (objFormPosSizeHandler != null) {
                fdialog.setFilterPath(objFormPosSizeHandler.getProperty(pstrLRUKey));
            }
            if ((strSelectedFileName = fdialog.open()) == null) {
                return strSelectedFileName;
            }
            if (objFormPosSizeHandler != null) {
                objFormPosSizeHandler.saveProperty(pstrLRUKey, strSelectedFileName);
            }
            return strSelectedFileName;
        } catch (Exception e) {
            new JobSchedulerException(String.format("error selecting file '%1$s'", strSelectedFileName), e);
            return null;
        }
    }

    private boolean saveFile() {
        String strFilename4Save = "";
        try {
            FileDialog fdialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
            if (objFormPosSizeHandler != null) {
                fdialog.setFilterPath(objFormPosSizeHandler.getProperty("LastSelectedFile4Save"));
            }
            strFilename4Save = fdialog.open();
            if (strFilename4Save == null) {
                return false;
            }
            JSFile objFile = new JSFile(strFilename4Save);
            objFile.writeLine(getText());
            objFile.close();
            if (objFormPosSizeHandler != null) {
                objFormPosSizeHandler.saveProperty("LastSelectedFile4Save", strFilename4Save);
            }
            return true;
        } catch (Exception e) {
            new JobSchedulerException(String.format("error saving file '%1$s'", strFilename4Save), e);
            return false;
        }
    }

    public void setXMLText(final String pstrXMLText) {
        List<XmlRegion> regions = new XmlRegionAnalyzer().analyzeXml(pstrXMLText);
        List<StyleRange> objStyleRanges = computeStyleRanges(regions);
        setText(pstrXMLText);
        verifyXMLText objVfT = new verifyXMLText();
        addVerifyListener(objVfT);
        addKeyListener(objVfT);
        setStyleRanges(objStyleRanges.toArray(new StyleRange[objStyleRanges.size()]));
        redraw();
    }

    /** Computes style ranges from XML regions.
     * 
     * @param regions an ordered list of XML regions
     * @return an ordered list of style ranges for SWT styled text */
    public List<StyleRange> computeStyleRanges(List<XmlRegion> regions) {
        List<StyleRange> styleRanges = new ArrayList<StyleRange>();
        for (XmlRegion xr : regions) {
            StyleRange sr = new StyleRange();
            switch (xr.getXmlRegionType()) {
            case MARKUP:
                sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
                sr.fontStyle = SWT.BOLD;
                break;
            case ATTRIBUTE:
                sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);
                break;
            case ATTRIBUTE_VALUE:
                sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN);
                break;
            case MARKUP_VALUE:
                sr.fontStyle = SWT.BOLD;
                break;
            case COMMENT:
                sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN);
                break;
            case INSTRUCTION:
                sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_GRAY);
                break;
            case CDATA:
                sr.foreground = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
                break;
            case WHITESPACE:
                break;
            default:
                break;
            }
            sr.start = xr.getStart();
            sr.length = xr.getEnd() - xr.getStart();
            styleRanges.add(sr);
        }
        return styleRanges;
    }

    public class verifyXMLText implements VerifyListener, KeyListener {

        private String strActualText = "";
        private boolean flgInATag = false;
        private int position = -1;

        private void addActualText(final String pstrText) {
            if (flgInATag) {
                strActualText += pstrText;
            }
        }

        @Override
        public void verifyText(VerifyEvent event) {
            if (event.end - event.start == 0) {
                String strEventText = event.text;
                switch (strEventText) {
                case "<":
                    if (flgInATag) {
                        strActualText = "";
                    }
                    flgInATag = true;
                    addActualText(strEventText);
                    break;
                case ">":
                    if (flgInATag == true) {
                        addActualText(strEventText);
                        strActualText = strActualText.replace("<", " </");
                        event.text = strEventText + strActualText;
                        sendKeyEvent(0);
                    }
                    break;
                default:
                    addActualText(strEventText);
                    if (flgInATag) {
                        switch (strActualText) {
                        case "<![C":
                            event.text = "CDATA[ ]]>";
                            sendKeyEvent(5);
                            break;
                        default:
                            break;
                        }
                    }
                    break;
                }
                if ("<p>".equals(event.text)) {
                    event.text = "<p></p>";
                }
            }
        }

        private void sendKeyEvent(final int intOffset) {
            flgInATag = false;
            strActualText = "";
            position = getCaretOffset() + intOffset;
            Event objEvent = new Event();
            objEvent.type = SWT.KeyDown;
            objEvent.keyCode = SWT.ARROW_LEFT;
            objEvent.item = objThis;
            Display.getCurrent().post(objEvent);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            e.doit = true;
            if (flgInATag || position > 0) {
                switch (e.keyCode) {
                case SWT.ARROW_LEFT:
                    if (position > 0) {
                        position++;
                        setSelection(position);
                        setCaretOffset(position);
                        position = -1;
                        e.doit = false;
                    }
                    break;
                case SWT.BS:
                    if ("<".equals(strActualText)) {
                        strActualText = "";
                        flgInATag = false;
                    } else {
                        strActualText = strActualText.substring(0, strActualText.length() - 1);
                        LOGGER.debug(strActualText);
                    }
                    return;
                default:
                    break;
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        objFormPosSizeHandler = null;
        objThis = null;
    }

    @Override
    protected void checkSubclass() {
        //
    }

}