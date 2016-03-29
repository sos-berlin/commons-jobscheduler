package sos.scheduler.db;

import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.scheduler.messages.JSMessages;

/** @author KB */
public class JobSchedulerPLSQLJob extends JSJobUtilitiesClass<JobSchedulerPLSQLJobOptions> {

    protected static final String conSettingDBMS_OUTPUT = "dbmsOutput";
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerPLSQLJob.class);
    private CallableStatement cs = null;
    private Connection objConnection = null;
    private DbmsOutput dbmsOutput = null;
    private String strOutput = "";
    private String strSqlError = "";

    public JobSchedulerPLSQLJob() {
        super(new JobSchedulerPLSQLJobOptions());
    }

    public JobSchedulerPLSQLJob Execute() throws Exception {
        final String methodName = "JobSchedulerPLSQLJob::Execute";
        JSJ_I_110.toLog(methodName);
        objJSJobUtilities.setJSParam(conSettingSQL_ERROR, "");
        try {
            Options().CheckMandatory();
            LOGGER.debug(Options().dirtyString());
            DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
            objConnection = DriverManager.getConnection(objOptions.db_url.Value(), objOptions.db_user.Value(), objOptions.db_password.Value());
            String plsql = objOptions.command.unescapeXML().replace("\r\n", "\n");
            plsql = objJSJobUtilities.replaceSchedulerVars(false, plsql);
            objOptions.replaceVars(plsql);
            dbmsOutput = new DbmsOutput(objConnection);
            dbmsOutput.enable(1000000);
            cs = objConnection.prepareCall(plsql);
            cs.execute();
        } catch (SQLException e) {
            LOGGER.error(JSMessages.JSJ_F_107.get(methodName), e);
            String strT = String.format("SQL Exception raised. Msg='%1$s', Status='%2$s'", e.getMessage(), e.getSQLState());
            LOGGER.error(strT);
            strSqlError = strT;
            objJSJobUtilities.setJSParam(conSettingSQL_ERROR, strT);
            throw new JobSchedulerException(strT, e);
        } catch (Exception e) {
            throw new JobSchedulerException(JSMessages.JSJ_F_107.get(methodName), e);
        } finally {
            objJSJobUtilities.setJSParam(conSettingDBMS_OUTPUT, "");
            objJSJobUtilities.setJSParam(conSettingSTD_OUT_OUTPUT, "");
            strOutput = dbmsOutput.getOutput();
            if (strOutput != null) {
                objJSJobUtilities.setJSParam(conSettingDBMS_OUTPUT, strOutput);
                objJSJobUtilities.setJSParam(conSettingSTD_OUT_OUTPUT, strOutput);
                int intRegExpFlags = Pattern.CASE_INSENSITIVE + Pattern.MULTILINE + Pattern.DOTALL;
                String strA[] = strOutput.split("\n");
                boolean flgAVariableFound = false;
                String strRegExp = objOptions.VariableParserRegExpr.Value();
                if (!strRegExp.isEmpty()) {
                    Pattern objRegExprPattern = Pattern.compile(strRegExp, intRegExpFlags);
                    for (String string : strA) {
                        Matcher objMatch = objRegExprPattern.matcher(string);
                        if (objMatch.matches()) {
                            objJSJobUtilities.setJSParam(objMatch.group(1), objMatch.group(2).trim());
                            flgAVariableFound = true;
                        }
                    }
                }
                dbmsOutput.close();
                if (!flgAVariableFound) {
                    LOGGER.info(String.format("no JS-variable definitions found using reg-exp '%1$s'.", strRegExp));
                }
                ResultSetMetaData csmd = cs.getMetaData();
                if (csmd != null) {
                    int nCols;
                    nCols = csmd.getColumnCount();
                    for (int i = 1; i <= nCols; i++) {
                        System.out.print(csmd.getColumnName(i));
                        int colSize = csmd.getColumnDisplaySize(i);
                        for (int k = 0; k < colSize - csmd.getColumnName(i).length(); k++) {
                            System.out.print(" ");
                        }
                    }
                    System.out.println("");
                }
            }
            if (cs != null) {
                cs.close();
                cs = null;
            }
            if (objConnection != null) {
                objConnection.close();
                objConnection = null;
            }
        }
        LOGGER.debug(JSMessages.JSJ_I_111.get(methodName));
        return this;
    }

    public void init() {
        doInitialize();
    }

    private void doInitialize() {
        // doInitialize
    } 

    public String getSqlError() {
        return strSqlError;
    }

    public String getOutput() {
        return strOutput;
    }

}