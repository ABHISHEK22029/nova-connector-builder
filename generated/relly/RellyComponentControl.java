package com.saba.integration.apps.relly;

import com.saba.integration.apps.commons.DTO.HTTPRequestDTO;
import com.saba.integration.apps.commons.authentication.AuthenticationStrategy;
import com.saba.integration.apps.commons.base.AbstractComponentControl;
import com.saba.integration.apps.commons.context.ContextDTO;
import com.saba.integration.apps.commons.message.Message;
import com.saba.integration.apps.commons.resolver.ResolverUtil;
import com.saba.integration.apps.commons.utils.SabaLogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

public class RellyComponentControl extends AbstractComponentControl {

    private static final Logger log = LoggerFactory.getLogger(RellyComponentControl.class);
    private String apiKey;

    public RellyComponentControl() {
        super();
    }

    public RellyComponentControl(String apiKey, String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, String responseValidator, String sslContextName, ContextDTO contextDTO, Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
        this.apiKey = apiKey;
    }

    @Override
    public HTTPRequestDTO nextRequest() {

        HTTPRequestDTO httpRequestDTO = super.nextRequest();
        if (null != httpRequestDTO) {
            String apiKey = ResolverUtil.resolveValue(super.getMessage(), RellyConstants.API_KEY, null);
            httpRequestDTO.addHeader("X-API-Key", apiKey);
        }

        return httpRequestDTO;
    }

    @Override
    public RellyComponentControl newInstance() {
        return new RellyComponentControl(apiKey, super.getUrlPattern(), super.getMethod(), super.getBody(), super.getHeaders(), super.getMultipartBody(), super.getAuthenticationStrategy(), super.getOutputTypeExpression(), super.getMaxLoopExpression(), super.getResponseValidator(), super.getSslContextName(), super.getContextDTO(), super.getMessage());
    }
}