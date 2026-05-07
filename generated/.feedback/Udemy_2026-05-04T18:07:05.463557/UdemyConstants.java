package com.saba.integration.apps.udemy;

/**
 * Constants for Udemy Business Integration
 */
public class UdemyConstants {

    // Integration ID
    public static final String UDEMY = "integ/mpent/minea0123456789abcdef0123456789ab";

    // LOGGING
    public static final String LOG_PREFIX = "[UdemyControl] ";
    public static final String LOG_AUTH_PREFIX = "[UdemyAuth] ";
    public static final String LOG_TEST_PREFIX = "[UdemyTestConnection] ";

    // CONFIGURATION KEYS (must match account configuration keys in the UI)
    public static final String CONFIG_CLIENT_ID = "CLIENTID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENTSECRET";
    public static final String CONFIG_PORTAL_ID = "PORTALID"; // Organization ID
    public static final String CONFIG_BASE_URL = "PORTALURL"; // e.g., https://{organization_name}.udemy.com

    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";
    public static final String CONFIG_UPDATE_FROM = "UPDATE_FROM"; // For incremental sync lookback in days

    // API ENDPOINTS AND PATHS
    public static final String API_VERSION_PATH = "/api-2.0";
    public static final String ORG_PATH_PREFIX = "/organizations/"; // Followed by {organization_id}
    public static final String COURSES_PATH = "/courses/";
    public static final String USERS_PATH = "/users/";
    public static final String COURSE_ENROLLMENTS_PATH = "/course-enrollments/";
    public static final String USER_ACTIVITY_PATH = "/user-activity/";

    // HEADER KEYS
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";
    public static final String HEADER_OBJECT_TYPE = "UDEMY_OBJECT_TYPE";
    public static final String HEADER_UPDATED_FROM = "UDEMY_UPDATED_FROM"; // For incremental sync timestamp
    public static final String HEADER_PORTAL_ID = "UDEMY_PORTAL_ID"; // To pass portal ID to flows/control

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "UDEMY_LAST_SYNC";
    public static final String COURSE_LAST_UPDATED = "UDEMY_COURSE_LAST_UPDATED";
    public static final String USER_LAST_UPDATED = "UDEMY_USER_LAST_UPDATED";
    public static final String ENROLLMENT_LAST_UPDATED = "UDEMY_ENROLLMENT_LAST_UPDATED";
    public static final String ACTIVITY_LAST_UPDATED = "UDEMY_ACTIVITY_LAST_UPDATED";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // OBJECT TYPES
    public static final String OBJECT_COURSE = "course";
    public static final String OBJECT_USER = "user";
    public static final String OBJECT_ENROLLMENT = "enrollment";
    public static final String OBJECT_ACTIVITY = "activity";

    // API PARAMETERS
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_LAST_UPDATE_DATE_GT = "last_update_date__gt"; // For incremental sync
    public static final String PARAM_FIELDS = "fields"; // To request specific fields
    public static final String PARAM_START_DATE = "start_date"; // For activity
    public static final String PARAM_END_DATE = "end_date"; // For activity

    // RESPONSE FIELDS
    public static final String FIELD_RESULTS = "results";
    public static final String FIELD_COUNT = "count";
    public static final String FIELD_NEXT = "next";
    public static final String FIELD_PREVIOUS = "previous";
    public static final String FIELD_ID = "id";
    public static final String FIELD_TITLE = "title"; // For courses
    public static final String FIELD_EMAIL = "email"; // For users
    public static final String FIELD_LAST_UPDATE_DATE = "last_update_date";
    public static final String FIELD_CREATED = "created"; // For enrollments/activity

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";
    public static final String APPLICATION_JSON = "application/json";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 100; // Max page size for Udemy Business API
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Udemy Business credentials (Client ID or Client Secret)";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Udemy Business API";
    public static final String ERROR_INVALID_PORTAL_ID = "Invalid Udemy Business Portal ID";

    // CACHED AUTH PROPERTY (for Basic Auth, we cache the credentials themselves, not a token)
    public static final String CACHED_AUTH_PROPERTY = "com.saba.udemy.auth.basic";

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}