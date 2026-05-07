package com.saba.integration.apps.udemy;

import com.saba.integration.apps.udemy.util.AppsLogger;
import com.saba.integration.core.api.HTTPComponentControl;
import com.saba.integration.core.api.HTTPRequestDTO;
import com.saba.integration.core.api.authentication.AuthenticationStrategy;
import com.saba.integration.core.model.ContextDTO;
import com.saba.integration.core.rest.HTTPResponseValidator;
import com.saba.integration.core.util.ResolverUtil;
import com.saba.integration.core.util.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.messaging.Message;

public class UdemyComponentControl extends HTTPComponentControl {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    private String clientId;
    private String clientSecret;
    private String url;

    public UdemyComponentControl(String clientId, String clientSecret, String url) {
        super();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.url = url;
    }

    public UdemyComponentControl(String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, HTTPResponseValidator responseValidator, String sslContextName, ContextDTO contextDTO, Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
    }

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl(clientId, clientSecret, url);
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        String nextPageUrl = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.NEXT, null);
        log.debug("#UdemyComponentControl: next page url is " + nextPageUrl);

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
            String authHeader = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.AUTH_HEADER, null);
            httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, UdemyConstants.AUTH_TYPE + authHeader);
        }

        return httpRequestDTO;
    }
}