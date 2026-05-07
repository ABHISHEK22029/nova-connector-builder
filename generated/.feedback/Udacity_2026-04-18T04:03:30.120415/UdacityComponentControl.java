package com.saba.integration.apps.udacity;

import com.saba.integration.framework.async.ContextDTO;
import com.saba.integration.framework.http.AbstractComponentControl;
import com.saba.integration.framework.http.HTTPComponentControl;
import com.saba.integration.framework.http.HTTPRequestDTO;
import com.saba.integration.framework.http.HTTPResponseValidator;
import com.saba.integration.framework.oauth2.Oauth2AuthenticationStrategy;
import com.saba.integration.utils.ResolverUtil;
import com.saba.integration.utils.SabaLogger;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public class UdacityComponentControl extends AbstractComponentControl {

    private static final String AUTHORIZATION_KEY = "authorization_token";
    private static final String NEXT_PAGE_URL = "next_page_url";
    private static final Logger log = LoggerFactory.getLogger(UdacityComponentControl.class);
    private String url;

    public UdacityComponentControl(String url) {
        super();
        this.url = url;
    }

    public UdacityComponentControl(String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, Oauth2AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, HTTPResponseValidator responseValidator, String sslContextName, ContextDTO contextDTO, com.saba.integration.framework.async.Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
    }

    @Override
    public HTTPComponentControl newInstance() {
        return new UdacityComponentControl(url);
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        String nextPageUrl = ResolverUtil.resolveValue(super.getMessage(), NEXT_PAGE_URL, null);
        log.debug("#UdacityComponentControl: next page url is " + nextPageUrl);

        if ((super.getMaxLoopCounter() != -1 && super.getPageNumber() >= super.getMaxLoopCounter())
            || StringUtils.isEmpty(super.getUrlPattern())) {
            super.setUrlPattern(null);
            super.setHaveMore(false);

        } else if (super.getPageNumber() != 0) {
            super.setUrlPattern(nextPageUrl);
            super.setHaveMore(!StringUtils.isEmpty(nextPageUrl));
        }

        HTTPRequestDTO httpRequestDTO = super.nextRequest();
        if (null != httpRequestDTO) {
            String token = ResolverUtil.resolveValue(super.getMessage(), AUTHORIZATION_KEY, null);
            httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, Oauth2AuthenticationStrategy.BEARER_TYPE + " " + token);
        }

        return httpRequestDTO;
    }
}