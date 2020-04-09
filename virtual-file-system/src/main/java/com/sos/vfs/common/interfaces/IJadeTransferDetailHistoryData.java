package com.sos.vfs.common.interfaces;

import java.time.Instant;
import java.util.Date;

public interface IJadeTransferDetailHistoryData {

    public abstract Integer getTransferDetailsId();

    public abstract Integer getTransferId();

    public abstract Long getFileSize();

    public abstract Integer getCommandType();

    public abstract String getCommand();

    public abstract String getPid();

    public abstract String getLastErrorMessage();

    public abstract String getTargetFilename();

    public abstract String getSourceFilename();

    public abstract String getMd5();

    public abstract Integer getStatus();

    public abstract Instant getStartTime();

    public abstract Instant getEndTime();

    public abstract String getModifiedBy();

    public abstract String getCreatedBy();

    public abstract Date getCreated();

    public abstract Date getModified();

    public abstract String getStatusText();

    public abstract String getSizeValue();

}