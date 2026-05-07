{
  "components": [
    {
      "type": "http",
      "name": "Udemy_Catalog_Http_Component",
      "control": {
        "type": "udemyComponentControl",
        "url": "sabaspel:payload.accountConfigs['BASE_URL'][0] + '/api-2.0/courses/?page_size=' + (headers.headers['isPreview']==true ? '10' : payload.accountConfigs['PAGE_SIZE'][0]) + '&page=' + #currentPageNumber"
      },
      "requestMethod": "GET",
      "headers": {
        "Authorization": "sabaspel: 'Basic ' + T(org.springframework.security.crypto.codec.Base64).encodeToString((payload.accountConfigs['CLIENT_ID'][0] + ':' + payload.accountConfigs['CLIENT_SECRET'][0]).bytes).toString()"
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
        "type": "oauth2v2",
        "detail": {
          "clientId": "sabaspel:payload.accountConfigs['CLIENT_ID'][0]",
          "clientSecret": "sabaspel:payload.accountConfigs['CLIENT_SECRET'][0]",
          "url": "sabaconst:https://api.udemy.com/oauth/token",
          "grantType": "sabaconst:client_credentials"
        },
        "cachedHeaderProperty": "com.saba.udemy.course.token"
      },
      "retryOptions": {
        "maxRetry": 3,
        "delaySeconds": 5
      }
    },
    {
      "type": "filePathProcessor",
      "name": "Udemy_Catalog_File_Processor",
      "delimiter": "_",
      "filePathEntries": {
        "com.saba.jsontoxml.outputfile": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] +'_'+ 'json.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogJson.xml'",
        "com.saba.xslt.output.filepath": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'transformed.csv' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogTransformed.csv'",
        "com.saba.xml.split.output.filepath": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'split.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogSplit.xml'"
      }
    },
    {
      "type": "jsonToXml",
      "name": "Udemy_Catalog_JsonToXml",
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
      "name": "Udemy_Catalog_Custom_Xslt_Property"
    },
    {
      "type": "xslt",
      "name": "Udemy_Catalog_Xslt",
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
      "name": "Udemy_Catalog_FileNameRename",
      "fileName": "UdemyCatalog",
      "suffix": ".csv"
    },
    {
      "type": "exception",
      "name": "Udemy_Catalog_Exception"
    },
    {
      "type": "previewFilter",
      "name": "Udemy_PreviewFilter"
    },
    {
      "type": "xmlMerger",
      "name": "Udemy_XmlMerger",
      "outputType": "FILE",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      }
    },
    {
      "type": "fileCopy",
      "name": "Udemy_File_Copy",
      "destinationPath": "sabaspel:headers.headers['storagePath']",
      "fileName": "sabaspel:headers.headers['storageFileName']",
      "fileSuffix": ".xml",
      "removeInputFiles": "false"
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Udemy_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "SUCCESS"
    },
    {
      "type": "router",
      "name": "Udemy_Router"
    },
    {
      "type": "flowMonitoring",
      "name": "UdemyHttpMonitoring",
      "detail": {
        "defaultMessage": "Udemy catalog Get Data is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "UdemyHttpException",
      "detail": {
        "defaultMessage": "Udemy catalog Get Data resulted in failure",
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
      "name": "UdemyJsonToXmlMonitoring",
      "detail": {
        "defaultMessage": "Udemy catalog transformation phase 1 is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "3"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "UdemyJsonToXmlException",
      "detail": {
        "defaultMessage": "Udemy catalog transformation phase 1 resulted in failure",
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
      "name": "UdemyXsltMonitoring",
      "detail": {
        "defaultMessage": "Udemy catalog transformation phase 2 is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "4"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "UdemyXsltException",
      "detail": {
        "defaultMessage": "Udemy catalog transformation phase 2 resulted in failure",
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
      "name": "UdemyEntityConfigUpdater",
      "entityId": "sabaspel:headers.headers[integrationDataFetchDetail].entityDetail.id",
      "configs": [
        {
          "code": "UPDATE_FROM",
          "values": [
            "sabaspel:#toEpoch(headers.headers['executionTime'], 'yyyy-MM-dd HH:mm:ss')"
          ]
        }
      ]
    }
  ],
  "flow": {
    "success": [
      [
        "SOURCE",
        "Udemy_Catalog_Http_Component",
        "UdemyHttpMonitoring",
        "Udemy_Catalog_File_Processor",
        "Udemy_Catalog_JsonToXml",
        "UdemyJsonToXmlMonitoring",
        "Udemy_Catalog_Custom_Xslt_Property",
        "Udemy_Catalog_Xslt",
        "UdemyXsltMonitoring",
        "Udemy_Catalog_FileNameRename",
        "UdemyEntityConfigUpdater",
        "Udemy_MetaNode",
        "Udemy_Router"
      ],
      [
        "Udemy_Catalog_JsonToXml",
        "Udemy_PreviewFilter",
        "Udemy_XmlMerger",
        "Udemy_File_Copy"
      ],
      [
        "UdemyHttpException",
        "Udemy_Catalog_Exception"
      ],
      [
        "UdemyJsonToXmlException",
        "Udemy_Catalog_Exception"
      ],
      [
        "UdemyXsltException",
        "Udemy_Catalog_Exception"
      ],
      [
        "Udemy_Catalog_Exception",
        "Udemy_Router"
      ]
    ],
    "failure": [
      [
        "Udemy_Catalog_Http_Component",
        "UdemyHttpException"
      ],
      [
        "Udemy_Catalog_JsonToXml",
        "UdemyJsonToXmlException"
      ],
      [
        "Udemy_Catalog_Xslt",
        "UdemyXsltException"
      ]
    ]
  },
  "name": "Udemy Catalog Import",
  "integrationId": "integ/mpent/minea491971469084469998859422334",
  "entityId": "mpent/mpent/minea491971469084469998859422334"
}