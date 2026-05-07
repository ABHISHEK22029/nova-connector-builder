package com.saba.integration.apps.stripe;

import com.saba.integration.http.HTTPComponentControl;

public class StripeComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new StripeComponentControl();
    }

    // Stripe uses an API Key in the Authorization header (Bearer scheme).
    // The base HTTPComponentControl and the configured AuthStrategy
    // (StripeAuthStrategy) handle injecting this token into the request headers
    // BEFORE nextRequest() is called.
    //
    // Therefore, no override of nextRequest() is needed for post-authentication
    // body manipulation or header adjustments, keeping this class minimal
    // as per framework rules.
}