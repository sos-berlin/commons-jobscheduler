/**
 *
 */
package com.sos.JSHelper.Options;

/**
 * @author KB
 *
 */
public interface IValueChangedListener {

	/**
	 * 
	*
	* \brief ValueHasChanged
	*
	* \details
	* 
	* \return void
	*
	 */
	public void ValueHasChanged (final String pstrNewValue);

	/**
	 * 
	*
	* \brief ValidationError
	*
	* \details
	* 
	* \return void
	*
	 */
	public void ValidationError (final SOSValidationError pobjVE);
}
