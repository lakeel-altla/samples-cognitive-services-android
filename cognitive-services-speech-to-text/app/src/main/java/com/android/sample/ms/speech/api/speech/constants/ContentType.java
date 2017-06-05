package com.android.sample.ms.speech.api.speech.constants;

public enum ContentType {
    APPLICATION_JSON("application/json; charset=utf-8"),
    AUDIO_WAV("audio/wav");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
