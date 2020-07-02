package com.sos.VirtualFileSystem.DataElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;

/** @author KB */
public class SOSVfsConnectionPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsConnectionPool.class);
    List<ISOSVFSHandler> list = Collections.synchronizedList(new ArrayList<ISOSVFSHandler>());

    public SOSVfsConnectionPool() {
        //
    }

    public void add(final ISOSVFSHandler pobjHandler) {
        list.add(pobjHandler);
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
        for (ISOSVFSHandler objHandler : list) {
            if (!objHandler.isLocked()) {
                objHandler.lock();
                return objHandler;
            }
        }
        return null;
    }

    public ISOSVFSHandler logout() {
        if (list != null) {
            for (ISOSVFSHandler objHandler : list) {
                try {
                    objHandler.closeSession();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
                objHandler.setLogin(false);
                objHandler.release();
            }
        }
        return null;
    }

}