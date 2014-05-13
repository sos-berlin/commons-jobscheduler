<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:xi="http://www.w3.org/2001/XInclude"

xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:dbdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1" 
xmlns="http://www.w3.org/1999/xhtml" xmlns:xhtml="http://www.w3.org/1999/xhtml" exclude-result-prefixes="dbdoc xhtml xi" >
  <xsl:output method="xml" omit-xml-declaration="no" version="1.0" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" 
  doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" indent="yes" />
  <!-- Wurzel, hier: Grundgerüst der Seite/ Reihenfolge der anderen Template-Aufrufe -->
  <xsl:variable name="default_lang" select="'en'"/>
  <xsl:template match="/dbdoc:description">
    <html>
      <head>
        <meta http-equiv="Content-Style-Type" content="text/css"/>
        <meta name="author" content="SOS GmbH"/>
        <meta name="publisher" content="Software- und Organisations- Service GmbH (SOS), Berlin"/>
        <meta name="copyright" content="Copyright 2010 SOFTWARE UND ORGANISATIONS-SERVICE GmbH (SOS), Berlin. All rights reserved."/>
        <title>SOS Documentation</title>
        <xsl:call-template name="get_css"/>
        <xsl:call-template name="get_js"/>
      </head>
      <body>
        <xsl:attribute name="onload">check_banner_gifs();select_lang('<xsl:value-of select="$default_lang"/>');</xsl:attribute>
        <xsl:call-template name="main"><xsl:with-param name="lang">de</xsl:with-param></xsl:call-template>
        <xsl:call-template name="main"><xsl:with-param name="lang">en</xsl:with-param></xsl:call-template>
      </body>
    </html>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template MAIN -->
  <xsl:template name="main">
    <xsl:param name="lang"/>
    <div>
      <xsl:attribute name="id">lang_<xsl:value-of select="$lang"/></xsl:attribute>
      <xsl:call-template name="navigation"><xsl:with-param name="lang" select="$lang"/></xsl:call-template>
      <xsl:apply-templates select="dbdoc:table"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:documentation[@language=$lang]"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:releases"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:resources"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:fields"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:primary_keys"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:foreign_keys"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      <xsl:apply-templates select="dbdoc:indexes"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
    </div>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Navigation -->
  <xsl:template name="navigation">
    <xsl:param name="lang"/>
    <table class="navi" width="100%">
      <tr>
        <td nowrap="nowrap">
          <xsl:if test="dbdoc:documentation">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('documentation','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Dokumentation'"/>
                <xsl:with-param name="label_en" select="'Documentation'"/>
              </xsl:call-template>
            </a>
            |
          </xsl:if>
          <xsl:if test="dbdoc:releases">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('firstRelease','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
            Releases
            </a>
            |
          </xsl:if>
          <xsl:if test="dbdoc:resources">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('resources','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Ressourcen'"/>
                <xsl:with-param name="label_en" select="'Resources'"/>
              </xsl:call-template>
            </a>
            |
          </xsl:if>
          <xsl:if test="dbdoc:fields">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('fields','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Felder'"/>
                <xsl:with-param name="label_en" select="'Fields'"/>
              </xsl:call-template>
            </a>
            |
          </xsl:if>
          <xsl:if test="dbdoc:primary_keys">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('primary_keys','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Primärschlüssel'"/>
                <xsl:with-param name="label_en" select="'Primary Keys'"/>
              </xsl:call-template>
            </a>
            |
          </xsl:if>
          <xsl:if test="dbdoc:foreign_keys">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('foreign_keys','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Fremdschlüssel'"/>
                <xsl:with-param name="label_en" select="'Foreign Keys'"/>
              </xsl:call-template>
            </a>
            |
          </xsl:if>
          <xsl:if test="dbdoc:indexes">
            <a class="navi" href="#">
              <xsl:attribute name="onclick">show_div('indexes','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Indizes'"/>
                <xsl:with-param name="label_en" select="'Indexes'"/>
              </xsl:call-template>
            </a>
            |
          </xsl:if>
          <a class="navi" href="#">
            <xsl:attribute name="onclick">show_div('all','<xsl:value-of select="$lang"/>');return false;</xsl:attribute>
            <xsl:call-template name="get_label">
              <xsl:with-param name="lang" select="$lang"/>
              <xsl:with-param name="label_de" select="'Alles&#160;anzeigen'"/>
              <xsl:with-param name="label_en" select="'Show&#160;all'"/>
            </xsl:call-template>
          </a>
        </td>
        <td style="text-align:center">
        </td>
        <td style="text-align:right">
          <a class="lang"><xsl:attribute name="href">javascript:switch_lang('de');</xsl:attribute>
          <img src="banner_german.gif" border="0" alt="deutsch" title="deutsch" /></a>
          <xsl:text>&#160;</xsl:text>
          <xsl:text>&#160;</xsl:text>
          <a class="lang"><xsl:attribute name="href">javascript:switch_lang('en');</xsl:attribute>
          <img src="banner_english.gif" border="0" alt="english" title="english" /></a>
        </td>
      </tr>
    </table>
    <p></p>
  </xsl:template>

  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template Table -->
  <xsl:template match="dbdoc:table">
    <xsl:param name="lang"/>
    <table class="box">
      <xsl:attribute name="id">table_<xsl:value-of select="$lang"/></xsl:attribute>
      <tr>
        <td class="td1">
          <span class="section">Table</span>
        </td>
        <td class="td2">
          <span class="label">
            <xsl:call-template name="get_label">
              <xsl:with-param name="lang" select="$lang"/>
              <xsl:with-param name="label_de" select="'Name/ Titel'"/>
              <xsl:with-param name="label_en" select="'Name/ Title'"/>
            </xsl:call-template>
          </span>
        </td>
        <td class="td3">
          <span class="sourceNameBold"><xsl:value-of select="@name"/></span>
          <xsl:text>&#160;</xsl:text>
          <xsl:text>&#160;</xsl:text>
          <span class="desc"><xsl:value-of select="@title"/></span>
        </td>
      </tr>
      <tr>
        <td class="td1">
          <xsl:text>&#160;</xsl:text>
        </td>
        <td class="td2" onmouseout="hideWMTT()" >
          <xsl:text>&#160;</xsl:text>
        </td>
        <td class="td3">
          <xsl:text>&#160;</xsl:text>
        </td>
      </tr>
      
      <xsl:apply-templates select="dbdoc:monitor/dbdoc:script">
        <xsl:with-param name="lang" select="$lang"/>
      </xsl:apply-templates>
    </table>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template Dokumentation -->
  <xsl:template match="dbdoc:documentation">
    <xsl:param name="lang"/>
    <table class="box">
      <xsl:attribute name="id">documentation_<xsl:value-of select="$lang"/></xsl:attribute>
      <tr>
        <td class="td1">
          <span class="section">
            <xsl:call-template name="get_label">
              <xsl:with-param name="lang" select="$lang"/>
              <xsl:with-param name="label_de" select="'Dokumentation'"/>
              <xsl:with-param name="label_en" select="'Documentation'"/>
            </xsl:call-template>
          </span>
        </td>
        <td class="td2"><xsl:text>&#160;</xsl:text><xsl:text/>
        </td>
        <td class="td3">

            <!-- <xsl:copy-of select="xhtml:div"/> -->
            <xsl:apply-templates mode="copy"/>

        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="xhtml:br" mode="copy">
   <br/>
  </xsl:template>

  <xsl:template match="xhtml:paramref" mode="copy">
   <code><xsl:value-of select="." /></code>
  </xsl:template>

  <xsl:template match="xhtml:paramval" mode="copy">
   <code><xsl:value-of select="." /></code>
  </xsl:template>

  <xsl:template match="xhtml:shell" mode="copy">
   <code><xsl:value-of select="." /></code>
  </xsl:template>

  <xsl:template match="*" mode="copy">
    <xsl:element name="{name()}">
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="copy"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="text()" mode="copy">
     <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="dbdoc:note">
  
    <xsl:apply-templates mode="copy" /> 
    
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template Include -->
  <xsl:template match="dbdoc:include">
    <xsl:if test="not(@file='')">
      <ul>
        <li>Include: <span class="sourceName"><xsl:value-of select="@file"/></span></li>
      </ul>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template Include, wenn XML generiert werden soll -->
  <xsl:template match="dbdoc:include" mode="genXML">
    <xsl:choose>
      <xsl:when test="ancestor::dbdoc:monitor">
        <br/>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;
      </xsl:when>
      <xsl:otherwise>
        <br/>&#160;&#160;&#160;&#160;&#160;&#160;
      </xsl:otherwise>
    </xsl:choose>

    &lt;include&#160;file&#160;=&#160;"<xsl:value-of select="@file"/>" /&gt;
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Environment/ Variable als Tabellenzeile -->
  <xsl:template match="dbdoc:variable">
    <tr>
      <td class="resource1"><span class="desc"><xsl:value-of select="@name"/></span></td>
      <td class="resource2"><span class="desc"><xsl:value-of select="@value"/></span></td>
      <td class="resource3"/>
      <td class="resource4"/>
    </tr>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Environment/ Variable, wenn XML generiert werden soll -->
  <xsl:template match="dbdoc:variable" mode="genXML">
    <br/>&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;
    &lt;variable&#160;name&#160;=&#160;"<xsl:value-of select="@name"/>"
    value&#160;=&#160;"<xsl:value-of select="@value"/>"  /&gt;
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Releases -->
  <xsl:template match="dbdoc:releases">
    <xsl:param name="lang"/>
    <xsl:variable name="cnt_releases" select="count(dbdoc:release)"/>
    <xsl:if test="$cnt_releases = 1">
      <table class="box" style="display:none">
        <xsl:attribute name="id">firstRelease_<xsl:value-of select="$lang"/></xsl:attribute>
        <xsl:for-each select="dbdoc:release">
          <xsl:if test="position() = 1">
            <xsl:call-template name="dbdoc:release_x">
              <xsl:with-param name="lang" select="$lang"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:for-each>
      </table>
    </xsl:if>
    <xsl:if test="$cnt_releases > 1">
      <table class="box" style="display:none">
        <xsl:attribute name="id">firstRelease_<xsl:value-of select="$lang"/></xsl:attribute>
        <xsl:for-each select="dbdoc:release">
          <xsl:if test="position() = 1">
            <xsl:call-template name="dbdoc:release_x">
              <xsl:with-param name="lang" select="$lang"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:for-each>
        <tr>
          <td/>
          <td/>
          <td>
            <a class="doc">
              <xsl:attribute name="id">releaseLink_<xsl:value-of select="$lang"/></xsl:attribute>
              <xsl:attribute name="href">javascript:showPreviousReleases('<xsl:value-of select="$lang"/>');</xsl:attribute>
              <span style="font-family:Arial;font-size:12px;">&#8594;</span>
              <xsl:text>&#160;</xsl:text>
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Ältere Releases anzeigen'"/>
                <xsl:with-param name="label_en" select="'Show previous Releases'"/>
              </xsl:call-template>
            </a>
          </td>
        </tr>
      </table>
      <table class="boxRelease" style="display:none">
        <xsl:attribute name="id">previousRelease_<xsl:value-of select="$lang"/></xsl:attribute>
        <xsl:for-each select="dbdoc:release">
          <xsl:if test="position() > 1">
            <xsl:call-template name="dbdoc:release_x">
              <xsl:with-param name="lang" select="$lang"/>
            </xsl:call-template>
          </xsl:if>
        </xsl:for-each>
      </table>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Releases/ Release -->
  <xsl:template name="dbdoc:release_x">
    <xsl:param name="lang"/>
    <tr>
      <td class="td1">
        <xsl:if test="position() = 1">
          <span class="section">Releases</span>
        </xsl:if>
      </td>
      <td class="td2">
        <span class="sourceNameBold">
          <xsl:value-of select="@id"/>
        </span>
      </td>
      <td class="td3">
        <span class="desc"><xsl:value-of select="dbdoc:title"/></span>
      </td>
    </tr>
    <tr>
      <td class="td1"/>
      <td class="td2"/>
      <td class="td3">
        <span class="desc">
          <xsl:value-of select="@created"/>
          [
          <xsl:call-template name="get_label">
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="label_de" select="'letzte Änderung '"/>
            <xsl:with-param name="label_en" select="'last Changes '"/>
          </xsl:call-template>
          <xsl:value-of select="@modified"/>
          ]
        </span>
      </td>
    </tr>
    <xsl:apply-templates select="dbdoc:author"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
    <tr>
      <td class="td1">
        <xsl:text> </xsl:text>
      </td>
      <td class="td2">
        <xsl:call-template name="get_label">
          <xsl:with-param name="lang" select="$lang"/>
          <xsl:with-param name="label_de" select="'Kommentar'"/>
          <xsl:with-param name="label_en" select="'Comment'"/>
        </xsl:call-template>
      </td>
      <td class="td3">
        <span class="desc">
          <xsl:apply-templates select="dbdoc:note[@language=$lang]"/>
        </span>
      </td>
    </tr>
    <xsl:apply-templates select="dbdoc:changes[@language=$lang]"/>
    <xsl:if test="position() > 1 and not( position()=last() )">
      <tr>
        <td class="td1">
          <xsl:text>&#160;</xsl:text>
        </td>
        <td class="td2_3" colspan="2">
          <hr/>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template Release/ Author -->
  <xsl:template match="dbdoc:author">
    <xsl:param name="lang"/>
    <tr>
      <td class="td1">
        <xsl:text> </xsl:text>
      </td>
      <td class="td2">
        <xsl:call-template name="get_label">
          <xsl:with-param name="lang" select="$lang"/>
          <xsl:with-param name="label_de" select="'Autor'"/>
          <xsl:with-param name="label_en" select="'Author'"/>
        </xsl:call-template>
      </td>
      <td class="td3">
        <span class="desc">
          <xsl:value-of select="@name"/>
          <xsl:text>  </xsl:text>
          <a class="mail">
            <xsl:attribute name="href">mailto:<xsl:value-of select="@email"/></xsl:attribute>
            <xsl:value-of select="@email"/>
          </a>
        </span>
      </td>
    </tr>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template Release/ Changes -->
  <xsl:template match="dbdoc:changes">
    <tr>
      <td/>
      <td class="td2">Change Log</td>
      <td class="td3">
        <span class="desc">
          <xsl:apply-templates mode="copy"/>
        </span>
      </td>
    </tr>
  </xsl:template>

  <!--  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Felder -->
  <xsl:template match="dbdoc:fields">
    <xsl:param name="lang"/>
    <!-- Nur wenn es Elemente unterhalb von fields gibt -->
    <xsl:if test="child::*">
      <table class="box" style="display:none">
        <xsl:attribute name="id">fields_<xsl:value-of select="$lang"/></xsl:attribute>
        <tr>
          <td class="td1">
            <span class="section">
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Felder'"/>
                <xsl:with-param name="label_en" select="'Fields'"/>
              </xsl:call-template>
            </span>
          </td>
          <td class="td2"><xsl:text>&#160;</xsl:text></td>
          <td class="td3">
            <xsl:choose>
              <xsl:when test="dbdoc:note[@language=$lang]">
                <xsl:apply-templates select="dbdoc:note[@language=$lang]"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:text>&#160;</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
        <xsl:apply-templates select="dbdoc:params">  <xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
      </table>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Fields/ Parameter -->
  <xsl:template match="dbdoc:params[parent::dbdoc:fields]">
    <xsl:param name="lang"/>
    <tr>
      <td class="td1">
        <xsl:call-template name="set_anchor"><xsl:with-param name="lang" select="$lang"/><xsl:with-param name="anchor_name" select="@id"/></xsl:call-template>
        <xsl:text>&#160;</xsl:text>
      </td>
      <td class="td2" onmouseout="hideWMTT()" >
        <xsl:attribute name="onmouseover">showWMTT('ttparams_<xsl:value-of select="$lang"/>')</xsl:attribute>
        <xsl:call-template name="get_label">
          <xsl:with-param name="lang" select="$lang"/>
          <xsl:with-param name="label_de" select="'Name / Typ'"/>
          <xsl:with-param name="label_en" select="'Name / Type'"/>
        </xsl:call-template>
          
      </td>
      <td class="td3">
        <xsl:choose>
          <xsl:when test="dbdoc:note[@language=$lang]">
            <xsl:apply-templates select="dbdoc:note[@language=$lang]"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="child::*">
              <table class="section" cellpadding="0" cellspacing="1">
              <xsl:apply-templates select="xi:include"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
                <xsl:apply-templates select="dbdoc:field"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
              </table>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
    <xsl:if test="dbdoc:note[@language=$lang]">
      <tr>
        <td class="td1"><xsl:text>&#160;</xsl:text></td>
        <td class="td1"><xsl:text>&#160;</xsl:text></td>
        <td class="td3">
          <xsl:if test="child::*">
            <table class="section" cellpadding="0" cellspacing="1">
                  <xsl:apply-templates select="xi:include"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
              <xsl:apply-templates select="dbdoc:field"><xsl:with-param name="lang" select="$lang"/></xsl:apply-templates>
            </table>
          </xsl:if>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für einzelnen Parameter -->
  <xsl:template match="dbdoc:field">
    <xsl:param name="lang"/>
        <xsl:param name="extdoc" />
    <tr>
      <td class="section1">
        <xsl:call-template name="set_anchor"><xsl:with-param name="lang" select="$lang"/><xsl:with-param name="anchor_name" select="@id"/></xsl:call-template>
        <xsl:call-template name="set_anchor"><xsl:with-param name="lang" select="$lang"/><xsl:with-param name="anchor_name" select="@name"/></xsl:call-template>
        <span class="sourceName">
          <xsl:value-of select="@name"/>
        </span>
      </td>
      <td class="section2">
            
            <span class="label">
              <xsl:if test="@type and not(@type='')">
                <xsl:value-of select="@type"/>
              </xsl:if>
          
              <xsl:if test="@len and not(@len='')">
                (<xsl:value-of select="@len"/>)&#160;&#160;
              </xsl:if>
            
              <xsl:text>     </xsl:text>
            
              <xsl:if test="@null and @null='false'">
                  NOT NULL&#160;&#160; 
              </xsl:if>
            
              <xsl:if test="@default and not(@default='')">
                  DEFAULT <xsl:value-of select="@default"/>&#160;&#160;
              </xsl:if>                       
            
            </span>
      </td>
    </tr>
    <xsl:if test="not(@reference)">
      <tr>
        <td class="section1">
          <xsl:if test="@required='false'">
            <span class="labelSmall">
              <xsl:text>[optional]</xsl:text>
            </span>
          </xsl:if>
          <xsl:if test="@required='true'">
            <xsl:text>&#160;</xsl:text>
          </xsl:if>
        </td>
        <td class="section2">
          <span class="desc">
            <xsl:if test="./dbdoc:note[@language=$lang]">
              <xsl:apply-templates select="./dbdoc:note[@language=$lang]"/>
            </xsl:if>
            
<!--                         <xsl:if test="$extdoc//dbdoc:note[@language=$lang]">
                            <xsl:apply-templates select="$extdoc//dbdoc:note[@language=$lang]"/>
                        </xsl:if>
                         -->
          </span>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  
  <!--  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Primary Key -->
  <xsl:template match="dbdoc:primary_keys">
    <xsl:param name="lang"/>
    <table class="box" style="display:none">
      <xsl:attribute name="id">primary_keys_<xsl:value-of select="$lang"/></xsl:attribute>
      <tr>
        <td class="td1">
          <span class="section">
            <xsl:if test="position() = 1">
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Primärschlüssel'"/>
                <xsl:with-param name="label_en" select="'Primary Keys'"/>
              </xsl:call-template>
            </xsl:if>
          </span>
        </td>
        
        <td class="td2">
          <xsl:call-template name="get_label">
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="label_de" select="'Primärschlüssel'"/>
            <xsl:with-param name="label_en" select="'Primary Key'"/>
          </xsl:call-template>
        </td>
        <td class="td3">
          <xsl:if test="dbdoc:fields">
            <table class="resource" cellpadding="0" cellspacing="1">
              <tr>
                <th class="resource1">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Feld'"/>
                    <xsl:with-param name="label_en" select="'Field'"/>
                  </xsl:call-template>
                </th>
                <th class="resource4">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Kommentar'"/>
                    <xsl:with-param name="label_en" select="'Comment'"/>
                  </xsl:call-template>
                </th>
              </tr>
              <xsl:apply-templates select="dbdoc:fields">
                <xsl:with-param name="lang" select="$lang"/>
              </xsl:apply-templates>
            </table>
          </xsl:if>
        </td>
      </tr>
      
    </table>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template primary key/ fields -->
  <xsl:template match="dbdoc:fields[parent::dbdoc:primary_keys]">
    <xsl:param name="lang"/>
  
    <xsl:if test="dbdoc:field">
      <xsl:apply-templates select="dbdoc:field">
        <xsl:with-param name="lang" select="$lang"/>
        <xsl:sort select="@type"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Eine Tabellenzeile pro primary key/ fields/ field -->
  <xsl:template match="dbdoc:field[parent::dbdoc:fields]">
    <xsl:param name="lang"/>
    <tr>
      <td class="resource1">
        <xsl:value-of select="@name"/>
      </td>
      <td class="resource4">
        <xsl:if test="dbdoc:note[@language=$lang]">
          <xsl:apply-templates select="dbdoc:note[@language=$lang]"/>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
    <!--  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Primary Key -->
  <xsl:template match="dbdoc:foreign_keys">
    <xsl:param name="lang"/>
    
    <table class="box" style="display:none">
      <xsl:attribute name="id">foreign_keys_<xsl:value-of select="$lang"/></xsl:attribute>
      <tr>
        <td class="td1">
          <span class="section">
            <xsl:if test="position() = 1">
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Fremdschlüssel'"/>
                <xsl:with-param name="label_en" select="'Foreign keys'"/>
              </xsl:call-template>
            </xsl:if>
          </span>
        </td>
        <td class="td2">
          <xsl:call-template name="get_label">
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="label_de" select="'Fremdschlüssel'"/>
            <xsl:with-param name="label_en" select="'Foreign key'"/>
          </xsl:call-template>
        </td>
        <td class="td3">
          <xsl:if test="dbdoc:field">
            <table class="resource" cellpadding="0" cellspacing="1">
              <tr>
                <th class="resource1">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Feld'"/>
                    <xsl:with-param name="label_en" select="'Field'"/>
                  </xsl:call-template>
                </th>
                <th class="resource2">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Tabelle'"/>
                    <xsl:with-param name="label_en" select="'Table'"/>
                  </xsl:call-template>
                </th>
                <th class="resource4">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Kommentar'"/>
                    <xsl:with-param name="label_en" select="'Comment'"/>
                  </xsl:call-template>
                </th>
              </tr>
              <xsl:apply-templates select="dbdoc:field">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:sort select="@os"/>
              </xsl:apply-templates>
            </table>
          </xsl:if>
        </td>
      </tr>
      
    </table>
  </xsl:template>
  
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template primary key/ fields -->
  <xsl:template match="dbdoc:field[parent::dbdoc:foreign_keys]">
    <xsl:param name="lang"/>
    <tr>
      <td class="resource1">
        <span class="sourceName"><xsl:value-of select="@name"/></span>
      </td>
      <td class="resource2">
        <span class="desc"><xsl:value-of select="@table"/></span>
      </td>
      <td class="resource4">
        <xsl:if test="dbdoc:note[@language=$lang]">
          <xsl:apply-templates select="dbdoc:note[@language=$lang]"/>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
  <!--  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template für Indizies -->
  <xsl:template match="dbdoc:indexes">
    <xsl:param name="lang"/>
    <table class="box" style="display:none">
      <xsl:attribute name="id">indexes_<xsl:value-of select="$lang"/></xsl:attribute>
      <tr>
        <td class="td1">
          <span class="section">
            <xsl:if test="position() = 1">
              <xsl:call-template name="get_label">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:with-param name="label_de" select="'Indizies'"/>
                <xsl:with-param name="label_en" select="'Indexes'"/>
              </xsl:call-template>
            </xsl:if>
          </span>
        </td>
        <td class="td2">
          <xsl:call-template name="get_label">
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="label_de" select="'Index'"/>
            <xsl:with-param name="label_en" select="'Index'"/>
          </xsl:call-template>
        </td>
        <td class="td3">
          <xsl:if test="dbdoc:index">
            <table class="resource" cellpadding="0" cellspacing="1">
              <tr>
                <th class="resource1">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Typ'"/>
                    <xsl:with-param name="label_en" select="'Type'"/>
                  </xsl:call-template>
                </th>
                <th class="resource2">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Name'"/>
                    <xsl:with-param name="label_en" select="'Name'"/>
                  </xsl:call-template>
                </th>
                <th class="resource3">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Felder'"/>
                    <xsl:with-param name="label_en" select="'Fields'"/>
                  </xsl:call-template>
                </th>
                <th class="resource4">
                  <xsl:call-template name="get_label">
                    <xsl:with-param name="lang" select="$lang"/>
                    <xsl:with-param name="label_de" select="'Kommentar'"/>
                    <xsl:with-param name="label_en" select="'Comment'"/>
                  </xsl:call-template>
                </th>
              </tr>
              <xsl:apply-templates select="dbdoc:index">
                <xsl:with-param name="lang" select="$lang"/>
                <xsl:sort select="@os"/>
              </xsl:apply-templates>
            </table>
          </xsl:if>
        </td>
      </tr>
      
    </table>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template indexes/ index -->
  <xsl:template match="dbdoc:index[parent::dbdoc:indexes]">
    <xsl:param name="lang"/>
    <tr>
      <td class="resource1">
        <span class="sourceName"><xsl:value-of select="@type"/></span>
      </td>
      <td class="resource2">
        <span class="desc"><xsl:value-of select="@name"/></span>
      </td>
      <td class="resource3">
        <span class="desc"><xsl:value-of select="@fields"/></span>
      </td>
      <td class="resource4">
        <xsl:if test="dbdoc:note[@language=$lang]">
          <xsl:apply-templates select="dbdoc:note[@language=$lang]"/>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template set_anchor -->
  <xsl:template name="set_anchor">
    <xsl:param name="lang"/>
    <xsl:param name="anchor_name"/>
    <a>
      <xsl:attribute name="name"><xsl:value-of select="$anchor_name"/>_<xsl:value-of select="$lang"/></xsl:attribute>
    </a>
  </xsl:template>
  <xsl:template match="dbdoc:note | dbdoc:changes | dbdoc:documentation" mode="reference">
    <xsl:apply-templates select="."/>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template get_label -->
  <xsl:template name="get_label">
    <xsl:param name="lang"/>
    <xsl:param name="label_de"/>
    <xsl:param name="label_en"/>
    <xsl:if test="$lang='de'">
      <xsl:value-of select="$label_de"/>
    </xsl:if>
    <xsl:if test="$lang='en'">
      <xsl:value-of select="$label_en"/>
    </xsl:if>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template get_css -->
  <xsl:template name="get_css">
    <style type="text/css"><![CDATA[

      body { background-color:#FFFFFF; margin-left:20px; font-family:verdana,arial,sans-serif;font-size:10pt; }

      ul    { margin-top: 0px; margin-bottom: 10px; margin-left: 0; padding-left: 1em;
              font-weight:300; }
      li    { margin-bottom: 10px; }

      h1    { font-family:verdana,arial,sans-serif;font-size:10pt; font-weight:600; }

      /* table { width:100%; background-color:#F3F4F4; } dunkler */
      /* table { width:100%; background-color:#F5FAFA; } heller */

      table.navi {background-color:#FFFFFF }

      table { font-family:verdana,arial,sans-serif;font-size:10pt;
              width:100%;
              background-color:#F9FCFC;
            }

      td {
        padding: 2px 2px 2px 2px;
        vertical-align:top; text-align: left;
        font-family:verdana, arial, sans-serif;font-size:10pt;
      }

      table.box        { border-width:2px; border-style:solid; border-color:#C0C0C0; }
      table.boxRelease { border-color:#C0C0C0;
                         border-left-width:2px;   border-left-style:solid;
                         border-right-width:2px;  border-right-style:solid;
                         border-top-width:1px;    border-top-style:none;
                         border-bottom-width:2px; border-bottom-style:solid;
                       }

      td.td1     {width:11%; }
      td.td2     {width:12%; }
      td.td3     {width:79%; }
      td.td2_3   {width:87%; }

      td.td31    {width:20%; }
      td.td32    {width:80%; }

      td.td31_32 {width:100%; }

      table.resource   { background-color:#DCDCDC; }

      th.resource1     { background-color:#F3F4F4;
                         text-align: left;
                         font-family:verdana,arial,sans-serif;font-size:10pt;
                         font-weight:300;
                         width:20%;
                       }
      th.resource2     { background-color:#F3F4F4;
                         text-align: left;
                         font-family:verdana,arial,sans-serif;font-size:10pt;
                         font-weight:300;
                         width:10%;
                       }
      th.resource3     { background-color:#F3F4F4;
                         text-align: left;
                         font-family:verdana,arial,sans-serif;font-size:10pt;
                         font-weight:300;
                         width:10%;
                       }
      th.resource2_3   { background-color:#F3F4F4;
                         text-align: left;
                         font-family:verdana,arial,sans-serif;font-size:10pt;
                         font-weight:300;
                         width:20%;
                       }
      th.resource4     { background-color:#F3F4F4;
                         text-align: left;
                         font-family:verdana,arial,sans-serif;font-size:10pt;
                         font-weight:300;
                         width:60%;
                       }

      td.resource1     { color:#009900;
                         background-color:#F3F4F4;
                         width:25%;
                       }
      td.resource2     { color:#336699;
                         background-color:#F3F4F4;
                         width:10%;
                       }
      td.resource3     { color:#336699;
                         background-color:#F3F4F4;
                         width:10%;
                       }
      td.resource2_3   { color:#336699;
                         background-color:#F3F4F4;
                         width:20%;
                       }
      td.resource4     { color:#336699;
                         background-color:#F3F4F4;
                         width:60%;
                       }

      table.section   { background-color:#DCDCDC; }

      td.section1     { color:#009900;
                        background-color:#F3F4F4;
                        width:20%;
                      }
      td.section2     { color:#336699;
                        font-weight:300;
                        background-color:#F3F4F4;
                        width:80%;
                      }

      .section        {color:#000000; font-weight:600; }                         /* schwarze Schrift, fett */
      .label          {color:#000000; font-weight:300; }
      .labelSmall     {color:#000000; font-weight:300; font-size:8pt; }          /* schwarze Schrift */
      .sourceName     {color:#009900; font-weight:300; }                         /* grüne Schrift */
      .sourceNameBold {color:#009900; font-weight:600; }                         /* grüne Schrift */
      .desc           {color:#336699; font-weight:300; }                         /* blaue Schrift */

      .code           {color:#000000; font-weight:300; font-family:"Courier New",sans-serif;font-size:10pt; }      /* Schrift für XML-Code */

      font.section        {color:#000000; font-weight:600; }                      /* schwarze Schrift, fett */
      font.label          {color:#000000; font-weight:300; }                      /* schwarze Schrift */
      font.labelSmall     {color:#000000; font-weight:300; font-size:8pt; }       /* schwarze Schrift */
      font.sourceName     {color:#009900; font-weight:300; }                      /* grüne Schrift */
      font.sourceNameBold {color:#009900; font-weight:600; }                      /* grüne Schrift */
      font.desc           {color:#336699; font-weight:300; }                      /* blaue Schrift */

      font.code           {color:#000000; font-weight:300; font-family:"Courier New",sans-serif;font-size:10pt; }      /* Schrift für XML-Code */
      .tooltip
       {
        position:absolute;
        width:400px; height:120px;
        display:none;
        background-color:#FFFFFF;
        border:1px solid;
        border-color:#FF6347;
        color:#000000;
        font-weight:300
       }

      /*** LINK Formatierungen ***/

        a                         { font-weight:600; text-decoration:none; font-size:10pt; }

      /* Links Navigation */
        a.navi:link               { color:#FF9900; font-weight:600; font-size:12pt;}
        a.navi:visited            { color:#FF9900; font-weight:600; font-size:12pt;}
        a.navi:hover              { color:#FFCC00; font-weight:600; font-size:12pt;}
        a.navi:active             { color:#FF6347; font-weight:600; font-size:12pt;}

      /* Links für Sprachumschaltung */
        a.lang                    { color:#660066; font-weight:600; font-size:10pt; text-decoration:underline; }

      /* Links im Doku-Text */
      /*a.doc:link                    { color:#663333; font-weight:300;}
        a.doc:visited                 { color:#663333; font-weight:300;} */
        a.doc:link                    { color:#FF9900; font-weight:300;}
        a.doc:visited                 { color:#CC3300; font-weight:300;}
        a.doc:hover                   { color:#FF9900; font-weight:300;}
        a.doc:active                  { color:#FF6347; font-weight:300;}

      /* Mail-Verweis */
        a.mail                    { color:#FF9900; font-weight:300;}

    ]]></style>
  </xsl:template>
  
  <xsl:template match="code" >
  <code>
    <apply-templates select="." />
  </code>
  </xsl:template>


  <xsl:template match="paramref" >
  <code> Paramref
  <i>
    <apply-templates select="." />
  </i>
  </code>
  </xsl:template>
  <!-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Template get_js -->
  <xsl:template name="get_js">
    <script type="text/javascript"><xsl:text disable-output-escaping="yes"><![CDATA[

  var wmtt = null;
      document.onmousemove = updateWMTT;

      function updateWMTT(e) {
    var x = (document.all) ? window.event.x + document.body.scrollLeft : e.pageX;
    var y = (document.all) ? window.event.y + document.body.scrollTop  : e.pageY;
        if (wmtt != null) {
          wmtt.style.left = (x + 20) + "px";
          wmtt.style.top  = (y + 20) + "px";
        }
      }

      function showWMTT(id) {
        wmtt = document.getElementById(id);
        wmtt.style.display = "block"
       }

      function hideWMTT() {
        wmtt.style.display = "none";
      }

      function select_lang( default_lang ) {

        var lang;
        if (default_lang == '' ) {
          lang = 'en';
        } else {
          lang = default_lang;
        }

        if( window.location.hash.search( /(de|en)/ ) != -1 ) lang = window.location.hash.slice(1);
        switch_lang( lang );
      }

      function switch_lang( lang ) {
        if ( lang == 'de' ) {
          document.getElementById('lang_de').style.display = '';
          document.getElementById('lang_en').style.display = 'none';
        } else {
          document.getElementById('lang_en').style.display = '';
          document.getElementById('lang_de').style.display = 'none';
        }
      }

      function show_div(id, lang) {
        if ( lang == 'de' ) {
          documentation_id = 'documentation_de';
          firstRelease_id  = 'firstRelease_de';
          resources_id     = 'resources_de';
          fields_id        = 'fields_de';
          primary_keys_id  = 'primary_keys_de';
          foreign_keys_id  = 'foreign_keys_de';
          indexes_id       = 'indexes_de';
          genXML_id        = 'genXML_de';
        } else {
          documentation_id = 'documentation_en';
          firstRelease_id  = 'firstRelease_en';
          resources_id     = 'resources_en';
          fields_id        = 'fields_en';
          primary_keys_id  = 'primary_keys_en';
          foreign_keys_id  = 'foreign_keys_en';
          indexes_id       = 'indexes_en';
          genXML_id        = 'genXML_en';
        }

        try {
          if ( id == 'all' ) {
            if ( document.getElementById(documentation_id)) { document.getElementById(documentation_id).style.display  = ''; }
            if ( document.getElementById(firstRelease_id))  { document.getElementById(firstRelease_id).style.display   = ''; }
            if ( document.getElementById(resources_id))     { document.getElementById(resources_id).style.display      = ''; }
            if (document.getElementById(fields_id))         { document.getElementById(fields_id).style.display  = ''; }
            if (document.getElementById(primary_keys_id))   { document.getElementById(primary_keys_id).style.display  = ''; }
            if (document.getElementById(foreign_keys_id))   { document.getElementById(foreign_keys_id).style.display  = ''; }
            if (document.getElementById(indexes_id))        { document.getElementById(indexes_id).style.display  = ''; }
            hide_div(genXML_id);
          } else {
            switch (id) {
              case  'documentation':
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(resources_id);
                hide_div(fields_id);
                hide_div(primary_keys_id);
                hide_div(foreign_keys_id);
                hide_div(indexes_id);
                hide_div(genXML_id);
                if (document.getElementById(documentation_id)) {
                  document.getElementById(documentation_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Dokumentation' );
                  } else {
                    alert( 'There is no documentation for this table.' );
                  }
                }
                break;
              case  'firstRelease':
                hide_div(documentation_id);
                hide_div(resources_id);
                hide_div(fields_id);
                hide_div(primary_keys_id);
                hide_div(foreign_keys_id);
                hide_div(indexes_id);
                hide_div(genXML_id);
                if (document.getElementById(firstRelease_id)) {
                  document.getElementById(firstRelease_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Releases.' );
                  } else {
                    alert( 'There are no releases for this table.' );
                  }
                }
                break;
              case  'resources':
                hide_div(documentation_id);
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(fields_id);
                hide_div(primary_keys_id);
                hide_div(foreign_keys_id);
                hide_div(indexes_id);
                hide_div(genXML_id);
                if (document.getElementById(resources_id)) {
                  document.getElementById(resources_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Ressourcen.' );
                  } else {
                    alert( 'There are no resources for this table.' );
                  }
                }
                break;
              case  'fields':
                hide_div(documentation_id);
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(resources_id);
                hide_div(primary_keys_id);
                hide_div(foreign_keys_id);
                hide_div(indexes_id);
                hide_div(genXML_id);
                if (document.getElementById(fields_id)) {
                  document.getElementById(fields_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Felder' );
                  } else {
                    alert( 'There is no fields for this table.' );
                  }
                }
                break;
              case  'primary_keys':
                hide_div(documentation_id);
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(resources_id);
                hide_div(fields_id);
                hide_div(foreign_keys_id);
                hide_div(indexes_id);
                hide_div(genXML_id);
                if (document.getElementById(primary_keys_id)) {
                  document.getElementById(primary_keys_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Primärschlüssel' );
                  } else {
                    alert( 'There is no primary keys for this table.' );
                  }
                }
                break;
              case  'foreign_keys':
                hide_div(documentation_id);
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(resources_id);
                hide_div(fields_id);
                hide_div(primary_keys_id);
                hide_div(indexes_id);
                hide_div(genXML_id);
                if (document.getElementById(foreign_keys_id)) {
                  document.getElementById(foreign_keys_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Fremdschlüssel' );
                  } else {
                    alert( 'There is no foreign keys for this table.' );
                  }
                }
                break;              
              case  'indexes':
                hide_div(documentation_id);
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(resources_id);
                hide_div(fields_id);
                hide_div(primary_keys_id);
                hide_div(foreign_keys_id);
                hide_div(genXML_id);
                if (document.getElementById(indexes_id)) {
                  document.getElementById(indexes_id).style.display = '';
                } else {
                  if ( lang == 'de' ) {
                    alert( 'Für diese Tabelle gibt es keine Indizies' );
                  } else {
                    alert( 'There is no indexes for this table.' );
                  }
                }
                break;
              case  'genXML':
                hide_div(documentation_id);
                if ( document.getElementById('previousRelease_' + lang) ) { hide_div('previousRelease_' + lang); }
                hide_div(firstRelease_id);
                hide_div(resources_id);
                hide_div(fields_id);
                hide_div(primary_keys_id);
                hide_div(indexes_id);
                document.getElementById(genXML_id).style.display = '';
                break;
            }
          }
        }
        catch(x){
          alert('show_div : '+x.message);
         }
      }

      function hide_div( id ) {
        if ( document.getElementById(id) ) {
          try {
            document.getElementById(id).style.display = 'none';
          }
          catch(x) {
            alert('hide_div : '+x.message);
          }
        }
      }

      function showPreviousReleases( lang ) {

        ref = "javascript:hidePreviousReleases('" + lang + "')";
        if ( lang == "de" ) {
          linkTxt = 'Nur letztes Release anzeigen';
        } else {
          linkTxt = 'Show most recent Release';
        }

        document.getElementById('previousRelease_' + lang).style.display = '';
        document.getElementById('releaseLink_' + lang).innerHTML = linkTxt;
        document.getElementById('releaseLink_' + lang).href = ref;
      }

      function hidePreviousReleases( lang ) {

        document.getElementById('previousRelease_' + lang).style.display = 'none';
        ref = "javascript:showPreviousReleases('" + lang + "')";
        if ( lang == "de" ) {
          linkTxt = 'Ältere Releases anzeigen';
        } else {
          linkTxt = 'Show previous Releases';
        }

        document.getElementById('releaseLink_' + lang).innerHTML = linkTxt;
        document.getElementById('releaseLink_' + lang).href = ref;
      }

      function genXML( lang ) {
         show_div('genXML',lang);
      }

      function check_banner_gifs() {

        var gifs = document.getElementsByTagName("img");
        var gifs_complete = true;
        var gifs_length   = gifs.length;
        var i = 0;
        while( i != gifs_length ) {
          if( !gifs[i].complete ) {
            gifs_complete = false;
            break;
          }
          i++;
        }
        if( !gifs_complete ) {
          i = 0;
          while( i != gifs_length ) {

            try {
              gifs[i].parentNode.innerHTML = gifs[i].title;
            }
            catch(E) { break; }
          }
          i++;
        }
      }

      ]]></xsl:text></script>
  </xsl:template>
  
<xsl:template match="xi:include">
<xsl:param name="lang"/>
    <xsl:apply-templates select="document(@href)/dbdoc:field" >
    <xsl:with-param name="lang" select="$lang"/>
    <xsl:with-param name="extdoc" select="document(@href)" />
    </xsl:apply-templates>
  </xsl:template>
  
</xsl:stylesheet>
