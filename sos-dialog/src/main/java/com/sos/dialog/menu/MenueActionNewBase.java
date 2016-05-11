package com.sos.dialog.menu;

import com.sos.dialog.message.DialogMsg;

public class MenueActionNewBase extends MenueActionBase {

    private static final String conI18NKeyTREENODE_MENU_NEW = "treenode_menu_New";

    public MenueActionNewBase(String pstrMenueTextI18NKey, final String pstrAccText, final String pstrImageFileName) {
        super(pstrMenueTextI18NKey, null);
        init(pstrMenueTextI18NKey, pstrAccText, pstrImageFileName);
    }

    public MenueActionNewBase(final String pstrMenueTextParameter) {
        this(new DialogMsg(conI18NKeyTREENODE_MENU_NEW).params(pstrMenueTextParameter), "Ctrl+N", "New.gif");
    }

    @Override
    public void setText(final String pstrI18NKey) {
        super.setText(new DialogMsg(conI18NKeyTREENODE_MENU_NEW).params(pstrI18NKey));
    }

    @Override
    public void run() {
        //
    }

}