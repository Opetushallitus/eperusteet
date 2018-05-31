<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:output method="xml"/>

    <xsl:template match="html">
        <fo:root>
            <fo:layout-master-set>
                <fo:simple-page-master master-name="blank"
                                       page-width="210mm" page-height="297mm"
                                       margin-top="25mm" margin-bottom="10mm"
                                       margin-left="25mm" margin-right="25mm">
                    <fo:region-body/>
                </fo:simple-page-master>
            </fo:layout-master-set>
            <fo:page-sequence master-reference="blank">
                <xsl:apply-templates select="body"/>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="body">
        <fo:flow flow-name="xsl-region-body">
            <xsl:choose>
                <xsl:when test="*|text()">
                    <xsl:apply-templates select="*|text()"/>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block font-size="12pt">
                        <xsl:text>Content is missing</xsl:text>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:flow>
    </xsl:template>

    <xsl:template match="h1">
        <fo:block font-size="14pt" line-height="1.25em" font-weight="bold"
                  keep-with-next="always" space-after="16pt">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="h2">
        <fo:block font-size="16pt" line-height="1.25em" font-weight="bold"
                  keep-with-next="always" space-after="10pt" color="#007EC5">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>


    <xsl:template match="h3">
        <fo:block font-size="14pt" line-height="1.25em" font-weight="bold"
                  keep-with-next="always" space-after="10pt" color="#007EC5">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="h4">
        <fo:block font-size="12pt" line-height="1.25em" font-weight="bold"
                  keep-with-next="always" space-after="10pt" color="#007EC5">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="h5">
        <fo:block font-size="12pt" line-height="1.25em" font-weight="bold"
                  keep-with-next="always" space-after="10pt" color="#007EC5">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="h6">
        <fo:block font-size="10pt" line-height="1.25em" font-weight="bold"
                  keep-with-next="always" space-after="10pt" color="#000000">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="a">
        <xsl:choose>
            <xsl:when test="@name">
                <xsl:if test="not(name(following-sibling::*[1])='h1')">
                    <fo:block line-height="0pt" space-after="0pt"
                              font-size="0pt" id="{@name}"/>
                </xsl:if>
            </xsl:when>
            <xsl:when test="@href and @href != ''">
                <fo:basic-link color="blue">
                    <xsl:attribute name="external-destination">
                        <xsl:value-of select="@href"/>
                    </xsl:attribute>
                    <xsl:apply-templates select="*|text()"/>
                </fo:basic-link>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="blockquote">
        <fo:block start-indent="1.5cm" end-indent="1.5cm"
                  space-after="12pt">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="br">
        <fo:block> </fo:block>
    </xsl:template>

    <xsl:template match="em">
        <fo:inline font-style="italic" >
            <xsl:apply-templates select="*|text()"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="hr">
        <fo:block>
            <fo:leader leader-pattern="rule" leader-length.maximum="100%" leader-length.optimum="100%"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="img">
        <fo:block space-after="12pt" text-align="center">
            <fo:table table-layout="fixed" width="100%">
                <fo:table-column column-width="proportional-column-width(1)"/>
                <fo:table-body>
                    <fo:table-row keep-with-next="always">
                        <fo:table-cell>
                            <fo:block>
                                <fo:external-graphic src="{@src}" content-width="scale-to-fit" content-height="100%"
                                                     width="100%" scaling="uniform"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block>
                                <xsl:value-of select="@alt"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </fo:block>
    </xsl:template>

    <xsl:template match="ol">
        <xsl:if test="li">
            <fo:list-block provisional-distance-between-starts="1cm"
                           provisional-label-separation="0.5cm">
                <xsl:attribute name="space-after">
                    <xsl:choose>
                        <xsl:when test="ancestor::ul or ancestor::ol">
                            <xsl:text>0pt</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>10pt</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="start-indent">
                    <xsl:variable name="ancestors">
                        <xsl:choose>
                            <xsl:when test="count(ancestor::ol) or boolean(count(ancestor::ul))">
                                <xsl:value-of select="1 +
                                    (count(ancestor::ol) +
                                     count(ancestor::ul)) *
                                    1.25"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>1</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:value-of select="concat($ancestors, 'cm')"/>
                </xsl:attribute>
                <xsl:apply-templates select="*"/>
            </fo:list-block>
        </xsl:if>
    </xsl:template>


    <xsl:template match="ol/li">
        <fo:list-item>
            <fo:list-item-label end-indent="label-end()">
                <fo:block>
                    <xsl:variable name="value-attr">
                        <xsl:choose>
                            <xsl:when test="../@start">
                                <xsl:number value="position() + number(../@start) - 1"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:number value="position()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="../@type='i'">
                            <xsl:number value="$value-attr" format="i. "/>
                        </xsl:when>
                        <xsl:when test="../@type='I'">
                            <xsl:number value="$value-attr" format="I. "/>
                        </xsl:when>
                        <xsl:when test="../@type='a'">
                            <xsl:number value="$value-attr" format="a. "/>
                        </xsl:when>
                        <xsl:when test="../@type='A'">
                            <xsl:number value="$value-attr" format="A. "/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:number value="$value-attr" format="1. "/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>
                    <xsl:apply-templates select="*|text()"/>
                </fo:block>
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>

    <xsl:template match="p">
        <fo:block space-after="0.75em">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="small">
        <fo:block font-size="8pt">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="div">
        <fo:block font-size="10pt" line-height="1.25em" space-after="20pt">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="article">
        <fo:block font-size="10pt" line-height="1.25em" space-after="20pt" text-align="justify">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="span">
        <fo:block font-size="10pt" line-height="1.25em">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="abbr">

        <xsl:value-of select="."/>

        <!-- Show endnotes bottom of the page -->
        <fo:footnote>
            <fo:inline baseline-shift="4pt" font-size="8pt">
                <xsl:value-of select="@number"/>
            </fo:inline>
            <fo:footnote-body>
                <fo:block font-size="8pt" line-height="10pt" start-indent="0" text-align="left" color="black">

                    <fo:table table-layout="fixed" width="100%">
                        <fo:table-column column-width="10mm"/>
                        <fo:table-column column-width="proportional-column-width(1)"/>
                        <fo:table-body>
                            <fo:table-row>
                                <fo:table-cell>
                                    <fo:block text-align="center">
                                        <xsl:value-of select="@number"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell>
                                    <fo:block>
                                        <xsl:value-of select="@text"/>
                                    </fo:block>
                                </fo:table-cell>
                            </fo:table-row>
                        </fo:table-body>
                    </fo:table>

                </fo:block>
            </fo:footnote-body>
        </fo:footnote>
    </xsl:template>

    <xsl:template match="pre">
        <fo:block font-family="monospace"
                  white-space-collapse="false" wrap-option="no-wrap"
                  linefeed-treatment="preserve" white-space-treatment="preserve">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="strong">
        <fo:inline font-weight="bold">
            <xsl:apply-templates select="*|text()"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="table">
        <fo:table table-layout="fixed" inline-progression-dimension="100%"
                  space-after="12pt" font-size="10pt"
                  page-break-inside="avoid" keep-together.within-column="1">
            <xsl:if test="caption">
                <fo:table-header>
                    <fo:table-cell>
                        <fo:block font-weight="bold" font-size="12pt" text-align="center">
                            <xsl:value-of select="caption"/>
                        </fo:block>
                    </fo:table-cell>
                </fo:table-header>
            </xsl:if>
            <fo:table-body>
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block/>
                    </fo:table-cell>
                </fo:table-row>
                <xsl:apply-templates select="thead|tbody|tr"/>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="td">
        <fo:table-cell
                padding-start="3pt" padding-end="3pt"
                padding-before="3pt" padding-after="3pt">
            <!-- FOP-2434 -->
            <xsl:if test="@colspan">
                <xsl:attribute name="number-columns-spanned">
                    <xsl:value-of select="@colspan"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@rowspan">
                <xsl:attribute name="number-rows-spanned">
                    <xsl:value-of select="@rowspan"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@border='1' or
                    ancestor::tr[@border='1'] or
                    ancestor::tbody[@border='1'] or
                    ancestor::thead[@border='1'] or
                    ancestor::table[@border='1']">
                <xsl:attribute name="border-style">
                    <xsl:text>solid</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="border-color">
                    <xsl:text>black</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="border-width">
                    <xsl:text>1pt</xsl:text>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@class">
                <xsl:attribute name="padding-start">
                    <xsl:choose>
                        <xsl:when test="@class='td1'">
                            <xsl:text>3pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='td2'">
                            <xsl:text>13pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='td3'">
                            <xsl:text>23pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='td4'">
                            <xsl:text>33pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='td5'">
                            <xsl:text>43pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='td6'">
                            <xsl:text>53pt</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>63pt</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:variable name="align">
                <xsl:choose>
                    <xsl:when test="@align">
                        <xsl:choose>
                            <xsl:when test="@align='center'">
                                <xsl:text>center</xsl:text>
                            </xsl:when>
                            <xsl:when test="@align='right'">
                                <xsl:text>end</xsl:text>
                            </xsl:when>
                            <xsl:when test="@align='justify'">
                                <xsl:text>justify</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>start</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="ancestor::tr[@align]">
                        <xsl:choose>
                            <xsl:when test="ancestor::tr/@align='center'">
                                <xsl:text>center</xsl:text>
                            </xsl:when>
                            <xsl:when test="ancestor::tr/@align='right'">
                                <xsl:text>end</xsl:text>
                            </xsl:when>
                            <xsl:when test="ancestor::tr/@align='justify'">
                                <xsl:text>justify</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>start</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="ancestor::thead">
                        <xsl:text>center</xsl:text>
                    </xsl:when>
                    <xsl:when test="ancestor::table[@align]">
                        <xsl:choose>
                            <xsl:when test="ancestor::table/@align='center'">
                                <xsl:text>center</xsl:text>
                            </xsl:when>
                            <xsl:when test="ancestor::table/@align='right'">
                                <xsl:text>end</xsl:text>
                            </xsl:when>
                            <xsl:when test="ancestor::table/@align='justify'">
                                <xsl:text>justify</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>start</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>start</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <fo:block text-align="{$align}">
                <xsl:apply-templates select="*|text()"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="th">
        <fo:table-cell padding-start="3pt" padding-end="3pt"
                       padding-before="3pt" padding-after="3pt">
            <!-- FOP-2434 -->
            <xsl:if test="@colspan">
                <xsl:attribute name="number-columns-spanned">
                    <xsl:value-of select="@colspan"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@rowspan">
                <xsl:attribute name="number-rows-spanned">
                    <xsl:value-of select="@rowspan"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@border='1' or
                    ancestor::tr[@border='1'] or
                    ancestor::tbody[@border='1'] or
                    ancestor::thead[@border='1'] or
                    ancestor::table[@border='1']">
                <xsl:attribute name="border-style">
                    <xsl:text>solid</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="border-color">
                    <xsl:text>black</xsl:text>
                </xsl:attribute>
                <xsl:attribute name="border-width">
                    <xsl:text>1pt</xsl:text>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="@class">
                <xsl:attribute name="padding-start">
                    <xsl:choose>
                        <xsl:when test="@class='th1'">
                            <xsl:text>3pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='th2'">
                            <xsl:text>13pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='th3'">
                            <xsl:text>23pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='th4'">
                            <xsl:text>33pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='th5'">
                            <xsl:text>43pt</xsl:text>
                        </xsl:when>
                        <xsl:when test="@class='th6'">
                            <xsl:text>53pt</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>63pt</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <fo:block text-align="center">
                <xsl:apply-templates select="*|text()"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="thead">
        <xsl:apply-templates select="tr"/>
    </xsl:template>

    <xsl:template match="tbody">
        <xsl:apply-templates select="tr"/>
    </xsl:template>

    <xsl:template match="tr">
        <fo:table-row>
            <xsl:if test="@bgcolor">
                <xsl:attribute name="background-color">
                    <xsl:value-of select="@bgcolor"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="th|td">
                    <xsl:apply-templates select="th|td"/>
                </xsl:when>
                <xsl:otherwise>
                    <fo:table-cell>
                        <fo:block/>
                    </fo:table-cell>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="ul">
        <xsl:if test="li">
            <fo:list-block provisional-distance-between-starts="0.5cm"
                           provisional-label-separation="0.25cm">
                <xsl:attribute name="space-after">
                    <xsl:choose>
                        <xsl:when test="ancestor::ul or ancestor::ol">
                            <xsl:text>0pt</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>12pt</xsl:text>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
                <xsl:attribute name="start-indent">
                    <xsl:variable name="ancestors">
                        <xsl:choose>
                            <xsl:when test="count(ancestor::ol) or boolean(count(ancestor::ul))">
                                <xsl:value-of select="0.5 +
                                        (count(ancestor::ol) +
                                         count(ancestor::ul)) *
                                        0.75"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>0.5</xsl:text>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:value-of select="concat($ancestors, 'cm')"/>
                </xsl:attribute>
                <xsl:apply-templates select="*"/>
            </fo:list-block>
        </xsl:if>
    </xsl:template>

    <xsl:template match="ul/li">
        <fo:list-item>
            <fo:list-item-label end-indent="label-end()">
                <fo:block>&#x2022;</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block>
                    <xsl:apply-templates select="*|text()"/>
                </fo:block>
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>

    <xsl:template match="cite">
        <fo:block color="#444444" font-style="italic" font-size="10pt"
                  space-after="20pt" text-align="justify" line-height="1.25em">
            <xsl:apply-templates select="*|text()"/>
        </fo:block>
    </xsl:template>

</xsl:stylesheet>
