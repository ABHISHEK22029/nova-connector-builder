var contentFlow = {
  "id": "integ-4a9bb7789c454a49994059d3a4c7429b",
  "name": "Udemy Content Flow",
  "description": "Flow to retrieve and transform Udemy content data.",
  "startNodeId": "fetchContent",
  "endNodeId": "end",
  "errorNodeId": "error",
  "components": [
    {
      "id": "fetchContent",
      "name": "Fetch Content from Udemy API",
      "type": "http-fetch",
      "config": {
        "url": "${configuration['baseUrl']}/courses/?page_size=100",
        "method": "GET",
        "headers": {
          "Authorization": "Basic ${T(java.util.Base64).getEncoder().encodeToString(('${configuration['clientId']}:${configuration['clientSecret']}').bytes)}"
        },
        "output": "response"
      },
      "successNodeId": "jsonToXml",
      "failNodeId": "error"
    },
    {
      "id": "jsonToXml",
      "name": "Convert JSON to XML",
      "type": "json-to-xml",
      "config": {
        "jsonField": "response.body",
        "xmlField": "xmlData"
      },
      "successNodeId": "xsltTransform",
      "failNodeId": "error"
    },
    {
      "id": "xsltTransform",
      "name": "Transform XML using XSLT",
      "type": "xslt-transform",
      "config": {
        "xmlField": "xmlData",
        "xslt": "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n  <xsl:output method=\"xml\" indent=\"yes\"/>\n  <xsl:template match=\"/\">\n    <courses>\n      <xsl:for-each select=\"json/results/object\">\n        <course>\n          <id><xsl:value-of select=\"id\"/></id>\n          <title><xsl:value-of select=\"title\"/></title>\n          <url><xsl:value-of select=\"url\"/></url>\n          <is_paid><xsl:value-of select=\"is_paid\"/></is_paid>\n          <price><xsl:value-of select=\"price\"/></price>\n          <num_subscribers><xsl:value-of select=\"num_subscribers\"/></num_subscribers>\n          <num_reviews><xsl:value-of select=\"num_reviews\"/></num_reviews>\n          <created><xsl:value-of select=\"created\"/></created>\n          <last_update_date><xsl:value-of select=\"last_update_date\"/></last_update_date>\n        </course>\n      </xsl:for-each>\n    </courses>\n  </xsl:template>\n</xsl:stylesheet>",
        "outputField": "transformedData"
      },
      "successNodeId": "end",
      "failNodeId": "error"
    },
    {
      "id": "end",
      "name": "End",
      "type": "end",
      "config": {}
    },
    {
      "id": "error",
      "name": "Error",
      "type": "error",
      "config": {}
    }
  ],
  "errorHandling": {
    "defaultErrorNodeId": "error"
  },
  "monitoring": {
    "enabled": true,
    "metrics": [
      "flow.duration",
      "component.duration"
    ]
  }
};