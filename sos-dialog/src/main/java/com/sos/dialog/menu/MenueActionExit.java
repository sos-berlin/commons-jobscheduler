package com.sos.dialog.menu;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MenueActionExit extends MenueActionBase {

    public MenueActionExit() {
        this("treenode_menue_exit", "Alt+F4", "/org/freedesktop/tango/16x16/actions/system-log-out.png");
    }

    public MenueActionExit(String pstrMenueText, final String pstrAccText, final String pstrImageFileName) {
        super(pstrMenueText, null);
        init(pstrMenueText, pstrAccText, pstrImageFileName);
    }

    @Override
    public void run() {
        Shell sShell = Display.getCurrent().getActiveShell();
        sShell.close();
    }

}