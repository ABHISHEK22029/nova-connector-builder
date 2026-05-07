package com.saba.integration.apps.successfactor;

public class SuccessfactorConstants {

    // ── API CONFIGURATION ──
    public static final String DEFAULT_BASE_URL = "https://api.successfactors.com";
    public static final String API_VERSION = "v2";
    public static final String ODATA_ENDPOINT = "/odata/" + API_VERSION;

    // ── STATE MANAGEMENT ──
    public static final String LAST_SYNC_TIMESTAMP = "SUCCESSFACTOR_LAST_SYNC";
    public static final String USER_LAST_UPDATED = "SUCCESSFACTOR_USER_LAST_UPDATED";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // ── HEADER KEYS ──
    public static final String HEADER_API_URL = "SUCCESSFACTOR_API_URL";
    public static final String HEADER_OBJECT_TYPE = "SUCCESSFACTOR_OBJECT_TYPE";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";
    public static final String HEADER_CURRENT_PAGE_NUMBER = "currentPageNumber";

    // ── CONFIGURATION KEYS ──
    public static final String CONFIG_BASE_URL = "BASE_URL";
    public static final String CONFIG_COMPANY_ID = "COMPANY_ID";
    public static final String CONFIG_USERNAME = "USERNAME";
    public static final String CONFIG_PASSWORD = "PASSWORD";
    public static final String CONFIG_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // ── OBJECT TYPES ──
    public static final String OBJECT_USER = "User";

    // ── API ENTITIES ──
    public static final String ENTITY_USER = "User";

    // ── API PARAMETERS ──
    public static final String PARAM_FORMAT = "$format";
    public static final String PARAM_SELECT = "$select";
    public static final String PARAM_FILTER = "$filter";
    public static final String PARAM_SKIP = "$skip";
    public static final String PARAM_TOP = "$top";

    // ── API VALUES ──
    public static final String VALUE_JSON = "json";

    // ── RESPONSE FIELDS ──
    public static final String D = "d";
    public static final String RESULTS = "results";
    public static final String METADATA = "__metadata";
    public static final String URI = "uri";
    public static final String NEXT = "__next";

    // ── USER FIELDS ──
    public static final String USER_USER_ID = "userId";
    public static final String USER_USERNAME = "username";
    public static final String USER_FIRST_NAME = "firstName";
    public static final String USER_LAST_NAME = "lastName";
    public static final String USER_EMAIL = "email";
    public static final String USER_STATUS = "status";

    // ── STATUS VALUES ──
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";

    // ── AUTHENTICATION ──
    public static final String AUTH_BASIC = "Basic ";

    // ── UTILITY CONSTANTS ──
    public static final String UTF_8 = "UTF-8";
    public static final String TRUE = "true";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 100;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_MAX_SECONDS_INT = 1800;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ── ERROR MESSAGES ──
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid SuccessFactors credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to SuccessFactors API";

    // ── CACHED AUTH PROPERTY ──
    public static final String CACHED_HEADER_PROPERTY = "com.saba.successfactor.user.token";

    // ── LOGGING ──
    public static final String LOG_PREFIX = "[SuccessfactorControl] ";
    public static final String LOG_AUTH_PREFIX = "[SuccessfactorAuth] ";
    public static final String LOG_TEST_PREFIX = "[SuccessfactorTestConnection] ";

    private SuccessfactorConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}