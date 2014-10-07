<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:h="http://www.w3.org/1999/xhtml"
		xmlns:doc="http://docbook.org/ns/docbook"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:epdoc="urn:fi.vm.sade.eperusteet.docgen">

  <xsl:output method="xml"
	      encoding="UTF-8"
	      indent="yes"
	      omit-xml-declaration="no"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="h:p">
    <doc:para>
      <xsl:apply-templates select="node()|@*"/>
    </doc:para>
  </xsl:template>

  <xsl:template match="h:strong">
    <doc:emphasis role="strong">
      <xsl:apply-templates select="node()|@*"/>
    </doc:emphasis>
  </xsl:template>

  <xsl:template match="h:s">
    <doc:emphasis role="strikethrough">
      <xsl:apply-templates select="node()|@*"/>
    </doc:emphasis>
  </xsl:template>

  <xsl:template match="h:em">
    <doc:emphasis>
      <xsl:apply-templates select="node()|@*"/>
    </doc:emphasis>
  </xsl:template>

  <xsl:template match="h:h1 | h:h2 | h:h3 | h:h4 | h:h5 | h:h6">
    <doc:bridgehead>
      <xsl:apply-templates select="node()|@*"/>
    </doc:bridgehead>
  </xsl:template>

  <!-- ewwwww...  -->
  <xsl:template match="h:hr">
    <epdoc:hr></epdoc:hr>
  </xsl:template>

  <xsl:template match="h:ul">
    <doc:itemizedlist>
      <xsl:apply-templates select="node()|@*"/>
    </doc:itemizedlist>
  </xsl:template>

  <xsl:template match="h:ol">
    <doc:orderedlist>
      <xsl:apply-templates select="node()|@*"/>
    </doc:orderedlist>
  </xsl:template>

  <xsl:template match="h:li">
    <doc:listitem>
      <xsl:apply-templates select="node()|@*"/>
    </doc:listitem>
  </xsl:template>

  <!-- Something else? para?-->
  <xsl:template match="h:pre">
    <doc:literallayout>
      <doc:literal>
         <xsl:apply-templates select="node()|@*"/>
      </doc:literal>
    </doc:literallayout>
  </xsl:template>

  <xsl:template match="h:blockquote">
    <doc:blockquote>
      <xsl:apply-templates select="node()|@*"/>
    </doc:blockquote>
  </xsl:template>

  <!-- table elements should go with simple namespace switch -->
  <xsl:template match="h:table">
    <doc:table>
      <xsl:apply-templates select="node()|@*"/>
    </doc:table>
  </xsl:template>
  <xsl:template match="h:tbody">
    <doc:tbody>
      <xsl:apply-templates select="node()|@*"/>
    </doc:tbody>
  </xsl:template>
  <xsl:template match="h:tr">
    <doc:tr>
      <xsl:apply-templates select="node()|@*"/>
    </doc:tr>
  </xsl:template>
  <xsl:template match="h:td">
    <doc:td>
      <xsl:apply-templates select="node()|@*"/>
    </doc:td>
  </xsl:template>
  <xsl:template match="h:thead">
    <doc:thead>
      <xsl:apply-templates select="node()|@*"/>
    </doc:thead>
  </xsl:template>
  <xsl:template match="h:th">
    <doc:th>
      <xsl:apply-templates select="node()|@*"/>
    </doc:th>
  </xsl:template>
  <xsl:template match="h:caption">
    <doc:caption>
      <xsl:apply-templates select="node()|@*"/>
    </doc:caption>
  </xsl:template>
  
  <!-- links be funky:
    - ignore target attribute
    - footnote the href attribute as ulink url...
    - ...after outputting the link text   
  -->
  <xsl:template match="h:a">
      <xsl:apply-templates select="node()|@*"/>
      <doc:footnote>
          <doc:para>
            <doc:ulink url="{@href}">
              <xsl:value-of select="@href" />              
            </doc:ulink>
          </doc:para>
      </doc:footnote>
  </xsl:template>

</xsl:stylesheet>
