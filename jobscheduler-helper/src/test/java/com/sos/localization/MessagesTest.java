package com.sos.localization;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagesTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagesTest.class);
    private Messages messages = null;

    @Before
    public void setUp() throws Exception {
        messages = new Messages("com/sos/localization/messages", Locale.getDefault());
    }

    @Test
    public void testVarArgs() {
        String strM = "das ist '%1$s', '%2$s' und '%3$s' ...";
        strM = messages.getMsg(strM, "eins", "zwei", "drei", Locale.getDefault());
        assertEquals("get message-text with parameter", "das ist 'eins', 'zwei' und 'drei' ...", strM);
        strM = "das ist '%1$s', '%2$d' und '%3$s' ...";
        strM = messages.getMsg(strM, "eins", 2, "drei", Locale.getDefault());
        assertEquals("get message-text with parameter", "das ist 'eins', '2' und 'drei' ...", strM);
    }

    @Test
    public void testGetMsgENGLISHSingle() {
        String strM = messages.getMsg("JSJ-I-106", Locale.ENGLISH);
        LOGGER.info(strM);
        assertEquals("JSJ-I-106", "JSJ-I-106: %1$s - ended without errors", strM);
    }

    @Test
    public void testGetMsgENGLISHAlias() {
        String strM = messages.getMsg("JSJ-I-111", Locale.ENGLISH);
        assertEquals("JSJ-I-111", "JSJ-I-111: %1$s - ended without errors", strM);
    }

    @Test
    public void testGetMsg_en_US() {
        String strM = messages.getMsg("JSJ-I-111", new Locale("en", "US"));
        LOGGER.info(strM);
        assertEquals("JSJ-I-111", "JSJ-I-111: %1$s - ended without errors", strM);
    }

    @Test
    public void testGetMsgFrenchSingle() {
        String strM = messages.getMsg("JSJ-I-106", Locale.FRENCH);
        assertEquals("JSJ-I-106", "JSJ-I-106: %1$s - s'est terminée sans erreurs", strM);
    }

    @Test
    public void testGetMsgFrenchAlias() {
        String strM = messages.getMsg("JSJ-I-111", Locale.FRENCH);
        assertEquals("JSJ-I-111", "JSJ-I-111: %1$s - s'est terminée sans erreurs", strM);
    }

    @Test
    public void testGetMsgSingle() {
        String strM = messages.getMsg("JSJ-I-106", new Locale("de", "DE"));
        assertEquals("JSJ-I-106", "JSJ-I-106: %1$s - wurde ohne Fehler beendet", strM);
    }

    @Test
    public void testGetMsgSingle_de_CH() {
        String strM = messages.getMsg("JSJ-I-106", new Locale("de", "CH"));
        assertEquals("JSJ-I-106", "JSJ-I-106: %1$s - wurde ohne Fehler beendet", strM);
    }

    @Test
    public void testGetMsgSingle_it() {
        String strM = messages.getMsg("JSJ-I-106", Locale.ITALIAN);
        LOGGER.info(strM);
        assertEquals("JSJ-I-106", messages.getMsg("JSJ-I-106", Locale.getDefault()), strM);
    }

    @Test
    @Ignore
    public void testGetMsgSingle_ja() {
        String strM = messages.getMsg("JSJ-I-105", Locale.JAPANESE);
        LOGGER.info(strM);
        assertEquals("JSJ-I-106", "JSJ-I-105: %1$s - wurde ohne Fehler beendet", strM);
    }

    @Test
    @Ignore("test makes no sense, depends on locale of host where the test runs, can´t be sure that it is german")
    public void testGetMsgAlias() {
        String strM = messages.getMsg("JSJ-I-111", Locale.getDefault());
        assertEquals("JSJ-I-111", "JSJ-I-111: %1$s - wurde ohne Fehler beendet", strM);
    }

    @Test
    public void testMissingMessage() {
        String conM = "This Message is not in the property file";
        String strM = messages.getMsg(conM);
        assertEquals("JSJ-I-111", conM, strM);
    }

}