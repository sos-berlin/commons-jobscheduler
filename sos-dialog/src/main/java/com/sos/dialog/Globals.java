package com.sos.dialog;
import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.sos.dialog.classes.SOSMsgControl;

public class Globals {
	private final static Logger	logger			= Logger.getLogger(Globals.class);
	public final String			conSVNVersion	= "$Id$";
	//	public static int gTextBoxStyle = SWT.None;
	public static int			gTextBoxStyle	= SWT.BORDER;
	public static int			gButtonStyle	= SWT.BORDER;
	public static FontRegistry	stFontRegistry	= new FontRegistry();
	public static ColorRegistry	stColorRegistry	= new ColorRegistry();
	public static SOSMsgControl	MsgHandler		= null;
	
	public static final String	conColor4TEXT				= "text";
	public static final String	conColor4INCLUDED_OPTION	= "IncludedOption";
	public static final String	conMANDATORY_FIELD_COLOR	= "MandatoryFieldColor";
	public static final String	conCOLOR4_FIELD_HAS_FOCUS	= "Color4FieldHasFocus";


	public Globals() {
	}
	public static ApplicationWindow	Application	= null;

	public static void setApplicationWindow(final ApplicationWindow pobjAW) {
		Application = pobjAW;
	}

	public static boolean	flgIgnoreColors	= true;

	public static void setStatus(final String pobjMessage) {
		Application.setStatus(pobjMessage);
	}

	public static Color getWidgetBackground() {
		return Application.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	public static Color getCompositeBackground() {
		Color objC = null;
		if (flgIgnoreColors == false) {
			objC = getDefaultColor(stColorRegistry.get("CompositeBackGround"));
		}

		return objC;
	}

	private static Color getDefaultColor (final Color pobjC) {
		Color objC = pobjC;
		if (objC == null && Application != null) {
			objC = Application.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		}
		return objC;
	}
	public static Color getFieldBackground() {
		Color objC = null;
		if (flgIgnoreColors == false) {
			objC = getDefaultColor(stColorRegistry.get("FieldBackGround"));
		}
		return objC;
	}

	public static Color getMandatoryFieldColor() {
		Color objC = null;
		if (flgIgnoreColors == false) {
			objC = getDefaultColor(stColorRegistry.get("MandatoryFieldColor"));
		}
		return objC;
	}

	public static Color getProtectedFieldColor() {
		Color objC = null;
		if (flgIgnoreColors == false) {
			objC = getDefaultColor(stColorRegistry.get(conColor4INCLUDED_OPTION));
		}
		return objC;
	}

	public static Color getFieldHasFocusBackground() {
		Color objC = null;
		if (flgIgnoreColors == false) {
			objC = getDefaultColor(stColorRegistry.get(conCOLOR4_FIELD_HAS_FOCUS));
		}
		return objC;
	}

	public static String stateMask(final int stateMask) {
		String string = "[";
		if ((stateMask & SWT.CTRL) != 0)
			string += " CTRL";
		if ((stateMask & SWT.ALT) != 0)
			string += " ALT";
		if ((stateMask & SWT.SHIFT) != 0)
			string += " SHIFT";
		if ((stateMask & SWT.COMMAND) != 0)
			string += " COMMAND";
		if ((stateMask & SWT.BUTTON1) != 0)
			string += " BUTTON1";
		if ((stateMask & SWT.BUTTON2) != 0)
			string += " BUTTON2";
		if ((stateMask & SWT.BUTTON3) != 0)
			string += " BUTTON3";
		if ((stateMask & SWT.BUTTON4) != 0)
			string += " BUTTON4";
		if ((stateMask & SWT.BUTTON5) != 0)
			string += " BUTTON5";
		return string + "], ";
	}
	public static Listener	listener	= new Listener() {
											@Override
											public void handleEvent(final Event e) {
												String string = "Unknown";
												switch (e.type) {
													case SWT.MouseDoubleClick:
														string = "DOUBLE-CLICK";
														break;
													case SWT.MouseDown:
														string = "DOWN";
														break;
													case SWT.MouseMove:
														string = "MOVE";
														break;
													case SWT.MouseUp:
														string = "UP";
														break;
													case SWT.MouseEnter:
														string = "ENTER";
														break;
													case SWT.MouseExit:
														string = "EXIT";
														break;
													case SWT.MouseVerticalWheel:
														string = "WHEEL VERTICAL";
														break;
													case SWT.MouseHorizontalWheel:
														string = "WHEEL HORIZONTAL";
														break;
												}
												string += " - button=" + e.button + ", ";
												string += "stateMask=0x" + Integer.toHexString(e.stateMask) + stateMask(e.stateMask);
												string += "location=(" + e.x + ", " + e.y + "), ";
												if (e.type == SWT.MouseVerticalWheel) {
													string += "detail=";
													string += e.detail == SWT.SCROLL_PAGE ? "SCROLL_PAGE, " : "SCROLL_LINE, ";
												}
												string += "count=" + e.count + ", widget=" + e.widget;
												logger.trace(string);
											}
										};
}
