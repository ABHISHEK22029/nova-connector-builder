package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.apps.relly.VendorConstants_patch;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author: hrahman
 * @Date: 1/13/20
 **/
public enum DefaultMappingConfig_patch {
    //Add the files at integration/apps/src/main/resources/com/saba/mapping/default_data/

    RELLY_EDCAST_CONTENT("RELLY EDCAST CONTENT", "IMPORT",
            VendorConstants_patch.INTEGRATION_ID,
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent46a9a29985034649a933464a7099a669",
            "mpent1574694635123a21f3fc60d7fd04c9d0",
            "relly_edcast_content.xml",
            "6D34A9B7123C49E089C5B26999417871",
            true);

    private String mappingName;
    private String type;
    private String sourceIntegrationId;
    private String targetIntegrationId;
    private String sourceEntityId;
    private String targetEntityId;
    private String fileName;
    private String md5Hash;
    private boolean isValid;

    DefaultMappingConfig_patch(String mappingName, String type, String sourceIntegrationId, String targetIntegrationId, String sourceEntityId, String targetEntityId, String fileName, String md5Hash, boolean isValid) {
        this.mappingName = mappingName;
        this.type = type;
        this.sourceIntegrationId = sourceIntegrationId;
        this.targetIntegrationId = targetIntegrationId;
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
        this.fileName = fileName;
        this.md5Hash = md5Hash;
        this.isValid = isValid;
    }

    public String getMappingName() {
        return mappingName;
    }

    public String getType() {
        return type;
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

    public String getMd5Hash() {
        return md5Hash;
    }

    public boolean isValid() {
        return isValid;
    }

    public static Optional<DefaultMappingConfig_patch> getDefaultMappingConfig(String mappingName) {
        return Arrays.stream(DefaultMappingConfig_patch.values()).filter(e -> e.getMappingName().equals(mappingName)).findFirst();
    }
}