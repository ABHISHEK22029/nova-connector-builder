package com.saba.integration.apps.stripe;

public class StripeConstants {

    // API CONFIGURATION
    public static final String DEFAULT_BASE_URL = "https://api.stripe.com/v1";

    // STATE MANAGEMENT
    public static final String LAST_SYNC_TIMESTAMP = "STRIPE_LAST_SYNC";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // HEADER KEYS
    public static final String HEADER_API_URL = "STRIPE_API_URL";
    public static final String HEADER_STRIPE_VERSION = "Stripe-Version";
    public static final String STRIPE_VERSION = "2023-10-16";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_OBJECT_TYPE = "STRIPE_OBJECT_TYPE";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // CONFIGURATION KEYS
    public static final String CONFIG_SECRET_KEY = "SECRET_KEY";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // OBJECT TYPES
    public static final String OBJECT_CUSTOMER = "customer";
    public static final String OBJECT_PAYMENT_INTENT = "payment_intent";
    public static final String OBJECT_SUBSCRIPTION = "subscription";

    // API ACTIONS
    public static final String ACTION_LIST = ""; // Stripe uses GET parameters for listing

    // RESPONSE FIELDS
    public static final String DATA = "data";
    public static final String HAS_MORE = "has_more";
    public static final String ID = "id";
    public static final String OBJECT = "object";
    public static final String CREATED = "created";

    // API PARAMETERS
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_STARTING_AFTER = "starting_after";
    public static final String PARAM_ENDING_BEFORE = "ending_before";
    public static final String PARAM_CREATED = "created";

    // UTILITY CONSTANTS
    public static final String UTF_8 = "UTF-8";
    public static final int DEFAULT_PAGE_SIZE_INT = 100;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_MAX_SECONDS_INT = 1800;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ERROR MESSAGES
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Stripe credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Stripe API";

    // LOGGING
    public static final String LOG_PREFIX = "[StripeComponentControl] ";
    public static final String LOG_TEST_PREFIX = "[StripeTestConnection] ";

    private StripeConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}