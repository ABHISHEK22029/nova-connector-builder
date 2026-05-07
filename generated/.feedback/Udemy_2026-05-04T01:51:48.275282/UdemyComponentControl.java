package com.saba.integration.apps.udemy;

import com.saba.integration.http.HTTPComponentControl;

/**
 * Udemy Component Control
 *
 * Minimal override of HTTPComponentControl.
 * For Udemy, authentication (Bearer token in Authorization header) and
 * pagination (page/page_size query parameters) are handled automatically
 * by the base HTTPComponentControl and the associated AuthenticationStrategy.
 *
 * Therefore, no custom logic is required in nextRequest() or onSuccess().
 *
 * @author Nova Integration Team
 */
public class UdemyComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl();
    }

    // No override of nextRequest() is needed for Udemy.
    // The base HTTPComponentControl handles:
    // - URL resolution from Content.js
    // - Request body construction from Content.js
    // - Pagination via #currentPageNumber SpEL
    // - Auth token injection into headers via the configured AuthenticationStrategy
    // - Response handling and loop termination
}