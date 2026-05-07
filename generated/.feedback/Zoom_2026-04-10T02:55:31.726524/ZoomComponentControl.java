package com.saba.integration.apps.zoom;

import com.saba.integration.core.model.HTTPRequestDTO;
import com.saba.integration.core.resolver.ResolverUtil;
import com.saba.integration.core.service.HTTPComponentControl;
import com.saba.integration.core.service.impl.AbstractComponentControl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

@Slf4j
public class ZoomComponentControl extends AbstractComponentControl {

    @Override
    public HTTPRequestDTO nextRequest() {
        // if no.of RecordsCounter is equal to total_records in responseHeader then stop.
        // else continue to fetch records with appropriate offset

        //String nextPageUrl = ResolverUtil.resolveValue(super.getMessage(), NEXT_PAGE_URL, null);
        //log.error("#ZoomComponentControl: next page url is " + nextPageUrl);
        // If we have next page url means we have more records to fetch
        // In case of preview we get maxLoopCounter as 1 and for schedule -1
        if ((super.getMaxLoopCounter() != -1 && super.getPageNumber() >= super.getMaxLoopCounter())
                || StringUtils.isEmpty(super.getUrlPattern())) {
            super.setUrlPattern(null);
            super.setHaveMore(false);

        } else if (super.getPageNumber() != 0) {
            //super.setUrlPattern(nextPageUrl);
            super.setHaveMore(true); //!StringUtils.isEmpty(nextPageUrl));
        }


        HTTPRequestDTO httpRequestDTO = super.nextRequest();
        if (null != httpRequestDTO) {
            String token = ResolverUtil.resolveValue(super.getMessage(), ZoomConstants.ACCESS_TOKEN, null);
            httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return httpRequestDTO;
    }

    @Override
    public HTTPComponentControl newInstance() {
        return new ZoomComponentControl();
    }
}