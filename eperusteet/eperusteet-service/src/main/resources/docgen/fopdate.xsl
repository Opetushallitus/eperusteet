<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" 
                xmlns:dc="http://purl.org/dc/elements/1.1/" >

  <xsl:output method="xml"
	            encoding="UTF-8"
	            indent="yes"
	            omit-xml-declaration="yes"/>

  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dc:date">
      <dc:date>
        <rdf:Seq>
           <rdf:li>
                <xsl:apply-templates select="node()|@*"/>      
           </rdf:li>      
        </rdf:Seq>
      </dc:date>
  </xsl:template>

</xsl:stylesheet>
