package com.sos.jobstreams.classes;

import java.util.ArrayList;
import java.util.List;

public class CheckHistoryCacheRules {
    
    private List<CheckHistoryCacheRule> listOfCheckHistoryChacheRules;
    
    public void initCacheRules() {
        listOfCheckHistoryChacheRules = new ArrayList<CheckHistoryCacheRule>();
        CheckHistoryCacheRule checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("returnCode");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedRunEndedSuccessful");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedRunEndedWithError");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedRunEndedTodaySuccessful");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedRunEndedTodayWithError");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedRunEndedWithError");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedIsEndedBefore");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedSuccessulIsEndedBefore");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedWithErrorIsEndedBefore");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedIsStartedBefore");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedSuccessfulIsStartedBefore");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("lastCompletedWithErrorIsStartedBefore");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedToday");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedTodayCompletedSuccessful");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedTodayCompletedWithError");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedTodayCompleted");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("prev");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("prevSuccessful");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("prevError");
        checkHistoryCacheRule.setValidateAlways(true);
        checkHistoryCacheRule.setValidateIfFalse(false);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isCompletedToday");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isCompletedTodaySuccessfully");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isCompletedTodayWithError");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isCompletedAfter");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isCompletedWithErrorAfter");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isCompletedSuccessfulAfter");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedAfter");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedWithErrorAfter");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        checkHistoryCacheRule = new CheckHistoryCacheRule();
        checkHistoryCacheRule.setQueryString("isStartedSuccessfulAfter");
        checkHistoryCacheRule.setValidateAlways(false);
        checkHistoryCacheRule.setValidateIfFalse(true);
        listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);
    }

    public List<CheckHistoryCacheRule> getListOfCheckHistoryChacheRules() {
        return listOfCheckHistoryChacheRules;
    }

}
