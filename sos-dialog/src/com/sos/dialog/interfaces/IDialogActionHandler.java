/**
 *
 */
package com.sos.dialog.interfaces;

/**
 * @author KB
 *
 */
public interface IDialogActionHandler {

	public void doCancel ();

	public void doEdit ();

	public void doNew ();

	public void doDelete ();

	public void doClose ();

	public void setDialogActionHandler (final IDialogActionHandler pobjDialogActionHandler);
}
