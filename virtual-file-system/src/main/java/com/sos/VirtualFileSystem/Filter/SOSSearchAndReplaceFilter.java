/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSSearchAndReplaceFilter extends SOSNullFilter {

    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private final Logger logger = Logger.getLogger(this.getClass());

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

        strT = strT.replaceAll(objOptions.replaceWhat.Value(), objOptions.replaceWith.Value());

        bteBuffer = strT.getBytes();
        logger.debug(byte2String(bteBuffer));

    } // private void doProcess
}
