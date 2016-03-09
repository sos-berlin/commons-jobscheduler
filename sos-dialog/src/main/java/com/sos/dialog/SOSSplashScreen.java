package com.sos.dialog;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

import com.sos.dialog.swtdesigner.SWTResourceManager;

/** @author KB */
public class SOSSplashScreen {

    private static final Logger LOGGER = Logger.getLogger(SOSSplashScreen.class);

    public SOSSplashScreen() {
        //
    }

    public static void main(String[] args) throws Exception {
        Display display = Display.getDefault();
        final Shell shell = new Shell();
        shell.setText("MainWND");
        Method m = SOSSplashScreen.class.getMethod("openApplicationMainWnd", new Class[] { Shell.class });
        SOSSplashScreen.startDialogExecuteLoop(shell, new Runnable() {

            @Override
            public void run() {
                doSomeTimeconsumingOperation();
            }
        }, new Image(display, SOSSplashScreen.class.getResourceAsStream("/SplashScreenJOE.bmp")), m, 2000);
    }

    public static void startDialogExecuteLoop(final Shell pobjParentShell, final Runnable pobjRunnable, final Image pobjImage2Show, final Method m,
            final int pintHowLong2Show) throws Exception {
        final Shell splashShell = new Shell(pobjParentShell, SWT.NONE);
        final Display display = splashShell.getDisplay();
        if (pobjImage2Show != null) {
            splashShell.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_WAIT));
            splashShell.setLayout(new FillLayout());
            Label label = new Label(splashShell, SWT.BORDER);
            label.setImage(pobjImage2Show);
            splashShell.pack();
            // center the dialog screen to the monitor
            Monitor primary = display.getPrimaryMonitor();
            Rectangle bounds = primary.getBounds();
            Rectangle rect = splashShell.getBounds();
            int x = bounds.x + (bounds.width - rect.width) / 2;
            int y = bounds.y + (bounds.height - rect.height) / 2;
            splashShell.setLocation(x, y);
            splashShell.open();
        }
        display.asyncExec(new Runnable() {

            @Override
            public void run() {
                display.timerExec(pintHowLong2Show, new Runnable() {

                    @Override
                    public void run() {
                        pobjRunnable.run();
                        splashShell.close();
                        splashShell.dispose();
                    }
                });
            }
        });
        while (!splashShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        m.invoke(null, new Object[] { pobjParentShell });
    }

    private static void doSomeTimeconsumingOperation() {
        LOGGER.debug("Executing some important initial environment checks...");
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.debug("finished");
    }

    public static void openApplicationMainWnd(Shell shell) {
        shell.open();
        Display d = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!d.readAndDispatch()) {
                d.sleep();
            }
        }
    }

}
