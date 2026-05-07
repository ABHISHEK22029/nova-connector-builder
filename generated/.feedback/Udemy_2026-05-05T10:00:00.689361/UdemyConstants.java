package com.saba.integration.apps.udemy;

public class UdemyConstants {

    // ── API CONFIGURATION ──
    public static final String DEFAULT_BASE_URL = "https://api.udemy.com/api-2.0";
    public static final String COURSES_ENDPOINT = "/courses/";
    public static final String RELEVANT_COURSES_ENDPOINT = "/courses/?page_size={pageSize}&page={currentPageNumber}";

    // ── STATE MANAGEMENT ──
    public static final String LAST_SYNC_TIMESTAMP = "UDEMY_LAST_SYNC";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // ── HEADER KEYS ──
    public static final String HEADER_API_URL = "UDEMY_API_URL";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";
    public static final String HEADER_AUTHORIZATION = "Authorization";

    // ── CONFIGURATION KEYS ──
    public static final String CONFIG_BASE_URL = "BASE_URL";
    public static final String CONFIG_CLIENT_ID = "CLIENT_ID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENT_SECRET";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // ── OBJECT TYPES ──
    public static final String OBJECT_COURSE = "course";

    // ── RESPONSE FIELDS ──
    public static final String RESULTS = "results";
    public static final String NEXT = "next";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String HEADLINE = "headline";
    public static final String URL = "url";

    // ── API PARAMETERS ──
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_PAGE = "page";

    // ── UTILITY CONSTANTS ──
    public static final String UTF_8 = "UTF-8";
    public static final String TRUE = "true";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 500;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_MAX_SECONDS_INT = 1800;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ── ERROR MESSAGES ──
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Udemy credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Udemy API";

    // ── CACHED AUTH PROPERTY ──
    public static final String CACHED_HEADER_PROPERTY = "com.saba.udemy.course.token";

    // ── LOGGING ──
    public static final String LOG_PREFIX = "[UdemyControl] ";
    public static final String LOG_AUTH_PREFIX = "[UdemyAuth] ";
    public static final String LOG_TEST_PREFIX = "[UdemyTestConnection] ";

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}