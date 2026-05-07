package com.saba.integration.apps.hrms;

import com.saba.integration.apps.commons.authentication.AuthenticationStrategy;
import com.saba.integration.apps.commons.component.HTTPComponentControl;
import com.saba.integration.apps.commons.dto.ContextDTO;
import com.saba.integration.apps.commons.http.HTTPRequestDTO;
import com.saba.integration.apps.commons.http.HTTPResponseValidator;
import com.saba.integration.apps.commons.resolver.ResolverUtil;
import com.saba.integration.apps.commons.util.StringUtils;
import com.saba.integration.apps.commons.logging.SabaLogger;
import com.saba.integration.apps.commons.apps.AppsLogger;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;
import org.springframework.messaging.Message;

public class HrmsComponentControl extends HTTPComponentControl {

    private static final SabaLogger log = SabaLogger.getLogger(AppsLogger.HrmsLogger);
    private String apiKey;
    private String companyId;

    public HrmsComponentControl() {
        super();
    }

    public HrmsComponentControl(String apiKey, String companyId) {
        super();
        this.apiKey = apiKey;
        this.companyId = companyId;
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
    public HrmsComponentControl(String urlPattern, String method, Object body, MultiValueMap<String, String> headers, MultiValueMap<String, String> multipartBody, AuthenticationStrategy authenticationStrategy, String outputTypeExpression, String maxLoopExpression, HTTPResponseValidator responseValidator, String sslContextName, ContextDTO contextDTO, Message message) {
        super(urlPattern, method, body, headers, multipartBody, authenticationStrategy, outputTypeExpression, maxLoopExpression, responseValidator, sslContextName, contextDTO, message, null);
    }

    @Override
    public HTTPComponentControl newInstance() {
        return new HrmsComponentControl(apiKey, companyId);
    }

    @Override
    public HTTPRequestDTO nextRequest() {
        HTTPRequestDTO httpRequestDTO = super.nextRequest();
        if (null != httpRequestDTO) {
            // Add API Key and Company ID to the header
            if (!StringUtils.isEmpty(apiKey)) {
                httpRequestDTO.addHeader("X-API-KEY", apiKey);
            }
            if (!StringUtils.isEmpty(companyId)) {
                httpRequestDTO.addHeader("X-COMPANY-ID", companyId);
            }
        }
        return httpRequestDTO;
    }
}