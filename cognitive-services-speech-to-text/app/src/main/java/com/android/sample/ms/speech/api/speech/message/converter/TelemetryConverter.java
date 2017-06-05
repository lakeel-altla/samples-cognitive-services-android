package com.android.sample.ms.speech.api.speech.message.converter;

import android.support.annotation.NonNull;

import com.android.sample.ms.speech.api.speech.helper.CurrentTime;
import com.android.sample.ms.speech.api.speech.helper.JsonMapper;
import com.android.sample.ms.speech.api.speech.message.Telemetry;
import com.android.sample.ms.speech.api.speech.message.Telemetry.Metrics;
import com.android.sample.ms.speech.api.speech.telemetry.ConnectionTelemetry;
import com.android.sample.ms.speech.api.speech.telemetry.MicrophoneTelemetry;
import com.android.sample.ms.speech.api.speech.telemetry.TimestampTelemetry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import static com.android.sample.ms.speech.api.speech.constants.ContentType.APPLICATION_JSON;
import static com.android.sample.ms.speech.api.speech.constants.Header.CONTENT_TYPE;
import static com.android.sample.ms.speech.api.speech.constants.Header.CRLF;
import static com.android.sample.ms.speech.api.speech.constants.Header.PATH;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_REQUEST_ID;
import static com.android.sample.ms.speech.api.speech.constants.Header.X_TIMESTAMP;
import static com.android.sample.ms.speech.api.speech.constants.Path.TELEMETRY;

public final class TelemetryConverter {

    private TelemetryConverter() {
    }

    public static String convert(@NonNull TimestampTelemetry timestampTelemetry,
                                 @NonNull ConnectionTelemetry connectionTelemetry,
                                 @NonNull MicrophoneTelemetry microphoneTelemetry,
                                 @NonNull String requestId,
                                 @NonNull String connectionId) {
        Metrics connectionMetrics = new Metrics();
        connectionMetrics.Name = "Connection";
        connectionMetrics.Id = connectionId;
        connectionMetrics.Start = connectionTelemetry.getStartConnectionTime();
        connectionMetrics.End = connectionTelemetry.getEstablishedConnectionTime();

        Metrics microphoneMetrics = new Metrics();
        microphoneMetrics.Name = "Microphone";
        microphoneMetrics.Start = microphoneTelemetry.getStartRecordTime();
        microphoneMetrics.End = microphoneTelemetry.getEndRecordTime();

        List<Metrics> metricsList = new ArrayList<>(1);
        metricsList.add(connectionMetrics);
        metricsList.add(microphoneMetrics);

        List<Map<String, Queue<String>>> ReceivedMessages = new ArrayList<>();

        Map<String, Queue<String>> map = timestampTelemetry.getTimestampMap();
        for (Map.Entry<String, Queue<String>> entry : map.entrySet()) {
            Map<String, Queue<String>> newMap = new HashMap<>();
            newMap.put(entry.getKey(), entry.getValue());
            ReceivedMessages.add(newMap);
        }

        Telemetry message = new Telemetry();
        message.ReceivedMessages = ReceivedMessages;
        message.Metrics = metricsList;

        String json = JsonMapper.toJson(message);

        return PATH.getValue() + ": " + TELEMETRY.getValue() + CRLF.getValue() +
                X_REQUEST_ID.getValue() + ": " + requestId + CRLF.getValue() +
                CONTENT_TYPE.getValue() + ": " + APPLICATION_JSON.getValue() + CRLF.getValue() +
                X_TIMESTAMP.getValue() + ": " + CurrentTime.newTime() + CRLF.getValue() + CRLF.getValue() +
                json;
    }
}
