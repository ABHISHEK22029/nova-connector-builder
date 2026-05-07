package com.saba.integration.apps.udemy;

import com.saba.integration.core.api.HTTPComponentControl;
import com.saba.integration.core.api.HTTPRequestDTO;
import com.saba.integration.core.api.HTTPResponseValidator;
import com.saba.integration.core.api.ResolverUtil;
import com.saba.integration.core.model.AuthenticationStrategy;
import com.saba.integration.core.model.ContextDTO;
import com.saba.integration.core.string.StringUtils;
import com.saba.integration.logging.SabaLogger;
import com.saba.integration.apps.AppsLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import com.saba.integration.core.model.Message;

public class UdemyComponentControl extends HTTPComponentControl {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    private String clientId;
    private String clientSecret;

    public UdemyComponentControl() {
        super();
    }

    public UdemyComponentControl(String clientId, String clientSecret) {
        super();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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
        return new UdemyComponentControl(clientId, clientSecret);
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        String nextPageUrl = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.NEXT_PAGE, null);
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
            String authHeader = UdemyConstants.AUTH_TYPE + java.util.Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
            httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }

        return httpRequestDTO;
    }
}