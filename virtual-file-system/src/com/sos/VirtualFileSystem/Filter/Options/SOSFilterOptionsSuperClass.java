/**
 *
 */
package com.sos.VirtualFileSystem.Filter.Options;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionRegExp;

/**
 * @author KB
 *
 */
public abstract class SOSFilterOptionsSuperClass extends JSOptionsClass {

	@SuppressWarnings("unused")
	private final String		conClassName		= this.getClass().getSimpleName();
	@SuppressWarnings("unused")
	private static final String	conSVNVersion		= "$Id$";
	@SuppressWarnings("unused")
	private final Logger		logger				= Logger.getLogger(this.getClass());
	/**
	 *
	 */
	private static final long	serialVersionUID	= 7496305355199139721L;

	public SOSFilterOptionsSuperClass() {
		// super("SOSVirtualFileSystem");
		objParentClass = this.getClass();
	} // public SOSFtpOptionsSuperClass

	public SOSFilterOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SOSFtpOptionsSuperClass (HashMap JSSettings)

	/**
	 * \option Filter_sequence
	 * \type SOSOptionValueList
	 * \brief Filter_sequence - Filter definitions: what to process and in what sequence
	 *
	 * \details
	 * Filter definitions: what to process and in what sequence
	 *
	 * \mandatory: false
	 *
	 * \created 24.11.2013 17:12:19 by KB
	 */
	@JSOptionDefinition(name = "Filter_sequence", description = "Filter definitions: what to process and in what sequence", key = "Filter_sequence", type = "SOSOptionValueList", mandatory = false)
	public SOSOptionFilterSequence	FilterSequence	= new SOSOptionFilterSequence( // ...
															this, // ....
															conClassName + ".Filter_sequence", // ...
															"Filter definitions: what to process and in what sequence", // ...
															"nullFilter", // ...
															"", // ...
															true);

	public String getFilter_sequence() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getFilter_sequence";

		return FilterSequence.Value();
	} // public String getFilter_sequence

	public SOSFilterOptionsSuperClass setFilter_sequence(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setFilter_sequence";
		FilterSequence.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setFilter_sequence

	/**
	 * \option do_replacing
	 * \type SOSOptionBoolean
	 * \brief do_replacing - Activate replacing
	 *
	 * \details
	 * Activate replacing
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 17:26:45 by KB
	 */
	@JSOptionDefinition(name = "do_replacing", description = "Activate replacing", key = "do_replacing", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	doReplacing	= new SOSOptionBoolean( // ...
												this, // ....
												conClassName + ".do_replacing", // ...
												"Activate replacing", // ...
												"false", // ...
												"false", // ...
												false);

	public String getdo_replacing() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getdo_replacing";

		return doReplacing.Value();
	} // public String getdo_replacing

	public SOSFilterOptionsSuperClass setdo_replacing(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setdo_replacing";
		doReplacing.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setdo_replacing

	/**
	 * \option replace_what
	 * \type SOSOptionRegExp
	 * \brief replace_what - Replace
	 *
	 * \details
	 * Replacing what
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 17:25:06 by KB
	 */
	@JSOptionDefinition(name = "replace_what", description = "Replacing what", key = "replace_what", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	replaceWhat	= new SOSOptionRegExp( // ...
												this, // ....
												conClassName + ".replace_what", // ...
												"Replacing what", // ...
												"", // ...
												"", // ...
												false);

	public String getreplace_what() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getreplace_what";

		return replaceWhat.Value();
	} // public String getreplace_what

	public SOSFilterOptionsSuperClass setreplace_what(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setreplace_what";
		replaceWhat.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setreplace_what

	/**
	 * \option replace_with
	 * \type SOSOptionRegExp
	 * \brief replace_with - replace with
	 *
	 * \details
	 * replacd with
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 17:29:19 by KB
	 */
	@JSOptionDefinition(name = "replace_with", description = "replace with", key = "replace_with", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	replaceWith	= new SOSOptionRegExp( // ...
												this, // ....
												conClassName + ".replace_with", // ...
												"replacd with", // ...
												"", // ...
												"", // ...
												false);

	public String getreplace_with() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getreplace_with";

		return replaceWith.Value();
	} // public String getreplace_with

	public SOSFilterOptionsSuperClass setreplace_with(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setreplace_with";
		replaceWith.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setreplace_with

	/**
	 * \option exclude_lines_before
	 * \type SOSOptionRegExp
	 * \brief exclude_lines_before - exclude_lines_before
	 *
	 * \details
	 * exclude_lines_before
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 17:34:09 by KB
	 */
	@JSOptionDefinition(name = "exclude_lines_before", description = "exclude_lines_before", key = "exclude_lines_before", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	excludeLinesBefore	= new SOSOptionRegExp( // ...
														this, // ....
														conClassName + ".exclude_lines_before", // ...
														"exclude_lines_before", // ...
														"", // ...
														"", // ...
														false);

	public String getexclude_lines_before() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getexclude_lines_before";

		return excludeLinesBefore.Value();
	} // public String getexclude_lines_before

	public SOSFilterOptionsSuperClass setexclude_lines_before(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setexclude_lines_before";
		excludeLinesBefore.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setexclude_lines_before

	/**
	 * \option exclude_lines_after
	 * \type SOSOptionRegExp
	 * \brief exclude_lines_after - exclude_lines_after
	 *
	 * \details
	 * exclude_lines_after
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 17:35:45 by KB
	 */
	@JSOptionDefinition(name = "exclude_lines_after", description = "exclude_lines_after", key = "exclude_lines_after", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	exclude_lines_after	= new SOSOptionRegExp( // ...
														this, // ....
														conClassName + ".exclude_lines_after", // ...
														"exclude_lines_after", // ...
														"", // ...
														"", // ...
														false);

	public String getexclude_lines_after() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getexclude_lines_after";

		return exclude_lines_after.Value();
	} // public String getexclude_lines_after

	public SOSFilterOptionsSuperClass setexclude_lines_after(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setexclude_lines_after";
		exclude_lines_after.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setexclude_lines_after

	/**
	 * \option exclude_lines
	 * \type SOSOptionRegExp
	 * \brief exclude_lines - exclude lines
	 *
	 * \details
	 * exclude lines
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 19:50:37 by KB
	 */
	@JSOptionDefinition(name = "exclude_lines", description = "exclude lines", key = "exclude_lines", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	excludeLines	= new SOSOptionRegExp( // ...
													this, // ....
													conClassName + ".exclude_lines", // ...
													"exclude lines", // ...
													"", // ...
													"", // ...
													false);

	public String getexclude_lines() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getexclude_lines";

		return excludeLines.Value();
	} // public String getexclude_lines

	public SOSFilterOptionsSuperClass setexclude_lines(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setexclude_lines";
		excludeLines.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setexclude_lines

	/**
	 * \option exclude_empty_lines
	 * \type SOSOptionRegExp
	 * \brief exclude_empty_lines - exclude empty lines
	 *
	 * \details
	 * exclude empty lines
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 19:49:08 by KB
	 */
	@JSOptionDefinition(name = "exclude_empty_lines", description = "exclude empty lines", key = "exclude_empty_lines", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionBoolean	excludeEmptyLines	= new SOSOptionBoolean( // ...
														this, // ....
														conClassName + ".exclude_empty_lines", // ...
														"exclude empty lines", // ...
														"false", // ...
														"false", // ...
														false);

	public String getexclude_empty_lines() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getexclude_empty_lines";

		return excludeEmptyLines.Value();
	} // public String getexclude_empty_lines

	public SOSFilterOptionsSuperClass setexclude_empty_lines(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setexclude_empty_lines";
		excludeEmptyLines.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setexclude_empty_lines

	/**
	 * \option include_lines
	 * \type SOSOptionRegExp
	 * \brief include_lines - include lines
	 *
	 * \details
	 * include lines
	 *
	 * \mandatory: false
	 *
	 * \created 20.11.2013 19:47:43 by KB
	 */
	@JSOptionDefinition(name = "include_lines", description = "include lines", key = "include_lines", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	includeLines	= new SOSOptionRegExp( // ...
													this, // ....
													conClassName + ".include_lines", // ...
													"include lines", // ...
													"", // ...
													"", // ...
													false);

	public String getinclude_lines() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getinclude_lines";

		return includeLines.Value();
	} // public String getinclude_lines

	public SOSFilterOptionsSuperClass setinclude_lines(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setinclude_lines";
		includeLines.Value(pstrValue);
		return this;
	} // public SOSFilterOptionsSuperClass setinclude_lines

}
