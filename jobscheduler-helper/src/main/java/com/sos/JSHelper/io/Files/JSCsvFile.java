package com.sos.JSHelper.io.Files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import com.sos.JSHelper.Listener.JSListener;

public class JSCsvFile extends JSTextFile {

    private static final long serialVersionUID = 1L;
    private String[] strHeaders = null;
    private String[] strCurrentLine = null;
    private char chrColumnDelimiter = FIELD_DELIMITER;
    private char chrRecordDelimiter = BLOCK_DELIMITER;
    private final char chrValueDelimiter = VALUE_DELIMITER;
    private boolean flgIsNewline;
    private boolean flgIgnoreValueDelimiter = false;
    private boolean flgHeadersWritten = false;
    private boolean flgAlwaysSurroundFielJSithQuotes = true;
    private int lngNoOfFieldsInBuffer = 0;
    private int intFieldCount = 0;
    private int intRecordCount = 0;
    private boolean flgFieldCount = true;
    private boolean flgCheckColumnCount = true;
    public static String END_OF_LINE = new String("END_OF_LINE");
    public static char FIELD_DELIMITER = ';';
    public static char BLOCK_DELIMITER = '\n';
    public static char VALUE_DELIMITER = '\"';

    public JSCsvFile(final String pstrFileName) {
        super(pstrFileName);
    }

    public JSCsvFile(final String pstrFileName, final JSListener objListener) {
        super(pstrFileName);
        registerMessageListener(objListener);
    }

    public JSCsvFile setColumnDelimiter(final String pstrColumnDelimiter) {
        chrColumnDelimiter = pstrColumnDelimiter.toCharArray()[0];
        return this;
    }

    public char getColumnDelimiter() {
        return chrColumnDelimiter;
    }

    public void loadHeaders() throws Exception {
        if (strHeaders == null) {
            strHeaders = readCSVLine();
        }
    }

    public boolean isCheckColumnCount() {
        return flgCheckColumnCount;
    }

    public void setCheckColumnCount(final boolean pflgCheckColumnCount) {
        flgCheckColumnCount = pflgCheckColumnCount;
    }

    public JSCsvFile setHeaders(final String[] pstrHeaders) throws Exception {
        strHeaders = pstrHeaders;
        writeHeaders();
        return this;
    }

    public JSCsvFile setHeaders(final ArrayList<String> fields) throws Exception {
        strHeaders = new String[fields.size()];
        for (int j = 0; j < fields.size(); j++) {
            strHeaders[j] = fields.get(j);
        }
        writeHeaders();
        return this;
    }

    public JSCsvFile writeHeaders() throws Exception {
        if (!flgHeadersWritten && getNoOfCharsInBuffer() <= 0) {
            this.append(strHeaders);
            flgHeadersWritten = true;
        }
        return this;
    }

    public String[] getHeaders() {
        return strHeaders;
    }

    public String[] readCSVLine() throws IOException {
        final ArrayList<String> list = new ArrayList<String>();
        String str = null;
        int intColumnCount = 0;
        intRecordCount++;
        while (true) {
            str = readCSVField();
            if (str == null) {
                break;
            }
            if (END_OF_LINE.equalsIgnoreCase(str) && intColumnCount == intFieldCount) {
                flgFieldCount = false;
                break;
            }
            if (flgFieldCount) {
                intFieldCount++;
            }
            intColumnCount++;
            if (END_OF_LINE.equalsIgnoreCase(str)) {
                list.add("");
                break;
            }
            list.add(str);
        }
        if (intColumnCount != intFieldCount && flgCheckColumnCount) {
            message("WARN: problem in record " + intRecordCount + " - " + intFieldCount + " columns expected, but the record contains "
                    + intColumnCount);
        }
        if (list.isEmpty() || intColumnCount != intFieldCount && flgCheckColumnCount) {
            return null;
        }
        return list.toArray(new String[0]);
    }

    public String readCSVField() throws IOException {
        if (flgIsNewline) {
            flgIsNewline = false;
            return END_OF_LINE;
        }
        StringBuilder stbBuilder = new StringBuilder();
        boolean flgFieldIsQuoted = false;
        int intLast = -1;
        int ch = getReader().read();
        if (ch == -1) {
            return null;
        }
        if (ch == chrValueDelimiter && !flgIgnoreValueDelimiter) {
            flgFieldIsQuoted = true;
        } else {
            if (ch == chrRecordDelimiter && !flgFieldIsQuoted) {
                return END_OF_LINE;
            } else {
                if (ch == chrColumnDelimiter && !flgFieldIsQuoted) {
                    return "";
                } else {
                    stbBuilder.append((char) ch);
                }
            }
        }
        while ((ch = getReader().read()) != -1) {
            if (ch == chrRecordDelimiter && !flgFieldIsQuoted) {
                final int intL = stbBuilder.length();
                if (intL > 1 && stbBuilder.charAt(stbBuilder.length() - 1) == '\r') {
                    final String strB = stbBuilder.substring(0, stbBuilder.length() - 1);
                    stbBuilder = new StringBuilder(strB);
                    lngNoOfLinesRead++;
                }
                flgIsNewline = true;
                break;
            }
            if (flgFieldIsQuoted && ch == chrValueDelimiter) {
                if (intLast == ch) {
                    intLast = -1;
                    stbBuilder.append(chrValueDelimiter);
                    continue;
                }
                intLast = ch;
                continue;
            }
            if (ch == chrColumnDelimiter) {
                if (flgFieldIsQuoted) {
                    if (intLast == chrValueDelimiter) {
                        break;
                    }
                } else {
                    break;
                }
            }
            stbBuilder.append((char) ch);
        }
        return removeNewLineChar(stbBuilder);
    }

    private String removeNewLineChar(final StringBuilder pstrB) {
        String strB = pstrB.toString();
        int intL = pstrB.length();
        if (intL > 1) {
            intL--;
            if (pstrB.charAt(intL) == '\r') {
                strB = pstrB.substring(1, pstrB.length());
            }
        }
        return strB;
    }

    public String readCSVField(final String pstrColumnName) throws Exception {
        if (strHeaders == null) {
            loadHeaders();
        }
        if (strCurrentLine == null) {
            nextBlock();
            if (strCurrentLine == null) {
                return null;
            }
        }
        int idx = -1;
        for (int i = 0; i < strHeaders.length; i++) {
            if (pstrColumnName.equalsIgnoreCase(strHeaders[i])) {
                idx = i;
            }
        }
        String strFieldValue = null;
        if (idx != -1) {
            strFieldValue = strCurrentLine[idx];
            strCurrentLine = null;
        } else {
            message(String.format("ColumnName '%1$s' not found in Headers.", pstrColumnName));
        }
        return strFieldValue;
    }

    public boolean nextBlock() throws IOException {
        strCurrentLine = readCSVLine();
        return strCurrentLine != null;
    }

    public JSCsvFile append(final Object[] fields) throws Exception {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            builder.append(maskSpecialChars(String.valueOf(fields[i])));
            if (i < fields.length - 1) {
                builder.append(chrColumnDelimiter);
            }
        }
        write(builder.toString());
        return this;
    }

    public JSCsvFile append(final Vector<String> fields) throws Exception {
        final StringBuilder builder = new StringBuilder();
        final int intSize = fields.size();
        for (int i = 0; i < intSize; i++) {
            builder.append(maskSpecialChars(String.valueOf(fields.elementAt(i))));
            if (i < intSize - 1) {
                builder.append(chrColumnDelimiter);
            }
        }
        write(builder.toString());
        return this;
    }

    public JSCsvFile append(final Iterator<String> fields) throws Exception {
        final StringBuilder builder = new StringBuilder();
        while (fields.hasNext()) {
            final String elem = fields.next();
            builder.append(maskSpecialChars(elem));
            if (fields.hasNext()) {
                builder.append(chrColumnDelimiter);
            }
        }
        write(builder.toString());
        return this;
    }

    public JSCsvFile addCellValues(final Object[] fields) throws Exception {
        for (final Object field : fields) {
            addCellValue(String.valueOf(field));
        }
        return this;
    }

    public JSCsvFile addCellValues(final Iterator<String> fields) throws Exception {
        this.append(fields);
        return this;
    }

    @Override
    public JSCsvFile newLine() throws Exception {
        super.newLine();
        lngNoOfFieldsInBuffer = 0;
        return this;
    }

    public JSCsvFile addCellValue(final String pstrS) throws Exception {
        if (getNoOfCharsInBuffer() > 0 || lngNoOfFieldsInBuffer > 0) {
            outChar(chrColumnDelimiter);
        }
        outString(maskSpecialChars(pstrS));
        lngNoOfFieldsInBuffer++;
        return this;
    }

    private String maskSpecialChars(String strT) {
        boolean flgSurroundWithQuotes = false;
        final String strDelim = String.valueOf(chrValueDelimiter);
        final int iPos = strT.indexOf(chrValueDelimiter);
        if (iPos >= 0) {
            strT = strT.replace(strDelim, strDelim + strDelim);
            flgSurroundWithQuotes = true;
        }
        if (!strT.isEmpty() && flgAlwaysSurroundFielJSithQuotes) {
            flgSurroundWithQuotes = true;
        }
        if (flgSurroundWithQuotes || strT.indexOf(chrColumnDelimiter) > 0 || strT.indexOf('\r') > 0 || strT.indexOf('\n') > 0) {
            strT = strDelim + strT + strDelim;
        }
        return strT;
    }

    public boolean isAlwaysSurroundFielJSithQuotes() {
        return flgAlwaysSurroundFielJSithQuotes;
    }

    public JSCsvFile setAlwaysSurroundFielJSithQuotes(final boolean pflgAlwaysSurroundFielJSithQuotes) throws Exception {
        flgAlwaysSurroundFielJSithQuotes = pflgAlwaysSurroundFielJSithQuotes;
        return this;
    }

    public boolean isIgnoreValueDelimiter() {
        return flgIgnoreValueDelimiter;
    }

    public JSCsvFile setIgnoreValueDelimiter(final boolean pflgIgnoreValueDelimiter) throws Exception {
        flgIgnoreValueDelimiter = pflgIgnoreValueDelimiter;
        return this;
    }

    public void setRecordDelimiter(final char pchrRecordDelimiter) {
        chrRecordDelimiter = pchrRecordDelimiter;
    }

    public void write(final String[] pstrCells) throws Exception {
        String strT = "";
        for (int i = 0; i < pstrCells.length; i++) {
            if (pstrCells[i] == null) {
                pstrCells[i] = "";
            } else {
                pstrCells[i] = adjustCsvValue(pstrCells[i]);
            }
            strT += pstrCells[i] + FIELD_DELIMITER;
        }
        if (bufWriter == null) {
            getWriter();
        }
        bufWriter.write(strT);
    }

    public void writeLine(final String[] pstrCells) throws Exception {
        write(pstrCells);
        this.writeLine("");
    }

    public String adjustCsvValue(final String pstrV) {
        String strT = pstrV;
        if (pstrV.indexOf(FIELD_DELIMITER) > 0) {
            strT = VALUE_DELIMITER + strT + VALUE_DELIMITER;
        }
        return strT;
    }

}