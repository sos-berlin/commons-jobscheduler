package com.sos.VirtualFileSystem.Filter.Options;

import java.util.Vector;

import com.sos.VirtualFileSystem.Filter.SOSBase64DecodeFilter;
import com.sos.VirtualFileSystem.Filter.SOSBase64EncodeFilter;
import com.sos.VirtualFileSystem.Filter.SOSDos2UnixFilter;
import com.sos.VirtualFileSystem.Filter.SOSExcludeIncludeRecordsFilter;
import com.sos.VirtualFileSystem.Filter.SOSNullFilter;
import com.sos.VirtualFileSystem.Filter.SOSRecordsFilter;
import com.sos.VirtualFileSystem.Filter.SOSSearchAndReplaceFilter;
import com.sos.VirtualFileSystem.Filter.SOSUnix2DosFilter;
import com.sos.VirtualFileSystem.Filter.Options.SOSOptionFilterSequence.enuFilterCodes;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;

/** @author KB */
public class SOSFilterOptions extends SOSFilterOptionsSuperClass {

    private static final long serialVersionUID = 555337165999031797L;
    private Vector<SOSNullFilter> lstFilters = null;

    public Vector<SOSNullFilter> getFilter() {
        if (lstFilters == null) {
            lstFilters = new Vector();
            if (filterSequence.isDirty()) {
                for (String strFilterName : filterSequence.getValueList()) {
                    lstFilters.add(getFilterInstance(strFilterName));
                }
            } else {
                if (excludeLinesAfter.isDirty() || excludeLinesBefore.isDirty()) {
                    SOSNullFilter objF = new SOSRecordsFilter(this);
                    lstFilters.add(objF);
                }
                if (excludeEmptyLines.isDirty() || excludeLines.isDirty() || includeLines.isDirty()) {
                    SOSNullFilter objF = new SOSExcludeIncludeRecordsFilter(this);
                    lstFilters.add(objF);
                }
            }
        }
        return lstFilters;
    }

    public void startPostProcessing(final ISOSVirtualFile objOutput) {
        for (SOSNullFilter sosNullFilter : lstFilters) {
            byte[] strB = sosNullFilter.getBuffer();
            if (strB != null && objOutput != null) {
                objOutput.write(strB);
            }
        }
    }

    public void startCloseProcessing(final ISOSVirtualFile objOutput) {
        for (SOSNullFilter sosNullFilter : lstFilters) {
            sosNullFilter.close();
        }
    }

    public SOSNullFilter getFilterInstance(final String pstrFilterName) {
        SOSNullFilter objF = null;
        for (enuFilterCodes enuFC : enuFilterCodes.values()) {
            if (enuFC.name().equalsIgnoreCase(pstrFilterName)) {
                switch (enuFC) {
                case dos2unix:
                    objF = new SOSDos2UnixFilter(this);
                    break;
                case unix2dos:
                    objF = new SOSUnix2DosFilter(this);
                    break;
                case searchreplace:
                    objF = new SOSSearchAndReplaceFilter(this);
                    break;
                case excludeinclude:
                    objF = new SOSExcludeIncludeRecordsFilter(this);
                    break;
                case base64encode:
                    objF = new SOSBase64EncodeFilter(this);
                    break;
                case base64decode:
                    objF = new SOSBase64DecodeFilter(this);
                    break;
                case md5filter:
                    break;
                case nullfilter:
                    objF = new SOSNullFilter(this);
                    break;
                case records:
                    objF = new SOSRecordsFilter(this);
                    break;
                default:
                    break;
                }
                break;
            }
        }
        return objF;
    }

}