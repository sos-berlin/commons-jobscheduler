<?xml version="1.0" encoding="UTF-8"?>
<!-- $Id$ -->
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
    <xsl:when test=".//xi:include">
      <description>
        <xsl:apply-templates select="child::*|@*|comment()" mode="copy_node" />
      </description>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="." />
    </xsl:otherwise>
  </xsl:choose> 
</xsl:template>


<xsl:template match="@*" mode="copy_node">
  <xsl:copy/>
</xsl:template>

<xsl:template match="*" mode="copy_node">
  <xsl:choose>
    <xsl:when test="name()='xi:include'">
    	<xsl:message><xsl:value-of select="concat('xi:xinclude for ', @href)"></xsl:value-of> 
    	</xsl:message>
      <xsl:apply-templates select="document(@href)/jobdoc:*" mode="copy_xiinclude" />
    </xsl:when>
    <xsl:when test="name() = 'params' and not(jobdoc:*) and not(xi:include)">
    </xsl:when>
    <xsl:when test=".//xi:include">
      <xsl:element name="{name()}" namespace="{namespace-uri()}">
        <xsl:apply-templates select="child::*|@*|comment()" mode="copy_node" />
        <xsl:value-of select="text()" />
      </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:copy-of select="." />
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="comment()" mode="copy_node">
  <!--xsl:text>&#x0A;   </xsl:text><xsl:copy-of select="." /><xsl:text>&#x0A;   </xsl:text-->
</xsl:template>

<xsl:template match="*" mode="copy_xiinclude">
  <xsl:element name="{name(.)}">
    <xsl:apply-templates select="@*" mode="copy_node" />
    <xsl:if test="@DefaultValue">
      <xsl:attribute name="default_value"><xsl:value-of select="@DefaultValue" /></xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="child::*" mode="copy_node" />
  </xsl:element>
</xsl:template>
  
</xsl:stylesheet>
