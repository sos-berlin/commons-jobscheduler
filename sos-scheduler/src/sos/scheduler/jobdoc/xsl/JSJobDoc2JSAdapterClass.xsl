<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
    xmlns:xi="http://www.w3.org/2001/XInclude"
	xmlns:java="http://xml.apache.org/xslt/java"
	xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:functx="http://www.functx.com"
	exclude-result-prefixes="jobdoc xhtml java">

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

import java.util.HashMap;

import <xsl:value-of select="$package_name" />.<xsl:value-of select="$WorkerClassName" />;
import <xsl:value-of select="$package_name" />.<xsl:value-of select="$WorkerClassName" />Options;
import sos.spooler.Order;
import sos.spooler.Variable_set;
import sos.scheduler.job.JobSchedulerJobAdapter;  // Super-Class for JobScheduler Java-API-Jobs
import org.apache.log4j.Logger;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.localization.*;
/**
 * \class 		<xsl:value-of select="$class_name" /> - JobScheduler Adapter for "<xsl:value-of select="$class_title" />"
 *
 * \brief AdapterClass of <xsl:value-of select="$WorkerClassName" /> for the SOSJobScheduler
 *
 * This Class <xsl:value-of select="$class_name" /> works as an adapter-class between the SOS
 * JobScheduler and the worker-class <xsl:value-of select="$WorkerClassName" />.
 *
<xsl:copy-of select="//jobdoc/description" />
 *
 * see \see <xsl:value-of select="$XMLDocuFilename" /> for more details.
 *
 * \verbatim ;
 * mechanicaly created by <xsl:value-of select="$XSLTFilename" /> from http://www.sos-berlin.com at <xsl:value-of select="$timestamp" />
 * \endverbatim
 */
public class <xsl:value-of select="$class_name" /> extends JobSchedulerJobAdapter <!-- extends <xsl:value-of select="$ExtendsClassName" /> --> {
	private final String					conClassName						= "<xsl:value-of select="$class_name" />";
	private static Logger		logger			= Logger.getLogger(<xsl:value-of select="$class_name" />.class);

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init";
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process";

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
            throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
   		}
		finally {
		} // finally
        return signalSuccess();

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		<xsl:value-of select="$WorkerClassName" /> objR = new <xsl:value-of select="$WorkerClassName" />();
		<xsl:value-of select="$WorkerClassName" />Options objO = objR.Options();

        objO.CurrentNodeName(this.getCurrentNodeName());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CheckMandatory();
        objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}

</xsl:template>

	<xsl:template match="text()">
		<!-- 	<xsl:value-of select="normalize-space(.)"/> -->
	</xsl:template>


</xsl:stylesheet>