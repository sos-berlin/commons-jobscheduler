package com.sos.testframework.h2;

/**
 * <h1>Type of database connection.</h1>
 *
 * <p>In general two connection types are supported:</p>
 * <ul>
 *      <li>
 *          <p>IN_MEMORY (correspondent with H2InMemoryConnection)</p>
 *          <p>
 *              It has no contents in the file system. The complete database is stored in memory of the running JVM only.
 *          </p>
 *      </li>
 *      <li>
 *          <p>FILE_BASED (correspondent with H2PersistentConnection or H2TemporaryConnection)</p>
 *          <p>
 *              The contents of the database are stored in the file system either in a temporary folder (H2TemporaryConnection) or in a persistent folder (H2PersistentConnection) of the file system.
 *          </p>
 *      </li>
 * </ul>
 *
 * @author Stefan Schädlich
 */
public enum H2ConnectionType {
    FILE_BASED("jdbc:h2:"),
    IN_MEMORY("jdbc:h2:mem:");

    private final String prefix;

    private H2ConnectionType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

}
