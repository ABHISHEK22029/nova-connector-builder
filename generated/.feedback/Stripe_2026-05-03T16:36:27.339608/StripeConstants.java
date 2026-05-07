package com.saba.integration.apps.stripe;

public class StripeConstants {

    // ── API CONFIGURATION ──
    // Base URL for the Stripe API
    public static final String DEFAULT_BASE_URL = "https://api.stripe.com/v1";

    // ── STATE MANAGEMENT ──
    // Keys used to track sync timestamps between runs (delta sync)
    public static final String LAST_SYNC_TIMESTAMP = "STRIPE_LAST_SYNC";
    public static final String CUSTOMER_LAST_UPDATED = "STRIPE_CUSTOMER_LAST_UPDATED";
    public static final String INVOICE_LAST_UPDATED = "STRIPE_INVOICE_LAST_UPDATED";
    public static final String SUBSCRIPTION_LAST_UPDATED = "STRIPE_SUBSCRIPTION_LAST_UPDATED";
    public static final String PRODUCT_LAST_UPDATED = "STRIPE_PRODUCT_LAST_UPDATED";
    public static final String PRICE_LAST_UPDATED = "STRIPE_PRICE_LAST_UPDATED";
    public static final String EVENT_LAST_UPDATED = "STRIPE_EVENT_LAST_UPDATED"; // For event-based delta sync
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30; // To account for API eventual consistency

    // ── HEADER KEYS ──
    // Internal message headers used to pass data between flow components
    // These are NOT HTTP headers — they're Spring Integration message headers
    public static final String HEADER_API_URL = "STRIPE_API_URL";
    public static final String HEADER_OBJECT_TYPE = "STRIPE_OBJECT_TYPE";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";
    public static final String HEADER_UPDATED_FROM = "STRIPE_UPDATED_FROM"; // For delta sync
    public static final String HEADER_UPDATED_TO = "STRIPE_UPDATED_TO";     // For delta sync
    public static final String HEADER_STRIPE_API_KEY = "STRIPE_API_KEY_INTERNAL"; // To pass API key internally

    // ── CONFIGURATION KEYS ──
    // These MUST match the keys sent by the Nova admin UI in accountConfigs
    // If the UI sends accountConfigs['BASE_URL'], this must be "BASE_URL"
    public static final String CONFIG_BASE_URL = "BASE_URL";
    public static final String CONFIG_API_KEY = "API_KEY";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";
    public static final String CONFIG_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String CONFIG_UPDATE_FROM = "UPDATE_FROM"; // For delta sync lookback

    // ── OBJECT TYPES ──
    // Used to distinguish entity types within the connector
    public static final String OBJECT_CUSTOMER = "customer";
    public static final String OBJECT_INVOICE = "invoice";
    public static final String OBJECT_SUBSCRIPTION = "subscription";
    public static final String OBJECT_PRODUCT = "product";
    public static final String OBJECT_PRICE = "price";
    public static final String OBJECT_EVENT = "event";

    // ── API ENDPOINTS / PATHS ──
    public static final String PATH_CUSTOMERS = "/customers";
    public static final String PATH_INVOICES = "/invoices";
    public static final String PATH_SUBSCRIPTIONS = "/subscriptions";
    public static final String PATH_PRODUCTS = "/products";
    public static final String PATH_PRICES = "/prices";
    public static final String PATH_EVENTS = "/events";

    // ── API PARAMETERS ──
    // Query parameters sent in the GET request
    public static final String PARAM_LIMIT = "limit"; // For pagination page size
    public static final String PARAM_STARTING_AFTER = "starting_after"; // For cursor-based pagination
    public static final String PARAM_CREATED_GT = "created[gt]"; // Greater than
    public static final String PARAM_CREATED_GTE = "created[gte]"; // Greater than or equal
    public static final String PARAM_CREATED_LT = "created[lt]"; // Less than
    public static final String PARAM_CREATED_LTE = "created[lte]"; // Less than or equal
    public static final String PARAM_EXPAND = "expand[]"; // To expand related objects
    public static final String PARAM_TYPE = "type"; // For filtering events

    // ── RESPONSE FIELDS ──
    // JSON keys from the vendor API response
    public static final String DATA = "data";
    public static final String HAS_MORE = "has_more";
    public static final String ID = "id";
    public static final String OBJECT = "object";
    public static final String CREATED = "created"; // Timestamp in seconds
    public static final String UPDATED = "updated"; // Stripe often uses 'created' for last update, or specific fields like 'updated' on some objects.
    public static final String LIVE_MODE = "livemode"; // boolean indicating live or test mode

    // ── AUTHENTICATION ──
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String AUTH_SCHEME_BEARER = "Bearer";

    // ── UTILITY CONSTANTS ──
    public static final String UTF_8 = "UTF-8";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 100;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ── ERROR MESSAGES ──
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Stripe API Key provided.";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Stripe API.";
    public static final String ERROR_AUTH_FAILED = "Stripe authentication failed.";

    // ── CACHED AUTH PROPERTY ──
    // This key is used in Content.js cachedHeaderProperty to cache auth tokens
    // Format: com.saba.{connector}.{entity}.token
    public static final String CACHED_HEADER_PROPERTY = "com.saba.stripe.customer.token";

    // ── LOGGING ──
    // Every class that logs should have its own prefix for grep-ability
    public static final String LOG_PREFIX = "[StripeControl] ";
    public static final String LOG_AUTH_PREFIX = "[StripeAuth] ";
    public static final String LOG_TEST_PREFIX = "[StripeTestConnection] ";

    private StripeConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}