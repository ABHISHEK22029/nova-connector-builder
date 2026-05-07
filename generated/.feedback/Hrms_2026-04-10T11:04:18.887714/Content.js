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
      "name": "HRMS_EMPLOYEE_JSON_TO_XML",
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
          "defaultMessage": "Employee data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "3"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Employee data transformation phase 1 resulted in failure",
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
      "name": "HRMS_EMPLOYEE_HTTP_COMPONENT",
      "retryOptions": {
        "maxRetry": 2,
        "delaySeconds": 5
      },
      "url": "sabaspel:headers.headers['HRMS_CLIENT_URL'] + '/employees'",
      "requestMethod": "GET",
      "headers": {
        "Accept-Encoding": [
          "gzip"
        ],
        "Accept": [
          "application/json"
        ],
        "X-API-Key": [
          "sabaspel:headers.headers['integrationDataFetchDetail'].accountConfigs['API_KEY'][0]"
        ],
        "Company-Id": [
          "sabaspel:headers.headers['integrationDataFetchDetail'].accountConfigs['COMPANY_ID'][0]"
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
          "defaultMessage": "Employee Data Fetch Complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "1"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Employee data fetch resulted in failure",
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
      "name": "HRMS_EMPLOYEE_CORE_JSON_TO_XML",
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
          "defaultMessage": "Employee data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "2"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Employee Core data transformation phase 1 resulted in failure",
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
      "name": "HRMS_EMPLOYEE_XML_MERGER",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      },
      "outputType": "FILE"
    },
    {
      "type": "xslt",
      "name": "HRMS_EMPLOYEE_XSLT",
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
    "HRMS_EMPLOYEE_HTTP_COMPONENT": {
      "success": "HRMS_EMPLOYEE_CORE_JSON_TO_XML",
      "failure": "MONITORING_CHECK_EXCEPTION"
    },
    "HRMS_EMPLOYEE_CORE_JSON_TO_XML": {
      "success": "HRMS_EMPLOYEE_XML_MERGER",
      "failure": "MONITORING_CHECK_EXCEPTION"
    },
    "HRMS_EMPLOYEE_XML_MERGER": {
      "success": "HRMS_EMPLOYEE_XSLT",
      "failure": "MONITORING_CHECK_EXCEPTION"
    },
    "HRMS_EMPLOYEE_XSLT": {
      "success": null,
      "failure": "MONITORING_CHECK_EXCEPTION"
    },
    "MONITORING_STATUS_CHECK": {
      "success": "HRMS_EMPLOYEE_HTTP_COMPONENT",
      "failure": "MONITORING_CHECK_EXCEPTION"
    },
    "MONITORING_CHECK_EXCEPTION": {
      "success": null,
      "failure": null
    }
  },
  "startComponent": "MONITORING_STATUS_CHECK"
}