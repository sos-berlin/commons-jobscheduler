package sos.scheduler.job.impl;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.sos.JSHelper.Options.SOSOptionTransferType.TransferTypes;

public class SOSSSHReadPidFileJob extends SOSSSHJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSSSHReadPidFileJob.class);

    private List<Integer> pids = new ArrayList<Integer>();
    private String pidFileName;

    public SOSSSHReadPidFileJob(String fileName) {
        super();
        pidFileName = fileName;
    }

    @Override
    public void execute() throws Exception {
        try {
            connect(TransferTypes.ssh);

            try {
                String cmd = String.format(objOptions.getPostCommandRead().getValue(), pidFileName);
                cmd = objJSJobUtilities.replaceSchedulerVars(cmd);

                LOGGER.debug(String.format("[read pids]%s", cmd));
                getHandler().executeCommand(cmd);

                objJSJobUtilities.setJSParam(PARAM_EXIT_CODE, "0");

                String pid = null;
                BufferedReader reader = new BufferedReader(new StringReader(new String(getHandler().getStdOut())));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    LOGGER.trace(line);
                    Matcher regExMatcher = Pattern.compile("^([^\r\n]*)\r*\n*").matcher(line);
                    if (regExMatcher.find()) {
                        pid = regExMatcher.group(1).trim();
                        try {
                            pids.add(Integer.parseInt(pid));
                            LOGGER.debug("PID: " + pid);
                            continue;
                        } catch (Exception e) {
                            LOGGER.debug("no parseable pid received in line:\n" + pid);
                        }
                    }
                }
                addStdOut();
                checkStdErr();
                checkExitCode();
                changeExitSignal();
            } catch (Exception e) {
                if (objOptions.raiseExceptionOnError.value()) {
                    if (objOptions.ignoreError.value()) {
                        if (objOptions.ignoreStderr.value()) {
                            LOGGER.debug(e.toString(), e);
                        } else {
                            throw e;
                        }
                    } else {
                        throw e;
                    }
                }
            } finally {
                if (!pids.isEmpty()) {
                    objJSJobUtilities.setJSParam(PARAM_PIDS_TO_KILL, Joiner.on(",").join(pids));
                }
            }

            deleteTempFiles();
        } catch (Exception e) {
            if (objOptions.raiseExceptionOnError.value()) {
                if (objOptions.ignoreError.value()) {
                    if (objOptions.ignoreStderr.value()) {
                        LOGGER.debug(e.toString(), e);
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        } finally {
            disconnect();
        }
    }
}