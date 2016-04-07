package com.sos.dialog.menu;

import com.sos.dialog.message.DialogMsg;

public class MenueActionOpenBase extends MenueActionBase {

    private static final String conI18NKeyTREENODE_MENU_OPEN = "treenode_menu_Open";

    public MenueActionOpenBase() {
        this("");
    }

    public MenueActionOpenBase(String pstrMenueTextI18NKey, final String pstrAccText, final String pstrImageFileName) {
        super(pstrMenueTextI18NKey, null);
        init(pstrMenueTextI18NKey, pstrAccText, pstrImageFileName);
    }

    public MenueActionOpenBase(final String pstrMenueTextParameter) {
        this(new DialogMsg(conI18NKeyTREENODE_MENU_OPEN).params(pstrMenueTextParameter), "Ctrl+O",
                "/org/freedesktop/tango/16x16/actions/document-open");
    }

    @Override
    public void setText(final String pstrI18NKey) {
        super.setText(new DialogMsg(conI18NKeyTREENODE_MENU_OPEN).params(pstrI18NKey));
    }

    @Override
    public void run() {
        executeOpen();
    }

    protected void executeOpen() {

    }
}
