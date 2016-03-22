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

/** @author KB */
public class SOSTextBox extends Text {

    private static final Logger LOGGER = Logger.getLogger(SOSTextBox.class);
    private static final String KEY_PRESS = "Ctrl+Space";
    private Vector<Control> objControlList = new Vector<>();
    private Text objThisText = this;
    private IAutoCompleteProposal objAutoCompleteHandler = null;

    public void setAutoCompletehandler(final IAutoCompleteProposal pobjAC) {
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
                setAutoCompletion(objThisText, getText());
            }
        });
    }

    public void addChild(final Control pobjC) {
        objControlList.add(pobjC);
    }

    public void setEnabledDisabled() {
        boolean flgT = true;
        for (Control objC : objControlList) {
            objC.setEnabled(flgT);
        }
    }

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
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public String getText() {
        String strT = super.getText();
        return strT;
    }

    @Override
    public void setText(final String pstrValue) {
        super.setText(pstrValue);
    }

    @Override
    public void dispose() {
        super.dispose();
        objControlList = null;
        objAutoCompleteHandler = null;
        objThisText = null;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
