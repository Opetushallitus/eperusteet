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

  <xsl:param name="local.l10n.xml" select="document('')"/>
  <xsl:param name="section.autolabel" select="1"></xsl:param>
  <xsl:param name="section.label.includes.component.label" select="1"></xsl:param>
  <xsl:param name="toc.section.depth" select="1"></xsl:param>  

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
