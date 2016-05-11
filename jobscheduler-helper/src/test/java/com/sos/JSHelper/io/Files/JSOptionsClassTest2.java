package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.Test;

import com.sos.JSHelper.Options.JSOptionsClass;

public class JSOptionsClassTest2 extends JSOptionsClass {

    private static final long serialVersionUID = 1L;
    @Test
    public void testGetIndexedItem() throws Exception {
        final HashMap<String, String> objSettings = new HashMap<String, String>();
        final String strIndexedKey = "sql_statment";
        objSettings.put("sql_statment" + "1", "Select * from table1;");
        objSettings.put("sql_statment" + "2", "Select * from table2;");
        final JSOptionsClass objOptionsClass = new JSOptionsClass(objSettings);
        final String strActual = objOptionsClass.getIndexedItem(strIndexedKey, "Test Indexed Settings", ";");
        final String strExpected = "Select * from table1;Select * from table2;";
        assertEquals("Alle Indexed options Values werden geliefert", strExpected, strActual);
    }

}