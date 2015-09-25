package com.sos.VirtualFileSystem.DataElements;

public class SOSTransferStateCounts {
	private long	success				= 0L;
	private long	failed				= 0L;
	private long	skipped				= 0L;
	private long    zerobytes           = 0L;
	
	public long getSuccessTransfers() {
		return success;
	}

	public void setSuccessTransfers(long success) {
		this.success = success;
	}

	public long getFailedTranfers() {
		return failed;
	}

	public void setFailedTransfers(long failed) {
		this.failed = failed;
	}

	public long getSkippedTransfers() {
		return skipped;
	}

	public void setSkippedTransfers(long skipped) {
		this.skipped = skipped;
	}
	
	public long getZeroBytesTransfers() {
		return zerobytes;
	}

	public void setZeroBytesTransfers(long zerobytes) {
		this.zerobytes = zerobytes;
	}
	
	public SOSTransferStateCounts() {
		//
	}
}
