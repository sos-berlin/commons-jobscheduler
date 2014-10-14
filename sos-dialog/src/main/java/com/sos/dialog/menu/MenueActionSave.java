package com.sos.dialog.menu;

import com.sos.dialog.components.SOSCursor;
import com.sos.dialog.message.DialogMsg;

public class MenueActionSave extends MenueActionBase {

	public MenueActionSave() {
		this("");
//		this("Save", "Ctrl+S", "/org/freedesktop/tango/16x16/actions/document-save.png");
	}

	public MenueActionSave(String pstrMenueText, final String pstrAccText, final String pstrImageFileName) {
		super(pstrMenueText, null);
		init(pstrMenueText, pstrAccText, pstrImageFileName);
	}

	public MenueActionSave(final String pstrMenueTextParameter) {
		this(new DialogMsg("treenode_menue_save").params(pstrMenueTextParameter), "Ctrl+S", "/org/freedesktop/tango/16x16/actions/document-save.png");
	}

	@Override
	public void run() {
		try (SOSCursor objC = new SOSCursor().showWait()) {
			executeSave();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void executeSave () {}

}
