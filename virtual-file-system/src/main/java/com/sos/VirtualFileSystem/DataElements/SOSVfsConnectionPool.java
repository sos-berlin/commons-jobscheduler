/**
 *
 */
package com.sos.VirtualFileSystem.DataElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;

/** @author KB */
public class SOSVfsConnectionPool {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final static Logger logger = Logger.getLogger(SOSVfsConnectionPool.class);
    List<ISOSVFSHandler> list = Collections.synchronizedList(new ArrayList<ISOSVFSHandler>());

    /**
	 *
	 */
    public SOSVfsConnectionPool() {
    }

    public void add(final ISOSVFSHandler pobjHandler) {
        // TODO avoid duplicate handler
        list.add(pobjHandler);
    }

    public void clear() {
        this.logout();
        list = Collections.synchronizedList(new ArrayList<ISOSVFSHandler>());
    }

    /** \brief getList
     *
     * \details Returns the list of the current instances of connections
     *
     * \return List<ISOSVFSHandler> */
    public List<ISOSVFSHandler> getList() {
        if (list == null) {
            this.clear();
        }
        return list;
    }

    /** \brief getUnused
     *
     * \details returns the next unused (idle) connection from the pool. Checks
     * the connection status and (re)connect, if needed. Checks the credentials
     * and make a (re)authentication, if needed. Set a lock to avoid duplicate
     * usage.
     *
     * \return ISOSVFSHandler */
    public ISOSVFSHandler getUnused() {
        for (ISOSVFSHandler objHandler : list) {
            if (objHandler.isLocked() == false) {
                // TODO check connect and user-id
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
                    logger.error(e.getLocalizedMessage());
                }
                objHandler.setLogin(false);
                objHandler.release();
            }
        }
        return null;
    }
}
