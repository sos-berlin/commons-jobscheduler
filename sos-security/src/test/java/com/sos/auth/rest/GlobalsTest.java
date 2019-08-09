package com.sos.auth.rest;

public class GlobalsTest {

    private static final String PASSWORD = "root";
    private static final String USER = "root";
    public static final String SCHEDULER_ID = "scheduler.1.12";
    public static SOSServicePermissionShiro sosServicePermissionShiro;
    public static SOSShiroCurrentUserAnswer sosShiroCurrentUserAnswer;

    public static String getAccessToken() throws Exception {
        sosServicePermissionShiro = new SOSServicePermissionShiro();
        sosShiroCurrentUserAnswer = (SOSShiroCurrentUserAnswer) sosServicePermissionShiro.loginPost("", USER, PASSWORD).getEntity();
        return sosShiroCurrentUserAnswer.getAccessToken();
    }

    public static void logout() {
        sosServicePermissionShiro.logoutPost(sosShiroCurrentUserAnswer.getAccessToken(), "");
    }

}
