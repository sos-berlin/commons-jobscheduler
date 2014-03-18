package com.sos.testframework.h2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.SQLException;

/**
 * To build a connection to a given database.
 */
public class H2PersistentConnection extends H2Connection {

    private final String dbName;
    private final File workingDirectory;

    private final static Logger logger = LoggerFactory.getLogger(H2PersistentConnection.class);

    public H2PersistentConnection(File dbLocation, String dbName, ResourceList fileList) {
        super( H2ConnectionType.FILE_BASED, fileList );
        this.dbName = dbName;
        this.workingDirectory = dbLocation;
        dbLocation.mkdirs();
    }

    public File getDatabaseLocation() {
        return workingDirectory;
    }

    public void close() {
        if (getConnection() != null ) {
            try {
                this.getConnection().close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing DB: " + getConnectionString(),e);
            }
        }
        super.close();
    }

    public String getDatabaseName() {
        return dbName;
    }

    @Override
    final protected String getConnectionURL() {
        StringBuffer result = new StringBuffer();
        result.append(super.getConnectionURL());
        result.append(getDatabaseLocation());
        result.append("/");
        result.append(dbName);
        return result.toString();
    }

}
