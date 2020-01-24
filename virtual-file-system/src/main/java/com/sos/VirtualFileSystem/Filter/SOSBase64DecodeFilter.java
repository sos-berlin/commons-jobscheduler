/**
 * 
 */
package com.sos.VirtualFileSystem.Filter;

import org.apache.commons.net.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSBase64DecodeFilter extends SOSNullFilter {

    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSBase64DecodeFilter.class);

    private Base64 objBase64 = null;

    /**
	 * 
	 */
    public SOSBase64DecodeFilter() {
        super();
        objBase64 = new Base64();
    }

    public SOSBase64DecodeFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
        objBase64 = new Base64();
    }

    @Override
    protected void doProcess() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcess";

        byte[] bteOut = objBase64.decode(bteBuffer);
        bteBuffer = bteOut;

        LOGGER.debug(byte2String(bteBuffer));

    } // private void doProcess
}
