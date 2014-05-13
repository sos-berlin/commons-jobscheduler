<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0">
	<xsl:output method="text" />
	<xsl:strip-space
		elements="description script log_mail_to log_mail_cc log_mail_bcc" />

	<!-- this parameter declares the output-folder of the generated single JS-object-files -->
	<xsl:param name="sos.destination" select="'.'" />

	<!-- this parameter declares the character endcoding for the created object-files -->
	<!-- <xsl:param name="charencoding" select="'UTF-8'" /> -->
	<xsl:param name="sos.charencoding" select="'iso-8859-1'" />

	<!-- modify destination (backslashes to shlashes, delete doublequotes, shlash 
		at the end) -->
	<xsl:param name="normalized_destination">
		<xsl:choose>
			<xsl:when test="$sos.destination">
				<xsl:value-of
					select="concat(replace(translate($sos.destination,'\&quot;','/'),'/$',''),'/')" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'./'" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:param>

	<!-- windows special for handling absolute destination paths -->
	<xsl:param name="file_destination">
		<xsl:choose>
			<xsl:when test="starts-with($normalized_destination,'file:')">
				<xsl:value-of select="$normalized_destination" />
			</xsl:when>
			<xsl:when test="contains($normalized_destination,':/')">
				<xsl:value-of select="concat('file:/',$normalized_destination)" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$normalized_destination" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:param>

	<!-- match some objects from a classic JobScheduler configuration file to 
		extract them as Hot Folder objects -->
	<xsl:template match="/spooler/config">
		<!--xsl:apply-templates mode="objects" select="params"/ --><!-- params have no attibutes to set the filename -->
		<xsl:apply-templates mode="objects"
			select="process_classes/process_class" />
		<xsl:apply-templates mode="objects" select="schedules/schedule" />
		<xsl:apply-templates mode="objects" select="locks/lock" />
		<xsl:apply-templates mode="objects" select="jobs/job" />
		<xsl:apply-templates mode="objects" select="job_chains/job_chain" />
		<xsl:apply-templates mode="objects"
			select="commands/order|commands/add_order" />
	</xsl:template>

	<!-- set filename for Hot Folder objects except orders and call generator -->
	<xsl:template match="process_class|schedule|lock|job|job_chain"
		mode="objects">
		<xsl:param name="objname" select="name()" />
		<xsl:variable name="filename" select="concat(@name,'.',$objname,'.xml')" />
		<xsl:call-template name="stdout">
			<xsl:with-param name="objects" select="name(parent::*)" />
			<xsl:with-param name="filename" select="$filename" />
			<xsl:with-param name="position" select="position()" />
		</xsl:call-template>
		<xsl:apply-templates mode="write" select=".">
			<xsl:with-param name="filename" select="$filename" />
			<xsl:with-param name="objname" select="$objname" />
		</xsl:apply-templates>
	</xsl:template>

	<!-- set filename for Hot Folder orders and call generator -->
	<xsl:template match="order|add_order" mode="objects">
		<xsl:param name="objname" select="'order'" />
		<xsl:variable name="filename"
			select="concat(@job_chain,',',@id,'.',$objname,'.xml')" />
		<xsl:call-template name="stdout">
			<xsl:with-param name="objects" select="'orders'" />
			<xsl:with-param name="filename" select="$filename" />
			<xsl:with-param name="position" select="position()" />
		</xsl:call-template>
		<xsl:apply-templates mode="write" select=".">
			<xsl:with-param name="filename" select="$filename" />
			<xsl:with-param name="objname" select="$objname" />
		</xsl:apply-templates>
	</xsl:template>

	<!-- generate Hot Folder objects -->
	<xsl:template match="*" mode="write">
		<xsl:param name="filename" />
		<xsl:param name="objname" select="name()" />
		<xsl:result-document
			href="{iri-to-uri(concat($file_destination,$filename))}" method="xml"
			encoding="{$sos.charencoding}" output-version="1.0" indent="yes"
			cdata-section-elements="description script log_mail_to log_mail_cc log_mail_bcc">
			<xsl:element name="{$objname}">
				<xsl:apply-templates select="child::*|@*|comment()"
					mode="copy_node" />
			</xsl:element>
		</xsl:result-document>
	</xsl:template>

	<!-- copy attributes except name, id, job_chain and replace -->
	<xsl:template match="@*" mode="copy_node">
		<xsl:choose>
			<xsl:when test="name()='name'">
			</xsl:when>
			<xsl:when test="name()='id'">
			</xsl:when>
			<xsl:when test="name()='job_chain'">
			</xsl:when>
			<xsl:when test="name()='replace'">
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="." />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- copy comments with some cosmetics for a better look -->
	<xsl:template match="comment()" mode="copy_node">
		<xsl:text>
   </xsl:text>
		<xsl:copy-of select="." />
		<xsl:if test="position()=last()">
			<xsl:text>
</xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- copy elements -->
	<xsl:template match="*" mode="copy_node">
		<xsl:copy-of select="." />
	</xsl:template>

	<!-- only some lines to stdout -->
	<xsl:template name="stdout">
		<xsl:param name="objects" select="'objects'" />
		<xsl:param name="filename" />
		<xsl:param name="position" />

		<xsl:if test="$position=1">
			<xsl:message>
The following <xsl:value-of select="$objects" /> will be generated in &quot;<xsl:value-of select="$normalized_destination" />&quot;:
</xsl:message>
		</xsl:if>
		<xsl:message>
			<xsl:value-of select="$position" />. <xsl:value-of select="$filename" />
</xsl:message>
	</xsl:template>

</xsl:stylesheet>
