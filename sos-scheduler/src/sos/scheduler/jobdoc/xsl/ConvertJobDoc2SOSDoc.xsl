<?xml version="1.0" encoding="utf-8"?>
<!-- $Id: CreateMediaWikiFromSOSDoc.xsl 15153 2011-09-14 11:59:34Z kb $ -->
<!-- 

 -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xi="http://www.w3.org/2001/XInclude" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1" xmlns:sosfn="http://www.sos-berlin.com/sosfn" xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xalan="http://xml.apache.org/xalan" xmlns:xd="http://www.pnp-software.com/XSLTdoc"
    exclude-result-prefixes="jobdoc xhtml fn xd xi xs sosfn">

    <xd:doc type="stylesheet">
        <xd:short>This template is used to convert a xml-file using the SOS JobDoc format to
            the SOSDocu format.
        </xd:short>
        <xd:detail>
        </xd:detail>
        <xd:author>kb</xd:author>
        <xd:copyright>SOS GmbH, Berlin, 2012</xd:copyright>
        <xd:cvsId>$Id: CreateMediaWikiFromSOSDoc.xsl 15153 2011-09-14 11:59:34Z kb $ </xd:cvsId>
    </xd:doc>

    <xd:doc type="string">
        A Stylesheet parameter of type string.
        <xd:param name="sos.timestamp" type="string">sos.timestamp</xd:param>
    </xd:doc>
    <xsl:param name="sos.timestamp" select="'xxxxx'" />

    <xd:doc type="string">
        A Stylesheet parameter of type string.
        <xd:param name="sos.doculanguage" type="string">sos.doculanguage</xd:param>
    </xd:doc>
    <xsl:param name="sos.doculanguage" select="'en'" />

    <xd:doc type="string">
        The mode of formatting, not used here
        <xd:param name="formattingMode" type="string">formattingMode</xd:param>
    </xd:doc>
    <xsl:variable name="formattingMode" select="'includeText'" />

    <xsl:variable name="this.doc" select="/" />
    <xsl:variable name="sos.i18nFileName">
        <xsl:value-of select="concat('./mediaWikiText_', $sos.doculanguage, '.xml')"></xsl:value-of>
    </xsl:variable>
    <xsl:variable name="sos.i18n" select="document($sos.i18nFileName)" />
    <xsl:output method="xml" encoding="utf-8" indent="yes" standalone="yes" />

    <xsl:variable name="lf" select="'&#xa;'" />
    <xsl:variable name="newline" select="'&#xa;&#x20;&#xa;'" />
    <xsl:variable name="EmptyLine" select="'&#x20;&#xa;&#x20;&#xa;'" />

    <xsl:variable name="heading" select="'=='" />
    <xsl:variable name="heading2" select="'==='" />
    <xsl:variable name="heading3" select="'===='" />
    <xsl:variable name="heading4" select="'====='" />
    <xsl:variable name="hr" select="'----'" />
    <xsl:variable name="dq" select="'&#34;'" />
    <xsl:variable name="italic" select="''''''" />
    <xsl:variable name="bold" select="''''''''" />
	<!-- <xsl:variable name="italic" select="' '" /> <xsl:variable name="bold" 
		select="' '" /> -->

    <xsl:variable name="JobName" as="xs:string">
        <xsl:value-of select="//jobdoc:job/@name" />
    </xsl:variable>

    <xsl:variable name="JobTitle" as="xs:string">
        <xsl:value-of select="//jobdoc:job/@title" />
    </xsl:variable>

    <xsl:variable name="JobCategory" as="xs:string">
        <xsl:value-of select="//jobdoc:job/@category" />
    </xsl:variable>

    <xsl:variable name="JobIsDeprecated" as="xs:string">
        <xsl:value-of select="//jobdoc:job/@deprecated" />
    </xsl:variable>

    <xsl:variable name="JobIsActiveSince" as="xs:string">
        <xsl:value-of select="//jobdoc:job/@since" />
    </xsl:variable>

    <xsl:function name="sosfn:wikilink" as="xs:string">
        <xsl:param name="arg" />
        <xsl:value-of select="concat('[[#', $arg, '|', $arg, ']]')" />
    </xsl:function>

    <xsl:function name="sosfn:i18n" as="xs:string">
        <xsl:param name="key" as="xs:string" />
        <xsl:value-of select="$sos.i18n//items/item[@id=$key] " />
    </xsl:function>

    <xsl:function name="sosfn:bold">
        <xsl:param name="pText" />
        <b>
            <xsl:sequence select="$pText" />
        </b>
    </xsl:function>

    <xsl:function name="sosfn:italic" as="xs:string">
        <xsl:param name="pText" />
        <xsl:element name="i">
            <xsl:sequence select="$pText" />
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:heading2" as="document-node()">
        <xsl:param name="pText" as="xs:string?" />
        <xsl:variable name="title">
            <xsl:element name="title">
                <xsl:attribute name="language" select="$sos.doculanguage" />
                <xsl:sequence select="$pText" />
            </xsl:element>
        </xsl:variable>
        <xsl:sequence select="$title" />
    </xsl:function>

    <xsl:function name="sosfn:heading3" as="element(title)">
        <xsl:param name="pText" as="xs:string?" />
        <xsl:element name="title">
            <xsl:attribute name="language" select="$sos.doculanguage" />
            <xsl:sequence select="$pText" />
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:anchor">
        <xsl:param name="anchortext" />
        <xsl:variable name="anchor">
            <xsl:element name="a">
                <xsl:attribute name="href" select="$anchortext" />
                <xsl:sequence select="$anchortext" />
            </xsl:element>
        </xsl:variable>
        <xsl:sequence select="$anchor" />
    </xsl:function>

    <xsl:function name="sosfn:category">
        <xsl:param name="pText" />
        <xsl:value-of select="concat('[[Category:', $pText, ']]')" />
    </xsl:function>

    <xsl:function name="sosfn:table" as="xs:string">
        <xsl:param name="border" />
        <xsl:element name="table">
            <xsl:sequence select="concat($lf,  '{| border=&#34;', $border, '&#34;' )" />
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:tabletitle">
        <xsl:param name="pTitle" />
        <xsl:element name="br">
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:tableend">
        <xsl:element name="br">
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:tr">
        <xsl:element name="tr">
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:td">
        <xsl:param name="pText" />
        <xsl:element name="td">
            <xsl:value-of select="$pText" />
        </xsl:element>
    </xsl:function>

    <xsl:function name="sosfn:th">
        <xsl:param name="pText" />
        <xsl:if test="$pText != '' ">
            <xsl:element name="thead">
                <xsl:element name="tr">
                    <xsl:for-each select="fn:tokenize($pText,'\|\|')">
                        <xsl:variable name="tooken">
                            <xsl:element name="td">
                                <xsl:sequence select="." />
                            </xsl:element>
                        </xsl:variable>
                        <xsl:sequence select="$tooken" />
                    </xsl:for-each>
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:function>

    <xsl:function name="sosfn:a">
        <xsl:param name="pHref" />
        <xsl:param name="pText" />
        <xsl:variable name="a">
            <xsl:element name="a">
                <xsl:attribute name="name" select="$pHref" />
                <xsl:value-of select="$pText" />
            </xsl:element>
        </xsl:variable>
        <xsl:sequence select="$a" />
    </xsl:function>

<!-- 
    <indexterm index="configuration file" display="false">/config/live</indexterm>
 -->

    <xd:doc>
        <xd:short>Creates an index-entry of type Indexterm.</xd:short>
        <xd:detail></xd:detail>
        <xd:param name="pText">
            The text which has to appear as index entry.
        </xd:param>
        <xd:param name="pIndex">
            The type of the index.
        </xd:param>
    </xd:doc>
    <xsl:function name="sosfn:ix" as="element(indexterm)">
        <xsl:param name="pText" as="xs:string" />
        <xsl:param name="pIndex" as="xs:string" />
        <xsl:element name="indexterm">
            <xsl:attribute name="index" select="$pIndex" />
            <xsl:attribute name="display" select="'false'" />
            <xsl:sequence select="$pText" />
        </xsl:element>
    </xsl:function>

    <xsl:template name="ix">
        <xsl:param name="pText" as="xs:string" />
        <xsl:param name="pIndex" as="xs:string" />
        <xsl:element name="indexterm">
            <xsl:attribute name="index" select="$pIndex" />
            <xsl:attribute name="display" select="'false'" />
            <xsl:value-of select="$pText" />
        </xsl:element>
    </xsl:template>

    <xsl:function name="sosfn:li" as="xs:string">
        <xsl:value-of select="'* '" />
    </xsl:function>

    <xd:doc>
        <xd:short>Calls sub-templates for each part of the transformation.</xd:short>
    </xd:doc>
    <xsl:template match="/">
        <xsl:element name="topic">
            <xsl:attribute name="id" select="concat($JobName, '.docu')" />
            <xsl:attribute name="version" select="1.0" />
<!-- 
                <xsl:element name="title">
                        <xsl:attribute name="language" select="$sos.doculanguage" />
                    <xsl:value-of select="concat('Job ', $JobName, ' - ', $JobTitle )" />
                </xsl:element>

                <xsl:element name="subtitle">
                        <xsl:attribute name="language" select="$sos.doculanguage" />
                </xsl:element>

 -->
            <xsl:sequence select="sosfn:ix('ReadyMadeJobs', 'Category')" />
            <xsl:sequence select="sosfn:ix( $JobName, 'Category')" />
            <xsl:if test="$JobCategory != ''">
                <xsl:for-each select="fn:tokenize($JobCategory,';')">
                    <xsl:variable name="tooken">
                        <xsl:sequence select="." />
                    </xsl:variable>
                    <xsl:sequence select="sosfn:ix( $tooken, 'Category')" />
                </xsl:for-each>
            </xsl:if>

            <xsl:value-of select="$newline" />

            <xsl:sequence select="sosfn:heading2(concat(sosfn:i18n('descriptionof'), $JobName, ' - ', $JobTitle))" />

            <xsl:element name="description">
                <xsl:apply-templates select="//jobdoc:description/jobdoc:documentation" />

                <xsl:element name="topic">
                    <xsl:attribute name="id" select="concat($JobName, '.configuration')" />
                    <xsl:sequence select="sosfn:heading2('Configuration')" />
                    <xsl:element name="description">
                        <xsl:apply-templates select="//jobdoc:configuration/jobdoc:note[@language=$sos.doculanguage]" />
                    </xsl:element>
                </xsl:element>

                <xsl:apply-templates select="//jobdoc:configuration/jobdoc:params[@id = 'job_parameter']" />

                <xsl:apply-templates select="//jobdoc:configuration/jobdoc:params[@id = 'return_parameter']" />
<!-- 
                <xsl:value-of select="concat($newline, $sos.i18n//items/item[@id='lastrevision'], '{{REVISIONMONTH}}/{{REVISIONDAY}}/{{REVISIONYEAR}}', '.')"></xsl:value-of>
                 -->
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="jobdoc:configuration/jobdoc:params[@id = 'return_parameter']">
        <xsl:element name="topic">
            <xsl:attribute name="id" select="concat($JobName, '.return_parameter')" />
            <xsl:element name="description">
                <xsl:sequence select="sosfn:heading2(concat(sosfn:i18n('return.parameter'), ' ',  $JobName)) " />
                <xsl:value-of select="concat(sosfn:i18n('return.parameter.text'), ' ', $JobName, $newline)" />

                <xsl:value-of select="$EmptyLine" />
                <xsl:element name="table">
                    <xsl:value-of select="sosfn:tabletitle('List of parameter')" />
                    <xsl:sequence select="sosfn:th(sosfn:i18n('parameter.table.title')) " />

                    <xsl:apply-templates select="./*" mode="createtable" />

                    <xsl:value-of select="sosfn:tableend()" />
                </xsl:element>

                <xsl:apply-templates select="./*" />
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="jobdoc:configuration/jobdoc:params[@id = 'job_parameter']">
        <xsl:element name="topic">
            <xsl:attribute name="id" select="concat($JobName, '.job_parameter')" />
            <xsl:sequence select="sosfn:heading2(concat(sosfn:i18n('usedby'), $JobName))" />
            <xsl:element name="description">
                <xsl:value-of select="$EmptyLine" />
                <xsl:element name="table">
                    <xsl:sequence select="sosfn:tabletitle('List of parameter')" />

                    <xsl:sequence select="sosfn:th( sosfn:i18n('parameter.table.title'))" />
                    <xsl:element name="tbody">
                        <xsl:apply-templates select="./*" mode="createtable" />
                    </xsl:element>
                </xsl:element>

                <xsl:value-of select="sosfn:tableend()" />


<xsl:copy-of select="." />

<!-- 
                <xsl:apply-templates select="./*" />
                 -->
            </xsl:element>
        </xsl:element>
    </xsl:template>

    <xsl:template match="jobdoc:releases"></xsl:template>
    <xsl:template match="jobdoc:resources"></xsl:template>

    <xsl:template match="jobdoc:documentation">
        <xsl:if test="@language = $sos.doculanguage ">
            <xsl:if test="$JobIsDeprecated != ''">
                <xsl:value-of select="concat($sos.i18n//items/item[@id='deprecatedjob'], $newline)" />
                <xsl:value-of select="concat($JobIsDeprecated, $sos.i18n//items/item[@id='useinstead'], $newline)" />
                <xsl:value-of select="$newline" />
            </xsl:if>
            <xsl:apply-templates select="./*" />
            <xsl:value-of select="$newline" />
            <xsl:apply-templates select="//jobdoc:description/jobdoc:note[@language=$sos.doculanguage]" />
            <xsl:value-of select="$newline" />
            <xsl:call-template name="create_job" />
            <xsl:value-of select="$newline" />
        </xsl:if>
    </xsl:template>

    <xsl:template name="create_job">
        <xsl:element name="topic">
            <xsl:attribute name="id" select="concat($JobName, '.create_job')" />
            <xsl:sequence select="sosfn:heading2(sosfn:i18n('job.example'))" />
            <xsl:element name="description">

                <xsl:element name="code">
                    <xsl:value-of select="concat('  &lt;job order=''no'' >', $lf)" />
                    <xsl:value-of select="concat('     &lt;params>', $lf)" />

                    <xsl:for-each select="//jobdoc:params[@id = 'job_parameter']">
                        <xsl:apply-templates select="./*" mode="create_job" />
                    </xsl:for-each>

                    <xsl:value-of select="concat('     &lt;/params>', $lf)" />
                    <xsl:value-of
                        select="concat('     &lt;script language=&#34;', //jobdoc:script/@language, '&#34; java_class=&#34;', //jobdoc:script/@java_class, '&#34; />', $lf)" />
                    <xsl:value-of select="concat('  &lt;/job>', $newline)" />
                </xsl:element>
            </xsl:element>
            <xsl:value-of select="$newline" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="jobdoc:param" mode="create_job">
        <xsl:value-of select="concat( '       &lt;param name=&#34;', sosfn:wikilink(./@name), '&#34; value=&#34;',
                ./@default_value, '&#34; />', $lf)" />
    </xsl:template>

    <xsl:template match="jobdoc:params" mode="create_job">
        <xsl:apply-templates select="./jobdoc:param" mode="create_job" />
    </xsl:template>

    <xsl:template match="jobdoc:param" mode="createtable">
        <xsl:element name="tr">
        <!-- 
            <xsl:sequence select="sosfn:tr()" /> 
         -->
            <xsl:sequence select="sosfn:td(sosfn:wikilink(./@name))" />
            <xsl:sequence select="sosfn:td(normalize-space(./jobdoc:note[@language=$sos.doculanguage][1]/jobdoc:title)) " />
            <xsl:sequence select="sosfn:td(./@required)" />
            <xsl:sequence select="sosfn:td(./@default_value)" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="jobdoc:params" mode="createtable">
        <xsl:apply-templates select="./jobdoc:param" mode="createtable" />
    </xsl:template>

    <xsl:template match="jobdoc:params">
        <xsl:copy-of select="." />
    <!-- 
        <xsl:element name="params">
            <xsl:apply-templates select="." />
        </xsl:element>
 -->
    </xsl:template>

    <xsl:template match="jobdoc:param">
        <xsl:copy-of select="./jobdoc:note[@language = $sos.doculanguage]" />
    </xsl:template>

    <xsl:template match="jobdoc:param_xx">
        <xsl:variable name="CurrentParam" select="./@name" />
        <xsl:message>
            <xsl:value-of select="concat('processing param ', $CurrentParam)" />
        </xsl:message>
        <xsl:if test="./@name != '' and ./@name != '*'">
            <xsl:sequence
                select="concat($lf, sosfn:heading3(concat(sosfn:i18n('parameter'), ' ', sosfn:anchor($CurrentParam) , ': ',  normalize-space(./jobdoc:note[@language=$sos.doculanguage][1]/jobdoc:title))))" />

            <xsl:apply-templates select="./*" />
            <xsl:value-of select="$newline" />

            <xsl:value-of select="concat($sos.i18n//items/item[@id='datatype'], ': ', @DataType, $newline)" />
            <xsl:if test="./@default_value != ''">
                <xsl:value-of select="concat(sosfn:i18n('defaultvalue'), ' ', sosfn:bold(./@default_value), '.', $newline)" />
            </xsl:if>
            <xsl:if test="@usewith">
                <xsl:value-of select="concat(sosfn:i18n('usewith'), $newline)" />
                <xsl:for-each select="fn:tokenize(./@usewith,',')">
                    <xsl:variable name="tooken" select="." />
                    <xsl:value-of
                        select="concat(sosfn:wikilink(normalize-space(.)), ' - ', normalize-space($this.doc//jobdoc:param[@name=$tooken]/jobdoc:note[@language=$sos.doculanguage][1]/jobdoc:title))" />
                </xsl:for-each>
                <xsl:value-of select="$newline" />
            </xsl:if>
        </xsl:if>

        <xsl:if test="./@required = 'true'">
            <xsl:value-of select="concat(sosfn:i18n('mandatory'), $newline )" />
        </xsl:if>
    </xsl:template>

    <xsl:template match="code | xhtml:code|jobdoc:code" mode="copy">
        <xsl:element name="code">
            <xsl:apply-templates mode="copy" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="paramref | xhtml:paramref|jobdoc:paramref" mode="copy">
        <xsl:element name="paramref">
            <xsl:apply-templates mode="copy" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="pre | xhtml:pre | jobdoc:pre" mode="copy">
        <xsl:element name="pre">
            <xsl:apply-templates mode="copy" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="pre | xhtml:pre | jobdoc:pre">
        <xsl:element name="pre">
            <xsl:apply-templates mode="copy" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="title|xhtml:title|xi:title|jobdoc:title" mode="copy">
        <xsl:variable name="pname" select="." />
        <xsl:element name="p">
            <xsl:attribute name="language" select="$sos.doculanguage" />
            <xsl:value-of select="concat(sosfn:italic(.), ' ')" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="title|xhtml:title|xi:title|jobdoc:title">
        <xsl:element name="p">
            <xsl:attribute name="language" select="$sos.doculanguage" />

        </xsl:element>
    </xsl:template>

<!-- 
    <xsl:template match="xhtml:paramref|jobdoc:paramref" mode="copy">
        <xsl:variable name="pname" select="." />
        <xsl:choose>
            <xsl:when test="$formattingMode='includeText'">
                <xsl:value-of select="concat(' ', sosfn:wikilink(normalize-space(.)), ' ')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat( ' [[Param ', normalize-space(.), '|', normalize-space(.), ']] ')" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
 -->

    <xsl:template match="xhtml:paramref|jobdoc:paramref">
        <xsl:variable name="pname" select="." />
        <xsl:choose>
            <xsl:when test="$formattingMode='includeText'">
                <xsl:value-of select="concat(' ', sosfn:wikilink(normalize-space(.)), ' ')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat( ' [[Param ', normalize-space(.), '|', normalize-space(.), ']] ')" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="a|xhtml:a|jobdoc:a" mode="copy">
        <xsl:value-of select="sosfn:a(./@href, normalize-space(.)) " />
    </xsl:template>

    <xsl:template match="xhtml:note|jobdoc:note">
        <xsl:if test="@language = $sos.doculanguage ">
            <xsl:apply-templates select="./*" />
        </xsl:if>
    </xsl:template>

    <xsl:template match="jobdoc:configuration">
        <xsl:apply-templates select="./*" />
    </xsl:template>

    <xsl:template match="xhtml:div">
        <xsl:apply-templates select="." mode="copy" />
    </xsl:template>

    <xsl:template match="xhtml:p | jobdoc:p | p">
        <xsl:element name="p">
            <xsl:attribute name="language" select="$sos.doculanguage" />
            
            <xsl:apply-templates select="./*" mode="copy" />
        </xsl:element>
    </xsl:template>


    <xsl:template match="xhtml:p" mode="copy">
            <xsl:copy-of select="." />
    </xsl:template>

    <xsl:template match="xhtml:em|jobdoc:em" mode="copy">
        <xsl:value-of select="concat(' ', sosfn:bold(normalize-space(.)), ' ')" />
    </xsl:template>

    <xsl:template match="xhtml:strong|jobdoc:strong" mode="copy">
        <xsl:value-of select="concat(' ', sosfn:bold(normalize-space(.)), ' ')" />
    </xsl:template>

    <xsl:template match="xhtml:cmdname|jobdoc:cmdname" mode="copy">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>

    <xsl:template match="xhtml:tt|jobdoc:tt" mode="copy">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>

    <xsl:template match="xhtml:example|jobdoc:example" mode="copy">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>

    <xsl:template match="xhtml:br|jobdoc:br" mode="copy">
        <xsl:value-of select="$lf" />
        <xsl:if test="./@clear != 'none'">
        </xsl:if>
		<!-- <xsl:element name="br" /> -->
    </xsl:template>

    <xsl:template match="xhtml:ul|jobdoc:ul" mode="copy">
        <xsl:element name="ul">
            <xsl:apply-templates select="./*" mode="copy" />
        </xsl:element>
        <xsl:value-of select="$newline" />
    </xsl:template>

    <xsl:template match="text()" mode="copy">
		<!-- <xsl:message select="concat('text() ', name(.), ' ', namespace-uri(.), 
			' = ', normalize-space(.))" /> -->
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>

    <xsl:template match="text()">
        <!-- <xsl:message select="concat('text() ', name(.), ' ', namespace-uri(.), ' = ', normalize-space(.))" /> -->
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>

    <xsl:template match="*" mode="copy">
		<!-- <xsl:message select="concat('* ', name(.), ' ', namespace-uri(.), 
			' = ', normalize-space(.))" /> -->
        <xsl:element name="{name()}">
            <xsl:attribute name="language" select="$sos.doculanguage" />
            <xsl:copy-of select="@*" />
            <xsl:apply-templates mode="copy" />
        </xsl:element>
    </xsl:template>



    <xsl:template match="table|xhtml:table|jobdoc:table">
        <xsl:value-of select="$EmptyLine" />
        <xsl:element name="table">
            <xsl:apply-templates select="./*" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="tr|xhtml:tr|jobdoc:tr">
        <xsl:value-of select="sosfn:tr()" />
        <xsl:apply-templates select="./*" />
    </xsl:template>

    <xsl:template match="li|xhtml:li|jobdoc:li" mode="copy">
        <xsl:element name="li">
            <xsl:apply-templates mode="copy" />
        </xsl:element>
    </xsl:template>

    <xsl:template match="th|xhtml:th|jobdoc:th">
        <xsl:sequence select="sosfn:th(.) " />
    </xsl:template>

    <xsl:template match="td|xhtml:td|jobdoc:td">
        <xsl:element name="td">
            <xsl:value-of select=". " />
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>