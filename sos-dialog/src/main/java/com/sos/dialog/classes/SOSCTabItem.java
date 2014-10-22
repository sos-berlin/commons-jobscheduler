/**
 *
 */
package com.sos.dialog.classes;

import static com.sos.dialog.classes.SOSCTabFolder.conCOMPOSITE_OBJECT_KEY;
import static com.sos.dialog.classes.SOSCTabFolder.conTABITEM_I18NKEY;
import static com.sos.dialog.classes.SOSCTabFolder.conTABITEM_SOSITEM;

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
		return (Composite) getData(conCOMPOSITE_OBJECT_KEY);
	}

	public void setComposite(final ISOSTabItem pobjComposite) {
		setData(conCOMPOSITE_OBJECT_KEY, pobjComposite);
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
			setChilds2Null();
			Control objC = this.getControl();
			if (objC != null) {
				if (objC.isDisposed() == false) {
					objC.dispose();
				}
			}
			else {
			}
			super.dispose();
		}
	}

	public void setChilds2Null() {
		try {
			setData(conCOMPOSITE_OBJECT_KEY, null);
			setData(conTABITEM_I18NKEY, null);
			setData(conTABITEM_SOSITEM, null);
			setData(null);
			objTabFolder = null;
			objTabComposite = null;

			this.setImage(null);
			if (getControl().isDisposed() == false && this.isDisposed() == false) {
				setControl(null);
			}
			setFont(null);
			setComposite(null);
		}
		catch (Exception e) {
		}
		finally {
			objTabFolder = null;
			logger = null;
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
