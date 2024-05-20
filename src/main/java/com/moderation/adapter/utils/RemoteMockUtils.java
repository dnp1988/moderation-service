package com.moderation.adapter.utils;

public class RemoteMockUtils {

    private static final String TRANSLATED_PREFIX = "translated ";

    public static Double getScoringValue(String messageText) {
        Double value = 1.0 * Math.abs(messageText.hashCode()) / Integer.MAX_VALUE;
        return Math.floor(value * 1000000) / 1000000;
    }

    public static String getTranslatedMessage(String messageText) {
        return TRANSLATED_PREFIX + messageText;
    }

    public static Long getRandomResponseDelayInMillis() {
        return (long) ((Math.random() * (200 - 50)) + 50);
    }
}
