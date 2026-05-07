{
  "components": [
    {
      "type": "http",
      "name": "Stripe_Http_Component",
      "control": {
        "type": "stripeComponentControl",
        "url": "sabaspel:payload.accountConfigs['BASE_URL'][0] + headers.headers['STRIPE_API_URL'] + '?' + 'limit=' + (headers.headers['isPreview']==true ? '10' : '100') + '&expand[]=data.object'"
      },
      "requestMethod": "GET",
      "headers": {
        "Accept_Encoding": [
          "gzip"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "sabaspel:headers.headers['isPreview']==true ? '1' : '-1'",
      "responseValidator": {
        "type": "noMoreRecords",
        "recordQName": {
          "namespaceURI": "",
          "localPart": "data"
        },
        "requiredDepth": 1,
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
      "authenticationStrategy": {
        "type": "stripeAuthStrategy",
        "detail": {
          "apiKey": "sabaspel:payload.accountConfigs['API_KEY'][0]"
        },
        "cachedHeaderProperty": "com.saba.stripe.customer.token"
      },
      "dataPrefix": "{\"Records\":",
      "dataSuffix": "}",
      "jsonforStax": "true"
    },
    {
      "type": "httpResponseExtractor",
      "name": "Stripe_Record_Validator",
      "isJson": "true",
      "requiredDepth": "1",
      "namespace": "",
      "localPart": "data",
      "removeInputFiles": "false"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Stripe_Has_Records_Filter",
      "accept": "sabaspel:'true'==headers.headers['HAS_RECORDS']"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Stripe_Has_No_Records_Filter",
      "accept": "sabaspel:'false'==headers.headers['HAS_RECORDS']"
    },
    {
      "type": "flowMonitoring",
      "name": "Stripe_No_Records_Monitoring",
      "detail": {
        "defaultMessage": "There are no updated records for Stripe object type: sabaspel:headers.headers['STRIPE_OBJECT_TYPE']",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2"
        }
      }
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Stripe_No_Records_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "COMPLETE"
    },
    {
      "type": "filePathProcessor",
      "name": "Stripe_File_Processor",
      "delimiter": "_",
      "filePathEntries": {
        "com.saba.jsontoxml.outputfile": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] +'_'+ 'json.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + headers.headers['STRIPE_OBJECT_TYPE'] + 'Json.xml'",
        "com.saba.xslt.output.filepath": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'transformed.csv' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + headers.headers['STRIPE_OBJECT_TYPE'] + 'Transformed.csv'",
        "com.saba.xml.split.output.filepath": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'split.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + headers.headers['STRIPE_OBJECT_TYPE'] + 'Split.xml'"
      }
    },
    {
      "type": "jsonToXml",
      "name": "Stripe_JsonToXml",
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
      "name": "Stripe_Custom_Xslt_Property"
    },
    {
      "type": "xslt",
      "name": "Stripe_Xslt",
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
      "name": "Stripe_FileNameRename",
      "fileName": "sabaspel:'Stripe_' + headers.headers['STRIPE_OBJECT_TYPE']",
      "suffix": ".csv"
    },
    {
      "type": "entityConfigUpdater",
      "name": "Stripe_EntityConfigUpdater",
      "entityId": "sabaspel:headers.headers['integrationDataFetchDetail'].entityDetail.id",
      "configs": [
        {
          "code": "sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] == 'customer' ? 'STRIPE_CUSTOMER_LAST_UPDATED' : (headers.headers['STRIPE_OBJECT_TYPE'] == 'invoice' ? 'STRIPE_INVOICE_LAST_UPDATED' : (headers.headers['STRIPE_OBJECT_TYPE'] == 'subscription' ? 'STRIPE_SUBSCRIPTION_LAST_UPDATED' : (headers.headers['STRIPE_OBJECT_TYPE'] == 'product' ? 'STRIPE_PRODUCT_LAST_UPDATED' : (headers.headers['STRIPE_OBJECT_TYPE'] == 'price' ? 'STRIPE_PRICE_LAST_UPDATED' : 'STRIPE_EVENT_LAST_UPDATED'))))",
          "values": [
            "sabaspel:headers.headers['STRIPE_UPDATED_TO']"
          ]
        }
      ]
    },
    {
      "type": "exception",
      "name": "Stripe_Exception"
    },
    {
      "type": "previewFilter",
      "name": "Stripe_PreviewFilter"
    },
    {
      "type": "xmlMerger",
      "name": "Stripe_XmlMerger",
      "outputType": "FILE",
      "xmlAppenderQName": {
        "namespaceURI": "",
        "localPart": "Record"
      }
    },
    {
      "type": "fileCopy",
      "name": "Stripe_File_Copy",
      "destinationPath": "sabaspel:headers.headers['storagePath']",
      "fileName": "sabaspel:headers.headers['storageFileName']",
      "fileSuffix": ".xml",
      "removeInputFiles": "false"
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Stripe_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "SUCCESS"
    },
    {
      "type": "router",
      "name": "Stripe_Router"
    },
    {
      "type": "flowMonitoring",
      "name": "StripeHttpMonitoring",
      "detail": {
        "defaultMessage": "Stripe catalog Get Data for sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "2"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "StripeHttpException",
      "detail": {
        "defaultMessage": "Stripe catalog Get Data for sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] resulted in failure",
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
      "name": "StripeJsonToXmlMonitoring",
      "detail": {
        "defaultMessage": "Stripe catalog transformation phase 1 for sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "3"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "StripeJsonToXmlException",
      "detail": {
        "defaultMessage": "Stripe catalog transformation phase 1 for sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] resulted in failure",
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
      "name": "StripeXsltMonitoring",
      "detail": {
        "defaultMessage": "Stripe catalog transformation phase 2 for sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] is complete",
        "status": "COMPLETE",
        "details": {
          "STEP_COMPLETED": "4"
        }
      }
    },
    {
      "type": "flowMonitoring",
      "name": "StripeXsltException",
      "detail": {
        "defaultMessage": "Stripe catalog transformation phase 2 for sabaspel:headers.headers['STRIPE_OBJECT_TYPE'] resulted in failure",
        "details": {
          "SABA_EXCEPTION": "sabaspel:headers.headers['SABA_EXCEPTION']",
          "INTEGRATION_MONITORING_ID": "sabaspel:headers.headers['INTEGRATION_MONITORING_ID']",
          "STEP_COMPLETED": "3"
        },
        "status": "FAILURE"
      }
    },
    {
      "type": "booleanConfigFilter",
      "name": "StripeScheduleFilter",
      "accept": "sabaspel:!headers.headers['isPreview']"
    },
    {
      "type": "flowMonitoring",
      "name": "Stripe_FileNameRename_Exception",
      "detail": {
        "defaultMessage": "Failure occurred in renaming files for Stripe object type: sabaspel:headers.headers['STRIPE_OBJECT_TYPE']",
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
        "Stripe_Http_Component",
        "StripeHttpMonitoring",
        "Stripe_Record_Validator"
      ],
      [
        "Stripe_Record_Validator",
        "Stripe_Has_No_Records_Filter",
        "Stripe_No_Records_Monitoring",
        "Stripe_No_Records_MetaNode",
        "Stripe_Router"
      ],
      [
        "Stripe_Record_Validator",
        "Stripe_Has_Records_Filter",
        "Stripe_File_Processor",
        "Stripe_JsonToXml"
      ],
      [
        "Stripe_JsonToXml",
        "StripeScheduleFilter",
        "StripeJsonToXmlMonitoring",
        "Stripe_Custom_Xslt_Property",
        "Stripe_Xslt",
        "StripeXsltMonitoring",
        "Stripe_FileNameRename",
        "Stripe_EntityConfigUpdater",
        "Stripe_MetaNode",
        "Stripe_Router"
      ],
      [
        "Stripe_JsonToXml",
        "Stripe_PreviewFilter",
        "Stripe_XmlMerger",
        "Stripe_File_Copy"
      ],
      [
        "StripeHttpException",
        "Stripe_Exception"
      ],
      [
        "StripeJsonToXmlException",
        "Stripe_Exception"
      ],
      [
        "StripeXsltException",
        "Stripe_Exception"
      ],
      [
        "Stripe_FileNameRename_Exception",
        "Stripe_Exception"
      ],
      [
        "Stripe_Exception",
        "Stripe_Router"
      ]
    ],
    "failure": [
      [
        "Stripe_Http_Component",
        "StripeHttpException"
      ],
      [
        "Stripe_JsonToXml",
        "StripeJsonToXmlException"
      ],
      [
        "Stripe_