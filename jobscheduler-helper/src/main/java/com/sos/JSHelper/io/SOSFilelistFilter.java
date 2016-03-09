package com.sos.JSHelper.io;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** <p>
 * Title:
 * </p>
 * <p>
 * Description: Filefilter-Schnittstelle
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: SOS GmbH
 * </p>
 * 
 * @author <a href="mailto:ghassan.beydoun@sos-berlin.com">Ghassan Beydoun</a>
 * @version $Id$ */
public class SOSFilelistFilter implements FilenameFilter {

    Pattern objPattern;

    /** Konstruktor
     * 
     * @param regexp ein regul�er Ausdruck
     * @param flag ist ein Integer-Wert: CASE_INSENSITIVE, MULTILINE, DOTALL,
     *            UNICODE_CASE, and CANON_EQ
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/constant-values.html#java.util.regex.Pattern.UNIX_LINES">Constant
     *      Field Values</a> */
    public SOSFilelistFilter(final String regexp, final int flag) throws Exception {
        objPattern = Pattern.compile(regexp, flag);
    }

    @Override
    public boolean accept(final File dir, final String filename) {
        return find(filename);
    }

    public boolean accept(final String filename) {
        return find(filename);
    }

    private boolean find(final String pstrValue) {
        Matcher matcher = objPattern.matcher(pstrValue);
        return matcher.find();
    }
}
