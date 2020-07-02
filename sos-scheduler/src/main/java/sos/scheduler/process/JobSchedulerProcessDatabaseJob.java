package sos.scheduler.process;

import java.util.Iterator;
import java.util.Map;

import sos.connection.SOSConnection;
import sos.spooler.Order;

/** @author andreas.pueschel@sos-berlin.com
 * @deprecated use sos.scheduler.managed.JobSchedulerManagedDatabaseJob */
public class JobSchedulerProcessDatabaseJob extends ProcessOrderJob {

    protected String command = "";

    public boolean spooler_process() {
        Order order = null;
        String orderId = "(none)";
        boolean rc = true;
        try {
            try {
                if (spooler_job.order_queue() != null) {
                    order = spooler_task.order();
                    orderId = order.id();
                    if (order.params().value("configuration_path") != null && !order.params().value("configuration_path").isEmpty()) {
                        this.setConfigurationPath(order.params().value("configuration_path"));
                    } else if (spooler_task.params().value("configuration_path") != null
                            && !spooler_task.params().value("configuration_path").isEmpty()) {
                        this.setConfigurationPath(spooler_task.params().value("configuration_path"));
                    }
                    if (order.params().value("configuration_file") != null && !order.params().value("configuration_file").isEmpty()) {
                        this.setConfigurationFilename(order.params().value("configuration_file"));
                    } else if (spooler_task.params().value("configuration_file") != null
                            && !spooler_task.params().value("configuration_file").isEmpty()) {
                        this.setConfigurationFilename(spooler_task.params().value("configuration_file"));
                    }
                    this.initConfiguration();
                }
                this.prepare();
                if (this.getParameters().value("db_class") == null || this.getParameters().value("db_class").isEmpty()) {
                    throw new Exception("no parameter [db_class] for database connection class was specified");
                }
                if (this.getParameters().value("db_driver") == null || this.getParameters().value("db_driver").isEmpty()) {
                    throw new Exception("no parameter [db_driver] for database JDBC driver was specified");
                }
                if (this.getParameters().value("db_url") == null || this.getParameters().value("db_url").isEmpty()) {
                    throw new Exception("no parameter [db_url] for database connection url was specified");
                }
                if (this.getParameters().value("db_user") == null || this.getParameters().value("db_user").isEmpty()) {
                    throw new Exception("no parameter [db_user] for database connection user was specified");
                }
                if (this.getParameters().value("db_password") == null) {
                    throw new Exception("no parameter [db_password] for database connection password was specified");
                }
                if (this.getParameters().value("command") != null && !this.getParameters().value("command").isEmpty()) {
                    this.setCommand(this.getParameters().value("command"));
                } else {
                    throw new Exception("no parameter [command] has been specified");
                }
            } catch (Exception e) {
                throw new Exception("error occurred preparing order: " + e.getMessage());
            }
            try {
                try {
                    if (this.getConnection() != null) {
                        try {
                            this.getConnection().rollback();
                            this.getConnection().disconnect();
                        } catch (Exception ex) {}
                    }
                    spooler_log.debug3("connecting to database ...");
                    this.setConnection(SOSConnection.createInstance(this.getParameters().value("db_class"), this.getParameters().value("db_driver"),
                            this.getParameters().value("db_url"), this.getParameters().value("db_user"), this.getParameters().value("db_password")));
                    this.getConnection().connect();
                    spooler_log.debug3("connected to database");
                } catch (Exception e) {
                    throw (new Exception("connect to database failed: " + e.getMessage()));
                }
                try {
                    String[] parameterNames = this.getParameters().names().split(";");
                    for (int i = 0; i < parameterNames.length; i++) {
                        this.setCommand(this.getCommand().replaceAll("\\$\\{" + parameterNames[i] + "\\}",
                                this.getParameters().value(parameterNames[i])));
                    }
                    spooler_log.info("executing database command: " + this.getCommand());
                    this.executeStatements(this.getConnection(), this.getCommand());
                    if (this.getConnection().getResultSet() != null) {
                        String warning = "";
                        Map<String, String> result = null;
                        while (!(result = this.getConnection().get()).isEmpty()) {
                            warning = "execution terminated with warning:";
                            Iterator<String> resultIterator = result.keySet().iterator();
                            while (resultIterator.hasNext()) {
                                String key = resultIterator.next();
                                if (key == null || key.isEmpty()) {
                                    continue;
                                }
                                warning += " " + key + "=" + result.get(key);
                            }
                        }
                        if (warning != null && !warning.isEmpty()) {
                            spooler_log.warn(warning);
                        }
                    }
                } catch (Exception e) {
                    throw (new Exception("database command failed: " + e.getMessage()));
                }
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
            return spooler_task.job().order_queue() != null ? rc : false;
        } catch (Exception e) {
            spooler_log.warn("error occurred processing order [" + orderId + "]: " + e.getMessage());
            return false;
        } finally {
            try {
                this.cleanup();
            } catch (Exception e) {
                //
            }
            if (this.getConnection() != null) {
                try {
                    this.getConnection().rollback();
                    this.getConnection().disconnect();
                } catch (Exception e) {}
            }
        }
    }

    protected void executeStatements(SOSConnection connection, String command) throws Exception {
        connection.executeStatements(command);
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}