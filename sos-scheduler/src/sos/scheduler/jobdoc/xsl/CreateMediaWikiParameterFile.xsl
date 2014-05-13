<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
	xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	exclude-result-prefixes="jobdoc xhtml fn">

	<xsl:param name="sos.timestamp" select="'xxxxx'" />

	<xsl:output method="xml" encoding="utf-8" indent="yes"
		standalone="yes" />

	<xsl:variable name="JobName">
		<xsl:value-of select="//jobdoc:job/@name" />
	</xsl:variable>

	<xsl:template match="/">
		<xsl:apply-templates select="//jobdoc:configuration/jobdoc:params" />
	</xsl:template>

	<xsl:template match="jobdoc:configuration/jobdoc:params">
		<xsl:for-each select="./jobdoc:param">
			<xsl:variable name="CurrentParam" select="./@name" />

			<xsl:if test="./@name != '' and ./@name != '*'">
				<xsl:variable name="DocumentName">
					<xsl:value-of
						select="concat('file:///c:/temp/mediaWiki/params/Param ', ./@name, '-', $JobName, '-', fn:generate-id(.), '.xml')"></xsl:value-of>
				</xsl:variable>
				<xsl:message>
					<xsl:value-of select="concat('DocFileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>

				<xsl:result-document href="{$DocumentName}"
					encoding="utf-8" indent="yes" method="xml" standalone="yes"
					output-version="1.0" >

					<xsl:element name="mediawiki">
						<!--
							<xsl:attribute name="xmlns"
							select="http://www.mediawiki.org/xml/export-0.3/" />
							<xsl:attribute name="xmlns:xsi"
							select="http://www.w3.org/2001/XMLSchema-instance" />
							<xsl:attribute name="xsi:schemaLocation"
							select="http://www.mediawiki.org/xml/export-0.3/
							http://www.mediawiki.org/xml/export-0.3.xsd" />
						-->
						<xsl:attribute name="version" select="0.3" />
						<xsl:attribute name="xml:lang" select="'en'" />

						<siteinfo>
							<sitename>jobscheduler</sitename>
							<base>http://sourceforge.net/apps/mediawiki/jobscheduler/index.php?title=Main_Page</base>
							<generator>MediaWiki 1.15.1</generator>
							<case>first-letter</case>
						</siteinfo>
						<xsl:element name="page">
							<xsl:element name="title">
								<xsl:value-of select="concat('JobParameter: ', $CurrentParam)" />
							</xsl:element>
							<xsl:element name="id">
							</xsl:element>
							<xsl:element name="revision">
								<xsl:element name="id">
								</xsl:element>
								<xsl:element name="timestamp">
									<xsl:value-of select="$sos.timestamp" />
								</xsl:element>
								<xsl:element name="contributor">
									<xsl:element name="username">
										<xsl:value-of select="'Soskb'" />
									</xsl:element>
									<xsl:element name="id">
										<xsl:value-of select="'8'" />
									</xsl:element>
								</xsl:element>
								<xsl:element name="text">
									<xsl:attribute name="xml:space" select="'preserve'" />
									<xsl:value-of select="'[[Category:Job Parameter]]'" />
								    <xsl:value-of select="concat('[[Category:', $JobName, ']]')" />
									<xsl:apply-templates select="." />
								</xsl:element>
							</xsl:element>
						</xsl:element>
					</xsl:element>
				</xsl:result-document>

				<xsl:message>
					<xsl:value-of select="concat('fettich DocFileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="xhtml:code" mode="copy">
		<xsl:variable name="pname" select="." />
		<xsl:element name="code">
			<xsl:value-of select="normalize-space(.)" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="jobdoc:note">
		<xsl:if test="@language = 'en' ">
			<xsl:apply-templates select="./*" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="xhtml:div">
		<xsl:element name="p">
			<xsl:apply-templates select="." mode="copy" />
		</xsl:element>
	</xsl:template>
	<!--
		<xsl:template match="xhtml:div" mode="copy"> <xsl:apply-templates
		select="." mode="copy" /> </xsl:template>
	-->
	<xsl:template match="xhtml:em" mode="copy">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="xhtml:cmdname" mode="copy">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="xhtml:tt" mode="copy">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="xhtml:example" mode="copy">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="xhtml:br" mode="copy">
		<xsl:element name="br" />
	</xsl:template>

	<xsl:template match="text()" mode="copy">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="*" mode="copy">
		<xsl:element name="{name()}">
			<xsl:copy-of select="@*" />
			<xsl:apply-templates mode="copy" />
		</xsl:element>
	</xsl:template>


	<xsl:template match="text()">
		<xsl:value-of select="normalize-space(.)" />
	</xsl:template>

	<xsl:template match="jobdoc:releases"></xsl:template>
	<xsl:template match="jobdoc:resources"></xsl:template>
	<xsl:template match="jobdoc:documentation"></xsl:template>

	<xsl:template name="CreateProlog">
		<xsl:element name="prolog">
			<xsl:element name="author">
				Klaus-Dieter Buettner
			</xsl:element>
			<publisher>SOS GmbH Berlin, Germany</publisher>
			<copyright>
				<copyryear year="2011" />
				<copyrholder>SOS GmbH Berlin, Germany (http://www.sos-berlin.com)</copyrholder>
			</copyright>
			<critdates>
				<xsl:element name="created">
					<xsl:attribute name="date" select="$sos.timestamp" />
				</xsl:element>
				<xsl:element name="revised">
					<xsl:attribute name="modified" select="$sos.timestamp" />
				</xsl:element>
			</critdates>
			<permissions view="internal" />
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>