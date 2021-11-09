package com.sos.jobstreams.classes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.sos.jobstreams.resolver.JSCondition;
import com.sos.jobstreams.resolver.JSReturnCodeResolver;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.checkhistory.JobChainHistory;
import com.sos.jitl.checkhistory.JobHistory;
import com.sos.jitl.checkhistory.JobSchedulerHistoryInfo;
import com.sos.jitl.checkhistory.classes.HistoryDatabaseExecuter;
import com.sos.jitl.restclient.WebserviceCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckHistoryCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckHistoryCondition.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final String JOB_CHAIN = "job_chain";
    private static final String JOB = "job";
    private JobHistory jobHistory;
    private JobChainHistory jobChainHistory;

    private Map<CheckHistoryKey, CheckHistoryValue> checkHistoryCache;

    public CheckHistoryCondition(String schedulerId) {
        super();
        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
        webserviceCredentials.setSchedulerId(schedulerId);

        jobHistory = new com.sos.jitl.checkhistory.JobHistory(webserviceCredentials);
        jobChainHistory = new com.sos.jitl.checkhistory.JobChainHistory(webserviceCredentials);
        checkHistoryCache = new HashMap<CheckHistoryKey, CheckHistoryValue>();
    }

    public CheckHistoryValue validateJob(SOSHibernateSession sosHibernateSession, JSCondition jsCondition, String conditionJob,
            Integer taskReturnCode) throws Exception   {
        String job = jsCondition.getConditionJob();
        if (job.isEmpty()) {
            job = conditionJob;
        } else {
            taskReturnCode = null;
        }

        HistoryDatabaseExecuter historyDatabaseExecuter = new HistoryDatabaseExecuter(sosHibernateSession);
        jobHistory.setHistoryDatasourceExecuter(historyDatabaseExecuter);

        String query = jsCondition.getConditionQuery().replace('[', '(').replace(']', ')');
        String method = HistoryHelper.getMethodName(query);
        if ("rc".equalsIgnoreCase(method)) {
            method = "returncode";
        }

        CheckHistoryValue validateResult = null;
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, job, jsCondition.getConditionQuery().toLowerCase());
        if (jsCondition.getConditionJob().isEmpty() && taskReturnCode != null && ("returncode".equals(method))) {
        } else {
            validateResult = getCache(checkHistoryKey);
        }
        if (validateResult == null) {
            JobSchedulerHistoryInfo jobHistoryInfo = jobHistory.getJobInfo(job);
            if ("returncode".equals(method)) {
                if (taskReturnCode == null) {
                    taskReturnCode = jobHistoryInfo.getLastCompleted().executionResult;
                }
                String returnCode = HistoryHelper.getParameter(query);
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                validateResult = new CheckHistoryValue(returnCodeResolver.resolve(taskReturnCode, returnCode), jsCondition);
            } else {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("validating job %s with: %s ", job, query));
                }
                validateResult = new CheckHistoryValue(jobHistoryInfo.queryHistory(query), jsCondition);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("validating result", validateResult.getValidateResult()));
                }
            }
            if ((jsCondition.getConditionJob().isEmpty() && taskReturnCode != null) || "returncode".equalsIgnoreCase(method)) {
            } else {
                putCache(checkHistoryKey, validateResult);
            }
        }
        return validateResult;
    }

    public CheckHistoryValue validateJobChain(SOSHibernateSession sosHibernateSession, JSCondition jsCondition) throws Exception {
        String jobChain = jsCondition.getConditionJobChain();
        jobChain = jobChain.replace('[', '(').replace(']', ')');

        String query = jsCondition.getConditionQuery().replace('[', '(').replace(']', ')');

        String method = HistoryHelper.getMethodName(query);
      //  if ("rc".equalsIgnoreCase(method)) {
      //      method = "returncode";
      //  }

        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB_CHAIN, jobChain, jsCondition.getConditionQuery().toLowerCase());
        CheckHistoryValue validateResult = checkHistoryCache.get(checkHistoryKey);
        if (validateResult == null) {

            HistoryDatabaseExecuter historyDatabaseExecuter = new HistoryDatabaseExecuter(sosHibernateSession);
            jobChainHistory.setHistoryDatasourceExecuter(historyDatabaseExecuter);
            JobSchedulerHistoryInfo jobChainHistoryInfo = jobChainHistory.getJobChainInfo(jobChain);

            //    if ("returncode".equals(method)) {
            //    Integer taskReturnCode = jobChainHistoryInfo.getLastCompleted().executionResult;

            //    String returnCode = HistoryHelper.getParameter(query);
            //    JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
            //    validateResult = new CheckHistoryValue(returnCodeResolver.resolve(taskReturnCode, returnCode), jsCondition);
            //} else {

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("validating job chain %s with: %s ", jobChain, query));
                }
                validateResult = new CheckHistoryValue(jobChainHistoryInfo.queryHistory(query), jsCondition);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("validating result", validateResult.getValidateResult()));
                }
                checkHistoryCache.put(checkHistoryKey, validateResult);
                //}
        }
        return validateResult;

    }

    public CheckHistoryValue getPrev(String prevKey, String job) throws Exception {
        JobSchedulerHistoryInfo jobHistoryInfo = null;
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, job, prevKey);
        CheckHistoryValue checkHistoryValue = checkHistoryCache.get(checkHistoryKey);
        if (checkHistoryValue == null) {
            if (jobHistoryInfo == null) {
                jobHistoryInfo = jobHistory.getJobInfo(job);
            }
            LocalDateTime start = null;
            LocalDateTime end = null;

            if ("prev".equalsIgnoreCase(prevKey)) {
                start = jobHistoryInfo.getLastCompleted().start;
                end = jobHistoryInfo.getLastCompleted().end;
            }
            if ("prevSuccessFul".equalsIgnoreCase(prevKey)) {
                start = jobHistoryInfo.getLastCompletedSuccessful().start;
                end = jobHistoryInfo.getLastCompletedSuccessful().end;
            }
            if ("prevError".equalsIgnoreCase(prevKey)) {
                start = jobHistoryInfo.getLastCompletedWithError().start;
                end = jobHistoryInfo.getLastCompletedWithError().end;
            }
            checkHistoryValue = new CheckHistoryValue(false, null);
            checkHistoryValue.setEndTime(end);
            checkHistoryValue.setStartTime(start);
        }
        return checkHistoryValue;
    }

    public CheckHistoryValue getCache(CheckHistoryKey checkHistoryKey) {
        return checkHistoryCache.get(checkHistoryKey);
    }

    public CheckHistoryValue putCache(CheckHistoryKey checkHistoryKey, CheckHistoryValue checkHistoryValue) {
        return checkHistoryCache.put(checkHistoryKey, checkHistoryValue);
    }
}
