package com.sos.testframework.h2;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** @author Stefan Schaedlich */
public abstract class H2Connection {

    private static final URL HIBERNATE_CFG_TEMPLATE = Resources.getResource("com/sos/testframework/h2/hibernate-h2.cfg.xml.template");
    private static final Logger LOGGER = LoggerFactory.getLogger(H2Connection.class);
    private final String connectionPrefix;
    private final List<File> sqlFiles;
    private final List<String> classNames;
    private Connection connection = null;
    private String initString = null;
    private String connectionString = null;
    private File temporaryWorkingDirectory = null;
    private File temporaryConfigFile = null;

    protected H2Connection(H2ConnectionType type) {
        this.connectionPrefix = type.getPrefix();
        this.sqlFiles = new ArrayList<File>();
        this.classNames = new ArrayList<String>();
    }

    protected H2Connection(H2ConnectionType type, ResourceList fileList) {
        this.connectionPrefix = type.getPrefix();
        this.sqlFiles = fileList.getFilelist();
        this.classNames = fileList.getClasslist();
    }

    private String getClassList() {
        StringBuilder result = new StringBuilder();
        for (String className : classNames) {
            result.append("<mapping class=\"");
            result.append(className);
            result.append("\"/>");
            result.append("\n");
        }
        return result.toString();
    }

    public String getConnectionString() {
        if (connectionString == null) {
            StringBuilder result = new StringBuilder();
            result.append(getConnectionURL());
            result.append(";");
            result.append(getInitString());
            connectionString = result.toString();
        }
        return connectionString;
    }

    private String getInitString() {
        if (initString == null) {
            StringBuilder result = new StringBuilder();
            for (File sqlFile : sqlFiles) {
                if (result.length() > 0) {
                    result.append("\\;");
                }
                result.append("runscript from '" + getFilename(sqlFile) + "'");
            }
            if (result.length() > 0) {
                result.insert(0, "INIT=");
            }
            initString = result.toString();

        }
        return initString;
    }

    public Connection connect() {
        if (connection == null) {
            try {
                LOGGER.info("Connecting to " + getConnectionString());
                this.connection = DriverManager.getConnection(getConnectionString());
            } catch (SQLException e) {
                throw new RuntimeException("Error connecting DB: " + getConnectionString(), e);
            }
        } else {
            LOGGER.info("Connection to [" + getConnectionURL() + "] is already set.");
        }
        return connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {

    }

    public void removeTemporaryFiles() {
        if (temporaryConfigFile != null && !temporaryConfigFile.delete()) {
            LOGGER.warn("Could not delete temporary configuration file [" + temporaryConfigFile.getAbsolutePath() + "]");
        }
        if (temporaryWorkingDirectory != null && !temporaryWorkingDirectory.delete()) {
            LOGGER.warn("Could not delete temporary configuration file folder [" + temporaryWorkingDirectory.getAbsolutePath() + "]");
        }
    }

    public String getFilename(File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }

    /** The file with the hibernate configuration takes placed in a (temporary)
     * folder selected by this class.
     * 
     * @return */
    public File createTemporaryHibernateConfiguration() {
        File configFile = createTemporaryEnvironment();
        createHibernateConfiguration(configFile);
        return configFile;
    }

    /** The file with the hibernate configuration takes placed in a (temporary)
     * folder selected by this class.
     * 
     * @return */
    public File createTemporaryEnvironment() {
        if (temporaryConfigFile == null) {
            if (temporaryWorkingDirectory == null) {
                temporaryWorkingDirectory = Files.createTempDir();
            }
            temporaryConfigFile = createTemporaryFile(temporaryWorkingDirectory);
        }
        return temporaryConfigFile;
    }

    public File createTemporaryFile(File targetDir) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("hibernate_", ".cfg.xml", targetDir);
        } catch (IOException e) {
            throw new RuntimeException("Error creating temporary file for hibernate configuration.", e);
        }
        return tempFile;
    }

    /** The file with the hibernate configuration takes place in a given file.
     * 
     * @param targetFile
     * @return */
    public File createHibernateConfiguration(File targetFile) {
        try {
            String content = Resources.toString(HIBERNATE_CFG_TEMPLATE, Charset.defaultCharset());
            content = content.replace("${connection.url}", getConnectionString());
            content = content.replace("${connection.classes}", getClassList());
            LOGGER.debug(content);
            Files.write(content, targetFile, Charset.defaultCharset());
            LOGGER.info("The hibernate configuration files placed at " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            String msg = "Error writing file " + targetFile.getAbsolutePath();
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return targetFile;
    }

    /** The file with the hibernate configuration takes place in a given file.
     * 
     * @param resource
     * @return */
    public File createHibernateConfigurationFromResource(URL resource, File targetDir) {
        temporaryWorkingDirectory = targetDir;
        temporaryConfigFile = createTemporaryFile(targetDir);
        try {
            String content = Resources.toString(resource, Charset.defaultCharset());
            Files.write(content, temporaryConfigFile, Charset.defaultCharset());
            LOGGER.info("The hibernate configuration files placed at " + temporaryConfigFile.getAbsolutePath());
        } catch (IOException e) {
            String msg = "Error writing file " + temporaryConfigFile.getAbsolutePath();
            LOGGER.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        return temporaryConfigFile;
    }

    /** To build a connection with a specific database location this methood
     * might be override.
     *
     * @return */
    protected String getConnectionURL() {
        return connectionPrefix;
    }

}