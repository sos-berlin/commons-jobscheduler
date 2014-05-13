<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.0"
	xmlns:java="http://xml.apache.org/xslt/java" xmlns:xhtml="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="jobdoc xhtml java">

	<xsl:output method="text" encoding="iso-8859-1" indent="no" />

	<!--
		c:\temp\JobSchedulerSSHJob.xml TODO Attribute title bei "param/note"

		TODO Attribute data_type bei param 
		TODO child aliase bei param 
		TODO child alias bei aliase <xsl:eval>formatDate(new Date().getVarDate(), "MMMM dd',' yyyy");</xsl:eval>
	-->

		<xsl:message>
			script JSJobDoc2JSOptionClass started ...
		</xsl:message>

      <xsl:param name="default_lang" required="yes" as="xs:string"/>
<!-- 
<xsl:variable name="timestamp"
		select="fn:format-dateTime(fn:current-dateTime(),'[M01]/[D01]/[Y0001] [h]:[m01] [Pn] [Zn] [z]')" />
 -->	
<xsl:variable name="timestamp" select="'2010-05-15-14-28-00'" />
 	<xsl:variable name="timestamp3">
		<xsl:eval>
			java:new java.text.SimpleDateFormat("HH/dd/yyyy HH:mm:ss").format(new
			java.util.Date())
		</xsl:eval>
	</xsl:variable>
	<xsl:variable name="SourceType" select="'options'" />
	<xsl:variable name="package_name" select="'com.sos.net.ftp'" />
	<xsl:variable name="ExtendsClassName"
		select="'com.sos.DataSwitchHelper.DSWOptionsClass'" />
	<xsl:variable name="keywords" select="'Options'" />
	<xsl:variable name="Category" select="'Options|JobScheduler-API|API-Job'" />
	<xsl:variable name="nl" select="'&#xa;'" />

	<xsl:template match="/jobdoc:description">
		<xsl:message>
			jobdoc:description reached ...
		</xsl:message>
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="jobdoc:job">
		<xsl:value-of
			select="concat('; mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at ', $timestamp, $nl)" />

		<xsl:value-of select="concat('. SourceType=',$SourceType, $nl)" />
		<xsl:value-of select="concat('. PackageName=',$package_name, $nl)" />
		<xsl:value-of select="concat('. ClassName=', ./@name, 'Options', $nl)" />
		<xsl:value-of select="concat('. Title=',./@title, $nl)" />
		<xsl:value-of select="concat('. Description=',./@title, $nl)" />
		<xsl:value-of select="concat('. ExtendsClassName=', $ExtendsClassName, $nl)" />
		<xsl:value-of select="concat('. keywords=', $keywords, $nl)" />
		<xsl:value-of select="concat('. Category=', $Category, $nl)" />
	</xsl:template>

	<xsl:template match="jobdoc:configuration">
		<xsl:apply-templates />
	</xsl:template>

	<xsl:template match="jobdoc:params">
		<xsl:for-each select="./jobdoc:param">
			<xsl:sort select="./@name" order="ascending" />
			<xsl:choose>
				<xsl:when test="@name and not(@name='') and not(@name='*')">
					<xsl:variable name="title">
						<xsl:value-of
							select="normalize-space(substring(./jobdoc:note[@language=$default_lang],2, 80 ))" />
					</xsl:variable>
					<xsl:variable name="descr">
						<xsl:value-of
							select="normalize-space(substring(./jobdoc:note[@language=$default_lang],2, 80 ))" />
					</xsl:variable>
		
					<xsl:variable name="datatype">
						<xsl:choose>
							<xsl:when test="@data_type and not(@data_type='')">
								<xsl:value-of select="@data_type" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text>String</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
		
					<xsl:variable name="initialvalue">
						<xsl:choose>
							<xsl:when test="@default_value and not(@default_value='')">
								<xsl:value-of select="@default_value" />
							</xsl:when>
							<xsl:otherwise>
								<xsl:text> </xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
		
					<xsl:value-of
						select="concat(./@name,';',./@name,';',$title,';',$descr,';',./@required,';',$datatype,';',$initialvalue,';',./@default_value, $nl)" />
					<!--
						select="string-join((@name,@name,$title,$descr,@required,$datatype,$initialvalue,@default_value,$nl),';')"
						/>
					-->
				</xsl:when>
				<xsl:otherwise>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>


	<xsl:template match="text()">
		<!--
	<xsl:value-of select="normalize-space(.)"/>
-->
	</xsl:template>

</xsl:stylesheet>