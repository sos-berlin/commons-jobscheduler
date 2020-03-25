package com.sos.VirtualFileSystem.DataElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sos.VirtualFileSystem.Interfaces.ISOSTransferHandler;

public class SOSVfsConnectionPool {

    List<ISOSTransferHandler> list = Collections.synchronizedList(new ArrayList<ISOSTransferHandler>());

    public SOSVfsConnectionPool() {
        //
    }

    public void add(final ISOSTransferHandler handler) {
        list.add(handler);
    }

    private void clear() {
        list = Collections.synchronizedList(new ArrayList<ISOSTransferHandler>());
    }

    public List<ISOSTransferHandler> getList() {
        if (list == null) {
            clear();
        }
        return list;
    }

    public ISOSTransferHandler getUnused() {
        for (ISOSTransferHandler handler : list) {
            return handler;
        }
        return null;
    }
}