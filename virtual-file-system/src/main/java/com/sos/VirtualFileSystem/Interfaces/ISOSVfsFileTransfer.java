/*
 * SOSFileTransfer.java
 * Created on 19.12.2007
 *
 */
package com.sos.VirtualFileSystem.Interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.VirtualFileSystem.DataElements.SOSFolderName;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.common.SOSFileEntries;

public interface ISOSVfsFileTransfer {

	/**
	 * @return True if the client is currently connected to a server, false otherwise.
	 */
	public boolean isConnected();
	
	/**
	 * reconnect the connection to the server if client is currently not connected.
	 * @param options
	 */
	public void reconnect(SOSConnection2OptionsAlternate options);

	/**
	 * @return True if successfully completed, false if not.
	 * @throws IOException
	 */
	public void logout() throws IOException;

	/**
	 * Closes the connection to the server and restores connection parameters to the default values.
	 * @throws IOException
	 */
	public void disconnect() throws IOException;

	/**
	 * @param pathname The new current working directory
	 * @return True if successfully completed, false if not
	 * @throws IOException
	 */
	public boolean changeWorkingDirectory(String pathname) throws IOException;

	/**
	 * Returns the entire text of the last FTP server response exactly as it was received, including all end of line markers in NETASCII format.
	 * @return The entire text from the last FTP response as a String (or "" if not supported)
	 */
	public String getReplyString();

	/**
	 * Creates a new subdirectory on the FTP server in the current directory .
	 * @param pathname The pathname of the directory to create.
	 * @return True if successfully completed, false if not.
	 * @throws java.lang.IOException
	 */
	public void mkdir(String pathname) throws IOException;
	public void rmdir(String pstrFolderName) throws IOException;
	public boolean rmdir(SOSFolderName pstrFolderName) throws IOException;

	/**
	 * Obtain a list of filenames in a directory (or just the name of a given file,
	 * which is not particularly useful). If the given pathname is a directory and
	 * contains no files, a zero length array is returned only if the server
	 * returned a positive completion code, otherwise null is
	 * returned (the FTP server returned a 550 error No files found.).
	 * If the directory is not empty, an array of filenames in the directory is returned.
	 * If the pathname corresponds to a file, only that file will be listed.
	 * The server may or may not expand glob expressions.
	 * @param pathname The file or directory to list.
	 * @return The list of filenames contained in the given path. null if the list could not be obtained.
	 *         If there are no filenames in the directory, a zero-length array is returned.
	 * @throws IOException
	 */
	public String[] listNames(String pathname) throws IOException;

	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 * @param pathname on remote machine
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception Exception
	 * @see #dir()
	 */
	public Vector<String> nList(String pathname) ; // throws Exception;

	public Vector<String> nList(String pathname, final boolean flgRecurseSubFolder);

	public OutputStream getAppendFileStream(String strFileName) ;
	public OutputStream getOutputStream(String strFileName) ;
	public OutputStream getOutputStream() ;
	public OutputStream getFileOutputStream();

	public InputStream getInputStream(String strFileName) ;
	public InputStream getInputStream() ;
	/**
	 * return a listing of the contents of a directory in short format on
	 * the remote machine
	 *
	 * @return a listing of the contents of a directory on the remote machine
	 *
	 * @exception Exception
	 */

	public Vector<String> nList(boolean recursive) throws Exception;

	public void put(String localFile, String remoteFile);

	public long putFile(String localFile, OutputStream out);

	/**
	 * Stores a file on the server using the given name.
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @return The total number of bytes written.
	 * @exception Exception
	 * @see #put( String, String )
	 */
	public long putFile(String localFile, String remoteFile) throws Exception;

	/**
	 * Deletes a file on the FTP server.
	 * @param The pathname of the file to be deleted.
	 * @return True if successfully completed, false if not.
	 * @throws IOException If an I/O error occurs while either sending a
	 * command to the server or receiving a reply from the server.
	 */
	public void delete(String pathname) throws IOException;


	/**
	 * Retrieves a named file from the ftp server.
	 *
	 * @param localFile The name of the local file.
	 * @param remoteFile The name of the remote file.
	 * @param append Appends the remote file to the local file.
	 * @return  The total number of bytes retrieved.
	 * @see #get( String, String )
	 * @exception Exception
	 */
	public long getFile(String remoteFile, String localFile, boolean append) throws Exception;

	public long getFile(String remoteFile, String localFile) throws Exception;

	public ISOSVirtualFile TransferMode(SOSOptionTransferMode pobjFileTransferMode);

	/**
	 *
	 * \brief ControlEncoding - specifiy the encoding-type used by the server(s)
	 *
	 * \details
	 * This Method is used to set the encoding-type, e.g. utf-8 or iso-8850-1, used by the (ftp-)server.
	 *
	 * \return void
	 *
	 * @param pstrControlEncoding
	 */
	public void ControlEncoding (final String pstrControlEncoding);

	public int passive();

	public void login(String strUserName, String strPassword);

	public Vector<String> nList() throws Exception;

	public void ascii();

	public void binary();

	public ISOSVFSHandler getHandler();

	public long appendFile(String localFile, String remoteFile);

	public ISOSVirtualFile getFileHandle (final String pstrFilename);

	public boolean isNegativeCommandCompletion () ;

	public String[] getFilelist(String folder, String regexp, int flag, boolean withSubFolder);
	public String[] getFolderlist(String folder, String regexp, int flag, boolean withSubFolder);

	public long getFileSize(String strFileName) ;

	public String getModificationTime(String strFileName) ;

	/**
	 *
	 * \brief CompletePendingCommand
	 *
	 * \details
	 *
	 * \return void
	 *
	 */
	public void CompletePendingCommand () ;

	public String DoPWD ();

	public boolean isDirectory(String strFileName);

	public void rename(String strFileName, String pstrNewFileName);

	/**
	 *
	 * \brief write - Writes len bytes from the specified byte array
	 *
	 * \details
	 * Writes len bytes from the specified byte array starting at offset off to this file.
	 * \return void
	 *
	 * @param bteBuffer
	 * @param intOffset
	 * @param intLength
	 */
	public void write (byte[] bteBuffer, int intOffset, int intLength );
	public void write (byte[] bteBuffer);
	public int read (byte[] bteBuffer) ;
	public int read (byte[] bteBuffer, int intOffset, int intLength );
	public void close () ;
	/**
	 *
	 * \brief flush - Flushes this file buffer
	 *
	 * \details
	 * Flushes this file buffer and forces any buffered output bytes to be written out.
	 * \return void
	 *
	 */
	public void flush () ;

	/**
	 *
	 * \brief closeInput
	 *
	 * \details
	 *
	 * \return void
	 *
	 */
	public void closeInput () ;

	/**
	 *
	 * \brief closeOutput
	 *
	 * \details
	 *
	 * \return void
	 *
	 */
	public void closeOutput () ;

	public void openInputFile (final String pstrFileName);
	public void openOutputFile (final String pstrFileName);

	public Vector<ISOSVirtualFile> getFiles(String string);
	public Vector<ISOSVirtualFile> getFiles();

	public void putFile(ISOSVirtualFile objVF);

    public SOSFileEntries getSOSFileEntries();


}
