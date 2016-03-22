package com.sos.dialog;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.sos.dialog.classes.SOSMsgControl;

public class Globals {

    private final static Logger LOGGER = Logger.getLogger(Globals.class);
    public static int gTextBoxStyle = SWT.None;
    public static int gButtonStyle = SWT.BORDER;
    public static FontRegistry stFontRegistry = new FontRegistry();
    public static ColorRegistry stColorRegistry = new ColorRegistry();
    public static SOSMsgControl MsgHandler = null;
    public static String gstrApplication = "";
    public static final String conColor4TEXT = "text";
    public static final String conColor4INCLUDED_OPTION = "IncludedOption";
    public static final String conMANDATORY_FIELD_COLOR = "MandatoryFieldColor";
    public static final String conCOLOR4_FIELD_HAS_FOCUS = "Color4FieldHasFocus";
    public static boolean flgIgnoreColors = false;

    public Globals() {
    }

    public static ApplicationWindow Application = null;

    public static void setApplicationWindow(final ApplicationWindow pobjAW) {
        Application = pobjAW;
        initColorAndFontRegistry();
    }

    public static void initColorAndFontRegistry() {
        try {
            Globals.stFontRegistry.put("button-text", new FontData[] { new FontData("Arial", 11, SWT.BOLD) });
            Globals.stFontRegistry.put("code", new FontData[] { new FontData("Courier New", 11, SWT.NORMAL) });
            Globals.stFontRegistry.put("text", new FontData[] { new FontData("Arial", 12, SWT.NORMAL) });
            Globals.stFontRegistry.put("tabitem-text", new FontData[] { new FontData("Arial", 11, SWT.NORMAL) });
            Globals.stFontRegistry.put("dirty-text", new FontData[] { new FontData("Arial", 11, SWT.BOLD | SWT.ITALIC) });
            Globals.stColorRegistry.put("IncludedOption", getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW).getRGB());
            Globals.stColorRegistry.put("MandatoryFieldColor", getSystemColor(SWT.COLOR_BLUE).getRGB());
            Globals.stColorRegistry.put("Color4FieldHasFocus", getSystemColor(SWT.COLOR_GREEN).getRGB());
            // Colorschema
            Globals.stColorRegistry.put("FieldBackGround", new RGB(245, 250, 210));
            Globals.stColorRegistry.put("DisabledFieldBackGround", new RGB(232, 232, 227));
            Globals.stColorRegistry.put("Color4FieldHasFocus", new RGB(124, 231, 0));
            Globals.stColorRegistry.put("Color4FieldInError", new RGB(255, 225, 0));
            Globals.stColorRegistry.put("CompositeBackGround", new RGB(245, 255, 255));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void setStatus(final String pobjMessage) {
        Application.setStatus(pobjMessage);
    }

    public static Color getWidgetBackground() {
        return Application.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    }

    public static Color getCompositeBackground() {
        Color objC = null;
        if (!flgIgnoreColors) {
            objC = getDefaultColor(stColorRegistry.get("CompositeBackGround"));
        }
        return objC;
    }

    public static Color getSystemColor(final int intColorCode) {
        return Application.getShell().getDisplay().getSystemColor(intColorCode);
    }

    private static Color getDefaultColor(final Color pobjC) {
        Color objC = pobjC;
        if (objC == null && Application != null) {
            objC = Application.getShell().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        }
        return objC;
    }

    public static Color getFieldBackground() {
        Color objC = null;
        if (!flgIgnoreColors) {
            objC = getDefaultColor(stColorRegistry.get("FieldBackGround"));
        }
        return objC;
    }

    public static Color getFieldBackground4Disabled() {
        Color objC = null;
        if (!flgIgnoreColors) {
            objC = getDefaultColor(stColorRegistry.get("DisabledFieldBackGround"));
        }
        return objC;
    }

    public static Color getMandatoryFieldColor() {
        Color objC = null;
        if (!flgIgnoreColors) {
            objC = getDefaultColor(stColorRegistry.get("MandatoryFieldColor"));
        }
        return objC;
    }

    public static Color getProtectedFieldColor() {
        Color objC = null;
        if (!flgIgnoreColors) {
            objC = getDefaultColor(stColorRegistry.get(conColor4INCLUDED_OPTION));
        }
        return objC;
    }

    public static Color getFieldHasFocusBackground() {
        Color objC = null;
        if (!flgIgnoreColors) {
            objC = getDefaultColor(stColorRegistry.get(conCOLOR4_FIELD_HAS_FOCUS));
        }
        return objC;
    }

    public static String stateMask(final int stateMask) {
        String string = "[";
        if ((stateMask & SWT.CTRL) != 0) {
            string += " CTRL";
        }
        if ((stateMask & SWT.ALT) != 0) {
            string += " ALT";
        }
        if ((stateMask & SWT.SHIFT) != 0) {
            string += " SHIFT";
        }
        if ((stateMask & SWT.COMMAND) != 0) {
            string += " COMMAND";
        }
        if ((stateMask & SWT.BUTTON1) != 0) {
            string += " BUTTON1";
        }
        if ((stateMask & SWT.BUTTON2) != 0) {
            string += " BUTTON2";
        }
        if ((stateMask & SWT.BUTTON3) != 0) {
            string += " BUTTON3";
        }
        if ((stateMask & SWT.BUTTON4) != 0) {
            string += " BUTTON4";
        }
        if ((stateMask & SWT.BUTTON5) != 0) {
            string += " BUTTON5";
        }
        return string + "], ";
    }

    public static Listener listener = new Listener() {

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
            LOGGER.trace(string);
        }
    };

    public static Shell globalShell() {
        return Display.getCurrent().getActiveShell();
    }

    public static void redraw(final boolean flgDoRedraw) {
        if (flgDoRedraw) {
            globalShell().layout(true, true);
            globalShell().setRedraw(true);
        } else {
            globalShell().setRedraw(false);
        }
    }

}
