/**
 *
 */
package com.sos.DataExchange;
import com.sos.DataExchange.Options.JADEOptions;
import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

/**
 * @author KB
 *
 */
public class JadeBaseEngine extends JSJobUtilitiesClass<JADEOptions> {
	/**
	 *
	 */
	@SuppressWarnings("unused") private final String	conSVNVersion	= "$Id$";

	public JadeBaseEngine() {
	}

	public JadeBaseEngine(final JADEOptions pobjOptions) {
		super(pobjOptions);
	}
}
