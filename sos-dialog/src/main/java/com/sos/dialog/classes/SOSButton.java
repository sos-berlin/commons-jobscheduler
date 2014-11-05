/**
 *
 */
package com.sos.dialog.classes;
import static com.sos.dialog.Globals.MsgHandler;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sos.dialog.message.DialogMsg;
import com.sos.dialog.swtdesigner.SWTResourceManager;
import com.sos.localization.SOSMsg;

/**
 * @author KB
 *
 */
public class SOSButton extends Button {
	@SuppressWarnings("unused")
	private final String			conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String		conSVNVersion	= "$Id: SOSButton.java 23878 2014-04-22 18:02:41Z kb $";
	@SuppressWarnings("unused")
	private final Logger			logger			= Logger.getLogger(this.getClass());

	private final Vector<Control>	objControlList	= new Vector<>();

	/**
	 *
	 */
	public SOSButton(final Composite parent, final String pstrI18NKey) {
		super(parent, SWT.PUSH | SWT.FLAT);
		SOSMsg objM = null;
		if (pstrI18NKey.toLowerCase().startsWith("dialog_")) {
			objM = new DialogMsg(pstrI18NKey);
		}
		else { // use application-global message handler
			objM = MsgHandler.newMsg(pstrI18NKey);
		}
		setText(objM.label());
		setImage(SWTResourceManager.getImageFromResource(objM.icon()));
		String strAcc = objM.accelerator();
		if (strAcc != null) {
			//			this.ac
		}
		setToolTipText(objM.tooltip());
		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				//				setEnabledDisabled();
			}
		});
	}

	public void addChild(final Control pobjC) {
		objControlList.add(pobjC);
	}

	public void setEnabledDisabled() {
		boolean flgT = true;
		if (getSelection() == true) {
		}
		else {
			flgT = false;
		}
		for (Control objC : objControlList) {
			objC.setEnabled(flgT);
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
