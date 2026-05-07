package com.saba.integration.apps.coursera;

import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import com.saba.integration.framework.authentication.AuthenticationStrategy;
import com.saba.integration.framework.connectors.AbstractComponentControl;
import com.saba.integration.framework.connectors.HTTPComponentControl;
import com.saba.integration.framework.connectors.HTTPRequestDTO;
import com.saba.integration.framework.connectors.HTTPResponseValidator;
import com.saba.integration.framework.util.ResolverUtil;
import com.saba.integration.framework.async.ContextDTO;
import com.saba.integration.framework.async.Message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class CourseraComponentControl extends AbstractComponentControl implements HTTPComponentControl {

    private String url;

    @Override
    public HTTPComponentControl newInstance() {
        return new CourseraComponentControl(url);
    }

    public CourseraComponentControl(String url) {
        super();
        this.url = url;
    }

    /**
     * @param urlPattern
     * @param method
     * @param body
     * @param headers
     * @param multipartBody
     * @param authenticationStrategy
     * @param outputTypeExpression
     * @param maxLoopExpression
     * @param responseValidator
     * @param sslContextName
     * @param message
     */
    public CourseraComponentControl(String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, HTTPResponseValidator responseValidator, String sslContextName, ContextDTO contextDTO, Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        // if no.of RecordsCounter is equal to total_records in responseHeader then stop.
        // else continue to fetch records with appropriate offset

        //String nextPageUrl = ResolverUtil.resolveValue(super.getMessage(), NEXT_PAGE_URL, null);
        //log.error("#CourseraComponentControl: next page url is " + nextPageUrl);
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
            String token = ResolverUtil.resolveValue(super.getMessage(), CourseraConstants.ACCESS_TOKEN, null);
            httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return httpRequestDTO;
    }
}