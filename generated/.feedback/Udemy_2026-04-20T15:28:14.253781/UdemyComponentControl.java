package com.saba.integration.apps.udemy;

import com.saba.integration.apps.udemy.constants.AppsLogger;
import com.saba.integration.apps.udemy.constants.UdemyConstants;
import com.saba.integration.framework.authentication.AuthenticationStrategy;
import com.saba.integration.framework.component.HTTPComponentControl;
import com.saba.integration.framework.component.HTTPRequestDTO;
import com.saba.integration.framework.datasource.AbstractComponentControl;
import com.saba.integration.framework.datasource.ContextDTO;
import com.saba.integration.framework.resourceloader.ResolverUtil;
import com.saba.integration.framework.response.HTTPResponseValidator;
import com.saba.integration.framework.support.SabaLogger;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

public class UdemyComponentControl extends AbstractComponentControl {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.UdemyLogger);

    public UdemyComponentControl(String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, HTTPResponseValidator responseValidator, String sslContextName, ContextDTO contextDTO, com.saba.integration.framework.support.Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
    }

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl(super.getUrlPattern(), super.getMethod(), super.getBody(), super.getHeaders(), super.getMultipartBody(), super.getAuthenticationStrategy(), super.getOutputTypeExpression(), super.getMaxLoopExpression(), super.getResponseValidator(), super.getSslContextName(), super.getContextDTO(), super.getMessage());
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        HTTPRequestDTO httpRequestDTO = super.nextRequest();

        if (null != httpRequestDTO) {
            String clientId = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.CONFIG_CLIENT_ID, null);
            String clientSecret = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.CONFIG_CLIENT_SECRET, null);

            if (StringUtils.isNotEmpty(clientId) && StringUtils.isNotEmpty(clientSecret)) {
                String auth = clientId + ":" + clientSecret;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = UdemyConstants.AUTH_TYPE + new String(encodedAuth);
                httpRequestDTO.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
            } else {
                log.error("Client ID or Client Secret is missing. Authentication header will not be added.");
            }

            //Paging
            String pageSize = ResolverUtil.resolveValue(super.getMessage(), UdemyConstants.PAGE_SIZE_PARAM, String.valueOf(UdemyConstants.DEFAULT_PAGE_SIZE));
            httpRequestDTO.addQueryParameter(UdemyConstants.PAGE_SIZE_PARAM, pageSize);

            String page = String.valueOf(super.getPageNumber() + 1);
            httpRequestDTO.addQueryParameter(UdemyConstants.PAGE_PARAM, page);
        }

        return httpRequestDTO;
    }
}