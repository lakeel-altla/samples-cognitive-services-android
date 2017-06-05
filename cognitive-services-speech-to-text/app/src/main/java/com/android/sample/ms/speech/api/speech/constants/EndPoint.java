package com.android.sample.ms.speech.api.speech.constants;

public enum EndPoint {
    Interactive("/speech/recognition/interactive/cognitiveservices/v1"),
    Conversation("/speech/recognition/conversation/cognitiveservices/v1"),
    Dictation("/speech/recognition/dictation/cognitiveservices/v1");

    private final String value;

    EndPoint(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
