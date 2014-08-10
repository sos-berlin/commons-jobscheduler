package com.sos.dialog.classes;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import com.sos.dialog.components.SOSDateTime;
import com.sos.dialog.swtdesigner.SWTResourceManager;
import com.sos.localization.SOSMsg;

public abstract class SOSMsgControl extends SOSMsg {
	@SuppressWarnings("unused") private final String		conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final Logger		logger			= Logger.getLogger(this.getClass());

	public SOSMsgControl(final String pstrMessageCode) {
		super(pstrMessageCode);
	}

	public abstract SOSMsgControl newMsg(final String pstrMessageCode);

	public Text Control(final Text pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		// Text kommt im Normalfall nicht aus einer Propertie-Datei. Deswegen ergibt es keinen Sinn
		//	pobjC.setText(label());
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		pobjC.addFocusListener(new FocusAdapter() {
			@Override public void focusGained(final FocusEvent e) {
				pobjC.selectAll();
			}

			@Override public void focusLost(final FocusEvent e) {
			}
		});
		return pobjC;
	} // public Text Control

	public Label Control(final Label pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(label());
		pobjC.setToolTipText(this.tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Label Control

	public Group Control(final Group pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Group Control

	public Button Control(final Button pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Button Control

	public Combo Control(final Combo pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Combo Control

	public Composite Control(final Composite pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Composite Control

	public CCombo Control(final CCombo pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public CCombo Control

	public TableColumn Control(final TableColumn pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		return pobjC;
	} // public TableColumn Control

	public TableColumn Control(final TableColumn pobjC, final int intDefaultSize) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		return pobjC;
	} // public TableColumn Control

	public Table Control(final Table pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		pobjC.setLinesVisible(true);
		pobjC.setHeaderVisible(true);
		return pobjC;
	} // public Table Control

	public SOSTable Control(final SOSTable pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Table Control

	public FileDialog Control(final FileDialog pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		return pobjC;
	} // public FileDialog Control

	public Spinner Control(final Spinner pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Spinner Control

	public MessageBox Control(final MessageBox pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setMessage(this.caption());
		return pobjC;
	} // public MessageBox Control

	public List Control(final List pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public List Control

	public Tree Control(final Tree pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(this.tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Tree Control

	public Browser Control(final Browser pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		setKeyListener(pobjC);
		return pobjC;
	} // public Browser Control

	public TreeColumn Control(final TreeColumn pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(tooltip());
		return pobjC;
	} // public TreeColumn Control

	public TabItem Control(final TabItem pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(caption());
		pobjC.setToolTipText(this.tooltip());
		return pobjC;
	} // public TabItem Control

	public CTabItem Control(final CTabItem pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setText(label());
		pobjC.setImage(SWTResourceManager.getImage(getImageFileName()));
		pobjC.setToolTipText(this.tooltip());
		return pobjC;
	} // public CTabItem Control

	private String getImageFileName () {
		String key = strMessageCode.trim().replaceAll(" ", "");

		String strImageFileName = Messages.getLabel(key + ".icon" /* conPropertyExtensionICon */);
		if (strImageFileName == null || strImageFileName.length() <= 0) {
			strImageFileName = "Blank-20.gif";
		}
		return strImageFileName;
	}
	public SOSDateTime Control(final SOSDateTime pobjC) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::Control";
		pobjC.setToolTipText(this.tooltip());
		return pobjC;
	} // public SOSDateTime Control

	private void setKeyListener(final Control pobjC) {
		//		strControlName = pobjC.
		pobjC.addKeyListener(new KeyListener() {
			@Override public void keyPressed(final KeyEvent event) {
				if (event.keyCode == SWT.F1) {
					// MainWindow.message("F1 gedrückt", SWT.ICON_INFORMATION);
					openHelp(getF1()); // "http:www.sos-berlin.com/doc/en/scheduler.doc/xml/job.xml");
				}
			}

			@Override public void keyReleased(final KeyEvent arg0) {
			}
		});
	} // private void setKeyListener

	public void openHelp(final String helpKey) {
	} // public void openHelp
}
