package com.sos.VirtualFileSystem.Interfaces;

import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

public interface ISOSTransferHistory {

    public void setData(SOSFTPOptions options);

    public void doTransferDetail();

    public void doTransferSummary();

    public void setJadeTransferData(IJadeTransferHistoryData data);

    public void setJadeTransferDetailData(IJadeTransferDetailHistoryData data);

    public void close();

    public String getFileName();

}
