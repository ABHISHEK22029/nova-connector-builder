package com.saba.integration.apps.udemy;

import com.saba.integration.http.HTTPComponentControl;

/**
 * Udemy Business Component Control
 *
 * Minimal override of HTTPComponentControl. The base class handles 90% of the work,
 * including URL resolution, body building, pagination, and authentication.
 *
 * For Udemy Business, the Basic Authentication header is injected by the
 * {@link com.saba.integration.apps.udemy.UdemyFlows.UdemyBasicAuthenticationStrategy}
 * directly into the request headers. No further post-authentication manipulation
 * of the request body or headers is required by this component control.
 *
 * @author Nova Integration Team
 * @version 1.0
 */
public class UdemyComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl();
    }

    // No override of nextRequest() is needed as there's no post-authentication
    // manipulation of the request body or headers required for Udemy Business.
    // The base class handles all standard HTTP request preparation.
}