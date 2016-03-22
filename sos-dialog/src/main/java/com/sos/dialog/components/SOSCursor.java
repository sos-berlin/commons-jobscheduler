/**
 * 
 */
package com.sos.dialog.components;

import static com.sos.dialog.swtdesigner.SWTResourceManager.getCursor;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/** @author KB */
public class SOSCursor implements AutoCloseable {

    @SuppressWarnings("unused")
    private final String conClassName = this.getClass().getSimpleName();
    @SuppressWarnings("unused")
    private static final String conSVNVersion = "$Id$";
    @SuppressWarnings("unused")
    private final Logger logger = Logger.getLogger(this.getClass());

    private final Stack<Cursor> objCursorStack = new Stack<>();
    private Shell shell = null;

    /**
	 * 
	 */
    public SOSCursor() {
        super();
        shell = getShell();
    }

    public SOSCursor(final int pintCursor) {
        this();
        showWait();

    }

    public SOSCursor showWait() {
        setCursor(SWT.CURSOR_WAIT);
        return this;
    }

    private void setCursor(final int pintCursor) {
        if (shell.isDisposed() == false) {
            objCursorStack.push(shell.getCursor());
            shell.setCursor(getCursor(pintCursor));
            logger.trace("set cursor to " + pintCursor);
        }

    }

    @Override
    // autoclose, implicitely used e.g. with try-with-resources statement
    public void close() throws Exception {
        try {
            if (shell.isDisposed() == false) {
                shell.setCursor(objCursorStack.pop());
                logger.trace("close SOSCursor. reestablish the cursor");
            }
        } catch (Exception e) {
            throw new JobSchedulerException(e);
        }
    }

    private Shell getShell() {
        return getActiveShell();
    }

    public static Shell getActiveShell() {
        Display display = Display.getDefault();
        Shell result = display.getActiveShell();

        if (result == null) {
            Shell[] shells = display.getShells();
            for (Shell shell : shells) {
                if (shell.getShells().length == 0) {
                    if (result != null) {
                        throw new AssertionError();
                    }
                    result = shell;
                }
            }
        }

        return result;
    }
}
