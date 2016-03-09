package com.sos.JSHelper.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.localization.Messages;

/** This class contains methods for several file operations
 * 
 * @author Florian Schreiber <fs@sos-berlin.com>
 * @since 2006-10-10
 * @version $Id$ */
public class SOSFileSystemOperations {

    private final Logger logger = Logger.getLogger(this.getClass());
    public Vector<File> lstResultList = null;
    /** copyFile and renameFile create the target directory if it does not yet
     * exist. */
    public static final int CREATE_DIR = 0x01;
    /** Disables error messages caused by specified (source and/or target) but
     * not existing files. */
    public static final int GRACIOUS = 0x02;
    /** Denies overwriting of already existing files.<br/>
     * By default files are overwritten. */
    public static final int NOT_OVERWRITE = 0x04;
    /** Enables recursive processing of all subdirectories.<br/>
     * It only Takes effect if the method's specified source is a directory. */
    public static final int RECURSIVE = 0x08;
    /** Forces removeFile to include directories for removal. */
    public static final int REMOVE_DIR = 0x10;
    /** Completely overwrites data when deleting files. */
    public static final int WIPE = 0x20;
    // bildet von diversen bool-aeqivalenten Strings auf "true" bzw. "false" ab
    private static Hashtable<String, String> BOOL = new Hashtable<String, String>();
    static {
        SOSFileSystemOperations.BOOL.put("true", "true");
        SOSFileSystemOperations.BOOL.put("false", "false");
        SOSFileSystemOperations.BOOL.put("j", "true");
        SOSFileSystemOperations.BOOL.put("ja", "true");
        SOSFileSystemOperations.BOOL.put("y", "true");
        SOSFileSystemOperations.BOOL.put("yes", "true");
        SOSFileSystemOperations.BOOL.put("n", "false");
        SOSFileSystemOperations.BOOL.put("nein", "false");
        SOSFileSystemOperations.BOOL.put("no", "false");
        SOSFileSystemOperations.BOOL.put("on", "true");
        SOSFileSystemOperations.BOOL.put("off", "false");
        SOSFileSystemOperations.BOOL.put("1", "true");
        SOSFileSystemOperations.BOOL.put("0", "false");
        SOSFileSystemOperations.BOOL.put("all", "true");
        SOSFileSystemOperations.BOOL.put("none", "false");
    }
    public Messages Messages = null;

    /** Maps the strings "true","j","ja","y","yes","on","1" and "all" to true.<br/>
     * Maps the strings "false","n","nein","no","off","0" and "none" to false.<br/>
     * The mapping is case insensitive, e.g. the string <em>"nEIN"</em> is
     * mapped to <em>false</em>.<br/>
     * If the value cannot be evaluated to a boolean then an exception is
     * thrown.
     * 
     * @param value
     * @return true oder false */
    public boolean toBoolean(final String value) throws Exception {
        try {
            if (value == null)
                throw new JobSchedulerException("null");
            String v = value.toLowerCase();
            String bool = SOSFileSystemOperations.BOOL.get(v);
            if (bool == null)
                throw new JobSchedulerException("\"" + value + "\"");
            return bool.equals("true");
        } catch (Exception e) {
            throw new JobSchedulerException("cannot evaluate to boolean: " + e.getMessage());
        }
    }

    /** Checks wether files can be written.
     * 
     * @param file file or directory
     * @param logger1 Object
     * @return true if file can be written or false if not
     * @throws Exception */
    public boolean canWrite(final String file, final Object objDummy1) throws Exception {
        File filename = new File(file);
        return canWrite(filename, null, 0, objDummy1);
    }

    /** Checks wether files can be written. if file is a directory and
     * <em>fileSpec</em> is not NULL <em>fileSpec</em> is applied for matching
     * files in the directory.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param logger Object
     * @return true if file can be written or false if not
     * @throws Exception */
    public boolean canWrite(final String file, final String fileSpec, final Object objDummy) throws Exception {
        File filename = new File(file);
        return canWrite(filename, fileSpec, Pattern.CASE_INSENSITIVE, objDummy);
    }

    /** Checks wether files can be written. if file is a directory and
     * <em>fileSpec</em> is not NULL <em>fileSpec</em> is applied for matching
     * files in the directory.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file can be written or false if not
     * @throws Exception */
    public boolean canWrite(final String file, final String fileSpec, final int fileSpecFlags, final Object objDummy) throws Exception {
        File filename = new File(file);
        return canWrite(filename, fileSpec, fileSpecFlags, logger);
    }

    /** Checks wether file can be written
     * 
     * @param file file or directory
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean canWrite(final File file, final Object objDummy) throws Exception {
        return canWrite(file, null, 0, logger);
    }

    /** Checks wether file can be written if file is a directory and
     * <em>fileSpec</em> is not NULL <em>fileSpec</em> is applied for matching
     * files in the directory.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean canWrite(final File file, final String fileSpec, final Object objDummy) throws Exception {
        return canWrite(file, fileSpec, Pattern.CASE_INSENSITIVE, logger);
    }

    /** Checks wether file can be written if file is a directory and
     * <em>fileSpec</em> is not NULL <em>fileSpec</em> is applied for matching
     * files in the directory.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean canWrite(File file, final String fileSpec, final int fileSpecFlags, final Object objDummy) throws Exception {
        try {
            log_debug1("arguments for canWrite:", logger);
            log_debug1("argument file=" + file.toString(), logger);
            log_debug1("argument fileSpec=" + fileSpec, logger);
            String msg = "";
            if (has(fileSpecFlags, Pattern.CANON_EQ))
                msg += "CANON_EQ";
            if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE))
                msg += "CASE_INSENSITIVE";
            if (has(fileSpecFlags, Pattern.COMMENTS))
                msg += "COMMENTS";
            if (has(fileSpecFlags, Pattern.DOTALL))
                msg += "DOTALL";
            if (has(fileSpecFlags, Pattern.MULTILINE))
                msg += "MULTILINE";
            if (has(fileSpecFlags, Pattern.UNICODE_CASE))
                msg += "UNICODE_CASE";
            if (has(fileSpecFlags, Pattern.UNIX_LINES))
                msg += "UNIX_LINES";
            log_debug1("argument fileSpecFlags=" + msg, logger);
            String filename = file.getPath();
            filename = substituteAllDate(filename);
            // should any opening and closing brackets be found in the file
            // name, then this is an error
            Matcher m = Pattern.compile("\\[[^]]*\\]").matcher(filename);
            if (m.find())
                throw new JobSchedulerException("unsupported file mask found: " + m.group());
            file = new File(filename);
            if (!file.exists()) {
                log("checking file " + file.getAbsolutePath() + ": no such file or directory", logger);
                return true;
            } else {
                // Es ist eine Datei und sie existiert
                if (!file.isDirectory()) {
                    log(String.format("checking the file '%1$s' :: file exists", file.getCanonicalPath()), logger);
                    // log(Messages.getMsg("checking the file '%1$s' :: file exists",
                    // file.getCanonicalPath() ), logger);
                    // Versuch zu schreiben
                    boolean writable = false;
                    try {
                        RandomAccessFile f = new RandomAccessFile(file.getAbsolutePath(), "rw");
                        f.close();
                        writable = true;
                    } catch (Exception e) {
                    }
                    if (!writable) {
                        log("file " + file.getCanonicalPath() + ": cannot be written ", logger);
                        return false;
                    } else {
                        return true;
                    }
                    // Es ist ein Verzeichnis und es existiert
                } else {
                    // wenn kein fileSpec angegeben wurde, reicht die simple
                    // Existenz aus
                    if (fileSpec == null || fileSpec.length() == 0) {
                        log("checking file " + file.getCanonicalPath() + ": directory exists", logger);
                        return true;
                    }
                    // Ein Dateifilter wurde angegeben
                    Vector<File> fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, false, 0, 0, -1, -1, 0, 0);
                    if (fileList.isEmpty()) {
                        log("checking file " + file.getCanonicalPath() + ": directory contains no files matching " + fileSpec, logger);
                        return false;
                    } else {
                        log("checking file " + file.getCanonicalPath() + ": directory contains " + fileList.size() + " file(s) matching " + fileSpec, logger);
                        for (int i = 0; i < fileList.size(); i++) {
                            File checkFile = fileList.get(i);
                            log("found " + checkFile.getCanonicalPath(), logger);
                            // Versuch zu schreiben
                            boolean writable = false;
                            try {
                                RandomAccessFile f = new RandomAccessFile(file.getAbsolutePath(), "rw");
                                f.close();
                                writable = true;
                            } catch (Exception e) {
                            }
                            if (!writable) {
                                log("file " + checkFile.getCanonicalPath() + ": cannot be written ", logger);
                                return false;
                            }
                        }
                        lstResultList = fileList;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException("error checking file: " + file.toString() + ": " + e.getMessage());
        }
    }

    /** Checks for existence of file or directory.
     * 
     * @param file file or directory
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final String file, final Object objDummy) throws Exception {
        File filename = new File(file);
        return existsFile(filename, null, 0, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final String file, final String fileSpec, final Object objDummy) throws Exception {
        File filename = new File(file);
        return existsFile(filename, fileSpec, Pattern.CASE_INSENSITIVE, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final String file, final String fileSpec, final int fileSpecFlags, final Object objDummy) throws Exception {
        File filename = new File(file);
        return existsFile(filename, fileSpec, fileSpecFlags, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param minFileSize Filter for file size: smaller files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param maxFileSize Filter for file size: greater files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param skipFirstFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final String file, final String fileSpec, final int fileSpecFlags, final String minFileAge, final String maxFileAge,
            final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        File filename = new File(file);
        return existsFile(filename, fileSpec, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param minFileSize Filter for file size: smaller files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param maxFileSize Filter for file size: greater files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param skipFirstFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final String file, final String fileSpec, final int fileSpecFlags, final String minFileAge, final String maxFileAge,
            final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, final int minNumOfFiles,
            final int maxNumOfFiles, final Object objDummy) throws Exception {
        File filename = new File(file);
        return existsFile(filename, fileSpec, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, minNumOfFiles, maxNumOfFiles, logger);
    }

    /** Checks for existence of file or directory.
     * 
     * @param file file or directory
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final File file, final Object objDummy) throws Exception {
        return existsFile(file, null, 0, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final File file, final String fileSpec, final Object objDummy) throws Exception {
        return existsFile(file, fileSpec, Pattern.CASE_INSENSITIVE, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final File file, final String fileSpec, final int fileSpecFlags, final Object objDummy) throws Exception {
        return existsFile(file, fileSpec, fileSpecFlags, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param minFileSize Filter for file size: smaller files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param maxFileSize Filter for file size: greater files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param skipFirstFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(final File file, final String fileSpec, final int fileSpecFlags, final String minFileAge, final String maxFileAge,
            final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        return existsFile(file, fileSpec, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, -1, -1, logger);
    }

    /** Checks for file existence. if file is a directory and <em>fileSpec</em>
     * is not NULL <em>fileSpec</em> is applied for matching files in the
     * directory. In this case true will only be returned if at least one file
     * was matched.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are considered as non existing. Possible values: sec,
     *            hh:mm, hh:mm:sec The Resulting set is sorted by file age in
     *            ascending order (most recent first)
     * @param minFileSize Filter for file size: smaller files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param maxFileSize Filter for file size: greater files are considered as
     *            non existing. Possible values: number (bytes), numberKB,
     *            numberMB, numberGB (KB, MB, GB case insensitive) The Resulting
     *            set is sorted by file size in ascending order (smallest
     *            first). If the set is additionally filtered by file age the
     *            set is sorted by file age.
     * @param skipFirstFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of noticed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return true if file exists or false if not
     * @throws Exception */
    public boolean existsFile(File file, final String fileSpec, final int fileSpecFlags, final String minFileAge, final String maxFileAge,
            final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, final int minNumOfFiles,
            final int maxNumOfFiles, final Object objDummy) throws Exception {
        long minAge = 0;
        long maxAge = 0;
        long minSize = -1;
        long maxSize = -1;
        try {
            log_debug1("arguments for existsFile:", logger);
            log_debug1("argument file=" + file.toString(), logger);
            log_debug1("argument fileSpec=" + fileSpec, logger);
            String msg = "";
            if (has(fileSpecFlags, Pattern.CANON_EQ))
                msg += "CANON_EQ";
            if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE))
                msg += "CASE_INSENSITIVE";
            if (has(fileSpecFlags, Pattern.COMMENTS))
                msg += "COMMENTS";
            if (has(fileSpecFlags, Pattern.DOTALL))
                msg += "DOTALL";
            if (has(fileSpecFlags, Pattern.MULTILINE))
                msg += "MULTILINE";
            if (has(fileSpecFlags, Pattern.UNICODE_CASE))
                msg += "UNICODE_CASE";
            if (has(fileSpecFlags, Pattern.UNIX_LINES))
                msg += "UNIX_LINES";
            log_debug1("argument fileSpecFlags=" + msg, logger);
            log_debug1("argument minFileAge=" + minFileAge, logger);
            log_debug1("argument maxFileAge=" + maxFileAge, logger);
            minAge = calculateFileAge(minFileAge);
            maxAge = calculateFileAge(maxFileAge);
            log_debug1("argument minFileSize=" + minFileSize, logger);
            log_debug1("argument maxFileSize=" + maxFileSize, logger);
            minSize = calculateFileSize(minFileSize);
            maxSize = calculateFileSize(maxFileSize);
            log_debug1("argument skipFirstFiles=" + skipFirstFiles, logger);
            log_debug1("argument skipLastFiles=" + skipLastFiles, logger);
            log_debug1("argument minNumOfFiles=" + minNumOfFiles, logger);
            log_debug1("argument maxNumOfFiles=" + maxNumOfFiles, logger);
            if (skipFirstFiles < 0) {
                throw new JobSchedulerException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
            }
            if (skipLastFiles < 0) {
                throw new JobSchedulerException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
            }
            if (skipFirstFiles > 0 && skipLastFiles > 0)
                throw new JobSchedulerException("skip only either first files or last files");
            if (skipFirstFiles > 0 || skipLastFiles > 0) {
                if (minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1)
                    throw new JobSchedulerException("missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
            }
            String filename = file.getPath();
            filename = substituteAllDate(filename);
            // should any opening and closing brackets be found in the file
            // name, then this is an error
            Matcher m = Pattern.compile("\\[[^]]*\\]").matcher(filename);
            if (m.find())
                throw new JobSchedulerException("unsupported file mask found: " + m.group());
            file = new File(filename);
            if (!file.exists()) {
                log("checking file " + file.getAbsolutePath() + ": no such file or directory", logger);
                return false;
            } else {
                if (!file.isDirectory()) { // Es ist eine Datei und sie
                                           // existiert
                    log("checking file " + file.getCanonicalPath() + ": file exists", logger);
                    long currentTime = System.currentTimeMillis();
                    if (minAge > 0) {
                        long interval = currentTime - file.lastModified();
                        if (interval < 0)
                            throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath() + "] was modified in the future.");
                        if (interval < minAge) {
                            log("checking file age " + file.lastModified() + ": minimum age required is " + minAge, logger);
                            return false;
                        }
                    }
                    if (maxAge > 0) {
                        long interval = currentTime - file.lastModified();
                        if (interval < 0)
                            throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath() + "] was modified in the future.");
                        if (interval > maxAge) {
                            log("checking file age " + file.lastModified() + ": maximum age required is " + maxAge, logger);
                            return false;
                        }
                    }
                    if (minSize > -1 && minSize > file.length()) {
                        log("checking file size " + file.length() + ": minimum size required is " + minFileSize, logger);
                        return false;
                    }
                    if (maxSize > -1 && maxSize < file.length()) {
                        log("checking file size " + file.length() + ": maximum size required is " + maxFileSize, logger);
                        return false;
                    }
                    if (skipFirstFiles > 0 || skipLastFiles > 0) {
                        log("file skipped", logger);
                        return false;
                    }
                    lstResultList = new Vector<File>();
                    lstResultList.add(file);
                    return true;
                    // Es ist ein Verzeichnis und es existiert
                } else {
                    // wenn kein fileSpec angegeben wurde, reicht die simple
                    // Existenz aus
                    if (fileSpec == null || fileSpec.length() == 0) {
                        log("checking file " + file.getCanonicalPath() + ": directory exists", logger);
                        return true;
                    }
                    // Ein Dateifilter wurde angegeben
                    // hier werden auch Filter wie FileAge, FileSize und
                    // Einschraenkung der Anzahl (skip files) angewendet
                    Vector<File> fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, false, minAge, maxAge, minSize, maxSize, skipFirstFiles, skipLastFiles);
                    if (fileList.isEmpty()) {
                        log("checking file " + file.getCanonicalPath() + ": directory contains no files matching " + fileSpec, logger);
                        return false;
                    } else {
                        log("checking file " + file.getCanonicalPath() + ": directory contains " + fileList.size() + " file(s) matching " + fileSpec, logger);
                        for (int i = 0; i < fileList.size(); i++) {
                            File checkFile = fileList.get(i);
                            log("found " + checkFile.getCanonicalPath(), logger);
                        }
                        if (minNumOfFiles >= 0 && fileList.size() < minNumOfFiles) {
                            log("found " + fileList.size() + " files, minimum expected " + minNumOfFiles + " files", logger);
                            return false;
                        }
                        if (maxNumOfFiles >= 0 && fileList.size() > maxNumOfFiles) {
                            log("found " + fileList.size() + " files, maximum expected " + maxNumOfFiles + " files", logger);
                            return false;
                        }
                        lstResultList = fileList;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            throw new JobSchedulerException("error checking file: " + file.toString() + ": " + e.getMessage());
        }
    }

    /** Removes one file or all files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory. If
     * <em>file</em> is a directory all contained files are removed.
     * 
     * @param file file or directory
     * @param logger
     * 
     *            Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final File file, final Object objDummy) throws Exception {
        return removeFile(file, ".*", 0, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Removes one file or all files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory. If
     * <em>file</em> is a directory all contained files are removed.
     * 
     * @param file file or directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final File file, final int flags, final Object objDummy) throws Exception {
        return removeFile(file, ".*", flags, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final File file, final String fileSpec, final Object objDummy) throws Exception {
        return removeFile(file, fileSpec, 0, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final File file, final String fileSpec, final int flags, final Object objDummy) throws Exception {
        return removeFile(file, fileSpec, flags, Pattern.CASE_INSENSITIVE, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final File file, final String fileSpec, final int flags, final int fileSpecFlags, final Object objDummy) throws Exception {
        return removeFile(file, fileSpec, flags, fileSpecFlags, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final File file, final String fileSpec, final int flags, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles,
            final Object objDummy) throws Exception {
        int nrOfRemovedObjects = removeFileCnt(file, fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, logger);
        return nrOfRemovedObjects > 0;
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns the total number of removed files and directories.
     * 
     * @throws Exception */
    public int removeFileCnt(final File file, final String fileSpec, final int flags, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles,
            final Object objDummy) throws Exception {
        boolean gracious;
        boolean recursive;
        boolean remove_dir;
        boolean wipe;
        int nrOfRemovedFiles = 0;
        int nrOfRemovedDirectories = 0;
        long minAge = 0;
        long maxAge = 0;
        long minSize = -1;
        long maxSize = -1;
        try {
            recursive = has(flags, SOSFileSystemOperations.RECURSIVE);
            gracious = has(flags, SOSFileSystemOperations.GRACIOUS);
            wipe = has(flags, SOSFileSystemOperations.WIPE);
            remove_dir = has(flags, SOSFileSystemOperations.REMOVE_DIR);
            // Argumente ausgeben
            log_debug1("arguments for removeFile:", logger);
            log_debug1("argument file=" + file, logger);
            log_debug1("argument fileSpec=" + fileSpec, logger);
            String msg = "";
            if (has(flags, SOSFileSystemOperations.GRACIOUS))
                msg += "GRACIOUS ";
            if (has(flags, SOSFileSystemOperations.RECURSIVE))
                msg += "RECURSIVE ";
            if (has(flags, SOSFileSystemOperations.REMOVE_DIR))
                msg += "REMOVE_DIR ";
            if (has(flags, SOSFileSystemOperations.WIPE))
                msg += "WIPE ";
            log_debug1("argument flags=" + msg, logger);
            msg = "";
            if (has(fileSpecFlags, Pattern.CANON_EQ))
                msg += "CANON_EQ ";
            if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE))
                msg += "CASE_INSENSITIVE ";
            if (has(fileSpecFlags, Pattern.COMMENTS))
                msg += "COMMENTS ";
            if (has(fileSpecFlags, Pattern.DOTALL))
                msg += "DOTALL ";
            if (has(fileSpecFlags, Pattern.MULTILINE))
                msg += "MULTILINE ";
            if (has(fileSpecFlags, Pattern.UNICODE_CASE))
                msg += "UNICODE_CASE ";
            if (has(fileSpecFlags, Pattern.UNIX_LINES))
                msg += "UNIX_LINES ";
            log_debug1("argument fileSpecFlags=" + msg, logger);
            log_debug1("argument minFileAge=" + minFileAge, logger);
            log_debug1("argument maxFileAge=" + maxFileAge, logger);
            minAge = calculateFileAge(minFileAge);
            maxAge = calculateFileAge(maxFileAge);
            log_debug1("argument minFileSize=" + minFileSize, logger);
            log_debug1("argument maxFileSize=" + maxFileSize, logger);
            minSize = calculateFileSize(minFileSize);
            maxSize = calculateFileSize(maxFileSize);
            log_debug1("argument skipFirstFiles=" + skipFirstFiles, logger);
            log_debug1("argument skipLastFiles=" + skipLastFiles, logger);
            if (skipFirstFiles < 0)
                throw new JobSchedulerException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
            if (skipLastFiles < 0)
                throw new JobSchedulerException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
            if (skipFirstFiles > 0 && skipLastFiles > 0)
                throw new JobSchedulerException("skip only either first files or last files");
            if (skipFirstFiles > 0 || skipLastFiles > 0) {
                if (minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1)
                    throw new JobSchedulerException("missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
            }
            if (!file.exists()) {
                if (gracious) {
                    log("cannot remove file: file does not exist: " + file.getCanonicalPath(), logger);
                    return 0;
                } else
                    throw new JobSchedulerException("file does not exist: " + file.getCanonicalPath());
            }
            Vector<File> fileList;
            // Verzeichnis
            if (file.isDirectory()) {
                if (!file.canRead())
                    throw new JobSchedulerException("directory is not readable: " + file.getCanonicalPath());
                log("remove [" + fileSpec + "] from " + file.getCanonicalPath() + (recursive ? " (recursive)" : ""), logger);
                // hier werden Filter wie FileAge, FileSize und Einschraenkung
                // der Anzahl (skip files) angewendet
                fileList = getFilelist(file.getPath(), fileSpec, fileSpecFlags, has(flags, SOSFileSystemOperations.RECURSIVE), minAge, maxAge, minSize, maxSize, skipFirstFiles, skipLastFiles);
            }
            // einzelne Datei
            else {
                fileList = new Vector<File>();
                fileList.add(file);
                // Filterung
                fileList = filelistFilterAge(fileList, minAge, maxAge);
                fileList = filelistFilterSize(fileList, minSize, maxSize);
                if (skipFirstFiles > 0 || skipLastFiles > 0)
                    fileList.clear();
            }
            File currentFile;
            for (int i = 0; i < fileList.size(); i++) {
                currentFile = fileList.get(i);
                log("remove file " + currentFile.getCanonicalPath(), logger);
                if (wipe) {
                    if (!wipe(currentFile, logger))
                        throw new JobSchedulerException("cannot remove file: " + currentFile.getCanonicalPath());
                } else {
                    if (!currentFile.delete())
                        throw new JobSchedulerException("cannot remove file: " + currentFile.getCanonicalPath());
                }
                nrOfRemovedFiles++;
            }
            // sollen auch Verzeichnisse entfernt werden ?
            if (remove_dir) {
                int firstSize = listFolders(file.getPath(), ".*", 0, recursive).size();
                if (recursive) {
                    recDeleteEmptyDir(file, fileSpec, fileSpecFlags, logger);
                } else {
                    Vector<File> list = listFolders(file.getPath(), fileSpec, fileSpecFlags);
                    File f;
                    for (int i = 0; i < list.size(); i++) {
                        f = list.get(i);
                        if (f.isDirectory()) {
                            if (!f.canRead())
                                throw new JobSchedulerException("directory is not readable: " + f.getCanonicalPath());
                            if (f.list().length == 0) {
                                if (!f.delete())
                                    throw new JobSchedulerException("cannot remove directory: " + f.getCanonicalPath());
                                log("remove directory " + f.getPath(), logger);
                            } else {
                                log_debug3("directory [" + f.getCanonicalPath() + "] cannot be removed because it is not empty", logger);
                                String lst = f.list()[0];
                                for (int n = 1; n < f.list().length; n++)
                                    lst += ", " + f.list()[n];
                                log_debug9("          contained files " + f.list().length + ": " + lst, logger);
                            }
                        }
                    }
                }
                nrOfRemovedDirectories = firstSize - listFolders(file.getPath(), ".*", 0, recursive).size();
            }
            msg = "";
            if (remove_dir) {
                if (nrOfRemovedDirectories == 1)
                    msg = " + 1 directory removed";
                else
                    msg = " + " + nrOfRemovedDirectories + " directories removed";
            }
            log(nrOfRemovedFiles + " file(s) removed" + msg, logger);
            lstResultList = fileList;
            return nrOfRemovedFiles + nrOfRemovedDirectories;
        } catch (Exception e) {
            throw new JobSchedulerException("error occurred removing file(s): " + e.getMessage());
        }
    }

    public Vector<File> listFolders(final String folder, final String regexp, final int flag, final boolean withSubFolder) throws Exception {
        Vector<File> filelist = new Vector<File>();
        File file = null;
        File[] subDir = null;

        file = new File(folder);
        subDir = file.listFiles();
        filelist.addAll(listFolders(folder, regexp, flag));
        if (withSubFolder) {
            for (File element : subDir) {
                if (element.isDirectory()) {
                    filelist.addAll(listFolders(element.getPath(), regexp, flag, true));
                }
            }
        }
        return filelist;
    }

    public Vector<File> listFolders(final String folder, final String regexp, final int flag) throws Exception {

        Vector<File> filelist = new Vector<File>();

        if (folder == null || folder.length() == 0)
            throw new FileNotFoundException("empty directory not allowed!!");

        File f = new File(folder);
        if (!f.exists())
            throw new FileNotFoundException("directory does not exist: " + folder);
        filelist = new Vector<File>();
        File[] files = f.listFiles(new SOSFilelistFilter(regexp, flag));
        for (int i = 0; i < files.length; i++) {
            if (!files[i].getName().equals(".") && !files[i].getName().equals(".."))
                filelist.add(files[i]);
        }

        return filelist;
    }

    private boolean recDeleteEmptyDir(final File dir, final String fileSpec, final int fileSpecFlags, final Object objDummy1) throws Exception {
        if (dir.isDirectory()) {
            if (!dir.canRead())
                throw new JobSchedulerException("directory is not readable: " + dir.getCanonicalPath());
        } else {
            // eine Datei gleich ausschließen
            return false;
        }
        // Verzeichnisinhalt inklusive Verzeichnisse einlesen - nicht rekursiv!
        File[] list = dir.listFiles();
        if (list.length == 0)
            return true;
        Pattern p = Pattern.compile(fileSpec, fileSpecFlags);
        File f;
        for (File element : list) {
            f = element;
            if (recDeleteEmptyDir(f, fileSpec, fileSpecFlags, objDummy1)) {
                if (p.matcher(f.getName()).matches()) {
                    if (!f.delete())
                        throw new JobSchedulerException("cannot remove directory: " + f.getCanonicalPath());
                    log("remove directory " + f.getPath(), objDummy1);
                }
            } else {
                if (f.isDirectory()) {
                    log_debug3("directory [" + f.getCanonicalPath() + "] cannot be removed because it is not empty", objDummy1);
                    String lst = f.list()[0];
                    for (int n = 1; n < f.list().length; n++)
                        lst += ", " + f.list()[n];
                    log_debug9("          contained files " + f.list().length + ": " + lst, objDummy1);
                }
            }
        }
        // ist das Verzeichnnis jetzt leer?
        return dir.list().length == 0;
    }

    /** Removes one file or all files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory. If
     * <em>file</em> is a directory all contained files are removed.
     * 
     * @param file file or directory
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final String file, final Object objDummy) throws Exception {
        return removeFile(new File(file), logger);
    }

    /** Removes one file or all files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory. If
     * <em>file</em> is a directory all contained files are removed.
     * 
     * @param file file or directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final String file, final int flags, final Object objDummy) throws Exception {
        return removeFile(new File(file), flags, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final String file, final String fileSpec, final Object objDummy) throws Exception {
        return removeFile(new File(file), fileSpec, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final String file, final String fileSpec, final int flags, final Object objDummy) throws Exception {
        return removeFile(new File(file), fileSpec, flags, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final String file, final String fileSpec, final int flags, final int fileSpecFlags, final Object objDummy) throws Exception {
        return removeFile(new File(file), fileSpec, flags, fileSpecFlags, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns true if at least one file or directory was removed.
     *         Returns false if zero files or directories were removed
     * @throws Exception */
    public boolean removeFile(final String file, final String fileSpec, final int flags, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles,
            final Object objDummy) throws Exception {
        return removeFile(new File(file), fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, logger);
    }

    /** Removes one file or several files of a directory from the file system.
     * Removes one file if <em>file</em> is a file but no directory (
     * <em>fileSpec</em> is ignored). If <em>file</em> is a directory all
     * contained files matching the expression <em>fileSpec</em> are removed.
     * 
     * @param file file or directory
     * @param fileSpec Regular expression for file filtering if file is a
     *            directory
     * @param flags Bit mask for following flags: {@link #GRACIOUS},
     *            {@link #RECURSIVE}, {@link #REMOVE_DIR} and {@link #WIPE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for removing. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            removing. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of removed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns the total number of removed files and directories.
     * 
     * @throws Exception */
    public int removeFileCnt(final String file, final String fileSpec, final int flags, final int fileSpecFlags, final String minFileAge,
            final String maxFileAge, final String minFileSize, final String maxFileSize, final int skipFirstFiles, final int skipLastFiles,
            final Object objDummy) throws Exception {
        return removeFileCnt(new File(file), fileSpec, flags, fileSpecFlags, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, logger);
    }

    /** Copies one file or all files of a directory. Already existing files are
     * overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final File target, final Object objDummy) throws Exception {
        return copyFile(source, target, ".*", 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or all files of a directory. By default already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final File target, final int flags, final Object objDummy) throws Exception {
        return copyFile(source, target, ".*", flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or several files of a directory. Already existing files
     * are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final File target, final String fileSpec, final Object objDummy) throws Exception {
        return copyFile(source, target, fileSpec, 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final File target, final String fileSpec, final int flags, final Object objDummy) throws Exception {
        return copyFile(source, target, fileSpec, flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory, this argument is also used
     *            as target
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bit mask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        return copyFile(source, null, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bit mask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final File target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        return copyFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bit mask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for copying. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for copying. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            copying. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            copying. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of copied files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of copied files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final File source, final File target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        String mode = "copy";
        return transferFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, mode, logger);
    }

    /** Copies one file or all files of a directory. Already existing files are
     * overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, logger);
    }

    /** Copies one file or all files of a directory. By default already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final int flags, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, flags, logger);
    }

    /** Copies one file or several files of a directory. Already existing files
     * are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final String fileSpec, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, fileSpec, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final String fileSpec, final int flags, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, fileSpec, flags, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags, final Object objDummy)
            throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory, this argument is also used
     *            as target
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        return copyFile(sourceFile, fileSpec, flags, fileSpecFlags, replacing, replacement, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for copying. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for copying. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            copying. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            copying. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of copied files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of copied files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns true if at least one file was copied. Returns false if
     *         zero files were copied
     * @throws Exception */
    public boolean copyFile(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return copyFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, logger);
    }

    /** Copies one file or several files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for copying. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for copying. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            copying. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            copying. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of copied files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of copied files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns the number of copied files.
     * @throws Exception */
    public int copyFileCnt(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        String mode = "copy";
        return transferFileCnt(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, mode, logger);
    }

    /** Renames/moves one file or all files of a directory. Already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final Object objDummy) throws Exception {
        return renameFile(source, target, ".*", 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or all files of a directory. By default already
     * existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final int flags, final Object objDummy) throws Exception {
        return renameFile(source, target, ".*", flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. Already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final Object objDummy) throws Exception {
        return renameFile(source, target, fileSpec, 0, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final int flags, final Object objDummy) throws Exception {
        return renameFile(source, target, fileSpec, flags, Pattern.CASE_INSENSITIVE, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final int flags, final int fileSpecFlags, final Object objDummy)
            throws Exception {
        return renameFile(source, target, fileSpec, flags, fileSpecFlags, null, null, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. Already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final String replacing, final String replacement,
            final Object objDummy) throws Exception {
        return renameFile(source, target, fileSpec, 0, Pattern.CASE_INSENSITIVE, replacing, replacement, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final int flags, final String replacing, final String replacement,
            final Object objDummy) throws Exception {
        return renameFile(source, target, fileSpec, flags, Pattern.CASE_INSENSITIVE, replacing, replacement, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory, this argument is also used
     *            as target
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        return renameFile(source, null, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        return renameFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, "0", "0", "-1", "-1", 0, 0, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for renaming. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for renaming. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            renaming. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            renaming. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of renamed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of renamed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final File source, final File target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        String mode = "rename";
        return transferFile(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, mode, logger);
    }

    /** Renames/moves one file or all files of a directory. Already existing
     * files are overwritten
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, logger);
    }

    /** Renames/moves one file or all files of a directory. By default already
     * existing files are overwritten
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final int flags, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, flags, logger);
    }

    /** Renames/moves one file or several files of a directory. Already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final int flags, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, flags, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags, final Object objDummy)
            throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, logger);
    }

    /** Renames/moves one file or several files of a directory. Already existing
     * files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final String replacing, final String replacement,
            final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, replacing, replacement, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final int flags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, flags, replacing, replacement, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory, this argument is also used
     *            as target
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        return renameFile(sourceFile, fileSpec, flags, fileSpecFlags, replacing, replacement, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags,
            final String replacing, final String replacement, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for renaming. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for renaming. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            renaming. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            renaming. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of renamed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of renamed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns true if at least one file was moved. Returns false if
     *         zero files were moved
     * @throws Exception */
    public boolean renameFile(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags,
            final String replacing, final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize,
            final String maxFileSize, final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        return renameFile(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, logger);
    }

    /** Renames/moves one file or several files of a directory. By default
     * already existing files are overwritten.
     * 
     * @param source Source file or source directory
     * @param target Target file or target directory
     * @param fileSpec Regular expression - file filter, used if source is a
     *            directory
     * @param flags Bit mask for following flags: {@link #CREATE_DIR},
     *            {@link #GRACIOUS}, {@link #NOT_OVERWRITE} and
     *            {@link #RECURSIVE}
     * @param fileSpecFlags Pattern bitmask providing the regular expression
     *            <em>fileSpec</em>
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html">java.util.regex.Pattern</a>
     * @param replacing Regular expression for file name replacement with
     *            <em>replacement</em>. <em>replacing</em> is ignored if it is
     *            NULL. Requires the parameter <em>replacement</em> being not
     *            NULL.
     * @param replacement String for replacement of the expression replacing.
     *            Replacements for multiple capturing groups are separated by a
     *            semicolon ";" <em>replacement</em> is ignored if it is NULL.
     *            Requires the parameter <em>replacing</em> being not NULL.
     * @param minFileAge Filter for file age: files with a earlier modification
     *            date are skipped for renaming. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param maxFileAge Filter for file age: files with a later modification
     *            date are skipped for renaming. Possible values: sec, hh:mm,
     *            hh:mm:sec The Resulting set is sorted by file age in ascending
     *            order (most recent first)
     * @param minFileSize Filter for file size: smaller files are skipped for
     *            renaming. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param maxFileSize Filter for file size: greater files are skipped for
     *            renaming. Possible values: number (bytes), numberKB, numberMB,
     *            numberGB (KB, MB, GB case insensitive) The Resulting set is
     *            sorted by file size in ascending order (smallest first). If
     *            the set is additionally filtered by file age the set is sorted
     *            by file age.
     * @param skipFirstFiles Decreases the number of renamed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The smallest or most recent files are
     *            skipped.
     * @param skipLastFiles Decreases the number of renamed files. Requires at
     *            least one filter of minFileAge, maxFilesAge, minFileSize or
     *            maxFileSize. The file sre skipped in respect of the sorting of
     *            the filtered set. The greatest or oldest files are skipped.
     * @param logger Object
     * @return Returns the number of renamed/moved files.
     * @throws Exception */
    public int renameFileCnt(final String source, final String target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final Object objDummy) throws Exception {
        File sourceFile = new File(source);
        File targetFile = target == null ? null : new File(target);
        String mode = "rename";
        return transferFileCnt(sourceFile, targetFile, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, mode, logger);
    }

    /** Used for copyFile and renameFile. Transfers files.
     * 
     * Returns true if at least one file was copied or moved. Returns false if
     * zero files were copied or moved */
    private boolean transferFile(final File source, final File target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final String mode, final Object objDummy) throws Exception {
        int nrOfTransferedFiles = transferFileCnt(source, target, fileSpec, flags, fileSpecFlags, replacing, replacement, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles, mode, logger);
        return nrOfTransferedFiles > 0;
    }

    /** Used for copyFile and renameFile. Transfers files.
     * 
     * Returns the number of the transferred files. */
    private int transferFileCnt(final File source, File target, final String fileSpec, final int flags, final int fileSpecFlags, final String replacing,
            final String replacement, final String minFileAge, final String maxFileAge, final String minFileSize, final String maxFileSize,
            final int skipFirstFiles, final int skipLastFiles, final String mode, final Object objDummy) throws Exception {
        int nrOfTransferedFiles = 0;
        boolean create_dir;
        boolean gracious;
        boolean overwrite;
        boolean replace = false;
        // modi
        boolean copying = false;
        boolean renaming = false;
        String targetFilename;
        long minAge = 0;
        long maxAge = 0;
        long minSize = -1;
        long maxSize = -1;
        try {
            if (mode.equals("copy"))
                copying = true;
            else if (mode.equals("rename"))
                renaming = true;
            else
                throw new JobSchedulerException("unsupported mode: " + mode);
            // welche Flags sind gesetzt?
            create_dir = has(flags, SOSFileSystemOperations.CREATE_DIR);
            gracious = has(flags, SOSFileSystemOperations.GRACIOUS);
            overwrite = !has(flags, SOSFileSystemOperations.NOT_OVERWRITE);
            // Argumente ausgeben
            if (copying)
                log_debug1("arguments for copyFile:", logger);
            else if (renaming)
                log_debug1("arguments for renameFile:", logger);
            log_debug1("argument source=" + source.toString(), logger);
            if (target != null)
                log_debug1("argument target=" + target.toString(), logger);
            log_debug1("argument fileSpec=" + fileSpec, logger);
            String msg = "";
            if (has(flags, SOSFileSystemOperations.CREATE_DIR))
                msg += "CREATE_DIR ";
            if (has(flags, SOSFileSystemOperations.GRACIOUS))
                msg += "GRACIOUS ";
            if (has(flags, SOSFileSystemOperations.NOT_OVERWRITE))
                msg += "NOT_OVERWRITE ";
            if (has(flags, SOSFileSystemOperations.RECURSIVE))
                msg += "RECURSIVE ";
            if (msg.equals(""))
                msg = "0";
            log_debug1("argument flags=" + msg, logger);
            msg = "";
            if (has(fileSpecFlags, Pattern.CANON_EQ))
                msg += "CANON_EQ ";
            if (has(fileSpecFlags, Pattern.CASE_INSENSITIVE))
                msg += "CASE_INSENSITIVE ";
            if (has(fileSpecFlags, Pattern.COMMENTS))
                msg += "COMMENTS ";
            if (has(fileSpecFlags, Pattern.DOTALL))
                msg += "DOTALL ";
            if (has(fileSpecFlags, Pattern.MULTILINE))
                msg += "MULTILINE ";
            if (has(fileSpecFlags, Pattern.UNICODE_CASE))
                msg += "UNICODE_CASE ";
            if (has(fileSpecFlags, Pattern.UNIX_LINES))
                msg += "UNIX_LINES ";
            if (msg.equals(""))
                msg = "0";
            log_debug1("argument fileSpecFlags=" + msg, logger);
            log_debug1("argument replacing=" + replacing, logger);
            log_debug1("argument replacement=" + replacement, logger);
            log_debug1("argument minFileAge=" + minFileAge, logger);
            log_debug1("argument maxFileAge=" + maxFileAge, logger);
            minAge = calculateFileAge(minFileAge);
            maxAge = calculateFileAge(maxFileAge);
            log_debug1("argument minFileSize=" + minFileSize, logger);
            log_debug1("argument maxFileSize=" + maxFileSize, logger);
            minSize = calculateFileSize(minFileSize);
            maxSize = calculateFileSize(maxFileSize);
            log_debug1("argument skipFirstFiles=" + skipFirstFiles, logger);
            log_debug1("argument skipLastFiles=" + skipLastFiles, logger);
            if (skipFirstFiles < 0)
                throw new JobSchedulerException("[" + skipFirstFiles + "] is no valid value for skipFirstFiles");
            if (skipLastFiles < 0)
                throw new JobSchedulerException("[" + skipLastFiles + "] is no valid value for skipLastFiles");
            if (skipFirstFiles > 0 && skipLastFiles > 0)
                throw new JobSchedulerException("skip only either first files or last files");
            if (skipFirstFiles > 0 || skipLastFiles > 0) {
                if (minAge == 0 && maxAge == 0 && minSize == -1 && maxSize == -1)
                    throw new JobSchedulerException("missed constraint for file skipping (minFileAge, maxFileAge, minFileSize, maxFileSize)");
            }
            // Wenn ersetzt werden soll, ist es gültig?
            if (replacing != null || replacement != null) {
                if (replacing == null)
                    throw new JobSchedulerException("replacing cannot be null if replacement is set");
                if (replacement == null)
                    throw new JobSchedulerException("replacement cannot be null if replacing is set");
                if (!replacing.equals("")) {
                    try {
                        Pattern.compile(replacing); // Pattern validieren
                    } catch (PatternSyntaxException pse) {
                        throw new JobSchedulerException("invalid pattern '" + replacing + "'");
                    }
                    replace = true;
                }
            }
            // Existiert die Quelle?
            if (!source.exists()) {
                if (gracious) {
                    log(nrOfTransferedFiles + " file(s) renamed", logger);
                    return nrOfTransferedFiles;
                } else
                    throw new JobSchedulerException("file or directory does not exist: " + source.getCanonicalPath());
            }
            if (!source.canRead())
                throw new JobSchedulerException("file or directory is not readable: " + source.getCanonicalPath());
            // Target
            // Substitution im Zielverzeichnisnamen
            if (target != null) {
                targetFilename = substituteAllDate(target.getPath());
                targetFilename = substituteAllDirectory(targetFilename, source.getPath());
                // should any opening and closing brackets be found in the file
                // name, then this is an error
                Matcher m = Pattern.compile("\\[[^]]*\\]").matcher(targetFilename);
                if (m.find())
                    throw new JobSchedulerException("unsupported file mask found: " + m.group());
                // zum Schluss Änderungen zuweisen
                target = new File(targetFilename);
            }
            // optional Zielverzeichnis anlegen
            if (create_dir) {
                if (target != null && !target.exists()) {
                    if (target.mkdirs())
                        log("create target directory " + target.getCanonicalPath(), logger);
                    else
                        throw new JobSchedulerException("cannot create directory " + target.getCanonicalPath());
                }
            }
            // Liste der Dateien
            Vector<File> list = null;
            // 1. Quelle ist ein Verzeichnis
            if (source.isDirectory()) {
                if (target != null) {
                    if (!target.exists())
                        throw new JobSchedulerException("directory does not exist: " + target.getCanonicalPath());
                    if (!target.isDirectory())
                        throw new JobSchedulerException("target is no directory: " + target.getCanonicalPath());
                }
                // hier werden Filter wie FileAge, FileSize und Einschraenkung
                // der Anzahl angewendet
                list = getFilelist(source.getPath(), fileSpec, fileSpecFlags, has(flags, SOSFileSystemOperations.RECURSIVE), minAge, maxAge, minSize, maxSize, skipFirstFiles, skipLastFiles);
                // 2. Quelle ist eine einzige Datei
            } else {
                list = new Vector<File>();
                list.add(source);
                // Filterung
                list = filelistFilterAge(list, minAge, maxAge);
                list = filelistFilterSize(list, minSize, maxSize);
                if (skipFirstFiles > 0 || skipLastFiles > 0)
                    list.clear();
            }
            File sourceFile, targetFile;
            File dir;
            for (int i = 0; i < list.size(); i++) {
                sourceFile = list.get(i);
                // Zieldatei ermitteln
                if (target != null) {
                    if (target.isDirectory()) {
                        // Rekursion berücksichtigen
                        String root = source.isDirectory() ? source.getPath() : source.getParent();
                        targetFilename = target.getPath() + sourceFile.getPath().substring(root.length());
                    } else {
                        targetFilename = target.getPath();
                    }
                    // target ist null, d.h. das Quellverzeichnis ist auch das
                    // Zielverzeichnis
                } else {
                    if (source.isDirectory()) {
                        // Rekursion berücksichtigen
                        String root = source.isDirectory() ? source.getPath() : source.getParent();
                        targetFilename = source.getPath() + sourceFile.getPath().substring(root.length());
                    } else
                        targetFilename = source.getParent() + "/" + sourceFile.getName();
                }
                targetFile = new File(targetFilename);
                // Dateinamen ersetzen
                try {
                    if (replace) {
                        targetFilename = targetFile.getName(); // bis jetzt noch
                                                               // ursprünglicher
                                                               // Dateiname
                        targetFilename = replaceGroups(targetFilename, replacing, replacement /*
                                                                                               * .
                                                                                               * split
                                                                                               * (
                                                                                               * ";"
                                                                                               * )
                                                                                               */);
                        targetFilename = substituteAllDate(targetFilename);
                        targetFilename = substituteAllFilename(targetFilename, targetFile.getName());
                        // should any opening and closing brackets be found in
                        // the file name, then this is an error
                        Matcher matcher = Pattern.compile("\\[[^]]*\\]").matcher(targetFilename);
                        if (matcher.find())
                            throw new JobSchedulerException("unsupported file mask found: " + matcher.group());
                        targetFile = new File(targetFile.getParent() + "/" + targetFilename); // neuer
                                                                                              // Dateiname
                    }
                } catch (Exception re) {
                    throw new JobSchedulerException("replacement error in file " + targetFilename + ": " + re.getMessage());
                }
                // Existieren alle benötigten Elternverzeichnisse?
                dir = new File(targetFile.getParent());
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        log("create directory " + dir.getCanonicalPath(), logger);
                    } else
                        throw new JobSchedulerException("cannot create directory " + dir.getCanonicalPath());
                }
                if (copying) {
                    if (!copyOneFile(sourceFile, targetFile, overwrite, gracious, logger))
                        continue;
                } else if (renaming) {
                    if (!renameOneFile(sourceFile, targetFile, overwrite, gracious, logger))
                        continue;
                }
                nrOfTransferedFiles++;
            }
            if (copying)
                log(nrOfTransferedFiles + " file(s) copied", logger);
            else if (renaming)
                log(nrOfTransferedFiles + " file(s) renamed", logger);
            lstResultList = list;
            return nrOfTransferedFiles;
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (copying)
                throw new JobSchedulerException("error occurred copying file(s): " + e.getMessage());
            else if (renaming)
                throw new JobSchedulerException("error occurred renaming file(s): " + e.getMessage());
            else
                return 0;
        }
    }

    private Vector<File> filelistFilterAge(Vector<File> filelist, final long minAge, final long maxAge) throws Exception {
        long currentTime = System.currentTimeMillis();
        if (minAge != 0) {
            File file;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                long interval = currentTime - file.lastModified();
                if (interval < 0)
                    throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath() + "] was modified in the future.");
                if (interval >= minAge)
                    newlist.add(file);
            }
            filelist = newlist;
        }
        if (maxAge != 0) {
            File file;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                long interval = currentTime - file.lastModified();
                if (interval < 0)
                    throw new JobSchedulerException("Cannot filter by file age. File [" + file.getCanonicalPath() + "] was modified in the future.");
                if (interval <= maxAge)
                    newlist.add(file);
            }
            filelist = newlist;
        }
        return filelist;
    }

    private Vector<File> filelistFilterSize(Vector<File> filelist, final long minSize, final long maxSize) throws Exception {
        if (minSize > -1) {
            File file;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                if (file.length() >= minSize)
                    newlist.add(file);
            }
            filelist = newlist;
        }
        if (maxSize > -1) {
            File file;
            Vector<File> newlist = new Vector<File>();
            for (int i = 0; i < filelist.size(); i++) {
                file = filelist.get(i);
                if (file.length() <= maxSize)
                    newlist.add(file);
            }
            filelist = newlist;
        }
        return filelist;
    }

    private Vector<File> filelistSkipFiles(Vector<File> filelist, final int skipFirstFiles, final int skipLastFiles, final String sorting) throws Exception {
        Object[] oArr = filelist.toArray();
        class SizeComparator implements Comparator {

            @Override
            public int compare(final Object o1, final Object o2) {
                int ret = 0;
                long val1 = ((File) o1).length();
                long val2 = ((File) o2).length();
                if (val1 < val2)
                    ret = -1;
                if (val1 == val2)
                    ret = 0;
                if (val1 > val2)
                    ret = 1;
                return ret;
            }
        }
        ;
        class AgeComparator implements Comparator {

            @Override
            public int compare(final Object o1, final Object o2) {
                int ret = 0;
                long val1 = ((File) o1).lastModified();
                long val2 = ((File) o2).lastModified();
                if (val1 > val2)
                    ret = -1;
                if (val1 == val2)
                    ret = 0;
                if (val1 < val2)
                    ret = 1;
                return ret;
            }
        }
        ;
        // sortiert die Dateien im Array aufsteigend nach Größe der Datei, d.h.
        // kleinere zuerst
        if (sorting.equals("sort_size"))
            Arrays.sort(oArr, new SizeComparator());
        else
        // sortiert die Dateien im Array aufsteigend nach Änderungsdatum der
        // Datei, d.h. jüngere zuerst
        if (sorting.equals("sort_age"))
            Arrays.sort(oArr, new AgeComparator());
        // Skip files
        filelist = new Vector<File>();
        for (int i = 0 + skipFirstFiles; i < oArr.length - skipLastFiles; i++) {
            filelist.add((File) oArr[i]);
        }
        return filelist;
    }

    public Vector<File> listFiles(final String folder, final String regexp, final int flag, final boolean withSubFolder) throws Exception {
        Vector<File> filelist = new Vector<File>();
        File file = null;
        File[] subDir = null;

        file = new File(folder);
        subDir = file.listFiles();
        filelist.addAll(listFiles(folder, regexp, flag));
        if (withSubFolder) {
            for (File element : subDir) {
                if (element.isDirectory()) {
                    filelist.addAll(listFiles(element.getPath(), regexp, flag, true));
                }
            }
        }
        return filelist;
    }

    public Vector<File> listFiles(final String folder, final String regexp, final int flag) throws Exception {

        Vector<File> filelist = new Vector<File>();

        if (folder == null || folder.length() == 0)
            throw new FileNotFoundException("empty directory not allowed!!");

        File f = new File(folder);
        if (!f.exists()) {
            throw new FileNotFoundException("directory does not exist: " + folder);
        }

        filelist = new Vector<File>();
        File[] files = f.listFiles(new SOSFilelistFilter(regexp, flag));
        for (File file : files) {
            if (file.isDirectory()) {
            } else if (file.isFile()) {
                filelist.add(file);
            } else {
                // unknown
            }
        }

        return filelist;
    }

    /** liefert Dateiliste des eingegebenen Verzeichnis zurueck.
     * 
     * @return Vector Dateiliste
     * @param regexp ein regul&auml;er Ausdruck
     * @param flag ein Integer-Wert: CASE_INSENSITIVE, MULTILINE, DOTALL,
     *            UNICODE_CASE, and CANON_EQ
     * @param withSubFolder
     * @see <a
     *      href="http://java.sun.com/j2se/1.4.2/docs/api/constant-values.html#java.util.regex.Pattern.UNIX_LINES">Constant
     *      Field Values</a> */
    private Vector<File> getFilelist(final String folder, final String regexp, final int flag, final boolean withSubFolder, final long minFileAge,
            final long maxFileAge, final long minFileSize, final long maxFileSize, final int skipFirstFiles, final int skipLastFiles) throws Exception {
        Vector<File> filelist = new Vector<File>();
        Vector<File> temp = new Vector<File>();
        File file = null;
        File[] subDir = null;
        file = new File(folder);
        subDir = file.listFiles();
        temp = this.listFiles(folder, regexp, flag);
        // filtern
        temp = filelistFilterAge(temp, minFileAge, maxFileAge);
        temp = filelistFilterSize(temp, minFileSize, maxFileSize);
        if ((minFileSize != -1 || maxFileSize != -1) && minFileAge == 0 && maxFileAge == 0)
            temp = filelistSkipFiles(temp, skipFirstFiles, skipLastFiles, "sort_size");
        else if (minFileAge != 0 || maxFileAge != 0)
            temp = filelistSkipFiles(temp, skipFirstFiles, skipLastFiles, "sort_age");
        filelist.addAll(temp);
        if (withSubFolder) {
            for (File element : subDir) {
                if (element.isDirectory()) {
                    filelist.addAll(getFilelist(element.getPath(), regexp, flag, true, minFileAge, maxFileAge, minFileSize, maxFileSize, skipFirstFiles, skipLastFiles));
                }
            }
        }
        return filelist;
    }

    /** @param fileage sec or hours:min[:sec]
     * @return long value
     * @throws Exception */
    private long calculateFileAge(final String fileage) throws Exception {
        long age = 0;
        if (fileage == null || fileage.trim().length() == 0)
            return 0;
        // sec
        if (fileage.indexOf(":") == -1) {
            if (!fileage.matches("[\\d]+"))
                throw new JobSchedulerException("[" + fileage + "] is no valid file age");
            else
                return Long.parseLong(fileage) * 1000;
        }
        // hours:min[:sec]
        if (!fileage.matches("^[\\d].*[\\d]$"))
            throw new JobSchedulerException("[" + fileage + "] is no valid file age");
        String[] timeArray = fileage.split(":");
        if (timeArray.length > 3)
            throw new JobSchedulerException("[" + fileage + "] is no valid file age");
        for (int i = 0; i < timeArray.length; i++) {
            if (!timeArray[i].matches("[\\d]+"))
                throw new JobSchedulerException("[" + fileage + "] is no valid file age");
        }
        long hours = Long.parseLong(timeArray[0]);
        long minutes = Long.parseLong(timeArray[1]);
        long seconds = 0;
        if (timeArray.length > 2) {
            seconds = Long.parseLong(timeArray[2]);
        }
        age = hours * 3600000 + minutes * 60000 + seconds * 1000;
        return age;
    }

    /** @param filesize X Bytes, X KB, X MB, X GB
     * @return long value
     * @throws Exception */
    private long calculateFileSize(final String filesize) throws Exception {
        long size;
        if (filesize == null || filesize.trim().length() == 0)
            return -1;
        if (filesize.matches("-1"))
            return -1;
        if (filesize.matches("[\\d]+")) {
            size = Long.parseLong(filesize);
        } else {
            if (filesize.matches("^[\\d]+[kK][bB]$"))
                size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024;
            else if (filesize.matches("^[\\d]+[mM][bB]$"))
                size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024 * 1024;
            else if (filesize.matches("^[\\d]+[gG][bB]$"))
                size = Long.parseLong(filesize.substring(0, filesize.length() - 2)) * 1024 * 1024 * 1024;
            else
                throw new JobSchedulerException("[" + filesize + "] is no valid file size");
        }
        return size;
    }

    private String substituteFirstFilename(String targetFilename, final String original) throws Exception {
        // check for [filename:...]
        Matcher matcher = Pattern.compile("\\[filename:([^\\]]*)\\]").matcher(targetFilename);
        if (matcher.find()) {
            if (matcher.group(1).equals("")) {
                targetFilename = targetFilename.replaceFirst("\\[filename:\\]", original);
            } else if (matcher.group(1).equals("lowercase")) {
                targetFilename = targetFilename.replaceFirst("\\[filename:lowercase\\]", original.toLowerCase());
            } else if (matcher.group(1).equals("uppercase")) {
                targetFilename = targetFilename.replaceFirst("\\[filename:uppercase\\]", original.toUpperCase());
            }
        }
        return targetFilename;
    }

    private String substituteAllFilename(String targetFilename, final String original) throws Exception {
        // original ist das replacement; es ist der urspruengliche Dateiname
        // inklusive Endung
        String temp = substituteFirstFilename(targetFilename, original);
        while (!targetFilename.equals(temp)) {
            targetFilename = temp;
            temp = substituteFirstFilename(targetFilename, original);
        }
        return temp;
    }

    private String substituteFirstDate(String targetFilename) throws Exception {
        final String conVarName = "[date:";
        try {
            // check for a date format string given in the file mask
            if (targetFilename.matches("(.*)(\\" + conVarName + ")([^\\]]*)(\\])(.*)")) {
                int posBegin = targetFilename.indexOf(conVarName);
                if (posBegin > -1) {
                    int posEnd = targetFilename.indexOf("]", posBegin + 6);
                    if (posEnd > -1) {
                        String strDateMask = targetFilename.substring(posBegin + 6, posEnd);
                        if (strDateMask.length() <= 0) {
                            strDateMask = SOSOptionTime.dateTimeFormat;
                        }
                        String strDateTime = SOSOptionTime.getCurrentTimeAsString(strDateMask);
                        String strT = (posBegin > 0 ? targetFilename.substring(0, posBegin) : "") + strDateTime;
                        if (targetFilename.length() > posEnd) {
                            strT += targetFilename.substring(posEnd + 1);
                        }
                        targetFilename = strT;
                    }
                }
            }
            return targetFilename;
        } catch (Exception e) {
            throw new RuntimeException("error substituting [date:]: " + e.getMessage());
        }
    }

    private String substituteAllDate(String targetFilename) throws Exception {
        String temp = substituteFirstDate(targetFilename);
        while (!targetFilename.equals(temp)) {
            targetFilename = temp;
            temp = substituteFirstDate(targetFilename);
        }
        return temp;
    }

    private String substituteFirstDirectory(String target, String source) throws Exception {
        try {
            File sourceFile = new File(source);
            if (!sourceFile.isDirectory()) {
                source = sourceFile.getParent();
            }
            // normalisieren
            source = source.replaceAll("\\\\", "/");
            target = target.replaceAll("\\\\", "/");
            Pattern p = Pattern.compile("\\[directory:(-[\\d]+|[\\d]*)\\]");
            Matcher m = p.matcher(target);
            if (m.find()) {
                String substitute = "";
                if (m.group(1).length() == 0 || m.group(1).equals("0") || m.group(1).equals("-0")) {
                    substitute = source;
                } else {
                    int depth = Integer.valueOf(m.group(1)).intValue();
                    StringTokenizer st = new StringTokenizer(source, "/");
                    int absDepth = depth < 0 ? -depth : depth;
                    if (absDepth >= st.countTokens()) {
                        substitute = source;
                    } else {
                        String[] dirs = new String[st.countTokens()];
                        int n = 0;
                        while (st.hasMoreTokens())
                            dirs[n++] = st.nextToken();
                        if (depth > 0)
                            while (depth > 0)
                                substitute = dirs[--depth] + "/" + substitute;
                        else if (depth < 0)
                            while (depth < 0)
                                substitute = substitute + dirs[dirs.length + depth++] + "/";
                    }
                }
                if (substitute.charAt(substitute.length() - 1) == '/')
                    substitute = substitute.substring(0, substitute.length() - 1);
                target = target.replaceFirst("\\[directory:[^\\]]*\\]", substitute);
            }
            return target;
        } catch (Exception e) {
            throw new JobSchedulerException("error substituting [directory]: " + e.getMessage());
        }
    }

    private String substituteAllDirectory(String target, final String source) throws Exception {
        String temp = substituteFirstDirectory(target, source);
        while (!target.equals(temp)) {
            target = temp;
            temp = substituteFirstDirectory(target, source);
        }
        return temp;
    }

    /** Replaces all groups in a string with given replacements. If only group 0
     * is existing (that means the entire match) it will be replaced with the
     * first replacement. Otherwise each capturing group will be replaced
     * accordingly.
     * 
     * @param input Inputstring
     * @param replacing Pattern
     * @param replacements replacement strings separated by semicolons
     * @return Result
     * @throws Exception */
    public String replaceGroups(final String input, final String replacing, final String replacements) throws Exception {
        if (replacements == null) {
            throw new RuntimeException("replacements missing: 0 replacements defined");
        }
        return replaceGroups(input, replacing, replacements.split(";"));
    }

    /** Replaces all groups in a string with given replacements. If only group 0
     * is existing (that means the entire match) it will be replaced with the
     * first replacement. Otherwise each capturing group will be replaced
     * accordingly.
     * 
     * @param input Inputstring
     * @param replacing Pattern
     * @param replacements Array von Ersetzungsstrings
     * @return Result
     * @throws Exception */
    public String replaceGroups(final String pstrSourceString, final String replacing, final String[] replacements) throws Exception {
        String result = "";
        if (replacements == null)
            throw new RuntimeException("replacements missing: 0 replacements defined");
        Pattern p = Pattern.compile(replacing); // the regular expression
        Matcher m = p.matcher(pstrSourceString); // check matching
        if (m.find() == false) // then nothing matched
            return pstrSourceString;
        int intGroupCount = m.groupCount();
        if (replacements.length < intGroupCount)
            throw new RuntimeException("replacements missing: " + replacements.length + " replacement(s) defined but " + intGroupCount + " groups found");
        // no groups, exchange the whole string
        if (intGroupCount == 0) {
            result = pstrSourceString.substring(0, m.start()) + replacements[0] + pstrSourceString.substring(m.end());
        } else {
            int index = 0;
            for (int i = 1; i <= intGroupCount; i++) {
                int intStart = m.start(i);
                if (intStart >= 0) {
                    String strRepl = replacements[i - 1].trim();
                    if (strRepl.length() > 0) {
                        if (strRepl.contains("\\") == true) {
                            strRepl = strRepl.replaceAll("\\\\-", "");
                            for (int j = 1; j <= intGroupCount; j++) {
                                strRepl = strRepl.replaceAll("\\\\" + j, m.group(j));
                            }
                        }
                        result += pstrSourceString.substring(index, intStart) + strRepl;
                    }
                }
                index = m.end(i);
            }
            result += pstrSourceString.substring(index);
        }
        return result;
    }

    private boolean copyOneFile(final File source, final File target, final boolean overwrite, final boolean gracious, final Object objDummy) throws Exception {
        boolean rc = false;
        if (source.equals(target))
            throw new JobSchedulerException("cannot copy file to itself: " + source.getCanonicalPath());
        if (overwrite || !target.exists()) {
            long modificationDate = source.lastModified();
            // if (logger != null)
            // SOSFile.setLogger(logger);
            rc = copyFile(source, target);
            target.setLastModified(modificationDate);
            log("copy " + source.getPath() + " to " + target.getPath(), logger);
            return rc;
        } else if (!gracious) {
            throw new JobSchedulerException("file already exists: " + target.getCanonicalPath());
        } else {
            log("file already exists: " + target.getCanonicalPath(), logger);
            return rc;
        }
    }

    private boolean renameOneFile(final File source, final File target, final boolean overwrite, final boolean gracious, final Object objDummy1)
            throws Exception {
        if (source.equals(target))
            throw new JobSchedulerException("cannot rename file to itself: " + source.getCanonicalPath());
        if (!overwrite && target.exists()) {
            if (!gracious) {
                throw new JobSchedulerException("file already exists: " + target.getCanonicalPath());
            } else {
                log("file already exists: " + target.getCanonicalPath(), logger);
                return false;
            }
        } else {
            if (target.exists()) {
                if (!target.delete())
                    throw new JobSchedulerException("cannot overwrite " + target.getCanonicalPath());
            }
            if (!source.renameTo(target)) {
                /** JS-690 in case of rename fails we try to use copy/delete
                 * instead kb 2011-06-21 */
                boolean rc = copyFile(source, target);
                if (rc == true) {
                    rc = source.delete();
                    if (rc == false) {
                        rc = target.delete();
                        throw new JobSchedulerException("cannot rename file from " + source.getCanonicalPath() + " to " + target.getCanonicalPath());
                    }
                } else {
                    throw new JobSchedulerException("cannot rename file from " + source.getCanonicalPath() + " to " + target.getCanonicalPath());
                }
            } else {
                log("rename " + source.getPath() + " to " + target.getPath(), logger);
            }
        }
        return true;
    }

    private void log(final String msg, final Object objDumy) {
        try {
            if (logger != null)
                logger.info(msg);
        } catch (Exception e) {
        }
    }

    private void log_debug1(final String msg, final Logger logger) {
        try {
            if (logger != null)
                logger.debug(msg);
        } catch (Exception e) {
        }
    }

    private void log_debug3(final String msg, final Object objDummy) {
        try {
            if (logger != null)
                logger.debug(msg);
        } catch (Exception e) {
        }
    }

    private void log_debug9(final String msg, final Object objDummy) {
        try {
            if (logger != null)
                logger.debug(msg);
        } catch (Exception e) {
        }
    }

    /** Tests if a flag is set or not */
    private boolean has(final int flags, final int f) {
        return (flags & f) > 0;
    }

    /** Method for invoking a method of this class by use of the Reflection API
     * 
     * @param methodname
     * @param argtypes
     * @param args
     * @throws Exception */
    @Deprecated
    public void callMethod(final String methodname, final Class[] argtypes, final Object[] args) throws Exception {
        Method method = null;
        try {
            if (argtypes.length != args.length)
                throw new JobSchedulerException("different array lengths: " + argtypes.length + " argument types but " + args.length + " arguments");
            try {
                method = Class.forName("sos.util.SOSFileOperations").getMethod(methodname, argtypes);
            } catch (NoSuchMethodException nsme) {
                throw new JobSchedulerException("method does not exist: " + nsme.getMessage());
            }
        } catch (Exception e) {
            throw new JobSchedulerException("callMethod: " + e.getMessage());
        }
        try {
            method.invoke(null, args);
        } catch (Exception x) {
            if (x.getMessage() == null)
                throw new JobSchedulerException(x.getCause().getMessage());
            else
                throw new JobSchedulerException("callMethod: " + x.getMessage());
        }
    }

    // Liefert einen neuen Dateinamen
    /** Returns a new filename where string replacements are applied. <br/>
     * After replacements are applied the patterns [date:...], [filename:],
     * [filename:lowercase] and [filename:uppercase] are substituted.
     * 
     * @param input original filename
     * @param replacing regular expression for matching
     * @param replacements replacement string
     * @throws Exception */
    public String getReplacementFilename(final String input, final String replacing, final String replacements) throws Exception {
        String targetFilename = input;
        try {
            targetFilename = replaceGroups(targetFilename, replacing, replacements.split(";"));
            targetFilename = substituteAllDate(targetFilename);
            targetFilename = substituteAllFilename(targetFilename, input);
            // should any opening and closing brackets be found in the file
            // name, then this is an error
            Matcher m = Pattern.compile("\\[[^\\]]*\\]").matcher(targetFilename);
            if (m.find())
                throw new JobSchedulerException("unsupported file mask found:" + m.group());
            return targetFilename;
        } catch (Exception e) {
            throw new JobSchedulerException("getReplacementFilename: " + e.getMessage());
        }
    }

    private boolean wipe(final File file, final Object objDummy) {
        try {
            RandomAccessFile rwFile = new RandomAccessFile(file, "rw");
            byte[] bytes = new byte[(int) rwFile.length()];
            int i = 0;
            while ((bytes[i++] = (byte) rwFile.read()) != -1)
                ;
            rwFile.seek(0);
            for (i = 0; i < bytes.length; i++)
                bytes[i] = (byte) (Math.random() * 10 % 9);
            rwFile.write(bytes);
            rwFile.close();
            logger.debug("Deleting file");
            boolean rc = file.delete();
            logger.debug("rc: " + rc);
            return rc;
        } catch (Exception e) {
            try {
                logger.warn("Failed to wipe file: " + e);
            } catch (Exception ex) {
            }
            return false;
        }
    }

    final int BUFF_SIZE = 100000;

    final byte[] buffer = new byte[BUFF_SIZE];

    /** Kopiert eine Datei von Quelle zum Ziel
     *
     * @param dest
     * @param source
     * @throws IOException
     * @author mo */
    public boolean copyFile(final File source, final File dest) throws Exception {
        return copyFile(source, dest, false);
    }

    /** Kopiert eine Datei von Quelle zum Ziel. Hier kann zusätzlich angegeben
     * werden, ob die Datei angehängt werden soll
     *
     * @param dest
     * @param source
     * @throws IOException
     * @author al */
    public boolean copyFile(final File source, final File dest, final boolean append) throws Exception {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (logger != null) {
                logger.debug("Copying file " + source.getAbsolutePath() + " with buffer of " + BUFF_SIZE + " bytes");
            }
            in = new FileInputStream(source);
            out = new FileOutputStream(dest, append);
            while (true) {
                synchronized (buffer) {
                    int amountRead = in.read(buffer);
                    if (amountRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, amountRead);
                }
            }
            if (logger != null) {
                logger.debug("File " + source.getAbsolutePath() + " with buffer of " + BUFF_SIZE + " bytes");
            }
            return true;
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    /** Kopiert eine Datei von Quelle zum Ziel. Die Parametern geben die
     * absoluter Pfad und Dateiname an, in dem die Datei kopiert werden sollen
     *
     * @param dest
     * @param source
     * @throws IOException
     * @author mo */
    public boolean copyFile(final String source, final String dest) throws Exception {
        return copyFile(new File(source), new File(dest), false);
    }

    /*
     * / public void main(String[] args) { try { //String fn =
     * getReplacementFilename("Hallo.XML", ".*", "[filename:lowercase]");
     * //System.out.println(fn); Object objDummy = new SOSStandardLogger(1);
     * String source = "C:/scheduler.managed/config/live/file1"; String target =
     * "C:/scheduler.managed/aaa/bbb/ccc/file1"; //File sFile = new
     * File(source);
     * //System.out.println("sFile.getPath() "+sFile.getParentFile().getPath());
     * //SOSFileOperations.removeFile(sFile.getPath(),
     * SOSFileOperations.RECURSIVE | SOSFileOperations.REMOVE_DIR, logger);
     * //renameFile(source, ".*dat$", 0, Pattern.CASE_INSENSITIVE,
     * "(ab)_c[\\d]d_(fg)", "first;second", logger);
     * //removeFile("K:/scheduler.test/files/file_operations/copy/12/out", ".*",
     * RECURSIVE | REMOVE_DIR, logger); //renameFile("files/", ".*", 0,
     * Pattern.CASE_INSENSITIVE, ".*", "[filename:]y", logger); //
     * /aaa/bbb/ccc/datei.txt sollte umbenannt werden nach
     * /aaa/bbb/ccc/20070101/datei_20070101.txt //copyFile("files/aaa/bbb/ccc/",
     * "[directory:]/[date:yyyyMMdd]/", "datei.txt", CREATE_DIR,
     * Pattern.CASE_INSENSITIVE, "[.]txt", "_[date:yyyyMMdd].txt", logger);
     * //copyFile("temp", "target", logger); // copyFile(source, target, ".*",
     * 0, Pattern.CASE_INSENSITIVE, null, null, "0:1", "0", "-1", "-1", 3, 0,
     * logger); // removeFile(target, ".*", RECURSIVE | REMOVE_DIR,
     * Pattern.CASE_INSENSITIVE, "0", "0", "6kb", "-1", 1, 0, logger); //
     * existsFile(source, ".*", Pattern.CASE_INSENSITIVE, "12:00", "0", "-1",
     * "50kb", 4, 0, logger); //copyFile("files/file_operations/copy/12/in/",
     * "[directory:4]/out/[date:yyyyMMdd]/[directory:-3]/", ".*", CREATE_DIR,
     * Pattern.CASE_INSENSITIVE, logger); //copyFile(source, target, ".*",
     * CREATE_DIR|RECURSIVE, Pattern.CASE_INSENSITIVE, ".*", "[filename:]",
     * logger); //copyFile(source, target, "da.*", RECURSIVE & 0,
     * Pattern.CASE_INSENSITIVE, ".*X.*",
     * "[date:yyyyMMdd]_[filename:uppercase]_[filename:lowercase]_[filename:]_[date:yyyy]"
     * , logger); } catch (Exception e) { System.err.println("main: " +
     * e.getMessage()); } } //
     */
}
