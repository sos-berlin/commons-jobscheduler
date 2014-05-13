<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- $Id$ -->

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:xi="http://www.w3.org/2001/XInclude" 
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:xd="http://www.pnp-software.com/XSLTdoc"
    exclude-result-prefixes="xhtml xd xi"
    version="2.0">

    <xsl:output method="xml" encoding="UTF-8" indent="yes" standalone="yes" />
    <xsl:strip-space elements="*" />
    
    <xsl:template match="/languages">
        <languages>
           <xsl:for-each-group select="i18n/text" group-by="@name">
                <xsl:sort select="lower-case(current-grouping-key())" />
                <xsl:if test="not(starts-with(lower-case(current-grouping-key()),'tooltip'))">
                <item key="{current-grouping-key()}">
                <xsl:for-each select="current-group()">
                    <xsl:sort select="parent::i18n/@lang" />
                    <xsl:variable name="lang" select="parent::i18n/@lang" />
                        <language id="{$lang}">
                            <text><xsl:value-of select="." /></text>
                            <description/>
                            <tooltip><xsl:value-of select="parent::i18n/text[lower-case(@name) = concat('tooltip',lower-case(current-grouping-key()))]"/></tooltip>
                        </language>
                </xsl:for-each>
                </item>
                </xsl:if>
           </xsl:for-each-group> 
        </languages>
    </xsl:template>
    
</xsl:stylesheet>