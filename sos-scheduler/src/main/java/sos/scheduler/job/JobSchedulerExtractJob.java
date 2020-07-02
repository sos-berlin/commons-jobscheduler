package sos.scheduler.job;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sos.hostware.Record;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.util.SOSFile;
import sos.util.SOSOrderedHashtable;

/** @author andreas pueschel
 * @author ghassan beydoun */
public class JobSchedulerExtractJob extends JobSchedulerJob {

    private String inputFilePath = "";
    private String inputFileSpec = "";
    private String inputFilenamePrefix = "";
    private String inputFileType = "file";
    private String inputDateFormat = "";
    private String outputFilePath = "";
    private String outputFilenamePrefix = "";
    private String outputFilenameExtension = "";
    private String outputFileType = "file";
    private String outputDateFormat = "";
    private String outputFieldNames = "";
    private String outputFieldSeparator = "";
    private String outputFieldDelimiter = "";
    private String outputFieldDelimiterQuote = "";
    private int maxErrors = 0;
    private Variable_set parameters = null;

    public boolean spooler_process() {
        boolean rc = true;
        long recordCount = 0;
        try {
            this.setParameters(spooler.create_variable_set());
            try {
                if (spooler_task.params() != null) {
                    this.getParameters().merge(spooler_task.params());
                }
                if (spooler_job.order_queue() != null) {
                    this.getParameters().merge(spooler_task.order().params());
                }
                if (this.getParameters().value("input_file") != null && !this.getParameters().value("input_file").isEmpty()) {
                    this.setInputFilePath(this.getParameters().value("input_file"));
                    spooler_log.debug1(".. parameter [input_file]: " + this.getInputFilePath());
                } else {
                    this.setInputFilePath(".");
                }
                if (this.getParameters().value("input_file_spec") != null && !this.getParameters().value("input_file_spec").isEmpty()) {
                    this.setInputFileSpec(this.getParameters().value("input_file_spec"));
                    spooler_log.debug1(".. parameter [input_file_spec]: " + this.getInputFileSpec());
                } else {
                    this.setInputFileSpec("^(.*)$");
                }
                if (this.getParameters().value("input_file_prefix") != null && !this.getParameters().value("input_file_prefix").isEmpty()) {
                    this.setInputFilenamePrefix(this.getParameters().value("input_file_prefix"));
                    spooler_log.debug1(".. parameter [input_file_prefix]: " + this.getInputFilenamePrefix());
                } else {
                    this.setInputFilenamePrefix("-in tab -csv -field-names | ");
                }
                if (this.getParameters().value("input_dateformat") != null && !this.getParameters().value("input_dateformat").isEmpty()) {
                    this.setInputDateFormat(this.getParameters().value("input_dateformat"));
                    spooler_log.debug1(".. parameter [input_dateformat]: " + this.getInputDateFormat());
                } else {
                    this.setInputDateFormat("yyyy-MM-dd");
                }
                if (this.getParameters().value("output_file") != null && !this.getParameters().value("output_file").isEmpty()) {
                    this.setOutputFilePath(getDateReplacement(this.getParameters().value("output_file")));
                    spooler_log.debug1(".. parameter [output_file]: " + this.getOutputFilePath());
                } else {
                    throw new Exception("no output file or directory was specified by parameter [output_file]");
                }
                if (this.getParameters().value("output_file_prefix") != null && !this.getParameters().value("output_file_prefix").isEmpty()) {
                    this.setOutputFilenamePrefix(this.getParameters().value("output_file_prefix"));
                    spooler_log.debug1(".. parameter [output_file_prefix]: " + this.getOutputFilenamePrefix());
                } else {
                    this.setOutputFilenamePrefix("-out | ");
                }
                if (this.getParameters().value("output_file_extension") != null && !this.getParameters().value("output_file_extension").isEmpty()) {
                    this.setOutputFilenameExtension(this.getParameters().value("output_file_extension"));
                    spooler_log.debug1(".. parameter [output_file_extension]: " + this.getOutputFilenameExtension());
                } else {
                    this.setOutputFilenameExtension("");
                }
                if (this.getParameters().value("output_dateformat") != null && !this.getParameters().value("output_dateformat").isEmpty()) {
                    this.setOutputDateFormat(this.getParameters().value("output_dateformat"));
                    spooler_log.debug1(".. parameter [output_dateformat]: " + this.getOutputDateFormat());
                } else {
                    this.setOutputDateFormat("yyyy-MM-dd");
                }
                if (this.getParameters().value("output_field_names") != null && !this.getParameters().value("output_field_names").isEmpty()) {
                    this.setOutputFieldNames(this.getParameters().value("output_field_names"));
                    spooler_log.debug1(".. parameter [output_field_names]: " + this.getOutputFieldNames());
                } else {
                    this.setOutputFieldNames("");
                }
                if (this.getParameters().value("output_field_separator") != null && !this.getParameters().value("output_field_separator").isEmpty()) {
                    this.setOutputFieldSeparator(this.getParameters().value("output_field_separator"));
                    spooler_log.debug1(".. parameter [output_field_separator]: " + this.getOutputFieldSeparator());
                } else {
                    this.setOutputFieldSeparator(";");
                }
                if (this.getParameters().value("output_field_delimiter") != null && !this.getParameters().value("output_field_delimiter").isEmpty()) {
                    this.setOutputFieldDelimiter(this.getParameters().value("output_field_delimiter"));
                    spooler_log.debug1(".. parameter [output_field_delimiter]: " + this.getOutputFieldDelimiter());
                } else {
                    this.setOutputFieldDelimiter("");
                }
                if (this.getParameters().value("output_field_delimiter_quote") != null
                        && !this.getParameters().value("output_field_delimiter_quote").isEmpty()) {
                    this.setOutputFieldDelimiterQuote(this.getParameters().value("output_field_delimiter_quote"));
                    spooler_log.debug1(".. parameter [output_field_delimiter_quote]: " + this.getOutputFieldDelimiterQuote());
                } else {
                    this.setOutputFieldDelimiterQuote("");
                }
                if (this.getParameters().value("max_errors") != null && !this.getParameters().value("max_errors").isEmpty()) {
                    try {
                        this.setMaxErrors(Integer.parseInt(this.getParameters().value("max_errors")));
                    } catch (Exception ex) {
                        throw new Exception("illegal, non-numeric value for maximum number of errors: " + this.getParameters().value("max_errors"));
                    }
                    spooler_log.debug1(".. parameter [max_errors]: " + this.getMaxErrors());
                } else {
                    this.setMaxErrors(-1);
                }
                if (this.getInputFilenamePrefix().toLowerCase().indexOf("-class=") != -1
                        || this.getInputFilenamePrefix().toLowerCase().indexOf("-conn-str=") > -1
                        || this.getInputFilePath().toLowerCase().indexOf("-class=") > -1
                        || this.getInputFilePath().toLowerCase().indexOf("-conn-str=") > -1) {
                    this.setInputFileType("database");
                } else {
                    this.setInputFileType("file");
                }
                if (this.getOutputFilenamePrefix().toLowerCase().indexOf("-class=") != -1
                        || this.getOutputFilenamePrefix().toLowerCase().indexOf("-conn-str=") > -1
                        || this.getOutputFilePath().toLowerCase().indexOf("-class=") > -1
                        || this.getOutputFilePath().toLowerCase().indexOf("-conn-str=") > -1) {
                    this.setOutputFileType("database");
                } else {
                    this.setOutputFileType("file");
                }
            } catch (Exception e) {
                throw new Exception("error occurred processing parameters: " + e.getMessage());
            }
            if ("database".equals(this.getInputFileType())) {
                this.getLogger().info("extracting records from database query: " + this.getInputFilenamePrefix() + " " + this.getInputFilePath());
                File outputFile = new File(this.getOutputFilePath());
                if (outputFile.isDirectory()) {
                    throw new Exception(
                            "for an input database query specified by the parameter [input_file] an output file instead of a directory must be "
                                    + "specified by the parameter [output_file]");
                }
                recordCount = this.extract(null, outputFile);
            } else if ("file".equals(this.getInputFileType())) {
                File inputFile = new File(this.getInputFilePath());
                if (!inputFile.exists()) {
                    throw new Exception("input file does not exist: " + inputFile.getAbsolutePath());
                }
                if (!inputFile.canRead()) {
                    throw new Exception("cannot access input file: " + inputFile.getAbsolutePath());
                }
                File outputFile = new File(this.getOutputFilePath());
                if (inputFile.isDirectory()) {
                    if (!outputFile.isDirectory()) {
                        throw new Exception(
                                "for an input directory specified by the parameter [input_file] an output directory must be specified by the parameter [output_file]");
                    }
                    Vector<File> filelist = SOSFile.getFilelist(inputFile.getAbsolutePath(), this.getInputFileSpec(), 0);
                    int count = 0;
                    for (File extractFile : filelist) {
                        outputFile =
                                new File(this.getOutputFilePath() + "/" + extractFile.getName().substring(0, extractFile.getName().lastIndexOf("."))
                                        + this.getOutputFilenameExtension());
                        spooler_log.debug("starting extraction for input file [" + extractFile.getAbsolutePath() + "] to output file ["
                                + outputFile.getAbsolutePath() + "]");
                        recordCount = this.extract(extractFile, outputFile);
                        count++;
                    }
                    if (count == 0) {
                        throw new Exception("no matching input files found");
                    }
                    this.getLogger().info(count + ((count == 1) ? " file has" : " files have") + " been extracted");
                } else {
                    if (outputFile.isDirectory()) {
                        throw new Exception(
                                "for an input file specified by the parameter [input_file] an output file must be specified by the parameter [output_file]");
                    }
                    spooler_log.debug("starting extraction for single input file [" + inputFile.getCanonicalPath() + "] to output file ["
                            + outputFile.getCanonicalPath() + "]");
                    recordCount = this.extract(inputFile, outputFile);
                }
            }
            return spooler_job.order_queue() != null ? rc : false;
        } catch (Exception e) {
            spooler_log.error("error occurred extracting records from "
                    + ("file".equals(this.getInputFileType()) ? this.getInputFilePath() : "database query") + ": " + e.getMessage());
            return false;
        }
    }

    public long extract(File inputFile, File outputFile) throws Exception {
        sos.hostware.File inFile = null;
        sos.hostware.File outFile = null;
        SOSOrderedHashtable outputFields = new SOSOrderedHashtable();
        boolean outputFieldNamesWrite = false;
        int posTypeBegin = 0;
        int posTypeEnd = 0;
        String fieldName = "";
        long fieldCount = 0;
        long recordCount = 0;
        long successRecordCount = 0;
        long errorRecordCount = 0;
        try {
            if ("\\".equals(this.getOutputFieldDelimiterQuote())) {
                this.setOutputFieldDelimiterQuote(this.getOutputFieldDelimiterQuote() + this.getOutputFieldDelimiterQuote());
            }
            inFile = new sos.hostware.File();
            if ("database".equals(this.getInputFileType())) {
                if (this.getInputFilePath().startsWith("-")) {
                    inFile.open(this.getInputFilePath());
                } else {
                    inFile.open(this.getInputFilenamePrefix() + this.getInputFilePath());
                }
            } else if ("file".equals(this.getInputFileType())) {
                if (inputFile != null && inputFile.getName().startsWith("-")) {
                    inFile.open(inputFile.getName());
                } else if (inputFile != null) {
                    inFile.open(this.getInputFilenamePrefix() + inputFile.getAbsolutePath());
                } else {
                    throw new Exception("no input file was specified");
                }
            } else {
                throw new Exception("illegal input file type: " + this.getInputFileType());
            }
            outFile = new sos.hostware.File();
            if ("database".equals(this.getOutputFileType())) {
                if (this.getOutputFilePath().startsWith("-")) {
                    outFile.open(this.getOutputFilePath());
                } else {
                    outFile.open(this.getOutputFilenamePrefix() + this.getOutputFilePath());
                }
            } else if ("file".equals(this.getOutputFileType())) {
                if (outputFile != null && outputFile.getName().startsWith("-")) {
                    outFile.open(outputFile.getName());
                } else if (outputFile != null) {
                    outFile.open(this.getOutputFilenamePrefix() + outputFile.getAbsolutePath());
                } else {
                    throw new Exception("no output file was specified");
                }
            } else {
                throw new Exception("unsupported output file type: " + this.getOutputFileType());
            }
            if (this.getOutputFieldNames() != null && !this.getOutputFieldNames().isEmpty()) {
                if ("true".equalsIgnoreCase(this.getOutputFieldNames()) || "yes".equalsIgnoreCase(this.getOutputFieldNames())
                        || "1".equals(this.getOutputFieldNames())) {
                    outputFieldNamesWrite = true;
                } else if (!"false".equalsIgnoreCase(this.getOutputFieldNames()) && !"no".equalsIgnoreCase(this.getOutputFieldNames())
                        && !"0".equals(this.getOutputFieldNames())) {
                    String[] fields = this.getOutputFieldNames().split(",");
                    String line = "";
                    for (int i = 0; i < fields.length; i++) {
                        if (i > 0) {
                            line += this.getOutputFieldSeparator();
                        }
                        int pos = fields[i].toLowerCase().indexOf(":");
                        if (pos > -1) {
                            line += fields[i].substring(0, pos);
                        } else {
                            line += fields[i];
                        }
                    }
                    outFile.put_line(line);
                }
            }
            String outputFieldNames = this.getOutputFilenamePrefix() + this.getOutputFilePath() + " ";
            posTypeBegin = outputFieldNames.toLowerCase().indexOf("-type=(");
            if (posTypeBegin > 0) {
                posTypeEnd = outputFieldNames.indexOf(") ", posTypeBegin);
                if (posTypeEnd > 0) {
                    String[] fields = outputFieldNames.substring(posTypeBegin + 7, posTypeEnd).split(",");
                    int pos = 0;
                    for (int i = 0; i < fields.length; i++) {
                        pos = fields[i].toLowerCase().indexOf(":");
                        if (pos > -1) {
                            outputFields.put(fields[i].substring(0, pos).toLowerCase(), fields[i].substring(pos + 1));
                        } else {
                            outputFields.put(fields[i].toLowerCase(), "string");
                        }
                    }
                }
            }
            if (outputFields.isEmpty()) {
                String inputFieldNames = this.getInputFilenamePrefix() + this.getInputFilePath() + " ";
                posTypeBegin = inputFieldNames.toLowerCase().indexOf("-type=(");
                if (posTypeBegin > 0) {
                    posTypeEnd = inputFieldNames.indexOf(") ", posTypeBegin);
                    if (posTypeEnd > 0) {
                        String[] fields = inputFieldNames.substring(posTypeBegin + 7, posTypeEnd).split(",");
                        for (int i = 0; i < fields.length; i++) {
                            int pos = fields[i].indexOf(":");
                            if (pos > -1) {
                                outputFields.put(fields[i].substring(0, pos).toLowerCase(), fields[i].substring(pos + 1));
                            } else {
                                outputFields.put(fields[i].toLowerCase(), "string");
                            }
                        }
                    }
                }
            }
            while (!inFile.eof()) {
                Record record = inFile.get();
                String line = "";
                recordCount++;
                fieldCount = 0;
                if (outputFieldNamesWrite) {
                    if (outputFields.isEmpty()) {
                        for (int i = 0; i < record.field_count(); i++) {
                            if (i > 0) {
                                line += this.getOutputFieldSeparator();
                            }
                            line += record.field_name(i);
                        }
                    } else {
                        Iterator it = outputFields.iterateKeys();
                        int itCount = 0;
                        while (it.hasNext()) {
                            if (itCount > 0) {
                                line += this.getOutputFieldSeparator();
                            }
                            line += (String) it.next();
                            itCount++;
                        }
                    }
                    outFile.put_line(line);
                    line = "";
                    outputFieldNamesWrite = false;
                }
                if (outputFields.isEmpty() && recordCount == 1) {
                    for (int i = 0; i < record.field_count(); i++) {
                        outputFields.put(record.field_name(i).toLowerCase(), "string");
                    }
                }
                try {
                    int outputFieldCount = 0;
                    Iterator it = outputFields.iterateKeys();
                    while (it.hasNext()) {
                        fieldName = (String) it.next();
                        fieldCount++;
                        if (record.string(fieldName) != null && !record.string(fieldName).isEmpty()) {
                            String fieldType = (String) outputFields.get(fieldName.toLowerCase());
                            if (fieldType != null && "string".equals(fieldType.toLowerCase())) {
                                if (!this.getOutputFieldDelimiterQuote().isEmpty()) {
                                    if (outputFieldCount > 0) {
                                        line += this.getOutputFieldSeparator();
                                    }
                                    line +=
                                            this.getOutputFieldDelimiter()
                                                    + record.string(fieldName).replaceAll("\\" + this.getOutputFieldDelimiter(),
                                                            this.getOutputFieldDelimiterQuote() + this.getOutputFieldDelimiter())
                                                    + this.getOutputFieldDelimiter();
                                    outputFieldCount++;
                                } else {
                                    if (outputFieldCount > 0) {
                                        line += this.getOutputFieldSeparator();
                                    }
                                    line += this.getOutputFieldDelimiter() + record.string(fieldName) + this.getOutputFieldDelimiter();
                                    outputFieldCount++;
                                }
                            } else if (fieldType != null && fieldType.toLowerCase().indexOf("date") > -1) {
                                try {
                                    if (outputFieldCount > 0) {
                                        line += this.getOutputFieldSeparator();
                                    }
                                    line +=
                                            this.getOutputFieldDelimiter()
                                                    + SOSDate.getDateAsString(SOSDate.getDate(record.string(fieldName), this.getInputDateFormat()),
                                                            this.getOutputDateFormat()) + this.getOutputFieldDelimiter();
                                    outputFieldCount++;
                                } catch (Exception e) {
                                    throw new Exception("could not convert date using output date format [" + this.getOutputDateFormat() + "]: "
                                            + record.string(fieldName));
                                }
                            } else if (fieldType == null || fieldType.isEmpty()) {
                                // skip record
                            } else {
                                throw new Exception("unsupported field type specified for field [" + fieldName + "]: " + fieldType);
                            }
                        } else {
                            if (outputFieldCount > 0) {
                                line += this.getOutputFieldSeparator();
                            }
                            line += this.getOutputFieldDelimiter() + this.getOutputFieldDelimiter();
                            outputFieldCount++;
                        }
                    }
                    if (!line.isEmpty()) {
                        outFile.put_line(line);
                        successRecordCount++;
                    }
                } catch (Exception e) {
                    errorRecordCount++;
                    if (this.getMaxErrors() <= errorRecordCount) {
                        throw new Exception("max. number of errors [" + this.getMaxErrors() + "] exceeded [" + errorRecordCount + "]: "
                                + e.getMessage());
                    } else {
                        this.getLogger().info("error [" + errorRecordCount + " of " + this.getMaxErrors() + "] occurred: " + e.getMessage());
                    }
                }
            }
            this.getLogger().info(
                    recordCount + " records found, " + errorRecordCount + " errors, " + successRecordCount + " records extracted from "
                            + ("file".equals(this.getInputFileType()) ? inputFile.getAbsolutePath() : "database query"));
            return successRecordCount;
        } catch (Exception e) {
            throw new Exception("error occurred [record " + recordCount + ", field index " + fieldCount + "]: " + e.getMessage());
        } finally {
            if (inFile != null) {
                try {
                    if (inFile.opened()) {
                        inFile.close();
                    }
                } catch (Exception ex) {
                    // ignore this error
                }
            }
            if (outFile != null) {
                try {
                    if (outFile.opened()) {
                        outFile.close();
                    }
                } catch (Exception ex) {
                    // ignore this error
                }
            }
        }
    }

    public Variable_set getParameters() {
        return parameters;
    }

    public void setParameters(Variable_set parameters) {
        this.parameters = parameters;
    }

    public String getInputDateFormat() {
        return inputDateFormat;
    }

    public void setInputDateFormat(String inputDateFormat) {
        this.inputDateFormat = inputDateFormat;
    }

    public String getInputFilenamePrefix() {
        return inputFilenamePrefix;
    }

    public void setInputFilenamePrefix(String inputFilenamePrefix) {
        this.inputFilenamePrefix = inputFilenamePrefix;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public String getInputFileSpec() {
        return inputFileSpec;
    }

    public void setInputFileSpec(String inputFileSpec) {
        this.inputFileSpec = inputFileSpec;
    }

    public String getInputFileType() {
        return inputFileType;
    }

    public void setInputFileType(String inputFileType) {
        this.inputFileType = inputFileType;
    }

    public int getMaxErrors() {
        return maxErrors;
    }

    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }

    public String getOutputDateFormat() {
        return outputDateFormat;
    }

    public void setOutputDateFormat(String outputDateFormat) {
        this.outputDateFormat = outputDateFormat;
    }

    public String getOutputFieldDelimiter() {
        return outputFieldDelimiter;
    }

    public void setOutputFieldDelimiter(String outputFieldDelimiter) {
        this.outputFieldDelimiter = outputFieldDelimiter;
    }

    public String getOutputFieldDelimiterQuote() {
        return outputFieldDelimiterQuote;
    }

    public void setOutputFieldDelimiterQuote(String outputFieldDelimiterQuote) {
        this.outputFieldDelimiterQuote = outputFieldDelimiterQuote;
    }

    public String getOutputFieldNames() {
        return outputFieldNames;
    }

    public void setOutputFieldNames(String outputFieldNames) {
        this.outputFieldNames = outputFieldNames;
    }

    public String getOutputFieldSeparator() {
        return outputFieldSeparator;
    }

    public void setOutputFieldSeparator(String outputFieldSeparator) {
        this.outputFieldSeparator = outputFieldSeparator;
    }

    public String getOutputFilenameExtension() {
        return outputFilenameExtension;
    }

    public void setOutputFilenameExtension(String outputFilenameExtension) {
        this.outputFilenameExtension = outputFilenameExtension;
    }

    public String getOutputFilenamePrefix() {
        return outputFilenamePrefix;
    }

    public void setOutputFilenamePrefix(String outputFilenamePrefix) {
        this.outputFilenamePrefix = outputFilenamePrefix;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public String getOutputFileType() {
        return outputFileType;
    }

    public void setOutputFileType(String outputFileType) {
        this.outputFileType = outputFileType;
    }

    public static String getDateReplacement(String fileName) throws Exception {
        String regExp = "(.*)(\\[date:)(\\s*)([yYmMDdhHsS_]+)(\\s*)(\\])(.*)";
        StringBuilder sb = new StringBuilder();
        Pattern pattern = Pattern.compile(regExp);
        Matcher matcher = pattern.matcher(fileName);
        boolean found = matcher.find();
        if (found) {
            if (matcher.group(1) != null && matcher.group(1) != null) {
                sb.append(matcher.group(1));
            }
            if (matcher.group(4) == null && matcher.group(1).isEmpty()) {
                throw new Exception("Could not find date mask to convert!!");
            } else {
                sb.append(SOSDate.getCurrentTimeAsString(matcher.group(4)));
            }
            sb.append(matcher.group(7));
            fileName = sb.toString();
        }
        return fileName;
    }

}