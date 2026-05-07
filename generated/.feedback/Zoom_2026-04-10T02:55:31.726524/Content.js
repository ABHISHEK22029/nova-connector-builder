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
      "name": "ZOOM_MEETING_JSON_TO_XML",
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
          "defaultMessage": "Zoom Meeting data transformation phase 1 is complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "3"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Zoom Meeting data transformation phase 1 resulted in failure",
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
      "name": "ZOOM_MEETING_HTTP_COMPONENT",
      "retryOptions": {
        "maxRetry": 2,
        "delaySeconds": 5
      },
      "url": "sabaspel:headers.headers['ZOOM_API_URL'] + '/meetings'",
      "requestMethod": "GET",
      "headers": {
        "Accept-Encoding": [
          "gzip"
        ],
        "Accept": [
          "application/json"
        ],
        "Authorization": [
          "sabaspel:'Bearer ' + headers.headers['zoom_access_token']"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "1",
      "strictLoopCounterCheck": "true",
      "authenticationStrategy": {
        "type": "oauth2",
        "detail": {
          "clientId": "sabaspel:headers.headers['integrationDataFetchDetail'].accountConfigs['ZOOM_CLIENT_ID'][0]",
          "clientSecret": "sabaspel:headers.headers['integrationDataFetchDetail'].accountConfigs['ZOOM_CLIENT_SECRET'][0]",
          "accountId": "sabaspel:headers.headers['integrationDataFetchDetail'].accountConfigs['ZOOM_ACCOUNT_ID'][0]",
          "url": "sabaspel:headers.headers['ZOOM_API_URL'] + '/oauth/token'",
          "grantType": "sabaconst:account_credentials",
          "sendClientCredentialsInBody": "true"
        },
        "cachedHeaderProperty": "com.saba.zoom.meeting.oauth"
      },
      "flowMetadata": {
        "successFlowMonitoring": {
          "defaultMessage": "Meeting Fetch Complete",
          "status": "COMPLETE",
          "details": {
            "STEP_COMPLETED": "1"
          }
        },
        "errorFlowMonitoring": {
          "defaultMessage": "Meeting fetch resulted in failure",
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
      "name": "ZOOM_MEETING_XSLT",
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
    "start": "MONITORING_STATUS_CHECK",
    "end": "ZOOM_MEETING_XSLT",
    "nodes": {
      "MONITORING_STATUS_CHECK": {
        "success": "ZOOM_MEETING_HTTP_COMPONENT",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      "ZOOM_MEETING_HTTP_COMPONENT": {
        "success": "ZOOM_MEETING_JSON_TO_XML",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      "ZOOM_MEETING_JSON_TO_XML": {
        "success": "ZOOM_MEETING_XSLT",
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      "ZOOM_MEETING_XSLT": {
        "success": null,
        "failure": "MONITORING_CHECK_EXCEPTION"
      },
      "MONITORING_CHECK_EXCEPTION": {
        "success": null,
        "failure": null
      }
    }
  }
}