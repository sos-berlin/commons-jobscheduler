/**
 *
 */
package com.sos.dialog.interfaces;

import com.sos.dialog.menu.SOSMenueEvent;

/** @author KB */
public interface IDialogActionHandler {

    public void doOK(final SOSMenueEvent pobjMenueEvent);

    public void doCancel(final SOSMenueEvent pobjMenueEvent);

    public void doEdit(final SOSMenueEvent pobjMenueEvent);

    public void doNew(final SOSMenueEvent pobjMenueEvent);

    public void doDelete(final SOSMenueEvent pobjMenueEvent);

    public void doClose(final SOSMenueEvent pobjMenueEvent);

    public void setDialogActionHandler(final IDialogActionHandler pobjDialogActionHandler);

    public boolean doValidation(final SOSMenueEvent pobjMenueEvent);

}
