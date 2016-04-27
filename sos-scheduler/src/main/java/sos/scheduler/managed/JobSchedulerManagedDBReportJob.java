package sos.scheduler.managed;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import sos.connection.SOSConnection;
import sos.spooler.Order;
import sos.xml.SOSXMLTransformer;

/** @author Andreas Liebert */
public class JobSchedulerManagedDBReportJob extends JobSchedulerManagedDatabaseJob {

    private static final String PARAMETER_SCHEDULER_ORDER_REPORT_STYLESHEET = "scheduler_order_report_stylesheet";
    private String xml;
    private String sql;
    private ManagedReporter reporter;

    @Override
    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        try {
            reporter = new ManagedReporter(this);
        } catch (Exception e) {
            try {
                getLogger().warn("Failed to initialize Job: " + e);
            } catch (Exception ex) {
            }
            rc = false;
        }
        return rc;
    }

    @Override
    protected void executeStatements(final SOSConnection conn, final String command) throws Exception {
        sql = command;
        reporter.setBody("Report for statement:\n[sql]");
        reporter.setSubject("Database Report [taskid]");
        reporter.addReplacement("\\[sql\\]", sql);
        try {
            conn.setColumnNamesCaseSensitivity(flgColumn_names_case_sensitivity);
            conn.executeStatements(command);
            HashMap<String, String> results = conn.get();
            if (results == null) {
                getLogger().info("No results found for query: " + command);
                return;
            }
            reporter.setHasResult(!results.isEmpty());
            xml = "<?xml version=\"1.0\" ?>\n";
            xml += "<report>\n";
            xml += " <table>\n";
            xml += "  <columns>\n";
            Iterator keysIt = results.keySet().iterator();
            while (keysIt.hasNext()) {
                String strColumnName = keysIt.next().toString();
                if (flgAdjust_column_names) {
                    strColumnName = normalize(strColumnName);
                }
                xml += "<column name=\"" + strColumnName + "\"/>\n";
            }
            xml += "  </columns>\n";
            xml += "  <rows>\n";
            while (results != null && !results.isEmpty()) {
                xml += "   <row>\n";
                String debug = writeFields(results);
                xml += debug;
                xml += "   </row>\n";
                results = conn.get();
            }
            conn.closeQuery();
            try {
                conn.disconnect();
            } catch (Exception ex) {
            }
            xml += "  </rows>\n";
            xml += " </table>\n";
            xml += "</report>";
            getLogger().debug3("Xml generated.");
            reporter.addReplacement("\\[xml\\]", xml);
            File attach = reporter.getReportFile();
            if (getOrderPayload() != null && getOrderPayload().var(PARAMETER_SCHEDULER_ORDER_REPORT_STYLESHEET) != null
                    && !getOrderPayload().var(PARAMETER_SCHEDULER_ORDER_REPORT_STYLESHEET).isEmpty()) {
                debugParamter(getOrderPayload(), PARAMETER_SCHEDULER_ORDER_REPORT_STYLESHEET);
                File stylesheet = new File(getOrderPayload().var(PARAMETER_SCHEDULER_ORDER_REPORT_STYLESHEET));
                getLogger().debug9("Calling stylesheet.canRead() ");
                if (!stylesheet.canRead()) {
                    throw new Exception("Could not read stylesheet: " + stylesheet.getAbsolutePath());
                } else {
                    getLogger().debug3("Doing xslt transformation...");
                    try {
                        getLogger().debug6("attach:" + attach.getAbsolutePath());
                        SOSXMLTransformer.transform(xml, stylesheet, attach);
                    } catch (Exception e) {
                        throw new Exception("Error occured during transformation: " + e);
                    }
                    getLogger().debug3("Xslt transformation done.");
                }
            } else {
                FileOutputStream fos = new FileOutputStream(attach);
                fos.write(xml.getBytes());
                fos.flush();
                fos.close();
            }
            reporter.report();
        } catch (Exception e) {
            Order order = spooler_task.order();
            getLogger().warn(
                    "An error occured creating database report"
                            + (order != null ? "  [Job Chain: " + order.job_chain().name() + ", Order:" + order.id() + "]" : "") + ": " + e);
            spooler_task.end();
        }
    }

    private String normalize(final String text) {
        String target;
        try {
            target = text.replaceAll("[^A-Za-z_0-9]", "_");
        } catch (Exception e) {
            try {
                getLogger().warn("An error occured replacing characters in element name \"" + text + "\"");
            } catch (Exception ex) {
            }
            return text;
        }
        return target;
    }

    private String writeFields(final HashMap results) {
        Iterator keysIt = results.keySet().iterator();
        String rc = "";
        while (keysIt.hasNext()) {
            String key = keysIt.next().toString();
            String value = results.get(key).toString();
            if (value == null || value.isEmpty()) {
                rc += "    <" + normalize(key) + "/>";
            } else {
                rc += "    <" + normalize(key) + "><![CDATA[" + value + "]]></" + normalize(key) + ">\n";
            }
        }
        return rc;
    }

}
