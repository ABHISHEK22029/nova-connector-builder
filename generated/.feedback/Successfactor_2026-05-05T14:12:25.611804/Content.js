{
  "components": [
    {
      "type": "http",
      "name": "Successfactor_Catalog_Http_Component",
      "control": {
        "type": "successfactorComponentControl",
        "url": "sabaspel:payload.accountConfigs['BASE_URL'][0] + '/odata/v2/' + payload.accountConfigs['OBJECT_TYPE'][0] + '?$format=json&$top=' + (headers.headers['isPreview']==true ? '10' : payload.accountConfigs['PAGE_SIZE'][0]) + '&$skip=' + (#currentPageNumber * (headers.headers['isPreview']==true ? 10 : Integer.parseInt(payload.accountConfigs['PAGE_SIZE'][0])))"
      },
      "requestMethod": "GET",
      "headers": {
        "Accept": [
          "application/json"
        ],
        "Content-Type": [
          "application/json"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "sabaspel:headers.headers['isPreview']==true ? '1' : '-1'",
      "responseValidator": {
        "type": "noMoreRecords",
        "recordQName": {
          "namespaceURI": "",
          "localPart": "results"
        },
        "requiredDepth": 2,
        "json": true
      },
      "authenticationStrategy": {
        "type": "basicAuth",
        "detail": {
          "username": "sabaspel:payload.accountConfigs['USERNAME'][0] + '@' + payload.accountConfigs['COMPANY_ID'][0]",
          "password": "sabaspel:payload.accountConfigs['PASSWORD'][0]"
        },
        "cachedHeaderProperty": "com.saba.successfactor.user.token"
      },
      "retryOptions": {
        "maxRetry": 3,
        "delaySeconds": 5
      }
    },
    {
      "type": "filePathProcessor",
      "name": "Successfactor_Catalog_File_Processor",
      "delimiter": "_",
      "filePathEntries": {
        "com.saba.jsontoxml.outputfile": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] +'_'+ 'json.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogJson.xml'",
        "com.saba.xslt.output.filepath": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'transformed.csv' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogTransformed.csv'",
        "com.saba.xml.split.output.filepath": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'split.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogSplit.xml'"
      }
    },
    {
      "type": "jsonToXml",
      "name": "Successfactor_Catalog_JsonToXml",
      "eventProcessor": {
        "type": "xmlTagRename",
        "replacements": [
          {
            "source": {
              "namespaceURI": "",
              "localPart": "results"
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
      "name": "Successfactor_Catalog_Custom_Xslt_Property"
    },
    {
      "type": "xslt",
      "name": "Successfactor_Catalog_Xslt",
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
      "name": "Successfactor_Catalog_FileNameRename",
      "fileName": "SuccessfactorCatalog",
      "suffix": ".csv"
    },
    {
      "type": "exception",
      "name": "Successfactor_Catalog_Exception"
    },
    {
      "type": "previewFilter",
      "name": "Successfactor_PreviewFilter"
    },
    {
      "type": "xmlMerger",
      "name": "Successfactor_XmlMerger",
      "outputType": "FILE",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      }
    },
    {
      "type": "fileCopy",
      "name": "Successfactor_File_Copy",
      "destinationPath": "sabaspel:headers.headers['storagePath']",
      "fileName": "sabaspel:headers.headers['storageFileName']",
      "fileSuffix": ".xml",
      "removeInputFiles": "false"
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Successfactor_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "SUCCESS"
    },
    {
      "type": "router",
      "name": "Successfactor_Router"
    },
    {
      "type": "flowMonitoring",
      "name": "SuccessfactorHttpMonitoring",
      "detail": {
        "defaultMessage": "Successfactor catalog Get Data is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "SuccessfactorHttpException",
      "detail": {
        "defaultMessage": "Successfactor catalog Get Data resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "1"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "flowMonitoring",
      "name": "SuccessfactorJsonToXmlMonitoring",
      "detail": {
        "defaultMessage": "Successfactor catalog transformation phase 1 is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "3"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "SuccessfactorJsonToXmlException",
      "detail": {
        "defaultMessage": "Successfactor catalog transformation phase 1 resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "2"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "flowMonitoring",
      "name": "SuccessfactorXsltMonitoring",
      "detail": {
        "defaultMessage": "Successfactor catalog transformation phase 2 is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "4"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "SuccessfactorXsltException",
      "detail": {
        "defaultMessage": "Successfactor catalog transformation phase 2 resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "3"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "entityConfigUpdater",
      "name": "SuccessfactorEntityConfigUpdater",
      "entityId": "sabaspel:headers.headers['integrationDataFetchDetail'].entityDetail.id",
      "configs": [
        {
          "code": "LAST_SYNC_TIMESTAMP",
          "values": [
            "sabaspel:#toEpoch(#now(),'yyyy-MM-dd HH:mm:ss')"
          ]
        }
      ]
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Successfactor_No_Records_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "COMPLETE"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Successfactor_Has_Records_Filter",
      "accept": "sabaspel:headers.headers['CamelHttpResponseCode'] == 200"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Successfactor_No_Records_Filter",
      "accept": "sabaspel:headers.headers['CamelHttpResponseCode'] != 200"
    },
    {
      "type": "flowMonitoring",
      "name": "Successfactor_No_Records_Monitoring",
      "detail": {
        "defaultMessage": "There are no updated records",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "Successfactor_Catalog_FileNameRename_Exception",
      "detail": {
        "defaultMessage": "Failure occurred in renaming files",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "1"
        },
        "status": "FAILURE"
      }
    }
  ],
  "flow": {
    "success": [
      [
        "SOURCE",
        "Successfactor_Catalog_Http_Component",
        "SuccessfactorHttpMonitoring",
        "Successfactor_Catalog_File_Processor",
        "Successfactor_Catalog_JsonToXml",
        "SuccessfactorJsonToXmlMonitoring",
        "Successfactor_Catalog_Custom_Xslt_Property",
        "Successfactor_Catalog_Xslt",
        "SuccessfactorXsltMonitoring",
        "Successfactor_Catalog_FileNameRename",
        "SuccessfactorEntityConfigUpdater",
        "Successfactor_MetaNode",
        "Successfactor_Router"
      ],
      [
        "Successfactor_Catalog_JsonToXml",
        "Successfactor_PreviewFilter",
        "Successfactor_XmlMerger",
        "Successfactor_File_Copy"
      ],
      [
        "SuccessfactorHttpException",
        "Successfactor_Catalog_Exception"
      ],
      [
        "SuccessfactorJsonToXmlException",
        "Successfactor_Catalog_Exception"
      ],
      [
        "SuccessfactorXsltException",
        "Successfactor_Catalog_Exception"
      ],
      [
        "Successfactor_Catalog_FileNameRename_Exception",
        "Successfactor_Catalog_Exception"
      ],
      [
        "Successfactor_Catalog_Exception",
        "Successfactor_Router"
      ],
      [
        "Successfactor_Catalog_Http_Component",
        "Successfactor_Has_Records_Filter",
        "Successfactor_Catalog_File_Processor",
        "Successfactor_Catalog_JsonToXml"
      ],
      [
        "Successfactor_Catalog_Http_Component",
        "Successfactor_No_Records_Filter",
        "Successfactor_No_Records_Monitoring",
        "Successfactor_No_Records_MetaNode",
        "Successfactor_Router"
      ]
    ],
    "failure": [
      [
        "Successfactor_Catalog_Http_Component",
        "SuccessfactorHttpException"
      ],
      [
        "Successfactor_Catalog_JsonToXml",
        "SuccessfactorJsonToXmlException"
      ],
      [
        "Successfactor_Catalog_Xslt",
        "SuccessfactorXsltException"
      ],
      [
        "Successfactor_Catalog_FileNameRename",
        "Successfactor_Catalog_FileNameRename_Exception"
      ]
    ]
  },
  "name": "Successfactor Catalog Import",
  "integrationId": "integ/mpent/minea9a2b3c4d5e6f7a8b9c0d1e2f3a4b",
  "entityId": "mpent1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6"
}