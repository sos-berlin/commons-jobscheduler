/********************************************************* begin of preamble
 **
 ** Copyright (C) 2003-2010 Software- und Organisations-Service GmbH. All rights
 * reserved.
 **
 ** This file may be used under the terms of either the
 **
 ** GNU General Public License version 2.0 (GPL)
 **
 ** as published by the Free Software Foundation
 * http://www.gnu.org/licenses/gpl-2.0.txt and appearing in the file LICENSE.GPL
 * included in the packaging of this file.
 **
 ** or the
 **
 ** Agreement for Purchase and Licensing
 **
 ** as offered by Software- und Organisations-Service GmbH in the respective
 * terms of supply that ship with this file.
 **
 ** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. end of preamble */
package sos.ftphistory.job;

import sos.connection.SOSConnection;
import sos.connection.SOSMySQLConnection;
import sos.connection.SOSPgSQLConnection;
import sos.scheduler.job.JobSchedulerJob;
import sos.settings.SOSProfileSettings;
import sos.spooler.Log;
import sos.spooler.Spooler;
import sos.spooler.Variable_set;
import sos.util.SOSLogger;

public class SOSFTPHistory {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSFTPHistory";
    public final String conSVNVersion = "$Id: SOSDataExchangeEngine.java 19091 2013-02-08 12:49:32Z kb $";

    public static String TABLE_FILES = "SOSFTP_FILES";
    public static String TABLE_FILES_HISTORY = "SOSFTP_FILES_HISTORY";
    public static String TABLE_FILES_POSITIONS = "SOSFTP_FILES_POSITIONS";
    public static String SEQ_TABLE_FILES = "SOSFTP_FILES_ID_SEQ";

    /** Testausgaben zulassen: siehe SOSFTPHistory.debugParams() */
    private static boolean _doDebug = false;

    /** Liefert SOSConnection Object
     *
     * - sind die connection Parameter im Auftrag gesetzt, so wird eine
     * vorhandene connection abgebaut und eine neue aufgebaut - werden es keine
     * connection Parameter im Auftrag übergeben, dann wird's geprüft ob eine
     * connection bereits vorhanden ist und wenn nicht - eine neue auf dem Basis
     * von Job Scheduler (factory.ini) aufgebaut
     *
     * @param spooler
     * @param conn
     * @param parameters
     * @param log
     * @return
     * @throws Exception */
    public static SOSConnection getConnection(final Spooler spooler, SOSConnection conn, final Variable_set parameters, final SOSLogger log)
            throws Exception {
        try { // to get the database connection
            if (parameters.value("db_class") != null && parameters.value("db_class").length() > 0) {
                if (conn != null) {
                    try {
                        conn.rollback();
                        conn.disconnect();
                    } catch (Exception ex) {
                    } // gracefully ignore this error
                }

                log.debug3("connecting to database using order params ...");
                conn = SOSConnection.createInstance(parameters.value("db_class"), parameters.value("db_driver"), parameters.value("db_url"), parameters.value("db_user"), parameters.value("db_password"), log);

                conn.connect();
                log.debug3("connected to database using order params");
            } else {
                if (conn == null) {
                    log.debug3("connecting to database using Job Scheduler connection ...");

                    conn = JobSchedulerJob.getSchedulerConnection(new SOSProfileSettings(spooler.ini_path()), log);

                    conn.connect();
                    log.debug3("connected to database using Job Scheduler connection");
                } else {
                    log.debug3("using existing connection");
                }
            }

        } catch (Exception e) {
            throw new Exception("connect to database failed: " + e.getMessage());
        }
        return conn;
    }

    /** Liefert einen Wert abhänging von DB Istaze und der Feldlänge
     *
     * @param val
     * @return */
    public static String getNormalizedValue(String val) {

        if (val != null && val.length() > 0) {
            val = val.toLowerCase().replaceAll("\\\\", "/");
        }

        return val;
    }

    /** Liefert einen Wert abhänging von DB Istaze und der Feldlänge
     *
     * returns a normalized value
     * 
     * @param value field value
     * @param length max. field length
     * @return */
    public static String getNormalizedField(final SOSConnection conn, String value, final int length) {

        if (value == null || value.length() == 0)
            return "";

        value = value.length() > length ? value.substring(0, length) : value;

        if (conn instanceof SOSPgSQLConnection || conn instanceof SOSMySQLConnection) {
            value = value.replaceAll("\\\\", "\\\\\\\\");
        }
        return value.replaceAll("'", "''");
    }

    /** wird nur beim Testen benutzt
     *
     * @param params
     * @param log
     * @throws Exception */
    public static void debugParams(final Variable_set params, final Log log) throws Exception {

        if (_doDebug) {
            if (params != null && params.count() > 0) {

                String[] names = params.names().split(";");
                for (String name : names) {
                    log.info("debugParams : " + name + " = " + params.value(name));

                }
            }
        }
    }

}
