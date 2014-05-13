<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"

	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
	xmlns:java="http://xml.apache.org/xslt/java" xmlns:xhtml="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="jobdoc xhtml java">

	<xsl:output method="xml" encoding="iso-8859-1" indent="yes" />

	<xsl:variable name="JobName">
		<xsl:value-of select="//jobdoc:job/@name"></xsl:value-of>
	</xsl:variable>

	<xsl:template match="jobdoc:description/jobdoc:resources">
		<xsl:for-each select="jobdoc:file">
			<xsl:if test="./@file != '' and ./@file != '*'">
				<xsl:variable name="DocumentName">
					<xsl:value-of
						select="concat('file:///c:/temp/resources/resource_', fn:replace(./@file,'\*','__') , '-', $JobName, '-', fn:generate-id(.), '.xml')"></xsl:value-of>
				</xsl:variable>
				<xsl:message>
					<xsl:value-of select="concat('Resource FileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>
				<xsl:result-document href="{$DocumentName}"
					method="xml">
					<xsl:copy-of select="." />
				</xsl:result-document>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="jobdoc:description/jobdoc:configuration/jobdoc:params">
		<xsl:for-each select="jobdoc:param">
			<xsl:if test="./@name != '' and ./@name != '*'">
				<xsl:variable name="DocumentName">
					<xsl:value-of
						select="concat('file:///c:/temp/params/param_', ./@name, '-', $JobName, '-', fn:generate-id(.), '.xml')"></xsl:value-of>
				</xsl:variable>
				<xsl:message>
					<xsl:value-of select="concat('DocFileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>
				<xsl:result-document href="{$DocumentName}"
					method="xml">
					<xsl:copy-of select="." />
				</xsl:result-document>
			</xsl:if>
		</xsl:for-each>
		<xsl:variable name="IncludesDocumentName">
			<xsl:value-of
				select="concat('file:///c:/temp/params/param_', 'includes', '-', $JobName, '.xml')"></xsl:value-of>
		</xsl:variable>

		<xsl:result-document href="{$IncludesDocumentName}"
			method="xml">
			<xsl:message>
				<xsl:value-of
					select="concat('$IncludesDocumentName is ', $IncludesDocumentName)"></xsl:value-of>
			</xsl:message>
			<xsl:for-each select="./jobdoc:param">
				<xsl:sort order="ascending" select="./@name"></xsl:sort>
				<xsl:if test="./@name != '' and ./@name
			!= '*'">
					<xsl:element name="xi:include" namespace="http://www.w3.org/2001/XInclude">
						<xsl:attribute name="href"
							select="concat('./params/param_', ./@name, '-', $JobName, '.xml')" />
						<xsl:attribute name="parse" select="'xml'" />
					</xsl:element>
				</xsl:if>
			</xsl:for-each>
		</xsl:result-document>

	</xsl:template>


	<xsl:template match="text()">
		<!--
    <xsl:value-of select="normalize-space(.)"/>
-->
	</xsl:template>

</xsl:stylesheet>