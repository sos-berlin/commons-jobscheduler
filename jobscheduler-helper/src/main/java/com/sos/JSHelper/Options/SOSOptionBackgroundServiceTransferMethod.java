package com.sos.JSHelper.Options;

/** @author KB */
public class SOSOptionBackgroundServiceTransferMethod extends SOSOptionStringValueList {

    private static final long serialVersionUID = 559611781725991697L;

    public enum enuBackgroundServiceTransferMethods {
        tcp("tcp"), udp("udp");

        public final String description;

        private enuBackgroundServiceTransferMethods() {
            this(null);
        }

        private enuBackgroundServiceTransferMethods(final String name) {
            String k;
            if (name == null) {
                k = this.name();
            } else {
                k = name;
            }
            description = k;
        }

        public static String[] getArray() {
            String[] strA = new String[2];
            int i = 0;
            for (enuBackgroundServiceTransferMethods enuType : enuBackgroundServiceTransferMethods.values()) {
                strA[i++] = enuType.name();
            }
            return strA;
        }

    }

    public SOSOptionBackgroundServiceTransferMethod(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription,
            final String pPstrValue, final String pPstrDefaultValue, final boolean pPflgIsMandatory) {
        super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
        super.valueList(enuBackgroundServiceTransferMethods.getArray());
    }

    public SOSOptionBackgroundServiceTransferMethod(final JSOptionsClass pobjParent, final String pstrIndexedKey, final String pstrDescription,
            final String pstrDefaultValue, final boolean pflgIsMandatory) {
        super(pobjParent, pstrIndexedKey, pstrDescription, pstrDefaultValue, pflgIsMandatory);
    }

    public boolean isTcp() {
        return this.Value().equalsIgnoreCase(enuBackgroundServiceTransferMethods.tcp.description);
    }

    public boolean isUdp() {
        return this.Value().equalsIgnoreCase(enuBackgroundServiceTransferMethods.udp.description);
    }

    public String getDescription() {
        String strT = "???";
        if (this.isTcp()) {
            strT = enuBackgroundServiceTransferMethods.tcp.description;
        }
        if (this.isUdp()) {
            strT = enuBackgroundServiceTransferMethods.udp.description;
        }
        return strT;
    }

}