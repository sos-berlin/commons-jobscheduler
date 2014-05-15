package com.sos.DataExchange;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;

@I18NResourceBundle(baseName = "SOSDataExchange", defaultLocale = "en")
public class SOSJadeMessageCodes extends JSToolBox {
	public final String			conSVNVersion					= "$Id$";

	public static final SOSMsgJade SOSJADE_I_0104 = new SOSMsgJade("SOSJADE_I_0104");	// "zero byte file(s) found";
	public static final SOSMsgJade SOSJADE_I_0100 = new SOSMsgJade("SOSJADE_I_0100");	// "one file transferred";
	public static final SOSMsgJade SOSJADE_I_0101 = new SOSMsgJade("SOSJADE_I_0101");	// "%1$d files transferred";
	public static final SOSMsgJade SOSJADE_I_0102 = new SOSMsgJade("SOSJADE_I_0102");	// "%1$d file(s) skipped due to zero byte constraint";
	public static final SOSMsgJade SOSJADE_I_0115 = new SOSMsgJade("SOSJADE_I_0115");	// "Operation 'getList' is specified. no transfer will be done.";
	public static final SOSMsgJade SOSJADE_E_0101 = new SOSMsgJade("SOSJADE_E_0101");	// "data transfer ended with error '%1$s'";
	public static final SOSMsgJade SOSJADE_E_0200 = new SOSMsgJade("SOSJADE_E_0200");	// "Problems creating/connecting DataSourceClient";
	public static final SOSMsgJade SOSJADE_D_0200 = new SOSMsgJade("SOSJADE_D_0200");	// "source directory/file: '%1$s' file regexp: '%2$s'";
	
	
	public static final SOSMsgJade SOSJADE_T_0010 = new SOSMsgJade("SOSJADE_T_0010");	// 
	public static final SOSMsgJade SOSJADE_T_0011 = new SOSMsgJade("SOSJADE_T_0011");	// 
	public static final SOSMsgJade SOSJADE_T_0012 = new SOSMsgJade("SOSJADE_T_0012");	// 
	public static final SOSMsgJade SOSJADE_T_0013 = new SOSMsgJade("SOSJADE_T_0013");	// 
	
	public static final SOSMsgJade 	SOSJADE_E_0100					= new SOSMsgJade("SOSJADE_E_0100");													// "No file name found which match the regular expression criteria '1$s'";

	/*!
	 * \var TRANSACTION_ABORTED
	 * \brief could not complete transaction, cause: %1$s
	 */
	public static final SOSMsgJade	TRANSACTION_ABORTED				= new SOSMsgJade("SOSDataExchangeEngine.TRANSACTION_ABORTED");
	/*!
	 * \var EXCEPTION_RAISED
	 * \brief %1$s: Exception raised, cause: %2$s
	 */

	public static final SOSMsgJade		EXCEPTION_RAISED				=  new SOSMsgJade("EXCEPTION_RAISED");

	protected SOSJadeMessageCodes() {
		super();
	}
}
