/**
 * 
 */
package com.sos.VirtualFileSystem.Filter.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionStringValueList;

/** @author KB */
public class SOSOptionFilterSequence extends SOSOptionStringValueList {

    public enum enuFilterCodes {
        dos2unix, //
        unix2dos, //
        searchreplace, //
        excludeinclude, //
        base64encode, //
        base64decode, //
        md5filter, //
        records, //
        nullfilter
    };

    private final enuFilterCodes objFC = null;

    /**
	 * 
	 */
    private static final long serialVersionUID = -3306255818817394338L;

    public SOSOptionFilterSequence(final String pstrValue) {
        super(null, "nullFilter", "description", "", false);
        this.Value(pstrValue);
    }

    public SOSOptionFilterSequence(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue,
            final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);

    }

    public SOSOptionFilterSequence(final JSOptionsClass pobjParent, final String pstrIndexedKey, final String pstrDescription, final String pstrDefaultValue,
            final boolean pflgIsMandatory) {
        super(pobjParent, pstrIndexedKey, pstrDescription, pstrDefaultValue, pflgIsMandatory);
    }

    @Override
    public void Value(final String pstrValueList) {
        if (isNotEmpty(pstrValueList)) {
            super.Value(pstrValueList);
            for (String strSingleValue : strValueList) {
                boolean flgFound = false;
                for (enuFilterCodes enuFC : enuFilterCodes.values()) {
                    if (enuFC.name().equalsIgnoreCase(strSingleValue)) {
                        flgFound = true;
                        break;
                    }
                }

                if (flgFound == false) {
                    String strM = String.format("Invalid Filter name found '%1$s'.", strSingleValue);
                    String strT = "\nValid Filter names are: ";
                    for (enuFilterCodes enuFC : enuFilterCodes.values()) {
                        strT = strT + enuFC.name() + ",";
                    }
                    strT = strT.substring(1, strT.length() - 1);
                    throw new JobSchedulerException(strM + strT);
                }
            }
        } else {
            throw new JobSchedulerException("An empty Filter list is not supported");
        }
    }
}
