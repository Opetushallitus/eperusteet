<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:fo="http://www.w3.org/1999/XSL/Format" 
                xmlns="http://docbook.org/ns/docbook"
                xmlns:doc="http://docbook.org/ns/docbook"
                xmlns:epdoc="urn:fi.vm.sade.eperusteet.docgen">

    <!-- this will process elements from doc ns to fo... -->
    <xsl:import href="res:xsl/docbook/fo/docbook.xsl" />
  
    <!-- ...and because there is no hr equivalent in docbook, let's 
    convert that from epdoc ns -->
    <xsl:template match="epdoc:hr">
        <fo:block border-top-style="solid" text-align="center"></fo:block>
    </xsl:template>

    <!-- From: http://www.sagehill.net/docbookxsl/CustomGentext.html
    Defines an XSL parameter named local.l10n.xml. The select attribute that
    provides the content of the parameter performs a neat trick. The XSL 
    document() function normally opens and reads another file. But the blank
    function argument is a special case, which means to read the current 
    document, that is, the current XSL file. This loads your entire 
    customization layer file into the parameter. Once loaded, specific
    instances of generated text can be extracted as needed.-->
    <xsl:param name="local.l10n.xml" select="document('')"/>
  
    <!-- Section numbering to use arabic numbers -->
    <xsl:param name="section.autolabel" select="1"></xsl:param>
  
    <!-- Section numbers to include chapter number -->
    <xsl:param name="section.label.includes.component.label" select="1"></xsl:param>
  
    <!-- Only include two levels below chapter table of contents -->
    <xsl:param name="toc.section.depth" select="2"></xsl:param>
  
    <!-- Only include the main toc (no list of figures, tables etc -->
    <xsl:param name="generate.toc">
        book    toc,title
    </xsl:param>

    <!-- body font size in pt -->
    <xsl:param name="body.font.master" select="12"></xsl:param>

    <!-- only numbrer header titles up to level 3 -->
    <xsl:param name="section.autolabel.max.depth" select="3"></xsl:param>

    <xsl:attribute-set name="section.title.level1.properties">
        <xsl:attribute name="font-size">
            <!--<xsl:value-of select="$body.font.master * 2.0736"></xsl:value-of>-->
            <xsl:value-of select="21"></xsl:value-of>            
            <xsl:text>pt</xsl:text>
        </xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="section.title.level2.properties">
        <xsl:attribute name="font-size">
            <xsl:value-of select="18"></xsl:value-of>
            <!--<xsl:value-of select="$body.font.master * 1.728"></xsl:value-of>-->
            <xsl:text>pt</xsl:text>
        </xsl:attribute>
        
    </xsl:attribute-set>

    <xsl:attribute-set name="section.title.level3.properties">
        <xsl:attribute name="font-size">
            <!--<xsl:value-of select="$body.font.master * 1.44"></xsl:value-of>-->
            <xsl:value-of select="16"></xsl:value-of>
            <xsl:text>pt</xsl:text>
        </xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="section.title.level4.properties">
        <xsl:attribute name="font-size">
            <xsl:value-of select="14"></xsl:value-of>
            <!--<xsl:value-of select="$body.font.master * 1.2"></xsl:value-of>-->
            <xsl:text>pt</xsl:text>
        </xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="section.title.level5.properties">
        <xsl:attribute name="font-size">
            <xsl:value-of select="$body.font.master"></xsl:value-of>
            <xsl:text>pt</xsl:text>
        </xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="section.title.level6.properties">
        <xsl:attribute name="font-size">
            <xsl:value-of select="$body.font.master"></xsl:value-of>
            <xsl:text>pt</xsl:text>
        </xsl:attribute>
    </xsl:attribute-set>

    <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
        <l:l10n language="fi">
      
            <l:context name="xref-number-and-title">
                <l:template name="chapter" text="%n. %t"/>
            </l:context>    

            <l:context name="xref-number">
                <l:template name="chapter" text="%n"/>
            </l:context>    

            <l:context name="title-numbered">
                <l:template name="chapter" text="%n. %t"/>
            </l:context>   

            <l:context name="title">
                <l:template name="chapter" text="%n. %t"/>
            </l:context>    

            <l:context name="title-unnumbered">
                <l:template name="chapter" text="%t"/>
            </l:context>

        </l:l10n>
    </l:i18n>
</xsl:stylesheet>
