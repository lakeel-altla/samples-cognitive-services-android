package com.android.sample.ms.speech.api.speech.constants;

import android.support.annotation.NonNull;

public enum Path {
    UNKNOWN("unknown"),
    SPEECH_CONFIG("speech.config"),
    AUDIO("audio"),
    TELEMETRY("telemetry"),
    SPEECH_HYPOTHESIS("speech.hypothesis"),
    SPEECH_PHRASE("speech.phrase"),
    SPEECH_START_DETECTED("speech.startDetected"),
    SPEECH_END_DETECTED("speech.endDetected"),
    TURN_START("turn.start"),
    TURN_END("turn.end");

    private final String value;

    Path(@NonNull String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Path toPath(String path) {
        for (Path value : Path.values()) {
            if (value.getValue().equals(path)) {
                return value;
            }
        }
        return UNKNOWN;
    }
}
