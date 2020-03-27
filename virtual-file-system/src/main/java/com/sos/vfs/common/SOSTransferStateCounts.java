package com.sos.vfs.common;

public class SOSTransferStateCounts {

    private long success = 0L;
    private long failed = 0L;
    private long skipped = 0L;

    private long successZeroBytes = 0L;// TransferZeroBytes=true
    private long abortedZeroBytes = 0L;// TransferZeroBytes=false|strict
    private long skippedZeroBytes = 0L;// TransferZeroBytes=relaxed

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long val) {
        success = val;
    }

    public long getFailed() {
        return failed;
    }

    public void setFailed(long val) {
        failed = val;
    }

    public long getSkipped() {
        return skipped;
    }

    public void setSkipped(long val) {
        skipped = val;
    }

    public long getSuccessZeroBytes() {
        return successZeroBytes;
    }

    public void setSuccessZeroBytes(long val) {
        successZeroBytes = val;
    }

    public long getAbortedZeroBytes() {
        return abortedZeroBytes;
    }

    public void setAbortedZeroBytes(long val) {
        abortedZeroBytes = val;
    }

    public long getSkippedZeroBytes() {
        return skippedZeroBytes;
    }

    public void setSkippedZeroBytes(long val) {
        skippedZeroBytes = val;
    }
}
