package com.android.sample.ms.speech.api.speech.telemetry;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.helper.CurrentTime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class MicrophoneTelemetry {

    private static final ConcurrentMap<String, MicrophoneTelemetry> POOL = new ConcurrentHashMap<>();

    private String startRecordTime;

    private String endRecordTime;

    private String errorMessage;

    public void saveStartRecordTime() {
        this.startRecordTime = CurrentTime.newTime();
    }

    public void saveEndRecordTime() {
        this.endRecordTime = CurrentTime.newTime();
    }

    public void saveErrorMessage(@NonNull String message) {
        this.errorMessage = message;
    }

    public String getStartRecordTime() {
        return startRecordTime;
    }

    public String getEndRecordTime() {
        return endRecordTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static MicrophoneTelemetry forRequestId(@NonNull String requestId) {
        MicrophoneTelemetry instance = POOL.get(requestId);
        if (instance == null) {
            MicrophoneTelemetry newInstance = new MicrophoneTelemetry();
            instance = POOL.putIfAbsent(requestId, newInstance);
            if (instance == null) {
                instance = newInstance;
            }
        }
        return instance;
    }
}
