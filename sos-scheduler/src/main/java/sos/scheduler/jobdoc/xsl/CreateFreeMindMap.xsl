<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"

	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
	xmlns:java="http://xml.apache.org/xslt/java" xmlns:xhtml="http://www.w3.org/1999/xhtml"
	xmlns:xd="http://www.pnp-software.com/XSLTdoc"
	exclude-result-prefixes="jobdoc xhtml java">

  <xd:doc type="stylesheet">
    <xd:short>Creates a MindMap (Freemind) from a JobDoc file</xd:short>
    <xd:detail>
      This stylesheet creates a Mindmap from the jobdoc xml-file. 
    </xd:detail>
    <xd:author>kb</xd:author>
    <xd:cvsId>$Id$</xd:cvsId>
    <xd:copyright>205-2012 SOS GmbH, Berlin</xd:copyright>
  </xd:doc>

	<xsl:output method="xml" encoding="iso-8859-1" indent="yes" />
	<xsl:variable name="NodeStyle" select="'fork'" />

	<xsl:variable name="JobName">
		<xsl:value-of select="//jobdoc:job/@name"></xsl:value-of>
	</xsl:variable>

	<xsl:template match="/">
		<xsl:variable name="RootNodeName">
			<xsl:value-of select="concat(//jobdoc:job/@name, ' - ', //jobdoc:job/@title)" />
		</xsl:variable>

		<xsl:variable name="DocumentName">
			<xsl:value-of
				select="concat('file:///c:/temp/mindmaps/mindmap_', $JobName, '.mm')" />
		</xsl:variable>
		<xsl:message>
			<xsl:value-of select="concat('DocFileName is ', $DocumentName)" />
		</xsl:message>
		<xsl:result-document href="{$DocumentName}"
			method="xml">
			<xsl:element name="map">
				<xsl:attribute name="version">0.8.0</xsl:attribute>

				<node ID="{generate-id()}" TEXT="{$RootNodeName}" STYLE="{$NodeStyle}">
					<edge STYLE="sharp_bezier" WIDTH="2" />

					<font NAME="Arial" SIZE="16" BOLD="true" />

					<xsl:apply-templates
						select="jobdoc:description/jobdoc:configuration/jobdoc:params" />
					<xsl:apply-templates
						select="jobdoc:description/jobdoc:configuration/jobdoc:payload/jobdoc:params" />
					<xsl:apply-templates select="jobdoc:description/jobdoc:resources" />
					<xsl:apply-templates select="jobdoc:description/jobdoc:documentation" />
				</node>
			</xsl:element>
		</xsl:result-document>
	</xsl:template>

	<xsl:template match="jobdoc:description/jobdoc:resources">
		<xsl:element name="node">
			<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

			<xsl:attribute name="TEXT">
            <xsl:value-of select="'Resources'" />
         </xsl:attribute>

			<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

			<xsl:for-each select="jobdoc:file">
				<xsl:if test="./@file != '' and ./@file != '*'">
					<xsl:element name="node">
						<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

						<xsl:attribute name="TEXT">
            <xsl:value-of select="concat(@file, ' - ', normalize-space(./jobdoc:note[@language = 'en']/xhtml:div/text()))" />
         </xsl:attribute>

						<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

								<xsl:attribute name="BACKGROUND_COLOR">#ffff99</xsl:attribute>

						<font NAME="Arial" SIZE="12" BOLD="true" />
					</xsl:element>
				</xsl:if>
			</xsl:for-each>

					<xsl:apply-templates select="./jobdoc:database" />

		</xsl:element>
	</xsl:template>


	<xsl:template match="jobdoc:database">
		<xsl:element name="node">
			<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

			<xsl:attribute name="TEXT">
            <xsl:value-of select="'Database'" />
         </xsl:attribute>

			<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

			<xsl:for-each select="jobdoc:resource">
					<xsl:element name="node">
						<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

						<xsl:attribute name="TEXT">
            <xsl:value-of select="concat(@name, ' - ', normalize-space(./jobdoc:note[@language = 'en']/xhtml:div/text()))" />
         </xsl:attribute>

						<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

								<xsl:attribute name="BACKGROUND_COLOR">#ffff99</xsl:attribute>

						<font NAME="Arial" SIZE="12" BOLD="true" />
					</xsl:element>
			</xsl:for-each>

		</xsl:element>
	</xsl:template>

	<xsl:template match="jobdoc:description/jobdoc:configuration/jobdoc:params">
		<xsl:element name="node">
			<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

			<xsl:attribute name="TEXT">
            <xsl:value-of select="'Parameter'" />
         </xsl:attribute>

			<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

			<xsl:for-each select="jobdoc:param">
				<xsl:sort order="ascending" select="@name"></xsl:sort>
				<xsl:if test="./@name != '' and ./@name != '*'">
					<xsl:element name="node">
						<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

						<xsl:attribute name="TEXT">
            <xsl:value-of select="concat(@name, ' - ', normalize-space(./jobdoc:note[@language = 'en']/xhtml:div/text()))" />
         </xsl:attribute>

						<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

						<xsl:choose>
							<xsl:when test="./@required = 'true'">
								<xsl:attribute name="BACKGROUND_COLOR">#66cc00</xsl:attribute>
							</xsl:when>

							<xsl:otherwise>
								<xsl:attribute name="BACKGROUND_COLOR">#ffff99</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>

						<font NAME="Arial" SIZE="12" BOLD="true" />
					</xsl:element>
				</xsl:if>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<xsl:template match="jobdoc:description/jobdoc:configuration/jobdoc:payload/jobdoc:params">
		<xsl:element name="node">
			<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

			<xsl:attribute name="TEXT">
            <xsl:value-of select="'Payload'" />
         </xsl:attribute>

			<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

			<xsl:for-each select="jobdoc:param">
				<xsl:sort order="ascending" select="@name"></xsl:sort>
				<xsl:if test="./@name != '' and ./@name != '*'">
					<xsl:element name="node">
						<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

						<xsl:attribute name="TEXT">
            <xsl:value-of select="concat(@name, ' - ', normalize-space(./jobdoc:note[@language = 'en']/xhtml:div/text()))" />
         </xsl:attribute>

						<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

						<xsl:choose>
							<xsl:when test="./@required = 'true'">
								<xsl:attribute name="BACKGROUND_COLOR">#66cc00</xsl:attribute>
							</xsl:when>

							<xsl:otherwise>
								<xsl:attribute name="BACKGROUND_COLOR">#ffff99</xsl:attribute>
							</xsl:otherwise>
						</xsl:choose>

						<font NAME="Arial" SIZE="12" BOLD="true" />
					</xsl:element>
				</xsl:if>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>

	<xsl:template match="jobdoc:description/jobdoc:documentation">
		<xsl:element name="node">
			<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

			<xsl:attribute name="TEXT">Documentation</xsl:attribute>

			<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

			<xsl:for-each select="xhtml:div">
					<xsl:element name="node">
						<xsl:attribute name="ID">
            <xsl:value-of select="generate-id()" />
         </xsl:attribute>

						<xsl:attribute name="TEXT">
            <xsl:value-of select="concat(normalize-space(text()),' ')" />
         </xsl:attribute>

						<xsl:attribute name="STYLE">
            <xsl:value-of select="'bubble'" />
         </xsl:attribute>

								<xsl:attribute name="BACKGROUND_COLOR">#ffff99</xsl:attribute>

						<font NAME="Arial" SIZE="12" BOLD="true" />
					</xsl:element>
			</xsl:for-each>

		</xsl:element>
	</xsl:template>


	<xsl:template match="text()">
		<!--
    <xsl:value-of select="normalize-space(.)"/>
-->
	</xsl:template>

</xsl:stylesheet>