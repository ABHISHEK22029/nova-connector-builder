/*
 * ============================================================================
 * GOLDEN REFERENCE: LinkedInLearningConstants.java — OAuth2 Pattern
 * ============================================================================
 * AUTH TYPE: OAuth2 Client Credentials
 * 
 * NOTE: This is an older connector with minimal constants.
 * New connectors SHOULD include more comprehensive constants following the
 * Kaltura pattern: LOG_PREFIX, CACHED_HEADER_PROPERTY, PREVIEW_RECORD_LIMIT, etc.
 * 
 * KEY PATTERN: TOKEN_URL and GRANT_TYPE are stored as constants here
 * but referenced in Content.js via "sabaconst:" expressions.
 * ============================================================================
 */
package com.saba.integration.apps.lil;

public class LinkedInLearningConstants {
    // Config keys matching admin UI accountConfigs
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String CLIENT_SECRET = "CLIENT_SECRET";
    public static final String CONSUMER_KEY = "CONSUMER_KEY";
    public static final String CONSUMER_SECRET = "CONSUMER_SECRET";

    // LTI/SSO integration
    public static final String DEFAULT_LTI_ACCOUNT = "DEFAULT_LTI_ACCOUNT";
    public static final String LINKEDIN_LEARNING_ACCOUNT_GROUP = "LINKEDIN_LEARNING_ACCOUNT_GROUP";
    public static final String LINKEDIN_REDIRECT_URL = "REDIRECT_URL";
    public static final String LTI_PROFILE_IDENTIFIER = "SABA_PROFILE_IDENTIFIER";

    // LinkedIn-specific headers
    public static final String REFERER_HEADER = "referer";
    public static final String REFERER_HEADER_VALUE_SABA = "urn:li:partner:saba";
    public static final String REFERER_HEADER_VALUE_CSX = "urn:li:partner:csx";
    public static final String LIL_REFERRER_HEADER = "linkedinRefererHeader";

    // OAuth2 endpoints
    public static final String TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken";
    public static final String LIL_GRANT_TYPE = "client_credentials";

    // Error handling
    public static final String ERROR_DESCRIPTION = "error_description";

    // Test connection endpoint (minimal data fetch)
    public static final String SSO_URL = "https://api.linkedin.com/v2/learningAssets?assetType=COURSE"
            + "&includeRetired=false&q=localeAndType"
            + "&fields=details:(urls:(ssoLaunch))"
            + "&sourceLocale.country=US&sourceLocale.language=en&start=1&count=1";
}
