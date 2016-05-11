package com.sos.JSHelper.Exceptions;

public class FTPCommandPendingException extends JobSchedulerException {

    private static final long serialVersionUID = 1328332945376323290L;

    public FTPCommandPendingException() {
        this("Error while process ftp command");
    }

    public FTPCommandPendingException(String pstrMessage) {
        super(pstrMessage);
        this.Status(JobSchedulerException.PENDING);
        this.eMailSubject("FTP-problem occured.");
    }

}