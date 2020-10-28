package com.sos.JSHelper.Options;

import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.RollingRandomAccessFileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Logging.SOSHtmlLayout;

import sos.util.SOSString;

public class SOSOptionLogFileName extends SOSOptionOutFileName {

    private static final long serialVersionUID = 144340120069043974L;
    private static Logger LOGGER = LoggerFactory.getLogger(SOSOptionLogFileName.class);
    private String htmlLogFile = "";
    public String ControlType = "file";

    public SOSOptionLogFileName(final JSOptionsClass parent, final String key, final String description, final String value,
            final String defaultValue, final boolean mandatory) {
        super(parent, key, description, value, defaultValue, mandatory);
    }

    public void setLogger(final Logger log) {
        if (!isDirty() || log == null) {
            return;
        }

        try {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            LoggerConfig config = context.getConfiguration().getLoggerConfig(log.getName());
            boolean update = false;
            for (Entry<String, Appender> pair : config.getAppenders().entrySet()) {
                Appender app = pair.getValue();
                if (app != null) {
                    FileAppender.Builder<?> faBuilder = null;
                    RollingFileAppender.Builder<?> rfaBuilder = null;
                    RollingRandomAccessFileAppender.Builder<?> rrfaBuilder = null;
                    String configuredFilePattern = null;
                    if (app instanceof FileAppender) {
                        faBuilder = createBuilder((FileAppender) app);
                    } else if (app instanceof RollingFileAppender) {
                        RollingFileAppender source = (RollingFileAppender) app;
                        configuredFilePattern = source.getFilePattern();
                        rfaBuilder = createBuilder(source);
                    } else if (app instanceof RollingRandomAccessFileAppender) {
                        RollingRandomAccessFileAppender source = (RollingRandomAccessFileAppender) app;
                        configuredFilePattern = source.getFilePattern();
                        rrfaBuilder = createBuilder(source);
                    }
                    if (faBuilder == null && rfaBuilder == null && rrfaBuilder == null) {
                        LOGGER.debug(String.format("[%s][skip]no File, RollingFile or RollingRandomAccessFile  appender found", app.getName()));
                        continue;
                    }
                    String fileName = getValue();
                    if (app.getLayout() instanceof SOSHtmlLayout || app.getLayout() instanceof HtmlLayout) {
                        htmlLogFile = getValue().concat(".html");
                        fileName = htmlLogFile;
                    }
                    app.stop();
                    config.removeAppender(app.getName());

                    if (faBuilder != null) {
                        faBuilder.withFileName(fileName);
                        FileAppender a = faBuilder.build();
                        a.start();
                        config.addAppender(a, Level.INFO, a.getFilter());
                        config.setLevel(Level.INFO);

                        LOGGER.debug(String.format("[File][%s]%s", a.getName(), fileName));
                        update = true;
                    } else if (rfaBuilder != null) {
                        rfaBuilder.withFileName(fileName);
                        String newFilePattern = getNewFilePattern(fileName, configuredFilePattern);
                        if (!SOSString.isEmpty(newFilePattern)) {
                            rfaBuilder.withFilePattern(newFilePattern);
                        }

                        RollingFileAppender a = rfaBuilder.build();
                        a.start();
                        config.addAppender(a, Level.INFO, a.getFilter());
                        config.setLevel(Level.INFO);

                        LOGGER.debug(String.format("[RollingFile][%s][%s][%s]", a.getName(), fileName, a.getFilePattern()));
                        update = true;
                    } else if (rrfaBuilder != null) {
                        rrfaBuilder.withFileName(fileName);
                        String newFilePattern = getNewFilePattern(fileName, configuredFilePattern);
                        if (!SOSString.isEmpty(newFilePattern)) {
                            rrfaBuilder.withFilePattern(newFilePattern);
                        }

                        RollingRandomAccessFileAppender a = rrfaBuilder.build();
                        a.start();
                        config.addAppender(a, Level.INFO, a.getFilter());
                        config.setLevel(Level.INFO);

                        LOGGER.debug(String.format("[RollingRandomAccessFile][%s][%s][%s]", a.getName(), fileName, a.getFilePattern()));
                        update = true;
                    }
                }
            }
            if (update) {
                context.updateLoggers();
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private String getNewFilePattern(String fileName, String configuredFilePattern) {
        if (!SOSString.isEmpty(configuredFilePattern)) {
            String[] arr = configuredFilePattern.replaceAll("\\\\", "/").split("/");
            String oldFileNameWithPattern = arr[arr.length - 1];

            // e.g.: filePattern="${env:TEMP}/YADE-%d{yyyy-MM-dd}.log"
            // oldFileNameWithPattern=YADE-%d{yyyy-MM-dd}.log
            int pidx = oldFileNameWithPattern.indexOf("%");
            if (pidx > -1) {
                return fileName.concat(".").concat(oldFileNameWithPattern.substring(pidx));
            }

            // e.g.: filePattern="${env:TEMP}/YADE.log"
            // oldFileNameWithPattern=YADE.log
            pidx = oldFileNameWithPattern.lastIndexOf(".");
            if (pidx > -1) {
                return fileName.concat(oldFileNameWithPattern.substring(pidx));
            }

            // e.g.: filePattern="${env:TEMP}/YADE"
            // oldFileNameWithPattern=YADE
            return fileName;
        }
        return null;
    }

    private FileAppender.Builder<?> createBuilder(FileAppender source) {
        try {
            FileAppender.Builder<?> b = FileAppender.newBuilder();
            b.setFilter(source.getFilter());
            b.setIgnoreExceptions(source.ignoreExceptions());
            b.setLayout(source.getLayout());
            b.setName(source.getName());
            b.setPropertyArray(source.getPropertyArray());

            b.withAdvertise(false);
            // b.withAdvertiseUri(advertiseUri)
            b.withImmediateFlush(source.getImmediateFlush());
            if (source.getManager() != null) {
                b.withAppend(source.getManager().isAppend());
                // b.withBufferedIo(bufferedIo);
                b.withBufferSize(source.getManager().getBufferSize());
                b.withCreateOnDemand(source.getManager().isCreateOnDemand());
                b.withFileGroup(source.getManager().getFileGroup());
                b.withFileOwner(source.getManager().getFileOwner());
                // b.withFilePermissions(source.getManager().getFilePermissions());
                b.withLocking(source.getManager().isLocking());
            }

            return b;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    private RollingFileAppender.Builder<?> createBuilder(RollingFileAppender source) {
        try {
            RollingFileAppender.Builder<?> b = RollingFileAppender.newBuilder();
            b.setFilter(source.getFilter());
            b.setIgnoreExceptions(source.ignoreExceptions());
            b.setLayout(source.getLayout());
            b.setName(source.getName());
            b.setPropertyArray(source.getPropertyArray());

            b.withAdvertise(false);
            // b.withAdvertiseUri(advertiseUri);
            b.withFilePattern(source.getFilePattern());
            b.withPolicy(source.getTriggeringPolicy());

            if (source.getManager() != null) {
                b.withAppend(source.getManager().isAppend());
                // b.withBufferedIo(bufferedIo);
                b.withBufferSize(source.getManager().getBufferSize());
                b.withCreateOnDemand(source.getManager().isCreateOnDemand());
                b.withFileGroup(source.getManager().getFileGroup());
                b.withFileOwner(source.getManager().getFileOwner());
                // b.withFilePermissions(source.getManager().getFilePermissions());
                b.withImmediateFlush(source.getImmediateFlush());
                b.withLocking(source.getManager().isLocking());
                b.withStrategy(source.getManager().getRolloverStrategy());
            }
            return b;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    private RollingRandomAccessFileAppender.Builder<?> createBuilder(RollingRandomAccessFileAppender source) {
        try {
            RollingRandomAccessFileAppender.Builder<?> b = RollingRandomAccessFileAppender.newBuilder();
            b.setFilter(source.getFilter());
            b.setIgnoreExceptions(source.ignoreExceptions());
            b.setLayout(source.getLayout());
            b.setName(source.getName());
            b.setPropertyArray(source.getPropertyArray());

            b.withAdvertise(false);
            // b.withAdvertiseUri(advertiseUri);
            b.withFilePattern(source.getFilePattern());

            if (source.getManager() != null) {
                b.withAppend(source.getManager().isAppend());
                // b.withBufferedIo(bufferedIo);
                b.withBufferSize(source.getManager().getBufferSize());
                b.withFileGroup(source.getManager().getFileGroup());
                b.withFileOwner(source.getManager().getFileOwner());
                // b.withFilePermissions(source.getManager().getFilePermissions());
                b.withImmediateFlush(source.getImmediateFlush());
                b.withStrategy(source.getManager().getRolloverStrategy());
                b.withPolicy(source.getManager().getTriggeringPolicy());
            }
            return b;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    public String getHtmlLogFileName() {
        return SOSString.isEmpty(htmlLogFile) ? "" : htmlLogFile;
    }

    public void resetHTMLEntities() {
        /** if (isNotEmpty(htmlLogFile) == true && fileAppender != null) { // fileAppender.close(); JSTextFile f = new JSTextFile(htmlLogFile); try {
         * f.replaceString("&lt;", "<"); f.replaceString("&gt;", ">"); } catch (IOException e) { // } } */
    }

    public String getContent() {
        return "";
    }

}