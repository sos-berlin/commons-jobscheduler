package com.sos.VirtualFileSystem.Factory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.VirtualFileSystem.FTPS.SOSVfsFtpS;
import com.sos.VirtualFileSystem.HTTP.SOSVfsHTTP;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.JCIFS.SOSVfsJCIFS;
import com.sos.VirtualFileSystem.JMS.SOSVfsJms;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.WebDAV.SOSVfsWebDAV;
import com.sos.VirtualFileSystem.common.SOSVfsConstants;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.VirtualFileSystem.local.SOSVfsLocal;
import com.sos.VirtualFileSystem.zip.SOSVfsZip;
import com.sos.i18n.Msg;
import com.sos.i18n.Msg.BundleBaseName;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class VFSFactory extends SOSVfsMessageCodes {

    protected static Msg objMsg = new Msg(new BundleBaseName("SOSVirtualFileSystem"));
    private static final Logger LOGGER = Logger.getLogger(VFSFactory.class);
    private static final String USE_TRILEAD = ".TRILEAD";
    private static final String USE_JSCH = ".JSCH";
    private static SOSConnection2OptionsAlternate objConnectionOptions = null;
    private static boolean useTrilead = true;
    @I18NMessages(value = { @I18NMessage("%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "en_UK", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s liefert eine Instanz der Klasse %2$s", locale = "de", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "es", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "fr", explanation = "%1$s returns instance of %2$s"),
            @I18NMessage(value = "%1$s returns instance of %2$s", locale = "it", explanation = "%1$s returns instance of %2$s") }, msgnum = "SOSVfs-D-0201", msgurl = "SOSVfs-D-0201")
    private static String strParentLoggerName = "";
    private static ClassLoader classLoader = null;
    public static String sFTPHandlerClassName = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft";

    public VFSFactory() {
        super(SOSVfsConstants.strBundleBaseName);
    }

    public static void setParentLogger(final String pstrParentLoggerName) {
        strParentLoggerName = pstrParentLoggerName;
    }

    public static String getLoggerName() {
        String strT = SOSVfsConstants.strVFSLoggerName;
        if (!strParentLoggerName.isEmpty()) {
            strT = strParentLoggerName + "." + strT;
        }
        return strT;
    }

    public static ISOSVFSHandler getHandler(final SOSOptionTransferType.enuTransferTypes penuTType) throws Exception {
        return getHandler(penuTType.getText());
    }

    public static ISOSVFSHandler getHandler(String pstrWhatURL) throws Exception {
        final String methodName = "VFSFactory::getHandler";
        boolean authenticate = true;
        ISOSVFSHandler objC = null;
        URL objURL = null;
        if (pstrWhatURL.startsWith("local:")) {
            pstrWhatURL = pstrWhatURL.replace("local:", "file:");
        }
        if (pstrWhatURL.contains(USE_TRILEAD)) {
            useTrilead = true;
            pstrWhatURL = pstrWhatURL.replace(USE_TRILEAD, "");
        } else if (pstrWhatURL.contains(USE_JSCH)) {
            useTrilead = false;
            pstrWhatURL = pstrWhatURL.replace(USE_JSCH, "");
        }
        String strWhatSystem = pstrWhatURL;
        try {
            objURL = new URL(pstrWhatURL);
            strWhatSystem = objURL.getProtocol();
            LOGGER.info(objURL.getFile());
            LOGGER.info(objURL.getPath());
        } catch (MalformedURLException e) {
            //
        }
        classLoader = Thread.currentThread().getContextClassLoader();
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ssh2.getText())) {
            Class objA;
            if (useTrilead) {
                objA = classLoader.loadClass("com.sos.VirtualFileSystem.SSH.SOSSSH2TriLeadImpl");
            } else {
                objA = classLoader.loadClass("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
            }
            ISOSVFSHandler objD = (ISOSVFSHandler) objA.newInstance();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, objD.toString()));
            if (objD instanceof ISOSVFSHandler) {
                LOGGER.trace("ISOSVFSHandler is part of class   ...  " + objA.toString());
                objC = objD;
            } else {
                LOGGER.error("ISOSVFSHandler not part of class");
            }
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ftp.getText())) {
            objC = getDynamicVFSHandler("com.sos.VirtualFileSystem.FTP.SOSVfsFtp");
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.ftps.getText())) {
            objC = new SOSVfsFtpS();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsFtpS.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.sftp.getText())) {
            objC = getDynamicVFSHandler(sFTPHandlerClassName);
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.local.getText())
                || strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.file.getText())) {
            objC = new SOSVfsLocal();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsLocal.class.toString()));
            authenticate = false;
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.zip.getText())) {
            objC = new SOSVfsZip();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsZip.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.webdav.getText())) {
            objC = new SOSVfsWebDAV();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsWebDAV.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.http.getText())) {
            objC = new SOSVfsHTTP();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsHTTP.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.smb.getText())) {
            objC = new SOSVfsJCIFS();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsJCIFS.class.toString()));
        }
        if (strWhatSystem.equalsIgnoreCase(SOSOptionTransferType.enuTransferTypes.mq.getText())) {
            objC = new SOSVfsJms();
            LOGGER.trace(SOSVfs_D_0201.params(methodName, SOSVfsJms.class.toString()));
        }
        if (objC == null) {
            throw new Exception(SOSVfs_E_0203.params(strWhatSystem));
        }
        if (objURL != null && authenticate) {
            String strHost = objURL.getHost();
            if (strHost != null) {
                int intPort = objURL.getPort();
                objC.connect(strHost, intPort);
                ISOSAuthenticationOptions objAO = new SOSFTPOptions();
                String strUserInfo = objURL.getUserInfo();
                String[] strUI = strUserInfo.split(":");
                objAO.getUser().setValue(strUI[0]);
                objAO.getPassword().setValue("");
                if (strUI.length > 1) {
                    objAO.getPassword().setValue(strUI[1]);
                }
                objC.authenticate(objAO);
                objAO = null;
                LOGGER.info("objURL.getAuthority() : " + objURL.getAuthority());
                LOGGER.info("objURL.getFile()" + objURL.getFile());
            }
        }
        return objC;
    }

    public static void setConnectionOptions(final SOSConnection2OptionsAlternate pobjConnectionOptions) {
        objConnectionOptions = pobjConnectionOptions;
    }

    private static ISOSVFSHandler getDynamicVFSHandler(final String pstrLoadClassNameDefault) {
        String strLoadClassName = pstrLoadClassNameDefault;
        if (objConnectionOptions != null && objConnectionOptions.loadClassName.isDirty()) {
            strLoadClassName = objConnectionOptions.loadClassName.getValue();
            if (strLoadClassName.isEmpty()) {
                strLoadClassName = pstrLoadClassNameDefault;
            } else {
                LOGGER.trace(String.format("loadClassName changed from '%1$s' to '%2$s'", pstrLoadClassNameDefault, strLoadClassName));
            }
        }
        ISOSVFSHandler objC = null;
        try {
            Class objA = null;
            if (objConnectionOptions != null && objConnectionOptions.javaClassPath.isDirty()) {
                String[] strJars = objConnectionOptions.javaClassPath.getValue().split(";");
                for (String strJarFileName : strJars) {
                    File objF = new File(strJarFileName);
                    if (objF.isFile() && objF.canExecute()) {
                        addJarsToClassPath(Thread.currentThread().getContextClassLoader(), new File[] { objF });
                    } else {
                        throw new JobSchedulerException(String.format("ClasspathElement '%1$s' not found or not accessible", strJarFileName));
                    }
                }
            }
            objA = classLoader.loadClass(strLoadClassName);
            objC = (ISOSVFSHandler) objA.newInstance();
            if (objC instanceof ISOSVFSHandler) {
                LOGGER.trace("ISOSVFSHandler is part of class   ...  " + objA.toString());
            } else {
                LOGGER.error("ISOSVFSHandler not part of class" + objA.toString());
            }
        } catch (Exception e) {
            throw new JobSchedulerException(String.format("Class with Name '%1$s' not found and not loaded", strLoadClassName), e);
        }
        return objC;
    }

    private static void addJarsToClassPath(final ClassLoader classLoader1, final File[] jars) {
        if (classLoader1 instanceof URLClassLoader) {
            try {
                Method addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
                if (null != addUrlMethod) {
                    addUrlMethod.setAccessible(true);
                    for (File jar : jars) {
                        try {
                            addUrlMethod.invoke(classLoader1, jar.toURI().toURL());
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

}