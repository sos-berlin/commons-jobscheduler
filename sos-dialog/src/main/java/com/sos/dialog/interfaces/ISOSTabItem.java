/**
 *
 */
package com.sos.dialog.interfaces;

import org.eclipse.swt.widgets.Composite;

/** @author KB */
public interface ISOSTabItem {

    /*
     * private static final String conSVNVersion = "$Id$";
     */

    /** \brief createTabItemComposite
     *
     * \details
     * 
     * \return void */
    public void createTabItemComposite();

    /** \brief createComposite
     *
     * \details
     * 
     * \return void */
    public void createComposite();

    /** \brief validateData
     *
     * \details
     * 
     * \return boolean */
    public boolean validateData();

    /** \brief setParent
     *
     * \details
     * 
     * \return boolean */
    public boolean setParent(final Composite pobjParent);

}
