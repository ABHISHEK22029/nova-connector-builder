package com.saba.integration.apps.business;

import com.saba.integration.http.HTTPComponentControl;
import com.saba.integration.http.HTTPRequestDTO;

/**
 * Business Component Control
 *
 * Minimal override of HTTPComponentControl. The base class handles:
 * - URL resolution from Content.js
 * - Request body construction from Content.js multipartBody
 * - Pagination via #currentPageNumber SpEL
 * - Auth token injection (API Key in headers)
 * - Response handling and loop termination
 *
 * No custom logic is required in nextRequest() as the API Key is a static header
 * and does not need to be moved to the body.
 *
 * @author Nova Integration Team
 */
public class BusinessComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new BusinessComponentControl();
    }

    // For API Key authentication, no override of nextRequest() is typically needed
    // as the API key is simply added to headers by the framework.
    // If a token needed to be moved from headers to the body post-authentication,
    // this method would be overridden (e.g., as in Kaltura).
    // In this case, the base class's nextRequest() is sufficient.
}