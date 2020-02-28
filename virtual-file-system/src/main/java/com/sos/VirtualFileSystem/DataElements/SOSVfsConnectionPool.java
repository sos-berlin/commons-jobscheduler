package com.sos.VirtualFileSystem.DataElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;

public class SOSVfsConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsConnectionPool.class);
    List<ISOSVFSHandler> list = Collections.synchronizedList(new ArrayList<ISOSVFSHandler>());

    public SOSVfsConnectionPool() {
        //
    }

    public void add(final ISOSVFSHandler handler) {
        list.add(handler);
    }

    public void clear() {
        this.logout();
        list = Collections.synchronizedList(new ArrayList<ISOSVFSHandler>());
    }

    public List<ISOSVFSHandler> getList() {
        if (list == null) {
            this.clear();
        }
        return list;
    }

    public ISOSVFSHandler getUnused() {
        for (ISOSVFSHandler handler : list) {
            if (!handler.isLocked()) {
                handler.lock();
                return handler;
            }
        }
        return null;
    }

    public ISOSVFSHandler logout() {
        if (list != null) {
            for (ISOSVFSHandler handler : list) {
                try {
                    handler.closeSession();
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
                handler.setLogin(false);
                handler.release();
            }
        }
        return null;
    }

}