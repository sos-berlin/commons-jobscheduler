package com.sos.VirtualFileSystem.Filter;

public class SOSNullFilterBase<T> {

    protected SOSNullFilter objF = null;
    protected byte[] bteBuffer = null;

    public SOSNullFilterBase(final T pobjT) {
        objF = (SOSNullFilter) pobjT;
    }

}