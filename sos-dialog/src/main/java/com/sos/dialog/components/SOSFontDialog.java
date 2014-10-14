/*
 * Created on 03.09.2010
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.sos.dialog.components;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.sos.dialog.swtdesigner.SWTResourceManager;

public class SOSFontDialog extends SOSPreferenceStore {

	@SuppressWarnings("unused")
	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused")
	private final Logger logger = Logger.getLogger(this.getClass());

	private static final String	conDefaultfontName			= "Courier New";
	private static final String	conSCRIPT_EDITOR_FONT_COLOR	= "script_editor_font_color";
	private static final String	conSCRIPT_EDITOR_FONT		= "script_editor_font";
	private FontData			fontData;
	private RGB					foreGround;
	private Shell				objParentShell				= null;
	private final  Class <?> objClass = this.getClass();

	public SOSFontDialog(final Shell pobjParentShell, final String pstrPreferenceStoreKey) {
		super();
		objParentShell = pobjParentShell;
		setKey(pstrPreferenceStoreKey);
	}

	public SOSFontDialog() {
		super();
		setKey("SOSFontDialog");
	}

	public void setParent(final Shell pobjParentShell) {
		objParentShell = pobjParentShell;
	}

	public SOSFontDialog(final String fontName_, final int fontSize_, final int fontStyle_, final RGB foreGround_) {
		this();
		fontData = new FontData(fontName_, fontSize_, fontStyle_);
		foreGround = foreGround_;
	}

	public SOSFontDialog(final String fontName_, final int fontSize_, final int fontType_) {
		this();
		fontData = new FontData(fontName_, fontSize_, SWT.NORMAL);
		foreGround = new RGB(0, 0, 0);
	}

	public SOSFontDialog(final FontData fontData_, final RGB foreGround_) {
		this();
		foreGround = foreGround_;
		fontData = fontData_;
	}

	public SOSFontDialog(final FontData fontData_) {
		this();
		foreGround = new RGB(0, 0, 0);
		fontData = fontData_;
	}

	public SOSFontDialog(final String fontData_) {
		this();
		foreGround = new RGB(0, 0, 0);
		fontData = new FontData(fontData_);
	}

	public void readFontData() {
		try {
		String s = getProperty(conSCRIPT_EDITOR_FONT);
		if (s == null) {
			fontData = new FontData(conDefaultfontName, 10, SWT.NORMAL);
		}
		else {
			fontData = new FontData(s);
		}

		s = getProperty(conSCRIPT_EDITOR_FONT_COLOR);
		if (s == null) {
			s = "";
		}
		s = s.replaceAll("RGB.*\\{(.*)\\}", "$1");
		String[] colours = Pattern.compile(",").split(s);
			int r = Integer.parseInt(colours[0].trim());
			int g = Integer.parseInt(colours[1].trim());
			int b = Integer.parseInt(colours[2].trim());
			foreGround = new RGB(r, g, b);
		}
		catch (Exception e) {
//			System.out.println("Wrong colour in Profile");
//			System.out.println(SOSJOEMessageCodes.JOE_M_WrongColour.label());
			foreGround = new RGB(0, 0, 0);
		}

	}

	private void saveFontData(final FontData f, final RGB foreGround) {
		saveProperty(conSCRIPT_EDITOR_FONT, f.toString());
		saveProperty(conSCRIPT_EDITOR_FONT_COLOR, foreGround.toString());
		fontData = f;
		this.foreGround = foreGround;
	}

	public void show() {
		final Display d = new Display();
		show(d);
	}

	public FontData getFontData() {
		return fontData;
	}

	public RGB getForeGround() {
		return foreGround;
	}

	public void show(final Display pobjDisplay) {

		final Display d = pobjDisplay;
		// final Shell s1 = new Shell(d);
		final Shell objCurrentShell = new Shell(pobjDisplay.getCurrent().getActiveShell(), SWT.CLOSE | SWT.TITLE | SWT.APPLICATION_MODAL | SWT.BORDER);
		final RGB aktForeGround = foreGround;

		objCurrentShell.setSize(302, 160);
//		s.setText(SOSJOEMessageCodes.JOE_M_FontDialog.label());
		objCurrentShell.setText("FontDialog: select or change font");
		objCurrentShell.setLayout(new GridLayout(11, false));

		new Label(objCurrentShell, SWT.NONE);
		final Text t = new Text(objCurrentShell, SWT.BORDER | SWT.WRAP | SWT.MULTI);

//		t.setText(SOSJOEMessageCodes.JOE_M_Blindtext.label());  // The quick brown fox jumps over the lazy poddle.
		t.setText("The quick brown fox jumps over the lazy poddle.");
		t.setFont(SWTResourceManager.getFont(conDefaultfontName, 10, SWT.NORMAL));
		t.setForeground(new Color(d, foreGround));

		t.setFont(new Font(d, fontData));
		t.setForeground(new Color(d, foreGround));

		GridData gd_t = new GridData(SWT.FILL, SWT.FILL, false, false, 10, 1);
		gd_t.heightHint = 74;
		t.setLayoutData(gd_t);
		new Label(objCurrentShell, SWT.NONE);
//		final Button btnChange = SOSJOEMessageCodes.JOE_B_FontDialog_Change.Control(new Button(s, SWT.PUSH | SWT.BORDER));
		final Button btnChange = new Button(objCurrentShell, SWT.PUSH | SWT.BORDER);
		btnChange.setText("Change");
		btnChange.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				FontDialog fd = new FontDialog(objCurrentShell, SWT.NONE);
//				fd.setText(SOSJOEMessageCodes.JOE_M_SelectFont.label());
				fd.setRGB(t.getForeground().getRGB());
				fd.setFontList(t.getFont().getFontData());

				FontData newFont = fd.open();
				if (newFont == null)
					return;
				t.setFont(new Font(d, newFont));
				t.setForeground(new Color(d, fd.getRGB()));
			}
		});

//		Button btnSave = SOSJOEMessageCodes.JOE_B_FontDialog_Save.Control(new Button(s, SWT.NONE));
		Button btnSave = new Button(objCurrentShell, SWT.NONE);
		btnSave.setText("Save");
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				saveFontData(t.getFont().getFontData()[0], t.getForeground().getRGB());
				objCurrentShell.dispose();
			}
		});

//		Button btnReset = SOSJOEMessageCodes.JOE_B_FontDialog_Reset.Control(new Button(s, SWT.NONE));
		Button btnReset = new Button(objCurrentShell, SWT.NONE);
		btnReset.setText("Reset");
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				t.setFont(new Font(d, fontData));
				t.setForeground(new Color(d, aktForeGround));
			}
		});

//		Button btnCancel = SOSJOEMessageCodes.JOE_B_FontDialog_Cancel.Control(new Button(s, SWT.NONE));
		Button btnCancel = new Button(objCurrentShell, SWT.NONE);
		btnCancel.setText("Cancel");
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				objCurrentShell.dispose();
			}
		});

		new Label(objCurrentShell, SWT.NONE);
		objCurrentShell.open();

		while (!objCurrentShell.isDisposed()) {
			if (!d.readAndDispatch())
				d.sleep();
		}

	}
}