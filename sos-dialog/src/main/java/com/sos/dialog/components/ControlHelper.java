/**
 *
 */
package com.sos.dialog.components;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.IValueChangedListener;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSValidationError;
import com.sos.dialog.Globals;
import com.sos.dialog.classes.SOSCheckBox;

/** @author KB */
public class ControlHelper implements IValueChangedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControlHelper.class);
    private Control objControl = null;
    private boolean flgIsDirty = false;
    private Label objLabel = null;
    private SOSOptionElement objOptionElement = null;
    public static final String conColor4TEXT = "text";
    public static final String conColor4DirtyField = "dirty-text";
    public static final String conColor4INCLUDED_OPTION = "IncludedOption";
    public static final String conMANDATORY_FIELD_COLOR = "MandatoryFieldColor";
    public static final String conCOLOR4_FIELD_HAS_FOCUS = "Color4FieldHasFocus";
    public static IValueChangedListener objValueChangedListener = null;
    boolean flgControlValueInError = false;

    public ControlHelper(final Label plblLabel, final Control pobjControl, final SOSOptionElement pobjOptionElement) {
        this(plblLabel, pobjControl, pobjOptionElement, 1, 1);
    }

    public ControlHelper(final Label plblLabel, final Control pobjControl, final SOSOptionElement pobjOptionElement, final int horizontalSpan,
            final int verticalSpan) {
        objControl = pobjControl;
        objLabel = plblLabel;
        objOptionElement = pobjOptionElement;
        objOptionElement.addValueChangedListener(this);
        if (objValueChangedListener != null) {
            objOptionElement.addValueChangedListener(objValueChangedListener);
        }
        objControl.setData(objOptionElement);
        objControl.setFont(Globals.stFontRegistry.get(conColor4TEXT));
        objLabel.setBackground(Globals.getCompositeBackground());
        if (objOptionElement.isMandatory()) {
            objLabel.setForeground(Globals.getMandatoryFieldColor());
        } else {
            objLabel.setForeground(null);
        }
        GridData objGD = new GridData(SWT.LEFT, SWT.BOTTOM, false, false, horizontalSpan, verticalSpan);
        objLabel.setLayoutData(objGD);
        objLabel.setFont(Globals.stFontRegistry.get(conColor4TEXT));
        objLabel.setBackground(Globals.getCompositeBackground());
        if (objOptionElement.isProtected()) {
            objControl.setBackground(Globals.getProtectedFieldColor());
        } else {
            objControl.setBackground(Globals.getFieldBackground());
        }
        objControl.setToolTipText(getToolTip());
        if (objControl instanceof Text) {
            Text objText = (Text) objControl;
            objText.setText(objOptionElement.getValue());
            setNoFocusColor(objControl);
            objText.setToolTipText(getToolTip());
            if (objOptionElement.isProtected()) {
                objText.setEditable(false);
            }
            objText.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent arg0) {
                    flgIsDirty = true;
                }
            });
            createControlDecoration(objText, SWT.RIGHT);
            objText.addTraverseListener(changeReturn2Tab());
        }
        if (objControl instanceof Combo) {
            Combo objCombo = (Combo) objControl;
            objCombo.setText(pobjOptionElement.getValue());
            if (pobjOptionElement instanceof SOSOptionStringValueList) {
                SOSOptionStringValueList objValueList = (SOSOptionStringValueList) pobjOptionElement;
                objCombo.setItems(objValueList.getValueList());
            }
            objCombo.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent arg0) {
                    flgIsDirty = true;
                }
            });
            createControlDecoration(objCombo, SWT.RIGHT);
            objCombo.addTraverseListener(changeReturn2Tab());
        }
        if (objControl instanceof CCombo) {
            CCombo objCombo = (CCombo) objControl;
            if (objOptionElement.isProtected()) {
                objCombo.setEditable(false);
                objCombo.setBackground(Globals.getProtectedFieldColor());
            }
            if (pobjOptionElement instanceof SOSOptionStringValueList) {
                SOSOptionStringValueList objValueList = (SOSOptionStringValueList) pobjOptionElement;
                objCombo.setItems(objValueList.getValueList());
            }
            // set this after setting the items of the combobox, otherwise text
            // will be deleted
            objCombo.setText(objOptionElement.getValue());
            objCombo.addModifyListener(new ModifyListener() {

                @Override
                public void modifyText(final ModifyEvent arg0) {
                    flgIsDirty = true;
                }
            });
            createControlDecoration(objCombo, SWT.RIGHT);
            objCombo.addTraverseListener(changeReturn2Tab());
        }
        if (objControl instanceof Button || objControl instanceof SOSCheckBox) {
            objControl.setBackground(Globals.getCompositeBackground());
            if (pobjOptionElement instanceof SOSOptionBoolean) {
                SOSOptionBoolean objBoolean = (SOSOptionBoolean) objOptionElement;
                ((Button) objControl).setSelection(objBoolean.value());
                ((Button) objControl).setText("");
                ((Button) objControl).setAlignment(SWT.BOTTOM);
            }
            if (objControl instanceof SOSCheckBox) {
                objControl.addTraverseListener(changeReturn2Tab());
            }
            createControlDecoration(objControl, SWT.LEFT);
        }
        objControl.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(final DisposeEvent e) {
                removeListeners((Control) e.getSource());
            }
        });
        FocusAdapter objFocusListener = new FocusAdapter() {

            @Override
            public void focusGained(final FocusEvent e) {
                Control objC = (Control) e.getSource();
                setFocusColor(objC);
                Globals.setStatus(objOptionElement.getDescription());
            }

            @Override
            public void focusLost(final FocusEvent e) {
                flgControlValueInError = false;
                final Control objC = (Control) e.getSource();
                LOGGER.debug("focusLost: " + objC.getToolTipText());
                setNoFocusColor(objC);
                Globals.setStatus("");
                if (objC instanceof Text && flgIsDirty) {
                    Text objText = (Text) objC;
                    objOptionElement.setValue(objText.getText());
                }
                if (objC instanceof SOSCheckBox) {
                    SOSCheckBox objCheck = (SOSCheckBox) objC;
                    ((SOSOptionBoolean) objOptionElement).value(objCheck.getSelection());
                }
                if (objC instanceof Combo) {
                    Combo objCheck = (Combo) objC;
                    objOptionElement.setValue(objCheck.getText());
                }
                if (objC instanceof CCombo) {
                    CCombo objCheck = (CCombo) objC;
                    objOptionElement.setValue(objCheck.getText());
                }
                if (flgControlValueInError) {
                    Display.getCurrent().asyncExec(new Runnable() {

                        @Override
                        public void run() {
                            objC.setFocus();
                        }
                    });
                }
            }
        };
        objControl.addFocusListener(objFocusListener);
        flgIsDirty = false;
    }

    private String getToolTip() {
        return objOptionElement.getToolTip();
    }

    private TraverseListener changeReturn2Tab() {
        TraverseListener tl = new TraverseListener() {

            @Override
            public void keyTraversed(final TraverseEvent e) {
                e.doit = true;
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    if ((e.stateMask & SWT.SHIFT) != 0) {
                        e.detail = SWT.TRAVERSE_TAB_PREVIOUS;
                    } else {
                        e.detail = SWT.TRAVERSE_TAB_NEXT;
                    }
                }
            }
        };
        return tl;
    }

    private void removeListeners(final Control pobjControl) {
        SOSOptionElement lobjOptionElement = (SOSOptionElement) pobjControl.getData("option");
        pobjControl.setData("option", null);
        pobjControl.setData(null);
        Listener[] objL = pobjControl.getListeners(SWT.ALL);
        for (Listener listener : objL) {
            pobjControl.removeListener(SWT.ALL, listener);
        }
        if (lobjOptionElement != null) {
            lobjOptionElement.removeValueChangedListener(this);
            LOGGER.debug("listeners disposed: " + lobjOptionElement.getShortKey());
        }
    }

    public void setFocusColor(final Control objControl) {
        if (!(objControl instanceof Button)) {
            if (objControl.isEnabled()) {
                objControl.setBackground(Globals.getFieldHasFocusBackground());
                if (objControl instanceof Text) {
                    ((Text) objControl).setSelection(0);
                }
            } else {
                objControl.setBackground(Globals.getProtectedFieldColor());
            }
        }
    }

    public void setDirtyFont(final Control objControl) {
        objControl.setFont(Globals.stFontRegistry.get(conColor4DirtyField));
    }

    public void setNoFocusColor(final Control objControl) {
        SOSOptionElement objOptionElement1 = (SOSOptionElement) objControl.getData();
        if (objOptionElement1.isProtected() || !objControl.isEnabled()) {
            objControl.setBackground(Globals.getProtectedFieldColor());
        } else {
            objControl.setBackground(Globals.getFieldBackground());
        }
        if ("checkbox".equals(objOptionElement1.getControlType())) {
            objControl.setBackground(Globals.getCompositeBackground());
        }
        if (flgIsDirty) {
            setDirtyFont(objControl);
        }
    }

    @Override
    public void valueHasChanged(final SOSOptionElement pobjOptionElement) {
        if (objControl.isDisposed()) {
            LOGGER.debug("Control is disposed");
            return;
        }
        String strCurrValue = pobjOptionElement.getValue();
        LOGGER.debug("value changed to " + strCurrValue + " (name of control is:  " + objControl.getClass().getName());
        if (objControl instanceof Text) {
            Text objText = (Text) objControl;
            if (!objText.isDisposed()) {
                objText.setText(strCurrValue);
            }
        }
        if (objControl instanceof Button && pobjOptionElement instanceof SOSOptionBoolean) {
            SOSOptionBoolean objBoolean = (SOSOptionBoolean) pobjOptionElement;
            ((Button) objControl).setSelection(objBoolean.value());
            if (objControl instanceof SOSCheckBox) {
                ((SOSCheckBox) objControl).setEnabledDisabled();
            }
        }
        if (objControl instanceof CCombo) {
            CCombo objCombo = (CCombo) objControl;
            objCombo.setText(strCurrValue);
        }
        if (objControl instanceof Combo) {
            Combo objCombo = (Combo) objControl;
            objCombo.setText(strCurrValue);
        }
    }

    @Override
    public void validationError(final SOSValidationError pobjVE) {
        flgControlValueInError = true;
        MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", pobjVE.getErrorMessage());
    }

    private void createControlDecoration(final Control pobjControl, final int intOrientation) {
        Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage();
        ControlDecoration decoration = new ControlDecoration(pobjControl, intOrientation | SWT.TOP);
        decoration.setImage(image);
        decoration.setDescriptionText(getToolTip());
    }

}