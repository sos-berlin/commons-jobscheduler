/*
 * SOSFileTransfer.java Created on 19.12.2007
 */
package sos.net.sosftp2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

public interface SOSFileTransfer {

    /** @return True if the client is currently connected to a server, false
     *         otherwise. */
    public boolean isConnected();

    /** @return True if successfully completed, false if not.
     * @throws IOException */
    public boolean logout() throws IOException;

    /** Closes the connection to the server and restores connection parameters to
     * the default values.
     * 
     * @throws IOException */
    public void disconnect() throws IOException;

    /** @param pathname The new current working directory
     * @return True if successfully completed, false if not
     * @throws IOException */
    public boolean changeWorkingDirectory(String pathname) throws IOException;

    /** Returns the entire text of the last FTP server response exactly as it was
     * received, including all end of line markers in NETASCII format.
     * 
     * @return The entire text from the last FTP response as a String (or "" if
     *         not supported) */
    public String getReplyString();

    /** Creates a new subdirectory on the FTP server in the current directory .
     * 
     * @param pathname The pathname of the directory to create.
     * @return True if successfully completed, false if not.
     * @throws java.lang.IOException */
    public boolean mkdir(String pathname) throws IOException;

    /** Obtain a list of filenames in a directory (or just the name of a given
     * file, which is not particularly useful). If the given pathname is a
     * directory and contains no files, a zero length array is returned only if
     * the server returned a positive completion code, otherwise null is
     * returned (the FTP server returned a 550 error No files found.). If the
     * directory is not empty, an array of filenames in the directory is
     * returned. If the pathname corresponds to a file, only that file will be
     * listed. The server may or may not expand glob expressions.
     * 
     * @param pathname The file or directory to list.
     * @return The list of filenames contained in the given path. null if the
     *         list could not be obtained. If there are no filenames in the
     *         directory, a zero-length array is returned.
     * @throws IOException */
    public String[] listNames(String pathname) throws IOException;

    /** return a listing of the contents of a directory in short format on the
     * remote machine
     * 
     * @param pathname on remote machine
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception
     * @see #dir() */
    public Vector<String> nList(String pathname) throws Exception;

    /** return a listing of the contents of a directory in short format on the
     * remote machine
     *
     * @return a listing of the contents of a directory on the remote machine
     *
     * @exception Exception */
    public Vector<String> nList() throws Exception;

    public Vector<String> nList(boolean recursive) throws Exception;

    public long getFile(String remoteFile, String localFile) throws Exception;

    public boolean put(String localFile, String remoteFile) throws Exception;

    /** Stores a file on the server using the given name.
     * 
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @return The total number of bytes written.
     * @exception Exception
     * @see #put(String, String ) */
    public long putFile(String localFile, String remoteFile) throws Exception;

    public long putFile(String localFile, OutputStream out) throws Exception;

    /** Deletes a file on the FTP server.
     * 
     * @param The pathname of the file to be deleted.
     * @return True if successfully completed, false if not.
     * @throws IOException If an I/O error occurs while either sending a command
     *             to the server or receiving a reply from the server. */
    public boolean delete(String pathname) throws IOException;

    /** Renames a remote file.
     * 
     * @param from The name of the remote file to rename
     * @param to The new name of the remote file
     * @return True if successfully completed, false if not
     * @throws IOException */
    public boolean rename(String from, String to) throws IOException;

    /** return the size of remote-file on the remote machine on success,
     * otherwise -1
     * 
     * @param remoteFile the file on remote machine
     * @return the size of remote-file on remote machine */
    public long size(String remoteFile) throws Exception;

    /** Retrieves a named file from the ftp server.
     *
     * @param localFile The name of the local file.
     * @param remoteFile The name of the remote file.
     * @param append Appends the remote file to the local file.
     * @return The total number of bytes retrieved.
     * @see #get(String, String )
     * @exception Exception */
    public long getFile(String remoteFile, String localFile, boolean append) throws Exception;

    public Vector<String> nList(String pathname, final boolean flgRecurseSubFolder) throws Exception;

}
