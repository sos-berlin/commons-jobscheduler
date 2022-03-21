package com.sos.vfs.common;

import sos.util.SOSString;

public class SOSFileEntryFile {

    private final String parent;
    private final String name;

    public SOSFileEntryFile(String path) {
        if (SOSString.isEmpty(path)) {
            parent = null;
            name = null;
        } else if (path.equals("/")) {
            parent = "/";
            name = null;
        } else {
            final int i = path.lastIndexOf("/");
            if (i < 0) {// test.txt
                parent = null;
                name = path;
            } else {
                if (path.endsWith("/")) {
                    parent = path.substring(0, i);
                    name = "";
                } else {
                    parent = ((i >= 0) ? path.substring(0, i) : path);
                    name = ((i >= 0) ? path.substring(i + 1) : path);
                }
            }
        }
    }

    public String getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

}
