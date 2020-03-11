package com.sos.JSHelper.Options;

public class SOSOptionTransferType extends SOSOptionStringValueList {

    private static final long serialVersionUID = 1359502923543333601L;
    private TransferTypes type = TransferTypes.local;

    public static enum TransferTypes {
        local(1), ftp(2), ftps(3), sftp(4), http(5), https(6), webdav(7), webdavs(8), smb(9), ssh(100), zip(200), mq(300), smtp(400), imap(500);

        private final int numeric;

        private TransferTypes(int val) {
            numeric = val;
        }

        public static String[] getArray() {
            String[] arr = new String[TransferTypes.values().length];
            int i = 0;
            for (TransferTypes type : TransferTypes.values()) {
                arr[i++] = type.name();
            }
            return arr;
        }

        public int numeric() {
            return numeric;
        }
    }

    public SOSOptionTransferType(final JSOptionsClass parent, final String key, final String description, final String value,
            final String defaultValue, final boolean isMandatory) {
        super(parent, key, description, value, defaultValue, isMandatory);
        super.valueList(TransferTypes.getArray());
    }

    public void setValue(final TransferTypes val) {
        type = val;
        super.setValue(type.name());
    }

    public boolean isLocal() {
        text2Enum();
        return type.equals(TransferTypes.local);
    }

    public boolean isSFTP() {
        text2Enum();
        return type.equals(TransferTypes.sftp);
    }

    public boolean isSSH() {
        text2Enum();
        return type.equals(TransferTypes.ssh);
    }

    public boolean isFTP() {
        text2Enum();
        return type.equals(TransferTypes.ftp);
    }

    public boolean isFTPS() {
        text2Enum();
        return type.equals(TransferTypes.ftps);
    }

    public boolean isHTTP() {
        text2Enum();
        return type.equals(TransferTypes.http) || type.equals(TransferTypes.https);
    }

    public boolean isWEBDAV() {
        text2Enum();
        return type.equals(TransferTypes.webdav);
    }

    public boolean isSMB() {
        text2Enum();
        return type.equals(TransferTypes.smb);
    }

    private void text2Enum() {
        for (TransferTypes enuType : TransferTypes.values()) {
            if (strValue.equalsIgnoreCase(enuType.name())) {
                type = enuType;
                break;
            }
        }
    }

    public TransferTypes getEnum() {
        text2Enum();
        return type;
    }

}