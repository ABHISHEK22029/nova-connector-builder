package com.saba.integration.apps.udemy;

import com.saba.integration.core.component.HTTPComponentControl;

public class UdemyComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl();
    }

    // For Udemy, the access token is passed in the Authorization header,
    // which is handled by the base HTTPComponentControl and the AuthenticationStrategy.
    // No special post-authentication body manipulation is required,
    // so overriding nextRequest() is not necessary.
}