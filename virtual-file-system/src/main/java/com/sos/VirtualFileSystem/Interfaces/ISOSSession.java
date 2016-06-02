package com.sos.VirtualFileSystem.Interfaces;

/** @author KB */
public interface ISOSSession {

    public ISOSSession openSession(ISOSShellOptions pobjShellOptions) throws Exception;

    public void closeSession() throws Exception;

}