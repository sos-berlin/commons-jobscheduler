/**
 * 
 */
package com.sos.JSHelper.interfaces;

/**
 * @author KB
 *
 */
public interface IDirty {

	/**
	 * 
	* \brief setDirty - 	
	*
	* \details
	* 
	*
	* @param pflgIsDirty
	* @param 
	* @author KB
	 */
	public void setDirty(final boolean pflgIsDirty);
	
	/**
	 * 
	* \brief setDirty - 	
	*
	* \details
	* 
	*
	* @param 
	* @author KB
	 */
	public void setDirty();

	/**
	 * 
	* \brief isDirty - 	
	*
	* \details
	* 
	*
	* @return
	* @param 
	* @author KB
	 */
	public boolean isDirty();

	/**
	 * 
	* \brief doSave - 	
	*
	* \details
	* 
	*
	* @return SWT.Yes, SWT.No, SWT.Cancel
	* @param 
	* @author KB
	 */
	public int doSave(final boolean pflgAskForSave);

}
