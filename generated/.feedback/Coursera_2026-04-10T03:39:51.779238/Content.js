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
      "type": "jsonToXml",
      "name": "COURSERA_CONTENT_JSON_TO_XML",
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
          "defaultMessage": "Coursera Content data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "3"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Coursera Content data transformation phase 1 resulted in failure",
          "details": {
            "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
            "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']"
          },
          "status": "FAILURE"
        }
      }
    },
    {
      "type": "http",
      "name": "COURSERA_CONTENT_HTTP_COMPONENT",
      "retryOptions": {
        "maxRetry": 2,
        "delaySeconds": 5
      },
      "url": "sabaspel:headers.headers['COURSERA_BASE_URL'] + '/api/opencatalog/v3/content'",
      "requestMethod": "GET",
      "headers": {
        "Accept-Encoding": [
          "gzip"
        ],
        "Accept": [
          "application/json"
        ],
        "Authorization": [
          "Bearer sabaspel:headers.headers['ACCESS_TOKEN']"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "1",
      "strictLoopCounterCheck": "true",
      "flowMetadata": {
        "successFlowMonitoring": {
          "defaultMessage": "Content Fetch Complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "1"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Content fetch resulted in failure",
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
      "name": "COURSERA_CONTENT_JSON_TO_XML_2",
      "outputType": "FILE",
      "eventProcessor": {
        "type": "xmlTagRename",
        "replacements": [
          {
            "source": {
              "namespaceURI": "",
              "localPart": "elements"
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
          "defaultMessage": "Coursera Content data transformation phase 2 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "2"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Coursera Content data transformation phase 2 resulted in failure",
          "details": {
            "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
            "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']"
          },
          "status": "FAILURE"
        }
      }
    },
    {
      "type": "xmlMerger",
      "name": "COURSERA_CONTENT_XML_MERGER",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      },
      "outputType": "FILE"
    },
    {
      "type": "xslt",
      "name": "COURSERA_CONTENT_XSLT",
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
        "includeHeadersForConfigMapping": "sabaspel:(#loopCounter ==0) ? 'true' : 'false' "
      },
      "appendMode": true
    }
  ],
  "flow": {
    "name": "coursera.content.data.fetch",
    "firstComponent": "MONITORING_STATUS_CHECK",
    "errorComponent": "MONITORING_CHECK_EXCEPTION",
    "componentFlows": {
      "MONITORING_STATUS_CHECK": {
        "SUCCESS": "COURSERA_CONTENT_HTTP_COMPONENT",
        "FAILURE": "MONITORING_CHECK_EXCEPTION"
      },
      "COURSERA_CONTENT_HTTP_COMPONENT": {
        "SUCCESS": "COURSERA_CONTENT_JSON_TO_XML_2",
        "FAILURE": "MONITORING_CHECK_EXCEPTION"
      },
      "COURSERA_CONTENT_JSON_TO_XML_2": {
        "SUCCESS": "COURSERA_CONTENT_XML_MERGER",
        "FAILURE": "MONITORING_CHECK_EXCEPTION"
      },
      "COURSERA_CONTENT_XML_MERGER": {
        "SUCCESS": "COURSERA_CONTENT_XSLT",
        "FAILURE": "MONITORING_CHECK_EXCEPTION"
      },
      "COURSERA_CONTENT_XSLT": {
        "SUCCESS": null,
        "FAILURE": "MONITORING_CHECK_EXCEPTION"
      },
      "MONITORING_CHECK_EXCEPTION": {
        "SUCCESS": null,
        "FAILURE": null
      }
    }
  }
}