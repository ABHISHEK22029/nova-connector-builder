package com.saba.integration.apps.business;

/**
 * Constants for Business Integration
 */
public class BusinessConstants {

    // INTEGRATION ID
    public static final String BUSINESS_INTEGRATION_ID = "integ/mpent/mineab1a2c3d4e5f6a7b8c9d0e1f2a3b4c5d6";

    // LOGGING
    public static final String LOG_PREFIX = "[BusinessControl] ";
    public static final String LOG_AUTH_PREFIX = "[BusinessAuth] ";
    public static final String LOG_TEST_PREFIX = "[BusinessTestConnection] ";

    // API CONFIGURATION
    public static final String API_BASE_PATH = "/api/v1";
    public static final String ENDPOINT_CONTENT = "/content";
    public static final String ENDPOINT_USERS = "/users";

    // CONFIGURATION KEYS (must match account configuration keys in the UI)
    public static final String CONFIG_BASE_URL = "BASE_URL";
    public static final String CONFIG_API_KEY = "API_KEY";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // HEADER KEYS
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_API_KEY = "X-API-Key"; // Common for API Key authentication
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";
    public static final String HEADER_OBJECT_TYPE = "BUSINESS_OBJECT_TYPE";

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "BUSINESS_LAST_SYNC";
    public static final String CONTENT_LAST_UPDATED = "BUSINESS_CONTENT_LAST_UPDATED";
    public static final String USER_LAST_UPDATED = "BUSINESS_USER_LAST_UPDATED";

    // OBJECT TYPES
    public static final String OBJECT_CONTENT = "content";
    public static final String OBJECT_USER = "user";

    // API PARAMETERS
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "pageSize";
    public static final String PARAM_UPDATED_SINCE = "updatedSince";

    // RESPONSE FIELDS
    public static final String FIELD_DATA = "data";
    public static final String FIELD_TOTAL_COUNT = "totalCount";
    public static final String FIELD_ID = "id";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_UPDATED_AT = "updatedAt";
    public static final String FIELD_CREATED_AT = "createdAt";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";
    public static final String TRUE = "true";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 100;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Business API Key";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Business API";

    // CACHED AUTH PROPERTY (Placeholder for consistency, even if API Key is stateless)
    public static final String CACHED_HEADER_PROPERTY = "com.saba.business.token";

    private BusinessConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}