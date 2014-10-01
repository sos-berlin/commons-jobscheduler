/**
 * 
 */
package com.sos.dialog.message;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

public class ErrorLog /* extends Exception */ {

	@SuppressWarnings("unused")
	private static final long	serialVersionUID	= -4414810697191992062L;
	@SuppressWarnings("unused")
	private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion		= "$Id: ErrorLog.java 20985 2013-09-04 09:13:12Z ur $";
	private final Logger		logger				= Logger.getLogger(this.getClass());

	public static String		gstrApplication		= "";

	public ErrorLog(final String msg) {
		super();
		try {
			message(msg, SWT.ERROR);
			logger.info(msg);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public ErrorLog(final String msg, final Exception e) {
		super();

		try {
			JobSchedulerException objJSE = new JobSchedulerException(msg, e);
			String strMsg = msg + "\n" + objJSE.ExceptionText();
			message(strMsg, SWT.ERROR);
			logger.error(strMsg);
		}
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public String getErrorMessage(final Exception ex) {
		String s = "";

		try {
			Throwable tr = ex.getCause();

			if (ex.toString() != null)
				s = ex.toString();

			while (tr != null) {
				if (s.indexOf(tr.toString()) == -1)
					s = (s.length() > 0 ? s + ", " : "") + tr.toString();
				tr = tr.getCause();
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return s;
	}

	private static Shell	sShell	= null;

	public static Shell getSShell() {
		if (sShell == null) {
			sShell = Display.getCurrent().getActiveShell();
		}
		return sShell;
	}

	public static void setSShell(Shell pobjShell) {
		sShell = pobjShell;
	}

	public static int message(String pstrMessage, int style) {
		MessageBox mb = new MessageBox(getSShell(), style);
		if (mb == null) {
			return -1;
		}
		if (pstrMessage == null) {
			pstrMessage = "??????";
		}
		mb.setMessage(pstrMessage);
		String title = getStyleText(style);
		mb.setText(gstrApplication + ": " + title);
		return mb.open();
	}

	public static String getStyleText(final int style) {
		String title = "message";
		switch (style) {
			case SWT.ICON_ERROR:
				title = "error";
				break;

			case SWT.ICON_INFORMATION:
				title = "information";
				break;

			case SWT.ICON_QUESTION:
				title = "question";
				break;

			case SWT.ICON_WARNING:
				title = "warning";
				break;

			default:
				break;
		}
		return getLabel(title);

	}

	private static String getLabel(final String pstrI18NKey) {
		String strR = "";
		strR = new DialogMsg(pstrI18NKey).get();
		return strR;
	}
}
