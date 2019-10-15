package com.sos.jobstreams.resolver;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.jobstreams.classes.FunctionResolver;

public class TestFunctionResolver {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testFunctionResolver() {
        FunctionResolver functionResolver = new FunctionResolver();
        String s = "";
        s = functionResolver.resolveFunctions("param value%%today()%% any value %%today()%%%%today()%% end of string");

    }

}
