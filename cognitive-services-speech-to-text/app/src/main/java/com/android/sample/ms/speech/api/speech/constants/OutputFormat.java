package com.android.sample.ms.speech.api.speech.constants;

import android.support.annotation.NonNull;

public enum OutputFormat {
    SIMPLE("simple"),
    DETAILED("detailed");

    private final String value;

    OutputFormat(@NonNull String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
