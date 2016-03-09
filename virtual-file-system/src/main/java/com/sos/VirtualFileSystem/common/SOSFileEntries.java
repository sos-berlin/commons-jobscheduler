package com.sos.VirtualFileSystem.common;

import java.util.ArrayList;
import java.util.Iterator;

public class SOSFileEntries implements Iterable<SOSFileEntry> {

    private ArrayList<SOSFileEntry> sosFileEntries;

    public SOSFileEntries() {
        super();
        sosFileEntries = new ArrayList<SOSFileEntry>();
    }

    public int size() {
        return sosFileEntries.size();
    }

    @Override
    public Iterator<SOSFileEntry> iterator() {
        return sosFileEntries.iterator();
    }

    public void add(SOSFileEntry sosFileEntry) {
        if (sosFileEntry.isFileOrFolder()) {
            sosFileEntries.add(sosFileEntry);
        }
    }

    public void clear() {
        sosFileEntries.clear();
    }

}
