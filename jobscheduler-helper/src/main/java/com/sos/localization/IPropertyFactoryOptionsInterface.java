package com.sos.localization;

import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionString;

public interface IPropertyFactoryOptionsInterface {

    public abstract SOSOptionString getOperation();

    public abstract void setOperation(SOSOptionString pOperation);

    public abstract SOSOptionString getPropertyFileNamePrefix();

    public abstract void setPropertyFileNamePrefix(SOSOptionString pPropertyFileName);

    public abstract SOSOptionFolderName getSourceFolderName();

    public abstract void setSourceFolderName(SOSOptionFolderName pSourceFolderName);

}