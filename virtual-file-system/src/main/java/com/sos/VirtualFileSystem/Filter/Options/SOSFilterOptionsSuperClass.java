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

    @JSOptionDefinition(name = "Line_numbering_position", description = "Insert line number at position", key = "Line_numbering_position",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger lineNumberingPosition = new SOSOptionInteger(this, CLASSNAME + ".Line_numbering_position", "Insert line number at position", "1", "1",
            false);

    public SOSOptionInteger getLineNumberingPosition() {
        return lineNumberingPosition;
    }

    public SOSFilterOptionsSuperClass setLineNumberingPosition(final SOSOptionInteger pstrValue) {
        lineNumberingPosition = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Line_Numbering_Format", description = "Format mask of line numbering value", key = "Line_Numbering_Format",
            type = "SOSOptionString", mandatory = false)
    public SOSOptionString lineNumberingFormat = new SOSOptionString(this, CLASSNAME + ".Line_Numbering_Format", "Format mask of line numbering value", 
            "%08d", "%08d", false);

    public SOSOptionString getLineNumberingFormat() {
        return lineNumberingFormat;
    }

    public SOSFilterOptionsSuperClass setLineNumberingFormat(final SOSOptionString pstrValue) {
        lineNumberingFormat = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Line_Numbering_Increment_Value", description = "Increment value for line-numbering", key = "Line_Numbering_Increment_Value",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger lineNumberingIncrementValue = new SOSOptionInteger(this, CLASSNAME + ".Line_Numbering_Increment_Value", 
            "Increment value for line-numbering", "1", "1", false);

    public SOSOptionInteger getLineNumberingIncrementValue() {
        return lineNumberingIncrementValue;
    }

    public SOSFilterOptionsSuperClass setLineNumberingIncrementValue(final SOSOptionInteger pstrValue) {
        lineNumberingIncrementValue = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Line_numbering_start_value", description = "STart value for line numbering", key = "Line_numbering_start_value",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger lineNumberingStartValue = new SOSOptionInteger(this, CLASSNAME + ".Line_numbering_start_value", "Start value for line numbering",
            "1", "1", false);

    public SOSOptionInteger getLineNumberingStartValue() {
        return lineNumberingStartValue;
    }

    public SOSFilterOptionsSuperClass setLineNumberingStartValue(final SOSOptionInteger pstrValue) {
        lineNumberingStartValue = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "create_line_numbers", description = "Create line number for all lines of a file", key = "create_line_numbers",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean createLineNumbers = new SOSOptionBoolean(this, CLASSNAME + ".create_line_numbers", "Create line number for all lines of a file",
            "false", "false", false);

    public SOSOptionBoolean getCreateLineNumbers() {
        return createLineNumbers;
    }

    public SOSFilterOptionsSuperClass setCreateLineNumbers(final SOSOptionBoolean pstrValue) {
        createLineNumbers = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "renumber_lines", description = "Make a renumbering of all lines in a file", key = "renumber_lines", type = "SOSOptionBoolean",
            mandatory = false)
    public SOSOptionBoolean renumberLines = new SOSOptionBoolean(this, CLASSNAME + ".renumber_lines", "Make a renumbering of all lines in a file", "false", 
            "false", false);

    public SOSOptionBoolean getRenumberLines() {
        return renumberLines;
    }

    public SOSFilterOptionsSuperClass setRenumberLines(final SOSOptionBoolean pstrValue) {
        renumberLines = pstrValue;
        return this;
    }

    @JSOptionDefinition(name = "Filter_sequence", description = "Filter definitions: what to process and in what sequence", key = "Filter_sequence",
            type = "SOSOptionValueList", mandatory = false)
    public SOSOptionFilterSequence filterSequence = new SOSOptionFilterSequence(this, CLASSNAME + ".Filter_sequence", 
            "Filter definitions: what to process and in what sequence", "nullFilter", "", true);

    public String getFilterSequence() {
        return filterSequence.getValue();
    }

    public SOSFilterOptionsSuperClass setFilterSequence(final String pstrValue) {
        filterSequence.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "do_replacing", description = "Activate replacing", key = "do_replacing", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean doReplacing = new SOSOptionBoolean(this, CLASSNAME + ".do_replacing", "Activate replacing", "false", "false", false);

    public String getDoReplacing() {
        return doReplacing.getValue();
    }

    public SOSFilterOptionsSuperClass setDoReplacing(final String pstrValue) {
        doReplacing.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "replace_what", description = "Replacing what", key = "replace_what", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replaceWhat = new SOSOptionRegExp(this, CLASSNAME + ".replace_what", "Replacing what", "", "", false);

    public String getReplaceWhat() {
        return replaceWhat.getValue();
    }

    public SOSFilterOptionsSuperClass setReplaceWhat(final String pstrValue) {
        replaceWhat.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "replace_with", description = "replace with", key = "replace_with", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp replaceWith = new SOSOptionRegExp(this, CLASSNAME + ".replace_with", "replaced with", "", "", false);

    public String getReplaceWith() {
        return replaceWith.getValue();
    }

    public SOSFilterOptionsSuperClass setReplaceWith(final String pstrValue) {
        replaceWith.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_lines_before", description = "exclude_lines_before", key = "exclude_lines_before", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionRegExp excludeLinesBefore = new SOSOptionRegExp(this, CLASSNAME + ".exclude_lines_before", "exclude_lines_before", "", "", false);

    public String getExcludeLinesBefore() {
        return excludeLinesBefore.getValue();
    }

    public SOSFilterOptionsSuperClass setExcludeLinesBefore(final String pstrValue) {
        excludeLinesBefore.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_lines_after", description = "exclude_lines_after", key = "exclude_lines_after", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionRegExp excludeLinesAfter = new SOSOptionRegExp(this, CLASSNAME + ".exclude_lines_after", "exclude_lines_after", "", "", false);

    public String getExcludeLinesAfter() {
        return excludeLinesAfter.getValue();
    }

    public SOSFilterOptionsSuperClass setExcludeLinesAfter(final String pstrValue) {
        excludeLinesAfter.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_lines", description = "exclude lines", key = "exclude_lines", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp excludeLines = new SOSOptionRegExp(this, CLASSNAME + ".exclude_lines", "exclude lines", "", "", false);

    public String getExcludeLines() {
        return excludeLines.getValue();
    }

    public SOSFilterOptionsSuperClass setExcludeLines(final String pstrValue) {
        excludeLines.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "exclude_empty_lines", description = "exclude empty lines", key = "exclude_empty_lines", type = "SOSOptionRegExp",
            mandatory = false)
    public SOSOptionBoolean excludeEmptyLines = new SOSOptionBoolean(this, CLASSNAME + ".exclude_empty_lines", "exclude empty lines", "false", "false", false);

    public String getExcludeEmptyLines() {
        return excludeEmptyLines.getValue();
    }

    public SOSFilterOptionsSuperClass setExcludeEmptyLines(final String pstrValue) {
        excludeEmptyLines.setValue(pstrValue);
        return this;
    }

    @JSOptionDefinition(name = "include_lines", description = "include lines", key = "include_lines", type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp includeLines = new SOSOptionRegExp(this, CLASSNAME + ".include_lines", "include lines", "", "", false);

    public String getIncludeLines() {
        return includeLines.getValue();
    }

    public SOSFilterOptionsSuperClass setIncludeLines(final String pstrValue) {
        includeLines.setValue(pstrValue);
        return this;
    }

}