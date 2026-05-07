{
  "components": [
    {
      "type": "updateHeader",
      "name": "Business_Update_Headers",
      "properties": {
        "BUSINESS_OBJECT_TYPE": "sabaspel:headers.headers['flow_bean_name'] == 'integration.business.import.content' ? sabaconst:com.saba.integration.apps.business.BusinessConstants.OBJECT_CONTENT : sabaconst:com.saba.integration.apps.business.BusinessConstants.OBJECT_USER",
        "LAST_SYNC_TIMESTAMP": "sabaspel:headers.headers['flow_bean_name'] == 'integration.business.import.content' ? payload.accountConfigs[sabaconst:com.saba.integration.apps.business.BusinessConstants.CONTENT_LAST_UPDATED][0] : payload.accountConfigs[sabaconst:com.saba.integration.apps.business.BusinessConstants.USER_LAST_UPDATED][0]"
      }
    },
    {
      "type": "http",
      "name": "Business_Http_Component",
      "control": {
        "type": "businessComponentControl",
        "url": "sabaspel:payload.accountConfigs[sabaconst:com.saba.integration.apps.business.BusinessConstants.CONFIG_BASE_URL][0] + sabaconst:com.saba.integration.apps.business.BusinessConstants.API_BASE_PATH + (headers.headers['BUSINESS_OBJECT_TYPE'] == sabaconst:com.saba.integration.apps.business.BusinessConstants.OBJECT_CONTENT ? sabaconst:com.saba.integration.apps.business.BusinessConstants.ENDPOINT_CONTENT : sabaconst:com.saba.integration.apps.business.BusinessConstants.ENDPOINT_USERS) + '?' + sabaconst:com.saba.integration.apps.business.BusinessConstants.PARAM_PAGE + '=' + #currentPageNumber + '&' + sabaconst:com.saba.integration.apps.business.BusinessConstants.PARAM_PAGE_SIZE + '=' + (headers.headers['isPreview']==true ? sabaconst:com.saba.integration.apps.business.BusinessConstants.PREVIEW_RECORD_LIMIT : payload.accountConfigs[sabaconst:com.saba.integration.apps.business.BusinessConstants.CONFIG_PAGE_SIZE][0]) + '&' + sabaconst:com.saba.integration.apps.business.BusinessConstants.PARAM_UPDATED_SINCE + '=' + #toEpoch(headers.headers['LAST_SYNC_TIMESTAMP'], 'yyyy-MM-dd HH:mm:ss')"
      },
      "requestMethod": "GET",
      "headers": {
        "Accept_Encoding": [
          "gzip"
        ],
        "X-API-Key": "sabaspel:payload.accountConfigs[sabaconst:com.saba.integration.apps.business.BusinessConstants.CONFIG_API_KEY][0]"
      },
      "outputType": "FILE",
      "maxLoopCounter": "sabaspel:headers.headers['isPreview']==true ? '1' : '-1'",
      "responseValidator": {
        "type": "noMoreRecords",
        "recordQName": {
          "namespaceURI": "",
          "localPart": "data"
        },
        "requiredDepth": 2,
        "json": true
      },
      "contextDTO": {
        "ownerId": "sabaspel:headers.headers['FLOW_OWNER_ID']",
        "ownerType": "MONITORING",
        "properties": {
          "STATUS": "IN_PROGRESS",
          "STEP_COMPLETED": "2",
          "IDENTIFIER": ""
        }
      },
      "retryOptions": {
        "maxRetry": 3,
        "delaySeconds": 5
      },
      "dataPrefix": "{\"Records\":",
      "dataSuffix": "}",
      "jsonforStax": "true"
    },
    {
      "type": "httpResponseExtractor",
      "name": "Business_Record_Validator",
      "isJson": "true",
      "requiredDepth": "2",
      "namespace": "",
      "localPart": "data",
      "removeInputFiles": "false"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Business_Has_Records_Filter",
      "accept": "sabaspel:'true'==headers.headers['HAS_RECORDS']"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Business_Has_No_Records_Filter",
      "accept": "sabaspel:'false'==headers.headers['HAS_RECORDS']"
    },
    {
      "type": "flowMonitoring",
      "name": "Business_No_Records_Monitoring",
      "detail": {
        "defaultMessage": "No new or updated records found for Business {OBJECT_TYPE}",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        }
      }
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Business_No_Records_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "COMPLETE"
    },
    {
      "type": "filePathProcessor",
      "name": "Business_File_Processor",
      "delimiter": "_",
      "filePathEntries": {
        "com.saba.jsontoxml.outputfile": "sabaspel:headers.headers['isPreview']==true ? integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + headers.headers['BUSINESS_OBJECT_TYPE'] + '_json.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID'] + '_' + headers.headers['BUSINESS_OBJECT_TYPE'] + '_json.xml'",
        "com.saba.xslt.output.filepath": "sabaspel:headers.headers['isPreview']==true ? integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + headers.headers['BUSINESS_OBJECT_TYPE'] + '_transformed.csv' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID'] + '_' + headers.headers['BUSINESS_OBJECT_TYPE'] + '_transformed.csv'",
        "com.saba.xml.split.output.filepath": "sabaspel:headers.headers['isPreview']==true ? integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + headers.headers['BUSINESS_OBJECT_TYPE'] + '_split.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID'] + '_' + headers.headers['BUSINESS_OBJECT_TYPE'] + '_split.xml'"
      }
    },
    {
      "type": "jsonToXml",
      "name": "Business_JsonToXml",
      "eventProcessor": {
        "type": "xmlTagRename",
        "replacements": [
          {
            "source": {
              "namespaceURI": "",
              "localPart": "data"
            },
            "replacement": {
              "namespaceURI": "",
              "localPart": "Record"
            },
            "depth": 2
          }
        ]
      },
      "outputType": "FILE"
    },
    {
      "type": "xsltTransformerProperty",
      "name": "Business_Custom_Xslt_Property"
    },
    {
      "type": "xslt",
      "name": "Business_Xslt",
      "xmlSplitConfig": {
        "qName": {
          "namespaceURI": "",
          "localPart": "Record"
        },
        "recordsPerFile": 400,
        "includeFirstFileWithCommonEvents": true
      },
      "outputType": "FILE",
      "xslFilePath": "sabaspel:headers.headers['integrationDataFetchDetail'].xslFilePath",
      "properties": {
        "includeHeadersForConfigMapping": "sabaspel:(#loopCounter ==0) ? 'true' : 'false' "
      },
      "appendMode": true
    },
    {
      "type": "fileNameRename",
      "name": "Business_FileNameRename",
      "fileName": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE'] == sabaconst:com.saba.integration.apps.business.BusinessConstants.OBJECT_CONTENT ? 'BusinessContent' : 'BusinessUser'",
      "suffix": ".csv"
    },
    {
      "type": "entityConfigUpdater",
      "name": "Business_EntityConfigUpdater",
      "entityId": "sabaspel:headers.headers['integrationDataFetchDetail'].entityDetail.id",
      "configs": [
        {
          "code": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE'] == sabaconst:com.saba.integration.apps.business.BusinessConstants.OBJECT_CONTENT ? sabaconst:com.saba.integration.apps.business.BusinessConstants.CONTENT_LAST_UPDATED : sabaconst:com.saba.integration.apps.business.BusinessConstants.USER_LAST_UPDATED",
          "values": [
            "sabaspel:#toEpoch(new java.util.Date(), 'yyyy-MM-dd HH:mm:ss')"
          ]
        }
      ]
    },
    {
      "type": "exception",
      "name": "Business_Exception"
    },
    {
      "type": "previewFilter",
      "name": "Business_PreviewFilter"
    },
    {
      "type": "xmlMerger",
      "name": "Business_XmlMerger",
      "outputType": "FILE",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      }
    },
    {
      "type": "fileCopy",
      "name": "Business_File_Copy",
      "destinationPath": "sabaspel:headers.headers['storagePath']",
      "fileName": "sabaspel:headers.headers['storageFileName']",
      "fileSuffix": ".xml",
      "removeInputFiles": "false"
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Business_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "SUCCESS"
    },
    {
      "type": "router",
      "name": "Business_Router"
    },
    {
      "type": "flowMonitoring",
      "name": "Business_HttpMonitoring",
      "detail": {
        "defaultMessage": "Business {OBJECT_TYPE} data fetch is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Business_HttpException",
      "detail": {
        "defaultMessage": "Business {OBJECT_TYPE} data fetch resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "1",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Business_JsonToXmlMonitoring",
      "detail": {
        "defaultMessage": "Business {OBJECT_TYPE} transformation phase 1 is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "3",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Business_JsonToXmlException",
      "detail": {
        "defaultMessage": "Business {OBJECT_TYPE} transformation phase 1 resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "2",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Business_XsltMonitoring",
      "detail": {
        "defaultMessage": "Business {OBJECT_TYPE} transformation phase 2 is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "4",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Business_XsltException",
      "detail": {
        "defaultMessage": "Business {OBJECT_TYPE} transformation phase 2 resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "3",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Business_FileNameRename_Exception",
      "detail": {
        "defaultMessage": "Failure occurred in renaming files for Business {OBJECT_TYPE}",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "4",
          "OBJECT_TYPE": "sabaspel:headers.headers['BUSINESS_OBJECT_TYPE']"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "booleanConfigFilter",
      "name": "Business_ScheduleFilter",
      "accept": "sabaspel:!headers.headers['isPreview']"
    }
  ],
  "flow": {
    "success": [
      [
        "SOURCE",
        "Business_Update_Headers",
        "Business_Http_Component",
        "Business_HttpMonitoring",
        "Business_Record_Validator"
      ],
      [
        "Business_Record_Validator",
        "Business_Has_No_Records_Filter",
        "Business_No_Records_Monitoring",
        "Business_No_Records_MetaNode",
        "Business_Router"
      ],
      [
        "Business_Record_Validator",
        "Business_Has_Records_Filter",
        "Business_File_Processor",
        "Business_JsonToXml"
      ],
      [
        "Business_JsonToXml",
        "Business_ScheduleFilter",
        "Business_JsonToXmlMonitoring",
        "Business_Custom_Xslt_Property",
        "Business_Xslt",
        "Business_XsltMonitoring",
        "Business_FileNameRename",
        "Business_EntityConfigUpdater",
        "Business_MetaNode",
        "Business_Router"
      ],
      [
        "Business_JsonToXml",
        "Business_PreviewFilter",
        "Business_XmlMerger",
        "Business_File_Copy"
      ],
      [
        "Business_HttpException",
        "Business_Exception"
      ],
      [
        "Business_JsonToXmlException",
        "Business_Exception"
      ],
      [
        "Business_XsltException",
        "Business_Exception"
      ],
      [
        "Business_FileNameRename_Exception",
        "Business_Exception"
      ],
      [
        "Business_Exception",
        "Business_Router"
      ]
    ],
    "failure": [
      [
        "Business_Http_Component",
        "Business_HttpException"
      ],
      [
        "Business_JsonToXml",
        "Business_JsonToXmlException"
      ],
      [
        "Business_Xslt",
        "Business_XsltException"
      ],
      [
        "Business_FileNameRename",
        "Business_FileNameRename_Exception"
      ]
    ]
  },
  "name": "Business Data Import",
  "integrationId": "integ/mpent/mineab1a2c3d4e5f6a7b8c9d0e1f2a3b4c5d6",
  "entityId": "mpent0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d"
}