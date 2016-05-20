package com.sos.dialog.classes;

import org.eclipse.swt.SWT;
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

import com.sos.dialog.components.IntegerField;
import com.sos.dialog.components.SOSDateTime;
import com.sos.localization.SOSMsg;

public abstract class SOSMsgControl extends SOSMsg {

    public SOSMsgControl(final String pstrMessageCode) {
        super(pstrMessageCode);
    }

    public abstract SOSMsgControl newMsg(final String pstrMessageCode);

    public Text control(final Text pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        pobjC.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                pobjC.selectAll();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                //
            }
        });
        return pobjC;
    }

    public IntegerField integerField(final IntegerField pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        pobjC.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                pobjC.selectAll();
            }

            @Override
            public void focusLost(final FocusEvent e) {
                //
            }
        });
        return pobjC;
    }

    public Label control(final Label pobjC) {
        pobjC.setText(label());
        pobjC.setToolTipText(this.tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Group control(final Group pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Button control(final Button pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Combo control(final Combo pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Composite control(final Composite pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public CCombo control(final CCombo pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public TableColumn control(final TableColumn pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        return pobjC;
    }

    public TableColumn control(final TableColumn pobjC, final int intDefaultSize) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        return pobjC;
    }

    public Table control(final Table pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        pobjC.setLinesVisible(true);
        pobjC.setHeaderVisible(true);
        return pobjC;
    }

    public SOSTable control(final SOSTable pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public FileDialog control(final FileDialog pobjC) {
        pobjC.setText(caption());
        return pobjC;
    }

    public Spinner control(final Spinner pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public MessageBox control(final MessageBox pobjC) {
        pobjC.setMessage(this.caption());
        return pobjC;
    }

    public List control(final List pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Tree control(final Tree pobjC) {
        pobjC.setToolTipText(this.tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public TreeColumn control(final TreeColumn pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        return pobjC;
    }

    public TabItem control(final TabItem pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(this.tooltip());
        return pobjC;
    }

    public CTabItem control(final CTabItem pobjC) {
        pobjC.setText(label());
        pobjC.setToolTipText(this.tooltip());
        return pobjC;
    }

    public SOSDateTime control(final SOSDateTime pobjC) {
        pobjC.setToolTipText(this.tooltip());
        return pobjC;
    }

    private void setKeyListener(final Control pobjC) {
        pobjC.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(final KeyEvent event) {
                if (event.keyCode == SWT.F1) {
                    openHelp(getF1());
                }
            }

            @Override
            public void keyReleased(final KeyEvent arg0) {
                //
            }
        });
    }

    public void openHelp(final String helpKey) {
        //
    }
    
}