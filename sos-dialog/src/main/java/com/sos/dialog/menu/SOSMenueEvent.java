package com.sos.dialog.menu;

/** @author KB */
public class SOSMenueEvent {

    private String strMessage = "";
    public boolean doIt = false;
    public boolean showMenueItem = true;
    public enuOperation operation = enuOperation.execute;

    public static enum enuOperation {
        show, execute;
    }

    public SOSMenueEvent() {
        strMessage = "";
    }

    public void setMessage(final String pstrMessage) {
        strMessage = pstrMessage;
    }

    public String getMessage() {
        return strMessage;
    }

    public void addMessage(final String pstrMessage) {
        if (pstrMessage != null) {
            strMessage += pstrMessage + "\n";
        }
    }

    public boolean hasMessage() {
        return !strMessage.isEmpty();
    }

}