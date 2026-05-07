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
      "name": "UDEMY_COURSE_JSON_TO_XML",
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
          "defaultMessage": "Udemy Course data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "3"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Udemy Course data transformation phase 1 resulted in failure",
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
      "name": "UDEMY_COURSE_HTTP_COMPONENT",
      "retryOptions": {
        "maxRetry": 2,
        "delaySeconds": 5
      },
      "url": "sabaspel:headers.headers['UDEMY_CLIENT_URL'] + '/courses'",
      "requestMethod": "GET",
      "headers": {
        "Accept-Encoding": [
          "gzip"
        ],
        "Accept": [
          "application/json"
        ],
        "Authorization": [
          "sabaspel:'Bearer ' + headers.headers['UDEMY_ACCESS_TOKEN']"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "1",
      "strictLoopCounterCheck": "true",
      "authenticationStrategy": {
        "type": "none"
      },
      "flowMetadata": {
        "successFlowMonitoring": {
          "defaultMessage": "Course Data Fetch Complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "1"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Course data fetch resulted in failure",
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
      "name": "UDEMY_COURSE_JSON_TO_XML",
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
          "defaultMessage": "Course data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "2"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Course data transformation phase 1 resulted in failure",
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
      "name": "UDEMY_COURSE_XML_MERGER",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      },
      "outputType": "FILE"
    },
    {
      "type": "xslt",
      "name": "UDEMY_COURSE_XSLT",
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
      "appendMode": true
    }
  ],
  "flow": {
    "name": "udemy.course.data.fetch",
    "startComponentName": "MONITORING_STATUS_CHECK",
    "endComponentName": "UDEMY_COURSE_XSLT",
    "nodes": [
      {
        "componentName": "MONITORING_STATUS_CHECK",
        "success": "UDEMY_COURSE_HTTP_COMPONENT",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDEMY_COURSE_HTTP_COMPONENT",
        "success": "UDEMY_COURSE_JSON_TO_XML",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDEMY_COURSE_JSON_TO_XML",
        "success": "UDEMY_COURSE_XML_MERGER",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDEMY_COURSE_XML_MERGER",
        "success": "UDEMY_COURSE_XSLT",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      {
        "componentName": "UDEMY_COURSE_XSLT",
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