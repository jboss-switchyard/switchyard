<?xml version="1.0" encoding="UTF-8"?>
<!--
 - Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors.
 - 
 - Licensed under the Apache License, Version 2.0 (the "License");
 - you may not use this file except in compliance with the License.
 - You may obtain a copy of the License at
 - http://www.apache.org/licenses/LICENSE-2.0
 - Unless required by applicable law or agreed to in writing, software
 - distributed under the License is distributed on an "AS IS" BASIS,
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 - See the License for the specific language governing permissions and
 - limitations under the License.
 -->
<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:as="urn:jboss:domain:1.7">

<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>

<xsl:template match="@*|node()">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
</xsl:template>

<xsl:template match="as:server[@name='server-one' and @group='main-server-group']/@group">
    <xsl:attribute name="group">
        <xsl:value-of select="'other-server-group'"/>
    </xsl:attribute>
</xsl:template>

<xsl:template match="as:server[@name='server-two']/@group">
    <xsl:attribute name="group">
        <xsl:value-of select="'other-server-group'"/>
    </xsl:attribute>
</xsl:template>

</xsl:stylesheet>
