package com.saba.integration.apps.udemy;

import com.saba.integration.http.HTTPComponentControl;
import com.saba.integration.http.HTTPRequestDTO;

public class UdemyComponentControl extends HTTPComponentControl {

    @Override
    public HTTPComponentControl newInstance() {
        return new UdemyComponentControl();
    }
}