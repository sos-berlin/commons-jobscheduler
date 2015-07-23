package com.sos.JSHelper.Options;
import java.io.File;
import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.io.Files.JSFolder;


/**
 * @author KB
 *
 */
public class SOSOptionFolderName extends SOSOptionFileName {
	private static final long	serialVersionUID	= 1197392401084895147L;
	private final String		conClassName		= "JSOptionFolderName";
	public final String			ControlType			= "folder";

	public SOSOptionFolderName(final String pstrFolderName) {
		super(null, "", "description", pstrFolderName, "", false);
	}
	/**
	* \brief CreateFolder - Option: Folder anlegen, wenn noch nicht vorhanden
	*
	* \details
	*
	*/
	@JSOptionDefinition(name = "CreateFolder", value = "true", description = "Folder anlegen, wenn noch nicht vorhanden", key = "CreateFolder", type = "JSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	CreateFolder	= new SOSOptionBoolean(objParentClass, // Verweis auf die SOSOptionClass-Instanz
													".CreateFolder", // Schlüssel, i.d.r. identisch mit dem Namen der Option
													"Folder anlegen, wenn noch nicht vorhanden", // Kurzbeschreibung
													"true", // Wert
													"true", // defaultwert
													false // Option muss einen Wert haben
											);

	/**
	 * \brief JSOptionFolderName
	 *
	 * \details
	 *
	 * @param pobjParent
	 * @param pstrKey
	 * @param pstrDescription
	 * @param pstrValue
	 * @param pstrDefaultValue
	 * @param pflgIsMandatory
	 */
	public SOSOptionFolderName(final JSOptionsClass pobjParent, final String pstrKey, final String pstrDescription, final String pstrValue,
			final String pstrDefaultValue, final boolean pflgIsMandatory) {
		super(pobjParent, pstrKey, pstrDescription, pstrValue, pstrDefaultValue, pflgIsMandatory);
		intOptionType = isOptionTypeFolder;
	}

	/**
	 * \brief Value - Wert der Option liefern
	 *
	 * \details
	 *
	 * @param pstrValue
	 * @return
	 */
	@Override
	public String Value() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Value";
		if (strValue == null) {
			strValue = "";
		}
		String strLValue = super.Value();
		if (IsNotEmpty()) {
			if (strLValue.endsWith("/") || strLValue.endsWith("\\") || isDotFolder()) {
			}
			else {
				strLValue = strLValue + "/";
			}
			if (objParentClass != null) {
				// prüfen, ob es den Folder gibt ...
				//				this.strValue = this.objParentClass.CheckFolder(this.strValue, conMethodName, this.CreateFolder.flgValue);
			}
		}
		return strLValue;
	}

	public boolean isDotFolder() {
		String strT = super.Value();
		return strT.equals(".") || strT.equals("..");
	}

	public File[] listFiles() {
		File[] objFL = this.JSFile().listFiles();
		if (objFL != null) {
		}
		else {
			throw new JobSchedulerException(String.format("No Files found for pathname '%1$s'", strValue));
		}
		return objFL;
	}

	public String[] getSubFolderArray() {
		String[] strRet = null;
		try {
			String path = strValue.trim().replaceAll("/(\\s*/)+", "/");
			String strPath = "";
			String strSlash = "";
			int iStart = 0;
			if (path.startsWith("/")) {
				strSlash = "/";
				iStart = 1;
			}
			String[] pathArray = path.substring(iStart).split("/");
			strRet = new String[pathArray.length];
			int i = 0;
			for (String strSubFolder : pathArray) {
				strPath += strSlash + strSubFolder;
				strSlash = "/";
				strRet[i] = strPath;
				i++;
			}
		}
		catch (Exception e) {
		}
		return strRet;
	}

	public String[] getSubFolderArrayReverse() {
		String[] strRet = null;
		try {
			String path = strValue.trim().replaceAll("/(\\s*/)+", "/");
			String strPath = "";
			String strSlash = "";
			int iStart = 0;
			if (path.startsWith("/")) {
				strSlash = "/";
				iStart = 1;
			}
			String[] pathArray = path.substring(iStart).split("/");
			strRet = new String[pathArray.length];
			int i = pathArray.length - 1;
			for (String strSubFolder : pathArray) {
				strPath += strSlash + strSubFolder;
				strSlash = "/";
				strRet[i] = strPath;
				i--;
			}
		}
		catch (Exception e) {
		}
		return strRet;
	}

	public JSFolder getFolder() {
		return new JSFolder(strValue);
	}
	
	private static final HashMap <String, String> defaultProposals = new HashMap<>();
	
	@Override
	public void addProposal (final String pstrProposal) {
		if (pstrProposal != null && pstrProposal.trim().length() > 0) {
			String strT = pstrProposal.trim();
			SOSOptionFolderName.defaultProposals.put(strT, strT);
		}
	}
	
	@Override
	public String[] getAllProposals(String text) {
		String[] proposals = SOSOptionFolderName.defaultProposals.keySet().toArray(new String[0]);
		return proposals;
	}


}
