<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="/xslt/xml-to-string.xslt"/>

  <xsl:template match="/">
    <html xml:space="preserve">
      <head>
        <title>Top-level elements displayed</title>
        <script language="JavaScript">
          <xsl:comment>
            <xsl:apply-templates select="/*/*" mode="script"/>
            <xsl:text>&#xA;// </xsl:text>
          </xsl:comment>
        </script>
      </head>
      <body>
        <h2>Top-level elements, each as a stand-alone XML document</h2>
        <form name="myForm">
          <textarea name="xml" cols="75" rows="18"/>
          <br/>
          <select name="choice" onchange="on_change();">
            <xsl:apply-templates select="/*/*"/>
          </select>
        </form>
        <script language="JavaScript">
          <xsl:comment>
            function on_change() {
                document.myForm.xml.value = eval('e' + document.myForm.choice.selectedIndex);
            }

            if (document.myForm.xml.value == "")
                on_change();
          // </xsl:comment>
        </script>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="/*/*">
    <option>
      <xsl:value-of select="position()"/>
      <xsl:text>. </xsl:text>
      <xsl:value-of select="name()"/>
    </option>
  </xsl:template>

  <xsl:template match="/*/*" mode="script">
    <xsl:text>&#xA;var e</xsl:text>
    <xsl:value-of select="count(preceding-sibling::*)"/>
    <xsl:text> = '</xsl:text>
    <xsl:call-template name="escape-string">
      <xsl:with-param name="text">
        <xsl:call-template name="xml-to-string"/>
      </xsl:with-param>
    </xsl:call-template>
    <xsl:text>';</xsl:text>
  </xsl:template>

  <xsl:template name="escape-string">
    <xsl:param name="text"/>
    <xsl:variable name="slashesEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$text"/>
        <xsl:with-param name="replace" select="'\'"/>
        <xsl:with-param name="with" select="'\\'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="apostrophesEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$slashesEscaped"/>
        <xsl:with-param name="replace" select="&quot;'&quot;"/>
        <xsl:with-param name="with" select="&quot;\&apos;&quot;"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="newlinesEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$apostrophesEscaped"/>
        <xsl:with-param name="replace" select="'&#xA;'"/>
        <xsl:with-param name="with" select="'\n'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="escape-dash-dash">
      <xsl:with-param name="text" select="$newlinesEscaped"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="escape-dash-dash">
    <xsl:param name="text"/>
    <xsl:variable name="tempText">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$text"/>
        <xsl:with-param name="replace" select="'--'"/>
        <xsl:with-param name="with" select='"-&apos; + &apos;-"'/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="contains($tempText, '--')">
        <xsl:call-template name="escape-dash-dash">
          <xsl:with-param name="text" select="$tempText"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$tempText"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>