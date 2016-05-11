package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class JSOptionValueListTest {

    private static final Logger LOGGER = Logger.getLogger(JSOptionValueListTest.class);
    private HashMap<String, String> settings = null;
    private JSOptionsClass objOptions = null;
    private final String strKey = "sql_statement";
    private final String strDescr = "SQL Statements";
    private final String strDelimiter = ";";
    private final String[] strStatements = new String[10];
    private JSOptionValueList optionValueList = null;

    @Before
    public void setUp() throws Exception {
        settings = new HashMap<String, String>();
        objOptions = new JSOptionsClass(settings);
    }

    @Test
    public void testConcatenatedValue1() throws Exception {
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "1", "", true);
        assertEquals("ConcatenatedValue::Empty", "", optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testConcatenatedValue2() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO";
        settings.put(strKey, strStatements[0]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "2", "", true);
        assertEquals("ConcatenatedValue::Single SQL Stmt1", strStatements[0], optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testConcatenatedValue3() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;";
        settings.put(strKey, strStatements[0]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "3", "", true);
        assertEquals("ConcatenatedValue::Single SQL Stmt2", strStatements[0], optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testConcatenatedValue4() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;SELECT ABC FROM TABELLE1;";
        strStatements[1] = "SELECT * FROM TABELLE2;";
        settings.put(strKey, strStatements[0]);
        settings.put(strKey + "1", strStatements[1]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "4", "", true);
        assertEquals("ConcatenatedValue::Multiple SQL Stmt1", strStatements[0] + strStatements[1], optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testConcatenatedValue5() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;SELECT ABC FROM TABELLE1";
        strStatements[1] = "SELECT * FROM TABELLE2;";
        settings.put(strKey, strStatements[0]);
        settings.put(strKey + "1", strStatements[1]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "5", "", true);
        assertEquals("ConcatenatedValue::Multiple SQL Stmt2", strStatements[0] + strDelimiter + strStatements[1],
                optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testConcatenatedValue6() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;SELECT ABC FROM TABELLE1;";
        strStatements[1] = "SELECT * FROM TABELLE222";
        strStatements[2] = "SELECT * FROM TABELLE4711;";
        settings.put(strKey + "1", strStatements[0]);
        settings.put(strKey + "2", strStatements[1]);
        settings.put(strKey + "3", strStatements[2]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "6", "", true);
        assertEquals("ConcatenatedValue::Multiple SQL Stmt2b", strStatements[0] + strStatements[1] + strDelimiter + strStatements[2],
                optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testConcatenatedValue7() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;SELECT ABC FROM TABELLE1;";
        strStatements[1] = "SELECT * FROM TABELLE222;";
        strStatements[2] = "SELECT SPALTE1 FROM TABELLE5;";
        settings.put(strKey + "1", strStatements[0]);
        settings.put(strKey + "3", strStatements[2]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "7", "", true);
        assertEquals("ConcatenatedValue::Multiple SQL Stmt4", strStatements[0], optionValueList.concatenatedValue(strDelimiter));
    }

    @Test
    public void testAppendValue2EmptyValueList() throws Exception {
        JSOptionValueList objVL = new JSOptionValueList(objOptions, "ValueList", "Descr", "", "", false);
        objVL.AppendValue("SELECT * FROM HALLO1;SELECT * FROM HALLO2");
        assertTrue("contains SELECT * FROM HALLO1", objVL.contains("SELECT * FROM HALLO1"));
    }

    @Test
    public void testAppendValue() throws Exception {
        JSOptionValueList objVL = new JSOptionValueList(objOptions, "ValueList", "Descr", "SELECT * FROM HALLO", "", false);
        assertTrue("contains SELECT * FROM HALLO", objVL.contains("SELECT * FROM HALLO"));
        objVL.AppendValue("SELECT * FROM HALLO1;SELECT * FROM HALLO2");
        assertTrue("contains SELECT * FROM HALLO", objVL.contains("SELECT * FROM HALLO"));
        LOGGER.info("JSOptionValueListTest.testAppendValue()" + objVL.concatenatedValue(";"));
        assertTrue("contains SELECT * FROM HALLO1", objVL.contains("SELECT * FROM HALLO1"));
        assertTrue("contains SELECT * FROM HALLO2", objVL.contains("SELECT * FROM HALLO2"));
        assertTrue("contains SELECT * FROM HALLO", objVL.contains("SELECT * FROM HALLO"));
        objVL = new JSOptionValueList(objOptions, "ValueList", "Descr", "", false);
        objVL.AppendValue("SELECT * FROM HALLO1;SELECT * FROM HALLO2");
        assertTrue("contains SELECT * FROM HALLO1", objVL.contains("SELECT * FROM HALLO1"));
        assertTrue("contains SELECT * FROM HALLO2", objVL.contains("SELECT * FROM HALLO2"));
        objVL.AppendValue("HUHU");
        assertTrue("contains SELECT * FROM HALLO1", objVL.contains("HUHU"));
        objVL = new JSOptionValueList(objOptions, "ValueList", "Descr", "", "", false);
        objVL.AppendValue("SELECT * FROM HALLO1;SELECT * FROM HALLO2");
        assertTrue("contains SELECT * FROM HALLO1", objVL.contains("SELECT * FROM HALLO1"));
        assertTrue("contains SELECT * FROM HALLO2", objVL.contains("SELECT * FROM HALLO2"));
    }

    @Test
    public void testKonstruktor4IndexedKey() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;SELECT ABC FROM TABELLE1;";
        strStatements[1] = "SELECT * FROM TABELLE2;";
        settings.put(strKey, strStatements[0]);
        settings.put(strKey + "1", strStatements[1]);
        JSOptionValueList objVL = new JSOptionValueList(objOptions, strKey, "Descr", "", false);
        int intActualLength = objVL.valueList().length;
        int intExpectedLength = 2;
        assertEquals("Anzahl indexed Options ist korrekt", intExpectedLength, intActualLength);
        objVL.Value(objVL.concatenatedValue(strKey));
        intActualLength = objVL.valueList().length;
        intExpectedLength = 3;
        assertEquals("Anzahl indexed Options ist korrekt", intExpectedLength, intActualLength);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testValue2() {
        JSOptionValueList optionValueList1 = new JSOptionValueList(objOptions, strKey, "eins;zwei;drei", "eins", "", true);
        assertEquals("Value must be 'eins'", "eins", optionValueList1.Value());
        optionValueList1.Value("zwei,drei,vier");
        assertEquals("Value must be 'zwei'", "zwei", optionValueList1.Value());
        String strT = optionValueList1.concatenatedValue(",");
        assertEquals("ValueList is wrong", "zwei,drei,vier", strT);
        optionValueList1.Value("sieben");
        assertEquals("Value must be 'sieben'", "sieben", optionValueList1.Value());
        assertTrue("Value must have value 'sieben'", optionValueList1.contains("sieben"));
    }

    @Test
    public void testValue() throws Exception {
        strStatements[0] = "SELECT * FROM HALLO;SELECT ABC FROM TABELLE1;";
        strStatements[1] = "SELECT * FROM TABELLE222;";
        strStatements[2] = "SELECT SPALTE1 FROM TABELLE5;";
        settings.put(strKey + "1", strStatements[0]);
        settings.put(strKey + "3", strStatements[2]);
        optionValueList = new JSOptionValueList(objOptions, strKey, strDescr + "7", "", true);
    }

    @Test
    public void testContains() throws Exception {
        JSOptionValueList objVL = new JSOptionValueList(objOptions, "ValueList", "Descr", "Das|ist|das|Haus|vom|Nikolaus", "", false);
        assertTrue("contains Nikolaus", objVL.contains("Nikolaus"));
        assertFalse("contains Nikolaus", objVL.contains("kolaus"));
        objVL.Value("Das|ist|das|Haus|vom|Nikolaus");
        assertTrue("contains Nikolaus", objVL.contains("Nikolaus"));
        assertFalse("contains Nikolaus", objVL.contains("kolaus"));
        objVL.Value("Das;ist;das;Haus;vom;Nikolaus");
        assertTrue("contains Nikolaus", objVL.contains("Nikolaus"));
        assertFalse("contains Nikolaus", objVL.contains("kolaus"));
        objVL.Value("Das|ist;das|Haus;vom|Nikolaus");
        assertTrue("contains Nikolaus", objVL.contains("Nikolaus"));
        assertFalse("contains Nikolaus", objVL.contains("kolaus"));
    }

    @Test
    public void testElementAt() throws Exception {
        JSOptionValueList objVL = new JSOptionValueList(objOptions, "ValueList", "Descr", "Das|ist|das|Haus|vom|Nikolaus", "", false);
        assertEquals("Element 1", "Das", objVL.ElementAt(0));
        assertEquals("Element 2", "ist", objVL.ElementAt(1));
        assertEquals("Element 3", "das", objVL.ElementAt(2));
        assertEquals("Element 4", "Haus", objVL.ElementAt(3));
        assertEquals("Element 5", "vom", objVL.ElementAt(4));
        assertEquals("Element 6", "Nikolaus", objVL.ElementAt(5));
    }

}