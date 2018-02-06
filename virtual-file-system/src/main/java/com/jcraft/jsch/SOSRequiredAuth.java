package com.jcraft.jsch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserAuth;

public class SOSRequiredAuth extends UserAuth {

    public static final String JSCH_AUTH_CLASS_PASSWORD = "com.jcraft.jsch.UserAuthPassword";
    public static final String JSCH_AUTH_CLASS_PUBLIC_KEY = "com.jcraft.jsch.UserAuthPublicKey";
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSRequiredAuth.class);
    private String _jschClass;
    private UserAuth _jschUserAuth;

    public SOSRequiredAuth(final String jschClass) throws Exception {
        try {
            _jschClass = jschClass;
            _jschUserAuth = (UserAuth) (Class.forName(_jschClass).newInstance());
        } catch (Throwable e) {
            LOGGER.error(String.format("failed to load %s", jschClass));
            throw e;
        }
    }

    @Override
    public boolean start(Session session) throws Exception {
        boolean isPasswordMethod = _jschClass.equals(JSCH_AUTH_CLASS_PASSWORD);
        String preffered = session.getConfig("PreferredAuthentications");
        LOGGER.debug(String.format("preffered=%s, isPasswordMethod=%s", preffered, isPasswordMethod));

        boolean result = _jschUserAuth.start(session);
        if (!result) {
            throw new JSchException("Auth fail");
        }
        if (preffered.startsWith("password") && isPasswordMethod) {
            return false;
        } else if (preffered.startsWith("publickey") && !isPasswordMethod) {
            return false;
        }
        return true;
    }
}
