package com.sos.JSHelper.Options;

/** @author KB */
public class SOSOptionTransferType extends SOSOptionStringValueList {

    private static final long serialVersionUID = 1359502923543333601L;
    private static final String CLASSNAME = "SOSOptionTransferType";
    private enuTransferTypes enuTT = enuTransferTypes.local;

    public static enum enuTransferTypes {
        local, file, ftp, sftp, ftps, ssh2, zip, mq, http, https, svn, webdav, smb, smtp, imap;

        public String getText() {
            return this.name();
        }

        public static String[] getArray() {
            String[] strA = new String[15];
            int i = 0;
            for (enuTransferTypes enuType : enuTransferTypes.values()) {
                strA[i++] = enuType.getText();
            }
            return strA;
        }
    }

    public SOSOptionTransferType(final JSOptionsClass pobjParent, final String pstrKey, final String pstrDescription, final String pstrValue,
            final String pstrDefaultValue, final boolean pflgIsMandatory) {
        super(pobjParent, pstrKey, pstrDescription, pstrValue, pstrDefaultValue, pflgIsMandatory);
        super.valueList(enuTransferTypes.getArray());
    }

    public void setValue(final enuTransferTypes penuTT) {
        enuTT = penuTT;
        super.setValue(penuTT.getText());
    }

    public boolean isSFtp() {
        text2Enum();
        return enuTT == enuTransferTypes.sftp;
    }

    public boolean isFtpS() {
        text2Enum();
        return enuTT == enuTransferTypes.ftps;
    }

    public boolean isLocal() {
        text2Enum();
        return enuTT == enuTransferTypes.local || enuTT == enuTransferTypes.file;
    }

    private void text2Enum() {
        for (enuTransferTypes enuType : enuTransferTypes.values()) {
            if (strValue.equalsIgnoreCase(enuType.getText())) {
                enuTT = enuType;
                break;
            }
        }
    }

    public enuTransferTypes getEnum() {
        text2Enum();
        return enuTT;
    }

}