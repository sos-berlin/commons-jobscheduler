package sos.scheduler.managed.configuration;

import java.io.File;

import sos.spooler.Order;
import sos.util.SOSSchedulerLogger;

/** <p>
 * ConfigurationOrderMonitor implements a Monitor script, which reads parameters
 * for the order (per job chain node) from an XML configuration (file)
 * </p>
 * 
 *
 * * !!!! Attention: this program is using the xml-paylod for making the changes
 * persistent !!!!
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2006-10-05 */

public class ConfigurationOrderMonitor extends ConfigurationBaseMonitor {

    /** Initialisierung vor Verarbeitung eines Auftrags
     * 
     * @see sos.spooler.Monitor_impl#spooler_process_before() */
    @Override
    public boolean spooler_process_before() {

        try { // to map order configuration to this job
            this.setLogger(new SOSSchedulerLogger(spooler_log));
            Order order = spooler_task.order();
            String liveFolder = "";
            // String orderXML = order.xml();
            // Vielleicht besser RegEx
            // ^(?:<\?[^?]+\?>)?\s*<order[^>]+\bjob_chain\s*=\s*["']([^"']+)["'].*
            // statt SOSXMLPath
            // weil man sonst ganz viel Zeug hat (Tonnen von Log-Zeilen), die
            // nicht gebraucht werden
            // JITL-82
            // SOSXMLXPath xp = new SOSXMLXPath(new StringBuffer(orderXML));
            // SOSXMLXPath xp = new SOSXMLXPath(new
            // ByteArrayInputStream(orderXML.getBytes("UTF-8")));

            // String jobChainPath =
            // xp.selectSingleNodeValue("/order/@job_chain");
            String jobChainPath = order.job_chain().path();

            if (order.params().value(conParamNameCONFIGURATION_PATH) != null && order.params().value(conParamNameCONFIGURATION_PATH).length() > 0) {
                spooler_log.debug3(conParamNameCONFIGURATION_PATH + " found in order parameters.");
                this.setConfigurationPath(order.params().value(conParamNameCONFIGURATION_PATH));
            } else if (spooler_task.params().value(conParamNameCONFIGURATION_PATH) != null
                    && spooler_task.params().value(conParamNameCONFIGURATION_PATH).length() > 0) {
                spooler_log.debug3(conParamNameCONFIGURATION_PATH + " found in task parameters.");
                this.setConfigurationPath(spooler_task.params().value(conParamNameCONFIGURATION_PATH));
            } else {
                if (spooler_job.configuration_directory().length() > 0) {
                    File fLiveBaseFolder = new File(spooler.configuration_directory());
                    File sJobChainPath = new File(fLiveBaseFolder, jobChainPath + ".job_chain.xml");
                    this.getLogger().debug7("Looking for job chain configuration path: " + sJobChainPath.getAbsolutePath());
                    if (!sJobChainPath.exists()) {
                        this.getLogger().debug2("Job Chain is probably configured in cache folder and not in live folder...");
                        File fCacheBaseFolder = new File(fLiveBaseFolder.getParentFile(), conDefaultFileName4CACHE);
                        sJobChainPath = new File(fCacheBaseFolder, jobChainPath);
                    }
                    liveFolder = sJobChainPath.getParent();

                    this.setConfigurationPath(liveFolder);
                } else {
                    this.setConfigurationPath(new File(spooler.ini_path()).getParent());
                }
                this.getLogger().debug2(".. parameter [" + conParamNameCONFIGURATION_PATH + "]: " + this.getConfigurationPath());
            }

            if (order.params().value(conParamNameCONFIGURATION_FILE) != null && order.params().value(conParamNameCONFIGURATION_FILE).length() > 0) {
                spooler_log.debug3(conParamNameCONFIGURATION_FILE + " found in order parameters.");
                this.setConfigurationFilename(order.params().value(conParamNameCONFIGURATION_FILE));
            } else if (spooler_task.params().value(conParamNameCONFIGURATION_FILE) != null
                    && spooler_task.params().value(conParamNameCONFIGURATION_FILE).length() > 0) {
                spooler_log.debug3(conParamNameCONFIGURATION_FILE + " found in task parameters.");
                this.setConfigurationFilename(spooler_task.params().value(conParamNameCONFIGURATION_FILE));
            } else {
                if (spooler_job.order_queue() != null) {
                    // this.setConfigurationFilename("scheduler_" +
                    // spooler_task.order().job_chain().name() + "_" +
                    // order.id() + ".config.xml");
                    if (spooler_job.configuration_directory().length() > 0) {
                        File confFile = new File(getConfigurationPath(), order.job_chain().name() + conFileNameExtensionCONFIG_XML);
                        File confOrderFile = new File(getConfigurationPath(), order.job_chain().name() + "," + order.id() + conFileNameExtensionCONFIG_XML);
                        if (confOrderFile.exists()) {
                            this.setConfigurationFilename(confOrderFile.getAbsolutePath());
                            this.getLogger().debug2(".. configuration file for this order exists. order_id:" + order.job_chain().name() + "/" + order.id());
                        } else {
                            this.setConfigurationFilename(confFile.getAbsolutePath());
                            this.getLogger().debug2(".. configuration file for job chain:" + order.job_chain().name() + "=" + this.getConfigurationFilename());
                        }
                    } else {
                        this.setConfigurationFilename("scheduler_" + spooler_task.order().job_chain().name() + conFileNameExtensionCONFIG_XML);
                    }
                    this.getLogger().debug2(".. parameter [" + conParamNameCONFIGURATION_FILE + "]: " + this.getConfigurationFilename());
                }
            }

            // Hier ein Pflaster analog zu ConfigurationBaseMonitor...
            File confFile = null;
            if (this.getConfigurationFilename().startsWith(".") || this.getConfigurationFilename().startsWith("/")
                    || this.getConfigurationFilename().startsWith("\\") || this.getConfigurationFilename().indexOf(":") > -1
                    || this.getConfigurationFilename() == null || this.getConfigurationFilename().length() == 0) {
                confFile = new File(this.getConfigurationFilename());
            } else {
                confFile = new File(this.getConfigurationPath(), this.getConfigurationFilename());
            }

            // Also Task-Parameters should be substituted.
            // Deleted because it has to many side effects.
            /*
             * Variable_set v = spooler.create_variable_set();
             * v.merge(spooler_task.params()); v.merge(
             * spooler_task.order().params());
             * spooler_task.order().params().merge(v);
             */

            if (confFile.exists()) {
                this.initConfiguration();
                this.prepareConfiguration();
            } else {
                if (spooler_task.order().xml_payload() != null) {
                    spooler_log.info("Configuration File: " + confFile.getAbsolutePath() + " not found (Probably running on an agent).");
                    spooler_log.info("Reading configuration from xml payload...");
                    try {
                        this.prepareConfiguration();
                    } catch (Exception e) {
                    }
                }
            }

            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before: " + e.getMessage());
            return false;
        }
    }

    /** Cleanup nach Verarbeitung eines Auftrags
     * 
     * @throws Exception
     * @see sos.spooler.Monitor_impl#spooler_process_after() */

    @Override
    public boolean spooler_process_after(final boolean rc) throws Exception {
        this.cleanupConfiguration();
        return rc;
        /*
         * Ist nicht mehr notwendig, da es on error setback am Knoten gibt.
         * Führt dann zu doppelten setbacks. try { // to map order configuration
         * to this job this.setLogger(new SOSSchedulerLogger(spooler_log));
         * Order order = spooler_task.order(); if (rc == false) { if
         * (order.params() != null && order.params().value("setback") != null &&
         * ( order.params().value("setback").equalsIgnoreCase("false") ||
         * order.params().value("setback").equalsIgnoreCase("no") ||
         * order.params().value("setback").equals("0") )) { // clear setback
         * parameter for other jobs
         * spooler_task.order().params().set_var("setback", ""); } else {
         * spooler_task.order().setback(); } } return rc; } catch (Exception e)
         * { spooler_log.warn("error occurred in spooler_process_after(): " +
         * e.getMessage()); return false; } finally { try {
         * this.cleanupConfiguration(); } catch (Exception e) {} }
         */
    }
}
