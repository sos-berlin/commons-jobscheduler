package com.sos.JSHelper.Options;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class SOSOptionProtocol extends JSOptionValueList {

    private static final long serialVersionUID = 8517927910957781607L;

    public enum Type {
        tcp, udp;

        public static String asString() {
            StringBuffer result = new StringBuffer();
            result.append("[");
            for (Type t : values()) {
                if (result.length() > 1)
                    result.append(", ");
                result.append(t.name());
            }
            result.append("]");
            return result.toString();
        }
    }

    public SOSOptionProtocol(JSOptionsClass pPobjParent, String pPstrKey, String pPstrDescription, String pPstrValue, String pPstrDefaultValue,
            boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
    }

    public SOSOptionProtocol(JSOptionsClass pobjParent, String pstrIndexedKey, String pstrDescription, String pstrDefaultValue, boolean pflgIsMandatory) {
        super(pobjParent, pstrIndexedKey, pstrDescription, pstrDefaultValue, pflgIsMandatory);
    }

    public boolean isTcp() {
        return (this.Value().equalsIgnoreCase(Type.tcp.name()));
    }

    public boolean isUdp() {
        return (this.Value().equalsIgnoreCase(Type.udp.name()));
    }

    public void Value(String value) {
        String testValue = value.toLowerCase();
        try {
            SOSOptionProtocol.Type t = SOSOptionProtocol.Type.valueOf(testValue);
        } catch (Exception e) {
            throw new JobSchedulerException("Value " + testValue + " is not valid for " + strKey + " - valid values are " + Type.asString() + ".");
        }
        super.Value(testValue);
    }

}
