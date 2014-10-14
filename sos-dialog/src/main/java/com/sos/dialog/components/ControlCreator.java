/**
 *
 */
package com.sos.dialog.components;
import static com.sos.dialog.Globals.MsgHandler;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.dialog.Globals;
import com.sos.dialog.classes.SOSCheckBox;
import com.sos.dialog.classes.SOSGroup;
import com.sos.dialog.classes.SOSTextBox;
import com.sos.dialog.layouts.Gridlayout;

/**
 * @author KB
 *
 */
public class ControlCreator {
	@SuppressWarnings("unused")
	private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion		= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger				= Logger.getLogger(this.getClass());
	private Composite			objParentComposite	= null;

	/**
	 *
	 */
	public ControlCreator(final Composite pobjParentComposite) {
		objParentComposite = pobjParentComposite;
	}

	/**
	 * 
	*
	* \brief getGroup
	*
	* \details
	* 
	* \return SOSGroup
	*
	 */
	public SOSGroup getGroup(final String pstrText) {
		SOSGroup group_source = new SOSGroup(objParentComposite, SWT.NONE);
		Gridlayout.set4ColumnGroupLayout(group_source);
		group_source.setLayout(Gridlayout.get4ColumnLayout());
		MsgHandler.newMsg(pstrText).Control(group_source);
		group_source.setBackground(Globals.getCompositeBackground());
		return group_source;
	}

	/**
	 * 
	*
	* \brief getControl
	*
	* \details
	* 
	* \return Control
	*
	 */
	public Control getControl(final SOSOptionElement pobjOption) {
		return getControl(pobjOption, 1);
	}

	public SOSCheckBox getCheckBox(final SOSOptionElement pobjOption) {
		return (SOSCheckBox) getControl(pobjOption, 1);
	}

	public Text getText(final SOSOptionElement pobjOption) {
		return (Text) getControl(pobjOption, 1);
	}

	/**
	 * 
	*
	* \brief getLabel
	*
	* \details
	* 
	* \return Control
	*
	 */
	public Control getLabel() {
		return getLabel(1);
	}

	/**
	 * 
	*
	* \brief getLabel
	*
	* \details
	* 
	* \return Control
	*
	 */
	public Control getLabel(final int intNoOfLabels) {
		Label label = null;
		for (int i = 0; i < intNoOfLabels; i++) {
			label = new Label(objParentComposite, SWT.NONE);
			label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		}
		return label;
	}

	/**
	 * 
	*
	* \brief getSeparator
	*
	* \details
	* 
	* \return Control
	*
	 */
	public Control getSeparator() {
		Label label = new Label(objParentComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		label.setBackground(Globals.getCompositeBackground());
		return label;
	}


	public Control getSeparator(final String pstrI18NKey) {
		CLabel label = new CLabel(objParentComposite, SWT.SHADOW_OUT | SWT.CENTER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 4, 1));
		label.setBackground(Globals.getCompositeBackground());
		label.setText(MsgHandler.newMsg(pstrI18NKey).label());
		label.setToolTipText(MsgHandler.newMsg(pstrI18NKey).tooltip());
		return label;
	}

	/**
	 * 
	*
	* \brief getInvisibleSeparator
	*
	* \details
	* 
	* \return Control
	*
	 */
	public Control getInvisibleSeparator() {
		Control label = getSeparator();
		label.setVisible(false);
		return label;
	}

	/**
	 * 
	*
	* \brief getControl
	*
	* \details
	* 
	* \return Control
	*
	 */
	public Control getControl(final SOSOptionElement pobjOption, final int pintHorizontalSpan) {
		Control objT = null;
		{
			Label lblNewLabel = new Label(objParentComposite, SWT.NONE);
			GridData lblGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			lblNewLabel.setLayoutData(lblGridData);
			MsgHandler.newMsg(pobjOption.getShortKey()).Control(lblNewLabel);
			String strControlType = pobjOption.getControlType();
			//
			if (strControlType.equalsIgnoreCase("text")) {
				SOSTextBox tbxText = new SOSTextBox(objParentComposite, Globals.gTextBoxStyle);
				if (pobjOption.isHideValue() == true) {
					tbxText.setEchoChar('*');
				}
				objT = tbxText;
				tbxText.setAutoCompletehandler(pobjOption);
			}
			if (strControlType.equalsIgnoreCase("combo")) {
				CCombo cbxCCombo1 = new CCombo(objParentComposite, SWT.NONE | Globals.gTextBoxStyle);
				objT = cbxCCombo1;
			}
			if (strControlType.equalsIgnoreCase("checkbox")) {
				SOSCheckBox btnO = new SOSCheckBox(objParentComposite, SWT.CHECK | SWT.FLAT);
				objT = btnO;
			}
			if (strControlType.equalsIgnoreCase("file")) {
				SOSFileNameSelector objFS = new SOSFileNameSelector(objParentComposite, Globals.gTextBoxStyle);
				objFS.setPreferenceStoreKey(pobjOption.getShortKey());
				objT = objFS;
				objFS.setAutoCompletehandler(pobjOption);
			}
			if (strControlType.equalsIgnoreCase("folder")) {
				SOSFileNameSelector objFS = new SOSFileNameSelector(objParentComposite, Globals.gTextBoxStyle);
				objFS.setPreferenceStoreKey(pobjOption.getShortKey());
				objT = objFS;
				objFS.setAutoCompletehandler(pobjOption);
			}
			if (objT != null) {
				objT.setData("option", pobjOption);
			}
			new ControlHelper(lblNewLabel, objT, pobjOption);
			if (objT != null) {
				objT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, pintHorizontalSpan, 1));
			}
		}
		return objT;
	}
}
