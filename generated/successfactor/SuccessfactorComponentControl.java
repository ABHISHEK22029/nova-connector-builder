package com.saba.integration.apps.successfactor;

import com.saba.integration.http.HTTPComponentControl;

public class SuccessfactorComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new SuccessfactorComponentControl();
    }

    // For most connectors, this is ALL you need.
    // Override nextRequest() ONLY if you need post-auth body manipulation.
}