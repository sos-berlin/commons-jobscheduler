package sos.scheduler.managed;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import sos.connection.SOSConnection;
import sos.hostware.Factory_processor;

/** @author Andreas Liebert */
public class JobSchedulerManagedCustomReportJob extends JobSchedulerManagedDatabaseJob {

    private ManagedReporter reporter;
    private Factory_processor processor;
    private boolean hasResult;
    public static final int JAVASCRIPT = 1;
    public static final int PERLSCRIPT = 2;
    public static final int VBSCRIPT = 3;

    @Override
    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        try {
            reporter = new ManagedReporter(this);
        } catch (Exception e) {
            try {
                getLogger().warn("Failed to initialize job: " + e);
            } catch (Exception ex) {
            }
            rc = false;
        }
        return rc;
    }

    @Override
    protected void executeStatements(final SOSConnection conn, final String command) throws Exception {
        hasResult = false;
        reporter.setBody("Report for statement:\n[sql]");
        reporter.setSubject("Database Report [taskid]");
        reporter.addReplacement("\\[sql\\]", command);
        String language = "javascript";
        int iLanguage = JAVASCRIPT;
        File template;
        if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_template") != null
                && !getOrderPayload().var("scheduler_order_report_template").isEmpty()) {
            debugParamter(getOrderPayload(), "scheduler_order_report_template");
            template = new File(getOrderPayload().var("scheduler_order_report_template"));
            reporter.addReplacement("\\[template\\]", getOrderPayload().var("scheduler_order_report_template"));
            if (!template.canRead()) {
                throw new Exception("Could not read template: " + template.getAbsolutePath());
            }
        } else {
            throw new Exception("No template was set.");
        }
        if (getOrderPayload() != null && getOrderPayload().var("scheduler_order_report_language") != null
                && !getOrderPayload().var("scheduler_order_report_language").isEmpty()) {
            debugParamter(getOrderPayload(), "scheduler_order_report_language");
            language = getOrderPayload().var("scheduler_order_report_language");
        }
        if ("perl".equalsIgnoreCase(language) || "perlscript".equalsIgnoreCase(language)) {
            iLanguage = PERLSCRIPT;
        } else if ("vbscript".equalsIgnoreCase(language)) {
            iLanguage = VBSCRIPT;
        }
        String[] blocks = command.split("/\\*\\s*(variable|VARIABLE)\\s*");
        String forerunscript = "";
        processor = new Factory_processor();
        if (blocks.length <= 1) {
            getLogger().info("No variable commands found.");
            super.executeStatements(conn, command);
            HashMap results = conn.get();
            if (results != null && !results.isEmpty()) {
                reporter.setHasResult(true);
            }
            forerunscript = result2variables(results);
        } else {
            int counter = 0;
            while (counter < blocks.length) {
                super.executeStatements(conn, blocks[counter]);
                counter++;
                if (counter < blocks.length) {
                    String paramName = blocks[counter].substring(0, blocks[counter].indexOf("*/")).trim();
                    getLogger().debug3("variable " + paramName + "");
                    forerunscript += result2array(paramName, conn, iLanguage);
                    forerunscript += "\n";
                    blocks[counter] = blocks[counter].substring(blocks[counter].indexOf("*/") + 2);
                    if (blocks[counter].trim().isEmpty()) {
                        counter++;
                    }
                }
            }
        }
        getLogger().debug8("start script:\n" + forerunscript);
        try {
            File reportFile = reporter.getReportFile();
            processor.set_document_filename(reportFile.getAbsolutePath());
            processor.set_language(language);
            processor.set_template_filename(template.getAbsolutePath());
            processor.set_param("-var-char=$");
            if (getOrderPayload().var("scheduler_order_report_template") != null) {
                processor.set_parameter("scheduler_order_report_template", getOrderPayload().var("scheduler_order_report_template"));
            }
            if (getOrderPayload().var("scheduler_order_report_mailto") != null) {
                processor.set_parameter("scheduler_order_report_mailto", getOrderPayload().var("scheduler_order_report_mailto"));
            }
            if (getOrderPayload().var("scheduler_order_report_mailcc") != null) {
                processor.set_parameter("scheduler_order_report_mailcc", getOrderPayload().var("scheduler_order_report_mailcc"));
            }
            if (getOrderPayload().var("scheduler_order_report_mailbcc") != null) {
                processor.set_parameter("scheduler_order_report_mailbcc", getOrderPayload().var("scheduler_order_report_mailbcc"));
            }
            if (getOrderPayload().var("scheduler_order_report_subject") != null) {
                processor.set_parameter("scheduler_order_report_subject", getOrderPayload().var("scheduler_order_report_subject"));
            }
            if (getOrderPayload().var("scheduler_order_report_path") != null) {
                processor.set_parameter("scheduler_order_report_path", getOrderPayload().var("scheduler_order_report_path"));
            }
            if (getOrderPayload().var("scheduler_order_report_filename") != null) {
                processor.set_parameter("scheduler_order_report_filename", getOrderPayload().var("scheduler_order_report_filename"));
            }
            if (orderJob) {
                String[] parameterNames = getOrderPayload().names().split(";");
                for (String paramName : parameterNames) {
                    processor.set_parameter(paramName, getOrderPayload().var(paramName));
                }
            }
            processor.add_parameters();
            if (getLogger() != null) {
                getLogger().debug9(".... trying to parse start script");
            }
            processor.parse(forerunscript);
            getLogger().info("Processing report...");
            processor.process();
            processor.close_output_file();
            getLogger().debug3("Closing factory_processor...");
            reporter.setHasResult(hasResult);
            reporter.report();
        } catch (Exception e) {
            throw new Exception("error occurred processing report: " + e.getMessage(), e);
        } finally {
            processor.close();
        }
    }

    private String result2array(final String paramName, final SOSConnection conn, final int language) throws Exception {
        String forerunscript = "";
        switch (language) {
        case JAVASCRIPT:
            forerunscript = "var " + paramName + " = new Array();\n";
            break;
        case VBSCRIPT:
            break;
        case PERLSCRIPT:
            forerunscript = "my @" + paramName + " = ();\n";
            break;
        }
        HashMap results = conn.get();
        int counter = 0;
        while (results != null && !results.isEmpty()) {
            hasResult = true;
            switch (language) {
            case JAVASCRIPT:
                forerunscript += paramName + "[" + counter + "] = new Object();\n";
                break;
            case VBSCRIPT:
                forerunscript += "Set " + paramName + "(" + counter + ") = CreateObject(\"Scripting.Dictionary\") \n";
                break;
            case PERLSCRIPT:
                forerunscript += "$" + paramName + "[" + counter + "] = ();\n";
                break;
            }
            Iterator keysIt = results.keySet().iterator();
            while (keysIt.hasNext()) {
                String key = keysIt.next().toString();
                String value = results.get(key).toString();
                switch (language) {
                case JAVASCRIPT:
                    value = value.replaceAll("\\\\", "\\\\\\\\");
                    value = value.replaceAll("\"", "\\\\\"");
                    value = value.replaceAll("\r\n", "\n");
                    value = value.replaceAll("\\n", "\\\\n\" ;\n" + paramName + "[" + counter + "] [\"" + key + "\"] += \"");
                    forerunscript += paramName + "[" + counter + "] [\"" + key + "\"] = \"" + value + "\";\n";
                    break;
                case VBSCRIPT:
                    value = value.replaceAll("\"", "\"\"");
                    value = value.replaceAll("\r\n", "\n");
                    value = value.replaceAll("\\n", "\"+ vbcrlf +_\n   \"");
                    forerunscript += paramName + "(" + counter + ").add \"" + key + "\" , \"" + value + "\"\n";
                    break;
                case PERLSCRIPT:
                    value = value.replaceAll("\\\\", "\\\\\\\\");
                    value = value.replaceAll("'", "\\\\'");
                    value = value.replaceAll("\r\n", "\n");
                    forerunscript += "$" + paramName + "[" + counter + "] {\"" + key + "\"} = '" + value + "';\n";
                    break;
                }
            }
            counter++;
            results = conn.get();
        }
        if (language == VBSCRIPT) {
            forerunscript = "Dim " + paramName + "(" + (counter - 1) + ")\n" + forerunscript;
        }
        return forerunscript;
    }

    private String result2variables(final HashMap results) throws Exception {
        String forerunscript = "";
        Iterator keysIt = results.keySet().iterator();
        while (keysIt.hasNext()) {
            hasResult = true;
            String key = keysIt.next().toString();
            String value = results.get(key).toString();
            getLogger().debug9("Setting parameter " + key);
            getLogger().debug9("      Value: " + value);
            processor.set_parameter(key, value);
        }
        return forerunscript;
    }

}