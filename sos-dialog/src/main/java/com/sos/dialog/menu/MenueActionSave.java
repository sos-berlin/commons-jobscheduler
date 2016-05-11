package com.sos.dialog.menu;

import org.apache.log4j.Logger;

import com.sos.dialog.components.SOSCursor;
import com.sos.dialog.message.DialogMsg;

public class MenueActionSave extends MenueActionBase {

    private static final Logger LOGGER = Logger.getLogger(MenueActionSave.class);

    public MenueActionSave() {
        this("");
    }

    public MenueActionSave(String pstrMenueText, final String pstrAccText, final String pstrImageFileName) {
        super(pstrMenueText, null);
        init(pstrMenueText, pstrAccText, pstrImageFileName);
    }

    public MenueActionSave(final String pstrMenueTextParameter) {
        this(new DialogMsg("treenode_menue_save").params(pstrMenueTextParameter), "Ctrl+S", "/org/freedesktop/tango/16x16/actions/document-save.png");
        strI18NKey = "treenode_menue_save";
    }

    @Override
    public void run() {
        try (SOSCursor objC = new SOSCursor().showWait()) {
            executeSave();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void executeSave() {
        //
    }

}