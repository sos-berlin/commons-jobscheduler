package sos.scheduler.file;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionGracious;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJobChainNode;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionRelOp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;

@JSOptionClass(name = "JSExistsFileOptionsSuperClass", description = "JSExistsFileOptionsSuperClass")
public class JSExistsFileOptionsSuperClass extends JSOptionsClass {

    private static final long serialVersionUID = -5168467682765829432L;
    private static final String CLASSNAME = "JSExistsFileOptionsSuperClass";

    @JSOptionDefinition(name = "count_files", description = "Return the size of resultset If this parameter is set true ", key = "count_files",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean count_files = new SOSOptionBoolean(this, CLASSNAME + ".count_files", "Return the size of resultset If this parameter is set true ",
            "false", "false", false);
    public SOSOptionBoolean DoCountFiles = (SOSOptionBoolean) count_files.SetAlias(CLASSNAME + ".DoCountFiles");

    public SOSOptionBoolean getcount_files() {
        return count_files;
    }

    public void setcount_files(SOSOptionBoolean p_count_files) {
        this.count_files = p_count_files;
    }

    @JSOptionDefinition(name = "create_order", description = "Activate file-order creation With this parameter it is possible to specif", key = "create_order",
            type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_order = new SOSOptionBoolean(this, CLASSNAME + ".create_order",
            "Activate file-order creation With this parameter it is possible to specif", "false", "false", false);

    public SOSOptionBoolean getcreate_order() {
        return create_order;
    }

    public void setcreate_order(SOSOptionBoolean p_create_order) {
        this.create_order = p_create_order;
    }

    @JSOptionDefinition(name = "create_orders_for_all_files", description = "Create a file-order for every file in the result-list",
            key = "create_orders_for_all_files", type = "SOSOptionBoolean", mandatory = false)
    public SOSOptionBoolean create_orders_for_all_files = new SOSOptionBoolean(this, CLASSNAME + ".create_orders_for_all_files",
            "Create a file-order for every file in the result-list", "false", "false", false);

    public SOSOptionBoolean getcreate_orders_for_all_files() {
        return create_orders_for_all_files;
    }

    public void setcreate_orders_for_all_files(SOSOptionBoolean p_create_orders_for_all_files) {
        this.create_orders_for_all_files = p_create_orders_for_all_files;
    }

    @JSOptionDefinition(name = "expected_size_of_result_set", description = "number of expected hits in result-list", key = "expected_size_of_result_set",
            type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger expected_size_of_result_set = new SOSOptionInteger(this, CLASSNAME + ".expected_size_of_result_set",
            "number of expected hits in result-list", "0", "0", false);

    public SOSOptionInteger getexpected_size_of_result_set() {
        return expected_size_of_result_set;
    }

    public void setexpected_size_of_result_set(SOSOptionInteger p_expected_size_of_result_set) {
        this.expected_size_of_result_set = p_expected_size_of_result_set;
    }

    @JSOptionDefinition(name = "file", description = "File or Folder to watch for Checked file or directory Supports", key = "file", type = "SOSOptionString",
            mandatory = true)
    public SOSOptionFileName file = new SOSOptionFileName(this, CLASSNAME + ".file", "File or Folder to watch for Checked file or directory Supports", ".",
            ".", true);
    public SOSOptionFileName FileName = (SOSOptionFileName) file.SetAlias(CLASSNAME + ".FileName");

    public SOSOptionFileName getfile() {
        return file;
    }

    public void setfile(SOSOptionFileName p_file) {
        this.file = p_file;
    }

    @JSOptionDefinition(name = "target", description = "target or Folder to watch for Checked target or directory Supports", key = "target",
            type = "SOSOptionString", mandatory = true)
    public SOSOptionFileName target = new SOSOptionFileName(this, CLASSNAME + ".target", "target or Folder to watch for Checked target or directory Supports",
            ".", ".", true);

    @JSOptionDefinition(name = "file_spec", description = "Regular Expression for filename filtering Regular Expression for file fi", key = "file_spec",
            type = "SOSOptionRegExp", mandatory = false)
    public SOSOptionRegExp file_spec = new SOSOptionRegExp(this, CLASSNAME + ".file_spec",
            "Regular Expression for filename filtering Regular Expression for file fi", ".*", ".*", false);
    public SOSOptionRegExp FileNameRegExp = (SOSOptionRegExp) file_spec.SetAlias(CLASSNAME + ".FileNameRegExp");

    public SOSOptionRegExp getfile_spec() {
        return file_spec;
    }

    public void setfile_spec(SOSOptionRegExp p_file_spec) {
        this.file_spec = p_file_spec;
    }

    @JSOptionDefinition(name = "gracious", description = "Specify error message tolerance Enables or disables error messages that", key = "gracious",
            type = "SOSOptionGracious", mandatory = false)
    public SOSOptionGracious gracious = new SOSOptionGracious(this, CLASSNAME + ".gracious",
            "Specify error message tolerance Enables or disables error messages that", "false", "false", false);
    public SOSOptionGracious ErrorBehaviour = (SOSOptionGracious) gracious.SetAlias(CLASSNAME + ".ErrorBehaviour");

    public SOSOptionGracious getgracious() {
        return gracious;
    }

    public void setgracious(SOSOptionGracious p_gracious) {
        this.gracious = p_gracious;
    }

    @JSOptionDefinition(name = "max_file_age", description = "maximum age of a file Specifies the maximum age of a file. If a file", key = "max_file_age",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime max_file_age = new SOSOptionTime(this, CLASSNAME + ".max_file_age",
            "maximum age of a file Specifies the maximum age of a file. If a file", "0", "0", false);
    public SOSOptionTime FileAgeMaximum = (SOSOptionTime) max_file_age.SetAlias(CLASSNAME + ".FileAgeMaximum");

    public SOSOptionTime getmax_file_age() {
        return max_file_age;
    }

    public void setmax_file_age(SOSOptionTime p_max_file_age) {
        this.max_file_age = p_max_file_age;
    }

    @JSOptionDefinition(name = "max_file_size", description = "maximum size of a file Specifies the maximum size of a file in", key = "max_file_size",
            type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize max_file_size = new SOSOptionFileSize(this, CLASSNAME + ".max_file_size",
            "maximum size of a file Specifies the maximum size of a file in", "-1", "-1", false);
    public SOSOptionFileSize FileSizeMaximum = (SOSOptionFileSize) max_file_size.SetAlias(CLASSNAME + ".FileSizeMaximum");

    public SOSOptionFileSize getmax_file_size() {
        return max_file_size;
    }

    public void setmax_file_size(SOSOptionFileSize p_max_file_size) {
        this.max_file_size = p_max_file_size;
    }

    @JSOptionDefinition(name = "min_file_age", description = "minimum age of a file Specifies the minimum age of a files. If the fi", key = "min_file_age",
            type = "SOSOptionTime", mandatory = false)
    public SOSOptionTime min_file_age = new SOSOptionTime(this, CLASSNAME + ".min_file_age",
            "minimum age of a file Specifies the minimum age of a files. If the fi", "0", "0", false);
    public SOSOptionTime FileAgeMinimum = (SOSOptionTime) min_file_age.SetAlias(CLASSNAME + ".FileAgeMinimum");

    public SOSOptionTime getmin_file_age() {
        return min_file_age;
    }

    public void setmin_file_age(SOSOptionTime p_min_file_age) {
        this.min_file_age = p_min_file_age;
    }

    @JSOptionDefinition(name = "min_file_size", description = "minimum size of one or multiple files Specifies the minimum size of one", key = "min_file_size",
            type = "SOSOptionFileSize", mandatory = false)
    public SOSOptionFileSize min_file_size = new SOSOptionFileSize(this, CLASSNAME + ".min_file_size",
            "minimum size of one or multiple files Specifies the minimum size of one", "-1", "-1", false);
    public SOSOptionFileSize FileSizeMinimum = (SOSOptionFileSize) min_file_size.SetAlias(CLASSNAME + ".FileSizeMinimum");

    public SOSOptionFileSize getmin_file_size() {
        return min_file_size;
    }

    public void setmin_file_size(SOSOptionFileSize p_min_file_size) {
        this.min_file_size = p_min_file_size;
    }

    @JSOptionDefinition(name = "next_state", description = "The first node to execute in a jobchain The name of the node of a jobchai", key = "next_state",
            type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode next_state = new SOSOptionJobChainNode(this, CLASSNAME + ".next_state",
            "The first node to execute in a jobchain The name of the node of a jobchai", " ", " ", false);

    public SOSOptionJobChainNode getnext_state() {
        return next_state;
    }

    public void setnext_state(SOSOptionJobChainNode p_next_state) {
        this.next_state = p_next_state;
    }

    @JSOptionDefinition(name = "on_empty_result_set", description = "Set next node on empty result set The next Node (Step, Job) to execute i",
            key = "on_empty_result_set", type = "SOSOptionJobChainNode", mandatory = false)
    public SOSOptionJobChainNode on_empty_result_set = new SOSOptionJobChainNode(this, CLASSNAME + ".on_empty_result_set",
            "Set next node on empty result set The next Node (Step, Job) to execute i", "", "", false);

    public SOSOptionJobChainNode geton_empty_result_set() {
        return on_empty_result_set;
    }

    public void seton_empty_result_set(SOSOptionJobChainNode p_on_empty_result_set) {
        this.on_empty_result_set = p_on_empty_result_set;
    }

    @JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch",
            key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
    public SOSOptionString order_jobchain_name = new SOSOptionString(this, CLASSNAME + ".order_jobchain_name",
            "The name of the jobchain which belongs to the order The name of the jobch", " ", " ", false);

    public SOSOptionString getorder_jobchain_name() {
        return order_jobchain_name;
    }

    public void setorder_jobchain_name(SOSOptionString p_order_jobchain_name) {
        this.order_jobchain_name = p_order_jobchain_name;
    }

    @JSOptionDefinition(name = "raise_error_if_result_set_is", description = "raise error on expected size of result-set With this parameter it is poss",
            key = "raise_error_if_result_set_is", type = "SOSOptionRelOp", mandatory = false)
    public SOSOptionRelOp raise_error_if_result_set_is = new SOSOptionRelOp(this, CLASSNAME + ".raise_error_if_result_set_is",
            "raise error on expected size of result-set With this parameter it is poss", "", "", false);

    public SOSOptionRelOp getraise_error_if_result_set_is() {
        return raise_error_if_result_set_is;
    }

    public void setraise_error_if_result_set_is(SOSOptionRelOp p_raise_error_if_result_set_is) {
        this.raise_error_if_result_set_is = p_raise_error_if_result_set_is;
    }

    @JSOptionDefinition(name = "result_list_file", description = "Name of the result-list file If the value of this parameter specifies a v",
            key = "result_list_file", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName result_list_file = new SOSOptionFileName(this, CLASSNAME + ".result_list_file",
            "Name of the result-list file If the value of this parameter specifies a v", "", "", false);

    public SOSOptionFileName getresult_list_file() {
        return result_list_file;
    }

    public void setresult_list_file(SOSOptionFileName p_result_list_file) {
        this.result_list_file = p_result_list_file;
    }

    @JSOptionDefinition(name = "scheduler_file_name", description = "Name of the file to process for a file-order", key = "scheduler_file_name",
            type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_name = new SOSOptionFileName(this, CLASSNAME + ".scheduler_file_name",
            "Name of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getscheduler_file_name() {
        return scheduler_file_name;
    }

    public void setscheduler_file_name(SOSOptionFileName p_scheduler_file_name) {
        this.scheduler_file_name = p_scheduler_file_name;
    }

    @JSOptionDefinition(name = "scheduler_file_parent", description = "pathanme of the file to process for a file-order", key = "scheduler_file_parent",
            type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_parent = new SOSOptionFileName(this, CLASSNAME + ".scheduler_file_parent",
            "pathanme of the file to process for a file-order", "", "", false);

    public SOSOptionFileName getscheduler_file_parent() {
        return scheduler_file_parent;
    }

    public void setscheduler_file_parent(SOSOptionFileName p_scheduler_file_parent) {
        this.scheduler_file_parent = p_scheduler_file_parent;
    }

    @JSOptionDefinition(name = "scheduler_file_path", description = "file to process for a file-order Using Directory Monitoring with",
            key = "scheduler_file_path", type = "SOSOptionFileName", mandatory = false)
    public SOSOptionFileName scheduler_file_path = new SOSOptionFileName(this, CLASSNAME + ".scheduler_file_path",
            "file to process for a file-order Using Directory Monitoring with", "", "", false);

    public SOSOptionFileName getscheduler_file_path() {
        return scheduler_file_path;
    }

    public void setscheduler_file_path(SOSOptionFileName p_scheduler_file_path) {
        this.scheduler_file_path = p_scheduler_file_path;
    }

    @JSOptionDefinition(name = "scheduler_sosfileoperations_file_count", description = "Return the size of the result set after a file operation",
            key = "scheduler_sosfileoperations_file_count", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger scheduler_sosfileoperations_file_count = new SOSOptionInteger(this, CLASSNAME + ".scheduler_sosfileoperations_file_count",
            "Return the size of the result set after a file operation", "0", "0", false);
    public SOSOptionInteger FileCount = (SOSOptionInteger) scheduler_sosfileoperations_file_count.SetAlias(CLASSNAME + ".FileCount");

    public SOSOptionInteger getscheduler_sosfileoperations_file_count() {
        return scheduler_sosfileoperations_file_count;
    }

    public void setscheduler_sosfileoperations_file_count(SOSOptionInteger p_scheduler_sosfileoperations_file_count) {
        this.scheduler_sosfileoperations_file_count = p_scheduler_sosfileoperations_file_count;
    }

    @JSOptionDefinition(name = "scheduler_sosfileoperations_resultset", description = "The result of the operation as a list of items",
            key = "scheduler_sosfileoperations_resultset", type = "SOSOptionstring", mandatory = false)
    public SOSOptionString scheduler_sosfileoperations_resultset = new SOSOptionString(this, CLASSNAME + ".scheduler_sosfileoperations_resultset",
            "The result of the operation as a list of items", "", "", false);
    public SOSOptionString ResultSet = (SOSOptionString) scheduler_sosfileoperations_resultset.SetAlias(CLASSNAME + ".ResultSet");

    public SOSOptionString getscheduler_sosfileoperations_resultset() {
        return scheduler_sosfileoperations_resultset;
    }

    public void setscheduler_sosfileoperations_resultset(SOSOptionString p_scheduler_sosfileoperations_resultset) {
        this.scheduler_sosfileoperations_resultset = p_scheduler_sosfileoperations_resultset;
    }

    @JSOptionDefinition(name = "scheduler_sosfileoperations_resultsetsize", description = "The amount of hits in the result set of the operation",
            key = "scheduler_sosfileoperations_resultsetsize", type = "SOSOptionsInteger", mandatory = false)
    public SOSOptionInteger scheduler_sosfileoperations_resultsetsize = new SOSOptionInteger(this, CLASSNAME + ".scheduler_sosfileoperations_resultsetsize",
            "The amount of hits in the result set of the operation", "", "", false);
    public SOSOptionInteger ResultSetSize = (SOSOptionInteger) scheduler_sosfileoperations_resultsetsize.SetAlias(CLASSNAME + ".ResultSetSize");

    public SOSOptionInteger getscheduler_sosfileoperations_resultsetsize() {
        return scheduler_sosfileoperations_resultsetsize;
    }

    public void setscheduler_sosfileoperations_resultsetsize(SOSOptionInteger p_scheduler_sosfileoperations_resultsetsize) {
        this.scheduler_sosfileoperations_resultsetsize = p_scheduler_sosfileoperations_resultsetsize;
    }

    @JSOptionDefinition(name = "skip_first_files", description = "number of files to remove from the top of the result-set The numbe",
            key = "skip_first_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skip_first_files = new SOSOptionInteger(this, CLASSNAME + ".skip_first_files", 
            "number of files to remove from the top of the result-set The numbe", "0", "0", false);
    public SOSOptionInteger NoOfFirstFiles2Skip = (SOSOptionInteger) skip_first_files.SetAlias(CLASSNAME + ".NoOfFirstFiles2Skip");

    public SOSOptionInteger getskip_first_files() {
        return skip_first_files;
    }

    public void setskip_first_files(SOSOptionInteger p_skip_first_files) {
        this.skip_first_files = p_skip_first_files;
    }

    @JSOptionDefinition(name = "skip_last_files", description = "number of files to remove from the bottom of the result-set The numbe",
            key = "skip_last_files", type = "SOSOptionInteger", mandatory = false)
    public SOSOptionInteger skip_last_files = new SOSOptionInteger(this, CLASSNAME + ".skip_last_files", 
            "number of files to remove from the bottom of the result-set The numbe", "0", "0", false);
    public SOSOptionInteger NoOfLastFiles2Skip = (SOSOptionInteger) skip_last_files.SetAlias(CLASSNAME + ".NoOfLastFiles2Skip");

    public SOSOptionInteger getskip_last_files() {
        return skip_last_files;
    }

    public void setskip_last_files(SOSOptionInteger p_skip_last_files) {
        this.skip_last_files = p_skip_last_files;
    }

    public JSExistsFileOptionsSuperClass() {
        objParentClass = this.getClass();
    }

    public JSExistsFileOptionsSuperClass(JSListener pobjListener) {
        this();
        this.registerMessageListener(pobjListener);
    }

    public JSExistsFileOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
        this();
        this.setAllOptions(JSSettings);
    }

    public void setAllOptions(HashMap<String, String> pobjJSSettings) {
        flgSetAllOptions = true;
        objSettings = pobjJSSettings;
        super.Settings(objSettings);
        super.setAllOptions(pobjJSSettings);
        flgSetAllOptions = false;
    }

    @Override
    public void CheckMandatory() throws JSExceptionMandatoryOptionMissing, Exception {
        try {
            super.CheckMandatory();
        } catch (Exception e) {
            throw new JSExceptionMandatoryOptionMissing(e.toString());
        }
    }

    @Override
    public void CommandLineArgs(String[] pstrArgs) {
        super.CommandLineArgs(pstrArgs);
        this.setAllOptions(super.objSettings);
    }

}