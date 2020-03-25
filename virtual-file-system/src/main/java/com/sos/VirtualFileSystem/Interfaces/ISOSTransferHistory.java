package com.sos.VirtualFileSystem.Interfaces;

import com.sos.VirtualFileSystem.Options.SOSBaseOptions;

public interface ISOSTransferHistory {

    public void setData(SOSBaseOptions options);

    public void doTransferDetail();

    public void doTransferSummary();

    public void setJadeTransferData(IJadeTransferHistoryData data);

    public void setJadeTransferDetailData(IJadeTransferDetailHistoryData data);

    public void close();

    public String getFileName();

}
