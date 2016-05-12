package com.sos.testframework.h2;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class H2ConnectionTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2ConnectionTest.class);
    private static final String RESOURCE_BASE = "com/sos/testframework/h2/";
    private static final String SQL_EXTENSION = ".sql";
    private static final HashMap<String, String> SQL_RESOURCE_NAMES = new HashMap<String, String>();
    private static ResourceList resources;

    @BeforeClass
    public static void beforeClass() {
        SQL_RESOURCE_NAMES.put("com.sos.testframework.h2.Table1DBItem", getSQLFileName("Table1"));
        SQL_RESOURCE_NAMES.put("com.sos.testframework.h2.Table2DBItem", getSQLFileName("Table2"));
        resources = new ResourceList(SQL_RESOURCE_NAMES);
    }

    @Test
    public void testTemporaryHibernateConnection() {
        H2Connection connection = new H2TemporaryConnection(resources);
        connection.connect();
        LOGGER.info(connection.getConnectionString());
        File f = connection.createTemporaryHibernateConfiguration();
        final String expected = "myName";
        Table1DBLayer dbLayer = new Table1DBLayer(f.getAbsolutePath());
        dbLayer.addRecord(expected);
        Table1DBItem record = dbLayer.getByName(expected);
        assertEquals(expected, record.getName());
        connection.close();
    }

    @Test
    public void testInMemoryHibernateConnection() {
        H2Connection connection = new H2InMemoryConnection(resources);
        connection.connect();
        LOGGER.info(connection.getConnectionString());
        File f = connection.createTemporaryHibernateConfiguration();
        final String expected = "myName";
        Table1DBLayer dbLayer = new Table1DBLayer(f.getAbsolutePath());
        dbLayer.addRecord(expected);
        Table1DBItem record = dbLayer.getByName(expected);
        assertEquals(expected, record.getName());
        connection.close();
        connection.removeTemporaryFiles();
    }

    private static String getSQLFileName(String baseName) {
        return RESOURCE_BASE + baseName + SQL_EXTENSION;
    }

    @AfterClass
    public static void afterClass() {
        resources.release();
    }

}