package com.sos.VirtualFileSystem.Options.keepass4j;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;

public interface ISOSCredentialStoreOptions {
	/**
	 * \brief getCredentialStore_AuthenticationMethod :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionString getCredentialStore_AuthenticationMethod();

	/**
	 * \brief setCredentialStore_AuthenticationMethod :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_AuthenticationMethod :
	 */
	public abstract void setCredentialStore_AuthenticationMethod(SOSOptionString p_CredentialStore_AuthenticationMethod);

	/**
	 * \brief getCredentialStore_DeleteExportedFileOnExit :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionBoolean getCredentialStore_DeleteExportedFileOnExit();

	/**
	 * \brief setCredentialStore_DeleteExportedFileOnExit :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_DeleteExportedFileOnExit :
	 */
	public abstract void setCredentialStore_DeleteExportedFileOnExit(SOSOptionBoolean p_CredentialStore_DeleteExportedFileOnExit);

	/**
	 * \brief getCredentialStore_FileName :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionInFileName getCredentialStore_FileName();

	/**
	 * \brief setCredentialStore_FileName :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_FileName :
	 */
	public abstract void setCredentialStore_FileName(SOSOptionInFileName p_CredentialStore_FileName);

	/**
	 * \brief getCredentialStore_KeyFileName :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionInFileName getCredentialStore_KeyFileName();

	/**
	 * \brief setCredentialStore_KeyFileName :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_KeyFileName :
	 */
	public abstract void setCredentialStore_KeyFileName(SOSOptionInFileName p_CredentialStore_KeyFileName);

	/**
	 * \brief getCredentialStore_KeyPath :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionString getCredentialStore_KeyPath();

	/**
	 * \brief setCredentialStore_KeyPath :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_KeyPath :
	 */
	public abstract void setCredentialStore_KeyPath(SOSOptionString p_CredentialStore_KeyPath);

	/**
	 * \brief getCredentialStore_ProcessNotesParams :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionBoolean getCredentialStore_ProcessNotesParams();

	/**
	 * \brief setCredentialStore_ProcessNotesParams :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_ProcessNotesParams :
	 */
	public abstract void setCredentialStore_ProcessNotesParams(SOSOptionBoolean p_CredentialStore_ProcessNotesParams);

	/**
	 * \brief getCredentialStore_StoreType :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionString getCredentialStore_StoreType();

	/**
	 * \brief setCredentialStore_StoreType :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_StoreType :
	 */
	public abstract void setCredentialStore_StoreType(SOSOptionString p_CredentialStore_StoreType);

	/**
	 * \brief getCredentialStore_ExportAttachment :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionBoolean getCredentialStore_ExportAttachment();

	/**
	 * \brief setCredentialStore_ExportAttachment :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_ExportAttachment :
	 */
	public abstract void setCredentialStore_ExportAttachment(SOSOptionBoolean p_CredentialStore_ExportAttachment);

	/**
	 * \brief getCredentialStore_ExportAttachment2FileName :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionOutFileName getCredentialStore_ExportAttachment2FileName();

	/**
	 * \brief setCredentialStore_ExportAttachment2FileName :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_ExportAttachment2FileName :
	 */
	public abstract void setCredentialStore_ExportAttachment2FileName(SOSOptionOutFileName p_CredentialStore_ExportAttachment2FileName);

	/**
	 * \brief getCredentialStore_password :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionPassword getCredentialStore_password();

	/**
	 * \brief setCredentialStore_password :
	 *
	 * \details
	 *
	 *
	 * @param CredentialStore_password :
	 */
	public abstract void setCredentialStore_password(SOSOptionPassword p_CredentialStore_password);

	/**
	 * \brief getuse_credential_Store :
	 *
	 * \details
	 *
	 *
	 * \return
	 *
	 */
	public abstract SOSOptionBoolean getuse_credential_Store();

	/**
	 * \brief setuse_credential_Store :
	 *
	 * \details
	 *
	 *
	 * @param use_credential_Store :
	 */
	public abstract void setuse_credential_Store(SOSOptionBoolean p_use_credential_Store);
}