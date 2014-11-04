/**
 *
 */
package com.sos.dialog.classes;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sos.dialog.Globals;

/**
 * @author KB
 *
 */
public class SOSComposite extends Composite {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private final Logger logger = Logger.getLogger(this.getClass());

	private final Vector <Control> objControlList = new Vector <Control> ();

	/**
	 *
	 */
	public static SOSComposite newComposite(final Control comp) {
		return new SOSComposite((Composite) comp, SWT.None);
	}
	public SOSComposite(final Composite parent, final int style) {
		super(parent,SWT.None);
		this.setBackground(Globals.getCompositeBackground());
		GridLayout gridLayout = new GridLayout(1, false);
		setLayout(gridLayout);
        //ur 20141104: When setting a gridlayout Data here you will get a class cast exception in JOE when resizing the window.
		//setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public void addChild (final Control pobjC) {
		objControlList.add(pobjC);
	}

	@Override protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void dispose() {
		super.dispose();
	}
}
