{
  "components": [
    {
      "type": "monitoringStatusCheck",
      "name": "MONITORING_STATUS_CHECK"
    },
    {
      "type": "flowMonitoring",
      "name": "MONITORING_CHECK_EXCEPTION",
      "detail": {
        "defaultMessage": "An error occurred ",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "3"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "http",
      "name": "UDACITY_CATALOG_HTTP_COMPONENT",
      "retryOptions": {
        "maxRetry": 2,
        "delaySeconds": 5
      },
      "url": "sabaspel:headers.headers['UDACITY_API_URL'] + '/catalogs'",
      "requestMethod": "GET",
      "headers": {
        "Accept-Encoding": [
          "gzip"
        ],
        "Accept": [
          "application/json"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "1",
      "strictLoopCounterCheck": "true",
      "authenticationStrategy": {
        "type": "header",
        "headerName": "Authorization",
        "headerValue": "sabaspel:'Bearer ' + headers.headers['UDACITY_API_KEY']"
      },
      "flowMetadata": {
        "successFlowMonitoring": {
          "defaultMessage": "Udacity Catalog Fetch Complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "1"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Udacity Catalog fetch resulted in failure",
          "details": {
            "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
            "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']"
          },
          "status": "FAILURE"
        }
      }
    },
    {
      "type": "jsonToXml",
      "name": "UDACITY_CATALOG_JSON_TO_XML",
      "outputType": "FILE",
      "eventProcessor": {
        "type": "xmlTagRename",
        "replacements": [
          {
            "source": {
              "namespaceURI": "",
              "localPart": "value"
            },
            "replacement": {
              "namespaceURI": "",
              "localPart": "Record"
            },
            "depth": 2
          }
        ]
      },
      "flowMetadata": {
        "successFlowMonitoring": {
          "defaultMessage": "Udacity Catalog data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "2"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Udacity Catalog data transformation phase 1 resulted in failure",
          "details": {
            "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
            "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']"
          },
          "status": "FAILURE"
        }
      }
    },
    {
      "type": "xslt",
      "name": "UDACITY_CATALOG_XSLT",
      "xmlSplitConfig": {
        "qName": {
          "namespaceURI": "",
          "localPart": "Record"
        },
        "recordsPerFile": 50,
        "includeFirstFileWithCommonEvents": true
      },
      "outputType": "FILE",
      "xslFilePath": "sabaspel:headers.headers['integrationDataFetchDetail'].xslFilePath",
      "properties": {
        "delimiter": "sabaspel:headers.headers['isPreview']==true ? '|' : #getConfValueOrDefault(headers.headers['integrationDataFetchDetail'].entityConfigs ,'READ_CSV_FILE_DELIMITER','|')",
        "includeHeadersForConfigMapping": "sabaspel:(#loopCounter ==0) ? 'true' : 'false' ",
        "scheduleStartDateTimeFilter": "sabaspel: headers.headers['com.saba.integration.process.start']",
        "scheduleEndDateTimeFilter": "sabaspel: headers.headers['com.saba.integration.process.lastUpdated']"
      },
      "appendMode": true,
      "flowMetadata": {
        "successFlowMonitoring": {
          "defaultMessage": "Udacity Catalog XSLT transformation is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "3"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Udacity Catalog XSLT transformation resulted in failure",
          "details": {
            "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
            "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']"
          },
          "status": "FAILURE"
        }
      }
    }
  ],
  "flow": {
    "name": "udacity.catalog.data.fetch",
    "startComponentName": "MONITORING_STATUS_CHECK",
    "endComponentName": "UDACITY_CATALOG_XSLT",
    "nodes": [
      {
        "componentName": "MONITORING_STATUS_CHECK",
        "success": "UDACITY_CATALOG_HTTP_COMPONENT",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDACITY_CATALOG_HTTP_COMPONENT",
        "success": "UDACITY_CATALOG_JSON_TO_XML",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDACITY_CATALOG_JSON_TO_XML",
        "success": "UDACITY_CATALOG_XSLT",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDACITY_CATALOG_XSLT",
        "success": null,
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "MONITORING_CHECK_EXCEPTION",
        "success": null,
        "failure": null
      }
    ]
  }
}