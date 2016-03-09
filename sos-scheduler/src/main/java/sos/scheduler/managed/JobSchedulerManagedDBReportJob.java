/*
 * JobSchedulerManagedDBReportJob.java Created on 20.06.2005
 */
package sos.scheduler.managed;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import sos.connection.SOSConnection;
import sos.spooler.Order;
import sos.xml.SOSXMLTransformer;

/** Diese Klasse führt Datanbank-Statements für Managed Orders aus und verschickt
 * einen Report im XML Format. Optional kann das xml mit Hilfe eines Stylesheets
 * in ein anderes Format gewandelt werden.<br>
 * Parameter:<br>
 * <ul>
 * <li><strong>scheduler_order_report_stylesheet<strong>: Pfad zu einem
 * Stylesheet, das für die xslt Transformation verwendet werden soll</li>
 * <li><strong>scheduler_order_report_mailto<strong>: email-Empfänger des
 * Reports.</li>
 * <li><strong>scheduler_order_report_mailcc<strong>: email-cc-Empfänger des
 * Reports.</li>
 * <li><strong>scheduler_order_report_mailbcc<strong>: email-bcc-Empfänger des
 * Reports.</li>
 * <li><strong>scheduler_order_report_subject<strong>: email-subject des
 * Reports.</li>
 * <li><strong>scheduler_order_report_body<strong>: email-body des Reports.</li>
 * <li><strong>scheduler_order_report_asbody<strong>: Bei 1 wird der Report
 * nicht als Attachment, sondern als body der email verschickt.</li>
 * <li><strong>scheduler_order_report_filename<strong>: Der gegebene Dateiname
 * wird für den Namen des Attachments verwendet und für die permanent abgelegte
 * Datei, falls ein Report-Pfad angegeben wird.</li>
 * <li><strong>scheduler_order_report_path<strong>: Wird hier ein Pfad
 * angegeben, so wird der Report nicht nur per email verschickt, sondern auch
 * hier abgelegt.</li>
 * </ul>
 * 
 * @author Andreas Liebert */
public class JobSchedulerManagedDBReportJob extends JobSchedulerManagedDatabaseJob {

    private static final String conParameterSCHEDULER_ORDER_REPORT_STYLESHEET = "scheduler_order_report_stylesheet";
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
            // conn.commit();

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
                if (flgAdjust_column_names == true) {
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
                // getLogger().debug9(debug);
                xml += "   </row>\n";
                results = conn.get();
            }
            conn.closeQuery();
            try {
                conn.disconnect();
            } catch (Exception ex) {
            } // ignore Errors
              // conn.commit();
            xml += "  </rows>\n";
            xml += " </table>\n";
            xml += "</report>";
            getLogger().debug3("Xml generated.");
            reporter.addReplacement("\\[xml\\]", xml);

            File attach = reporter.getReportFile();

            if (getOrderPayload() != null && getOrderPayload().var(conParameterSCHEDULER_ORDER_REPORT_STYLESHEET) != null
                    && getOrderPayload().var(conParameterSCHEDULER_ORDER_REPORT_STYLESHEET).length() > 0) {
                debugParamter(getOrderPayload(), conParameterSCHEDULER_ORDER_REPORT_STYLESHEET);
                File stylesheet = new File(getOrderPayload().var(conParameterSCHEDULER_ORDER_REPORT_STYLESHEET));
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
            getLogger().warn("An error occured creating database report"
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
            if (value == null || value.length() == 0)
                rc += "    <" + normalize(key) + "/>";
            else
                rc += "    <" + normalize(key) + "><![CDATA[" + value + "]]></" + normalize(key) + ">\n";
        }
        return rc;
    }

}
