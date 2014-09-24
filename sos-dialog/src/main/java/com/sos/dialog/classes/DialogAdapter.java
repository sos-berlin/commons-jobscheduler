package com.sos.dialog.classes;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
		init(parent);
	}

	public DialogAdapter(final Shell parent, final String pstrPreferenceStoreKey) {
		super(parent, SWT.NONE);
		init(parent);
		objFormHelper.setKey(pstrPreferenceStoreKey);
	}

	private void init(final Shell parent) {
		shell = new Shell(parent, SWT.ON_TOP | SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		objFormHelper = new WindowsSaver(this.getClass(), parent, 643, 600);
		setText("SWT Dialog");

	}
	private Composite	objC	= null;

	public Object open(final ICompositeBaseAbstract objChildComposite) {
		shell.setRedraw(false);
		shell.open();
		set4ColumnLayout(shell);
		;
		if (objC == null) {
			objC = createContents();
		}
		objChildComposite.createComposite(objC);
		set4ColumnLayout((Composite) objChildComposite);
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
			@Override
			public void handleEvent(final Event event) {
				switch (event.detail) {
					case SWT.TRAVERSE_ESCAPE:
						closeShell();
						event.detail = SWT.TRAVERSE_NONE;
						event.doit = false;
						break;
				}
			}
		});
		shell.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(final ControlEvent e) {
				saveWindowPosAndSize();
			}

			@Override
			public void controlResized(final ControlEvent e) {
				saveWindowPosAndSize();
			}
		});
		Composite composite = new Group(shell, SWT.NONE | SWT.RESIZE);
		set4ColumnLayout(composite);

		//		setGridLayout(composite);
		composite.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(final DisposeEvent e) {
				//					if (butApply.isEnabled()) {
				//						save();
				//					}
			}
		});
		composite.layout(true, true);
		Group grp2 = new Group(shell, SWT.None);
		grp2.setLayout(new GridLayout(4, false));
		grp2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		//		set4ColumnLayout(grp2);
		//		grp2.setLayout(new RowLayout());
		Button btnOK = new Button(grp2, SWT.PUSH);
		btnOK.setText("OK");
		shell.setDefaultButton(btnOK);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		btnOK.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, true, 2, 1));

		//		btnOK.setLayoutData(data);
		btnOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				closeShell();
			}
		});

		Button btnCancel = new Button(grp2, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));

		//		btnCancel.setLayoutData(data);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				closeShell();
			}
		});

		//		shell.layout(true, true);
		objC = composite;
		return composite;
	}

	public Shell getShell() {
		return shell;
	}

	private GridLayout	gridLayout		= null;
	private GridData	GridData4Column	= null;

	public void set4ColumnLayout(final Composite objC) {
		objC.setLayout(get4ColumnLayout());
		if (GridData4Column == null) {
			GridData4Column = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		}
		objC.setLayoutData(GridData4Column);
	}

	public GridLayout get4ColumnLayout() {
		if (gridLayout == null) {
			gridLayout = new GridLayout(1, true);
			gridLayout.makeColumnsEqualWidth = true;
			gridLayout.horizontalSpacing = 4;
			gridLayout.verticalSpacing = 5;
			gridLayout.marginBottom = 0;
			gridLayout.marginLeft = 1;
			gridLayout.marginRight = 1;
			gridLayout.marginTop = 2;
			gridLayout.marginHeight = 2;
		}
		return gridLayout;
	}

	private void setGridLayout(final Composite pobjComposite) {
		pobjComposite.setLayout(new GridLayout());
		pobjComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	@Override
	public void doCancel() {
		closeShell();
		objDialogActionHandler.doClose();
	}

	@Override
	public void doEdit() {
		closeShell();
		objDialogActionHandler.doEdit();
	}

	@Override
	public void doNew() {
		closeShell();
		objDialogActionHandler.doNew();
	}

	@Override
	public void doDelete() {
		closeShell();
		objDialogActionHandler.doDelete();
	}

	@Override
	public void doClose() {
		closeShell();
		objDialogActionHandler.doClose();
	}

	private void closeShell() {
		shell.close();
	}

	@Override
	public void setDialogActionHandler(final IDialogActionHandler pobjDialogActionHandler) {
		objDialogActionHandler = pobjDialogActionHandler;
	}

	@Override
	public boolean doValidation() {
		boolean isValid = objDialogActionHandler.doValidation();
		return isValid;
	}
}
