package com.sos.JSHelper.Options;

import java.util.Map.Entry;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Logging.SOSHtmlLayout;

public class SOSOptionLogFileName extends SOSOptionOutFileName {

    private static final long serialVersionUID = 144340120069043974L;
    private static Logger LOGGER = LoggerFactory.getLogger(SOSOptionLogFileName.class);
    private String htmlLogFile = "";
    public String ControlType = "file";
    // private FileAppender fileAppender = null;

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

                    if (app instanceof FileAppender) {
                        faBuilder = createBuilder((FileAppender) app);
                    } else if (app instanceof RollingFileAppender) {
                        rfaBuilder = createBuilder((RollingFileAppender) app);
                    }
                    if (faBuilder == null && rfaBuilder == null) {
                        LOGGER.debug(String.format("[%s][skip]no File or RollingFile appender found", app.getName()));
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

                        LOGGER.debug(String.format("[File][%s]%s", a.getName(), fileName));
                        update = true;
                    } else if (rfaBuilder != null) {
                        rfaBuilder.withFileName(fileName);
                        try {
                            rfaBuilder.withFilePattern(fileName.concat(".%d{yyyy-MM}-%i.gz"));
                        } catch (Throwable e) {

                        }

                        RollingFileAppender a = rfaBuilder.build();
                        a.start();
                        config.addAppender(a, Level.INFO, a.getFilter());

                        LOGGER.debug(String.format("[RollingFile][%s]%s", a.getName(), fileName));
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

    private FileAppender.Builder<?> createBuilder(FileAppender source) {
        try {
            FileAppender.Builder<?> b = FileAppender.newBuilder();
            b.setName(source.getName());
            b.setFilter(source.getFilter());
            b.setLayout(source.getLayout());
            b.withAppend(source.getManager().isAppend());
            b.withCreateOnDemand(false);
            b.withLocking(false);
            b.withAdvertise(false);
            return b;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    private RollingFileAppender.Builder<?> createBuilder(RollingFileAppender source) {
        try {
            RollingFileAppender.Builder<?> b = RollingFileAppender.newBuilder();
            b.setName(source.getName());
            b.setFilter(source.getFilter());
            b.setLayout(source.getLayout());
            b.withFilePattern(source.getFilePattern());
            b.withPolicy(source.getTriggeringPolicy());
            b.withStrategy(source.getManager().getRolloverStrategy());
            b.withAppend(source.getManager().isAppend());
            b.withCreateOnDemand(false);
            b.withLocking(false);
            b.withAdvertise(false);
            return b;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
        return null;
    }

    public String getHtmlLogFileName() {
        if (!isEmpty(htmlLogFile)) {
            return htmlLogFile;
        } else {
            return "";
        }
    }

    public void resetHTMLEntities() {
        /** if (isNotEmpty(htmlLogFile) == true && fileAppender != null) { // fileAppender.close(); JSTextFile f = new JSTextFile(htmlLogFile); try {
         * f.replaceString("&lt;", "<"); f.replaceString("&gt;", ">"); } catch (IOException e) { // } } */
    }

    public String getContent() {
        return "";
    }

}