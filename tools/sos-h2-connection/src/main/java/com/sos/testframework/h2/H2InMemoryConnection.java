package com.sos.testframework.h2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A H2 connection with contents in the memory, only.
 *
 * @author Stefan Schädlich
 * @version 28.05.13 16:04
 */
public class H2InMemoryConnection extends H2Connection {

    private final static Logger logger = LoggerFactory.getLogger(H2InMemoryConnection.class);

    public H2InMemoryConnection(ResourceList fileList) {
        super(H2ConnectionType.IN_MEMORY, fileList );
    }

}
