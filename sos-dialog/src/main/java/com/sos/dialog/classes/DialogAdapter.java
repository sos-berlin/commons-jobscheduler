package com.sos.dialog.classes;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.sos.dialog.interfaces.ICompositeBaseAbstract;
import com.sos.dialog.interfaces.IDialogActionHandler;

/**
 *
 * \Example:
 *
 * @author KB
 *
 */
public class DialogAdapter extends Dialog implements IDialogActionHandler {
	@SuppressWarnings("unused")
	private final String			conClassName			= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String		conSVNVersion			= "$Id$";
	@SuppressWarnings("unused")
	private final Logger			logger					= Logger.getLogger(this.getClass());
	protected Object				result;
	protected Shell					shell;
	private WindowsSaver			objFormHelper;
	private IDialogActionHandler	objDialogActionHandler	= this;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public DialogAdapter(final Shell parent, final int style) {
		super(parent, style);
		shell = new Shell(parent, SWT.ON_TOP | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		objFormHelper = new WindowsSaver(this.getClass(), parent, 643, 600);
		setText("SWT Dialog");
	}

	public Object open(final ICompositeBaseAbstract objChildComposite) {
		shell.setRedraw(false);
		shell.open();
		shell.setLayout(new FillLayout());
		Composite objC = createContents();
		objChildComposite.createComposite(objC);
		((Group) objC).setText(objChildComposite.getWindowTitle());
		shell.setText(objChildComposite.getWindowTitle());
		objC.layout(true, true);
		shell.layout(true, true);
		shell.setRedraw(true);
		//		shell.forceActive();
		//		Object objO = open();
		Object objO = null;
		return objO;
	}

	public Object open(final IDialogActionHandler pobjDialogActionHandler) {
		objDialogActionHandler = pobjDialogActionHandler;
		objDialogActionHandler.setDialogActionHandler(this);
		return open();
	}

	private void saveWindowPosAndSize() {
		objFormHelper.saveWindow();
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	public Composite createContents() {
		objFormHelper = new WindowsSaver(this.getClass(), shell, 400, 200);
		objFormHelper.restoreWindow();
		shell.addListener(SWT.Traverse, new Listener() {
			@Override public void handleEvent(final Event event) {
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						shell.close();
						event.detail = SWT.TRAVERSE_NONE;
						event.doit = false;
						break;
				}
			}
		});
		shell.addControlListener(new ControlAdapter() {
			@Override public void controlMoved(final ControlEvent e) {
				saveWindowPosAndSize();
			}

			@Override public void controlResized(final ControlEvent e) {
				saveWindowPosAndSize();
			}
		});
		Composite composite = new Group(shell, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.addDisposeListener(new DisposeListener() {
			@Override public void widgetDisposed(final DisposeEvent e) {
				//					if (butApply.isEnabled()) {
				//						save();
				//					}
			}
		});
		composite.layout(true, true);
		shell.layout(true, true);
		return composite;
	}

	public Shell getShell() {
		return shell;
	}

	@Override public void doCancel() {
		closeShell();
		objDialogActionHandler.doClose();
	}

	@Override public void doEdit() {
		closeShell();
		objDialogActionHandler.doEdit();
	}

	@Override public void doNew() {
		closeShell();
		objDialogActionHandler.doNew();
	}

	@Override public void doDelete() {
		closeShell();
		objDialogActionHandler.doDelete();
	}

	@Override public void doClose() {
		closeShell();
		objDialogActionHandler.doClose();
	}

	private void closeShell() {
		shell.close();
	}

	@Override public void setDialogActionHandler(final IDialogActionHandler pobjDialogActionHandler) {
		objDialogActionHandler = pobjDialogActionHandler;
	}
}
