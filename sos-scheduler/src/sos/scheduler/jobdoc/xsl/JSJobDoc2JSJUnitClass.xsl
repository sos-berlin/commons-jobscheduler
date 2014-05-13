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

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		<xsl:value-of select="$class_name" /> - JUnit-Test for "<xsl:value-of select="$class_title" />"
 *
 * \brief MainClass to launch <xsl:value-of select="$WorkerClassName" /> as an executable command-line program
 *
<xsl:copy-of select="//jobdoc/description/documentation[language=$default_lang]" />
 *
 * see \see <xsl:value-of select="$XMLDocuFilename" /> for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by <xsl:value-of select="$XSLTFilename" /> from http://www.sos-berlin.com at <xsl:value-of select="$timestamp" /> 
 * \endverbatim
 */
public class <xsl:value-of select="$class_name" /> extends <xsl:value-of select="$ExtendsClassName" /> {
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private final static String					conClassName						= "<xsl:value-of select="$class_name" />"; //$NON-NLS-1$
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(<xsl:value-of select="$class_name" />.class);
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private static Log4JHelper	objLogger		= null;

	protected <xsl:value-of select="$WorkerClassName" />Options	objOptions			= null;
	private <xsl:value-of select="$WorkerClassName" /> objE = null;
	
	
	public <xsl:value-of select="$class_name" />() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objE = new <xsl:value-of select="$WorkerClassName" />();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExecute() throws Exception {
		
		
		objE.Execute();
		
//		assertEquals ("auth_file", objOptions.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objOptions.user.Value(),"test"); //$NON-NLS-1$


	}
}  // class <xsl:value-of select="$class_name" />

</xsl:template>

	<xsl:template match="text()">
		<!-- 	<xsl:value-of select="normalize-space(.)"/> -->
	</xsl:template>


</xsl:stylesheet>