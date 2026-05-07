package com.saba.integration.apps.udemy;

public class UdemyConstants {

    // API CONFIGURATION
    public static final String DEFAULT_BASE_URL = "https://www.udemy.com/api-2.0";
    public static final String API_BASE_PATH = "/";

    // CONFIGURATION KEYS
    public static final String CONFIG_CLIENT_ID = "CLIENT_ID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENT_SECRET";

    // API ENDPOINTS
    public static final String COURSES_ENDPOINT = "/courses/";
    public static final String USERS_ENDPOINT = "/users/";
    public static final String REVIEWS_ENDPOINT = "/courses/{course_id}/reviews/";

    // AUTHENTICATION
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_TYPE = "Basic ";

    // OBJECT TYPES
    public static final String OBJECT_COURSE = "course";
    public static final String OBJECT_USER = "user";
    public static final String OBJECT_REVIEW = "review";

    // PAGING
    public static final String PAGE_SIZE_PARAM = "page_size";
    public static final String PAGE_PARAM = "page";
    public static final int DEFAULT_PAGE_SIZE = 50;
    public static final String NEXT_PAGE = "next";

    // RESPONSE FIELDS
    public static final String RESULTS = "results";
    public static final String COUNT = "count";

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "UDEMY_LAST_SYNC";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // HEADER KEYS
    public static final String HEADER_API_URL = "UDEMY_API_URL";
    public static final String HEADER_OBJECT_TYPE = "UDEMY_OBJECT_TYPE";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_MAX_SECONDS_INT = 1800;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Udemy credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Udemy API";

    // LOGGING
    public static final String LOG_PREFIX = "[UdemyComponentControl] ";
    public static final String LOG_TEST_PREFIX = "[UdemyTestConnection] ";

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}