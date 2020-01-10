/**
 *
 */
package com.sos.VirtualFileSystem.Filter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSRecordsFilter extends SOSNullFilter {

    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private boolean flgIncludeRecord = false;
    private long lngRecordsIncluded = 0;
    private long lngRecordsExcluded = 0;

    /**
	 *
	 */
    public SOSRecordsFilter() {
        super();
    }

    public SOSRecordsFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
        logger.debug(conClassName);
        // excludeLinesBefore is not mandatory
        flgIncludeRecord = pobjOptions.excludeLinesBefore.isNotDirty();
    }

    @Override
    protected void doProcess() {
        @SuppressWarnings("unused")
        final String conMethodName = conClassName + "::doProcess";
        if (bteBuffer == null) {
            return;
        }
        String strT = byte2String(bteBuffer);
        if (objOptions.excludeLinesBefore.match(strT)) {
            flgIncludeRecord = true;
        }
        if (flgIncludeRecord == true) {
            if (objOptions.excludeEmptyLines.isTrue()) {
                if (strT.trim().length() > 0) {
                    fillBuffer(strT);
                }
            } else {
                fillBuffer(strT);
            }
        } else {
            bteBuffer = null;
            lngRecordsExcluded++;
        }
        if (objOptions.excludeLinesAfter.match(strT)) {
            flgIncludeRecord = false;
        }
    } // private void doProcess

    private String createLineNumbers(final String pstrT) {
        String strT = pstrT;
        if (objOptions.createLineNumbers.isTrue()) {
            String strF = objOptions.lineNumberingFormat.getValue();
            String strN = String.format(strF, lngRecordsIncluded);
            int intP = objOptions.lineNumberingPosition.value();
            if (intP <= 1) {
                strT = strN + strT.substring(intP - 1);
            } else {
                strT = strT.substring(0, intP) + strN + strT.substring(intP - 1);
            }
        }
        return strT;
    }

    private void fillBuffer(final String pstrT) {
        lngRecordsIncluded++;
        String strT = createLineNumbers(pstrT);
        bteBuffer = strT.getBytes();
    }

    @Override
    public void close() {
        objJSJobUtilities.setJSParam(conClassName + ".records_included", String.valueOf(lngRecordsIncluded));
        objJSJobUtilities.setJSParam(conClassName + ".records_excluded", String.valueOf(lngRecordsExcluded));
    }
}
