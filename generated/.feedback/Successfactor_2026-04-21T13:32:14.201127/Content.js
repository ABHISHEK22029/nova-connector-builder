// Content.js
module.exports = {
  "name": "Get Users Flow",
  "description": "Orchestrates fetching users from SuccessFactors, transforming the data, and handling errors.",
  "startComponentId": "httpFetchUsers",
  "components": [
    {
      "id": "httpFetchUsers",
      "name": "Fetch Users from SuccessFactors",
      "type": "http-request",
      "settings": {
        "url": "${configuration.apiBaseUrl}/odata/v2/User?$format=json",
        "method": "GET",
        "headers": {
          "Content-Type": "application/json",
          "Authorization": "Basic ${authentication.encodedCredentials}"
        },
        "timeout": 60000
      },
      "onSuccess": "jsonToXmlUsers",
      "onFailure": "handleError"
    },
    {
      "id": "jsonToXmlUsers",
      "name": "Convert JSON to XML",
      "type": "json-to-xml",
      "settings": {
        "rootName": "users",
        "elementName": "user"
      },
      "onSuccess": "xsltTransformUsers",
      "onFailure": "handleError"
    },
    {
      "id": "xsltTransformUsers",
      "name": "Transform XML using XSLT",
      "type": "xslt-transform",
      "settings": {
        "xsltTemplate": "${configuration.xsltTemplate}"
      },
      "onSuccess": "successHandler",
      "onFailure": "handleError"
    },
    {
      "id": "successHandler",
      "name": "Success Handler",
      "type": "script",
      "settings": {
        "script": "console.log('Successfully processed users.'); context.setVariable('success', true);"
      }
    },
    {
      "id": "handleError",
      "name": "Error Handler",
      "type": "script",
      "settings": {
        "script": "console.error('Error during user processing:', context.error); context.setVariable('success', false);"
      }
    }
  ],
  "routing": {
    "httpFetchUsers": {
      "onSuccess": "jsonToXmlUsers",
      "onFailure": "handleError"
    },
    "jsonToXmlUsers": {
      "onSuccess": "xsltTransformUsers",
      "onFailure": "handleError"
    },
    "xsltTransformUsers": {
      "onSuccess": "successHandler",
      "onFailure": "handleError"
    }
  },
  "monitoring": {
    "enabled": true,
    "metrics": [
      "flow.duration",
      "component.duration"
    ]
  }
};