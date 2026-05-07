package com.saba.integration.apps.udemy;

import com.saba.integration.core.api.AppsLogger;
import com.saba.integration.core.api.HTTPComponentControl;
import com.saba.integration.core.api.Message;
import com.saba.integration.core.api.ResolverUtil;
import com.saba.integration.core.model.ContextDTO;
import com.saba.integration.core.model.HTTPRequestDTO;
import com.saba.integration.core.rest.AuthenticationStrategy;
import com.saba.integration.core.rest.HTTPResponseValidator;
import com.saba.integration.core.rest.Oauth2AuthenticationStrategy;
import com.saba.integration.framework.api.connectors.AbstractComponentControl;
import com.saba.integration.utils.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public class UdemyComponentControl extends AbstractComponentControl {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);
    private String clientId;
    private String clientSecret;
    private String url;

    public UdemyComponentControl() {
        super();
    }

    public UdemyComponentControl(String clientId, String clientSecret, String url) {
        super();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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
    public UdemyComponentControl(String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, HTTPResponseValidator responseValidator, String sslContextName, ContextDTO contextDTO, Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
    }

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl(clientId, clientSecret, url);
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        // if no.of RecordsCounter is equal to total_records in responseHeader then stop.
        // else continue to fetch records with appropriate offset

        String next = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.NEXT, null);
        log.info("#UdemyComponentControl: next page url is " + next);
        // If we have next page url means we have more records to fetch
        // In case of preview we get maxLoopCounter as 1 and for schedule -1
        if ((super.getMaxLoopCounter() != -1 && super.getPageNumber() >= super.getMaxLoopCounter())
                || StringUtils.isEmpty(super.getUrlPattern())) {
            super.setUrlPattern(null);
            super.setHaveMore(false);

        } else if (super.getPageNumber() != 0) {
            super.setUrlPattern(next);
            super.setHaveMore(!StringUtils.isEmpty(next));
        }


        HTTPRequestDTO httpRequestDTO = super.nextRequest();
        if (null != httpRequestDTO) {
            String auth = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.AUTH_HEADER, null);
            httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, UdemyConstants.AUTH_TYPE + auth);
        }

        return httpRequestDTO;
    }
}