package com.sos.vfs.common;

import java.io.File;

public class SOSFileEntry {

    public static enum EntryType {
        FILESYSTEM, HTTP, SMB
    }

    private static final String FILE = "File";
    private static final String FOLDER = "Folder";

    private EntryType type;
    private String parentPath;
    private String filename;
    private long filesize;
    private boolean directory;
    // private long lastModified; last modified info is currently used only by YADE check steady state - get the current timestamp after an interval

    public SOSFileEntry(final EntryType val) {
        type = val;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String val) {
        filename = val;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long val) {
        filesize = val;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean val) {
        directory = val;
    }

    public String getParentPath() {
        if (parentPath == null) {
            return "";
        } else {
            return parentPath;
        }
    }

    public void setParentPath(String parent) {
        if (parent == null) {
            parentPath = "/";
        } else {
            if (type.equals(EntryType.FILESYSTEM)) {
                // TODO use nio
                parentPath = SOSCommonProvider.normalizePath(new File(parent).getPath());
            } else {
                parentPath = parent;
            }
        }
    }

    public String getFullPath() {
        if (type.equals(EntryType.FILESYSTEM)) {
            // TODO use nio Path
            return SOSCommonProvider.normalizePath(new File(parentPath, filename).getPath());
        } else {
            if (parentPath == null) {
                return filename;
            } else {
                String parent = parentPath.endsWith("/") ? parentPath : parentPath + "/";
                return parent + filename;
            }
        }
    }

    public boolean isDirUp() {
        return filename != null && "..".equals(filename);
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
        return !(filename == null || "..".equals(filename) || ".".equals(filename) || "".equals(filename));
    }
}
