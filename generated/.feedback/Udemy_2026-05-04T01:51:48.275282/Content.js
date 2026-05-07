{
  "components": [
    {
      "type": "resolvedProperties",
      "name": "Udemy_Resolved_Properties",
      "properties": {
        "UDEMY_UPDATED_FROM": "sabaspel:payload.accountConfigs['UPDATE_FROM'][0] != null && payload.accountConfigs['UPDATE_FROM'][0] != '' ? payload.accountConfigs['UPDATE_FROM'][0] : (headers.headers['integrationDataFetchDetail'].entityConfigs['UDEMY_COURSE_LAST_UPDATED'][0] != null && headers.headers['integrationDataFetchDetail'].entityConfigs['UDEMY_COURSE_LAST_UPDATED'][0] != '' ? headers.headers['integrationDataFetchDetail'].entityConfigs['UDEMY_COURSE_LAST_UPDATED'][0] : null)"
      }
    },
    {
      "type": "http",
      "name": "Udemy_Catalog_Http_Component",
      "control": {
        "type": "udemyComponentControl"
      },
      "url": "sabaspel:payload.accountConfigs['BASE_URL'][0] + 'courses/?page=' + #currentPageNumber + '&page_size=' + (headers.headers['isPreview']==true ? '10' : '100') + (headers.headers['UDEMY_UPDATED_FROM'] != null && headers.headers['UDEMY_UPDATED_FROM'] != '' ? '&updated_after=' + headers.headers['UDEMY_UPDATED_FROM'] : '')",
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
          "localPart": "results"
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
              "IDENTIFIER": "",
              "RETRY_COUNTER": 1
            }
      },
      "authenticationStrategy": {
          "type": "oauth2v2",
          "detail": {
              "clientId": "sabaspel:payload.accountConfigs['CLIENT_ID'][0]",
              "clientSecret": "sabaspel:payload.accountConfigs['CLIENT_SECRET'][0]",
              "url": "sabaconst:https://www.udemy.com/oauth/2.0/token/",
              "grantType": "sabaconst:client_credentials"
          },
          "cachedHeaderProperty": "sabaconst:com.saba.udemy.course.token"
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
      "type":"httpResponseExtractor",
      "name":"Udemy_Record_Validator",
      "isJson":"sabaspel:headers.headers['IS_JSON']",
      "requiredDepth":"sabaspel:headers.headers['REQUIRED_DEPTH']",
      "namespace":"",
      "localPart":"results",
      "removeInputFiles":"false"
    },
    {
      "type": "booleanConfigFilter",
      "name": "Udemy_Has_Records_Filter",
      "accept": "sabaspel:'true'==headers.headers['HAS_RECORDS']"
    },
    {
        "type": "booleanConfigFilter",
        "name": "Udemy_Has_No_Records_Filter",
        "accept": "sabaspel:'false'==headers.headers['HAS_RECORDS']"
    },
    {
        "type": "flowMonitoring",
        "name": "Udemy_No_Records_Monitoring",
        "detail": {
           "defaultMessage": "There are no updated records",
           "status": "COMPLETE",
           "details": {
             "STEP_COMPLETED": "2"
           }
         }
    },
    {
      "type": "dataFetchResponseTransformer",
      "name": "Udemy_No_Records_MetaNode",
      "event": "FETCH_COMPLETE",
      "eventType": "ALL",
      "status": "COMPLETE"
    },
    {
          "type": "filePathProcessor",
          "name": "Udemy_Catalog_File_Processor",
          "delimiter":"_",
          "filePathEntries":{
                      "com.saba.jsontoxml.outputfile": "sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] +'_'+ 'json.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogJson.xml'",
                      "com.saba.xslt.output.filepath":"sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'transformed.csv' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogTransformed.csv'",
                      "com.saba.xml.split.output.filepath":"sabaspel:headers.headers['isPreview']==true ?integrationMeta.tenant + '_' + headers.headers['INTEGRATION_PREVIEW_MONITORING_ID'] + '_' + 'split.xml' : integrationMeta.tenant + '_' + headers.headers['integrationDataFetchDetail'].scheduleId + '_' + headers.headers['INTEGRATION_MONITORING_ID']+ '_' + 'catalogSplit.xml'"
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
      "type": "entityConfigUpdater",
      "name": "Udemy_EntityConfigUpdater",
      "entityId": "sabaspel:headers.headers['integrationDataFetchDetail'].entityDetail.id",
      "configs": [
        {
          "code": "sabaconst:UDEMY_COURSE_LAST_UPDATED",
          "values": [
            "sabaspel:T(java.time.Instant).now().toString()"
          ]
        }
      ]
    },
    {
      "type": "accountConfigUpdater",
      "name": "UdemyAccountConfigUpdater",
      "accountId": "sabaspel:headers.headers[integrationDataFetchDetail].accountDetail.id",
      "configs": [
        {
          "code": "FULL_SYNC_TOKEN",
          "values": [
             "sabaspel:headers.headers[FULL_SYNC_TOKEN]"
          ]
        },
        {
          "code": "PREVIOUS_SYNC_TOKEN