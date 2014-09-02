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

import com.sos.dialog.Globals;
import com.sos.dialog.interfaces.ISOSControlProperties;
import com.sos.dialog.interfaces.ISOSTabItem;
import com.sos.dialog.layouts.Gridlayout;

/**
 * @author KB
 *
 */
public class SOSCTabFolder extends CTabFolder {
	@SuppressWarnings("unused")
	private final String				conClassName				= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String			conSVNVersion				= "$Id$";
	@SuppressWarnings("unused")
	private final Logger				logger						= Logger.getLogger(this.getClass());
//	private final Vector<SOSCTabItem>	objItemList					= new Vector<SOSCTabItem>();
	public boolean				ItemsHasClose				= true;
	public boolean						gflgCreateControlsImmediate	= true;
	public boolean flgRejectTabItemSelection = false;
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
		this.setSelectionBackground(new Color[]{Globals.getFieldHasFocusBackground(), Globals.getCompositeBackground(), Globals.getCompositeBackground()}, new int[]{50, 100}, true);
		Gridlayout.set4ColumnLayout(this);
		//
		addFocusListener(new FocusListener() {
			@Override public void focusLost(final FocusEvent e) {
				// TODO validate?
			}

			@SuppressWarnings("unused") @Override public void focusGained(final FocusEvent e) {
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
			@Override public void widgetSelected(final SelectionEvent event) {
				logger.debug("CTabFolder Item selected");
				if (flgRejectTabItemSelection == true) {
					return;
				}
				CTabItem objSelectedItem = getSelection();
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
						//						if (gflgCreateControlsImmediate == true) {
						//							for (Control objChildControl : objC.getChildren()) {
						//								if (objChildControl instanceof ISOSTabItem) {
						//									objChildControl.dispose();
						//									objChildControl = null;
						//									Composite objCC = (Composite) objChildControl;
						//									for (Control objC1 : objCC.getChildren()) {
						//										logger.trace("dispose " + objC1.getClass().getName());
						//										objC1.dispose();
						//										objC1 = null;
						//										//								}
						//									}
						//								}
						//								objC.dispose();
						//								logger.debug("disposed? " + objC.isDisposed());
						//							}
						//						}
					}
				}
				setData("lastSelected", objSelectedItem);
				//				Composite objComposite = (Composite) objSelectedItem.getData("composite");
				//				if (objComposite.isDisposed() == true) {
				//					objComposite.dispose();
				//				}
				//				ISOSTabItem objCurrTab = (ISOSTabItem) objComposite;
				//				Control objC = objSelectedItem.getControl();
				//				if (objCurrTab != null) {
				//					if (objC == null || objC.isDisposed() == true) {
				//						Composite composite = new Composite(objTabF, SWT.NONE);
				//						objSelectedItem.setControl(composite);
				//						Gridlayout.set4ColumnLayout(composite);
				//						objCurrTab.setParent(composite);
				//					}
				//					//					Control[] objCA = objC.getChildren();
				//					//					if (objCA.length <= 1) {
				//					objCurrTab.createTabItemComposite();
				//					//					}
				//					objComposite.layout(true);
				//					getParent().layout(true);
				//					layout(true);
				//					//					redraw();
				//				}
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
			@Override public void handleEvent(final Event event) {
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
			@Override public void handleEvent(final Event event) {
				// the widget here is the menueItem
//								event.widget.dispose();
			}
		});
		MenuItem closeOthers = new MenuItem(contextMenu, SWT.NONE);
		closeOthers.setText("Close Others");
		closeOthers.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(final Event event) {
			}
		});
		MenuItem closeAll = new MenuItem(contextMenu, SWT.NONE);
		closeAll.setText("Close All");
		closeAll.addListener(SWT.Selection, new Listener() {
			@Override public void handleEvent(final Event event) {
			}
		});
		this.setMenu(contextMenu);
		this.addSelectionListener(new SelectionListener() {
			boolean	flgIsActive	= false;

			@Override public void widgetDefaultSelected(final SelectionEvent arg0) {
				widgetSelected(arg0);
			}

			@Override public void widgetSelected(final SelectionEvent arg0) {
				logger.debug("widgetSelected");
				//				doHandleEvent(arg0);
				arg0.doit = true;
			}
		});
		this.addCTabFolder2Listener(new CTabFolder2Adapter() {
			 public void itemClosed (final CTabFolderEvent event) {
				
			}
			@Override public void close(final CTabFolderEvent event) {
				CTabItem objI = (CTabItem) event.item;
				if (objI != null) {
//					for (SOSCTabItem objTI : objItemList) {
//						if (objTI.equals(objI)) {
//							objItemList.remove(objI);
							CTabFolder objC = objI.getParent();
							
							objI.dispose();
							event.doit = true;
							logger.debug("tabitem disposed");
							objC.layout(true, true);
//							break;
//						}
//					}
//					if (objI.getControl() != null) {
//						objI.getControl().dispose();
//					}
				}
			}  // close

			@Override public void minimize(final CTabFolderEvent event) {
			}

			@Override public void maximize(final CTabFolderEvent event) {
			}

			@Override public void restore(final CTabFolderEvent event) {
			}

			@Override public void showList(final CTabFolderEvent event) {
			}
		});
	}

	private void handleSelection() {
		logger.debug("handleSelection");
		CTabItem objSelectedItem = getSelection();
		CTabItem tbiLastSelected = (CTabItem) getData("lastSelected");
		//		event.doit
		if (tbiLastSelected != null) {
			Composite objC = (Composite) tbiLastSelected.getControl();
			if (objC != null && objC.isDisposed() != true) {
				for (Control objChildControl : objC.getChildren()) {
					//					if (objChildControl instanceof ISOSTabItem ) {
					//					Control objCC = objChildControl;
					//					objCC.dispose();
					//						for (Control objC1 : objCC.getChildren()) {
					//							logger.debug("dispose " + objC1.getClass().getName());
					//							objC1.dispose();
					//							objC1 = null;
					//						}
					//						if (objCC instanceof ISOSTabItem) {
					//							objCC.dispose();
					//							objCC = null;
					//						}
					//					}
				}
			}
		}
		setData("lastSelected", objSelectedItem);
		Composite objComposite = (Composite) objSelectedItem.getData("composite");
		ISOSTabItem objCurrTab = (ISOSTabItem) objComposite;
		//		if (objCurrTab != null) {
		//			Control objC = objSelectedItem.getControl();
		//			if (objC == null) {
		//				Composite composite = new Composite(this, SWT.NONE);
		//				objSelectedItem.setControl(composite);
		//				Gridlayout.set4ColumnLayout(composite);
		//				objCurrTab.setParent(composite);
		//			}
		//			objCurrTab.createTabItemComposite();
		//			objComposite.layout(true);
		//			Composite objCS = (Composite) objC;
		//			objCS.layout(true);
		//			//			objComposite.redraw();
		//			//			objComposite.pack();
		//			getParent().layout(true);
		//			layout(true);
		//			//			getParent().redraw();
		//		}
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
	public SOSCTabItem getTabItem(final String pstrCaption) {
		SOSCTabItem objTabItem = new SOSCTabItem(this, SWT.NONE);
		MsgHandler.newMsg(pstrCaption).Control(objTabItem);
//		objTabItem.setData("key", pstrCaption);
		objTabItem.setFont(Globals.stFontRegistry.get("tabitem-text"));

		objTabItem.setShowClose(ItemsHasClose);
		Composite composite = new SOSComposite(this, SWT.NONE);
		objTabItem.setControl(composite);
//		objTabItem.setData("composite", composite);
		Gridlayout.set4ColumnLayout(composite);
		
//		objItemList.add(objTabItem);

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

	@SuppressWarnings("unused") private void doHandleEvent(final SelectionEvent arg0) {
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

	@Override public void setSelection(final int intTabIndex) {
		super.setSelection(intTabIndex);
		layout(true,true);
	}

	@Override protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
