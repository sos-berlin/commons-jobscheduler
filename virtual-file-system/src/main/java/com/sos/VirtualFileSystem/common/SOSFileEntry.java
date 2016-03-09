package com.sos.VirtualFileSystem.common;

import java.io.File;

public class SOSFileEntry {

    private static final String FILE = "File";
    private static final String FOLDER = "Folder";
    private String filename;
    private long filesize;
    private boolean directory;
    private String parentPath;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public String getParentPath() {
        if (parentPath == null) {
            return "";
        } else {
            return parentPath;
        }
    }

    public void setParentPath(String parentPath) {
        if (parentPath == null) {
            parentPath = "/";
        } else {
            parentPath = new File(parentPath).getPath();
            if (parentPath != null) {
                this.parentPath = parentPath.replaceAll("\\\\", "/");
            } else {
                this.parentPath = parentPath;
            }
        }
    }

    public String getFullPath() {
        File f = new File(parentPath, filename);
        String p = f.getPath();
        p = p.replaceAll("\\\\", "/");
        return p;
    }

    public boolean isDirUp() {
        return (filename != null && filename.equals(".."));
    }

    public String getFilesizeAsString() {
        return String.valueOf(filesize);
    }

    public String getCategory() {
        if (isDirectory()) {
            return FOLDER;
        } else {
            return FILE;
        }
    }

    public boolean isFileOrFolder() {
        return !(filename == null || filename.equals("..") || filename.equals(".") || filename.equals(""));
    }
}
