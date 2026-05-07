package com.saba.integration.apps.udemy;

import com.saba.integration.http.HTTPComponentControl;

/**
 * Udemy Component Control
 *
 * Minimal override of HTTPComponentControl. The base class handles:
 * - URL resolution from Content.js
 * - Request body construction from Content.js multipartBody
 * - Pagination via #currentPageNumber SpEL
 * - Auth token injection via authenticate()
 * - Response handling and loop termination
 *
 * No custom logic is required for Udemy as its authentication (Bearer token in header)
 * and pagination (page/page_size query parameters) are handled by the base framework.
 *
 * @author Nova Integration Team
 */
public class UdemyComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl();
    }

    // No override of nextRequest() is needed because Udemy's authentication
    // (Bearer token in Authorization header) is handled by the base HTTPComponentControl
    // and the associated AuthenticationStrategy.
    // No post-authentication body manipulation is required.
}