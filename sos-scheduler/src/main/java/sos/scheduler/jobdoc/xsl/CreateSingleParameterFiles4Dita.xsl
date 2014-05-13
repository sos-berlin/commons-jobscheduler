<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
	xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:fn="http://www.w3.org/2005/xpath-functions"
	exclude-result-prefixes="jobdoc xhtml fn">

	<xsl:param name="sos.timestamp" select="xxxxx" />

	<xsl:output method="xml" encoding="utf-8" indent="yes"
		standalone="yes" doctype-public="-//OASIS//DTD DITA Topic//EN"
		doctype-system="topic.dtd" />

	<xsl:variable name="JobName">
		<xsl:value-of select="//jobdoc:job/@name" />
	</xsl:variable>

	<xsl:template match="/">
		<xsl:apply-templates select="//jobdoc:configuration/jobdoc:params" />
		<xsl:apply-templates select="//jobdoc:description/jobdoc:resources" />
	</xsl:template>

	<xsl:template match="jobdoc:description/jobdoc:resources">
		<xsl:for-each select="jobdoc:file">
			<xsl:if test="./@file != '' and ./@file != '*'">
				<xsl:variable name="DocumentName">
					<xsl:value-of
						select="concat('file:///c:/temp/dita/resource/resource_', fn:replace(./@file,'\*','__') , '-', $JobName, '-', fn:generate-id(.), '.dita')"></xsl:value-of>
				</xsl:variable>
				<xsl:message>
					<xsl:value-of select="concat('Resource FileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>
				<xsl:result-document href="{$DocumentName}"
					doctype-public="-//OASIS//DTD DITA Topic//EN" doctype-system="topic.dtd"
					encoding="utf-8" indent="yes" method="xml" standalone="yes">

					<xsl:element name="topic">
						<xsl:attribute name="xml:lang" select="'en'" />
						<xsl:attribute name="id"
							select="concat('resource_', ./@file)" />

						<xsl:element name="title">
							<xsl:value-of select="concat('Resource: ', ./@file)" />
						</xsl:element>
						<!-- 
						<titlealts>
							<navtitle>
								<xsl:value-of select="concat('Resource: ', ./@file)" />
							</navtitle>
							<searchtitle>
								<xsl:value-of select="concat('Resource: ', ./@file)" />
							</searchtitle>
						</titlealts>
                         -->
						<xsl:element name="shortdesc">
							<xsl:attribute name="xml:lang" select="'en'" />
							<xsl:attribute name="id" select="fn:generate-id(.)" />
							<xsl:element name="ph">
								<xsl:attribute name="xml:lang" select="'en'" />
							</xsl:element>
						</xsl:element>
						<xsl:call-template name="CreateProlog" />
						<xsl:element name="body">
							<xsl:element name="section">
								<xsl:attribute name="translate" select="'no'" />
								<indexterm>
									<xsl:value-of select="./@file" />
									<indexterm>Resource</indexterm>
								</indexterm>
								<indexterm>
									Resource
									<indexterm>
										<xsl:value-of select="./@file" />
									</indexterm>
								</indexterm>
							</xsl:element>
							<xsl:apply-templates select="." />
						</xsl:element>
					</xsl:element>
				</xsl:result-document>

				<xsl:message>
					<xsl:value-of
						select="concat('fettich Resource FileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="jobdoc:configuration/jobdoc:params">
		<xsl:for-each select="./jobdoc:param">
			<xsl:variable name="CurrentParam" select="./@name" />

			<xsl:if test="./@name != '' and ./@name != '*'">
				<xsl:variable name="DocumentName">
					<xsl:value-of
						select="concat('file:///c:/temp/dita/params/param_', ./@name, '-', $JobName, '-', fn:generate-id(.), '.dita')"></xsl:value-of>
				</xsl:variable>
				<xsl:message>
					<xsl:value-of select="concat('DocFileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>

				<xsl:result-document href="{$DocumentName}"
					doctype-public="-//OASIS//DTD DITA Topic//EN" doctype-system="topic.dtd"
					encoding="utf-8" indent="yes" method="xml" standalone="yes">

					<xsl:element name="topic">
						<xsl:attribute name="xml:lang" select="'en'" />
						<xsl:attribute name="id"
							select="concat('param_', $CurrentParam)" />

						<xsl:element name="title">
							<xsl:value-of select="concat('Parameter: ', $CurrentParam)" />
						</xsl:element>
						<!-- 
						<titlealts>
							<navtitle>
								<xsl:value-of select="concat('Parameter: ', $CurrentParam)" />
							</navtitle>
							<searchtitle>
								<xsl:value-of select="concat('Parameter: ', $CurrentParam)" />
							</searchtitle>
						</titlealts>
 -->
						<xsl:element name="shortdesc">
							<xsl:attribute name="xml:lang" select="'en'" />
							<xsl:attribute name="id" select="fn:generate-id(.)" />
							<xsl:element name="ph">
								<xsl:attribute name="xml:lang" select="'en'" />
							</xsl:element>
						</xsl:element>
						<xsl:call-template name="CreateProlog" />
						<xsl:element name="body">
							<xsl:element name="section">
								<xsl:attribute name="translate" select="'no'" />
								<indexterm>
									<xsl:value-of select="./@name" />
									<indexterm>Parameter</indexterm>
								</indexterm>
								<indexterm>
									Parameter
									<indexterm>
										<xsl:value-of select="./@name" />
									</indexterm>
								</indexterm>
							</xsl:element>
							<xsl:apply-templates select="." />
						</xsl:element>
					</xsl:element>
				</xsl:result-document>

				<xsl:message>
					<xsl:value-of select="concat('fettich DocFileName is ', $DocumentName)"></xsl:value-of>
				</xsl:message>
			</xsl:if>
		</xsl:for-each>

		<xsl:variable name="IncludesDocumentName">
			<xsl:value-of
				select="concat('file:///c:/temp/dita/maps/params_', 'map-', $JobName, '-', fn:generate-id(.), '.ditamap')"></xsl:value-of>
		</xsl:variable>

		<xsl:result-document href="{$IncludesDocumentName}"
			doctype-public="-//OASIS//DTD DITA Map//EN" doctype-system="map.dtd"
			encoding="utf-8" indent="yes" method="xml">
			<xsl:message>
				<xsl:value-of
					select="concat('$IncludesDocumentName is ', $IncludesDocumentName)"></xsl:value-of>
			</xsl:message>
			<xsl:element name="map">
				<title>
					<xsl:value-of
						select="concat('Parameter for ',//jobdoc:job/@name, ' ', //jobdoc:job/@title)" />
				</title>
				<xsl:call-template name="CreateProlog" />
				<xsl:for-each select="./jobdoc:param">
					<xsl:sort order="ascending" select="./@name" />
					<xsl:if test="./@name != '' and ./@name != '*'">
						<xsl:element name="topicref">
							<xsl:attribute name="href"
								select="concat('./params/param_', ./@name, '.dita')" />
							<xsl:attribute name="toc" select="'yes'" />
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
			</xsl:element>
		</xsl:result-document>

	</xsl:template>

	<xsl:template match="xhtml:code" mode="copy">
		<xsl:variable name="pname" select="." />
		<indexterm>
			<xsl:value-of select="$pname" />
			<indexterm>ref</indexterm>
		</indexterm>
		<xsl:element name="xref">
			<xsl:attribute name="href"
				select="concat('./param_', $pname, '.dita#param_', $pname)" />
		</xsl:element>
		<xsl:element name="parmname">
			<xsl:value-of select="normalize-space(.)" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="jobdoc:note">
		<xsl:if test="@language = 'en' ">
			<xsl:element name="section">
				<xsl:attribute name="xml:lang" select="./@language" />
				<xsl:attribute name="id" select="fn:generate-id(.)" />
			</xsl:element>
			<xsl:apply-templates select="./*" />
		</xsl:if>
	</xsl:template>

	<xsl:template match="xhtml:div">
		<xsl:element name="p">
			<xsl:apply-templates select="." mode="copy" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="xhtml:em" mode="copy">
		<indexterm>
			<xsl:value-of select="." />
			<indexterm>Attribute</indexterm>
		</indexterm>
		<xsl:element name="option">
			<xsl:value-of select="normalize-space(.)" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="xhtml:cmdname" mode="copy">
		<indexterm>
			<xsl:value-of select="." />
			<indexterm>command</indexterm>
		</indexterm>
		<xsl:element name="cmdname">
			<xsl:value-of select="normalize-space(.)" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="xhtml:tt" mode="copy">
		<xsl:element name="tt">
			<xsl:value-of select="normalize-space(.)" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="xhtml:br" mode="copy">
		<xsl:element name="br" />
		<xsl:comment>
			tag br ignored
		</xsl:comment>
	</xsl:template>

	<xsl:template match="text()" mode="copy">
		<xsl:value-of select="." />
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
					<xsl:attribute name="date" select="$sos.timestamp"  />
				</xsl:element>
				<xsl:element name="revised">
					<xsl:attribute name="modified" select="$sos.timestamp"  />
				</xsl:element>
			</critdates>
			<permissions view="internal" />
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>