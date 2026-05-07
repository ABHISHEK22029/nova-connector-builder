package com.saba.integration.apps.zoom;

public class ZoomConstants {

    public static final String ZOOM_BASE_URL = "https://api.zoom.us";
    public static final String AUTHENTICATION_URL = "https://zoom.us/oauth/token";
    public static final String AUTHORIZATION="Authorization";

    public static final String AUTH_CODE = "AUTH_CODE";
    public static final String ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";
    public static final String CLIENT_SECRET = "CLIENT_SECRET";
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String REDIRECT_URL = "REDIRECT_URL";

//    public static final String HOST_VIDEO = "HOST_VIDEO";
//    public static final String PARTICIPANT_VIDEO = "PARTICIPANT_VIDEO";
//    public static final String JOIN_BEFORE_HOST = "JOIN_BEFORE_HOST" ;
//    public static final String REGISTRANTS_EMAIL_NOTIFY = "REG_EMAIL_NOTIFY" ;
//    public static final String MUTE_UPON_ENTRY = "MUTE_UPON_ENTRY" ;

    public static final String ZOOM_ACCOUNT_ID = "ZOOM_ACCOUNT_ID";

    public static final String DEFAULT_USER = "DEFAULT_USER";
    public static final String WEBINAR_DEFAULT_USER = "WEBINAR_DEFAULT_USER";
    public static final String VOIP_AUDIO="voip";
    public static final String TELEPHONY_AUDIO="telephony";
    public static final String BOTH_AUDIO="both";

    public static final Integer SCHEDULED_MEETING = 2;
    public static final Integer APPROVAL_TYPE = 0;
    public static final Integer SCHEDULED_WEBINAR = 5;
    public static final long TO_MILLI=1000;

    public static final String TEMPLATE_NAME="TEMPLATE_NAME";
    public static final String TEMPLATE_ID="TEMPLATE_ID";
    public static final String AUTO_GENERATE_PASSWORD="AUTO_GEN_PASSWORD";
    public static final String MEETING_NOT_FOUND = "Zoom meeting not found";
    public static final String USER_NOT_FOUND="User not found";
    public static final String MEETING_HOST="MEETING_HOST";
    public static final String WEBINAR_HOST="WEBINAR_HOST";
    public static final String ZOOM_MEETING_UUID="ZOOM_MEETING_UUID";
    public static final String ZOOM_WEBINAR_UUID="ZOOM_WEBINAR_UUID";
    public static final String DOUBLE_ENCODE="DOUBLE_ENCODE";
    public static final String ENCODE_SLASH="ENCODE_SLASH";

    public static final String GET_URL_ACTION="getUrl";
    public static final String DELETE_REGISTRATION="delete";
    public static final String USER_ME = "me";
    public static final String USER_ACCESS_TOKEN = "access_token";
    public static final String SC_ZOOM_INTEG_ID = "SC_ZOOM_INTEG_ID";
}