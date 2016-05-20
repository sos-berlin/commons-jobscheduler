package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSOptionJadeOperation extends SOSOptionStringValueList {

    private static final long serialVersionUID = 1786255103960193423L;
    private enuJadeOperations enuTT = enuJadeOperations.undefined;

    public static enum enuJadeOperations {
        send, receive, copy, move, delete, undefined, rename, zip, getlist, sendusingdmz, receiveusingdmz, copytointernet, copyfrominternet, remove;
        
        public String getText() {
            return this.name();
        }
    }

    public SOSOptionJadeOperation(final JSOptionsClass pobjParent, final String pstrKey, final String pstrDescription, final String pstrValue,
            final String pstrDefaultValue, final boolean pflgIsMandatory) {
        super(pobjParent, pstrKey, pstrDescription, pstrValue, pstrDefaultValue, pflgIsMandatory);
        String strT = "";
        for (enuJadeOperations enuT : enuJadeOperations.values()) {
            strT += enuT.getText() + ";";
        }
        this.createValueList(strT.substring(0, strT.length() - 1));
    }

    public void setValue(final enuJadeOperations penuOperation) {
        enuTT = penuOperation;
        if (enuTT == enuJadeOperations.remove) {
            enuTT = enuJadeOperations.delete;
        }
        super.setValue(enuTT.getText());
    }

    public enuJadeOperations value() {
        for (enuJadeOperations enuT : enuJadeOperations.values()) {
            if (enuT.name().equalsIgnoreCase(strValue)) {
                enuTT = enuT;
                break;
            }
        }
        return enuTT;
    }

    @Override
    public void setValue(final String pstrValue) {
        boolean flgOperationIsValid = false;
        if (pstrValue == null) {
            throw new JobSchedulerException("illegal parameter value: null");
        }

        if (pstrValue != null) {
            for (enuJadeOperations enuT : enuJadeOperations.values()) {
                if (enuT.name().equalsIgnoreCase(pstrValue)) {
                    this.setValue(enuT);
                    flgOperationIsValid = true;
                    super.setValue(enuTT.getText());
                    return;
                }
            }

            if (flgOperationIsValid == false) {
                throw new JobSchedulerException(String.format("unknown or invalid value for parameter '%1$s' specified: '%2$s'", this.getShortKey(),
                        pstrValue));
            }
        }
        super.setValue(pstrValue);
    }

    public boolean isOperationSend() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.send || enuT == enuJadeOperations.sendusingdmz;
    }

    public boolean isOperationSendUsingDMZ() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.sendusingdmz;
    }

    public boolean isOperationCopyFromInternet() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.copyfrominternet;
    }

    public boolean isOperationCopyToInternet() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.copytointernet;
    }

    public boolean isOperationReceiveUsingDMZ() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.receiveusingdmz;
    }

    public boolean isOperationReceive() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.receive || enuT == enuJadeOperations.receiveusingdmz;
    }

    public boolean isOperationCopy() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.copy;
    }

    public boolean isOperationGetList() {
        enuJadeOperations enuT = this.value();
        return enuT == enuJadeOperations.getlist;
    }

}