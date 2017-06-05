package com.android.sample.ms.speech.api.speech.constants;

import android.support.annotation.NonNull;

public enum Header {
    X_CONNECTION_ID("X-ConnectionId"),
    OCP_APIM_SUBSCRIPTION_KEY("Ocp-Apim-Subscription-Key"),
    PATH("Path"),
    X_REQUEST_ID("X-RequestId"),
    X_TIMESTAMP("X-Timestamp"),
    CONTENT_TYPE("Content-Type"),
    CRLF("\r\n");

    private final String value;

    Header(@NonNull String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
