package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.marketplace.vendor.VendorConstants;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author: hrahman
 * @Date: 1/13/20
 **/
public enum DefaultMappingConfig {

    SUCCESSFACTOR_CONTENT(
        VendorConstants.SUCCESSFACTOR,
        "integ49739562109b46948506796024179213",
        "successfactor_edcast_content.xml"
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

    public Optional<DefaultMappingConfig> getConfig(String sourceEntityId) {
        return Arrays.stream(DefaultMappingConfig.values())
            .filter(e -> e.getSourceEntityId().equals(sourceEntityId))
            .findFirst();
    }

    public String getSourceEntityId() {