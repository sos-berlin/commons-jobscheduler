package sos.scheduler.file;

import java.io.File;

import org.apache.log4j.Logger;

public class FileDescriptor {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());
    private long lastModificationDate;
    private long lastFileLength;
    private String FileName;
    private boolean flgIsSteady;

    public FileDescriptor() {
        super();
    }

    public FileDescriptor(final File objFile) {
        this();
        setLastFileLength(objFile.length());
        setLastModificationDate(objFile.lastModified());
        setFileName(objFile.getAbsolutePath());
    }

    public FileDescriptor(long lastModificationDate, long lastFileLength, String fileName, boolean flgIsSteady) {
        this.lastModificationDate = lastModificationDate;
        this.lastFileLength = lastFileLength;
        FileName = fileName;
        this.flgIsSteady = flgIsSteady;
    }

    public File getFile() {
        File objActFile = new File(getFileName());
        logger.debug("result is : " + objActFile.lastModified() + ", " + this.getLastModificationDate() + ", " + objActFile.length() + ", "
                + this.getLastFileLength());

        return objActFile;
    }

    public boolean isSteady() {
        boolean isSteady = true;
        File objActFile = getFile();
        if (objActFile.lastModified() != this.getLastModificationDate() || objActFile.length() != this.getLastFileLength()) {
            setLastModificationDate(objActFile.lastModified());
            setLastFileLength(objActFile.length());
            setFlgIsSteady(false);
            isSteady = false;
            logger.info(String.format("File '%1$s' changed during checking steady state", objActFile.getAbsolutePath()));
        } else {
            setFlgIsSteady(true);
        }
        return isSteady;
    }

    public long getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(long lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }

    public long getLastFileLength() {
        return lastFileLength;
    }

    public void setLastFileLength(long lastFileLength) {
        this.lastFileLength = lastFileLength;
    }

    public String getFileName() {
        return FileName;
    }

    public void setFileName(String fileName) {
        FileName = fileName;
    }

    public boolean isFlgIsSteady() {
        return flgIsSteady;
    }

    public void setFlgIsSteady(boolean flgIsSteady) {
        this.flgIsSteady = flgIsSteady;
    }
}