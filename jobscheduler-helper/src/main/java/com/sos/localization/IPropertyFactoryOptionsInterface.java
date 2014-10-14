package com.sos.localization;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionString;

/**
* \interface IPropertyFactoryOptionsInterface - Interface for PropertyFactora - a Factoroy to maintain I18N Files
*
* \brief
*
* 
*
* see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-297718331111000308.html for (more) details.
*
* \verbatim ;
* mechanicaly created by com/sos/resources/xsl/jobdoc/sourcegenerator/java/JSJobDoc2JSOptionInterface.xsl	from http://www.sos-berlin.com at 20141009200110
* \endverbatim
*/
public interface IPropertyFactoryOptionsInterface {

	/**
	* \brief getOperation:
	*
	* \details
	*

	*
	* \return 
	*
	*/
	public abstract SOSOptionString Operation();

	/**
	* \brief set				Operation			:			
	*
	* \details
	* 
	*
	* @param				Operation : 
	*/
	public abstract void Operation(SOSOptionString p_Operation);

	/**
	* \brief getPropertyFileName:
	*
	* \details
	*

	*
	* \return 
	*
	*/
	public abstract SOSOptionString PropertyFileNamePrefix();

	/**
	* \brief set				PropertyFileName			:			
	*
	* \details
	* 
	*
	* @param				PropertyFileName : 
	*/
	public abstract void PropertyFileNamePrefix(SOSOptionString p_PropertyFileName);

	/**
	* \brief getSourceFolderName:The Folder, which has all the I18N Property files.
	*
	* \details
	*
	The Folder, which has all the I18N Property files.
	*
	* \return The Folder, which has all the I18N Property files.
	*
	*/
	public abstract SOSOptionFolderName SourceFolderName();

	/**
	* \brief set				SourceFolderName			:			The Folder, which has all the I18N Property files.
	*
	* \details
	* The Folder, which has all the I18N Property files.
	*
	* @param				SourceFolderName : The Folder, which has all the I18N Property files.
	*/
	public abstract void SourceFolderName(SOSOptionFolderName p_SourceFolderName);

} // public interface IPropertyFactoryOptionsInterface