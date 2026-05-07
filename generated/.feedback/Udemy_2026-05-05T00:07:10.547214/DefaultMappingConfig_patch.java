package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.marketplace.vendor.VendorConstants;

public enum DefaultMappingConfig {

    UDEMY_CONTENT(
        VendorConstants.UDEMY,
        "integ/mpent/minea472b3c1d5a2f9b8d7c6",
        "udemy_edcast_content.xml"
    );

    private final String integrationId;
    private final String sourceEntityId;
    private final String targetEntityId;

    DefaultMappingConfig(String integrationId, String sourceEntityId, String targetEntityId) {
        this.integrationId = integrationId;
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getSourceEntityId() {
        return sourceEntityId;
    }

    public String getTargetEntityId() {
        return targetEntityId;
    }
}