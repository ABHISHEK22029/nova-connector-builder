package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.apps.udemy.VendorConstants_patch;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author: hrahman
 * @Date: 1/13/20
 **/
public enum DefaultMappingConfig_patch {
    //Add the files at integration/apps/src/main/resources/com/saba/mapping/default_data/

    UDEMY_EDCAST_CONTENT("UDEMY EDCAST CONTENT", "IMPORT",
            VendorConstants_patch.UDEMY_INTEGRATION_ID,
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent9e4999557a5a4759a64990699315565",
            "mpent1574694635123a21f3fc60d7fd04c9d0",
            "udemy_edcast_content.xml",
            "27B7B49369799798F835437999B67D5A",
            true);

    private String mappingName;
    private String flowType;
    private String sourceIntegrationId;
    private String targetIntegrationId;
    private String sourceEntityId;
    private String targetEntityId;
    private String fileName;
    private String md5Checksum;
    private boolean isValid;

    DefaultMappingConfig_patch(String mappingName, String flowType, String sourceIntegrationId, String targetIntegrationId, String sourceEntityId, String targetEntityId, String fileName, String md5Checksum, boolean isValid) {
        this.mappingName = mappingName;
        this.flowType = flowType;
        this.sourceIntegrationId = sourceIntegrationId;
        this.targetIntegrationId = targetIntegrationId;
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
        this.fileName = fileName;
        this.md5Checksum = md5Checksum;
        this.isValid = isValid;
    }

    public String getMappingName() {
        return mappingName;
    }

    public String getFlowType() {
        return flowType;
    }

    public String getSourceIntegrationId() {
        return sourceIntegrationId;
    }

    public String getTargetIntegrationId() {
        return targetIntegrationId;
    }

    public String getSourceEntityId() {
        return sourceEntityId;
    }

    public String getTargetEntityId() {
        return targetEntityId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public boolean isValid() {
        return isValid;
    }

    public static Optional<DefaultMappingConfig_patch> findBySourceEntityId(String sourceEntityId) {
        return Arrays.stream(DefaultMappingConfig_patch.values()).filter(e -> e.getSourceEntityId().equals(sourceEntityId)).findFirst();
    }
}