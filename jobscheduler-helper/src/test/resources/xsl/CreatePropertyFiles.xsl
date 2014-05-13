<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:strip-space elements="*" />
	<xsl:preserve-space elements="*" />

	<xsl:output method="text" encoding="utf-8" indent="no" />
	<xsl:variable name="nl" select="'&#xa;'" />

	<xsl:param name="lang" select="'de'" />

	<xsl:template match="/messages">
		<xsl:message>
			<xsl:value-of select="concat('Start conversion for language ', $lang)" />
		</xsl:message>

		<xsl:text>
#
# This file is mechanicaly created by CreatePropertyFiles.xsl. do not edit, otherwise the changes will be lost.
#
# this file is part of the Open Source JobScheduler powered by www.sos-berlin.com
#
</xsl:text>

		<xsl:for-each select="./message">
			<xsl:sort order="ascending" select="./@code" />
			<xsl:apply-templates select="." />
		</xsl:for-each>
	</xsl:template>

	<xsl:template match="message">
		<xsl:choose>
			<xsl:when test="./@alias and ./@alias != ''">
				<xsl:value-of select="concat(@code, '=[', ./@alias, ']', $nl)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="./text[@lang = $lang]">
						<xsl:apply-templates select="./text[@lang = $lang]" />
					</xsl:when>
					<xsl:otherwise>
				<xsl:value-of select="concat('# ', @code, '= missing Text for language [', $lang, ']', $nl)" />
						<xsl:apply-templates select="./text[@lang = 'en']" />
					</xsl:otherwise>
				</xsl:choose>

			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="text">
		<xsl:value-of select="concat(./../@code, '=', normalize-space(./title), $nl)" />
	</xsl:template>

	<xsl:template match="description">
	</xsl:template>

	<xsl:template match="text()">
		<!-- 	<xsl:value-of select="normalize-space(.)"/> -->
	</xsl:template>

</xsl:stylesheet>