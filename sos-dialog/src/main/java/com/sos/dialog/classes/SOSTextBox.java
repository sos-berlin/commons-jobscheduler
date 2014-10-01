/**
 *
 */
package com.sos.dialog.classes;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.sos.JSHelper.interfaces.IAutoCompleteProposal;

/**
 * @author KB
 *
 */
public class SOSTextBox extends Text {
	@SuppressWarnings("unused")
	private final String			conClassName	= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String		conSVNVersion	= "$Id: SOSTextBox.java 23878 2014-04-22 18:02:41Z kb $";
	@SuppressWarnings("unused")
	private Logger			logger			= Logger.getLogger(this.getClass());

	private Vector<Control>	objControlList	= new Vector<>();
	private Text objThisText = this;
	
	private  IAutoCompleteProposal objAutoCompleteHandler = null;
	/**
	 *
	 */
	
	public void setAutoCompletehandler (final IAutoCompleteProposal pobjAC) {
		objAutoCompleteHandler = pobjAC;
	}
	
	public SOSTextBox(final Composite parent, final int style) {
		super(parent, style);
		addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				setEnabledDisabled();
			}
		});

		setAutoCompletion(this, null);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent ke) {
				//Method for autocompletion
				setAutoCompletion(objThisText, getText());
			}
		});

	}

	public void addChild(final Control pobjC) {
		objControlList.add(pobjC);
	}

	public void setEnabledDisabled() {
		boolean flgT = true;
//		if (getSelection() == true) {
//		}
//		else {
//			flgT = false;
//		}
		for (Control objC : objControlList) {
			objC.setEnabled(flgT);
		}
	}

	/**
	 * see http://bytes.com/topic/java/insights/879093-auto-completion-swt-text-field
	 */
	private static String	KEY_PRESS	= "Ctrl+Space";

	private void setAutoCompletion(Text text, String value) {
		if (objAutoCompleteHandler == null) {
			return;
		}
		
		try {
			ContentProposalAdapter adapter = null;
			String[] defaultProposals = objAutoCompleteHandler.getAllProposals(value);
			SimpleContentProposalProvider scp = new SimpleContentProposalProvider(defaultProposals);
			scp.setProposals(defaultProposals);
			KeyStroke ks = KeyStroke.getInstance(KEY_PRESS);
			adapter = new ContentProposalAdapter(text, new TextContentAdapter(), scp, ks, null);
			adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private final String[]	defaultProposals	= new String[] { "Assistance 1", "Assistance 2", "Assistance 3", "Assistance 4", "Assistance 5" };
//	private static HashMap <String, String> defaultProposals = new HashMap<>();
//	
//	public void addProposal (final String pstrProposal) {
//		if (pstrProposal != null && pstrProposal.trim().length() > 0) {
//			defaultProposals.put(pstrProposal, pstrProposal);
//		}
//	}
//	
//	private String[] getAllProposals(String text) {
//		String[] proposals = defaultProposals.keySet().toArray(new String[0]);
//		return proposals;
//	}
//
	@Override
	public String getText() {
		String strT = super.getText();
//		addProposal(strT);
		return strT;
	}
	
	@Override
	public void setText (final String pstrValue) {
		super.setText(pstrValue);
//		addProposal(pstrValue);
	}
	
	@Override
	public void dispose() {
		super.dispose();
		logger = null;
		objControlList = null;
		objAutoCompleteHandler = null;
		objThisText = null;
	}
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
