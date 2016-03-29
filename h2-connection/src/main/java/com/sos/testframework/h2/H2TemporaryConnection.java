package com.sos.testframework.h2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class H2TemporaryConnection extends H2PersistentConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2TemporaryConnection.class);
    private static File databaseFile = null;

    public H2TemporaryConnection(ResourceList fileList) {
        super(fileList.getWorkingDirectory(), createDBFile(fileList.getWorkingDirectory()), fileList);
    }

    private static String createDBFile(File inDir) {
        try {
            databaseFile = File.createTempFile("h2-", "db", inDir);
        } catch (IOException e) {
            throw new RuntimeException("Error creating temporary file.");
        }
        return databaseFile.getName();
    }

    public void close() {
        super.close();
        if (!databaseFile.delete()) {
            LOGGER.warn("Database at location " + databaseFile.getAbsolutePath() + " could not delete.");
        }
        super.removeTemporaryFiles();
    }

}