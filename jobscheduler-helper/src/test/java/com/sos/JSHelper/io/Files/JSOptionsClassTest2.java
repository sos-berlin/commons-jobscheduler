package com.sos.JSHelper.io.Files;

import java.util.HashMap;

import org.junit.Test;

import com.sos.JSHelper.Options.JSOptionsClass;

public class JSOptionsClassTest2 extends JSOptionsClass {

    private static final long serialVersionUID = 1L;

    @Test
    public void testGetIndexedItem() throws Exception {
        final HashMap<String, String> objSettings = new HashMap<String, String>();
        objSettings.put("sql_statment" + "1", "Select * from table1;");
        objSettings.put("sql_statment" + "2", "Select * from table2;");
    }

}