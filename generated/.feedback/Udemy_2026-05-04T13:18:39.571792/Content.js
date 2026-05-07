{
  "components": [
    {
      "type": "http",
      "name": "Udemy_Http_Component",
      "control": {
        "type": "udemyComponentControl",
        "url": "sabaspel:payload.accountConfigs['PORTALURL'][0] + '/api-2.0/organizations/' + payload.accountConfigs['PORTALID'][0] + '/' + (payload.accountConfigs['OBJECT_TYPE'][0] == 'course' ? 'courses/' : (payload.accountConfigs['OBJECT_TYPE'][0] == 'user' ? 'users/' : 'course-enrollments/')) + '?page=' + #currentPageNumber + '&page_size=' + (headers.headers['isPreview']==true ? '10' : payload.accountConfigs['PAGE_SIZE'][0]) + (payload.accountConfigs['UDEMY_LAST_SYNC'][0] != null && headers.headers['isPreview'] != true ? '&updated_after=' + #toIsoDateTime(#addDays(#toDateTime(payload.accountConfigs['UDEMY_LAST_SYNC'][0], 'yyyy-MM-dd HH:mm:ss'), -payload.accountConfigs['LOOKBACK_DAYS'][0])) : '')"
      },
      "requestMethod": "GET",
      "headers": {
        "Accept_Encoding": [
          "gzip"
        ]
      },
      "outputType": "FILE",
      "maxLoopCounter": "sabasp