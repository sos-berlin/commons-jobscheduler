package com.sos.JSHelper.io.Files;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionRegExp;

/** @author KB */
public class JSFolder extends File {

    private static final long serialVersionUID = -4423886110579623387L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JSFolder.class);
    private static final long UNDEFINED = -1L;
    private String strFolderName = "";
    public long IncludeOlderThan = UNDEFINED;
    public long IncludeNewerThan = UNDEFINED;
    public int intNoOfObjectsDeleted = 0;

    public JSFolder(String pathname) {
        super(pathname);
        init();
    }

    public class SOSFilelistFilter implements FilenameFilter {

        Pattern pattern = null;

        public SOSFilelistFilter(String regexp, int flag) throws Exception {
            if (regexp != null) {
                pattern = Pattern.compile(regexp, flag);
            }
        }

        @Override
        public boolean accept(File dir, String filename) {
            boolean flgR = true;
            if (pattern != null) {
                Matcher matcher = pattern.matcher(filename);
                flgR = matcher.find();
            } else {
                flgR = true;
            }
            if (flgR) {
                flgR = checkMoreFilter(new JSFile(dir, filename));
            }
            return flgR;
        }
    }

    private boolean checkMoreFilter(final JSFile pfleFile) {
        boolean flgR = true;
        if (IncludeOlderThan != UNDEFINED && flgR) {
            flgR = pfleFile.isOlderThan(IncludeOlderThan);
        } else {
            if (IncludeNewerThan != UNDEFINED && flgR) {
                flgR = !pfleFile.isOlderThan(IncludeNewerThan);
            }
        }
        return flgR;
    }

    public String getFolderName() {
        return strFolderName;
    }

    public String addFileSeparator(final String str) {
        return str.endsWith("/") || str.endsWith("\\") ? str : str + "/";
    }

    private void init() {
        strFolderName = addFileSeparator(this.getAbsolutePath());
    }

    public JSFolder(URI uri) {
        super(uri);
        init();
    }

    public JSFolder(String parent, String child) {
        super(parent, child);
        init();
    }

    public JSFolder(File parent, String child) {
        super(parent, child);
        init();
    }

    public Vector<JSFile> getFilelist(final String regexp, final int flag) {
        if (!this.exists()) {
            throw new JobSchedulerException(String.format("directory does not exist: %1$s", this.getAbsolutePath()));
        }
        LOGGER.debug("regexp for filelist: " + regexp);
        Vector<JSFile> filelist = new Vector<>();
        try {
            for (File file : listFiles(new SOSFilelistFilter(regexp, flag))) {
                if (file.isFile()) {
                    filelist.add(new JSFile(file.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
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
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
        return objFolderList;
    }

    public JSFile getNewFile(final String pstrFileName) {
        return new JSFile(strFolderName + pstrFileName);
    }

    public static final JSFolder getTempDir() {
        return new JSFolder(System.getProperty("java.io.tmpdir"));
    }

    public int deleteFolder() {
        intNoOfObjectsDeleted = 0;
        return deleteFolder(this);
    }

    public int deleteFolder(final JSFolder pobjFolder) {
        try {
            if (pobjFolder.isDirectory()) {
                for (File file2 : pobjFolder.listFiles()) {
                    deleteFolder(new JSFolder(file2.getAbsolutePath()));
                }
            }
            pobjFolder.delete();
            intNoOfObjectsDeleted++;
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Folder '%1$s' not deleted due to an error.", pobjFolder.getAbsolutePath()), e);
        }
        return intNoOfObjectsDeleted;
    }

    public String checkFolder(final Boolean flgCreateIfNotExist) {
        if (!this.exists()) {
            if (!flgCreateIfNotExist) {
                LOGGER.error(String.format("Folder '%1$s' does not exist.", strFolderName));
            } else {
                this.mkdirs();
                LOGGER.debug(String.format("Folder '%1$s' created.", strFolderName));
            }
        }
        if (!this.canRead()) {
            LOGGER.error(String.format("File '%1$s'. canRead returns false. Check permissions.", strFolderName));
        }
        return strFolderName;
    }

    public Vector<String> deleteFileList(final SOSOptionRegExp objRegExpr4Files2Delete) {
        return deleteFileList(objRegExpr4Files2Delete.getValue());
    }

    public int deleteFiles(final String strRegExpr4Files2Delete) {
        return deleteFileList(strRegExpr4Files2Delete).size();
    }

    public Vector<String> deleteFileList(final String strRegExpr4Files2Delete) {
        int intNoOfFilesDeleted = 0;
        Vector<String> objFileList = new Vector<>();
        for (JSFile tempFile : this.getFilelist(strRegExpr4Files2Delete, 0)) {
            tempFile.delete();
            String strName = tempFile.getAbsolutePath();
            objFileList.add(strName);
            LOGGER.debug(String.format("File '%1$s' deleted", strName));
            intNoOfFilesDeleted++;
        }
        LOGGER.debug(String.format("%1$s files deleted matching the regexp '%2$s'", intNoOfFilesDeleted, strRegExpr4Files2Delete));
        return objFileList;
    }

    public int compressFiles(final String strRegExpr4Files2Compress) {
        return compressFileList(strRegExpr4Files2Compress).size();
    }

    public Vector<String> compressFileList(final SOSOptionRegExp objRegExpr4Files2Compress) {
        return compressFileList(objRegExpr4Files2Compress.getValue());
    }

    public Vector<String> compressFileList(final String strRegExpr4Files2Compress) {
        int intNoOfFilesCompressed = 0;
        Vector<String> objFileList = new Vector<>();
        for (JSFile tempFile : this.getFilelist(strRegExpr4Files2Compress, 0)) {
            LOGGER.debug(String.format("...compresing File '%1$s'", tempFile.getAbsolutePath()));
            tempFile.createZipFile(getFolderName());
            tempFile.delete();
            String strName = tempFile.getAbsolutePath();
            objFileList.add(strName);
            intNoOfFilesCompressed++;
            LOGGER.debug(String.format("File '%1$s' compressed", strName));
        }
        LOGGER.debug(String.format("%1$s files compressed matching the regexp '%2$s'", intNoOfFilesCompressed, strRegExpr4Files2Compress));
        return objFileList;
    }

}