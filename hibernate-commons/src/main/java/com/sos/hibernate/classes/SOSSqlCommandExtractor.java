package com.sos.hibernate.classes;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sos.util.SOSString;

public class SOSSqlCommandExtractor {

    final static Logger LOGGER = LoggerFactory.getLogger(SOSSqlCommandExtractor.class);

    private final Enum<SOSHibernateFactory.Dbms> dbms;
    private int majorVersion = -1;
    private int minorVersion = -1;

    private static final String REPLACE_BACKSLASH = "\\\\'";
    private static final String REPLACEMENT_BACKSLASH = "XxxxX";
    private static final String REPLACE_DOUBLE_APOSTROPHE = "''";
    private static final String REPLACEMENT_DOUBLE_APOSTROPHE = "YyyyY";
    private String beginProcedure = "";

    public SOSSqlCommandExtractor(Enum<SOSHibernateFactory.Dbms> dbms) {
        this.dbms = dbms;
    }

    public ArrayList<String> extractCommands(String content) throws Exception {
        String method = "extractCommands";
        if (SOSString.isEmpty(content)) {
            throw new Exception("content is empty");
        }
        LOGGER.debug(String.format("%s: content=%s", method, content));

        ArrayList<String> commands = new ArrayList<String>();
        Preparer preparer = new Preparer(dbms, majorVersion, minorVersion, content);
        preparer.prepare();

        for (String cmd : preparer.getCommands()) {
            if (cmd == null || cmd.trim().isEmpty()) {
                continue;
            }
            String command = cmd.trim();

            if (endsWithEnd(command)) {
                if (this.isProcedureSyntax(command)) {
                    commands.add(command + preparer.getCommandCloser());

                    LOGGER.debug(String.format("%s: command=%s%s", method, command, preparer.getCommandCloser()));
                } else {
                    this.split(commands, replace(command), null, preparer.getCommandCloser(), true, 0);
                    if (!"".equals(beginProcedure)) {
                        int posBeginProcedure = command.indexOf(beginProcedure);
                        String subCommand = command.substring(posBeginProcedure);
                        commands.add(subCommand + preparer.getCommandCloser());

                        LOGGER.debug(String.format("%s: command=%s%s", method, subCommand, preparer.getCommandCloser()));
                    }
                }
            } else {
                String end = preparer.addCommandCloser() ? preparer.getCommandCloser() : "";
                this.split(commands, replace(command), null, end, false, 0);
            }

        }
        return commands;
    }

    private StringBuffer replace(String value) {
        String s = value.replaceAll(REPLACE_BACKSLASH, REPLACEMENT_BACKSLASH);
        s = s.replaceAll(REPLACE_DOUBLE_APOSTROPHE, REPLACEMENT_DOUBLE_APOSTROPHE);
        return new StringBuffer(s.trim());
    }

    private void split(final ArrayList<String> commands, final StringBuffer st, final Integer position, final String procedurEnd,
            final boolean returnProcedureBegin, int count) throws Exception {
        String method = "split";

        beginProcedure = "";
        count += 1;
        StringBuffer sub;
        int semicolon = -1;
        int apostropheFirst = -1;
        if (position == null) {
            semicolon = st.indexOf(";");
            apostropheFirst = st.indexOf("'");
        } else {
            semicolon = st.indexOf(";", position.intValue());
            apostropheFirst = st.indexOf("'", position.intValue());
        }
        if (apostropheFirst > semicolon || apostropheFirst == -1) {
            String value = "";
            if (semicolon == -1) {
                value = st.toString().trim();
            } else {
                value = st.toString().substring(0, semicolon).trim();
            }
            value = value.replaceAll(REPLACEMENT_BACKSLASH, REPLACE_BACKSLASH);
            value = value.replaceAll(REPLACEMENT_DOUBLE_APOSTROPHE, REPLACE_DOUBLE_APOSTROPHE);
            if (this.isProcedureSyntax(value)) {
                if (returnProcedureBegin) {
                    beginProcedure = value;
                    return;
                } else if (!"".equals(procedurEnd)) {
                    value += procedurEnd;
                }
            }
            if (!"".equals(value)) {
                commands.add(value);
                LOGGER.debug(String.format("%s: command=%s", method, value));
            }
            if (semicolon != -1) {
                sub = new StringBuffer(st.substring(semicolon + 1));
                if (sub != null && sub.length() != 0) {
                    this.split(commands, sub, null, procedurEnd, returnProcedureBegin, count);
                }
            }
        } else {
            int apostropheSecond = st.indexOf("'", apostropheFirst + 1);
            if (apostropheSecond != -1) {
                this.split(commands, st, new Integer(apostropheSecond + 1), procedurEnd, returnProcedureBegin, count);
            } else {
                throw new Exception(String.format("closing apostrophe not found = %s = %s ", apostropheFirst, st));
            }
        }
    }

    private boolean isProcedureSyntax(String command) throws Exception {
        if (command == null) {
            throw new Exception("command is empty");
        }
        command = command.toLowerCase().trim();
        if (command.startsWith("procedure") || command.startsWith("function") || command.startsWith("declare") || command.startsWith("begin")) {
            return true;
        }
        StringBuilder patterns = new StringBuilder();
        patterns.append("^(re)?create+[\\s]*procedure");
        patterns.append("|^create+[\\s]*function");
        patterns.append("|^create+[\\s]*operator");
        patterns.append("|^create+[\\s]*package");
        patterns.append("|^create+[\\s]*trigger");
        patterns.append("|^drop+[\\s]*function");
        patterns.append("|^drop+[\\s]*operator");
        patterns.append("|^drop+[\\s]*package");
        patterns.append("|^drop+[\\s]*procedure");
        patterns.append("|^drop+[\\s]*trigger");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*procedure");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*function");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*package");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*operator");
        patterns.append("|^create+[\\s]*or+[\\s]*replace+[\\s]*trigger");
        Pattern p = Pattern.compile(patterns.toString());
        Matcher matcher = p.matcher(command);
        if (matcher.find()) {
            return true;
        }
        return false;
    }

    private boolean endsWithEnd(String statement) {
        // END END; END$$; END MY_PROCEDURE;
        String patterns = "end[\\s]*[\\S]*[;]*$";
        Pattern p = Pattern.compile(patterns, Pattern.CASE_INSENSITIVE);

        Matcher matcher = p.matcher(statement);
        return matcher.find();
    }

    public class Preparer {

        private final Enum<SOSHibernateFactory.Dbms> dbms;
        private final int majorVersion;
        private final int minorVersion;
        private final String content;

        private String commandSpltter;
        private String commandCloser;
        private boolean addCommandCloser;
        private String[] commands;

        public Preparer(Enum<SOSHibernateFactory.Dbms> dbms, int majorVersion, int minorVersion, String content) {
            this.dbms = dbms;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.content = content;

        }

        public void prepare() throws Exception {
            String content = init();
            content = stripComments(content);
            commands = content.split(commandSpltter);

            LOGGER.info("AAA1 = " + content);
            LOGGER.info("AAA2 = " + commands.length);

        }

        public String[] getCommands() {
            return commands;
        }

        public boolean addCommandCloser() {
            return addCommandCloser;
        }

        public String getCommandCloser() {
            return commandCloser;
        }

        public String getCommandSplitter() {
            return commandSpltter;
        }

        private String init() throws Exception {
            String method = "init";
            commandCloser = "";
            addCommandCloser = true;

            //replaceAll(":=","\\\\:=")) to avoid hibernate 
            //"Space is not allowed after parameter prefix ':'" Exception 
            //e.g. Oracle:  myVar := SYSDATE; 
            StringBuffer sb = new StringBuffer(this.content.replaceAll("\r\n", "\n").replaceAll("\\;[ \\t]", ";").replaceAll(":=","\\\\:="));
            if (this.dbms.equals(SOSHibernateFactory.Dbms.MSSQL)) {
                commandSpltter = "(?i)\nGO\\s*\n|\n/\n";
            } else if (this.dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
                commandSpltter = "\n\\\\g\n";
            } else if (this.dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
                commandSpltter = "\n/\n";
            } else if (this.dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
                commandSpltter = "\\$\\${1}[\\s]+(LANGUAGE|language){1}[\\s]+(plpgsql|PLPGSQL){1}[\\s]*;";
                commandCloser = "$$ LANGUAGE plpgsql;";
                addCommandCloser = false;
            } else if (this.dbms.equals(SOSHibernateFactory.Dbms.DB2)) {
                commandSpltter = "\n@\n";
            } else if (this.dbms.equals(SOSHibernateFactory.Dbms.SYBASE)) {
                commandSpltter = "\ngo\n";
            } else if (this.dbms.equals(SOSHibernateFactory.Dbms.FBSQL)) {
                StringBuffer patterns = new StringBuffer("set+[\\s]*term[inator]*[\\s]*(.*);");
                Pattern p = Pattern.compile(patterns.toString());
                Matcher matcher = p.matcher(this.content.toString().toLowerCase().trim());
                if (matcher.find()) {
                    commandSpltter = "\\" + matcher.group(1);
                    String ct = this.content.replaceAll("(?i)set+[\\s]*term[inator]*[\\s]*.*\\n", "");
                    sb.delete(0, sb.length());
                    sb.append(ct);
                } else {
                    commandSpltter = "\n/\n";
                }
            } else {
                throw new Exception(String.format("unsupported dbms=%s", this.dbms));
            }

            LOGGER.debug(String.format("%s: commandSpltter=%s, commandCloser=%s", method, commandSpltter, commandCloser));

            return sb.toString();
        }

        private String stripComments(String content) throws Exception {
            StringBuilder sb = new StringBuilder();
            StringTokenizer st = new StringTokenizer(content, "\n");
            boolean addRow = true;
            boolean isVersionComment = false;
            while (st.hasMoreTokens()) {
                String row = st.nextToken().trim();
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (row.startsWith("--") || row.startsWith("//") || row.startsWith("#")) {
                    continue;
                }
                row = row.replaceAll("^[/][*](?s).*?[*][/][\\s]*;*", "");
                if (row.isEmpty()) {
                    continue;
                }
                if (row.startsWith("/*!")) {
                    String[] rowArr = row.substring(3).trim().split(" ");
                    if (rowArr[0].length() == 5 || rowArr[0].length() == 6) {
                        String version = rowArr[0].length() == 5 ? "0" + rowArr[0] : rowArr[0];
                        try {
                            int major = Integer.parseInt(version.substring(0, 2));
                            if (this.majorVersion >= major) {
                                LOGGER.debug(String.format("use sql comment : db major version=%s >= comment major version=%s", this.majorVersion,
                                        major));
                                int minor = Integer.parseInt(version.substring(2, 4));
                                if (this.minorVersion >= minor) {
                                    isVersionComment = true;
                                    LOGGER.debug(String.format("use sql comment : db minor version=%s >= comment minor version=%s", this.minorVersion,
                                            minor));
                                } else {
                                    LOGGER.debug(String.format("skip sql comment : db minor version=%s < comment minor version=%s", this.minorVersion,
                                            minor));
                                }
                            } else {
                                LOGGER.debug(String.format("skip sql comment : db major version=%s < comment major version=%s", this.majorVersion,
                                        major));
                            }
                        } catch (Exception e) {
                            LOGGER.warn(String.format(
                                    "skip sql comment : no numerical major/minor version in comment=%s (database major version=%s, minor version=%s",
                                    version, this.majorVersion, this.minorVersion));
                        }
                    } else {
                        LOGGER.warn(String.format("skip sql comment : invalid comment major version length=%s (database major version=%s)", rowArr[0],
                                this.majorVersion));
                    }
                    if (!isVersionComment) {
                        addRow = false;
                    }
                    continue;
                } else if (row.startsWith("/*")) {
                    addRow = false;
                    continue;
                }
                if (row.endsWith("*/") || row.endsWith("*/;")) {
                    if (isVersionComment) {
                        if (!addRow) {
                            addRow = true;
                        } else {
                            isVersionComment = false;
                        }
                        continue;
                    }
                    if (!addRow) {
                        addRow = true;
                        continue;
                    }
                }
                if (!addRow) {
                    continue;
                }
                sb.append(row + "\n");
            }
            return sb.toString();
        }
    }

}
