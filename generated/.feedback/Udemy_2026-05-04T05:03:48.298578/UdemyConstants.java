package com.saba.integration.apps.udemy;

public class UdemyConstants {

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }

    // --- Integration Details ---
    /**
     * Unique integration ID for Udemy.
     * This ID is registered in VendorConstants.java in the marketplace module.
     */
    public static final String INTEGRATION_ID = "intege1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6";

    // --- Logging Prefixes ---
    public static final String LOG_PREFIX_CONTROL = "[UdemyControl] ";
    public static final String LOG_PREFIX_AUTH = "[UdemyAuth] ";
    public static final String LOG_PREFIX_TEST_CONNECTION = "[UdemyTestConnection] ";

    // --- Configuration Keys (must match account configuration keys in the UI) ---
    public static final String CONFIG_CLIENT_ID = "CLIENT_ID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENT_SECRET";
    public static final String CONFIG_BASE_URL = "BASE_URL"; // e.g., https://business.udemy.com/api-2.0/

    // --- API Endpoints ---
    public static final String API_TOKEN_PATH = "/oauth/token/";
    public static final String API_COURSES_PATH = "/courses/";
    public static final String API_COURSE_ENROLLMENTS_PATH = "/course-enrollments/";
    public static final String API_USERS_PATH = "/users/";

    // --- HTTP Headers ---
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";

    // --- Content Types ---
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    // --- OAuth 2.0 Grant Types ---
    public static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";

    // --- State Management Keys (for caching tokens, etc.) ---
    public static final String STATE_ACCESS_TOKEN = "access_token";
    public static final String STATE_TOKEN_EXPIRATION_SECONDS = "expires_in";
    public static final String STATE_TOKEN_EXPIRATION_TIMESTAMP = "expires_at"; // Calculated timestamp
    public static final String CACHED_HEADER_PROPERTY = "com.saba.udemy.auth.token";

    // --- Pagination & Preview ---
    public static final int DEFAULT_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_NUMBER = 1;
    public static final int PREVIEW_RECORD_LIMIT = 10; // Max records for preview mode

    // --- Object Types for Content Flows ---
    public static final String OBJECT_TYPE_COURSE = "course";
    public static final String OBJECT_TYPE_COURSE_ENROLLMENT = "course_enrollment";
    public static final String OBJECT_TYPE_USER = "user";

    // --- Query Parameters ---
    public static final String QUERY_PARAM_PAGE = "page";
    public static final String QUERY_PARAM_PAGE_SIZE = "page_size";
    public static final String QUERY_PARAM_FIELDS = "fields";
    public static final String QUERY_PARAM_CREATED_AFTER = "created__gt";
    public static final String QUERY_PARAM_MODIFIED_AFTER = "modified__gt";
    public static final String QUERY_PARAM_IS_ACTIVE = "is_active";
}