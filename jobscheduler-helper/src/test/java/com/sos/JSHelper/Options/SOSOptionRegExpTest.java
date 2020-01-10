package com.sos.JSHelper.Options;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.DataElements.JSDataElementDate;

/** @author KB */
public class SOSOptionRegExpTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionRegExpTest.class);
    private SOSOptionRegExp objRE = null;

    @Test
    public void wrongRegExp() {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "^test_dh000000.err", "", false);
        Pattern p = objRE.getPattern();
        p = objRE.getPattern("/\\.swf(\\?\\.*)?$/i");
    }

    @Test
    public void testMatcher() {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "^(.*/*)2.*\\.txt$", "", false);
        boolean flgMatched = objRE.match("/home/test/kb/4_abc.txt");
        assertFalse("do not match", flgMatched);
        flgMatched = objRE.match("/2_abc.txt");
        assertTrue("must match", flgMatched);
        flgMatched = objRE.match("/home/test/kb/2_abc.txt1");
        assertFalse("must not match", flgMatched);
        flgMatched = objRE.match("2_abc.txt");
        assertTrue("must match", flgMatched);
        flgMatched = objRE.match("/home/test/kb/2_abc.txt");
        assertTrue("must match", flgMatched);
        String strGroupText = objRE.getGroup(1);
        assertEquals("group found", "/home/test/kb/", strGroupText);
    }

    @Test
    public void testRegExpWithPlaceHolders() throws Exception {
        String strDateformat = "yyyy-MM-dd";
        String strDate = JSDataElementDate.getCurrentTimeAsString(strDateformat);
        LOGGER.info(strDate);
        objRE = new SOSOptionRegExp(new JSOptionsClass(), "test", "TestOption", String.format("^.*_[date:%1$s]_\\.txt$", strDateformat), "", false);
        String strV = objRE.getValue();
        LOGGER.info("value after replace is: " + strV);
        assertEquals("place holders 1", String.format("^.*_%1$s_\\.txt$", strDate), strV);
    }

    @Test
    public void testDoReplace() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        String strReplaceWhat = "";
        String strReplaceWith = "";
        String strStringToWorkOn = "";
        strReplaceWhat = "Hello";
        strReplaceWith = "World";
        strStringToWorkOn = "Hello";
        objRE.setValue(strReplaceWhat);
        assertEquals("RegExp Value failed", strReplaceWhat, objRE.getValue());
        assertEquals("replace failed", strReplaceWith, objRE.doReplace(strStringToWorkOn, strReplaceWith));
        strReplaceWhat = "Hello";
        strReplaceWith = "";
        strStringToWorkOn = "Hello  World";
        objRE.setValue(strReplaceWhat);
        assertEquals("RegExp Value failed", strReplaceWhat, objRE.getValue());
        assertEquals("replace failed", "  World", objRE.doReplace(strStringToWorkOn, strReplaceWith));
        strReplaceWhat = "(1)abc(12)def(.*)";
        strReplaceWith = "A;BB;CCC";
        strStringToWorkOn = "1abc12def123.txt";
        objRE.setValue(strReplaceWhat);
        assertEquals("RegExp Value failed", strReplaceWhat, objRE.getValue());
        assertEquals("replace failed", "AabcBBdefCCC", objRE.doReplace(strStringToWorkOn, strReplaceWith));
        strReplaceWhat = "(INT_)(.*)";
        strReplaceWith = "\\-;\\2";
        strStringToWorkOn = "INT_4711-0815.txt";
        objRE.setValue(strReplaceWhat);
        assertEquals("RegExp Value failed", strReplaceWhat, objRE.getValue());
        assertEquals("replace failed", "4711-0815.txt", objRE.doReplace(strStringToWorkOn, strReplaceWith));
        strReplaceWhat = "(.{5})(.{6})(.*)";
        strReplaceWith = "\\1;;\\3;";
        strStringToWorkOn = "abcba123456hallo.txt";
        objRE.setValue(strReplaceWhat);
        assertEquals("RegExp Value failed", strReplaceWhat, objRE.getValue());
        assertEquals("replace failed", "abcbahallo.txt", objRE.doReplace(strStringToWorkOn, strReplaceWith));
        doTestReplace("Swapping", "(1)abc(12)def(.*)", "\\2;\\1;CCC", "1abc12def123.txt", "12abc1defCCC");
        doTestReplace("Swapping", "(1)abc(12)def(.*)", "\\2;\\1;\\-", "1abc12def123.txt", "12abc1def");
        doTestReplace("Prefix", "(.*)", "prefix\\1", "1abc12def123.txt", "prefix1abc12def123.txt");
        doTestReplace("Prefix", "(.*)", "\\1suffix", "1abc12def123.txt", "1abc12def123.txtsuffix");
        doTestReplace("Prefix", "(.*)", "prefix\\1suffix", "1abc12def123.txt", "prefix1abc12def123.txtsuffix");
        doTestReplace("Prefix", "(prefix)(.*)", "\\-;\\2", "prefix1abc12def123.txt", "1abc12def123.txt");
        doTestReplace("Prefix", "(prefix)(.*)", "\\2", "prefix1abc12def123.txt", "1abc12def123.txt");
        String strDate = JSDataElementDate.getCurrentTimeAsString("yyyyMMddHHmm");
        doTestReplace("Date insertion", "(.*)(.txt)", "\\1_[date:yyyyMMddHHmm];\\2", "1.txt", "1_" + strDate + ".txt");
        strDate = JSDataElementDate.getCurrentTimeAsString();
        doTestReplace("Date insertion without date-format", "(.*)(.txt)", "\\1_[date:];\\2", "1.txt", "1_" + strDate + ".txt");
        strDate = JSDataElementDate.getCurrentTimeAsString();
        doTestReplace("Date suffix", "(.*)(.txt)", "\\1;\\2_[date:]", "1.txt", "1.txt_" + strDate);
        strDate = JSDataElementDate.getCurrentTimeAsString();
        doTestReplace("Date prefix", "(.*)(.txt)", "[date:]_\\1;\\2", "1.txt", strDate + "_1.txt");
        doTestReplace("FileName uppercase 1", ".*", "[filename:uppercase]", "1.txt", "1.TXT");
        doTestReplace("FileName lowercase 2", ".*", "[filename:lowercase]", "1.txt", "1.txt");
        String strExpectedFileName = "20120613_144343_SHRTIS001_0000000001011689.DAT";
        String strFileName4Test = "20120613_144343_SHRTIS001_0000000001011689.DAT.SHRTIS001_DOR";
        doTestReplace("ausschneiden 1", "(.*)(\\.DAT)(\\.SHRTIS001_DOR)", "\\1;\\2;", strFileName4Test, strExpectedFileName);
        doTestReplace("ausschneiden 2", "(.*)(\\.DAT)(\\.SHRTIS001_DOR)", "\\1;\\2; ;", strFileName4Test, strExpectedFileName);
        doTestReplace("ausschneiden 3", "(.*)(\\.DAT)(.*$)", "\\1;\\2", strFileName4Test, strExpectedFileName);
        doTestReplace("ausschneiden 4", "(.*)(\\.DAT).*$", "\\1;\\2", strFileName4Test, strFileName4Test);
    }

    @Test
    public void testUnixTimeStamp() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        doTestReplace("timestamp", "(.*)(.txt)", "[timestamp:]_\\1;\\2", "abc.txt", null);
    }

    @Test
    @Ignore("Test set to Ignore for later examination, fails in Jenkins build")
    public void testTempFile() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        doTestReplace("tempfile", "(.*)(.txt)", "[tempfile:]_\\1;\\2", "abc.txt", null);
    }

    @Test
    public void testUUID() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        doTestReplace("UUID", "(.*)(.txt)", "[uuid:]_\\1;\\2", "1.txt", null);
    }

    @Test
    public void testSQLTimeStamp() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        doTestReplace("SQLTimeStamp", "(.*)(.txt)", "[sqltimestamp:]_\\1;\\2", "1.txt", null);
    }

    @Test
    public void testRegExpSQLTimeStamp() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        doRegExTest("SQLTimeStamp", "(.*)(.txt)", "[sqltimestamp:]_$1$2", "abcd.txt", null);
    }

    @Test
    public void testDoRegExReplace() throws Exception {
        objRE = new SOSOptionRegExp(null, "test", "TestOption", "", "", false);
        doRegExTest("ausschneiden", "(.*)(\\.DAT)(\\.SHRTIS001_DOR)", "$1$2", "20120613_144343_SHRTIS001_0000000001011689.DAT.SHRTIS001_DOR",
                "20120613_144343_SHRTIS001_0000000001011689.DAT");
    }

    private void doTestReplace(final String strText, final String strReplaceWhat, final String strReplaceWith, final String strWork,
            final String strExpectedResult) throws Exception {
        objRE.setValue(strReplaceWhat);
        assertEquals(strText, strReplaceWhat, objRE.getValue());
        String strResult = objRE.doReplace(strWork, strReplaceWith);
        if (strExpectedResult != null) {
            assertEquals(strText, strExpectedResult, strResult);
        }
        LOGGER.info(strResult);
    }

    private void doRegExTest(final String strText, final String strReplaceWhat, final String strReplaceWith, final String strWork,
            final String strExpectedResult) throws Exception {
        objRE.setValue(strReplaceWhat);
        assertEquals(strText, strReplaceWhat, objRE.getValue());
        String strResult = objRE.doRegExpReplace(strWork, strReplaceWith);
        if (strExpectedResult != null) {
            assertEquals(strText, strExpectedResult, strResult);
        }
        LOGGER.info(strResult);
    }

}