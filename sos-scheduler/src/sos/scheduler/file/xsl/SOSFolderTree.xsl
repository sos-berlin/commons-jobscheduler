<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:sos="http://www.sos-berlin.com/namespace" version="1.0">
   <xsl:output method="html" encoding="iso-8859-1" omit-xml-declaration="yes" />
   <xsl:include href="./elog.xsl" />
   <xsl:variable name="IntEditMask">###.###.###.##0 </xsl:variable>
   <xsl:variable name="IntSortMask">000.000.000.000 </xsl:variable>
   <xsl:template match="/">
      <xsl:apply-templates select="FolderTree" />
   </xsl:template>
   <xsl:template match="FolderTree">
      <html>
         <xsl:call-template name="CreateHTMLIncludes" />
         <head>
            <title></title>
         </head>
         <body>
            <br />
            <h2>Folders</h2>
            <table border="off">
               <colgroup>
                  <col width = "35%" />
                  <col width = "5%" />
                  <col width = "5%" />
                  <col width = "10%" />
                  <col width = "12%" />
                  <col width = "12%" />
                  <col width = "15%" />
               </colgroup>
               <thead>
                  <tr>
                     <th> FolderName </th>
                     <th> NoOfFiles </th>
                     <th> NoOfFolders </th>
                     <th> Size </th>
                     <th> NoOfFiles </th>
                     <th> NoOfFolders </th>
                     <th> Size </th>
                  </tr>
               </thead>
               <tbody>
                  <xsl:for-each select="./Folder" >
                     <xsl:sort select="format-number(TreeStatistic/Size, $IntSortMask)" order="descending" />
                     <xsl:apply-templates select="." />
                  </xsl:for-each>
               </tbody>
            </table>
            <br />
         </body>
      </html>
   </xsl:template>
   <!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        <docu>
        <description>
        ein einzelner Folder
        </description>
        <date>
        Dienstag, 9. Dezember 2003
        </date>
        <copyright>
        (c) 09.12.03 by SGLO111
        </copyright>
        <author>
        SGLO111
        </author>
        </docu>
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
   <xsl:template match="Folder">
      <tr>
         <td align="left">
         <!-- 
            <a>
               <xsl:call-template name="CreateDisplayIcon">
                  <xsl:with-param name="ParaNo" select="sos:DoNumber()" />
               </xsl:call-template>
            </a>
            
            -->
            <xsl:value-of select="./Name" />
         </td>
         <td align="right">
            <xsl:value-of select="format-number(./FolderStatistic/FileCnt, $IntEditMask)" />
         </td>
         <td align="right">
            <xsl:value-of select="format-number(./FolderStatistic/FolderCnt, $IntEditMask)" />
         </td>
         <td align="right">
            <xsl:value-of select="format-number(./FolderStatistic/Size, $IntEditMask)" />
         </td>
         <td align="right">
            <xsl:value-of select="format-number(./TreeStatistic/FileCnt, $IntEditMask)" />
         </td>
         <td align="right">
            <xsl:value-of select="format-number(./TreeStatistic/FolderCnt, $IntEditMask)" />
         </td>
         <td align="right">
            <xsl:value-of select="format-number(./TreeStatistic/Size, $IntEditMask)" />
         </td>
      </tr>
      <tr>
         <xsl:attribute name="style">margin-left: +5em</xsl:attribute>
         <!-- 
         <xsl:attribute name="style">display: none; margin-left: +5em</xsl:attribute>
         <xsl:attribute name="ID">
            <xsl:value-of select="concat('exp',sos:GetNumber())" />
         </xsl:attribute>
          -->
         <td align="left" colspan="7">
            <table border="off">
               <colgroup>
                  <col width = "35%" />
                  <col width = "5%" />
                  <col width = "5%" />
                  <col width = "10%" />
                  <col width = "12%" />
                  <col width = "12%" />
                  <col width = "15%" />
               </colgroup>
               <tbody>
                  <xsl:for-each select="./Folders/Folder" >
                     <xsl:sort select="format-number(TreeStatistic/Size, $IntSortMask)" order="descending" />
                     <xsl:apply-templates select="." />
                  </xsl:for-each>
               </tbody>
            </table>
         </td>
      </tr>
   </xsl:template>
   <!-- <<<<<<<<<<<<<<<<<<<<<Ende der eigenen Templates -->
</xsl:stylesheet>

