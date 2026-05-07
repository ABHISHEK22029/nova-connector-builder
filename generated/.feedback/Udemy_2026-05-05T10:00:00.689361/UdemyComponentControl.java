package com.saba.integration.apps.udemy;

import com.saba.integration.http.HTTPComponentControl;

public class UdemyComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl();
    }

    // For most connectors, this is ALL you need.
    // Override nextRequest() ONLY if you need post-auth body manipulation.
}