package com.sos.JSHelper.io.Files;

import com.sos.JSHelper.Listener.JSListenerClass;

/** @author Klaus Buettner */
public class JSFileListEntry extends JSListenerClass {

    private String strRemoteFileName = null;
    private String strLocalFileName = null;
    private long lngNoOfBytesTransferred = 0;

    public JSFileListEntry() {
        //
    }

    public JSFileListEntry(String pstrLocalFileName) {
        this("", pstrLocalFileName, 0);
    }

    public JSFileListEntry(String pstrRemoteFileName, String pstrLocalFileName, long plngNoOfBytesTransferred) {
        strRemoteFileName = pstrRemoteFileName;
        strLocalFileName = pstrLocalFileName;
        lngNoOfBytesTransferred = plngNoOfBytesTransferred;
    }

    public String toString() {
        return String.format("RemoteFile = %1$s, LocalFile = %2$s, BytesTransferred = %3$s", this.getRemoteFileName(), this.getLocalFileName(),
                this.getNoOfBytesTransferred());
    }

    public String getRemoteFileName() {
        return strRemoteFileName;
    }

    public void setRemoteFileName(String pstrRemoteFileName) {
        this.strRemoteFileName = pstrRemoteFileName;
    }

    public String getLocalFileName() {
        return strLocalFileName;
    }

    public void setLocalFileName(String pstrLocalFileName) {
        this.strLocalFileName = pstrLocalFileName;
    }

    public long getNoOfBytesTransferred() {
        return lngNoOfBytesTransferred;
    }

    public void setNoOfBytesTransferred(long plngNoOfBytesTransferred) {
        this.lngNoOfBytesTransferred = plngNoOfBytesTransferred;
    }

}