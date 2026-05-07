package com.saba.integration.apps.successfactor;

public class SuccessfactorConstants {

    // API CONFIGURATION
    public static final String DEFAULT_BASE_URL = "https://api.successfactors.com";

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "SUCCESSFACTOR_LAST_SYNC";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // HEADER KEYS
    public static final String HEADER_API_URL = "SUCCESSFACTOR_API_URL";
    public static final String HEADER_UPDATED_FROM = "SUCCESSFACTOR_UPDATED_FROM";
    public static final String HEADER_UPDATED_TO = "SUCCESSFACTOR_UPDATED_TO";
    public static final String HEADER_OBJECT_TYPE = "SUCCESSFACTOR_OBJECT_TYPE";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // CONFIGURATION KEYS
    public static final String CONFIG_BASE_URL = "SERVICE_URL";
    public static final String CONFIG_COMPANY_ID = "COMPANY_ID";
    public static final String CONFIG_USERNAME = "USERNAME";
    public static final String CONFIG_PASSWORD = "PASSWORD";

    public static final String CONFIG_UPDATE_FROM = "UPDATE_FROM";
    public static final String CONFIG_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // OBJECT TYPES
    public static final String OBJECT_USER = "User";

    // API ACTIONS
    public static final String ACTION_QUERY = "/odata/v2/%s";

    // RESPONSE FIELDS
    public static final String D = "d";
    public static final String RESULTS = "results";

    // API PARAMETERS
    public static final String PARAM_FORMAT = "json";
    public static final String PARAM_FILTER = "$filter";
    public static final String PARAM_SELECT = "$select";
    public static final String PARAM_SKIP = "$skip";
    public static final String PARAM_TOP = "$top";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";
    public static final int JSON_FORMAT = 1;
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 500;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_MAX_SECONDS_INT = 1800;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid SuccessFactors credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to SuccessFactors API";

    // LOGGING
    public static final String LOG_PREFIX = "[SuccessFactorsComponentControl] ";
    public static final String LOG_TEST_PREFIX = "[SuccessFactorsTestConnection] ";

    private SuccessfactorConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}