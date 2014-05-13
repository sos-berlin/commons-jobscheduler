<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id: ResolveXIncludes.xsl 16416 2012-02-01 23:05:44Z oh $ -->
<xsl:stylesheet xmlns="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                version="2.0"
                exclude-result-prefixes="jobdoc xi xhtml">
<xsl:output method="xml"
            encoding="UTF-8" 
            version="1.0" 
            indent="yes" 
            omit-xml-declaration="no" />
<!--xsl:strip-space elements="*"/-->


<xsl:template match="/">
  <xsl:if test="not(jobdoc:description)">
    <xsl:message>nichts zu tun</xsl:message> 
    <xsl:copy-of select="." />
  </xsl:if>
  <xsl:if test="jobdoc:description">
    <xsl:apply-templates select="jobdoc:description" />
  </xsl:if> 
</xsl:template>


<xsl:template match="/jobdoc:description">
  <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="scheduler_job_documentation_v1.1.xsl"</xsl:processing-instruction> 
  <xsl:text>&#x0A;</xsl:text>
  <xsl:choose>
    <xsl:when test="count(.//jobdoc:params) &gt; 0">
      <description>
        <xsl:apply-templates select="child::*|@*|comment()" mode="copy_node" />
      </description>
    </xsl:when>
    <xsl:otherwise>
        <xsl:message>nichts zu tun</xsl:message> 
      <xsl:copy-of select="." />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="@*" mode="copy_node">
  <xsl:copy/>
</xsl:template>

<xsl:template match="*" mode="copy_node">
  <xsl:copy-of select="." />
</xsl:template>

<xsl:template match="jobdoc:configuration" mode="copy_node">
  <configuration>
    <xsl:apply-templates select="@*" mode="copy_node" />
    <xsl:apply-templates select="jobdoc:note" mode="copy_node" />
    <xsl:apply-templates select="jobdoc:params[@id = 'return_parameter' and count(.//jobdoc:param) &gt; 0]" mode="copy_node" />
    <xsl:apply-templates select="jobdoc:params[not(@id = 'return_parameter') and count(.//jobdoc:param) &gt; 0][1]" mode="copy_node" />
    <xsl:apply-templates select="jobdoc:payload" mode="copy_node" />
    <xsl:apply-templates select="jobdoc:settings" mode="copy_node" />
  </configuration>
</xsl:template>

<xsl:template match="jobdoc:configuration/jobdoc:params[@id = 'return_parameter']" mode="copy_node">
    <params order="1">
        <xsl:apply-templates select="child::*|@*|comment()" mode="copy_node" />
    </params>
</xsl:template>

<xsl:template match="jobdoc:configuration/jobdoc:params[not(@id = 'return_parameter')]" mode="copy_node">
    <params>
        <xsl:apply-templates select="@*" mode="copy_node" />
        <xsl:apply-templates select="note" mode="copy_node" />
        <xsl:apply-templates select="parent::jobdoc:configuration/jobdoc:params[not(@id = 'return_parameter')]//jobdoc:param" mode="copy_node" />
    </params>
</xsl:template>

<xsl:template match="comment()" mode="copy_node">
  <!--xsl:text>&#x0A;   </xsl:text><xsl:copy-of select="." /><xsl:text>&#x0A;   </xsl:text-->
</xsl:template>

</xsl:stylesheet>