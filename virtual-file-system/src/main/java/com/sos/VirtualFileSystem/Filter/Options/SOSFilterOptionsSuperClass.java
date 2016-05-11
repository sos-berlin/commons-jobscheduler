package com.sos.VirtualFileSystem.Filter.Options;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;

/** @author KB */
public abstract class SOSFilterOptionsSuperClass extends JSOptionsClass {

    private static final String CLASSNAME = "SOSFilterOptionsSuperClass";
    private static final long serialVersionUID = 7496305355199139721L;

    public SOSFilterOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public SOSFilterOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    @JSOptionDefinition(name = "Line_numbering_position", description = "Insert line number at position", key = "Line_numbering_position", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger Line_numbering_position = new SOSOptionInteger(this, CLASSNAME + ".Line_numbering_position",
            "Insert line number at position", "1", "1", false);

    public SOSOptionInteger getLine_numbering_position() {
        return Line_numbering_position;
    }

    public SOSFilterOptionsSuperClass setLine_numbering_position(final SOSOptionInteger pstrValue) {
        Line_numbering_position = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Line_Numbering_Format", description = "Format mask of line numbering value", key = "Line_Numbering_Format", type = "SOSOptionString", mandatory = false)
    public SOSOptionString Line_Numbering_Format = new SOSOptionString(this, CLASSNAME + ".Line_Numbering_Format",
            "Format mask of line numbering value", "%08d", "%08d", false);

    public SOSOptionString getLine_Numbering_Format() {
        return Line_Numbering_Format;
    }

    public SOSFilterOptionsSuperClass setLine_Numbering_Format(final SOSOptionString pstrValue) {
        Line_Numbering_Format = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Line_Numbering_Increment_Value", description = "Increment value for line-numbering", key = "Line_Numbering_Increment_Value", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger Line_Numbering_Increment_Value = new SOSOptionInteger(this, CLASSNAME + ".Line_Numbering_Increment_Value",
            "Increment value for line-numbering", "1", "1", false);

    public SOSOptionInteger getLine_Numbering_Increment_Value() {
        return Line_Numbering_Increment_Value;
    }

    public SOSFilterOptionsSuperClass setLine_Numbering_Increment_Value(final SOSOptionInteger pstrValue) {
        Line_Numbering_Increment_Value = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Line_numbering_start_value", description = "STart value for line numbering", key = "Line_numbering_start_value", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger Line_numbering_start_value = new SOSOptionInteger(this, CLASSNAME + ".Line_numbering_start_value",
            "Start value for line numbering", "1", "1", false);

    public SOSOptionInteger getLine_numbering_start_value() {
        return Line_numbering_start_value;
    }

    public SOSFilterOptionsSuperClass setLine_numbering_start_value(final SOSOptionInteger pstrValue) {
        Line_numbering_start_value = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "create_line_numbers", description = "Create line number for all lines of a file", key = "create_line_numbers", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_line_numbers = new SOSOptionBoolean(this, CLASSNAME + ".create_line_numbers",
            "Create line number for all lines of a file", "false", "false", false);

    public SOSOptionBoolean getcreate_line_numbers() {
        return create_line_numbers;
    }

    public SOSFilterOptionsSuperClass setcreate_line_numbers(final SOSOptionBoolean pstrValue) {
        create_line_numbers = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "renumber_lines", description = "Make a renumbering of all lines in a file", key = "renumber_lines", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean renumber_lines = new SOSOptionBoolean(this, CLASSNAME + ".renumber_lines", "Make a renumbering of all lines in a file",
            "false", "false", false);

    public SOSOptionBoolean getrenumber_lines() {
        return renumber_lines;
    }

    public SOSFilterOptionsSuperClass setrenumber_lines(final SOSOptionBoolean pstrValue) {
        renumber_lines = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Filter_sequence", description = "Filter definitions: what to process and in what sequence", key = "Filter_sequence", type = "SOSOptionValueList", mandatory = false)
    public SOSOptionFilterSequence FilterSequence = new SOSOptionFilterSequence(this, CLASSNAME + ".Filter_sequence",
            "Filter definitions: what to process and in what sequence", "nullFilter", "", true);

    public String getFilter_sequence() {
        return FilterSequence.Value();
    }

    public SOSFilterOptionsSuperClass setFilter_sequence(final String pstrValue) {
        FilterSequence.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "do_replacing", description = "Activate replacing", key = "do_replacing", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean doReplacing = new SOSOptionBoolean(this, CLASSNAME + ".do_replacing", "Activate replacing", "false", "false", false);

    public String getdo_replacing() {
        return doReplacing.Value();
    }

    public SOSFilterOptionsSuperClass setdo_replacing(final String pstrValue) {
        doReplacing.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "replace_what", description = "Replacing what", key = "replace_what", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replaceWhat = new SOSOptionRegExp(this, CLASSNAME + ".replace_what", "Replacing what", "", "", false);

    public String getreplace_what() {
        return replaceWhat.Value();
    }

    public SOSFilterOptionsSuperClass setreplace_what(final String pstrValue) {
        replaceWhat.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "replace_with", description = "replace with", key = "replace_with", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replaceWith = new SOSOptionRegExp(this, CLASSNAME + ".replace_with", "replaced with", "", "", false);

    public String getreplace_with() {
        return replaceWith.Value();
    }

    public SOSFilterOptionsSuperClass setreplace_with(final String pstrValue) {
        replaceWith.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_lines_before", description = "exclude_lines_before", key = "exclude_lines_before", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp excludeLinesBefore = new SOSOptionRegExp(this, CLASSNAME + ".exclude_lines_before", "exclude_lines_before", "", "", false);

    public String getexclude_lines_before() {
        return excludeLinesBefore.Value();
    }

    public SOSFilterOptionsSuperClass setexclude_lines_before(final String pstrValue) {
        excludeLinesBefore.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_lines_after", description = "exclude_lines_after", key = "exclude_lines_after", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp exclude_lines_after = new SOSOptionRegExp(this, CLASSNAME + ".exclude_lines_after", "exclude_lines_after", "", "", false);

    public String getexclude_lines_after() {
        return exclude_lines_after.Value();
    }

    public SOSFilterOptionsSuperClass setexclude_lines_after(final String pstrValue) {
        exclude_lines_after.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_lines", description = "exclude lines", key = "exclude_lines", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp excludeLines = new SOSOptionRegExp(this, CLASSNAME + ".exclude_lines", "exclude lines", "", "", false);

    public String getexclude_lines() {
        return excludeLines.Value();
    }

    public SOSFilterOptionsSuperClass setexclude_lines(final String pstrValue) {
        excludeLines.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_empty_lines", description = "exclude empty lines", key = "exclude_empty_lines", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionBoolean excludeEmptyLines = new SOSOptionBoolean(this, CLASSNAME + ".exclude_empty_lines", "exclude empty lines", "false",
            "false", false);

    public String getexclude_empty_lines() {
        return excludeEmptyLines.Value();
    }

    public SOSFilterOptionsSuperClass setexclude_empty_lines(final String pstrValue) {
        excludeEmptyLines.Value(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "include_lines", description = "include lines", key = "include_lines", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp includeLines = new SOSOptionRegExp(this, CLASSNAME + ".include_lines", "include lines", "", "", false);

    public String getinclude_lines() {
        return includeLines.Value();
    }

    public SOSFilterOptionsSuperClass setinclude_lines(final String pstrValue) {
        includeLines.Value(pstrValue);
        return this;
    }

}