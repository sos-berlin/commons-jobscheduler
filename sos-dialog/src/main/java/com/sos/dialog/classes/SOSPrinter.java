package com.sos.dialog.classes;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class SOSPrinter {

    private Shell shell;
    private Font font;
    private String textToPrint;
    private Printer printer;
    private int rightMargin;
    private int leftMargin;
    private int topMargin;
    private int bottomMargin;
    private GC gc;
    private Font printerFont;
    private int tabWidth;
    private int lineHeight;
    private Color foregroundColor;
    private Color backgroundColor;
    private Color printerForegroundColor;
    private Color printerBackgroundColor;
    private StringBuffer wordBuffer;
    private int index;
    private int end;
    private int x;
    private int y;
    private int orientation = PrinterData.PORTRAIT;

    public SOSPrinter(Shell shell_) {
        this.shell = shell_;
        Display display = shell.getDisplay();

        font = new Font(display, "Courier", 10, SWT.NORMAL);
        foregroundColor = display.getSystemColor(SWT.COLOR_BLACK);
        backgroundColor = display.getSystemColor(SWT.COLOR_WHITE);

    }

    public void print() throws IOException {
        PrintDialog printDialog = new PrintDialog(this.getShell(), SWT.NULL);

        PrinterData data = new PrinterData();
        if (data != null) {
            data.orientation = orientation;
            printDialog.setPrinterData(data);
        }

        printDialog.setText("Print");
        data = printDialog.open();

        if (data == null)
            return;
        if (data.printToFile) {
            data.fileName = "print.out";
        }

        printer = new Printer(data);

        Thread printingThread = new Thread("Printing") {

            public void run() {
                print(printer);
                printer.dispose();
            }
        };
        printingThread.start();

    }

    private void print(Printer printer) {
        if (printer.startJob("Text")) {   // the string is the job name - shows up
                                        // in the printer's job list
            Rectangle clientArea = printer.getClientArea();
            Rectangle trim = printer.computeTrim(0, 0, 0, 0);
            Point dpi = printer.getDPI();
            leftMargin = dpi.x + trim.x; // one inch from left side of paper
            rightMargin = clientArea.width - dpi.x + trim.x + trim.width; // one
                                                                          // inch
                                                                          // from
                                                                          // right
                                                                          // side
                                                                          // of
                                                                          // paper
            topMargin = dpi.y + trim.y; // one inch from top edge of paper
            bottomMargin = clientArea.height - dpi.y + trim.y + trim.height; // one
                                                                             // inch
                                                                             // from
                                                                             // bottom
                                                                             // edge
                                                                             // of
                                                                             // paper

            int tabSize = 4; // is tab width a user setting in your UI?
            StringBuffer tabBuffer = new StringBuffer(tabSize);
            for (int i = 0; i < tabSize; i++)
                tabBuffer.append(' ');
            String tabs = tabBuffer.toString();

            gc = new GC(printer);

            FontData fontData = font.getFontData()[0];
            printerFont = new Font(printer, fontData.getName(), fontData.getHeight(), fontData.getStyle());
            gc.setFont(printerFont);
            tabWidth = gc.stringExtent(tabs).x;
            lineHeight = gc.getFontMetrics().getHeight();
            RGB rgb = foregroundColor.getRGB();
            printerForegroundColor = new Color(printer, rgb);
            gc.setForeground(printerForegroundColor);
            rgb = backgroundColor.getRGB();
            printerBackgroundColor = new Color(printer, rgb);
            gc.setBackground(printerBackgroundColor);

            printText();
            printer.endJob();

            printerFont.dispose();
            printerForegroundColor.dispose();
            printerBackgroundColor.dispose();
            gc.dispose();
        }
    }

    private void printText() {
        printer.startPage();
        wordBuffer = new StringBuffer();
        x = leftMargin;
        y = topMargin;
        index = 0;
        end = textToPrint.length();
        while (index < end) {
            char c = textToPrint.charAt(index);
            index++;
            if (c != 0) {
                if (c == 0x0a || c == 0x0d) {
                    if (c == 0x0d && index < end && textToPrint.charAt(index) == 0x0a) {
                        index++; // if this is cr-lf, skip the lf
                    }
                    printWordBuffer();
                    newline();
                } else {
                    if (c != '\t') {
                        wordBuffer.append(c);
                    }
                    if (Character.isWhitespace(c)) {
                        printWordBuffer();
                        if (c == '\t') {
                            x += tabWidth;
                        }
                    }
                }
            }
        }
        if (y + lineHeight <= bottomMargin) {
            printer.endPage();
        }
    }

    private void printWordBuffer() {
        if (wordBuffer.length() > 0) {
            String word = wordBuffer.toString();
            int wordWidth = gc.stringExtent(word).x;
            if (x + wordWidth > rightMargin) {
                /* word doesn't fit on current line, so wrap */
                newline();
            }
            gc.drawString(word, x, y, false);
            x += wordWidth;
            wordBuffer = new StringBuffer();
        }
    }

    void newline() {
        x = leftMargin;
        y += lineHeight;
        if (y + lineHeight > bottomMargin) {
            printer.endPage();
            if (index + 1 < end) {
                y = topMargin;
                printer.startPage();
            }
        }
    }

    public Shell getShell() {
        return shell;
    }

    public void setText(String text) {
        this.textToPrint = text;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

}
