/**
 *
 */
package com.sos.dialog.classes;
import static com.sos.dialog.Globals.MsgHandler;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.sos.JSHelper.interfaces.IDirty;
import com.sos.dialog.Globals;
import com.sos.dialog.components.CompositeBaseClass;
import com.sos.dialog.components.SOSCursor;
import com.sos.dialog.interfaces.ISOSControlProperties;
import com.sos.dialog.interfaces.ISOSTabItem;
import com.sos.dialog.layouts.Gridlayout;
import com.sos.dialog.message.ErrorLog;
import com.sos.dialog.swtdesigner.SWTResourceManager;

/**
 * @author KB
 *
 */
public class SOSCTabFolder extends CTabFolder {
	@SuppressWarnings("unused")
	private final String		conClassName				= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion				= "$Id$";
	private final Logger		logger						= Logger.getLogger(this.getClass());
	//	private final Vector<SOSCTabItem>	objItemList					= new Vector<SOSCTabItem>();
	public boolean				ItemsHasClose				= true;
	public boolean				gflgCreateControlsImmediate	= true;
	public boolean				flgRejectTabItemSelection	= false;

	/**
	 * @param parent
	 * @param style
	 */
	public SOSCTabFolder(final Composite parent, final int style) {
		super(parent, style);
		final CTabFolder objTabF = this;
		objTabF.setSimple(true);
		setMaximizeVisible(false);
		this.setBorderVisible(false);
		setTabHeight(getTabHeight() + 6);
		this.setBackground(Globals.getCompositeBackground());

		//		this.setSelectionBackground(new Color[]{new Color(getDisplay(), new RGB(242, 244, 247)), new Color(getDisplay(), new RGB(157, 167, 195))}, new int[]{100}, true);
		this.setSelectionBackground(new Color[] { Globals.getFieldHasFocusBackground(), Globals.getCompositeBackground(), Globals.getCompositeBackground() },
				new int[] { 50, 100 }, true);
		Gridlayout.set4ColumnLayout(this);
		//
		addFocusListener(new FocusListener() {
			@Override
			public void focusLost(final FocusEvent e) {
				// TODO validate?
			}

			@SuppressWarnings("unused")
			@Override
			public void focusGained(final FocusEvent e) {
				logger.debug("focusGained");
				SOSCTabFolder o = (SOSCTabFolder) e.widget;
				Object d = e.data;
				Object f = e.getSource();
				//				handleSelection();
			}
		});
		/**
		 * SelectionListener
		 */
		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				logger.debug("CTabFolder Item selected");
				if (flgRejectTabItemSelection == true) {
					return;
				}
				CTabItem objSelectedItem = getSelection();
				Composite objCurrComposite = (Composite) objSelectedItem.getControl();
				if (objCurrComposite instanceof CompositeBaseClass) {
					if (CompositeBaseClass.gflgCreateControlsImmediate == false) {
						CompositeBaseClass<?> objBC = (CompositeBaseClass<?>) objCurrComposite;
						objBC.createTabItemComposite();
//						objCurrComposite.pack(true);
					}
				}
				CTabItem tbiLastSelected = (CTabItem) getData("lastSelected");
				if (tbiLastSelected == objSelectedItem) {
					return;
				}
				if (tbiLastSelected != null) {
					Composite objC = (Composite) tbiLastSelected.getControl();
					if (objC != null && objC.isDisposed()) {
						objC.dispose();
					}
					if (objC != null && objC.isDisposed() != true) {
					}
				}
				setData("lastSelected", objSelectedItem);
			}
		});
		this.addListener(SWT.MouseDoubleClick, Globals.listener);
		this.addListener(SWT.MouseDown, Globals.listener);
		this.addListener(SWT.MouseMove, Globals.listener);
		this.addListener(SWT.MouseUp, Globals.listener);
		this.addListener(SWT.MouseEnter, Globals.listener);
		this.addListener(SWT.MouseExit, Globals.listener);
		this.addListener(SWT.MouseWheel, Globals.listener);
		this.addListener(SWT.MouseHorizontalWheel, Globals.listener);
		this.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				System.out.println("doubleClick");
				maximizeToSashForm();
				event.doit = true;
				//				if (event.y > getTabHeight())
				//					return;
			}
		});
		Menu contextMenu = new Menu(this);
		MenuItem close = new MenuItem(contextMenu, SWT.NONE);
		close.setText("Close");
		close.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				CTabItem objTabItem = getSelection();
				event.doit = closeTabItem(objTabItem);
			}
		});
		MenuItem closeOthers = new MenuItem(contextMenu, SWT.NONE);
		closeOthers.setText("Close Others");
		closeOthers.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				CTabItem objTabItem = getSelection();
				for (CTabItem objTI : getItems()) {
					if (objTI != objTabItem) {
						closeTabItem(objTI);
					}
				}
			}
		});
		MenuItem closeAll = new MenuItem(contextMenu, SWT.NONE);
		closeAll.setText("Close All");
		closeAll.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				for (CTabItem objTI : getItems()) {
					closeTabItem(objTI);
				}
			}
		});
		this.setMenu(contextMenu);
		this.addSelectionListener(new SelectionListener() {
			boolean	flgIsActive	= false;

			@Override
			public void widgetDefaultSelected(final SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override
			public void widgetSelected(final SelectionEvent arg0) {
				logger.debug("widgetSelected");
				arg0.doit = true;
			}
		});
		this.addCTabFolder2Listener(new CTabFolder2Adapter() {
			public void itemClosed(final CTabFolderEvent event) {

			}

			@Override
			public void close(final CTabFolderEvent event) {
				SOSCTabItem objI = (SOSCTabItem) event.item;
				if (objI != null) {
					event.doit = closeTabItem(objI);
				}
			} // close

			@Override
			public void minimize(final CTabFolderEvent event) {
			}

			@Override
			public void maximize(final CTabFolderEvent event) {
			}

			@Override
			public void restore(final CTabFolderEvent event) {
			}

			@Override
			public void showList(final CTabFolderEvent event) {
			}
		});
	}

	private boolean closeTabItem(final CTabItem pobjTabItem) {
		boolean flgDoClose = true;
		try (SOSCursor objC = new SOSCursor().showWait()) {
			int buttonID = -99;

			if (pobjTabItem != null) {
				Object objO = pobjTabItem.getData();
				if (objO instanceof IDirty) {
					IDirty objDH = (IDirty) objO;
					if (objDH.isDirty()) {
						buttonID = objDH.doSave(true);
					}
					else {
						buttonID = SWT.NO;
					}
				}
				switch (buttonID) {
					case SWT.CANCEL:
						flgDoClose = false;
						break;
					case SWT.YES:
					case SWT.NO:
						pobjTabItem.dispose();
						logger.debug("tabitem disposed");
						System.gc();
						flgDoClose = true;
						break;

					default:
						break;
				}
			}
			return flgDoClose;
		}
		catch (Exception e) {
			new ErrorLog("problem", e);
		}
		return flgDoClose;
	}

	private void handleSelection() {
		try (SOSCursor objCurs = new SOSCursor().showWait()) {
			logger.debug("handleSelection");
			CTabItem objSelectedItem = getSelection();
			CTabItem tbiLastSelected = (CTabItem) getData("lastSelected");
			//		event.doit
			if (tbiLastSelected != null) {
				Composite objC = (Composite) tbiLastSelected.getControl();
				if (objC != null && objC.isDisposed() != true) {
					for (Control objChildControl : objC.getChildren()) {
					}
				}
			}
			setData("lastSelected", objSelectedItem);
			Composite objComposite = (Composite) objSelectedItem.getData("composite");
			ISOSTabItem objCurrTab = (ISOSTabItem) objComposite;

		}
		catch (Exception e) {
			new ErrorLog("problem", e);
		}
		finally {
		}
	}

	/**
	 * 
	*
	* \brief getTabItem
	*
	* \details
	* 
	* \return SOSCTabItem
	*
	 */
	public SOSCTabItem getTabItem(final Object pobjObject) {
		//		for (SOSCTabItem objTI : objItemList) {
		//			if (objTI.getData().equals(pobjObject)) {
		//				return objTI;
		//			}
		//		}
		return null;
	}

	/**
	 * 
	*
	* \brief getTabItem
	*
	* \details
	* 
	* \return SOSCTabItem
	*
	 */
	public SOSCTabItem getTabItem(final String pstrCaption, final ISOSTabItem pobjTabComposite) {
		SOSCTabItem objTab = getTabItem(pstrCaption);
		objTab.setData("composite", pobjTabComposite);
		return objTab;
	}

	/**
	 * 
	*
	* \brief getTabItem
	*
	* \details
	* 
	* \return SOSCTabItem
	*
	 */
	public SOSCTabItem getTabItem(final String pstrI18NKey) {
		SOSCTabItem objTabItem = null;
		try (SOSCursor objC = new SOSCursor().showWait()) {
			objTabItem = new SOSCTabItem(this, SWT.NONE);
			SOSMsgControl objMsg = MsgHandler.newMsg(pstrI18NKey);

			objMsg.Control(objTabItem);
			objTabItem.setData("key", pstrI18NKey);
			objTabItem.setFont(Globals.stFontRegistry.get("tabitem-text"));
			objTabItem.setImage(SWTResourceManager.getImageFromResource(objMsg.icon()));
			objTabItem.setShowClose(ItemsHasClose);
			objTabItem.setToolTipText(objMsg.tooltip());

			Composite composite = new SOSComposite(this, SWT.None /* SWT.H_SCROLL | SWT.V_SCROLL */);
			objTabItem.setControl(composite);
			objTabItem.setData("composite", composite);
			Gridlayout.set4ColumnLayout(composite);

			//		objItemList.add(objTabItem);

		}
		catch (Exception e) {
			new ErrorLog("problem", e);
		}
		finally {
		}
		return objTabItem;
	}

	/**
	 * 
	*
	* \brief newTabItem
	*
	* \details
	* 
	* \return SOSCTabItem
	*
	 */
	public SOSCTabItem newTabItem(final ISOSControlProperties pobjObject) {
		//		if (objItemList.contains(pobjObject)) {
		//			return getTabItem(pobjObject);
		//		}
		int iSWT = SWT.None;
		if (ItemsHasClose == true) {
			iSWT = SWT.Close;
		}
		SOSCTabItem objTI = new SOSCTabItem(this, iSWT);
		objTI.setData(pobjObject);
		objTI.setShowClose(ItemsHasClose);
		String strT = pobjObject.getName();
		objTI.setData("key", strT);
		objTI.setText(pobjObject.getTitle());
		objTI.setFont(Globals.stFontRegistry.get("text"));
		//		objItemList.add(objTI);
		SOSComposite composite = new SOSComposite(this, SWT.NONE);
		objTI.setControl(composite);
		Gridlayout.set4ColumnLayout(composite);
		return objTI;
	}

	public SOSCTabItem setFocus(final Object pobjObject) {
		SOSCTabItem objTI = getTabItem(pobjObject);
		for (CTabItem objT : this.getItems()) {
			Object objO = objT.getData();
			if (objO != null && objO.equals(pobjObject)) {
				this.setSelection(objT);
				return objTI;
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	private void doHandleEvent(final SelectionEvent arg0) {
		CTabItem objTI = (CTabItem) arg0.item;
		logger.debug(objTI.getText() + " selected");
		Object objCP = objTI.getData();
		if (objCP != null) {
			if (objCP instanceof ISOSControlProperties) {
				ISOSControlProperties objC2 = (ISOSControlProperties) objCP;
				objC2.selectChild();
			}
		}
		handleSelection();
		if ((arg0.stateMask & SWT.CTRL) != 0) {
			maximizeToSashForm();
		}
	}

	private void maximizeToSashForm() {
		Object objO1 = this.getParent();
		if (objO1 instanceof SashForm) {
			SashForm objSash = (SashForm) objO1;
			if (objSash.getMaximizedControl() != null) {
				objSash.setMaximizedControl(null);
			}
			else {
				objSash.setMaximizedControl(this);
			}
		}
	}

	public void showLastTab() {
		SOSCTabItem objSelectedItem = (SOSCTabItem) getData("lastSelected");
		if (objSelectedItem == null) {
			objSelectedItem = (SOSCTabItem) getItem(0);
		}
		Composite objComposite = objSelectedItem.getComposite();
		ISOSTabItem objCurrTab = (ISOSTabItem) objComposite;
		if (objCurrTab != null) {
			Control objC = objSelectedItem.getControl();
			if (objC == null || objC.isDisposed() == true) {
				Composite composite = new Composite(this, SWT.NONE);
				objSelectedItem.setControl(composite);
				Gridlayout.set4ColumnLayout(composite);
				objCurrTab.setParent(composite);
			}
			objCurrTab.createTabItemComposite();
			objComposite.layout(true, true);
			getParent().layout(true);
			layout(true);
			//			redraw();
		}
	}

	/**
	 * 
	*
	* \brief activateTab
	*
	* \details
	* 
	* \return void
	*
	 */
	public void activateTab(final String pstrTabKey) {
		for (CTabItem objTabItem : getItems()) {
			String strT = (String) objTabItem.getData("key");
			if (strT != null && strT.equalsIgnoreCase(pstrTabKey)) {
				ISOSTabItem objCurrTab = (ISOSTabItem) objTabItem.getData("composite");
				if (objCurrTab != null) {
					objCurrTab.createTabItemComposite();
					//					objParent.layout();
				}
				layout(true, true);
				break;
			}
		}
	}

	@Override
	public void setSelection(final int intTabIndex) {
		super.setSelection(intTabIndex);
		layout(true, true);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
