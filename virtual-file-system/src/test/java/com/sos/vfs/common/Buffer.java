package com.sos.vfs.common;

public class Buffer {

    private byte[] bytes = new byte[0];
    private int length = 0;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] val) {
        bytes = val;
        length = val.length;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int val) {
        length = val;
    }
}
