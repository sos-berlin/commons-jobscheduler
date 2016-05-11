package com.sos.VirtualFileSystem.Interfaces;

/** @author KB */
public interface ISOSFileContentFilter {

    public void write(byte[] bteBuffer, int intOffset, int intLength);

    public void write(byte[] bteBuffer);

    public void write(String pstrBuffer);

    public int read(byte[] bteBuffer);

    public int read(byte[] bteBuffer, int intOffset, int intLength);

    public byte[] read();

    public String readString();

    public byte[] read(int intOffset, int intLength);

    public byte[] readBuffer();

    public byte[] readBuffer(int intOffset, int intLength);

    public void close();

    public void open();

    public byte[] getBuffer();

}