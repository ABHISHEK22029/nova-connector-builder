/*
 * ============================================================================
 * GOLDEN REFERENCE: KalturaConstants.java
 * ============================================================================
 * AUTH TYPE: Session Token (KS)
 * CONNECTOR TYPE: Content Import (Media)
 * API STYLE: POST with form-urlencoded body
 * 
 * PATTERN RULES:
 * 1. ONE class, ZERO methods (except private constructor), ONLY public static final fields
 * 2. Group constants by PURPOSE with section comments
 * 3. Keys like CONFIG_* must EXACTLY MATCH the accountConfigs keys from the admin UI
 * 4. HEADER_* keys are internal framework headers passed between components
 * 5. All API URLs, paths, parameter names must be here — NEVER hardcoded elsewhere
 * 6. Include LOG_PREFIX for each class that logs (ComponentControl, Flows, TestConnection)
 * 7. Private constructor prevents instantiation
 * 8. PREVIEW_RECORD_LIMIT is always 10
 * ============================================================================
 */
package com.saba.integration.apps.kaltura;

public class KalturaConstants {

    // ── API CONFIGURATION ──
    // Base URL and path structure for the vendor API
    public static final String DEFAULT_BASE_URL = "https://www.kaltura.com";
    public static final String API_VERSION = "api_v3";
    public static final String SERVICE_PREFIX = "/service/";
    public static final String API_BASE_PATH = "/" + API_VERSION + SERVICE_PREFIX;

    // ── STATE MANAGEMENT ──
    // Keys used to track sync timestamps between runs (delta sync)
    public static final String LAST_SYNC_TIMESTAMP = "KALTURA_LAST_SYNC";
    public static final String MEDIA_LAST_UPDATED = "KALTURA_MEDIA_LAST_UPDATED";
    public static final String USER_LAST_UPDATED = "KALTURA_USER_LAST_UPDATED";
    public static final int DEFAULT_LOOKBACK_DAYS = 7;
    public static final int SAFETY_MARGIN_SECONDS = 30;

    // ── HEADER KEYS ──
    // Internal message headers used to pass data between flow components
    // These are NOT HTTP headers — they're Spring Integration message headers
    public static final String HEADER_API_URL = "KALTURA_API_URL";
    public static final String HEADER_UPDATED_FROM = "KALTURA_UPDATED_FROM";
    public static final String HEADER_UPDATED_TO = "KALTURA_UPDATED_TO";
    public static final String HEADER_SESSION_TOKEN = "KALTURA_SESSION_TOKEN";
    public static final String HEADER_OBJECT_TYPE = "KALTURA_OBJECT_TYPE";
    public static final String HEADER_IS_PREVIEW = "isPreview";
    public static final String HEADER_INTEGRATION_MONITORING_ID = "INTEGRATION_MONITORING_ID";

    // ── CONFIGURATION KEYS ──
    // These MUST match the keys sent by the Nova admin UI in accountConfigs
    // If the UI sends accountConfigs['SERVICE_URL'], this must be "SERVICE_URL"
    public static final String CONFIG_BASE_URL = "SERVICE_URL";
    public static final String CONFIG_PARTNER_ID = "PARTNER_ID";
    public static final String CONFIG_ADMIN_SECRET = "SECRET";
    public static final String CONFIG_SESSION_TYPE = "SESSION_TYPE";
    public static final String CONFIG_UPDATE_FROM = "UPDATE_FROM";
    public static final String CONFIG_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String CONFIG_PAGE_SIZE = "PAGE_SIZE";
    public static final String CONFIG_MAX_ITEMS = "MAX_ITEMS_TO_PROCESS";

    // ── OBJECT TYPES ──
    // Used to distinguish entity types within the connector
    public static final String OBJECT_MEDIA = "media";
    public static final String OBJECT_USER = "user";
    public static final String OBJECT_CATEGORY = "category";

    // ── API ACTIONS ──
    public static final String ACTION_LIST = "action/list";
    public static final String ACTION_START_SESSION = "action/startSession";

    // ── FILTER OBJECTS ──
    public static final String FILTER_MEDIA_ENTRY = "KalturaMediaEntryFilter";

    // ── RESPONSE FIELDS ──
    // JSON keys from the vendor API response
    public static final String OBJECTS = "objects";
    public static final String TOTAL_COUNT = "totalCount";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String UPDATED_AT = "updatedAt";
    public static final String CREATED_AT = "createdAt";

    // ── API PARAMETERS ──
    // Form parameters sent in the POST body
    public static final String PARAM_KS = "ks";
    public static final String PARAM_FORMAT = "format";
    public static final String PARAM_SERVICE = "service";
    public static final String PARAM_ACTION = "action";
    public static final String PAGER_PAGE_INDEX = "pager[pageIndex]";
    public static final String PAGER_PAGE_SIZE = "pager[pageSize]";

    // ── STATUS VALUES ──
    public static final int STATUS_READY = 2;
    public static final int USER_STATUS_ACTIVE = 1;

    // ── ORDER BY ──
    public static final String ORDER_BY_UPDATED_AT_ASC = "+updatedAt";

    // ── AUTHENTICATION ──
    public static final int SESSION_TYPE_USER = 0;
    public static final int SESSION_TYPE_ADMIN = 2;
    public static final int DEFAULT_SESSION_EXPIRY = 86400;

    // ── UTILITY CONSTANTS ──
    public static final String UTF_8 = "UTF-8";
    public static final int JSON_FORMAT = 1;
    public static final String TRUE = "true";
    public static final int PREVIEW_RECORD_LIMIT = 10;
    public static final int DEFAULT_PAGE_SIZE_INT = 500;
    public static final int DEFAULT_MAX_ITEMS_INT = 100000;
    public static final int DEFAULT_MAX_SECONDS_INT = 1800;
    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_DAY = 86400L;

    // ── ERROR MESSAGES ──
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid Kaltura credentials";
    public static final String ERROR_CONNECTION_FAILED = "Failed to connect to Kaltura API";
    public static final String ERROR_SESSION_FAILED = "Failed to generate Kaltura session";

    // ── CACHED AUTH PROPERTY ──
    // This key is used in Content.js cachedHeaderProperty to cache auth tokens
    // Format: com.saba.{connector}.{entity}.token
    public static final String CACHED_HEADER_PROPERTY = "com.saba.kaltura.media.token";
    public static final String SESSION_URL_PATH = "/api_v3/service/session/action/start";
    public static final String HEADER_API_PATH = "KALTURA_API_PATH";

    // ── LOGGING ──
    // Every class that logs should have its own prefix for grep-ability
    public static final String LOG_PREFIX = "[KalturaControl] ";
    public static final String LOG_AUTH_PREFIX = "[KalturaSessionAuth] ";
    public static final String LOG_TEST_PREFIX = "[KalturaTestConnection] ";

    private KalturaConstants() {
        throw new UnsupportedOperationException("Constants class");
    }
}
