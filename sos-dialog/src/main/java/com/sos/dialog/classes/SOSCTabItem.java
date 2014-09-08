/**
 *
 */
package com.sos.dialog.classes;

import org.apache.log4j.Logger;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sos.dialog.interfaces.ISOSTabItem;

/**
 * @author KB
 *
 */
public class SOSCTabItem extends CTabItem {

	@SuppressWarnings("unused")
	private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused")
	private Logger				logger			= Logger.getLogger(this.getClass());

	@SuppressWarnings("unused")
	private SOSCTabFolder		objTabFolder	= null;
	@SuppressWarnings("unused")
	private ISOSTabItem			objTabComposite	= null;

	/**
	 * @param parent
	 * @param style
	 */
	public SOSCTabItem(final SOSCTabFolder parent, final int style) {
		super(parent, style);
		objTabFolder = parent;
	}

	public Composite getComposite() {
		return (Composite) getData("composite");
	}

	public void setComposite(final ISOSTabItem pobjComposite) {
		setData("composite", pobjComposite);
		setData(pobjComposite);
		objTabComposite = pobjComposite;
	}

	/**
	 * @param parent
	 * @param style
	 * @param index
	 */
	public SOSCTabItem(final CTabFolder parent, final int style, final int index) {
		super(parent, style, index);
	}

	public void setParent(final SOSCTabFolder pobjTabFolder) {

	}

	@Override
	public void dispose() {
		if (this.isDisposed() == false) {
			Control objC = this.getControl();
			if (objC != null && objC.isDisposed() == false) {
				setChilds2Null();
				objC.dispose();
				objTabFolder = null;
				objTabComposite = null;
				//			this.dispose();
				super.dispose();
			}
		}
	}

	public void setChilds2Null() {
		setData("composite", null);
		this.setImage(null);
		setData("key", null);
		setControl(null);
		setData(null);
		setFont(null);
		setComposite(null);
		logger = null;
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
