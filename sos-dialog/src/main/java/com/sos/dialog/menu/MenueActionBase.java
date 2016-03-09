package com.sos.dialog.menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

import com.sos.dialog.interfaces.IDialogActionHandler;
import com.sos.dialog.message.DialogMsg;
import com.sos.dialog.swtdesigner.SWTResourceManager;

public class MenueActionBase extends Action implements IDialogActionHandler {

    protected String strI18NKey = "";
    protected String strAccText = "";

    public MenueActionBase(String pstrMenueText, ImageDescriptor pobjImgDescr) {
        super(pstrMenueText, pobjImgDescr);
    }

    public Action addParam(final String pstrMenueTextParameter) {
        super.setText(new DialogMsg(strI18NKey).params(pstrMenueTextParameter) + "\t" + strAccText);
        return this;
    }

    protected void init(String pstrMenueText, final String pstrAccText, final String pstrImageFileName) {
        super.setText(pstrMenueText + "\t" + pstrAccText);
        strAccText = pstrAccText;
        super.setToolTipText(pstrMenueText + " the changed document");
        super.setAccelerator(Action.convertAccelerator(pstrAccText));
        super.setImageDescriptor(getImageDescr(pstrImageFileName));
    }

    protected ImageDescriptor getImageDescr(final String pstrFileName) {
        return ImageDescriptor.createFromImage(SWTResourceManager.getImageFromResource(pstrFileName));
    }

    @Override
    public void doCancel(final SOSMenueEvent objE) {
        objE.doIt = true;
    }

    @Override
    public void doOK(final SOSMenueEvent objE) {
        objE.doIt = true;
    }

    @Override
    public void doEdit(final SOSMenueEvent objE) {
        objE.doIt = true;
    }

    @Override
    public void doNew(final SOSMenueEvent objE) {
        objE.doIt = true;
    }

    @Override
    public void doDelete(final SOSMenueEvent objE) {
        objE.doIt = true;
    }

    @Override
    public void doClose(final SOSMenueEvent objE) {
        objE.doIt = true;
    }

    @Override
    public void setDialogActionHandler(IDialogActionHandler pobjDialogActionHandler) {
    }

    @Override
    public boolean doValidation(final SOSMenueEvent objE) {
        objE.doIt = true;
        return false;
    }
}
