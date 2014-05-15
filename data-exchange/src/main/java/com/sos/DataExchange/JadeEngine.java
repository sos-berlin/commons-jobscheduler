/**
 * 
 */
package com.sos.DataExchange;
import java.util.HashMap;
import java.util.Properties;

import com.sos.DataExchange.Options.JADEOptions;


/**
 * @author KB
 *
 */
public class JadeEngine extends SOSDataExchangeEngine {
	@SuppressWarnings({ "unused", "hiding" }) private final String	conSVNVersion	= "$Id$";
	@SuppressWarnings("unused") private final String				conClassName	= this.getClass().getName();

	/**
	 * @throws Exception
	 */
	public JadeEngine() throws Exception {
	}

	/**
	 * @param pobjProperties
	 * @throws Exception
	 */
	public JadeEngine(final Properties pobjProperties) throws Exception {
		super(pobjProperties);
	}

	/**
	 * @param pobjOptions
	 * @throws Exception
	 */
	public JadeEngine(final JADEOptions pobjOptions) throws Exception {
		super( pobjOptions);
	}

	/**
	 * @param pobjJSSettings
	 * @throws Exception
	 */
	public JadeEngine(final HashMap<String, String> pobjJSSettings) throws Exception {
		super(pobjJSSettings);
	}
}
