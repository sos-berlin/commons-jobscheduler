package com.sos.VirtualFileSystem.Interfaces;

public interface ISOSSession {

    public ISOSSession openSession(ISOSShellOptions pobjShellOptions) throws Exception;

    public void closeSession() throws Exception;

}