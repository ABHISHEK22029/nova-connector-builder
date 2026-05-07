package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.marketplace.vendor.VendorConstants;

/**
 * @author: hrahman
 * @Date: 1/13/20
 **/
public enum DefaultMappingConfig {

    UDEMY_CONTENT(
            VendorConstants.UDEMY,
            "integ/mpent/minea54149993e64463493172147482932",
            "udemy_edcast_content.xml"
    );

    private String sourceEntityId;
    private String targetEntityId;
    private String mappingFileName;

    DefaultMappingConfig(String sourceEntityId, String targetEntityId, String mappingFileName) {
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
        this.mappingFileName = mappingFileName;
    }

    public String getMappingFileName() {
        return mappingFileName;
    }

    public String getSourceEntityId() {