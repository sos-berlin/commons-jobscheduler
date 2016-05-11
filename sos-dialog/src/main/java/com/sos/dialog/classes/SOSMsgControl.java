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

    public Text Control(final Text pobjC) {
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

    public Label Control(final Label pobjC) {
        pobjC.setText(label());
        pobjC.setToolTipText(this.tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Group Control(final Group pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Button Control(final Button pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Combo Control(final Combo pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Composite Control(final Composite pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public CCombo Control(final CCombo pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public TableColumn Control(final TableColumn pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        return pobjC;
    }

    public TableColumn Control(final TableColumn pobjC, final int intDefaultSize) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        return pobjC;
    }

    public Table Control(final Table pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        pobjC.setLinesVisible(true);
        pobjC.setHeaderVisible(true);
        return pobjC;
    }

    public SOSTable Control(final SOSTable pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public FileDialog Control(final FileDialog pobjC) {
        pobjC.setText(caption());
        return pobjC;
    }

    public Spinner Control(final Spinner pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public MessageBox Control(final MessageBox pobjC) {
        pobjC.setMessage(this.caption());
        return pobjC;
    }

    public List Control(final List pobjC) {
        pobjC.setToolTipText(tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public Tree Control(final Tree pobjC) {
        pobjC.setToolTipText(this.tooltip());
        setKeyListener(pobjC);
        return pobjC;
    }

    public TreeColumn Control(final TreeColumn pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(tooltip());
        return pobjC;
    }

    public TabItem Control(final TabItem pobjC) {
        pobjC.setText(caption());
        pobjC.setToolTipText(this.tooltip());
        return pobjC;
    }

    public CTabItem Control(final CTabItem pobjC) {
        pobjC.setText(label());
        pobjC.setToolTipText(this.tooltip());
        return pobjC;
    }

    public SOSDateTime Control(final SOSDateTime pobjC) {
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