package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.marketplace.vendor.VendorConstants;

public enum DefaultMappingConfig {

    UDEMY_CONTENT(
            VendorConstants.UDEMY,
            "integ/mpent/minea115899b849444999821f9939856991",
            "udemy_edcast_content.xml"
    );

    private final String sourceEntityId;
    private final String targetEntityId;
    private final String mappingFileName;

    DefaultMappingConfig(String sourceEntityId, String targetEntityId, String mappingFileName) {
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
        this.mappingFileName = mappingFileName;
    }

    public String getMappingFileName() {
        return mappingFileName;
    }

    public String getSourceEntityId() {