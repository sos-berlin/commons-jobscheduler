package com.sos.JSHelper.Logging;

import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.helpers.Transform;

/** @author Ceki G&uuml;lc&uuml; */
public class SOSHtmlLayout extends Layout {

    protected final int BUF_SIZE = 256;
    protected final int MAX_CAPACITY = 1024;
    private StringBuilder sb = new StringBuilder();
    public static final String LOCATION_INFO_OPTION = "LocationInfo";
    public static final String TITLE_OPTION = "Title";
    static String TRACE_PREFIX = "<br>&nbsp;&nbsp;&nbsp;&nbsp;";
    boolean locationInfo = false;
    String title = "Log4J Log Messages";

    /** The <b>LocationInfo</b> option takes a boolean value. By default, it is
     * set to false which means there will be no location information output by
     * this layout. If the the option is set to true, then the file name and
     * line number of the statement at the origin of the log statement will be
     * output.
     * 
     * <p>
     * If you are embedding this layout within an
     * {@link org.apache.log4j.net.SMTPAppender} then make sure to set the
     * <b>LocationInfo</b> option of that appender as well. */
    public void setLocationInfo(boolean flag) {
        locationInfo = flag;
    }

    /** Returns the current value of the <b>LocationInfo</b> option. */
    public boolean getLocationInfo() {
        return locationInfo;
    }

    /** The <b>Title</b> option takes a String value. This option sets the
     * document title of the generated HTML document.
     * 
     * <p>
     * Defaults to 'Log4J Log Messages'. */
    public void setTitle(String title) {
        this.title = title;
    }

    /** Returns the current value of the <b>Title</b> option. */
    public String getTitle() {
        return title;
    }

    /** Returns the content type output by this layout, i.e "text/html". */
    public String getContentType() {
        return "text/html";
    }

    /** No options to activate. */
    public void activateOptions() {
        //
    }

    public String format(LoggingEvent event) {
        if (sb.capacity() > MAX_CAPACITY) {
            sb = new StringBuilder();
        } else {
            sb.setLength(0);
        }
        sb.append(Layout.LINE_SEP + "<tr>" + Layout.LINE_SEP);
        sb.append("<td>");
        sb.append(event.timeStamp - LoggingEvent.getStartTime());
        sb.append("</td>" + Layout.LINE_SEP);
        String escapedThread = Transform.escapeTags(event.getThreadName());
        sb.append("<td title=\"" + escapedThread + " thread\">");
        sb.append(escapedThread);
        sb.append("</td>" + Layout.LINE_SEP);
        sb.append("<td title=\"Level\">");
        if (event.getLevel().equals(Level.DEBUG)) {
            sb.append("<font color=\"#339933\">");
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
            sb.append("</font>");
        } else if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
            sb.append("<font color=\"#993300\"><strong>");
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
            sb.append("</strong></font>");
        } else {
            sb.append(Transform.escapeTags(String.valueOf(event.getLevel())));
        }
        sb.append("</td>" + Layout.LINE_SEP);
        String escapedLogger = Transform.escapeTags(event.getLoggerName());
        sb.append("<td title=\"" + escapedLogger + " category\">");
        sb.append(escapedLogger);
        sb.append("</td>" + Layout.LINE_SEP);
        if (locationInfo) {
            LocationInfo locInfo = event.getLocationInformation();
            sb.append("<td>");
            sb.append(Transform.escapeTags(locInfo.getFileName()));
            sb.append(':');
            sb.append(locInfo.getLineNumber());
            sb.append("</td>" + Layout.LINE_SEP);
        }
        sb.append("<td title=\"Message\">");
        String strT = Transform.escapeTags(event.getRenderedMessage());
        strT = strT.replaceAll("\\n", "<br/>");
        sb.append(strT);
        sb.append("</td>" + Layout.LINE_SEP);
        sb.append("</tr>" + Layout.LINE_SEP);
        if (event.getNDC() != null) {
            sb.append("<tr><td bgcolor=\"#EEEEEE\" style=\"font-size : xx-small;\" colspan=\"6\" title=\"Nested Diagnostic Context\">");
            sb.append("NDC: " + Transform.escapeTags(event.getNDC()));
            sb.append("</td></tr>" + Layout.LINE_SEP);
        }
        String[] s = event.getThrowableStrRep();
        if (s != null) {
            sb.append("<tr><td bgcolor=\"#993300\" style=\"color:White; font-size : xx-small;\" colspan=\"6\">");
            appendThrowableAsHTML(s, sb);
            sb.append("</td></tr>" + Layout.LINE_SEP);
        }
        return sb.toString();
    }

    void appendThrowableAsHTML(String[] s, StringBuilder sb) {
        if (s != null) {
            int len = s.length;
            if (len == 0) {
                return;
            }
            sb.append(Transform.escapeTags(s[0]));
            sb.append(Layout.LINE_SEP);
            for (int i = 1; i < len; i++) {
                sb.append(TRACE_PREFIX);
                sb.append(Transform.escapeTags(s[i]));
                sb.append(Layout.LINE_SEP);
            }
        }
    }

    /** Returns appropriate HTML headers. */
    public String getHeader() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">" + Layout.LINE_SEP);
        sbuf.append("<html>" + Layout.LINE_SEP);
        sbuf.append("<head>" + Layout.LINE_SEP);
        sbuf.append("<title>" + title + "</title>" + Layout.LINE_SEP);
        sbuf.append("<style type=\"text/css\">" + Layout.LINE_SEP);
        sbuf.append("<!--" + Layout.LINE_SEP);
        sbuf.append("body, table {font-family: arial,sans-serif; font-size: x-small;}" + Layout.LINE_SEP);
        sbuf.append("th {background: #336699; color: #FFFFFF; text-align: left;}" + Layout.LINE_SEP);
        sbuf.append("-->" + Layout.LINE_SEP);
        sbuf.append("</style>" + Layout.LINE_SEP);
        sbuf.append("</head>" + Layout.LINE_SEP);
        sbuf.append("<body bgcolor=\"#FFFFFF\" topmargin=\"6\" leftmargin=\"6\">" + Layout.LINE_SEP);
        sbuf.append("<hr size=\"1\" noshade>" + Layout.LINE_SEP);
        sbuf.append("Log session start time " + new java.util.Date() + "<br>" + Layout.LINE_SEP);
        sbuf.append("<br>" + Layout.LINE_SEP);
        sbuf.append("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\" bordercolor=\"#224466\" width=\"100%\">" + Layout.LINE_SEP);
        sbuf.append("<tr>" + Layout.LINE_SEP);
        sbuf.append("<th>Time</th>" + Layout.LINE_SEP);
        sbuf.append("<th>Thread</th>" + Layout.LINE_SEP);
        sbuf.append("<th>Level</th>" + Layout.LINE_SEP);
        sbuf.append("<th>Category</th>" + Layout.LINE_SEP);
        if (locationInfo) {
            sbuf.append("<th>File:Line</th>" + Layout.LINE_SEP);
        }
        sbuf.append("<th>Message</th>" + Layout.LINE_SEP);
        sbuf.append("</tr>" + Layout.LINE_SEP);
        return sbuf.toString();
    }

    /** Returns the appropriate HTML footers. */
    public String getFooter() {
        StringBuilder sb = new StringBuilder();
        sb.append("</table>" + Layout.LINE_SEP);
        sb.append("<br>" + Layout.LINE_SEP);
        sb.append("</body></html>");
        return sb.toString();
    }

    /** The HTML layout handles the throwable contained in logging events. Hence,
     * this method return <code>false</code>. */
    public boolean ignoresThrowable() {
        return false;
    }

}