package com.sos.JSHelper.enums;

public enum enuTransferModes {
    ascii("ascii"), binary("binary"), text("text");

    public String description;

    private enuTransferModes() {
        this(null);
    }

    public String getText() {
        return this.name();
    }

    private enuTransferModes(final String name) {
        String k;
        if (name == null) {
            k = this.name();
        } else {
            k = name;
        }
        description = k;
    }

    public static String[] getArray() {
        String[] strA = new String[3];
        int i = 0;
        for (enuTransferModes enuType : enuTransferModes.values()) {
            strA[i++] = enuType.getText();
        }
        return strA;
    }

}