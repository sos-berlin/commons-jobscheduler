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
	<!--  the name of the class is taken from the attribute name of the job-tac -->
		<xsl:variable name="class_name">
			<xsl:value-of select="concat(./@name, $ClassNameExtension)" />
		</xsl:variable>

		<xsl:variable name="class_title">
			<xsl:value-of select="./@title" />
		</xsl:variable>

package <xsl:value-of select="$package_name" />;

import java.util.HashMap;

import com.sos.JSHelper.Annotations.JSOptionClass;

import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import org.apache.log4j.Logger;

/**
 * \class 		<xsl:value-of select="$class_name" /> - <xsl:value-of select="$class_title" />
 *
 * \brief
 * An Options as a container for the Options super class.
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *
<xsl:copy-of select="//jobdoc/description/documentation[language=$default_lang]" />
 *
 * see \see <xsl:value-of select="$XMLDocuFilename" /> for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at <xsl:value-of select="$timestamp" />
 * \endverbatim
 */
@JSOptionClass(name = "<xsl:value-of select="$class_name" />", description = "<xsl:value-of select="$class_title" />")
public class <xsl:value-of select="$class_name" /> extends <xsl:value-of select="$ExtendsClassName" /> {
	@SuppressWarnings("unused")
	private final String					conClassName						= "<xsl:value-of select="$class_name" />";
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(<xsl:value-of select="$class_name" />.class);

    /**
    * constructors
    */

	public <xsl:value-of select="$class_name" />() {
	} // public <xsl:value-of select="$class_name" />

	public <xsl:value-of select="$class_name" />(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public <xsl:value-of select="$class_name" />

		//

	public <xsl:value-of select="$class_name" /> (HashMap &lt;String, String&gt; JSSettings) throws Exception {
		super(JSSettings);
	} // public <xsl:value-of select="$class_name" /> (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // <xsl:value-of select="$ExtendsClassName" />
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()
}

</xsl:template>

	<xsl:template match="text()">
		<!-- 	<xsl:value-of select="normalize-space(.)"/> -->
	</xsl:template>


</xsl:stylesheet>