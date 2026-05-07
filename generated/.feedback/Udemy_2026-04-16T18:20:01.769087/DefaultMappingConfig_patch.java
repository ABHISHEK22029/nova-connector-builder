package com.saba.integration.marketplace.mapping.util;

import com.saba.integration.apps.udemy.VendorConstants_patch;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author: hrahman
 * @Date: 1/13/20
 **/
public enum DefaultMappingConfig {
    //Add the files at integration/apps/src/main/resources/com/saba/mapping/default_data/

    ULTIPRO_SABA_EMPLOYMENTDETAILS_PERSONINTERNAL("ULTIPRO SABA EMPLOYMENTDETAILS PERSONINTERNAL", "IMPORT",
            "integ15743711107576451b40205e41042530",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent1574667172848d6c0aab40cdc70420a0",
            "mpent1574694635123a21f3fc60d7fd04c9d0",
            "ultipro_saba_employmentdetails_personinternal.xml",
            "7BD467A1CB5BD21784EDB36E6A38C9C9",
            true),

    ULTIPRO_SABA_LOCATIONS_LOCATION("ULTIPRO SABA LOCATIONS LOCATION", "IMPORT",
            "integ15743711107576451b40205e41042530",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent15746672250464e20cd3809d3904bf80",
            "mpent1574694646268a3d5eca30ffec044800",
            "ultipro_saba_locations_location.xml",
            "4E73E0CEC561442A675E89E49F07E178",
            true),

    ULTIPRO_SABA_JOBS_JOBTYPE("ULTIPRO SABA JOBS JOBTYPE", "IMPORT",
            "integ15743711107576451b40205e41042530",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent15746672339086a4aa9290848a04cab0",
            "mpent15746947119414db7d9460f3e604c3f0",
            "ultipro_saba_jobs_jobtype.xml",
            "38DF8932D548E70862C1D8414C1C8EF9",
            true),

    SABA_ULTIPRO_TRANSCRIPT_PARTICIPATION("SABA ULTIPRO TRANSCRIPT PARTICIPATION","EXPORT",
            "integ15746680281997bad7d050d8bd04dc70",
            "integ15743711107576451b40205e41042530",
            "mpenta843eae9e50340b3b471ea1595597801",
            "mpent1574667172848d6c0aab401595598462",
            "saba_ultipro_transcript_participation.xml",
            "A8E39A8D1E99A3981C59AFC4F3436415",
            true),

    ULTIPRO_SABA_ORGLEVELS_ORGANIZATIONINTERNAL("ULTIPRO SABA ORGLEVELS ORGANIZATIONINTERNAL", "IMPORT",
            "integ15743711107576451b40205e41042530",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent15746672471633d79acb60f0bd04bf50",
            "mpent1574694698748c7a7250e0f2710459b0",
            "ultipro_saba_orglevels_organizationinternal.xml",
            "DD84B6B158262092D99CC11A2C958EDA",
            true),


    ADP_WFN_SABA_WORKER_PERSONINTERNAL("ADP WFN SABA WORKER PERSONINTERNAL", "IMPORT",
            "integ1576870947315a747f95c0fba4047420",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent1578617750999fbf6c33c023e604a550",
            "mpent1574694635123a21f3fc60d7fd04c9d0",
            "adp_wfn_saba_worker_personinternal.xml",
            "FFF14E213CC4B82816090F861373984D",
            true),

    ADP_WFN_SABA_LOCATIONS_LOCATION("ADP WFN SABA LOCATIONS LOCATION", "IMPORT",
            "integ1576870947315a747f95c0fba4047420",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent1578780723326f71bad030b8a8040ea0",
            "mpent1574694646268a3d5eca30ffec044800",
            "adp_wfn_saba_locations_location.xml",
            "6F2B56BEB9B819EF81236D984B07A571",
            true),

    ADP_WFN_SABA_JOB_TITLES_JOBTYPE("ADP WFN SABA JOB_TITLES JOBTYPE", "IMPORT",
            "integ1576870947315a747f95c0fba4047420",
            "integ15746680281997bad7d050d8bd04dc70",
            "mpent15787807562372ed143cf0468a043d50",
            "mpent15746947119414db7d9460f3e604c3f0",
            "adp_wfn_saba_job_titles_jobtype.xml",
            "79859998199990999999999999999999",
            true),

    UDEMY_EDCAST_CONTENT("UDEMY EDCAST CONTENT", "EXPORT",
            VendorConstants_patch.UDEMY_INTEGRATION_ID,
            "integ00000000000000000000000000000002",
            "minea79a969933944158999b165935d229",
            "mpent00000000000000000000000000000004",
            "udemy_edcast_content.xml",
            "9117A1B499D2E574B589148798190511",
            true);

    private String mappingName;
    private String type;
    private String sourceIntegrationId;
    private String targetIntegrationId;
    private String sourceEntityId;
    private String targetEntityId;
    private String fileName;
    private String checksum;
    private boolean isValid;

    DefaultMappingConfig(String mappingName, String type, String sourceIntegrationId, String targetIntegrationId, String sourceEntityId, String targetEntityId, String fileName, String checksum, boolean isValid) {
        this.mappingName = mappingName;
        this.type = type;
        this.sourceIntegrationId = sourceIntegrationId;
        this.targetIntegrationId = targetIntegrationId;
        this.sourceEntityId = sourceEntityId;
        this.targetEntityId = targetEntityId;
        this.fileName = fileName;
        this.checksum = checksum;
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

    public String getChecksum() {
        return checksum;
    }

    public boolean isValid() {
        return isValid;
    }

    public static Optional<DefaultMappingConfig> findBySourceEntityId(String sourceEntityId) {
        return Arrays.stream(DefaultMappingConfig.values())
                .filter(mapping -> mapping.getSourceEntityId().equals(sourceEntityId))
                .findFirst();
    }

    public static Optional<DefaultMappingConfig> findByTargetEntityId(String targetEntityId) {
        return Arrays.stream(DefaultMappingConfig.values())
                .filter(mapping -> mapping.getTargetEntityId().equals(targetEntityId))
                .findFirst();
    }

    public static Optional<DefaultMappingConfig> findByMappingName(String mappingName) {
        return Arrays.stream(DefaultMappingConfig.values())
                .filter(mapping -> mapping.getMappingName().equals(mappingName))
                .findFirst();
    }
}