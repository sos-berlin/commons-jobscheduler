package com.sos.JSHelper.interfaces;

import com.sos.JSHelper.Options.JSOptionsClass;

public interface IJadeEngine extends Runnable {

    public abstract boolean Execute() throws Exception;

    // public abstract SOSFileList getFileList();
    /** \brief getCC
     *
     * \details
     * 
     * \return int */
    public abstract int getCC();

    /** \brief getState
     *
     * \details
     * 
     * \return String */
    public abstract String getState();

    /** \brief Logout
     *
     * \details
     *
     * \return void */
    public abstract void Logout();

    /** \brief setJadeOptions
     *
     * \details
     * 
     * \return void */
    public abstract void setJadeOptions(JSOptionsClass pobjOptions);

    @Override
    public abstract void run();
}