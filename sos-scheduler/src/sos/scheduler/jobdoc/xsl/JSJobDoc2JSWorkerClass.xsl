<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="jobdoc">

	<xsl:output method="text" encoding="iso-8859-1" indent="no" />

<xsl:param name="Category" required="no" as="xs:string"/>
<xsl:param name="ClassNameExtension" required="yes" as="xs:string"/>
<xsl:param name="ExtendsClassName"  required="yes" as="xs:string"/>
<xsl:param name="WorkerClassName"  required="yes" as="xs:string"/>
<xsl:param name="XMLDocuFilename"  required="yes" as="xs:string"/>
<xsl:param name="XSLTFilename"  required="yes" as="xs:string"/>

<xsl:param name="timestamp" required="yes" as="xs:string"/>
<xsl:param name="package_name" required="yes" as="xs:string"/>
<xsl:param name="standalone" required="yes" as="xs:string"/>
<xsl:param name="sourcetype" required="yes" as="xs:string"/>
<xsl:param name="keywords" required="no" as="xs:string"/>
<xsl:param name="default_lang" required="yes" as="xs:string"/>


	<xsl:variable name="nl" select="'&#xa;'" />

	<xsl:template match="//jobdoc:description">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="jobdoc:job">
	<!--  the name of the class is derived from the attribute name of the job-tac -->
		<xsl:variable name="class_name">
			<xsl:value-of select="concat(./@name, $ClassNameExtension)" />
		</xsl:variable>

		<xsl:variable name="class_title">
			<xsl:value-of select="./@title" />
		</xsl:variable>

package <xsl:value-of select="$package_name" />;

import <xsl:value-of select="$package_name" />.<xsl:value-of select="$WorkerClassName" />;
import <xsl:value-of select="$package_name" />.<xsl:value-of select="$WorkerClassName" />Options;
import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.localization.*;
import com.sos.JSHelper.Basics.JSJobUtilities;

/**
 * \class 		<xsl:value-of select="$class_name" /> - Workerclass for "<xsl:value-of select="$class_title" />"
 *
 * \brief AdapterClass of <xsl:value-of select="$WorkerClassName" /> for the SOSJobScheduler
 *
 * This Class <xsl:value-of select="$class_name" /> is the worker-class.
 *
<xsl:copy-of select="//jobdoc/description[lang=$default_lang]" />
 *
 * see \see <xsl:value-of select="$XMLDocuFilename" /> for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by <xsl:value-of select="$XSLTFilename" /> from http://www.sos-berlin.com at <xsl:value-of select="$timestamp" />
 * \endverbatim
 */
public class <xsl:value-of select="$class_name" /> extends JSJobUtilities <xsl:value-of select="$class_name"/>Options  {
	private final String					conClassName						= "<xsl:value-of select="$class_name" />";
	private static Logger		logger			= Logger.getLogger(<xsl:value-of select="$class_name" />.class);

	/**
	 *
	 * \brief <xsl:value-of select="$class_name" />
	 *
	 * \details
	 *
	 */
	public <xsl:value-of select="$class_name" />() {
		super(new <xsl:value-of select="$class_name"/>Options());
	}

	/**
	 *
	 * \brief Execute - Start the Execution of <xsl:value-of select="$class_name" />
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see <xsl:value-of select="$class_name" />Main
	 *
	 * \return <xsl:value-of select="$class_name" />
	 *
	 * @return
	 */
	public <xsl:value-of select="$class_name" /> Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";

        JSJ_I_110.toLog(conMethodName);

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());

		}
		catch (Exception e) {
            throw new JobSchedulerException(JSJ_F_107.get(conMethodName) + ":"+ e.getMessage(), e);
		}
		finally {
		}

        JSJ_I_111.toLog(conMethodName);
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}


}  // class <xsl:value-of select="$class_name" />

</xsl:template>

	<xsl:template match="text()">
		<!-- 	<xsl:value-of select="normalize-space(.)"/> -->
	</xsl:template>


</xsl:stylesheet>