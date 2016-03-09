/**
 *
 */
package com.sos.dialog.interfaces;

/** @author KB */
public interface ISOSPreferenceStore {

    public void setPreferenceStoreKey(final String pstrKey);

    public String getPreferenceStoreKey();

    public String readPreferenceStore();

    public String writePreferenceStore(final String strT);

}
