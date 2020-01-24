/**
 *
 */
package com.sos.VirtualFileSystem.Filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSSearchAndReplaceFilter extends SOSNullFilter {

    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSearchAndReplaceFilter.class);

    /**
	 *
	 */
    public SOSSearchAndReplaceFilter() {
        super();
    }

    public SOSSearchAndReplaceFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
    }

    @Override
    protected void doProcess() {

        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcess";
        String strT = byte2String(bteBuffer);

        strT = strT.replaceAll(objOptions.replaceWhat.getValue(), objOptions.replaceWith.getValue());

        bteBuffer = strT.getBytes();
        LOGGER.debug(byte2String(bteBuffer));

    } // private void doProcess
}
