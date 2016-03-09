package com.sos.testframework.h2;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;

/** Tests for different connection types. */
public class H2ConnectionTest {

    private final Logger logger = LoggerFactory.getLogger(H2ConnectionTest.class);

    private static final String resourceBase = "com/sos/testframework/h2/";
    private static final String sqlExtension = ".sql";
    private static final HashMap<String, String> sqlResourceNames = new HashMap<String, String>();
    private static ResourceList resources;

    @BeforeClass
    public static void beforeClass() {
        sqlResourceNames.put("com.sos.testframework.h2.Table1DBItem", getSQLFileName("Table1"));
        sqlResourceNames.put("com.sos.testframework.h2.Table2DBItem", getSQLFileName("Table2"));
        resources = new ResourceList(sqlResourceNames);
    }

    @Test
    public void testTemporaryHibernateConnection() {
        H2Connection connection = new H2TemporaryConnection(resources);
        connection.connect();
        logger.info(connection.getConnectionString());
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
        logger.info(connection.getConnectionString());
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
        return resourceBase + baseName + sqlExtension;
    }

    @AfterClass
    public static void afterClass() {
        resources.release();
    }

}
