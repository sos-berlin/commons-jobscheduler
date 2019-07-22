package com.sos.eventhandlerservice.classes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.sos.eventhandlerservice.resolver.JSCondition;
import com.sos.hibernate.classes.SOSHibernateSession;
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

    public CheckHistoryValue validateJob(JSCondition jsCondition, String conditionJob) throws Exception {
        String job = jsCondition.getConditionJob();
        if (job.isEmpty()) {
            job = conditionJob;
        }
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, job, jsCondition.getConditionQuery().toLowerCase());
        CheckHistoryValue validateResult = checkHistoryCache.get(checkHistoryKey);
        if (validateResult == null) {
            JobSchedulerHistoryInfo jobHistoryInfo = jobHistory.getJobInfo(job);
            validateResult = new CheckHistoryValue(jobHistoryInfo.queryHistory(jsCondition.getConditionQuery()), jsCondition);
            checkHistoryCache.put(checkHistoryKey, validateResult);
        }
        return validateResult;

    }
    
    public CheckHistoryValue validateJobChain(JSCondition jsCondition) throws Exception {
        String jobChain = jsCondition.getConditionJobChain();
        jobChain.replace('[','(');
        jobChain.replace(']',')');
       
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB_CHAIN, jobChain, jsCondition.getConditionQuery().toLowerCase());
        CheckHistoryValue validateResult = checkHistoryCache.get(checkHistoryKey);
        if (validateResult == null) {
            JobSchedulerHistoryInfo jobChainHistoryInfo = jobChainHistory.getJobChainInfo(jobChain);
            validateResult = new CheckHistoryValue(jobChainHistoryInfo.queryHistory(jsCondition.getConditionQuery()), jsCondition);
            checkHistoryCache.put(checkHistoryKey, validateResult);
        }
        return validateResult;

    }
    public CheckHistoryValue getPrev(String prevKey, String job) throws Exception   {
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

    public CheckHistoryValue get(CheckHistoryKey checkHistoryKey) {
        return checkHistoryCache.get(checkHistoryKey);
    }

}
