<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:jobdoc="http://www.sos-berlin.com/schema/scheduler_job_documentation_v1.1"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:sos="http://www.sos-berlin.com"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                version="2.0"
                exclude-result-prefixes="jobdoc xi xhtml xsi fn sos">              

<xsl:output method="xml"
            encoding="UTF-8" 
            version="1.0" 
            indent="yes" 
            omit-xml-declaration="no" />
<!--xsl:strip-space elements="*"/-->
          
	<xsl:variable name="requiredAttributes" select="fn:tokenize('datatype,required,defaultvalue',',')"/>
    <xsl:variable name="requiredAttributeValues" select="fn:tokenize('SOSOptionString,false,',',')"/>
    <xsl:variable name="targetSourceScope" select="'ssh_auth_file,ssh_auth_method,ssh_proxy_host,ssh_proxy_password,ssh_proxy_port,ssh_proxy_user,host,passive_mode,password,port,protocol,transfer_mode,user,load_class_name'"/>
    <xsl:variable name="targetScope" select="'replacement,replacing'"/>
    <xsl:variable name="sourceScope" select="''"/>
        
        
    <xsl:template match="/">
        <params>
            <xsl:apply-templates select="//jobdoc:params[@id='job_parameter']" mode="attr"/>
            <xsl:call-template name="addInclude"/>
        </params>
    </xsl:template>
    
    <xsl:template match="jobdoc:params" mode="attr">
        <xsl:apply-templates select="jobdoc:param|xi:include" mode="attr"/>
    </xsl:template>
    
    <xsl:template match="xi:include" mode="attr">
        <xsl:apply-templates select="document(@href)/jobdoc:param|document(@href)/jobdoc:params" mode="attr" />
    </xsl:template>
    
    <xsl:template match="jobdoc:param" mode="attr">
        <param>
            <xsl:apply-templates select="@*" mode="attr" />
            <xsl:call-template name="addRequiredAttributes"/>
        </param>
        <xsl:choose>
            <xsl:when test="sos:itemOfAlternative(@name) = true()">
                <xsl:call-template name="addAlternatives"/>
            </xsl:when>
            <xsl:when test="sos:itemOfTargetSourceScope(@name) = true()">
                <xsl:call-template name="addTargetSourceScope"/>
            </xsl:when>
            <xsl:when test="sos:itemOfTargetScope(@name) = true()">
                <xsl:call-template name="addTargetScope"/>
            </xsl:when>
            <xsl:when test="sos:itemOfSourceScope(@name) = true()">
                <xsl:call-template name="addSourceScope"/>
            </xsl:when>         
        </xsl:choose>
	</xsl:template>
    
    
    <xsl:template name="addRequiredAttributes">
        <xsl:param name="param_name" select="@name"/>
        <xsl:variable name="attributes" select="@*"/>
               
        <xsl:for-each select="$requiredAttributes">
            <xsl:variable name="index" select="position()"/>
            <xsl:variable name="requiredAttributes" select="."/>
            <xsl:variable name="checkAttribute" select="$attributes[sos:normalize(name(.)) = $requiredAttributes]"/>
            <xsl:if test="fn:empty($checkAttribute) = true() or $checkAttribute = ''">
               <xsl:message>add Attribute <xsl:value-of select="$requiredAttributes"/> to <xsl:value-of select="$param_name"/></xsl:message>
               <xsl:element name="{$requiredAttributes}"><xsl:value-of select="$requiredAttributeValues[$index]"/></xsl:element> 
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="addAlternatives">
        <xsl:variable name="param_prefix" select="'alternative_'"/>
        <xsl:variable name="param_suffix" select="substring-after(@name,'_')" />
        <xsl:variable name="param_target" select="concat($param_prefix,'target_',$param_suffix)"/>
        <xsl:variable name="param_source" select="concat($param_prefix,'source_',$param_suffix)"/>
        <param>
            <xsl:message>add Element <xsl:value-of select="$param_target"/></xsl:message>
            <xsl:apply-templates select="@*" mode="attr">
                <xsl:with-param name="param_name" select="$param_target"/>
            </xsl:apply-templates>
            <xsl:call-template name="addRequiredAttributes">
                <xsl:with-param name="param_name" select="$param_target"/>
            </xsl:call-template>
        </param>
        <param>
            <xsl:message>add Element <xsl:value-of select="$param_source"/></xsl:message>
            <xsl:apply-templates select="@*" mode="attr">
                <xsl:with-param name="param_name" select="$param_source"/>
            </xsl:apply-templates>
            <xsl:call-template name="addRequiredAttributes">
                <xsl:with-param name="param_name" select="$param_source"/>
            </xsl:call-template>
        </param>
    </xsl:template>
    
    <xsl:template name="addTargetSourceScope">
        <xsl:call-template name="addTargetScope"/>
        <xsl:call-template name="addSourceScope"/>
    </xsl:template>
    
    <xsl:template name="addTargetScope">
        <xsl:call-template name="addScope" >
            <xsl:with-param name="param_prefix" select="'target_'"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template name="addSourceScope">
        <xsl:call-template name="addScope"/>
    </xsl:template>
    
    <xsl:template name="addScope">
        <xsl:param name="param_prefix" select="'source_'"/>
        <xsl:variable name="param_name" select="concat($param_prefix,@name)"/>
        <param>
            <xsl:message>add Element <xsl:value-of select="$param_name"/></xsl:message>
            <xsl:apply-templates select="@*" mode="attr">
                <xsl:with-param name="param_name" select="$param_name"/>
            </xsl:apply-templates>
            <xsl:call-template name="addRequiredAttributes">
                <xsl:with-param name="param_name" select="$param_name"/>
            </xsl:call-template>
        </param>
    </xsl:template>
    
    <xsl:template name="addInclude">
        <xsl:variable name="param_name" select="'include'"/>
        <param>
            <xsl:message>add Element <xsl:value-of select="$param_name"/></xsl:message>
            <xsl:element name="name"><xsl:value-of select="$param_name"/></xsl:element>
            <xsl:for-each select="$requiredAttributes">
                <xsl:variable name="index" select="position()"/>
                <xsl:element name="{.}"><xsl:value-of select="$requiredAttributeValues[$index]"/></xsl:element> 
            </xsl:for-each>
        </param>
    </xsl:template>
    
    <xsl:template match="@name" mode="attr">
        <xsl:param name="param_name" select="."/>
        <xsl:element name="name"><xsl:value-of select="fn:lower-case($param_name)"/></xsl:element>
    </xsl:template>
    
    <xsl:template match="@*" mode="attr">
        <xsl:element name="{sos:normalize(name(.))}"><xsl:value-of select="."/></xsl:element>
    </xsl:template>
    
    <xsl:template match="@xsi:schemaLocation" mode="attr">
    </xsl:template>
    
    <xsl:function name="sos:normalize">
        <xsl:param name="arg"/>
        <xsl:value-of select="fn:lower-case(fn:replace($arg, '_', ''))"/>
    </xsl:function>
    
    <xsl:function name="sos:itemOf">
        <xsl:param name="item"/>
        <xsl:param name="list"/>
        <xsl:value-of select="contains(concat(',',$list,','),concat(',',fn:lower-case($item),','))"/>
    </xsl:function>
    
    <xsl:function name="sos:itemOfTargetSourceScope">
        <xsl:param name="item"/>
        <xsl:value-of select="sos:itemOf($item,$targetSourceScope)"/>
    </xsl:function>
    
    <xsl:function name="sos:itemOfTargetScope">
        <xsl:param name="item"/>
        <xsl:value-of select="sos:itemOf($item,$targetScope)"/>
    </xsl:function>
    
    <xsl:function name="sos:itemOfSourceScope">
        <xsl:param name="item"/>
        <xsl:value-of select="sos:itemOf($item,$sourceScope)"/>
    </xsl:function>
    
    <xsl:function name="sos:itemOfAlternative">
        <xsl:param name="item"/>
        <xsl:value-of select="contains(fn:lower-case($item),'alternative_')"/>
    </xsl:function>
    
</xsl:stylesheet>