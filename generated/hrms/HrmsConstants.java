package com.saba.integration.apps.hrms;

public class HrmsConstants {

    // API CONFIGURATION
    public static final String DEFAULT_BASE_URL = "https://api.example.com"; // Replace with actual default base URL

    // CONFIGURATION KEYS
    public static final String API_KEY = "API_KEY";
    public static final String COMPANY_ID = "COMPANY_ID";

    // OBJECT TYPES
    public static final String OBJECT_EMPLOYEE = "employee";
    public static final String OBJECT_DEPARTMENT = "department";

    // API ENDPOINTS
    public static final String EMPLOYEE_ENDPOINT = "/employees";
    public static final String DEPARTMENT_ENDPOINT = "/departments";

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "HRMS_LAST_SYNC";

    // RESPONSE FIELDS
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String DEPARTMENT_ID = "departmentId";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid HRMS credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to HRMS API";

    // LOGGING
    public static final String LOG_PREFIX = "[HrmsComponentControl] ";
    public static final String LOG_TEST_PREFIX = "[HrmsTestConnection] ";

    private HrmsConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}