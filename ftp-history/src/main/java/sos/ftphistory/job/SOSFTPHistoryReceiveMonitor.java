package sos.ftphistory.job;

import sos.connection.SOSConnection;
import sos.spooler.Monitor_impl;
import sos.spooler.Variable_set;
import sos.util.SOSClassUtil;
import sos.util.SOSSchedulerLogger;

public class SOSFTPHistoryReceiveMonitor extends Monitor_impl {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSFTPHistoryReceiveMonitor";
    public final String conSVNVersion = "$Id: SOSDataExchangeEngine.java 19091 2013-02-08 12:49:32Z kb $";

    /** Parameter replacing und replacement am Auftrag setzen. Dadurch wird die
     * original Datei beim Transfer nach einem bestimmten Muster umbenannt.
     *
     * Initialisierung vor Verarbeitung eines Auftrags
     * 
     * @see sos.spooler.Monitor_impl#spooler_process_before() */
    @Override
    public boolean spooler_process_before() {

        try {
            /*
             * if(spooler_task.order().params().value("replacing") == null ||
             * spooler_task.order().params().value("replacing").length() == 0){
             * spooler_task.order().params().set_var("replacing","[.]csv"); }
             */

            spooler_task.order().params().set_var("replacing", "[.]csv");
            // siehe SOSFTPHistoryReceiveMonitor.fillPosition#remoteFilename
            spooler_task.order().params().set_var("replacement", "{sos[date:yyyyMMddHHmmssSSS]sos}.csv");

            return true;
        } catch (Exception e) {
            spooler_log.warn("error occurred in spooler_process_before(222): " + e.getMessage());
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * @see sos.spooler.Monitor_impl#spooler_process_after(boolean)
     * @Override
     */
    @Override
    public boolean spooler_process_after(final boolean arg0) throws Exception {

        if (!arg0)
            return arg0;

        SOSConnection conn = null;
        SOSSchedulerLogger log = null;
        Variable_set parameters = null;
        String host = null;
        String remoteDir = null;

        try {
            parameters = spooler.create_variable_set();
            if (spooler_task.params() != null)
                parameters.merge(spooler_task.params());
            if (spooler_job.order_queue() != null)
                parameters.merge(spooler_task.order().params());

            SOSFTPHistory.debugParams(parameters, spooler_log);

            if (parameters != null && parameters.count() > 0) {
                if (parameters.value("ftp_result_files") == "0") {
                    spooler_log.debug9("no files were received");
                } else {
                    host = parameters.value("ftp_host");
                    remoteDir = parameters.value("ftp_remote_dir");

                    if (host != null && host.length() > 0 && remoteDir != null && remoteDir.length() > 0) {
                        try {
                            String[] files = parameters.value("ftp_result_filepaths").split(";");

                            log = new SOSSchedulerLogger(spooler_log);
                            conn = SOSFTPHistory.getConnection(spooler, conn, parameters, log);

                            for (String file : files) {
                                fillPosition(conn, host, remoteDir, file);
                            }
                        } catch (Exception e) {
                            spooler_log.info("ERROR : cannot found position : " + e.getMessage());
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.disconnect();
                                }
                            } catch (Exception e) {
                            }
                        }
                    } else {
                        spooler_log.debug3("missing host or ftp_remote_dir for file positions : host = " + host + " ftp_remote_dir = " + remoteDir);
                    }
                }
            }
        } catch (Exception e) {

        }
        return super.spooler_process_after(arg0);
    }

    /** @param conn
     * @param host
     * @param remoteDir
     * @param file
     * @throws Exception */
    private void fillPosition(final SOSConnection conn, String host, String remoteDir, String file) throws Exception {

        StringBuffer sql = new StringBuffer();
        String remoteFilename = "";
        String localFilename = "";

        try {
            host = SOSFTPHistory.getNormalizedValue(host);
            remoteDir = SOSFTPHistory.getNormalizedValue(remoteDir);
            file = SOSFTPHistory.getNormalizedValue(file);

            String[] fp = file.split("/");
            localFilename = fp[fp.length - 1];

            // bei FTP Übertragung original Datei wird umbenannt
            // aus "1.csv" wird 1{sos20081218132248123sos}.csv
            // {sos + timestamp + sos}
            // siehe spooler_process_before#
            remoteFilename = localFilename.toLowerCase().replaceAll("(\\{sos)(.)*(sos\\})(\\.csv)$", "\\.csv");

            sql = new StringBuffer("select \"LOCAL_FILENAME\" from " + SOSFTPHistory.TABLE_FILES_POSITIONS + " ").append("where \"HOST\" = '"
                    + SOSFTPHistory.getNormalizedField(conn, host, 128) + "' and ").append("   \"REMOTE_DIR\" = '"
                    + SOSFTPHistory.getNormalizedField(conn, remoteDir, 255) + "' and ").append("   \"REMOTE_FILENAME\" = '"
                    + SOSFTPHistory.getNormalizedField(conn, remoteFilename, 255) + "'");

            String lastLocalFileName = conn.getSingleValue(sql.toString());
            // FileSize wird in SOSFTPHistoryJob.importFile() gesetzt
            if (lastLocalFileName == null || lastLocalFileName.length() == 0) {
                sql = new StringBuffer("insert into " + SOSFTPHistory.TABLE_FILES_POSITIONS
                        + "(\"HOST\",\"REMOTE_DIR\",\"REMOTE_FILENAME\",\"LOCAL_FILENAME\",\"FILE_SIZE\",\"POSITION\") ").append("values('"
                        + SOSFTPHistory.getNormalizedField(conn, host, 128) + "','" + SOSFTPHistory.getNormalizedField(conn, remoteDir, 255) + "','"
                        + SOSFTPHistory.getNormalizedField(conn, remoteFilename, 255) + "','"
                        + SOSFTPHistory.getNormalizedField(conn, localFilename, 255) + "',0,0)");
            } else {
                // wir ersetzen alte gefundene local_filename durch die neue
                // (mit dem aktuellen timestamp)
                sql = new StringBuffer("update " + SOSFTPHistory.TABLE_FILES_POSITIONS + " ").append("set \"LOCAL_FILENAME\" = '"
                        + SOSFTPHistory.getNormalizedField(conn, localFilename, 255) + "' ").append("where \"LOCAL_FILENAME\" = '"
                        + SOSFTPHistory.getNormalizedField(conn, lastLocalFileName, 255) + "'");

            }

            conn.execute(sql.toString());
            conn.commit();
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (Exception ee) {
            }
            throw new Exception(SOSClassUtil.getMethodName() + " : " + e.getMessage());
        }
    }

}
