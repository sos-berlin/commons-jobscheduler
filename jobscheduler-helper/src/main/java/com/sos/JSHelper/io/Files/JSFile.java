package com.sos.JSHelper.io.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

import sos.util.SOSGZip;

import com.sos.JSHelper.Archiver.IJSArchiver;
import com.sos.JSHelper.Archiver.JSArchiver;
import com.sos.JSHelper.Archiver.JSArchiverOptions;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.DataElements.JSDataElementTimeStampISO;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

@I18NResourceBundle(baseName = "com_sos_JSHelper_Messages", defaultLocale = "en")
public class JSFile extends java.io.File implements JSListener, IJSArchiver {

    protected String strFileName;
    protected File fleFile;
    protected FileReader fleFileReader;
    protected BufferedReader bufReader;
    protected long lngNoOfLinesRead = 0;
    protected String strCharSet4OutputFile = null;
    protected String strCharSet4InputFile = null;
    protected BufferedWriter bufWriter = null;
    protected boolean flgIsAppendMode = false;
    private static final Logger LOGGER = Logger.getLogger(JSFile.class);
    private static final String ConDefaultExtension4BackupFile = ".bak";
    private static final long serialVersionUID = -1430552107244301112L;
    private final char[] charArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private FileWriter fleFileWriter = null;
    private StringBuffer strRecordBuffer;
    private String strPushBackBuffer = "";
    private long lngNoOfLinesWritten = 0;
    private long lngNoOfCharsInBuffer = 0;
    private RandomAccessFile randomFile = null;
    private FileLock fleLock = null;
    private boolean flgFileIsLocked = false;
    private boolean flgIsExclusive = false;
    private JSFile fleExclusiveFile = null;
    private String strExclusiveFootPrint = "";
    private boolean flgIsZipfile = false;
    private Messages Messages = null;
    private JSListener JSListener;
    private JSArchiver objArchiver = null;
    public static final String conPropertySOS_JSFILE_PREFIX_4_TEMPFILE = "sos.jsfile.prefix.4.tempfile";
    public static final String conPropertySOS_JSFILE_EXTENSION_4_TEMPFILE = "sos.jsfile.extension.4.tempfile";
    public static final String conPropertySOS_JSFILE_EXTENSION_4_EXCLUSIVEFILE = "sos.jsfile.extension.4.exclusivefile";
    public static final String conPropertySOS_JSFILE_EXTENSION_4_BACKUPFILE = "sos.jsfile.extension.4.backupfile";
    public SOSOptionFolderName BackupFolderName = new SOSOptionFolderName(null, "BackupFolderName", "Name of Folder for Backup of this file", "", "", false);
    final String JSH_E_0010 = "JSH_E_0010";
    final String JSH_E_0020 = "JSH_E_0020";
    final String JSH_E_0040 = "JSH_E_0040";
    final String JSH_E_0050 = "JSH_E_0050";
    final String JSH_E_0060 = "JSH_E_0060";
    final String JSH_E_0070 = "JSH_E_0070";
    final String JSH_E_0090 = "JSJ_E_0090";
    final String JSH_E_0140 = "JSH_E_0140";
    final String JSH_I_0010 = "JSH_I_0010";
    final String JSH_I_0020 = "JSH_I_0020";
    final String JSH_I_0030 = "JSH_I_0030";
    final String JSH_I_0040 = "JSH_I_0040";
    final String JSH_I_0060 = "JSH_I_0060";
    final String JSH_I_0070 = "JSH_I_0070";
    final String JSH_I_0080 = "JSH_I_0080";
    final String JSH_I_0090 = "JSH_I_0090";
    final String JSH_I_0100 = "JSH_I_0100";
    final String JSH_I_0110 = "JSH_I_0110";
    final String JSH_I_0120 = "JSH_I_0120";
    final String JSH_I_0130 = "JSH_I_0130";
    String JSH_I_0150 = "JSH_I_0150";

    public JSFile(final String pstrFileAndPathName) {
        super(pstrFileAndPathName.replace("file:/", ""));
        strFileName = pstrFileAndPathName;
        doInit();
    }

    public JSFile(final File parent, final String child) {
        super(parent, child);
        strFileName = getAbsolutePath();
        doInit();
    }

    public JSFile(final String parent, final String child) {
        super(parent, child);
        doInit();
    }

    public JSFile(final URI pobjUri) throws MalformedURLException {
        super(pobjUri);
        doInit();
        strFileName = pobjUri.toURL().getFile();
    }

    private void doInit() {
        try {
            strFileName = getAbsolutePath();
            fleFile = this;
            int i = strFileName.indexOf("//");
            if (i > 0) {
                if (strFileName.startsWith("local:")) {
                    strFileName = strFileName.replace("local:", "file:");
                }
                URL objURL = new URL(strFileName);
                strFileName = objURL.getFile();
                LOGGER.info(objURL.getFile());
                LOGGER.info(objURL.getPath());
            } else {
                strFileName = getAbsolutePath();
            }
            JSToolBox objT = new JSToolBox("com_sos_JSHelper_Messages");
            Messages = objT.getMessageObject();
        } catch (final Exception e) {
            LOGGER.error("doInit()", e);
        }
    }

    public String createZipFile(final String pstrPathName) {
        String gzipFilename = addFileSeparator(pstrPathName) + getName().concat(".gz");
        try {
            File gzipFile = new File(gzipFilename);
            SOSGZip.compressFile(this, gzipFile);
            gzipFile.setLastModified(this.lastModified());
            LOGGER.debug(String.format("file %1$s compressed to %2$s", this.getAbsolutePath(), gzipFilename));
        } catch (Exception e) {
            String strT = String.format("Error during compress for file %1$s", gzipFilename);
            LOGGER.error(strT);
            throw new JobSchedulerException(strT, e);
        }
        return gzipFilename;
    }

    public String addFileSeparator(final String str) {
        return str.endsWith("/") || str.endsWith("\\") ? str : str + "/";
    }

    public void setZipFile(final boolean pflgIsZipFile) {
        flgIsZipfile = pflgIsZipFile;
    }

    protected BufferedReader Reader() throws IOException {
        if (bufReader == null) {
            if (this.checkExclusiveDeny()) {
                throw new JobSchedulerException(Messages.getMsg(JSH_E_0070, strFileName, strExclusiveFootPrint));
            }
            if (randomFile != null) {
                fleFileReader = new FileReader(randomFile.getFD());
            } else {
                if (isZipFile()) {
                    GZIPInputStream objGZ = new GZIPInputStream(new FileInputStream(this));
                    if (strCharSet4InputFile != null) {
                        bufReader = new BufferedReader(new InputStreamReader(objGZ, strCharSet4InputFile));
                    } else {
                        bufReader = new BufferedReader(new InputStreamReader(objGZ));
                    }
                } else {
                    fleFileReader = new FileReader(this);
                    if (strCharSet4InputFile == null) {
                        bufReader = new BufferedReader(fleFileReader);
                    } else {
                        bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(this), strCharSet4InputFile));
                    }
                }
            }
            LOGGER.debug(Messages.getMsg(JSH_I_0090, strFileName, strCharSet4InputFile));
        }
        return bufReader;
    }

    public boolean isZipFile() {
        boolean flgRet = false;
        if (flgIsZipfile || strFileName.toLowerCase().endsWith(".gz") || strFileName.toLowerCase().endsWith(".zip")) {
            flgRet = true;
        }
        return flgRet;
    }

    public void CharSet4InputFile(final String pstrCharSet4InputFile) {
        strCharSet4InputFile = pstrCharSet4InputFile;
    }

    public String CharSet4InputFile() {
        return strCharSet4InputFile;
    }

    public String CopyTimeStamp() throws Exception {
        try {
            String strNewFileName = null;
            final String strTimeStamp = getTimeStamp();
            final int i = strFileName.lastIndexOf(".");
            if (i > 0) {
                strNewFileName = strFileName.substring(0, i) + "-" + strTimeStamp + strFileName.substring(i);
            } else {
                strNewFileName = strFileName + "-" + strTimeStamp;
            }
            copy(strNewFileName);
            return strNewFileName;
        } catch (final Exception e) {
            throw new JobSchedulerException("CopyTimeStamp Error", e);
        }
    }

    public String CreateBackup() {
        String strExtension4BackupFile = System.getProperty(conPropertySOS_JSFILE_EXTENSION_4_BACKUPFILE, ConDefaultExtension4BackupFile);
        String strR = this.doCreateBackUp(strExtension4BackupFile);
        return strR;
    }

    public String CreateBackup(final String pstrExtension4BackupFile) throws Exception {
        return this.doCreateBackUp(pstrExtension4BackupFile);
    }

    private String doCreateBackUp(final String pstrExtension4BackupFileName) {
        String strNewFileName = "";
        String strBackupFolderName = BackupFolderName.Value();
        if (!strBackupFolderName.isEmpty()) {
            strNewFileName = strBackupFolderName + this.getName() + pstrExtension4BackupFileName;
        } else {
            strNewFileName = strFileName + pstrExtension4BackupFileName;
        }
        try {
            copy(strNewFileName);
        } catch (Exception e) {
            throw new JobSchedulerException("problems with createbackup " + e.getMessage(), e);
        }
        LOGGER.debug(Messages.getMsg(JSH_I_0120, strNewFileName));
        return strNewFileName;
    }

    private String getTimeStamp() {
        final Date now = new Date();
        String strT = null;
        SimpleDateFormat formatter;
        final Locale currentLocale = new Locale("de", "DE");
        formatter = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss", currentLocale);
        strT = formatter.format(now);
        return strT;
    }

    public boolean isOlderThan(final long plngCompareTo) {
        boolean flgR = false;
        long interval = System.currentTimeMillis() - lastModified();
        if (interval > plngCompareTo) {
            flgR = true;
        }
        return flgR;
    }

    public JSFile move(final String pstrNewFileName) throws Exception {
        final JSFile RetVal = null;
        try {
            final File fleNewFile = new File(pstrNewFileName);
            if (fleNewFile.exists()) {
                LOGGER.debug(Messages.getMsg(JSH_I_0130, fleNewFile.getAbsoluteFile()));
                fleNewFile.delete();
            }
            copy(pstrNewFileName);
            try {
                delete();
                LOGGER.info(Messages.getMsg(JSH_I_0090, getAbsoluteFile()));
            } catch (final Exception e) {
                LOGGER.error(Messages.getMsg(JSH_E_0090, getName()));
                throw e;
            }
        } catch (final Exception e) {
            throw e;
        }
        return RetVal;
    }

    public void RenameTimeStamp() {
        //
    }

    public void MustExist() throws Exception {
        if (!exists()) {
            final String s = Messages.getMsg(JSH_E_0140, strFileName);
            LOGGER.debug(s);
            throw new Exception(s);
        }
    }

    public boolean getAppendMode() {
        return flgIsAppendMode;
    }

    public JSFile setAppendMode(final boolean pflgIsAppendMode) {
        flgIsAppendMode = pflgIsAppendMode;
        return this;
    }

    protected BufferedWriter Writer() throws IOException {
        if (bufWriter == null) {
            if (this.checkExclusiveDeny()) {
                throw new IOException(String.format(JSH_E_0070, strFileName, strExclusiveFootPrint));
            }
            fleFileWriter = new FileWriter(this, flgIsAppendMode);
            if (strCharSet4OutputFile == null) {
                if (isZipFile()) {
                    GZIPOutputStream objGZ = new GZIPOutputStream(new FileOutputStream(this));
                    if (strCharSet4OutputFile != null) {
                        bufWriter = new BufferedWriter(new OutputStreamWriter(objGZ, strCharSet4OutputFile));
                    } else {
                        bufWriter = new BufferedWriter(new OutputStreamWriter(objGZ));
                    }
                } else {
                    bufWriter = new BufferedWriter(fleFileWriter);
                    LOGGER.debug(Messages.getMsg(JSH_I_0060, strFileName));
                }
            } else {
                bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this), strCharSet4OutputFile));
                LOGGER.debug(Messages.getMsg(JSH_I_0070, strFileName, strCharSet4InputFile));
            }
        }
        return bufWriter;
    }

    public boolean isExclusive() {
        return flgIsExclusive;
    }

    public JSFile NewLine() throws Exception {
        WriteLine("");
        lngNoOfCharsInBuffer = 0;
        return this;
    }

    public long NoOfCharsInBuffer() {
        return lngNoOfCharsInBuffer;
    }

    public JSFile OutChar(final char pchrC) throws IOException {
        bufWriter.write(pchrC);
        lngNoOfCharsInBuffer++;
        return this;
    }

    public void OutString(final String pstrLine) throws Exception {
        if (bufWriter == null) {
            Writer();
        }
        bufWriter.write(pstrLine);
        lngNoOfCharsInBuffer += pstrLine.length();
    }

    public void Write(final String pstrLine) throws Exception {
        WriteLine(pstrLine);
    }

    public JSFile WriteLine(final String pstrLine) throws IOException {
        if (bufWriter == null) {
            Writer();
        }
        String strBuff = pstrLine + System.getProperty("line.separator");
        if (randomFile == null) {
            bufWriter.write(strBuff);
            bufWriter.flush();
        } else {
            randomFile.writeBytes(strBuff);
        }
        lngNoOfLinesWritten++;
        return this;
    }

    public void WriteLine() throws IOException, Exception {
        WriteLine("");
    }

    public StringBuffer GetLine() {
        String strT;
        StringBuffer strSB = new StringBuffer("");
        try {
            if (strPushBackBuffer.length() > 0) {
                strSB.append(strPushBackBuffer);
                strPushBackBuffer = "";
                strRecordBuffer = strSB;
            } else {
                if (bufReader == null) {
                    Reader();
                }
                strT = bufReader.readLine();
                if (strT != null) {
                    lngNoOfLinesRead++;
                    strSB = new StringBuffer(strT);
                    strRecordBuffer = strSB;
                } else {
                    bufReader.close();
                    bufReader = null;
                    strSB = null;
                }
            }
        } catch (final Exception e) {
            LOGGER.error("GetLine() " + e.getMessage(), e);
            strSB = null;
        }
        return strSB;
    }

    public void PushBack() {
        strPushBackBuffer = strRecordBuffer.toString();
    }

    public void copy(final String pstrTargetFileName) throws Exception {
        final String conMethodName = "JSFile::copy";
        final File infile = new File(strFileName);
        if (!infile.getAbsoluteFile().exists()) {
            SignalError(String.format("%1$s: File '%2$s' not exist. copy not possible", conMethodName, strFileName));
            return;
        }
        final File outfile = new File(pstrTargetFileName);
        if (infile.getCanonicalPath().equals(outfile.getCanonicalPath())) {
            message(String.format("%3$s: File '%1$s' is equal to '%2$s'. copy is useless", strFileName, pstrTargetFileName, conMethodName));
            return;
        }
        try {
            final FileOutputStream out = new FileOutputStream(outfile);
            try {
                final FileInputStream in = new FileInputStream(infile);
                ExecuteCopy(in, out);
            } catch (final FileNotFoundException e) {
                SignalError(String.format("%1$s: File '%2$s' not exist. can not copy to '%3$s'", conMethodName, strFileName, pstrTargetFileName));
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            SignalError(Messages.getMsg(JSH_E_0060, conMethodName, getName(), pstrTargetFileName));
        }
        LOGGER.debug(Messages.getMsg(JSH_I_0080, strFileName, pstrTargetFileName, conMethodName));
    }

    public void ReplaceWith(final String strReplaceWhatAsRegEx, final String strReplaceWith) throws Exception {
        final String conMethodName = "JSFile::ReplaceWith";
        final File infile = fleFile;
        if (!infile.exists()) {
            SignalError(String.format("%1$s: File '%2$s' not exist. nothing to do", conMethodName, strFileName));
            return;
        }
        final File fleTempfile = java.io.File.createTempFile("JSFile", ".tmp");
        try {
            FileInputStream in = new FileInputStream(infile);
            FileOutputStream out = new FileOutputStream(fleTempfile);
            ExecuteReplace(in, out, strReplaceWhatAsRegEx, strReplaceWith);
            fleFile.delete();
            in = new FileInputStream(fleTempfile);
            out = new FileOutputStream(infile);
            ExecuteCopy(in, out);
            fleTempfile.delete();
        } catch (final FileNotFoundException e) {
            SignalError(String.format("%1$s: File '%2$s' not exist. nothing to do", conMethodName, strFileName));
        } catch (final Exception e) {
            SignalError(String.format("%1$s: replace not succesfull for file '%2$s' ", conMethodName, getName()));
        }
        message(String.format("%1$s: Replacing in File '%2$s' done ", conMethodName, strFileName));
    }

    private void ExecuteReplace(final FileInputStream in, final FileOutputStream out, final String strReplaceWhatAsRegEx, final String strReplaceWith)
            throws IOException {
        final int intBuffsize = 4096;
        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[intBuffsize];
                while (true) {
                    final int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    if (bytesRead < intBuffsize) {
                        byte[] tmpB = new byte[bytesRead];
                        tmpB = buffer;
                        buffer = new byte[bytesRead];
                        for (int i = 0; i < bytesRead; i++) {
                            buffer[i] = tmpB[i];
                        }
                    }
                    String strLine = new String(buffer);
                    strLine = strLine.replaceAll(strReplaceWhatAsRegEx, strReplaceWith);
                    byte[] outB = new byte[intBuffsize + strReplaceWith.length()];
                    outB = strLine.getBytes();
                    out.write(outB, 0, strLine.length());
                }
            }
        }
        in.close();
        out.close();
    }

    private long in2out(final FileInputStream in, final FileOutputStream out) throws IOException {
        long lngBytesCopies = 0;
        synchronized (in) {
            synchronized (out) {
                final byte[] buffer = new byte[256];
                while (true) {
                    final int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                    lngBytesCopies += bytesRead;
                }
            }
        }
        return lngBytesCopies;
    }

    private long ExecuteCopyWOClose(final FileInputStream in, final FileOutputStream out) throws IOException {
        return in2out(in, out);
    }

    private long ExecuteCopy(final FileInputStream in, final FileOutputStream out) throws IOException {
        final long lngBytesCopied = in2out(in, out);
        in.close();
        out.close();
        return lngBytesCopied;
    }

    private void ExecuteCopy(final FileInputStream in, final PrintStream out) throws IOException {
        synchronized (in) {
            synchronized (out) {
                final byte[] buffer = new byte[1024];
                while (true) {
                    final int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
        in.close();
    }

    public String File2String() {
        final String strT = getContent();
        return strT;
    }

    public String getContent() {
        String strB = "";
        BufferedReader fin;
        try {
            fin = this.Reader();
        } catch (IOException e1) {
            throw new JobSchedulerException(e1.getMessage(), e1);
        }
        int lngFileSize = 0;
        try {
            final char[] buffer = new char[4096];
            while (true) {
                final int bytesRead = fin.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                lngFileSize += bytesRead;
                strB = strB + new String(buffer);
            }
        } catch (final IOException e) {
            LOGGER.error("getContent() - " + e.getMessage(), e);
            throw new JobSchedulerException("Error in JSFile", e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                    fin = null;
                }
            } catch (final IOException e) {
                fin = null;
            }
        }
        final String strT = strB.substring(0, lngFileSize);
        return strT;
    }

    public void dumpAscii(final PrintStream out) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(strFileName);
            ExecuteCopy(fin, out);
        } catch (final IOException e) {
            LOGGER.error("dumpAscii(PrintStream) - " + e.getMessage(), e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (final IOException e) {
            }
        }
    }

    public void dumpAscii(final FileOutputStream out) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(strFileName);
            ExecuteCopy(fin, out);
        } catch (final IOException e) {
            LOGGER.error("dumpAscii(FileOutputStream) - " + e.getMessage(), e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (final IOException e) {
            }
        }
    }

    public String byte2String(final byte[] content) {
        final StringBuilder buffer = new StringBuilder(content.length * 2);
        for (final byte element : content) {
            buffer.append(charArray[(0xF0 & element) >> 4]);
            buffer.append(charArray[0xF & element]);
        }
        return buffer.toString();
    }

    public void dumpDecimal(final String filename) {
        FileInputStream fin = null;
        final byte[] buffer = new byte[16];
        boolean end = false;
        int bytesRead;
        try {
            fin = new FileInputStream(filename);
            while (!end) {
                bytesRead = 0;
                while (bytesRead < buffer.length) {
                    final int r = fin.read(buffer, bytesRead, buffer.length - bytesRead);
                    if (r == -1) {
                        end = true;
                        break;
                    }
                    bytesRead += r;
                }
                for (int i = 0; i < bytesRead; i++) {
                    int dec = buffer[i];
                    if (dec < 0) {
                        dec = 256 + dec;
                    }
                    if (dec < 10) {
                        LOGGER.debug("dumpDecimal(String) - 00" + dec + " ");
                    } else if (dec < 100) {
                        LOGGER.debug("dumpDecimal(String) - 0" + dec + " ");
                    } else {
                        LOGGER.debug("dumpDecimal(String) - " + dec + " ");
                    }
                }
                LOGGER.debug("dumpDecimal(String)");
            }
        } catch (final IOException e) {
            LOGGER.error("dumpDecimal(String) - " + e.getMessage(), e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                }
            } catch (final IOException e) {
            }
        }
    }

    public void dumpHex(final PrintStream out) {
        BufferedReader fin = null;
        final char[] buffer = new char[24];
        boolean end = false;
        int bytesRead;
        try {
            fin = Reader();
            while (!end) {
                bytesRead = 0;
                while (bytesRead < buffer.length) {
                    final int r = fin.read(buffer, bytesRead, buffer.length - bytesRead);
                    if (r == -1) {
                        end = true;
                        break;
                    }
                    bytesRead += r;
                }
                for (int i = 0; i < bytesRead; i++) {
                    int hex = buffer[i];
                    if (hex < 0) {
                        hex = 256 + hex;
                    }
                    if (hex >= 16) {
                        out.print(Integer.toHexString(hex) + " ");
                    } else {
                        out.print("0" + Integer.toHexString(hex) + " ");
                    }
                }
                out.println();
            }
        } catch (final IOException e) {
            LOGGER.error("dumpHex(PrintStream) - " + e.getMessage(), e);
        } finally {
            try {
                if (fin != null) {
                    fin.close();
                    fin = null;
                }
            } catch (final IOException e) {
            }
        }
    }

    @Override
    public void message(final String pstrMsg) {
        if (JSListener != null) {
            JSListener.message(pstrMsg);
        } else {
            LOGGER.debug("message(String) - " + pstrMsg);
        }
    }

    @Deprecated
    public void registerMessageListener(final JSListener l) {
        JSListener = l;
    }

    public void SignalAbort(final String strS) {
        String strT = " ###ProgramAbort### ";
        strT = strT + strS + strS;
        LOGGER.fatal(strT);
        throw new JobSchedulerException(strT);
    }

    public void SignalInfo(final String strS) {
        LOGGER.info(strS);
    }

    public void SignalError(final String strS) {
        String strT = " ### Error ### ";
        strT = strT + strS + strS;
        LOGGER.error(strT);
        throw new JobSchedulerException(strT);
    }

    @Override
    public JSArchiver getArchiver() throws Exception {
        if (objArchiver == null) {
            objArchiver = new JSArchiver();
            objArchiver.registerMessageListener(this);
            objArchiver.Options().FileName(super.getAbsoluteFile().toString());
        }
        return objArchiver;
    }

    public JSArchiver getArchiver(final JSArchiverOptions pobjArchiverOptions) throws Exception {
        if (objArchiver == null) {
            objArchiver = new JSArchiver();
            objArchiver.registerMessageListener(this);
            pobjArchiverOptions.FileName(super.getAbsoluteFile().toString());
            objArchiver.Options(pobjArchiverOptions);
        }
        return objArchiver;
    }

    public void close() throws IOException {
        final String conMethodName = "JSFile::close";
        if (bufWriter != null) {
            bufWriter.flush();
            bufWriter.close();
            bufWriter = null;
            final NumberFormat fmt = NumberFormat.getInstance();
            fmt.setGroupingUsed(true);
            final String strT = Messages.getMsg(JSH_I_0020, fleFile.getAbsoluteFile(), fmt.format(fleFile.length()), conMethodName);
            LOGGER.debug(strT);
            LOGGER.debug(Messages.getMsg(JSH_I_0030, strFileName, lngNoOfLinesWritten, conMethodName));
        } else {
            if (bufReader != null) {
                bufReader.close();
                bufReader = null;
                LOGGER.debug(Messages.getMsg(JSH_I_0040, strFileName, lngNoOfLinesRead, conMethodName));
            }
        }
        this.doUnlock();
        this.doReleaseExclusive();
    }

    public static String createTempFileName() {
        String strTempFileNameExtension = System.getProperty(conPropertySOS_JSFILE_EXTENSION_4_TEMPFILE, ".tmp");
        String strTempFileNamePrefix = System.getProperty(conPropertySOS_JSFILE_PREFIX_4_TEMPFILE, "SOS_");
        String strTempFileName = null;
        try {
            File objF = File.createTempFile(strTempFileNamePrefix, strTempFileNameExtension);
            objF.deleteOnExit();
            strTempFileName = objF.getAbsoluteFile().getAbsolutePath();
            strTempFileName = strTempFileName.replaceAll("\\\\", "/");
        } catch (IOException e) {
            //
        }
        return strTempFileName;
    }

    public static JSFile createTempFile() {
        String strTempFileName = createTempFileName();
        JSFile tempFile = new JSFile(strTempFileName);
        tempFile.deleteOnExit();
        return tempFile;
    }

    public long AppendFile(final String pstrFileName) throws Exception {
        final String conMethodName = "JSFile::AppendFile";
        long lngBytesWritten = 0;
        FileInputStream in = null;
        FileOutputStream out = null;
        final String strFileN = getAbsolutePath();
        final File objFile2Append = new File(strFileN);
        if (!objFile2Append.getAbsoluteFile().exists()) {
            SignalError(Messages.getMsg(JSH_E_0010, conMethodName, strFileN));
            return -1;
        }
        JSFile tempFile = createTempFile();
        try {
            out = new FileOutputStream(tempFile);
            try {
                final File AFile = new File(pstrFileName);
                if (!AFile.getAbsoluteFile().exists()) {
                    SignalError(Messages.getMsg(JSH_E_0010, conMethodName, pstrFileName));
                    tempFile.delete();
                    return -1;
                }
                if (AFile.getAbsoluteFile().getAbsolutePath().equalsIgnoreCase(strFileN)) {
                    SignalError(Messages.getMsg(JSH_E_0020, conMethodName, strFileN, pstrFileName));
                    tempFile.delete();
                    return -1;
                }
                in = new FileInputStream(objFile2Append);
                lngBytesWritten += ExecuteCopyWOClose(in, out);
                in.close();
                in = new FileInputStream(AFile);
                lngBytesWritten += ExecuteCopyWOClose(in, out);
                in.close();
                out.close();
                message(Messages.getMsg(JSH_I_0010, conMethodName, pstrFileName, strFileN));
                objFile2Append.delete();
                tempFile.renameTo(objFile2Append);
                tempFile = null;
                return lngBytesWritten;
            } catch (final FileNotFoundException e) {
                SignalError(Messages.getMsg(JSH_E_0040, conMethodName, pstrFileName, strFileN));
            } catch (final Exception e) {
                if (in != null) {
                    in.close();
                }
                out.close();
                throw e;
            }
        } catch (final Exception e) {
            LOGGER.error("AppendFile(String) " + e.getMessage(), e);
            SignalError(Messages.getMsg(JSH_E_0050, conMethodName, getName(), pstrFileName));
        } finally {
            if (tempFile != null) {
                tempFile.delete();
                tempFile = null;
            }
        }
        return lngBytesWritten;
    }

    public JSFile Send2FtpServer(final HashMap<String, String> settings) throws Exception {
        return this;
    }

    public boolean compare(final JSFile pfleFile) throws Exception {
        final BufferedReader objReader1 = Reader();
        final BufferedReader objReader2 = pfleFile.Reader();
        int char1, char2;
        if (pfleFile.length() != length()) {
            return false;
        }
        while (true) {
            char1 = objReader1.read();
            char2 = objReader2.read();
            if (char1 != char2) {
                return false;
            }
            if (char1 == -1 && char2 == -1) {
                return true;
            }
            if (char1 == -1 || char2 == -1) {
                return false;
            }
        }
    }

    public boolean compare(final String pstrFile2Compare) throws IOException, Exception {
        return this.compare(new JSFile(pstrFile2Compare));
    }

    public void Write(final StringBuffer pstrLine) throws Exception {
        this.Write(pstrLine.toString());
    }

    public boolean doLock(final String pstrAccessMode) throws Exception {
        randomFile = new RandomAccessFile(this, pstrAccessMode);
        FileChannel channel = randomFile.getChannel();
        FileLock fleLock = null;
        try {
            fleLock = channel.tryLock();
        } catch (OverlappingFileLockException e) {
            flgFileIsLocked = true;
        } catch (Exception e) {
            LOGGER.error("doLock(String) " + e.getMessage(), e);
            throw new JobSchedulerException("doLock(String) " + e.getMessage(), e);
        }
        if (fleLock == null) {
            flgFileIsLocked = false;
            randomFile = null;
        } else {
            flgFileIsLocked = true;
        }
        if (flgFileIsLocked) {
            String JSH_I_0160 = "JSH_I_0160";
            message(String.format(JSH_I_0160, strFileName));
        }
        return flgFileIsLocked;
    }

    public boolean doLock() throws Exception {
        return this.doLock("rw");
    }

    public boolean doUnlock() throws IOException {
        if (randomFile != null) {
            try {
                if (fleLock != null) {
                    fleLock.release();
                    fleLock = null;
                }
                randomFile.getChannel().close();
            } catch (Exception e) {
            }
            randomFile = null;
            flgFileIsLocked = false;
            message(String.format(JSH_I_0150, strFileName));
        }
        return true;
    }

    public boolean isLocked() {
        boolean flgIsLocked = false;
        if (randomFile != null) {
            flgIsLocked = flgFileIsLocked;
        }
        return flgIsLocked;
    }

    public boolean setExclusive(final boolean pflgIsExclusive) throws IOException {
        if (flgIsExclusive && !pflgIsExclusive) {
            this.doReleaseExclusive();
            return flgIsExclusive;
        }
        flgIsExclusive = pflgIsExclusive;
        if (flgIsExclusive) {
            if (!this.checkExclusiveDeny()) {
                InetAddress ia = InetAddress.getLocalHost();
                String strUserName = System.getProperty("user.name");
                JSDataElementTimeStampISO tstIso = new JSDataElementTimeStampISO(new JSDataElementDate(new Date()));
                String strValues = strFileName + ";" + strUserName + ";" + tstIso.FormattedValue() + ";" + ia.getHostAddress();
                fleExclusiveFile.WriteLine(strValues);
                LOGGER.debug(Messages.getMsg(JSH_I_0110, strValues));
                fleExclusiveFile.close();
            }
        }
        return flgIsExclusive;
    }

    private boolean checkExclusiveDeny() throws IOException {
        final String conMethodName = "JSFile::checkExclusiveDeny";
        boolean flgExclusiveDeny = false;
        String strExclusiveFileNameExtension = System.getProperty(conPropertySOS_JSFILE_EXTENSION_4_EXCLUSIVEFILE, "~");
        fleExclusiveFile = new JSFile(strFileName + strExclusiveFileNameExtension);
        if (fleExclusiveFile.exists()) {
            strExclusiveFootPrint = fleExclusiveFile.File2String();
            if (!strExclusiveFootPrint.isEmpty()) {
                message(String.format("%1$s - Footprint is %2$s", conMethodName, strExclusiveFootPrint));
                String[] strP = strExclusiveFootPrint.split(";");
                if (!strP[1].equalsIgnoreCase(System.getProperty("user.name"))) {
                    fleExclusiveFile.close();
                    fleExclusiveFile = null;
                    flgExclusiveDeny = true;
                }
            }
        }
        return flgExclusiveDeny;
    }

    private void doReleaseExclusive() {
        flgIsExclusive = false;
        if (fleExclusiveFile != null) {
            fleExclusiveFile.delete();
            fleExclusiveFile = null;
        }
    }

    public String toXml() {
        String strXml = String.format("<file name='%1$s' size='%2$d' modificationdate='%3$s' />", this.getAbsolutePath(), fleFile.length(), new Date(fleFile.lastModified()));
        return strXml;
    }

    public static final String getTempdir() {
        return System.getProperty("java.io.tmpdir");
    }

    public String getUniqueFileName() {
        String strUniqueFileName = strFileName;
        String strE = getFileExtensionName();
        String strF = getName();
        if (this.exists()) {
            for (int i = 2;; i++) {
                String strN = "(" + String.valueOf(i) + ")";
                if (strE.isEmpty()) {
                    strUniqueFileName = strFileName + strN;
                } else {
                    strUniqueFileName = strFileName.replaceFirst(strE, strN + strE);
                }
                if (!new File(strUniqueFileName).exists()) {
                    break;
                }
            }
        }
        return strUniqueFileName;
    }

    public String removeFileNameExtension() {
        String filename = strFileName;
        int lastSeparatorIndex = strFileName.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            filename = strFileName.substring(lastSeparatorIndex + 1);
        }
        int extensionIndex = filename.lastIndexOf(".");
        if (extensionIndex != -1) {
            filename = filename.substring(0, extensionIndex);
        }
        return filename;
    }

    public String getFileExtensionName() {
        String strRet = "";
        int intIdx = strFileName.lastIndexOf(".");
        if (intIdx == -1) {
            return "";
        } else {
            int intFL = strFileName.length();
            int intExtensionLength = strFileName.length() - intIdx;
            strRet = strFileName.substring(intFL - intExtensionLength, intFL);
        }
        return strRet;
    }

    public String getCharSet4OutputFile() {
        return strCharSet4OutputFile;
    }

    public void setCharSet4OutputFile(final String strCharSet4OutputFile) {
        this.strCharSet4OutputFile = strCharSet4OutputFile;
    }

    public String getCharSet4InputFile() {
        return strCharSet4InputFile;
    }

    public void setCharSet4InputFile(final String strCharSet4InputFile) {
        this.strCharSet4InputFile = strCharSet4InputFile;
    }

}
