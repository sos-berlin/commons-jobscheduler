package com.sos.vfs.common.interfaces;

import java.util.Date;

public interface IJadeTransferHistoryData {

    public abstract Integer getTransferId();

    public abstract String getMandator();

    public abstract Long getFileSize();

    public abstract String getSourceHost();

    public abstract String getSourceHostIp();

    public abstract String getSourceUser();

    public abstract String getSourceDir();

    public abstract String getTargetHost();

    public abstract String getTargetHostIp();

    public abstract String getTargetUser();

    public abstract String getTargetDir();

    public abstract Integer getProtocolType();

    public abstract Integer getPort();

    public abstract Integer getFilesCount();

    public abstract String getProfileName();

    public abstract String getProfile();

    public abstract Integer getCommandType();

    public abstract String getCommand();

    public abstract String getLog();

    public abstract Integer getStatus();

    public abstract String getLastErrorMessage();

    public abstract Date getStartTime();

    public abstract Date getEndTime();

    public abstract String getStatusText();

    public abstract String getModifiedBy();

    public abstract String getCreatedBy();

    public abstract Date getCreated();

    public abstract Date getModified();

}