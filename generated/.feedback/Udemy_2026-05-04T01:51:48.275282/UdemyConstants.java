package com.saba.integration.apps.udemy;

/**
 * Constants for Udemy Integration
 */
public class UdemyConstants {

    // ── API CONFIGURATION ──
    // Base URL and path structure for the vendor API
    public static final String DEFAULT_BASE_URL = "https://www.udemy.com/api-2.0/";
    public static final String OAUTH_TOKEN_URL = "https://www.udemy.com/oauth/2.0/token/";

    // ── STATE MANAGEMENT ──
    // Keys used to track sync timestamps between runs (delta sync)
    public static final String LAST_SYNC_TIMESTAMP = "UDEMY_LAST_SYNC";
    public static final String COURSE_LAST_UPDATED = "UDEMY_COURSE_LAST_UPDATED";
    public static final String USER_LAST_UPDATED = "UDEMY_USER_LAST_UPDATED"; // If user sync is implemented
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // ── HEADER KEYS ──
    // Internal message headers used to pass data between flow components
    // These are NOT HTTP headers — they're Spring Integration message headers
    public static final String HEADER_API_URL = "UDEMY_API_URL";
    public static final String HEADER_UPDATED_FROM = "UDEMY_UPDATED_FROM";
    public static final String HEADER_UPDATED_TO = "UDEMY_UPDATED_TO";
    public static final String HEADER_ACCESS_TOKEN = "UDEMY_ACCESS_TOKEN";
    public static final String HEADER_OBJECT_TYPE = "UDEMY_OBJECT_TYPE";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // ── CONFIGURATION KEYS ──
    // These MUST match the keys sent by the Nova admin UI in accountConfigs
    // If the UI sends accountConfigs['CLIENT_ID'], this must be "CLIENT_ID"
    public static final String CONFIG_BASE_URL = "BASE_URL";
    public static final String CONFIG_CLIENT_ID = "CLIENT_ID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENT_SECRET";
    public static final String CONFIG_UPDATE_FROM = "UPDATE_FROM";
    public static final String CONFIG_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // ── OBJECT TYPES ──
    // Used to distinguish entity types within the connector
    public static final String OBJECT_COURSE = "course";
    public static final String OBJECT_USER = "user";

    // ── API ENDPOINTS / PATHS ──
    public static final String PATH_COURSES = "courses/";
    public static final String PATH_USERS = "users/";
    public static final String PATH_COURSE_LECTURES = "courses/{course_id}/lectures/";

    // ── AUTHENTICATION ──
    public static final String OAUTH_GRANT_TYPE = "client_credentials";
    public static final String OAUTH_SCOPE = "course:read"; // Default scope, can be extended
    public static final String AUTH_HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTH_HEADER_BASIC_PREFIX = "Basic ";
    public static final String AUTH_HEADER_BEARER_PREFIX = "Bearer ";

    // ── RESPONSE FIELDS ──
    // JSON keys from the vendor API response
    public static final String RESULTS = "results";
    public static final String COUNT = "count";
    public static final String NEXT_PAGE_URL = "next";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String LAST_UPDATE_DATE = "last_update_date";
    public static final String CREATED = "created";
    public static final String DETAIL_URL = "url";

    // ── API PARAMETERS ──
    // Query parameters sent in the GET request
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_PAGE_SIZE = "page_size";
    public static final String PARAM_FIELDS = "fields";
    public static final String PARAM_ORDERING = "ordering";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_UPDATED_AFTER = "updated_after";

    // ── UTILITY CONSTANTS ──
    public static final String UTF_8 = "UTF-8";
    public static final String TRUE = "true";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 500;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ── ERROR MESSAGES ──
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Udemy credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Udemy API";
    public static final String ERROR_TOKEN_FAILED = "Failed to generate Udemy access token";

    // ── CACHED AUTH PROPERTY ──
    // This key is used in Content.js cachedHeaderProperty to cache auth tokens
    // Format: com.saba.{connector}.{entity}.token
    public static final String CACHED_HEADER_PROPERTY = "com.saba.udemy.course.token";

    // ── LOGGING ──
    // Every class that logs should have its own prefix for grep-ability
    public static final String LOG_PREFIX = "[UdemyControl] ";
    public static final String LOG_AUTH_PREFIX = "[UdemyAuth] ";
    public static final String LOG_TEST_PREFIX = "[UdemyTestConnection] ";

    // ── INTEGRATION ID ──
    // Unique ID for this connector instance (used in VendorConstants.java patch)
    public static final String UDEMY = "integ0123456789abcdef0123456789abcdef";

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}