/**
 * 
 */
package com.sos.JSHelper.io.Files;
import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionRegExp;

/**
 * @author KB
 *
 */
public class JSFolder extends File {
	/**
	 * 
	 */
	private static final long								serialVersionUID	= -4423886110579623387L;
	@SuppressWarnings("unused") private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion		= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger				= Logger.getLogger(this.getClass());
	private final long										UNDEFINED			= -1L;
	public long												IncludeOlderThan	= UNDEFINED;
	public long												IncludeNewerThan	= UNDEFINED;
	private String											strFolderName		= "";
	//
	public class SOSFilelistFilter implements FilenameFilter {
		Pattern	pattern	= null;

		/**
		 * Konstruktor
		 * @param regexp ein regul�er Ausdruck
		 * @param flag ist ein Integer-Wert: CASE_INSENSITIVE, MULTILINE, DOTALL, UNICODE_CASE, and CANON_EQ
		 * @see <a href="http://java.sun.com/j2se/1.4.2/docs/api/constant-values.html#java.util.regex.Pattern.UNIX_LINES">Constant Field Values</a> 
		 */
		public SOSFilelistFilter(String regexp, int flag) throws Exception {
			if (regexp != null) {
				pattern = Pattern.compile(regexp, flag);
			}
		}

		@Override public boolean accept(File dir, String filename) {
			boolean flgR = true;
			if (pattern != null) {
				Matcher matcher = pattern.matcher(filename);
				flgR = matcher.find();
			}
			else {
				flgR = true;
			}
			if (flgR == true) {
				flgR = checkMoreFilter(new JSFile(dir, filename));
			}
			return flgR;
		}
	}

	private boolean checkMoreFilter(final JSFile pfleFile) {
		boolean flgR = true;
		if (IncludeOlderThan != UNDEFINED && flgR) {
			flgR = pfleFile.isOlderThan(IncludeOlderThan);
		}
		else {
			if (IncludeNewerThan != UNDEFINED && flgR) {
				flgR = !pfleFile.isOlderThan(IncludeNewerThan);
			}
		}
		return flgR;
	}

	/**
	 * @param pathname
	 */
	public JSFolder(String pathname) {
		super(pathname);
		init();
	}

	public String getFolderName () {
		return strFolderName;
	}
	
	public String addFileSeparator(final String str) {
		return str.endsWith("/") || str.endsWith("\\") ? str : str + "/";
	}

	private void init() {
		strFolderName = addFileSeparator(this.getAbsolutePath());
		if (isDirectory() == true) {
			//
		}
		else {
//			throw new JobSchedulerException(String.format("%1$s is not a directory", strFolderName));
		}
	}

	/**
	 * @param uri
	 */
	public JSFolder(URI uri) {
		super(uri);
		init();
	}

	/**
	 * @param parent
	 * @param child
	 */
	public JSFolder(String parent, String child) {
		super(parent, child);
		init();
	}

	/**
	 * @param parent
	 * @param child
	 */
	public JSFolder(File parent, String child) {
		super(parent, child);
		init();
	}

	public Vector<JSFile> getFilelist(final String regexp, final int flag) {
		if (!this.exists()) {
			throw new JobSchedulerException(String.format("directory does not exist: %1$s", this.getAbsolutePath()));
		}
		logger.debug("regexp for filelist: " + regexp);
		Vector<JSFile> filelist = new Vector<>();
		try {
			for (File file : listFiles(new SOSFilelistFilter(regexp, flag))) {
				if (file.isDirectory()) {
				}
				else
					if (file.isFile()) {
						filelist.add(new JSFile(file.getAbsolutePath()));
					}
					else {
						// unknown
					}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e);
		}
		return filelist;
	}

	public Vector<JSFolder> getFolderlist(final String regexp, final int intRegExpOptions) {
		if (!this.exists()) {
			throw new JobSchedulerException(String.format("directory does not exist: %1$s", this.getAbsolutePath()));
		}
		Vector<JSFolder> objFolderList = new Vector<>();
		try {
			for (File file : listFiles(new SOSFilelistFilter(regexp, intRegExpOptions))) {
				if (file.isDirectory()) {
					objFolderList.add(new JSFolder(file.getAbsolutePath()));
				}
				else
					if (file.isFile()) {
					}
					else {
						// unknown
					}
			}
		}
		catch (Exception e) {
			throw new JobSchedulerException(e);
		}
		return objFolderList;
	}

	public JSFile newFile(final String pstrFileName) {
		JSFile fleT = new JSFile(strFolderName + pstrFileName);
		return fleT;
	}

	public static final JSFolder getTempDir() {
		String strT = System.getProperty("java.io.tmpdir");
		return new JSFolder(strT);
	}

	public int deleteFolder() {
		intNoOfObjectsDeleted = 0;
		return deleteFolder(this);
	}

	public int intNoOfObjectsDeleted = 0;
	
	public int deleteFolder(final JSFolder pobjFolder) {
		try {
			if (pobjFolder.isDirectory()) {
				for (File file2 : pobjFolder.listFiles()) {
					deleteFolder(new JSFolder(file2.getAbsolutePath()));
				}
			}
			pobjFolder.delete();
			intNoOfObjectsDeleted++;
		}
		catch (Exception e) {
			throw new JobSchedulerException(String.format("Folder '%1$s' not deleted due to an error.", pobjFolder.getAbsolutePath()), e);
		}
		return intNoOfObjectsDeleted;
	} // deleteFile
	
	public String CheckFolder(final Boolean flgCreateIfNotExist) {
			if (this.exists() == false) {
				if (!flgCreateIfNotExist) {
					logger.error(String.format("Folder '%1$s' does not exist.", strFolderName));
				}
				else {
					this.mkdirs();
					logger.debug(String.format("Folder '%1$s' created.", strFolderName));
				}
			}
			if (this.canRead() == false) {
				logger.error(String.format("File '%1$s'. canRead returns false. Check permissions.", strFolderName));
			}
		return strFolderName;
	} // CheckFolder

	public Vector<String> deleteFileList (final SOSOptionRegExp objRegExpr4Files2Delete) {
		return deleteFileList(objRegExpr4Files2Delete.Value());
	}

	
	public int deleteFiles (final String strRegExpr4Files2Delete) {
		return deleteFileList(strRegExpr4Files2Delete).size();
	}
	
	public Vector<String> deleteFileList (final String strRegExpr4Files2Delete) {
		int intNoOfFilesDeleted = 0;
		Vector<String> objFileList = new Vector<>();

		for (JSFile tempFile : this.getFilelist(strRegExpr4Files2Delete, 0)) {
			tempFile.delete();
			String strName = tempFile.getAbsolutePath();
			objFileList.add(strName);
			logger.debug(String.format("File '%1$s' deleted", strName));
			intNoOfFilesDeleted++;
		}
		logger.debug(String.format("%1$s files deleted matching the regexp '%2$s'", intNoOfFilesDeleted, strRegExpr4Files2Delete));
		return objFileList;
	}

	public int compressFiles (final String strRegExpr4Files2Compress) {
		return compressFileList(strRegExpr4Files2Compress).size();
	}
	
	public Vector<String> compressFileList (final SOSOptionRegExp objRegExpr4Files2Compress) {
		return compressFileList(objRegExpr4Files2Compress.Value());
	}
	
	public Vector<String> compressFileList (final String strRegExpr4Files2Compress) {
		int intNoOfFilesCompressed = 0;
		Vector<String> objFileList = new Vector<>();

		for (JSFile tempFile : this.getFilelist(strRegExpr4Files2Compress, 0)) {
			intNoOfFilesCompressed++;
			tempFile.createZipFile(getFolderName());
			tempFile.delete();
			String strName = tempFile.getAbsolutePath();
			objFileList.add(strName);
			logger.debug(String.format("File '%1$s' compressed", strName));
			intNoOfFilesCompressed++;
		}
		logger.debug(String.format("%1$s files compressed matching the regexp '%2$s'", intNoOfFilesCompressed, strRegExpr4Files2Compress));
		return objFileList;
	}

}
