package com.moderation.api;

public class ApiConstants {

    private ApiConstants() {
    }

    public static final String MODERATION_CONTROLLER_PATH = "/api/moderation";

    public static final String MESSAGES_ENDPOINT_PATH = "/messages";

    public static final String RESULTS_ENDPOINT_PATH = "/results";

    public static final String MESSAGES_ENDPOINT_FULL_PATH = MODERATION_CONTROLLER_PATH + MESSAGES_ENDPOINT_PATH;

    public static final String RESULTS_ENDPOINT_FULL_PATH = MODERATION_CONTROLLER_PATH + RESULTS_ENDPOINT_PATH;

    public static final String MESSAGES_ENDPOINT_FILE_MULTIPART = "file";

}
