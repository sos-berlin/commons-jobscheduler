/**
 *
 */
package com.sos.dialog.components;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

import com.sos.JSHelper.Options.IValueChangedListener;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.dialog.Globals;
import com.sos.dialog.classes.SOSCTabFolder;
import com.sos.dialog.classes.SOSCTabItem;
import com.sos.dialog.interfaces.ICompositeBaseAbstract;
import com.sos.dialog.interfaces.ISOSTabItem;
import com.sos.dialog.layouts.Gridlayout;

/**
 * @author KB
 *
 */
public abstract class CompositeBaseClass<T> extends Composite implements ISOSTabItem, ICompositeBaseAbstract {
	private final String		conClassName				= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion				= "$Id$";
	protected Logger			logger						= Logger.getLogger(this.getClass());
	protected Composite			objParent					= null;
	protected T					objJadeOptions				= null;
	protected ControlCreator	objCC						= null;
	protected Composite			composite					= this;
	public static boolean		gflgCreateControlsImmediate	= true;

	private static int			intCompositeStyle			= SWT.None;														// .H_SCROLL | SWT.V_SCROLL;

	protected WaitCursor		objC						= null;

	public CompositeBaseClass(final SOSCTabFolder pobjCTabFolder, final T objOptions) {
		super(pobjCTabFolder, intCompositeStyle);
		objJadeOptions = objOptions;
		getControlCreator(this);
	}

	public CompositeBaseClass(final Composite parent, final T objOptions) {
		super(parent, intCompositeStyle);
		objJadeOptions = objOptions;
		getControlCreator(this);
	}

	public CompositeBaseClass(final Composite parent) {
		super(parent, SWT.None);
		getControlCreator(this);
	}

	private void getControlCreator(final Composite pobjParentComposite) {
		composite = pobjParentComposite;
		objParent = pobjParentComposite;
		//				Gridlayout.set4ColumnLayout(composite);
		setLayout(Gridlayout.get4ColumnLayout());
		objCC = new ControlCreator(this);
		//		objCC.getInvisibleSeparator();
		setBackground(Globals.getCompositeBackground());
	}

	@Override
	public boolean setParent(final Composite pobjParent) {
		super.setParent(pobjParent);
		return true;
	}

	protected boolean	flgCompositeIsCreated	= false;

	@Override
	public void createTabItemComposite() {
		//		Gridlayout.set4ColumnLayout(composite);
		if (flgCompositeIsCreated == false) {
			objCC = new ControlCreator(composite);
			createComposite();
			logger.debug("createTabItemComposite " + conClassName);
			composite.pack();
			composite.layout(true, true);
			composite.getParent().layout(true, true);
		}
		flgCompositeIsCreated = true;
	}
	protected SelectionAdapter	EnableFieldsListener	= new SelectionAdapter() {
															@Override
															public void widgetSelected(final SelectionEvent e) {
																enableFields();
															}
														};

	protected void enableFields() {
	}

	@Override
	public boolean validateData() {
		return true;
	}

	@Override
	public void dispose() {
		logger.debug("Control disposed: " + conClassName);
		if (composite.isDisposed() == false) {
			for (Control objContr : composite.getChildren()) {
				//			Object objO = objContr.getData();
				//			if (objO != null && objO instanceof SOSOptionElement) {
				//				
				//			}
				Object objO = objContr.getData();
				if (objO instanceof IValueChangedListener) {
					SOSOptionElement objV = (SOSOptionElement) objO;

				}
				Listener[] objL = objContr.getListeners(SWT.ALL);
				for (Listener listener : objL) {
					objContr.removeListener(SWT.ALL, listener);
				}
				//
			}
			super.dispose();
			composite.dispose();
		}
	}

	protected void createTab(SOSCTabFolder pobjMainTabFolder, Composite pobjComposite, final String pstrI18NKey) {
		SOSCTabItem tbtmItem = pobjMainTabFolder.getTabItem(pstrI18NKey);
		tbtmItem.setComposite((ISOSTabItem) pobjComposite);
		tbtmItem.setControl(pobjComposite);
	}

	@Override
	public void createGroup(final Composite parent) {
	};

	@Override
	public void init() {
	};

	@Override
	public Composite createComposite(final Composite parent) {
		return this;
	};

	@Override
	public String getWindowTitle() {
		return "Window-Title";
	};

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
