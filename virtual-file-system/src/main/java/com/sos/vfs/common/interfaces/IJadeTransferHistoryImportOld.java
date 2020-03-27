package com.sos.vfs.common.interfaces;

import com.sos.vfs.common.options.SOSBaseOptions;

public interface IJadeTransferHistoryImportOld {

    public void setData(SOSBaseOptions options);

    public void doTransferDetail();

    public void doTransferSummary();

    public void setJadeTransferData(IJadeTransferHistoryData data);

    public void setJadeTransferDetailData(IJadeTransferDetailHistoryData data);

    public void close();

    public String getFileName();

}
