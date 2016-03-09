package sos.scheduler.job;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import sos.hostware.Global;
import sos.spooler.Variable_set;
import sos.util.SOSString;

/** <p>
 * Job zum Überprüfen von Resourcen, die der Scheduler benötigt
 * </p>
 * <h3>Konfiguration:</h3>
 * <p>
 * Der Job kann über die Datenbank des Schedulers, Einstellungen aus der
 * factory.ini-Datei und über Job-Parameter konfiguriert werden. Dabei gilt
 * folgende Hierarchie: Job-Parameter haben Vorrang vor factory.ini, factory.ini
 * vor Datenbank, Datenbank vor defaults
 * </p>
 * 
 * <p>
 * Die Einstellungen teilen sich auf in Kategorien mit Unterkategorien. Zu jedem
 * Kategorie-Unterkategorie Paar wird ein Wert (Value) angegeben.
 * </p>
 * 
 * <p>
 * Je nach Konfigurationsort sind die Einstellungen wie folgt anzugeben:
 * </p>
 * <h4>Job-Parameter, ini-Datei:</h4>
 * <p>
 * Jeweils drei Settings gehören zusammen. Diese tragen die Namen "category_n",
 * "subtype_n", "value_n" mit gleicher Zahl n für alle drei Für weitere
 * Einstellungen ist n zu inkrementieren. n muss bei 1 anfangen, die Werte
 * dürfen keine Lücken aufweisen.
 * </p>
 * 
 * <h4>Datenbank:</h4>
 * <p>
 * In der Tabelle SCHEDULER_SANITY_REFERENCES existieren entsprechende Felder
 * "CATEGORY", "SUBTYPE", "VALUE". Zusaätzlich sollte die ID des entsprechenden
 * Schedulers angegeben werden, ist sie null, gelten die Einstellungen für alle
 * Scheduler, die diese Datenbank verwenden.
 * </p>
 * 
 * <h4>Kategorien:</h4>
 * 
 * <p>
 * <strong>disk_space:</strong> Überprüfung des Festplattenplatzes, SUBTYPE gibt
 * das Verzeichnis an, dessen Platz geprüft werden soll, Value gibt den minimal
 * erlaubten Wert an, bevor eine Warnung ausgegeben wird.
 * </p>
 * 
 * <p>
 * <strong>free_memory:</strong> Überprüfung des Speichers. Value gibt den
 * minmal erlaubten Wert in Bytes an. SUBTYPE kann sein:
 * <ul>
 * <li>jvm: Freier Speicher innerhalb der java virtual machine</li>
 * </ul>
 * </p>
 * 
 * @author andreas.pueschel@sos-berlin.com
 * @since 1.0 2005-01-29 */
public class JobSchedulerCheckSanity extends JobSchedulerJob {

    private class DiskCheck {

        public String location;
        public long minDiskSpace;

        public DiskCheck(String loc, long ds) {
            location = loc;
            minDiskSpace = ds;
        }
    }

    /** Verbindung zur Datenbank */
    // private Connection connection = null;

    /** Prüfungskategorien */
    private Properties checkReferences = null;

    /** Parameter: check disk space */
    private boolean checkDiskSpace = true;

    /** Parameter: check disk space for Scheduler user */
    private boolean checkDiskSpaceUser = true;

    /** Parameter: check available memory */
    private boolean checkMemorySize = true;

    /** Parameter: minimum disk space */
    private long minDiskSpace = 10000000; // 10MB

    /** Parameter: check minimum available memory */
    private long minMemorySize = 0;

    /** Attribut: maxRetryConnectCount: Maximale Anzahl Versuche für
     * Verbindungsaufbau bis Fehlerzustand erreicht ist */
    private int maxRetry = 20;

    /** Attribut: maxRetryConnectInterval: Zeitintervall für
     * Wiederholungsversuche für Verbindungsaufbau wenn Fehlerzustand erreicht
     * ist */
    private int maxRetryInterval = 14400; // alle 4 Stunden

    private HashMap diskChecks;

    private HashMap diskChecksUser;

    private HashMap memChecks;

    private Global hostware = null;

    // Strings for History in case of other Errors
    private String subtype = "process";
    private String val = "0";
    private String refVal = "0";

    public boolean spooler_init() {
        boolean rc = super.spooler_init();
        if (!rc)
            return false;

        ArrayList references = null;
        spooler_job.set_delay_after_error(1, maxRetryInterval);
        spooler_job.set_delay_after_error(maxRetry, "STOP");

        try {
            diskChecks = new HashMap();
            diskChecksUser = new HashMap();
            memChecks = new HashMap();
            // initialize hostware
            try {
                spooler_log.debug3("Initializing hostware...");
                hostware = new Global();
            } catch (Exception e) {
                throw new Exception("Failed to initialize hostware: " + e.getMessage());
            }
            checkDataBaseSettings();
            checkINISettings();
            checkJobSettings();
            checkDefaults();

            return true;
        } catch (Exception e) {
            try {
                writeHistory("Job", "init", "0", "0", true, e.getMessage());
            } catch (Exception f) {
            }
            if (spooler_log != null)
                try {
                    spooler_log.error(e.getMessage());
                } catch (Exception ex) {
                }
            return false;
        }

    }

    public boolean spooler_process() {

        long freeDiskSpace = 0;
        long feeMemorySize = 0;

        try {

            if (checkDiskSpace)
                checkDiskSpace(false);
            if (checkDiskSpaceUser)
                checkDiskSpace(true);
            if (checkMemorySize)
                checkMemory();

        } catch (Exception e) {
            writeHistory("Job", subtype, val, refVal, true, e.getMessage());

            spooler_log.error("error occurred checking scheduler sanity: " + e.getMessage());
        }

        return false;
    }

    private void checkDataBaseSettings() throws Exception {
        if (this.getConnection() == null)
            return; // No Database Settings when running without Database
        SOSString sosString = new SOSString();
        try { // to retrieve reference categories and values
            ArrayList results = this.getConnection().getArray("SELECT \"CATEGORY\", \"VALUE\", \"SUBTYPE\" FROM SCHEDULER_SANITY_REFERENCES WHERE \"SPOOLER_ID\" IS NULL AND \"CATEGORY\"='disk_space'");

            ArrayList results2 = this.getConnection().getArray("SELECT \"CATEGORY\", \"VALUE\", \"SUBTYPE\" FROM SCHEDULER_SANITY_REFERENCES WHERE \"SPOOLER_ID\"='"
                    + spooler.id() + "' AND \"CATEGORY\"='disk_space' ");

            results.addAll(results2);
            Iterator iter = results.iterator();

            while (iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                String location = sosString.parseToString(row, "subtype");
                String sMinSpace = sosString.parseToString(row, "value");
                Long minSpace = new Long(minDiskSpace);
                try {
                    minSpace = new Long(sMinSpace);
                } catch (Exception e) {
                }
                if (location.length() > 0) {
                    diskChecks.put(location, minSpace);
                }
            }
            spooler_log.debug3("Found " + diskChecks.size() + " drives/directories to check in database.");

            results = this.getConnection().getArray("SELECT \"CATEGORY\", \"VALUE\", \"SUBTYPE\" FROM SCHEDULER_SANITY_REFERENCES WHERE \"SPOOLER_ID\" IS NULL AND \"CATEGORY\"='disk_space_user'");

            results2 = this.getConnection().getArray("SELECT \"CATEGORY\", \"VALUE\", \"SUBTYPE\" FROM SCHEDULER_SANITY_REFERENCES WHERE \"SPOOLER_ID\"='"
                    + spooler.id() + "' AND \"CATEGORY\"='disk_space_user' ");

            results.addAll(results2);
            iter = results.iterator();

            while (iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                String location = sosString.parseToString(row, "subtype");
                String sMinSpace = sosString.parseToString(row, "value");
                Long minSpace = new Long(minDiskSpace);
                try {
                    minSpace = new Long(sMinSpace);
                } catch (Exception e) {
                }
                if (location.length() > 0) {
                    diskChecksUser.put(location, minSpace);
                }
            }
            spooler_log.debug3("Found " + diskChecksUser.size() + " drives/directories for Scheduler user to check in database.");

            results = this.getConnection().getArray("SELECT \"CATEGORY\", \"VALUE\", \"SUBTYPE\" FROM SCHEDULER_SANITY_REFERENCES WHERE \"SPOOLER_ID\" IS NULL "
                    + "AND \"CATEGORY\"='free_memory' ");
            results2 = this.getConnection().getArray("SELECT \"CATEGORY\", \"VALUE\", \"SUBTYPE\" FROM SCHEDULER_SANITY_REFERENCES WHERE \"SPOOLER_ID\"='"
                    + spooler.id() + "' AND \"CATEGORY\"='free_memory' ");
            results.addAll(results2);
            iter = results.iterator();

            while (iter.hasNext()) {
                HashMap row = (HashMap) iter.next();
                String type = sosString.parseToString(row, "subtype");
                String sMinMem = sosString.parseToString(row, "value");
                Long minMem = new Long(minMemorySize);
                try {
                    minMem = new Long(sMinMem);
                } catch (Exception e) {
                }
                if (type.length() > 0) {
                    memChecks.put(type, minMem);
                }
            }

        } catch (Exception e) {
            throw (new Exception("error occurred retrieving database settings: " + e.getMessage()));
        }
    }

    private void checkINISettings() throws Exception {
        try {
            int dsCounter = 0;
            int dsuCounter = 0;
            SOSString sosString = new SOSString();
            Properties settings = getJobProperties();
            Enumeration keys = settings.keys();
            while (keys.hasMoreElements()) {
                String key = sosString.parseToString(keys.nextElement());
                if (key.startsWith("category_")) {
                    String number = key.substring(9);
                    String category = sosString.parseToString(settings, key);
                    String subtype = sosString.parseToString(settings, "subtype_" + number);
                    String value = sosString.parseToString(settings, "value_" + number);
                    if (category.equalsIgnoreCase("disk_space")) {
                        Long minSpace = new Long(minDiskSpace);
                        try {
                            minSpace = new Long(value);
                        } catch (Exception e) {
                        }
                        if (subtype.length() > 0) {
                            diskChecks.put(subtype, minSpace);
                            dsCounter++;
                        }
                    }
                    if (category.equalsIgnoreCase("disk_space_user")) {
                        Long minSpace = new Long(minDiskSpace);
                        try {
                            minSpace = new Long(value);
                        } catch (Exception e) {
                        }
                        if (subtype.length() > 0) {
                            diskChecksUser.put(subtype, minSpace);
                            dsuCounter++;
                        }
                    }
                    if (category.equalsIgnoreCase("free_memory")) {
                        Long minMem = new Long(minMemorySize);
                        try {
                            minMem = new Long(value);
                        } catch (Exception e) {
                        }
                        if (subtype.length() > 0) {
                            memChecks.put(subtype, minMem);
                        }
                    }
                }
            }
            if (settings.getProperty("delay_after_error") != null) {
                String[] delays = settings.getProperty("delay_after_error").toString().split(";");
                if (delays.length > 0)
                    spooler_job.clear_delay_after_error();
                for (int i = 0; i < delays.length; i++) {
                    String[] delay = delays[i].split(":");
                    spooler_job.set_delay_after_error(Integer.parseInt(delay[0]), delay[1]);
                }
            }
            spooler_log.debug6("Found " + dsCounter + " drives/directories to check in ini.");
            spooler_log.debug6("Found " + dsuCounter + " drives/directories for Scheduler user to check in ini.");
        } catch (Exception e) {
            throw (new Exception("error occurred retrieving ini settings: " + e.getMessage()));
        }
    }

    private void checkJobSettings() throws Exception {
        try {
            int dsCounter = 0;
            int dsuCounter = 0;
            SOSString sosString = new SOSString();
            Variable_set params = spooler_task.params();
            if (params.var("check_disk_space") != null && params.var("check_disk_space").length() > 0) {
                String sCheckDiskSpace = params.var("check_disk_space");
                if (sCheckDiskSpace.equalsIgnoreCase("0") || sCheckDiskSpace.equalsIgnoreCase("false") || sCheckDiskSpace.endsWith("no")) {
                    checkDiskSpace = false;
                }
            }
            if (params.var("check_disk_space_user") != null && params.var("check_disk_space_user").length() > 0) {
                String sCheckDiskSpace = params.var("check_disk_space_user");
                if (sCheckDiskSpace.equalsIgnoreCase("0") || sCheckDiskSpace.equalsIgnoreCase("false") || sCheckDiskSpace.endsWith("no")) {
                    checkDiskSpaceUser = false;
                }
            }
            if (params.var("check_free_memory") != null && params.var("check_free_memory").length() > 0) {
                String sCheckDiskSpace = params.var("check_free_memory");
                if (sCheckDiskSpace.equalsIgnoreCase("0") || sCheckDiskSpace.equalsIgnoreCase("false") || sCheckDiskSpace.endsWith("no")) {
                    checkMemorySize = false;
                }
            }
            int number = 1;
            boolean found = true;
            while (found) {
                if (params.var("category_" + number) != null && params.var("category_" + number).length() > 0) {
                    String category = params.var("category_" + number);
                    String subtype = params.var("subtype_" + number);
                    String value = params.var("value_" + number);
                    if (subtype != null && value != null) {
                        if (category.equalsIgnoreCase("disk_space")) {
                            Long minSpace = new Long(minDiskSpace);
                            try {
                                minSpace = new Long(value);
                            } catch (Exception e) {
                            }
                            if (subtype.length() > 0) {
                                diskChecks.put(subtype, minSpace);
                                dsCounter++;
                            }
                        }
                        if (category.equalsIgnoreCase("disk_space_user")) {
                            Long minSpace = new Long(minDiskSpace);
                            try {
                                minSpace = new Long(value);
                            } catch (Exception e) {
                            }
                            if (subtype.length() > 0) {
                                diskChecksUser.put(subtype, minSpace);
                                dsCounter++;
                            }
                        }
                        if (category.equalsIgnoreCase("free_memory")) {
                            Long minMem = new Long(minMemorySize);
                            try {
                                minMem = new Long(value);
                            } catch (Exception e) {
                            }
                            if (subtype.length() > 0) {
                                memChecks.put(subtype, minMem);
                                dsCounter++;
                            }
                        }
                    }
                    number++;
                } else
                    found = false;
            }
            spooler_log.debug6("Found " + dsCounter + " drives/directories to check in job params.");
            spooler_log.debug6("Found " + dsuCounter + " drives/directories for Scheduler user to check in job params.");
        } catch (Exception e) {
            throw (new Exception("error occurred retrieving job params: " + e.getMessage()));
        }
    }

    private void checkDefaults() {
        if (diskChecks.isEmpty() && diskChecksUser.isEmpty() && memChecks.isEmpty()) {
            memChecks.put("ram", new Long(minMemorySize));
            diskChecks.put(spooler.directory(), new Long(minDiskSpace));
        }
    }

    /** Überprüft den Platz auf den in der Hashmap diskChecks(User) angegebenen
     * Verzeichnissen
     * 
     * @param user bei true wird der für den Scheduler user verfügbare Platz
     *            überprüft
     * @throws Exception */
    private void checkDiskSpace(boolean user) throws Exception {
        spooler_log.debug3("Checking diskspace...");
        String sUser = "";
        String historyKey = "disk_space";
        Iterator iter = null;
        if (user) {
            sUser = " for Scheduler user";
            historyKey = "disk_space_user";
            iter = diskChecksUser.keySet().iterator();
        } else
            iter = diskChecks.keySet().iterator();

        try {
            while (iter.hasNext()) {
                long freeDiskSpace = 0;
                SOSString sosString = new SOSString();
                String location = sosString.parseToString(iter.next());
                long minDS = 0;
                if (user)
                    minDS = ((Long) diskChecksUser.get(location)).longValue();
                else
                    minDS = ((Long) diskChecks.get(location)).longValue();
                spooler_log.info("Checking disk space on partition " + location + sUser);
                subtype = location;
                refVal = "" + minDS;
                if (user) {
                    spooler_log.debug6("Calling hostware.long_system_information(\"total_free_disk_space\"," + location + ")");
                    freeDiskSpace = hostware.long_system_information("total_free_disk_space", location);
                } else {
                    spooler_log.debug6("Calling hostware.long_system_information(\"total_free_disk_space\"," + location + ")");
                    freeDiskSpace = hostware.long_system_information("free_disk_space", location);
                }
                val = "" + freeDiskSpace;
                if (freeDiskSpace <= minDS) {
                    String message = "free disk space on partition " + location + sUser + " has fallen below minimum value [" + minDS / 1048576
                            + "MB]: " + freeDiskSpace / 1048576 + "MB";
                    if (spooler_log != null)
                        spooler_log.error(message);
                    writeHistory(historyKey, location, "" + freeDiskSpace, "" + minDS, true, message);
                } else {
                    if (spooler_log != null)
                        spooler_log.info("minimum disk space on partition " + location + sUser + ": " + minDS / 1048576 + "MB" + ", free disk space"
                                + sUser + ": " + freeDiskSpace / 1048576 + "MB");
                    writeHistory(historyKey, location, "" + freeDiskSpace, "" + minDS, false, "");
                }
            }
        } catch (Exception e) {
            throw (new Exception("error occurred checking diskspace" + sUser + ": " + e.getMessage()));
        }
    }

    private long getFreeSpaceOnWindows(String path) throws Exception {
        long bytesFree = -1;

        File script = new File(System.getProperty("java.io.tmpdir"), "sos_script.bat");
        PrintWriter writer = new PrintWriter(new FileWriter(script, false));
        writer.println("dir \"" + path + "\"");
        writer.close();

        // get the output from running the .bat file
        Process p = Runtime.getRuntime().exec(script.getAbsolutePath());
        InputStream reader = new BufferedInputStream(p.getInputStream());
        StringBuffer buffer = new StringBuffer();
        for (;;) {
            int c = reader.read();
            if (c == -1)
                break;
            buffer.append((char) c);
        }
        String outputText = buffer.toString();
        reader.close();

        // parse the output text for the bytes free info
        StringTokenizer tokenizer = new StringTokenizer(outputText, "\n");
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken().trim();
            // see if line contains the bytes free information
            if (line.endsWith("bytes free") || line.endsWith("Bytes frei")) {
                tokenizer = new StringTokenizer(line, " ");
                tokenizer.nextToken();
                tokenizer.nextToken();
                bytesFree = Long.parseLong(tokenizer.nextToken().replaceAll(",", "").replaceAll("\\.", ""));
            }
        }
        return bytesFree;
    }

    private long getFreeSpaceOnUnix(String path) throws Exception {
        long bytesFree = -1;
        // -B 1 block Größe 1 Byte
        Process p = Runtime.getRuntime().exec("df -B 1 " + "/" + path);
        InputStream reader = new BufferedInputStream(p.getInputStream());
        StringBuffer buffer = new StringBuffer();
        for (;;) {
            int c = reader.read();
            if (c == -1)
                break;
            buffer.append((char) c);
        }
        String outputText = buffer.toString();
        reader.close();

        // parse the output text for the bytes free info
        StringTokenizer tokenizer = new StringTokenizer(outputText, "\n");
        tokenizer.nextToken();
        if (tokenizer.hasMoreTokens()) {
            String line2 = tokenizer.nextToken();
            StringTokenizer tokenizer2 = new StringTokenizer(line2, " ");
            if (tokenizer2.countTokens() >= 4) {
                tokenizer2.nextToken();
                tokenizer2.nextToken();
                tokenizer2.nextToken();
                bytesFree = Long.parseLong(tokenizer2.nextToken());
                return bytesFree;
            }
        }

        return bytesFree;
    }

    private void checkMemory() throws Exception {
        subtype = "mem";
        val = "0";
        refVal = "0";
        Iterator iter = memChecks.keySet().iterator();
        try {
            while (iter.hasNext()) {
                subtype = "mem";
                val = "0";
                refVal = "0";
                long freeMemory = 0;
                SOSString sosString = new SOSString();
                String type = sosString.parseToString(iter.next());
                subtype = type;
                long minMem = ((Long) memChecks.get(type)).longValue();
                refVal = "" + minMem;
                spooler_log.info("Checking free memory [" + type + "]...");
                if (type.equalsIgnoreCase("jvm")) {
                    spooler_log.debug6("Calling Runtime.getRuntime().freeMemory()");
                    freeMemory = Runtime.getRuntime().freeMemory();
                } else if (type.equalsIgnoreCase("ram")) {
                    spooler_log.debug6("Calling hostware.long_system_information(\"free_memory\")");
                    freeMemory = hostware.long_system_information("free_memory");
                } else if (type.equalsIgnoreCase("swap")) {
                    spooler_log.debug6("Calling hostware.long_system_information(\"free_swap\")");
                    freeMemory = hostware.long_system_information("free_swap");
                } else {
                    spooler_log.info("unknown memory-type: " + type);
                    continue;
                }
                val = "" + freeMemory;

                if (freeMemory <= minMem) {
                    String message = "free memory [" + type + "] has fallen below minimum value [" + minMem / 1048576 + "MB]: " + freeMemory
                            / 1048576 + "MB";
                    if (spooler_log != null)
                        spooler_log.error(message);
                    writeHistory("free_memory", type, "" + freeMemory, "" + minMem, true, message);
                } else {
                    if (spooler_log != null)
                        spooler_log.info("minimum memory [" + type + "]: " + minMem / 1048576 + "MB" + ", free memory: " + freeMemory / 1048576
                                + "MB");
                    writeHistory("free_memory", type, "" + freeMemory, "" + minMem, false, "");
                }

            }
        } catch (Exception e) {
            throw (new Exception("error occurred checking memory: " + e.getMessage()));
        }
    }

    private void writeHistory(String category, String subType, String value, String refValue, boolean failed, String message) {
        if (this.getConnection() == null)
            return; // No Database Settings when running without Database
        String currentStateText = "";
        if (message != null)
            currentStateText = message;
        if (currentStateText != null && currentStateText.length() > 250) {
            currentStateText = currentStateText.substring(currentStateText.length() - 250);
        }
        int id = 0;
        if (spooler_task != null)
            id = spooler_task.id();
        String sFailed = "0";
        if (failed)
            sFailed = "1";
        // Einen Backslash gegen zwei tauschen:
        subType = subType.replaceAll("\\\\", "\\\\\\\\");
        currentStateText = currentStateText.replaceAll("'", "''");
        String query = "INSERT INTO SCHEDULER_SANITY_HISTORY (\"SPOOLER_ID\", \"JOB_ID\","
                + " \"CATEGORY\", \"SUBTYPE\", \"VALUE\", \"REFERENCE_VALUE\", \"FAILED\"," + " \"MESSAGE\", \"CREATED\", \"CREATED_BY\") VALUES ("
                + "'" + spooler.id() + "', '" + id + "', '" + category + "', '" + subType + "', '" + value + "', '" + refValue + "', " + sFailed
                + ", '" + currentStateText + "', %now, '" + spooler_job.name() + "')";
        try {
            getConnection().execute(query);
            getConnection().commit();
        } catch (Exception e) {
            try {
                getConnection().rollback();
                spooler_log.warn("an error occured writing history: " + e.getMessage());
            } catch (Exception f) {
            }
        }
    }

}
