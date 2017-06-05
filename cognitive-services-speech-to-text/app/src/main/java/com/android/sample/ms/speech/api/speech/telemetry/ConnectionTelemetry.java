package com.android.sample.ms.speech.api.speech.telemetry;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.helper.CurrentTime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ConnectionTelemetry {

    private static final ConcurrentMap<String, ConnectionTelemetry> POOL = new ConcurrentHashMap<>();

    private String startConnectionTime;

    private String establishedConnectionTime;

    private String errorMessage;

    public void saveStartConnectionTime() {
        startConnectionTime = CurrentTime.newTime();
    }

    public void saveEstablishedConnectionTime() {
        establishedConnectionTime = CurrentTime.newTime();
    }

    public void saveErrorMessage(@NonNull String message) {
        errorMessage = message;
    }

    public String getStartConnectionTime() {
        return startConnectionTime;
    }

    public String getEstablishedConnectionTime() {
        return establishedConnectionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static ConnectionTelemetry forConnectionId(@NonNull String connectionId) {
        ConnectionTelemetry instance = POOL.get(connectionId);
        if (instance == null) {
            ConnectionTelemetry newInstance = new ConnectionTelemetry();
            instance = POOL.putIfAbsent(connectionId, newInstance);
            if (instance == null) {
                instance = newInstance;
            }
        }
        return instance;
    }
}
