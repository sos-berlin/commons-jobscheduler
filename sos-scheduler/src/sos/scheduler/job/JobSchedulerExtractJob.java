package sos.scheduler.job;


import java.io.File;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import sos.hostware.Record;
import sos.scheduler.job.JobSchedulerJob;
import sos.spooler.Variable_set;
import sos.util.SOSDate;
import sos.util.SOSFile;
import sos.util.SOSOrderedHashtable;

/**
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @author ghassan.beydoun@sos-berlin.com
 *
 *    This job is used to extract records from a csv file or from a database to a file
 *
 */

public class JobSchedulerExtractJob extends JobSchedulerJob { 

    /** file name samples:            
        - default without field names: -in -type=(ppid,pid,client,application,process,host,account,script,log_file,start_time:Datetime('yyyy-mm-dd HH:MM:SS'),end_time:Datetime('yyyy-mm-dd HH:MM:SS'),state,message) tab -csv
        - sample:                      -in tab -csv -field-names                -> separator=; delimiter="
        - sample:                      -in tab -quote='x' -tab='y' -field-names -> separator=y delimiter=x
        - sample for ODBC:             -in odbc -conn-str='DRIVER=Microsoft Access Driver (*.mdb);DBQ=c:\loginfo\application_loginfo.mdb' SELECT "PPID", "PID", "CLIENT", "APPLICATION", "PROCESS", "HOST", "ACCOUNT", "SCRIPT_FILE" as "SCRIPT", "LOG_FILE", "START_TIME", "END_TIME", "STATE", "LOG_MESSAGE" as "MESSAGE" FROM APPLICATION_LOGS ORDER BY "START_TIME" ASC
        - sample for JDBC:             -in jdbc -class=oracle.jdbc.driver.OracleDriver jdbc:oracle:thin:@localhost:1521:orcl -user=appman -password=appman SELECT "PPID", "PID", "CLIENT", "APPLICATION", "PROCESS", "HOST", "ACCOUNT", "SCRIPT_FILE" as "SCRIPT", "START_TIME", "END_TIME", "STATE", "LOG_FILE", "LOG_MESSAGE" as "MESSAGE" FROM APPLICATION_LOGS ORDER BY "START_TIME" ASC
    */
    
    /** input directory or file name */
    private String inputFilePath          = "";

    /** input file specification (regular expression) */
    private String inputFileSpec          = "";

    /** hostWare file name prefix for input file */
    private String inputFilenamePrefix    = "";

    /** type of input file: database or file */
    private String inputFileType          = "file";
    
    /** Standard date format for input files (ISO) */
    private String inputDateFormat        = "";


    /** output directory or file name */
    private String outputFilePath          = "";

    /** hostWare file name prefix for output file */
    private String outputFilenamePrefix    = "";
    
    /** output file extension */
    private String outputFilenameExtension = "";
    
    /** type of output file: database or file */
    private String outputFileType          = "file";
    
    /** Standard date format for output files (ISO) */
    private String outputDateFormat        = "";

    /** list of field names that are written at top of the output file */
    private String outputFieldNames        = "";
    
    /** output file field separator character(s) */
    private String outputFieldSeparator    = "";
    
    /** output file field delimiter character(s) */
    private String outputFieldDelimiter    = "";
    
    /** character that quotes the field delimiter within field values of output files */
    private String outputFieldDelimiterQuote = "";
    
    
    /** max. number of tolerated errors */
    private int maxErrors                  = 0;
    
    /** order parameters */
    private Variable_set parameters        = null;
    

    
    public boolean spooler_process() {

        boolean rc = true;
        long recordCount = 0;
        
        try {
            this.setParameters(spooler.create_variable_set()); 
            
            try {
                if (spooler_task.params() != null) this.getParameters().merge(spooler_task.params());
                if (spooler_job.order_queue() != null) this.getParameters().merge(spooler_task.order().params());
                
                
                /* input file processing parameters */
                
                if (this.getParameters().value("input_file") != null && this.getParameters().value("input_file").length() > 0) {
                    this.setInputFilePath(this.getParameters().value("input_file"));
                    spooler_log.debug1(".. parameter [input_file]: " + this.getInputFilePath());
                } else {
                    this.setInputFilePath(".");
                }

                if (this.getParameters().value("input_file_spec") != null && this.getParameters().value("input_file_spec").length() > 0) {
                    this.setInputFileSpec(this.getParameters().value("input_file_spec"));
                    spooler_log.debug1(".. parameter [input_file_spec]: " + this.getInputFileSpec());
                } else {
                    this.setInputFileSpec("^(.*)$");
                }

                if (this.getParameters().value("input_file_prefix") != null && this.getParameters().value("input_file_prefix").length() > 0) {
                    this.setInputFilenamePrefix(this.getParameters().value("input_file_prefix"));
                    spooler_log.debug1(".. parameter [input_file_prefix]: " + this.getInputFilenamePrefix());
                } else {
                    this.setInputFilenamePrefix("-in tab -csv -field-names | ");
                }
                
                if (this.getParameters().value("input_dateformat") != null && this.getParameters().value("input_dateformat").length() > 0) {
                    this.setInputDateFormat(this.getParameters().value("input_dateformat"));
                    spooler_log.debug1(".. parameter [input_dateformat]: " + this.getInputDateFormat());
                } else {
                    this.setInputDateFormat("yyyy-MM-dd");
                }
                
                
                /* output file processing parameters */
                
                if (this.getParameters().value("output_file") != null && this.getParameters().value("output_file").length() > 0) {
                    this.setOutputFilePath( getDateReplacement( this.getParameters().value("output_file") ) );
                    spooler_log.debug1(".. parameter [output_file]: " + this.getOutputFilePath());
                } else {
                    throw new Exception("no output file or directory was specified by parameter [output_file]");
                }

                if (this.getParameters().value("output_file_prefix") != null && this.getParameters().value("output_file_prefix").length() > 0) {
                    this.setOutputFilenamePrefix(this.getParameters().value("output_file_prefix"));
                    spooler_log.debug1(".. parameter [output_file_prefix]: " + this.getOutputFilenamePrefix());
                } else {
                    this.setOutputFilenamePrefix("-out | ");
                }
                
                if (this.getParameters().value("output_file_extension") != null && this.getParameters().value("output_file_extension").length() > 0) {
                    this.setOutputFilenameExtension(this.getParameters().value("output_file_extension"));
                    spooler_log.debug1(".. parameter [output_file_extension]: " + this.getOutputFilenameExtension());
                } else {
                    this.setOutputFilenameExtension("");
                }
                
                if (this.getParameters().value("output_dateformat") != null && this.getParameters().value("output_dateformat").length() > 0) {
                    this.setOutputDateFormat(this.getParameters().value("output_dateformat"));
                    spooler_log.debug1(".. parameter [output_dateformat]: " + this.getOutputDateFormat());
                } else {
                    this.setOutputDateFormat("yyyy-MM-dd");
                }
                
                if (this.getParameters().value("output_field_names") != null && this.getParameters().value("output_field_names").length() > 0) {
                    this.setOutputFieldNames(this.getParameters().value("output_field_names"));
                    spooler_log.debug1(".. parameter [output_field_names]: " + this.getOutputFieldNames());
                } else {
                    this.setOutputFieldNames("");
                }
                
                if (this.getParameters().value("output_field_separator") != null && this.getParameters().value("output_field_separator").length() > 0) {
                    this.setOutputFieldSeparator(this.getParameters().value("output_field_separator"));
                    spooler_log.debug1(".. parameter [output_field_separator]: " + this.getOutputFieldSeparator());
                } else {
                    this.setOutputFieldSeparator(";");
                }

                if (this.getParameters().value("output_field_delimiter") != null && this.getParameters().value("output_field_delimiter").length() > 0) {
                    this.setOutputFieldDelimiter(this.getParameters().value("output_field_delimiter"));
                    spooler_log.debug1(".. parameter [output_field_delimiter]: " + this.getOutputFieldDelimiter());
                } else {
                    this.setOutputFieldDelimiter("");
                }
                
                if (this.getParameters().value("output_field_delimiter_quote") != null && this.getParameters().value("output_field_delimiter_quote").length() > 0) {
                    this.setOutputFieldDelimiterQuote(this.getParameters().value("output_field_delimiter_quote"));
                    spooler_log.debug1(".. parameter [output_field_delimiter_quote]: " + this.getOutputFieldDelimiterQuote());
                } else {
                    this.setOutputFieldDelimiterQuote("");
                }
                
                
                /* additional processing parameters */
                
                if (this.getParameters().value("max_errors") != null && this.getParameters().value("max_errors").length() > 0) {
                    try {
                       this.setMaxErrors(Integer.parseInt(this.getParameters().value("max_errors")));
                    } catch (Exception ex) {
                        throw new Exception("illegal, non-numeric value for maximum number of errors: " + this.getParameters().value("max_errors"));
                    }
                    spooler_log.debug1(".. parameter [max_errors]: " + this.getMaxErrors());
                } else {
                    this.setMaxErrors(-1);
                }

                
                /* determine the input and output types */

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
            
            
            if (this.getInputFileType().equals("database")) { 
                this.getLogger().info("extracting records from database query: " + this.getInputFilenamePrefix() + " " + this.getInputFilePath() );
                File outputFile = new File(this.getOutputFilePath());
                if (outputFile.isDirectory()) throw new Exception("for an input database query specified by the parameter [input_file] an output file instead of a directory must be specified by the parameter [output_file]");
                recordCount = this.extract(null, outputFile);
            } 
            else if (this.getInputFileType().equals("file")) {
                File inputFile = new File(this.getInputFilePath());
                if (!inputFile.exists())  throw new Exception ("input file does not exist: " + inputFile.getAbsolutePath());
                if (!inputFile.canRead()) throw new Exception ("cannot access input file: "  + inputFile.getAbsolutePath());

                File outputFile = new File(this.getOutputFilePath());

                if (inputFile.isDirectory()) {
                    if (!outputFile.isDirectory()) throw new Exception("for an input directory specified by the parameter [input_file] an output directory must be specified by the parameter [output_file]");
                    Vector filelist = SOSFile.getFilelist( inputFile.getAbsolutePath(), this.getInputFileSpec(), 0);
                    Iterator iterator = filelist.iterator();
                    int count = 0;
                    while(iterator.hasNext()) {
                        File extractFile = (File)iterator.next();
                        outputFile = new File(this.getOutputFilePath() + "/" + extractFile.getName().substring(0, extractFile.getName().lastIndexOf(".")) + this.getOutputFilenameExtension());
                        spooler_log.debug("starting extraction for input file [" + extractFile.getAbsolutePath() + "] to output file [" + outputFile.getAbsolutePath() + "]" );
                        recordCount = this.extract(extractFile, outputFile);
                        count++;
                    }
                    if (count == 0) throw new Exception("no matching input files found");
                    this.getLogger().info(count + ((count == 1) ? " file has" : " files have") + " been extracted");
                } else {
                    if (outputFile.isDirectory()) throw new Exception("for an input file specified by the parameter [input_file] an output file must be specified by the parameter [output_file]");
                    spooler_log.debug("starting extraction for single input file [" + inputFile.getCanonicalPath() + "] to output file [" + outputFile.getCanonicalPath() + "]" );
                    recordCount = this.extract(inputFile, outputFile);
                }
            }
            
            return ((spooler_job.order_queue() != null) ? rc : false);
            
        } catch (Exception e) {
            spooler_log.error("error occurred extracting records from " + (this.getInputFileType().equals("file") ? this.getInputFilePath() : "database query") + ": " + e.getMessage());
            return false;
        }
    }

     
    /*
    * extract from database query or csv file to target file or database and return number of successfully extracted records 
    **/
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
            if (this.getOutputFieldDelimiterQuote().equals("\\")) this.setOutputFieldDelimiterQuote(this.getOutputFieldDelimiterQuote() + this.getOutputFieldDelimiterQuote());

            inFile = new sos.hostware.File();

            if (this.getInputFileType().equals("database")) {
                // hostWare file names start with "-"
                if (this.getInputFilePath().startsWith("-")) {
                    inFile.open(this.getInputFilePath());
                } else {
                    inFile.open(this.getInputFilenamePrefix() + this.getInputFilePath());
                }
            } else if (this.getInputFileType().equals("file")) {
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
            
            if (this.getOutputFileType().equals("database")) {
                // hostWare file names start with "-"
                if (this.getOutputFilePath().startsWith("-")) {
                    outFile.open(this.getOutputFilePath());
                } else {
                    outFile.open(this.getOutputFilenamePrefix() + this.getOutputFilePath());
                }
            } else if (this.getOutputFileType().equals("file")) {
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


            // optionally add field names to output file
            if (this.getOutputFieldNames() != null && this.getOutputFieldNames().length() > 0) {
                if (this.getOutputFieldNames().equalsIgnoreCase("true") || this.getOutputFieldNames().equalsIgnoreCase("yes") || this.getOutputFieldNames().equals("1") ) {
                    outputFieldNamesWrite = true;
                } else if (!this.getOutputFieldNames().equalsIgnoreCase("false") && !this.getOutputFieldNames().equalsIgnoreCase("no") && !this.getOutputFieldNames().equals("0") ) {
                    String[] fields = this.getOutputFieldNames().split(",");
                    String line = "";
                    for(int i=0; i<fields.length; i++) {
                        if (i>0) line += this.getOutputFieldSeparator();
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

            // lookup output fields from the output file specification ...
            String outputFieldNames = this.getOutputFilenamePrefix() + this.getOutputFilePath() + " ";
            posTypeBegin = outputFieldNames.toLowerCase().indexOf("-type=(");
            if (posTypeBegin > 0) {
                posTypeEnd = outputFieldNames.indexOf(") ", posTypeBegin);
                if (posTypeEnd > 0) {
                    String[] fields = outputFieldNames.substring(posTypeBegin+7, posTypeEnd).split(",");
                    int pos = 0;
                    for(int i=0; i<fields.length; i++) {
                        pos = fields[i].toLowerCase().indexOf(":");
                        if (pos > -1) {
                            outputFields.put(fields[i].substring(0, pos).toLowerCase(), fields[i].substring(pos+1));   
                        } else {
                            outputFields.put(fields[i].toLowerCase(), "string");
                        }
                    }
                }
            }
            
            // ... should no output fields have been found, then lookup output fields from the input file specification
            if (outputFields.isEmpty()) {
                String inputFieldNames = this.getInputFilenamePrefix() + this.getInputFilePath() + " ";
                posTypeBegin = inputFieldNames.toLowerCase().indexOf("-type=(");
                if (posTypeBegin > 0) {
                    posTypeEnd = inputFieldNames.indexOf(") ", posTypeBegin);
                    if (posTypeEnd > 0) {
                        String[] fields = inputFieldNames.substring(posTypeBegin+7, posTypeEnd).split(",");
                        for(int i=0; i<fields.length; i++) {
                            int pos = fields[i].indexOf(":");
                            if (pos > -1) {
                                outputFields.put(fields[i].substring(0, pos).toLowerCase(), fields[i].substring(pos+1));   
                            } else {
                                outputFields.put(fields[i].toLowerCase(), "string");
                            }
                        }
                    }
                }
            }
            
            while (!inFile.eof())
            {
                Record record = inFile.get();
                String line = "";
                recordCount++;
                fieldCount = 0;

                if (outputFieldNamesWrite) {
                    if (outputFields.isEmpty()) {
                        for(int i=0; i<record.field_count(); i++) {
                            if (i>0) line += this.getOutputFieldSeparator();
                            line += record.field_name(i);
                        }
                    } else {
                        Iterator it = outputFields.iterateKeys();
                        int itCount = 0;
                        while(it.hasNext()) {
                            if (itCount>0) line += this.getOutputFieldSeparator();
                            line += (String) it.next();
                            itCount++;
                        }
                    }
                    outFile.put_line(line);
                    line = "";
                    outputFieldNamesWrite = false;
                }

                // should no output fields have been found, then use them from the first ecord
                if (outputFields.isEmpty() && recordCount == 1) {
                    for(int i=0; i<record.field_count(); i++) {
                        outputFields.put(record.field_name(i).toLowerCase(), "string");   
                    }
                }
                
                try {
                    int outputFieldCount = 0;
                    Iterator it = outputFields.iterateKeys();
                    while(it.hasNext()) {
                        fieldName = (String) it.next();
                        fieldCount++;

                        if (record.string(fieldName) != null && record.string(fieldName).length() > 0) {
                            String fieldType = (String) outputFields.get(fieldName.toLowerCase());
                            if (fieldType != null && fieldType.toLowerCase().equals("string")) {
                                if (this.getOutputFieldDelimiterQuote().length() > 0) {
                                    if (outputFieldCount>0) line += this.getOutputFieldSeparator();
                                    line += this.getOutputFieldDelimiter() + record.string(fieldName).replaceAll("\\" + this.getOutputFieldDelimiter(), this.getOutputFieldDelimiterQuote() + this.getOutputFieldDelimiter()) + this.getOutputFieldDelimiter();
                                    outputFieldCount++;
                                } else {
                                    if (outputFieldCount>0) line += this.getOutputFieldSeparator();
                                    line += this.getOutputFieldDelimiter() + record.string(fieldName) + this.getOutputFieldDelimiter();
                                    outputFieldCount++;
                                }
                            } else if (fieldType != null && fieldType.toLowerCase().indexOf("date") > -1) {
                                try {
                                    if (outputFieldCount>0) line += this.getOutputFieldSeparator();
                                    // line += this.getOutputFieldDelimiter() + SOSDate.getDateAsString(SOSDate.getDate(record.string(fieldName), SOSDate.dateTimeFormat), this.getOutputDateFormat()) + this.getOutputFieldDelimiter();
                                    line += this.getOutputFieldDelimiter() + SOSDate.getDateAsString(SOSDate.getDate(record.string(fieldName), this.getInputDateFormat()), this.getOutputDateFormat()) + this.getOutputFieldDelimiter();
                                    outputFieldCount++;
                                } catch (Exception e) {
                                    throw new Exception("could not convert date using output date format [" + this.getOutputDateFormat() + "]: " + record.string(fieldName));
                                }
                            } else if (fieldType == null || fieldType.length() == 0) {
                                // skip record
                            } else {
                                throw new Exception("unsupported field type specified for field [" + fieldName + "]: " + fieldType);
                            }
                        } else {
                            if (outputFieldCount>0) line += this.getOutputFieldSeparator();
                            line += this.getOutputFieldDelimiter() + this.getOutputFieldDelimiter();
                            outputFieldCount++;
                        }
                    }

                    if (line.length() > 0) {
                        outFile.put_line(line);
                        successRecordCount++;
                    }
                } catch (Exception e) {
                    errorRecordCount++;
                    if (this.getMaxErrors() <= errorRecordCount) {
                        throw new Exception("max. number of errors [" + this.getMaxErrors() + "] exceeded [" + errorRecordCount + "]: " + e.getMessage());
                    } else {
                        this.getLogger().info("error [" + errorRecordCount + " of " + this.getMaxErrors() + "] occurred: " + e.getMessage());
                    }
                }
            }
        
            this.getLogger().info(recordCount + " records found, " + errorRecordCount + " errors, " + successRecordCount + " records extracted from " + (this.getInputFileType().equals("file") ? inputFile.getAbsolutePath() : "database query"));

            return successRecordCount;

        } catch (Exception e) {
            throw new Exception("error occurred [record " + recordCount + ", field index " + fieldCount + "]: " + e.getMessage());
        } finally {
            if (inFile != null) try { if (inFile.opened()) inFile.close(); } catch (Exception ex) {} // ignore this error
            if (outFile != null) try { if (outFile.opened()) outFile.close(); } catch (Exception ex) {} // ignore this error
        }
    }


    /**
     * @return Returns the parameters.
     */
    public Variable_set getParameters() {
        return parameters;
    }


    /**
     * @param parameters The parameters to set.
     */
    public void setParameters(Variable_set parameters) {
        this.parameters = parameters;
    }


    /**
     * @return Returns the inputDateFormat.
     */
    public String getInputDateFormat() {
        return inputDateFormat;
    }


    /**
     * @param inputDateFormat The inputDateFormat to set.
     */
    public void setInputDateFormat(String inputDateFormat) {
        this.inputDateFormat = inputDateFormat;
    }


    /**
     * @return Returns the inputFilenamePrefix.
     */
    public String getInputFilenamePrefix() {
        return inputFilenamePrefix;
    }


    /**
     * @param inputFilenamePrefix The inputFilenamePrefix to set.
     */
    public void setInputFilenamePrefix(String inputFilenamePrefix) {
        this.inputFilenamePrefix = inputFilenamePrefix;
    }


    /**
     * @return Returns the inputFilePath.
     */
    public String getInputFilePath() {
        return inputFilePath;
    }


    /**
     * @param inputFilePath The inputFilePath to set.
     */
    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }


    /**
     * @return Returns the inputFileSpec.
     */
    public String getInputFileSpec() {
        return inputFileSpec;
    }


    /**
     * @param inputFileSpec The inputFileSpec to set.
     */
    public void setInputFileSpec(String inputFileSpec) {
        this.inputFileSpec = inputFileSpec;
    }


    /**
     * @return Returns the inputFileType.
     */
    public String getInputFileType() {
        return inputFileType;
    }


    /**
     * @param inputFileType The inputFileType to set.
     */
    public void setInputFileType(String inputFileType) {
        this.inputFileType = inputFileType;
    }


    /**
     * @return Returns the maxErrors.
     */
    public int getMaxErrors() {
        return maxErrors;
    }


    /**
     * @param maxErrors The maxErrors to set.
     */
    public void setMaxErrors(int maxErrors) {
        this.maxErrors = maxErrors;
    }


    /**
     * @return Returns the outputDateFormat.
     */
    public String getOutputDateFormat() {
        return outputDateFormat;
    }


    /**
     * @param outputDateFormat The outputDateFormat to set.
     */
    public void setOutputDateFormat(String outputDateFormat) {
        this.outputDateFormat = outputDateFormat;
    }


    /**
     * @return Returns the outputFieldDelimiter.
     */
    public String getOutputFieldDelimiter() {
        return outputFieldDelimiter;
    }


    /**
     * @param outputFieldDelimiter The outputFieldDelimiter to set.
     */
    public void setOutputFieldDelimiter(String outputFieldDelimiter) {
        this.outputFieldDelimiter = outputFieldDelimiter;
    }


    /**
     * @return Returns the outputFieldDelimiterQuote.
     */
    public String getOutputFieldDelimiterQuote() {
        return outputFieldDelimiterQuote;
    }


    /**
     * @param outputFieldDelimiterQuote The outputFieldDelimiterQuote to set.
     */
    public void setOutputFieldDelimiterQuote(String outputFieldDelimiterQuote) {
        this.outputFieldDelimiterQuote = outputFieldDelimiterQuote;
    }


    /**
     * @return Returns the outputFieldNames.
     */
    public String getOutputFieldNames() {
        return outputFieldNames;
    }


    /**
     * @param outputFieldNames The outputFieldNames to set.
     */
    public void setOutputFieldNames(String outputFieldNames) {
        this.outputFieldNames = outputFieldNames;
    }


    /**
     * @return Returns the outputFieldSeparator.
     */
    public String getOutputFieldSeparator() {
        return outputFieldSeparator;
    }


    /**
     * @param outputFieldSeparator The outputFieldSeparator to set.
     */
    public void setOutputFieldSeparator(String outputFieldSeparator) {
        this.outputFieldSeparator = outputFieldSeparator;
    }


    /**
     * @return Returns the outputFilenameExtension.
     */
    public String getOutputFilenameExtension() {
        return outputFilenameExtension;
    }


    /**
     * @param outputFilenameExtension The outputFilenameExtension to set.
     */
    public void setOutputFilenameExtension(String outputFilenameExtension) {
        this.outputFilenameExtension = outputFilenameExtension;
    }


    /**
     * @return Returns the outputFilenamePrefix.
     */
    public String getOutputFilenamePrefix() {
        return outputFilenamePrefix;
    }


    /**
     * @param outputFilenamePrefix The outputFilenamePrefix to set.
     */
    public void setOutputFilenamePrefix(String outputFilenamePrefix) {
        this.outputFilenamePrefix = outputFilenamePrefix;
    }


    /**
     * @return Returns the outputFilePath.
     */
    public String getOutputFilePath() {
        return outputFilePath;
    }


    /**
     * @param outputFilePath The outputFilePath to set.
     */
    public void setOutputFilePath(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }


    /**
     * @return Returns the outputFileType.
     */
    public String getOutputFileType() {
        return outputFileType;
    }


    /**
     * @param outputFileType The outputFileType to set.
     */
    public void setOutputFileType(String outputFileType) {
        this.outputFileType = outputFileType;
    }


	public static String getDateReplacement(String fileName) throws Exception {
		String regExp = "(.*)(\\[date:)(\\s*)([yYmMDdhHsS_]+)(\\s*)(\\])(.*)";
		/*
		 group(0): our string itself
		 group(1): prefix
		 group(2): [date:
		 group(3): white space
		 group(4): yyyyMMdd_HHmmss
		 group(5): white space
		 group(6): ]
		 group(7): rest of string
		 */

		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile(regExp);
		Matcher matcher = pattern.matcher(fileName);
		boolean found = matcher.find();
		if (found) {
			if (matcher.group(1) != null && matcher.group(1) != null)
				sb.append(matcher.group(1));

			if (matcher.group(4) == null && matcher.group(1).length() == 0)
				throw new Exception("Could not find date mask to convert!!");
			else
				sb.append(SOSDate.getCurrentTimeAsString(matcher.group(4)));
			sb.append(matcher.group(7));
			fileName = sb.toString();
		}// found

		return fileName;
	}


}
