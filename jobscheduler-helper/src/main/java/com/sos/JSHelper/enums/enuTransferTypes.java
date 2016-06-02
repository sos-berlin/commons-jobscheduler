package com.sos.JSHelper.enums;

/** @author KB */
public enum enuTransferTypes {
    local, file, ftp, sftp, ftps, ssh2, zip, mq, http, https, svn, webdav, smb, smtp, imap;

    public String getText() {
        return this.name();
    }

    public static String[] getArray() {
        String[] strA = new String[15];
        int i = 0;
        for (enuTransferTypes enuType : enuTransferTypes.values()) {
            strA[i++] = enuType.getText();
        }
        return strA;
    }

}