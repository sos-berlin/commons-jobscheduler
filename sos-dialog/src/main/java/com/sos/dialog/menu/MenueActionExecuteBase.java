package com.sos.dialog.menu;

import com.sos.dialog.message.DialogMsg;

public class MenueActionExecuteBase extends MenueActionBase {

    private static final String conI18NKey = "treenode_menu_Execute";

    public MenueActionExecuteBase() {
        this("");
    }

    public MenueActionExecuteBase(String pstrMenueTextI18NKey, final String pstrAccText, final String pstrImageFileName) {
        super(pstrMenueTextI18NKey, null);
        init(pstrMenueTextI18NKey, pstrAccText, pstrImageFileName);
    }

    public MenueActionExecuteBase(final String pstrMenueTextParameter) {
        this(new DialogMsg(conI18NKey).params(pstrMenueTextParameter), "Ctrl+T", "ExecuteProject.gif");
        strI18NKey = conI18NKey;
    }

    @Override
    public void setText(final String pstrI18NKey) {
        super.setText(new DialogMsg(conI18NKey).params(pstrI18NKey));
    }

    @Override
    public void run() {
        execute();
    }

    protected void execute() {

    }
}
