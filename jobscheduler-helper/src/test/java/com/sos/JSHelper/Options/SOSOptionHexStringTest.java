/**
 *
 */
package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** @author KB */
public class SOSOptionHexStringTest {

    /** \brief setUpBeforeClass
     *
     * \details
     *
     * \return void */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /** \brief tearDownAfterClass
     *
     * \details
     *
     * \return void */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /** \brief setUp
     *
     * \details
     *
     * \return void */
    @Before
    public void setUp() throws Exception {
    }

    /** \brief tearDown
     *
     * \details
     *
     * \return void */
    @After
    public void tearDown() throws Exception {
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionHexString#getValue()}. */
    @Test
    public void testValue() {
        SOSOptionHexString objHS = new SOSOptionHexString(null, "key", "desc", "value", "", false);
        objHS.setValue("das&#x0d; ist&#x0a; das&#x0d; Haus&#x0a; vom&#x0d; Nikolaus");
        System.out.println(objHS.getValue());
        assertEquals("unescapeXMLEntities", "das\r ist\n das\r Haus\n vom\r Nikolaus", objHS.getValue());
    }

    /** Test method for
     * {@link com.sos.JSHelper.Options.SOSOptionFileString#setValue(java.lang.String)}
     * . */
    @Test
    public void testValueString() {
    }

}
