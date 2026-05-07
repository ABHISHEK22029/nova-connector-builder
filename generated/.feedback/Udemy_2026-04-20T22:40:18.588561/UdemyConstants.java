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
    public static final String NEXT = "next";

    // RESPONSE FIELDS - COURSES
    public static final String COURSE_ID = "id";
    public static final String COURSE_TITLE = "title";
    public static final String COURSE_URL = "url";
    public static final String COURSE_INSTRUCTOR = "visible_instructors";
    public static final String COURSE_CREATED = "created";
    public static final String COURSE_MODIFIED = "modified";

    // RESPONSE FIELDS - USERS
    public static final String USER_ID = "id";
    public static final String USER_TITLE = "title";
    public static final String USER_NAME = "name";
    public static final String USER_URL = "url";

    // RESPONSE FIELDS - REVIEWS
    public static final String REVIEW_ID = "id";
    public static final String REVIEW_CONTENT = "content";
    public static final String REVIEW_RATING = "rating";
    public static final String REVIEW_CREATED = "created";
    public static final String REVIEW_USER = "user";

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "UDEMY_LAST_SYNC";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";

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