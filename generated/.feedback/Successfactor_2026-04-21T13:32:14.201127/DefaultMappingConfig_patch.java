package com.cornerstoneondemand.connector.successfactors;

public enum DefaultMappingConfig_patch {

    SUCCESSFACTORS_EDCAST_CONTENT("successfactors-edcast-content-integration", "edcast-content", "successfactor_edcast_content.xml");

    private final String integrationId;
    private final String entityId;
    private final String mappingFile;

    DefaultMappingConfig_patch(String integrationId, String entityId, String mappingFile) {
        this.integrationId = integrationId;
        this.entityId = entityId;
        this.mappingFile = mappingFile;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getMappingFile() {
        return mappingFile;
    }
}