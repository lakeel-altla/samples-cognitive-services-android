package com.android.sample.ms.speech.api.speech.telemetry;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.helper.CurrentTime;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public final class TimestampTelemetry {

    private static final ConcurrentMap<String, TimestampTelemetry> POOL = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Queue<String>> timestampMap = new ConcurrentHashMap<>();

    private TimestampTelemetry() {
    }

    public void saveTimestamp(@NonNull String path) {
        Queue<String> timestamps = timestampMap.get(path);
        if (timestamps == null) {
            Queue<String> newTimestamps = new ConcurrentLinkedQueue<>();
            timestamps = timestampMap.putIfAbsent(path, newTimestamps);
            if (timestamps == null) {
                timestamps = newTimestamps;
            }
        }
        timestamps.add(CurrentTime.newTime());
    }

    public static TimestampTelemetry forRequestId(@NonNull String requestId) {
        TimestampTelemetry instance = POOL.get(requestId);
        if (instance == null) {
            TimestampTelemetry newInstance = new TimestampTelemetry();
            instance = POOL.putIfAbsent(requestId, newInstance);
            if (instance == null) {
                instance = newInstance;
            }
        }
        return instance;
    }

    public ConcurrentMap<String, Queue<String>> getTimestampMap() {
        return timestampMap;
    }
}
