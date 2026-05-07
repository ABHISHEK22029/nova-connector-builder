package com.saba.integration.apps.udemy;

public class UdemyConstants {

    // API
    public static final String API_COURSES_PATH = "/api-2.0/courses/";
    public static final String API_REPORTS_COURSES_PATH = "/api-2.0/reports/courses/";
    public static final String API_REPORTS_USERS_PATH = "/api-2.0/reports/users/";
    public static final String API_USERS_PATH = "/api-2.0/users/";
    public static final String API_REVIEWS_PATH = "/api-2.0/reviews/";
    public static final String API_INSTRUCTORS_PATH = "/api-2.0/instructors/";

    // Headers
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String ACCEPT_JSON = "application/json";
    public static final String HEADER_UDEMY_API_URL = "UDEMY_API_URL";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // Config
    public static final String CONFIG_CLIENT_ID = "CLIENT_ID";
    public static final String CONFIG_CLIENT_SECRET = "CLIENT_SECRET";
    public static final String CONFIG_BASE_URL = "BASE_URL";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // Object Types
    public static final String OBJECT_COURSE = "course";
    public static final String OBJECT_USER = "user";

    // Response Fields
    public static final String RESULTS = "results";
    public static final String NEXT = "next";
    public static final String COUNT = "count";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String NAME = "name";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";

    // Pagination
    public static final String PAGE = "page";
    public static final String PAGE_SIZE = "page_size";

    // Auth
    public static final String AUTH_BASIC = "Basic ";

    // Utility
    public static final String UTF_8 = "UTF-8";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 100;
    public static final int DEFAULT_MAX_ITEMS_INT = 10000;
    public static final long MILLIS_PER_SECOND = 1000L;

    // Error Messages
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Udemy credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Udemy API";

    // Cached Auth Property
    public static final String CACHED_HEADER_PROPERTY = "com.saba.udemy.course.token";

    // Logging
    public static final String LOG_PREFIX = "[UdemyControl] ";
    public static final String LOG_AUTH_PREFIX = "[UdemyAuth] ";
    public static final String LOG_TEST_PREFIX = "[UdemyTestConnection] ";

    // Integration ID
    public static final String UDEMY = "integ/mpent/minea3b304989b39448984a941b3927419c";

    private UdemyConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}