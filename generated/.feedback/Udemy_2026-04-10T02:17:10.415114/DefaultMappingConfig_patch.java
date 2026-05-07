package com.cornerstoneondemand.connector.udemy;

public enum DefaultMappingConfig_patch {
  UDEMY_EDCAST_CONTENT(
      VendorConstants_patch.UDEMY_INTEGRATION_ID,
      "learningAsset",
      "udemy_edcast_content.xml");

  private final String integrationId;
  private final String entityId;
  private final String mappingFile;

  DefaultMappingConfig_patch(String integrationId, String entityId, String mappingFile) {
    this.integrationId = integrationId;
    this.entityId = entityId;
    this.mappingFile = mappingFile;
  }

  public String integrationId() {
    return integrationId;
  }

  public String entityId() {
    return entityId;
  }

  public String mappingFile() {
    return mappingFile;
  }
}