package com.sos.VirtualFileSystem.common;

import java.io.File;

public class SOSFileEntry {

    // TODO webdav, http, smb
    public static enum EntryType {
        FILESYSTEM, HTTP, SMB, ZIP
    }

    private static final String FILE = "File";
    private static final String FOLDER = "Folder";
    private String filename;
    private long filesize;
    private boolean directory;
    private String parentPath;
    private EntryType type;

    public SOSFileEntry(final EntryType entryType) {
        type = entryType;
    }

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

    public void setParentPath(String parent) {
        if (parent == null) {
            parentPath = "/";
        } else {
            if (type.equals(EntryType.FILESYSTEM)) {
                // TODO use nio
                parentPath = new File(parent).getPath().replaceAll("\\\\", "/");
            } else {
                // TODO not tested
                parentPath = parent;
            }
        }
    }

    public String getFullPath() {
        if (type.equals(EntryType.FILESYSTEM)) {
            // TODO use nio Path
            String path = new File(parentPath, filename).getPath();
            return path.replaceAll("\\\\", "/");
        } else {
            // TODO not tested
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
