/**
 *
 */
package com.sos.dialog.layouts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * @author KB
 *
 */
public class Gridlayout {
	private static GridLayout	gridLayout	= null;
	private static GridData GridData4Column = null;

	public Gridlayout() {
	}

	public static void set4ColumnGroupLayout(final Composite objC) {
		objC.setLayout(get4ColumnLayout());
		objC.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
	}

	public static void set4ColumnLayout(final Composite objC) {
		objC.setLayout(get4ColumnLayout());
		if (GridData4Column == null) {
			GridData4Column = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		}
		objC.setLayoutData(GridData4Column);
	}

	public static GridLayout get4ColumnLayout() {
		if (gridLayout == null) {
			gridLayout = new GridLayout(4, true);
			gridLayout.makeColumnsEqualWidth = false;
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
}
