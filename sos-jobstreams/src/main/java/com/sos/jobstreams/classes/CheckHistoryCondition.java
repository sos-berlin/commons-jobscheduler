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

public class CheckHistoryCondition {

    private static final String JOB_CHAIN = "job_chain";
    private static final String JOB = "job";
    private JobHistory jobHistory;
    private JobChainHistory jobChainHistory;

    private Map<CheckHistoryKey, CheckHistoryValue> checkHistoryCache;

    public CheckHistoryCondition(SOSHibernateSession sosHibernateSession, String schedulerId) {
        super();
        HistoryDatabaseExecuter historyDatabaseExecuter = new HistoryDatabaseExecuter(sosHibernateSession);
        WebserviceCredentials webserviceCredentials = new WebserviceCredentials();
        webserviceCredentials.setSchedulerId(schedulerId);

        jobHistory = new com.sos.jitl.checkhistory.JobHistory("", webserviceCredentials);
        jobHistory.setHistoryDatasourceExecuter(historyDatabaseExecuter);
        jobChainHistory = new com.sos.jitl.checkhistory.JobChainHistory("", webserviceCredentials);
        jobChainHistory.setHistoryDatasourceExecuter(historyDatabaseExecuter);
        checkHistoryCache = new HashMap<CheckHistoryKey, CheckHistoryValue>();
    }

    public CheckHistoryValue validateJob(JSCondition jsCondition, String conditionJob, Integer taskReturnCode) throws Exception {
        String job = jsCondition.getConditionJob();
        if (job.isEmpty()) {
            job = conditionJob;
        } else {
            taskReturnCode = null;
        }
        String query = jsCondition.getConditionQuery().replace('[', '(').replace(']', ')');

        CheckHistoryValue validateResult = null;
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, job, jsCondition.getConditionQuery().toLowerCase());
        if (jsCondition.getConditionJob().isEmpty() && taskReturnCode != null && "returncode".equalsIgnoreCase(jsCondition.getConditionQuery())) {
            // will never come from cache
        } else {
            validateResult = getCache(checkHistoryKey);
        }
        if (validateResult == null) {
            JobSchedulerHistoryInfo jobHistoryInfo = jobHistory.getJobInfo(job);
            if ("returncode".equalsIgnoreCase(jsCondition.getConditionQuery())) {
                if (taskReturnCode == null) {
                    taskReturnCode = jobHistoryInfo.getLastCompleted().executionResult;
                }
                HistoryHelper jobHistoryHelper = new HistoryHelper();
                String returnCode = jobHistoryHelper.getParameter(query);
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                validateResult = new CheckHistoryValue(returnCodeResolver.resolve(taskReturnCode, returnCode), jsCondition);
            } else {
                validateResult = new CheckHistoryValue(jobHistoryInfo.queryHistory(query), jsCondition);
            }
            if ((jsCondition.getConditionJob().isEmpty() && taskReturnCode != null && "returncode".equalsIgnoreCase(jsCondition
                    .getConditionQuery()))) {
                // returncode of the actual job will not be cached as returncode is availabe with the JobScheduler event.
            } else {
                putCache(checkHistoryKey, validateResult);
            }
        }
        return validateResult;
    }

    public CheckHistoryValue validateJobChain(JSCondition jsCondition) throws Exception {
        String jobChain = jsCondition.getConditionJobChain();
        jobChain = jobChain.replace('[', '(').replace(']', ')');

        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB_CHAIN, jobChain, jsCondition.getConditionQuery().toLowerCase());
        CheckHistoryValue validateResult = checkHistoryCache.get(checkHistoryKey);
        if (validateResult == null) {
            JobSchedulerHistoryInfo jobChainHistoryInfo = jobChainHistory.getJobChainInfo(jobChain);
            validateResult = new CheckHistoryValue(jobChainHistoryInfo.queryHistory(jsCondition.getConditionQuery()), jsCondition);
            checkHistoryCache.put(checkHistoryKey, validateResult);
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
