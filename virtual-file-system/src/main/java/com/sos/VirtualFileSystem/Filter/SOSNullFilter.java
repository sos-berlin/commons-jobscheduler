package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSFileContentFilter;

/** @author KB */
public class SOSNullFilter extends JSJobUtilitiesClass<SOSFilterOptions> implements ISOSFileContentFilter {

    private static final Logger LOGGER = Logger.getLogger(SOSNullFilter.class);
    protected byte[] bteBuffer = null;

    public SOSNullFilter() {
        super();
    }

    public SOSNullFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
        objOptions = pobjOptions;
    }

    @Override
    public SOSFilterOptions getOptions() {
        if (objOptions == null) {
            objOptions = new SOSFilterOptions();
        }
        return objOptions;
    }

    @Override
    public void write(final byte[] pbteBuffer, final int intOffset, final int intLength) {
        bteBuffer = pbteBuffer;
        doProcess();
    }

    @Override
    public void write(final byte[] pbteBuffer) {
        bteBuffer = pbteBuffer;
        doProcess();
    }

    protected String byte2String(final byte[] pbteBuffer) {
        String strT = "";
        try {
            strT = new String(bteBuffer);
            LOGGER.trace(strT);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strT;
    }

    protected void doProcess() {
        if (bteBuffer != null) {
            String strT = byte2String(bteBuffer);
            bteBuffer = strT.getBytes();
            LOGGER.trace(byte2String(bteBuffer));
        }
    }

    @Override
    public int read(byte[] pbteBuffer) {
        pbteBuffer = bteBuffer;
        bteBuffer = null;
        return pbteBuffer.length;
    }

    @Override
    public int read(byte[] pbteBuffer, final int intOffset, final int intLength) {
        pbteBuffer = bteBuffer;
        bteBuffer = null;
        return pbteBuffer.length;
    }

    @Override
    public byte[] readBuffer() {
        return bteBuffer;
    }

    @Override
    public byte[] readBuffer(final int intOffset, final int intLength) {
        return bteBuffer;
    }

    @Override
    public void close() {

    }

    @Override
    public byte[] read() {
        return bteBuffer;
    }

    @Override
    public byte[] read(final int intOffset, final int intLength) {
        return bteBuffer;
    }

    @Override
    public void write(final String pstrBuffer) {
        if (pstrBuffer != null) {
            bteBuffer = pstrBuffer.getBytes();
            doProcess();
        } else {
            bteBuffer = null;
        }
    }

    @Override
    public String readString() {
        String strT = null;
        if (bteBuffer != null) {
            strT = byte2String(bteBuffer);
        }
        return strT;
    }

    protected void dumpByteBuffer() {
        String strT = "";
        for (byte element : bteBuffer) {
            strT += element + " ";
        }
        LOGGER.debug(strT);
        LOGGER.debug(byte2String(bteBuffer));
    }

    @Override
    public void open() {

    }

    @Override
    public byte[] getBuffer() {
        return null;
    }

}
