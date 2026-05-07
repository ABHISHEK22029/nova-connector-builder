package com.cornerstoneondemand.connector.udemy;

public class UdemyConstants {

  /**
   * Configuration keys
   */
  public static final String CLIENT_ID = "client_id";
  public static final String CLIENT_SECRET = "client_SECRET";

  /**
   * API Endpoints
   */
  public static final String BASE_URL = "base_url";
  public static final String COURSES_ENDPOINT = "/courses/";
  public static final String USERS_ENDPOINT = "/users/";
  public static final String RELEVANT_COURSES_ENDPOINT = "/courses/relevant/";

  /**
   * Header names
   */
  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String CONTENT_TYPE_HEADER = "Content-Type";

  /**
   * Object Types
   */
  public static final String COURSE_OBJECT = "Course";
  public static final String USER_OBJECT = "User";

  /**
   * Pagination
   */
  public static final String PAGE_SIZE = "page_size";
  public static final String NEXT_PAGE = "next";

  /**
   * State management keys
   */
  public static final String COURSES_LAST_SYNC = "courses_last_sync";
  public static final String USERS_LAST_SYNC = "users_last_sync";

  /**
   * Content Type
   */
  public static final String APPLICATION_JSON = "application/json";

  /**
   * Authentication Type
   */
  public static final String BASIC_AUTH = "Basic";

  /**
   * Date Format
   */
  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

}