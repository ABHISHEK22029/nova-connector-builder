package com.saba.integration.apps.udemy;

/**
 * Constants for Udemy Integration
 */
public class UdemyConstants {

    // INTEGRATION ID
    // This ID must be unique across all Nova integrations.
    public static final String UDEMY = "integ/mpent/minea7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d";

    // LOGGING PREFIXES
    public static final String LOG_PREFIX = "[UdemyControl] ";
    public static final String LOG_AUTH_PREFIX = "[UdemyAuth] ";
    public static final String LOG_TEST_PREFIX = "[UdemyTestConnection] ";

    // API CONFIGURATION
    public static final String API_VERSION = "api-2.0";
    public static final String API_BASE_PATH = "/" + API_VERSION + "/";

    // API ENDPOINTS
    public static final String TOKEN_URL_PATH = "/oauth/token/";
    public static final String COURSES_ENDPOINT = "organizations/{portal_id}/courses/";
    public static final String USERS_ENDPOINT = "organizations/{portal_id}/users/";
    public static final String ENROLLMENTS_ENDPOINT = "organizations/{portal_id}/course-enrollments/";
    public static final String COURSE_DETAIL_ENDPOINT = "organizations/{portal_id}/courses/{course_id}/";
    public static final String USER_DETAIL_ENDPOINT = "organizations/{portal_id}/users/{user_id}/";

    // AUTHENTICATION
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    public static final String SCOPE_READ = "read";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String EXPIRES_IN = "expires_in";
    public static final int DEFAULT_TOKEN_EXPIRY_SECONDS = 3600; // 1 hour

    // HEADER KEYS
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // CONFIGURATION KEYS (must match account configuration keys in the UI)
    public static final String CONFIG_BASE_URL = "PORTALURL";
    public static final String CONFIG_CLIENT_ID = "CLIENTID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENTSECRET";
    public static final String CONFIG_PORTAL_ID = "PORTALID";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";
    public static final String CONFIG_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String CONFIG_LOOKBACK_DAYS = "LOOKBACK_DAYS";

    // STATE MANAGEMENT KEYS
    public static final String LAST_SYNC_TIMESTAMP = "UDEMY_LAST_SYNC";
    public static final String COURSE_LAST_UPDATED = "UDEMY_COURSE_LAST_UPDATED";
    public static final String USER_LAST_UPDATED = "UDEMY_USER_LAST_UPDATED";
    public static final String ENROLLMENT_LAST_UPDATED = "UDEMY_ENROLLMENT_LAST_UPDATED";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;

    // API PARAMETERS
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_PAGE_SIZE_DEFAULT = "100";
    public static final String PARAM_FIELDS = "fields";
    public static final String PARAM_UPDATED_AFTER = "updated_after";
    public static final String PARAM_CREATED_AFTER = "created_after";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_SORT_ASC = "asc";
    public static final String PARAM_SORT_DESC = "desc";

    // OBJECT TYPES
    public static final String OBJECT_TYPE_COURSE = "course";
    public static final String OBJECT_TYPE_USER = "user";
    public static final String OBJECT_TYPE_ENROLLMENT = "enrollment";

    // RESPONSE FIELDS
    public static final String FIELD_ID = "id";
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_LAST_UPDATE_DATE = "last_update_date";
    public static final String FIELD_CREATED = "created";
    public static final String FIELD_RESULTS = "results";
    public static final String FIELD_COUNT = "count";
    public static final String FIELD_NEXT = "next";
    public static final String FIELD_PREVIOUS = "previous";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 100;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Udemy credentials (Client ID or Client Secret)";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Udemy API";
    public static final String ERROR_TOKEN_GENERATION_FAILED = "Failed to generate Udemy access token";
    public static final String ERROR_MISSING_PORTAL_ID = "Missing Portal ID in configuration";

    // CACHED AUTH PROPERTY
    public static final String CACHED_HEADER_PROPERTY = "com.saba.udemy.token";

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}