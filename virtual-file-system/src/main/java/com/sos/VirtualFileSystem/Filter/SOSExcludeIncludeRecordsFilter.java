/**
 *
 */
package com.sos.VirtualFileSystem.Filter;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.Filter.Options.SOSFilterOptions;

/** @author KB */
public class SOSExcludeIncludeRecordsFilter extends SOSNullFilter {

    private final String conClassName = this.getClass().getSimpleName();
    private final Logger logger = Logger.getLogger(this.getClass());
    private boolean flgIncludeRecord = false;
    private long lngRecordsIncluded = 0;
    private long lngRecordsExcluded = 0;

    public SOSExcludeIncludeRecordsFilter() {
        super();
    }

    public SOSExcludeIncludeRecordsFilter(final SOSFilterOptions pobjOptions) {
        super(pobjOptions);
        logger.debug(conClassName);
    }

    @Override
    protected void doProcess() {
        if (bteBuffer == null) {
            return;
        }
        String strT = byte2String(bteBuffer);
        flgIncludeRecord = true;
        if (objOptions.excludeEmptyLines.isDirty() && strT.trim().isEmpty()) {
            flgIncludeRecord = false;
        }
        if (objOptions.excludeLines.isNotEmpty() && flgIncludeRecord && objOptions.excludeLines.match(strT)) {
            flgIncludeRecord = false;
        }
        if (objOptions.includeLines.isNotEmpty() && flgIncludeRecord && !objOptions.includeLines.match(strT)) {
            flgIncludeRecord = false;
        }
        if (flgIncludeRecord) {
            bteBuffer = strT.getBytes();
            lngRecordsIncluded++;
        } else {
            bteBuffer = null;
            lngRecordsExcluded++;
        }
    }

    @Override
    public void close() {
        objJSJobUtilities.setJSParam(conClassName + ".records_included", String.valueOf(lngRecordsIncluded));
        objJSJobUtilities.setJSParam(conClassName + ".records_excluded", String.valueOf(lngRecordsExcluded));
    }

}